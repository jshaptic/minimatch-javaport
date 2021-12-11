package com.github.jshaptic.minimatch.test;

import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import com.github.jshaptic.minimatch.Minimatch;

public class TrickyNegationsTest extends Assert
{
	@Test
	public void testTrickyNegations()
	{
		SoftAssert t = new SoftAssert();
		
		Map<String, Map<String, Boolean>> cases = new HashMap<String, Map<String, Boolean>>();
		
		Map<String, Boolean> c = new HashMap<String, Boolean>();
		cases.put("bar.min.js", c);
		c.put("*.!(js|css)", true);
		c.put("!*.+(js|css)", false);
		c.put("*.!(js|css)", true);
		
		c = new HashMap<String, Boolean>();
		cases.put("a-integration-test.js", c);
		c.put("*.!(j)",  true);
		c.put("!(*-integration-test.js)", false);
		c.put("*-!(integration-)test.js", true);
		c.put("*-!(integration)-test.js", false);
		c.put("*!(-integration)-test.js", true);
		c.put("*!(-integration-)test.js", true);
		c.put("*!(integration)-test.js", true);
		c.put("*!(integration-test).js", true);
		c.put("*-!(integration-test).js", true);
		c.put("*-!(integration-test.js)", true);
		c.put("*-!(integra)tion-test.js", false);
		c.put("*-integr!(ation)-test.js", false);
		c.put("*-integr!(ation-t)est.js", false);
		c.put("*-i!(ntegration-)test.js", false);
		c.put("*i!(ntegration-)test.js", true);
		c.put("*te!(gration-te)st.js", true);
		c.put("*-!(integration)?test.js", false);
		c.put("*?!(integration)?test.js", true);
		
		c = new HashMap<String, Boolean>();
		cases.put("foo-integration-test.js", c);
		c.put("foo-integration-test.js", true);
		c.put("!(*-integration-test.js)", false);
		
		c = new HashMap<String, Boolean>();
		cases.put("foo.jszzz.js", c);
		c.put("*.!(js).js", true);
		
		c = new HashMap<String, Boolean>();
		cases.put("asd.jss", c);
		c.put("*.!(js)", true);
		
		c = new HashMap<String, Boolean>();
		cases.put("asd.jss.xyz", c);
		c.put("*.!(js).!(xy)", true);
		
		c = new HashMap<String, Boolean>();
		cases.put("asd.jss.xy", c);
		c.put("*.!(js).!(xy)", false);
		
		c = new HashMap<String, Boolean>();
		cases.put("asd.js.xyz", c);
		c.put("*.!(js).!(xy)", false);
		
		c = new HashMap<String, Boolean>();
		cases.put("asd.js.xy", c);
		c.put("*.!(js).!(xy)", false);
		
		c = new HashMap<String, Boolean>();
		cases.put("asd.sjs.zxy", c);
		c.put("*.!(js).!(xy)", true);
		
		c = new HashMap<String, Boolean>();
		cases.put("asd..xyz", c);
		c.put("*.!(js).!(xy)", true);
		
		c = new HashMap<String, Boolean>();
		cases.put("asd..xy", c);
		c.put("*.!(js).!(xy)", false);
		c.put("*.!(js|x).!(xy)", false);
		
		c = new HashMap<String, Boolean>();
		cases.put("foo.js.js", c);
		c.put("*.!(js)", true);
		
		c = new HashMap<String, Boolean>();
		cases.put("testjson.json", c);
		c.put("*(*.json|!(*.js))", true);
		c.put("+(*.json|!(*.js))", true);
		c.put("@(*.json|!(*.js))", true);
		c.put("?(*.json|!(*.js))", true);
		
		c = new HashMap<String, Boolean>();
		cases.put("foojs.js", c);
		c.put("*(*.json|!(*.js))", false); // MINIMATCH_XXX bash 4.3 disagrees!
		c.put("+(*.json|!(*.js))", false); // MINIMATCH_XXX bash 4.3 disagrees!
		c.put("@(*.json|!(*.js))", false);
		c.put("?(*.json|!(*.js))", false);
		
		c = new HashMap<String, Boolean>();
		cases.put("other.bar", c);
		c.put("*(*.json|!(*.js))", true);
		c.put("+(*.json|!(*.js))", true);
		c.put("@(*.json|!(*.js))", true);
		c.put("?(*.json|!(*.js))", true);
		
		int options = Minimatch.NO_NEGATE;
		
		cases.keySet().stream().forEach(file -> {
			cases.get(file).keySet().stream().forEach(pattern -> {
				boolean res = cases.get(file).get(pattern);
				String s = file + " " + pattern;
				System.out.println(s);
				t.assertEquals(Minimatch.minimatch(file, pattern, options), res, s);
			});
		});
		System.out.println();
		t.assertAll();
	}
}