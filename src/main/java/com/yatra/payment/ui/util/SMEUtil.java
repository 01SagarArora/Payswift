package com.yatra.payment.ui.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.json.JSONObject;

public class SMEUtil {
	
	private static Logger logger = Logger.getLogger(SMEUtil.class);
	
	public static final String CONTENT_TYPE_JSON = "application/json";
	public static final String STATUS_200_OK = "200";
	private static final String HTTP_302 = "302";
	public static final String HTTP_METHOD_POST = "POST";
	public static final String HTTP_METHOD_GET = "GET";

	
	public static String getGETURL(String url, Map<String, String> requestMap) {
		StringBuilder stringBuilder = new StringBuilder(url);
		Iterator<Entry<String, String>> iterator = requestMap.entrySet().iterator();
		if(stringBuilder.length()>0) {
			stringBuilder.append("?");
			if(iterator.hasNext()) {
				Entry<String, String> entry = iterator.next();
				stringBuilder.append(entry.getKey());
				stringBuilder.append("=");
				stringBuilder.append(entry.getValue());
			}

			while(iterator.hasNext()) {
				stringBuilder.append("&");
				Entry<String, String> entry = iterator.next();
				stringBuilder.append(entry.getKey());
				stringBuilder.append("=");
				stringBuilder.append(entry.getValue());
			}
		}
		return stringBuilder.toString();
	}
	public static String sendSimplePostRequest(String postData, String url) throws IOException  {
		URL urlObj = new URL(url);
		HttpURLConnection   connection = (HttpURLConnection )urlObj.openConnection();

		connection.setDoOutput(true);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "application/json");

		OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
		out.write(postData);
		out.close();

		StringBuilder sb = new StringBuilder();

		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String line = null;

		while ((line = reader.readLine()) != null) {
			sb.append(line);
		}

		return new String(sb);
	} 


	public static Map<String,String> getSSOTokenDetails(String ssoToken){
		logger.info("Getting ssoToken Details for ssoToken : " + ssoToken);

		Map<String,String > resultMap  = new HashMap<>();
		resultMap.put("ssoToken",ssoToken);
		resultMap.put(PaymentUIUtil.STATUS,"false");

		Map<String,String> headerMap = new TreeMap<>();

		headerMap.put("com.yatra.tenant.header.tenantId","1011");
		headerMap.put("Content-Type","application/json");
		headerMap.put("httpMethod", HTTP_METHOD_POST);

		String url = "http://service1.yatra.com/single-signon-service/services/user-client-mapping/user/mapping/_retrieve";

		StringBuilder bodyBuidler = new StringBuilder();

		bodyBuidler.append("{ \"ssoToken\" : ").append("\""+ssoToken+"\"").append("}");
		try {
			String response = makePostRequestWithHeaders(url, bodyBuidler.toString(), headerMap, "GetSSOTokenDetails");

			logger.info("Response recieved for ssoToken " + ssoToken + " is : " + response);

			JSONObject responseObj = new JSONObject(response);
			String status = responseObj.getString("status");
			if (status.equalsIgnoreCase("false")){
				logger.info("no details for soo Token " + ssoToken + " detail response Obj : " + responseObj);
				return resultMap;
			}
			JSONObject clientMappingObj = responseObj.optJSONObject("clientMapping");
			if(clientMappingObj != null)
			{
				String corpId = clientMappingObj.optString("clientId");
				resultMap.put(PaymentUIUtil.CORP_ID,corpId);
			}
			JSONObject userDetailsObj = responseObj.optJSONObject("userDetails");
			if(userDetailsObj != null)
			{
				String userId = userDetailsObj.optString("userId");
				resultMap.put(PaymentUIUtil.USER_ID,userId);
			}
			resultMap.put(PaymentUIUtil.STATUS,"true");

			return resultMap;
		}catch (Exception e){
			logger.error("Error in parsing response for ssoToken " + ssoToken);
			return resultMap;
		}
	}
	
	public static String makePostRequestWithHeaders(String url, String data, Map<String, String> headers, String serviceName) throws Exception {

		BufferedReader reader = null;
		OutputStream output = null;
		try {
			logger.info("Posting data : " + data + " on URL : " + url);
			long startTime = System.currentTimeMillis();
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setDoOutput(true); 
			connection.setRequestMethod(headers.get("httpMethod"));
			connection.setRequestProperty("com.yatra.tenant.header.tenantId", headers.get("com.yatra.tenant.header.tenantId"));
			connection.setRequestProperty("Content-Type", headers.get("Content-Type"));
			connection.setReadTimeout(10000);
			output = connection.getOutputStream();
			output.write(data.getBytes(PaymentUIUtil.UTF_8_ENCODING));
			InputStream inputStream = connection.getInputStream();
			reader = new BufferedReader(new InputStreamReader(inputStream));
			StringBuilder response = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null)
				response.append(line + "\n");
			
			long responseTime = System.currentTimeMillis() - startTime;
			if(responseTime > 1000){
				logger.error("ResponseTime Threshold crossed - "+serviceName+" : "+responseTime);
			}
			return response.toString();
			
		} catch (Exception e) {
			logger.error("Exception occurred while posting data : " + data + " on URL : " + url, e);
			throw e;
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					logger.error("Exception occurred while posting data : " + data + " on URL : " + url +" while closing reader", e);
				}
			if (output != null)
				try {
					output.close();
				} catch (IOException e) {
					logger.error("Exception occurred while posting data : " + data + " on URL : " + url +" while closing output", e);
				}
		}
	}

}
