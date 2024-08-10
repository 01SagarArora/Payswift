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
import com.yatra.payment.ui.dao.bean.UIPayopMessageMapping;
import com.yatra.payment.ui.dao.sql.PaymentUISql;
@Component("payopMessageMappingDao")
public class UIPayopMessageMappingDAOImpl implements PaymentUIGenericDAO<UIPayopMessageMapping> {

	@Autowired
	private SimpleJdbcTemplate jdbcTemplate;

	private static Logger logger = Logger.getLogger(UIPayopMessageMappingDAOImpl.class);

	@Override
	public List<UIPayopMessageMapping> getAll() throws Exception {
		ParameterizedBeanPropertyRowMapper<UIPayopMessageMapping> messagePayOptionMapper = new ParameterizedBeanPropertyRowMapper<UIPayopMessageMapping>() {
			public UIPayopMessageMapping mapRow(ResultSet rs, int rowNum) throws SQLException {

				UIPayopMessageMapping messageDO = new UIPayopMessageMapping();
				messageDO.setMessage(rs.getString("message"));
				messageDO.setMessageType(rs.getString("messageType"));
				messageDO.setStatus(rs.getString("status"));
				messageDO.setPaymentOption(rs.getString("payment_option"));
				return messageDO;
			}
		};

		List<UIPayopMessageMapping> messageList = null;
		try {
			logger.debug("Fetching information from ui_payop_message_mapping.");
			messageList = jdbcTemplate.query(PaymentUISql.SELECT_ALL_PAYMENT_OPTION_MESSAGES, messagePayOptionMapper);
		} catch (Exception e) {
			logger.error("Exception occurred in getEMIBanksList() while fetching from ui_payop_message_mapping." , e);
			throw e;
		}
		return messageList;
	}

	@Override
	public List<UIPayopMessageMapping> getList(UIPayopMessageMapping payopMessageMapping) throws Exception {

		ParameterizedBeanPropertyRowMapper<UIPayopMessageMapping> messagePayOptionMapper = new ParameterizedBeanPropertyRowMapper<UIPayopMessageMapping>() {
			public UIPayopMessageMapping mapRow(ResultSet rs, int rowNum) throws SQLException {

				UIPayopMessageMapping messageDO = new UIPayopMessageMapping();
				messageDO.setMessage(rs.getString("message"));
				messageDO.setMessageType(rs.getString("messageType"));
				messageDO.setStatus(rs.getString("status"));
				return messageDO;
			}
		};

		List<UIPayopMessageMapping> messageList = null;
		try {
			logger.debug("Fetching information from ui_payop_message_mapping.");
			messageList = jdbcTemplate.query(PaymentUISql.SELECT_PAYMENT_OPTION_MESSAGES, messagePayOptionMapper, payopMessageMapping.getPaymentOption());
		} catch (Exception e) {
			logger.error("Exception occurred in getEMIBanksList() while fetching from ui_payop_message_mapping." , e);
			throw e;
		}
		return messageList;
	}

	@Override
	public UIPayopMessageMapping get(UIPayopMessageMapping t) throws Exception {
		return null;
	}

}
