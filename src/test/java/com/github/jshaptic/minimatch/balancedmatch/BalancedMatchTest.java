package com.github.jshaptic.minimatch.balancedmatch;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import com.github.jshaptic.minimatch.BalancedMatch;
import java.util.regex.Pattern;
import org.testng.annotations.Test;

public class BalancedMatchTest {

  @Test
  public void balanced() {
    BalancedMatch result = BalancedMatch.balanced("{", "}", "pre{in{nest}}post");
    assertEquals(result.getStart(), 3);
    assertEquals(result.getEnd(), 12);
    assertEquals(result.getPre(), "pre");
    assertEquals(result.getBody(), "in{nest}");
    assertEquals(result.getPost(), "post");

    result = BalancedMatch.balanced("{", "}", "{{{{{{{{{in}post");
    assertEquals(result.getStart(), 8);
    assertEquals(result.getEnd(), 11);
    assertEquals(result.getPre(), "{{{{{{{{");
    assertEquals(result.getBody(), "in");
    assertEquals(result.getPost(), "post");

    result = BalancedMatch.balanced("{", "}", "pre{body{in}post");
    assertEquals(result.getStart(), 8);
    assertEquals(result.getEnd(), 11);
    assertEquals(result.getPre(), "pre{body");
    assertEquals(result.getBody(), "in");
    assertEquals(result.getPost(), "post");

    result = BalancedMatch.balanced("{", "}", "pre{in}po}st");
    assertEquals(result.getStart(), 3);
    assertEquals(result.getEnd(), 6);
    assertEquals(result.getPre(), "pre");
    assertEquals(result.getBody(), "in");
    assertEquals(result.getPost(), "po}st");

    result = BalancedMatch.balanced("{", "}", "pre}{in{nest}}post");
    assertEquals(result.getStart(), 4);
    assertEquals(result.getEnd(), 13);
    assertEquals(result.getPre(), "pre}");
    assertEquals(result.getBody(), "in{nest}");
    assertEquals(result.getPost(), "post");

    result = BalancedMatch.balanced("{", "}", "pre{body}between{body2}post");
    assertEquals(result.getStart(), 3);
    assertEquals(result.getEnd(), 8);
    assertEquals(result.getPre(), "pre");
    assertEquals(result.getBody(), "body");
    assertEquals(result.getPost(), "between{body2}post");

    result = BalancedMatch.balanced("<b>", "</b>", "pre<b>in<b>nest</b></b>post");
    assertEquals(result.getStart(), 3);
    assertEquals(result.getEnd(), 19);
    assertEquals(result.getPre(), "pre");
    assertEquals(result.getBody(), "in<b>nest</b>");
    assertEquals(result.getPost(), "post");

    result = BalancedMatch.balanced("<b>", "</b>", "pre</b><b>in<b>nest</b></b>post");
    assertEquals(result.getStart(), 7);
    assertEquals(result.getEnd(), 23);
    assertEquals(result.getPre(), "pre</b>");
    assertEquals(result.getBody(), "in<b>nest</b>");
    assertEquals(result.getPost(), "post");

    result = BalancedMatch.balanced("{{", "}}", "pre{{{in}}}post");
    assertEquals(result.getStart(), 3);
    assertEquals(result.getEnd(), 9);
    assertEquals(result.getPre(), "pre");
    assertEquals(result.getBody(), "{in}");
    assertEquals(result.getPost(), "post");

    result = BalancedMatch.balanced("{{{", "}}", "pre{{{in}}}post");
    assertEquals(result.getStart(), 3);
    assertEquals(result.getEnd(), 8);
    assertEquals(result.getPre(), "pre");
    assertEquals(result.getBody(), "in");
    assertEquals(result.getPost(), "}post");

    result = BalancedMatch.balanced("{", "}", "pre{{first}in{second}post");
    assertEquals(result.getStart(), 4);
    assertEquals(result.getEnd(), 10);
    assertEquals(result.getPre(), "pre{");
    assertEquals(result.getBody(), "first");
    assertEquals(result.getPost(), "in{second}post");

    result = BalancedMatch.balanced("<?", "?>", "pre<?>post");
    assertEquals(result.getStart(), 3);
    assertEquals(result.getEnd(), 4);
    assertEquals(result.getPre(), "pre");
    assertEquals(result.getBody(), "");
    assertEquals(result.getPost(), "post");

    result = BalancedMatch.balanced("___", "___", "PRE ___BODY___ POST");
    assertEquals(result.getStart(), 4);
    assertEquals(result.getEnd(), 11);
    assertEquals(result.getPre(), "PRE ");
    assertEquals(result.getBody(), "BODY");
    assertEquals(result.getPost(), " POST");

    assertNull(BalancedMatch.balanced((Pattern) null, (Pattern) null, "nope"), "should be notOk");
    assertNull(BalancedMatch.balanced((String) null, (String) null, "nope"), "should be notOk");
    assertNull(BalancedMatch.balanced("{", "}", "nope"), "should be notOk");
    assertNull(BalancedMatch.balanced("{", "}", "{nope"), "should be notOk");
    assertNull(BalancedMatch.balanced("{", "}", "nope}"), "should be notOk");
    assertNull(BalancedMatch.balanced(Pattern.compile("\\{"), Pattern.compile("\\}"), "nope"), "should be notOk");

    result = BalancedMatch.balanced(Pattern.compile("\\s+\\{\\s+"), Pattern.compile("\\s+\\}\\s+"),
        "pre  {   in{nest}   }  post");
    assertEquals(result.getStart(), 3);
    assertEquals(result.getEnd(), 17);
    assertEquals(result.getPre(), "pre");
    assertEquals(result.getBody(), "in{nest}");
    assertEquals(result.getPost(), "post");
  }

}
