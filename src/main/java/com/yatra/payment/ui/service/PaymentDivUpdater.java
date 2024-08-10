package com.yatra.payment.ui.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.ui.util.HttpUtil;
import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.platform.commons.property.PropertyManager;

import static com.yatra.payment.ui.util.PaymentUIUtil.CONTENT_TYPE_APPLICATION_JSON;
import static com.yatra.payment.ui.util.PaymentUIUtil.CONTENT_TYPE_FORM_URLENCODED;

@Service("paymentDivUpdater")
public class PaymentDivUpdater {
	
	private static Logger logger = Logger.getLogger(PaymentDivUpdater.class);
	
	@Autowired private HttpUtil httpUtil;
	@Autowired	private PropertyManager  yatraPropertyReader;
	
	/**
	 * updatePaymentDiv makes serevr call to all the paymeny servers to update the div, except local 
	 * @param localIpAddress
	 * @throws Exception 
	 */
	public void updatePaymentDiv(String localIpAddress) throws Exception {
		logger.debug("updating payment div in all the other payment servers");
		String paymentServersString = yatraPropertyReader.getProperty("payment.servers");
		String paymnetDivUpdateUrl = yatraPropertyReader.getProperty("payment.div.update.url");
		String params = httpUtil.createPostDataFromMap(new HashMap<String, String>(){{put("internal", "true");}});
		
		List<String> paymentServersList = PaymentUIUtil.createList(paymentServersString, ",");
		for(String paymentServer : paymentServersList){
			if(paymentServer!=null && !paymentServer.equals(localIpAddress)){
				String completeUpdateDivUrl = PaymentUIUtil.HTTP_PROTOCOL_URL_PREFIX+paymentServer+paymnetDivUpdateUrl;
				Map<String, String> headers = new HashMap<>();
				headers.put("httpMethod", "PUT");
				headers.put("contentType", CONTENT_TYPE_FORM_URLENCODED);
				headers.put("encoding", PaymentUIUtil.UTF_8_ENCODING);
				String result = httpUtil.invoke(completeUpdateDivUrl, params, headers, "UPDATE_PAYMENT_DIV");
				logger.info("Response from update payment div in :"+paymentServer+" :"+result);
			}
		}
	}
	

}
