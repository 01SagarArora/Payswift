package com.yatra.payment.qb.corporate.validator;

import com.yatra.payment.qb.corporate.bean.DeleteCardRequest;
import com.yatra.payment.qb.corporate.bean.GetCardsRequest;
import com.yatra.payment.qb.corporate.bean.SaveCardRequest;


public abstract class QBServiceRequestValidator {
	
	protected String validateGetCardsRequest(GetCardsRequest getCardsRequest){
		return null;
	}
	
	protected String validateDeleteCardRequest(DeleteCardRequest deleteCardRequest){
		return null;
	}
	
	protected String validateSaveCardRequest(SaveCardRequest saveCardRequest){
		return null;
	}
	
	
}
