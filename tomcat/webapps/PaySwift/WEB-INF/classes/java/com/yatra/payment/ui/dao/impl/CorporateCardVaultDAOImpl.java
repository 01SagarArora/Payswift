package com.yatra.payment.ui.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Component;

import com.yatra.payment.qb.corporate.bean.CorporateTokenizedCard;
import com.yatra.payment.ui.dao.sql.PaymentUISql;

@Component
public class CorporateCardVaultDAOImpl {
	@Autowired
	private SimpleJdbcTemplate jdbcTemplate;

	private ParameterizedBeanPropertyRowMapper<CorporateTokenizedCard> tokenizedCardMapper = new ParameterizedBeanPropertyRowMapper<CorporateTokenizedCard>() {
		public CorporateTokenizedCard mapRow(ResultSet rs, int rowNum) throws SQLException {
			{
				CorporateTokenizedCard card = new CorporateTokenizedCard();
				card.setCardId(rs.getString("card_id"));
				card.setCardtype(rs.getString("card_type"));
				card.setTokenStatus(rs.getString("token_status"));
				return card;
			}
		}
	};

	public List<CorporateTokenizedCard> getTokenizedCards(String corporateId) {
		final String strQuery = PaymentUISql.GET_CORP_CARD_TOKEN_STATUS;
		List<CorporateTokenizedCard> list = jdbcTemplate.query(strQuery, tokenizedCardMapper, corporateId);
		return list;
	}

}
