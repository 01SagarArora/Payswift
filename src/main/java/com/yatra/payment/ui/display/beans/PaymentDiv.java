package com.yatra.payment.ui.display.beans;

import java.util.List;

public class PaymentDiv {
	
	private String merchantCode;
	private String productCode;
	private List<PaymentOption> paymentOptions;
	private String version;
	public String getMerchantCode() {
		return merchantCode;
	}

	public void setMerchantCode(String merchantCode) {
		this.merchantCode = merchantCode;
	}

	public String getProductCode() {
		return productCode;
	}

	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}

	public List<PaymentOption> getPaymentOptions() {
		return paymentOptions;
	}

	public void setPaymentOptions(List<PaymentOption> paymentOptions) {
		this.paymentOptions = paymentOptions;
	}
	
	@Override
	public String toString() {
		return new StringBuilder().append("PaymentOptionsBean [paymentOptions=").append(paymentOptions).append("]").toString();
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
	
}
