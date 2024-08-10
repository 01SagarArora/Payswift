package com.yatra.payment.ui.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Component;

import com.yatra.payment.ui.dao.impl.UIPayopDAOImpl;
import com.yatra.payment.ui.dao.sql.PaymentUISql;
import com.yatra.platform.commons.role.CallContextKeeper;

@Component("apiInfoDAO")
public class ApiInfoDAO {
	
	@Autowired
	private SimpleJdbcTemplate jdbcTemplate;

	private static Logger logger = Logger.getLogger(UIPayopDAOImpl.class);
	

	public void logIntoAPIInfoAudit(String superPnr, String apiName, String params, String stage){
		String serverIP = getHostIP();
		logger.debug("Now logging into api_info_audit with params "+params +" , apiName "+apiName +" ,superPnr "+superPnr +" and stage "+stage);
		String sql = PaymentUISql.INSERT_INTO_API_INFO_AUDIT;
		try{
			jdbcTemplate.update(sql, new Object[]{superPnr,apiName,stage,params,serverIP});
		}
		catch(Exception ex){
			logger.error("Error occurred while inserting into api info audit ",ex);
		}
	}
	
	public String getSkipOtpResponseForSuperPnr(String superPnr){
		try {
			logger.info("Fetching response from api_info_audit for super_pnr = " + superPnr);
			List<String> result = jdbcTemplate.query(PaymentUISql.GET_SKIP_OTP_RESPONSE_FOR_SUPERPNR, new ParameterizedBeanPropertyRowMapper<String>() {
				public String mapRow(ResultSet rs, int rowNum) throws SQLException {
					String cardType = rs.getString("params");
					return cardType;
				}
			}, superPnr);

			logger.debug("cardType : " + result.get(0));
			return result.get(0).toString();
			
		} catch (Exception e) {
			logger.error("Exception occurred while fetching skipOtpResponse response from api_info_audit table : ", e);
			return null;
		}
	}
	
	public String getSkipOtpBinsForSuperPnr(String superPnr){
		try {
			logger.info("Fetching response from api_info_audit for super_pnr = " + superPnr);
			List<String> result = jdbcTemplate.query(PaymentUISql.GET_SKIP_OTP_BINS_FOR_SUPERPNR, new ParameterizedBeanPropertyRowMapper<String>() {
				public String mapRow(ResultSet rs, int rowNum) throws SQLException {
					String binList = rs.getString("params");
					return binList;
				}
			}, superPnr);

			logger.debug("binList : " + result.get(0));
			return result.get(0).toString();
			
		} catch (Exception e) {
			logger.error("Exception occurred while fetching skipOtpBins response from api_info_audit table : ", e);
			return null;
		}
	}

	private String getHostIP() {
		try{
			String paySwiftServerIP = CallContextKeeper.getCallContext().getHostIPAddress();
			return paySwiftServerIP;
		}catch(Exception ex){
			return null;

		}
	}
}
