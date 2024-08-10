package com.yatra.payment.qb.corporate.service.impl;

import java.util.List;
import java.util.Map;

import com.yatra.payment.ui.service.QuickBookExpressUIService;
import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.express.cards.client.ExpressCardsServiceClient;
import com.yatra.express.cards.util.ExpressCardsPropertiesConstants;
import com.yatra.express.cards.util.RSAEncryptionUtil;
import com.yatra.express.cards.v3.beans.ResponseStatus;
import com.yatra.express.cards.v3.beans.UserCardInfo;
import com.yatra.express.cards.v3.beans.UserCardInfoResponse;
import com.yatra.express.cards.v3.beans.UserCardPropertiesInfo;
import com.yatra.express.cards.v3.beans.UserCardsDisplayResponse;
import com.yatra.express.cards.v3.beans.GDSPropertyResponse;
import com.yatra.payment.offline.service.GDSService;
import com.yatra.payment.qb.corporate.bean.DeleteCardRequest;
import com.yatra.payment.qb.corporate.bean.GetCardsRequest;
import com.yatra.payment.qb.corporate.bean.PersonalQBDeleteCardRequest;
import com.yatra.payment.qb.corporate.bean.PersonalQBGetCardsRequest;
import com.yatra.payment.qb.corporate.bean.PersonalQBSaveCardRequest;
import com.yatra.payment.qb.corporate.bean.SaveCardRequest;
import com.yatra.payment.qb.corporate.service.QBService;
import com.yatra.payment.ui.display.beans.QuickBookCard;
import com.yatra.payment.ui.service.QuickBookExpressUIService;
import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.payment.ui.util.QuickBookUIUtil;
import java.security.PrivateKey;
import java.util.HashMap;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

@Service
public class PersonalQBServiceImpl implements QBService {

    private static final Logger logger = Logger.getLogger(PersonalQBServiceImpl.class);

    @Autowired
    private ExpressCardsServiceClient expressCardsServiceClient;
    @Autowired private QuickBookExpressUIService quickBookUIService;
    @Autowired private GDSService gdsService;

    @Override
    public List<QuickBookCard> getCards(GetCardsRequest getCardsRequest) {
        PersonalQBGetCardsRequest bean = null;
        try {
            bean = (PersonalQBGetCardsRequest)getCardsRequest;
            UserCardsDisplayResponse userCardsDisplayResponse = expressCardsServiceClient
                    .getCardsForDisplayWithUserId(bean.getTenantId(),bean.getSsoToken(),bean.getSsoUserId(), bean.getBookingType());
            List<UserCardInfo> userCardList = userCardsDisplayResponse.getUserCards();
            List<QuickBookCard> payswiftQBList = null ;
            if(userCardList != null && !userCardList.isEmpty()){
                payswiftQBList = quickBookUIService.convertPersonalQBCardListToPaySwiftCardList(userCardList);
                logger.info("no of personal qb card returned : " + userCardList.size());
            }else{
                ResponseStatus responseStatus = userCardsDisplayResponse.getResponse();
                logger.info("quickbook response for ssoToken :"+bean.getSsoToken()+" ,status:"+
                        responseStatus.getStatus()+" ,errorCode:"+responseStatus.getErrorCode()
                        + " , errorMessage : "+responseStatus.getErrorMessage());
            }
            return payswiftQBList ;
        } catch (Exception e) {
            logger.error("Exception occured while getting cards for ssoToken :"+bean.getSsoToken(),e);
            return null;
        }
    }

    @Override
    public String deleteCard(DeleteCardRequest deleteCardRequest, boolean isGDSCard, String userId) {
        PersonalQBDeleteCardRequest bean = null;
        try {
            bean = (PersonalQBDeleteCardRequest) deleteCardRequest;
            Long targetUserId = bean.getSsoUserId();
            ResponseStatus responseStatus;
            if( targetUserId != null && !targetUserId.equals(0L)){
                responseStatus = expressCardsServiceClient.removeCardWithUserId(bean.getTenantId(), bean.getSsoToken(),bean.getSsoUserId(),
                        bean.getCardId());
            }else {
                responseStatus = expressCardsServiceClient.removeCard(bean.getTenantId(), bean.getSsoToken(),
                        bean.getCardId());
            }
            if (isGDSCard && !StringUtils.isEmpty(responseStatus.getStatus()) && "SUCCESS".equalsIgnoreCase(responseStatus.getStatus())) {
                JSONObject params = new JSONObject();
                params.put("ssoToken", deleteCardRequest.getSsoToken());
                gdsService.deleteCard(params, userId);
            }
            return QuickBookUIUtil.getResponseJSON(responseStatus);
        } catch (Exception e) {
            logger.error("Exception occurred while deleting card for ssoToken : " + bean.getSsoToken(), e);
            return QuickBookUIUtil.getExceptionJSON(e.getMessage());
        }
    }

