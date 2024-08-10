package com.yatra.payment.client.enums;


public enum ResponseStatus {
	SUCCESS, FAILURE;
	
	private String value;

	public String getValue()
	{
		return this.value;
	}

}