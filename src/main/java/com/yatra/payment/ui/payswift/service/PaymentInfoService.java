package com.yatra.payment.ui.payswift.service;

import static com.yatra.payment.ui.util.PaymentUIUtil.byPassUIChecksForWhitelistedEmail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.yatra.payment.ui.beans.PromoUiMessage;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import com.yatra.base.log.Logger;
import com.yatra.base.log.LoggerFactory;
import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.cache.service.impl.PaymentDivCache;
import com.yatra.payment.cache.service.impl.ProductServiceUrlCache;
import com.yatra.payment.gv.GiftVoucherService;
import com.yatra.payment.payswift.beans.LobDetailRequest;
import com.yatra.payment.payswift.beans.PaymentInfoBean;
import com.yatra.payment.ui.dao.CompletePaymentInfoDAO;
import com.yatra.payment.ui.dao.YatraPropertiesDAO;
import com.yatra.payment.ui.dao.impl.PayswiftStagesDAOImpl;
import com.yatra.payment.ui.display.beans.EMIBank;
import com.yatra.payment.ui.display.beans.EMIPaymentOption;
import com.yatra.payment.ui.display.beans.EmiType;
import com.yatra.payment.ui.display.beans.NoCostEmiBank;
import com.yatra.payment.ui.display.beans.NoCostEmiProduct;
import com.yatra.payment.ui.display.beans.PaymentDiv;
import com.yatra.payment.ui.display.beans.PaymentOption;
import com.yatra.payment.ui.display.beans.ProductServiceUrlBean;
import com.yatra.payment.ui.display.beans.QuickBookPaymentOption;
import com.yatra.payment.ui.display.beans.TwidPaymentOption;
import com.yatra.payment.ui.service.QuickBookUIService;
import com.yatra.payment.ui.service.impl.CurrencyConversionServiceImpl;
import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.payment.ui.util.QuickBookUIUtil;
import com.yatra.payment.ui.util.SMEUtil;
import com.yatra.platform.commons.property.PropertyManager;
import com.yatra.platform.http.HttpEndPoint;
import com.yatra.platform.http.HttpService;
import com.yatra.platform.util.YatraUtil;

@Service
public class PaymentInfoService {
	@Autowired private LobDetailsService lobDetailsService;
	@Autowired private PropertyManager property;
	@Autowired private HttpService httpService;
	@Autowired private QuickBookUIService quickBookUIService;
	@Autowired private PaymentDivCache paymentDivCache;
	@Autowired private UICacheManager<NoCostEmiProduct> noCostEmiBanksCache;
	@Autowired private ProductServiceUrlCache productServiceUrlCache; 
	@Autowired private CompletePaymentInfoDAO completePaymentInfoDAO;
	@Autowired private CurrencyConversionServiceImpl currencyConversionService;
	@Autowired ThreadPoolTaskExecutor threadPoolTaskExecutor;
	@Autowired private GiftVoucherService voucherService;
	@Autowired private PayswiftStagesDAOImpl payswiftStagesDAOImpl;
	@Autowired private YatraPropertiesDAO yatraPropertiesDAO;

	private Logger logger = LoggerFactory.getLogger();
	private final String statusNode = "status";
	private final String loggedInNode = "loggedInStatus";
	private final ObjectMapper objectMapper = new ObjectMapper();

	public String getPaymentInfo(PaymentInfoBean paymentInfoBean){
		if(paymentInfoBean.getLoggedInStatus()){
			fetchECashDetail(paymentInfoBean);
			fetchStoredCardDetail(paymentInfoBean);
			fetchCashPoolDetails(paymentInfoBean);
		}
		fetchBookingDetails(paymentInfoBean);
		fetchPaymentOpJson(paymentInfoBean);
		fetchCurrencyConversionJSON(paymentInfoBean);
		setStatus(paymentInfoBean);
		setByPassUIChecksForWhitelistedEmail(paymentInfoBean);
		setGVProduct(paymentInfoBean);
		return paymentInfoBean.getResponseObj().toString();
	}

	private void setGVProduct(PaymentInfoBean paymentInfoBean) {
		String product = paymentInfoBean.getProductCode();
		try{
			paymentInfoBean.getResponseObj().put("gvProduct", voucherService.getGVProduct(product));
		}
		catch(Exception e) {
			logger.error("Error setting gvProduct for superPnr : " + paymentInfoBean.getSuperPnr() + " and product : " + product , e.getMessage());
		}
	}

	private boolean areBookingDetailsRequired(String productCode) {
		ProductServiceUrlBean productServiceUrlBean	= productServiceUrlCache.get(productCode);
		String returnEcash = productServiceUrlBean.getBookingDetailsRequired();
		if(returnEcash.equalsIgnoreCase("true"))
			return true;
		else return false;
	}