    @Override
    public String saveCard(SaveCardRequest saveCardRequest) {
        PersonalQBSaveCardRequest bean = null;
        try {
            bean = (PersonalQBSaveCardRequest) saveCardRequest;
            ResponseStatus responseStatus;
            Long targetUserId = bean.getSsoUserId();
            if( targetUserId != null && !targetUserId.equals(0L)){
                responseStatus = expressCardsServiceClient.addCardWithUserId(bean.getTenantId(), bean.getSsoToken(),bean.getSsoUserId(),
                        bean.getUserCardInfo());
            }
            else {
                responseStatus = expressCardsServiceClient.addCard(bean.getTenantId(), bean.getSsoToken(),
                        bean.getUserCardInfo());
            }
            return QuickBookUIUtil.getResponseJSON(responseStatus);

        } catch (Exception e) {
            logger.error("Exception occurred while saving card details for ssoToken : " + bean.getSsoToken(), e);
            return QuickBookUIUtil.getExceptionJSON(e.getMessage());
        }
    }

    @Override
    public List<?> getYatraCards(GetCardsRequest getCardsRequest) {
        //not supported yet
        return null;
    }

    @Override
    public String saveYatraCard(SaveCardRequest saveCardRequest) {
        //not supported yet
        return null;
    }

    @Override
    public String deleteYatraCard(DeleteCardRequest deleteCardRequest) {
        //not supported yet
        return null;
    }

    @Override
    public String addGDSCardProperty(String ssoToken, String cardId) {
        try {
            if (cardId == null) {
                GDSPropertyResponse addPropertyResponse = expressCardsServiceClient.addGDSProperty(ssoToken, null);
                if (!StringUtils.isEmpty(addPropertyResponse.getStatus()) && "SUCCESS".equalsIgnoreCase(addPropertyResponse.getStatus())) {
                    return "-1";
                }
                return null;
            }
            HashMap<String, String> property = new HashMap<String, String>();
            property.put(ExpressCardsPropertiesConstants.IS_GDS_CARD, "true");
            UserCardPropertiesInfo propertiesInfo = new UserCardPropertiesInfo();
            propertiesInfo.setCardId(cardId);
            propertiesInfo.setCardProperties(property);
            GDSPropertyResponse addPropertyResponse = expressCardsServiceClient.addGDSProperty(ssoToken, propertiesInfo);
            if (!StringUtils.isEmpty(addPropertyResponse.getStatus()) && "SUCCESS".equalsIgnoreCase(addPropertyResponse.getStatus())) {
                return addPropertyResponse.getMessage();
            }
        } catch (Exception ex) {
            logger.error("Exception occurred while deleting GDS card property for ssoToken : " + ssoToken, ex);
        }
        return null;
    }

    @Override
    public String saveCardToGDS(String ssoToken, String cardId, Long tenantId, String userId) {
        UserCardPropertiesInfo propertiesInfo = null;
        GDSPropertyResponse addPropertyResponse = null;
        String deletedPropertyCardId = null;
        try {
            HashMap<String, String> property = new HashMap<String, String>();
            property.put(ExpressCardsPropertiesConstants.IS_GDS_CARD, "true");
            propertiesInfo = new UserCardPropertiesInfo();
            propertiesInfo.setCardId(cardId);
            propertiesInfo.setCardProperties(property);
            addPropertyResponse = expressCardsServiceClient.addGDSProperty(ssoToken, propertiesInfo);
            if ("FAILURE".equalsIgnoreCase(addPropertyResponse.getStatus())){
                return null;
            }
            deletedPropertyCardId = addPropertyResponse.getMessage();
            UserCardInfoResponse response =  expressCardsServiceClient.getCardForPaymentWithUserId("PaymentPortal",ssoToken, cardId, null, null, getPrivateKey());
            if (response == null || response.getResponse() == null || "FAILURE".equalsIgnoreCase(response.getResponse().getStatus())) {
                //revert flow
                propertiesInfo.setCardId(deletedPropertyCardId);
                expressCardsServiceClient.addGDSProperty(ssoToken, propertiesInfo);
                return null;
            }
            JSONObject params = new JSONObject();
            params.put("ssoToken", ssoToken);
            String addCardStatus = gdsService.addCard(params, createGDSCardInfo(response.getUserCardInfo()), userId);
            if (addCardStatus == null) {
                //revert flow
                propertiesInfo.setCardId(deletedPropertyCardId);
                expressCardsServiceClient.addGDSProperty( ssoToken, propertiesInfo);
                return null;
            }
        } catch (Exception ex) {
            logger.error("Exception occurred while saving Existing card to GDS for ssoToken : " + ssoToken, ex);
            revert(propertiesInfo, ssoToken, addPropertyResponse, tenantId);
            return null;
        }
        return QuickBookUIUtil.getResponseJSON(true);
    }

