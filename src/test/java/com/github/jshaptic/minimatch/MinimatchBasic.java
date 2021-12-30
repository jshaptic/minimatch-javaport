package com.github.jshaptic.minimatch;

import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertEquals;

import com.github.jshaptic.minimatch.Patterns.CommentPattern;
import com.github.jshaptic.minimatch.Patterns.FunctionPattern;
import com.github.jshaptic.minimatch.Patterns.TestPattern;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.lang3.ObjectUtils;
import org.testng.annotations.Test;

// http://www.bashcookbook.com/bashinfo/source/bash-1.14.7/tests/glob-test
//
// MINIMATCH_TODO: Some of these tests do very bad things with backslashes, and will
// most likely fail badly on windows. They should probably be skipped.
public class MinimatchBasic {

  private Patterns patterns = new Patterns();
  private List<String> regexps = patterns.regexps;
  private List<String> files = patterns.files;
  private int re = 0;

  @Test
  public void basicTests() {
    // [ pattern, [matches], MM opts, files, TAP opts]
    for (TestPattern c : patterns.patterns) {
      if (c instanceof FunctionPattern) {
        ((FunctionPattern) c).run();
        continue;
      }
      if (c instanceof CommentPattern) {
        System.out.println(c.toString());
        continue;
      }

      final String pattern = c.getPattern();
      final List<String> expect = c.getExpect().stream().sorted(this::alpha).collect(toList());
      int options = c.getOptions();
      final List<String> files = ObjectUtils.defaultIfNull(c.getFiles(), this.files);

      // options = options | Minimatch.DEBUG;
      Minimatch m = new Minimatch(pattern, options);
      Pattern r = m.makeRe();
      String expectRe = regexps.get(re++);
      expectRe = "/" + expectRe.substring(1, expectRe.length() - 1).replaceAll("([^\\\\])/", "$1\\/") + "/";
      String actualRe = patternToString(r);
      actualRe = "/" + actualRe.substring(1, actualRe.length() - 1).replaceAll("([^\\\\])/", "$1\\/") + "/";

      List<String> actual = Minimatch.match(files, pattern, options);
      actual.sort(this::alpha);

      System.out.println(buildMessage(pattern, expect, actualRe, files));
      System.out.println(buildMessage(pattern, null, actualRe, files));

      assertEquals(actual, expect);
      assertEquals(actualRe, expectRe);
    }
  }

  // PORT_INFO: no need to test for global leak since it's java

  private int alpha(String a, String b) {
    return a.compareTo(b);
  }

  // PORT_INFO: added this method to print regular expressions in JS style
  private String patternToString(Pattern pattern) {
    if (pattern == null) {
      return "false";
    }

    StringBuilder result = new StringBuilder();
    result.append("/");
    result.append(pattern.toString());
    result.append("/");
    if ((pattern.flags() & Pattern.CASE_INSENSITIVE) == Pattern.CASE_INSENSITIVE) {
      result.append("i");
    }

    return result.toString();
  }

  // PORT_INFO: added this method to print informative messages
  private String buildMessage(String pattern, List<String> expect, String re, List<String> files) {
    StringBuilder message = new StringBuilder();

    message.append(pattern);

    if (expect != null) {
      message.append(" " + expect);
    }

    message.append("\n    re: " + re);
    message.append("\n    files: " + files);
    message.append("\n    pattern: " + pattern);
    message.append("\n    ");
    return message.toString();
  }

}
