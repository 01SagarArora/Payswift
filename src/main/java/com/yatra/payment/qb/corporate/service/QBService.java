package com.yatra.payment.qb.corporate.service;

import java.util.List;
import com.yatra.payment.qb.corporate.bean.DeleteCardRequest;
import com.yatra.payment.qb.corporate.bean.GetCardsRequest;
import com.yatra.payment.qb.corporate.bean.PersonalQBSaveCardRequest;
import com.yatra.payment.qb.corporate.bean.SaveCardRequest;

public interface QBService {

	public List<?> getCards(GetCardsRequest getCardsRequest);

	public String deleteCard(DeleteCardRequest deleteCardRequest, boolean isGDSCard, String userId);

	public String saveCard(SaveCardRequest saveCardRequest);

	public List<?> getYatraCards(GetCardsRequest getCardsRequest);

	public String saveYatraCard(SaveCardRequest saveCardRequest);

	public String deleteYatraCard(DeleteCardRequest deleteCardRequest);

	public String addGDSCardProperty(String ssoToken, String cardId);

	public String saveCardToGDS(String ssoToken, String cardId, Long tenantId, String userId);

	public String addCardToGDS(String ssoToken, PersonalQBSaveCardRequest saveCardRequest, String userId);

	public String saveCorpCardViaPersonalFlow(String ssoToken, SaveCardRequest saveCardRequest);

}
