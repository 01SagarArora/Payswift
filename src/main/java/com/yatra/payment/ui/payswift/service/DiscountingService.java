package com.yatra.payment.ui.payswift.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.yatra.payment.payswift.beans.VisaCardValidationResponse;
import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.platform.commons.property.PropertyManager;
import com.yatra.platform.http.HttpService;

@Service 
@Qualifier("visaDiscountingService")
public class DiscountingService {
	
	@Autowired	private PropertyManager  propertyManager;
	@Autowired	private HttpService httpService;
	@Autowired LoggingProcessor loggingProcessor;
	
	Logger logger = Logger.getLogger(DiscountingService.class);

	/*public static String maskCreditCardNumber(final String cardNumber) {
		if (StringUtils.isBlank(cardNumber))
			return null;
		// we need to preserve only first 6 and last 4 digits of any credit card
		StringBuffer maskedCardNo = new StringBuffer(cardNumber);
		if (maskedCardNo.length() >= 6) {
			maskedCardNo.replace(6, maskedCardNo.length() - 4, cardNumber.substring(6, cardNumber.length() - 4).replaceAll(".", "x"));
		}
		return maskedCardNo.toString();
	}*/

	  /* public String getHashedValue(String data, String hashingAlgorithm) {
			if (data == null)
			    return null;
			
			
			MessageDigest md = null;
			try {
			    md = MessageDigest.getInstance(hashingAlgorithm);
			} catch (Exception outerException) {
			    logger.error(outerException.getMessage());
			}

			md.update(data.getBytes());
			byte[] mdbytes = md.digest();

			// convert the byte to hex format method 2
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < mdbytes.length; i++) {
			    String hex = Integer.toHexString(0xFF & mdbytes[i]);
			    if (hex.length() == 1) {
				hexString.append('0');
			    }
			    hexString.append(hex);
			}
			return hexString.toString();
		    }
*/
	

	private VisaCardValidationResponse constructValidationResponse(String response) throws JSONException {
		JSONObject jsonResponse = new JSONObject(response);
		VisaCardValidationResponse visaCardValidationResponse = new VisaCardValidationResponse();
		try{
		visaCardValidationResponse.setStatus(jsonResponse.optString(PaymentUIUtil.STATUS));
		visaCardValidationResponse.setDiscountType(jsonResponse.optString(PaymentUIUtil.DISCOUNT_TYPE));
		visaCardValidationResponse.setMessage(jsonResponse.optString(PaymentUIUtil.Message));
		visaCardValidationResponse.setAmount(jsonResponse.optString(PaymentUIUtil.AMOUNT));
		}catch(Exception ex){
			logger.error("Exception Occured while validating card discouting service , "
					+ "most probably card service is down or is not responding correctly ,"
					+ "Received this from card discouting service ");
		}
		
		return visaCardValidationResponse;
	}

