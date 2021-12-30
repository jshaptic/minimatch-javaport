package com.github.jshaptic.minimatch;

import static java.util.stream.Collectors.toList;

import java.nio.file.FileSystems;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * A minimal matching utility. It works by converting glob expressions into RegExp Pattern objects.
 * </p>
 * <p>
 * Supports these glob features:
 * </p>
 * <ul>
 * <li>Brace Expansion</li>
 * <li>Extended glob matching</li>
 * <li>"Globstar" {@code **} matching</li>
 * </ul>
 */
public class Minimatch {

  public static final int NO_OPTIONS = 0;
  public static final int DEBUG = 1;
  public static final int NO_BRACE = 2;
  public static final int NO_GLOBSTAR = 4;
  public static final int DOT = 8;
  public static final int NO_EXT = 16;
  public static final int NO_CASE = 32;
  public static final int NO_NULL = 64;
  public static final int MATCH_BASE = 128;
  public static final int NO_COMMENT = 256;
  public static final int NO_NEGATE = 512;
  public static final int FLIP_NEGATE = 1024;

  private static final String SEP = FileSystems.getDefault().getSeparator();

  private static final MinimatchPattern GLOBSTAR = new MinimatchPattern();

  private static final Map<Character, PlType> plTypes = new HashMap<>(5);
  static {
    plTypes.put('!', new PlType("(?:(?!(?:", "))[^/]*?)"));
    plTypes.put('?', new PlType("(?:", ")?"));
    plTypes.put('+', new PlType("(?:", ")+"));
    plTypes.put('*', new PlType("(?:", ")*"));
    plTypes.put('@', new PlType("(?:", ")"));
  }

  // any single thing other than /
  // don't need to escape / when using new RegExp()
  private static final String qmark = "[^/]";

  // * => any number of characters
  private static final String star = qmark + "*?";

  // ** when dots are allowed. Anything goes, except .. and .
  // not (^ or / followed by one or two dots followed by $ or /),
  // followed by anything, any number of times.
  private static final String twoStarDot = "(?:(?!(?:\\/|^)(?:\\.{1,2})($|\\/)).)*?";

  // not a ^ or / followed by a dot,
  // followed by anything, any number of times.
  private static final String twoStarNoDot = "(?:(?!(?:\\/|^)\\.).)*?";

  // characters that need to be escaped in RegExp.
  private static final char[] reSpecials = {'(', ')', '.', '*', '{', '}', '+', '?', '[', ']', '^', '$', '\\', '!'};

  // normalizes slashes.
  private static final Pattern slashSplit = Pattern.compile("\\/+");

  // PORT_INFO: static precompiled patterns to avoid big memory consumption.
  private static final Pattern braceExpansion = Pattern.compile("\\{.*\\}");
  private static final Pattern tailNormalizer = Pattern.compile("((?:\\\\{2}){0,64})(\\\\?)\\|");
  private static final Pattern nestedParensNormalizer = Pattern.compile("\\)[+*?]?");
  private static final Pattern regexpEscaper = Pattern.compile("[-\\[\\]{}()*+?.,\\\\^$|#\\s]");

  private int options;
  private MinimatchPattern[][] set;
  private String pattern;
  private boolean regexpCompiled;
  private Pattern regexp;
  private boolean negate;
  private boolean comment;
  private boolean empty;

  /**
   * Minimatch class constructor.
   *
   * @param pattern pattern to use for matching.
   * @param options various flags that can affect matching.
   */
  public Minimatch(String pattern, int options) {
    Objects.requireNonNull(pattern);

    pattern = pattern.trim();

    // windows support: need to use /, not \
    if (!SEP.equals("/")) {
      pattern = StringUtils.replace(pattern, SEP, "/");
    }

    this.options = options;
    this.set = new MinimatchPattern[][] {};
    this.pattern = pattern;
    this.regexpCompiled = false;
    this.regexp = null;
    this.negate = false;
    this.comment = false;
    this.empty = false;

    // make the set of regexps etc.
    this.make();
  }

  /**
   * Minimatch class constructor.
   *
   * @param pattern pattern to use for matching.
   */
  public Minimatch(String pattern) {
    this(pattern, NO_OPTIONS);
  }

  // PORT_INFO: defaults and ext methods are not needed in this port, too javascript specific.

  /**
   * Returns a function that tests its supplied argument, suitable for use with filter functions.
   *
   * @param pattern pattern to use for testing.
   * @param options various flags that can affect testing.
   * @return predicate function, which can be used for testing.
   */
  public static Predicate<String> filter(String pattern, int options) {
    return (p) -> minimatch(p, pattern, options);
  }

  /**
   * Returns a function that tests its supplied argument, suitable for use with filter functions.
   *
   * @param pattern pattern to use for testing.
   * @return predicate function, which can be used for testing.
   */
  public static Predicate<String> filter(String pattern) {
    return filter(pattern, NO_OPTIONS);
  }

