package com.yatra.payment.ui.controller;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.servlet.http.HttpServletRequest;

import com.yatra.express.crp.cards.v3.beans.CorporateCardInfo;
import com.yatra.express.crp.cards.v3.beans.CorporateCardsDisplayResponse;
import com.yatra.payment.ui.dao.bean.UIQBCardBrandMaster;
import com.yatra.payment.ui.exceptions.PayswiftException;
import com.yatra.payment.ui.service.CorporateCardsUIService;
import com.yatra.payment.ui.service.QuickBookUIService;
import com.yatra.payment.ui.service.impl.FetchItineraryDetailsServiceImpl;
import com.yatra.platform.util.YatraUtil;
import net.sf.json.JSONArray;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yatra.payment.payswift.FutureTasks.MiniServiceImplThread;
import com.yatra.payment.payswift.beans.LobDetailResponse;
import com.yatra.payment.ui.display.beans.QuickBookPaymentOption;
import com.yatra.payment.ui.helper.PaymentUIHelper;
import com.yatra.payment.ui.helper.QuickBookUIHelper;
import com.yatra.payment.ui.payswift.service.PaymentInfoService;
import com.yatra.payment.ui.service.MiniServiceI;
import com.yatra.payment.ui.service.impl.AgentSpecificPaymentOptionImpl;
import com.yatra.payment.ui.service.impl.CorporateBinsConfigServiceImpl;
import com.yatra.payment.ui.service.impl.CorporateQuickbookServiceImpl;
import com.yatra.payment.ui.util.CorporateProductConverterUtil;
import com.yatra.payment.ui.util.MiniServiceFactory;
import com.yatra.payment.ui.util.MiniServiceUtil;
import com.yatra.payment.ui.util.PaymentUIUtil;

import static com.yatra.payment.ui.enums.FailureCode.FETCH_CORPORATE_CARDS_EXCEPTION;
import static com.yatra.payment.ui.util.MiniServiceUtil.BOOKING_DETAILS_KEY;
import static com.yatra.payment.ui.util.MiniServiceUtil.PAYMENT_OPTION_KEY;
import static com.yatra.payment.ui.util.MiniServiceUtil.CORP_CARD_KEY;
import static java.util.Arrays.asList;

import com.yatra.payment.ui.util.ZipUtils;

@Controller
public class PaymentInfoController {
    Logger logger = Logger.getLogger(PaymentInfoController.class);
    @Autowired
    MiniServiceFactory miniServiceFactory;
    @Autowired
    ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Autowired
    PaymentInfoService paymentInfoService;
    @Autowired
    PaymentUIHelper paymentUIHelper;
    @Autowired
    AgentSpecificPaymentOptionImpl agentSpecificPaymentOptionImpl;
    @Autowired
    CorporateQuickbookServiceImpl corporateQuickbookServiceImpl;
    @Autowired CorporateBinsConfigServiceImpl corporateBinsConfigServiceImpl;
    @Autowired private CorporateCardsUIService corporateCardsUIService;
    @Autowired private FetchItineraryDetailsServiceImpl fetchItineraryDetailsServiceImpl;
    @Autowired private QuickBookUIService quickBookUIService;
	
    @Autowired
    private QuickBookUIHelper quickbookUIHelper;
    
    private ObjectMapper mapper = new ObjectMapper();
    private String NO_CARDS_SAVED_JSON = "{\"nonMoto\":false,\"displayText\":\"Quick Book\",\"code\":\"qb\",\"saveCard\":\"ENABLED\",\"authenticationFailed\":false,\"errorCode\":\"106\",\"priority\":null,\"iframeEnabled\":null,\"authMode\":null,\"authenticationRequired\":false,\"messages\":null,\"quickBookCards\":null,\"errorMesaage\":\"There are no cards saved by User\",\"status\":\"DISABLED\"}";
   
