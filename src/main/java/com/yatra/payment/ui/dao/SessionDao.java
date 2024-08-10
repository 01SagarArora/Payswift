package com.yatra.payment.ui.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.SerializationUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import com.yatra.payment.payswift.beans.SessionStorageCardInfo;
import com.yatra.payment.ui.dao.sql.PaymentUISql;
import com.yatra.payment.ui.enums.FailureCode;
import com.yatra.payment.ui.exceptions.PayswiftException;

public class SessionDao extends SimpleJdbcDaoSupport {

	private static Logger logger = Logger.getLogger(SessionDao.class);

    public boolean insertToSessionMaster(String userId, SessionStorageCardInfo cardInfo) {
	    try{
	        SimpleJdbcInsert jobInsertSql = new SimpleJdbcInsert(this.getDataSource()).withTableName("session_master").
	                usingColumns("session_uuid", "session_data").usingGeneratedKeyColumns("id");
	        Map<String, Object> jobParams = new HashMap<String, Object>();
	        jobParams.put("session_uuid", userId);
	        jobParams.put("session_data", SerializationUtils.serialize(cardInfo));
	        jobInsertSql.execute(jobParams);
	        return true;
    	}
    	catch(Exception ex){
    		throw new PayswiftException(FailureCode.SAVE_CARD_IN_SESSION_EXCEPTION.getCode(), "Could not save card in Session", ex);
    	}
    }

    public SessionStorageCardInfo getCardInfoFromSessionDB(final String uuid) {
        try {
            String sql = PaymentUISql.GET_CARD_FROM_SESSION_OBJECT;
	        ParameterizedRowMapper<SessionStorageCardInfo> mapper = new ParameterizedRowMapper<SessionStorageCardInfo>()
	        {
	        	public SessionStorageCardInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
	        		if(rs==null){
	        			return null;
	        		}
	        		SessionStorageCardInfo userCardInfo = null;
	                byte[] bytes = rs.getBytes("session_data");
	               
	                if(bytes!= null && bytes.length > 0){
	                	userCardInfo = (SessionStorageCardInfo) SerializationUtils.deserialize(bytes);
	                }
	                return userCardInfo;
	            }
	        };
	        ArrayList<SessionStorageCardInfo> cardInfoList = (ArrayList<SessionStorageCardInfo>) getSimpleJdbcTemplate().query(sql, mapper, new Object[]{uuid});
	        if (cardInfoList != null && cardInfoList.size() > 0){
	            return cardInfoList.get(0);
	        }
	        else{
	            return null;
	        }
        } catch (Exception ioExp) {
        	logger.error("Error in fetching card details from session db :"+ioExp);
        	return null;
        } 
    }

}
