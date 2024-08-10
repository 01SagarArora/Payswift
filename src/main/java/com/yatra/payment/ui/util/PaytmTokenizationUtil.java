package com.yatra.payment.ui.util;

import com.yatra.payment.ui.dao.impl.PayswiftStagesDAOImpl;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

public class PaytmTokenizationUtil {

    private static Logger logger = Logger.getLogger(PaytmTokenizationUtil.class);



    public static String BIN_INFO_END_URL;

    public static final String BIN_INFO_END_URL_KEY= "fetch_bin_url";

    public static final String SANDBOX_BIN_INFO_END_URL = "https://securegw-stage.paytm.in/fetchBinDetail";

    public static String TOKENIZE_CARD_REMOVE_URL;

    public static final String TOKENIZE_CARD_REMOVE_KEY = "card_delete_url";

    public static final String MID_VALUE ="YatraO27701140418153";

    public static final String SANBOX_MID_VALUE = "YatraO05851311924654";

    public static final String MERCHANT_KEY ="7ErOhezcqlUtdDjC";

    public static final String SANDBOX_MERCHANT_KEY = "%fNLYJtdlaA6xqCk";

    public static final String TOKEN = "token";

    public static final String VERSION = "version";

    public static final String VERSION_V1 = "v1";

    public static final String REQUEST_TIMESTAMP = "requestTimestamp";
    public static final String TOKEN_TYPE = "tokenType";

    public static final String BIN = "bin";

    public static final String MID ="mid";

    public static final String HEAD = "head";

    public static final String BODY = "body";

    public static final String BIN_DETAILS = "binDetail";
    public static final String CHANNEL_ID = "channelId";
    public static final String WEB = "WEB";
    public static final String REQUEST_TYPE = "requestType";
    public static final String REQUEST_VALUE = "requestValue";
    public static final String REQUEST_ID = "requestId";
    public static final String SIGNATURE = "signature";
    public static final String SIGNATURE_TYPE = "signatureType";
    public static final String SIGNATURE_TYPE_CHECKSUM = "CHECKSUM";

    public static final String CLIENT_ID = "clientId";
    public static final String PAR= "panUniqueReference";
    public static String CARD_TO_PAR_END_URL;
    public static final String CARD_TO_PAR_END_KEY = "card_to_pan_url";


    public static String encryptByPublicKey(String plainData) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        String  publicKey="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjT3YWjF4IlAiVjC3g65n7+3LEKqGQWQQgpG0wbdsg7kCgp/y4Lc7o+/e09SVaXDPXg7DXJ90WlucPvobMJvJijyioOmAk5hXdBZFa+RvUvzR5zGx0/QglNHGdWzyLvRTqHPbQz+WiDfln9O85xauVKHG+8e2Jn3UlPzhKSQ5/FU4t0EN8i0j8nYjFNVJ1ll8CxNZZMqVCfNCCrdcr+2fkZv7kohVX38QTJwThC98TCSqOUSKiTewFBOAyilZCOBxFfGKdtTe/EUCQtzeax9u0P2yYIQTa1IHcHNBfBRO8KOuMmuTA7peS2Bc+bO7lOyRfNTcKzZuDH4RfWSwPOy8ZQIDAQAB";
        //propertyManager.getProperty(PaymentUtil.SUCCESSFUL_PAYMENT_CHECK_ENABLED_KEY);
        PublicKey key= KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(publicKey)));
        Cipher cipher=Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE,key);
        return Base64.getEncoder().encodeToString(cipher.doFinal(plainData.getBytes()));
    }

    public static String getResponse(String request, String requestUrl) throws Exception {
        String post_data = request.toString();
        /* for Staging */
        //TODO logger to be removed after testing
        logger.info("request is :"+request);
        URL url = new URL(requestUrl);
        /* for Production */
//         URL url = new URL("https://secure.paytmpayments.com/v3/order/status");
        String responseData = "";
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            DataOutputStream requestWriter = new DataOutputStream(connection.getOutputStream());
            requestWriter.writeBytes(post_data);
            requestWriter.close();

            InputStream is = connection.getInputStream();
            BufferedReader responseReader = new BufferedReader(new InputStreamReader(is));
            if ((responseData = responseReader.readLine()) != null) {
                logger.info("Response: " + responseData);//TODO to be removed after testing
            }
            //System.out.println("Request: " + post_data);
            responseReader.close();
        } catch (Exception e) {
            logger.error("error while getting response from paytm "+e);
        }
        return responseData;
    }
    public static  String getTimestamp() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss aa");
        return sdf.format(date);
    }

    public static String getBinInfoEndUrl(PayswiftStagesDAOImpl payswiftStagesDAO){
        if(StringUtils.isBlank(BIN_INFO_END_URL))
            BIN_INFO_END_URL = payswiftStagesDAO.getPropertyValueofTR(BIN_INFO_END_URL_KEY);
        return BIN_INFO_END_URL;
    }

    public static String getTokenizeCardRemoveUrl(PayswiftStagesDAOImpl payswiftStagesDAO){
        if(StringUtils.isBlank(TOKENIZE_CARD_REMOVE_URL))
            TOKENIZE_CARD_REMOVE_URL = payswiftStagesDAO.getPropertyValueofTR(TOKENIZE_CARD_REMOVE_KEY);
        return TOKENIZE_CARD_REMOVE_URL;
    }

    public static String getCardToParEndUrl(PayswiftStagesDAOImpl payswiftStagesDAO){
        if(StringUtils.isBlank(CARD_TO_PAR_END_URL))
            CARD_TO_PAR_END_URL = payswiftStagesDAO.getPropertyValueofTR(CARD_TO_PAR_END_KEY);
        return CARD_TO_PAR_END_URL;
    }


}
