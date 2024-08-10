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
import com.yatra.payment.ui.dao.bean.UIBankDownMessageMapping;
import com.yatra.payment.ui.dao.bean.UIPayopMessageMapping;
import com.yatra.payment.ui.dao.bean.UIProductBanksMapping;
import com.yatra.payment.ui.dao.sql.PaymentUISql;

@Component("bankDownMessageMappingDao")
public class UIBankDownMessageMappingDAOImpl implements PaymentUIGenericDAO<UIBankDownMessageMapping> {

	@Autowired
	private SimpleJdbcTemplate jdbcTemplate;

	private static Logger logger = Logger.getLogger(UIBankDownMessageMappingDAOImpl.class);
	
	@Override
	public List<UIBankDownMessageMapping> getAll() throws Exception {
		ParameterizedBeanPropertyRowMapper<UIBankDownMessageMapping> messageBankDownMapper = new ParameterizedBeanPropertyRowMapper<UIBankDownMessageMapping>() {
			public UIBankDownMessageMapping mapRow(ResultSet rs, int rowNum) throws SQLException {

				UIBankDownMessageMapping messageDO = new UIBankDownMessageMapping();
				messageDO.setName(rs.getString("name"));
				messageDO.setCode(rs.getString("code"));
				messageDO.setMessage(rs.getString("message"));
				messageDO.setPaymentStatus(rs.getString("payment_status"));
				messageDO.setMessageStatus(rs.getString("message_status"));
				return messageDO;
			}
		};

		List<UIBankDownMessageMapping> messageList = null;
		try {
			logger.debug("Fetching information from ui_payop_message_mapping.");
			messageList = jdbcTemplate.query(PaymentUISql.SELECT_ALL_BANK_DOWN_MESSAGES, messageBankDownMapper);
		} catch (Exception e) {
			logger.error("Exception occurred in getEMIBanksList() while fetching from ui_payop_message_mapping." , e);
			throw e;
		}
		return messageList;
	}

	@Override
	public List<UIBankDownMessageMapping> getList(UIBankDownMessageMapping t)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UIBankDownMessageMapping get(UIBankDownMessageMapping t)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
