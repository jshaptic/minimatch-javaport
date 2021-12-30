package com.github.jshaptic.minimatch;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

public class MinimatchExtglobEndingWithStateChar {

  @Test
  public void extglobEndingWithStateChar() {
    assertFalse(Minimatch.minimatch("ax", "a?(b*)"));
    assertTrue(Minimatch.minimatch("ax", "?(a*|b)"));
  }

}
