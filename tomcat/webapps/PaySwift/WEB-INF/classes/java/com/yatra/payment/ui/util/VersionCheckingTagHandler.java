package com.yatra.payment.ui.util;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

public class VersionCheckingTagHandler extends TagSupport {

    private double version;
     
    @Override
    public int doStartTag() throws JspException {
	final String ENV = System.getenv("environment");
	boolean enabled = ((version == 1.4) && !ENV.equalsIgnoreCase("dev"));
	//pageContext.setAttribute("minify", enabled);
	return SKIP_BODY;
    }
    public double getVersion() {
        return version;
    }
    public void setVersion(double version) {
        this.version = version;
    }
}
