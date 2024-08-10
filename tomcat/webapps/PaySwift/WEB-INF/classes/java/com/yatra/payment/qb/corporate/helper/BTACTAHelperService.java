package com.yatra.payment.qb.corporate.helper;

import com.yatra.express.crp.cards.v3.beans.CorporateCardInfo;
import com.yatra.payment.ui.display.beans.QuickBookCard;
import com.yatra.platform.commons.property.PropertyManager;
import com.yatra.platform.http.HttpEndPoint;
import com.yatra.platform.http.HttpService;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class BTACTAHelperService {

	private static final String CORP_CONFIG_SERVICE_URL = "corporate.config.service.endpoint";
	private static Logger logger = Logger.getLogger(BTACTAHelperService.class);
	private static final JSONArray CACHED_BIN_CONFIG_ARR;

	static {
		// TODO: 13/2/19 Do what needs to be done here
		CACHED_BIN_CONFIG_ARR = buildCardsFeatureConfigsArrForPayopApi();
	}

	@Autowired
	PropertyManager properties;
	@Autowired
	HttpService httpService;


	public boolean isBtaCtaFlow(String ssoToken) throws Exception {
		if (ssoToken == null || ssoToken.isEmpty()) {
			return false;
		}
		String url = properties.getProperty(CORP_CONFIG_SERVICE_URL);
		String postData = buildReqJson(ssoToken);
		logger.info("contacting services for ssoToken details");
		HttpEndPoint serviceEndPoint = new HttpEndPoint(url, "POST");
		String response = httpService.invoke(serviceEndPoint, null, null, postData);
		logger.info("Received response from Corporate Bin Configuration Service: " + response);
		JSONObject respJson = new JSONObject(response);
		try{
			boolean isAllowed =  respJson.getJSONObject("configurations").getJSONObject("cardsFeatureConfig").getBoolean("isAllowed");
			return isAllowed;
		}catch (Exception e){
			logger.error(e);
			return false;
		}
		//return true;
	}

	private String buildReqJson(String ssoToken) throws Exception {

		JSONObject reqJson = new JSONObject();
		reqJson.put("ssoToken", ssoToken);
		reqJson.put("configs", CACHED_BIN_CONFIG_ARR);

		return reqJson.toString();
	}

	public List<String> filterBtaCardsInPersonalCards(List<CorporateCardInfo> corporateCards, List<QuickBookCard> personalCards) {
		List<String> resultList = new ArrayList<String>(Collections.emptyList());
		if (corporateCards == null || personalCards == null){
			return resultList;
		}

		for (int i=0;i<personalCards.size();++i){
			for (int j=0;j<corporateCards.size();++j){


				String cardNumberFromPersonal = personalCards.get(i).getCardNumber();

				String cardNumber1PartCorporate = corporateCards.get(j).getCardNumber1();
				if (!cardNumberFromPersonal.substring(0,0 + cardNumber1PartCorporate.length()).equalsIgnoreCase(corporateCards.get(j).getCardNumber1())){
					continue;
				}


				StringBuilder cardNumberFromCorporate = new StringBuilder();
				cardNumberFromCorporate.append(corporateCards.get(j).getCardNumber1()).append("-");

				cardNumberFromCorporate.append(corporateCards.get(j).getCardNumber2()).append("-");
				cardNumberFromCorporate.append(corporateCards.get(j).getCardNumber3()).append("-");
				cardNumberFromCorporate.append(corporateCards.get(j).getCardNumber4());
				
				String cardNumFromCorp = cardNumberFromCorporate.toString();
				//handling Amex case
				if('-' == (cardNumFromCorp.charAt(cardNumFromCorp.length()-1)))
					cardNumFromCorp = cardNumFromCorp.substring(0, cardNumFromCorp.length()-1);
				
				if (cardNumFromCorp.equalsIgnoreCase(cardNumberFromPersonal)){
					resultList.add(personalCards.get(i).getCardId());
				}
			}
		}

		return resultList;

	}

	public CorporateCardInfo getCorporateCardForPersonalCardInBtaCards(List<CorporateCardInfo> corporateCards, List<QuickBookCard> personalCards,String personalCardId){
		CorporateCardInfo corporateCardInfo = null;

		if (personalCardId == null || personalCardId.isEmpty()) {
			return corporateCardInfo;
		}
		QuickBookCard personalCardToBeDeleted = null;
		for(QuickBookCard qbCard : personalCards){
			if (qbCard.getCardId().equalsIgnoreCase(personalCardId)){
				personalCardToBeDeleted = qbCard;
			}
		}

		if (personalCardToBeDeleted == null){
			return corporateCardInfo;
		}

		for (CorporateCardInfo corpCard : corporateCards){
			if (!corpCard.getCardNumber1().equalsIgnoreCase(personalCardToBeDeleted.getCardNumber().substring(0,0 + corpCard.getCardNumber1().length()))){
				continue;
			}

			StringBuilder corpCardNumber = new StringBuilder();
			corpCardNumber.append(corpCard.getCardNumber1()).append("-")
					.append(corpCard.getCardNumber2()).append("-")
					.append(corpCard.getCardNumber3()).append("-")
					.append(corpCard.getCardNumber4());

			if (corpCardNumber.toString().equalsIgnoreCase(personalCardToBeDeleted.getCardNumber())){
				corporateCardInfo = corpCard;
			}
		}
		return corporateCardInfo;
	}
	
	private static JSONArray buildCardsFeatureConfigsArrForPayopApi() {

		JSONArray configArr = new JSONArray();
		try {
			JSONObject whereConditionArrElem1 = new JSONObject();
			whereConditionArrElem1.put("name", "channel");
			whereConditionArrElem1.put("value", "web");

			JSONArray whereConditionArr = new JSONArray();
			whereConditionArr.put(whereConditionArrElem1);

			JSONObject configArrElem1 = new JSONObject();
			configArrElem1.put("name", "cardsFeatureConfig");
			configArrElem1.put("whereConditions", whereConditionArr);

			configArr.put(configArrElem1);
		} catch (Exception e) { 
			logger.error("Error while creating Request for CardsFeatureConfiguration Service", e);
		}

		return configArr;
	}
	
}
