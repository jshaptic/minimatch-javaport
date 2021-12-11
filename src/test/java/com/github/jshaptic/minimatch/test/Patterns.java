package com.github.jshaptic.minimatch.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.github.jshaptic.minimatch.Minimatch;

public class Patterns
{
	private final List<String> files = new ArrayList<String>(Arrays.asList(
		"a", "b", "c", "d",
		"abc", "abd", "abe", "bb", "bcd",
		"ca", "cb", "dd", "de",
		"bdir/", "bdir/cfile"
	));
	
	private final List<TestPattern> patterns = Arrays.asList(
		new CommentPattern("http://www.bashcookbook.com/bashinfo/source/bash-1.14.7/tests/glob-test"),
		new TestPattern("a*", Arrays.asList("a", "abc", "abd", "abe")),
		new TestPattern("X*", Arrays.asList("X*"), Minimatch.NO_NULL),

		// allow null glob expansion
		new TestPattern("X*", Collections.emptyList()),

		// isaacs: Slightly different than bash/sh/ksh
		// \\* is not un-escaped to literal "*" in a failed match,
		// but it does make it get treated as a literal star
		new TestPattern("\\*", Arrays.asList("\\*"), Minimatch.NO_NULL),
		new TestPattern("\\**", Arrays.asList("\\**"), Minimatch.NO_NULL),
		new TestPattern("\\*\\*", Arrays.asList("\\*\\*"), Minimatch.NO_NULL),

		new TestPattern("b*/", Arrays.asList("bdir/")),
		new TestPattern("c*", Arrays.asList("c", "ca", "cb")),
		new TestPattern("**", files),

		new TestPattern("\\.\\./*/", Arrays.asList("\\.\\./*/"), Minimatch.NO_NULL),
		new TestPattern("s/\\..*//", Arrays.asList("s/\\..*//"), Minimatch.NO_NULL),

		new CommentPattern("legendary larry crashes bashes"),
		new TestPattern("/^root:/{s/^[^:]*:[^:]*:([^:]*).*$/\\1/", Arrays.asList("/^root:/{s/^[^:]*:[^:]*:([^:]*).*$/\\1/"), Minimatch.NO_NULL),
		new TestPattern("/^root:/{s/^[^:]*:[^:]*:([^:]*).*$/\u0001/", Arrays.asList("/^root:/{s/^[^:]*:[^:]*:([^:]*).*$/\u0001/"), Minimatch.NO_NULL),

		new CommentPattern("character classes"),
		new TestPattern("[a-c]b*", Arrays.asList("abc", "abd", "abe", "bb", "cb")),
		new TestPattern("[a-y]*[^c]", Arrays.asList("abd", "abe", "bb", "bcd", "bdir/", "ca", "cb", "dd", "de")),
		new TestPattern("a*[^c]", Arrays.asList("abd", "abe")),
		new FunctionPattern(() -> { files.addAll(Arrays.asList("a-b", "aXb")); }),
		new TestPattern("a[X-]b", Arrays.asList("a-b", "aXb")),
		new FunctionPattern(() -> { files.addAll(Arrays.asList(".x", ".y")); }),
		new TestPattern("[^a-c]*", Arrays.asList("d", "dd", "de")),
		new FunctionPattern(() -> { files.addAll(Arrays.asList("a*b/", "a*b/ooo")); }),
		new TestPattern("a\\*b/*", Arrays.asList("a*b/ooo")),
		new TestPattern("a\\*?/*", Arrays.asList("a*b/ooo")),
		new TestPattern("*\\\\!*", Collections.emptyList(), 0, Arrays.asList("echo !7")),
		new TestPattern("*\\!*", Arrays.asList("echo !7"), 0, Arrays.asList("echo !7")),
		new TestPattern("*.\\*", Arrays.asList("r.*"), 0, Arrays.asList("r.*")),
		new TestPattern("a[b]c", Arrays.asList("abc")),
		new TestPattern("a[\\b]c", Arrays.asList("abc")),
		new TestPattern("a?c", Arrays.asList("abc")),
		new TestPattern("a\\*c", Collections.emptyList(), 0, Arrays.asList("abc")),
		new TestPattern("", Arrays.asList(""), 0, Arrays.asList("")),

		new CommentPattern("http://www.opensource.apple.com/source/bash/bash-23/bash/tests/glob-test"),
		new FunctionPattern(() -> { files.addAll(Arrays.asList("man/", "man/man1/", "man/man1/bash.1")); }),
		new TestPattern("*/man*/bash.*", Arrays.asList("man/man1/bash.1")),
		new TestPattern("man/man1/bash.1", Arrays.asList("man/man1/bash.1")),
		new TestPattern("a***c", Arrays.asList("abc"), 0, Arrays.asList("abc")),
		new TestPattern("a*****?c", Arrays.asList("abc"), 0, Arrays.asList("abc")),
		new TestPattern("?*****??", Arrays.asList("abc"), 0, Arrays.asList("abc")),
		new TestPattern("*****??", Arrays.asList("abc"), 0, Arrays.asList("abc")),
		new TestPattern("?*****?c", Arrays.asList("abc"), 0, Arrays.asList("abc")),
		new TestPattern("?***?****c", Arrays.asList("abc"), 0, Arrays.asList("abc")),
		new TestPattern("?***?****?", Arrays.asList("abc"), 0, Arrays.asList("abc")),
		new TestPattern("?***?****", Arrays.asList("abc"), 0, Arrays.asList("abc")),
		new TestPattern("*******c", Arrays.asList("abc"), 0, Arrays.asList("abc")),
		new TestPattern("*******?", Arrays.asList("abc"), 0, Arrays.asList("abc")),
		new TestPattern("a*cd**?**??k", Arrays.asList("abcdecdhjk"), 0, Arrays.asList("abcdecdhjk")),
		new TestPattern("a**?**cd**?**??k", Arrays.asList("abcdecdhjk"), 0, Arrays.asList("abcdecdhjk")),
		new TestPattern("a**?**cd**?**??k***", Arrays.asList("abcdecdhjk"), 0, Arrays.asList("abcdecdhjk")),
		new TestPattern("a**?**cd**?**??***k", Arrays.asList("abcdecdhjk"), 0, Arrays.asList("abcdecdhjk")),
		new TestPattern("a**?**cd**?**??***k**", Arrays.asList("abcdecdhjk"), 0, Arrays.asList("abcdecdhjk")),
		new TestPattern("a****c**?**??*****", Arrays.asList("abcdecdhjk"), 0, Arrays.asList("abcdecdhjk")),
		new TestPattern("[-abc]", Arrays.asList("-"), 0, Arrays.asList("-")),
		new TestPattern("[abc-]", Arrays.asList("-"), 0, Arrays.asList("-")),
		new TestPattern("\\", Arrays.asList("\\"), 0, Arrays.asList("\\")),
		new TestPattern("[\\\\]", Arrays.asList("\\"), 0, Arrays.asList("\\")),
		new TestPattern("[[]", Arrays.asList("["), 0, Arrays.asList("[")),
		new TestPattern("[", Arrays.asList("["), 0, Arrays.asList("[")),
		new TestPattern("[*", Arrays.asList("[abc"), 0, Arrays.asList("[abc")),

		new CommentPattern("a right bracket shall lose its special meaning and\n" +
			"represent itself in a bracket expression if it occurs\n" +
			"first in the list.  -- POSIX.2 2.8.3.2"),
		new TestPattern("[]]", Arrays.asList("]"), 0, Arrays.asList("]")),
		new TestPattern("[]-]", Arrays.asList("]"), 0, Arrays.asList("]")),
		new TestPattern("[a-z]", Arrays.asList("p"), 0, Arrays.asList("p")),
		new TestPattern("??**********?****?", Collections.emptyList(), 0, Arrays.asList("abc")),
		new TestPattern("??**********?****c", Collections.emptyList(), 0, Arrays.asList("abc")),
		new TestPattern("?************c****?****", Collections.emptyList(), 0, Arrays.asList("abc")),
		new TestPattern("*c*?**", Collections.emptyList(), 0, Arrays.asList("abc")),
		new TestPattern("a*****c*?**", Collections.emptyList(), 0, Arrays.asList("abc")),
		new TestPattern("a********???*******", Collections.emptyList(), 0, Arrays.asList("abc")),
		new TestPattern("[]", Collections.emptyList(), 0, Arrays.asList("a")),
		new TestPattern("[abc", Collections.emptyList(), 0, Arrays.asList("[")),

		new CommentPattern("nocase tests"),
		new TestPattern("XYZ", Arrays.asList("xYz"), Minimatch.NO_CASE, Arrays.asList("xYz", "ABC", "IjK")),
		new TestPattern("ab*", Arrays.asList("ABC"), Minimatch.NO_CASE, Arrays.asList("xYz", "ABC", "IjK")),
		new TestPattern("[ia]?[ck]", Arrays.asList("ABC", "IjK"), Minimatch.NO_CASE, Arrays.asList("xYz", "ABC", "IjK")),

		// [ pattern, [matches], MM opts, files, TAP opts]
		new CommentPattern("onestar/twostar"),
		new TestPattern("{/*,*}", Collections.emptyList(), 0, Arrays.asList("/asdf/asdf/asdf")),
		new TestPattern("{/?,*}", Arrays.asList("/a", "bb"), 0, Arrays.asList("/a", "/b/b", "/a/b/c", "bb")),

		new CommentPattern("dots should not match unless requested"),
		new TestPattern("**", Arrays.asList("a/b"), 0, Arrays.asList("a/b", "a/.d", ".a/.d")),

		// .. and . can only match patterns starting with .,
		// even when options.dot is set.
		new FunctionPattern(() -> { files.addAll(Arrays.asList("a/./b", "a/../b", "a/c/b", "a/.d/b")); }),
		new TestPattern("a/*/b", Arrays.asList("a/c/b", "a/.d/b"), Minimatch.DOT),
		new TestPattern("a/.*/b", Arrays.asList("a/./b", "a/../b", "a/.d/b"), Minimatch.DOT),
		new TestPattern("a/*/b", Arrays.asList("a/c/b"), 0),
		new TestPattern("a/.*/b", Arrays.asList("a/./b", "a/../b", "a/.d/b"), 0),

		// this also tests that changing the options needs
		// to change the cache key, even if the pattern is
		// the same!
		new TestPattern("**", Arrays.asList("a/b", "a/.d", ".a/.d"), Minimatch.DOT, Arrays.asList(".a/.d", "a/.d", "a/b")),

		new CommentPattern("paren sets cannot contain slashes"),
		new TestPattern("*(a/b)", Arrays.asList("*(a/b)"), Minimatch.NO_NULL, Arrays.asList("a/b")),

		// brace sets trump all else.
		//
		// invalid glob pattern.  fails on bash4 and bsdglob.
		// however, in this implementation, it's easier just
		// to do the intuitive thing, and let brace-expansion
		// actually come before parsing any extglob patterns,
		// like the documentation seems to say.
		//
		// MINIMATCH_XXX: if anyone complains about this, either fix it
		// or tell them to grow up and stop complaining.
		//
		// bash/bsdglob says this:
		// , ["*(a|{b),c)}", ["*(a|{b),c)}"], {}, ["a", "ab", "ac", "ad"]]
		// but we do this instead:
		new TestPattern("*(a|{b),c)}", Arrays.asList("a", "ab", "ac"), 0, Arrays.asList("a", "ab", "ac", "ad")),

		// test partial parsing in the presence of comment/negation chars
		new TestPattern("[!a*", Arrays.asList("[!ab"), 0, Arrays.asList("[!ab", "[ab")),
		new TestPattern("[#a*", Arrays.asList("[#ab"), 0, Arrays.asList("[#ab", "[ab")),

		// like: {a,b|c\\,d\\\|e} except it's unclosed, so it has to be escaped.
		new TestPattern("+(a|*\\|c\\\\|d\\\\\\|e\\\\\\\\|f\\\\\\\\\\|g",
			Arrays.asList("+(a|b\\|c\\\\|d\\\\|e\\\\\\\\|f\\\\\\\\|g"),
			0,
			Arrays.asList("+(a|b\\|c\\\\|d\\\\|e\\\\\\\\|f\\\\\\\\|g", "a", "b\\c")),

		// crazy nested {,,} and *(||) tests.
		new FunctionPattern(() -> {
			files.clear();
			files.addAll(Arrays.asList(
				"a", "b", "c", "d", "ab", "ac", "ad", "bc", "cb", "bc,d",
				"c,db", "c,d", "d)", "(b|c", "*(b|c", "b|c", "b|cc", "cb|c",
				"x(a|b|c)", "x(a|c)", "(a|b|c)", "(a|c)"
			));
		}),
		new TestPattern("*(a|{b,c})", Arrays.asList("a", "b", "c", "ab", "ac")),
		new TestPattern("{a,*(b|c,d)}", Arrays.asList("a", "(b|c", "*(b|c", "d)")),
		// a
		// *(b|c)
		// *(b|d)
		new TestPattern("{a,*(b|{c,d})}", Arrays.asList("a", "b", "bc", "cb", "c", "d")),
		new TestPattern("*(a|{b|c,c})", Arrays.asList("a", "b", "c", "ab", "ac", "bc", "cb")),

		// test various flag settings.
		new TestPattern("*(a|{b|c,c})", Arrays.asList("x(a|b|c)", "x(a|c)", "(a|b|c)", "(a|c)"), Minimatch.NO_EXT),
		new TestPattern("a?b", Arrays.asList("x/y/acb", "acb/"), Minimatch.MATCH_BASE, Arrays.asList("x/y/acb", "acb/", "acb/d/e", "x/y/acb/d")),
		new TestPattern("#*", Arrays.asList("#a", "#b"), Minimatch.NO_COMMENT, Arrays.asList("#a", "#b", "c#d")),

		// begin channelling Boole and deMorgan...
		new CommentPattern("negation tests"),
		new FunctionPattern(() -> {
			files.clear();
			files.addAll(Arrays.asList("d", "e", "!ab", "!abc", "a!b", "\\!a"));
		}),

		// anything that is NOT a* matches.
		new TestPattern("!a*", Arrays.asList("\\!a", "d", "e", "!ab", "!abc")),

		// anything that IS !a* matches.
		new TestPattern("!a*", Arrays.asList("!ab", "!abc"), Minimatch.NO_NEGATE),

		// anything that IS a* matches
		new TestPattern("!!a*", Arrays.asList("a!b")),

		// anything that is NOT !a* matches
		new TestPattern("!\\!a*", Arrays.asList("a!b", "d", "e", "\\!a")),

		// negation nestled within a pattern
		new FunctionPattern(() -> {
			files.clear();
			files.addAll(Arrays.asList(
				"foo.js",
				"foo.bar",
				"foo.js.js",
				"blar.js",
				"foo.",
				"boo.js.boo"
			));
		}),
		// last one is tricky! * matches foo, . matches ., and 'js.js' != 'js'
		// copy bash 4.3 behavior on this.
		new TestPattern("*.!(js)", Arrays.asList("foo.bar", "foo.", "boo.js.boo", "foo.js.js") ),

		new CommentPattern("https://github.com/isaacs/minimatch/issues/5"),
		new FunctionPattern(() -> {
			files.clear();
			files.addAll(Arrays.asList(
				"a/b/.x/c", "a/b/.x/c/d", "a/b/.x/c/d/e", "a/b/.x", "a/b/.x/",
				"a/.x/b", ".x", ".x/", ".x/a", ".x/a/b", "a/.x/b/.x/c", ".x/.x"
			));
		}),
		new TestPattern("**/.x/**", 
			Arrays.asList(".x/", ".x/a", ".x/a/b", "a/.x/b", "a/b/.x/", "a/b/.x/c", "a/b/.x/c/d", "a/b/.x/c/d/e")),

		new CommentPattern("https://github.com/isaacs/minimatch/issues/59"),
		new TestPattern("[z-a]", Collections.emptyList()),
		new TestPattern("a/[2015-03-10T00:23:08.647Z]/z", Collections.emptyList()),
		new TestPattern("[a-0][a-\u0100]", Collections.emptyList())
	);
	
