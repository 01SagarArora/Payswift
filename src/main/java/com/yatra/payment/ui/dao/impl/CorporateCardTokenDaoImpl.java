package com.yatra.payment.ui.dao.impl;

import com.yatra.payment.ui.dao.sql.PaymentUISql;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("corporateCardTokenDaoImpl")
public class CorporateCardTokenDaoImpl {
    @Autowired
    private SimpleJdbcTemplate jdbcTemplate;

    Logger logger = Logger.getLogger(CorporateCardTokenDaoImpl.class);

    public Map<String,Object> getCorpCardTokenInfo(String cardId){
        Map<String, Object> resultMap = null;
        String sql = PaymentUISql.SELECT_ACTIVE_CORP_CARD_TOKEN_INFO;
        Object[] objs = new Object[] {cardId};
        try {
            logger.info("Fetching Token info  for cardid = "+cardId);
            resultMap = jdbcTemplate.queryForMap(sql,objs);
            return resultMap;

        } catch (Exception e) {
            logger.error("Exception occurred while fetching token info  for cardid  : "+cardId, e);
            return null;
        }
    }
}
