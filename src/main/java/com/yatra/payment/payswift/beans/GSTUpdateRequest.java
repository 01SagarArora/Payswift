package com.yatra.payment.payswift.beans;

import javax.servlet.http.HttpServletRequest;

import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.platform.request.MapBasedRequest;

public class GSTUpdateRequest extends MapBasedRequest {
	private static final long serialVersionUID = 1L;
	public GSTUpdateRequest(HttpServletRequest httpRequest) {
		super(httpRequest);
	}
	public String getSuperPnr() {
		return getParameter(PaymentUIUtil.SUPER_PNR);
	}
	public String getProduct() {
		return getParameter(PaymentUIUtil.PRODUCT_CODE);
	}
	public String getTotalDiscount() {
		return getParameter(PaymentUIUtil.TOTAL_DISCOUNT);
	}
	public String getUpdateGSTUrl() {
		return getParameter (PaymentUIUtil.GST_URL);
	}
	public String getPaymentMode() {
		return getParameter(PaymentUIUtil.PAYMENT_MODE);
	}
}
