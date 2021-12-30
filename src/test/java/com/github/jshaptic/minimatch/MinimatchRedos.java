package com.github.jshaptic.minimatch;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

public class MinimatchRedos {

  // utility function for generating long strings
  private String genstr(int len, String chr) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i <= len; i++) {
      result.append(chr);
    }

    return result.toString();
  }

  @Test
  public void test() {
    String exploit = "!(" + genstr(1024 * 15, "\\") + "A";

    // within the limits, and valid match
    assertTrue(Minimatch.minimatch("A", exploit));

    // within the limits, but results in an invalid regexp
    exploit = "[!(" + genstr(1024 * 15, "\\") + "A";
    assertFalse(Minimatch.minimatch("A", exploit));

    assertThrows(() -> {
      // too long, throws TypeError
      String exp = "!(" + genstr(1024 * 64, "\\") + "A)";
      Minimatch.minimatch("A", exp);
    });
  }

}
