package com.yatra.payment.qb.corporate.helper;

import java.util.Arrays;
import java.util.List;

public class APIMandatoryParameters {

	private static final String MERCHANT = "merchant";
	private static final String PRODUCT = "product";
	private static final String CARDID = "cardId";
	
	public static List<String> DELETE_CORP_CARD = Arrays.asList(CARDID);
	public static List<String> DELETE_PERSONAL_CARD = Arrays.asList(MERCHANT,PRODUCT,CARDID);
	
	public static List<String> SAVE_CORP_CARD = Arrays.asList();
	public static List<String> SAVE_PERSONAL_CARD = Arrays.asList(MERCHANT,PRODUCT);
	
	public static List<String> GET_CORP_CARD = Arrays.asList();
	public static List<String> GET_PERSONAL_CARD = Arrays.asList(MERCHANT,PRODUCT);
	
	
}