  /**
   * Tests a path against the pattern using the options.
   *
   * @param p path to test.
   * @param pattern pattern to use for testing.
   * @param options various flags that can affect testing.
   * @return {@code true} if path is matching specified pattern, {@code false} otherwise.
   */
  public static boolean minimatch(String p, String pattern, int options) {
    Objects.requireNonNull(pattern);

    // shortcut: comments match nothing.
    if (!hasOption(options, NO_COMMENT) && pattern.charAt(0) == '#') {
      return false;
    }

    // "" only matches ""
    if (pattern.trim().isEmpty()) {
      return p.isEmpty();
    }

    return new Minimatch(pattern, options).match(p);
  }

  /**
   * Tests a path against the pattern using the options.
   *
   * @param p path to test.
   * @param pattern pattern to use for testing.
   * @return {@code true} if path is matching specified pattern, {@code false} otherwise.
   */
  public static boolean minimatch(String p, String pattern) {
    return minimatch(p, pattern, NO_OPTIONS);
  }

  private void make() {
    String pattern = this.pattern;
    int options = this.options;

    // empty patterns and comments match nothing.
    if (!hasOption(options, NO_COMMENT) && StringUtils.startsWith(pattern, "#")) {
      this.comment = true;
      return;
    }
    if (StringUtils.isEmpty(pattern)) {
      this.empty = true;
      return;
    }

    // step 1: figure out negation, etc.
    this.parseNegate();

    // step 2: expand braces
    String[] globSet = this.braceExpand();

    debug(false, pattern, Arrays.toString(globSet));

    // step 3: now we have a set, so turn each one into a series of path-portion
    // matching patterns.
    // These will be regexps, except in the case of "**", which is
    // set to the GLOBSTAR object for globstar behavior,
    // and will not contain any / characters
    String[][] globParts = new String[globSet.length][];
    for (int i = 0; i < globParts.length; i++) {
      globParts[i] = slashSplit.split(globSet[i], -1);
    }

    debug(false, pattern, Arrays.deepToString(globParts));

    // glob --> regexps
    MinimatchPattern[][] set = new MinimatchPattern[globParts.length][];
    for (int i = 0; i < set.length; i++) {
      set[i] = new MinimatchPattern[globParts[i].length];
      for (int j = 0; j < set[i].length; j++) {
        set[i][j] = parse(globParts[i][j], false);
      }
    }

    debug(false, pattern, Arrays.deepToString(set));

    this.set = set;
  }

  private void parseNegate() {
    String pattern = this.pattern;
    boolean negate = false;
    int options = this.options;
    int negateOffset = 0;

    if (hasOption(options, NO_NEGATE)) {
      return;
    }

    for (int i = 0, l = pattern.length(); i < l && pattern.charAt(i) == '!'; i++) {
      negate = !negate;
      negateOffset++;
    }

    if (negateOffset > 0) {
      this.pattern = pattern.substring(negateOffset);
    }
    this.negate = negate;
  }

  /**
   * <p>
   * Brace expansion:
   * </p>
   * <ul>
   * <li>{@code a&#123;b,c&#125;d -> abd acd}</li>
   * <li>{@code a&#123;b,&#125;c -> abc ac}</li>
   * <li>{@code a&#123;0..3&#125;d -> a0d a1d a2d a3d}</li>
   * <li>{@code a&#123;b,c&#123;d,e&#125;f&#125;g -> abg acdfg acefg}</li>
   * <li>{@code a&#123;b,c&#125;d&#123;e,f&#125;g -> abdeg acdeg abdeg abdfg}</li>
   * </ul>
   * 
   * <p>
   * Invalid sets are not expanded:
   * </p>
   * <ul>
   * <li>{@code a&#123;2..&#125;b -> a&#123;2..&#125;b}</li>
   * <li>{@code a&#123;b&#125;c -> a&#123;b&#125;c}</li>
   * </ul>
   *
   * @param pattern pattern to use for expansion.
   * @param options flags that can affect pattern expansion.
   * @return list of expanded strings.
   */
  public static String[] braceExpand(String pattern, int options) {
    Objects.requireNonNull(pattern);

    if (hasOption(options, NO_BRACE) || !braceExpansion.matcher(pattern).find()) {
      // shortcut. no need to expand.
      return new String[] {pattern};
    }

    return BraceExpansion.expand(pattern);
  }