	public void fetchBookingDetails(PaymentInfoBean paymentInfoBean) {
		try{
			LobDetailRequest lobDetailRequest = new LobDetailRequest(paymentInfoBean);
			String response = "";
			String maxLOBEcash ="0.0";
			try{
				response = lobDetailsService.fetchItineraryDetailsFromLob(lobDetailRequest).getRawResponse(); 
			}catch(Exception e){
				logger.error("Excpetion in lobDetailsService.fetchItineraryDetailsFromLob due to {} ", e);
			}
			logger.debug("fetchBookingDetails response received for superPnr {} is {}",paymentInfoBean.getSuperPnr(), response);
			if(response.isEmpty()){
				logger.error("Empty response received from the LOB");
				return;
			}
			JSONObject resObj = new JSONObject(response);
			if("true".equalsIgnoreCase(paymentInfoBean.getIsBookingDetailsRequired()))
				paymentInfoBean.getResponseObj().put("bookingDetailsJSON",resObj);
			Double totalAmt = resObj.getDouble("totalAmount");
			if(paymentInfoBean.getLoggedInStatus()){
				if(areBookingDetailsRequired(paymentInfoBean.getProductCode())) {
					logger.info("Returning booking details for this tenant : " + paymentInfoBean.getProductCode());
					if(resObj.has("maxRedeemableECash")){
						maxLOBEcash = (resObj.getString("maxRedeemableECash"));
					}
					else if(resObj.has("walletDetails")){
						JSONObject walletDetails = new JSONObject(resObj.getString("walletDetails"));
						maxLOBEcash = (walletDetails.getString("maxRedeemableECash"));
					}
					calculateMaxRedeemableEcash(totalAmt,maxLOBEcash, paymentInfoBean);
				}
			}
			String promoCode = getPromoCode(resObj);
			paymentInfoBean.setPromoCode(promoCode);
			paymentInfoBean.setMobile(getUserMobile(resObj));
			paymentInfoBean.setTotalAmount(resObj.getString("totalAmount"));
			if(StringUtils.isNotBlank(resObj.getString(PaymentUIUtil.PRODUCT_CODE))) {
				paymentInfoBean.setProductCode(resObj.getString(PaymentUIUtil.PRODUCT_CODE));
			}
		}catch(Exception e){
			logger.error("Caught an Exception at fetchBookingDetails on superPnr {} due to {}", paymentInfoBean.getSuperPnr(),e);
		}
	}
	public String getPromoCode(JSONObject lobDetailsJSON) throws JSONException {
		
		if (lobDetailsJSON.has(PaymentUIUtil.PROMO_CODE_NODE)) {
			JSONObject promoJson = lobDetailsJSON.getJSONObject(PaymentUIUtil.PROMO_CODE_NODE);
			if (promoJson != null) {
				return promoJson.optString(PaymentUIUtil.CODE);
			}
		}
		return null;
	}
	public String getUserMobile(JSONObject lobDetailsJSON) throws JSONException {
		if (!lobDetailsJSON.has(PaymentUIUtil.USER_DETAILS)) {
			return null;
		}
		return lobDetailsJSON.getJSONObject(PaymentUIUtil.USER_DETAILS).optString(PaymentUIUtil.MOBILE);
	}
	
	public void calculateMaxRedeemableEcash(Double bookingAmt, String maxLOBEcash, PaymentInfoBean paymentInfoBean) {
		try{
			Double finalRedeemableEcash=0.0;
			Double maxEcash = Double.parseDouble(maxLOBEcash);
			JSONObject eCashObj = paymentInfoBean.getResponseObj().getJSONObject("eCashJSON");
			Integer ylp=0, um=0;
			JSONArray eCashList = eCashObj.getJSONArray("ECashList");
			for(int i=0;i<eCashList.length();i++){
				JSONObject ecashDetail = eCashList.getJSONObject(i);
				if(ecashDetail.getString("eCashType").equalsIgnoreCase("EXPIRABLE")){
					ylp =  ((Double)(ecashDetail.getInt("redeemableECashInPaisa") * 0.01)).intValue() ;
				}
				if(ecashDetail.getString("eCashType").equalsIgnoreCase("NON_EXPIRABLE")){
					um = ((Double)(ecashDetail.getInt("redeemableECashInPaisa") * 0.01)).intValue();
				}
			}
			if(ylp>maxEcash){
				finalRedeemableEcash= Math.min(maxEcash+um,bookingAmt);
			}else{
				finalRedeemableEcash = Math.min(ylp+um,bookingAmt);
			}
			paymentInfoBean.getResponseObj().put("maxRedeemableEcash", finalRedeemableEcash);
		}catch(Exception e){
			logger.error("Caught an Exception at calculateMaxRedeemableEcash on superPnr {} due to {}", paymentInfoBean.getSuperPnr(),e);
		}
	}

        private List<String> getMobileProductList() {
		List<String> mobileProductList = new ArrayList<String>();
			String mobileProducts = property.getProperty("yt.mobile.products");
			if (StringUtils.isNotEmpty(mobileProducts))
				mobileProductList = new ArrayList<String>(Arrays.asList(mobileProducts.split(",")));
			else
				mobileProductList = new ArrayList<String>();
		return mobileProductList;
	}
        
