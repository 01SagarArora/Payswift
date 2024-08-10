package com.yatra.payment.ui.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import javax.servlet.http.HttpServletResponse;

import com.yatra.payment.ui.exceptions.PayswiftException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import com.yatra.express.cards.client.ExpressCardsServiceClient;
import com.yatra.express.cards.util.ExpressCardsPropertiesConstants;
import com.yatra.express.cards.v3.beans.ResponseStatus;
import com.yatra.express.cards.v3.beans.UserCardInfo;
import com.yatra.express.cards.v3.beans.UserCardsDisplayResponse;
import com.yatra.payment.cache.service.impl.QuickBookCardBrandCache;
import com.yatra.payment.cache.service.impl.QuickBookCardTypeCache;
import com.yatra.payment.ui.builder.QuickBookOptionBuilder;
import com.yatra.payment.ui.dao.bean.UIQBCardBrandMaster;
import com.yatra.payment.ui.dao.bean.UIQBCardTypeMaster;
import com.yatra.payment.ui.display.beans.QuickBookCard;
import com.yatra.payment.ui.display.beans.QuickBookPaymentOption;
import com.yatra.payment.ui.enums.Status;
import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.payment.ui.util.QuickBookUIUtil;
import com.yatra.payment.ui.util.SSOAndUPSCode;
import com.yatra.platform.commons.property.PropertyManager;

import static com.yatra.payment.ui.enums.FailureCode.*;

@Service("quickBookExpressUIService")
public class QuickBookExpressUIService {

    private static Logger logger = Logger.getLogger(QuickBookUIService.class);

    @Autowired private PropertyManager yatraPropertyReader;
    @Autowired private ExpressCardsServiceClient expressCardsServiceClient;
    @Autowired private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Autowired private TenantService tenantService;
    @Autowired private UserLoginService userLoginService;
    @Autowired private QuickBookSaveCardService quickBookSaveCardService;
    @Autowired private QuickBookValidator quickBookValidator;
    @Autowired private QuickBookOptionBuilder quickBookOptionBuilder;
    @Autowired private QuickBookCardTypeCache quickBookCardTypeCache;
    @Autowired private QuickBookCardBrandCache quickBookCardBrandCache;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public QuickBookPaymentOption getQuickBookBean(String merchantCode, String productCode, String ssoToken, Long ssoUserId, String bookingType) {
        logger.debug("Initiating getQuickBookBean() for ssoToken : " + ssoToken + " merchant : " + merchantCode + " product : " + productCode);

        quickBookValidator.validateGetQBCardsRequest(merchantCode, productCode, ssoToken);

        QuickBookPaymentOption quickBookPaymentOption = createQBBeanForCurrentRequest(merchantCode, productCode);

        Long tenantId = tenantService.getTenantId(merchantCode, productCode);
        logger.debug("Tenant ID : " + tenantId + " for  ssoToken : " + ssoToken + ", merchant : " + merchantCode + " and product : " + productCode);

        addQBCards(quickBookPaymentOption, tenantId, ssoToken, ssoUserId, bookingType);

        return quickBookPaymentOption;
    }

    public QuickBookPaymentOption quickBookPaymentOptionBean(final String merchantCode, final String productCode, final String ssoToken, final Long ssoUserId, final String bookingType) throws Exception {
        Future<QuickBookPaymentOption> future = threadPoolTaskExecutor.submit(new QBCardsFetchTask(merchantCode, productCode, ssoToken, ssoUserId, bookingType));
        return future.get(getQBFetchTimeOut(), TimeUnit.SECONDS);
    }

