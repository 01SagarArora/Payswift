package com.yatra.payment.client.service.impl;

import java.util.Map;

import com.yatra.payment.client.beans.CardHolderDetails;

public interface PaymentDivService {

	
	
	/**
	 * To return payment option details. These details are displayed on confirmation page.
	 * 
	 * @param paymentParamMap
	 * @return
	 */
	public String getPaymentOptionJson(Map<String, String> paymentParamMap) throws Exception;
	
	/**
	 * To generate payment parameters map from payment parameters string object.
	 * 
	 * @param paymentParams
	 * @return
	 * @throws Exception
	 */
	public Map<String, String> getPaymentParamsMap(String paymentParams) throws Exception;
	
	/**
	 * To generate promocode parameters map from promocode parameters string object.
	 * 
	 * @param promoCodePaymentParams
	 * @return
	 * @throws Exception
	 */
	public Map<String, String> getPromoCodeParamsMap(String promoCodePaymentParams) throws Exception;	
	
	/**
	 * To fetch card details for international payments.
	 * 
	 * @param paymentParams
	 * @return
	 * @throws Exception
	 */
	public CardHolderDetails getCardHolderDetails(String paymentParams) throws Exception;
	
}
