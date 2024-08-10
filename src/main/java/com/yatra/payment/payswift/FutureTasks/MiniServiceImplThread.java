package com.yatra.payment.payswift.FutureTasks;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.yatra.payment.ui.service.MiniServiceI;
import com.yatra.platform.commons.utils.Timeit;

public class MiniServiceImplThread implements Runnable {

	Logger logger = Logger.getLogger(MiniServiceImplThread.class);
	CountDownLatch latch;
	MiniServiceI miniService;
	JSONObject requestJson;
	JSONObject responseJSON;
	public  MiniServiceImplThread(MiniServiceI miniService, JSONObject requestJson , CountDownLatch latch, JSONObject responseJSON){
		this.miniService = miniService;
		this.requestJson = requestJson;
		this.latch = latch;
		this.responseJSON = responseJSON;
	}
	
	
	@Override
	public void run() {
		try{
			Timeit.timeIt("Starting thread for " + miniService.getResultKey());
			JSONObject resultJson = miniService.getRequiredData(requestJson, responseJSON);
			synchronized (responseJSON) {
				responseJSON.accumulate(miniService.getResultKey(),resultJson);
			}
			Timeit.timeUp();
			logger.debug("Time takesn by "+miniService.getResultKey()+" is "+Timeit.timeTaken());
			latch.countDown();
		}catch(Exception ex){
			logger.debug("An Exception occured while exwecuting service ", ex);
		}
	}
		

}
