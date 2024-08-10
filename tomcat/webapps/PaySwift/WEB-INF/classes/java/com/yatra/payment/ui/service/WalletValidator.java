package com.yatra.payment.ui.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.yatra.payment.ui.util.PaymentUIUtil;

@Service("walletValidator")
public class WalletValidator extends Validator {
	
	public String validateGetECashRequest(Map<String, String> requestMap) {
		String merchantCode = requestMap.get(PaymentUIUtil.MERCHANT_CODE);
		String productCode = requestMap.get(PaymentUIUtil.PRODUCT_CODE);
		String error = validatePayOpEnabled(merchantCode, productCode, PaymentUIUtil.PAYMENT_OPTION_EWALLET);
		return error;
	}
}
