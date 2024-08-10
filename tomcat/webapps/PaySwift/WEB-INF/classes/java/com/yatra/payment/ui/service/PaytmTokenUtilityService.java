package com.yatra.payment.ui.service;

import com.paytm.pg.merchant.PaytmChecksum;
import com.yatra.payment.ui.dao.impl.PayswiftStagesDAOImpl;
import com.yatra.payment.ui.helper.PaymentUIHelper;
import com.yatra.payment.ui.util.LoggingUtil;
import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.payment.ui.util.PaytmTokenizationUtil;
import com.yatra.platform.util.YatraUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


@Service
public class PaytmTokenUtilityService {
    @Autowired private PaymentUIHelper paymentUIHelper;
    @Autowired
    private PayswiftStagesDAOImpl payswiftStagesDAO;
    private static final Logger logger = Logger.getLogger(PaytmTokenUtilityService.class);


    /* for Bin details*/
    public JSONObject getBinInfo(String cardId, String superPnr, String ssoToken) {
        try {
            String data = String.valueOf(binInfoReq(cardId, ssoToken));
            String url = PaytmTokenizationUtil.getBinInfoEndUrl(payswiftStagesDAO) + "?mid=" + PaytmTokenizationUtil.MID_VALUE + "&orderId=" + superPnr;
            logger.info("request sent to paytm for fetch bin info api for superpnr  "+superPnr+" is : "+(url+data));

            paymentUIHelper.logIntoDB( (url+" : "+data), "REQUEST" , superPnr , "FETCH_BIN_DETAILS_PAYTM" );
            String response = PaytmTokenizationUtil.getResponse(data, url);

            Map<String,String> fieldMapToMask = new HashMap<String,String>();
            fieldMapToMask.put("bin", LoggingUtil.REGEX_PRESERVE_NONE);
            fieldMapToMask.put("cardPrefix",LoggingUtil.REGEX_PRESERVE_NONE);
            String maskedResponse = getMaskedDataForLogging(response,fieldMapToMask);
            logger.info("response received from paytm for fetch bin info  api against superpnr "+superPnr+" is : "+maskedResponse);
            paymentUIHelper.logIntoDB( maskedResponse, "RESPONSE" , superPnr , "FETCH_BIN_DETAILS_PAYTM" );

            JSONObject binResponse = new JSONObject(response);
            logger.info("output : " + response);
            return binResponse;
        } catch (Exception e) {
            logger.error("Exception occured while calling paytm for bin details against superpnr "+superPnr,e);
        }
        return null;
    }

    private JSONObject binInfoReq(String cardId, String ssoToken) throws Exception {
        JSONObject data = new JSONObject();
        JSONObject body = new JSONObject();
        JSONObject head = new JSONObject();

        //Getting Token bin from getTokenBinForPromo function which extract from payment-tokenize.
        String bin = PaymentUIUtil.getTokenBinForPromo(cardId, ssoToken);
        body.put(PaytmTokenizationUtil.BIN, bin);
        body.put(PaytmTokenizationUtil.MID, PaytmTokenizationUtil.MID_VALUE);
        String checksum = PaytmChecksum.generateSignature(body.toString(), PaytmTokenizationUtil.MERCHANT_KEY);// pgBean.getMerchantKey());
        head.put(PaytmTokenizationUtil.CHANNEL_ID, PaytmTokenizationUtil.WEB);
        head.put(PaytmTokenizationUtil.TOKEN_TYPE, "CHECKSUM");
        head.put(PaytmTokenizationUtil.TOKEN, checksum);

        data.put(PaytmTokenizationUtil.HEAD, head);
        data.put(PaytmTokenizationUtil.BODY, body);
        return data;
    }

