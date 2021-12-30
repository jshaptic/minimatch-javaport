package com.github.jshaptic.minimatch.braceexpansion;

import static org.testng.Assert.assertEquals;

import com.github.jshaptic.minimatch.BraceExpansion;
import java.util.Arrays;
import org.testng.annotations.Test;

public class BraceExpansionOrder {

  @Test
  public void order() {
    assertEquals(Arrays.asList(BraceExpansion.expand("a{d,c,b}e")), Arrays.asList("ade", "ace", "abe"));
  }

}
