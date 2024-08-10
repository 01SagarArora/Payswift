package com.yatra.payment.xss.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.yatra.platform.util.XssScriptPatterns;

public interface XssScriptPatternsWrapper extends XssScriptPatterns {
	public static final List<Pattern> NEW_PATTERNS = new ArrayList() {
		{
			this.addAll(XssScriptPatterns.STRICT_PATTERNS);
			this.add(Pattern.compile("data:text/html;base64(.*?)", 42));
			this.add(Pattern.compile("base64(.*?)", 42));
			this.add(Pattern.compile("prompt(.*?)", 42));
		}
	};
}
