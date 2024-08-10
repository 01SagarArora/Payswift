package com.yatra.payment.ui.service.impl;

import com.yatra.payment.ui.display.beans.QuickBookPaymentOption;
import com.yatra.payment.ui.service.QuickBookExpressUIService;
import com.yatra.payment.ui.util.QuickBookUIUtil;

import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.base.log.Logger;
import com.yatra.base.log.LoggerFactory;
import com.yatra.payment.ui.service.MiniServiceI;
import com.yatra.platform.commons.property.PropertyManager;
import com.yatra.platform.util.YatraUtil;

@Service
public class QuickbookExpressServiceImpl implements MiniServiceI {

    @Autowired private QuickBookExpressUIService quickBookExpressUIService;
    @Autowired private PropertyManager yatraPropertyReader;
    private Logger logger = LoggerFactory.getLogger();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public JSONObject getRequiredData(JSONObject requestJson, JSONObject responseJSON) {
        JSONObject errorJson = validateMandatoryParameters(requestJson);
        String superPnr = null;
        if(errorJson!= null)
            return errorJson;

        QuickBookPaymentOption quickBookPaymentOption;
        try{
            superPnr = requestJson.getString("superPnr");
            List<String> crpProducts = yatraPropertyReader.getPropertyAsList("crp.products");
            boolean isCrpProduct = crpProducts.contains(requestJson.getString("product"));
            if(isCrpProduct && getSSOId(requestJson).compareTo(0L) == 0){
                logger.error("travellerSSOId is null for Corporate flow for SuperPNR " + superPnr);
                quickBookPaymentOption = quickBookExpressUIService.getQBErrorBean(QuickBookUIUtil.ERROR_CODE_INVALID, QuickBookUIUtil.TRAVELLER_SSO_USER_ID_ABSENT);
            } else {
                quickBookPaymentOption = quickBookExpressUIService.getQuickBookBean(requestJson.getString("merchant"), requestJson.getString("product"), requestJson.getString("ssoToken"), getSSOId(requestJson), getBookingType(requestJson));
            }
        } catch (Exception e) {
            logger.error("Exception occurred while getting QuickBook Bean : ", e);
            quickBookPaymentOption = quickBookExpressUIService.getQBErrorBean(QuickBookUIUtil.ERROR_CODE_EXCEPTION, e.getMessage());
        }
        try {
            String qbJson = objectMapper.writeValueAsString(quickBookPaymentOption);
            logger.debug("QB response for superPnr {} is {}",superPnr,qbJson);
            return new JSONObject(qbJson);
        } catch (Exception e) {
            logger.error("Caught an Exception at fetchStoredCardDetail on superPnr {} due to {}", superPnr , e);
            return null;
        }
    }

    private String getBookingType(JSONObject requestJson) {
        try {
            if (requestJson.has("agentProfileType") ){
                if("PERSONAL".equalsIgnoreCase(requestJson.getString("agentProfileType"))) {
                    return "PERSONAL";
                }
                else return "OFFICIAL";
            }
        } catch (JSONException e) {
            logger.error("Error in getting QuickbookServiceImpl.getBookingType()", e);
        }
        return null;
    }

    private Long getSSOId(JSONObject requestJson) {
        try {
            if (requestJson.has("travellerSSOId")) {
                return requestJson.getLong("travellerSSOId");
            }
        } catch (JSONException e) {
            logger.error("Error in getting QuickbookServiceImpl.getBookingType()", e);
        }
        return 0L;
    }

    @Override
    public JSONObject validateMandatoryParameters(JSONObject requestJson) {


        JSONObject errorJson = new JSONObject();
        StringBuffer bufferedError = new StringBuffer();
        if(!requestJson.has("merchant"))
            bufferedError.append("merchant not present");
        if(!requestJson.has("product"))
            bufferedError.append("product not present");
        if(!requestJson.has("ssoToken"))
            bufferedError.append("ssoToken not present");

        String error = null;
        error = bufferedError.toString();

        if(!YatraUtil.isNullOrEmpty(error)) {
            try {
                return errorJson.put("error", error);
            } catch (JSONException e) {
                logger.info("Error while preparing Quickbook Error Json", e);
            }
        }

        return null;

    }

    @Override
    public String getResultKey() {
        // TODO Auto-generated method stub
        return "storedCardJSON";
    }

}
