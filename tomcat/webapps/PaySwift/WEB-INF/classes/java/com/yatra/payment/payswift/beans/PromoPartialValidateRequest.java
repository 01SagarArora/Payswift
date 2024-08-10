package com.yatra.payment.payswift.beans;

import javax.servlet.http.HttpServletRequest;

import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.platform.request.MapBasedRequest;

public class PromoPartialValidateRequest extends MapBasedRequest {

	private static final long serialVersionUID = 1L;
	public PromoPartialValidateRequest(HttpServletRequest httpRequest) {
		super(httpRequest);
	}
	public String getSuperPnr() {
		return getParameter(PaymentUIUtil.SUPER_PNR);
	}
	public String getProduct() {
		return getParameter(PaymentUIUtil.PRODUCT_CODE);
	}
	public String getPromoCode() {
		return getParameter(PaymentUIUtil.PROMO_CODE);
	}
	public String getPaymentOption() {
		return getParameter(PaymentUIUtil.PAYMENT_OPTION);
	}
	public String getPaymentMode() {
		return getParameter(PaymentUIUtil.MODE);
	}
	public String getCardBin() {
		return getParameter (PaymentUIUtil.CARD_BIN);
	}
	public String getPaymentType() {
		return getParameter (PaymentUIUtil.PAYMENT_TYPE);
	}
}
