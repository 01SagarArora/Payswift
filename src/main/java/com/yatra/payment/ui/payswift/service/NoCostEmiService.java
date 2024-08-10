package com.yatra.payment.ui.payswift.service;

import com.yatra.payment.payswift.beans.PayNowRequest;
import com.yatra.payment.ui.dao.impl.PayswiftStagesDAOImpl;
import com.yatra.payment.ui.dao.sql.PaymentUISql;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jetty.util.preventers.SecurityProviderLeakPreventer;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.ui.helper.PaymentUIHelper;
import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.platform.commons.property.PropertyManager;
import com.yatra.platform.http.HttpEndPoint;
import com.yatra.platform.http.HttpService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

@Service
public class NoCostEmiService {

	private static Logger logger = Logger.getLogger(NoCostEmiService.class);

	@Autowired
	private PayswiftStagesDAOImpl paymentDAO;
	@Autowired
	private JdbcTemplate jdbcTemplateNoCost;
	@Autowired private PropertyManager yatraPropertyReader;
	

	public boolean fetchNoCostEmiParams(PayNowRequest payNowRequest)  throws Exception{

		return fetchNoCostEmiParams(payNowRequest, null);
	}
	
	
	public boolean fetchNoCostEmiParams(PayNowRequest payNowRequest, Map<String, String> parameterMap)  throws Exception{

		//Hitting PP to calculate Fresh Discount
		String emiAmount = payNowRequest.getAmountDisplayed();
		String emiTenure = payNowRequest.getEMITenure();
		String emiTenureAfterTrim = emiTenure;
		if(emiTenureAfterTrim.contains("."))
		{
			emiTenureAfterTrim = emiTenure.substring(0, emiTenure.indexOf("."));
		}
		String emiBank  =  payNowRequest.getEMIBankCode();
		String superPnr =  payNowRequest.getSuperPnr();
		Map<String, String> fieldMap = new HashMap<String,String>();
		fieldMap.put(PaymentUIUtil.AMOUNT, emiAmount);
		fieldMap.put("tenure", emiTenureAfterTrim);
		fieldMap.put("bank", emiBank);
		fieldMap.put(PaymentUIUtil.SUPER_PNR_PAYMENT_PORTAL, superPnr);
		
		String url = yatraPropertyReader.getProperty("payment.service.emi");
		try {
			
			String getURL = PaymentUIUtil.getUrlString(url, fieldMap);
			logger.debug("Fetching no cost emi parameter for URL "+getURL);
			String getResonse = PaymentUIUtil.sendRequest(getURL, "GET");
			
			logger.debug("Response from Payment Portal for No cost emi is  "+getResonse);
			JSONObject emiResponse = new JSONObject(getResonse);
			String discount = emiResponse.getString("discount");
			String actualAmount = emiResponse.getString("actualAmount");
			String bank = emiResponse.getString("bank");
			String tenure = emiResponse.getString("tenure");
			String success = emiResponse.getString("status");
			
			if(success == null && !"success".equals(success)) 
				return true;
			if(parameterMap == null)
			{
				payNowRequest.setNoCostEmiParams("NoCostEmi_" + bank, discount, actualAmount, bank, tenure);
			}
			else
			{
				payNowRequest.setNoCostEmiParams("NoCostEmi_" + bank, discount, actualAmount,bank, tenure, parameterMap);
			}
			return false;
			
		} catch (Exception e) {
			logger.info("Exception occured while hitting Payment Portal to get amount in no cost emi flow for super pnr "+superPnr);
			return true;
		}
	}

	public Number insertIntoNoCostEmiAudit(String ttid, String params, String discount) {
		return createNoCostEmiAudit(ttid, params, discount);
	}

	public Number createNoCostEmiAudit(final String ttid, final String params, final String discountAmount) {

		KeyHolder keyHolder = new GeneratedKeyHolder();

		final String strQuery = PaymentUISql.CREATE_NO_COST_EMI_AUDIT;

		jdbcTemplateNoCost.update(new PreparedStatementCreator() {
			public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
				PreparedStatement stmt = connection.prepareStatement(strQuery, new String[] { "id" });
				stmt.setString(1, ttid);
				stmt.setString(2, params);
				return stmt;
			}
		}, keyHolder);

		Number auditId = keyHolder.getKey();
		logger.info("PaymentDAO.createPaymentRequestAudit() called, query executed is: " + strQuery + " ,parameters used are: " + params);
		return auditId;
	}
}
