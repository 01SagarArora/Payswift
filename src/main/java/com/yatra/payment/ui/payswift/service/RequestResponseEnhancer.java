package com.yatra.payment.ui.payswift.service;

import com.yatra.payment.client.enums.ResponseStatus;
import com.yatra.payment.payswift.beans.*;
import com.yatra.payment.ui.beans.BajajEmiRequest;
import com.yatra.payment.ui.dao.YatraPropertiesDAO;
import com.yatra.payment.ui.dao.impl.PayswiftStagesDAOImpl;
import com.yatra.payment.ui.enums.ClientType;
import com.yatra.payment.ui.enums.FailureCode;
import com.yatra.payment.ui.enums.ResponseDecisionScenarios;
import com.yatra.payment.ui.exceptions.PayswiftException;
import com.yatra.payment.ui.helper.PaymentUIHelper;
import com.yatra.payment.ui.helper.RequestResponseEnhancementHelper;
import com.yatra.payment.ui.service.AgentProfileService;
import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.platform.commons.property.PropertyManager;
import com.yatra.platform.util.YatraUtil;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.yatra.payment.ui.enums.FailureCode.B2B_USER_PROFILE_SERVICE_EXCEPTION;
import static com.yatra.payment.ui.enums.ResponseDecisionScenarios.SUCESS_REDIRECT_TO_PORTAL;

import java.util.logging.Level;

import static org.apache.commons.lang.StringUtils.isEmpty;

import org.apache.commons.lang3.StringUtils;

@Service
public class RequestResponseEnhancer implements PayNowProcessor{
	
	@Autowired
	private PayNowProcessor guestUserSaveCardProcessor;
	@Autowired 
	private PayswiftStagesDAOImpl paymentDAO;
	@Autowired
	private RequestResponseEnhancementHelper rreHelper;
	@Autowired
	private LobDetailsService lobDetailsService;
	@Autowired
	private AgentProfileService agentProfileService;
	@Autowired
	private PaymentUIHelper paymentUIHelper;
	@Autowired 
	private YatraPropertiesDAO yatraPropertiesDAO;
	
	@Autowired 
	private PropertyManager propertyManager;
	
	private static Logger logger = Logger.getLogger(RequestResponseEnhancer.class);

	private static List<String> privyBins = Arrays.asList("447746","407659","447747","410059","407651","447758","444341"
			,"402368","402853","552418","517719","517653","517637","525996","540282","545207","486630","462986","524193"
			,"524376","523951","377040","377041","377043","376944","473195","455452","470573","377042","423227","423226"
			,"437551","374740","374741","474846","374742","450172","461133","405533","407352","431581","222708","222709"
			,"222710","401704","401711","401767","401772","401780","401784","401796","401798","401806","401809","401920"
			,"402635","402657","403641","405994","406597","406894","407984","409513","409642","409643","413531","414366"
			,"414367","415623","415625","415641","416438","418123","418124","418133","418157","418158","418228","418276"
			,"419975","420084","420088","421323","421329","421395","421438","421478","421480","421495","421590","421627"
			,"421628","421630","421635","421647","421675","421676","428382","428383","430366","432716","432727","434301"
			,"436534","436543","438114","438503","439359","450711","455446","455451","457274","459648","464651","464970"
			,"465474","465511","465514","465573","466597","466704","466706","466730","466731","468799","468949","468957"
			,"468958","469164","469375","470456","470589","471267","471268","472254","473196","473903","475717","477798"
			,"478677","479932","479966","480259","483834","483851","484411","490015","490016","504642","504991","508534"
			,"511382","511385","511414","511416","514765","516105","516107","516640","517435","517890","522318","523673"
			,"524290","524451","524482","527536","529372","529374","529375","529376","531804","532127","532666","532718"
			,"532729","533112","534044","534424","534450","535960","535995","536012","537654","537687","539189","539190"
			,"539998","542464","542541","542572","542705","549533","559404","559405","559423","559431","559462","559469"
			,"559725","559735","559939","559946","559966","607406","607408","607411","607412","607415","607416","607417"
			,"607423","607425","607426","607462","607466","607470","607473","607474","607478","607479","607540","608062"
			,"608063","608064","608076","608199","652170","652180","652245","940105","940106","940111","940112","940120"
			,"940134","940135","940136","940139","940140","940149","940150","940151","940152","940153","940160");
	
