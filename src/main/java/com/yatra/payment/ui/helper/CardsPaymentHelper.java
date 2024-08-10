package com.yatra.payment.ui.helper;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yatra.payment.payswift.beans.PayNowRequest;
import com.yatra.payment.ui.beans.BinDetails;
import com.yatra.payment.ui.dao.BinDetailsDAO;
import com.yatra.payment.ui.service.PaytmTokenUtilityService;
import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.payment.ui.util.PaytmTokenizationUtil;
import com.yatra.payment.ui.util.QuickBookUIUtil;
import com.yatra.platform.util.YatraUtil;
import com.yatra.tokenized.cards.v3.beans.UserCardInfo;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class CardsPaymentHelper {
    private static final ObjectMapper objectMapper = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    private static final Logger logger = Logger.getLogger(CardsPaymentHelper.class);
    @Autowired
    private PaytmTokenUtilityService paytmTokenUtilityService;
    @Autowired
    private BinDetailsDAO binDetailsDAO;


    public String getHashedCardNumber(PayNowRequest payNowRequest) {
        String hashedCardNumber = null;
        Map<String, String> parameterMap = payNowRequest.getParameterMap();
        String superPnr = parameterMap.get(PaymentUIUtil.SUPER_PNR);
        String product = parameterMap.get(PaymentUIUtil.PRODUCT_CODE);
        String ssoToken = parameterMap.get(PaymentUIUtil.SSO_TOKEN);
        String cardId = parameterMap.get("cardid");

        String cno;
        String cno4 = null;

        String cbrand = parameterMap.get("cardBrand");
        if (StringUtils.isBlank(cbrand)) {
            cbrand = parameterMap.get(PaymentUIUtil.CARD_TYPE);
        }
        JSONObject parDetailJson;
        boolean isCorporateProduct = PaymentUIUtil.checkIfCorporateProduct(product);
        JSONObject loggingJson = getJsonForLogging(parameterMap);

        try {

            logger.info("request params for generating hashed card number are : " + loggingJson);
            /* todo remove is-not-corporate check once corp personal card tokenization is completed  */
            if (PaymentUIUtil.PAYMENT_OPTION_QUICK_BOOK.equalsIgnoreCase(parameterMap.get(PaymentUIUtil.PAYMENT_OPTION)) && !isCorporateProduct && !StringUtils.isBlank(ssoToken)) {
                UserCardInfo userCardInfo;
                JSONObject cardDetailJson = PaymentUIUtil.getTokenizedCardDetail(cardId, ssoToken);
                parameterMap.put("isTokenBasedTxn","true");
                if (cardDetailJson != null) {
                    userCardInfo = objectMapper.readValue(cardDetailJson.toString(), UserCardInfo.class);
                    parameterMap.put(PaymentUIUtil.CARD_TOKEN, userCardInfo.getTokenRefID());

                    if (!StringUtils.isBlank(userCardInfo.getTokenPan())) {
                         logger.info("PAR retrieved from db for superpnr  : " + superPnr + ", is : " + hashedCardNumber);
                         return userCardInfo.getTokenPan();
                    } else {
                        /* getting PAR from paytm  */
                        hashedCardNumber = paytmTokenUtilityService.getCardParValue(parameterMap);
                        if (StringUtils.isBlank(hashedCardNumber)) {
                            /*  generating hash on the combination of (first9+lastFourDigits+ccsc+issuingbak)  */

                            String bin = paytmTokenUtilityService.requestCardPrefix(cardId, superPnr, ssoToken, cbrand);
                            return generateCardHash(bin, userCardInfo.getCardNumber4(), parameterMap.get(PaymentUIUtil.CARD_SECURITY_CODE), superPnr);

                        }

                    }
                }

            } else {
                if (isCorporateProduct) {
                    return getHashedCardNoForCorp(parameterMap);
                }
                hashedCardNumber = paytmTokenUtilityService.getCardParValue(parameterMap);
                if (StringUtils.isBlank(hashedCardNumber)) {
                    /* generating hash on the combination of (firstsixdigits+lastFourDigits+email) */
                    cno = getCardNoFromMap(parameterMap);
                    cno4 = getLastFourFromCno(cno);
                    String bin = getBinFromCno(cno);
                    return generateCardHash(bin, cno4, parameterMap.get(PaymentUIUtil.CARD_SECURITY_CODE), superPnr);

                }
            }
        } catch (Exception e) {
            logger.info("Exception occurred  while generating hashed card no for super pnr : " + superPnr + ", " + ExceptionUtils.getFullStackTrace(e));
        }

        return hashedCardNumber;
    }

    private String getCardNoFromMap(Map<String, String> requestMap) {
        String cno = requestMap.get(PaymentUIUtil.CARD_NO);
        if (StringUtils.isBlank(cno)) {
            cno = requestMap.get("cardNumber");
        }
        return cno;
    }

    private String getHashedCardNoForCorp(Map<String, String> paramMap) {

        Map<String,String> requestMap = new HashMap<>(paramMap);
        String hashedCardNumber = null;
        String superPnr = requestMap.get(PaymentUIUtil.SUPER_PNR);
        String ssoToken = requestMap.get(PaymentUIUtil.SSO_TOKEN);
        String cardId = requestMap.get("cardid");
        String cno, cno4, bin;


        if(PaymentUIUtil.PAYMENT_OPTION_QUICK_BOOK.equalsIgnoreCase(requestMap.get(PaymentUIUtil.PAYMENT_OPTION))){
            requestMap.put(PaymentUIUtil.CARD_NO,PaymentUIUtil.getCardNumberForNonTokenizedQBTxn(cardId, ssoToken));
        }

        hashedCardNumber = paytmTokenUtilityService.getCardParValue(requestMap);
        if(StringUtils.isBlank(hashedCardNumber)){
            cno = getCardNoFromMap(requestMap);
            cno4 = getLastFourFromCno(cno);
            bin = getBinFromCno(cno);
            return  generateCardHash(bin, cno4, requestMap.get(PaymentUIUtil.CARD_SECURITY_CODE), superPnr);

        }
        return hashedCardNumber;
    }

    private String generateCardHash(String bin, String cno4, String ccsc, String superPnr) {
        String hashedCardNumber;
        String text = getRequiredTextForHashing(bin, cno4, ccsc, superPnr);
        hashedCardNumber = generateHashOnRequest(text, superPnr);
        logger.info("hashed card number generated  against  saved card  for superpnr  : " + superPnr + ", is : " + hashedCardNumber);
        return hashedCardNumber;
    }

    private String getLastFourFromCno(String cno) {
        return cno.substring(cno.length() - 4);
    }

    private String getBinFromCno(String cno) {
        return cno.substring(0, 6);
    }

    private String getRequiredTextForHashing(final String bin, final String cno4, final String ccsc, String superpnr) {
        String bankCode = getBankCode(bin, superpnr);
        String text = bin + cno4 + ccsc + bankCode;
        return text;
    }

    private String generateHashOnRequest(final String text, String superPnr) {
        String rawSalt = YatraUtil.getPropertyManager().getProperty(PaymentUIUtil.HASH_SALT_PROP);
        logger.info("salt generated for hashing against superpnr : " + superPnr + ", is : " + rawSalt);
        byte[] salt = rawSalt.getBytes();
        return PaymentUIUtil.createHash(text, salt);
    }

    private String getBankCode(String bin, String superpnr) {
        String bankCode = null;
        try {
            /* suffixing bin */
            bin = bin + "000";
            Optional<BinDetails> binInfo = binDetailsDAO.getOneBinBankDetails(bin);
            if (binInfo.isPresent()) {
                BinDetails binDetails = binInfo.get();
                bankCode = binDetails.getBankCode();
            }
        } catch (Exception ex) {
            logger.error("Exception : bankcode for superpnr : " + superpnr + " : " + bankCode, ex);
        }
        if (StringUtils.isBlank(bankCode)) {
            bankCode = "YATRA";
        }
        logger.info("bankcode for superpnr : " + superpnr + " : " + bankCode);
        return bankCode;
    }

    private JSONObject getJsonForLogging(Map<String, String> requestMap) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(PaymentUIUtil.SUPER_PNR, requestMap.get(PaymentUIUtil.SUPER_PNR));
            jsonObject.put(PaymentUIUtil.PRODUCT_CODE, requestMap.get(PaymentUIUtil.PRODUCT_CODE));
            jsonObject.put(PaymentUIUtil.SSO_TOKEN, requestMap.get(PaymentUIUtil.SSO_TOKEN));
            jsonObject.put(PaymentUIUtil.PAYMENT_OPTION, requestMap.get(PaymentUIUtil.PAYMENT_OPTION));
            jsonObject.put(PaymentUIUtil.CARD_ID, requestMap.get("cardid"));
            jsonObject.put(QuickBookUIUtil.CARD_BRAND, requestMap.get("cardBrand"));
            jsonObject.put(PaymentUIUtil.USER_ID, requestMap.get(PaymentUIUtil.USER_ID));
        } catch (Exception exception) {
            logger.error("exception occurred while preparing json for logger info for superpnr : " + requestMap.get(PaymentUIUtil.SUPER_PNR), exception);
        }
        return jsonObject;
    }


}