	public void fetchPaymentOpJson(PaymentInfoBean paymentInfoBean) {
		try{
			String key = PaymentUIUtil.getPaymentDivKey(paymentInfoBean.getMerchantCode(), paymentInfoBean.getProductCode());
			String currentVersion = property.getProperty("app.json.version");
			//Instead of HTTP call directly call inner method .
			if(StringUtils.isNotBlank(paymentInfoBean.getVersionNo()) && paymentInfoBean.getVersionNo().equalsIgnoreCase(currentVersion)){
				paymentInfoBean.getResponseObj().put("paymentOptionJSON",new JSONObject());
				paymentInfoBean.setStatus(false);
				return;
			}
			PaymentDiv paymentDiv = paymentDivCache.get(key);

			PaymentDiv noCostEMIDiv = overridePaymentDiv(paymentInfoBean,paymentDiv);
			reorderPaymentDivForSME(paymentDiv, paymentInfoBean.getProductCode());
			// OverRiding payment Div if No cost emi option is available 
			boolean flag = false;
			if(noCostEMIDiv != null) {
				paymentDiv = noCostEMIDiv; 
				flag = true;
			}
			/*PaymentDiv updatedTwidDiv = updateBalanceInTwidPayop(paymentInfoBean, paymentDiv);
			if(updatedTwidDiv != null) {
				paymentDiv = updatedTwidDiv; 
			}*/
			/*PaymentDiv updatedCredDiv = restrictCredPayop(paymentInfoBean, paymentDiv);
			if(updatedCredDiv != null) {
				paymentDiv = updatedCredDiv; 
			}*/

			paymentDiv.setVersion(currentVersion);
			ObjectWriter ow = new ObjectMapper().viewWriter(PaymentDiv.class);
			String paymentOptionJson = ow.writeValueAsString(paymentDiv);
			logger.debug("Payment Option JSON Response for prroduct {} is {} ",paymentInfoBean.getProductCode(), paymentOptionJson);
			JSONObject resObj = new JSONObject(paymentOptionJson);

			resObj.put("isNoCostEmiFlag",flag);
			String productCode = paymentInfoBean.getProductCode();
			List<String> mobileProductList = getMobileProductList();

			// Code for updating messages for payment options
			String promoCode = paymentInfoBean.getPromoCode();
			setPromoUiMessages(promoCode,resObj);

			if("pwadomfsv_flight".equalsIgnoreCase(paymentInfoBean.getProductCode())
				|| "pwaintfsv_flight".equalsIgnoreCase(paymentInfoBean.getProductCode())
				|| "BajajFinservDom".equalsIgnoreCase(paymentInfoBean.getProductCode())
				|| "BajajFinservInt".equalsIgnoreCase(paymentInfoBean.getProductCode())) {
				filterOutNonBajajEMI(resObj);
			} else {
				if("true".equalsIgnoreCase(yatraPropertiesDAO.getYatraPropValue("bajaj.emi.restricted"))
						&& !(PaymentUIUtil.getDomesticFlightProductList(yatraPropertiesDAO).contains(paymentInfoBean.getProductCode())
							|| PaymentUIUtil.getDomesticHotelProductList(yatraPropertiesDAO).contains(paymentInfoBean.getProductCode())))
					filterOutBajajEMI(resObj);
				if(paymentInfoBean.getProductCode().contains("ios"))
					filterOutDebitEMIForApp(resObj);	
			}
			resObj.put("tezMerchantInfo", getGoogleTezMerchantInfo(productCode, paymentInfoBean.getSuperPnr()));
			paymentInfoBean.getResponseObj().put("paymentOptionJSON",resObj);
			paymentInfoBean.setStatus(true);
		}catch(Exception e){
			logger.error("Caught an Exception at fetchPaymentOpJson on superPnr {} due to {}", paymentInfoBean.getSuperPnr(),e);
			paymentInfoBean.setStatus(false);
		}
	}

	private List<PromoUiMessage> getPromoUiMessages(){
		String promoPayOpMessages = YatraUtil.getPropertyManager().getProperty("promo.payment.option.message");
		List<PromoUiMessage> promoUiMessageList = new ArrayList<PromoUiMessage>();
		String[] promoMessages = promoPayOpMessages.split(",");
		for(String promoMessage: promoMessages){
			String[] promoBeanArray = promoMessage.split("\\|");
			PromoUiMessage promoUiMessage = new PromoUiMessage(promoBeanArray[0],promoBeanArray[1],promoBeanArray[2]);
			promoUiMessageList.add(promoUiMessage);
 		}
		return promoUiMessageList;
	}

	private void setPromoUiMessages(String promoCode, JSONObject paymentOptionJson){
		List<PromoUiMessage> promoUiMessageList = getPromoUiMessages();
		logger.info("Updating Promo message in paymentOptionJson");
		if(paymentOptionJson!=null && promoCode!=null) {
			try {
				if(paymentOptionJson.has("paymentOptions")
						&& paymentOptionJson.getJSONArray("paymentOptions")!=null
						&& paymentOptionJson.getJSONArray("paymentOptions").length()>0) {
					JSONArray paymentOptionList = paymentOptionJson.getJSONArray("paymentOptions");
					for(int i = 0; i<paymentOptionList.length(); i++) {
						String payOp = paymentOptionList.getJSONObject(i).getString("code");
						for(PromoUiMessage promoUiMessage: promoUiMessageList){
							if(promoUiMessage.getPayOp().equalsIgnoreCase(payOp) && promoUiMessage.getPromoCode().equalsIgnoreCase(promoCode)){
								paymentOptionList.getJSONObject(i).put("paymentOptionMessage",promoUiMessage.getMessage());
							}
						}
					}
					paymentOptionJson.put("paymentOptions", paymentOptionList);
				}
				logger.info("Updated Promo messages");
			} catch (JSONException e) {
				logger.error("Exception while updating promo messages in paymentJSON", e);
			}
		}

	}