	private static final List<String> regexps = new ArrayList<String>();
	static
	{
		regexps.add("/^(?:(?=.)a[^/]*?)$/");
		regexps.add("/^(?:(?=.)X[^/]*?)$/");
		regexps.add("/^(?:(?=.)X[^/]*?)$/");
		regexps.add("/^(?:\\*)$/");
		regexps.add("/^(?:(?=.)\\*[^/]*?)$/");
		regexps.add("/^(?:\\*\\*)$/");
		regexps.add("/^(?:(?=.)b[^/]*?\\/)$/");
		regexps.add("/^(?:(?=.)c[^/]*?)$/");
		regexps.add("/^(?:(?:(?!(?:\\/|^)\\.).)*?)$/");
		regexps.add("/^(?:\\.\\.\\/(?!\\.)(?=.)[^/]*?\\/)$/");
		regexps.add("/^(?:s\\/(?=.)\\.\\.[^/]*?\\/)$/");
		regexps.add("/^(?:\\/\\^root:\\/\\{s\\/(?=.)\\^[^:][^/]*?:[^:][^/]*?:\\([^:]\\)[^/]*?\\.[^/]*?\\$\\/1\\/)$/");
		regexps.add("/^(?:\\/\\^root:\\/\\{s\\/(?=.)\\^[^:][^/]*?:[^:][^/]*?:\\([^:]\\)[^/]*?\\.[^/]*?\\$\\/\u0001\\/)$/");
		regexps.add("/^(?:(?!\\.)(?=.)[a-c]b[^/]*?)$/");
		regexps.add("/^(?:(?!\\.)(?=.)[a-y][^/]*?[^c])$/");
		regexps.add("/^(?:(?=.)a[^/]*?[^c])$/");
		regexps.add("/^(?:(?=.)a[X-]b)$/");
		regexps.add("/^(?:(?!\\.)(?=.)[^a-c][^/]*?)$/");
		regexps.add("/^(?:a\\*b\\/(?!\\.)(?=.)[^/]*?)$/");
		regexps.add("/^(?:(?=.)a\\*[^/]\\/(?!\\.)(?=.)[^/]*?)$/");
		regexps.add("/^(?:(?!\\.)(?=.)[^/]*?\\\\\\![^/]*?)$/");
		regexps.add("/^(?:(?!\\.)(?=.)[^/]*?\\![^/]*?)$/");
		regexps.add("/^(?:(?!\\.)(?=.)[^/]*?\\.\\*)$/");
		regexps.add("/^(?:(?=.)a[b]c)$/");
		regexps.add("/^(?:(?=.)a[b]c)$/");
		regexps.add("/^(?:(?=.)a[^/]c)$/");
		regexps.add("/^(?:a\\*c)$/");
		regexps.add("false");
		regexps.add("/^(?:(?!\\.)(?=.)[^/]*?\\/(?=.)man[^/]*?\\/(?=.)bash\\.[^/]*?)$/");
		regexps.add("/^(?:man\\/man1\\/bash\\.1)$/");
		regexps.add("/^(?:(?=.)a[^/]*?[^/]*?[^/]*?c)$/");
		regexps.add("/^(?:(?=.)a[^/]*?[^/]*?[^/]*?[^/]*?[^/]*?[^/]c)$/");
		regexps.add("/^(?:(?!\\.)(?=.)[^/][^/]*?[^/]*?[^/]*?[^/]*?[^/]*?[^/][^/])$/");
		regexps.add("/^(?:(?!\\.)(?=.)[^/]*?[^/]*?[^/]*?[^/]*?[^/]*?[^/][^/])$/");
		regexps.add("/^(?:(?!\\.)(?=.)[^/][^/]*?[^/]*?[^/]*?[^/]*?[^/]*?[^/]c)$/");
		regexps.add("/^(?:(?!\\.)(?=.)[^/][^/]*?[^/]*?[^/]*?[^/][^/]*?[^/]*?[^/]*?[^/]*?c)$/");
		regexps.add("/^(?:(?!\\.)(?=.)[^/][^/]*?[^/]*?[^/]*?[^/][^/]*?[^/]*?[^/]*?[^/]*?[^/])$/");
		regexps.add("/^(?:(?!\\.)(?=.)[^/][^/]*?[^/]*?[^/]*?[^/][^/]*?[^/]*?[^/]*?[^/]*?)$/");
		regexps.add("/^(?:(?!\\.)(?=.)[^/]*?[^/]*?[^/]*?[^/]*?[^/]*?[^/]*?[^/]*?c)$/");
		regexps.add("/^(?:(?!\\.)(?=.)[^/]*?[^/]*?[^/]*?[^/]*?[^/]*?[^/]*?[^/]*?[^/])$/");
		regexps.add("/^(?:(?=.)a[^/]*?cd[^/]*?[^/]*?[^/][^/]*?[^/]*?[^/][^/]k)$/");
		regexps.add("/^(?:(?=.)a[^/]*?[^/]*?[^/][^/]*?[^/]*?cd[^/]*?[^/]*?[^/][^/]*?[^/]*?[^/][^/]k)$/");
		regexps.add("/^(?:(?=.)a[^/]*?[^/]*?[^/][^/]*?[^/]*?cd[^/]*?[^/]*?[^/][^/]*?[^/]*?[^/][^/]k[^/]*?[^/]*?[^/]*?)$/");
		regexps.add("/^(?:(?=.)a[^/]*?[^/]*?[^/][^/]*?[^/]*?cd[^/]*?[^/]*?[^/][^/]*?[^/]*?[^/][^/][^/]*?[^/]*?[^/]*?k)$/");
		regexps.add("/^(?:(?=.)a[^/]*?[^/]*?[^/][^/]*?[^/]*?cd[^/]*?[^/]*?[^/][^/]*?[^/]*?[^/][^/][^/]*?[^/]*?[^/]*?k[^/]*?[^/]*?)$/");
		regexps.add("/^(?:(?=.)a[^/]*?[^/]*?[^/]*?[^/]*?c[^/]*?[^/]*?[^/][^/]*?[^/]*?[^/][^/][^/]*?[^/]*?[^/]*?[^/]*?[^/]*?)$/");
		regexps.add("/^(?:(?!\\.)(?=.)[-abc])$/");
		regexps.add("/^(?:(?!\\.)(?=.)[abc-])$/");
		regexps.add("/^(?:\\\\)$/");
		regexps.add("/^(?:(?!\\.)(?=.)[\\\\])$/");
		regexps.add("/^(?:(?!\\.)(?=.)[\\[])$/");
		regexps.add("/^(?:\\[)$/");
		regexps.add("/^(?:(?=.)\\[(?!\\.)(?=.)[^/]*?)$/");
		regexps.add("/^(?:(?!\\.)(?=.)[\\]])$/");
		regexps.add("/^(?:(?!\\.)(?=.)[\\]-])$/");
		regexps.add("/^(?:(?!\\.)(?=.)[a-z])$/");
		regexps.add("/^(?:(?!\\.)(?=.)[^/][^/][^/]*?[^/]*?[^/]*?[^/]*?[^/]*?[^/]*?[^/]*?[^/]*?[^/]*?[^/]*?[^/][^/]*?[^/]*?[^/]*?[^/]*?[^/])$/");
		regexps.add("/^(?:(?!\\.)(?=.)[^/][^/][^/]*?[^/]*?[^/]*?[^/]*?[^/]*?[^/]*?[^/]*?[^/]*?[^/]*?[^/]*?[^/][^/]*?[^/]*?[^/]*?[^/]*?c)$/");
		regexps.add("/^(?:(?!\\.)(?=.)[^/][^/]*?[^/]*?[^/]*?[^/]*?[^/]*?[^/]*?[^/]*?[^/]*?[^/]*?[^/]*?[^/]*?[^/]*?c[^/]*?[^/]*?[^/]*?[^/]*?[^/][^/]*?[^/]*?[^/]*?[^/]*?)$/");
		regexps.add("/^(?:(?!\\.)(?=.)[^/]*?c[^/]*?[^/][^/]*?[^/]*?)$/");
		regexps.add("/^(?:(?=.)a[^/]*?[^/]*?[^/]*?[^/]*?[^/]*?c[^/]*?[^/][^/]*?[^/]*?)$/");
		regexps.add("/^(?:(?=.)a[^/]*?[^/]*?[^/]*?[^/]*?[^/]*?[^/]*?[^/]*?[^/]*?[^/][^/][^/][^/]*?[^/]*?[^/]*?[^/]*?[^/]*?[^/]*?[^/]*?)$/");
		regexps.add("/^(?:\\[\\])$/");
		regexps.add("/^(?:\\[abc)$/");
		regexps.add("/^(?:(?=.)XYZ)$/i");
		regexps.add("/^(?:(?=.)ab[^/]*?)$/i");
		regexps.add("/^(?:(?!\\.)(?=.)[ia][^/][ck])$/i");
		regexps.add("/^(?:\\/(?!\\.)(?=.)[^/]*?|(?!\\.)(?=.)[^/]*?)$/");
		regexps.add("/^(?:\\/(?!\\.)(?=.)[^/]|(?!\\.)(?=.)[^/]*?)$/");
		regexps.add("/^(?:(?:(?!(?:\\/|^)\\.).)*?)$/");
		regexps.add("/^(?:a\\/(?!(?:^|\\/)\\.{1,2}(?:$|\\/))(?=.)[^/]*?\\/b)$/");
		regexps.add("/^(?:a\\/(?=.)\\.[^/]*?\\/b)$/");
		regexps.add("/^(?:a\\/(?!\\.)(?=.)[^/]*?\\/b)$/");
		regexps.add("/^(?:a\\/(?=.)\\.[^/]*?\\/b)$/");
		regexps.add("/^(?:(?:(?!(?:\\/|^)(?:\\.{1,2})($|\\/)).)*?)$/");
		regexps.add("/^(?:(?!\\.)(?=.)[^/]*?\\(a\\/b\\))$/");
		regexps.add("/^(?:(?!\\.)(?=.)(?:a|b)*|(?!\\.)(?=.)(?:a|c)*)$/");
		regexps.add("/^(?:(?=.)\\[(?=.)\\!a[^/]*?)$/");
		regexps.add("/^(?:(?=.)\\[(?=.)#a[^/]*?)$/");
		regexps.add("/^(?:(?=.)\\+\\(a\\|[^/]*?\\|c\\\\\\\\\\|d\\\\\\\\\\|e\\\\\\\\\\\\\\\\\\|f\\\\\\\\\\\\\\\\\\|g)$/");
		regexps.add("/^(?:(?!\\.)(?=.)(?:a|b)*|(?!\\.)(?=.)(?:a|c)*)$/");
		regexps.add("/^(?:a|(?!\\.)(?=.)[^/]*?\\(b\\|c|d\\))$/");
		regexps.add("/^(?:a|(?!\\.)(?=.)(?:b|c)*|(?!\\.)(?=.)(?:b|d)*)$/");
		regexps.add("/^(?:(?!\\.)(?=.)(?:a|b|c)*|(?!\\.)(?=.)(?:a|c)*)$/");
		regexps.add("/^(?:(?!\\.)(?=.)[^/]*?\\(a\\|b\\|c\\)|(?!\\.)(?=.)[^/]*?\\(a\\|c\\))$/");
		regexps.add("/^(?:(?=.)a[^/]b)$/");
		regexps.add("/^(?:(?=.)#[^/]*?)$/");
		regexps.add("/^(?!^(?:(?=.)a[^/]*?)$).*$/");
		regexps.add("/^(?:(?=.)\\!a[^/]*?)$/");
		regexps.add("/^(?:(?=.)a[^/]*?)$/");
		regexps.add("/^(?!^(?:(?=.)\\!a[^/]*?)$).*$/");
		regexps.add("/^(?:(?!\\.)(?=.)[^\\/]*?\\.(?:(?!(?:js)$)[^\\/]*?))$/");
		regexps.add("/^(?:(?:(?!(?:\\/|^)\\.).)*?\\/\\.x\\/(?:(?!(?:\\/|^)\\.).)*?)$/");
		regexps.add("/^(?:\\[z\\-a\\])$/");
		regexps.add("/^(?:a\\/\\[2015\\-03\\-10T00:23:08\\.647Z\\]\\/z)$/");
		regexps.add("/^(?:(?=.)\\[a-0\\][a-Ā])$/");
	}
	
