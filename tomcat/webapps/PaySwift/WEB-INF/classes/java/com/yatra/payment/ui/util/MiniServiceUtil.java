package com.yatra.payment.ui.util;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class MiniServiceUtil {

    private static final Logger logger = Logger.getLogger(MiniServiceUtil.class);
    public static final String PAYMENT_INFO_API = "PAYMENT_INFO_API";
    public static final String PAYMENT_OPTION_KEY = "paymentOptionJSON";
    public static final String BOOKING_DETAILS_KEY = "bookingDetails";
    public static final String CORP_CONFIG_SERVICE_KEY = "corpConfigService";
    public static final String CORP_BIN_CONFIG_SERVICE_KEY = "corpBinConfigService";
    public static final String CORP_CARD_KEY = "corporateCards";
    
    private MiniServiceUtil() {
    }

    public static JSONObject errorJson(Exception e) {
        JSONObject errorJson = new JSONObject();
        try {
            errorJson.put("error", e.getMessage());
        } catch (JSONException ex) {
            logger.error("JSON processing exception while adding error key");
        }
        return errorJson;
    }
}