    private UserCardInfo createGDSCardInfo(UserCardInfo qbUserCardInfo) throws Exception {
        UserCardInfo cardInfo = new UserCardInfo();
        cardInfo.setCardNumber1(qbUserCardInfo.getCardNumber1());
        cardInfo.setCardNumber2(qbUserCardInfo.getCardNumber2());
        cardInfo.setCardNumber3(qbUserCardInfo.getCardNumber3());
        cardInfo.setCardNumber4(qbUserCardInfo.getCardNumber4());
        cardInfo.setCardBrand(qbUserCardInfo.getCardBrand());
        cardInfo.setExpiryMonth(qbUserCardInfo.getExpiryMonth());
        String expiryYear = qbUserCardInfo.getExpiryYear();
        if (expiryYear.length() >= 2) {
            cardInfo.setExpiryYear(expiryYear.substring(expiryYear.length()-2, expiryYear.length()));
        } else {
            throw new Exception("Expiry year Length Exception. ExpiryYear: " + expiryYear);
        }
        return cardInfo;
    }

    @Override
    public String addCardToGDS(String ssoToken, PersonalQBSaveCardRequest saveCardRequest, String userId) {
        GDSPropertyResponse deleteCardPropertyResponse = null;
        String addCardQBResponse = null;
        UserCardPropertiesInfo propertiesInfo = null;
        try {
            HashMap<String, String> property = new HashMap<String, String>();
            property.put(ExpressCardsPropertiesConstants.IS_GDS_CARD, "true");
            propertiesInfo = new UserCardPropertiesInfo();
            propertiesInfo.setCardProperties(property);
            deleteCardPropertyResponse = expressCardsServiceClient.addGDSProperty(ssoToken, null);
            if (deleteCardPropertyResponse == null || "FAILURE".equalsIgnoreCase(deleteCardPropertyResponse.getStatus())) {
                logger.error("Error while deleting gds property ssoToken: " + ssoToken);
                return null;
            }
            String deletedPropertyCardId = deleteCardPropertyResponse.getMessage();
            UserCardInfo cardInfo = createGDSCardInfo(saveCardRequest.getUserCardInfo());
            HashMap<String, String> properties = saveCardRequest.getUserCardInfo().getCardProperties();
            properties.put(ExpressCardsPropertiesConstants.IS_GDS_CARD, "true");
            addCardQBResponse = saveCard(saveCardRequest);
            JSONObject addCardQBResponseJson = new JSONObject(addCardQBResponse);
            if (addCardQBResponseJson.has("status") && "SUCCESS".equalsIgnoreCase(addCardQBResponseJson.getString("status"))) {
                JSONObject params = new JSONObject();
                params.put("ssoToken", ssoToken);
                String response = gdsService.addCard(params, cardInfo, userId);
                if (response == null) {
                    //revert because card couldn't be saved in QB
                    propertiesInfo.setCardId(deletedPropertyCardId);
                    expressCardsServiceClient.addGDSProperty(ssoToken, propertiesInfo);
                    addCardQBResponse = QuickBookUIUtil.getResponseJSON("SUCCESS", "109", null, addCardQBResponseJson.getString("cardId"));
                }
            } else {
                //revert because card couldn't be saved at GDS
                propertiesInfo.setCardId(deletedPropertyCardId);
                expressCardsServiceClient.addGDSProperty(ssoToken, propertiesInfo);
            }
        } catch (Exception ex) {
            logger.error("Error while saving card to GDS and QB ssoToken: " + ssoToken, ex);
            propertiesInfo.setCardId(deleteCardPropertyResponse != null ? deleteCardPropertyResponse.getMessage() : null);
            expressCardsServiceClient.addGDSProperty( ssoToken, propertiesInfo);
        }
        return addCardQBResponse;
    }

