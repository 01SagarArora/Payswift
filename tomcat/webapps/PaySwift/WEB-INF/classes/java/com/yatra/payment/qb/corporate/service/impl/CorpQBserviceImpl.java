package com.yatra.payment.qb.corporate.service.impl;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.express.crp.cards.client.B2BExpressCardsServiceClient;
import com.yatra.express.crp.cards.v3.beans.CorporateCardInfo;
import com.yatra.express.crp.cards.v3.beans.CorporateCardsDisplayResponse;
import com.yatra.express.crp.cards.v3.beans.ResponseStatus;
import com.yatra.payment.qb.corporate.bean.CorpQBDeleteCardRequest;
import com.yatra.payment.qb.corporate.bean.CorpQBGetCardsRequest;
import com.yatra.payment.qb.corporate.bean.CorpQBSaveCardRequest;
import com.yatra.payment.qb.corporate.bean.DeleteCardRequest;
import com.yatra.payment.qb.corporate.bean.GetCardsRequest;
import com.yatra.payment.qb.corporate.bean.PersonalQBSaveCardRequest;
import com.yatra.payment.qb.corporate.bean.SaveCardRequest;
import com.yatra.payment.qb.corporate.service.QBService;
import com.yatra.payment.ui.util.CorporateQuickBookUIUtil;

@Service
public class CorpQBserviceImpl implements QBService {

	private static final Logger logger = Logger.getLogger(CorpQBserviceImpl.class);
	@Autowired
	private B2BExpressCardsServiceClient b2BExpressCardsServiceClient;

	@Override
	public List<CorporateCardInfo> getCards(GetCardsRequest getCardsRequest) {
		CorpQBGetCardsRequest bean = null ;
		try {
			bean = (CorpQBGetCardsRequest) getCardsRequest ;
			CorporateCardsDisplayResponse corporateCardsDisplayResponse =  b2BExpressCardsServiceClient.getSsoCardsForDisplay(bean.getSsoToken(), null);
			ResponseStatus responseStatus = corporateCardsDisplayResponse.getResponse();
			if(!responseStatus.getStatus().equalsIgnoreCase("SUCCESS")) {
				logger.info("qb response for ssoToken :"+bean.getSsoToken()+" ,errorCode :"+responseStatus.getErrorCode()
						+" ,errorMessage :"+responseStatus.getErrorMessage());
				return null;
			}
			logger.info("no of corporate qb card returned : " + corporateCardsDisplayResponse.getCorporateCards().size());
			return corporateCardsDisplayResponse.getCorporateCards();
		} catch (Exception e) {
			logger.error("Error occurred while getting corporate cards.", e);
			return null;
		}
	}

	@Override
	public String deleteCard(DeleteCardRequest deleteCardRequest, boolean isGDSCard, String userId) {
		CorpQBDeleteCardRequest bean = null;
		try {
			bean = (CorpQBDeleteCardRequest) deleteCardRequest;
			ResponseStatus responseStatus = b2BExpressCardsServiceClient.removeCard(bean.getSsoToken(),
					bean.getCardId());
			return CorporateQuickBookUIUtil.getResponseJSON(responseStatus);

		} catch (Exception e) {
			logger.error("Exception occurred while deleting card for ssoToken : " + bean.getSsoToken(), e);
			return CorporateQuickBookUIUtil.getExceptionJSON(e.getMessage());
		}
	}

	@Override
	public String saveCard(SaveCardRequest saveCardRequest) {
		CorpQBSaveCardRequest bean = null;
		try {
			bean = (CorpQBSaveCardRequest) saveCardRequest;
			ResponseStatus response = b2BExpressCardsServiceClient.addSsoCard(bean.getSsoToken(), bean.getCorporateCardInfo(), bean.getTarget());
			return CorporateQuickBookUIUtil.getResponseJSON(response);
		} catch (Exception e) {
			logger.error("Exception occurred while saving card details for ssoToken : " + bean.getSsoToken(), e);
			return CorporateQuickBookUIUtil.getExceptionJSON(e.getMessage());
		}
	}
        