    /*
     * This is an aggregator controller which will return a JSON Object .
     * If the code has reached controller it is assumed that all parameters have been validated in filter .
     *
     */
    @RequestMapping(value = "getCompletePaymentInfo")
    @ResponseBody
    public String getCompletePaymentInfo(HttpServletRequest req) throws Exception {
        JSONObject reqJson = parseInputRequest(req);
        logger.debug(reqJson);
        paymentInfoService.logIntoDB(reqJson, "REQUEST", reqJson.optString(PaymentUIUtil.SUPER_PNR));
        String product = "";
        if(!StringUtils.isEmpty(reqJson.get("product").toString())){
            product = reqJson.get("product").toString();
        }
        //TODO log into DB the request and response Sent by spawning a separate Thread . Create table in DB , indexed by super PNR .
        List<MiniServiceI> listOfServices = miniServiceFactory.getRequestedServices(reqJson.getJSONArray("servicesRequested").put(MiniServiceUtil.CORP_CONFIG_SERVICE_KEY),product);
        String bookingType = paymentUIHelper.getBookingType(reqJson.getString("agentProfileType"));
        reqJson.put("bookingType", bookingType);
        reqJson.put("productForFalcon", CorporateProductConverterUtil.getFalconProduct(reqJson.getString("product")));
        
        listOfServices.add(corporateBinsConfigServiceImpl);
        JSONObject responseJSON = new JSONObject();

        int parallelServiceExecutionCount = listOfServices.size() - numberOfSequentialThreads(listOfServices);
        CountDownLatch latch = new CountDownLatch(parallelServiceExecutionCount);

        MiniServiceI agentPaymentOptionService = null;
        MiniServiceI corporateCardsService = null;
        for (MiniServiceI service : listOfServices) {
            if (PAYMENT_OPTION_KEY.equalsIgnoreCase(service.getResultKey())) {
                agentPaymentOptionService = service;
                continue;
            }
            if (CORP_CARD_KEY.equalsIgnoreCase(service.getResultKey())) {
                corporateCardsService = service;
                continue;
            }
            threadPoolTaskExecutor.submit(new MiniServiceImplThread(service, reqJson, latch, responseJSON));
        }
        latch.await();
        if (agentPaymentOptionService != null) {
            responseJSON.accumulate(agentPaymentOptionService.getResultKey(), agentPaymentOptionService.getRequiredData(reqJson, responseJSON));
        }
        if (corporateCardsService != null) {
            CorporateCardsDisplayResponse response = null;
            String ssoToken = reqJson.getString(PaymentUIUtil.SSO_TOKEN);

            response = getCorporateCardsResponse(ssoToken, reqJson, responseJSON);
            List<String> corporateCardTypesToDisplayList = getCorpCardToDisplayListFromJSON(reqJson, responseJSON);
            List<CorporateCardInfo> corporateCardInfoList = filterCards(corporateCardsUIService.getCorpCardsStatusHandling(response),corporateCardTypesToDisplayList);

            CorporateCardsDisplayResponse filteredCorporateCardDisplayResponse  = response;
            filteredCorporateCardDisplayResponse.setCorporateCards(corporateCardInfoList);
            JSONObject enrichedCorporateCardsDisplayResponse = getJsonWithEncrichedCVVLengthAndLabel(filteredCorporateCardDisplayResponse);

            responseJSON.accumulate(corporateCardsService.getResultKey(), enrichedCorporateCardsDisplayResponse);
        }
        overrideQBJsonIfRequired(reqJson, responseJSON);
        //paymentInfoService.populateSkipOtpFlag(responseJSON, reqJson.getString(PaymentUIUtil.PRODUCT_CODE));
		paymentInfoService.filterPaymentOptionForGDSHotel(responseJSON, reqJson.getString(PaymentUIUtil.PRODUCT_CODE));
		try {
			paymentInfoService.addCurrencyConversionJsonIfNeeded(responseJSON, reqJson);
		} catch(Exception e) {
			logger.info("Exception encountered while fetching currency data, so disabling paypal payop for superPnr : " + reqJson.optString(PaymentUIUtil.SUPER_PNR) + " due to error :" , e );
			if(responseJSON.has("paymentOptionJSON") && responseJSON.getJSONObject("paymentOptionJSON").has("paypal"))
				responseJSON.getJSONObject("paymentOptionJSON").remove("paypal");
		}
		paymentInfoService.logIntoDB(ZipUtils.getZipped(responseJSON.toString()),"RESPONSE",reqJson.optString(PaymentUIUtil.SUPER_PNR));
		
		logSkipOtpDecision(responseJSON, reqJson.getString(PaymentUIUtil.SUPER_PNR));
		return responseJSON.toString();
    }
    
    
    private void logSkipOtpDecision(JSONObject responseJSON, String superPnr) throws JSONException {
    	net.sf.json.JSONObject paymentOptionJsonForDBLogging = new net.sf.json.JSONObject();
    	paymentOptionJsonForDBLogging.put("paymentOptionJSON", responseJSON.getJSONObject("paymentOptionJSON").toString());
    	paymentUIHelper.logIntoDB(paymentUIHelper.getCVVSupportedCardTypeJSON(paymentOptionJsonForDBLogging.getJSONObject("paymentOptionJSON")).toString(),"RESPONSE", superPnr, "SkipOtpDecision");	
	}

