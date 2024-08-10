package com.yatra.payment.payswift.FutureTasks;

import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.yatra.payment.ui.service.impl.CorporateConfigurationServiceImpl;

public class CorporateConfigServiceData implements Callable {

	Logger logger = Logger.getLogger(CorporateConfigServiceData.class);
	
	private String ssoToken;
	private String superPNR;
	private CorporateConfigurationServiceImpl corpConfigService;
	
	public CorporateConfigServiceData(String ssoToken, String superPNR, CorporateConfigurationServiceImpl corpConfigService) {
		this.ssoToken = ssoToken;
		this.superPNR = superPNR;
		this.corpConfigService = corpConfigService;
	}

	@Override
	public Object call() throws Exception {
		JSONObject request = createRequestJSON(this.ssoToken, this.superPNR);
		return net.sf.json.JSONObject.fromObject(corpConfigService.getRequiredData(request, new JSONObject()).toString());
	}
	
	private JSONObject createRequestJSON(String ssoToken, String superPNR) {
		JSONObject request = new JSONObject();
		try{
			request.put("ssoToken", ssoToken);
			if(StringUtils.isNotEmpty(superPNR))
				request.put("superPnr", superPNR);
			return request;
		} catch(Exception ex) {
			logger.error("Error while creating request for CorporateConfigService", ex);
		}
		return request;
	}

}
