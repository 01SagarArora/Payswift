package com.yatra.payment.ui.dao.impl;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Component;

import com.yatra.payment.ui.dao.sql.PaymentUISql;

@Component("payswiftLatencyDAOImpl")
public class PayswiftLatencyDAOImpl {
	@Autowired private SimpleJdbcTemplate jdbcTemplate;

	Logger logger = Logger.getLogger(PayswiftLatencyDAOImpl.class);

	public void insertIntoPayswiftLatency(String superPnr, String ttid, String stage, String product, String responseTime){
		try {
			logger.info("inserting into payswift_latency values : superPnr : "+superPnr+" stage : "+stage);
			String insertSQL = PaymentUISql.INSERT_INTO_PAYSWIFT_LATENCY;
			jdbcTemplate.update(insertSQL, new Object[]{superPnr,ttid,stage,product,responseTime});

		} catch (Exception e) {
			logger.error("Exception occurred while inserting into payswift_latency with parameters : "+superPnr+" , " + ttid + ", " +stage+" , ", e);
		}
	}
}
