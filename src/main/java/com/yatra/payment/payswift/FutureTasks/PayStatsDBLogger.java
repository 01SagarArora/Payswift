package com.yatra.payment.payswift.FutureTasks;

import com.yatra.payment.ui.dao.impl.PayswiftLatencyDAOImpl;
import com.yatra.platform.commons.YatraRunable;

public class PayStatsDBLogger extends YatraRunable {
	private String superPnr;
	private String ttid;
	private String stage;
	private String product;
	private String latency;
	private PayswiftLatencyDAOImpl payswiftDao;

	public PayStatsDBLogger(String superPnr, String ttid, String stage, String product, String responseTime,
			PayswiftLatencyDAOImpl paymentDao) {
		this.superPnr = superPnr;
		this.ttid = ttid;
		this.stage = stage;
		this.product = product;
		this.latency = responseTime;
		this.payswiftDao = paymentDao;

	}

	@Override
	public void myRun() {
		payswiftDao.insertIntoPayswiftLatency(superPnr, ttid, stage, product, latency);

	}

}
