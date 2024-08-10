package com.yatra.payment.qb.corporate.helper;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

public class APIMandatoryParametersValidator {
	
	String validate(HttpServletRequest request ,List<String> mandatoryParamList){
		StringBuilder sb = new StringBuilder();
		for(String paramName : mandatoryParamList){
			String paramValue = request.getParameter(paramName);
			if(StringUtils.isBlank(paramValue)){
				sb.append(paramName).append(",");
			}
		}
		if(!sb.toString().equals("")){
			sb.append(" mandatory parameters missing");
			return "{\"status\":\"FAILURE\",\"error_msg\":" + sb.toString() + "}";
		}
		return "";
	}
}
