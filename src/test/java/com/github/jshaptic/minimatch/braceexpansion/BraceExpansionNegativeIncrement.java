package com.github.jshaptic.minimatch.braceexpansion;

import static org.testng.Assert.assertEquals;

import com.github.jshaptic.minimatch.BraceExpansion;
import java.util.Arrays;
import org.testng.annotations.Test;

public class BraceExpansionNegativeIncrement {

  @Test
  public void negativeIncrement() {
    assertEquals(Arrays.asList(BraceExpansion.expand("{3..1}")), Arrays.asList("3", "2", "1"));
    assertEquals(Arrays.asList(BraceExpansion.expand("{10..8}")), Arrays.asList("10", "9", "8"));
    assertEquals(Arrays.asList(BraceExpansion.expand("{10..08}")), Arrays.asList("10", "09", "08"));
    assertEquals(Arrays.asList(BraceExpansion.expand("{c..a}")), Arrays.asList("c", "b", "a"));

    assertEquals(Arrays.asList(BraceExpansion.expand("{4..0..2}")), Arrays.asList("4", "2", "0"));
    assertEquals(Arrays.asList(BraceExpansion.expand("{4..0..-2}")), Arrays.asList("4", "2", "0"));
    assertEquals(Arrays.asList(BraceExpansion.expand("{e..a..2}")), Arrays.asList("e", "c", "a"));
  }

}