	private PaymentDiv updateBalanceInTwidPayop(
			PaymentInfoBean paymentInfoBean, PaymentDiv paymentDiv) {
		List<PaymentOption> updatedPayOps = new ArrayList<>();
		//return paymentDiv;
		String balance = null;
		if(YatraUtil.isNullOrEmpty(paymentInfoBean.getSsoToken()))
			return paymentDiv;
		else {
			try {
				String response = PaymentUIUtil.makeCheckTwidEligibilityCall(paymentInfoBean.getMobile(), paymentInfoBean.getTotalAmount(), paymentInfoBean.getSuperPnr(), paymentInfoBean.getProductCode());
				balance = new org.json.JSONObject(response).optString("pointRedeemable");
				if(YatraUtil.isNullOrEmpty(balance))
					return paymentDiv;
			} catch(Exception e) {
				logger.error("Could not get balance, hence returning same div");
				return paymentDiv;
			}
		}
		
		PaymentDiv updatedDiv = new PaymentDiv();
        
		updatedDiv.setMerchantCode(paymentDiv.getMerchantCode());
		updatedDiv.setProductCode(paymentDiv.getProductCode());
		
		for(PaymentOption p : paymentDiv.getPaymentOptions()) {
			if("twid".equalsIgnoreCase(p.getCode())) {
				logger.info("twid old text : " + ((TwidPaymentOption) p).getSubText());
				TwidPaymentOption twidPayOpNew = new TwidPaymentOption((TwidPaymentOption) p);
				twidPayOpNew.setSubText(payswiftStagesDAOImpl.getYatraPropertyValueFromDB("payop.twid.sub.text"));
				logger.info("twid new text : " + twidPayOpNew.getSubText());
				twidPayOpNew.setBalance(balance);
				updatedPayOps.add(twidPayOpNew);
			} else {
				updatedPayOps.add(p);
			}
		}
		updatedDiv.setPaymentOptions(updatedPayOps);
		return updatedDiv;
	}
	
	private PaymentDiv restrictCredPayop(
			PaymentInfoBean paymentInfoBean, PaymentDiv paymentDiv) {
		List<PaymentOption> updatedPayOps = new ArrayList<>();
		if("yatratestbookings@gmail.com".equalsIgnoreCase(paymentInfoBean.getUserEmail())) {
			return paymentDiv;
		} else {
			PaymentDiv updatedDiv = new PaymentDiv();
			updatedDiv.setMerchantCode(paymentDiv.getMerchantCode());
			updatedDiv.setProductCode(paymentDiv.getProductCode());
			
			for(PaymentOption p : paymentDiv.getPaymentOptions()) {
				if(!"cred".equalsIgnoreCase(p.getCode())) {
					updatedPayOps.add(p);
				}
			}
			updatedDiv.setPaymentOptions(updatedPayOps);
			return updatedDiv;
		}
	}

	private PaymentDiv overridePaymentDiv(PaymentInfoBean paymentInfoBean, PaymentDiv paymentDiv) {

		try {
			if (noCostEmiBanksCache.get(paymentInfoBean.getProductCode()) == null) {
				return null;
			}
			NoCostEmiProduct noCostProduct = noCostEmiBanksCache.get(paymentInfoBean.getProductCode());
			List<NoCostEmiBank> banks = noCostProduct.getEmiBanks();
			if (banks == null || banks.isEmpty()) {
				return null;
			}

			boolean flag = false;
			if (!noCostProduct.getIsPromoAllowed()) {
				String promo = paymentInfoBean.getPromoCode();
				if(paymentInfoBean.getProductCode().equalsIgnoreCase("mdomandroid")
						|| paymentInfoBean.getProductCode().equalsIgnoreCase("mdomhotelandroid")
						|| paymentInfoBean.getProductCode().equalsIgnoreCase("mdomhotelios")
						|| paymentInfoBean.getProductCode().equalsIgnoreCase("mdomios")
						|| paymentInfoBean.getProductCode().equalsIgnoreCase("mintandroid")
						|| paymentInfoBean.getProductCode().equalsIgnoreCase("minthotelandroid")
						|| paymentInfoBean.getProductCode().equalsIgnoreCase("minthotelios")
						|| paymentInfoBean.getProductCode().equalsIgnoreCase("mintios")) {
					String isPromoApplied = paymentInfoBean.getIsPromoApplied();
					if(StringUtils.isEmpty(isPromoApplied))
						return null;
					else if("true".equalsIgnoreCase(isPromoApplied))
						promo = isPromoApplied;

				}
				if (!StringUtils.isEmpty(promo))
					return null;
			}
			PaymentOption emiPayOp = null;
			List<PaymentOption> updatedPayOps = new ArrayList<>();

			for(PaymentOption p : paymentDiv.getPaymentOptions()) {
				if("emi".equalsIgnoreCase(p.getCode())) {
					EMIPaymentOption emiPayOpNew = new EMIPaymentOption((EMIPaymentOption) p);
					emiPayOp = emiPayOpNew;
					updatedPayOps.add(emiPayOpNew);
				} else {
					updatedPayOps.add(p);
				}
			}
			if (emiPayOp == null) {
				return null;
			}
			PaymentDiv updatedDiv = new PaymentDiv();
                       
			updatedDiv.setMerchantCode(paymentDiv.getMerchantCode());
			updatedDiv.setProductCode(paymentDiv.getProductCode());
			updatedDiv.setPaymentOptions(updatedPayOps);
                        emiPayOp.setDisplayText(PaymentUIUtil.NO_COST_DISPLAY_TEXT);
			List<EMIBank> emiBanks = ((EMIPaymentOption)emiPayOp).getBanks();
			List<EMIBank> newEmiBanks = new ArrayList<>();
			for (EMIBank emiBnk : emiBanks) {
				EMIBank eb = new EMIBank(emiBnk);
				newEmiBanks.add(eb);
				copyList(eb);
			}
			((EMIPaymentOption)emiPayOp).setBanks(newEmiBanks);
			for (NoCostEmiBank noCostEmiBank: banks) {
				for (EMIBank emiBank : newEmiBanks) {
					if (emiBank.getCode().equalsIgnoreCase(noCostEmiBank.getBank())) {
						String disptext = emiBank.getDisplayText() + " - No Cost EMI";
						emiBank.setDisplayText(disptext);
						List<EmiType> emiTypes = emiBank.getEmiTypes();
						List<EmiType> noCostEmiTypes = noCostEmiBank.getEmiTypes();
						for (EmiType noCostEmiType : noCostEmiTypes) {
							for (EmiType emiType : emiTypes) {
								if (noCostEmiType.getEmiTenure().equalsIgnoreCase(emiType.getEmiTenure())) {
									flag = true;
									emiType.setInterestRate(noCostEmiType.getInterestRate());
								}
							}

						}
					}
				}
			}
			return updatedDiv;

		} catch (Exception ex) {
			logger.error("Error while overriding no cost emi values " + paymentInfoBean.getSuperPnr() + " error: " + ex.getMessage());
			return null;
		}

	}
	private void copyList(EMIBank eb) {
		List<EmiType> newEmiTypes = new ArrayList<>();
		for (EmiType emiType : eb.getEmiTypes()) {
			EmiType newEmiType = new EmiType(emiType.getEmiTenure(), emiType.getInterestRate(), emiType.getMinAmount(), emiType.getMaxAmount());
			newEmiTypes.add(newEmiType);
		}
		eb.setEmiTypes(newEmiTypes);
	}
	
