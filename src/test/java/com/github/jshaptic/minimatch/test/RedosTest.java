package com.github.jshaptic.minimatch.test;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.github.jshaptic.minimatch.Minimatch;

public class RedosTest extends Assert
{
	private String genstr(int len, String chr)
	{
		StringBuilder result = new StringBuilder();
		for (int i = 0; i <= len; i++)
		{
			result.append(chr);
		}
		
		return result.toString();
	}
	
	@Test
	public void testRedosWithinLimitsValidMatch()
	{
		String exploit = "!(" + genstr(1024 * 15, "\\") + "A";
		
		// within the limits, and valid match
		assertTrue(Minimatch.minimatch("A", exploit));
	}
	
	@Test
	public void testRedosWithinLimitsInvalidRegexp()
	{
		String exploit = "[!(" + genstr(1024 * 15, "\\") + "A";
		
		// within the limits, but results in an invalid regexp
		assertFalse(Minimatch.minimatch("A", exploit));
	}
	
	@Test(expectedExceptions = RuntimeException.class)
	public void testRedosThrowsError()
	{
		String exploit = "!(" + genstr(1024 * 64, "\\") + "A)";
		
		Minimatch.minimatch("A", exploit);
	}
}