package com.yatra.payment.ui.service.impl;

import com.yatra.payment.client.utils.PaymentConstants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.yatra.payment.payswift.beans.B2BUserProfileDetailBean;
import com.yatra.payment.ui.dao.impl.PayswiftStagesDAOImpl;
import com.yatra.payment.ui.exceptions.PayswiftException;
import com.yatra.payment.ui.util.HttpUtil;
import com.yatra.payment.ui.util.PaymentUIUtil;

import net.sf.json.JSONObject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.ui.service.AgentProfileService;
import com.yatra.platform.commons.property.PropertyManager;
import com.yatra.platform.http.HttpEndPoint;
import com.yatra.platform.http.HttpService;

import static com.yatra.payment.ui.enums.FailureCode.*;

import com.yatra.payment.ui.service.TenantService;

import static com.yatra.payment.ui.util.PaymentUIUtil.CONTENT_TYPE_APPLICATION_JSON;
import static java.util.Arrays.asList;

import javax.annotation.PostConstruct;

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;

@Service("agentProfileService")
public class AgentProfileServiceImpl implements AgentProfileService {

    @Autowired private HttpUtil httpUtil;
    @Autowired
    PropertyManager properties;
    @Autowired
    private PropertyManager propertyManager;
    @Autowired private PropertyManager yatraPropertyReader;
    @Autowired HttpService httpService;
    @Autowired TenantService tenantService;
    @Autowired PayswiftStagesDAOImpl paymentDao;

    private static List<String> crpProductArrayList = new ArrayList<>();
    private Logger logger = Logger.getLogger(AgentProfileServiceImpl.class);

    
    @PostConstruct
	public void setCrpProducts(){
		String crpProducts = yatraPropertyReader.getProperty("crp.products");
		if(org.apache.commons.lang.StringUtils.isNotBlank(crpProducts)){
			crpProductArrayList = asList(crpProducts.split(","));
		}
	}
    
    @Override
    public boolean isCrpProduct(String productCode) {
		return crpProductArrayList.contains(productCode);
	}
    
    public List<String> getAgentPaymentOptions(String ssoToken) throws Exception {

        try {
            String url = properties.getProperty("agent.profile.service.endpoint");
            JSONObject requestJson = new JSONObject();
            requestJson.put("ssoToken", ssoToken);
            requestJson.put("method", "getPaymentOptions");
            requestJson.put("application", "web");

            Map<String, String> requestParameters = new HashMap<String, String>();
            requestParameters.put("POSTDATA", requestJson.toString());

            logger.info("Hitting B2B service " + properties.getProperty("agent.profile.service.endpoint") + " to get payment options with parameters : " + requestJson.toString());
            //String paymentOptions = httpUtil.invoke(url, requestJson.toString(), headers(), "B2B_AGENT_PAYMENT_OPTIONS");
            HttpEndPoint serviceEndPoint = new HttpEndPoint(url, "POST");
            String  paymentOptions = httpService.invoke(serviceEndPoint,null,null,requestJson.toString(),true);
            
            logger.info("B2B service response : " + paymentOptions);

            JSONObject paymentOptionsJsonObject = JSONObject.fromObject(paymentOptions);

            if (paymentOptionsJsonObject.getInt("resCode") != 200) {
                throw new Exception();
            }

            String paymentOptionString = paymentOptionsJsonObject.getString("payOpt");
            ArrayList<String> paymentOptionList = new ArrayList<String>(Arrays.asList(paymentOptionString.split(",")));
            return paymentOptionList;

        } catch (Exception e) {
            logger.error("Error getting AgentPaymentOptions for ssoToken :  " + ssoToken + e);
            throw e;
        }
    }

