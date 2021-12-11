package com.github.jshaptic.minimatch.test;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.github.jshaptic.minimatch.Minimatch;

public class ExtglobEndingWithStateCharTest extends Assert
{
	@Test
	public void testExtglobEndingWithStateChar()
	{
		assertFalse(Minimatch.minimatch("ax", "a?(b*)"));
		assertTrue(Minimatch.minimatch("ax", "?(a*|b)"));
	}
}