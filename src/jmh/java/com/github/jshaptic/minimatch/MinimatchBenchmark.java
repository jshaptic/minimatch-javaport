package com.github.jshaptic.minimatch;

import org.openjdk.jmh.annotations.Benchmark;

public class MinimatchBenchmark {

  private static String pattern = "**/*.js";
  private static String[] files = BraceExpansion.expand("x/y/z/{1..1000}.js");

  public static void main(String[] args) throws Exception {
    org.openjdk.jmh.Main.main(args);
  }

  @Benchmark
  public void match() {
    for (int f = 0; f < files.length; f++) {
      Minimatch.minimatch(files[f], pattern);
    }
  }

}
