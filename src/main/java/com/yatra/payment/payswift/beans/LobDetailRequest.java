package com.yatra.payment.payswift.beans;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.platform.commons.utils.RequestUtils;
import com.yatra.platform.util.YatraUtil;

public class LobDetailRequest {
	private String superPnr;
	private String client;
	private String uuid;
	private String product;
	private String ssoToken;
	private String endpoint;
	private String additionalPayment;
	private String appVersion;

	public LobDetailRequest(PaymentRequest paymentRequest){
		this.superPnr = paymentRequest.getSuperPnr();
		this.client = paymentRequest.getClient().toString();
		this.uuid = paymentRequest.getUuid();
		this.product = paymentRequest.getProductCode();
		this.endpoint = paymentRequest.getBookingDetailAPIEndpoint();
		this.additionalPayment = paymentRequest.getAdditionalPayment();
		this.ssoToken = paymentRequest.getSsoToken();
	}
	
	public LobDetailRequest(PaymentInfoBean paymentInfoBean){
		this.superPnr = paymentInfoBean.getSuperPnr();
		this.client = paymentInfoBean.getClient();
		this.uuid = paymentInfoBean.getUuid();
		this.product = paymentInfoBean.getProductCode();
		this.ssoToken = paymentInfoBean.getSsoToken();
		this.additionalPayment = paymentInfoBean.getAdditionalPayment();
		this.endpoint = paymentInfoBean.getBookingDetailAPIEndpoint();
		this.appVersion = paymentInfoBean.getAppVersion();
	}
	
	public LobDetailRequest(PayNowRequest payNowRequest) {
		this.superPnr = payNowRequest.getSuperPnr();
		this.client = payNowRequest.getClient().toString();
		this.product = payNowRequest.getProductCode();
		this.uuid = payNowRequest.getUUID();
		this.additionalPayment = payNowRequest.getAdditionalPayment();
		this.ssoToken = payNowRequest.getSSOToken();
		this.endpoint = payNowRequest.getBookingDetailAPIEndpoint();
		this.appVersion = payNowRequest.getAppVersion();
	}
	
	public LobDetailRequest(String superPnr, String client, String uuid, String productCode, String ssoToken, String additionalPayment){
		this.superPnr = superPnr;
		this.client = client;
		this.uuid = uuid;
		this.product = productCode;
		this.ssoToken = ssoToken;
		this.additionalPayment = additionalPayment;
	}
	

	public Map<String,String> getRequestMap(){
		Map<String,String> requestMap = new HashMap<String,String>();
		requestMap.put(PaymentUIUtil.SUPER_PNR, superPnr);
		requestMap.put(PaymentUIUtil.CLIENT, client);
		if(!YatraUtil.isNullOrEmpty(this.ssoToken) && !this.ssoToken.equalsIgnoreCase("null")){
			requestMap.put(PaymentUIUtil.SSO_TOKEN, this.ssoToken);
		}
		if(StringUtils.isNotBlank(uuid) && !uuid.equalsIgnoreCase("null")){
			requestMap.put(PaymentUIUtil.UUID, uuid);
		}
		if(StringUtils.isNotBlank(additionalPayment) && !additionalPayment.equalsIgnoreCase("null")){
			requestMap.put(PaymentUIUtil.ADDITIONAL_PAYMENT, additionalPayment);
		}
		if(StringUtils.isNotBlank(appVersion)){
			requestMap.put(PaymentUIUtil.APP_VERSION,appVersion);
		}
		return requestMap;
	}
	public String getSuperPnr(){
		return superPnr;
	}
	public String getProduct(){
		return product;
	}

	@Override
	public String toString() {
		return RequestUtils.getQueryString(getRequestMap());
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}
	
	public String getAdditionalPayment() {
		return additionalPayment;
	}
}