	private void filterOutDebitEMIForApp(JSONObject paymentOptionJson) {
		logger.info("Filtering out Bajaj EMI ");
		if(paymentOptionJson!=null) {
			try {
				if(paymentOptionJson.has("paymentOptions") 
						&& paymentOptionJson.getJSONArray("paymentOptions")!=null
						&& paymentOptionJson.getJSONArray("paymentOptions").length()>0) {
					JSONArray paymentOptionList = paymentOptionJson.getJSONArray("paymentOptions");
					for(int i = 0; i<paymentOptionList.length(); i++) {
						if(paymentOptionList.getJSONObject(i).getString("code").equalsIgnoreCase("emi")) {
							JSONObject emiJson = paymentOptionList.getJSONObject(i);
							if(emiJson.has("banks") && emiJson.getJSONArray("banks")!=null 
									&& emiJson.getJSONArray("banks").length()>0) {
								JSONArray bankList = paymentOptionList.getJSONObject(i).getJSONArray("banks");
								ArrayList<JSONObject> finalBankList = new ArrayList<JSONObject>();
								for(int j=0; j<bankList.length(); j++) {
									if(!bankList.getJSONObject(j).getString("code").equalsIgnoreCase("dhdfc")
											&& !bankList.getJSONObject(j).getString("code").equalsIgnoreCase("dkotak")
											&& !bankList.getJSONObject(j).getString("code").equalsIgnoreCase("dfdrl")
											&& !bankList.getJSONObject(j).getString("code").equalsIgnoreCase("dbob")) {
										finalBankList.add(bankList.getJSONObject(j));
									}
								}
								paymentOptionList.getJSONObject(i).put("banks", finalBankList);
							}
							break;
						}
					}

					paymentOptionJson.put("paymentOptions", paymentOptionList);
				}
				logger.info("Filtered out Bajaj EMI");
			} catch (JSONException e) {
				logger.error("Exception while removing Bajaj EMI from paymentjson", e);
			}
		}

	}

	private void filterOutBajajEMI(JSONObject paymentOptionJson) {
		logger.info("Filtering out Bajaj EMI ");
		if(paymentOptionJson!=null) {
			try {
				if(paymentOptionJson.has("paymentOptions") 
						&& paymentOptionJson.getJSONArray("paymentOptions")!=null
						&& paymentOptionJson.getJSONArray("paymentOptions").length()>0) {
					JSONArray paymentOptionList = paymentOptionJson.getJSONArray("paymentOptions");
					for(int i = 0; i<paymentOptionList.length(); i++) {
						if(paymentOptionList.getJSONObject(i).getString("code").equalsIgnoreCase("emi")) {
							JSONObject emiJson = paymentOptionList.getJSONObject(i);
							if(emiJson.has("banks") && emiJson.getJSONArray("banks")!=null 
									&& emiJson.getJSONArray("banks").length()>0) {
								JSONArray bankList = paymentOptionList.getJSONObject(i).getJSONArray("banks");
								ArrayList<JSONObject> finalBankList = new ArrayList<JSONObject>();
								for(int j=0; j<bankList.length(); j++) {
									if(!bankList.getJSONObject(j).getString("code").equalsIgnoreCase("bajaj")) {
										finalBankList.add(bankList.getJSONObject(j));
									}
								}
								paymentOptionList.getJSONObject(i).put("banks", finalBankList);
							}
							break;
						}
					}

					paymentOptionJson.put("paymentOptions", paymentOptionList);
				}
				logger.info("Filtered out Bajaj EMI");
			} catch (JSONException e) {
				logger.error("Exception while removing Bajaj EMI from paymentjson", e);
			}
		}

	}
	
	private void filterOutNonBajajEMI(JSONObject paymentOptionJson) {
		logger.info("Filtering out non Bajaj EMI ");
		if(paymentOptionJson!=null) {
			try {
				if(paymentOptionJson.has("paymentOptions") 
						&& paymentOptionJson.getJSONArray("paymentOptions")!=null
						&& paymentOptionJson.getJSONArray("paymentOptions").length()>0) {
					JSONArray paymentOptionList = paymentOptionJson.getJSONArray("paymentOptions");
					for(int i = 0; i<paymentOptionList.length(); i++) {
						if(paymentOptionList.getJSONObject(i).getString("code").equalsIgnoreCase("emi")) {
							JSONObject emiJson = paymentOptionList.getJSONObject(i);
							if(emiJson.has("banks") && emiJson.getJSONArray("banks")!=null 
									&& emiJson.getJSONArray("banks").length()>0) {
								JSONArray bankList = paymentOptionList.getJSONObject(i).getJSONArray("banks");
								ArrayList<JSONObject> finalBankList = new ArrayList<JSONObject>();
								for(int j=0; j<bankList.length(); j++) {
									if(bankList.getJSONObject(j).getString("code").equalsIgnoreCase("bajaj")) {
										finalBankList.add(bankList.getJSONObject(j));
									}
								}
								paymentOptionList.getJSONObject(i).put("banks", finalBankList);
							}
							break;
						}
					}

					paymentOptionJson.put("paymentOptions", paymentOptionList);
				}
				logger.info("Filtered out non Bajaj EMI");
			} catch (JSONException e) {
				logger.error("Exception while removing non Bajaj EMI from paymentjson", e);
			}
		}

	}