	@Override
	public PayNowResponse process(PayNowRequest payNowRequest,PayNowResponse payNowResponse) {
		
		String paymentOptionParameters = payNowRequest.getParameter("paymentOptionParameters");
		Map<String, String> pgDataMap = rreHelper.getPaymentOptionParameters(paymentOptionParameters);
		
		payNowRequest.putPgDataMap(pgDataMap);
		LobDetailResponse bookingDetails = fetchBookingDetails(payNowRequest);
		payNowRequest.setBookingDetails(bookingDetails);
		if(!validateSkipOtpFlow(payNowRequest)) {
			logger.debug("RequestResponseEnhancer : Fraud case, SkipOtp flag is true for B2C product, hence returning false");
			payNowResponse.setResponseDecision(ResponseDecisionScenarios.GENERIC_ERROR);
			return prepareResponse(payNowRequest,payNowResponse);
		}
		if(violatingCreditCardConditionForBookingDotCom(payNowRequest)) {
			logger.debug("RequestResponseEnhancer : Cannot use Debit Card in Booking.com flow, hence returning false");
			payNowResponse.setResponseDecision(ResponseDecisionScenarios.BOOKINGDOTCOM_ERROR);
			return prepareResponse(payNowRequest,payNowResponse);
		}
		if(violatingPrivyFlowCondition(payNowRequest)) {
			logger.debug("RequestResponseEnhancer : Cannot use this card in current privy flow, hence returning false");
			payNowResponse.setResponseDecision(ResponseDecisionScenarios.PRIVY_FLOW_ERROR);
			return prepareResponse(payNowRequest,payNowResponse);
		}
		if(!validateCorpCardEligibilityForPayment(payNowRequest)){
			String cardtype = pgDataMap.get(PaymentUIUtil.CARD_TYPE_KEY_FOR_CORPORATE);
			logger.debug("RequestResponseEnhancer :  card type is "+cardtype+" and isNotTokenized ,hence returning false ");
			payNowResponse.setResponseDecision(ResponseDecisionScenarios.CARD_NOT_ELIGIBLE_FOR_PAYMENT);
			return prepareResponse(payNowRequest,payNowResponse);
		}
		
		if(bookingDetails.getResponseStatus() == ResponseStatus.SUCCESS){
			logger.debug("RequestResponseEnhancer : getBookingDetails Success, requestEnhancer complete now forwarding to ewallet handler .");
			payNowResponse = guestUserSaveCardProcessor.process(payNowRequest, payNowResponse);
			setAgentCodeIfRequired(payNowRequest, payNowResponse);
			setMinkasuFlag(payNowRequest, payNowResponse);
			setJusPayFlag(payNowResponse);

			if("twid".equalsIgnoreCase(payNowRequest.getPaymentOption()) 
					&& !isUserEligible(payNowRequest.getBookingDetails().getUserMobile(), payNowResponse.getValidationResponse().getAmountToCollect(),
							payNowRequest.getSuperPnr(), payNowRequest.getBookingDetails().getProductCode(), payNowRequest.getPromoCode())) {
				logger.debug("RequestResponseEnhancer : Twid eligibility Failed, Error Message : USER_NOT_ELIGIBLE_FOR_TWID" );
				payNowResponse.setResponseDecision(ResponseDecisionScenarios.GENERIC_ERROR);
				payNowResponse.setFailureCode(bookingDetails.getFailureCode());
				payNowResponse.setMessage("Dear customer, you currently don't have any reward balance. Please choose another payment mode to pay");
			}
			
			try {
				if("tez".equalsIgnoreCase(payNowRequest.getPaymentOption())) {
					logger.debug("RequestResponseEnhancer : TEZ UPI verification");
					int maxAmount = propertyManager.getPropertyAsInt("upi.max.amount");
					String displayedAmount = payNowRequest.getAmountDisplayed();
					int currAmount = Integer.parseInt(displayedAmount);
					if (currAmount >= maxAmount) {
						logger.debug("RequestResponseEnhancer : UPI Amount over Rs. 1 lakh, Error Message : AMOUNT_IS_OVER_ONE_LAKH");
						payNowResponse.setResponseDecision(ResponseDecisionScenarios.GENERIC_ERROR);
						payNowResponse.setFailureCode(bookingDetails.getFailureCode());
						payNowResponse.setMessage("UPI Payment limit is Rs. 1 lakh. Please use a different payment option.");
					}
				}
				if("upi".equalsIgnoreCase(payNowRequest.getPaymentOption()))
				{
					logger.debug("RequestResponseEnhancer : UPI verification");
				
					int maxAmount = propertyManager.getPropertyAsInt("upi.max.amount");
					String displayedAmount = payNowRequest.getAmountDisplayed();
					int currAmount = Integer.parseInt(displayedAmount);
					if(currAmount >= maxAmount) {
						logger.debug("RequestResponseEnhancer : UPI Amount over Rs. 1 lakh, Error Message : AMOUNT_IS_OVER_ONE_LAKH" );
						payNowResponse.setResponseDecision(ResponseDecisionScenarios.GENERIC_ERROR);
						payNowResponse.setFailureCode(bookingDetails.getFailureCode());
						payNowResponse.setMessage("UPI Payment limit is Rs. 1 lakh. Please use a different payment option.");
					}
					else {
						
					boolean status = false;
					status = validateAndSetVPA(payNowRequest);

					if(!status) {
						logger.debug("RequestResponseEnhancer : UPI validation Failed, Error Message : USER_UPI_VALIDATION_FAILED" );
						payNowResponse.setResponseDecision(ResponseDecisionScenarios.GENERIC_ERROR);
						payNowResponse.setFailureCode(bookingDetails.getFailureCode());
						payNowResponse.setMessage("The UPI ID provided does not exist. Please check the UPI ID and try again.");
					}
				}
				}	
			}catch(Exception e) {
				logger.error("Exception Occured While VPA : " ,e);
			}
			
			// for bajaj Emi eligibility API
			try {

				if ("emi".equalsIgnoreCase(payNowRequest.getPaymentOption())) {

					// checking wether the user selected card is bajaj or not

					if (PaymentUIUtil.CARD_TYPE_BAJAJ.equals(payNowRequest.getCardType())) {
						// this code will also need to have a condition when the emi
						// selected card is bajaj_emi only in that case this code should execute.
						logger.debug("RequestResponseEnhancer : Bajaj Emi Eligibility check");

						
						BajajEmiRequest request = new BajajEmiRequest();
						request.setSuperPnr(payNowRequest.getSuperPnr());
						request.setCardNumber(payNowRequest.getCardNumber());
						request.setMerchantCode(payNowRequest.getMerchantCode());
						request.setProductCode(payNowRequest.getProductCode());
						request.setAmount(payNowRequest.getAmountDisplayed());
						request.setTenure(payNowRequest.getEMITenure());
						request.setTtid(payNowResponse.getValidationResponse().getTtid());
						
						
						String requestString = PaymentUIUtil.getJson(request);
						
						String respString = "";
						
						if(requestString != null) {
						 respString = PaymentUIUtil.checkEmiEligibility(requestString);	
						}
						
						if(!respString.isEmpty()) {
							JSONObject responseJson = new JSONObject(respString);

							String msg = responseJson.optString("ResponseDescription");
							String errorType = responseJson.optString("ErrorCode");
							String errorCode = responseJson.optString("Responsecode");
							
							
							//if it's not 00 then it will run
							if (!"00".equals(errorCode)) {
								logger.debug(
										"RequestResponseEnhancer : Bajaj eligibility Failed, Error Message : USER_NOT_ELIGIBLE_FOR_BAJAJ");
								payNowResponse.setResponseDecision(ResponseDecisionScenarios.GENERIC_ERROR);
								payNowResponse.setFailureCode(bookingDetails.getFailureCode());
								payNowResponse.setMessage(msg);
							}
						}
			            
						
					}

				}
			} catch (Exception e) {
				logger.error("Exception occured while bajaj eligibility check : ", e);
			}

		} else{
			logger.debug("RequestResponseEnhancer : getBookingDetails Failed, Error Message :" + bookingDetails.getFailureCode().getCode());
			payNowResponse.setResponseDecision(ResponseDecisionScenarios.GENERIC_ERROR);
			payNowResponse.setFailureCode(bookingDetails.getFailureCode());
		}
		return prepareResponse(payNowRequest,payNowResponse);
	}

