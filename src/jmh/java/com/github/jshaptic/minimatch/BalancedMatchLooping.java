package com.github.jshaptic.minimatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BalancedMatchLooping {

  private int start;
  private int end;
  private String pre;
  private String body;
  private String post;

  private BalancedMatchLooping() {}

  public static BalancedMatchLooping balanced(Pattern a, Pattern b, String str) {
    Objects.requireNonNull(str);
    return balanced(maybeMatch(a, str), maybeMatch(b, str), str);
  }

  public static BalancedMatchLooping balanced(String a, String b, String str) {
    Objects.requireNonNull(str);

    if (a == null || a.isEmpty() || b == null || b.isEmpty()) {
      return null;
    }

    RangePair r = range(a, b, str);

    if (r == null) {
      return null;
    }

    BalancedMatchLooping result = new BalancedMatchLooping();
    result.start = r.left;
    result.end = r.right;
    result.pre = str.substring(0, r.left);
    result.body = r.left + a.length() <= r.right ? str.substring(r.left + a.length(), r.right) : "";
    result.post = str.substring(r.right + b.length());
    return result;
  }

  public int getStart() {
    return start;
  }

  public int getEnd() {
    return end;
  }

  public String getPre() {
    return pre;
  }

  public String getBody() {
    return body;
  }

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

  private static RangePair range(String a, String b, String str) {
    List<Integer> ais = new ArrayList<>();
    List<Integer> bis = new ArrayList<>();
    for (int i = 0; i < str.length(); i++) {
      if (str.substring(i, Math.min(i + a.length(), str.length())).equals(a)) {
        // FIXME: original code has error here, i < bis[bis.length]) will always result to false
        if (bis.size() > 0 && i < bis.get(bis.size() - 1) || bis.isEmpty()) {
          ais.add(i);
        } else {
          break;
        }
      }

      if (str.substring(i, Math.min(i + b.length(), str.length())).equals(b) && ais.size() > 0 && i > ais.get(0)
          && bis.size() < ais.size()) {
        bis.add(i);
      }
    }

    if (ais.isEmpty()) {
      return null;
    }

    return new RangePair(ais.get(ais.size() - bis.size()), bis.get(bis.size() - 1));
  }

  private static class RangePair {

    private int left;
    private int right;

    private RangePair(int left, int right) {
      this.left = left;
      this.right = right;
    }

  }

}