	private JSONObject getGoogleTezMerchantInfo(String productCode, String superPnr) {
    	String bankId = "DEFAULT";
    	String paymentGateway;
    	String mcc =  property.getProperty("google.tez.merchant.category.code");
    	String pa = property.getProperty("google.tez.merchant.vpa");
		if(productCode.toLowerCase().contains("pwa"))
			bankId = PaymentUIUtil.GOOGLE_TEZ_BANK_CODE;
		else bankId = PaymentUIUtil.GOOGLE_ANDROID_BANK_CODE;
		
		Map<String, String> bankMap = null;
		bankMap = payswiftStagesDAOImpl.getGatewayCodeForBankId(bankId);
		if(bankMap != null)
		{
			paymentGateway = selectGoogleTezGateway(bankMap, superPnr);
			if(PaymentUIUtil.PG_GOOGLE_TEZ_RAZOR.equalsIgnoreCase(paymentGateway)
					|| PaymentUIUtil.PG_GOOGLE_TEZ_ANDROID_RAZOR.equalsIgnoreCase(paymentGateway)) {
				mcc = "5411";
				pa = "yatra1@hdfcbank";
			}
		}
		
        JSONObject merchantInfo = new JSONObject();
        try {
            merchantInfo.put("currencyCode", property.getProperty("google.tez.currency.code"));
            merchantInfo.put("merchantName", property.getProperty("google.tez.merchant.name"));
            merchantInfo.put("mcc", mcc);
            merchantInfo.put("pa", pa);
            return merchantInfo;
        } catch (JSONException ex) {
            logger.error("google tez merchantInfo cannot be extracted " + ex.getMessage());
        }
        return new JSONObject();
    }
	
	private String selectGoogleTezGateway(Map<String, String> bankMap, String superPnr) {
		if(StringUtils.isNumeric(superPnr.substring(superPnr.length()-1, superPnr.length()))) {
			int lastDigitOfSuperPnr = Integer.parseInt(superPnr.substring(superPnr.length()-1, superPnr.length()));
			int primaryShare = Integer.parseInt(bankMap.get("primary_share"));
			if (lastDigitOfSuperPnr <= (primaryShare/10)-1){
				return bankMap.get("code");
			}else{
				return bankMap.get("code2");
			}
		} else {
			return bankMap.get("code");
		}
		
	}
	
	
	private void fetchCurrencyConversionJSON(PaymentInfoBean paymentInfoBean) {
		try {
			JSONObject jsonRequest = currencyConversionService.getCurrencyConversionAPIRequest(paymentInfoBean.getProductCode(), paymentInfoBean.getMerchantCode(), paymentInfoBean.getSuperPnr());
			paymentInfoBean.getResponseObj().put(currencyConversionService.getResultKey(), currencyConversionService.getRequiredData(jsonRequest, null));
		} catch (Exception ex) {
			logger.error("Caught an Exception at fetchCurrencyConversionJSON on superPnr {} due to {}", paymentInfoBean.getSuperPnr(), ex);
		}
	}

	public void fetchStoredCardDetail(PaymentInfoBean paymentInfoBean) {
		// https://secure.yatra.com/PaySwift/qb.htm?product=bus&merchant=yatra&ssoToken=dbc89d2e-c5aa-4038-a86a-be340c544338
		String ssoToken = paymentInfoBean.getSsoToken();
		QuickBookPaymentOption quickBookPaymentOption;
		try{
			quickBookPaymentOption = quickBookUIService.getQuickBookBean(paymentInfoBean.getMerchantCode(), paymentInfoBean.getProductCode(), ssoToken, 0L, null);
		} catch (Exception e) {
			logger.error("Exception occurred while getting QuickBook Bean : ", e);
			quickBookPaymentOption = quickBookUIService.getQBErrorBean(QuickBookUIUtil.ERROR_CODE_EXCEPTION, e.getMessage());
		}
		try {
			String qbJson = objectMapper.writeValueAsString(quickBookPaymentOption);
			logger.debug("QB response for superPnr {} is {}",paymentInfoBean.getSuperPnr(),qbJson);
			JSONObject resObj = new JSONObject(qbJson);
			paymentInfoBean.getResponseObj().put("storedCardJSON",resObj);
		} catch (Exception e) {
			logger.error("Caught an Exception at fetchStoredCardDetail on superPnr {} due to {}", paymentInfoBean.getSuperPnr(), e);
		}
	}

