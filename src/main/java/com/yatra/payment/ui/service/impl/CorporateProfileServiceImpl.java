package com.yatra.payment.ui.service.impl;

import com.yatra.express.crp.cards.v3.beans.CorporateCardInfo;

import java.util.*;

import com.yatra.payment.payswift.beans.B2BUserProfileDetailBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.client.utils.PaymentConstants;
import com.yatra.payment.payswift.FutureTasks.EntityAndUsersData;
import com.yatra.payment.qb.corporate.bean.GetCardsResponse;
import com.yatra.platform.commons.property.PropertyManager;
import com.yatra.platform.http.HttpEndPoint;
import com.yatra.platform.http.HttpService;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.FutureTask;
import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import org.json.JSONObject;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Service
public class CorporateProfileServiceImpl {
	private Logger logger = Logger.getLogger(CorporateProfileServiceImpl.class);
	
	@Autowired PropertyManager propertyManager;
	@Autowired HttpService httpService;
	@Autowired PropertyManager properties;
	@Autowired ThreadPoolTaskExecutor threadPoolTaskExecutor;
         
    public static final String BILLING_ENTITIES = "billingEntities";
    public static final String CORPORATE_USER_PROFILES = "corporateUsersProfile";
         
	 public JSONObject getEntityGroupsJson(String clientId) throws Exception {
		try {
            String url = String.format(propertyManager.getProperty("crp.profile.entity.groups.service.endpoint"), clientId);
            HttpEndPoint serviceEndPoint = new HttpEndPoint(url, "GET");
            Map<String, String> headers = new HashMap<>();
            headers.put("Accept", "application/json");
            headers.put("Content-Type", "application/json");
            headers.put("com.yatra.tenant.header.tenantId", PaymentConstants.DEFAULT_TENANTID);
            logger.info("Hitting Corporate Profile Service to fetch Entity Groups for ClientID : " + clientId + " and URL is : " + url);
            
            String response = httpService.invoke(serviceEndPoint, null, headers);
            
            logger.info("Received response from Corporate Profile Service for fetching Entity Groups for ClientID : " + clientId + " is : " + response);
            
            if(StringUtils.isEmpty(response)) throw new Exception("Empty response from the service.");
            return new JSONObject(response);
        } catch (Exception ex) {
            logger.error("Error getting Entity Groups for clientId :  " + clientId + ". Exception is : " + ex);
            throw ex;
        }
	}
         
     public void addUsersAndEntityData(List<CorporateCardInfo> corporateCards, GetCardsResponse getCardsResponse) {
    	if(corporateCards == null || corporateCards.size() < 1)
    		return;
        JSONArray users = new JSONArray();
        JSONArray entities = new JSONArray();
        for (CorporateCardInfo corpCard: corporateCards) {
            if (corpCard.getLevelGroup() != null) {
                List<String> userList = corpCard.getLevelGroup().getUser();
                if (userList != null && !userList.isEmpty()) {
                    users.addAll(userList);
                }
                List<String> entityList = corpCard.getLevelGroup().getEntity();
                if (entityList != null && !entityList.isEmpty()) {
                    entities.addAll(entityList);
                }
            }
        }
        FutureTask<String> futureTaskForUsersData = null;
        FutureTask<String> futureTaskForEntityData = null;
        if (!users.isEmpty()) {
            futureTaskForUsersData = new FutureTask<>(new EntityAndUsersData(this, users, true));
            threadPoolTaskExecutor.submit(futureTaskForUsersData);
        }
        if (!entities.isEmpty()) {
            futureTaskForEntityData = new FutureTask<>(new EntityAndUsersData(this, entities, false));
            threadPoolTaskExecutor.submit(futureTaskForEntityData);
        }
        try {
            if (futureTaskForUsersData != null) {
                getCardsResponse.setUsers(futureTaskForUsersData.get());
            }
            if (futureTaskForEntityData != null) {
                getCardsResponse.setEntities(futureTaskForEntityData.get());
            } 
        } catch (Exception ex) {
            logger.error("Error occured while getting users and entity data: " + ex.getMessage());
        }
    }
        
