package com.yatra.payment.ui.dao.bean;

public class UIMerchantPayopMapping {

	private Integer id;
	private String paymentOption;
	private String status;
	private String merchantCode;
	private String productCode;
	private boolean nonMoto;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getPaymentOption() {
		return paymentOption;
	}

	public void setPaymentOption(String paymentOption) {
		this.paymentOption = paymentOption;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

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

	public boolean isNonMoto() {
		return nonMoto;
	}

	public void setNonMoto(boolean nonMoto) {
		this.nonMoto = nonMoto;
	}

}