    private int numberOfSequentialThreads(List<MiniServiceI> listOfServices) {
        int i = 0;
        for (MiniServiceI service : listOfServices) {
            if (agentSpecificPaymentOptionImpl.getResultKey().equalsIgnoreCase(service.getResultKey())) {
                i++;
            }
            if (corporateQuickbookServiceImpl.getResultKey().equalsIgnoreCase(service.getResultKey())) {
                i++;
            }
        }
        return i;
    }

    private JSONObject parseInputRequest(HttpServletRequest req) throws Exception {
        try {
            JSONObject requestJson;
            StringBuilder buffer = new StringBuilder();
            BufferedReader reader = req.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            String postBody = buffer.toString();

            requestJson = new JSONObject(postBody);
            return requestJson;
        } catch (Exception ex) {
            logger.debug(ex);
            throw ex;
        }
    }
    
    private boolean isQBEnabled(JSONObject responseJSON, String bookingType, String entity, String product, boolean passthrough) {
    	boolean isQBEnabled = false;
    	try {
    		JSONObject agentPaymentOptionJSON = new JSONObject();
    		if (responseJSON.has(BOOKING_DETAILS_KEY) && !responseJSON.isNull(BOOKING_DETAILS_KEY)) {
    			agentPaymentOptionJSON = responseJSON.getJSONObject(BOOKING_DETAILS_KEY).getJSONObject("agentPaymentOptions");
    		} 
    		net.sf.json.JSONObject agentPaymentOptionJSONTemp = new net.sf.json.JSONObject();
    		agentPaymentOptionJSONTemp.accumulate("agentPaymentOptions", agentPaymentOptionJSON.toString());
    		
    		isQBEnabled = quickbookUIHelper.displayQBCardsCRP(agentPaymentOptionJSONTemp.getJSONObject("agentPaymentOptions"), bookingType, entity, product, passthrough);
    	} catch (Exception ex) {
    		logger.error("Exception while computing isQBEnabled, Exception is :" + ex);
    	}
    	logger.info("returing isQBEnabled flag as: " + isQBEnabled);
    	return isQBEnabled;
    }
    
