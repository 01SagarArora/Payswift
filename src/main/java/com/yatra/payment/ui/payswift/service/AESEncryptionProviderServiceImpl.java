package com.yatra.payment.ui.payswift.service;

import java.math.BigInteger;
import java.security.Key;
import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.log4j.Logger;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import com.yatra.platform.util.YatraUtil;

public class AESEncryptionProviderServiceImpl {
    
	private static final String ALGORITHM = "AES";
	private Logger logger = Logger.getLogger(getClass());
    
    public String getEncryptedData(String data, String keyPhrase) throws Exception {
        if (keyPhrase == null || data == null) {
            throw new Exception("data or keyphrase cannot be null");
        }
        Key key = new SecretKeySpec(keyPhrase.getBytes(), ALGORITHM);
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedData = cipher.doFinal(data.getBytes());
            String encryptedString = new BASE64Encoder().encode(encryptedData);
            return encryptedString;
        } catch(Exception e){
            logger.error("getEncryptedData", e);
            throw new Exception(e);
        }
    }

    public String getDecryptionKey(String data, String keyPhrase) throws Exception {
        if (keyPhrase == null || data == null) {
            throw new Exception("data or keyphrase cannot be null");
        }
        Key key = new SecretKeySpec(keyPhrase.getBytes(), ALGORITHM);
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decodeBuffer = new BASE64Decoder().decodeBuffer(data);
            byte[] decryptedData = cipher.doFinal(decodeBuffer);
            String decryptedString = new String(decryptedData);
            return decryptedString;
        }catch(Exception e){
            logger.error("getDecryptionKey", e);
            throw new Exception(e);
        }
    }

    public String getKeyPhrase(String userKey) throws Exception {
        
		if (userKey == null || userKey.trim().equals("")) {
            throw new Exception("Error in getting User Key");
        }
    	
		String applicationKey = getApplicationKey();
        String compositeKey = userKey + applicationKey;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(compositeKey.getBytes("UTF-8"));
            BigInteger bigInt = new BigInteger(digest);
            String keyPhrase = bigInt.toString(16);
            while(keyPhrase.length()<16){
                keyPhrase = "0"+keyPhrase;
            }
            return keyPhrase.substring(keyPhrase.length()-16, keyPhrase.length());
        } catch (Exception e) {
            logger.error("getKeyPhrase", e);
            throw new RuntimeException(e.getMessage());
        } 
    }
    
    private String getApplicationKey() {
    	return YatraUtil.getPropertyManager().getProperty("payswift.session.encryption.key");
    }
}
