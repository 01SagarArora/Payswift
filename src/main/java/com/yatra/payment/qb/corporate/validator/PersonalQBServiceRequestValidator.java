package com.yatra.payment.qb.corporate.validator;

import org.springframework.stereotype.Component;

import com.yatra.payment.qb.corporate.bean.PersonalQBDeleteCardRequest;
import com.yatra.payment.qb.corporate.bean.PersonalQBGetCardsRequest;
import com.yatra.payment.qb.corporate.bean.PersonalTokenSaveCardRequest;

@Component
public class PersonalQBServiceRequestValidator extends QBServiceRequestValidator {
	
	String validateGetCardsRequest(PersonalQBGetCardsRequest getCardsRequest){
		return null;
	}
	
	String validateDeleteCardRequest(PersonalQBDeleteCardRequest deleteCardRequest){
		return null;
	}
	
	String validateSaveCardRequest(PersonalTokenSaveCardRequest saveCardRequest){
		return null;
	}	
}
