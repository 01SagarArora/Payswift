package com.yatra.payment.ui.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import com.yatra.payment.ui.dao.sql.PaymentUISql;
import com.yatra.payment.ui.util.ZipUtils;
import com.yatra.platform.commons.role.CallContextKeeper;
import com.yatra.platform.util.YatraUtil;

@Component("payStagesDAOImpl")
public class PayswiftStagesDAOImpl {
	@Autowired private SimpleJdbcTemplate jdbcTemplate;

	Logger logger = Logger.getLogger(PayswiftStagesDAOImpl.class);

	public String getItineraryDetailsUrl(String productCode){
		try {
			logger.info("Fetching itenerary url for product = "+productCode);
			String itineraryUrl = jdbcTemplate.queryForObject(PaymentUISql.SELECT_ITINERARY_DETAILS_URL, String.class, new Object[]{productCode});
			return itineraryUrl;

		} catch (Exception e) {
			logger.error("Exception occurred while fetching itinerary url for product : "+productCode, e);
			return null;
		}
	}

	public String getPayNowServerValidationUrl(String productCode){
		try {
			logger.info("Fetching itenerary url for product = "+productCode);
			String itineraryUrl = jdbcTemplate.queryForObject(PaymentUISql.SELECT_PAY_NOW_VALIDATION_URL, String.class, new Object[]{productCode});
			return itineraryUrl;

		} catch (Exception e) {
			logger.error("Exception occurred while fetching itinerary url for product : "+productCode, e);
			return null;
		}
	}

	public String fetchParamForSuperPNR(String superPnr , String stage){
		try {
			logger.info("Fetching params json from payment stages  for super pnr = "+superPnr);
			String itineraryDetails = jdbcTemplate.queryForObject(PaymentUISql.SELECT_PARAM_FROM_PAYSWIFT_STAGES, String.class, new Object[]{superPnr,stage});
			return itineraryDetails;
		} 
		catch (EmptyResultDataAccessException e) {
			logger.info("No params found in DB for superPnr: "+superPnr);
			return null;
		}
		catch (Exception e) {
			logger.error("Exception occurred while fetching booking details for product : "+superPnr, e);
			return null;
		}	
	}
        
        public String fetchNoCostEmiParams(String superPnr) {
            logger.info("Fetching no cost emi params : " + superPnr);
            String sql = PaymentUISql.GET_NO_COST_EMI_DETAILS;
            try {
                String params = jdbcTemplate.queryForObject(sql,String.class, new Object[] {superPnr});
                if(YatraUtil.isNullOrEmpty(params))
                	return null;
                return params;
            } catch (EmptyResultDataAccessException ex) {
                    logger.error("No params found in payment_stages for superPnr: " + superPnr);
                    return null;
            }
            
        }
        
	public String fetchParamForSuperPnrUnzipped(String superPnr , String stage){
		try {
			logger.info("Fetching params json from payment stages  for super pnr = "+superPnr);
			String itineraryDetailsZipped = jdbcTemplate.queryForObject(PaymentUISql.SELECT_PARAM_FROM_PAYSWIFT_STAGES, String.class, new Object[]{superPnr,stage});
			String itineraryDetails = ZipUtils.getUnZippedBase64(itineraryDetailsZipped);
			return itineraryDetails;
		} 
		catch (EmptyResultDataAccessException e) {
			logger.error("No params found in payswift_stgaes for superPnr: " + superPnr);
			try {
				String itineraryDetails = jdbcTemplate.queryForObject(PaymentUISql.SELECT_PARAM_FROM_PAYMENT_STAGES,String.class, new Object[] { superPnr, stage });
				return itineraryDetails;
			} catch (EmptyResultDataAccessException ex) {
				logger.error("No params found in payment_stages for superPnr: " + superPnr);
				return null;
			}
		}
		catch (Exception e) {
			logger.error("Exception occurred while fetching booking details for product : "+superPnr, e);
			return null;
		}	
	}
	
        public String fetchParamForSuperPnrPaymentInfoAuditUnzipped(String superPnr , String stage){
		try {
			logger.info("Fetching params json from payment stages  for super pnr = "+superPnr);
			String paramsZipped = jdbcTemplate.queryForObject(PaymentUISql.SELECT_PARAM_FROM_PAYMENT_INFO_AUDIT, String.class, new Object[]{superPnr,stage});
			String itineraryDetails = ZipUtils.getUnZippedBase64(paramsZipped);
			return itineraryDetails;
		} 
		catch (EmptyResultDataAccessException e) {
			logger.error("No params found in payswift_stgaes for superPnr: " + superPnr);
			return null;
		
		} catch (Exception ex) {
                    logger.error("Exception occured while fetching params in payment_info_audit for superPnr: " + superPnr);
                    return null;
                }
		
	}
        
