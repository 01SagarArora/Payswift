package com.yatra.payment.qb.corporate.bean;

public class PersonalQBDeleteCardRequest extends DeleteCardRequest {
	
	private Long tenantId;
	private String merchant;
	private String product;

	public String getMerchant() {
		return merchant;
	}

	public void setMerchant(String merchant) {
		this.merchant = merchant;
	}

	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public Long getTenantId() {
		return tenantId;
	}

	public void setTenantId(Long tenantId) {
		this.tenantId = tenantId;
	}
}
