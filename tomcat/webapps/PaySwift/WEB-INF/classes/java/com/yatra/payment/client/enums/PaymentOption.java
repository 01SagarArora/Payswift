package com.yatra.payment.client.enums;

public enum PaymentOption {

	CC("Credit Card"), 
	DC("Debit Card"), 
	EMI("EMI"), 
	QB("Quick Book"), 
	NB("Net Banking"), 
	REWARDS("Reward Payment"), 
	ATM("ATM"), 
	ITZ("Cash Card"), 
	MW("Mobile Wallet"), 
	EC("Amex EzeClick"),
	EW("EWallet"),
	CP("Credit Pool"),
	UPI("UPI"),
	DCPIN("DCPIN"),
	CORPCARD("Corporate Card"),
	JCASH("Jaldi Cash"),
	BANKIT("BankIt"),
	PAYNEARBY("PayNearBy"),
	FNP("Fino Pay"),
	OXIGEN_B2B("Oxigen B2B"),
	INDIFI("indifi"),
	PAYPAL("paypal"),
	GV("Gift Voucher"),
	TEZ("Google Tez"),
	TEZ_PWA("Google Tez"),
	PAYLTR("PayLater"),
	PAYMONK("PayMonk"),
	TSIWALLET("TSI"),
	SPICEMONEY("SpiceMoney"),
	PHONEPE("PhonePe"),
	CASHPOOL("Cash Pool"),
	ZESTMONEY("ZestMoney"),
	PAY4BIZ("Pay4Biz"),
	ROINET("Roinet"),
	VAKRANGEE("Vakrangee"),
	PAYTM("Paytm"),
	USELOCATOR("UseLocator"),
	UPINONMOTO("UPINonMoto"),
	EBIXCASH("EbixCash"),
	TWID("Pay With Rewards"),
	QUICKSON("Quickson"),
	CRED("Cred");
	
	private String value;

	public String getValue() {
		return this.value;
	}

	private PaymentOption(String value) {
		this.value = value;
	}
}
