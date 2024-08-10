package com.yatra.payment.ui.service.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.ui.dao.impl.PayswiftStagesDAOImpl;

@Service
public class GiftVoucherValidationService {
	private static Logger logger = Logger.getLogger(GiftVoucherValidationService.class);
	
	@Autowired
	private PayswiftStagesDAOImpl payswiftStagesDAOImpl;
	
	private static JSONObject vouchersConfig = new JSONObject();
	
	@PostConstruct
	public void constructVouchersConfig(){
		logger.info("Entering constructVouchersConfig()");
		List<Map<String,Object>> voucherDetailsMap =  payswiftStagesDAOImpl.getAllVoucherConfigDetails();
		if(voucherDetailsMap.size()>0) {
			try {
				for(Map<String,Object> voucherDetailMap : voucherDetailsMap) {
					JSONObject configJson = new JSONObject();
					String key = voucherDetailMap.get("cpg_bin").toString();
					configJson.put("LOB", voucherDetailMap.getOrDefault("lob", "ALL"));
					configJson.put("isCumulative", voucherDetailMap.getOrDefault("is_cumulative", "true"));
					configJson.put("isPromoSupported", voucherDetailMap.getOrDefault("is_promo_supported", "false"));
					vouchersConfig.put(key, configJson);
				}
			} catch (JSONException e) {
				logger.info("Error creating vouchersConfig JSON", e);
			}
		}
		logger.info("Exiting constructVouchersConfig()");
	}
	
	public static JSONObject getVouchersConfig() {
		return vouchersConfig;
	}
	
	public int getValidateErrorCode(String superPnr, JSONArray voucherJsonArray, String product) {
		logger.info("Validating voucher array " + voucherJsonArray + "for superPnr : " + superPnr );
		int status = 1;
		try {
			for(int i = 0; i< voucherJsonArray.length(); i++) {
				JSONObject voucherJson = new JSONObject(voucherJsonArray.getString(i));
				String voucher = voucherJson.getString("code").substring(0,9);
				if(vouchersConfig.has(voucher)) {
					JSONObject voucherConf = vouchersConfig.getJSONObject(voucher);
					String LOBs = voucherConf.getString("LOB");
					if(!(LOBs.equalsIgnoreCase("ALL") || Arrays.asList(LOBs.split(",")).contains(product))) {
						status = 2;
						break;
					}
					else if(!voucherConf.getBoolean("isCumulative") && voucherJsonArray.length()>1) {
						status = 3;
						break;
					} else status = 0;
				} else {
					status = 0;
				}
			}
		}
		catch(Exception e) {
			logger.error("Error while validating voucher against vouchersConfig for superPnr : " + superPnr);
		}
		logger.info("Validation of vouchers for superPnr : " + superPnr + " is complete with status code : " + status );
		return status;
	}
	
}
