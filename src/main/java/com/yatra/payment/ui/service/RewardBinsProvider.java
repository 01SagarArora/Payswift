package com.yatra.payment.ui.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.ui.display.beans.RewardBin;
import com.yatra.platform.commons.property.PropertyManager;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Service("rewardBinsProvider")
public class RewardBinsProvider {
	
	private static Logger logger = Logger.getLogger(RewardBinsProvider.class);
	
	@Autowired	private PropertyManager  yatraPropertyReader;
	
	public List<RewardBin> getRewardCardBinList() {

		List<RewardBin> rewardBinList = new ArrayList<RewardBin>();
		try {
			// Get list of reward types.
			List<String> rewardTypeList = getRewardTypeList();
			
			// For each reward type, create a RewardBin.
			for (String rewardType : rewardTypeList) {

				// Get bin list for reward type.
				List<String> binList = getBinList(rewardType);
				
				// Get conversion rate for reward type.
				Integer conversionRate = getConversionRate(rewardType);
				
				// Create RewardBin for reward type. 
				RewardBin rewardBinBean = new RewardBin();
				rewardBinBean.setRewardTypeName(rewardType);
				rewardBinBean.setBinList(binList);
				rewardBinBean.setConversionRate(conversionRate);
				rewardBinList.add(rewardBinBean);
			}
		} catch (Exception e) {
			logger.error("Exception occurred while getting reward bin list ", e);
		}
		return rewardBinList;
	}
	
	private List<String> getRewardTypeList() throws Exception {
		String rewardTypes = yatraPropertyReader.getProperty("rewardTypes");
		return PaymentUIUtil.createList(rewardTypes, ",");
	}
	
	private List<String> getBinList(String rewardType) throws Exception {
		String binList = yatraPropertyReader.getProperty(rewardType);
		return PaymentUIUtil.createList(binList, ",");
	}
	
	private Integer getConversionRate(String rewardType) throws Exception {
		String conversionRate = yatraPropertyReader.getProperty(rewardType + "_Rate");
		return new Integer(conversionRate);
	}
}