	/**
	 * case: TwidEligibility check will be skipped
	 * 		-> when promoCodes used is configured in twid.promo.codes.list property
	 * 		-> twid.promo.codes.list contains 'ALL'
	 */
	private boolean isUserEligible(String userMobile,
			String amountToCollect, String superPnr, String product, String promoCode) {
		List<String> twidSpecificPromoCodeList = PaymentUIUtil.getTwidSpecificPromoCodeList(yatraPropertiesDAO); //paymentDAO.getYatraPropertyValueFromDB("check.twid.eligibility.for.promocode");
		logger.info("Promo codes configured in DB: "+twidSpecificPromoCodeList);
		if(twidSpecificPromoCodeList!=null && twidSpecificPromoCodeList.size() > 0 
				&& (twidSpecificPromoCodeList.contains(promoCode) || twidSpecificPromoCodeList.contains("ALL"))) {
			logger.info("Skipping twid eligibility check");
			return true;
		}
		logger.info("checking TwidEligibility for mobile: "+userMobile);		
		try {
			String response = PaymentUIUtil.makeCheckTwidEligibilityCall(userMobile, amountToCollect, superPnr, product);
			logger.info("Response for checkTwidEligibility API : " + response);
			return new JSONObject(response).getBoolean("isUserEligibile");
		} catch(Exception e) {
			return true;
		}
	}

