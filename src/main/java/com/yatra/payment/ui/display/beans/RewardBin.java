package com.yatra.payment.ui.display.beans;

import java.util.List;

public class RewardBin {
	
	private String rewardTypeName;
	private List<String> binList;
	private Integer conversionRate;
	
	public String getRewardTypeName() {
		return rewardTypeName;
	}
	public void setRewardTypeName(String rewardTypeName) {
		this.rewardTypeName = rewardTypeName;
	}
	public List<String> getBinList() {
		return binList;
	}
	public void setBinList(List<String> binList) {
		this.binList = binList;
	}
	public Integer getConversionRate() {
		return conversionRate;
	}
	public void setConversionRate(Integer conversionRate) {
		this.conversionRate = conversionRate;
	}
	
	@Override
	public String toString() {
		// Do no change this implementation. We set List<RewardBin> in model and use it on client. It will work only with this implementation.
		return new StringBuilder().append("[rewardTypeName= '").append(rewardTypeName).append("' ,binList=").append(binList).append(", conversionRate=").append(conversionRate).append("]").toString();
	}
}