    public QuickBookPaymentOption getYatraQBAuthBean(String merchantCode, String productCode, String email, String password, HttpServletResponse response) {

        String userType = getUserTypeFromProduct(productCode);
        try {
            // Request SingleSignOnService for login.
            String ssoToken = userLoginService.login(email, password,userType);

            // If ssoToken = null, authentication failed.
            if (StringUtils.isEmpty(ssoToken))
                return getQBAuthenticationFailedBean(userType);
            QuickBookPaymentOption quickBookPaymentOption;
            try {
                quickBookPaymentOption = getQuickBookBean(merchantCode, productCode, ssoToken, 0L, null);
            } catch (Exception e) {
                logger.error("Exception occurred while getting QuickBook Bean : ", e);
                quickBookPaymentOption = getQBErrorBean(QuickBookUIUtil.ERROR_CODE_EXCEPTION, e.getMessage());
            }

            // Add new ssoToken to Cookie
            userLoginService.addSSOTokenCookie(response, ssoToken);

            return quickBookPaymentOption;

        } catch (Exception e) {
            logger.error("Exception occurred while re-authentcating customer with email : " + email + " : ", e);
            return getQBAuthenticationFailedBean(userType);
        }
    }

    public String getYatraQBAuthBeanJSON(String merchantCode, String productCode, String email, String password, HttpServletResponse response) {
        ObjectMapper mapper = new ObjectMapper();
        String quickBookJSON = null;
        try {
            QuickBookPaymentOption quickBookPaymentOption = getYatraQBAuthBean(merchantCode, productCode, email, password, response);
            quickBookJSON = mapper.writeValueAsString(quickBookPaymentOption);
        } catch (Exception e) {
            logger.error("Exception occurred while getting QuickBook JSON for merchant : " + merchantCode + " and product : " + productCode + " : ", e);
        }
        return quickBookJSON;
    }

    public QuickBookPaymentOption getFacebookQBAuthBean(String merchantCode, String productCode, String accessToken, String sourcePage, HttpServletResponse response) {

        String authMode = QuickBookUIUtil.AUTH_MODE_FB;
        try {
            // If accessToken = null, authentication failed.
            if (StringUtils.isEmpty(accessToken))
                return getQBAuthenticationFailedBean(authMode);

            Map<String, String> fbLoginInfo = userLoginService.getFBLoginInfo(accessToken, sourcePage);
            String ssoToken = fbLoginInfo.get(QuickBookUIUtil.SSO_TOKEN_KEY);
            String responseCode = fbLoginInfo.get(QuickBookUIUtil.RESPONSE_CODE_KEY);

            if (!StringUtils.isEmpty(ssoToken) && StringUtils.equals(responseCode, SSOAndUPSCode.SOCIAL_SERVICE_SUCCESSFULLY_LOGGED_IN_)) {

                QuickBookPaymentOption quickBookPaymentOption;
                try {
                    quickBookPaymentOption = getQuickBookBean(merchantCode, productCode, ssoToken, 0L, null);
                } catch (Exception e) {
                    logger.error("Exception occurred while getting QuickBook Bean : ", e);
                    quickBookPaymentOption = getQBErrorBean(QuickBookUIUtil.ERROR_CODE_EXCEPTION, e.getMessage());
                }

                // Add new ssoToken to Cookie
                userLoginService.addSSOTokenCookie(response, ssoToken);

                return quickBookPaymentOption;
            }

            return getQBAuthenticationFailedBean(authMode);

        } catch (Exception e) {
            logger.error("Exception occurred while re-authentcating FB customer ", e);
            return getQBAuthenticationFailedBean(authMode);
        }
    }

    public String getFacebookQBAuthBeanJSON(String merchantCode, String productCode, String accessToken, String sourcePage, HttpServletResponse response) {
        ObjectMapper mapper = new ObjectMapper();
        String quickBookJSON = null;
        try {
            QuickBookPaymentOption quickBookPaymentOption = getFacebookQBAuthBean(merchantCode, productCode, accessToken, sourcePage, response);
            quickBookJSON = mapper.writeValueAsString(quickBookPaymentOption);
        } catch (Exception e) {
            logger.error("Exception occurred while getting QuickBook JSON for merchant : " + merchantCode + " and product : " + productCode + " : ", e);
        }
        return quickBookJSON;
    }

