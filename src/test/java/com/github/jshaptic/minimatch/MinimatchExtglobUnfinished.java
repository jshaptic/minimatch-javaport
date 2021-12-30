package com.github.jshaptic.minimatch;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import org.testng.annotations.Test;

public class MinimatchExtglobUnfinished {

  private String[] types = "!?+*@".split("");

  @Test
  public void test() {
    Arrays.stream(types).forEach(type -> {
      assertTrue(Minimatch.minimatch(type + "(a|B", type + "(a|B", Minimatch.NO_NEGATE));
      assertFalse(Minimatch.minimatch(type + "(a|B", "B", Minimatch.NO_NEGATE));
    });
  }

}
