package com.yatra.payment.payswift.beans;

import javax.servlet.http.HttpServletRequest;

import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.platform.request.MapBasedRequest;

public class VisaCardValidationRequest extends MapBasedRequest {

	private static final long serialVersionUID = 1L;
	
	public VisaCardValidationRequest(HttpServletRequest httpRequest) {
		super(httpRequest);
	}
	
	public String getCardData(){
		return getParameter(PaymentUIUtil.CARD_DATA);
	}

	public String getTtid() {
		return getParameter(PaymentUIUtil.TTID);
	}
	public String getProduct() {
		return getParameter(PaymentUIUtil.PRODUCT_CODE);
	}

	public String getPaymentOption() {
		return getParameter(PaymentUIUtil.PAYMENT_OPTION);
	}

	public String getCardId() {
		return getParameter(PaymentUIUtil.CARD_ID);
	}
	
}
