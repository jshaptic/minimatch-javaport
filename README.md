# minimatch

[![Maintainability](https://api.codeclimate.com/v1/badges/535b2d6e4b0fe84a2dc3/maintainability)](https://codeclimate.com/github/jshaptic/minimatch-javaport/maintainability)
[![Test Coverage](https://api.codeclimate.com/v1/badges/535b2d6e4b0fe84a2dc3/test_coverage)](https://codeclimate.com/github/jshaptic/minimatch-javaport/test_coverage)
![Maven Central](https://img.shields.io/maven-central/v/com.github.jshaptic/minimatch-javaport)
[![javadoc](https://javadoc.io/badge2/com.github.jshaptic/minimatch-javaport/javadoc.svg)](https://javadoc.io/doc/com.github.jshaptic/minimatch-javaport)
[![MIT License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

A minimal matching utility.

## Usage

```java
Minimatch.minimatch("bar.foo", "*.foo"); // true!
Minimatch.minimatch("bar.foo", "*.bar"); // false!
Minimatch.minimatch("bar.foo", "*.+(bar|foo)", Minimatch.DEBUG); // true, and noisy!
```

## Features

Supports these glob features:

- Brace Expansion
- Extended glob matching
- "Globstar" `**` matching

See:

- `man sh`
- `man bash`
- `man 3 fnmatch`
- `man 5 gitignore`

## Minimatch Class

Create a minimatch object by instantiating the `Minimatch` class.

```java
Minimatch mm = new Minimatch(pattern, options);
```

### Instance Methods

#### makeRe()

Generate the `Pattern` object if necessary, and return it. Will return `null` if the pattern is invalid.

#### match(fname)

Return true if the filename matches the pattern, or false otherwise.

### Static Methods

#### Minimatch.minimatch(path, pattern, options)

Tests a path against the pattern using the options.

```java
boolean isJS = Minimatch.minimatch(file, "*.js", Minimatch.MATCH_BASE);
```

#### Minimatch.filter(pattern, options)

Returns a function that tests its supplied argument, suitable for use with `Stream::filter`. Example:

```java
List<String> javascripts = fileList.stream().filter(
  Minimatch.filter("*.js", Minimatch.MATCH_BASE)
).collect(Collectors.toList());
```

#### Minimatch.match(list, pattern, options)

Match against the list of files, in the style of fnmatch or glob. If nothing is matched, and
`Minimatch.NO_NULL` is set, then return a list containing the pattern itself.

```java
List<String> javascripts = Minimatch.match(fileList, "*.js", Minimatch.MATCH_BASE);
```

#### Minimatch.makeRe(pattern, options)

Make a regular expression object from the pattern.

### Options

All options are switched off by default.

#### Minimatch.DEBUG

Dump a ton of stuff to stderr.

#### Minimatch.NO_BRACE

Do not expand `{a,b}` and `{1..3}` brace sets.

#### Minimatch.NO_GLOBSTAR

Disable `**` matching against multiple folder names.

#### Minimatch.DOT

Allow patterns to match filenames starting with a period, even if
the pattern does not explicitly have a period in that spot.

Note that by default, `a/**/b` will **not** match `a/.d/b`, unless `dot`
is set.

#### Minimatch.NO_EXT

Disable "extglob" style patterns like `+(a|b)`.

#### Minimatch.NO_CASE

Perform a case-insensitive match.

#### Minimatch.NO_NULL

When a match is not found by `Minimatch.match`, return a list containing
the pattern itself if this option is set. When not set, an empty list
is returned if there are no matches.

#### Minimatch.MATCH_BASE

If set, then patterns without slashes will be matched
against the basename of the path if it contains slashes. For example,
`a?b` would match the path `/xyz/123/acb`, but not `/xyz/acb/123`.

#### Minimatch.NO_COMMENT

Suppress the behavior of treating `#` at the start of a pattern as a
comment.

#### Minimatch.NO_NEGATE

Suppress the behavior of treating a leading `!` character as negation.

#### Minimatch.FLIP_NEGATE

Returns from negate expressions the same as if they were not negated.
(Ie, true on a hit, false on a miss.)

## Comparisons to other fnmatch/glob implementations

While strict compliance with the existing standards is a worthwhile
goal, some discrepancies exist between minimatch and other
implementations, and are intentional.

If the pattern starts with a `!` character, then it is negated. Set the
`NO_NEGATE` flag to suppress this behavior, and treat leading `!`
characters normally. This is perhaps relevant if you wish to start the
pattern with a negative extglob pattern like `!(a|B)`. Multiple `!`
characters at the start of a pattern will negate the pattern multiple
times.

If a pattern starts with `#`, then it is treated as a comment, and
will not match anything. Use `\#` to match a literal `#` at the
start of a line, or set the `NO_COMMENT` flag to suppress this behavior.

The double-star character `**` is supported by default, unless the
`NO_GLOBSTAR` flag is set. This is supported in the manner of bsdglob
and bash 4.1, where `**` only has special significance if it is the only
thing in a path part. That is, `a/**/b` will match `a/x/y/b`, but
`a/**b` will not.

If an escaped pattern has no matches, and the `NO_NULL` flag is set,
then `Minimatch.match` returns the pattern as-provided, rather than
interpreting the character escapes. For example,
`Minimatch.match(new ArrayList<String>(), "\\*a\\?")` will return `"\\*a\\?"` rather than
`"*a?"`. This is akin to setting the `nullglob` option in bash, except
that it does not resolve escaped pattern characters.

If brace expansion is not disabled, then it is performed before any
other interpretation of the glob pattern. Thus, a pattern like
`+(a|{b),c)}`, which would not be valid in bash or zsh, is expanded
**first** into the set of `+(a|b)` and `+(a|c)`, and those patterns are
checked for validity. Since those two are valid, matching proceeds.
