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
import com.yatra.payment.ui.dao.bean.UIProductBanksMapping;
import com.yatra.payment.ui.dao.sql.PaymentUISql;
@Component("productBanksMappingDao")
public class UIProductBanksMappingDAOImpl implements PaymentUIGenericDAO<UIProductBanksMapping> {

	@Autowired
	private SimpleJdbcTemplate jdbcTemplate;

	private static Logger logger = Logger.getLogger(UIProductBanksMappingDAOImpl.class);

	@Override
	public List<UIProductBanksMapping> getAll() throws Exception {
		ParameterizedBeanPropertyRowMapper<UIProductBanksMapping> productsBanksMapper = new ParameterizedBeanPropertyRowMapper<UIProductBanksMapping>() {
			public UIProductBanksMapping mapRow(ResultSet rs, int rowNum) throws SQLException {
				UIProductBanksMapping banksMapping = new UIProductBanksMapping();
				banksMapping.setBankCode(rs.getString("bank_code"));
				banksMapping.setStatus(rs.getString("status"));
				banksMapping.setMerchantCode(rs.getString("merchant_code"));
				banksMapping.setProductCode(rs.getString("product_code"));
				return banksMapping;
			}
		};

		try {
			logger.debug("Fetching information from ui_product_banks_mapping.");
			List<UIProductBanksMapping> uIProductBanksMapping = jdbcTemplate.query(PaymentUISql.SELECT_ALL_PRODUCT_BANKS, productsBanksMapper);
			return uIProductBanksMapping;
		} catch (Exception e) {
			logger.error("Exception occurred while fetching from ui_product_banks_mapping." , e);
			throw e;
		}
	}

	@Override
	// Fetch Banks for a Payment Option using table 'ui_product_banks_mapping'
	public List<UIProductBanksMapping> getList(UIProductBanksMapping uiProductBanksMapping) throws Exception {
		ParameterizedBeanPropertyRowMapper<UIProductBanksMapping> productsBanksMapper = new ParameterizedBeanPropertyRowMapper<UIProductBanksMapping>() {
			public UIProductBanksMapping mapRow(ResultSet rs, int rowNum) throws SQLException {
				UIProductBanksMapping banksMapping = new UIProductBanksMapping();
				banksMapping.setBankCode(rs.getString("bank_code"));
				banksMapping.setStatus(rs.getString("status"));
				return banksMapping;
			}
		};

		Object[] objs = new Object[] { uiProductBanksMapping.getMerchantCode(), uiProductBanksMapping.getProductCode() };
		try {
			logger.debug("Fetching information from ui_product_banks_mapping.");
			List<UIProductBanksMapping> uIProductBanksMapping = jdbcTemplate.query(PaymentUISql.SELECT_PRODUCT_BANKS, productsBanksMapper, objs);
			return uIProductBanksMapping;
		} catch (Exception e) {
			logger.error("Exception occurred while fetching from ui_product_banks_mapping." , e);
			throw e;
		}
	}

	@Override
	public UIProductBanksMapping get(UIProductBanksMapping t) throws Exception {
		return null;
	}

}
