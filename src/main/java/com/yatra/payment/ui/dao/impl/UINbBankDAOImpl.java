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
import com.yatra.payment.ui.dao.bean.UINbBank;
import com.yatra.payment.ui.dao.sql.PaymentUISql;

@Component("netBanksDao")
public class UINbBankDAOImpl implements PaymentUIGenericDAO<UINbBank> {

	@Autowired
	private SimpleJdbcTemplate jdbcTemplate;

	private static Logger logger = Logger.getLogger(UINbBankDAOImpl.class);

	@Override
	public List<UINbBank> getAll() throws Exception {
		List<UINbBank> nbBanksList = null;

		ParameterizedBeanPropertyRowMapper<UINbBank> netBankMapper = new ParameterizedBeanPropertyRowMapper<UINbBank>() {
			public UINbBank mapRow(ResultSet rs, int rowNum) throws SQLException {
				UINbBank bankDO = new UINbBank();
				bankDO.setBankCode(rs.getString("bank_code"));
				bankDO.setDisplayName(rs.getString("display_name"));
				bankDO.setPreferred(rs.getBoolean("preferred"));
				bankDO.setImageURL(rs.getString("image_url"));
				bankDO.setStatus(rs.getString("status"));
				bankDO.setIframeEnabled(rs.getString("iframe_enabled"));
				bankDO.setPriority(rs.getInt("priority"));
				return bankDO;
			}
		};

		try {
			logger.debug("Fetching banks from ui_nb_banks.");
			nbBanksList = jdbcTemplate.query(PaymentUISql.SELECT_ALL_NET_BANKS, netBankMapper);
		} catch (Exception e) {
			logger.error("Exception occurred while fetching from ui_nb_banks." , e);
			throw e;
		}

		return nbBanksList;
	}

	@Override
	public List<UINbBank> getList(UINbBank t) throws Exception {
		return null;
	}

	@Override
	public UINbBank get(UINbBank t) throws Exception {
		return null;
	}

}