	private boolean violatingCreditCardConditionForBookingDotCom(
			PayNowRequest payNowRequest) {
		String product = payNowRequest.getBookingDetails().getProductCode();
		boolean isBookingDotComFlow = false;
		if("INT_HOTEL_BKG".equalsIgnoreCase(product)
				|| "minthoteliosbkg".equalsIgnoreCase(product)
				|| "minthotelandroidbkg".equalsIgnoreCase(product)
				|| "smepinthotelandroidbkg".equalsIgnoreCase(product)
				|| "smeointhotelandroidbkg".equalsIgnoreCase(product)
				|| "smepinthoteliosbkg".equalsIgnoreCase(product)
				|| "smeointhoteliosbkg".equalsIgnoreCase(product))
			isBookingDotComFlow = true;

		if(("crpdomhotel".equalsIgnoreCase(product)
				|| "cdomhotelandroid".equalsIgnoreCase(product)
				|| "cdomhotelios".equalsIgnoreCase(product)
				|| "cinthotelandroid".equalsIgnoreCase(product)
				|| "cinthotelios".equalsIgnoreCase(product)
				|| "crpinthotel".equalsIgnoreCase(product))
				&& payNowRequest.getBookingDetails().isBookingDotComFlow())
			isBookingDotComFlow = true;

		if(isBookingDotComFlow) {
			String cardBin = payNowRequest.getCardBin();
			String cardbinType = paymentDAO.getCardBinType(cardBin);
			if (!StringUtils.isEmpty(cardbinType)) {
				if ("DEBIT".equalsIgnoreCase(cardbinType)) {
					return true;
				}
			}
		}		
		return false;
	}
	
	private boolean violatingPrivyFlowCondition(
			PayNowRequest payNowRequest) {
		if(payNowRequest.getBookingDetails().isPrivyFlow()) {
			String payop = payNowRequest.getPaymentOption();
			if("cc".equalsIgnoreCase(payop) || "dc".equalsIgnoreCase(payop) || "qb".equalsIgnoreCase(payop)) {
				String bin = payNowRequest.getCardBin();
				if(!privyBins.contains(bin))
					return true;	
			} else if ("nb".equalsIgnoreCase(payop)) {
				if(!"icicinet".equalsIgnoreCase(payNowRequest.getBankCode()))
					return true;	
			} else return true;
		}
		return false;
	}
	
	private boolean validateSkipOtpFlow(PayNowRequest payNowRequest) {
		List<String> crpProdList = PaymentUIUtil.getCrpProducts();
		if(YatraUtil.isNullOrEmpty(crpProdList)) {
			logger.info("crpproductlist is coming as null, hence returned false");
			return false;
		}
		String product = payNowRequest.getProductCode();
		String payop = payNowRequest.getPaymentOption();
		
		String cvvRequired = payNowRequest.getCVVRequired();
		if("false".equalsIgnoreCase(cvvRequired)) {
			if(!crpProdList.contains(product))
				return false;
			else if(payop.equalsIgnoreCase("cc")
					|| payop.equalsIgnoreCase("dc"))  {
				return paymentUIHelper.validateSkipOtp(payNowRequest.getSuperPnr(), payop, payNowRequest.getCardType(), payNowRequest.getCardBin(), payNowRequest.getProductCode(), payNowRequest.getBookingDetails().isPassThrough());
			}
			else if(payop.equalsIgnoreCase("qb"))			//in case of QB in Corporate tenants, we get cardType as cc, and cardBrand as VISA
				return paymentUIHelper.validateSkipOtp(payNowRequest.getSuperPnr(), payNowRequest.getPaymentOptionParamMap().get("cardType"), payNowRequest.getCardBrand(), payNowRequest.getCardBin(), payNowRequest.getProductCode(), payNowRequest.getBookingDetails().isPassThrough());
			else return true;
		}		
		return true;
	}
	private boolean validateCorpCardEligibilityForPayment(PayNowRequest payNowRequest){

		String payop = payNowRequest.getPaymentOption();
		Map<String,String> paramMap = payNowRequest.getParameterMap();
		if(StringUtils.equals(payop,PaymentUIUtil.PAYMENT_OPTION_CORPORATE_CARD) && StringUtils.equalsIgnoreCase(paramMap.get(PaymentUIUtil.CARD_TYPE_KEY_FOR_CORPORATE),PaymentUIUtil.CARD_TYPE_PCC)
				&& !StringUtils.equalsIgnoreCase(paramMap.get(PaymentUIUtil.IS_CORPORATE_CARD_TOKENIZED),PaymentUIUtil.BOOLEAN_TRUE)){
				return false;
		}
		return true;
	}

