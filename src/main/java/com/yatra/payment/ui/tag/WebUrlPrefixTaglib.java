package com.yatra.payment.ui.tag;

import com.yatra.platform.util.YatraUtil;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;


public class WebUrlPrefixTaglib extends TagSupport
{
   private String key;
   private String def;
   private String var;

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public String getDef()
    {
        return def;
    }

    public void setDef(String def)
    {
        this.def = def;
    }

    public String getVar()
    {
        return var;
    }

    public void setVar(String var)
    {
        this.var = var;
    }

    public WebUrlPrefixTaglib() {
    }

    public int doEndTag() throws JspException
    {
        return EVAL_PAGE;
    }


    public int doStartTag() throws JspException {

        String value = YatraUtil.getPropertyManager().getProperty(getKey());
        if (value == null || value.equalsIgnoreCase(""))
            value = def;
        pageContext.setAttribute(var,value);

        return SKIP_BODY;
    }


}
