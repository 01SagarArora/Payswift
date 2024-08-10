/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yatra.payment.offline.service;

import com.yatra.payment.ui.util.EnvironmentVariableReader;
import org.json.JSONArray;
import com.yatra.payment.offline.constants.GDSConstants;
import com.yatra.payment.offline.util.GDSUtil;
import com.yatra.payment.offline.util.VendorCodeEnum;
import com.yatra.platform.commons.property.PropertyManager;
import com.yatra.platform.http.HttpEndPoint;
import com.yatra.platform.http.HttpService;
import com.yatra.express.cards.v3.beans.UserCardInfo;


import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.xml.soap.SOAPBody;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author YATRAONLINE\rohit.lohia
 */
@Service
public class GDSService {
    
    @Autowired
    PropertyManager properties;
    
    @Autowired
    private HttpService httpService;
    
    private static Logger logger = Logger.getLogger(GDSService.class);
    
    public JSONObject populateNecessaryData(JSONObject data, Map<String, String> profiles) throws JSONException {
        //call to fetch corporateId and userId 
        
        String username = properties.getProperty("gds.save.card.username");
        String password = EnvironmentVariableReader.getEnvironmentVariable("gds_save_card_password");
        String profile = properties.getProperty("gds.save.card.hap");
        String url = properties.getProperty("gds.save.card.url");
        data.put("username", username);
        data.put("password", password);
        data.put("profile", profile + profiles.get("pcc"));
        data.put("url", url);
        data.put("crsId", "1G");
        data.put("pcc", profiles.get("pcc"));
        data.put("businessTitle", profiles.get("businessTitle"));
        data.put("personalTitle", profiles.get("personalTitle"));
        return data;
    }
    
    private JSONObject populateCardDetails(JSONObject params, UserCardInfo userCardInfo) throws JSONException {
        params.put("cardNumber", userCardInfo.getCardNumber1() + userCardInfo.getCardNumber2() + userCardInfo.getCardNumber3() + userCardInfo.getCardNumber4());
        params.put("cardVendor", VendorCodeEnum.VendorCodes.getCodeForBrand(userCardInfo.getCardBrand()));
        params.put("expiryMonth", userCardInfo.getExpiryMonth());
        params.put("expiryYear", userCardInfo.getExpiryYear());
        return params;
    }
    
    public String convertLineToThreeDigits(String line) {
        int length = line.length();
        if (length >= 3) {
            return line;
        }
        if (length == 2) {
            return "0"+line;
        }
        return "00"+line;
    }
    
    public String deleteCard(JSONObject params, String userId) throws JSONException {
    	boolean cardDeleted = false;
    	try {
        	JSONObject profileJson = getGDSProfiles(params.getString("ssoToken"));
        	Map<String, String> amadeusProfile = getProfile(profileJson, "amadeusGDSProfile");
        	Map<String, String> galProfile = getProfile(profileJson, "galGDSProfile");
            logger.info("delete card request received :"+ params.toString());
            if(amadeusProfile!=null)
            	cardDeleted = deleteCardInAmadeusProfile(userId);
        	if(galProfile!=null)
        		cardDeleted = deleteCardInGalileoProfile(params, galProfile);
        } catch (Exception ex) {
            logger.error("Couldn't delete card error params ", ex);
        }
        if(cardDeleted) return "Card Deleted";
        else return null;
    }
    
    public boolean isGDSCard(HttpServletRequest request) {
        String isGDSCard = request.getParameter(GDSConstants.IS_GDS_CARD);
        return !StringUtils.isEmpty(isGDSCard);
    }
    
