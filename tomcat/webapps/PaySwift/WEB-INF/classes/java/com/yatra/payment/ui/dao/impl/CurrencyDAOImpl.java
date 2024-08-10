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
import com.yatra.payment.ui.dao.bean.Currency;
import com.yatra.payment.ui.dao.sql.PaymentUISql;

@Component("currencyDAO")
public class CurrencyDAOImpl implements PaymentUIGenericDAO<Currency>{

    @Autowired
    private SimpleJdbcTemplate jdbcTemplate;

    private static Logger logger = Logger.getLogger(CurrencyDAOImpl.class);

	@Override
	public List<Currency> getAll() throws Exception {
		try {
			logger.debug("Fetching information from yp_payment_currency_master");
			ParameterizedBeanPropertyRowMapper<Currency> currencyMapper = new ParameterizedBeanPropertyRowMapper<Currency>() {
				public Currency mapRow(ResultSet rs, int rowNum) throws SQLException {
					Currency currency = new Currency();
					currency.setCode(rs.getString("code"));
					currency.setName(rs.getString("name"));
					currency.setUnicode(rs.getString("unicode"));
					return currency;
				}
			};
			List<Currency> currencyList = jdbcTemplate.query(PaymentUISql.SELECT_ALL_ENABLED_CURRENCIES, currencyMapper);
			return currencyList;
		} catch (Exception e) {
			logger.error("Exception occurred in populateCardTypesMap() while fetching from yp_payment_currency_master." , e);
			throw e;
		}
	}

	@Override
	public List<Currency> getList(Currency t) throws Exception {
		return null;
	}

	@Override
	public Currency get(Currency t) throws Exception {
		return null;
	}
}
