package com.yatra.payment.qb.corporate.bean;

import java.util.Collections;
import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;

import com.yatra.express.crp.cards.v3.beans.CorporateCardInfo;
import com.yatra.payment.qb.corporate.constant.QBConstant;
import com.yatra.payment.ui.display.beans.QuickBookCard;
import org.codehaus.jackson.annotate.JsonRawValue;

@JsonPropertyOrder({"status","CORPORATE","PERSONAL"})
public class GetCardsResponse {
	
	private String status = QBConstant.STATUS_SUCCESS;
	
	@JsonProperty("PERSONAL")
	private List<QuickBookCard> personalCards = Collections.emptyList();
	@JsonProperty("CORPORATE")
	private List<CorporateCardInfo> corporateCards = Collections.emptyList();
	@JsonProperty("allowOfflineBookings")
        private boolean allowOfflineBookings = Boolean.TRUE;
        @JsonProperty("users")
        private String users = null;
        @JsonProperty("entities")
        private String entities = null;
        
	public GetCardsResponse(){}
	
	public GetCardsResponse(List<CorporateCardInfo> corporateCards,List<QuickBookCard> personalCards){
		if(corporateCards != null) this.corporateCards = corporateCards;
		if(personalCards != null) this.personalCards = personalCards;
	}
	
	public GetCardsResponse(List<QuickBookCard> personalCards){
		if(personalCards != null) this.personalCards = personalCards;
	}
	
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public List<QuickBookCard> getPersonalCards() {
		return personalCards;
	}
	public void setPersonalCards(List<QuickBookCard> personalCards) {
		if(personalCards != null) this.personalCards = personalCards;
	}
	public List<CorporateCardInfo> getCorporateCards() {
		return corporateCards;
	}
	public void setCorporateCards(List<CorporateCardInfo> corporateCards) {
		if(corporateCards != null) this.corporateCards = corporateCards;
	}

        public boolean isAllowOfflineBookings() {
            return allowOfflineBookings;
        }

        public void setAllowOfflineBookings(boolean allowOfflineBookings) {
            this.allowOfflineBookings = allowOfflineBookings;
        }

        @JsonRawValue
        public String getUsers() {
            return users;
        }

        public void setUsers(String users) {
            this.users = users;
        }

        @JsonRawValue
        public String getEntities() {
            return entities;
        }

        public void setEntities(String entities) {
            this.entities = entities;
        }
	
}