  /**
   * <p>
   * Brace expansion:
   * </p>
   * <ul>
   * <li>{@code a&#123;b,c&#125;d -> abd acd}</li>
   * <li>{@code a&#123;b,&#125;c -> abc ac}</li>
   * <li>{@code a&#123;0..3&#125;d -> a0d a1d a2d a3d}</li>
   * <li>{@code a&#123;b,c&#123;d,e&#125;f&#125;g -> abg acdfg acefg}</li>
   * <li>{@code a&#123;b,c&#125;d&#123;e,f&#125;g -> abdeg acdeg abdeg abdfg}</li>
   * </ul>
   * 
   * 
   * <p>
   * Invalid sets are not expanded:
   * </p>
   * <ul>
   * <li>{@code a&#123;2..&#125;b -> a&#123;2..&#125;b}</li>
   * <li>{@code a&#123;b&#125;c -> a&#123;b&#125;c}</li>
   * </ul>
   *
   * @param pattern pattern to use for expansion.
   * @return list of expanded strings.
   */
  public static String[] braceExpand(String pattern) {
    return braceExpand(pattern, NO_OPTIONS);
  }

  private String[] braceExpand() {
    return braceExpand(this.pattern, this.options);
  }

  // parse a component of the expanded set.
  // At this point, no pattern may contain "/" in it
  // so we're going to return a 2d array, where each entry is the full
  // pattern, split on '/', and then turned into a regular expression.
  // A regexp is made at the end which joins each array with an
  // escaped /, and another full one which joins each regexp with |.
  //
  // Following the lead of Bash 4.1, note that "**" only has special meaning
  // when it is the *only* thing in a path portion. Otherwise, any series
  // of * is equivalent to a single *. Globstar behavior is enabled by
  // default, and can be disabled by setting options.noglobstar.
  private static final Object SUBPARSE = new Object();

