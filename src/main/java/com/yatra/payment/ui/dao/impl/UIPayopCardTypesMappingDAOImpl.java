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
import com.yatra.payment.ui.dao.bean.UIPayopCardTypesMapping;
import com.yatra.payment.ui.dao.sql.PaymentUISql;
@Component("payopCardTypesMappingDao")
public class UIPayopCardTypesMappingDAOImpl implements PaymentUIGenericDAO<UIPayopCardTypesMapping> {

	@Autowired
	private SimpleJdbcTemplate jdbcTemplate;

	private static Logger logger = Logger.getLogger(UIPayopCardTypesMappingDAOImpl.class);

	@Override
	public List<UIPayopCardTypesMapping> getAll() throws Exception {
		ParameterizedBeanPropertyRowMapper<UIPayopCardTypesMapping> cardPayOptionMapper = new ParameterizedBeanPropertyRowMapper<UIPayopCardTypesMapping>() {
			public UIPayopCardTypesMapping mapRow(ResultSet rs, int rowNum) throws SQLException {
				UIPayopCardTypesMapping uIPayopCardTypesMapping = new UIPayopCardTypesMapping();
				uIPayopCardTypesMapping.setCardType(rs.getString("card_type"));
				uIPayopCardTypesMapping.setStatus(rs.getString("status"));
				uIPayopCardTypesMapping.setPaymentOption(rs.getString("payment_option"));
				return uIPayopCardTypesMapping;
			}
		};

		try {
			logger.debug("Fetching information from ui_payop_cardtypes_mapping.");
			List<UIPayopCardTypesMapping> cardTypeForPaymentOption = jdbcTemplate.query(PaymentUISql.SELECT_ALL_PAYOP_CARD_TYPES, cardPayOptionMapper);
			return cardTypeForPaymentOption;
		} catch (Exception e) {
			logger.error("Exception occurred while fetching from ui_payop_cardtypes_mapping." , e);
			throw e;
		}
	}

	@Override
	public List<UIPayopCardTypesMapping> getList(UIPayopCardTypesMapping uiPayopCardTypesMapping) throws Exception {
		ParameterizedBeanPropertyRowMapper<UIPayopCardTypesMapping> cardPayOptionMapper = new ParameterizedBeanPropertyRowMapper<UIPayopCardTypesMapping>() {
			public UIPayopCardTypesMapping mapRow(ResultSet rs, int rowNum) throws SQLException {
				UIPayopCardTypesMapping uIPayopCardTypesMapping = new UIPayopCardTypesMapping();
				uIPayopCardTypesMapping.setCardType(rs.getString("card_type"));
				uIPayopCardTypesMapping.setStatus(rs.getString("status"));
				uIPayopCardTypesMapping.setPaymentOption(rs.getString("payment_option"));
				return uIPayopCardTypesMapping;
			}
		};

		try {
			logger.debug("Fetching information from ui_payop_cardtypes_mapping.  paymentOption = " + uiPayopCardTypesMapping.getPaymentOption());
			List<UIPayopCardTypesMapping> cardTypeForPaymentOption = jdbcTemplate.query(PaymentUISql.SELECT_CARD_TYPES, cardPayOptionMapper, uiPayopCardTypesMapping.getPaymentOption());
			return cardTypeForPaymentOption;
		} catch (Exception e) {
			logger.error("Exception occurred while fetching from ui_payop_cardtypes_mapping." , e);
			throw e;
		}
	}

	@Override
	public UIPayopCardTypesMapping get(UIPayopCardTypesMapping t) throws Exception {
		return null;
	}

}
