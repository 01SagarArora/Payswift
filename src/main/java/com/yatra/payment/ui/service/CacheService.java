package com.yatra.payment.ui.service;

import com.yatra.payment.ui.beans.BinDetails;
import com.yatra.platform.commons.property.PropertyManager;
import com.yatra.platform.http.HttpEndPoint;
import com.yatra.platform.http.HttpService;
import com.yatra.platform.util.YatraUtil;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.yatra.payment.ui.util.PaymentUIUtil.MULTI_KEY_SEPERATOR;

@Service("cacheService")
public class CacheService {
    private static Logger logger = Logger.getLogger(CacheService.class);

    @Autowired
    @Qualifier("redisHttpService")
    private HttpService httpService;

    @Autowired
    private PropertyManager propertyManager;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String BIN_PREFIX = "bin:";
    private static final String PRODUCT_PREFIX = "mpe:product:";


    public Map<String, Object> multiPayFlowBinInfo(String bin, String product) {
        Map<String, Object> multiPayFlowBinInfo = new HashMap<>();
        StringBuilder urlBuilder = new StringBuilder(propertyManager.getProperty("cache.multiget.url"))
                .append(PRODUCT_PREFIX).append(product)
                .append(MULTI_KEY_SEPERATOR)
                .append(BIN_PREFIX)
                .append(bin);
        try {
            String url = urlBuilder.toString();

            String result = httpService.invoke(new HttpEndPoint(url, "GET"));

            JSONArray jsonArray = new JSONArray(result);

            String isMultiPayFlowEnabled = jsonArray.getString(0);
            String binDetails = jsonArray.getString(1);

            if(isMultiPayFlowEnabled.equals("null")) {
                multiPayFlowBinInfo.put("isMultiPayFlowEnabled", null);
            } else {
                multiPayFlowBinInfo.put("isMultiPayFlowEnabled", isMultiPayFlowEnabled);
            }

            if(binDetails.equals("null")) {
                multiPayFlowBinInfo.put("binDetails", null);
            } else {
                multiPayFlowBinInfo.put("binDetails", Optional.of(objectMapper.readValue(binDetails, BinDetails.class)));
            }

            return multiPayFlowBinInfo;
        } catch (Exception e) {
            logger.error("Unable to get bin details for bin: " + bin, e);
            return new HashMap<>();
        }
    }

    public Optional<BinDetails> findBinDetailsByBinNumber(String bin) {
        try {
            String url = propertyManager.getProperty("cache.get.url") + BIN_PREFIX + bin;

            String result = httpService.invoke(new HttpEndPoint(url, "GET"));

            return Optional.of(objectMapper.readValue(result, BinDetails.class));
        } catch (Exception e) {
            logger.error("Unable to get bin details for bin: " + bin, e);
            return Optional.empty();
        }
    }

    public void saveBinDetails(BinDetails binDetails) {
        try {
            String binDetailsJson = objectMapper.writeValueAsString(binDetails);
            String postData = postBody(BIN_PREFIX + binDetails.getBinNumber(), binDetailsJson);

            HttpEndPoint endPoint = new HttpEndPoint(propertyManager.getProperty("cache.add.url"),
                    "POST");
            HashMap<String, String> headers = new HashMap<String, String>();
            headers.put("Content-Type", "application/json");
            httpService.invoke(endPoint, null, headers, postData);
        } catch (Exception e) {
            logger.error("Unable to save bin details", e);
        }
    }

    public void saveIsProductMultiPayFlowEnabled(String product, String isMultiPayFlowEnabled) {
        try {
            String postData = postBody(PRODUCT_PREFIX + product, isMultiPayFlowEnabled);

            HttpEndPoint endPoint = new HttpEndPoint(propertyManager.getProperty("cache.add.url"),
                    "POST");
            HashMap<String, String> headers = new HashMap<String, String>();
            headers.put("Content-Type", "application/json");
            httpService.invoke(endPoint, null, headers, postData);
        } catch (Exception e) {
            logger.error("Unable to save product isMultiPayFlowEnabled info", e);
        }
    }

    private String postBody(String key, String value) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("key", key);
        jsonObject.put("value", value);
        return jsonObject.toString();
    }
}
