package com.yatra.payment.ui.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import com.yatra.payment.client.beans.LoginInfoDo;
import com.yatra.platform.http.HttpEndPoint;
import com.yatra.platform.http.HttpService;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.payment.ui.util.QuickBookUIUtil;
import com.yatra.platform.commons.property.PropertyManager;
import com.yatra.profile.service.client.UserProfileServiceClient;
import com.yatra.user.profile.service.beans.RetrieveUserProfileRequestWO;
import com.yatra.user.profile.service.beans.RetrieveUserProfileResponseWO;

@Service("userLoginService")
public class UserLoginService {
	
	private static Logger logger = Logger.getLogger(UserLoginService.class);
	
	@Autowired private PropertyManager yatraPropertyReader;
	@Autowired private UserProfileServiceClient userProfileServiceClient;
	public static final String VALIDATE_USER_CARD_ACCESS_BASE_REQUEST_NEW = "{\"ssoToken\" : \"%s\"}";
	private static final String VALIDATE_USER_CARD_ACCESS_URL_NEW = "http://service1.yatra.com/single-signon-service/services/user-login-service/user/session/info";

	private final String USER_TYPE = "userType";
	private final String EMAIL_ID = "emailId";
	private final String USER_ID = "userId";
	private final String LOGIN_MODE = "loginMode";
	private final String IS_AUTHENTICATED = "isAuthenticated";
	private final String HTTPCODE = "httpCode";
	private final String STATUS = "status";
	private final String LOGIN_STATUS = "loginStatus";
	private final String CODE = "code";
	private final String LOGIN_STATUS_VAL = "LOGGED_IN";
	
	public String login(String email, String password , String userType) throws Exception {
		
		String url = "http://service1.yatra.com/single-signon-service/services/user-login-service/user/userType/" + userType.toUpperCase() + "/account/loginSession";
		JSONObject userLoginRequest = new JSONObject();
		try {
			userLoginRequest.put("emailId", email);
			userLoginRequest.put("password", password);
			String userTypeResponse = PaymentUIUtil.sendSSOJsonPostRequest(userLoginRequest.toString(), url, "1011");
			JSONObject responseJsonObject = new JSONObject(userTypeResponse);
			return responseJsonObject.optString("ssoToken");
		} catch (Exception e) {
			logger.error("Exception occurred while login user with email : " + email, e);
			throw e;
		}
	}
	
	public String getEmailForSSOToken(String ssoToken) {
		
		String url = "http://service1.yatra.com/single-signon-service/services/user-login-service/user/session/loginInfo";
		JSONObject userLoginInfoRequest = new JSONObject();
		try {
			userLoginInfoRequest.put("ssoToken", ssoToken);
			String userLoginInfoResponse = PaymentUIUtil.sendSSOJsonPostRequest(userLoginInfoRequest.toString(), url, "1011");
			JSONObject responseJsonObject = new JSONObject(userLoginInfoResponse);
			return responseJsonObject.getString("emailId");
		} catch (Exception e) {
			logger.error("Exception occurred while fetching email Id from User Profile Service for ssoToken : " + ssoToken, e);
			return null;
		}
	}
	
	public boolean isPasswordCompromised(String ssoToken) throws Exception {
		String url = "http://service1.yatra.com/single-signon-service/services/single-signon-service/customer/account";
		JSONObject retrieveUserInfoRequest = new JSONObject();
		try {
			retrieveUserInfoRequest.put("ssoToken", ssoToken);
			String retrieveUserInfoResponse = PaymentUIUtil.sendSSOJsonPostRequest(retrieveUserInfoRequest.toString(), url, "1011");
			JSONObject responseJsonObject = new JSONObject(retrieveUserInfoResponse);
			return responseJsonObject.optJSONObject("userAccountWO").optBoolean("passwordCompromised");
		} catch (Exception e) {
			logger.error("Exception occurred while determining if password is compromised for ssoToken : " + ssoToken, e);
			throw e;
		}
	}
	
	public boolean isUserAuthenticated(String ssoToken) throws Exception {
		
		String url = "http://service1.yatra.com/single-signon-service/services/sso-validation-service/user/session/authenticity";
		JSONObject checkAuthenticationRequest = new JSONObject();
		try {
			checkAuthenticationRequest.put("ssoToken", ssoToken);
			checkAuthenticationRequest.put("updateAccessTime", true);
			String checkAuthenticationResponse = PaymentUIUtil.sendSSOJsonPostRequest(checkAuthenticationRequest.toString(), url, "1011");
			org.json.JSONObject responseJsonObject = new org.json.JSONObject(checkAuthenticationResponse);
			return responseJsonObject.optBoolean("status");
		} catch (Exception e) {
			logger.error("Exception occurred while validating if authentication is required for ssoToken : " + ssoToken, e);
			throw e;
		}
	}
	

