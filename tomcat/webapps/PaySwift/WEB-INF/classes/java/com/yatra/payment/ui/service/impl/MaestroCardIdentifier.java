package com.yatra.payment.ui.service.impl;

import com.yatra.payment.ui.dao.BinDetailsDAO;
import com.yatra.payment.ui.service.CardTypeIdentifier;
import com.yatra.payment.ui.util.PaymentUIUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("maestroCardIdentifier")
public class MaestroCardIdentifier implements CardTypeIdentifier {

	@Autowired private BinDetailsDAO binDetailsDAO;
	@Override
	public String getCardType(String bin) {
		String dbCardType;
		final String MAESTRO = "MAESTRO";
		
		if(bin.length() == 9) {
			dbCardType = binDetailsDAO.getCardTypeForNineBin(bin);
		}else {
			dbCardType = binDetailsDAO.getCardTypeForBin(bin);
			
		}
		
		if(dbCardType!=null && (dbCardType.equalsIgnoreCase(PaymentUIUtil.CARD_TYPE_MAESTRO) || dbCardType.equalsIgnoreCase(MAESTRO)))
			return dbCardType;
		else
			return null;
	}

}
