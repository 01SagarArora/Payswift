package com.yatra.payment.ui.service.impl;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.express.crp.cards.client.B2BExpressCardsServiceClient;
import com.yatra.express.crp.cards.v3.beans.CorporateCardsDisplayResponse;
import com.yatra.payment.ui.dao.bean.UIQBCardBrandMaster;
import com.yatra.payment.ui.service.MiniServiceI;
import com.yatra.payment.ui.service.QuickBookUIService;
import static com.yatra.payment.ui.util.MiniServiceUtil.BOOKING_DETAILS_KEY;

import com.yatra.payment.ui.util.PaymentUIUtil;
import java.util.HashMap;

@Service
public class CorporateQuickbookServiceImpl implements MiniServiceI {

	@Autowired private B2BExpressCardsServiceClient b2BExpressCardsServiceClient;
	@Autowired private QuickBookUIService quickBookUIService;
        @Autowired private FetchItineraryDetailsServiceImpl fetchItineraryDetailsServiceImpl;
	private Logger logger = Logger.getLogger(CorporateQuickbookServiceImpl.class);
	private static final ObjectMapper ObjectMapper = new ObjectMapper();
        
	@Override
	public JSONObject getRequiredData(JSONObject requestJson, JSONObject responseJSON) {
		JSONObject resultJson = validateMandatoryParameters(requestJson);
		if(resultJson!= null)
			return resultJson;
		String superPnr = null;
		try {
			superPnr = requestJson.getString("superPnr");
                        JSONObject bookingDetailsJSON;
			if (responseJSON.has(BOOKING_DETAILS_KEY) && !responseJSON.isNull(BOOKING_DETAILS_KEY)) {
				 bookingDetailsJSON = responseJSON.getJSONObject(BOOKING_DETAILS_KEY);
			} else {
				bookingDetailsJSON = fetchItineraryDetailsServiceImpl.getRequiredData(requestJson, responseJSON);
			}
			HashMap<String, String> crpInfo = PaymentUIUtil.getCrpInfoNode(net.sf.json.JSONObject.fromObject(bookingDetailsJSON.toString()));
			CorporateCardsDisplayResponse response = b2BExpressCardsServiceClient.getSsoCardsForDisplay(requestJson.getString("ssoToken"), crpInfo);
			ObjectWriter ow = new ObjectMapper().viewWriter(CorporateCardsDisplayResponse.class);
			resultJson = new JSONObject(ow.writeValueAsString(response));
			enrichJsonWithCVVLengthAndLabel(resultJson);
		} catch (JSONException e) {
			logger.error("Caught an Exception at getCorporateCardsForDisplay on superPnr {} due to {}" + superPnr , e);
		} catch(Exception e){
			logger.error("An exception occurred while hitting B2B service . ", e);
		}
		
		return resultJson;
		
	}

         
         
	private void enrichJsonWithCVVLengthAndLabel(JSONObject resultJson) throws JSONException {
		if(resultJson == null){
			return;
		}
		JSONArray corporateCards = resultJson.getJSONArray("corporateCards");
		if(corporateCards == null){
			return;
		}
		for(int i = 0 ; i < corporateCards.length() ; i++){
			JSONObject corporateCard = corporateCards.getJSONObject(i);
			corporateCard.accumulate("cvvLabel", "Enter CVV");
			
			String cardBrand = corporateCard.getString("cardBrand");
			UIQBCardBrandMaster quickBookCardBrand = quickBookUIService.getQBCardBrandBean(cardBrand);
			String cvvLength = quickBookCardBrand.getCvvLength();
			corporateCard.accumulate("cvvLength", cvvLength);
		}
		
		
	}

	@Override
	public JSONObject validateMandatoryParameters(JSONObject requestJson) {
		JSONObject errorJson = new JSONObject();
		if(!requestJson.has("ssoToken"))
			try {
				return errorJson.put("error", "ssoToken not present");
			} catch (JSONException e) {
				logger.info("Error while preparing CorporateQuickbook Error Json", e);
			}
			
		return null;
	}

	@Override
	public String getResultKey() {
		return "corporateCards";
	}

}
