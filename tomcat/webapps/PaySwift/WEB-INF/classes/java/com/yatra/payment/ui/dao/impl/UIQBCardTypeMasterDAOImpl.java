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
import com.yatra.payment.ui.dao.bean.UIQBCardTypeMaster;
import com.yatra.payment.ui.dao.sql.PaymentUISql;

@Component("qbCardTypeDao")
public class UIQBCardTypeMasterDAOImpl implements PaymentUIGenericDAO<UIQBCardTypeMaster> {

	@Autowired
	private SimpleJdbcTemplate jdbcTemplate;

	private static Logger logger = Logger.getLogger(UIQBCardTypeMasterDAOImpl.class);

	@Override
	public List<UIQBCardTypeMaster> getAll() throws Exception {
		ParameterizedBeanPropertyRowMapper<UIQBCardTypeMaster> uiQBCardTypeMaster = new ParameterizedBeanPropertyRowMapper<UIQBCardTypeMaster>() {
			public UIQBCardTypeMaster mapRow(ResultSet rs, int rowNum) throws SQLException {
				UIQBCardTypeMaster uiQBCardType = new UIQBCardTypeMaster();
				uiQBCardType.setName(rs.getString("name"));
				uiQBCardType.setCode(rs.getString("code"));
				uiQBCardType.setCardTypeLabel(rs.getString("card_type_label"));
				uiQBCardType.setCvvLabel(rs.getString("cvv_label"));
				return uiQBCardType;
			}
		};

		try {
			logger.debug("Fetching information from ui_qb_card_type_master.");
			List<UIQBCardTypeMaster> qbCardTypeList = jdbcTemplate.query(PaymentUISql.SELECT_ALL_QB_CARD_TYPES, uiQBCardTypeMaster);
			return qbCardTypeList;
		} catch (Exception e) {
			logger.error("Exception occurred in populateQBCardTypeMap() while fetching from ui_qb_card_type_master." , e);
			throw e;
		}
	}

	@Override
	public List<UIQBCardTypeMaster> getList(UIQBCardTypeMaster t) throws Exception {
		return null;
	}

	@Override
	public UIQBCardTypeMaster get(UIQBCardTypeMaster t) throws Exception {
		return null;
	}
}
