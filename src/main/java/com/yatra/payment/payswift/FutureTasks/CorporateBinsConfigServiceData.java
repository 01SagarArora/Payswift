package com.yatra.payment.payswift.FutureTasks;

import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import com.yatra.payment.ui.service.impl.CorporateBinsConfigServiceImpl;

public class CorporateBinsConfigServiceData implements Callable {
	
Logger logger = Logger.getLogger(CorporateBinsConfigServiceData.class);
	
	private String ssoToken;
	private String superPNR;
	private String bookingType;
	private String productForFalcon;
	private CorporateBinsConfigServiceImpl corpBinConfigService;
	
	public CorporateBinsConfigServiceData(String ssoToken, String superPNR, CorporateBinsConfigServiceImpl corpBinConfigService, String bookingType, String productForFalcon) {
		this.ssoToken = ssoToken;
		this.superPNR = superPNR;
		this.corpBinConfigService = corpBinConfigService;
		this.bookingType = bookingType;
		this.productForFalcon = productForFalcon;
	}

	@Override
	public Object call() throws Exception {
		JSONObject request = createRequestJSON(this.ssoToken, this.superPNR, this.bookingType, this.productForFalcon);
		return net.sf.json.JSONObject.fromObject(corpBinConfigService.getRequiredData(request, new JSONObject()).toString());
	}
	
	private JSONObject createRequestJSON(String ssoToken, String superPNR, String bookingType, String productForFalcon ) {
		JSONObject request = new JSONObject();
		try{
			request.put("ssoToken", ssoToken);
			if(StringUtils.isNotEmpty(superPNR))
				request.put("superPnr", superPNR);
			if(StringUtils.isNotEmpty(bookingType))
				request.put("bookingType", bookingType);
			if(StringUtils.isNotEmpty(productForFalcon))
				request.put("productForFalcon", productForFalcon);
			return request;
		} catch(Exception ex) {
			logger.error("Error while creating request for CorporateBinConfigService", ex);
		}
		return request;
	}
	

}
