package com.yatra.payment.qb.corporate.constant;

public final class QBConstant {

	public static final String BTA_CTA_FLOW = "BTACTAFLOW";

	private QBConstant(){}
	
	public static final String NOT_AUTHERIZE_JSON = "{\"status\":\"FAILURE\",\"error_msg\":\"authentication failure\"}";
	public static final String NOT_ACCESS_JSON="{\"status\":\"FAILURE\",\"error_msg\":\"You don't have access to perform this operation\"}";
        public static final String NOT_ACCESS_YATRA_CARD_JSON="{\"status\":\"FAILURE\",\"error_msg\":\"You don't have access to perform this operation\", \"error_type\":\"AUTH_FAILED\"}";
	public static final String INVALID_CSRF_TOKEN_JSON="{\"status\":\"FAILURE\",\"error_msg\":\"csrf token is invalid\"}";
	public static final String SSO_TOKEN_EMPTY_JSON="{\"status\":\"FAILURE\",\"error_msg\":\"ssoToken is not present\"}";
	public static final String FATAL_ERROR_JSON="{\"status\":\"FAILURE\",\"error_msg\":\"Exception occured while processing request\"}";
	public static final String EMPTY_ARRAY_JSON = "[]";
	public static final String FATAL_ERROR_MESSAGE = "Exception occured while processing request";
	
	public static final String USER_PROFILE_BEAN_KEY = "USER_PROFILE_BEAN";
	
	public static final String USER_ROLE_ADMIN = "ADMIN";
	public static final String USER_ROLE_EA = "EA";
	public static final String USER_ROLE_SBT = "SBT";
	public static final String USER_ROLE_EMPTY = "";
	public static final String USER_ROLE_NULL = "null";
	
	public static final String QB_TYPE_KEY= "qbType";
	public static final String QB_TYPE_CORPORATE = "CORPORATE";
	public static final String QB_TYPE_PERSONAL = "PERSONAL";

	public static final String CSRF_TOKEN_KEY = "csrfToken";
	public static final String CARDID_KEY = "cardid";

	public static final String STATUS_FAILURE = "FAILURE";
	public static final String STATUS_SUCCESS = "SUCCESS";

	public static final String SHOW_CORPORATE_CARDS_TAB = "showCorpCardsTab";
	public static final String SHOW_PERSONAL_CARDS_TAB = "showPersonalCardsTab";
	public static final String IS_USER_AUTHORIZED = "isAuthorized";
	public static final String CLIENT_ID = "clientId";
	public static final String CORP_USER_ID = "corpUserId";
	public static final String CORP_USER_ROLE = "corpUserRole";
	public static final String C_TYPE = "ctype";
	
        public static final String ROLE_VIEW = "VIEW";
        public static final String ROLE_EDIT = "EDIT";
        public static final String ROLE_DELETE = "DELETE";
}
