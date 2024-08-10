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
import com.yatra.payment.ui.dao.bean.UIPayopSuboptionMapping;
import com.yatra.payment.ui.dao.sql.PaymentUISql;


@Component("payopSuboptionMappingDAO")
public class UIPayopSuboptionMappingDAOImpl implements PaymentUIGenericDAO<UIPayopSuboptionMapping> {

	@Autowired	private SimpleJdbcTemplate jdbcTemplate;

	private static Logger logger = Logger.getLogger(UIPayopSubTypesMappingDAOImpl.class);

	@Override
	public List<UIPayopSuboptionMapping> getAll() throws Exception {
		ParameterizedBeanPropertyRowMapper<UIPayopSuboptionMapping> payopSuboptionMapper = new ParameterizedBeanPropertyRowMapper<UIPayopSuboptionMapping>() {
			public UIPayopSuboptionMapping mapRow(ResultSet rs, int rowNum) throws SQLException {
				UIPayopSuboptionMapping uiPayopSuboptionMapping = new UIPayopSuboptionMapping();
				uiPayopSuboptionMapping.setSubOption(rs.getString("sub_option"));
				uiPayopSuboptionMapping.setPaymentOption(rs.getString("payment_option"));
				uiPayopSuboptionMapping.setDisplayName(rs.getString("display_name"));
				uiPayopSuboptionMapping.setImageURL(rs.getString("image_url"));
				uiPayopSuboptionMapping.setStatus(rs.getString("status"));
				uiPayopSuboptionMapping.setMerchantCode(rs.getString("merchant_code"));
				uiPayopSuboptionMapping.setProductCode(rs.getString("product_code"));
				uiPayopSuboptionMapping.setPriority(rs.getInt("priority"));
				return uiPayopSuboptionMapping;
			}
		};

		List<UIPayopSuboptionMapping> uiPayopSuboptionMapping = null;
		try {
			logger.debug("Fetching information from ui_payop_suboption_mapping ");
			uiPayopSuboptionMapping = jdbcTemplate.query(PaymentUISql.SELECT_ALL_SUBOPTION_DETAILS, payopSuboptionMapper);
			return uiPayopSuboptionMapping;
		} catch (Exception e) {
			logger.error("Exception occurred in getCashCardList() while fetching from ui_payop_suboption_mapping." , e);
			throw e;
		}
	}

	@Override
	public List<UIPayopSuboptionMapping> getList(UIPayopSuboptionMapping t)	throws Exception {
		return null;
	}

	@Override
	public UIPayopSuboptionMapping get(UIPayopSuboptionMapping t) throws Exception {
		return null;
	}

}
