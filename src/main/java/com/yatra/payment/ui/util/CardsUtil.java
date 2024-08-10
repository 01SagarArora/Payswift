package com.yatra.payment.ui.util;

import java.io.BufferedReader;
import java.security.Key;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.yatra.platform.util.YatraUtil;

public class CardsUtil {
	static Logger logger = Logger.getLogger(CardsUtil.class);
	
	private static final String ALGORITHM = "AES";
	private static final String KEYPHRASE = "27b1a565174b52e1";
	
	public static String getUserId(HttpServletRequest request) throws Exception {
		Map<String,String> requestMap = PaymentUIUtil.getRequestMap(request);
		String userId = requestMap.getOrDefault(PaymentUIUtil.USER_ID, null);
		if(YatraUtil.isNullOrEmpty(userId)) {
			JSONObject reqJson = parseInputRequest(request);
			logger.debug(reqJson);
			userId = reqJson.getString(PaymentUIUtil.USER_ID);
		}
		return userId;
	}

	
	private static JSONObject parseInputRequest(HttpServletRequest req) throws Exception {
		try{
			JSONObject requestJson ;
			StringBuilder buffer = new StringBuilder();
			BufferedReader reader = req.getReader();
			String line;
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
			} 
			String postBody = buffer.toString();
			
			requestJson = new JSONObject(postBody);
			return requestJson;
		}
		catch(Exception ex){
			logger.debug(ex);
			throw ex;
		}
	}


	public static String processCardInfoForSafeLogging(JSONObject resultJson) throws Exception {
		String cardJsonString = resultJson.toString();
		JSONObject cardJson = new JSONObject(cardJsonString);
		if(cardJson.has("cvv") && !cardJson.getString("cvv").equalsIgnoreCase("null"))
			cardJson.put("cvv", cardJson.getString("cvv").replaceAll(".", "x"));
		if(cardJson.has("cardNumber") && !cardJson.getString("cardNumber").equalsIgnoreCase("null"))
			cardJson.put("cardNumber",maskCreditCardNumber(cardJson.getString("cardNumber")));
		if(cardJson.has("expiryMonth") && !cardJson.getString("expiryMonth").equalsIgnoreCase("null"))
			cardJson.put("expiryMonth", cardJson.getString("expiryMonth").replaceAll(".", "x"));
		if(cardJson.has("expiryYear") && !cardJson.getString("expiryYear").equalsIgnoreCase("null"))
			cardJson.put("expiryYear", cardJson.getString("expiryYear").replaceAll(".", "x"));
		return cardJson.toString();
	}	
	
	public static String maskCreditCardNumber(final String cardNumber) {
		if (StringUtils.isBlank(cardNumber))
			return null;
		// we need to preserve only first 6 and last 4 digits of any credit card
		StringBuffer maskedCardNo = new StringBuffer(cardNumber);
		if (maskedCardNo.length() >= 6) {
			maskedCardNo.replace(6, maskedCardNo.length() - 4, cardNumber.substring(6, cardNumber.length() - 4).replaceAll(".", "x"));
		}
		return maskedCardNo.toString();
	}
	
	public static String getEncryptedData(String data) throws Exception {
        if (data == null) {
            throw new Exception("data cannot be null");
        }
        String keyPhrase = KEYPHRASE;
        Key key = new SecretKeySpec(keyPhrase.getBytes(), ALGORITHM);
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedData = cipher.doFinal(data.getBytes());
            String encryptedString = new BASE64Encoder().encode(encryptedData);
            return encryptedString;
        } catch(Exception e){
            logger.error("getEncryptedData", e);
            throw new Exception(e);
        }
    }

    public static String getDecryptedData(String data) throws Exception {
        if (data == null) {
            throw new Exception("data cannot be null");
        }
        String keyPhrase = KEYPHRASE;
        Key key = new SecretKeySpec(keyPhrase.getBytes(), ALGORITHM);
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decodeBuffer = new BASE64Decoder().decodeBuffer(data);
            byte[] decryptedData = cipher.doFinal(decodeBuffer);
            String decryptedString = new String(decryptedData);
            return decryptedString;
        }catch(Exception e){
            logger.error("getDecryptionKey", e);
            throw new Exception(e);
        }
    }
	
}