    public String provideBin(String cardId, String superPnr, String ssoToken, String cardBrand) {

        int bin_count = Integer.valueOf(YatraUtil.getPropertyManager().getProperty("bin.length.count"));
        String bin;
        try {
            /* returning tokenbin in as bin when cardBrand is amex*/
            if (StringUtils.equalsIgnoreCase(cardBrand, PaymentUIUtil.CARD_TYPE_AMEX)) {
                String tokenBin = PaymentUIUtil.getTokenBinForPromo(cardId, ssoToken);
                return tokenBin.substring(0, bin_count);
            }

            JSONObject object = getBinInfo(cardId, superPnr, ssoToken);
            JSONObject obj2 = object.getJSONObject(PaytmTokenizationUtil.BODY).getJSONObject(PaytmTokenizationUtil.BIN_DETAILS);
            bin = obj2.getString("cardPrefix");
            if (StringUtils.isNotBlank(bin)) {
                return bin.substring(0, bin_count);
            } else {
                logger.info("bin not received from paytm for superpnr : " + superPnr + ", is : " + bin);
                return null;
            }


        } catch (Exception e) {
            logger.info("exception while getting bin  from paytm fetch bin API response");
            return null;
        }
    }
    public String getCardParValue(Map<String,String> requestMap){
        String cardPar = null;
        try {
            JSONObject responseJson = sendRequestForCardToParApi(requestMap);
            if (responseJson != null && responseJson.getJSONObject("body").has(PaytmTokenizationUtil.PAR)) {
                cardPar = responseJson.getJSONObject("body").get(PaytmTokenizationUtil.PAR).toString();
                logger.info("PAR  received from paytm for superpnr  : " + requestMap.get(PaymentUIUtil.SUPER_PNR) + ", is : " + cardPar);
            }
        }
        catch (Exception ex){
            logger.error("Exception occurred while fetching PAR value from paytm for superPnr : "+requestMap.get(PaymentUIUtil.SUPER_PNR),ex);
        }
        return  cardPar;
    }
    public JSONObject sendRequestForCardToParApi(Map<String, String> requestParamMap) {
        String superPnr = requestParamMap.get(PaymentUIUtil.SUPER_PNR);
        try {
            String data = String.valueOf(generateParRequest(requestParamMap));
            logger.info("paytm card to par api request   for super pnr  : " + requestParamMap.get(PaymentUIUtil.SUPER_PNR) + " is " + data);
            String url = PaytmTokenizationUtil.getCardToParEndUrl(payswiftStagesDAO) + "?mid=" + PaytmTokenizationUtil.MID_VALUE + "&orderId=" + superPnr;
            String response = PaytmTokenizationUtil.getResponse(data, url);
            logger.info("paytm card to par api response  for super pnr  : " + requestParamMap.get(PaymentUIUtil.SUPER_PNR) + " is " + response);
            return new JSONObject(response);
        } catch (Exception e) {
            System.out.println("exception : while calling paytm's card to par api for super pnr " + superPnr + " : " + e);
        }
        return null;
    }

    private JSONObject generateParRequest(Map<String, String> requestParamMap) throws Exception {
        JSONObject data = new JSONObject();
        JSONObject body = new JSONObject();
        JSONObject head = new JSONObject();
        String requestType = getParRequestTypeBasedOnPayop(requestParamMap);
        String requestValue = getParRequestValueBasedOnPayop(requestParamMap);

        body.put(PaytmTokenizationUtil.MID, PaytmTokenizationUtil.MID_VALUE);
        body.put(PaytmTokenizationUtil.REQUEST_TYPE, requestType);
        body.put(PaytmTokenizationUtil.REQUEST_VALUE, requestValue);
        //body.put(PaytmTokenizationUtil.REQUEST_VALUE,PaytmTokenizationUtil.encryptByPublicKey("4889940000043807"));
        String checksum = PaytmChecksum.generateSignature(body.toString(), PaytmTokenizationUtil.MERCHANT_KEY);// pgBean.getMerchantKey());

        head.put(PaytmTokenizationUtil.VERSION, PaytmTokenizationUtil.VERSION_V1);
        head.put(PaytmTokenizationUtil.REQUEST_TIMESTAMP, PaytmTokenizationUtil.getTimestamp());
        head.put(PaytmTokenizationUtil.REQUEST_ID, requestParamMap.get(PaymentUIUtil.SUPER_PNR));
        head.put(PaytmTokenizationUtil.SIGNATURE, checksum);
        head.put(PaytmTokenizationUtil.SIGNATURE_TYPE, PaytmTokenizationUtil.SIGNATURE_TYPE_CHECKSUM);
        head.put(PaytmTokenizationUtil.CLIENT_ID, "");

        data.put(PaytmTokenizationUtil.HEAD, head);
        data.put(PaytmTokenizationUtil.BODY, body);
        return data;
    }

