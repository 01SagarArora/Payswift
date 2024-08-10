package com.yatra.payment.ui.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Service;

import com.yatra.payment.ui.dao.PaymentUIGenericDAO;
import com.yatra.payment.ui.dao.bean.UICardTypes;
import com.yatra.payment.ui.dao.sql.PaymentUISql;

@Service("cardTypeDao")
public class UICardTypesDAOImpl implements PaymentUIGenericDAO<UICardTypes> {

	@Autowired
	private SimpleJdbcTemplate jdbcTemplate;

	private static Logger logger = Logger.getLogger(UICardTypesDAOImpl.class);

	@Override
	public List<UICardTypes> getAll() throws Exception {
		try {
			logger.debug("Fetching information from ui_cardtypes.");
			ParameterizedBeanPropertyRowMapper<UICardTypes> cardTypeMapper = new ParameterizedBeanPropertyRowMapper<UICardTypes>() {
				public UICardTypes mapRow(ResultSet rs, int rowNum) throws SQLException {
					UICardTypes uICardTypes = new UICardTypes();
					uICardTypes.setCode(rs.getString("code"));
					uICardTypes.setImageURL(rs.getString("image_url"));
					uICardTypes.setDisplayName(rs.getString("display_name"));
					return uICardTypes;
				}
			};
			List<UICardTypes> cardTypeList = jdbcTemplate.query(PaymentUISql.SELECT_ALL_CARD_TYPES, cardTypeMapper);
			return cardTypeList;
		} catch (Exception e) {
			logger.error("Exception occurred in populateCardTypesMap() while fetching from card_type_master." , e);
			throw e;
		}
	}

	@Override
	public List<UICardTypes> getList(UICardTypes t) throws Exception {
		return null;
	}

	@Override
	public UICardTypes get(UICardTypes t) throws Exception {
		return null;
	}

}
