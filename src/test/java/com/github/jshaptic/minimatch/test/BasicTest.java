package com.github.jshaptic.minimatch.test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ObjectUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import com.github.jshaptic.minimatch.Minimatch;

//http://www.bashcookbook.com/bashinfo/source/bash-1.14.7/tests/glob-test
//
//MINIMATCH_TODO: Some of these tests do very bad things with backslashes, and will
//most likely fail badly on windows.  They should probably be skipped.
public class BasicTest extends Assert
{
	private final static Pattern regEscape = Pattern.compile("([^\\\\])/");
	
	private Patterns p = new Patterns();
	private List<Patterns.TestPattern> patterns = p.getPatterns();
	private List<String> regexps = p.getRegexps();
	private List<String> files = p.getFiles();
	
	@Test
	public void testBasicTests()
	{
		SoftAssert t = new SoftAssert();
		AtomicInteger re = new AtomicInteger();
		
		// [ pattern, [matches], MM opts, files, TAP opts]
		patterns.forEach(c -> {
			if (c instanceof Patterns.FunctionPattern)
			{
				((Patterns.FunctionPattern)c).run();
				return;
			}
			if (c instanceof Patterns.CommentPattern)
			{
				System.out.println(c.toString());
				return;
			}
			
			String pattern = c.getPattern();
			List<String> expect = c.getExpect();
			expect.sort(this::alpha);
			int options = c.getOptions();
			List<String> f = ObjectUtils.defaultIfNull(c.getFiles(), files);
			
			// options.debug = true
			Minimatch m = new Minimatch(pattern, options);
			Pattern r = m.makeRe();
			
			String expectRe = regexps.get(re.getAndIncrement());
			expectRe = "/" + regEscape.matcher(expectRe.substring(1, expectRe.length() - 1)).replaceAll("$1\\/") + "/";
			String actualRe = r != null ? r.toString() : "false";
			actualRe = "/" + regEscape.matcher(actualRe).replaceAll("$1\\/") + "/";
			
			List<String> actual = Minimatch.match(f, pattern, options);
			actual.sort(this::alpha);
			
			System.out.println(pattern);
			t.assertEquals(actual, expect, buildMessage(pattern, expect, actualRe, f));
			
			t.assertEquals(actualRe, expectRe, buildMessage(pattern, null, actualRe, f));
		});
		
		System.out.println();
		t.assertAll();
	}
	
	// PORT INFO: no need to test for global leak
	
	private int alpha(String a, String b)
	{
		return a.compareTo(b);
	}
	
	// PORT INFO: added this method to print informative messages
	private String buildMessage(String pattern, List<String> expect, String re, List<String> files)
	{
		StringBuilder message = new StringBuilder();
		message.append(pattern);
		if (expect != null) message.append(" " + expect);
		message.append("\n	  re: " + re);
		message.append("\n	  files: " + files);
		message.append("\n	  pattern: " + pattern);
		message.append("\n	  ");
		return message.toString();
	}
}