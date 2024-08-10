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
import com.yatra.payment.ui.dao.bean.UICardTypesParamMapping;
import com.yatra.payment.ui.dao.sql.PaymentUISql;
@Component("cardTypeParamMappingDao")
public class UICardTypesParamMappingDAOImpl implements PaymentUIGenericDAO<UICardTypesParamMapping> {

	@Autowired
	private SimpleJdbcTemplate jdbcTemplate;

	private static Logger logger = Logger.getLogger(UICardTypesParamMappingDAOImpl.class);

	@Override
	public UICardTypesParamMapping get(UICardTypesParamMapping uICardTypesParamMapping) throws Exception {
		StringBuilder query = new StringBuilder(PaymentUISql.SELECT_PARAM_VALUE);
		Object[] objs = new Object[3];

		int i = 0;

		if (uICardTypesParamMapping.getPaymentOption() != null) {
			query = query.append(" where payment_option = ? ");
			objs[i] = uICardTypesParamMapping.getPaymentOption();
			i++;
		}

		if (uICardTypesParamMapping.getParamKey() != null) {
			if (i > 0)
				query = query.append(" and param_key = ? ");
			else
				query = query.append(" where param_key = ? ");
			objs[i] = uICardTypesParamMapping.getParamKey();
			i++;
		}

		if (uICardTypesParamMapping.getCardType() != null) {
			if (i > 0)
				query = query.append(" and card_type = ? ");
			else
				query = query.append(" where card_type = ? ");
			objs[i] = uICardTypesParamMapping.getCardType();
			i++;
		}

		query = query.append(" limit 1 ");

		List<Map<String, Object>> paramList = null;

		try {
			logger.debug("Fetching parameter value from ui_cardtypes_params_mapping. payment_option = " + uICardTypesParamMapping.getPaymentOption() + ", param_key="
					+ uICardTypesParamMapping.getParamKey() + " card_type = " + uICardTypesParamMapping.getCardType());
			paramList = jdbcTemplate.queryForList(query.toString(), objs);
		} catch (Exception e) {
			logger.error("Exception occurred while fetching parameter value from ui_cardtypes_params_mapping_central." , e);
			throw e;
		}

		UICardTypesParamMapping uIParamMapping = new UICardTypesParamMapping();
		if (paramList != null && !paramList.isEmpty()) {
			Map<String, Object> resultMap = paramList.get(0);
			if (resultMap != null && !resultMap.isEmpty()) {
				uIParamMapping.setParamValue((String) resultMap.get("param_value"));
			}
		}

		return uIParamMapping;
	}

	@Override
	public List<UICardTypesParamMapping> getAll() throws Exception {
		logger.debug("UICardTypesParamMappingDAO :: getAllUICardTypesParamMappingCentral() starts here ");

		ParameterizedBeanPropertyRowMapper<UICardTypesParamMapping> cardTypesParamMappingMapper = new ParameterizedBeanPropertyRowMapper<UICardTypesParamMapping>() {
			public UICardTypesParamMapping mapRow(ResultSet rs, int rowNum) throws SQLException {
				UICardTypesParamMapping cardTypesParamMapping = new UICardTypesParamMapping();
				cardTypesParamMapping.setCardType(rs.getString("card_type"));
				cardTypesParamMapping.setParamKey(rs.getString("param_key"));
				cardTypesParamMapping.setParamValue(rs.getString("param_value"));
				cardTypesParamMapping.setPaymentOption(rs.getString("payment_option"));
				return cardTypesParamMapping;
			}
		};

		List<UICardTypesParamMapping> cardTypesParamMappingList = null;
		try {
			logger.debug("Fetching information from merchant_master.");
			cardTypesParamMappingList = jdbcTemplate.query(PaymentUISql.SELECT_CARD_TYPE_PARAMS_MAPPING, cardTypesParamMappingMapper);
		} catch (Exception e) {
			logger.error("Exception occurred while fetching from merchant_master." , e);
			throw new Exception(e);
		}
		return cardTypesParamMappingList;
	}

	@Override
	public List<UICardTypesParamMapping> getList(UICardTypesParamMapping t) throws Exception {
		return null;
	}

}
