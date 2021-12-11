package com.github.jshaptic.minimatch;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class BalancedMatch
{
	private int start;
	private int end;
	private String pre;
	private String body;
	private String post;
	
	private BalancedMatch(int start, int end, String pre, String body, String post)
	{
		this.start = start;
		this.end = end;
		this.pre = pre;
		this.body = body;
		this.post = post;
	}
	
	public int getStart()
	{
		return start;
	}

	public int getEnd()
	{
		return end;
	}

	public String getPre()
	{
		return pre;
	}

	public String getBody()
	{
		return body;
	}

	public String getPost()
	{
		return post;
	}
	
	public static BalancedMatch balanced(Pattern a, Pattern b, String str)
	{
		str = StringUtils.defaultString(str);
		return balanced(maybeMatch(a, str), maybeMatch(a, str), str);
	}

	public static BalancedMatch balanced(String a, String b, String str)
	{
		a = StringUtils.defaultString(a);
		b = StringUtils.defaultString(b);
		str = StringUtils.defaultString(str);
		
		int[] r = range(a, b, str);
		
		return r != null ? new BalancedMatch(r[0], r[1], str.substring(0, r[0]), str.substring(r[0] + a.length(), r[1]), str.substring(r[1] + b.length())) : null;
	}
	
	private static String maybeMatch(Pattern reg, String str)
	{
		Matcher m = reg.matcher(str);
		return m.find() ? m.group(0) : null;
	}
	
	private static int[] range(String a, String b, String str)
	{
		int[] result = null;
		Deque<Integer> begs = null;
		int ai = str.indexOf(a);
		int bi = str.indexOf(b, ai + 1);
		int i = ai;
		
		if (ai >= 0 && bi >= 0)
		{
			begs = new ArrayDeque<Integer>();
			int left = str.length();
			int right = -1;
			
			while (i >= 0 && result == null)
			{
				if (i == ai)
				{
					begs.push(i);
					ai = str.indexOf(a, i + 1);
				}
				else if (begs.size() == 1)
				{
					result = new int[]{begs.pop(), bi};
				}
				else
				{
					int beg = begs.pop();
					if (beg < left)
					{
						left = beg;
						right = bi;
					}
					
					bi = str.indexOf(b, i + 1);
				}
				
				i = ai < bi && ai >= 0 ? ai : bi;
			}
			
			if (begs.size() > 0)
			{
				result = new int[]{left, right};
			}
		}
		
		return result;
	}
}