    public void revert(UserCardPropertiesInfo propertiesInfo, String ssoToken, GDSPropertyResponse addPropertyReponse, Long tenantId) {
        propertiesInfo.setCardId(addPropertyReponse != null ? addPropertyReponse.getMessage() : null);
        expressCardsServiceClient.addGDSProperty(ssoToken, propertiesInfo);
    }

    private PrivateKey getPrivateKey() {
        PrivateKey privateKey = null;
        try {
            privateKey = RSAEncryptionUtil.readPrivateKeyFromFile("/PaySwiftPrivate.key");
        } catch (Exception exception) {
            logger.error("Private key could not be found..."+exception);
        }

        return privateKey;
    }

    @Override
    public String saveCorpCardViaPersonalFlow(String ssoToken, SaveCardRequest saveCardRequest) {
        // TODO Auto-generated method stub
        return null;
    }

    public Map<String, String> createParameterMapUsingRequestMap(String ssoToken,
                                                                 Map<String, String> requestMap) {
        UserCardInfoResponse response =  expressCardsServiceClient.getCardForPaymentWithUserId("PaymentPortal",ssoToken, requestMap.get("cardid"), null, null, getPrivateKey());
        UserCardInfo personalCardInfo = response.getUserCardInfo();
        Map<String,String> parameterMap = new HashMap<String, String>();

        populateCardInfoInRequestMap(personalCardInfo, parameterMap);
        parameterMap.put("saveAsCorpCard", "true");

			/*parameterMap.put(QuickBookUIUtil.CARD_BRAND, personalCardInfo.getCardBrand());
			parameterMap.put(PaymentUIUtil.CARD_TYPE, personalCardInfo.getCardType());
			parameterMap.put(PaymentUIUtil.CARD_HOLDER_NAME, personalCardInfo.getFirstName() + " " + personalCardInfo.getLastName());
			parameterMap.put(PaymentUIUtil.CARD_EXP_YEAR, personalCardInfo.getExpiryYear());
			parameterMap.put(PaymentUIUtil.CARD_EXP_MONTH, personalCardInfo.getExpiryMonth());
			parameterMap.put(PaymentUIUtil.CARD_NO, personalCardInfo.getCardNumber1() + personalCardInfo.getCardNumber2() + personalCardInfo.getCardNumber3() + personalCardInfo.getCardNumber4());
			parameterMap.put(QuickBookUIUtil.CARD_NAME, personalCardInfo.getCardName());
			parameterMap.put("saveAsCorpCard", "true");
			String cardAccesstype = personalCardInfo.getCardAccessType();
			if(cardAccesstype.equalsIgnoreCase(QuickBookUIUtil.CARD_ACCESS_TYPE_INT)) {
				parameterMap.put(PaymentUIUtil.IS_CARD_INTERNATIONAL, "true");
			} else parameterMap.put(PaymentUIUtil.IS_CARD_INTERNATIONAL, "false");

			parameterMap.put(key, value);
			parameterMap.put(key, value);
			parameterMap.put(key, value);


			String cardBrand = parameterMap.get(QuickBookUIUtil.CARD_BRAND);
			String cardType = parameterMap.get(PaymentUIUtil.CARD_TYPE);
			String firstName = PaymentUIUtil.getFirstName(parameterMap.get(PaymentUIUtil.CARD_HOLDER_NAME));
			String lastName = PaymentUIUtil.getLastName(parameterMap.get(PaymentUIUtil.CARD_HOLDER_NAME));

			String cardExpiryYear = parameterMap.get(PaymentUIUtil.CARD_EXP_YEAR);
			String cardExpiryMonth = parameterMap.get(PaymentUIUtil.CARD_EXP_MONTH);
			String cardNumber = parameterMap.get(PaymentUIUtil.CARD_NO);
			String productGroupArray = parameterMap.get(QuickBookUIUtil.PRODUCT_GROUP);
			String levelGroupJson = parameterMap.get(QuickBookUIUtil.LEVEL_GROUP);
			String cardId = parameterMap.get(QuickBookUIUtil.CARD_ID);
			String cardName = parameterMap.get(QuickBookUIUtil.CARD_NAME);
			parameterMap.get("saveAsCorpCard")

			parameterMap.get(PaymentUIUtil.IS_CARD_INTERNATIONAL)


			// Set address details for International cards.
			if (StringUtils.equalsIgnoreCase(isCardInternational, "true")) {
				Map<String, String> billingAddressMap = getBillingAddress(parameterMap);
				cardInfo.setCardProperties((HashMap<String, String>)billingAddressMap);
			}
			*/

        return parameterMap;
    }

