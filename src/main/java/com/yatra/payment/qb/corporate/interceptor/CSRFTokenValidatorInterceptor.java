package com.yatra.payment.qb.corporate.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.yatra.payment.qb.corporate.constant.QBConstant;
import com.yatra.payment.ui.util.PaymentUIUtil;

public class CSRFTokenValidatorInterceptor extends HandlerInterceptorAdapter {
	
	private static final Logger logger = Logger.getLogger(CSRFTokenValidatorInterceptor.class);
	
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)throws Exception {
		String ssoToken = PaymentUIUtil.getSSOToken(request);
		String csrfToken = request.getParameter(QBConstant.CSRF_TOKEN_KEY);
		if(StringUtils.isBlank(ssoToken) || StringUtils.isBlank(csrfToken) || !PaymentUIUtil.isTokenValid(ssoToken, csrfToken)){
			response.getWriter().write(QBConstant.INVALID_CSRF_TOKEN_JSON);
			logger.error("CSRF Token  is Invalid for ssoToken :" + ssoToken +" ,csrf Token :" + csrfToken);
			return false;
		}
		logger.info("CSRF Token  is Valid for ssoToken :" + ssoToken + " ,csrf Token :" + csrfToken);
		return true;
	}
	
}
