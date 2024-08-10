package com.yatra.payment.ui.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yatra.payment.ui.beans.UserProfile;
import com.yatra.payment.ui.service.QuickBookUIService;
import com.yatra.payment.ui.service.UserProfileService;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Controller
@RequestMapping("/pay-div/{version}/users")
public class UserAccessController {

	@Autowired private QuickBookUIService quickBookUIService;
	@Autowired private UserProfileService userProfileService;
	
	@RequestMapping(method = RequestMethod.GET, headers = {"content-type=application/json"})
	public @ResponseBody
	String getUserProfile(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String ssoToken = PaymentUIUtil.getSSOToken(request);
		Map<String, String> requestMap = PaymentUIUtil.getRequestMap(request);
		UserProfile userProfile = userProfileService.getUserProfile(requestMap, ssoToken);
		return PaymentUIUtil.convertBeanToJsonString(userProfile);
	}

	@RequestMapping(value="/yatra/auth", method = RequestMethod.GET, headers = {"content-type=application/json"})
	public @ResponseBody
	String authenticateYatraUserJSON(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
		Map<String, String> requestMap = PaymentUIUtil.getRequestMap(request);
		UserProfile userProfile = userProfileService.authenticateYatraUser(requestMap, response);
		return PaymentUIUtil.convertBeanToJsonString(userProfile);
	}

	@RequestMapping(value="/fb/auth", method = RequestMethod.GET, headers = {"content-type=application/json"})
	public @ResponseBody
	String getAuthenticateQBFacebookUser(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {

		// Fetch Request Params.
		String merchant = request.getParameter("merchant");
		String product = request.getParameter("product");
		String accessToken = request.getParameter("accessToken");
		String sourcePage = request.getParameter("sourcepage");

		return quickBookUIService.getFacebookQBAuthBeanJSON(merchant, product, accessToken, sourcePage, response);
	}
	
	@RequestMapping(value="/yatra", method = RequestMethod.PUT)
	public  @ResponseBody 
	String updatePassword(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
		String ssoToken = PaymentUIUtil.getSSOToken(request);
		Map<String,String> requestMap = PaymentUIUtil.getRequestMap(request);
		return quickBookUIService.updatePassword(ssoToken, requestMap);
	}
	
	@RequestMapping("/yatra/is-pass-compromised")
	public  @ResponseBody 
	String isPasswordCompromised(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
		String ssoToken = PaymentUIUtil.getSSOToken(request);
		return quickBookUIService.isPasswordCompromised(ssoToken);
	}
}
