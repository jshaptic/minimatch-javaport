package com.github.jshaptic.minimatch;

import static org.testng.Assert.assertEquals;

import com.github.jshaptic.minimatch.Patterns.TestPattern;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.testng.annotations.Test;

public class MinimatchBraceExpand {

  @Test
  public void braceExpansion() {
    // [ pattern, [expanded] ]
    List<TestPattern> patterns = new ArrayList<>();
    patterns.add(new Patterns.TestPattern("a{b,c{d,e},{f,g}h}x{y,z}",
        Arrays.asList("abxy", "abxz", "acdxy", "acdxz", "acexy", "acexz", "afhxy", "afhxz", "aghxy", "aghxz")));
    patterns.add(new Patterns.TestPattern("a{1..5}b", Arrays.asList("a1b", "a2b", "a3b", "a4b", "a5b")));
    patterns.add(new Patterns.TestPattern("a{b}c", Arrays.asList("a{b}c")));
    patterns.add(new Patterns.TestPattern("a{00..05}b", Arrays.asList("a00b", "a01b", "a02b", "a03b", "a04b", "a05b")));
    patterns.add(new Patterns.TestPattern("z{a,b},c}d", Arrays.asList("za,c}d", "zb,c}d")));
    patterns.add(new Patterns.TestPattern("z{a,b{,c}d", Arrays.asList("z{a,bd", "z{a,bcd")));
    patterns.add(new Patterns.TestPattern("a{b{c{d,e}f}g}h", Arrays.asList("a{b{cdf}g}h", "a{b{cef}g}h")));
    patterns.add(new Patterns.TestPattern("a{b{c{d,e}f{x,y}}g}h",
        Arrays.asList("a{b{cdfx}g}h", "a{b{cdfy}g}h", "a{b{cefx}g}h", "a{b{cefy}g}h")));
    patterns.add(new Patterns.TestPattern("a{b{c{d,e}f{x,y{}g}h",
        Arrays.asList("a{b{cdfxh", "a{b{cdfy{}gh", "a{b{cefxh", "a{b{cefy{}gh")));

    patterns.forEach(tc -> {
      String p = tc.getPattern();
      List<String> expect = tc.getExpect();
      System.out.println(p);
      assertEquals(Arrays.asList(Minimatch.braceExpand(p)), expect, p);
    });
  }

}
