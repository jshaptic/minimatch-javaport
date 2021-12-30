package com.github.jshaptic.minimatch.braceexpansion;

import static org.testng.Assert.assertEquals;

import com.github.jshaptic.minimatch.BraceExpansion;
import java.util.Arrays;
import org.testng.annotations.Test;

public class BraceExpansionPad {

  @Test
  public void pad() {
    assertEquals(Arrays.asList(BraceExpansion.expand("{9..11}")), Arrays.asList("9", "10", "11"));
    assertEquals(Arrays.asList(BraceExpansion.expand("{09..11}")), Arrays.asList("09", "10", "11"));
  }

}
