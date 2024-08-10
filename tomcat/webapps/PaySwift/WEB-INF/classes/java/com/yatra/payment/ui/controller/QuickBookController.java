package com.yatra.payment.ui.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.yatra.platform.commons.property.PropertyManager;
import com.yatra.payment.ui.service.QuickBookUIService;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Controller
@RequestMapping("/pay-div/{version}/qb-cards*")
public class QuickBookController {

	@Autowired	private QuickBookUIService quickBookUIService;
	@Autowired	private PropertyManager  yatraPropertyReader;
	
	@RequestMapping(value="/qb-wizard", method = RequestMethod.GET, headers = {"content-type=application/html"})
	public ModelAndView geQuickBookHTML(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		String version = PaymentUIUtil.getURLVersion(request);
		String viewName = "desktop/" + version + "/my-booking-qb";
		
		ModelAndView modelAndView = new ModelAndView();
		String staticContentURL = yatraPropertyReader.getProperty("static.content.url");
		modelAndView.addObject("static_content_url", staticContentURL);
		modelAndView.addObject("version", version);
		modelAndView.setViewName(viewName);
		return modelAndView;
	}

	@RequestMapping(value="/{cardid}", method = RequestMethod.DELETE, headers = {"content-type=application/json"})
	public @ResponseBody 
	String deletedQuickBookCard(@PathVariable("cardid") String cardId, HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
		String ssoToken = PaymentUIUtil.getSSOToken(request);
		Map<String,String> requestMap = PaymentUIUtil.getRequestMap(request);
		requestMap.put("cardid", cardId);
		return quickBookUIService.deleteQuickBookCard(ssoToken, requestMap);
	}

	@RequestMapping(method = RequestMethod.POST, headers = {"content-type=application/json"})
	public  @ResponseBody 
	String saveQuickBookCard(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
		String ssoToken = PaymentUIUtil.getSSOToken(request);
		Map<String,String> requestMap = PaymentUIUtil.getRequestMap(request);
		return quickBookUIService.saveQuickBookCard(ssoToken, requestMap);
	}
}