	/*public VisaCardValidationResponse isQbCardValidForVisaDiscount(String cardId,String superPnr,String product) {
		logger.info("Inside isQbCardValidForVisaDiscount for super PNR "+superPnr);
		
		String discountingUrl = null ;
		String response =null ;
		try{
			
			logger.info("Visa Card Validation request received for superPnr "+superPnr);
			discountingUrl = "http://172.16.1.129/VMDiscountingService/isValidCard";//propertyManager.getProperty("visa.card.validation.endPoint");  
			logger.debug("Hitting visa discounting service hosted at "+discountingUrl+" for Super PNR "+superPnr);
			Map<String,String> parameterMap = new HashMap<>();
			parameterMap.put(PaymentUIUtil.CARD_ID,cardId);
			
			String postData = discountingUrl + "?" + PaymentUIUtil.createPostDataFromMap(parameterMap);
			loggingProcessor.logIntoPaymentStages(superPnr, PaymentUIUtil.VALIDATE_VISA_DISCOUNTING_REQ, postData, product);
			
			response = httpService.invoke(discountingUrl,parameterMap);
			
			loggingProcessor.logIntoPaymentStages(superPnr, PaymentUIUtil.VALIDATE_VISA_DISCOUNTING_RES, response, product);
			VisaCardValidationResponse visaCardValidationResponse = constructValidationResponse(response);
			return visaCardValidationResponse;
		}catch(Exception ex){
			logger.error("Exception occured while verifying visa discount for superPnr "+superPnr);
			logger.error("Service endpoint Hit "+discountingUrl+" response : "+response,ex);
			return null;
		}
	}
		
	
	
	public VisaCardValidationResponse isCardValidForVisaDiscount(String cardNumber,String superPnr,String product) {
		String discountingUrl = null ;
		String cardMask = null;
		String response =null ;
		try{
			
			logger.info("Visa Card Validation request received for superPnr "+superPnr);
			discountingUrl = propertyManager.getProperty("visa.card.validation.endPoint");  
			String cardHash = PaymentUIUtil.getHashedValue(cardNumber, "SHA-512");
			String cardBin = PaymentUIUtil.extractBin(cardNumber);
			logger.debug("Hitting visa discounting service hosted at "+discountingUrl+" for card Number "+cardMask);
			Map<String,String> parameterMap = new HashMap<>();
			parameterMap.put(PaymentUIUtil.CARD_BIN,cardBin);
			parameterMap.put(PaymentUIUtil.CARD_HASH,cardHash);
			
			String postData = discountingUrl + "?" + PaymentUIUtil.createPostDataFromMap(parameterMap);
			loggingProcessor.logIntoPayswiftStages(superPnr, PaymentUIUtil.VALIDATE_VISA_DISCOUNTING_REQ, postData, product);
			
			response = httpService.invoke(discountingUrl,parameterMap);
			loggingProcessor.logIntoPaymentStagesSerial(superPnr, PaymentUIUtil.VALIDATE_VISA_DISCOUNTING_RES, response, product);
			VisaCardValidationResponse visaCardValidationResponse = constructValidationResponse(response);
			return visaCardValidationResponse;
		}catch(Exception ex){
			logger.error("Exception occured while verifying visa discount for card Number "+cardMask);
			logger.error("Service endpoint Hit "+discountingUrl+" response : "+response,ex);
			return null;
		}
		
		
				
		
		
		
	}
	*/
	
	public VisaCardValidationResponse isCardValidForVisaDiscount(String cardData,String superPnr,String product,String payOp) {
			logger.info("Inside isQbCardValidForCardDiscount for super PNR "+superPnr);
			String cardMask = null;
			String discountingUrl = null ;
			String response =null ;
			try{
				
				logger.info("Card Validation request received for superPnr "+superPnr);
				discountingUrl =propertyManager.getProperty("visa.card.validation.endPoint");  
				logger.debug("Hitting card discounting service hosted at "+discountingUrl+" for Super PNR "+superPnr);
				Map<String,String> parameterMap = new HashMap<>();
				if(payOp.equalsIgnoreCase("qb")){
					parameterMap.put(PaymentUIUtil.CARD_ID,cardData);
				}
				else {
					String cardHash = PaymentUIUtil.getHashedValue(cardData, "SHA-512");
					String cardBin = PaymentUIUtil.extractBin(cardData);
					parameterMap.put(PaymentUIUtil.CARD_BIN,cardBin);
					parameterMap.put(PaymentUIUtil.CARD_HASH,cardHash);
				}
				String postData = discountingUrl + "?" + PaymentUIUtil.createPostDataFromMap(parameterMap);
				loggingProcessor.logIntoPayswiftStages(superPnr, PaymentUIUtil.VALIDATE_CARD_DISCOUNTING_REQ, postData, product);
				
				response = httpService.invoke(discountingUrl,parameterMap);
				
				loggingProcessor.logIntoPayswiftStages(superPnr, PaymentUIUtil.VALIDATE_CARD_DISCOUNTING_RES, response, product);
				VisaCardValidationResponse visaCardValidationResponse = constructValidationResponse(response);
				return visaCardValidationResponse;
			}catch(Exception ex){
				logger.error("Exception occured while verifying card discount for superPnr "+superPnr);
				logger.error("Service endpoint Hit "+discountingUrl+" response : "+response,ex);
				return null;
			}
				
		
	}
		    

}