    public String deleteQuickBookCard(String ssoToken, Map<String,String> requestMap) {
        try {

            // Check if user is authenticated (when login has not expired).
            if(!PaymentUIUtil.isTokenValid(ssoToken, requestMap.get("csrfToken"))){
                QuickBookUIUtil.getExceptionJSON("Invalid Csrf Token");
            }
            boolean isAuthenticated = userLoginService.isUserAuthenticated(ssoToken);
            if(!isAuthenticated) {
                return QuickBookUIUtil.getAuthRequiredJSON();
            }
            // Validate request.
            String error = quickBookValidator.validateDeleteCardRequest(ssoToken, requestMap);
            boolean isValid = StringUtils.isEmpty(error);
            if (!isValid)
                return QuickBookUIUtil.getInvalidRequestJSON(error);

            // Get tenant Id for merchant product.
            Long tenantId = getTenantId(requestMap);

            // Call Express Checkout to delete card.
            String cardId = requestMap.get("cardid");
            ResponseStatus responseStatus = expressCardsServiceClient.removeCard(tenantId, ssoToken, cardId);
            return QuickBookUIUtil.getResponseJSON(responseStatus);

        } catch (Exception e) {
            logger.error("Exception occurred while deleting card for ssoToken : " + ssoToken, e);
            return QuickBookUIUtil.getExceptionJSON(e.getMessage());
        }
    }

    public String saveQuickBookCard(String ssoToken, Map<String, String> requestMap) {
        return quickBookSaveCardService.saveQuickBookCard(ssoToken, requestMap);
    }

    public String updatePassword(String ssoToken, Map<String, String> requestMap) {
        try {

            // Check if user is authenticated (when login has not expired).
            boolean isAuthenticated = userLoginService.isUserAuthenticated(ssoToken);
            if(!isAuthenticated) {
                return QuickBookUIUtil.getAuthRequiredJSON();
            }

            String emailId = userLoginService.getEmailForSSOToken(ssoToken);
            requestMap.put("email", emailId);

            // Validate request.
            String error = quickBookValidator.validateUpdatePassRequest(ssoToken, requestMap);
            boolean isValid = StringUtils.isEmpty(error);
            if (!isValid)
                return QuickBookUIUtil.getInvalidRequestJSON(error);

            // Get request parameters.
            String oldPassword = requestMap.get("oldPassword");
            String newPassword = requestMap.get("newPassword");

            // Update password.
            boolean status = userLoginService.updatePassword(ssoToken, oldPassword, newPassword);
            return QuickBookUIUtil.getResponseJSON(status);

        } catch (Exception e) {
            logger.error("Exception occurred while updating password for ssoToken : " + ssoToken, e);
            return QuickBookUIUtil.getExceptionJSON(e.getMessage());
        }
    }

    public String isPasswordCompromised(String ssoToken) {
        try {
            // Validate request.
            String error = quickBookValidator.validatePassCompromisedRequest(ssoToken);
            boolean isValid = StringUtils.isEmpty(error);
            if (!isValid)
                return QuickBookUIUtil.getInvalidRequestJSON(error);

            // Check if password is compromised.
            boolean status = userLoginService.isPasswordCompromised(ssoToken);
            return QuickBookUIUtil.getResponseJSON(status);

        } catch (Exception e) {
            logger.error("Exception occurred while determining if password is compromised for ssoToken : " + ssoToken, e);
            return QuickBookUIUtil.getExceptionJSON(e.getMessage());
        }
    }

    private Long getTenantId(Map<String, String> requestMap) {
        String merchantCode = requestMap.get("merchant");
        String productCode = requestMap.get("product");
        return tenantService.getTenantId(merchantCode, productCode);
    }

    private class QBCardsFetchTask implements Callable<QuickBookPaymentOption> {

        private String merchantCode;
        private String productCode;
        private String ssoToken;
        private Long ssoUserId;
        private String bookingType;

        public QBCardsFetchTask(String merchantCode, String productCode, String ssoToken, Long ssoUserId, String bookingType) {
            this.merchantCode = merchantCode;
            this.productCode = productCode;
            this.ssoToken = ssoToken;
            this.ssoUserId = ssoUserId;
            this.bookingType = bookingType;

        }