	public String fetchParamForSuperPnrAndIdUnzipped(String superPnr , String stage, String id){
		try {
			logger.info("Fetching params json from payment stages  for super pnr = "+superPnr + " and id = " + id);
			String itineraryDetailsZipped = jdbcTemplate.queryForObject(PaymentUISql.SELECT_PARAM_FROM_PAYSWIFT_STAGES_ON_ID, String.class, new Object[]{superPnr,stage,id});
			String itineraryDetails = ZipUtils.getUnZippedBase64(itineraryDetailsZipped);
			return itineraryDetails;
		} 
		catch (EmptyResultDataAccessException e) {
			logger.error("No params found in payswift_stgaes for superPnr: " + superPnr);
			try {
				String itineraryDetails = jdbcTemplate.queryForObject(PaymentUISql.SELECT_PARAM_FROM_PAYSWIFT_STAGES_ON_ID,String.class, new Object[] { superPnr, stage, id });
				return itineraryDetails;
			} catch (EmptyResultDataAccessException ex) {
				logger.error("No params found in payment_stages for superPnr: " + superPnr);
				return null;
			}
		}
		catch (Exception e) {
			logger.error("Exception occurred while fetching booking details for product : "+superPnr, e);
			return null;
		}	
	}

	public void insertIntoPayswiftStages(String superPnr, String ttid, String stage, String data, String product, String responseStatus, String failureCode){
		try {
			String serverIP = getHostIP();
			logger.info("inserting into payswift stages values : superPnr : "+superPnr+" stage "+stage);
			String insertSQL = PaymentUISql.INSERT_INTO_PAYSWIFT_STAGE;
			jdbcTemplate.update(insertSQL, new Object[]{superPnr,ttid,stage,data,serverIP,product,responseStatus,failureCode});

		} catch (Exception e) {
			logger.error("Exception occurred while inserting into payment stages with parameters : "+superPnr+" , " + ttid + ", " +stage+" , ", e);
		}
	}

	public void insertIntoPayswiftStagesZipped(String superPnr, String ttid, String stage, String data, String product, String responseStatus, String failureCode) {
		String serverIP = getHostIP();
		final String zippedData = ZipUtils.getZipped(data);
		logger.info("inserting into payswift stages values : superPnr : " + superPnr + " stage " + stage);
		String insertSQL = PaymentUISql.INSERT_INTO_PAYSWIFT_STAGE;
		jdbcTemplate.update(insertSQL, new Object[]{superPnr, ttid, stage, zippedData, serverIP, product, responseStatus, failureCode});
	}
	
