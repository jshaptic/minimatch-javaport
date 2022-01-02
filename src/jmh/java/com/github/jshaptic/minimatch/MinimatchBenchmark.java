package com.github.jshaptic.minimatch;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;

public class MinimatchBenchmark {

  private static String pattern = "**/*.js";
  private static String[] files = BraceExpansion.expand("x/y/z/{1..1000}.js");

  public static void main(String[] args) throws Exception {
    org.openjdk.jmh.Main.main(args);
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  public void match() {
    for (int f = 0; f < files.length; f++) {
      Minimatch.minimatch(files[f], pattern);
    }
  }

}
