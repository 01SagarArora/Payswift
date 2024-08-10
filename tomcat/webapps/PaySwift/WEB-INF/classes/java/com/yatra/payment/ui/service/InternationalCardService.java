package com.yatra.payment.ui.service;

import com.yatra.payment.ui.dao.BinDetailsDAO;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service("internationalCardService")
public class InternationalCardService {
	
	private static Logger logger = Logger.getLogger(InternationalCardService.class);

	@Autowired private BinDetailsDAO binDetailsDAO;
	
	public boolean isCardInternational(String bin) throws Exception {
		Integer count = binDetailsDAO.getCardBinCount(bin);
		return (count == null || count <= 0);
	}
	
	public String getInternationalCardJSON(String bin) {
		
		JSONObject isCardInternationalJSON = new JSONObject();
		isCardInternationalJSON.put("bin", bin);
		Boolean showSubOption = false;
		try {
			// Get Server date.
			String serverDate = getServerDate();
			
			// Determine if card is international.
			Boolean isCardInternational = isCardInternational(bin);
			
			if(!isCardInternational)
				showSubOption = doesBinSupportATM(bin);
				//showSubOption = isCardDebitHDFC(bin);
			
			// Create JSON.
			isCardInternationalJSON.put("serverDate", serverDate);
			isCardInternationalJSON.put("isCardInternational", isCardInternational.toString());
			isCardInternationalJSON.put("showSubOption", showSubOption.toString());
			
		} catch (Exception e) {
			logger.error("Exception occurred while identifying if card is international ", e);
			isCardInternationalJSON.put("isCardInternational", "error");
		}
		logger.debug("PaymentUIService :: Returning JSON from PaymentUIService.isCardInternational() : " + isCardInternationalJSON.toString());
		return isCardInternationalJSON.toString();
	}
	
	
	private Boolean isCardDebitHDFC(String bin) {
		String bankName = binDetailsDAO.getBankNameForBin(bin);
		if("DEBIT HDFC BANK".equalsIgnoreCase(bankName))
			return true;
		else return false;
	}
	
	private Boolean doesBinSupportATM(String bin) {
		String atmSupported = binDetailsDAO.getAtmSupportedFlagForBin(bin);
		if("1".equalsIgnoreCase(atmSupported))
			return true;
		else return false;
	}

	private String getServerDate() {
		String serverDate = "";
		try {
			SimpleDateFormat Dbformat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa");
			Date currDate = new Date();
			serverDate = Dbformat.format(currDate);
			
		} catch (Exception e) {
			logger.error("Exception occurred while getting server date ", e);
		}
		return serverDate;
	}
}