	public void fetchCashPoolDetails(PaymentInfoBean paymentInfoBean) {
		String ssoToken = paymentInfoBean.getSsoToken();
		Map<String,String> userInfoMap = SMEUtil.getSSOTokenDetails(ssoToken);
		JSONObject resObj = new JSONObject();
		String userId = userInfoMap.get(PaymentUIUtil.USER_ID);
		String corpId = userInfoMap.get(PaymentUIUtil.CORP_ID);
		//need to give host entry on properties file
		String url = YatraUtil.getPropertyManager().getProperty("cash.pool.get.balance");

		try {

			JSONObject requestMap = new JSONObject();
			requestMap.put(PaymentUIUtil.USER_ID, userId);
			requestMap.put(PaymentUIUtil.CORP_ID, corpId);
			requestMap.put(PaymentUIUtil.SSO_TOKEN, ssoToken);

			logger.info("Fetching balance for user id "+ userId + " and corp Id "+corpId +" with url "+ url);
			String response = SMEUtil.sendSimplePostRequest(requestMap.toString(), url);
			logger.info("Response get from cash pool application for user id and corp Id "+userId+" "+corpId+" is "+response);
			if(StringUtils.isEmpty(response)) {
				logger.info("Getting response from Cash Pool Application is Empty for user id "+userId +" corp id "+corpId);
				resObj = prepareFailureCashPoolResponse(); 
				logger.info("Adding cashPoolJson as failure object json");
			} else {
				JSONObject responseJson = new JSONObject(response);
				String status = (String) responseJson.get(PaymentUIUtil.STATUS);
				if(!"success".equalsIgnoreCase(status)) {
					logger.info("Getting failure from cashPool service");
					resObj = prepareFailureCashPoolResponse(); 
					logger.info("Adding cashPoolJson as failure object json");
				} else {
					//Updating cash pool payment option
					Double userBalance = responseJson.getDouble(PaymentUIUtil.BALANCE);
					String userBalanceString  = Double.toString(userBalance);
					resObj.put("status", "success");
					resObj.put("userBalance", userBalanceString);	
				}
			}
			paymentInfoBean.getResponseObj().put("cashPoolJSON",resObj);
			logger.info("Exit from cash pool service for updating payment div");

		}
		catch (Exception e) {
			logger.error("Caught an Exception at fetchCashPoolDetails on superPnr {} due to {}", paymentInfoBean.getSuperPnr(), e);			
		}
	}
	
	
	private JSONObject prepareFailureCashPoolResponse() {
		JSONObject respJson = new JSONObject();
		try {
			respJson.put("status", "failure");
			respJson.put("userBalance", "0.00");
		} catch(Exception e) {
			logger.error("Error creating failure cashPoolResponse Object", e.getMessage());
		}
		return respJson;
	}

	public void fetchECashDetail(PaymentInfoBean paymentInfoBean) {
		// https://secure.yatra.com/checkout/wallet/get-ecash.htm?cust_email=yatratestbookings%40gmail.com&merchant_code=yatra&product_code=bus 
		String ssoToken = paymentInfoBean.getSsoToken();
		String userEmail="";

		try{
			String url = "http://service1.yatra.com/single-signon-service/services/user-login-service/user/session/loginInfo";
			JSONObject userLoginInfoRequest = new JSONObject();
			userLoginInfoRequest.put("ssoToken", ssoToken);
			String userLoginInfoResponse = PaymentUIUtil.sendSSOJsonPostRequest(userLoginInfoRequest.toString(), url, "1011");
			JSONObject responseJsonObject = new JSONObject(userLoginInfoResponse);
			userEmail = responseJsonObject.optString("emailId");
			
			Map<String,String> parameterMap = new HashMap<String,String>();
			parameterMap.put("ssoToken",ssoToken);
			parameterMap.put("super_pnr", paymentInfoBean.getSuperPnr());
			parameterMap.put("cust_email",userEmail);
			parameterMap.put("merchant_code", paymentInfoBean.getMerchantCode());
			parameterMap.put("product_code", paymentInfoBean.getProductCode());
			String getEcashURL = property.getProperty("secure.get.ecash.url");
			logger.debug("fetchECashDetail Request with customerEmail {} , product_code {}, merchantCode {}",userEmail,paymentInfoBean.getProductCode(),paymentInfoBean.getMerchantCode() );
			HttpEndPoint endPoint = new HttpEndPoint(getEcashURL, "POST");
			String response = httpService.invoke(endPoint, parameterMap);
			logger.debug("Response for fetchECashDetail is {}", response);
			JSONObject resObj = new JSONObject(response);
			paymentInfoBean.getResponseObj().put("eCashJSON",resObj);
		}catch(Exception e){
			logger.error("Caught an Exception at fetchECashDetail on superPnr {} due to {}", paymentInfoBean.getSuperPnr(), e);
		}
		return;
	}

	public void setStatus(PaymentInfoBean paymentInfoBean) {
		try{
			if(paymentInfoBean.getStatus())
				paymentInfoBean.getResponseObj().put(statusNode, "true");
			else
				paymentInfoBean.getResponseObj().put(statusNode, "false");

			if(paymentInfoBean.getLoggedInStatus())
				paymentInfoBean.getResponseObj().put(loggedInNode, "true");
			else
				paymentInfoBean.getResponseObj().put(loggedInNode, "false");
		}catch(Exception e){
			logger.error("Caught an Exception in settingStatus {}", e);
		}
	}

	public void logIntoDB(JSONObject params, String stage,String pnr) {
		threadPoolTaskExecutor.submit(new Runnable() {
			@Override
			public void run() {
				completePaymentInfoDAO.logIntoPaymentInfoAudit(pnr, params.toString(), stage);
			}
		});

	}