	private PayNowResponse prepareResponse(PayNowRequest payNowRequest,PayNowResponse payNowResponse) {
		//logger.info("switch case: "+payNowResponse.getResponseDecision()); //To be removed
		
		switch(payNowResponse.getResponseDecision()){
		
		case COMPLETE_WALLET_SUCCESS:
			payNowResponse.setSuccess(true);
			payNowResponse.setRurl(payNowResponse.getValidationResponse().getRurl());
			Map<String, String> resultMap = new HashMap<String, String>();
			resultMap.put("suc", "true");
			resultMap.put("amount", "0");
			resultMap.put("superPnr", payNowRequest.getSuperPnr());
			resultMap.put("ttid", payNowResponse.getValidationResponse().getTtid());
			resultMap.put("wallet_amount",payNowRequest.getWalletInfo().getAmountInRupees());
			resultMap.put("wallet_id", payNowRequest.getWalletInfo().getWalletId());
			resultMap.put("mtxnid", payNowRequest.getWalletInfo().getWalletId());
			payNowResponse.setPromoParameters();
			payNowResponse.putInPgData(resultMap);
			paymentUIHelper.makeS2SCallIfEligible(payNowRequest, payNowResponse, resultMap, true);
			payNowResponse.setMessage(null);
			break;
			
		case COMPLETE_VOUCHER_SUCCESS:
			payNowResponse.setSuccess(true);
			payNowResponse.setRurl(payNowResponse.getValidationResponse().getRurl());
			Map<String, String> voucherResultMap = new HashMap<String, String>();
			voucherResultMap.put("suc", "true");
			voucherResultMap.put("amount", "0");
			voucherResultMap.put("superPnr", payNowRequest.getSuperPnr());
			voucherResultMap.put("ttid", payNowResponse.getValidationResponse().getTtid());
			if(payNowRequest.getWalletInfo() != null) {
				voucherResultMap.put("wallet_amount",payNowRequest.getWalletInfo().getAmountInRupees());
				voucherResultMap.put("wallet_id", payNowRequest.getWalletInfo().getWalletId());
			}
			voucherResultMap.put("mtxnid", payNowRequest.getMtxnID());
			voucherResultMap.put(PaymentUIUtil.VOUCHER_AMOUNT, payNowRequest.getVoucherRedeemedAmount());
			if(payNowRequest.getBookingDetails().getIxigoMoney() != null)
				voucherResultMap.put("ixigoMoney", payNowRequest.getBookingDetails().getIxigoMoney());
			payNowResponse.putInPgData(voucherResultMap);
			payNowResponse.setPromoParameters();
			paymentUIHelper.makeS2SCallIfEligible(payNowRequest, payNowResponse, voucherResultMap, true);
			payNowResponse.setMessage(null);
			break;
			
		case COMPLETE_IXIGO_PAYMENT_SUCCESS:
			payNowResponse.setSuccess(true);
			payNowResponse.setRurl(payNowResponse.getValidationResponse().getRurl());
			Map<String, String> ixigoResultMap = new HashMap<String, String>();
			ixigoResultMap.put("suc", "true");
			ixigoResultMap.put("amount", "0");
			ixigoResultMap.put("superPnr", payNowRequest.getSuperPnr());
			ixigoResultMap.put("ttid", payNowResponse.getValidationResponse().getTtid());
			ixigoResultMap.put("ixigoMoney", payNowRequest.getBookingDetails().getIxigoMoney());
			if(payNowRequest.getWalletInfo() != null) {
				ixigoResultMap.put("wallet_amount",payNowRequest.getWalletInfo().getAmountInRupees());
				ixigoResultMap.put("wallet_id", payNowRequest.getWalletInfo().getWalletId());
			}
			ixigoResultMap.put("mtxnid", payNowRequest.getMtxnID());			
			payNowResponse.putInPgData(ixigoResultMap);
			payNowResponse.setPromoParameters();
			paymentUIHelper.makeS2SCallIfEligible(payNowRequest, payNowResponse, ixigoResultMap, true);
			payNowResponse.setMessage(null);
			break;
		
		case VOUCHER_VALIDATION_FAILURE:
			setGenericMesage(payNowResponse);
			break;
		/*case EWALLET_HANDSHAKE_FAILURE:
			setGenericMesage(payNowResponse);
			break;*/
			
		case SUCESS_REDIRECT_TO_PORTAL:
			//logger.info("Inside SUCESS_REDIRECT_TO_PORTAL case"); //To be removed
			String rurl = payNowRequest.getHost()+PaymentUIUtil.getPortalUrl(payNowRequest.getPaymentOption());
            if (("cc".equalsIgnoreCase(payNowRequest.getPaymentOption()) || "dc".equalsIgnoreCase(payNowRequest.getPaymentOption()))) {
            	if(!StringUtils.isEmpty(payNowRequest.getCardBinType())) 
            		rurl = payNowRequest.getHost()+getCardUrlFromBinType(payNowRequest);
            	else rurl = payNowRequest.getHost()+getCardUrlFromBinNumber(payNowRequest);
            }

            logger.info("inside : "+SUCESS_REDIRECT_TO_PORTAL);

            payNowResponse.setRurl(rurl); 
			payNowResponse.setSuccess(true);
			payNowResponse.putInPgData(rreHelper.preparePortalMap(payNowRequest,payNowResponse));
			//payNowResponse.setMessage(null);
			payNowResponse.setRedirect(false);
			payNowResponse.setMessage(payNowResponse.getValidationResponse().getErrorMessage());
			payNowResponse.setDiscountSuccess();
			payNowResponse.setPromoParameters();
			if("cc".equalsIgnoreCase(payNowRequest.getPaymentOption()) || "dc".equalsIgnoreCase(payNowRequest.getPaymentOption()) || "qb".equalsIgnoreCase(payNowRequest.getPaymentOption())) {
			    payNowResponse.setPollIntervalInSec("3");
			    payNowResponse.setPollTimeoutInSec("1200");
			    payNowResponse.setPollTransationUrl(PaymentUIUtil.getPollTransactionUrl());
			}
			// check if responseToBeRedirected to paymentportal is to be ajax
			if(isPaymentRedirectAjax(payNowRequest)){
				payNowResponse.setAjax(true);
				payNowResponse.setPollStatusUrl(payNowRequest.getHost()+PaymentUIUtil.getPollStatusUrl());
				payNowResponse.setFailRedirectMap(rreHelper.getFailRedirectData(payNowResponse));
				if(payNowRequest.getPaymentOption().equalsIgnoreCase(PaymentUIUtil.PAYMENT_OPTION_GOOGLE_TEZ) 
					|| payNowRequest.getPaymentOption().equalsIgnoreCase(PaymentUIUtil.PAYMENT_OPTION_GOOGLE_TEZ_PWA)) {
					payNowResponse.setGooglePay(true);
				}
				if(payNowRequest.getPaymentOption().equalsIgnoreCase(PaymentUIUtil.PAYMENT_OPTION_CRED))
					payNowResponse.setCred(true);
			}
			boolean saveCard =false;
			String saveCardConsent = payNowRequest.getPaymentOptionParamMap().get(PaymentUIUtil.SAVE_QB_CARD);
			if(!YatraUtil.isNullOrEmpty(saveCardConsent) && StringUtils.equalsIgnoreCase(saveCardConsent,"true")){
				saveCard = true;
			}
			// changes for redirecting to payment-tokenize
			if( ((("cc".equalsIgnoreCase(payNowRequest.getPaymentOption()) ||
					"dc".equalsIgnoreCase(payNowRequest.getPaymentOption())) && saveCard) ||
					"qb".equalsIgnoreCase(payNowRequest.getPaymentOption()) )
							&& !PaymentUIUtil.checkIfCorporateProduct(payNowRequest.getProductCode()) && !PaymentUIUtil.checkIfInternationalProduct(payNowRequest.getProductCode(),yatraPropertiesDAO)
			){
				payNowResponse.setRurl(payNowRequest.getHost()+"/tokenize"+PaymentUIUtil.getPortalUrl(payNowRequest.getPaymentOption()));
				payNowResponse.setPollTransationUrl("/tokenize"+PaymentUIUtil.getPollTransactionUrl());
			}
			break;
		
		case VALIDATION_FAILURE_WITH_REDIRECT:
			payNowResponse.setRedirect(true);
			payNowResponse.setRurl(payNowResponse.getValidationResponse().getRurl());
			Map<String, String> paramMap = payNowResponse.getValidationResponse().getErrorParamMap();
			payNowResponse.putInPgData(paramMap);
			payNowResponse.setSuccess(false);
			payNowResponse.setPromoParameters();
			break;

		case COMPLETE_GV_PAYMENT_FAILURE:
			payNowResponse.setRedirect(true);
			payNowResponse.setSuccess(false);
			payNowResponse.setMessage(GiftVoucherProcessor.FULL_GV_PAYMENT_FAILURE_MSG);
			payNowResponse.setRurl(payNowResponse.getValidationResponse().getRurl());
			Map<String, String> gvFailMap = new HashMap<String, String>();
			gvFailMap.put("suc", "false");
			gvFailMap.put("superPnr", payNowRequest.getSuperPnr());
			gvFailMap.put("ttid", payNowResponse.getValidationResponse().getTtid());
			gvFailMap.put("mtxnid", payNowRequest.getMtxnID());
			payNowResponse.setPromoParameters();
			payNowResponse.putInPgData(gvFailMap);
			break;
			
		case COMPLETE_IXIGO_PAYMENT_FAILURE:
			payNowResponse.setRedirect(true);
			payNowResponse.setSuccess(false);
			payNowResponse.setMessage(CompleteIxigoMoneyProcessor.FULL_IXIGO_MONEY_PAYMENT_FAILURE_MSG);
			payNowResponse.setRurl(payNowResponse.getValidationResponse().getFullIxigoFailureUrl());
			Map<String, String> ixigoFailMap = new HashMap<String, String>();
			ixigoFailMap.put("suc", "false");
			ixigoFailMap.put("superPnr", payNowRequest.getSuperPnr());
			//ixigoFailMap.put("ttid", payNowResponse.getValidationResponse().getTtid());
			ixigoFailMap.put("mtxnid", payNowRequest.getMtxnID());
			//ixigoFailMap.put("isFullIxigoFailure", "true");
			payNowResponse.setPromoParameters();
			payNowResponse.putInPgData(ixigoFailMap);
			break;
			
			
		case VALIDATION_FAILURE_WITHOUT_REDIRECT:
			payNowResponse.setRedirect(false);
			payNowResponse.setRurl(null);
			payNowResponse.setSuccess(false);
			payNowResponse.putInPgData(PaymentUIUtil.AMOUNT, payNowResponse.getValidationResponse().getAmountToCollect());
			payNowResponse.setMessage(payNowResponse.getValidationResponse().getErrorMessage());
			payNowResponse.setDiscountSuccess();
			payNowResponse.setPromoParameters();
			break;
			
		case COMPLETE_WALLET_FAILURE : 
			setGenericMesage(payNowResponse);
			payNowResponse.setRedirect(false);
			payNowResponse.setRurl(null);
			payNowResponse.setSuccess(false);
			payNowResponse.setPromoParameters();
			Map<String, String> resMap = new HashMap<String, String>();
			resMap.put("suc", "false");
			resMap.put("superPnr", payNowRequest.getSuperPnr());
			resMap.put("ttid", payNowResponse.getValidationResponse().getTtid());
			paymentUIHelper.makeS2SCallIfEligible(payNowRequest, payNowResponse, resMap, false);
			break;
		
		case BOOKINGDOTCOM_ERROR :
			payNowResponse.setMessage("Please enter a credit card to proceed.");
			payNowResponse.setRedirect(false);
			payNowResponse.setRurl(null);
			payNowResponse.setSuccess(false);
			payNowResponse.setFailureCode(FailureCode.BOOKINGDOTCOM_ERROR);
			break;
			
		case PRIVY_FLOW_ERROR :
			payNowResponse.setMessage("You can only pay using ICICI bank cards or bank account.");
			payNowResponse.setRedirect(false);
			payNowResponse.setRurl(null);
			payNowResponse.setSuccess(false);
			payNowResponse.setFailureCode(FailureCode.PRIVY_FLOW_ERROR);
			break;
			
		case USER_NOT_ELIGIBLE_FOR_REWARDS :
			payNowResponse.setMessage("Your number is not eligible for Rewards flow");
			payNowResponse.setRedirect(false);
			payNowResponse.setRurl(null);
			payNowResponse.setSuccess(false);
			payNowResponse.setFailureCode(FailureCode.USER_NOT_ELIGIBLE_FOR_REWARDS);
			break;

		case PASSTHROUGH_FLOW_SAVE_CARD_TEMPORARILY_FAILURE:
			payNowResponse.setMessage("Due to some technical issue, your payment didn't go through! Please enter your card details again.");
			payNowResponse.setRedirect(false);
			payNowResponse.setRurl(null);
			payNowResponse.setSuccess(false);
			payNowResponse.setFailureCode(FailureCode.PASSTHROUGH_SAVE_CARD_TEMPORARILY_FAILURE);
			break;
			
		case GENERIC_ERROR :
			setGenericMesage(payNowResponse);
			payNowResponse.setRedirect(false);
			payNowResponse.setRurl(null);
			payNowResponse.setSuccess(false);
			break;

		case CARD_NOT_ELIGIBLE_FOR_PAYMENT:
			payNowResponse.setMessage("This card can not be used for booking, please use a different card/payment option.");
			payNowResponse.setRedirect(false);
			payNowResponse.setRurl(null);
			payNowResponse.setSuccess(false);
			break;
			
		}
		// TODO Auto-generated method stub
		logger.debug("Sending following response back to paySwift UI for server LOB validation for TTID "+ payNowRequest.getTtid()
				+ "response"
				+ payNowResponse.toString());
		return payNowResponse;
	}