  private MinimatchPattern parse(String pattern, Object isSub) {
    if (pattern.length() > 1024 * 64) {
      throw new RuntimeException("pattern is too long");
    }

    int options = this.options;

    // shortcuts
    if (!hasOption(options, NO_GLOBSTAR) && pattern.equals("**")) {
      return GLOBSTAR;
    }
    if (pattern.isEmpty()) {
      return new MinimatchPattern("");
    }

    final StringBuilder re = new StringBuilder();
    final AtomicBoolean hasMagic = new AtomicBoolean(hasOption(options, NO_CASE));
    boolean escaping = false;
    // ? => one single character
    final Deque<PlTypePattern> patternListStack = new ArrayDeque<>();
    final Deque<PlTypePattern> negativeLists = new ArrayDeque<>();
    final AtomicReference<Character> stateChar = new AtomicReference<>();
    boolean inClass = false;
    int reClassStart = -1;
    int classStart = -1;
    // . and .. never match anything that doesn't start with .,
    // even when options.dot is set.
    String patternStart = pattern.charAt(0) == '.' ? "" // anything
        // not (start or / followed by . or .. followed by / or end)
        : hasOption(options, DOT) ? "(?!(?:^|\\/)\\.{1,2}(?:$|\\/))" : "(?!\\.)";

    Runnable clearStateChar = () -> {
      if (stateChar.get() != null) {
        // we had some state-tracking character
        // that wasn't consumed by this pass.
        switch (stateChar.get()) {
          case '*':
            re.append(star);
            hasMagic.set(true);
            break;
          case '?':
            re.append(qmark);
            hasMagic.set(true);
            break;
          default:
            re.append("\\").append(stateChar);
            break;
        }
        debug("clearStateChar '%s' '%s'", stateChar, re);
        stateChar.set(null);
      }
    };

    for (int i = 0; i < pattern.length(); i++) {
      char c = pattern.charAt(i);
      debug("%-8s%s %s '%s'", pattern, i, re, c);

      // skip over any that are escaped.
      if (escaping && ArrayUtils.indexOf(reSpecials, c) >= 0) {
        re.append("\\").append(c);
        escaping = false;
        continue;
      }

      switch (c) {
        case '/':
          // completely not allowed, even escaped.
          // Should already be path-split by now.
          return null;

        case '\\':
          clearStateChar.run();
          escaping = true;
          continue;

        // the various stateChar values
        // for the "extglob" stuff.
        case '?':
        case '*':
        case '+':
        case '@':
        case '!':
          debug("%-8s%s %s '%s' <-- stateChar", pattern, i, re, c);

          // all of those are literals inside a class, except that
          // the glob [!a] means [^a] in regexp
          if (inClass) {
            debug("  in class");
            if (c == '!' && i == classStart + 1) {
              c = '^';
            }
            re.append(c);
            continue;
          }

          // if we already have a stateChar, then it means
          // that there was something like ** or +? in there.
          // Handle the stateChar, then proceed with this one.
          debug("call clearStateChar '%s'", stateChar);
          clearStateChar.run();
          stateChar.set(c);
          // if extglob is disabled, then +(asdf|foo) isn't a thing.
          // just clear the statechar *now*, rather than even diving into
          // the patternList stuff.
          if (hasOption(options, NO_EXT)) {
            clearStateChar.run();
          }
          continue;

        case '(':
          if (inClass) {
            re.append("(");
            continue;
          }

          if (stateChar.get() == null) {
            re.append("\\(");
            continue;
          }

          patternListStack.push(new PlTypePattern(stateChar.get(), re.length(), plTypes.get(stateChar.get()).open,
              plTypes.get(stateChar.get()).close));
          // negation is (?:(?!js)[^/]*)
          re.append(stateChar.get() == '!' ? "(?:(?!(?:" : "(?:");
          debug("plType '%s' %s", stateChar, re);
          stateChar.set(null);
          continue;

        case ')':
          if (inClass || patternListStack.size() == 0) {
            re.append("\\)");
            continue;
          }

          clearStateChar.run();
          hasMagic.set(true);
          PlTypePattern pl = patternListStack.pop();
          // negation is (?:(?!js)[^/]*)
          // The others are (?:<pattern>)<type>
          re.append(pl.close);
          if (pl.type == '!') {
            negativeLists.add(pl);
          }
          pl.reEnd = re.length();
          continue;

        case '|':
          if (inClass || patternListStack.size() == 0 || escaping) {
            re.append("\\|");
            escaping = false;
            continue;
          }

          clearStateChar.run();
          re.append("|");
          continue;

        // these are mostly the same in regexp and glob
        case '[':
          // swallow any state-tracking char before the [
          clearStateChar.run();

          if (inClass) {
            re.append("\\").append(c);
            continue;
          }

          inClass = true;
          classStart = i;
          reClassStart = re.length();
          re.append(c);
          continue;

        case ']':
          // a right bracket shall lose its special
          // meaning and represent itself in
          // a bracket expression if it occurs
          // first in the list. -- POSIX.2 2.8.3.2
          if (i == classStart + 1 || !inClass) {
            re.append("\\").append(c);
            escaping = false;
            continue;
          }

          // handle the case where we left a class open.
          // "[z-a]" is valid, equivalent to "\[z-a\]"
          if (inClass) {
            // split where the last [ was, make sure we don't have
            // an invalid re. if so, re-walk the contents of the
            // would-be class to re-translate any characters that
            // were passed through as-is
            // MINIMATCH_TODO: It would probably be faster to determine this
            // without a try/catch and a new RegExp, but it's tricky
            // to do safely. For now, this is safe and works.
            String cs = pattern.substring(classStart + 1, i);
            // PORT_INFO: correct all Java special cases before compiling to pattern
            cs = StringUtils.replace(cs, "\\b", "\b");
            cs = StringUtils.replace(cs, "[", "\\[");
            try {
              Pattern.compile("[" + cs + "]");
            } catch (PatternSyntaxException er) {
              // not a valid class!
              MinimatchPattern sp = this.parse(cs, SUBPARSE);
              re.setLength(reClassStart);
              re.append("\\[").append(sp.asString()).append("\\]");
              hasMagic.set(hasMagic.get() || sp.hasMagic);
              inClass = false;
              continue;
            }
          }

          // finish up the class.
          hasMagic.set(true);
          inClass = false;
          re.append(c);
          continue;

        default:
          // swallow any state char that wasn't consumed
          clearStateChar.run();

          if (escaping) {
            // no need
            escaping = false;
          } else if (ArrayUtils.indexOf(reSpecials, c) >= 0 && !(c == '^' && inClass)) {
            re.append("\\");
          }

          re.append(c);
      } // switch
    } // for

    // handle the case where we left a class open.
    // "[abc" is valid, equivalent to "\[abc"
    if (inClass) {
      // split where the last [ was, and escape it
      // this is a huge pita. We now have to re-walk
      // the contents of the would-be class to re-translate
      // any characters that were passed through as-is
      String cs = pattern.substring(classStart + 1);
      MinimatchPattern sp = this.parse(cs, SUBPARSE);
      re.setLength(reClassStart);
      re.append("\\[").append(sp.asString());
      hasMagic.set(hasMagic.get() || sp.hasMagic);
    }

    // handle the case where we had a +( thing at the *end*
    // of the pattern.
    // each pattern list stack adds 3 chars, and we need to go through
    // and escape any | chars that were passed through as-is for the regexp.
    // Go through and escape them, taking care not to double-escape any
    // | chars that were already escaped.
    while (patternListStack.size() > 0) {
      PlTypePattern pl = patternListStack.pop();
      String tail = re.substring(pl.reStart + pl.open.length());
      debug("setting tail", re, pl);
      // maybe some even number of \, then maybe 1 \, followed by a |
      tail = normalizeTail(tail);

      debug("tail=%s\n   %s", tail, tail, pl, re);
      String t = pl.type == '*' ? star : pl.type == '?' ? qmark : "\\" + pl.type;

      hasMagic.set(true);
      re.setLength(pl.reStart);
      re.append(t).append("\\(").append(tail);
    }

    // handle trailing things that only matter at the very end.
    clearStateChar.run();
    if (escaping) {
      // trailing \\
      re.append("\\\\");
    }

    // only need to apply the nodot start if the re starts with
    // something that could conceivably capture a dot
    boolean addPatternStart = false;
    switch (re.charAt(0)) {
      case '.':
      case '[':
      case '(':
        addPatternStart = true;
        break;
      default:
        break;
    }

    // Hack to work around lack of negative lookbehind in JS
    // A pattern like: *.!(x).!(y|z) needs to ensure that a name
    // like 'a.xyz.yz' doesn't match. So, the first negative
    // lookahead, has to look ALL the way ahead, to the end of
    // the pattern.
    for (Iterator<PlTypePattern> iterator = negativeLists.descendingIterator(); iterator.hasNext();) {
      PlTypePattern nl = iterator.next();

      final String nlBefore = re.substring(0, nl.reStart);
      final String nlFirst = re.substring(nl.reStart, nl.reEnd - 8);
      String nlLast = re.substring(nl.reEnd - 8, nl.reEnd);
      String nlAfter = re.substring(nl.reEnd);

      nlLast += nlAfter;

      // Handle nested stuff like *(*.js|!(*.json)), where open parens
      // mean that we should *not* include the ) in the bit that is considered
      // "after" the negated section.
      int openParensBefore = StringUtils.countMatches(nlBefore, '(');
      String cleanAfter = nlAfter;
      for (int i = 0; i < openParensBefore; i++) {
        cleanAfter = nestedParensNormalizer.matcher(cleanAfter).replaceFirst("");
      }
      nlAfter = cleanAfter;

      String dollar = "";
      if (nlAfter.isEmpty() && isSub != SUBPARSE) {
        dollar = "$";
      }
      String newRe = nlBefore + nlFirst + nlAfter + dollar + nlLast;
      re.replace(0, re.length(), newRe);
    }

    // if the re is not "" at this point, then we need to make sure
    // it doesn't match against an empty path part.
    // Otherwise a/* will match a/, which it should not.
    if (re.length() > 0 && hasMagic.get()) {
      re.insert(0, "(?=.)");
    }

    if (addPatternStart) {
      re.insert(0, patternStart);
    }

    // parsing just a piece of a larger pattern.
    if (isSub == SUBPARSE) {
      return new MinimatchPattern(re.toString(), hasMagic.get());
    }

    // skip the regexp for non-magical patterns
    // unescape anything in it, though, so that it'll be
    // an exact match against a file etc.
    if (!hasMagic.get()) {
      return new MinimatchPattern(globUnescape(pattern));
    }

    MinimatchPattern regExp;
    int flags = hasOption(options, NO_CASE) ? Pattern.CASE_INSENSITIVE : 0;
    try {
      regExp = new MinimatchPattern(Pattern.compile("^" + re + "$", flags));
    } catch (PatternSyntaxException er) {
      // If it was an invalid regular expression, then it can't match
      // anything. This trick looks for a character after the end of
      // the string, which is of course impossible, except in multi-line
      // mode, but it's not a /m regex.
      return new MinimatchPattern(Pattern.compile("$."));
    }

    regExp.glob = pattern;
    regExp.src = re.toString();

    return regExp;
  }