	public boolean updatePassword(String ssoToken, String oldPassword, String newPassword) throws Exception {
		
		String url = "http://service1.yatra.com/single-signon-service/services/user-common-service/user/account/password";
		JSONObject updatePasswordRequest = new JSONObject();
		try {
			updatePasswordRequest.put("ssoToken", ssoToken);
			updatePasswordRequest.put("oldPassword", oldPassword);
			updatePasswordRequest.put("newPassword", newPassword);
			String updatePasswordResponse = PaymentUIUtil.sendSSOJsonPostRequest(updatePasswordRequest.toString(), url, "1011");
			JSONObject responseJsonObject = new JSONObject(updatePasswordResponse);
			return responseJsonObject.optBoolean("status");
		} catch (Exception e) {
			logger.error("Exception occurred while updating password for ssoToken : " + ssoToken, e);
			throw e;
		}
	}
	// returning userType
	public String getAuthMode(String ssoToken) throws Exception {
		
		String url = "http://service1.yatra.com/single-signon-service/services/user-login-service/user/session/loginInfo";
		JSONObject userLoginInfoRequest = new JSONObject();
		try {
			userLoginInfoRequest.put("ssoToken", ssoToken);
			String userLoginInfoResponse = PaymentUIUtil.sendSSOJsonPostRequest(userLoginInfoRequest.toString(), url, "1011");
			JSONObject responseJsonObject = new JSONObject(userLoginInfoResponse);
			return responseJsonObject.optString("userType");
		} catch (Exception e) {
			logger.error("Exception occurred while getting auth mode for ssoToken : " + ssoToken, e);
			throw e;
		}
	}
	
	/*public Map<String, String> getFBLoginInfo(String accessToken, String sourcePage) throws Exception {
		
		Map<String, String> fbLoginInfo = new HashMap<String, String>();
		OutputStream output = null;
		try {
			String charset = QuickBookUIUtil.UTF_CHAR_SET;

			// Social Web Core URL
			String socialWebCoreURL = yatraPropertyReader.getProperty("social.web.core.url");

			
			// Social Web Core query params
			String query = String.format("authMode=%s&loginStep=%s&accessToken=%s&sourcepage=%s", URLEncoder.encode(QuickBookUIUtil.RE_AUTH_MODE_FB, charset),
					URLEncoder.encode(QuickBookUIUtil.LOGIN_STEP, charset), URLEncoder.encode(accessToken, charset), URLEncoder.encode(sourcePage, charset));

			// Connect to Social Web Core Service
			URLConnection connection = new URL(socialWebCoreURL).openConnection();
			connection.setDoOutput(true); // Triggers POST.
			connection.setRequestProperty("Accept-Charset", charset);
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
			connection.setReadTimeout(10 * 1000);
			output = connection.getOutputStream();
			output.write(query.getBytes(charset));

			// Get ssoToken
			String ssoToken = getSSOToken(connection);

			// Get Response Code
			String responseCode = getResponseCode(connection);

			// Add info to map and return
			fbLoginInfo.put(QuickBookUIUtil.SSO_TOKEN_KEY, ssoToken);
			fbLoginInfo.put(QuickBookUIUtil.RESPONSE_CODE_KEY, responseCode);

			return fbLoginInfo;

		} catch (Exception e) {
			logger.error("Exception occurred while fetching FB Login Info : ", e);
			throw e;
		} finally {
			try {
				if (output != null)
					output.close();
			} catch (Exception e) {
			}
		}
	}
	*/
	
public Map<String, String> getFBLoginInfo(String accessToken, String sourcePage) throws Exception {
		
		Map<String, String> fbLoginInfo = new HashMap<String, String>();
		
		
		String url = "http://service1.yatra.com/single-signon-service/services/social-login-service/social/login/loginMode/FACEBOOK";
		JSONObject socialLoginRequest = new JSONObject();
		try {
			socialLoginRequest.put("accessToken", accessToken);
			String socialLoginResponse = PaymentUIUtil.sendSSOJsonPostRequest(socialLoginRequest.toString(), url, "1011");
			JSONObject responseJsonObject = new JSONObject(socialLoginResponse);
			String ssoToken = responseJsonObject.optString("ssoToken");
			String responseCode = responseJsonObject.optString("code");
			
			fbLoginInfo.put(QuickBookUIUtil.SSO_TOKEN_KEY, ssoToken);
			fbLoginInfo.put(QuickBookUIUtil.RESPONSE_CODE_KEY, responseCode);

			return fbLoginInfo;

		} catch (Exception e) {
			logger.error("Exception occurred while fetching FB Login Info : ", e);
			throw e;
	
		}
	}
	

