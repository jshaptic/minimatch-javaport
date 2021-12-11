package com.github.jshaptic.minimatch.test;

import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import com.github.jshaptic.minimatch.Minimatch;

public class BraceExpandTest extends Assert
{	
	@Test
	public void testBraceExpansion()
	{
		SoftAssert t = new SoftAssert();
		
		List<Patterns.TestPattern> patterns = Arrays.asList(
			new Patterns.TestPattern("a{b,c{d,e},{f,g}h}x{y,z}", Arrays.asList(
				"abxy",
				"abxz",
				"acdxy",
				"acdxz",
				"acexy",
				"acexz",
				"afhxy",
				"afhxz",
				"aghxy",
				"aghxz"
			)),
			new Patterns.TestPattern("a{1..5}b", Arrays.asList(
				"a1b",
				"a2b",
				"a3b",
				"a4b",
				"a5b"
			)),
			new Patterns.TestPattern("a{b}c", Arrays.asList("a{b}c")),
			new Patterns.TestPattern("a{00..05}b", Arrays.asList(
				"a00b",
				"a01b",
				"a02b",
				"a03b",
				"a04b",
				"a05b"
			)),
			new Patterns.TestPattern("z{a,b},c}d", Arrays.asList("za,c}d", "zb,c}d")),
			new Patterns.TestPattern("z{a,b{,c}d", Arrays.asList("z{a,bd", "z{a,bcd")),
			new Patterns.TestPattern("a{b{c{d,e}f}g}h", Arrays.asList("a{b{cdf}g}h", "a{b{cef}g}h")),
			new Patterns.TestPattern("a{b{c{d,e}f{x,y}}g}h", Arrays.asList(
				"a{b{cdfx}g}h",
				"a{b{cdfy}g}h",
				"a{b{cefx}g}h",
				"a{b{cefy}g}h"
			)),
			new Patterns.TestPattern("a{b{c{d,e}f{x,y{}g}h", Arrays.asList(
				"a{b{cdfxh",
				"a{b{cdfy{}gh",
				"a{b{cefxh",
				"a{b{cefy{}gh"
			))
		);
		
		patterns.forEach(tc -> {
			String p = tc.getPattern();
			List<String> expect = tc.getExpect();
			System.out.println(p);
			t.assertEquals(Arrays.asList(Minimatch.braceExpand(p)), expect, p);
		});
		
		System.out.println();
		t.assertAll();
	}
}