        public QuickBookPaymentOption call() throws Exception {
            return getQuickBookBean(merchantCode, productCode, ssoToken, ssoUserId, bookingType);
        }
    }

    private long getQBFetchTimeOut() {
        try {
            String qbFetchCardsTimeOut = yatraPropertyReader.getProperty("qb.fetch.cards.timeout");
            return Long.valueOf(qbFetchCardsTimeOut);

        } catch (Exception e) {
            logger.error("Exception occurred while getting time out for QB Cards Fetch call", e);
            return QuickBookUIUtil.DEFAULT_TIMEOUT_FOR_QB_FETCH;
        }
    }

    private QuickBookPaymentOption createQBBeanForCurrentRequest(String merchantCode, String productCode) {
        try {
            QuickBookPaymentOption qbBeanForCurrentRequest = null;

            QuickBookPaymentOption qbBeanForMerchantProduct = (QuickBookPaymentOption) quickBookOptionBuilder.build(merchantCode, productCode);

            if (qbBeanForMerchantProduct == null) {
                throw new PayswiftException(CREATE_DEFAULT_QB_BEAN_FAILED.getCode(), "Failed to create default QB Bean with merchant_code = " + merchantCode + " and product_code = " + productCode);
            }
            qbBeanForCurrentRequest = new QuickBookPaymentOption();
            qbBeanForCurrentRequest.setDisplayText(qbBeanForMerchantProduct.getDisplayText());
            qbBeanForCurrentRequest.setCode(qbBeanForMerchantProduct.getCode());
            qbBeanForCurrentRequest.setStatus(Status.DISABLED);
            qbBeanForCurrentRequest.setSaveCard(Status.DISABLED);
            qbBeanForCurrentRequest.setAuthenticationRequired(false);
            qbBeanForCurrentRequest.setAuthenticationFailed(false);

            return qbBeanForCurrentRequest;

        } catch (Exception e) {
            throw new PayswiftException(CREATE_DEFAULT_QB_BEAN_FAILED.getCode(), "Failed to create default QB Bean with merchant_code = " + merchantCode + " and product_code = " + productCode, e);
        }
    }

    private void addQBCards(QuickBookPaymentOption quickBookPaymentOption, Long tenantId, String ssoToken, Long ssoUserId, String bookingType) {
        List<UserCardInfo> userCardList;
        try {
            UserCardsDisplayResponse userCardsDisplayResponse = expressCardsServiceClient.getCardsForDisplayWithUserId(tenantId, ssoToken, ssoUserId, bookingType);
            userCardList = userCardsDisplayResponse.getUserCards();

            ResponseStatus responseStatus = userCardsDisplayResponse.getResponse();
            String errorCode = responseStatus.getErrorCode();
            quickBookPaymentOption.setErrorCode(errorCode);
            quickBookPaymentOption.setErrorMesaage(responseStatus.getErrorMessage());
            logger.info("Error Code returned by Express Checkout Service while fetching cards for tenantId : " + tenantId + " is : " + errorCode);

            /*
             * Show QuickBook Tab when : errorCode is 112 or 103 (re-authentication required) or cards are returned by the service
             */
            if (StringUtils.equals(errorCode, QuickBookUIUtil.RE_AUTH_CODE) || StringUtils.equals(errorCode, QuickBookUIUtil.RE_LOGIN_CODE) || (userCardList != null && userCardList.size() > 0)) {
                quickBookPaymentOption.setStatus(Status.ENABLED);
                quickBookPaymentOption.setHasSavedCards(true);
                logger.debug("Quick Book Tab enabled for tenantId : " + tenantId + " and ssoToken : " + ssoToken);
            }

            /*
             * Show save card check box when : errorCode is 106 (No cards already saved) / 112, 103 (re-authentication required) or cards are returned by the service
             */
            if (StringUtils.equals(errorCode, QuickBookUIUtil.NO_CARDS_CODE) || StringUtils.equals(errorCode, QuickBookUIUtil.RE_AUTH_CODE)
                    || StringUtils.equals(errorCode, QuickBookUIUtil.RE_LOGIN_CODE) || (userCardList != null && userCardList.size() > 0)) {
                quickBookPaymentOption.setSaveCard(Status.ENABLED);
                logger.debug("Save Card check box enabled for tenantId : " + tenantId + " and ssoToken : " + ssoToken);
            }

            // Ask user for re-authentication when errorCode = 112 or 103.
            if (StringUtils.equals(errorCode, QuickBookUIUtil.RE_AUTH_CODE) || StringUtils.equals(errorCode, QuickBookUIUtil.RE_LOGIN_CODE)) {

                String authMode = userLoginService.getAuthMode(ssoToken); // YATRA or FACEBOOK
                quickBookPaymentOption.setAuthMode(authMode);
                quickBookPaymentOption.setAuthenticationRequired(true);
                logger.debug("Re-authentication required for tenantId : " + tenantId + " and ssoToken : " + ssoToken + ". authMode : " + authMode);
            }
        } catch (Exception e) {
            throw new PayswiftException(FETCH_QB_CARDS_EXCEPTION.getCode(), "Error occurred while fetching qb cards from ExpressCheckoutService for ssoToken = " + ssoToken, e);
        }

        setUserCardsInQBPaymentOption(userCardList, quickBookPaymentOption);
    }

