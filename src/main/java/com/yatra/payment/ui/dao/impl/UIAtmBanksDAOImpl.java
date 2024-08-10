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
import com.yatra.payment.ui.dao.bean.UIAtmBanks;
import com.yatra.payment.ui.dao.sql.PaymentUISql;

@Component("atmBanksDAO")
public class UIAtmBanksDAOImpl implements PaymentUIGenericDAO<UIAtmBanks> {

	@Autowired
	private SimpleJdbcTemplate jdbcTemplate;

	private static Logger logger = Logger.getLogger(UIQBCardBrandMasterDAOImpl.class);

	@Override
	// Fetch Banks for a Payment Option using table 'ui_atm_banks'
	public List<UIAtmBanks> getAll() throws Exception {
		ParameterizedBeanPropertyRowMapper<UIAtmBanks> atmBankMapper = new ParameterizedBeanPropertyRowMapper<UIAtmBanks>() {
			public UIAtmBanks mapRow(ResultSet rs, int rowNum) throws SQLException {
				UIAtmBanks bankDO = new UIAtmBanks();
				bankDO.setBankCode(rs.getString("bank_code"));
				bankDO.setDisplayName(rs.getString("display_name"));
				bankDO.setImageURL(rs.getString("image_url"));
				bankDO.setStatus(rs.getString("status"));
				return bankDO;
			}
		};

		try {
			logger.debug("Fetching banks from ui_atm_banks.");
			List<UIAtmBanks> banksList = jdbcTemplate.query(PaymentUISql.SELECT_ALL_ATM_BANKS, atmBankMapper);
			return banksList;
		} catch (Exception e) {
			logger.error("Exception occurred while fetching from banking_payment_options_mapping." , e);
			throw e;
		}
	}

	@Override
	public List<UIAtmBanks> getList(UIAtmBanks t) throws Exception {
		return null;
	}

	@Override
	public UIAtmBanks get(UIAtmBanks t) throws Exception {
		return null;
	}

}
