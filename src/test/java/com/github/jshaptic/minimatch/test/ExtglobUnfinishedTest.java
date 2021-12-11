package com.github.jshaptic.minimatch.test;

import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.github.jshaptic.minimatch.Minimatch;

public class ExtglobUnfinishedTest extends Assert
{
	private String[] types = "!?+*@".split("");
	
	@Test
	public void testExtglobUnfinished()
	{
		Arrays.stream(types).forEach(type -> {
			assertTrue(Minimatch.minimatch(type + "(a|B", type + "(a|B", Minimatch.NO_NEGATE));
			assertFalse(Minimatch.minimatch(type + "(a|B", "B", Minimatch.NO_NEGATE));
		});
	}
}