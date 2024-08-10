package com.yatra.payment.payswift.beans;

import com.yatra.payment.ui.enums.ClientType;
import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.platform.commons.utils.RequestUtils;
import com.yatra.platform.request.MapBasedRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.FutureTask;

public class PaymentRequest extends MapBasedRequest {

	private static final long serialVersionUID = 5547838305416945960L;
	
	Map<String,FutureTask> futureTasksMap = new HashMap<>();
	private Map<String,String> cookieMap ;
	private Map<String, String> skipOtpFlagMap;
	private String host;

	private static Logger logger = Logger.getLogger(PaymentRequest.class);

	public PaymentRequest(HttpServletRequest httpRequest) {
		super(httpRequest);
		setCookieMap(PaymentUIUtil.getCookieMap(httpRequest));
		try {
			URL url =  new URL(httpRequest.getRequestURL().toString());
			this.host = url.getHost();
			logger.info("url host is:"+host);
		} catch (MalformedURLException e) {
			logger.warn("Error in getting host from request url"+e.getMessage());
		}
	}
	
	public void addFutureTask(String key,FutureTask task){
		futureTasksMap.put(key, task);
	}
	public FutureTask getFutureTask(String key){
		return futureTasksMap.get(key);
	}
	
	public String getTtid() {
		return getParameter(PaymentUIUtil.TTID);
	}
	public String getBookingDetailAPIEndpoint() {
		return getParameter(PaymentUIUtil.DETAIL_API_ENDPOINT);
	}
	public String getSuperPnr() {
		return getParameter(PaymentUIUtil.SUPER_PNR);
	}
	public String getProductCode() {
		return getParameter(PaymentUIUtil.PRODUCT_CODE);
	}
	public String getMerchantCode() {
		return getParameter(PaymentUIUtil.MERCHANT_CODE);
	}
	public String getSsoToken() {
		return cookieMap.get(PaymentUIUtil.SSO_TOKEN);
	}
	public String getUuid() {
		return getParameter(PaymentUIUtil.UUID);
	}
	public String getAdditionalPayment() {
		return getParameter(PaymentUIUtil.ADDITIONAL_PAYMENT);
	}
	public String getSkipUI() {
		return getParameter(PaymentUIUtil.SKIP_UI);
	}
	public String getMessageForDisplay() {
		String message= getParameter(PaymentUIUtil.MESSAGE);
		if(StringUtils.isNotBlank(message))
			return message;
		else return "";
	}
	
	public ClientType getClient() {
		try {
			if (getParameterMap().containsKey(PaymentUIUtil.CLIENT)) {
				return ClientType.valueOf(getParameter(PaymentUIUtil.CLIENT));
			}
		} catch (Exception e) {
		}
		return ClientType.DESKTOP;
	}
	public void putPgDataMap(Map<String, String> pgDataMap) {
		this.getParameterMap().putAll(pgDataMap);
	}

	@Override
	public String toString() {
		return "PaymentRequest [getTtid()=" + getTtid() + ", getSuperPnr()=" + getSuperPnr() + ", getProductCode()=" + getProductCode() + ", getMerchantCode()=" + getMerchantCode()
				+ ", getSsoToken()=" + getSsoToken() + "]";
	}
	
	public String getQueryString(){
		Map<String,String> parameterMap = getParameterMap();
		parameterMap.remove(PaymentUIUtil.SSO_TOKEN);
		return RequestUtils.getQueryString(parameterMap);
	}

	public Map<String,String> getCookieMap() {
		return cookieMap;
	}

	public void setCookieMap(Map<String,String> cookieMap) {
		this.cookieMap = cookieMap;
	}

	public String getHost() {
		return host;
	}

	public Optional<String> getMinifyInfo() {
		String minify = getParameter(PaymentUIUtil.MINIFY);
		if(StringUtils.isNotBlank(minify)) {
			return Optional.of(minify);
		}
		return Optional.empty();
	}

	public Map<String, String> getSkipOtpFlagMap() {
		return skipOtpFlagMap;
	}

	public void setSkipOtpFlagMap(Map<String, String> skipOtpFlagMap) {
		this.skipOtpFlagMap = skipOtpFlagMap;
	}
	
}
