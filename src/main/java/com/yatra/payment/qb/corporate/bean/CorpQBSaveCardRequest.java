package com.yatra.payment.qb.corporate.bean;

import com.yatra.express.crp.cards.v3.beans.CorporateCardInfo;

public class CorpQBSaveCardRequest extends SaveCardRequest {
	
	private CorporateCardInfo corporateCardInfo;
	private String target;

	public CorporateCardInfo getCorporateCardInfo() {
		return corporateCardInfo;
	}

	public void setCorporateCardInfo(CorporateCardInfo corporateCardInfo) {
		this.corporateCardInfo = corporateCardInfo;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}
	
}