    private void setUserCardsInQBPaymentOption(List<UserCardInfo> userCardList, QuickBookPaymentOption quickBookPaymentOption) {
        if (userCardList != null && userCardList.size() > 0) {
            try {
                populateQuickBookCards(userCardList, quickBookPaymentOption);
            } catch (Exception ex) {
                throw new PayswiftException(POPULATE_QB_CARDS_EXCEPTION.getCode(), "Error occurred while setting user cards in quickBookPaymentOption", ex);
            }
        }
    }

    private void populateQuickBookCards(List<UserCardInfo> userCardList, QuickBookPaymentOption quickBookPaymentOption) throws Exception {

        try {

            List<QuickBookCard> quickBookCards = new ArrayList<QuickBookCard>();
            for (UserCardInfo userCard : userCardList) {

                // Get Card Type - CreditCard, DebitCard
                String cardType = userCard.getCardType();

                // Get Card Brand - VISA, MASTER, DINERS, AMEX, MAESTRO
                String cardBrand = userCard.getCardBrand();

                // Get CardAccess Type - DOM, INT
                String cardAccessType = userCard.getCardAccessType();

                // Get Card Name - Eg : My VISA Card.
                String cardName = userCard.getCardName();

                // Get Card Holder's Name.
                String cardHolderFirstName = userCard.getFirstName();
                String cardHolderLastName = userCard.getLastName();

                // Check if billing address is saved for Card
                boolean isAddressSaved = false;
                if((null != userCard.getCardProperties()) && (null != userCard.getCardProperties().get(ExpressCardsPropertiesConstants.ADDRESS1)))
                    isAddressSaved = true;

                // Get QuickBookCardType and QuickBookCardBrand Beans
                UIQBCardTypeMaster quickBookCardType = getQBCardTypeBean(cardType);
                UIQBCardBrandMaster quickBookCardBrand = getQBCardBrandBean(cardBrand);

                if(quickBookCardType == null || quickBookCardBrand == null)
                    continue;

                // Get details from QuickBookCardType Bean
                String cardTypeLabel = quickBookCardType.getCardTypeLabel();
                String cvvLabel = quickBookCardType.getCvvLabel();

                // Get details from QuickBookCardBrand Bean
                String cardLogoURL = quickBookCardBrand.getImageUrl();
                String cvvLength = quickBookCardBrand.getCvvLength();

                // Get details from UserCard Bean
                String cardId = userCard.getCardId();
                String cardNumber = userCard.getCardNumber1() + "-" + userCard.getCardNumber2() + "-" + userCard.getCardNumber3();
                if (!StringUtils.isBlank(userCard.getCardNumber4())) // null or blank("")
                    cardNumber += "-" + userCard.getCardNumber4();


                // Get Expiry Details from UserCard Bean
                String expiryYear = userCard.getExpiryYear();
                String expiryMonth = userCard.getExpiryMonth();

                // Create QuickBookCard
                QuickBookCard quickBookCard = new QuickBookCard(cardTypeLabel, cardBrand, cardLogoURL, cardNumber, cvvLabel, cvvLength, cardId, cardAccessType, isAddressSaved, cardName, cardHolderFirstName, cardHolderLastName, expiryMonth, expiryYear, cardType, false, null,false);

                // Add QuickBookCard
                quickBookCards.add(quickBookCard);
            }
            quickBookPaymentOption.setQuickBookCards(quickBookCards);
        } catch (Exception e) {
            throw e;
        }
    }

