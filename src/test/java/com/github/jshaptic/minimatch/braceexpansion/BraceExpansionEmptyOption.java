package com.github.jshaptic.minimatch.braceexpansion;

import static org.testng.Assert.assertEquals;

import com.github.jshaptic.minimatch.BraceExpansion;
import java.util.Arrays;
import org.testng.annotations.Test;

public class BraceExpansionEmptyOption {

  @Test
  public void emptyOption() {
    assertEquals(Arrays.asList(BraceExpansion.expand("-v{,,,,}")), Arrays.asList("-v", "-v", "-v", "-v", "-v"));
  }

}
