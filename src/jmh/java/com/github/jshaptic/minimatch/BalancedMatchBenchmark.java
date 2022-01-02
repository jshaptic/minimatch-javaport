package com.github.jshaptic.minimatch;

import java.util.regex.Pattern;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;

public class BalancedMatchBenchmark {

  public static void main(String[] args) throws Exception {
    org.openjdk.jmh.Main.main(args);
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  public void indexOf() {
    BalancedMatch.balanced("{", "}", "pre{in{nest}}post");
    BalancedMatch.balanced("{", "}", "{{{{{{{{{in}post");
    BalancedMatch.balanced("{", "}", "pre{body{in}post");
    BalancedMatch.balanced("{", "}", "pre}{in{nest}}post");
    BalancedMatch.balanced("{", "}", "pre{body}between{body2}post");
    BalancedMatch.balanced("{", "}", "nope");
    BalancedMatch.balanced("<b>", "</b>", "pre<b>in<b>nest</b></b>post");
    BalancedMatch.balanced("<b>", "</b>", "pre</b><b>in<b>nest</b></b>post");
    BalancedMatch.balanced("{{", "}}", "pre{{{in}}}post");
    BalancedMatch.balanced("{{{", "}}", "pre{{{in}}}post");
    BalancedMatch.balanced("{", "}", "pre{{first}in{second}post");
    BalancedMatch.balanced("<?", "?>", "pre<?>post");
    BalancedMatch.balanced(Pattern.compile("\\{"), Pattern.compile("\\}"), "nope");
    BalancedMatch.balanced(Pattern.compile("\\s+\\{\\s+"), Pattern.compile("\\s+\\}\\s+"),
        "pre  {   in{nest}   }  post");
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  public void looping() {
    BalancedMatchLooping.balanced("{", "}", "pre{in{nest}}post");
    BalancedMatchLooping.balanced("{", "}", "{{{{{{{{{in}post");
    BalancedMatchLooping.balanced("{", "}", "pre{body{in}post");
    BalancedMatchLooping.balanced("{", "}", "pre}{in{nest}}post");
    BalancedMatchLooping.balanced("{", "}", "pre{body}between{body2}post");
    BalancedMatchLooping.balanced("{", "}", "nope");
    BalancedMatchLooping.balanced("<b>", "</b>", "pre<b>in<b>nest</b></b>post");
    BalancedMatchLooping.balanced("<b>", "</b>", "pre</b><b>in<b>nest</b></b>post");
    BalancedMatchLooping.balanced("{{", "}}", "pre{{{in}}}post");
    BalancedMatchLooping.balanced("{{{", "}}", "pre{{{in}}}post");
    BalancedMatchLooping.balanced("{", "}", "pre{{first}in{second}post");
    BalancedMatchLooping.balanced("<?", "?>", "pre<?>post");
    BalancedMatchLooping.balanced(Pattern.compile("\\{"), Pattern.compile("\\}"), "nope");
    BalancedMatchLooping.balanced(Pattern.compile("\\s+\\{\\s+"), Pattern.compile("\\s+\\}\\s+"),
        "pre  {   in{nest}   }  post");
  }

}
