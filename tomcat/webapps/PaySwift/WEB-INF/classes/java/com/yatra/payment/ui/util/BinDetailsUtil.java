package com.yatra.payment.ui.util;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class BinDetailsUtil {
    private static final Logger logger = Logger.getLogger(BinDetailsUtil.class);
    public static final String GET_BIN_DETAILS_API = "GET_BIN_DETAILS_API";

    private BinDetailsUtil() {
    }

    public static String errorJson(Exception e) {
        JSONObject errorJson = new JSONObject();
        try {
            errorJson.put("error", e.getMessage());
            errorJson.put("skipOtpForBin", false);
        } catch (JSONException ex) {
            logger.error("JSON processing exception while adding error key");
        }
        return errorJson.toString();
    }
}