    private void overrideQBJsonIfRequired(JSONObject requestJSON, JSONObject responseJSON) {
    	try {
    		String bookingType = requestJSON.getString("bookingType");
    		LobDetailResponse detailsJson = new LobDetailResponse(responseJSON.getString(BOOKING_DETAILS_KEY));
    		boolean isQBEnabled = isQBEnabled(responseJSON, bookingType, detailsJson.getEntity(), detailsJson.getProductCode(), detailsJson.isPassThrough());
    		if(!isQBEnabled) {
    			logger.debug("QB is disabled for this user, overriding the QBJsonObject");
    			if(responseJSON.has("storedCardJSON")){
    				responseJSON.put("storedCardJSON", new JSONObject(NO_CARDS_SAVED_JSON));
    				
    			}
    		} else {
    			if(responseJSON.has("storedCardJSON") && responseJSON.has(MiniServiceUtil.CORP_CONFIG_SERVICE_KEY)){
					QuickBookPaymentOption qbPayop = mapper.readValue(responseJSON.getString("storedCardJSON"), QuickBookPaymentOption.class);
					quickbookUIHelper.filerCardsBasedOnConfig(qbPayop, responseJSON.getJSONObject(MiniServiceUtil.CORP_CONFIG_SERVICE_KEY).toString(), bookingType, detailsJson.getProductForFalcon());
					responseJSON.put("storedCardJSON", new JSONObject(mapper.writeValueAsString(qbPayop)));
    			}
    		}
    		if(responseJSON.has(MiniServiceUtil.CORP_CONFIG_SERVICE_KEY))	responseJSON.remove(MiniServiceUtil.CORP_CONFIG_SERVICE_KEY);
    		if(responseJSON.has(MiniServiceUtil.CORP_BIN_CONFIG_SERVICE_KEY))	responseJSON.remove(MiniServiceUtil.CORP_BIN_CONFIG_SERVICE_KEY);
    	} catch(Exception ex) {
    		logger.error("Exception while overriding QBJSon, Exception is :" + ex);
    	}
    }
    private List<String> getCorpCardToDisplayListFromJSON(JSONObject requestJSON, JSONObject responseJSON) throws Exception {
        JSONObject bookingDetailsJSON;
        if (responseJSON.has(BOOKING_DETAILS_KEY) && !responseJSON.isNull(BOOKING_DETAILS_KEY)) {
            bookingDetailsJSON = responseJSON.getJSONObject(BOOKING_DETAILS_KEY);
        } else {
            bookingDetailsJSON = fetchItineraryDetailsServiceImpl.getRequiredData(requestJSON, responseJSON);
        }
        LobDetailResponse lobDetailResponse = new LobDetailResponse(bookingDetailsJSON.toString());
        boolean isCrpProduct = PaymentUIUtil.checkIfCorporateProduct(lobDetailResponse.getProductCode());
        net.sf.json.JSONObject agentPaymentOptionJSON = lobDetailResponse.getAgentPaymentOptions();
        String entity = lobDetailResponse.getEntity();
        String product = lobDetailResponse.getProductCode();
        boolean passthrough = lobDetailResponse.isPassThrough();
        String bookingType = paymentUIHelper.getBookingType(lobDetailResponse.getAgentProfileType());
        if(isCrpProduct) {
            return corpCardToDisplayListForCrp(agentPaymentOptionJSON, bookingType, entity, product, passthrough);
        }
        return corpCardToDisplayListForB2B(agentPaymentOptionJSON);
    }
    private List<String> corpCardToDisplayListForCrp(net.sf.json.JSONObject agentPaymentOptionJSON, String bookingType, String entity, String product, boolean passthrough) {
        List<String> displayList = new ArrayList<>();
        net.sf.json.JSONObject paymentOptionsByBookingType = paymentUIHelper.crpPaymentOptionsByBookingType(agentPaymentOptionJSON, bookingType, entity, product, passthrough);
        if(paymentOptionsByBookingType.has("CORPCARD")) {
            net.sf.json.JSONObject corpCardJson = paymentOptionsByBookingType.getJSONObject("CORPCARD");

            if(corpCardJson.has("displayTypes")) {
                JSONArray displayTypes = corpCardJson.getJSONArray("displayTypes");
                logger.debug("Value of displayTypes: " + displayTypes.toString());
                for(int index = 0; index < displayTypes.size(); index++) {
                    displayList.add(displayTypes.getString(index));
                }
            }
        }
        return displayList;
    }
    private List<String> corpCardToDisplayListForB2B(net.sf.json.JSONObject agentPaymentOptionJSON) {
        String corpCrdsToDisplayString = agentPaymentOptionJSON.getString("corpCardsToDisplay");
        logger.debug("Value of corpCardsToDisplay from JSON is :: " + corpCrdsToDisplayString);
        if(YatraUtil.isNullOrEmpty(corpCrdsToDisplayString))
            return new ArrayList<String>();
        ArrayList<String> corpCrdsToDisplayList =  new ArrayList<String>(asList(corpCrdsToDisplayString.split("\\s*,\\s*")));
        return corpCrdsToDisplayList;
    }
    public CorporateCardsDisplayResponse getCorporateCardsResponse(String ssoToken , JSONObject requestJSON , JSONObject responseJSON ) throws  Exception {
        JSONObject bookingDetailsJSON;
        if (responseJSON.has(BOOKING_DETAILS_KEY) && !responseJSON.isNull(BOOKING_DETAILS_KEY)) {
            bookingDetailsJSON = responseJSON.getJSONObject(BOOKING_DETAILS_KEY);
        } else {
            bookingDetailsJSON = fetchItineraryDetailsServiceImpl.getRequiredData(requestJSON, responseJSON);
        }
        HashMap<String, String> crpInfo = PaymentUIUtil.getCrpInfoNode(net.sf.json.JSONObject.fromObject(bookingDetailsJSON.toString()));
        return  corporateCardsUIService.getCorporateCardsResponseLevelWise(ssoToken,crpInfo);
    }