    @Deprecated
    public JSONObject getAgentPaymentOptionsAsJsonObject(String ssoToken, String productCode) {
        if (propertyManager.getPropertyAsBoolean("dummy.booking.detail.api")) {
            logger.info("getting dummy agent payment options from text file");
            try {
                return JSONObject.fromObject(IOUtils.toString(getClass().getClassLoader().getResourceAsStream("dummy/agentPaymentOptions.txt"), "UTF-8"));
            } catch (Exception e) {
                throw new PayswiftException(FETCH_AGENT_PAYMENT_OPTIONS_EXCEPTION.getCode(),"Error getting dummy AgentPaymentOptions", e);
            }
        }

        try {
            String url = properties.getProperty("agent.profile.service.endpoint");
            JSONObject requestJson = new JSONObject();
            requestJson.put("ssoToken", ssoToken);
            requestJson.put("method", "getPaymentOptions");
            requestJson.put("application", "web");
            requestJson.put("productType", productCode);

            Map<String, String> requestParameters = new HashMap<String, String>();
            requestParameters.put("POSTDATA", requestJson.toString());

            logger.debug("Hitting B2B service to get payment options with parameters : " + requestJson.toString());
            //String paymentOptions = httpUtil.invoke(url, requestJson.toString(), headers(), "B2B_AGENT_PAYMENT_OPTIONS");
            HttpEndPoint serviceEndPoint = new HttpEndPoint(url,"POST");
            String paymentOptions = httpService.invoke(serviceEndPoint, null,null, requestJson.toString(), true);
            logger.info("B2B service response : " + paymentOptions);

            return JSONObject.fromObject(paymentOptions);
        } catch (Exception e) {
            throw new PayswiftException(FETCH_AGENT_PAYMENT_OPTIONS_EXCEPTION.getCode(), "Error getting AgentPaymentOptions for ssoToken :  "+ssoToken.substring(0, 5), e);
        }
    }

    
    @Override
    public JSONObject getUserDetailsCorp(String ssoToken) {
        String userDetails = "";
        try {
            String url = properties.getProperty("corporate.user.profile.service.endpoint");
            JSONObject requestJson = new JSONObject();
            requestJson.put("ssoToken", ssoToken);

            Map<String, String> requestParameters = new HashMap<String, String>();
            requestParameters.put("POSTDATA", requestJson.toString());

            logger.debug("Hitting SSO Corporate User Service to get userId and corporateId with parameters : " + requestJson.toString());
            HttpEndPoint serviceEndPoint = new HttpEndPoint(url, "POST");
            Map<String, String> headers = new HashMap<>();
            headers.put("Accept", "application/json");
            headers.put("Content-Type", "application/json");
            headers.put("com.yatra.tenant.header.tenantId", PaymentConstants.DEFAULT_TENANTID);
            userDetails = httpService.invoke(serviceEndPoint,null,headers,requestJson.toString(),true);
            logger.info("SSO Corporate User Service response : " + userDetails);
        } catch (Exception e) {
            throw new PayswiftException(FETCH_CORPORATE_CREDITPOOL_BALANCE_EXCEPTION.getCode(), "Error while getting loggedIn user info for ssoToken :  " + ssoToken, e);
        }
        JSONObject userDetailsJsonObject = JSONObject.fromObject(userDetails);
        if (userDetailsJsonObject.getInt("httpCode") != 200) {
            String errorMessage = String.valueOf(userDetailsJsonObject.getJSONArray("messages").get(0));
            throw new PayswiftException(FETCH_CORPORATE_USER_DETAILS_INVALID_RESPONSE_CODE.getCode(), "Invalid response code for getting loggedIn user info for ssoToken :  " + ssoToken + " errorMessage: " + errorMessage);
        }
        if (!validateUserDetails(userDetailsJsonObject)) {
            throw new PayswiftException(FETCH_CORPORATE_USER_DETAILS_EXCEPTION.getCode(), "userId/clientId not received while fetching loggedIn user info for ssoToken :  " + ssoToken);
        }
        return userDetailsJsonObject;
    }
    
    private boolean validateUserDetails(JSONObject userDetailsJSONObject) {
        if (!userDetailsJSONObject.containsKey("userId") && !userDetailsJSONObject.containsKey("clientId")) {
            return false;
        }
        Object userId = userDetailsJSONObject.get("userId");
        Object clientId = userDetailsJSONObject.get("clientId");
        if (userId instanceof JSONNull) {
            return false;
        }
        if (clientId instanceof JSONNull) {
            return false;
        }
        return true;
    }
    
    @Override
    public JSONObject getCreditPoolCorp(JSONObject userDetails, String ssoToken, String ctripId) {
        String creditOptions = "";
        String userId = userDetails.getString("userId");
        String clientId = userDetails.getString("clientId");
        String emailId = userDetails.getString("emailId");
        try {
            String url = properties.getProperty("corporate.credit.pool.service.endpoint");
            Map<String, String> requestParams = new HashMap<String, String>();
            requestParams.put("corporateId", clientId);
            requestParams.put("userId", userId);
            requestParams.put("ssoToken", ssoToken);
            requestParams.put("clientUserName", emailId);
            requestParams.put("tripId", ctripId);
            
            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Accept", "application/json");
            logger.debug("Hitting Credit pool corp service to get credit Pool balance with parameters : " + requestParams.toString());
            HttpEndPoint serviceEndPoint = new HttpEndPoint(url, "GET");
            creditOptions = httpService.invoke(serviceEndPoint, requestParams, headers);
            logger.info("Credit pool corp service response : " + creditOptions);
        } catch (Exception e) {
            throw new PayswiftException(FETCH_CORPORATE_CREDITPOOL_BALANCE_EXCEPTION.getCode(), "Data Not Found for userId: " + userId + " clientId: " + clientId, e);
        }
        JSONObject creditPoolDetailsJsonObject = JSONObject.fromObject(creditOptions);

        return convertCreditPoolCorpResponse(creditPoolDetailsJsonObject);
    }
    
