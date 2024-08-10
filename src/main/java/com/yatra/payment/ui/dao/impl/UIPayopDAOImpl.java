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
import com.yatra.payment.ui.dao.bean.UIPayop;
import com.yatra.payment.ui.dao.sql.PaymentUISql;
@Component("paymentOptionDao")
public class UIPayopDAOImpl implements PaymentUIGenericDAO<UIPayop> {

	@Autowired
	private SimpleJdbcTemplate jdbcTemplate;

	private static Logger logger = Logger.getLogger(UIPayopDAOImpl.class);

	@Override
	public List<UIPayop> getAll() throws Exception {
		ParameterizedBeanPropertyRowMapper<UIPayop> payOptionMapper = new ParameterizedBeanPropertyRowMapper<UIPayop>() {
			public UIPayop mapRow(ResultSet rs, int rowNum) throws SQLException {
				UIPayop uiPayop = new UIPayop();
				uiPayop.setCode(rs.getString("code"));
				uiPayop.setDisplayName(rs.getString("display_name"));
				uiPayop.setPriority(new Integer(rs.getString("priority")));
				uiPayop.setIframeEnabled(rs.getString("iframe_enabled"));
				return uiPayop;
			}
		};

		try {
			logger.debug("Fetching information from ui_payop.");
			List<UIPayop> uiPayopList = jdbcTemplate.query(PaymentUISql.SELCT_ALL_PAY_OPTS, payOptionMapper);
			return uiPayopList;
		} catch (Exception e) {
			logger.error("Exception occurred in populatePaymentOptionMap() while fetching from payment_options_master." , e);
			throw e;
		}
	}

	@Override
	public List<UIPayop> getList(UIPayop t) throws Exception {
		return null;
	}

	@Override
	public UIPayop get(UIPayop t) throws Exception {
		return null;
	}

}