    public List<QuickBookCard> convertPersonalQBCardListToPaySwiftCardList(List<UserCardInfo> userCardList){

        try {

            List<QuickBookCard> quickBookCards = new ArrayList<QuickBookCard>();
            for (UserCardInfo userCard : userCardList) {

                // Get Card Type - CreditCard, DebitCard
                String cardType = userCard.getCardType();

                // Get Card Brand - VISA, MASTER, DINERS, AMEX, MAESTRO
                String cardBrand = userCard.getCardBrand();

                // Get CardAccess Type - DOM, INT
                String cardAccessType = userCard.getCardAccessType();

                // Get Card Name - Eg : My VISA Card.
                String cardName = userCard.getCardName();

                // Get Card Holder's Name.
                String cardHolderFirstName = userCard.getFirstName();
                String cardHolderLastName = userCard.getLastName();

                // Check if billing address is saved for Card
                boolean isAddressSaved = false;
                if((null != userCard.getCardProperties()) && (null != userCard.getCardProperties().get(ExpressCardsPropertiesConstants.ADDRESS1)))
                    isAddressSaved = true;

                //Check if it's a GDS Card saved for offline booking
                boolean isGDSCard = false;
                if (null != userCard.getCardProperties() && null != userCard.getCardProperties().get(ExpressCardsPropertiesConstants.IS_GDS_CARD))
                    isGDSCard = true;

                // Get QuickBookCardType and QuickBookCardBrand Beans
                UIQBCardTypeMaster quickBookCardType = getQBCardTypeBean(cardType);
                UIQBCardBrandMaster quickBookCardBrand = getQBCardBrandBean(cardBrand);

                if(quickBookCardType == null || quickBookCardBrand == null)
                    continue;

                // Get details from QuickBookCardType Bean
                String cardTypeLabel = quickBookCardType.getCardTypeLabel();
                String cvvLabel = quickBookCardType.getCvvLabel();

                // Get details from QuickBookCardBrand Bean
                String cardLogoURL = quickBookCardBrand.getImageUrl();
                String cvvLength = quickBookCardBrand.getCvvLength();

                // Get details from UserCard Bean
                String cardId = userCard.getCardId();
                String cardNumber = userCard.getCardNumber1() + "-" + userCard.getCardNumber2() + "-" + userCard.getCardNumber3();
                if (!StringUtils.isBlank(userCard.getCardNumber4())) // null or blank("")
                    cardNumber += "-" + userCard.getCardNumber4();


                // Get Expiry Details from UserCard Bean
                String expiryYear = userCard.getExpiryYear();
                String expiryMonth = userCard.getExpiryMonth();

                String bookingType = "";
                if (null != userCard.getCardProperties() && null != userCard.getCardProperties().get("bookingType"))
                    bookingType = userCard.getCardProperties().get("bookingType");

                if (!StringUtils.isEmpty(bookingType)) {
                    bookingType = bookingType.toLowerCase();
                }
                // Create QuickBookCard
                QuickBookCard quickBookCard = new QuickBookCard(cardTypeLabel, cardBrand, cardLogoURL, cardNumber, cvvLabel, cvvLength, cardId, cardAccessType, isAddressSaved, cardName, cardHolderFirstName, cardHolderLastName, expiryMonth, expiryYear, cardType, isGDSCard, bookingType,false);

                // Add QuickBookCard
                quickBookCards.add(quickBookCard);
            }

            return quickBookCards ;

        } catch (Exception e) {
            logger.error("Exception occured in convertPersonalQBCardListToPaySwiftCardList method ",e);
            return Collections.emptyList();

        }

    }