	public void addCookie(HttpServletResponse response, String name, String value) throws Exception {        
		try{
			int maxAge = yatraPropertyReader.getPropertyAsInt("sso_token_max_age");
			String path = yatraPropertyReader.getProperty("sso_token_path");
			String domain = yatraPropertyReader.getProperty("sso_token_domain");
			
			Cookie cookie = new Cookie(name, value);  
	        cookie.setMaxAge(maxAge);
	        cookie.setPath(path);
	        cookie.setDomain(domain);
	        cookie.setSecure(true);
			response.addCookie(cookie);
		}catch (Exception e) {
			logger.error("Exception occurred while adding " + name + " to cookie  : ", e);
			throw e;
		}      
    }
	
	public void addSSOTokenCookie(HttpServletResponse response, String ssoToken) throws Exception {
		addCookie(response, QuickBookUIUtil.SSO_TOKEN_COOKIE, ssoToken);
	}
	
	public void addUserNameCookie(HttpServletResponse response, String ssoToken) throws Exception {
		RetrieveUserProfileRequestWO retrieveUserProfileRequest = new RetrieveUserProfileRequestWO();
		retrieveUserProfileRequest.setSsoToken(ssoToken);
		RetrieveUserProfileResponseWO userProfile = userProfileServiceClient.getUserProfile(retrieveUserProfileRequest);
		addCookie(response, QuickBookUIUtil.USER_NAME_COOKIE, userProfile.getUserProfile().getPersonName().getFirstName());
		
	}

	private String getSSOToken(URLConnection connection) throws Exception {
		try {
			String ssoToken = null;
			List<String> cookies = connection.getHeaderFields().get("Set-Cookie");
			if (cookies != null) {
				for (String cookie : cookies) {
					if (cookie.startsWith(QuickBookUIUtil.SSO_TOKEN_COOKIE)) {

						String cookieName = cookie.split(";")[0].split("=")[0];
						String cookieValue = cookie.split(";")[0].split("=")[1];

						if (cookieName.equals(QuickBookUIUtil.SSO_TOKEN_COOKIE)) {
							ssoToken = cookieValue;
							break;
						}
					}
				}
			}
			return ssoToken;
		} catch (Exception e) {
			logger.error("Exception occurred while fetching ssoToken : ", e);
			throw e;
		}
	}

	private String getResponseCode(URLConnection connection) throws Exception {
		BufferedReader reader = null;
		try {
			InputStream inputStream = connection.getInputStream();
			reader = new BufferedReader(new InputStreamReader(inputStream));

			StringBuilder response = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null)
				response.append(line);

			// Get response code
			JSONObject jsonObject = new JSONObject(response.toString());
			return jsonObject.get("responseCode").toString();

		} catch (Exception e) {
			logger.error("Exception occurred while fetching responseCode : ", e);
			throw e;
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (Exception e) {
			}
		}
	}
	public LoginInfoDo getUserLoginInfoResponse(String ssoToken) {

		String request = String.format(VALIDATE_USER_CARD_ACCESS_BASE_REQUEST_NEW, ssoToken);
		logger.info("Request for isValid SSO Call :" + request);
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "application/json");
		HttpEndPoint httpEndPoint = new HttpEndPoint(VALIDATE_USER_CARD_ACCESS_URL_NEW, PaymentUIUtil.HTTP_METHOD_POST);
		HttpService httpService = new HttpService();
		String response = httpService.invoke(httpEndPoint, null, headers, request);
		LoginInfoDo userLoginInfoResponseDO = new LoginInfoDo();
		logger.info("Response for isValid SSO Call :" + response);

		try {
			JSONObject jsonObj = new JSONObject(response);
			userLoginInfoResponseDO.setCode(jsonObj.get(CODE).toString());
			userLoginInfoResponseDO.setStatus((Boolean) jsonObj.get(STATUS));
			userLoginInfoResponseDO.setHttpCode(jsonObj.get(HTTPCODE).toString());
			userLoginInfoResponseDO.setLoginStatus(jsonObj.get(LOGIN_STATUS).toString());
			userLoginInfoResponseDO.setLoginMode(jsonObj.get(LOGIN_MODE).toString());
			userLoginInfoResponseDO.setIsAuthorized((Boolean) jsonObj.get(IS_AUTHENTICATED));
			if(LOGIN_STATUS_VAL.equals(jsonObj.get(LOGIN_STATUS).toString())) {
				userLoginInfoResponseDO.setUserType(jsonObj.get(USER_TYPE).toString());
				userLoginInfoResponseDO.setEmailId(jsonObj.get(EMAIL_ID).toString());
				userLoginInfoResponseDO.setUserId(Long.parseLong(jsonObj.get(USER_ID).toString()));
			}
		} catch (Exception e) {
			logger.error("Inproper Json Response from url : "+VALIDATE_USER_CARD_ACCESS_URL_NEW+", Response received : "+response);
		}

		return userLoginInfoResponseDO;
	}
}