	private String getCardUrlFromBinNumber(PayNowRequest payNowRequest) {
        	String cardBin = payNowRequest.getCardBin();
            String cardbinType = paymentDAO.getCardBinType(cardBin);
            if (!StringUtils.isEmpty(cardbinType)) {
                try {
                    if ("DEBIT".equalsIgnoreCase(cardbinType)) {
                        payNowRequest.setPayOpBasedOnCardBinType("dc");
                        return "/checkout/pay/debitcard.htm";
                    }
                } catch (Exception ex) {
                    logger.error("Error while getting card url  from bin details " + ex);
                }
            }
            payNowRequest.setPayOpBasedOnCardBinType("cc");
            return "/checkout/pay/creditcard.htm";
        }

	private void setGenericMesage(PayNowResponse payNowResponse) {
		payNowResponse.setRurl(null);
		payNowResponse.setSuccess(false);
		payNowResponse.setRedirect(false);
		if(YatraUtil.isNullOrEmpty(payNowResponse.getMessage()))
			payNowResponse.setMessage("Oops, we seem to have temporary hiccup. We apologise for the inconvenience caused and request you to try again.");
	}

	private LobDetailResponse fetchBookingDetails(PayNowRequest payNowRequest) {
		String paymentDetailsFromDB = null;
	
		//making fresh getBookingDetails hit for non Desktop cases so as to encounter cases where price may change, leading to change in maxRedeemable Ecash value
		if(!payNowRequest.getClient().equals(ClientType.APP))
			paymentDetailsFromDB = paymentDAO.fetchParamForSuperPnrUnzipped(payNowRequest.getSuperPnr(), "FETCH_ITINERARY_RES");

		if(YatraUtil.isNullOrEmpty(paymentDetailsFromDB)) {
			logger.info("Fetching details by making call to GetBookingDetails API");
			return lobDetailsService.fetchItineraryDetailsFromLob(new LobDetailRequest(payNowRequest));
		}
		else {
			logger.info("Fetching details from DB");
			return new LobDetailResponse(paymentDetailsFromDB);
		}

	}