    private UIQBCardTypeMaster getQBCardTypeBean(String cardType) {

        if (StringUtils.equals(cardType, QuickBookUIUtil.CARD_TYPE_CREDIT_CARD) || StringUtils.equals(cardType, QuickBookUIUtil.CARD_TYPE_CREDIT_CARD_2))
            return quickBookCardTypeCache.get(QuickBookUIUtil.CREDIT_CARD_CODE);

        if (StringUtils.equals(cardType, QuickBookUIUtil.CARD_TYPE_DEBIT_CARD) || StringUtils.equals(cardType, QuickBookUIUtil.CARD_TYPE_DEBIT_CARD_2))
            return quickBookCardTypeCache.get(QuickBookUIUtil.DEBIT_CARD_CODE);

        logger.error("No mapping found for cardType : " + cardType + ",hence defaulting to Credit Card");
        return quickBookCardTypeCache.get(QuickBookUIUtil.CREDIT_CARD_CODE);
    }

    public UIQBCardBrandMaster getQBCardBrandBean(String cardBrand) {

        if (StringUtils.equals(cardBrand, QuickBookUIUtil.CARD_BRAND_VISA))
            return quickBookCardBrandCache.get(QuickBookUIUtil.VISA_CODE);

        if (StringUtils.equals(cardBrand, QuickBookUIUtil.CARD_BRAND_MASTER))
            return quickBookCardBrandCache.get(QuickBookUIUtil.MASTER_CODE);

        if (StringUtils.equals(cardBrand, QuickBookUIUtil.CARD_BRAND_AMEX))
            return quickBookCardBrandCache.get(QuickBookUIUtil.AMEX_CODE);

        if (StringUtils.equals(cardBrand, QuickBookUIUtil.CARD_BRAND_DINERS))
            return quickBookCardBrandCache.get(QuickBookUIUtil.DINERS_CODE);

        if (StringUtils.equals(cardBrand, QuickBookUIUtil.CARD_BRAND_MAESTRO))
            return quickBookCardBrandCache.get(QuickBookUIUtil.MAESTRO_CODE);

        logger.error("No mapping found for cardBrand : " + cardBrand);
        return null;
    }

    private QuickBookPaymentOption getQBAuthenticationFailedBean(String authMode) {
        QuickBookPaymentOption quickBookPaymentOption = new QuickBookPaymentOption();
        quickBookPaymentOption.setAuthenticationFailed(true);
        quickBookPaymentOption.setAuthMode(authMode);
        quickBookPaymentOption.setStatus(Status.ENABLED);
        quickBookPaymentOption.setSaveCard(Status.ENABLED);
        return quickBookPaymentOption;
    }

    public QuickBookPaymentOption getQBErrorBean(String errorCode, String errorMsg) {
        QuickBookPaymentOption qbErrorBean = new QuickBookPaymentOption();
        qbErrorBean.setStatus(Status.DISABLED);
        qbErrorBean.setSaveCard(Status.DISABLED);
        qbErrorBean.setAuthenticationRequired(false);
        qbErrorBean.setAuthenticationFailed(false);
        qbErrorBean.setErrorCode(errorCode);
        qbErrorBean.setErrorMesaage(errorMsg);
        return qbErrorBean;
    }

    private String getUserTypeFromProduct(String product){
        List<String> b2bProducts = yatraPropertyReader.getPropertyAsList("b2b.products");
        List<String> crpProducts = yatraPropertyReader.getPropertyAsList("crp.products");
        if(crpProducts.contains(product)){
            return "CORPORATE";
        }else if(b2bProducts.contains(product)){
            return "B2B_AGENT";
        }else{
            return "YATRA";
        }
    }
}