    public String addCard(JSONObject params, UserCardInfo userCardInfo, String userId) {
    	boolean cardSaved = false;
    	try {
        	JSONObject profileJson = getGDSProfiles(params.getString("ssoToken"));
        	Map<String, String> amadeusProfile = getProfile(profileJson, "amadeusGDSProfile");
        	Map<String, String> galProfile = getProfile(profileJson, "galGDSProfile");
        	if(amadeusProfile!=null)
        		cardSaved = saveCardInAmadeusProfile(userId);
        	if(galProfile!=null)
        		cardSaved = saveCardInGalileoProfile(params, userCardInfo, galProfile);
        } catch (Exception ex) {
            logger.error("Couldn't add card error GDS", ex);
            cardSaved = false;
        }
        if(cardSaved == true)
        	return "Card Saved";
        else return null;
    }
    
    
    public String getLineNumberForFOP(JSONObject params, boolean isDeleteRequest, Map<String, String> profiles) {
        try {
            params = populateNecessaryData(params, profiles);
            SOAPBody soapBody = GDSUtil.sendPostCallToGalileo(params, "DisplayPAR");
            if (soapBody == null) {
                return null;
            }
            NodeList nodes = soapBody.getElementsByTagName("LineNum");
            //file contains no lines
            if (nodes.getLength() == 0) {
                if (isDeleteRequest) {
                    return null;
                }
                return "1";
            }
            Node fopNode = null;
            NodeList dataTypeNodes = soapBody.getElementsByTagName("DataType");
            for (int i=0;i<dataTypeNodes.getLength();i++) {
                String dataType = dataTypeNodes.item(i).getTextContent();
                
                if (!StringUtils.isEmpty(dataType)
                        && GDSConstants.FOP_DATA_TYPE == Integer.parseInt(dataType)) {
                    fopNode = dataTypeNodes.item(i);
                    break;
                }
            }
            if(fopNode != null) {
                Node parent = fopNode.getParentNode();
                    NodeList children = parent.getChildNodes();
                    for (int j=0;j<children.getLength();j++) {
                        Node node = children.item(j);
                        if ("LineNum".equalsIgnoreCase(node.getNodeName())) {
                            return node.getTextContent();
                        }
                    }
            }
            if (isDeleteRequest) {
                return null;
            }
            String text = nodes.item(nodes.getLength() - 1).getTextContent();
            return String.valueOf(Integer.parseInt(text) + 1);
        } catch (Exception ex) {
            logger.error("Couldn't get line number error: " + ex.getMessage());
        }
        return null;
    }
    
    public JSONObject getGDSProfiles(String ssoToken) throws JSONException {
        JSONObject corpGDSProfile = getCorpGDSDetails(ssoToken);
        if (corpGDSProfile == null) {
            return null;
        }
        JSONObject galGDSProfile = null;
        JSONObject amadeusGDSProfile = null;
        boolean cardAccessAllowedForGal = true;
        boolean cardAccessAllowedForAmadeus = true;
        if(corpGDSProfile.has("userDetails")) {
        	JSONArray userDetailsArray = corpGDSProfile.getJSONArray("userDetails");
        	for(int i = 0; i < userDetailsArray.length(); i++) {
        		JSONObject gdsDetails = userDetailsArray.getJSONObject(i);
        		if("GALILEO".equalsIgnoreCase(gdsDetails.getString("gds")))
        			galGDSProfile = gdsDetails;
        		else if("AMADEUS".equalsIgnoreCase(gdsDetails.getString("gds")))
        			amadeusGDSProfile = gdsDetails;
        	}
        	if(galGDSProfile!=null && Boolean.FALSE.equals(galGDSProfile.getBoolean("cardSaveEnabled"))) {
        		System.out.println("User doesn't have permission to access Galileo GDS profile ssoToken: " );
        		cardAccessAllowedForGal = false;
        	}
        	if(amadeusGDSProfile!=null && Boolean.FALSE.equals(amadeusGDSProfile.getBoolean("cardSaveEnabled"))) {
        		System.out.println("User doesn't have permission to access Amadeus GDS profile ssoToken: " );
        		cardAccessAllowedForAmadeus = false;
        	}
        	if(!cardAccessAllowedForAmadeus && !cardAccessAllowedForGal) {
        		System.out.println("User doesn't have permission to access any GDS profile ssoToken: " );
        		return null;
        	}
        } else {
        	logger.error("corpGDSProfile doesn't have userDetails Object for ssoToken: " + ssoToken);
        	return null;
        }
        JSONObject gdsDetailsJSON = new JSONObject();
        if(cardAccessAllowedForGal)
        	gdsDetailsJSON.put("galGDSProfile", galGDSProfile);
        if(cardAccessAllowedForAmadeus)
        	gdsDetailsJSON.put("amadeusGDSProfile", amadeusGDSProfile);
        
        return gdsDetailsJSON;
    }
    
    public boolean getSaveCardToGDSFlag(String ssoToken) throws JSONException {
    	try {
    		JSONObject corpGDSProfile = getCorpGDSDetails(ssoToken);
    		if (corpGDSProfile == null) {
    			return false;
    		}
    		JSONObject firstUserDetailsJsonObject = corpGDSProfile.getJSONArray("userDetails").getJSONObject(0);
    		if (firstUserDetailsJsonObject.has("cardSaveEnabled") && Boolean.TRUE.equals(firstUserDetailsJsonObject.getBoolean("cardSaveEnabled"))) {
    			return true;
    		}
    	} catch(Exception e) {
    		logger.error("Got exception while trying to get cardSaveEnabled flag value from corporate service", e);
    		return false;
    	}
    	return false;
    }
    