  // PORT_INFO: extracted this method from parse method to make it more readable
  private static String normalizeTail(String tail) {
    Function<Matcher, String> replacer = m -> {
      String g1 = m.group(1);
      String g2 = m.group(2);

      if (g2.isEmpty()) {
        // the | isn't already escaped, so escape it.
        g2 = "\\";
      }

      // need to escape all those slashes *again*, without escaping the
      // one that we need for escaping the | character. As it works out,
      // escaping an even number of slashes can be done by simply repeating
      // it exactly after itself. That's why this trick works.
      //
      // I am sorry that you have to see this.
      return g1 + g1 + g2 + "|";
    };

    Matcher matcher = tailNormalizer.matcher(tail);
    StringBuffer result = new StringBuffer();
    while (matcher.find()) {
      // PORT_INFO: it's needed to escape all backslashes here because appendReplacement treatens all backslahes as
      // special characters
      matcher.appendReplacement(result, StringUtils.replace(replacer.apply(matcher), "\\", "\\\\"));
    }
    matcher.appendTail(result);

    return result.toString();
  }

  /**
   * <p>
   * Make a regular expression {@link Pattern} object from the pattern string.
   * </p>
   * 
   * <p>
   * Each call of this method will implicitly create one instance of {@link Minimatch} object.
   * </p>
   *
   * @param pattern pattern to process
   * @param options flags that can affect pattern processing
   * @return regular expression generated from the pattern or {@code null} if the pattern is invalid.
   */
  public static Pattern makeRe(String pattern, int options) {
    return new Minimatch(pattern, options).makeRe();
  }

