package com.yatra.payment.qb.corporate.bean;

import com.yatra.express.crp.cards.v3.beans.CorporateCardInfo;
import com.yatra.payment.ui.display.beans.QuickBookCard;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;

import java.util.Collections;
import java.util.List;

@JsonPropertyOrder({"status","CORPORATE","PERSONAL","BTAINPERSONAL"})
public class GetBtaCtaCaseCardsResponse extends GetCardsResponse {


	@JsonProperty("BTAINPERSONAL")
	private List<String> btaInPersonalCards = Collections.emptyList();


	public GetBtaCtaCaseCardsResponse(List<CorporateCardInfo> corporateCards, List<QuickBookCard> personalCards){
		super(corporateCards,personalCards);
	}

	public GetBtaCtaCaseCardsResponse(List<QuickBookCard> personalCards) {
		super(personalCards);
	}

	public void addToBtaInPersonalCards(String cardId){
		if (btaInPersonalCards == null){
			btaInPersonalCards = Collections.emptyList();
		}

		btaInPersonalCards.add(cardId);
	}

	public List<String> getBtaInPersonalCards() {
		return btaInPersonalCards;
	}

	public void setBtaInPersonalCards(List<String> btaInPersonalCards) {
		this.btaInPersonalCards = btaInPersonalCards;
	}

}
