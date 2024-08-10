package com.yatra.payment.ui.display.beans;

import org.apache.commons.lang.builder.ToStringBuilder;

public class EzeClickPaymentOption extends PaymentOption {

	private String displayName;
	private String optionCode;
	private String logoURL;
	
	public EzeClickPaymentOption() {}
	
	public EzeClickPaymentOption(PaymentOption paymentOption) {
		super(paymentOption);
		if(paymentOption instanceof EzeClickPaymentOption) {
			EzeClickPaymentOption ezeClickPaymentOption = (EzeClickPaymentOption) paymentOption;
			this.displayName = ezeClickPaymentOption.getDisplayName();
			this.optionCode = ezeClickPaymentOption.getOptionCode();
			this.logoURL = ezeClickPaymentOption.getLogoURL();
		}
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getOptionCode() {
		return optionCode;
	}

	public void setOptionCode(String optionCode) {
		this.optionCode = optionCode;
	}

	public String getLogoURL() {
		return logoURL;
	}

	public void setLogoURL(String logoURL) {
		this.logoURL = logoURL;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