    private List<CorporateCardInfo> filterCards(List<CorporateCardInfo> corporateCardList, List<String> corpCardToDisplayListFromJSON) {
        // Adding all the cards if empty list returned in response
        if(YatraUtil.isNullOrEmpty(corpCardToDisplayListFromJSON))
            return corporateCardList;
        else{
            List<CorporateCardInfo> filteredCardsList = new ArrayList<CorporateCardInfo>();
            for(CorporateCardInfo card : corporateCardList){
                if(corpCardToDisplayListFromJSON.contains(card.getCardType()))
                    filteredCardsList.add(card);
            }
            return filteredCardsList;
        }
    }
    private JSONObject getJsonWithEncrichedCVVLengthAndLabel(CorporateCardsDisplayResponse corporateCardsDisplayResponse) throws Exception {
        ObjectWriter ow = new ObjectMapper().viewWriter(CorporateCardsDisplayResponse.class);
        JSONObject resultJson = new JSONObject(ow.writeValueAsString(corporateCardsDisplayResponse));

        if(resultJson == null){
            return null;
        }
        org.json.JSONArray corporateCards = resultJson.getJSONArray("corporateCards");
        if(corporateCards == null){
            return null;
        }
        for(int i = 0 ; i < corporateCards.length() ; i++){
            JSONObject corporateCard = corporateCards.getJSONObject(i);
            corporateCard.accumulate("cvvLabel", "Enter CVV");

            String cardBrand = corporateCard.getString("cardBrand");
            UIQBCardBrandMaster quickBookCardBrand = quickBookUIService.getQBCardBrandBean(cardBrand);
            String cvvLength = quickBookCardBrand.getCvvLength();
            corporateCard.accumulate("cvvLength", cvvLength);

            /* adding extra node for mobile app to disable/enable cvv input field on ui */
            String withoutCVVSupported = "true";
            String cardType = corporateCard.getString("cardType");
            if(StringUtils.isNotBlank(cardType) && StringUtils.equalsIgnoreCase(cardType,PaymentUIUtil.CARD_TYPE_CORPORATE)){
                withoutCVVSupported = "false";
            }
            corporateCard.accumulate("withoutCVVSupported",withoutCVVSupported);

        }

        return  resultJson;
    }
}
