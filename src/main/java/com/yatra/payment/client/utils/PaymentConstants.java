package com.yatra.payment.client.utils;

public class PaymentConstants {
	
	// Common Constants specific to Offline Payments.
	public static final String OFFLINE_PAYMENT_OPTION = "offlineOption";
	public static final String OFFLINE_AMOUNT_ALLOCATED = "amountAllocated" ;
	public static final String OFFLINE_COLLECTOR_NAME = "clName" ;
	public static final String OFFLINE_AUTH_CODE = "authCode";
	public static final String OFFLINE_PAYMENT_METHOD_TYPE = "paymentMethodType";
	
	// Constants specific to Offline Cheque Payments.
	public static final String OFFLINE_CHEQUE_PAYMENT = "CHEQUE";
	public static final String OFFLINE_DRAWER_NAME = "drawerName";
	public static final String OFFLINE_CHEQUE_NO = "chequeNo";
	public static final String OFFLINE_CHEQUE_DATE = "chequeDate";
	public static final String OFFLINE_BANK_ID = "bankID";
	
	// Constants specific to Offline Cash Payments.
	public static final String OFFLINE_CASH_PAYMENT = "CASH";
	
	// Constants specific to Offline Card Swipe Payments (CC/ DC).
	public static final String OFFLINE_CC_PAYMENT = "Offline Credit Card";
	public static final String OFFLINE_DC_PAYMENT = "Offline Debit Card";
	public static final String OFFLINE_CARD_HOLDER_NAME = "cardHolderName";
	public static final String OFFLINE_CARD_ISSUER_NAME = "cardIssuerName";
	public static final String OFFLINE_CARD_NO = "cardNumber";
	public static final String OFFLINE_CARD_CODE = "cardCode";
	public static final String OFFLINE_CARD_TYPE = "cardType";
	public static final String OFFLINE_CARD_EXPIRY_DATE = "expireDate";
	public static final String OFFLINE_EFFECTIVE_DATE = "effectiveDate";
	
	// Constants specific to payment option name
	public static final String PAY_OP_CC_NAME = "Credit Card";
	public static final String PAY_OP_DC_NAME = "Debit Card";
	public static final String PAY_OP_EMI_NAME = "EMI";
	public static final String PAY_OP_QB_NAME = "Quick Book";
	public static final String PAY_OP_NB_NAME = "Net Banking";
	public static final String PAY_OP_REWARDS_NAME = "Reward Points";
	public static final String PAY_OP_ATM_NAME = "ATM Card";
	public static final String PAY_OP_ITZ_NAME = "Cash Card";
	public static final String PAY_OP_MW_NAME = "Mobile Wallet";
	public static final String PAY_OP_EC_NAME = "ezeClick";
	public static final String PAY_OP_EW_NAME = "eCash";
	public static final String PAY_OP_CP_NAME = "Credit Pool";
	public static final String PAY_OP_CORPCARD_NAME = "Corporate Card";
	
	
	// Payment Options 
	public static final String PAY_OP_CC_CODE = "cc";
	public static final String PAY_OP_DC_CODE = "dc";
	public static final String PAY_OP_EMI_CODE = "emi";
	public static final String PAY_OP_QB_CODE = "qb";
	public static final String PAY_OP_NB_CODE = "nb";
	public static final String PAY_OP_REWARDS_CODE = "rewards";
	public static final String PAY_OP_ATM_CODE = "atm";
	public static final String PAY_OP_ITZ_CODE = "itz";
	public static final String PAY_OP_MW_CODE = "mw";
	public static final String PAY_OP_PAY_LATER = "payltr";
	public static final String PAY_OP_EC_CODE = "ec";
	public static final String PAY_OP_EW_CODE = "ew";
	public static final String PAY_OP_CP_CODE = "cp";
	public static final String PAY_OP_CORPCARD_CODE = "corpCard";

	//HTTP Constants
	public static final String UTF_8_ENCODING = "UTF-8";
	public static final String SEPARATOR = "&";
	
	// Parameters
	public static final String PAYMENT_OPTION_PARAM = "payop";
	public static final String PREVIOUS_PAYMENT_OPTION_PARAM = "previousPayOp";
	public static final Object REWARD_AMOUNT_PARAM = "rewamount";
	public static final Object CARD_ID_PARAM = "cardid";
	
	// Card specific parameters,
	public static final String CARD_NUMBER_PARAM = "cno";
	public static final String CARD_HOLDER_NAME_PARAM = "cardholder_name";
	public static final String CARD_EXPIRY_YEAR_PARAM = "cexpy";
	public static final String CARD_EXPIRY_MONTH_PARAM = "cexpm";
	public static final String CARD_TYPE_PARAM = "ctype";
	public static final String CARD_CVV_PARAM = "ccsc";
	public static final String IS_CARD_INTERNATIONAL_PARAM = "isCardInternational";
	public static final String CARD_HOLDER_BILLING_ADDRESS_PARAM = "ba1";
	public static final String CARD_HOLDER_BILLING_CITY_PARAM = "bacy";
	public static final String CARD_HOLDER_BILLING_STATE_PARAM = "bast";
	public static final String CARD_HOLDER_BILLING_COUNTRY_PARAM = "bacu";
	public static final String CARD_HOLDER_BILLING_PIN_PARAM = "bapi";
	public static final String CARD_HOLDER_BILLING_ISD_CODE_PARAM = "baisd";
	public static final String CARD_HOLDER_BILLING_MOBILE_PARAM = "bamob";
	
	public static final String MERCHANT_CODE = "merchant_code";
	public static final String PRODUCT_CODE = "product_code";
	public static final String TRIP_ID = "ttid";
	public static final String MTXN_REF_ID = "mtxnid";
	public static final String AMOUNT = "amount";
	public static final String MODE_AUTH = "auth";
	public static final String WALLET_ID = "wallet_id";
	public static final String CUSTOMER_EMAIL = "cust_email";
	public static final String ZERO_AMOUNT = "0.0";
	public static final String ORIGIN = "origin";
	public static final String ORIGIN_SERVER = "server";
	public static final String PRODUCT = "product";
	public static final String CHANNEL = "channel";
	public static final String DEVICE = "device";
	public static final String ACTUAL_TTID = "actual_ttid";
	
	// Pay div call request parameters
	public static final String MERCHANT_CODE_PAY_DIV_PARAM = "merchant";
	public static final String PRODUCT_CODE_PAY_DIV_PARAM = "product";
	public static final String SSO_TOKEN = "ssoToken";
	public static final String LANGAUGE_PAY_DIV_PARAM = "language";
	public static final String VALIDATE = "validate";
	public static final String YLPMAX = "ylp_max";
	public static final String INCL_JS_CSS = "inclJsCss";
	
        // Default TenantId
        public static final String DEFAULT_TENANTID = "1011";
	public static final String MULTI_PAY_OPTION_DEFAULT_SECURE = "secure";
	public static final String MULTI_PAY_OPTION_OTP = "otp";
	public static final String MULTI_PAY_OPTION_ATM = "atm";
	public static final String SEPARATOR_PIPE = "|";
}
