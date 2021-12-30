package com.github.jshaptic.minimatch.braceexpansion;

import static org.testng.Assert.assertEquals;

import com.github.jshaptic.minimatch.BraceExpansion;
import java.util.Arrays;
import org.testng.annotations.Test;

public class BraceExpansionDollar {

  @Test
  public void ignoresDollar() {
    assertEquals(Arrays.asList(BraceExpansion.expand("${1..3}")), Arrays.asList("${1..3}"));
    assertEquals(Arrays.asList(BraceExpansion.expand("${a,b}${c,d}")), Arrays.asList("${a,b}${c,d}"));
    assertEquals(Arrays.asList(BraceExpansion.expand("x${a,b}x${c,d}x")), Arrays.asList("x${a,b}x${c,d}x"));
  }

}
