package com.github.jshaptic.minimatch;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 * Brace expansion, as known from sh/bash, in Java.
 *
 * @version based on the javascript library <b>juliangruber/brace-expansion v1.1.11</b>
 */
public class BraceExpansion {

  private static final String escSlash = "\0SLASH" + Math.random() + "\0";
  private static final String escOpen = "\0OPEN" + Math.random() + "\0";
  private static final String escClose = "\0CLOSE" + Math.random() + "\0";
  private static final String escComma = "\0COMMA" + Math.random() + "\0";
  private static final String escPeriod = "\0PERIOD" + Math.random() + "\0";

  private static final Pattern numericSequence = Pattern.compile("^-?\\d+\\.\\.-?\\d+(?:\\.\\.-?\\d+)?$");
  private static final Pattern alphaSequence = Pattern.compile("^[a-zA-Z]\\.\\.[a-zA-Z](?:\\.\\.-?\\d+)?$");

  private BraceExpansion() {}

  private static long numeric(String str) {
    try {
      return Long.parseLong(str, 10);
    } catch (NumberFormatException e) {
      return str.codePointAt(0);
    }
  }

  private static String escapeBraces(String str) {
    return StringUtils.replaceEach(str, new String[] {"\\\\", "\\{", "\\}", "\\,", "\\."},
        new String[] {escSlash, escOpen, escClose, escComma, escPeriod});
  }

  private static String unescapeBraces(String str) {
    return StringUtils.replaceEach(str, new String[] {escSlash, escOpen, escClose, escComma, escPeriod},
        new String[] {"\\", "{", "}", ",", "."});
  }

  // Basically just str.split(","), but handling cases
  // where we have nested braced sections, which should be
  // treated as individual members, like {a,{b,c},d}
  private static String[] parseCommaParts(String str) {
    if (str == null || str.isEmpty()) {
      return new String[] {""};
    }

    BalancedMatch m = BalancedMatch.balanced("{", "}", str);

    if (m == null) {
      return str.split(",", -1);
    }

    String pre = m.getPre();
    String body = m.getBody();
    String post = m.getPost();
    String[] p = pre.split(",", -1);

    p[p.length - 1] += "{" + body + "}";
    String[] postParts = parseCommaParts(post);
    if (post.length() > 0 && postParts.length > 0) {
      p[p.length - 1] += postParts[0];
      p = Arrays.copyOf(p, p.length + postParts.length - 1);
      System.arraycopy(postParts, 1, p, p.length - (postParts.length - 1), postParts.length - 1);
    }

    return p;
  }

  private static String embrace(String str) {
    return "{" + str + "}";
  }

  private static boolean isPadded(String el) {
    return (el.length() >= 3 && el.startsWith("-0") && Character.isDigit(el.charAt(2)))
        || (el.length() >= 2 && el.startsWith("0") && Character.isDigit(el.charAt(1)));
  }

  private static boolean lte(long i, long y) {
    return i <= y;
  }

  private static boolean gte(long i, long y) {
    return i >= y;
  }

  /**
   * Return an array of all possible and valid expansions of {@code str}. If none are found, {@code [str]} is returned.
   *
   * @param str string to expand.
   * @return array of valid expansions of {@code str}, if none are found, {@code [str]} is returned.
   */
  public static String[] expand(String str) {
    if (str == null || str.isEmpty()) {
      return new String[0];
    }

    // I don't know why Bash 4.3 does this, but it does.
    // Anything starting with {} will have the first two bytes preserved
    // but *only* at the top level, so {},a}b will not expand to anything,
    // but a{},b}c will be expanded to [a}c,abc].
    // One could argue that this is a bug in Bash, but since the goal of
    // this module is to match Bash's rules, we escape a leading {}
    if (str.startsWith("{}")) {
      str = "\\{\\}" + str.substring(2);
    }

    return Arrays.stream(expand(escapeBraces(str), true)).map(BraceExpansion::unescapeBraces).toArray(String[]::new);
  }

  private static String[] expand(String str, boolean isTop) {
    BalancedMatch m = BalancedMatch.balanced("{", "}", str);
    if (m == null || m.getPre().endsWith("$")) {
      return new String[] {str};
    }

    boolean isNumericSequence = numericSequence.matcher(m.getBody()).find();
    boolean isAlphaSequence = alphaSequence.matcher(m.getBody()).find();
    boolean isSequence = isNumericSequence || isAlphaSequence;
    boolean isOptions = m.getBody().indexOf(',') >= 0;
    if (!isSequence && !isOptions) {
      // {a},b}
      if (m.getPost().indexOf(',') >= 0 && m.getPost().indexOf('}') >= 0
          && m.getPost().indexOf(',') < m.getPost().indexOf('}')) {
        str = m.getPre() + "{" + m.getBody() + escClose + m.getPost();
        return expand(str, false);
      }
      return new String[] {str};
    }

    String[] n;
    if (isSequence) {
      n = StringUtils.split(m.getBody(), "..");
    } else {
      n = parseCommaParts(m.getBody());
      if (n.length == 1) {
        // x{{a,b}}y ==> x{a}y x{b}y
        n = Arrays.stream(expand(n[0], false)).map(BraceExpansion::embrace).toArray(String[]::new);
        if (n.length == 1) {
          String n0 = n[0];
          String[] post = !m.getPost().isEmpty() ? expand(m.getPost(), false) : new String[] {""};
          return Arrays.stream(post).map(p -> m.getPre() + n0 + p).toArray(String[]::new);
        }
      }
    }

    // at this point, n is the parts, and we know it's not a comma set
    // with a single entry.

    // no need to expand pre, since it is guaranteed to be free of brace-sets
    String pre = m.getPre();
    String[] post = !m.getPost().isEmpty() ? expand(m.getPost(), false) : new String[] {""};

    List<String> nn;

    if (isSequence) {
      long x = numeric(n[0]);
      long y = numeric(n[1]);
      int width = Math.max(n[0].length(), n[1].length());
      long incr = n.length == 3 ? Math.abs(numeric(n[2])) : 1;
      BiPredicate<Long, Long> test = BraceExpansion::lte;
      boolean reverse = y < x;
      if (reverse) {
        incr *= -1;
        test = BraceExpansion::gte;
      }
      boolean pad = Arrays.stream(n).anyMatch(BraceExpansion::isPadded);

      nn = new ArrayList<>();

      for (long i = x; test.test(i, y); i += incr) {
        String c = "";
        if (isAlphaSequence) {
          c = Character.toString((char) i);
          if (c.equals("\\")) {
            c = "";
          }
        } else {
          c = Long.toString(i);
          if (pad) {
            int need = width - c.length();
            if (need > 0) {
              String z = String.join("", Collections.nCopies(need, "0"));
              if (i < 0) {
                c = "-" + z + c.substring(1);
              } else {
                c = z + c;
              }
            }
          }
        }
        nn.add(c);
      }
    } else {
      nn = Arrays.stream(n).flatMap(el -> Arrays.stream(expand(el, false))).collect(toList());
    }

    List<String> expansions = new ArrayList<>();

    for (int j = 0; j < nn.size(); j++) {
      for (int k = 0; k < post.length; k++) {
        String expansion = pre + nn.get(j) + post[k];
        if (!isTop || isSequence || !expansion.isEmpty()) {
          expansions.add(expansion);
        }
      }
    }

    return expansions.toArray(new String[] {});
  }

}
