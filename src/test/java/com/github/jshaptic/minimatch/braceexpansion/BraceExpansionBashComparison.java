package com.github.jshaptic.minimatch.braceexpansion;

import static org.testng.Assert.assertEquals;

import com.github.jshaptic.minimatch.BraceExpansion;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class BraceExpansionBashComparison {

  private Scanner cases;

  @BeforeClass
  private void getCases() throws FileNotFoundException {
    cases = new Scanner(new File(getClass().getClassLoader().getResource("braceexpansion/bash-results.txt").getFile()))
        .useDelimiter("><><><><");
  }

  @Test
  public void matchesBashExpansions() {
    while (cases.hasNext()) {
      String[] set = cases.next().split("\n", -1);
      String pattern = set[0];
      set = Arrays.copyOfRange(set, 1, set.length);
      String[] actual = BraceExpansion.expand(pattern);

      // If it expands to the empty string, then it's actually
      // just nothing, but Bash is a singly typed language, so
      // "nothing" is the same as "".
      if (set.length == 1 && set[0].isEmpty()) {
        set = new String[0];
      } else {
        // otherwise, strip off the [] that were added so that
        // "" expansions would be preserved properly.
        set = Arrays.stream(set).map(s -> s.replaceAll("^\\[|\\]$", "")).toArray(String[]::new);
      }

      assertEquals(Arrays.asList(actual), Arrays.asList(set), pattern);
    }
  }

}
