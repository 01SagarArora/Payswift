package com.yatra.payment.qb.corporate.validator;

import org.springframework.stereotype.Component;

import com.yatra.payment.qb.corporate.bean.CorpQBDeleteCardRequest;
import com.yatra.payment.qb.corporate.bean.CorpQBGetCardsRequest;
import com.yatra.payment.qb.corporate.bean.CorpQBSaveCardRequest;

@Component
public class CorpQBServiceRequestValidator extends QBServiceRequestValidator{
	
	String validateGetCardsRequest(CorpQBGetCardsRequest getCardsRequest){
		return null;
	}
	
	String validateDeleteCardRequest(CorpQBDeleteCardRequest deleteCardRequest){
		return null;
	}
	
	String validateSaveCardRequest(CorpQBSaveCardRequest saveCardRequest){
		return null;
	}
}
