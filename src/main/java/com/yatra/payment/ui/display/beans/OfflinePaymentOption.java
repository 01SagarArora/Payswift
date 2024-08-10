package com.yatra.payment.ui.display.beans;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

public class OfflinePaymentOption extends PaymentOption {

	private List<OfflineType> offlineOptions;
	private String cardHolderName;
	private String offlineAmount;
	private String collectorName;
	private String offlineAuthCode;
	private String offlineBankName;
	private String chequeNo;
	private String drawerName;
	private String chequeDate;
	
	public OfflinePaymentOption() {}
	
	public OfflinePaymentOption(PaymentOption paymentOption) {
		super(paymentOption);
		if(paymentOption instanceof OfflinePaymentOption) {
			OfflinePaymentOption offlinePaymentOption = (OfflinePaymentOption) paymentOption;
			this.offlineOptions = offlinePaymentOption.getOfflineOptions();
			this.cardHolderName = offlinePaymentOption.getCardHolderName();
			this.offlineAmount = offlinePaymentOption.getOfflineAmount();
			this.collectorName = offlinePaymentOption.getCollectorName();
			this.offlineAuthCode = offlinePaymentOption.getOfflineAuthCode();
			this.offlineBankName = offlinePaymentOption.getOfflineBankName();
			this.chequeNo = offlinePaymentOption.getChequeNo();
			this.drawerName = offlinePaymentOption.getDrawerName();
			this.chequeDate = offlinePaymentOption.getChequeDate();
		}
	}

	public String getCardHolderName() {
		return cardHolderName;
	}

	public void setCardHolderName(String cardHolderName) {
		this.cardHolderName = cardHolderName;
	}

	public List<OfflineType> getOfflineOptions() {
		return offlineOptions;
	}

	public void setOfflineOptions(List<OfflineType> offlineOptions) {
		this.offlineOptions = offlineOptions;
	}

	public String getOfflineAmount() {
		return offlineAmount;
	}

	public void setOfflineAmount(String offlineAmount) {
		this.offlineAmount = offlineAmount;
	}

	public String getCollectorName() {
		return collectorName;
	}

	public void setCollectorName(String collectorName) {
		this.collectorName = collectorName;
	}

	public String getOfflineAuthCode() {
		return offlineAuthCode;
	}

	public void setOfflineAuthCode(String offlineAuthCode) {
		this.offlineAuthCode = offlineAuthCode;
	}

	public String getOfflineBankName() {
		return offlineBankName;
	}

	public void setOfflineBankName(String offlineBankName) {
		this.offlineBankName = offlineBankName;
	}

	public String getChequeNo() {
		return chequeNo;
	}

	public void setChequeNo(String chequeNo) {
		this.chequeNo = chequeNo;
	}

	public String getDrawerName() {
		return drawerName;
	}

	public void setDrawerName(String drawerName) {
		this.drawerName = drawerName;
	}

	public String getChequeDate() {
		return chequeDate;
	}

	public void setChequeDate(String chequeDate) {
		this.chequeDate = chequeDate;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