	public void setByPassUIChecksForWhitelistedEmail(PaymentInfoBean paymentInfoBean) {
		String email = paymentInfoBean.getUserEmail();
		logger.info("Checking whether to bypass ui checks for user email: " + email);

		boolean byPassUIChecksForWhitelistedEmail;
		try {
			byPassUIChecksForWhitelistedEmail = byPassUIChecksForWhitelistedEmail(email, paymentInfoBean.getProductCode(), property, payswiftStagesDAOImpl);
			paymentInfoBean.getResponseObj().put("byPassUIChecksForWhitelistedEmail", byPassUIChecksForWhitelistedEmail);
		} catch (Exception e) {
			logger.error("Caught an Exception in setting byPassUIChecksForWhitelistedEmail {}", e);
		}
	}

	public void populateSkipOtpFlag(JSONObject responseJSON, String product) throws JSONException {
		if(isProductGDSHotel(product, responseJSON)) {
			responseJSON.getJSONObject("paymentOptionJSON").getJSONObject("cc").put(PaymentUIUtil.SKIP_OTP_FLAG , "true");
		}
		else if(responseJSON.has("paymentOptionJSON") && responseJSON.getJSONObject("paymentOptionJSON").has(PaymentUIUtil.SKIP_OTP_LOB_FLAG)) {
			String skipOTPFlag = responseJSON.getJSONObject("paymentOptionJSON").getString(PaymentUIUtil.SKIP_OTP_LOB_FLAG);
			responseJSON.getJSONObject("paymentOptionJSON").remove(PaymentUIUtil.SKIP_OTP_LOB_FLAG);
			responseJSON.put(PaymentUIUtil.SKIP_OTP_FLAG , skipOTPFlag);
		}
		else responseJSON.put(PaymentUIUtil.SKIP_OTP_FLAG , "false");
	}

	public void filterPaymentOptionForGDSHotel(JSONObject responseJSON,	String product){
		logger.info("inside filterPaymentOptionForGDS method for product " + product);
		try {
			if(isProductGDSHotel(product, responseJSON)) {
				logger.info("Since it is GDS hotel flow, removing all Payops except Credit Card and setting withoutCVVsupported to true for all cards");
				if(responseJSON.has("paymentOptionJSON")) {
					JSONObject paymentOptionJson = new JSONObject();
					paymentOptionJson =responseJSON.getJSONObject("paymentOptionJSON").getJSONObject("cc");
					for(int i = 0; i<paymentOptionJson.getJSONArray("cardTypes").length(); i++) {
						paymentOptionJson.getJSONArray("cardTypes").getJSONObject(i).getJSONObject("cardDetail").put("withoutCVVSupported","true");
					}
					responseJSON.remove("paymentOptionJSON");
					responseJSON.put("paymentOptionJSON", new JSONObject().put("cc", paymentOptionJson));
					responseJSON.getJSONObject("paymentOptionJSON").getJSONObject("cc").put(PaymentUIUtil.SKIP_OTP_FLAG , "true");
				}
			}
		} catch (JSONException e) {
			logger.error("Exception while checking if product is GDS hotel", e);
		}	
	}

	private boolean isProductGDSHotel(String product, JSONObject responseJSON) throws JSONException {
		return isProductCorporateHotel(product) && isPassThrough(responseJSON);
	}

	private boolean isProductCorporateHotel(String product) {
		if(PaymentUIUtil.PRODUCT_CORP_APP_DOM_HOTEL_ANDROID.equalsIgnoreCase(product) 
				|| PaymentUIUtil.PRODUCT_CORP_APP_INT_HOTEL_ANDROID.equalsIgnoreCase(product)
				|| PaymentUIUtil.PRODUCT_CORP_APP_DOM_HOTEL_IOS.equalsIgnoreCase(product)
				|| PaymentUIUtil.PRODUCT_CORP_APP_INT_HOTEL_IOS.equalsIgnoreCase(product))
			return true;
		return false;
	}

	private boolean isPassThrough(JSONObject responseJSON) throws JSONException {
		if(responseJSON.has("bookingDetails") 
				&& responseJSON.getJSONObject("bookingDetails").has(PaymentUIUtil.ISPASSTHROUGH)
				&& responseJSON.getJSONObject("bookingDetails").getString(PaymentUIUtil.ISPASSTHROUGH).equalsIgnoreCase("true"))
			return true;
		return false;
	}

	public void logIntoDB(String params, String stage,String pnr) {
		threadPoolTaskExecutor.submit(new Runnable() {
			@Override
			public void run() {
				completePaymentInfoDAO.logIntoPaymentInfoAudit(pnr, params, stage);
			}
		});

	}
	
	private void reorderPaymentDivForSME(PaymentDiv paymentDiv, String product) {
		if(PaymentUIUtil.isSMEProduct(product)) {
			List<PaymentOption> paymentOptionList = paymentDiv.getPaymentOptions();
			for (int i = 0; i < paymentOptionList.size(); i++) {
				PaymentOption payop = paymentOptionList.get(i);
				if(payop.getCode().equalsIgnoreCase("nb")) {
					Collections.swap(paymentOptionList, i, 0);
				}
			}
			paymentDiv.setPaymentOptions(paymentOptionList);
		}
	}

	public void addCurrencyConversionJsonIfNeeded(JSONObject responseJSON, JSONObject reqJson) throws JSONException {
		if(responseJSON.has("paymentOptionJSON") && responseJSON.getJSONObject("paymentOptionJSON").has("paypal")) {
			JSONObject jsonRequest = currencyConversionService.getCurrencyConversionAPIRequest(reqJson.getString(PaymentUIUtil.PRODUCT_CODE), reqJson.getString(PaymentUIUtil.MERCHANT_CODE), reqJson.getString(PaymentUIUtil.SUPER_PNR));
			responseJSON.accumulate(currencyConversionService.getResultKey(), currencyConversionService.getRequiredData(jsonRequest, null));
		}
			
	}

}