    private JSONObject convertCreditPoolCorpResponse(JSONObject creditPoolCorpDetails) {
        JSONObject creditPoolDetails = new JSONObject();
        creditPoolDetails.accumulate("resCode", creditPoolCorpDetails.getJSONObject("meta").getInt("statusCode"));
        creditPoolDetails.accumulate("resMessage", creditPoolCorpDetails.getJSONObject("meta").getString("statusMessage").toLowerCase());
        creditPoolDetails.accumulate("creditPool", String.valueOf(creditPoolCorpDetails.getJSONObject("data").getJSONObject("credit").getLong("balance")));
        if (creditPoolCorpDetails.getJSONObject("data").getJSONObject("credit").has("showBal")) {
            String showBalance = creditPoolCorpDetails.getJSONObject("data").getJSONObject("credit").getString("showBal");
            if ("true".equalsIgnoreCase(showBalance) || "1".equalsIgnoreCase(showBalance)) {
                creditPoolDetails.accumulate("showBal", "1");
                return creditPoolDetails;
            } 
        }
        
        creditPoolDetails.accumulate("showBal", "0");    
        return creditPoolDetails;
    }
    
    @Override
    public JSONObject getCreditPoolDetails(String ssoToken, String productCode, String ctripId) {
        if (isCrpProduct(productCode)) {
            JSONObject userDetails = getUserDetailsCorp(ssoToken);
            return getCreditPoolCorp(userDetails, ssoToken, ctripId);
        }
        String creditOptions = "";
        if ("false".equalsIgnoreCase(paymentDao.useNewB2BPoolEndPoint())) {
        	try {
                String url = properties.getProperty("agent.profile.service.endpoint");
                JSONObject requestJson = new JSONObject();
                requestJson.put("ssoToken", ssoToken);
                requestJson.put("method", "checkBalance");
                requestJson.put("application", "web");
                requestJson.put("product", productCode);

                Map<String, String> requestParameters = new HashMap<String, String>();
                requestParameters.put("POSTDATA", requestJson.toString());

                logger.debug("Hitting B2B service to get credit Pool balance with parameters : " + requestJson.toString());
                //creditOptions = httpUtil.invoke(url, requestJson.toString(), headers(), "B2B_CREDIT_POOL");
                HttpEndPoint serviceEndPoint = new HttpEndPoint(url, "POST");
                creditOptions = httpService.invoke(serviceEndPoint,null,null,requestJson.toString(),true);
                logger.info("B2B service response : " + creditOptions);
            } catch (Exception e) {
                throw new PayswiftException(FETCH_CREDITPOOL_BALANCE_EXCEPTION.getCode(), "Error while getting credit Pool balance for ssoToken :  " + ssoToken, e);
            }	
        } else {
        	try {
                HttpEndPoint serviceEndPoint = new HttpEndPoint(properties.getProperty("agent.profile.service.new.endpoint") + "/checkBalance", "POST");
    			JSONObject requestJson = new JSONObject();
    			requestJson.put("ssoToken", ssoToken);
    			requestJson.put("req_method", "POST");
    			requestJson.put("application","web");
    			requestJson.put("channel","b2b");
    			//requestJson.put("ipaddress",ipAddress);
    			
    			Map<String,String> headers = new HashMap<String, String>();
    			headers.put("ssoToken", ssoToken);
    			
    			Map<String, String> requestParameters = new HashMap<String, String>();
    			requestParameters.put("POSTDATA", requestJson.toString());
    			creditOptions= httpService.invoke(serviceEndPoint,requestParameters,headers,null,true);
                logger.info("B2B service response : " + creditOptions);
            } catch (Exception e) {
                throw new PayswiftException(FETCH_CREDITPOOL_BALANCE_EXCEPTION.getCode(), "Error while getting credit Pool balance for ssoToken :  " + ssoToken, e);
            }
        }
        JSONObject creditPoolDetailsJsonObject = JSONObject.fromObject(creditOptions);

        if (creditPoolDetailsJsonObject.getInt("resCode") != 200) {
            throw new PayswiftException(FETCH_CREDITPOOL_BALANCE_INVALID_RESPONSE_CODE.getCode(), "Invalid response code for get credit Pool balance request for ssoToken :  " + ssoToken);
        }
        return creditPoolDetailsJsonObject;
    }

