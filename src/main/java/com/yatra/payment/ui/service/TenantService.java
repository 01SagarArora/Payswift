package com.yatra.payment.ui.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.ui.dao.ProductMasterDAO;

@Service("tenantService")
public class TenantService {
	
	private static Logger logger = Logger.getLogger(TenantService.class);
	
	@Autowired private ProductMasterDAO productMasterDAO;
	
	private Map<String, Integer> tenanIdMap = new HashMap<String, Integer>();
	private Map<String, Boolean> multiPayEnabledProductMap = new ConcurrentHashMap<>();
	private Map<String, String> gvProductCodeMap = new HashMap<String, String>();
	
	@PostConstruct
	public void populateTenantIdMap() {
		try {
			// Get all product details from DB.
			List<Map<String, Object>> resultMapList = productMasterDAO.getAllProductsDetails();
			
			// If resultMapList is not null/empty, populate tenanIdMap
			if(resultMapList != null && !resultMapList.isEmpty()) {
				
				for(Map<String, Object> resultMap : resultMapList) {

					// Get merchantCode, productCode and tenantId
					String merchantCode =  (String) resultMap.get("merchant_code");
					String productCode = (String) resultMap.get("Key");
					Integer tenantId = (Integer) resultMap.get("tenant_id");
					Integer isMultiPayEnabled = (Integer) resultMap.get("multi_pay_flow_enabled");
					boolean multiPayEnabled = isMultiPayEnabled.equals(1);
					String gvProductCode = (String) resultMap.get("gv_product_code");

					// Populate tenanIdMap only when neither of the three values is null - merchantCode, productCode and tenantId
					if(StringUtils.isNotBlank(merchantCode) && StringUtils.isNotBlank(productCode) && tenantId != null && tenantId > 0) {
						String key = getTenantMapKey(merchantCode, productCode);
						tenanIdMap.put(key, tenantId);
					}
					multiPayEnabledProductMap.put(productCode, multiPayEnabled);
					gvProductCodeMap.put(productCode, StringUtils.isEmpty(gvProductCode) ? "" : gvProductCode);
				}
			}			
		} catch (Exception e) {
			logger.error("Exception occurred while populating tenantIdMap on postConstruct", e);
		}
		
	}
	
	public Long getTenantId(String merchantCode, String productCode) {
		String key = getTenantMapKey(merchantCode, productCode);
		Integer tenantId = tenanIdMap.get(key);
		return tenantId == null ? null : Long.valueOf(tenantId);
	}
	
	private String getTenantMapKey(String merchantCode, String productCode) {
		return merchantCode + "_" + productCode;
	}

	Boolean isProductMultiPayFlowEnabled(String productCode) {
		Boolean aBoolean = multiPayEnabledProductMap.get(productCode);
		return aBoolean != null && aBoolean;
	}
	
	public String getGVProductCode(String productCode) {
		return gvProductCodeMap.get(productCode);
	}
}
