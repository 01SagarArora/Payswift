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
import com.yatra.payment.ui.dao.bean.UIQBCardBrandMaster;
import com.yatra.payment.ui.dao.sql.PaymentUISql;

@Component("qbCardBrandDao")
public class UIQBCardBrandMasterDAOImpl implements PaymentUIGenericDAO<UIQBCardBrandMaster> {

	@Autowired
	private SimpleJdbcTemplate jdbcTemplate;

	private static Logger logger = Logger.getLogger(UIQBCardBrandMasterDAOImpl.class);

	@Override
	public List<UIQBCardBrandMaster> getAll() throws Exception {
		ParameterizedBeanPropertyRowMapper<UIQBCardBrandMaster> qbCardBrandMapper = new ParameterizedBeanPropertyRowMapper<UIQBCardBrandMaster>() {
			public UIQBCardBrandMaster mapRow(ResultSet rs, int rowNum) throws SQLException {
				UIQBCardBrandMaster uiqbCardBrandMaster = new UIQBCardBrandMaster();
				uiqbCardBrandMaster.setName(rs.getString("name"));
				uiqbCardBrandMaster.setCode(rs.getString("code"));
				uiqbCardBrandMaster.setImageUrl(rs.getString("image_url"));
				uiqbCardBrandMaster.setCvvLength(rs.getString("cvv_length"));
				return uiqbCardBrandMaster;
			}
		};

		try {
			logger.debug("Fetching information from ui_qb_card_brand_master.");
			List<UIQBCardBrandMaster> qbCardBrandList = jdbcTemplate.query(PaymentUISql.SELECT_ALL_QB_CARD_BRANDS, qbCardBrandMapper);
			return qbCardBrandList;
		} catch (Exception e) {
			logger.error("Exception occurred in populateQBCardBrandMap() while fetching from ui_qb_card_brand_master." , e);
			throw e;
		}
	}

	@Override
	public List<UIQBCardBrandMaster> getList(UIQBCardBrandMaster t) throws Exception {
		return null;
	}

	@Override
	public UIQBCardBrandMaster get(UIQBCardBrandMaster t) throws Exception {
		return null;
	}

}