	public long insertIntoPaySwiftStagesReturnsId(String superPnr, String ttid, String stage, String data, String product, String responseStatus, String failureCode) {
			KeyHolder keyHolder = new GeneratedKeyHolder();
			String serverIP = getHostIP();
			logger.info("inserting into payswift_stages values : superPnr : " + superPnr + " stage : " + stage);
			String insertSql = PaymentUISql.INSERT_INTO_PAYSWIFT_STAGE;
			jdbcTemplate.getJdbcOperations().update(new PreparedStatementCreator() {
				@Override
				public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
					PreparedStatement stmt = con.prepareStatement(insertSql, new String[] {"id"});
					stmt.setString(1, StringUtils.left(superPnr, 20));
					stmt.setString(2, StringUtils.left(ttid, 20));
					stmt.setString(3, StringUtils.left(stage, 50));
					stmt.setString(4, data);
					stmt.setString(5, StringUtils.left(serverIP, 25));
					stmt.setString(6, StringUtils.left(product, 64));
					stmt.setString(7, StringUtils.left(responseStatus, 20));
					stmt.setString(8, failureCode);
					return stmt;
				}
			}, keyHolder);
			long dbId = keyHolder.getKey().longValue();
			logger.info("PaymentDAO.insertIntoPaySwiftStagesReturnsId() called, query executed is: " + insertSql + " ,parameter returned is: " + dbId);
			return dbId;	
	}
	

	private String getHostIP() {
		try{
			String paySwiftServerIP = CallContextKeeper.getCallContext().getHostIPAddress();
			return paySwiftServerIP;
		}catch(Exception ex){
			return null;

		}
	}

	public JSONObject getBankDownMap() {
		String sql = PaymentUISql.GET_BANK_DOWN_MESSAGES;
		Map<String, Object> resultMap = null;			// Query parameters.
		Object[] obj = new Object[] { "ENABLED" };

		// Fire Query.
		try{
			resultMap = jdbcTemplate.queryForMap(sql, obj);
		}catch (DataAccessException e) {
			logger.error("Exception occurred while fetching bank down messaging", e);
		}

		JSONObject bankMessageJson = new JSONObject(resultMap);
		return bankMessageJson;
	}
	
	public JSONObject getPayOpMessageMap() {
		String sql = PaymentUISql.GET_PAYOP_MESSAGES;
		Map<String, Object> resultMap = null;			// Query parameters.
		Object[] obj = new Object[] { "ENABLED" };

		// Fire Query.
		try{
			resultMap = jdbcTemplate.queryForMap(sql, obj);
		}catch (DataAccessException e) {
			logger.error("Exception occurred while fetching bank down messaging", e);
		}
		
		JSONObject bankMessageJson = new JSONObject(resultMap);
		return bankMessageJson;
	}
	
	public String getSkipOtpFlagPayments() {
		String sql = PaymentUISql.GET_YATRA_PROPERTY_VALUE;
		String result = null;			// Query parameters.
		Object[] obj = new Object[] { "skipOtpFlagPayments" };

		try{
			result = jdbcTemplate.queryForObject(sql, String.class, obj);
		}catch (DataAccessException e) {
			logger.error("Exception occurred while fetching yatra property", e);
		}
		return result;
	}
	
	public void createS2SDBEntry(String tripId, String mtxnId, String product, String S2SRequest, String response, String status) {
		try {
			String query = PaymentUISql.CREATE_S2S_RESPONSE_AUDIT;
			logger.debug("Executing query : " + query + " with params : tripId = " + tripId + " ,mtxnId = " + mtxnId + " ,request = " + S2SRequest + " ,product = " + product + " ,response = " + response + " ,status = " + status );
			jdbcTemplate.update(query, new Object[] { tripId, mtxnId, product, S2SRequest, response, status });
		} catch (Exception e) {
			logger.error("Exception occurred while inserting record in s2s_response_audit table : " , e);
		}
	}

	public String getIntentUrl(String mtxnId, String uid) {
		try {
			logger.info("Fetching link from IntententUrlAudit  for mtxnId = "+mtxnId + " and uid = " + uid);
			String link = jdbcTemplate.queryForObject(PaymentUISql.GET_LINK_FROM_INTENT_URL_AUDIT, String.class, new Object[]{mtxnId,uid});
			return link;
		} 
		catch (EmptyResultDataAccessException e) {
			logger.info("No IntentUrl found in DB for mtxnId = "+mtxnId + " and uid = " + uid);
			return null;
		}
		catch (Exception e) {
			logger.error("Exception occurred while fetching intent url for mtxnId = "+mtxnId + " and uid = " + uid, e);
			return null;
		}	
	}
	
	public String isAdBannerEnabled() {
		String sql = PaymentUISql.GET_YATRA_PROPERTY_VALUE;
		String result = null;			// Query parameters.
		Object[] obj = new Object[] { "isAdBannerEnabled" };

		try{
			result = jdbcTemplate.queryForObject(sql, String.class, obj);
		}catch (DataAccessException e) {
			logger.error("Exception occurred while fetching yatra property", e);
		}
		return result;
	}
	
	public String getAdBannerImageUrl() {
		String sql = PaymentUISql.GET_YATRA_PROPERTY_VALUE;
		String result = null;			// Query parameters.
		Object[] obj = new Object[] { "adBannerImageUrl" };

		try{
			result = jdbcTemplate.queryForObject(sql, String.class, obj);
		}catch (DataAccessException e) {
			logger.error("Exception occurred while fetching yatra property", e);
		}
		return result;
	}

	public Boolean getIsGVFraudCheckEnabled() {
		String sql = PaymentUISql.GET_YATRA_PROPERTY_VALUE;
		String result = null;			// Query parameters.
		Object[] obj = new Object[] { "isGVFraudCheckEnabled" };

		try{
			result = jdbcTemplate.queryForObject(sql, String.class, obj);
			if(Boolean.valueOf(result))
				return true;
			else return false;
		}catch (DataAccessException e) {
			logger.error("Exception occurred while fetching yatra property", e);
		}
		return false;
	}

	public String getJusPayFlag() {
		String sql = PaymentUISql.GET_YATRA_PROPERTY_VALUE;
		String result = null;			// Query parameters.
		Object[] obj = new Object[] { "jusPayFlag" };

		try{
			result = jdbcTemplate.queryForObject(sql, String.class, obj);
		}catch (DataAccessException e) {
			logger.error("Exception occurred while fetching yatra property", e);
			return "true";
		}
		return result;
	}
	
	public String useNewB2BPoolEndPoint() {
		logger.info("getting use.new.pool flag value : ");
        String sql = PaymentUISql.GET_YATRA_PROPERTY_VALUE;
        try {
            String result = jdbcTemplate.queryForObject(sql,String.class, new Object[] {"use.new.pool"});
            if(!YatraUtil.isNullOrEmpty(result))
            	return result;
            else return "false";
        } catch (EmptyResultDataAccessException ex) {
             logger.error("No entry found in yatra_properties for use.new.pool");
            return "false";
        }
	}
	
	public String getCardBinType(String cardBin) {
		logger.info("getting cardBinType for bin : " + cardBin);
        String sql = PaymentUISql.GET_BIN_TYPE_FOR_BIN;
        try {
            String result = jdbcTemplate.queryForObject(sql,String.class, new Object[] {cardBin});
            if(!YatraUtil.isNullOrEmpty(result))
            	return result;
            else return "CREDIT";
        } catch (EmptyResultDataAccessException ex) {
             logger.error("No entry found cardBinType for bin : " + cardBin);
            return "CREDIT";
        }
	}
	
	public Map<String, String> getGatewayCodeForBankId(String bankId) {
		final String strQuery = PaymentUISql.SELECT_GATEWAY_FOR_BANK;
		List<Map<String, Object>> resultList = jdbcTemplate.queryForList(strQuery, new Object[] { bankId });
		Map<String, String> retMap = new HashMap<String, String>();
		if (resultList != null && resultList.size() > 0) {
			if (resultList.get(0).get("code") != null)
				retMap.put("code", resultList.get(0).get("code").toString());
			if (resultList.get(0).get("code2") != null)
				retMap.put("code2", resultList.get(0).get("code2").toString());
			if (resultList.get(0).get("primary_share") != null)
				retMap.put("primary_share", resultList.get(0).get("primary_share").toString());
			if (resultList.get(0).get("bank_name") != null)
				retMap.put("name", resultList.get(0).get("bank_name").toString());
		}
		return retMap;
	}
	
	public List<Map<String, Object>> getAllVoucherConfigDetails() {
		try {
			String query = PaymentUISql.GET_QC_VOUCHER_CONFIG;
			return jdbcTemplate.queryForList(query);
		} catch (Exception e) {
			logger.error("Exception occurred while getting all voucher Config details", e);
			return null;
		}
	}
	
	public String getYatraPropertyValueFromDB(String propKey) {
		String sql = PaymentUISql.GET_YATRA_PROPERTY_VALUE;
		String result = null;			// Query parameters.
		Object[] obj = new Object[] { propKey };

		try{
			result = jdbcTemplate.queryForObject(sql, String.class, obj);
		}catch (DataAccessException e) {
			logger.error("Exception occurred while fetching yatra property", e);
			return "true";
		}
		return result;
	}
	
	public void updateHDFCEMIRate(String value) {
		logger.debug("updating hdfc emi rate for all tenure to : " + value);
		String sql = "UPDATE ui_emi_banks SET interest_rate = ? WHERE code = 'hdfc'";
		Object[] obj = new Object[]{value};
		jdbcTemplate.update(sql, obj);
	}
	
	public String twidEligibilityBypassPromo(String propertyName) {
		String sql = PaymentUISql.GET_YATRA_PROPERTY_VALUE;
		String promoConfigured = null;
		Object[] obj = new Object[] { propertyName };
		try{
			promoConfigured = jdbcTemplate.queryForObject(sql, String.class, obj);
		}catch (DataAccessException e) {
			logger.error("Exception occurred while fetching yatra property", e);
			return "true";
		}		
		return promoConfigured;
	}

	public void insertIntoWebEvents(String superPnr, String eventType, String web_event_msg,
									String merchantCode, String productCode){
		String sql=PaymentUISql.INSERT_INTO_WEB_EVENTS;

		try {
			Object [] InsertObj = new Object[]{superPnr,eventType,web_event_msg,merchantCode,productCode};
			jdbcTemplate.update(sql, InsertObj);
		} catch (Exception e) {
			logger.error("Exception occurred while inserting data for method insertIntoWebEvents : " , e);
		}

	}

	public String getPropertyValueofTR(String property_key){
		try {
			String qry = PaymentUISql.FETCH_BIN_DETAIL;
			logger.info("Getting property_value for property_key : " + property_key);
			Object[] obj = new Object[]{property_key};
			return jdbcTemplate.queryForObject(qry,String.class,obj);

		}catch (Exception ex){
			logger.error("Exception occured while getting fetchBinDetail url , Exception is :",ex);
		}
		return null;
	}
}
