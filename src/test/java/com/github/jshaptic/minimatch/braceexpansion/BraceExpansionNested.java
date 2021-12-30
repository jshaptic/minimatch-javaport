package com.github.jshaptic.minimatch.braceexpansion;

import static org.testng.Assert.assertEquals;

import com.github.jshaptic.minimatch.BraceExpansion;
import java.util.Arrays;
import org.testng.annotations.Test;

public class BraceExpansionNested {

  @Test
  public void nested() {
    assertEquals(Arrays.asList(BraceExpansion.expand("{a,b{1..3},c}")), Arrays.asList("a", "b1", "b2", "b3", "c"));
    assertEquals(Arrays.asList(BraceExpansion.expand("{{A..Z},{a..z}}")),
        Arrays.asList("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".split("")));
    assertEquals(Arrays.asList(BraceExpansion.expand("ppp{,config,oe{,conf}}")),
        Arrays.asList("ppp", "pppconfig", "pppoe", "pppoeconf"));
  }

}
