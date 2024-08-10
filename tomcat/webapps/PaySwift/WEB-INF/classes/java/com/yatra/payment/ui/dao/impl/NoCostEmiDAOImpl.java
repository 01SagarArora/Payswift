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
import com.yatra.payment.ui.dao.bean.UINoCostEmiBanks;
import com.yatra.payment.ui.dao.sql.PaymentUISql;
import org.json.JSONArray;

@Service("noCostEmiDAO")
public class NoCostEmiDAOImpl implements PaymentUIGenericDAO<UINoCostEmiBanks> {

	@Autowired
	private SimpleJdbcTemplate jdbcTemplate;

	private static Logger logger = Logger.getLogger(UIEmiBanksDAOImpl.class);

	@Override
	public List<UINoCostEmiBanks> getAll() throws Exception {
		List<UINoCostEmiBanks> banksList = null;

		ParameterizedBeanPropertyRowMapper<UINoCostEmiBanks> emiNoCostEmiMapper = new ParameterizedBeanPropertyRowMapper<UINoCostEmiBanks>() {
			public UINoCostEmiBanks mapRow(ResultSet rs, int rowNum) throws SQLException {
				UINoCostEmiBanks bankDO = new UINoCostEmiBanks();
				try {
                                    bankDO.setLob(rs.getString("lob"));
                                    bankDO.setIsPromoAllowed(rs.getInt("is_promo_allowed"));
                                    bankDO.setBankConfigurations(rs.getString("bank_configurations"));
                                    String products = rs.getString("products");
                                    List<String> productsList = new ArrayList<String>();
                                    JSONArray array =  new JSONArray(products);
                                    if (array.length() > 0) {
                                        for (int i = 0; i<array.length();i++) {
                                            productsList.add(array.getString(i));
                                        }
                                    }
                                    bankDO.setProducts(productsList);
                                } catch (Exception ex) { 
                                    logger.error("Error while fetching ui_no_cost_emi_banks " + ex.getMessage());
                                }
				return bankDO;
			}
		};

		try {
			logger.debug("Fetching information from ui_no_cost_emi.");
			banksList = jdbcTemplate.query(PaymentUISql.SELECT_ALL_NO_COST_EMI_BANKS, emiNoCostEmiMapper);
		} catch (Exception e) {
			logger.error("Exception occurred in getEMIBanksList() while fetching from emi_banks_master." , e);
			throw e;
		}

		return banksList;
	}

	@Override
	public List<UINoCostEmiBanks> getList(UINoCostEmiBanks t) throws Exception {
		return null;
	}

	@Override
	public UINoCostEmiBanks get(UINoCostEmiBanks t) throws Exception {
		return null;
	}

}
 