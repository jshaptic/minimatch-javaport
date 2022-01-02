package com.github.jshaptic.minimatch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

public class BraceExpansionBenchmark {

  @State(Scope.Benchmark)
  public static class Data {

    private List<String> cases;

    @Setup(Level.Trial)
    public void doSetup() throws IOException {
      cases = new ArrayList<>();
      try (InputStream in = ClassLoader.getSystemResourceAsStream("braceexpansion/cases.txt");
          BufferedReader reader = new BufferedReader(new InputStreamReader(in));) {
        while (reader.ready()) {
          cases.add(reader.readLine());
        }
      }
    }

  }

  public static void main(String[] args) throws Exception {
    org.openjdk.jmh.Main.main(args);
  }

  @Benchmark
  public void expand(Data data) {
    for (String c : data.cases) {
      BraceExpansion.expand(c);
    }
  }

}