        @Override
        public List<?> getYatraCards(GetCardsRequest getCardsRequest) {
            CorpQBGetCardsRequest bean = null ;
            try {
                    bean = (CorpQBGetCardsRequest) getCardsRequest ;
                    CorporateCardsDisplayResponse corporateCardsDisplayResponse =  b2BExpressCardsServiceClient.getYatraCardsForDisplay(getCardsRequest.getSsoToken());
                    ResponseStatus responseStatus = corporateCardsDisplayResponse.getResponse();
                    if(!responseStatus.getStatus().equalsIgnoreCase("SUCCESS")) {
                        logger.info("qb response for ssoToken :"+bean.getSsoToken()+" ,errorCode :"+responseStatus.getErrorCode()
                                        +" ,errorMessage :"+responseStatus.getErrorMessage());
                        return null;
                    }
                    logger.info("no of corporate qb card returned : " + corporateCardsDisplayResponse.getCorporateCards().size());
                    return corporateCardsDisplayResponse.getCorporateCards();
            } catch (Exception e) {
                    logger.error("Error occurred while getting corporate cards.", e);
                    return null;
            }
        }
        
        @Override
        public String saveYatraCard(SaveCardRequest saveCardRequest) {
            CorpQBSaveCardRequest bean = null;
            try {
                    bean = (CorpQBSaveCardRequest) saveCardRequest;
                    ResponseStatus response = b2BExpressCardsServiceClient.addYatraCard(bean.getCorporateCardInfo(), bean.getTarget());
                    return CorporateQuickBookUIUtil.getResponseJSON(response);
            } catch (Exception e) {
                    logger.error("Exception occurred while saving Yatra card details for accessToken : " + bean.getSsoToken(), e);
                    return CorporateQuickBookUIUtil.getExceptionJSON(e.getMessage());
            }
        
        }
        
        @Override
        public String deleteYatraCard(DeleteCardRequest deleteCardRequest) {
            CorpQBDeleteCardRequest bean = null;
            try {
                    bean = (CorpQBDeleteCardRequest) deleteCardRequest;
                    ResponseStatus responseStatus = b2BExpressCardsServiceClient.removeYatraCard(bean.getCardId());
                    return CorporateQuickBookUIUtil.getResponseJSON(responseStatus);
            } catch (Exception e) {
                    logger.error("Exception occurred while deleting Yatra card for ssoToken : " + bean.getSsoToken(), e);
                    return CorporateQuickBookUIUtil.getExceptionJSON(e.getMessage());
            }
        }
        
        @Override
        public String addGDSCardProperty(String ssoToken, String cardId) {
            return null;
        }
        
        @Override
        public String saveCardToGDS(String ssoToken, String cardId, Long tenantId, String userId) {
            //not supported yet
            return null;
        }

        @Override
        public String addCardToGDS(String ssoToken, PersonalQBSaveCardRequest saveCardRequest, String userId) {
            //not supported yet
            return null;
        }

		@Override
		public String saveCorpCardViaPersonalFlow(String ssoToken, SaveCardRequest saveCardRequest) {
			CorpQBSaveCardRequest bean = null;
            try {
                    bean = (CorpQBSaveCardRequest) saveCardRequest;
                    ResponseStatus response = b2BExpressCardsServiceClient.addCorporateCardViaPersonalFlow(ssoToken, bean.getCorporateCardInfo());
                    return CorporateQuickBookUIUtil.getResponseJSON(response);
            } catch (Exception e) {
                    logger.error("Exception occurred while saving Yatra card details for accessToken : " + bean.getSsoToken(), e);
                    return CorporateQuickBookUIUtil.getExceptionJSON(e.getMessage());
            }
        
        }
}
