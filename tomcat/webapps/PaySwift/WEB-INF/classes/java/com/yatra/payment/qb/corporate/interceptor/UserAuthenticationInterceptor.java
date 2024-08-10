package com.yatra.payment.qb.corporate.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.yatra.payment.payswift.beans.B2BUserProfileDetailBean;
import com.yatra.payment.qb.corporate.constant.QBConstant;
import com.yatra.payment.ui.exceptions.PayswiftException;
import com.yatra.payment.ui.service.AgentProfileService;
import com.yatra.payment.ui.util.PaymentUIUtil;
import org.apache.commons.lang3.StringUtils;

public class UserAuthenticationInterceptor extends HandlerInterceptorAdapter {

	@Autowired
	private AgentProfileService agentProfileService;

	private static final Logger logger = Logger.getLogger(UserAuthenticationInterceptor.class);

	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		String ssoToken = PaymentUIUtil.getSSOToken(request);
                String merchant = !StringUtils.isEmpty(request.getParameter("merchant")) ? request.getParameter("merchant") : "yatra";
                String product = !StringUtils.isEmpty(request.getParameter("product")) ? request.getParameter("product") : "crpdom";
		try {
			B2BUserProfileDetailBean profileDetailBean = agentProfileService.getUserProfileDetailCorp(ssoToken, merchant, product);
			request.setAttribute(QBConstant.USER_PROFILE_BEAN_KEY, profileDetailBean);
			logger.info("User Authentication is successful for ssoToken :" + ssoToken);
			return true;
		} catch (PayswiftException e) {
			logger.error("User Authentication fail for ssoToken :" + ssoToken, e);
			response.getWriter().write(QBConstant.NOT_AUTHERIZE_JSON);
			return false;
		}
	}

}
