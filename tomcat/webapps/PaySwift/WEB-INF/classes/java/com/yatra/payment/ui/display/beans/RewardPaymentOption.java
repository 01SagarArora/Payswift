package com.yatra.payment.ui.display.beans;

import java.util.List;
import org.apache.commons.lang.builder.ToStringBuilder;

public class RewardPaymentOption extends PaymentOption {

	private String cardHolderNameLabel;
	private String cardNoLabel;
	private List<CardType> cardTypes;
	private String rewardPointLabel;
	private String rewardPointValueLabel;
	private String bankLogoURL;
	private List<RewardBin> rewardBinList;

	public RewardPaymentOption() {}

	public RewardPaymentOption(PaymentOption paymentOption) {
		super(paymentOption);
		if(paymentOption instanceof RewardPaymentOption) {
			RewardPaymentOption rewardPaymentOption = (RewardPaymentOption) paymentOption;
			this.cardHolderNameLabel = rewardPaymentOption.getCardHolderNameLabel();
			this.cardNoLabel = rewardPaymentOption.getCardNoLabel();
			this.cardTypes = rewardPaymentOption.getCardTypes();
			this.rewardPointLabel = rewardPaymentOption.getRewardPointLabel();
			this.rewardPointValueLabel = rewardPaymentOption.getRewardPointValueLabel();
			this.bankLogoURL = rewardPaymentOption.getBankLogoURL();
			this.rewardBinList = rewardPaymentOption.getRewardBinList();
		}
	}

	public String getCardHolderNameLabel() {
		return cardHolderNameLabel;
	}

	public void setCardHolderNameLabel(String cardHolderNameLabel) {
		this.cardHolderNameLabel = cardHolderNameLabel;
	}

	public String getCardNoLabel() {
		return cardNoLabel;
	}

	public void setCardNoLabel(String cardNoLabel) {
		this.cardNoLabel = cardNoLabel;
	}

	public List<CardType> getCardTypes() {
		return cardTypes;
	}

	public void setCardTypes(List<CardType> cardTypes) {
		this.cardTypes = cardTypes;
	}

	public String getRewardPointLabel() {
		return rewardPointLabel;
	}

	public void setRewardPointLabel(String rewardPointLabel) {
		this.rewardPointLabel = rewardPointLabel;
	}

	public String getRewardPointValueLabel() {
		return rewardPointValueLabel;
	}

	public void setRewardPointValueLabel(String rewardPointValueLabel) {
		this.rewardPointValueLabel = rewardPointValueLabel;
	}

	public String getBankLogoURL() {
		return bankLogoURL;
	}

	public void setBankLogoURL(String bankLogoURL) {
		this.bankLogoURL = bankLogoURL;
	}

	public List<RewardBin> getRewardBinList() {
		return rewardBinList;
	}

	public void setRewardBinList(List<RewardBin> rewardBinList) {
		this.rewardBinList = rewardBinList;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