    public JSONObject agentLogin(String emailId, String ssoToken) {
        try {
            String url = properties.getProperty("agent.profile.service.endpoint");
            JSONObject requestJson = new JSONObject();
            requestJson.put("method", "agentLogin");
            requestJson.put("application", "web");
            requestJson.put("email", emailId);
            requestJson.put("channel", "b2b");
            requestJson.put("ssoToken", ssoToken);

            logger.debug("Hitting B2B service to agent Login with parameters : " + requestJson.toString());
            //String agentLoginResponse = httpUtil.invoke(url, requestJson.toString(), headers(), "B2B_AGENT_LOGIN");
            HttpEndPoint serviceEndPoint = new HttpEndPoint(url, "POST");
            String agentLoginResponse = httpService.invoke(serviceEndPoint,null,null,requestJson.toString(),true);
            logger.info("B2B service response : " + agentLoginResponse);

            return JSONObject.fromObject(agentLoginResponse);

        } catch (Exception e) {
            logger.error("Error while agent Login for ssoToken :  " + ssoToken + e);
            return null;
        }
    }

    @Override
    public B2BUserProfileDetailBean getUserProfileDetailCorp(String ssoToken, String merchantCode, String productCode) {
        if(StringUtils.isEmpty(ssoToken)){
            throw new PayswiftException(B2B_USER_PROFILE_SERVICE_EXCEPTION.getCode(), "ssoToken Empty for getUserProfileDetailCorp");
        }
        return new B2BUserProfileDetailBean(getUserDetailsCorp(ssoToken), false);
    }
    
    @Override
    public B2BUserProfileDetailBean getUserProfileDetail(String ssoToken) {
        if(StringUtils.isEmpty(ssoToken)){
            throw new PayswiftException(B2B_USER_PROFILE_SERVICE_EXCEPTION.getCode(), "ssoToken Empty for getUserProfileDetail");
        }

        try{
            String url = properties.getProperty("user.profile.service.endpoint");
            JSONObject requestJson = new JSONObject();
            requestJson.put("ssoToken", ssoToken);
            requestJson.put("method", "getUserProfileDetail");
            requestJson.put("application","web");

            logger.info("Hitting Falcon service " + url + " to get userProfile details with parameters : " + requestJson.toString());
           // String  userProfileData = httpUtil.invoke(url, requestJson.toString(), headers(), "FALCON_USER_PROFILE_DETAIL");
            HttpEndPoint serviceEndPoint = new HttpEndPoint(url, "POST");
            String userProfileData = httpService.invoke(serviceEndPoint,null,null,requestJson.toString(),true);
            logger.info("Falcon service response : " + userProfileData);

            if(StringUtils.isEmpty(userProfileData)){
                throw new PayswiftException(B2B_USER_PROFILE_SERVICE_EMPTY_RESPONSE.getCode(), "empty response from getUserProfileDetail");
            }

            JSONObject userProfileDataJsonObject = JSONObject.fromObject(userProfileData);

            return new B2BUserProfileDetailBean(userProfileDataJsonObject, true);

        } catch(Exception e){
            logger.error("Error getting UserProfileDetail for ssoToken :  "+ ssoToken , e);
            throw new PayswiftException(B2B_USER_PROFILE_SERVICE_EXCEPTION.getCode(), e.getMessage());
        }
    }

    @Override
    public String getAclDetailsForYatraCards(String accessToken) {
        if(StringUtils.isEmpty(accessToken)){
            logger.error("ssoToken Empty for getAclDetailsForYatraCards");
            return null;
        }
        
        String url = properties.getProperty("user.profile.acl.endpoint");
        JSONObject requestJson = new JSONObject();
        requestJson.put("accessToken", accessToken);
        requestJson.put("application","YatraCardsManager");
    
        HttpEndPoint serviceEndPoint = new HttpEndPoint(url, "POST");
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        String userDetails = httpService.invoke(serviceEndPoint,null,headers,requestJson.toString(),true);
        logger.info("ACL Corporate User Service response : " + userDetails);
        return userDetails;
    }
    
    private Map<String, String> headers() {
        Map<String, String> headers = new HashMap<>();
        headers.put("httpMethod", "POST");
        headers.put("contentType", CONTENT_TYPE_APPLICATION_JSON);
        headers.put("encoding", PaymentUIUtil.UTF_8_ENCODING);
        return headers;
    }
}
