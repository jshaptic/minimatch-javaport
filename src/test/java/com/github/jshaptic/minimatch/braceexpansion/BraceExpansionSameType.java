package com.github.jshaptic.minimatch.braceexpansion;

import static org.testng.Assert.assertEquals;

import com.github.jshaptic.minimatch.BraceExpansion;
import java.util.Arrays;
import org.testng.annotations.Test;

public class BraceExpansionSameType {

  @Test
  public void sameType() {
    assertEquals(Arrays.asList(BraceExpansion.expand("{a..9}")), Arrays.asList("{a..9}"));
  }

}