    private String generateUrlString(String url, JSONArray entities) {
        StringBuilder urlString = new StringBuilder(url);
        urlString.append("?");
        String separatorStr = "";
        for (int i = 0, size = entities.size(); i < size; i++) {
            urlString.append(separatorStr);
            urlString.append("ids=");
            urlString.append(String.valueOf(entities.get(i)));
            separatorStr = "&";
        }
        
        return urlString.toString();
    }
    
    public String getEntityData(JSONArray entities) {
        try {
            String url = properties.getProperty("corporate.user.profile.service.entity.details.endpoint");
            String urlString = generateUrlString(url, entities);
            logger.debug("Hitting Corporate User Service to get entity information url : " + urlString);
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGetRequest = new HttpGet(urlString);
            httpGetRequest.setHeader("Accept", "application/json");
            httpGetRequest.setHeader("com.yatra.tenant.header.tenantId", PaymentConstants.DEFAULT_TENANTID);
            HttpResponse response = httpClient.execute(httpGetRequest);
            BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
            String output;
            StringBuilder contentBuilder = new StringBuilder();
            while ((output = br.readLine()) != null) {
                    contentBuilder.append(output);
            }
            logger.info("Corporate User Service entity response : " + contentBuilder);
            net.sf.json.JSONObject responseJSON = net.sf.json.JSONObject.fromObject(contentBuilder.toString());
            return getBillingEntities(responseJSON);
        } catch (Exception ex) {
            logger.error("Failed to fetch entity data from Corporate Service " + ex.getMessage());
        }
        return null;
    }
    
    private String getBillingEntities(net.sf.json.JSONObject responseJSON) {
        if (responseJSON.get(BILLING_ENTITIES) == null) {
            return null;
        }
        JSONArray entityList = responseJSON.getJSONArray(BILLING_ENTITIES);
        net.sf.json.JSONObject returnObject = new net.sf.json.JSONObject();
        for (int i=0;i<entityList.size(); i++) {
            net.sf.json.JSONObject json = (net.sf.json.JSONObject) entityList.get(i);
            String id = String.valueOf(json.get("id"));
            returnObject.put(id, json);
        }
        return returnObject.toString();
    }
    
    public String getUsersData(JSONArray users) {
        try {
            String url = properties.getProperty("corporate.user.profile.service.user.details.endpoint");
            net.sf.json.JSONObject requestJson = new net.sf.json.JSONObject();
            requestJson.put("userIds", users.toString());

            logger.debug("Hitting Corporate User Service to get user information with parameters : " + requestJson.toString());
            HttpEndPoint serviceEndPoint = new HttpEndPoint(url, "POST");
            Map<String, String> headers = new HashMap<>();
            headers.put("Accept", "application/json");
            headers.put("Content-Type", "application/json");
            headers.put("com.yatra.tenant.header.tenantId", PaymentConstants.DEFAULT_TENANTID);
            String response = httpService.invoke(serviceEndPoint,null,headers,requestJson.toString(),true);
            logger.info("Corporate User Service response : " + response);
            net.sf.json.JSONObject responseJSON = net.sf.json.JSONObject.fromObject(response);
            if (!(responseJSON.get(CORPORATE_USER_PROFILES) instanceof JSONNull)) {
                return responseJSON.getJSONObject(CORPORATE_USER_PROFILES).toString();
            }
        } catch (Exception ex) {
            logger.error("Failed to fetch users data from Corporate Service " + ex.getMessage());
        }
        return null;
    }

    public List<CorporateCardInfo> filterEmployeeLevelPcc(List<CorporateCardInfo> corporateCardInfoList , B2BUserProfileDetailBean userProfileDetailBean){

         List<CorporateCardInfo> filteredCorporateCardList = new ArrayList<CorporateCardInfo>();
         if(corporateCardInfoList != null && !corporateCardInfoList.isEmpty()){
             for (CorporateCardInfo corporateCard : corporateCardInfoList) {

                 if (StringUtils.equalsIgnoreCase(corporateCard.getCardType(), "PCC")) {
                     List<String> userList = corporateCard.getLevelGroup().getUser();

                     if (userList != null && userList.contains(userProfileDetailBean.getUserId())) {
                         filteredCorporateCardList.add(corporateCard);
                     }
                 }

             }

         }

         return filteredCorporateCardList;
    }
}
