package com.yatra.payment.ui.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.platform.commons.property.PropertyManager;
import com.yatra.platform.http.TransferUtils;


@Service("httpUtil")
public class HttpUtil {
	
	private static Logger logger = Logger.getLogger(HttpUtil.class);
	@Autowired private PropertyManager propertyManager;
	private static final String separator= "&";
	private static int responseTimeThreshold;
	private static int readTimeout;
	
	@PostConstruct
	public void postConstruct() throws Exception {
		responseTimeThreshold = propertyManager.getPropertyAsInt("connection.response.time.threshold.ms");
		readTimeout = propertyManager.getPropertyAsInt("connection.read.timeout.ms");
	}
	
	public String createPostDataFromMap(Map<String, String> fieldMap) {
		StringBuffer buf = new StringBuffer();
		String separatorStr = "";

		if (fieldMap == null || fieldMap.isEmpty()) 
			return null;

		for (String key : fieldMap.keySet()) {
			String value = fieldMap.get(key);
			try {
				if (StringUtils.isNotEmpty(value)) {
					buf.append(separatorStr);
					buf.append(URLEncoder.encode(key, PaymentUIUtil.UTF_8_ENCODING));
					buf.append('=');
					buf.append(URLEncoder.encode(value, PaymentUIUtil.UTF_8_ENCODING));
					separatorStr = separator;
				}
			} catch (Exception e) {
				logger.error("Exception occurred while encoding post data,  key:" + key+ " value:"+value , e);
			}
		}
		return buf.toString();
	}
	
	public String invoke(String url, String data, String serviceName) throws Exception {
		Map<String, String> headers = new HashMap<>();
		headers.put("httpMethod", "POST");
		headers.put("contentType", PaymentUIUtil.CONTENT_TYPE_FORM_URLENCODED);
		headers.put("encoding", PaymentUIUtil.UTF_8_ENCODING);
		headers = TransferUtils.getTransferHeaders(headers);
		return invoke(url, data, headers, serviceName);
	}
	
	public String invoke(String url, Map<String, String> fieldMap, String serviceName) throws Exception {
		Map<String, String> headers = new HashMap<>();
		headers.put("httpMethod", "POST");
		headers.put("contentType", PaymentUIUtil.CONTENT_TYPE_FORM_URLENCODED);
		headers.put("encoding", PaymentUIUtil.UTF_8_ENCODING);
		headers = TransferUtils.getTransferHeaders(headers);
		return invoke(url, createPostDataFromMap(fieldMap), headers, serviceName);
	}

	public String invoke(String url, Map<String, String> fieldMap, Map<String, String> headers, String serviceName) throws Exception {
		headers = TransferUtils.getTransferHeaders(headers);
		return invoke(url, createPostDataFromMap(fieldMap), headers, serviceName);
	}

	public String invoke(String url, String data, Map<String, String> headers, String serviceName) throws Exception {

		BufferedReader reader = null;
		OutputStream output = null;
		try {
			logger.info("Posting data : " + data + " on URL : " + url);
			long startTime = System.currentTimeMillis();
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setDoOutput(true); 
			connection.setRequestMethod(headers.get("httpMethod"));// Triggers PTT.
			connection.setRequestProperty("Accept-Charset", headers.get("encoding"));
			connection.setRequestProperty("Content-Type", headers.get("contentType"));
			connection.setRequestProperty("cookie", headers.get("cookie"));
			connection.setReadTimeout(readTimeout);
			output = connection.getOutputStream();
			output.write(data.getBytes(PaymentUIUtil.UTF_8_ENCODING));
			InputStream inputStream = connection.getInputStream();
			reader = new BufferedReader(new InputStreamReader(inputStream));
			StringBuilder response = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null)
				response.append(line + "\n");
			
			long responseTime = System.currentTimeMillis() - startTime;
			if(responseTime > responseTimeThreshold){
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