        private String getCardUrlFromBinType(PayNowRequest payNowRequest) {
            String cardbinType = payNowRequest.getCardBinType();
            if (!StringUtils.isEmpty(cardbinType)) {
                try {
                    if ("DEBIT".equalsIgnoreCase(cardbinType)) {
                        payNowRequest.setPayOpBasedOnCardBinType("dc");
                        return "/checkout/pay/debitcard.htm";
                    }
                } catch (Exception ex) {
                    logger.error("Error while getting card url  from bin details " + ex);
                }
            }
            payNowRequest.setPayOpBasedOnCardBinType("cc");
            return "/checkout/pay/creditcard.htm";
        }
        
	private boolean isPaymentRedirectAjax(PayNowRequest payNowRequest){
		// This can later be put in properties file
		if (payNowRequest.getPaymentOption().equalsIgnoreCase(PaymentUIUtil.PAYMENT_OPTION_UPI)
				|| payNowRequest.getPaymentOption().equalsIgnoreCase(PaymentUIUtil.PAYMENT_OPTION_GOOGLE_TEZ)
				|| payNowRequest.getPaymentOption().equalsIgnoreCase(PaymentUIUtil.PAYMENT_OPTION_GOOGLE_TEZ_PWA)
				|| payNowRequest.getPaymentOption().equalsIgnoreCase(PaymentUIUtil.PAYMENT_OPTION_CRED)) {
			return true;
		}
		return false;
	}

