package com.github.jshaptic.minimatch;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Match balanced string pairs, like { and } or &lt; b &gt; and &lt; /b &gt;. Supports regular expressions as well!
 *
 * @version based on the javascript library <b>juliangruber/balanced-match v1.0.2</b>
 */
public class BalancedMatch {

  private int start;
  private int end;
  private String pre;
  private String body;
  private String post;

  private BalancedMatch() {}

  /**
   * Works just like {@link #balanced(String, String, String)}, but accepts RegExp patterns.
   *
   * @param a first RegExp pattern of the matching pair
   * @param b second RegExp pattern of the matching pair
   * @param str string against which to execute matching.
   * @return object with matched pair data
   *
   * @see #balanced(String, String, String)
   */
  public static BalancedMatch balanced(Pattern a, Pattern b, String str) {
    Objects.requireNonNull(str);
    return balanced(maybeMatch(a, str), maybeMatch(b, str), str);
  }

  /**
   * <p>
   * For the first non-nested matching pair of {@code a} and {@code b} in {@code str}, return instance of the
   * {@link BalancedMatch} class.
   * </p>
   * <p>
   * If there's no match, {@code null} will be returned.
   * </p>
   * <p>
   * If the {@code str} contains more {@code a} than {@code b} / there are unmatched pairs, the first match that was
   * closed will be used. For example, {@code &#123;&#123;a&#125;} will match {@code ['&#123;', 'a', '']} and
   * {@code &#123;a&#125;&#125;} will match {@code ['', 'a', '&#125;']}.
   * </p>
   *
   * @param a first token of the matching pair
   * @param b second token of the matching pair
   * @param str string against which to execute matching.
   * @return object with matched pair data
   */
  public static BalancedMatch balanced(String a, String b, String str) {
    Objects.requireNonNull(str);

    RangePair r = range(a, b, str);

    if (r == null) {
      return null;
    }

    BalancedMatch result = new BalancedMatch();
    result.start = r.ai;
    result.end = r.bi;
    result.pre = str.substring(0, r.ai);
    result.body = r.ai + a.length() <= r.bi ? str.substring(r.ai + a.length(), r.bi) : "";
    result.post = str.substring(r.bi + b.length());
    return result;
  }

  /**
   * The index of the first match of a.
   */
  public int getStart() {
    return start;
  }

  /**
   * The index of the matching b.
   */
  public int getEnd() {
    return end;
  }

  /**
   * The preamble, a and b not included.
   */
  public String getPre() {
    return pre;
  }

  /**
   * The match, a and b not included.
   */
  public String getBody() {
    return body;
  }

  /**
   * The postscript, a and b not included.
   */
  public String getPost() {
    return post;
  }

  private static String maybeMatch(Pattern reg, String str) {
    if (reg == null) {
      return null;
    }
    Matcher m = reg.matcher(str);
    return m.find() ? m.group(0) : null;
  }

  /**
   * <p>
   * For the first non-nested matching pair of {@code a} and {@code b} in {@code str}, return {@link RangePair} with
   * indexes: {@code [ <a index>, <b index> ]}.
   * </p>
   * <p>
   * If there's no match, {@code null} will be returned.
   * </p>
   * <p>
   * If the {@code str} contains more {@code a} than {@code b} / there are unmatched pairs, the first match that was
   * closed will be used. For example, {@code &#123;&#123;a&#125;} will match {@code [ 1, 3 ]} and
   * {@code &#123;a&#125;&#125;} will match {@code [0, 2]}.
   * </p>
   *
   * @param a first token of the matching pair.
   * @param b second token of the matching pair.
   * @param str string against which to execute matching.
   * @return an array with indexes.
   */
  public static RangePair range(String a, String b, String str) {
    if (a == null || b == null) {
      return null;
    }

    RangePair result = null;
    int ai = str.indexOf(a);
    int bi = str.indexOf(b, ai + 1);
    int i = ai;

    if (ai >= 0 && bi >= 0) {
      if (Objects.equals(a, b)) {
        return new RangePair(ai, bi);
      }
      Deque<Integer> begs = new ArrayDeque<>();
      int left = str.length();
      int right = -1;

      while (i >= 0 && result == null) {
        if (i == ai) {
          begs.push(i);
          ai = str.indexOf(a, i + 1);
        } else if (begs.size() == 1) {
          result = new RangePair(begs.pop(), bi);
        } else {
          int beg = begs.pop();
          if (beg < left) {
            left = beg;
            right = bi;
          }

          bi = str.indexOf(b, i + 1);
        }

        i = ai < bi && ai >= 0 ? ai : bi;
      }

      if (begs.size() > 0) {
        result = new RangePair(left, right);
      }
    }

    return result;
  }

  /**
   * Matching pair with indexes.
   */
  public static class RangePair {

    private int ai;
    private int bi;

    private RangePair(int ai, int bi) {
      this.ai = ai;
      this.bi = bi;
    }

    /**
     * Index of the token a.
     */
    public int getAIndex() {
      return ai;
    }

    /**
     * Index of the token b.
     */
    public int getBIndex() {
      return bi;
    }

  }

}
