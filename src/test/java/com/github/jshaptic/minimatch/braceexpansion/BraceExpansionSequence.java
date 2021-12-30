package com.github.jshaptic.minimatch.braceexpansion;

import static org.testng.Assert.assertEquals;

import com.github.jshaptic.minimatch.BraceExpansion;
import java.util.Arrays;
import org.testng.annotations.Test;

public class BraceExpansionSequence {

  @Test
  public void numericSequences() {
    assertEquals(Arrays.asList(BraceExpansion.expand("a{1..2}b{2..3}c")),
        Arrays.asList("a1b2c", "a1b3c", "a2b2c", "a2b3c"));
    assertEquals(Arrays.asList(BraceExpansion.expand("{1..2}{2..3}")), Arrays.asList("12", "13", "22", "23"));
  }

  @Test
  public void numericSequencesWithStepCount() {
    assertEquals(Arrays.asList(BraceExpansion.expand("{0..8..2}")), Arrays.asList("0", "2", "4", "6", "8"));
    assertEquals(Arrays.asList(BraceExpansion.expand("{1..8..2}")), Arrays.asList("1", "3", "5", "7"));
  }

  @Test
  public void numericSequencesWithNegativeXY() {
    assertEquals(Arrays.asList(BraceExpansion.expand("{3..-2}")), Arrays.asList("3", "2", "1", "0", "-1", "-2"));
  }

  @Test
  public void alphabeticSequences() {
    assertEquals(Arrays.asList(BraceExpansion.expand("1{a..b}2{b..c}3")),
        Arrays.asList("1a2b3", "1a2c3", "1b2b3", "1b2c3"));
    assertEquals(Arrays.asList(BraceExpansion.expand("{a..b}{b..c}")), Arrays.asList("ab", "ac", "bb", "bc"));
  }

  @Test
  public void alphabeticSequencesWithStepCount() {
    assertEquals(Arrays.asList(BraceExpansion.expand("{a..k..2}")), Arrays.asList("a", "c", "e", "g", "i", "k"));
    assertEquals(Arrays.asList(BraceExpansion.expand("{b..k..2}")), Arrays.asList("b", "d", "f", "h", "j"));
  }

}
