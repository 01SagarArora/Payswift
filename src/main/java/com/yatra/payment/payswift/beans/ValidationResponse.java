package com.yatra.payment.payswift.beans;

public class ValidationResponse {

	private boolean isValid;
	private String errorMessage;
	private String superPnr;
	
	public ValidationResponse(){}
	
	public ValidationResponse(boolean isValid,String mesage,String pnr){
		this.isValid = isValid;
		this.errorMessage = mesage;
		this.superPnr = pnr;
	}

	public boolean isValid() {
		return isValid;
	}

	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getSuperPnr() {
		return superPnr;
	}

	public void setSuperPnr(String superPnr) {
		this.superPnr = superPnr;
	}

	@Override
	public String toString() {
		return "ValidationResponse [isValid=" + isValid + ", errorMessage="
				+ errorMessage + ", superPnr=" + superPnr + "]";
	}
	
}