    private String getParRequestTypeBasedOnPayop(Map<String, String> requestParamMap) {
        String __return = "PAN";
        String payop = requestParamMap.get(PaymentUIUtil.PAYMENT_OPTION);
        String isTokenBasedTxn = requestParamMap.get("isTokenBasedTxn");
        if (StringUtils.equalsIgnoreCase(payop, PaymentUIUtil.PAYMENT_OPTION_QUICK_BOOK)
              && StringUtils.isNotBlank(isTokenBasedTxn) && StringUtils.equalsIgnoreCase(isTokenBasedTxn,"true")) {
            __return = "TIN";
        }
        return __return;

    }

    private String getParRequestValueBasedOnPayop(Map<String, String> requestParamMap) throws Exception {
        String __return = null;
        String payop = requestParamMap.get(PaymentUIUtil.PAYMENT_OPTION);
        String cno;
        String isTokenBasedTxn = requestParamMap.get("isTokenBasedTxn");
        if (StringUtils.equalsIgnoreCase(payop, PaymentUIUtil.PAYMENT_OPTION_QUICK_BOOK)
            && StringUtils.isNotBlank(isTokenBasedTxn) && StringUtils.equalsIgnoreCase(isTokenBasedTxn,"true")) {
            __return = requestParamMap.get(PaymentUIUtil.CARD_TOKEN);
        }
        else{
            cno = requestParamMap.get(PaymentUIUtil.CARD_NO);
            if (StringUtils.isBlank(cno)) {
                cno = requestParamMap.get("cardNumber");
            }
            __return = PaytmTokenizationUtil.encryptByPublicKey(cno);
        }
        return __return;

    }
    public String requestCardPrefix(String cardId, String superPnr, String ssoToken, String cardBrand) {

        String bin = null ;
        try {
            /* returning tokenbin in as bin when cardBrand is amex*/
            if (StringUtils.equalsIgnoreCase(cardBrand, PaymentUIUtil.CARD_TYPE_AMEX)) {
                return PaymentUIUtil.getTokenBinForPromo(cardId, ssoToken);
            }
            JSONObject binDetailsRes = getBinInfo(cardId, superPnr, ssoToken);
            JSONObject binDetails = binDetailsRes.getJSONObject(PaytmTokenizationUtil.BODY).getJSONObject(PaytmTokenizationUtil.BIN_DETAILS);
            
            if ( binDetails != null  && binDetails.has("cardPrefix") && StringUtils.isNotBlank("cardPrefix")) {
                bin = binDetails.getString("cardPrefix");
                bin = bin.substring(0,6);
                return bin ;
            } else {
                logger.info("bin not received from paytm for superpnr : " + superPnr + ", is : " + bin);
                return null;
            }
        } catch (Exception e) {
            logger.error("Exception occurred for superpnr  "+superPnr,e);
            return null;
        }
    }
    private String getMaskedDataForLogging(String responseJson , Map<String,String> fieldMapToMask){

        String maskedResponse = LoggingUtil.getMaskedJsonForLogging(responseJson,fieldMapToMask);
        return maskedResponse;

    }
}
