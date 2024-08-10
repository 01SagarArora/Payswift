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
import com.yatra.payment.ui.dao.sql.PaymentUISql;
import com.yatra.payment.ui.display.beans.Merchant;

@Component("merchantMasterDao")
public class MerchantMasterDAOImpl implements PaymentUIGenericDAO<Merchant> {

	@Autowired
	private SimpleJdbcTemplate jdbcTemplate;

	private static Logger logger = Logger.getLogger(MerchantMasterDAOImpl.class);

	@Override
	public List<Merchant> getAll() throws Exception {
		logger.debug("MerchantMasterDAO :: getAllMerchantProducts() starts here ");

		ParameterizedBeanPropertyRowMapper<Merchant> merchantProductMapper = new ParameterizedBeanPropertyRowMapper<Merchant>() {
			public Merchant mapRow(ResultSet rs, int rowNum) throws SQLException {
				Merchant merchant = new Merchant();
				merchant.setMerchantCode(rs.getString("merchant_code"));
				merchant.setProductCode(rs.getString("product_code"));
				merchant.setMerchantName(rs.getString("merchant_name"));
				merchant.setProductName(rs.getString("product_name"));
				merchant.setRequestChecksum(rs.getString("request_checksum"));
				merchant.setResponseChecksum(rs.getString("response_checksum"));
				merchant.setResponseUrl(rs.getString("response_url"));
				merchant.setLogoUrl(rs.getString("logo_url"));
				return merchant;
			}
		};

		try {
			logger.debug("Fetching information from merchant_master.");
			return jdbcTemplate.query(PaymentUISql.SELECT_MERCHANT_PRODUCTS, merchantProductMapper);
			
		} catch (Exception e) {
			logger.error("Exception occurred while fetching from merchant_master.", e);
			throw e;
		}
	}

	@Override
	public List<Merchant> getList(Merchant t) throws Exception {
		return null;
	}

	@Override
	public Merchant get(Merchant t) throws Exception {
		return null;
	}

}
