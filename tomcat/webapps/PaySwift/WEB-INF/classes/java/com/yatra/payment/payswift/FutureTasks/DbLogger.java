package com.yatra.payment.payswift.FutureTasks;

import com.yatra.payment.ui.dao.impl.PayswiftStagesDAOImpl;
import com.yatra.platform.commons.YatraRunable;

public class DbLogger extends YatraRunable{

	private String superPnr;
	private String ttid;
	private String stage;
	private String params;
	private String product;
	private String responseStatus;
	private String failureCode;
	private PayswiftStagesDAOImpl paymentDao;
	
	public DbLogger(String superPnr, String ttid, String stage, String params,String product, String responseStatus, String failureCode, 
			PayswiftStagesDAOImpl paymentDao){
		this.superPnr = superPnr;
		this.stage = stage;
		this.params = params;
		this.product = product;
		this.responseStatus = responseStatus;
		this.failureCode = failureCode;
		this.paymentDao = paymentDao;
		this.ttid = ttid;
	}
	@Override
	public void myRun() {
		paymentDao.insertIntoPayswiftStages(superPnr, ttid, stage, params, product, responseStatus, failureCode);
		
	}
	
}
