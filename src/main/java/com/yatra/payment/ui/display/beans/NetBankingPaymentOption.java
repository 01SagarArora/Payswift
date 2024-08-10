package com.yatra.payment.ui.display.beans;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

public class NetBankingPaymentOption extends PaymentOption {

	private List<Bank> banks;
	private List<Bank> preferredBanks;

	public NetBankingPaymentOption() {}

	public NetBankingPaymentOption(PaymentOption paymentOption) {
		super(paymentOption);
		if(paymentOption instanceof NetBankingPaymentOption) {
			NetBankingPaymentOption netBankingPaymentOption = (NetBankingPaymentOption) paymentOption;
			this.banks = netBankingPaymentOption.getBanks();
			this.preferredBanks = netBankingPaymentOption.getPreferredBanks();
		}
	}

	public List<Bank> getBanks() {
		return banks;
	}

	public void setBanks(List<Bank> banks) {
		this.banks = banks;
	}

	public List<Bank> getPreferredBanks() {
		return preferredBanks;
	}

	public void setPreferredBanks(List<Bank> preferredBanks) {
		this.preferredBanks = preferredBanks;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