  /**
   * <p>
   * Make a regular expression {@link Pattern} object from the pattern string.
   * </p>
   * 
   * <p>
   * Each call of this method will implicitly create one instance of {@link Minimatch} object.
   * </p>
   *
   * @param pattern pattern to process
   * @return regular expression generated from the pattern or {@code null} if the pattern is invalid.
   */
  public static Pattern makeRe(String pattern) {
    return makeRe(pattern, NO_OPTIONS);
  }

  /**
   * <p>
   * Generate the {@link Pattern} object if necessary, and return it. Will return {@code null} if the pattern is
   * invalid.
   * </p>
   *
   * @return regular expression generated from the pattern or {@code null} if the pattern is invalid.
   */
  public Pattern makeRe() {
    if (this.regexp != null || this.regexpCompiled) {
      return this.regexp;
    }

    // at this point, this.set is a 2d array of partial
    // pattern strings, or "**".
    //
    // It's better to use .match(). This function shouldn't
    // be used, really, but it's pretty convenient sometimes,
    // when you just want to work with a regex.
    MinimatchPattern[][] set = this.set;

    if (set.length == 0) {
      this.regexpCompiled = true;
      this.regexp = null;
      return this.regexp;
    }
    int options = this.options;

    String twoStar = hasOption(options, NO_GLOBSTAR) ? star : hasOption(options, DOT) ? twoStarDot : twoStarNoDot;
    int flags = hasOption(options, NO_CASE) ? Pattern.CASE_INSENSITIVE : 0;

    String re = Arrays.stream(set).map(pattern -> {
      return Arrays.stream(pattern).map(p -> {
        return (p == GLOBSTAR) ? twoStar : (p.isString()) ? regExpEscape(p.asString()) : p.src;
      }).collect(Collectors.joining("\\/"));
    }).collect(Collectors.joining("|"));

    // must match entire pattern
    // ending in a * or ** will make it less strict.
    re = "^(?:" + re + ")$";

    // can match anything, as long as it's not this.
    if (this.negate) {
      re = "^(?!" + re + ").*$";
    }

    try {
      this.regexp = Pattern.compile(re, flags);
    } catch (PatternSyntaxException ex) {
      this.regexp = null;
    }
    this.regexpCompiled = true;
    return this.regexp;
  }

  /**
   * <p>
   * Match against the list of files, in the style of fnmatch or glob. If nothing is matched, and {@link #NO_NULL} is
   * set, then return a list containing the pattern itself.
   * </p>
   * 
   * <p>
   * Each call of this method will implicitly create one instance of {@link Minimatch} object.
   * </p>
   *
   * @param list list of files to match.
   * @param pattern pattern to use for matching.
   * @param options various flags that can affect files matching.
   * @return list of files, that were successfully matched against pattern.
   */
  public static List<String> match(List<String> list, String pattern, int options) {
    Minimatch mm = new Minimatch(pattern, options);
    list = list.stream().filter(mm::match).collect(toList());
    if (hasOption(mm.options, NO_NULL) && list.size() == 0) {
      list.add(pattern);
    }
    return list;
  }

  /**
   * <p>
   * Match against the list of files, in the style of fnmatch or glob. If nothing is matched, and {@link #NO_NULL} is
   * set, then return a list containing the pattern itself.
   * </p>
   * 
   * <p>
   * Each call of this method will implicitly create one instance of {@link Minimatch} object.
   * </p>
   *
   * @param list list of files to match.
   * @param pattern pattern to use for matching.
   * @return list of files, that were successfully matched against pattern.
   */
  public static List<String> match(List<String> list, String pattern) {
    return match(list, pattern, NO_OPTIONS);
  }

  /**
   * <p>
   * Return {@code true} if the path matches the pattern, or {@code false} otherwise.
   * </p>
   *
   * @param path path to match.
   * @return {@code true} if the path matches the pattern, or {@code false} otherwise.
   */
  public boolean match(String path) {
    return match(path, false);
  }

  private boolean match(String path, boolean partial) {
    debug(false, "match", path, this.pattern);
    // short-circuit in the case of busted things.
    // comments, etc.
    if (this.comment) {
      return false;
    }
    if (this.empty) {
      return path.isEmpty();
    }

    if (path.equals("/") && partial) {
      return true;
    }

    int options = this.options;

    // windows: need to use /, not \
    if (!SEP.equals("/")) {
      path = StringUtils.replace(path, SEP, "/");
    }

    // treat the test path as a set of pathparts.
    String[] f = slashSplit.split(path, -1);
    debug(false, this.pattern, "split", Arrays.toString(f));

    // just ONE of the pattern sets in this.set needs to match
    // in order for it to be valid. If negating, then just one
    // match means that we have failed.
    // Either way, return on the first hit.

    MinimatchPattern[][] set = this.set;
    debug(false, this.pattern, "set", Arrays.deepToString(set));

    // Find the basename of the path by looking for the last non-empty segment
    String filename = "";
    for (int i = f.length - 1; i >= 0; i--) {
      filename = f[i];
      if (!filename.isEmpty()) {
        break;
      }
    }

    for (int i = 0; i < set.length; i++) {
      MinimatchPattern[] p = set[i];
      String[] file = f;
      if (hasOption(options, MATCH_BASE) && p.length == 1) {
        file = new String[] {filename};
      }
      boolean hit = this.matchOne(file, p, partial);
      if (hit) {
        if (hasOption(options, FLIP_NEGATE)) {
          return true;
        }
        return !this.negate;
      }
    }

    // didn't get any hits. this is success if it's a negative
    // pattern, failure otherwise.
    if (hasOption(options, FLIP_NEGATE)) {
      return false;
    }
    return this.negate;
  }

