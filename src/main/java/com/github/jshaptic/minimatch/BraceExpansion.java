package com.github.jshaptic.minimatch;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.BiPredicate;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class BraceExpansion
{
	private static final String escSlash = "\0SLASH" + Math.random() + "\0";
	private static final String escOpen = "\0OPEN" + Math.random() + "\0";
	private static final String escClose = "\0CLOSE" + Math.random() + "\0";
	private static final String escComma = "\0COMMA" + Math.random() + "\0";
	private static final String escPeriod = "\0PERIOD" + Math.random() + "\0";
	
	private static final Pattern numericSequence = Pattern.compile("^-?\\d+\\.\\.-?\\d+(?:\\.\\.-?\\d+)?$");
	private static final Pattern alphaSequence = Pattern.compile("^[a-zA-Z]\\.\\.[a-zA-Z](?:\\.\\.-?\\d+)?$");
	
	private BraceExpansion() {}
	
	private static int numeric(String str)
	{
		try
		{
			return Integer.parseInt(str, 10);
		}
		catch (NumberFormatException e)
		{
			return str.codePointAt(0);
		}
	}
	
	private static String escapeBraces(String str)
	{
		return StringUtils.replaceEach(str, new String[]{"\\\\", "\\{", "\\}", "\\,", "\\."},
			new String[]{escSlash, escOpen, escClose, escComma, escPeriod});
	}
	
	private static String unescapeBraces(String str)
	{
		return StringUtils.replaceEach(str, new String[]{escSlash, escOpen, escClose, escComma, escPeriod},
			new String[]{"\\", "{", "}", ",", "."});
	}
	
	// Basically just str.split(","), but handling cases
	// where we have nested braced sections, which should be
	// treated as individual members, like {a,{b,c},d}
	private static String[] parseCommaParts(String str)
	{
		if (str.isEmpty())
			return new String[] {""};
		
		BalancedMatch m = BalancedMatch.balanced("{", "}", str);
		
		if (m == null)
			return str.split(",", -1);
		
		String pre = m.getPre();
		String body = m.getBody();
		String post = m.getPost();
		String[] p = pre.split(",", -1);
		
		p[p.length-1] += "{" + body + "}";
		String[] postParts = parseCommaParts(post);
		if (post.length() > 0 && postParts.length > 0)
		{
			p[p.length-1] += postParts[0];
			p = Arrays.copyOf(p, p.length + postParts.length - 1);
			System.arraycopy(postParts, 1, p, p.length - (postParts.length - 1), postParts.length - 1);
		}
		
		return p;
	}
	
	public static String[] expand(String str)
	{
		// I don't know why Bash 4.3 does this, but it does.
		// Anything starting with {} will have the first two bytes preserved
		// but *only* at the top level, so {},a}b will not expand to anything,
		// but a{},b}c will be expanded to [a}c,abc].
		// One could argue that this is a bug in Bash, but since the goal of
		// this module is to match Bash's rules, we escape a leading {}
		if (str.startsWith("{}"))
		{
			str = "\\{\\}" + str.substring(2);
		}
		
		String[] expansions = expand(escapeBraces(str), true);
		for (int i = 0; i < expansions.length; i++)
			expansions[i] = unescapeBraces(expansions[i]);
		
		return expansions;
	}
	
	private static String embrace(String str)
	{
		return "{" + str + "}";
	}
	
	private static boolean isPadded(String el)
	{
		return (el.length() >= 3 && el.startsWith("-0") && Character.isDigit(el.charAt(2))) ||
			(el.length() >= 2 && el.startsWith("0") && Character.isDigit(el.charAt(1)));
	}
	
	private static boolean lte(int i, int y)
	{
		return i <= y;
	}
	
	private static boolean gte(int i, int y)
	{
		return i >= y;
	}
	
	private static String[] expand(String str, boolean isTop)
	{
		BalancedMatch m = BalancedMatch.balanced("{", "}", str);		
		if (m == null || m.getPre().endsWith("$")) return new String[]{str};
		
		boolean isNumericSequence = numericSequence.matcher(m.getBody()).find();
		boolean isAlphaSequence = alphaSequence.matcher(m.getBody()).find();
		boolean isSequence = isNumericSequence || isAlphaSequence;
		boolean isOptions = m.getBody().indexOf(',') >= 0;
		if (!isSequence && !isOptions)
		{
			// {a},b}
			if (m.getPost().indexOf(',') < m.getPost().indexOf('}'))
			{
				str = m.getPre() + "{" + m.getBody() + escClose + m.getPost();
				return expand(str, false);
			}
			return new String[]{str};
		}
		
		String[] n;
		if (isSequence)
		{
			n = StringUtils.split(m.getBody(), "..");
		}
		else
		{
			n = parseCommaParts(m.getBody());
			if (n.length == 1)
			{
				// x{{a,b}}y ==> x{a}y x{b}y
				n = expand(n[0], false);
				for (int i = 0; i < n.length; i++)
					n[i] = embrace(n[i]);
				if (n.length == 1)
				{
					String[] post = !m.getPost().isEmpty() ? expand(m.getPost(), false) : new String[] {""};
					for (int i = 0; i < post.length; i++)
						post[i] = m.getPre() + n[0] + post[i];
					return post;
				}
			}
		}
		
		// at this point, n is the parts, and we know it's not a comma set
		// with a single entry.
		
		// no need to expand pre, since it is guaranteed to be free of brace-sets
		String pre = m.getPre();
		String[] post = !m.getPost().isEmpty() ? expand(m.getPost(), false) : new String[] {""};
		
		String[] N;
		
		if (isSequence)
		{
			int x = numeric(n[0]);
			int y = numeric(n[1]);
			int width = Math.max(n[0].length(), n[1].length());
			int incr = n.length == 3 ? Math.abs(numeric(n[2])) : 1;
			BiPredicate<Integer, Integer> test = BraceExpansion::lte;
			boolean reverse = y < x;
			if (reverse)
			{
				incr *= -1;
				test = BraceExpansion::gte;
			}
			boolean pad = Arrays.stream(n).anyMatch(BraceExpansion::isPadded);
			
			N = new String[(Math.abs(x - y) + 1)/Math.abs(incr)]; 
			
			for (int i = x, j = 0; test.test(i, y); i += incr, j++)
			{
				String c = "";
				if (isAlphaSequence)
				{
					c = Character.toString((char)i);
					if (c.equals("\\")) c = "";
				}
				else
				{
					c = Integer.toString(i);
					if (pad)
					{
						int need = width - c.length();
						if (need > 0)
						{
							String z = String.join("", Collections.nCopies(need, "0"));
							if (i < 0)
								c = "-" + z + c.substring(1);
							else
								c = z + c;
						}
					}
				}
				N[j] = c;
			}
		}
		else
		{
			N = new String[0];
			for (int i = 0; i < n.length; i++)
			{
				String[] nnn = expand(n[i], false);
				N = Arrays.copyOf(N, N.length + nnn.length);
				System.arraycopy(nnn, 0, N,  N.length - nnn.length, nnn.length);
			}
		}
		
		String[] expansions = new String[N.length * post.length];
		
		int i = 0;
		for (int j = 0; j < N.length; j++)
		{
		    for (int k = 0; k < post.length; k++)
		    {
		    	String expansion = pre + N[j] + post[k];
		    	if (!isTop || isSequence || !expansion.isEmpty())
		    	{
		    		expansions[i] = expansion;
		    		i++;
		    	}
		    }
		}
		
		return Arrays.copyOf(expansions, i);
	}
}