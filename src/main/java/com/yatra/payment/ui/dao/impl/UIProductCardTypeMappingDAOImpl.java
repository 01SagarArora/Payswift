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
import com.yatra.payment.ui.dao.bean.UIProductCardTypeMapping;
import com.yatra.payment.ui.dao.sql.PaymentUISql;
@Component("productCardTypeMappingDao")
public class UIProductCardTypeMappingDAOImpl implements PaymentUIGenericDAO<UIProductCardTypeMapping> {

	@Autowired
	private SimpleJdbcTemplate jdbcTemplate;

	private static Logger logger = Logger.getLogger(UIProductCardTypeMappingDAOImpl.class);

	@Override
	public List<UIProductCardTypeMapping> getAll() throws Exception {
		ParameterizedBeanPropertyRowMapper<UIProductCardTypeMapping> productsCardTypeMapper = new ParameterizedBeanPropertyRowMapper<UIProductCardTypeMapping>() {
			public UIProductCardTypeMapping mapRow(ResultSet rs, int rowNum) throws SQLException {
				UIProductCardTypeMapping uiProductCardTypeMapping = new UIProductCardTypeMapping();
				uiProductCardTypeMapping.setCardTypeCode(rs.getString("card_type_code"));
				uiProductCardTypeMapping.setMerchantCode(rs.getString("merchant_code"));
				uiProductCardTypeMapping.setProductCode(rs.getString("product_code"));
				uiProductCardTypeMapping.setStatus(rs.getString("status"));
				return uiProductCardTypeMapping;
			}
		};

		try {
			logger.debug("Fetching information from ui_product_card_type_mapping.");
			List<UIProductCardTypeMapping> cardTypeCodeList = jdbcTemplate.query(PaymentUISql.SELECT_ALL_PRODUCT_CARD_TYPES, productsCardTypeMapper);
			return cardTypeCodeList;
		} catch (Exception e) {
			logger.error("Exception occurred while fetching from ui_payop_cardtypes_mapping." , e);
			throw e;
		}
	}

	@Override
	public List<UIProductCardTypeMapping> getList(UIProductCardTypeMapping uiProductCardTypeMapping) throws Exception {
		Object[] objs = new Object[] { uiProductCardTypeMapping.getMerchantCode(), uiProductCardTypeMapping.getProductCode() };

		ParameterizedBeanPropertyRowMapper<UIProductCardTypeMapping> productsCardTypeMapper = new ParameterizedBeanPropertyRowMapper<UIProductCardTypeMapping>() {
			public UIProductCardTypeMapping mapRow(ResultSet rs, int rowNum) throws SQLException {
				UIProductCardTypeMapping uiProductCardTypeMapping = new UIProductCardTypeMapping();
				uiProductCardTypeMapping.setCardTypeCode(rs.getString("card_type_code"));
				return uiProductCardTypeMapping;
			}
		};

		try {
			logger.debug("Fetching information from ui_product_card_type_mapping.");
			List<UIProductCardTypeMapping> cardTypeCodeList = jdbcTemplate.query(PaymentUISql.SELECT_PRODUCT_CARD_TYPES, productsCardTypeMapper, objs);
			return cardTypeCodeList;
		} catch (Exception e) {
			logger.error("Exception occurred while fetching from ui_payop_cardtypes_mapping." , e);
			throw e;
		}
	}

	@Override
	public UIProductCardTypeMapping get(UIProductCardTypeMapping t) throws Exception {
		return null;
	}

}
