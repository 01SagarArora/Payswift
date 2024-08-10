package com.yatra.payment.qb.corporate.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CorporateTokenizedCards 
{
	private List<CorporateTokenizedCard> corpTokenizedCards;

	public List<CorporateTokenizedCard> getCorpTokenizedCards() {
		return corpTokenizedCards;
	}

	public void setCorpTokenizedCards(List<CorporateTokenizedCard> corpTokenizedCards) {
		this.corpTokenizedCards = corpTokenizedCards;
	}
	
}
