package com.yatra.payment.payswift.beans;

import com.yatra.payment.ui.exceptions.PayswiftException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

import com.yatra.payment.client.enums.ResponseStatus;
import com.yatra.payment.client.utils.PaymentClientUtil;
import com.yatra.payment.ui.enums.FailureCode;
import com.yatra.payment.ui.util.CorporateProductConverterUtil;
import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.platform.json.YTJSON;
import com.yatra.platform.util.YatraUtil;

import static com.yatra.payment.ui.enums.FailureCode.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class LobDetailResponse extends YTJSON {

	private static final long serialVersionUID = -5864745984427273831L;
	JSONObject lobDetailsJSON;
	APIStatusBean statusBean;
	private final String rawResponse;

	public LobDetailResponse(String lobDetails) {
		super(lobDetails);
		lobDetailsJSON = this.getJsonObject();
		this.rawResponse = lobDetails;
		this.statusBean = new APIStatusBean();
		validateResponseBean();
	}
	
	public LobDetailResponse(String lobDetails, FailureCode failureCode) {
		super(lobDetails);
		lobDetailsJSON = this.getJsonObject();
		this.rawResponse = lobDetails;
		this.statusBean = new APIStatusBean(ResponseStatus.FAILURE, failureCode);
	}

	public String getDiscountType() {
		if (!lobDetailsJSON.containsKey(PaymentUIUtil.DISCOUNT)) {
			return null;
		}
		JSONObject discountJson = JSONObject.fromObject(getDiscountJSON());
		return discountJson.getString(PaymentUIUtil.DISCOUNT_TYPE);

	}

    public JSONObject getAgentPaymentOptions() {
		if (!lobDetailsJSON.containsKey(PaymentUIUtil.AGENT_PAYMENT_OPTIONS)) {
			return null;
		}
		return lobDetailsJSON.getJSONObject(PaymentUIUtil.AGENT_PAYMENT_OPTIONS);
	}
    
	public String getItineraryDetailsHTML() {
		if (!lobDetailsJSON.containsKey(PaymentUIUtil.ITINERARY_HTML)) {
			return null;
		}
		return lobDetailsJSON.getString(PaymentUIUtil.ITINERARY_HTML);
	}

	public String getDiscountJSON() {
		if (!lobDetailsJSON.containsKey(PaymentUIUtil.DISCOUNT)) {
			return null;
		}
		return lobDetailsJSON.getString(PaymentUIUtil.DISCOUNT);
	}
	public String getCrpB2cFlag() {
		if (!lobDetailsJSON.containsKey(PaymentUIUtil.ROUTE_TO_B2C)) {
			return "false";
		}
		return lobDetailsJSON.getString(PaymentUIUtil.ROUTE_TO_B2C);
	}
	
	public String getCrpGateway() {
		if (!lobDetailsJSON.containsKey(PaymentUIUtil.CRP_GAYEWAY)) {
			return "nope";
		}
		return lobDetailsJSON.getString(PaymentUIUtil.CRP_GAYEWAY);
	}

	public String getFareBreakUpJson() {
		if (!lobDetailsJSON.containsKey(PaymentUIUtil.FARE_BREAKUP_JSON_AS_STRING)) {
			return null;
		}
		return lobDetailsJSON.getString(PaymentUIUtil.FARE_BREAKUP_JSON_AS_STRING);
	}

	public String getUserEmail() {
		if (!lobDetailsJSON.containsKey(PaymentUIUtil.USER_DETAILS) && (!lobDetailsJSON.containsKey(PaymentUIUtil.EMAIL))) {
			return null;
		}
		return lobDetailsJSON.getJSONObject(PaymentUIUtil.USER_DETAILS).getString(PaymentUIUtil.EMAIL);
	}
	
	public String getProductForFalcon() {
		if (!lobDetailsJSON.containsKey(PaymentUIUtil.CRP_INFO)) {
            return null;
        }
		
		return CorporateProductConverterUtil.getFalconProduct(lobDetailsJSON.getJSONObject(PaymentUIUtil.CRP_INFO).getString(PaymentUIUtil.BASE_PRODUCT));
		
	}

	public HashMap<String, String> getCrpInfoNode() {
		return PaymentUIUtil.getCrpInfoNode(lobDetailsJSON);
	}
	
	public String getCorporateId() {
		String corporateId = "ALL";
		Map<String, String> crpInfoMap = getCrpInfoNode();
		if(null != crpInfoMap && crpInfoMap.size() != 0) {
			if(crpInfoMap.containsKey("CLIENTID"))
				corporateId = crpInfoMap.get("CLIENTID");
		}
		return corporateId;
	}
	
	public String getCorporateTripId() {
		String ctripId = "";
		Map<String, String> crpInfoMap = getCrpInfoNode();
		if(null != crpInfoMap && crpInfoMap.size() != 0) {
			if(crpInfoMap.containsKey("TRIPID"))
				ctripId = crpInfoMap.get("TRIPID");
		}
		return ctripId;
	}

	public String getEntity() {
		if(!lobDetailsJSON.containsKey(PaymentUIUtil.ENTITY))
			return getMerchantCode();
		else return lobDetailsJSON.getString(PaymentUIUtil.ENTITY);
	}
	
	public String getUserMobile() {
		if (!lobDetailsJSON.containsKey(PaymentUIUtil.USER_DETAILS) && (!lobDetailsJSON.containsKey(PaymentUIUtil.MOBILE))) {
			return null;
		}
		return lobDetailsJSON.getJSONObject(PaymentUIUtil.USER_DETAILS).getString(PaymentUIUtil.MOBILE);
	}
	
	public String getISDCode() {
		if (!lobDetailsJSON.containsKey(PaymentUIUtil.USER_DETAILS) && (!lobDetailsJSON.containsKey(PaymentUIUtil.MOBILE_ISD_CODE))) {
			return "91";	//returning default value, as its compulsory param for promo vouchers
		}
		return lobDetailsJSON.getJSONObject(PaymentUIUtil.USER_DETAILS).getString(PaymentUIUtil.MOBILE_ISD_CODE);
	}

	public String getProductCode() {
		if (!lobDetailsJSON.containsKey(PaymentUIUtil.PRODUCT_CODE)) {
			return null;
		}
		return lobDetailsJSON.getString(PaymentUIUtil.PRODUCT_CODE);
	}
	
	public boolean isBookingDotComFlow() {
		if (!lobDetailsJSON.containsKey(PaymentUIUtil.IS_BOOKING_DOT_COM_FLOW)) {
			return false;
		}
		return lobDetailsJSON.getBoolean(PaymentUIUtil.IS_BOOKING_DOT_COM_FLOW);
	}

	public boolean isPrivyFlow() {
		if (!lobDetailsJSON.containsKey(PaymentUIUtil.PRIVY))
			return false;
		else if(YatraUtil.isNullOrEmpty(lobDetailsJSON.getString(PaymentUIUtil.PRIVY)))
			return false;
		else return true;
	}
	
	public boolean isTCSEnabled() {
		if (!lobDetailsJSON.containsKey(PaymentUIUtil.IS_TCS_ENABLED))
			return false;
		return lobDetailsJSON.getBoolean(PaymentUIUtil.IS_TCS_ENABLED);
	}
	
	public String getProductCodeForPortal() {
		if (!lobDetailsJSON.containsKey(PaymentUIUtil.PRODUCT_CODE)) {
			return null;
		}
		if (lobDetailsJSON.containsKey(PaymentUIUtil.RESCHEDULING)) {
			return PaymentUIUtil.PRODUCT_DOM2;
		}
		return lobDetailsJSON.getString(PaymentUIUtil.PRODUCT_CODE);
	}

	public String getProductCodeForUI() {
		if (lobDetailsJSON.containsKey(PaymentUIUtil.UI_PRODUCT_CODE)) {
			String product = lobDetailsJSON.getString(PaymentUIUtil.UI_PRODUCT_CODE);
			if (StringUtils.isNotBlank(product) && !product.equalsIgnoreCase("null")) {
				return product;
			}
		}
		return getProductCode();
	}
	
	public String getHideEcashDivFlag() {
		if(lobDetailsJSON.containsKey(PaymentUIUtil.HIDE_ECASH_DIV_FLAG)){
			return lobDetailsJSON.getString(PaymentUIUtil.HIDE_ECASH_DIV_FLAG);
		}
		return "false";
	}

	public String getMerchantCode() {
		if (!lobDetailsJSON.containsKey(PaymentUIUtil.MERCHANT_CODE)) {
			return null;
		}
		return lobDetailsJSON.getString(PaymentUIUtil.MERCHANT_CODE);
	}

	public String getMaximumRedeemableECash() {
		String maxRedeemableEcash = "0.0";
		if (lobDetailsJSON.containsKey(PaymentUIUtil.MAX_YLP)) {
			maxRedeemableEcash = lobDetailsJSON.getString(PaymentUIUtil.MAX_YLP);
		}
		else if(lobDetailsJSON.containsKey(PaymentUIUtil.WALLET_DETAILS)){
			JSONObject walletDetails = lobDetailsJSON.getJSONObject(PaymentUIUtil.WALLET_DETAILS);
			maxRedeemableEcash = walletDetails.getString(PaymentUIUtil.MAX_YLP);
		}
		return maxRedeemableEcash;
	}
	
	public String getWalletProduct() {
		String product = "";
		if(lobDetailsJSON.containsKey(PaymentUIUtil.WALLET_DETAILS)){
			JSONObject walletDetails = lobDetailsJSON.getJSONObject(PaymentUIUtil.WALLET_DETAILS);
			product = walletDetails.getString(PaymentUIUtil.WALLET_PRODUCT);
		}
		return product;
	}

	public String getChannel() {
		String product = "";
		if(lobDetailsJSON.containsKey(PaymentUIUtil.WALLET_DETAILS)){
			JSONObject walletDetails = lobDetailsJSON.getJSONObject(PaymentUIUtil.WALLET_DETAILS);
			product = walletDetails.getString(PaymentUIUtil.CHANNEL);
		}
		return product;
	}
	
	public String getDevice() {
		String product = "";
		if(lobDetailsJSON.containsKey(PaymentUIUtil.WALLET_DETAILS)){
			JSONObject walletDetails = lobDetailsJSON.getJSONObject(PaymentUIUtil.WALLET_DETAILS);
			product = walletDetails.getString(PaymentUIUtil.DEVICE);
		}
		return product;
	}

	public boolean isPassThrough() {
		if (lobDetailsJSON.containsKey(PaymentUIUtil.ISPASSTHROUGH)) {
			String passthru = lobDetailsJSON.get(PaymentUIUtil.ISPASSTHROUGH).toString();
			return StringUtils.isNotBlank(passthru) && !passthru.equals("false");
		}
		return false;
	}

	public String getAmount() {
		if (lobDetailsJSON.containsKey(PaymentUIUtil.PARTIAL_AMOUNT)) {
			return lobDetailsJSON.getString(PaymentUIUtil.PARTIAL_AMOUNT);
		}
		return null;
	}

	public String getTotalAmount() {
		if (lobDetailsJSON.containsKey(PaymentUIUtil.TOTAL_AMOUNT)) {
			return lobDetailsJSON.getString(PaymentUIUtil.TOTAL_AMOUNT);
		}
		return null;
	}

	public String getOrigin() {
		if (lobDetailsJSON.containsKey(PaymentUIUtil.ORIGIN)) {
			return lobDetailsJSON.getString(PaymentUIUtil.ORIGIN);
		}
		return null;
	}

	public String getDestination() {
		if (lobDetailsJSON.containsKey(PaymentUIUtil.DESTINATION)) {
			return lobDetailsJSON.getString(PaymentUIUtil.DESTINATION);
		}
		return null;
	}

	public String getTripType() {
		if (lobDetailsJSON.containsKey(PaymentUIUtil.TRIP_TYPE)) {
			return lobDetailsJSON.getString(PaymentUIUtil.TRIP_TYPE);
		}
		return null;
	}
	public String getValidateAPIEndpoint() {
		if(lobDetailsJSON.containsKey(PaymentUIUtil.VALIDATE_API_ENDPOINT)){
			return lobDetailsJSON.getString(PaymentUIUtil.VALIDATE_API_ENDPOINT);
		}
		return null;
	}

	public String getTravelDate() {
		if (lobDetailsJSON.containsKey(PaymentUIUtil.TRAVEL_DATE)) {
			return lobDetailsJSON.getString(PaymentUIUtil.TRAVEL_DATE);
		}
		return null;
	}
	
	public String getAgentProfileType() {
		if (lobDetailsJSON.containsKey(PaymentUIUtil.AGENT_PROFILE_TYPE)) {
			return lobDetailsJSON.getString(PaymentUIUtil.AGENT_PROFILE_TYPE);
		}
		return null;
	}
	
	public String getBookingType(){
		String agentProfileType = getAgentProfileType();
		if(!StringUtils.isEmpty(agentProfileType)){
			return "PERSONAL".equalsIgnoreCase(agentProfileType) ? agentProfileType.toUpperCase() : "OFFICIAL"; 
		}
		return null;
	}
	
	public String getPromoCode() {
		if (lobDetailsJSON.containsKey(PaymentUIUtil.PROMO_CODE_NODE)) {
			JSONObject promoJson = lobDetailsJSON.getJSONObject(PaymentUIUtil.PROMO_CODE_NODE);

			if (!promoJson.isEmpty() && !promoJson.isNullObject()) {
				return promoJson.optString(PaymentUIUtil.CODE);
			}
		}
		return null;
	}
	
	public String getIsGVApplicableWithPromoCode() {
		if (lobDetailsJSON.containsKey(PaymentUIUtil.PROMO_CODE_NODE)) {
			JSONObject promoJson = lobDetailsJSON.getJSONObject(PaymentUIUtil.PROMO_CODE_NODE);
			if (promoJson != null) {
				return promoJson.optString(PaymentUIUtil.IS_VOUCHER_APPLICABLE);
			}
		}
		return null;
	}

	public String getOriginAirport() {
		if (lobDetailsJSON.containsKey(PaymentUIUtil.OTHER_DETAILS) && (lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).containsKey(PaymentUIUtil.ORIGIN_AIRPORT))) {
			return lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).getString(PaymentUIUtil.ORIGIN_AIRPORT);
		}
		return null;
	}

	public String getDestinationAirport() {
		if (lobDetailsJSON.containsKey(PaymentUIUtil.OTHER_DETAILS) && (lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).containsKey(PaymentUIUtil.DESTINATION_AIRPORT))) {
			return lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).getString(PaymentUIUtil.DESTINATION_AIRPORT);
		}
		return null;
	}

	public String getOriginCountry() {
		if (lobDetailsJSON.containsKey(PaymentUIUtil.OTHER_DETAILS) && (lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).containsKey(PaymentUIUtil.ORIGIN_COUNTRY))) {
			return lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).getString(PaymentUIUtil.ORIGIN_COUNTRY);
		}
		return null;
	}

	public String getDestinationCountry() {
		if (lobDetailsJSON.containsKey(PaymentUIUtil.OTHER_DETAILS) && (lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).containsKey(PaymentUIUtil.DESTINATION_COUNTRY))) {
			return lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).getString(PaymentUIUtil.DESTINATION_COUNTRY);
		}
		return null;
	}

	public String getOriginCity() {
		if (lobDetailsJSON.containsKey(PaymentUIUtil.OTHER_DETAILS) && (lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).containsKey(PaymentUIUtil.ORIGIN_CITY))) {
			return lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).getString(PaymentUIUtil.ORIGIN_CITY);
		}
		return null;
	}

	public String getDestinationCity() {
		if (lobDetailsJSON.containsKey(PaymentUIUtil.OTHER_DETAILS) && (lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).containsKey(PaymentUIUtil.DESTINATION_CITY))) {
			return lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).getString(PaymentUIUtil.DESTINATION_CITY);
		}
		return null;
	}

	public String getNumAdtPax() {
		if (lobDetailsJSON.containsKey(PaymentUIUtil.OTHER_DETAILS) && (lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).containsKey(PaymentUIUtil.NUM_ADULT_PAX))) {
			return lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).getString(PaymentUIUtil.NUM_ADULT_PAX);
		}
		return null;
	}

	public String getNumChdPax() {
		if (lobDetailsJSON.containsKey(PaymentUIUtil.OTHER_DETAILS) && (lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).containsKey(PaymentUIUtil.NUM_CHILD_PAX))) {
			return lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).getString(PaymentUIUtil.NUM_CHILD_PAX);
		}
		return null;
	}

	public String getNumInfPax() {
		if (lobDetailsJSON.containsKey(PaymentUIUtil.OTHER_DETAILS) && (lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).containsKey(PaymentUIUtil.NUM_INFANT_PAX))) {
			return lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).getString(PaymentUIUtil.NUM_INFANT_PAX);
		}
		return null;
	}

	public String getNumTotPax() {
		if (lobDetailsJSON.containsKey(PaymentUIUtil.OTHER_DETAILS) && (lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).containsKey(PaymentUIUtil.NUM_TOTAL_PAX))) {
			return lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).getString(PaymentUIUtil.NUM_TOTAL_PAX);
		}
		return null;
	}

	public String getAddons() {
		if (lobDetailsJSON.containsKey(PaymentUIUtil.OTHER_DETAILS) && (lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).containsKey(PaymentUIUtil.ADDONS))) {
			return lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).getString(PaymentUIUtil.ADDONS);
		}
		return null;
	}

	public String getCarrierName() {
		if (lobDetailsJSON.containsKey(PaymentUIUtil.OTHER_DETAILS) && (lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).containsKey(PaymentUIUtil.CARRIER_NAME))) {
			return lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).getString(PaymentUIUtil.CARRIER_NAME);
		}
		return null;
	}
	
	public String getOriginCityName() {
		if (lobDetailsJSON.containsKey(PaymentUIUtil.OTHER_DETAILS) && (lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).containsKey(PaymentUIUtil.ORIGIN_CITY_NAME))) {
			return lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).getString(PaymentUIUtil.ORIGIN_CITY_NAME);
		}
		return null;
	}
	
	public String getDestinationCityName() {
		if (lobDetailsJSON.containsKey(PaymentUIUtil.OTHER_DETAILS) && (lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).containsKey(PaymentUIUtil.DESTINATION_CITY_NAME))) {
			return lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).getString(PaymentUIUtil.DESTINATION_CITY_NAME);
		}
		return null;
	}
	
	public String getTotalTravelTime() {
		if (lobDetailsJSON.containsKey(PaymentUIUtil.OTHER_DETAILS) && (lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).containsKey(PaymentUIUtil.TOTAL_TRAVEL_TIME))) {
			return lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).getString(PaymentUIUtil.TOTAL_TRAVEL_TIME);
		}
		return null;
	}

	public String getIsDomestic() {
		if (lobDetailsJSON.containsKey(PaymentUIUtil.OTHER_DETAILS) && (lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).containsKey(PaymentUIUtil.IS_DOMESTIC))) {
			String isDomestic = lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).getString(PaymentUIUtil.IS_DOMESTIC);
			if (isDomestic.equalsIgnoreCase("true"))
				return "DOMESTIC";
			else
				return "INTERNATIONAL";
		}
		return null;
	}

	public String getPaxList() {
		if (lobDetailsJSON.containsKey(PaymentUIUtil.OTHER_DETAILS) && (lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).containsKey(PaymentUIUtil.PAX_LIST))) {
			return lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).getJSONArray(PaymentUIUtil.PAX_LIST).toString();
		}
		return null;
	}

	public String getFlightNumber() {
		if (lobDetailsJSON.containsKey(PaymentUIUtil.OTHER_DETAILS) && (lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).containsKey(PaymentUIUtil.FLIGHT_NUMBER))) {
			return lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).getString(PaymentUIUtil.FLIGHT_NUMBER);
		}
		return null;
	}

	public String getFlightClass() {
		if (lobDetailsJSON.containsKey(PaymentUIUtil.OTHER_DETAILS) && (lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).containsKey(PaymentUIUtil.FLIGHT_CLASS))) {
			return lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).getString(PaymentUIUtil.FLIGHT_CLASS);
		}
		return null;
	}
	
	public String autoRedeemEcash() {
		if (lobDetailsJSON.containsKey(PaymentUIUtil.AUTO_REDEEM_ECASH)) {
			return lobDetailsJSON.getString(PaymentUIUtil.AUTO_REDEEM_ECASH);
		}
		return "false";
	}

	public boolean saveGuestCard() {
			if(lobDetailsJSON.containsKey(PaymentUIUtil.SAVEGUESTCARD)){
				String saveGuestCard = lobDetailsJSON.get(PaymentUIUtil.SAVEGUESTCARD).toString();
				return StringUtils.isNotBlank(saveGuestCard) && !saveGuestCard.equals("{}") ;
			}
				return false;
	}
	
	public String getHotelName() {
		if (lobDetailsJSON.containsKey(PaymentUIUtil.OTHER_DETAILS) && (lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).containsKey(PaymentUIUtil.HOTEL_NAME))) {
			return lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).getString(PaymentUIUtil.HOTEL_NAME);
		}
		return null;
	}
	
	public String getHotelCountry() {
		if (lobDetailsJSON.containsKey(PaymentUIUtil.OTHER_DETAILS) && (lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).containsKey(PaymentUIUtil.HOTEL_COUNTRY))) {
			return lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).getString(PaymentUIUtil.HOTEL_COUNTRY);
		}
		return null;
	}
	
	public String getHotelCity() {
		if (lobDetailsJSON.containsKey(PaymentUIUtil.OTHER_DETAILS) && (lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).containsKey(PaymentUIUtil.HOTEL_CITY))) {
			return lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).getString(PaymentUIUtil.HOTEL_CITY);
		}
		return null;
	}
	
	public String getHotelGrade() {
		if (lobDetailsJSON.containsKey(PaymentUIUtil.OTHER_DETAILS) && (lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).containsKey(PaymentUIUtil.HOTEL_GRADE))) {
			return lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).getString(PaymentUIUtil.HOTEL_GRADE);
		}
		return null;
	}
	
	public String getNumberOfNightStay() {
		if (lobDetailsJSON.containsKey(PaymentUIUtil.OTHER_DETAILS) && (lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).containsKey(PaymentUIUtil.NUMBER_OF_NIGHT_STAY))) {
			return lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).getString(PaymentUIUtil.NUMBER_OF_NIGHT_STAY);
		}
		return null;
	}
	
	public String getNumberOfRoom() {
		if (lobDetailsJSON.containsKey(PaymentUIUtil.OTHER_DETAILS) && (lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).containsKey(PaymentUIUtil.NUMBER_OF_ROOM))) {
			return lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).getString(PaymentUIUtil.NUMBER_OF_ROOM);
		}
		return null;
	}
	
	public String getNumberOfGuest() {
		if (lobDetailsJSON.containsKey(PaymentUIUtil.OTHER_DETAILS) && (lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).containsKey(PaymentUIUtil.NUMBER_OF_GUEST))) {
			return lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).getString(PaymentUIUtil.NUMBER_OF_GUEST);
		}
		return null;
	}
	
	public String getServiceType() {
		if (lobDetailsJSON.containsKey(PaymentUIUtil.SERVICE_TYPE)) {
			return lobDetailsJSON.getString(PaymentUIUtil.SERVICE_TYPE);
		}
		return null;
	}
	
	public String getBookingRef() {
		if (lobDetailsJSON.containsKey(PaymentUIUtil.SAPG_BOOKING_REF)) {
			return lobDetailsJSON.getString(PaymentUIUtil.SAPG_BOOKING_REF);
		}
		return null;
	}
	
	public String getSuperPnr() {
		if (lobDetailsJSON.containsKey(PaymentUIUtil.SUPER_PNR)) {
			return lobDetailsJSON.getString(PaymentUIUtil.SUPER_PNR);
		}
		return null;
	}
	
	public boolean isGAEnabled() {
		boolean isGAEnabled = true;
		if (lobDetailsJSON.containsKey(PaymentUIUtil.FORCE_GA)) {
			String isGAEnabledString = lobDetailsJSON.getString(PaymentUIUtil.FORCE_GA);
			if("false".equalsIgnoreCase(isGAEnabledString))
				isGAEnabled = false;
		}
		return isGAEnabled;
	}
	
	public boolean saveCardWithCVV(){
		if(lobDetailsJSON.containsKey(PaymentUIUtil.SAVECARDWITHCVV)){
			String saveCardWithCVV = lobDetailsJSON.get(PaymentUIUtil.SAVECARDWITHCVV).toString();
				return StringUtils.isNotBlank(saveCardWithCVV) && !saveCardWithCVV.equals("{}") ;
		}
			return false;
	}
	
	public JSONArray getHeaderMessages(){
		if(lobDetailsJSON.containsKey(PaymentUIUtil.HEADER_MESSAGES)){
			return lobDetailsJSON.getJSONArray(PaymentUIUtil.HEADER_MESSAGES);
		}
			return null;
	}
	
	public String getCarrierClass() {
		if (lobDetailsJSON.containsKey(PaymentUIUtil.OTHER_DETAILS) && (lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).containsKey(PaymentUIUtil.CARRIER_CLASS))) {
			return lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).getString(PaymentUIUtil.CARRIER_CLASS);
		}
		return null;
	}
	
	public String getCarrierNumber() {
		if (lobDetailsJSON.containsKey(PaymentUIUtil.OTHER_DETAILS) && (lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).containsKey(PaymentUIUtil.CARRIER_NUMBER))) {
			return lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).getString(PaymentUIUtil.CARRIER_NUMBER);
		}
		return null;
	}
	
	public String getActivityType() {
		if (lobDetailsJSON.containsKey(PaymentUIUtil.OTHER_DETAILS) && (lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).containsKey(PaymentUIUtil.ACTIVITY_TYPE))) {
			return lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).getString(PaymentUIUtil.ACTIVITY_TYPE);
		}
		return null;
	}
	
	public String getMonumentCode() {
		if (lobDetailsJSON.containsKey(PaymentUIUtil.OTHER_DETAILS) && (lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).containsKey(PaymentUIUtil.MONUMENT_CODE))) {
			return lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).getString(PaymentUIUtil.MONUMENT_CODE);
		}
		return null;
	}
	
	public String getAsiAmount() {
		if (lobDetailsJSON.containsKey(PaymentUIUtil.OTHER_DETAILS) && (lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).containsKey(PaymentUIUtil.ASI_AMOUNT))) {
			return lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).getString(PaymentUIUtil.ASI_AMOUNT);
		}
		return null;
	}
	
	public String getLdaAmount() {
		if (lobDetailsJSON.containsKey(PaymentUIUtil.OTHER_DETAILS) && (lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).containsKey(PaymentUIUtil.LDA_AMOUNT))) {
			return lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).getString(PaymentUIUtil.LDA_AMOUNT);
		}
		return null;
	}
	
	public String getSpecialAmountForMonuments() {
		if (lobDetailsJSON.containsKey(PaymentUIUtil.OTHER_DETAILS) && (lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).containsKey(PaymentUIUtil.SPECIAL_AMOUNT_FOR_MONUMENTS))) {
			return lobDetailsJSON.getJSONObject(PaymentUIUtil.OTHER_DETAILS).getString(PaymentUIUtil.SPECIAL_AMOUNT_FOR_MONUMENTS);
		}
		return null;
	}
	
	public String getTravellerSSOId(){
		if(lobDetailsJSON.containsKey(PaymentUIUtil.TRAVELLER_SSO_ID)){
			return lobDetailsJSON.getString(PaymentUIUtil.TRAVELLER_SSO_ID);
		}
		return null;
	}
	
	public String getAgentId() {
		if (lobDetailsJSON.containsKey(PaymentUIUtil.AGENT_PAYMENT_OPTIONS) && (lobDetailsJSON.getJSONObject(PaymentUIUtil.AGENT_PAYMENT_OPTIONS).containsKey(PaymentUIUtil.AGENT_ID))) {
			return lobDetailsJSON.getJSONObject(PaymentUIUtil.AGENT_PAYMENT_OPTIONS).getString(PaymentUIUtil.AGENT_ID);
		}
		return null;
	}
	
	public String getIxigoMoney(){
		if(lobDetailsJSON.containsKey(PaymentUIUtil.IXIGO_MONEY)){
			return lobDetailsJSON.getString(PaymentUIUtil.IXIGO_MONEY);
		}
		return null;
	}
	
	public boolean isOnlyEcashGV(){
		if(lobDetailsJSON.containsKey(PaymentUIUtil.ONLY_ECASH_GV)){
			return "true".equalsIgnoreCase(lobDetailsJSON.getString(PaymentUIUtil.ONLY_ECASH_GV));
		}
		return false;
	}

	public ResponseStatus getResponseStatus() {
		return this.statusBean.getResponseStatus();
	}
	
	public FailureCode getFailureCode() {
		return this.statusBean.getFailureCode();
	}
	
	public void setFailureCode(FailureCode failureCode) {
		this.statusBean.setFailureCode(failureCode);
	}

	public void setStatusBean(APIStatusBean statusBean) {
		this.statusBean = statusBean;
	}

	public APIStatusBean getStatusBean() {
		return statusBean;
	}

	public String getRawResponse() {
		return rawResponse;
	}
	
	public String getPayLaterFlow() {
		if (lobDetailsJSON.containsKey(PaymentUIUtil.PAY_LATER_FLOW)) {
			return lobDetailsJSON.getString(PaymentUIUtil.PAY_LATER_FLOW);
		}
		return null;
	}
	
	public int getMaxCountOfVoucherAllowed() {
		if (lobDetailsJSON.containsKey(PaymentUIUtil.VOUCHER_LIMIT)) {
			return lobDetailsJSON.getInt(PaymentUIUtil.VOUCHER_LIMIT);
		}
		return 0;
	}
	
	
	public JSONArray getCancellationPolicy(){
		if(lobDetailsJSON.containsKey(PaymentUIUtil.CANCELLATION_POLICY)){
			return lobDetailsJSON.getJSONArray(PaymentUIUtil.CANCELLATION_POLICY);
		}
			return null;
	}

	private void validateResponseBean(){
		char separator = ';';
		
		if(this.statusBean.getResponseStatus() == ResponseStatus.FAILURE)
			return;

		if(lobDetailsJSON.containsKey("error") && StringUtils.isNotBlank(lobDetailsJSON.getString("error"))){
			this.statusBean.setFailureCode(FETCH_ITINERARY_FAIL_RESPONSE.setCustomCode("Fetch itinerary response contains error: " + lobDetailsJSON.getString("error")));
		}
		
		StringBuilder missingParamsSB = new StringBuilder();
		if(YatraUtil.isNullOrEmpty(getSuperPnr())){
			missingParamsSB.append(PaymentUIUtil.SUPER_PNR).append(separator);
		}
		
		if(YatraUtil.isNullOrEmpty(getProductCode())){
			missingParamsSB.append(PaymentUIUtil.PRODUCT_CODE).append(separator);
		}
		
		if(YatraUtil.isNullOrEmpty(getFareBreakUpJson())){
			missingParamsSB.append(PaymentUIUtil.FARE_BREAKUP_JSON_AS_STRING).append(separator);
		}
		
		if(missingParamsSB.length() > 0){
			this.statusBean.setFailureCode(FETCH_ITINERARY_REQUIRED_PARAMS_MISSING.setCustomCode(missingParamsSB.toString()));
		}

		FailureCode responseFailureCode = this.statusBean.getFailureCode();
		if (responseFailureCode != NO_FAILURE_CAPTURED) {
			throw new PayswiftException(responseFailureCode.toString(), responseFailureCode.getCode());
		}
	}
	
	public String getUUID() {
		if(!lobDetailsJSON.containsKey(PaymentUIUtil.UUID))
			return null;
		else return lobDetailsJSON.getString(PaymentUIUtil.UUID);
	}

	public String getIsAuthPreferred() {
		if (!lobDetailsJSON.containsKey(PaymentUIUtil.IS_AUTH_PREFERRED)) {
			return null;
		}
		return lobDetailsJSON.getString(PaymentUIUtil.IS_AUTH_PREFERRED);
	}

	public String getCheckBookingStatusUrl() {
		if (!lobDetailsJSON.containsKey(PaymentUIUtil.CHECK_BOOKING_STATUS_URL)) {
			return null;
		}
		return lobDetailsJSON.getString(PaymentUIUtil.CHECK_BOOKING_STATUS_URL);
	}

	public String getNZDCurrency() {
		if (!lobDetailsJSON.containsKey(PaymentUIUtil.CURRENCY_NAME)) {
			return null;
		}
		return lobDetailsJSON.getString(PaymentUIUtil.CURRENCY_NAME);
	}


}