    private void populateCardInfoInRequestMap(UserCardInfo userCardInfo, Map<String, String> requestMap) {
        if(userCardInfo == null)
            return;

        String cardNumber = userCardInfo.getCardNumber1() + userCardInfo.getCardNumber2() + userCardInfo.getCardNumber3() + userCardInfo.getCardNumber4();
        String cardExpiryYear = userCardInfo.getExpiryYear();
        String cardExpiryMonth = userCardInfo.getExpiryMonth();
        String cardBrand = userCardInfo.getCardBrand();
        String cardType = userCardInfo.getCardType();
        String firstName = userCardInfo.getFirstName();
        String lastName = userCardInfo.getLastName();
        String title = userCardInfo.getTitle();
        String cardAccessType = userCardInfo.getCardAccessType();

        addFieldToMap(requestMap, PaymentUIUtil.CARD_NO, cardNumber);
        addFieldToMap(requestMap, PaymentUIUtil.CARD_EXP_YEAR, cardExpiryYear);
        addFieldToMap(requestMap, PaymentUIUtil.CARD_EXP_MONTH, cardExpiryMonth);
        addFieldToMap(requestMap, QuickBookUIUtil.CARD_BRAND, cardBrand);
        addFieldToMap(requestMap, PaymentUIUtil.CARD_TYPE, cardType);
        addFieldToMap(requestMap, PaymentUIUtil.CARD_HOLDER_NAME, firstName + " " + lastName);
        addFieldToMap(requestMap, QuickBookUIUtil.CARD_HOLDER_TITLE, title);
        if(PaymentUIUtil.CARD_TYPE_AMEX.equals(cardBrand) && PaymentUIUtil.CARD_ACCESS_TYPE_INT.equals(cardAccessType))
            requestMap.put(PaymentUIUtil.IS_CARD_INTERNATIONAL, "true");
        // Add card holder's billing address details.
        populateBillingAddressDetails(requestMap, userCardInfo);
    }

    private void addFieldToMap(Map<String, String> requestMap, String fieldName, String fieldValue) {
        if(StringUtils.isNotBlank(fieldValue))
            requestMap.put(fieldName, fieldValue);
    }

    private void populateBillingAddressDetails(Map<String, String> requestMap, UserCardInfo userCardInfo) {

        Map<String, String> cardBillingAddressMap = userCardInfo.getCardProperties();

        if (cardBillingAddressMap == null || cardBillingAddressMap.isEmpty()) {
            return;
        }

        String billingAddress1 = MapUtils.getString(cardBillingAddressMap, ExpressCardsPropertiesConstants.ADDRESS1);
        String billingAddress2 = MapUtils.getString(cardBillingAddressMap, ExpressCardsPropertiesConstants.ADDRESS2);
        String billingCity = MapUtils.getString(cardBillingAddressMap, ExpressCardsPropertiesConstants.CITY);
        String billingState = MapUtils.getString(cardBillingAddressMap, ExpressCardsPropertiesConstants.STATE);
        String billingPin = MapUtils.getString(cardBillingAddressMap, ExpressCardsPropertiesConstants.PINCODE);
        String billingCountry = MapUtils.getString(cardBillingAddressMap, ExpressCardsPropertiesConstants.COUNTRY);
        String billingMobile = MapUtils.getString(cardBillingAddressMap, ExpressCardsPropertiesConstants.ISDCODE, "") + MapUtils.getString(cardBillingAddressMap, ExpressCardsPropertiesConstants.MOBILE_NUMBER, "");

        addFieldToMap(requestMap, PaymentUIUtil.BILLING_ADDRESS_LINE_1, billingAddress1);
        addFieldToMap(requestMap, PaymentUIUtil.BILLING_ADDRESS_LINE_2, billingAddress2);
        addFieldToMap(requestMap, PaymentUIUtil.BILLING_ADDRESS_LINE_CITY, billingCity);
        addFieldToMap(requestMap, PaymentUIUtil.BILLING_ADDRESS_LINE_STATE, billingState);
        addFieldToMap(requestMap, PaymentUIUtil.BILLING_ADDRESS_LINE_PIN, billingPin);
        addFieldToMap(requestMap, PaymentUIUtil.BILLING_ADDRESS_LINE_COUNTRY, billingCountry);
        addFieldToMap(requestMap, PaymentUIUtil.BILLING_ADDRESS_MOBILE, billingMobile);
    }
}
