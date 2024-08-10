package com.yatra.payment.qb.corporate.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yatra.payment.qb.corporate.bean.CorporateTokenizedCards;
import com.yatra.payment.qb.corporate.bean.GetEntityGroupsResponse;
import com.yatra.payment.qb.corporate.manager.QBManager;

@Controller
@RequestMapping("/corporate-quickbook/**")
public class QBController {

	@Autowired
	private QBManager qbManager;
	private static final Logger logger = Logger.getLogger(QBController.class);

	@RequestMapping("get-cards")
	public @ResponseBody String getCards(HttpServletRequest request, HttpServletResponse response) {
		return qbManager.getCards(request);
	}

	@RequestMapping("save-card")
	public @ResponseBody String addCard(HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (request.getParameter("ctype") != null && request.getParameter("ctype").equalsIgnoreCase("CTA")) {
			return qbManager.saveAndTokenizeCorpCards(request);
		} else {
			return qbManager.saveCardBifurcator(request);
		}
	}

	@RequestMapping("delete-card")
	public @ResponseBody String deleteCard(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return qbManager.deleteCardBifurcator(request);
	}

	@RequestMapping("save-gds-card")
	public @ResponseBody String saveGDSCard(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return qbManager.saveGDSCard(request);
	}

	@RequestMapping("delete-gds-card")
	public @ResponseBody String deleteGDSCard(HttpServletRequest request, HttpServletResponse response)	throws Exception {
		return qbManager.deleteGDSCard(request);
	}
	
	@RequestMapping("get-entity-groups")
	public @ResponseBody GetEntityGroupsResponse getEntityGroups(HttpServletRequest request, HttpServletResponse response)	throws Exception {
		return qbManager.getEntityGroups(request);
	}

	@RequestMapping(value = "del-bta-card")
	public @ResponseBody String deleteBtaCard(HttpServletRequest request, HttpServletResponse response) throws Exception{
		return qbManager.deleteBtaCard(request);
	}

	@RequestMapping("save-corp-card-via-personal-flow")
	public @ResponseBody String saveCorpCardViaPersonalFlow(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return qbManager.saveCorpCardViaPersonalFlow(request, true);
	}
	
	@RequestMapping(value = "corporateCardsTokenizationStatus", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<CorporateTokenizedCards> getCorporateCardsTokenizationStatus(
			@RequestParam("corporateId") String corporateId) throws Exception {
		try 
		{
			if(corporateId == null || corporateId.trim().isEmpty())
			{
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
			
			CorporateTokenizedCards cards = qbManager.getCorporateCardsTokenizationStatus(corporateId);
			return new ResponseEntity<>(cards, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("error occurred while fetching status ", e);
		}
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