  // set partial to true to test if, for example,
  // "/a/b" matches the start of "/*/b/*/d"
  // Partial means, if you run out of file before you run
  // out of pattern, then that's fine, as long as all
  // the parts match.
  private boolean matchOne(String[] file, MinimatchPattern[] pattern, boolean partial) {
    int options = this.options;

    debug("matchOne \n  file: %s\n  pattern: %s", Arrays.toString(file), Arrays.toString(pattern));

    debug(false, "matchOne", file.length, pattern.length);

    int fi = 0;
    int pi = 0;
    int fl = file.length;
    int pl = pattern.length;
    for (; (fi < fl) && (pi < pl); fi++, pi++) {
      debug(false, "matchOne loop");
      MinimatchPattern p = pattern[pi];
      String f = file[fi];

      debug(false, Arrays.toString(pattern), p, f);

      if (p == GLOBSTAR) {
        debug(false, "GLOBSTAR", Arrays.toString(pattern), p, f);

        // "**"
        // a/**/b/**/c would match the following:
        // a/b/x/y/z/c
        // a/x/y/z/b/c
        // a/b/x/b/x/c
        // a/b/c
        // To do this, take the rest of the pattern after
        // the **, and see if it would match the file remainder.
        // If so, return success.
        // If not, the ** "swallows" a segment, and try again.
        // This is recursively awful.
        //
        // a/**/b/**/c matching a/b/x/y/z/c
        // - a matches a
        // - doublestar
        // - matchOne(b/x/y/z/c, b/**/c)
        // - b matches b
        // - doublestar
        // - matchOne(x/y/z/c, c) -> no
        // - matchOne(y/z/c, c) -> no
        // - matchOne(z/c, c) -> no
        // - matchOne(c, c) yes, hit
        int fr = fi;
        int pr = pi + 1;
        if (pr == pl) {
          debug(false, "** at the end");
          // a ** at the end will just swallow the rest.
          // We have found a match.
          // however, it will not swallow /.x, unless
          // options.dot is set.
          // . and .. are *never* matched by **, for explosively
          // exponential reasons.
          for (; fi < fl; fi++) {
            if (file[fi].equals(".") || file[fi].equals("..")
                || (!hasOption(options, DOT) && file[fi].startsWith("."))) {
              return false;
            }
          }
          return true;
        }

        // ok, let's see if we can swallow whatever we can.
        while (fr < fl) {
          String swallowee = file[fr];

          debug(false, "\nglobstar while", Arrays.toString(file), fr, Arrays.toString(pattern), pr, swallowee);

          // MINIMATCH_XXX remove this slice. Just pass the start index.
          if (this.matchOne(Arrays.copyOfRange(file, fr, file.length), Arrays.copyOfRange(pattern, pr, pattern.length),
              partial)) {
            debug(false, "globstar found match!", fr, fl, swallowee);
            // found a match.
            return true;
          } else {
            // can't swallow "." or ".." ever.
            // can only swallow ".foo" when explicitly asked.
            if (swallowee.equals(".") || swallowee.equals("..")
                || (!hasOption(options, DOT) && swallowee.startsWith("."))) {
              debug(false, "dot detected!", Arrays.toString(file), fr, Arrays.toString(pattern), pr);
              break;
            }

            // ** swallows a segment, and continue.
            debug(false, "globstar swallow a segment, and continue");
            fr++;
          }
        }

        // no match was found.
        // However, in partial mode, we can't say this is necessarily over.
        // If there's more *pattern* left, then
        if (partial) {
          // ran out of file
          debug(false, "\n>>> no match, partial?", Arrays.toString(file), fr, Arrays.toString(pattern), pr);
          if (fr == fl) {
            return true;
          }
        }
        return false;
      }

      // something other than **
      // non-magic patterns just have to match exactly
      // patterns with magic have been turned into regexps.
      boolean hit = false;
      if (p.isString()) {
        if (hasOption(options, NO_CASE)) {
          hit = f.equalsIgnoreCase(p.asString());
        } else {
          hit = f.equals(p.asString());
        }
        debug(false, "string match", p, f, hit);
      } else {
        hit = p.isPattern() && p.asPattern().matcher(f).matches();
        debug(false, "pattern match", p, f, hit);
      }

      if (!hit) {
        return false;
      }
    }

    // Note: ending in / means that we'll get a final ""
    // at the end of the pattern. This can only match a
    // corresponding "" at the end of the file.
    // If the file ends in /, then it can only match a
    // a pattern that ends in /, unless the pattern just
    // doesn't have any more for it. But, a/b/ should *not*
    // match "a/b/*", even though "" matches against the
    // [^/]*? pattern, except in partial mode, where it might
    // simply not be reached yet.
    // However, a/b/ should still satisfy a/*

    // now either we fell off the end of the pattern, or we're done.
    if (fi == fl && pi == pl) {
      // ran out of pattern and filename at the same time.
      // an exact hit!
      return true;
    } else if (fi == fl) {
      // ran out of file, but still had pattern left.
      // this is ok if we're doing the match as part of
      // a glob fs traversal.
      return false;
    } else if (pi == pl) {
      // ran out of pattern, still have file left.
      // this is only acceptable if we're on the very last
      // empty segment of a file with a trailing slash.
      // a/* should match a/b/
      boolean emptyFileEnd = (fi == fl - 1) && (file[fi].isEmpty());
      return emptyFileEnd;
    }

    // should be unreachable.
    throw new RuntimeException("wtf?");
  }

