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
import com.yatra.payment.ui.dao.bean.UIPayopSubTypesMapping;
import com.yatra.payment.ui.dao.sql.PaymentUISql;

@Component("payopSubTypesMappingDAO")
public class UIPayopSubTypesMappingDAOImpl implements PaymentUIGenericDAO<UIPayopSubTypesMapping> {

	@Autowired	private SimpleJdbcTemplate jdbcTemplate;

	private static Logger logger = Logger.getLogger(UIPayopSubTypesMappingDAOImpl.class);

	@Override
	public List<UIPayopSubTypesMapping> getAll() throws Exception {
		ParameterizedBeanPropertyRowMapper<UIPayopSubTypesMapping> payopSubTypeMapper = new ParameterizedBeanPropertyRowMapper<UIPayopSubTypesMapping>() {
			public UIPayopSubTypesMapping mapRow(ResultSet rs, int rowNum) throws SQLException {
				UIPayopSubTypesMapping uIPayopSubTypesMapping = new UIPayopSubTypesMapping();
				uIPayopSubTypesMapping.setOptionCode(rs.getString("option_code"));
				uIPayopSubTypesMapping.setPaymentOption(rs.getString("payment_option"));
				uIPayopSubTypesMapping.setDisplayName(rs.getString("display_name"));
				uIPayopSubTypesMapping.setImageURL(rs.getString("image_url"));
				uIPayopSubTypesMapping.setStatus(rs.getString("status"));
				return uIPayopSubTypesMapping;
			}
		};

		List<UIPayopSubTypesMapping> uiPayopSubTypesMappings = null;
		try {
			logger.debug("Fetching information from ui_payop_subtypes_mapping ");
			uiPayopSubTypesMappings = jdbcTemplate.query(PaymentUISql.SELECT_ALL_NON_BANKING_DETAILS, payopSubTypeMapper);
			return uiPayopSubTypesMappings;
		} catch (Exception e) {
			logger.error("Exception occurred in getCashCardList() while fetching from ui_payop_subtypes_mapping." , e);
			throw e;
		}
	}

	@Override
	public List<UIPayopSubTypesMapping> getList(UIPayopSubTypesMapping uiPayopSubTypesMapping) throws Exception {
		ParameterizedBeanPropertyRowMapper<UIPayopSubTypesMapping> payopSubTypeMapper = new ParameterizedBeanPropertyRowMapper<UIPayopSubTypesMapping>() {
			public UIPayopSubTypesMapping mapRow(ResultSet rs, int rowNum) throws SQLException {
				UIPayopSubTypesMapping uIPayopSubTypesMapping = new UIPayopSubTypesMapping();
				uIPayopSubTypesMapping.setOptionCode(rs.getString("option_code"));
				uIPayopSubTypesMapping.setDisplayName(rs.getString("display_name"));
				uIPayopSubTypesMapping.setImageURL(rs.getString("image_url"));
				uIPayopSubTypesMapping.setStatus(rs.getString("status"));
				return uIPayopSubTypesMapping;
			}
		};

		List<UIPayopSubTypesMapping> uiPayopSubTypesMappings = null;
		try {
			logger.debug("Fetching information from ui_payop_subtypes_mapping. paymentOption = " + uiPayopSubTypesMapping.getPaymentOption());
			uiPayopSubTypesMappings = jdbcTemplate.query(PaymentUISql.SELECT_NON_BANKING_DETAILS, payopSubTypeMapper, uiPayopSubTypesMapping.getPaymentOption());
			return uiPayopSubTypesMappings;
		} catch (Exception e) {
			logger.error("Exception occurred in getCashCardList() while fetching from ui_payop_subtypes_mapping." , e);
			throw e;
		}
	}

	@Override
	public UIPayopSubTypesMapping get(UIPayopSubTypesMapping t) throws Exception {
		return null;
	}

}
