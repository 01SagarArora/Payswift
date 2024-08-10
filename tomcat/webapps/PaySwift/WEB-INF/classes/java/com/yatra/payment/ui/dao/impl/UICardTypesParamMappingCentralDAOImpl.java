package com.yatra.payment.ui.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Component;

import com.yatra.payment.ui.dao.PaymentUIGenericDAO;
import com.yatra.payment.ui.dao.bean.UICardTypesParamMappingCentral;
import com.yatra.payment.ui.dao.sql.PaymentUISql;

@Component("cardTypeParamMappingCentralDao")
public class UICardTypesParamMappingCentralDAOImpl implements PaymentUIGenericDAO<UICardTypesParamMappingCentral> {

	@Autowired
	private SimpleJdbcTemplate jdbcTemplate;

	private static Logger logger = Logger.getLogger(UICardTypesParamMappingCentralDAOImpl.class);

	@Override
	public UICardTypesParamMappingCentral get(UICardTypesParamMappingCentral uICardTypesParamMappingCentral) throws Exception {

		StringBuilder query = new StringBuilder(PaymentUISql.SELECT_CENTRAL_PARAMS_VALUE);
		Object[] objs = new Object[3];

		int i = 0;

		if (uICardTypesParamMappingCentral.getPaymentOption() != null) {
			query = query.append(" where payment_option = ? ");
			objs[i] = uICardTypesParamMappingCentral.getPaymentOption();
			i++;
		}

		if (uICardTypesParamMappingCentral.getParamKey() != null) {
			if (i > 0)
				query = query.append(" and param_key = ? ");
			else
				query = query.append(" where param_key = ? ");
			objs[i] = uICardTypesParamMappingCentral.getParamKey();
			i++;
		}

		if (uICardTypesParamMappingCentral.getCardType() != null) {
			if (i > 0)
				query = query.append(" and card_type = ? ");
			else
				query = query.append(" where card_type = ? ");
			objs[i] = uICardTypesParamMappingCentral.getCardType();
			i++;
		}

		query = query.append(" limit 1 ");

		List<Map<String, Object>> paramList = null;

		try {
			logger.debug("Fetching parameter value from ui_cardtypes_params_mapping_central. payment_option = " + uICardTypesParamMappingCentral.getPaymentOption() + ", param_key="
					+ uICardTypesParamMappingCentral.getParamKey() + " card_type = " + uICardTypesParamMappingCentral.getCardType());
			paramList = jdbcTemplate.queryForList(query.toString(), objs);
		} catch (Exception e) {
			logger.error("Exception occurred while fetching parameter value from ui_cardtypes_params_mapping_central." , e);
			throw e;
		}

		UICardTypesParamMappingCentral uIParamMapping = new UICardTypesParamMappingCentral();
		if (paramList != null && !paramList.isEmpty()) {
			Map<String, Object> resultMap = paramList.get(0);
			if (resultMap != null && !resultMap.isEmpty()) {
				uIParamMapping.setParamValue((String) resultMap.get("param_value"));
			}
		}

		return uIParamMapping;
	}

	@Override
	public List<UICardTypesParamMappingCentral> getAll() throws Exception {
		logger.debug("UICardTypesParamMappingCentralDAO :: getAllUICardTypesParamMappingCentral() starts here ");

		ParameterizedBeanPropertyRowMapper<UICardTypesParamMappingCentral> cardTypesParamMappingCentralMapper = new ParameterizedBeanPropertyRowMapper<UICardTypesParamMappingCentral>() {
			public UICardTypesParamMappingCentral mapRow(ResultSet rs, int rowNum) throws SQLException {
				UICardTypesParamMappingCentral cardTypesParamMappingCentral = new UICardTypesParamMappingCentral();
				cardTypesParamMappingCentral.setCardType(rs.getString("card_type"));
				cardTypesParamMappingCentral.setParamKey(rs.getString("param_key"));
				cardTypesParamMappingCentral.setParamValue(rs.getString("param_value"));
				cardTypesParamMappingCentral.setPaymentOption(rs.getString("payment_option"));
				return cardTypesParamMappingCentral;
			}
		};

		List<UICardTypesParamMappingCentral> cardTypesParamMappingCentralList = null;
		try {
			logger.debug("Fetching information from merchant_master.");
			cardTypesParamMappingCentralList = jdbcTemplate.query(PaymentUISql.SELECT_CARD_TYPE_PARAMS_MAPPING, cardTypesParamMappingCentralMapper);
		} catch (Exception e) {
			logger.error("Exception occurred while fetching from merchant_master." , e);
			throw new Exception(e);
		}
		return cardTypesParamMappingCentralList;
	}

	@Override
	public List<UICardTypesParamMappingCentral> getList(UICardTypesParamMappingCentral t) throws Exception {
		return null;
	}

}