    public JSONObject getCorpGDSDetails(String ssoToken) throws JSONException {
        String userDetails;
        String url = properties.getProperty("corporate.user.profile.gds.service.endpoint");
        JSONObject requestJson = new JSONObject();
        requestJson.put("ssoToken", ssoToken);

        Map<String, String> requestParameters = new HashMap<String, String>();
        requestParameters.put("POSTDATA", requestJson.toString());

        logger.debug("Hitting SSO Corporate User Service to get userId and corporateId for GDS with parameters : " + requestJson.toString());
        HttpEndPoint serviceEndPoint = new HttpEndPoint(url, "POST");
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        headers.put("Content-Type", "application/json");
        userDetails = httpService.invoke(serviceEndPoint,null,headers,requestJson.toString(),true);
        logger.info("SSO Corporate User Service response : " + userDetails);
        return new JSONObject(userDetails);
    }
    
    private Map<String, String> getProfile(JSONObject gdsProfile, String supplierProfile) throws Exception{
    	if(gdsProfile == null || !gdsProfile.has(supplierProfile))
    		return null;
    	Map<String, String> result = new HashMap<String, String>();
        if(gdsProfile.getJSONObject(supplierProfile)!=null) {
        	result.put("businessTitle", gdsProfile.getJSONObject(supplierProfile).getString("businessTitle"));
            result.put("personalTitle", gdsProfile.getJSONObject(supplierProfile).getString("personalTitle"));
            if (gdsProfile.getJSONObject(supplierProfile).has("pcc") && !StringUtils.isEmpty(String.valueOf(gdsProfile.getJSONObject(supplierProfile).get("pcc")))) {
                result.put("pcc",String.valueOf(gdsProfile.getJSONObject(supplierProfile).get("pcc")));
            } else {
                result.put("pcc", properties.getProperty("gds.save.card.pcc"));
            }
        }        
        return result;
	}
    
    private boolean deleteCardInGalileoProfile(JSONObject params,
			Map<String, String> profiles) {
    	boolean cardDeleted = false;
    	try {
    		 String line = getLineNumberForFOP(params, true, profiles);
             if (StringUtils.isEmpty(line)) {
                 throw new Exception("No line number to delete.");
             }
             params.put("lineNumber", convertLineToThreeDigits(line));
             SOAPBody result = GDSUtil.sendPostCallToGalileo(params, "DeleteCard");
             if (result != null) 
                 cardDeleted = true;
    	} catch(Exception ex) {
    		logger.error("Couldn't delete card error params ", ex);
    		cardDeleted = false;
    	}
    	return cardDeleted;
	}

	private boolean saveCardInGalileoProfile(JSONObject params, UserCardInfo userCardInfo,
			Map<String, String> galProfile) {
    	boolean cardSaved = false;
    	try {
    		String line = getLineNumberForFOP(params, false, galProfile);
            if (null == line) {
                throw new Exception("Line number couldn't be retrieved.");
            }
            params.put("lineNumber", convertLineToThreeDigits(line));
            params = populateCardDetails(params, userCardInfo);
            SOAPBody soapBody = GDSUtil.sendPostCallToGalileo(params, "AddCard");
            if (soapBody != null) {
            	cardSaved = true;
            }
    	} catch (Exception ex) {
    		logger.error("Couldn't add card error Galileo GDS", ex);
            cardSaved = false;
    	}
        return cardSaved;
	}

	private boolean deleteCardInAmadeusProfile(String userId) {
		return dropUserIdForAmadeusProfileSync(Long.valueOf(userId));
	}
	
    private boolean saveCardInAmadeusProfile(String userId) {
    	return dropUserIdForAmadeusProfileSync(Long.valueOf(userId));
	}
    
    public boolean dropUserIdForAmadeusProfileSync(Long userId) {
    	boolean status = false;
    	try {
    		String response;
    		String url = properties.getProperty("corporate.user.profile.gds.card.sync.service.endpoint");
    		JSONObject requestJson = new JSONObject();
    		JSONArray userIds = new JSONArray();
    		userIds.put(userId);
    		requestJson.put("userIds", userIds);
    		Map<String, String> requestParameters = new HashMap<String, String>();
    		requestParameters.put("POSTDATA", requestJson.toString());
    		logger.debug("Hitting SSO Corporate User Service to sync card to Amadeus for GDS with parameters : " + requestJson.toString());
    		HttpEndPoint serviceEndPoint = new HttpEndPoint(url, "POST");
    		Map<String, String> headers = new HashMap<>();
    		headers.put("Accept", "application/json");
    		headers.put("Content-Type", "application/json");
    		response = httpService.invoke(serviceEndPoint,null,headers,requestJson.toString(),true);
    		logger.info("SSO Corporate User Service response : " + response);
    		JSONObject resJson = new JSONObject(response);
    		status = resJson.getBoolean("status");
    	} catch(Exception e) {
    		status = false;
    	}
    	return status;
    }
}
