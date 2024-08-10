package com.yatra.payment.ui.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Service;

import com.yatra.payment.ui.dao.PaymentUIGenericDAO;
import com.yatra.payment.ui.dao.bean.UIEmiBanks;
import com.yatra.payment.ui.dao.sql.PaymentUISql;

@Service("emiBanksDAO")
public class UIEmiBanksDAOImpl implements PaymentUIGenericDAO<UIEmiBanks> {

	@Autowired
	private SimpleJdbcTemplate jdbcTemplate;

	private static Logger logger = Logger.getLogger(UIEmiBanksDAOImpl.class);

	@Override
	public List<UIEmiBanks> getAll() throws Exception {
		List<UIEmiBanks> banksList = null;

		ParameterizedBeanPropertyRowMapper<UIEmiBanks> emiBankPayOptionMapper = new ParameterizedBeanPropertyRowMapper<UIEmiBanks>() {
			public UIEmiBanks mapRow(ResultSet rs, int rowNum) throws SQLException {
				UIEmiBanks bankDO = null;
				bankDO = new UIEmiBanks();
				bankDO.setCode(rs.getString("code"));
				bankDO.setDisplayName(rs.getString("display_name"));
				bankDO.setTenure(rs.getString("tenure"));
				bankDO.setInterestRate(rs.getString("interest_rate"));
				try {
					bankDO.setTransactionFee(rs.getString("transaction_fee"));
				} catch (Exception e) {
					logger.error(e.getStackTrace());
					System.out.println(e.getMessage());
				}
				bankDO.setStatus(rs.getString("status"));
				bankDO.setImageUrl(rs.getString("image_url"));
				bankDO.setMinAmount(rs.getString("min_amount"));
				bankDO.setMaxAmount(rs.getString("max_amount"));
				bankDO.setDisplayMobile(rs.getString("display_mobile"));
				String[] cardTypeArray = StringUtils.split(rs.getString("card_types"), ",");

				List<String> cardTypeList = new ArrayList<String>();
				if (cardTypeArray != null && cardTypeArray.length > 0) {
					for (String card : cardTypeArray) {
						cardTypeList.add(card);
					}
				}
				bankDO.setCardTypes(cardTypeList);
				return bankDO;
			}
		};

		try {
			logger.debug("Fetching information from ui_emi_banks.");
			banksList = jdbcTemplate.query(PaymentUISql.SELECT_ALL_EMI_BANKS, emiBankPayOptionMapper);
		} catch (Exception e) {
			logger.error("Exception occurred in getEMIBanksList() while fetching from emi_banks_master." , e);
			throw e;
		}

		return banksList;
	}

	@Override
	public List<UIEmiBanks> getList(UIEmiBanks t) throws Exception {
		return null;
	}

	@Override
	public UIEmiBanks get(UIEmiBanks t) throws Exception {
		return null;
	}

}
