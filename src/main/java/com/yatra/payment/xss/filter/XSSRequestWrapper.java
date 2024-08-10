package com.yatra.payment.xss.filter;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import com.yatra.platform.util.XssScriptPatterns;

public class XSSRequestWrapper extends HttpServletRequestWrapper {

	List<Pattern> xssScriptPatterns = null;
	
	public XSSRequestWrapper(HttpServletRequest servletRequest) {
        super(servletRequest);
        this.xssScriptPatterns = XssScriptPatterns.COMMON_PATTERNS;
    }
	
    public XSSRequestWrapper(HttpServletRequest servletRequest, List<Pattern> xssScriptPatterns) {
        super(servletRequest);
        this.xssScriptPatterns = xssScriptPatterns;
    }

    @Override public Enumeration<String> getParameterNames() {

        Enumeration<?> e = super.getParameterNames();
    	Set<String> allParamNames = new LinkedHashSet<String>();
        while (e.hasMoreElements()){
        	String fieldName = stripXSS((String) e.nextElement());
        	allParamNames.add(fieldName);
        }

        Enumeration<String> fieldnames = Collections.enumeration(allParamNames);
        return fieldnames;
      }

    @Override
    public String[] getParameterValues(String parameter) {
        String[] values = super.getParameterValues(parameter);

        if (values == null) {
            return null;
        }

        int count = values.length;
        String[] encodedValues = new String[count];
        for (int i = 0; i < count; i++) {
            encodedValues[i] = stripXSS(values[i]);
        }

        return encodedValues;
    }

    @Override
    public String getParameter(String parameter) {
        String value = super.getParameter(parameter);

        return stripXSS(value);
    }

    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);
        return stripXSS(value);
    }

    private String stripXSS(String value) {

        if (value != null) {
            // NOTE: It's highly recommended to use the ESAPI library and uncomment the following line to
            // avoid encoded attacks.
            // value = ESAPI.encoder().canonicalize(value);

            // Avoid null characters
            value = value.replaceAll("", "");

            // remove XSS patterns from value
            for (Pattern pattern : this.xssScriptPatterns) {
            	value = pattern.matcher(value).replaceAll("");
            }

        	//Avoid HTML tags
			value = removeHtml(value);
        }
        return value;
    }

	private String removeHtml(String htmlString) {
		String regex = "\\<.*?\\>";
		String html = htmlString.replaceAll("\\s+", " ");
		String noHTMLString = html.replaceAll(regex, "");

		return noHTMLString;
	}
}