	private void setAgentCodeIfRequired(PayNowRequest payNowRequest, PayNowResponse payNowResponse) {
		if ("indifi".equalsIgnoreCase(payNowRequest.getPaymentOption()) && payNowResponse.getResponseDecision() == SUCESS_REDIRECT_TO_PORTAL) {
			B2BUserProfileDetailBean resBean = null;
			try {
				resBean = agentProfileService.getUserProfileDetail(payNowRequest.getSSOToken());
			} catch (PayswiftException pse) {
				setPayNowErrorResponse(payNowResponse, FailureCode.valueOf(pse.getErrorCode()), pse.getMessage());
				return;
			}

			String profileType = payNowRequest.getBookingDetails().getAgentProfileType();
			if (isEmpty(profileType)) {
				setPayNowErrorResponse(payNowResponse, B2B_USER_PROFILE_SERVICE_EXCEPTION, "AgentProfileType Empty from getBookingDetails");
				return;
			}

			Optional<String> erpCode = resBean.getErpCode(profileType);
			if (!erpCode.isPresent()) {
				setPayNowErrorResponse(payNowResponse, B2B_USER_PROFILE_SERVICE_EXCEPTION, "ERP Code found Empty");
				return;
			}
			payNowResponse.setAgentCode(erpCode.get());
		}
	}

	private void setPayNowErrorResponse(PayNowResponse payNowResponse, FailureCode failureCode, String customErrorMessage) {
		payNowResponse.setResponseDecision(ResponseDecisionScenarios.GENERIC_ERROR);
		payNowResponse.setFailureCode(failureCode.setCustomCode(customErrorMessage));
	}
	
	private void setMinkasuFlag(PayNowRequest payNowRequest,
			PayNowResponse payNowResponse) {
		payNowResponse.setMinkasuFlag(PaymentUIUtil.getMinkasuFlag(payNowRequest.getSuperPnr(), payNowRequest.getPaymentOption(), payNowRequest.getBankCode(), payNowRequest.getCardBin(), payNowRequest.getPaymentProcessType(), payNowRequest.getProductCode()));
	}
	
	private void setJusPayFlag(PayNowResponse payNowResponse) {
		String flag = "true";
		try {
			flag = paymentDAO.getJusPayFlag();
		} catch(Exception e) {
			logger.error("Error while trying to set jusPayFlag", e);
		}
		payNowResponse.setJusPayFlag(flag);
	}

	private boolean validateAndSetVPA(PayNowRequest payNowRequest){
		JSONObject respJson = PaymentUIUtil.validateUpiId(payNowRequest.getSuperPnr(), payNowRequest.getPayerVA(), payNowRequest.getMerchantCode(), payNowRequest.getProductCode());

        try {
            if(respJson!=null && respJson.has("success") && respJson.getBoolean("success"))
            {
                payNowRequest.setPayerVA(respJson.optString("vpa",""));
                return true;
            }
        } catch (JSONException e) {
            logger.error("Exception occured at setAndValidatePayerVA " ,e);
        }
		return false;
	}
}
