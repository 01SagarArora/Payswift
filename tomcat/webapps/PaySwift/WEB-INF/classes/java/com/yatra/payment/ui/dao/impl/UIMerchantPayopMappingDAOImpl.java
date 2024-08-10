package com.yatra.payment.ui.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Component;

import com.yatra.payment.ui.dao.PaymentUIGenericDAO;
import com.yatra.payment.ui.dao.bean.UIMerchantPayopMapping;
import com.yatra.payment.ui.dao.sql.PaymentUISql;
@Component("merchantPayopMappingDao")
public class UIMerchantPayopMappingDAOImpl implements PaymentUIGenericDAO<UIMerchantPayopMapping> {

	@Autowired
	private SimpleJdbcTemplate jdbcTemplate;

	private static Logger logger = Logger.getLogger(UIMerchantPayopMappingDAOImpl.class);

	@Override
	public List<UIMerchantPayopMapping> getAll() throws Exception {

		ParameterizedBeanPropertyRowMapper<UIMerchantPayopMapping> productPayOptionMapper = new ParameterizedBeanPropertyRowMapper<UIMerchantPayopMapping>() {
			public UIMerchantPayopMapping mapRow(ResultSet rs, int rowNum) throws SQLException {
				UIMerchantPayopMapping uIMerchantPayopMapping = new UIMerchantPayopMapping();
				uIMerchantPayopMapping.setPaymentOption(rs.getString("payment_option"));
				uIMerchantPayopMapping.setStatus(rs.getString("status"));
				uIMerchantPayopMapping.setMerchantCode(rs.getString("merchant_code"));
				uIMerchantPayopMapping.setProductCode(rs.getString("product_code"));
				uIMerchantPayopMapping.setId(rs.getInt("id"));
				uIMerchantPayopMapping.setNonMoto(rs.getBoolean("non_moto"));
				return uIMerchantPayopMapping;
			}
		};

		try {
			logger.debug("Fetching information from ui_merchant_payop_mapping.");
			return jdbcTemplate.query(PaymentUISql.SELECT_PAYMENT_OPTIONS_MAPPING, productPayOptionMapper);
			
		} catch (Exception e) {
			logger.error("Exception occurred in getUIPaymentOptions() while fetching from ui_merchant_payop_mapping." , e);
			throw e;
		}
	}

	@Override
	public List<UIMerchantPayopMapping> getList(UIMerchantPayopMapping uIMerchantPayopMapping) throws Exception {
		List<UIMerchantPayopMapping> uIMerchantPayopMappings = null;

		ParameterizedBeanPropertyRowMapper<UIMerchantPayopMapping> productPayOptionMapper = new ParameterizedBeanPropertyRowMapper<UIMerchantPayopMapping>() {
			public UIMerchantPayopMapping mapRow(ResultSet rs, int rowNum) throws SQLException {

				UIMerchantPayopMapping uIMerchantPayopMapping = new UIMerchantPayopMapping();
				uIMerchantPayopMapping.setPaymentOption(rs.getString("payment_option"));
				uIMerchantPayopMapping.setStatus(rs.getString("status"));
				uIMerchantPayopMapping.setNonMoto(rs.getBoolean("non_moto"));
				return uIMerchantPayopMapping;
			}
		};

		Object[] objs = new Object[] { uIMerchantPayopMapping.getMerchantCode(), uIMerchantPayopMapping.getProductCode() };

		try {
			logger.debug("Fetching information from ui_merchant_payop_mapping. merchant_code= " + uIMerchantPayopMapping.getMerchantCode() + ", product_code = "
					+ uIMerchantPayopMapping.getProductCode());
			uIMerchantPayopMappings = jdbcTemplate.query(PaymentUISql.SELECT_PRODUCT_PAY_OPTS, productPayOptionMapper, objs);
		} catch (Exception e) {
			logger.error("Exception occurred in getUIPaymentOptions() while fetching from ui_merchant_payop_mapping." , e);
			throw e;
		}

		return uIMerchantPayopMappings;
	}

	@Override
	public UIMerchantPayopMapping get(UIMerchantPayopMapping t) throws Exception {
		return null;
	}

}
