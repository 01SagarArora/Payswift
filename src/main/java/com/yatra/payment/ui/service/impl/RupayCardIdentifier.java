package com.yatra.payment.ui.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yatra.payment.ui.beans.BinDetails;
import com.yatra.payment.ui.dao.BinDetailsDAO;
import com.yatra.payment.ui.service.CardTypeIdentifier;
import com.yatra.payment.ui.util.PaymentUIUtil;

@Component("rupayCardIdentifier")
public class RupayCardIdentifier implements CardTypeIdentifier {
	@Autowired private BinDetailsDAO binDetailsDAO;
	
	
	/** not needed now **/
//	String[] rupayBinRange = {"508500-508999","606985-607384","607385-607484","607485-607984","608001-608100","608101-608200","608201-608300","608301-608350","608351-608500","652150-652849","652850-653049","653050-653149"};
	
	
	@Override
	public String getCardType(String bin) {
		
		
		//new bins issued, outside of aforementioned range
		String dbCardType;
		
		if(bin.length() == 9) {
			dbCardType = binDetailsDAO.getCardTypeForNineBin(bin);
		}else {
			dbCardType = binDetailsDAO.getCardTypeForBin(bin);
		}
		
		
		if(dbCardType!=null && dbCardType.equalsIgnoreCase(PaymentUIUtil.CARD_TYPE_RUPAY))
			return dbCardType;
		else
			return null;
		
		/*boolean isBinRupay = checkBinRanges(bin);
		if(isBinRupay)
			return PaymentUIUtil.CARD_TYPE_RUPAY;
		else return null;*/

	}
	/*private boolean checkBinRanges(String binAsString) {
		int bin = Integer.parseInt(binAsString);
		for (int i = 0; i < rupayBinRange.length; i++) {
			int lowerLimit = Integer.parseInt(rupayBinRange[i].split("-")[0]);
			int upperLimit = Integer.parseInt(rupayBinRange[i].split("-")[1]);
			if (bin >= lowerLimit && bin <= upperLimit) {
				return true;
			}
		}
		return false;
	}*/

}