  // replace stuff like \* with *
  private static String globUnescape(String s) {
    StringBuilder sb = new StringBuilder();
    int start = 0;
    for (int i = 0; i < s.length(); i++) {
      if (s.charAt(i) == '\\') {
        if (i != 0) {
          sb.append(s.substring(start, i));
        }
        i++;
        if (i < s.length()) {
          sb.append(s.charAt(i));
        } else {
          sb.append(s.charAt(i - 1));
        }
        start = i + 1;
      }
    }

    if (start < s.length()) {
      sb.append(s.substring(start));
    }

    return sb.toString();
  }

  private static String regExpEscape(String s) {
    return regexpEscaper.matcher(s).replaceAll("\\\\$0");
  }

  // PORT_INFO: shortcut method to check if option is set
  private static boolean hasOption(int options, int o) {
    return (options & o) == o;
  }

  // PORT_INFO: method for printing debug information
  private void debug(boolean hasLogline, Object... vars) {
    if (hasOption(options, DEBUG)) {
      String logline = "";
      if (hasLogline) {
        Object[] v = {};
        if (vars != null && vars.length > 0 && vars[0] instanceof String) {
          logline = vars[0].toString();
          if (vars.length > 1) {
            v = new Object[vars.length - 2];
            for (int i = 0; i < v.length; i++) {
              v[i] = vars[i + 1];
            }
          }
        }
        System.out.format("%s: ", Thread.currentThread().getStackTrace()[2].getLineNumber());
        System.out.format(logline + "\n", v);
      } else if (vars != null) {
        for (int i = 0; i < vars.length; i++) {
          logline += "%s ";
        }
        System.out.format("%s: ", Thread.currentThread().getStackTrace()[2].getLineNumber());
        System.out.format(logline.trim() + "\n", vars);
      }
    }
  }

  // PORT_INFO: method for printing debug information
  private void debug(String logline, Object... vars) {
    if (hasOption(options, DEBUG)) {
      System.out.format("%s: ", Thread.currentThread().getStackTrace()[2].getLineNumber());
      System.out.format(logline + "\n", vars);
    }
  }

  private static class PlType {

    private String open;
    private String close;

    PlType(String open, String close) {
      this.open = open;
      this.close = close;
    }

  }

  private static class PlTypePattern {

    private Character type;
    private int reStart;
    private int reEnd;
    private String open;
    private String close;

    private PlTypePattern(Character type, int reStart, String open, String close) {
      this.type = type;
      this.reStart = reStart;
      this.open = open;
      this.close = close;
    }

  }

  private static class MinimatchPattern {

    private Object pattern;
    private boolean hasMagic;

    private String glob;
    private String src;

    private MinimatchPattern() {}

    private MinimatchPattern(String pattern, boolean hasMagic) {
      this.pattern = pattern;
      this.hasMagic = hasMagic;
    }

    private MinimatchPattern(String pattern) {
      this.pattern = pattern;
    }

    private MinimatchPattern(Pattern pattern) {
      this.pattern = pattern;
    }

    private boolean isString() {
      return pattern != null && pattern instanceof String;
    }

    private String asString() {
      if (!isString()) {
        return null;
      }
      return (String) pattern;
    }

    private boolean isPattern() {
      return pattern != null && pattern instanceof Pattern;
    }

    private Pattern asPattern() {
      if (!isPattern()) {
        return null;
      }
      return (Pattern) pattern;
    }

    @Override
    public String toString() {
      return "{pattern=" + pattern + ", hasMagic=" + hasMagic + ", glob=" + glob + ", src=" + src + "}";
    }

  }

}
