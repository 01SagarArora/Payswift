package com.yatra.payment.ui.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Service;

import com.yatra.payment.ui.dao.PaymentUIGenericDAO;
import com.yatra.payment.ui.dao.bean.UIMerchantCurrencyMapping;
import com.yatra.payment.ui.dao.sql.PaymentUISql;

@Service
public class UIMerchantCurrencyMappingDaoImpl implements PaymentUIGenericDAO<UIMerchantCurrencyMapping>{

	@Autowired
	private SimpleJdbcTemplate jdbcTemplate;

	private static Logger logger = Logger.getLogger(UIMerchantCurrencyMappingDaoImpl.class);
	
	@Override
	public List<UIMerchantCurrencyMapping> getAll() throws Exception {
		
		ParameterizedBeanPropertyRowMapper<UIMerchantCurrencyMapping> productPayOptionMapper = new ParameterizedBeanPropertyRowMapper<UIMerchantCurrencyMapping>() {
			public UIMerchantCurrencyMapping mapRow(ResultSet rs, int rowNum) throws SQLException {
				UIMerchantCurrencyMapping uIMerchantCurrencyMapping = new UIMerchantCurrencyMapping();
				uIMerchantCurrencyMapping.setId(rs.getInt("id"));
				uIMerchantCurrencyMapping.setMerchantCode(rs.getString("merchant_code"));
				uIMerchantCurrencyMapping.setProductCode(rs.getString("product_code"));
				uIMerchantCurrencyMapping.setCurrencyCode(rs.getString("currency_code"));
				uIMerchantCurrencyMapping.setDefault(rs.getBoolean("is_default"));
				uIMerchantCurrencyMapping.setEnabled(rs.getBoolean("is_enabled"));
				uIMerchantCurrencyMapping.setPriority(rs.getInt("priority"));
				return uIMerchantCurrencyMapping;
			}
		};

		try {
			logger.debug("Fetching information from ui_merchant_currency_mapping.");
			return jdbcTemplate.query(PaymentUISql.SELECT_ENABLED_CURRENCY_CONFIG_MAPPING, productPayOptionMapper);
			
		} catch (Exception e) {
			logger.error("Exception occurred in getUICurrencyConfigMappings() while fetching from ui_merchant_currency_mapping." , e);
			throw e;
		}
	}

	@Override
	public List<UIMerchantCurrencyMapping> getList(UIMerchantCurrencyMapping t) throws Exception {
		return null;
	}

	@Override
	public UIMerchantCurrencyMapping get(UIMerchantCurrencyMapping t) throws Exception {
		return null;
	}

}
