package com.yatra.payment.gv;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.yatra.platform.util.YatraUtil;


/**
 * we would pass 2 parameter from payswift to payment portal related to voucher
 * 1) voucher_data -> this paramter would contain base64 encoded json 
 * 		(
 * 			we are first performing base64 encoding and not directly url encoding because of readability purpose of other
 * 			parameters in the request , otherwise it would clutter request
 * 		)
 * 2) voucher_key -> which would contain hash key of base64 encoded voucher json 
 * 
 * 
 * voucher_data=base64(voucher_json)&voucher_key=
 * 
 * Please always url encode all parameters values (base64 encoded value also) otherwise  it may cause problem
 * 
 * what kind of problem may happen , you can read at below url ->
 * 	
 * http://www.simonkrueger.com/2014/07/10/always-url-encode-your-base64-encoded-query-parameters.html 
 * 
 */
public class GiftVoucherHashUtil {
	
	private static final Logger logger = Logger.getLogger(GiftVoucherHashUtil.class);
	
	private static final String CHARSET_UTF8 = "UTF-8";
	private static final String SECERET_KEY = YatraUtil.getPropertyManager().getProperty("hash.key.phrase");
	private static final String ALGORITHM_HMAC_SHA_512 = "HmacSHA512";
	
	/**
	 * voucher_json:
	 * {
	 * 	"authCode":"1234567890",
	 * 	"vouchers" : [
	 * 		{"type":"QC", "code":"123", "pin":"00133", "redeemedBalance":99.50 },
	 * 		{"type":"QC", "code":"111", "pin":"33345", "redeemedBalance":77.40 },
	 * 	]
	 * }
	 * 
	 * input to this function would be : base64(voucher_json)
	 *
	 * use HmacSHA512 and encode its output using base64 encoding
	 *
	 * @throws NoSuchAlgorithmException 
	 * @throws UnsupportedEncodingException 
	 * @throws InvalidKeyException 
	 * 
	 */
	public static String generateHashCode(String inputStr) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException{
		Mac mac = Mac.getInstance(ALGORITHM_HMAC_SHA_512);
		SecretKeySpec secretKeySpec = new SecretKeySpec(SECERET_KEY.getBytes(CHARSET_UTF8), ALGORITHM_HMAC_SHA_512);
		mac.init(secretKeySpec);
		byte[] rawData = mac.doFinal(inputStr.getBytes(CHARSET_UTF8));
		return getBase64Encoded(rawData);
	}
	
	public static boolean isHashKeyValid(String inputStr,String receivedHashKey) throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException{
		return StringUtils.equals(generateHashCode(inputStr),receivedHashKey);
	}
	
	public static String getBase64Encoded(String inputStr) throws UnsupportedEncodingException{
		return new String(Base64.getEncoder().encode(inputStr.getBytes(CHARSET_UTF8)),CHARSET_UTF8);
	}
	
	public static String getBase64Decoded(String base64EncodedStr){
		try {
			return new String(Base64.getDecoder().decode(base64EncodedStr.getBytes(CHARSET_UTF8)),CHARSET_UTF8);
		} catch (UnsupportedEncodingException e) {
			logger.error("Error while decoding base64encoded data : ",e);
			throw new RuntimeException("Error while decoding base64encoded data", e);
		}
	}
	
	//---------------------------- private methods ----------------------------------------------------
	private static String getBase64Encoded(byte[] rawData) throws UnsupportedEncodingException{
		return new String (Base64.getEncoder().encode(rawData),CHARSET_UTF8);
	}
	
	
}