	public List<TestPattern> getPatterns()
	{
		return Collections.unmodifiableList(patterns);
	}
	
	public List<String> getRegexps()
	{
		return Collections.unmodifiableList(regexps);
	}
	
	public List<String> getFiles()
	{
		return Collections.unmodifiableList(files);
	}
	
	// PORT INFO: created dedicated classes for pattern list
	public static class TestPattern
	{
		private String pattern;
		private List<String> expect;
		private int options = 0;
		private List<String> files = null;
		
		private TestPattern() {};
		
		TestPattern(String pattern, List<String> expect)
		{
			this.pattern = pattern;
			this.expect = expect;
		}
		
		TestPattern(String pattern, List<String> expect, int options)
		{
			this.pattern = pattern;
			this.expect = expect;
			this.options = options;
		}
		
		TestPattern(String pattern, List<String> expect, int options, List<String> files)
		{
			this.pattern = pattern;
			this.expect = expect;
			this.options = options;
			this.files = files;
		}

		public String getPattern()
		{
			return pattern;
		}

		public List<String> getExpect()
		{
			return expect;
		}

		public int getOptions()
		{
			return options;
		}

		public List<String> getFiles()
		{
			return files;
		}
	}
	
	public static class CommentPattern extends TestPattern
	{
		private String comment;
		
		CommentPattern(String comment)
		{
			this.comment = comment;
		}
		
		@Override
		public String toString()
		{
			return comment;
		}
	}
	
	public static class FunctionPattern extends TestPattern
	{
		private Runnable func;
		
		FunctionPattern(Runnable func)
		{
			this.func = func;
		}
		
		public void run()
		{
			func.run();
		}
	}
}