package com.yatra.payment.payswift.beans;

import com.yatra.payment.client.enums.ResponseStatus;
import com.yatra.payment.ui.enums.FailureCode;

public class APIStatusBean {

	private ResponseStatus responseStatus; 
	private FailureCode failureCode;
	
	public APIStatusBean() {
		this.responseStatus = ResponseStatus.SUCCESS;
		this.failureCode = FailureCode.NO_FAILURE_CAPTURED;
	}
	
	public APIStatusBean(ResponseStatus responseStatus, FailureCode failureCode) {
		this.responseStatus = responseStatus;
		this.failureCode = failureCode;
	}
	
	public ResponseStatus getResponseStatus() {
		return responseStatus;
	}
	public void setResponseStatus(ResponseStatus responseStatus) {
		this.responseStatus = responseStatus;
	}
	public FailureCode getFailureCode() {
		return failureCode;
	}
	public void setFailureCode(FailureCode failureCode) {
		this.failureCode = failureCode;
		if(failureCode != null && failureCode != FailureCode.NO_FAILURE_CAPTURED )
			this.setResponseStatus(ResponseStatus.FAILURE);
	}
}
