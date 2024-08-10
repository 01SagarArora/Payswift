package com.yatra.payment.ui.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.yatra.platform.commons.property.PropertyManager;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import com.yatra.payment.payswift.beans.PayNowResponse;
import com.yatra.payment.payswift.beans.SessionStorageCardInfo;
import com.yatra.payment.ui.beans.BajajEmiRequest;
import com.yatra.payment.ui.dao.YatraPropertiesDAO;
import com.yatra.payment.ui.dao.impl.PayswiftStagesDAOImpl;
import com.yatra.payment.ui.enums.ClientType;
import com.yatra.payment.ui.enums.Status;
import com.yatra.payment.ui.exceptions.PayswiftException;
import com.yatra.platform.util.YatraUtil;
import com.yatra.tokenized.cards.util.TokenizedCardsPropertiesConstants;

import static java.util.Arrays.asList;

public class PaymentUIUtil {

	private static Logger logger = Logger.getLogger(PaymentUIUtil.class);
	public static final String CARD_ID = "cardId";
	private static final String ALGORITHM = "AES";
	public static final String IS_CARD_SAVED_TEMPORARILY  = "isCardSavedTemporarily";
	public final static String FRESCO_DESKTOP_VERSIONS = "fresco.desktop.versions";
	public final static String FRESCO_WAP_VERSIONS = "fresco.wap.versions";
	public final static String MULTI_KEY_SEPERATOR = ",";

	public static final String CONTENT_TYPE_JSON = "application/json";
	public static final String STATUS_200_OK = "200";
	private static final String HTTP_302 = "302";
	public static final String HTTP_METHOD_POST = "POST";
	public static final String HTTP_METHOD_GET = "GET";

	public static final String PAY_BY_DURTN_SEC = "payByDurationInSec";
	public static final String POLL_INTERVAL_SEC = "pollIntervalInSec";
	public static final String TG_HOST = "secure.travelguru.com";

	public static final String RETURN_ID = "retId";
	public static final String PAYMENT_OPTION = "payop";
	public static final String SUB_PAYMENT_OPTION = "sub_payop";
	public static final String SUB_PAYMENT_OPTION_QR = "qr";
	public static final String SUB_PAYMENT_OPTION_TEZ = "tez";
	public static final String IS_QR_FLOW = "isQRFlow";
	public static final String IS_GOOGLE_UPI_FLOW = "isGoogleUPIFlow";
	public static final String ROUTE_TO_B2C = "isB2CPaymentGateway";
	public static final String CRP_GAYEWAY = "corporateWisePaymentGateWay";
	public static final String TOTAL_AMOUNT = "totalAmount";
	public static final String CARD_TYPE = "ctype";
	public static final String CARD_HOLDER_NAME = "cardholder_name";
	public static final String CARD_EXP_MONTH = "cexpm";
	public static final String CARD_EXP_YEAR = "cexpy";
	public static final String CARD_SECURITY_CODE = "ccsc";
	public static final String CARD_NO = "cno";
	public static final String CARD_CODE = "cardCode";
	public static final String RURL = "rurl";
	public static final String S2SURL = "s2sUrl";
	public static final String S2S = "s2s";
	public static final String RURL_METHOD_TYPE = "rurlMethodType";
	public static final String STATUS = "status";
	public static final String PAYMENT_OPTION_CREDIT_CARD = "cc";
	public static final String PAYMENT_OPTION_DEBIT_CARD = "dc";
	public static final String PAYMENT_OPTION_DEBIT_PIN = "dcpin";
	public static final String PAYMENT_OPTION_NET_BANKING = "nb";
	public static final String PAYMENT_OPTION_CASH_CARD = "cashCard";
	public static final String PAYMENT_OPTION_ATM_CARD = "atm";
	public static final String PAYMENT_OPTION_EMI = "emi";
	public static final String PAYMENT_OPTION_MOBILE_WALLET = "mw";
	public static final String PAYMENT_OPTION_PAY_LATER = "payltr";
	public static final String PAYMENT_OPTION_EZE_CLICK = "ec";
	public static final String PAYMENT_OPTION_REWARDS = "rewards";
	public static final String PAYMENT_OPTION_QUICK_BOOK = "qb";
	public static final String PAYMENT_OPTION_EWALLET = "ew";
	public static final String PAYMENT_OPTION_PAYU = "payu";
	public static final String PAYMENT_OPTION_CREDIT_POOL = "cp";
	public static final String PAYMENT_OPTION_CORPORATE_CARD = "corpCard";
	public static final String PAYMENT_OPTION_BUDDY = "buddy";
	public static final String PAYMENT_OPTION_AMAZONPAY = "amazonPay";
	public static final String PAYMENT_OPTION_PAYTM = "paytm";
	public static final String PAYMENT_OPTION_UPI = "upi";
	public static final String PAYMENT_OPTION_GV = "gv";
	public static final String PAYMENT_OPTION_IXIGO_MONEY = "ixigoMoney";
	public static final String PAYMENT_OPTION_INDIFI = "indifi";
	public static final String PAYMENT_OPTION_PAYPAL = "paypal";
	public static final String PAYMENT_OPTION_JCASH = "jcash";
	public static final String PAYMENT_OPTION_BANKIT = "bankIt";
	public static final String PAYMENT_OPTION_EBIXCASH = "ebixCash";
	public static final String PAYMENT_OPTION_ZESTMONEY = "zestMoney";
	public static final String PAYMENT_OPTION_SPICEMONEY = "spiceMoney";
	public static final String PAYMENT_OPTION_QUICKSON = "quickson";
	public static final String PAYMENT_OPTION_PAYNEARBY = "payNearBy";
	public static final String PAYMENT_OPTION_PAYMONK = "payMonk";
	public static final String PAYMENT_OPTION_TSIWALLET = "tsiWallet";
	public static final String PAYMENT_OPTION_PAY4BIZ = "pay4Biz";
	public static final String PAYMENT_OPTION_USELOCATOR = "useLocator";
	public static final String PAYMENT_OPTION_ROINET = "roinet";
	public static final String PAYMENT_OPTION_VAKRANGEE = "vakrangee";
	public static final String PAYMENT_OPTION_PHONEPE = "phonePe";
	public static final String PAYMENT_OPTION_FINOPAY = "fnp";
	public static final String PAYMENT_OPTION_OXIGEN_B2B = "oxigen_b2b";
	public static final String PAYMENT_OPTION_GOOGLE_TEZ = "tez";
	public static final String PAYMENT_OPTION_GOOGLE_TEZ_PWA = "tez_pwa";
	public static final String PAYMENT_OPTION_GOOGLE_PAY_UPI = "googlePayUPI";
	public static final String PAYMENT_OPTION_UPI_NONMOTO = "upiNonMoto";
	public static final String PAYMENT_OPTION_TWID = "twid";
	public static final String CARD_TYPE_VISA = "VISA";
	public static final String CARD_TYPE_MASTER = "MASTER";
	public static final String CARD_TYPE_AMEX = "AMEX";
	public static final String CARD_TYPE_DINERS = "DINERS";
	public static final String CARD_TYPE_DINNERS = "DINNERS";
	public static final String CARD_TYPE_MAESTRO = "MAEST";
	public static final String CARD_TYPE_RUPAY = "RUPAY";
	public static final String CARD_TYPE_BAJAJ = "BAJAJ";
	public static final String BILLING_ADDRESS_LINE_1 = "ba1";
	public static final String BILLING_ADDRESS_LINE_2 = "ba2";
	public static final String BILLING_ADDRESS_LINE_CITY = "bacy";
	public static final String BILLING_ADDRESS_LINE_STATE = "bast";
	public static final String BILLING_ADDRESS_LINE_COUNTRY = "bacu";
	public static final String BILLING_ADDRESS_LINE_PIN = "bapi";
	public static final String BILLING_ADDRESS_ISD = "baisd";
	public static final String BILLING_ADDRESS_MOBILE = "bamob";
	public static final String IS_CARD_INTERNATIONAL = "isCardInternational";
	public static final String UTF_8_ENCODING = "UTF-8";
	public static final String CONTENT_TYPE_FORM_URLENCODED = "application/x-www-form-urlencoded;charset=" + UTF_8_ENCODING;
	public static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
	public static final String SEPARATOR = "&";
	public static final String HTTP_PROTOCOL_URL_PREFIX = "http://";
	public static final String EMAIL = "email";
	public static final String COPORATE_ID = "corporateId";
	public static final String CORPORATE_TRIP_ID = "ctripId";
	public static final String ENTITY = "entity";
	public static final String PASSWORD = "password";
	public static final String MERCHANT_CODE = "merchant";
	public static final String PRODUCT_CODE = "product";
	public static final String IS_BOOKING_DOT_COM_FLOW = "isBookingDotComFlow";
	public static final String PRIVY = "privy";
	public static final String IS_TCS_ENABLED = "isTcsEnabled";
	public static final String UI_PRODUCT_CODE = "ui_product";
	public static final String SSO_TOKEN = "ssoToken";
	public static final String MESSAGE = "message";
	public static final String MINIFY = "minify";
	public static final String ERROR_CODE_EXCEPTION = "EXCEPTION";
	public static final String ERROR_CODE_INVALID = "INVALID";
	public static final String ERROR_CODE_UNKNOWN = "UNKNOWN";
	public static final String ERROR_CODE_AUTH_FAILED = "AUTH_FAILED";
	public static final String REGEX_SPECIAL_CHAR  = "[^a-z0-9\\-\\_\\. ]";
	public static final String REGEX_SPECIAL_UPI_CHAR  = "[^a-z0-9\\- ]";
	public static final String REGEX_SPECIAL_EROOR  = "403";
	public static final String REGEX_SPECIAL_ERROR_MESSAGE  = "Invalid Input parameters";
	
	public static final String DEFAULT_TENANT_ID = "0";
	public static final String ERROR_MSG_AUTH_FAILED = "Authentication failed for this user.";
	public static final String AUTH_MODE_FB = "FACEBOOK";
	public static final String AUTH_MODE_YATRA = "YATRA";
	public static final String SUPER_PNR = "superPnr";
	public static final String TYPE = "type";
	public static final String APP_VERSION = "appVersion";
	public static final String MODE = "mode";
	public static final String CUSTOMER_ITINERARY = "customerItinerary";
	public static final String FARE_BREAKUP = "fareBreakup";
	public static final String DISCOUNTING_TYPE = "discountingType";
	public static final String FETCH_ITINERARY_RESPONSE = "FETCH_ITINERARY_RES";
	public static final String FETCH_ITINERARY_REQUEST = "FETCH_ITINERARY_REQ";
	public static final String VALIDATE_PAYMENT_WITH_LOB_REQ = "VALIDATE_PAYMENT_WITH_LOB_REQ";
	public static final String VALIDATE_PAYMENT_WITH_LOB_RES = "VALIDATE_PAYMENT_WITH_LOB_RES";
	public static final String PAY_NOW_REQ = "PAY_NOW_REQ";
	public static final String PAY_NOW_RES = "PAY_NOW_RES";
	public static final String PAY_NOW_RES_FINAL = "PAY_NOW_RES_FINAL";
	public static final String PAY_NOW_RES_FINAL_GOOGLE_TEZ = "PAY_NOW_RES_FINAL";
	public static final String PAY_REQ = "PAY_REQ";
	public static final String PAY_RES = "PAY_RES";
	public static final String LOB_PAGE_LOAD_REQ = "LOB_PAGE_LOAD_REQ";
	public static final String VALIDATE_CARD_DISCOUNTING_REQ = "VALIDATE_CARD_DISCOUNTING_REQ";
	public static final String VALIDATE_CARD_DISCOUNTING_RES = "VALIDATE_CARD_DISCOUNTING_RES";
	public static final String PARTIAL_VALIDATE_PROMO_CODE_REQ = "PARTIAL_VALIDATE_PROMO_CODE_REQ";
	public static final String PARTIAL_VALIDATE_PROMO_CODE_RES = "PARTIAL_VALIDATE_PROMO_CODE_RES";
	public static final String PUR_RQ = "PUR_RQ";
	public static final String PUR_RS = "PUR_RS";
	public static final String UUID = "uuid";
	public static final String ADDITIONAL_PAYMENT = "additionalPayment";
	public static final String SKIP_UI = "skipUI";
	public static final String VOUCHER_AMOUNT = "voucherAmount";
	public static final String VOUCHER_REDEEMED_AMOUNT = "voucherRedeemedAmount";
	public static final String VOUCHER_VALIDATED_JSON= "voucherValidatedJSON";
	public static final String VOUCHER_AUTH_CODE = "voucherAuthCode";
	public static final String VOUCHER_JSON = "vouchers";
	public static final String IS_VOUCHER_APPLIED = "isVoucherApplied";
	public static final String LOB_PAGE_LOAD = "LOB_PAGE_LOAD";
	public static final String FETCH_ITINERARY = "FETCH_ITINERARY";
	public static final String GST_UPDATE_REQ = "GST_UPDATE_REQUEST";
	public static final String GST_UPDATE_RES = "GST_UPDATE_RESPONSE";
	public static final String VALIDATE_PAYMENT = "VALIDATE_PAYMENT";
	public static final String PAYMENT_PROCESS_TYPE = "paymentProcessType";
	public static final String GOOGLE_TEZ_BANK_CODE = "GGL_TEZ";

	public static final String BANK_CODE= "bankCode";

	public static final String TTID = "ttid";
	public static final String MINKASU_ID = "minkasuId"; 
	public static final String ITINERARY_HTML = "itenaryHtml";
	public static final String AGENT_PAYMENT_OPTIONS = "agentPaymentOptions";
	public static final String FARE_BREAKUP_JSON_AS_STRING = "fareBreakup";
	public static final String USER_DETAILS = "userDetails";
	public static final String MOBILE = "mob";
	public static final String MOBILE_ISD_CODE = "mobileNoISD";
	public static final String BOOKING_DETAIL = "bookingDetail";
	public static final String CRP_INFO = "crpInfo";
	public static final String STOP_WHEN_FOUND = "stopWhenFoundQB";
	public static final String BASE_PRODUCT = "baseProduct";

	public static final String MAX_YLP = "maxRedeemableECash";
	public static final String YLP = "ylp";
	public static final String AUTO_REDEEM_ECASH = "autoRedeemECash";

	public static final String AMOUNT_DISPLAYED = "amountDisplayed";
	public static final String PAYMENT_TYPE = "paymentType";
	public static final String WALLET_AMOUNT_REDEEMED = "walletAmountRedeemed";
	public static final String PAYMENT_MODE = "paymentMode";
	public static final String PAYMENT_OPTION_PARAMETERS = "paymentOptionParameters";
	public static final String WALLET_ID = "wallet_id";
	public static final String WALLET_AMOUNT = "wallet_amount";
	public static final String AMOUNT_TO_REDEEM = "amountToRedeem";
	public static final String WALLET_DETAILS = "walletDetails";
	public static final String APP_CYBERSOURCE_FINGERPRINT_ID = "cybersourceFingerprintId";
	public static final String CANCELLATION_POLICY = "cancellationPolicy";
	public static final String WALLET_PRODUCT = "product";
	public static final String CHANNEL = "channel";
	public static final String DEVICE = "device";
	public static final String PG_GOOGLE_TEZ_RAZOR = "google_tez_rzr";
	public static final String PG_GOOGLE_TEZ_ANDROID_RAZOR = "gpay_android_rzr";

	public static final String PARTIAL_AMOUNT = "partialAmout";
	public static final String AMOUNT = "amount";
	public static final String ORIGIN = "origin";
	public static final String DESTINATION = "destination";
	public static final String MODE_PURCHASE = "pur";
	public static final String DESTINATION_COUNTRY = "destinationCountry";
	public static final String TRAVEL_DATE = "travelDate";
	public static final String AGENT_PROFILE_TYPE = "agentProfileType";

	public static final String ORIGIN_COUNTRY = "originCountry";
	public static final String CLIENT = "client";
	public static final String HASH_KEY = "hk";
	public static final String PREVIOUS_PAY_OP = "previousPayOp";

	public static final String AGENT_DATA_FUTURE_TASK = "agentDataFutureTask";
	public static final String DC_PIN_BANK_VS_PG_DETAIL_FUTURE_TASK = "dcPinBankVsPgFutureTask";
	public static final String CREDIT_POOL_FUTURE_TASK = "creditPoolFutureTask";
	public static final String CORP_CONFIG_FUTURE_TASK = "corpConfigFutureTask";
	public static final String CORP_BIN_CONFIG_FUTURE_TASK = "corpBinConfigFutureTask";

	public static final String CLIENT_DESKTOP = "DESKTOP";
	public static final String COOKIE_MAP = "cookieMap";
	public static final String PRODUCT_B2B = "B2B";
	public static final String PRODUCT_B2B_UPLOAD = "b2b-payment-upload";
	public static final String PRODUCT_B2B_IMPORT_PNR = "b2bimportpnr";
	public static final String PRODUCT_RA_UPLOAD = "rapayupload";
	public static final String PRODUCT_CORPORATE = "crp";
	public static final String PRODUCT_CORP_APP_DOM_HOTEL_ANDROID = "cdomhotelandroid";
	public static final String PRODUCT_CORP_APP_INT_HOTEL_ANDROID = "cinthotelandroid";
	public static final String PRODUCT_CORP_APP_DOM_HOTEL_IOS = "cdomhotelios";
	public static final String PRODUCT_CORP_APP_INT_HOTEL_IOS = "cinthotelios";
	public static final String PRODUCT_CORP_DOM_HOTEL = "crpdom_hotel";
	public static final String PRODUCT_CORP_INT_HOTEL = "crpint_hotel";
	public static final String PRODUCT_CORP_DOM_HOTEL_NEW = "crpdomhotel";
	public static final String PRODUCT_CORP_INT_HOTEL_NEW = "crpinthotel";
	public static final String PRODUCT_CORPORATE_BUS_ANDROID = "cbusandroid";
	public static final String PRODUCT_CORPORATE_BUS_IOS = "cbusios";
	public static final String PRODUCT_CODE_CORPORATE_BUS_DESKTOP = "corpbusdesktop";


	public static final String CARD_BIN = "cardBin";
	public static final String CARD_HASH = "cardHash";

	public static final String DISCOUNT_TYPE = "type";

	public static final String Message = "message";

	public static final String RESCHEDULING = "rescheduling";
	public static final String PRODUCT_DOM2 = "dom2";
	public static final String PRODUCT_ECASH_GV = "ecashGV";
	public static final String SUPER_PNR_PAYMENT_PORTAL = "super_pnr";
	private static final String secretKey = "XMzDdG4D03CKm2IxIWQw7g==";

	public static final String DISCOUNT = "discount";

	public static final String VISA = "VISA";

	public static final String CARD_MASK = "cardMask";
	public static final String PROMO_CODE = "promoCode";
	public static final String TOTAL_DISCOUNT = "totalDiscount";
	public static final String GST_URL = "gstUrl";

	public static final String CARD_DISCOUNTING_STATUS = "cardDiscountingStatus";
	public static final String OTHER_DISCOUNTING_STATUS = "otherDiscountingStatus";

	public static final String PROMO_CODE_CONTEXT = "FULL";
	public static final String PROMO_CODE_NODE = "promo";
	public static final String CODE = "code";
	public static final String IS_VOUCHER_APPLICABLE = "isVouchersApplicable";

	public static final String CARD_DATA = "cardData";
	public static final String TRANSACTION_TYPE = "txntype";
	public static final String TRANSACTION_TYPE_NON_MOTO = "nm";

	public static final String DESKTOP_RESPONSIVE_PAGE = "desktop/PaySwiftContainer";

	public static final String TRIP_TYPE = "tripType";
	public static final String SERVICE_TYPE = "serviceType";
	public static final String SAPG_BOOKING_REF = "booking_ref";
	public static final String FORCE_GA = "forceGA";
	public static final String BOOKING_AMOUNT = "bookingAmount";
	public static final String TOTAL_PAX = "totPax";
	
	//Cybersource specific parameters
	public static final String ORIGIN_CITY = "orgCityCode";
	public static final String DESTINATION_CITY = "destCityCode";
	public static final String ORIGIN_AIRPORT = "orgAirportCode";
	public static final String DESTINATION_AIRPORT = "destAirportCode";
	public static final String NUM_ADULT_PAX = "noAdtPax";
	public static final String NUM_CHILD_PAX = "noChdPax";
	public static final String NUM_INFANT_PAX = "noInfPax";
	public static final String NUM_TOTAL_PAX = "noTotPax";
	public static final String ADDONS = "addOns";
	public static final String CARRIER_NAME = "carrierName";
	public static final String IS_DOMESTIC = "domestic";
	public static final String TRAVEL_DATE_TIME = "travelDateTime";
	public static final String BOOKING_TYPE = "bookingType";
	public static final String CRP_BOOKING_TYPE = "crpBookingType";
	public static final String PAX_LIST = "paxList";
	public static final String OTHER_DETAILS = "otherDetails";
	public static final String TRAVELLER_SSO_ID = "travellerSSOId";
	public static final String ONLY_ECASH_GV = "isOnlyEcashGV";
	public static final String FLIGHT_NUMBER = "flightNumber";
	public static final String FLIGHT_CLASS = "flightClass";
	public static final String MARKETPLACE = "mp";
	public static final String CYBERSOURCE_FINGERPRINT_ID = "deviceFingerprintID";
	public static final String CARRIER_NUMBER = "carrierNumber";
	public static final String AGENT_CODE = "agentCode";
	public static final String AGENT_ID = "agentId";
	public static final String B2B_AGENT_ID = "b2bAgentId";
	public static final String CARRIER_CLASS = "carrierClass";
	public static final String ACTIVITY_TYPE = "activityType";
	public static final String LDA_AMOUNT = "ldaAmount";
	public static final String ASI_AMOUNT = "asiAmount";
	public static final String MONUMENT_CODE = "monumentCode";
	public static final String SPECIAL_AMOUNT_FOR_MONUMENTS = "specialAmountForMonuments";
	public static final String IXIGO_MONEY = "ixigoMoney";


	public static final String PASSTHROUGH = "passthrough";
	public static final String ISPASSTHROUGH = "isPassthrough";
	public static final String SAVECARDWITHCVV = "saveCardWithCVV";
	public static final String CARD_ACCESS_TYPE_INT = "INT";
	public static final String CARD_ACCESS_TYPE_DOM = "DOM";
	public static final String IS_TRANSACTION_INTERNATIONAL = "isTransactionInternational";

	public static final String SAVE_CARD_TEMPORARILY = "saveCardTemporarily";
	public static final String HEADER_MESSAGES = "headerMessages";

	public static final String DUMMY_SSO_TOKEN = "dummySsoToken";
	public static final String DETAIL_API_ENDPOINT = "detailAPIEndpoint";
	public static final String VALIDATE_API_ENDPOINT = "validateAPIEndPoint";

	public static final String HOTEL_NAME = "hotelName";
	public static final String HOTEL_COUNTRY = "hotelCountry";
	public static final String HOTEL_CITY = "hotelCity";
	public static final String HOTEL_GRADE = "hotelGrade";
	public static final String NUMBER_OF_NIGHT_STAY = "numberOfNightsStay";
	public static final String NUMBER_OF_ROOM = "numberOfRoom";
	public static final String NUMBER_OF_GUEST = "numberOfGuest";

	public static final String USER_ID = "userId";
	public static final String TEMPORARY_USER_ID = "TemporaryUserId";

	public static final String SAVEGUESTCARD = "saveGuestCard";

	public static final String UNIQUE_USER_ID = "paymentUniqueUserId";

	public static final String AMOUNT_IN_PAISA = "amountInPaisa";
	public static final String REDEEMED_ECASH = "redeemedEcash";
	public static final String RESPONSE_CODE = "responseCode";
	public static final String RES_CODE = "resCode";
	public static final String RES_MESSAGE = "resMsg";
	public static final String DIS_MESSAGE = "displayMessage";
	public static final String SOURCE_ID = "sourceId";
	public static final String RESPONSE_MSG = "responseMsg";
	public static final String TRANSACTION_ID = "transactionId";
	public static final String WALLETID = "walletId";
	public static final Object lock = new Object();

	public static final String HIDE_ECASH_DIV_FLAG = "hideEcashDiv";

	// for handling AJAX fail case
	public static final String IS_SUCCESSFUL = "suc";
	public static final String DESCRIPTION = "desc";
	public static final String RESPONSE_CODE_AJAX = "respc";
	public static final String RESPONSE_CODE_FAILURE = "4010";
	public static final String RESPONSE_MESSAGE_TIMEOUT = "Request Timed Out";

	public static final String CARD_TYPE_CORPORATE = "CORPORATE";
	public static final String CARD_TYPE_BTA = "BTA";
	public static final String CARD_TYPE_CTA = "CTA";

	public static final String CARD_TYPE_PCC = "PCC";
	public static final String CARD_TYPE_PCARD = "PCARD";
	public static final String CARD_TYPE_KEY_FOR_CORPORATE = "cardType";

	private static Random random = new Random();

	public static final String CURRENCY= "currency";
	public static final String CURRENCY_CODE = "curcd";
	public static final String ORDER_ID = "orderId";
	public static final String PACKAGE = "Package";
	public static final String SDK_VERSION = "SDKVersion";

	public static final String IS_ZERO_AMOUNT_TXN = "isZeroAmountTxn";

	//
	public static final String AUTO_BOOK_PRIORITY_NODE_KEY = "autoBookPriority";
	public static final String CORPCARD = "CORPCARD";
	public static final String CP = "CP";
	public static final String PAYMENT_OPTION_KEY_FOR_LOB = "paymentOption";
	public static final String PRODUCT_CODE_KEY_FOR_PAYMENT_PORTAL = "product_code";
	public static final String MERCHANT_CODE_KEY_FOR_PAYMENT_PORTAL = "merchant_code";
	public static final String MERCHANT_TXN_ID = "mtxnid";
	public static final String PAYMENT_METHOD = "paymentMethod";
	public static final String POOL_TYPE = "poolType";
	public static final String DEFAULT_YATRA_MERCHANT = "yatra";
	public static final String URL_ENCODING_FORMAT = "UTF-8";
	public static final String SKIP_OTP_LOB_FLAG = "skipOtpForCC";
	public static final String SKIP_OTP_FLAG = "skipOtpFlag";
	public static final String CVV_REQUIRED = "cvvRequired";
	public static final String GV_CODE = "voucherCode";
	public static final String GV_IS_PG_PAYMENT_REQUIRED = "isPGPaymentRequiredForVoucher";
	public static final String PP_GV_KEY = "voucher_key";
	public static final String PP_GV_DATA = "voucher_data";
	public static final String PP_GV_AUTH_CODE = "authCode";
	public static final String PP_GV_VOUCHERS = "vouchers";
	public static final String PP_GV_IS_VOUCHER_APPLIED = "is_voucher_applied";
	private static List<String> crpProductList;
	private static List<String> internationalProductList;
	private static List<String> internationalCurrencyList;
	private static List<String> smeProductList;
	private static List<String> dflightProductList;
	private static List<String> dhotelProductList;
	private static List<String> holidayProductList;
	private static List<String> twidPromoCodeList;
	public static final String GOOGLE_TEZ_FLOW = "google_tez_flow";
	public static final String IS_NO_COST_EMI_PAYMENT = "isNoCostEmiPayment";
	public static final String NCEMI_FLAG = "ncemi";
	public static final String NCEMI_HK = "ncemi_hk";
	public static final String EMI_BANK= "emiBank";
	public static final String EMI_TENURE= "emiTenure";
	public static final String NO_COST_EMI_DISCOUNT = "noCostEmiDiscount";
	public static final String ACTUAL_AMOUNT = "actualAmount";
	public static final String NO_COST_EMI_CODE = "noCostEmiCode";
	public static final String NO_COST_DISPLAY_TEXT = "No Cost EMI,On Selected Banks";
	public static final String CARD_BIN_TYPE = "card_bin_type";
	public static final String PRODUCT_TYPE = "productType";
	public static final String IS_NON_SDK_FLOW = "isNonSDKFlow";
	public static final String AMAZON_PAY_SDK_FLOW = "amazon_pay_sdk_flow";

	public static final String ORIGIN_CITY_NAME = "sourceCityName";
	public static final String DESTINATION_CITY_NAME = "destinationCityName";
	public static final String TOTAL_TRAVEL_TIME = "totalTravelTime";
	public static final String PAY_LATER_FLOW = "payLaterFlow";
	public static final String VOUCHER_LIMIT = "voucherLimit";
	public static final String VOUCHER_LIMIT_TO = "voucherLimitTo";
	public static final String ACTUAL_BOOKING_AMOUNT = "actualBookingAmount";
	public static final String PAH_HK = "pah_hk";
	public static final String GOOGLETEZ_ANDROID_FLOW = "google_tez_android_flow";
	public static final String GOOGLE_ANDROID_BANK_CODE = "GGL_TEZ_ANDROID";
	public static final String EMPTY_STRING = "";
	//PhonePe Utils
	public static final String PHONEPE_ANDROID_FLOW = "phonepe_android_flow";
	public static final String PHONEPE_VERSION_CODE = "phonePeVersionCode"; 
	public static final String PHONEPE_SWITCH_FLOW = "phonepe_switch_flow";
	public static final String PHONE_PE_BANK_CODE = "PHONE_PE";
	public static final String PHONE_PE_SWITCH_BANK_CODE = "PHONE_PE_SWITCH";
	public static final String PAYMENT_OPTION_CASHPOOL = "cashPool";
	public static final String CORP_ID = "corpId";
	public static final String BALANCE = "balance";
	public static final String VOCHER_LENGTH = "voucherLength";
	public static final String VOCHER_NUMBER = "vouherNumbers";
	public static final String SUCCESS = "success";
	public static final String FAILURE = "failure";
	public static final String PROMO_VOUCHER_FAILURE_MESSAGE = "This voucher cannot be applied with Promo Code.";
	public static final String POLL_DURATION_AFTER_GETTING_APP_CONTROL = "pollDurationAfterGettingAppControl";
	private static final String GENERIC_ERROR_DISPLAY_MESSAGE = "check balance failed, Please try again.";
	private static final String GENERIC_VALIDATE_FAILURE_ERROR_MESSAGE = "ValidateVoucher Failed";
	public static final String PAYMENT_OPTION_CRED = "cred";
	public static final String CRED_BANK_CODE = "CRED";
	public static final String CRED_APP_FLOW = "cred_app_flow";
	public static final String PAY_BY_TOKEN = "tokenization.enabled";

	public static final String IS_CORPORATE_CARD_TOKENIZED ="isCorporateCardTokenized";
	public static final String CORPORATE_CARD_TOKEN = "corporateCardToken";
	public static final String TOKEN_PROVIDER = "tokenProvider";

	public static final String CORPORATE_TOKEN_STATUS = "tokenStatus";

	public static final String IS_TOKENIZED = "isTokenized";
	public static final String SAVE_QB_CARD = "saveQBCard";

	public static final String BOOLEAN_TRUE = "true";
	public static final String BOOLEAN_FALSE = "false";

	public static final String TOKEN_STATUS_ACTIVE ="active";
	public static final String TOKEN_STATUS_INACTIVE = "inactive";

	public static final String CARD_TOKEN = "cardToken";

	public static final String HASH_SALT_PROP = "card.bin.hash.salt";
	public static final String HASHING_ALGO_SHA_256="SHA-256";
	public static final String PRIME_DESKTOP = "prime_desktop";
	public static final String IS_AUTH_PREFERRED = "isAuthPref";
	public static final String CHECK_BOOKING_STATUS_URL = "checkBookingStatusUrl";
	public static final String CURRENCY_NAME = "currencyName";

	private static List<String> trainLobProductList;

	public static final String TXN_ID = "txnId";

	public static final String TARGET_USER_ID = "targetUserId";

	public static final String INTERNATIONAL_PRODUCTS = "international.products";

	public static final String INTERNATIONAL_CURRENCY = "international.currency";




	public static List<String> createList(String inputString, String separator) {
		List<String> stringList = new ArrayList<String>();

		String[] infoArray = StringUtils.split(inputString, separator);

		if (infoArray != null && infoArray.length > 0) {
			stringList = asList(infoArray);
			return stringList;
		}
		return stringList;
	}

	public static String getUniqueRandomNumberOfFixedLength(int length) {
		Calendar calendar = Calendar.getInstance();
		long millis;
		synchronized (lock) {
			millis = calendar.getTimeInMillis();
		}
		StringBuffer randomNumberBuilder = new StringBuffer().append(millis);
		//String milliSecondString = millis +"";
		int remainingLength = length - randomNumberBuilder.length();
		if (remainingLength > 0) {
			for (int i = 0; i < remainingLength; i++) {
				randomNumberBuilder.append(random.nextInt(10));
			}

		}
		randomNumberBuilder.reverse();
		String uniqueRandomNumber = randomNumberBuilder.toString();
		return randomNumberBuilder.substring(0, length);
	}

	public static String getFirstName(String cardHolderNm) {
		String firstName = null;
		String cardHolderName = cardHolderNm == null ? null : cardHolderNm.trim();
		if (cardHolderName != null) {
			String name[] = cardHolderName.split(" ");
			firstName = name[0];
		}
		return firstName;
	}

	public static Cookie getPerticularCookie(HttpServletRequest httpRequest, String cookieName) {
		Cookie[] cookies = httpRequest.getCookies();
		if (cookies == null) {
			return null;
		}
		for (Cookie cookie : cookies) {
			if (cookie.getName().equalsIgnoreCase(cookieName)) {
				return cookie;
			}
		}

		return null;
	}

	public static HashMap<String, String> getCrpInfoNode(net.sf.json.JSONObject lobDetailsJSON) {
		if (!lobDetailsJSON.containsKey(PaymentUIUtil.CRP_INFO)) {
			return null;
		}
		net.sf.json.JSONObject crpInfo = lobDetailsJSON.getJSONObject(PaymentUIUtil.CRP_INFO);
		if (crpInfo.containsKey(PaymentUIUtil.STOP_WHEN_FOUND)) {
			crpInfo.put(PaymentUIUtil.STOP_WHEN_FOUND, String.valueOf(crpInfo.get(PaymentUIUtil.STOP_WHEN_FOUND)));
		} else {
			crpInfo.put(PaymentUIUtil.STOP_WHEN_FOUND, "true");
		}
		Iterator<?> keys = crpInfo.keys();
		HashMap<String, String> map = new HashMap<String, String>();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			if(crpInfo.getString(key).equalsIgnoreCase("null"))
				continue;
			String value = (String) crpInfo.get(key);
			if (PaymentUIUtil.BASE_PRODUCT.equalsIgnoreCase(key))
				value = CorporateProductConverterUtil.getQBProduct(value);
			map.put(key.toUpperCase(), value);
		}
		return map;
	}

	public static String getLastName(String cardHolderNm) {
		String lastName = null;
		String cardHolderName = cardHolderNm == null ? null : cardHolderNm.trim();
		if (cardHolderName != null) {
			String name[] = cardHolderName.split(" ");
			if (name.length == 2)
				lastName = name[1];
			if (name.length > 2) {
				// middleName = name[1];
				lastName = name[2];
			}
		}
		return lastName;
	}

	public static Double ceilTillTwoDecimalPlaces(double amount) {
		if (amount <= 0) {
			return 0D;
		}
		amount = Math.ceil(amount * 100);
		return amount / 100;

	}

	public static Double floorTillTwoDecimalPlaces(double amount) {
		if (amount > 0 && amount < 1) {
			return 0D;
		}
		amount = Math.floor(amount * 100);
		return amount / 100;

	}

	public static String getAccessToken(HttpServletRequest request) {
		try {
			String ssoToken = null;
			Cookie cookies[] = request.getCookies();
			if (cookies != null && cookies.length > 0) {
				for (Cookie cookie : cookies) {
					String cookieName = cookie.getName();
					if (cookieName.equals("accessToken")) {
						ssoToken = cookie.getValue();
						logger.info("PaymentUIUtil::getAccessToken AccessToken fetched from cookie: " + ssoToken);
						break;
					}
				}
			}
			return StringUtils.isNotEmpty(ssoToken) ? ssoToken : request.getParameter("accessToken");
		} catch (Exception e) {
			logger.error("Exception occurred while fetching ssoToken from cookies.", e);
			return null;
		}
	}

	public static String getSSOToken(HttpServletRequest request) {
		try {
			String ssoToken = null;
			Cookie cookies[] = request.getCookies();
			if (cookies != null && cookies.length > 0) {
				for (Cookie cookie : cookies) {
					String cookieName = cookie.getName();
					if (cookieName.equals("ssoToken")) {
						ssoToken = cookie.getValue();
						logger.info("PaymentUIUtil::getSSOToken SSOToken fetched from cookie: " + ssoToken);
						break;
					}
				}
			}
			// If "ssoToken" cookie is not present, fetch value of "ssoToken"
			// request parameter. (This is in case of mobile APP)
			return StringUtils.isNotEmpty(ssoToken) ? ssoToken : request.getParameter("ssoToken");
		} catch (Exception e) {
			logger.error("Exception occurred while fetching ssoToken from cookies.", e);
			return null;
		}
	}

	public static HashMap<String, String> getCookieMap(HttpServletRequest request) {
		HashMap<String, String> cookieMap = new HashMap<String, String>();
		try {
			Cookie cookies[] = request.getCookies();
			if (cookies != null && cookies.length > 0) {
				for (Cookie cookie : cookies) {
					cookieMap.put(cookie.getName(), cookie.getValue());
				}
			}
		} catch (Exception e) {
			logger.error("Exception occurred while fetching ssoToken from cookies.", e);
			return cookieMap;
		}
		return cookieMap;
	}


	public static Map<String, String> getFieldMap(HttpServletRequest request) {
		Map<String, String> fieldMap = new HashMap<String, String>();
		Enumeration e = request.getParameterNames();

		while (e.hasMoreElements()) {
			String fieldName = (String) e.nextElement();
			String fieldValue = request.getParameter(fieldName);

			if (StringUtils.isNotBlank(fieldValue))
				fieldMap.put(fieldName, fieldValue);
		}
		return fieldMap;
	}

	public static String getURLVersion(HttpServletRequest request) {
		String requestURI = request.getRequestURI();
		return requestURI.split("/")[3];
	}

	public static String getViewName(ClientType clientType) {
		switch (clientType) {
		case DESKTOP:
			return "desktop/PaySwiftContainer";
		case WAP:
			return "wap/PaySwiftContainer";
		default:
			return null;
		}
	}

	public static String maskCreditCardNumber(final String cardNumber) {
		if (StringUtils.isBlank(cardNumber)) {
			logger.info("got Card Number as null");
			return null;
		}
		/* as per RBI guidelines masking all characters except last four  */
			return cardNumber.replaceAll(".(?=.{4})", "x");

	}


	public static String getHashedValue(String data, String hashingAlgorithm) {
		if (data == null)
			return null;

		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance(hashingAlgorithm);
		} catch (Exception outerException) {
			logger.error(outerException.getMessage());
		}

		md.update(data.getBytes());
		byte[] mdbytes = md.digest();

		// convert the byte to hex format method 2
		StringBuffer hexString = new StringBuffer();
		for (int i = 0; i < mdbytes.length; i++) {
			String hex = Integer.toHexString(0xFF & mdbytes[i]);
			if (hex.length() == 1) {
				hexString.append('0');
			}
			hexString.append(hex);
		}
		return hexString.toString();
	}

	public static void populateMap(Map<String, String> map, String key, String value) {
		if (StringUtils.isNotEmpty(value))
			map.put(key, value);
	}

	public static void populateMap(Map<String, Object> map, String key, Object value) {
		if (value instanceof String) {
			String strValue = (String) value;
			if (StringUtils.isNotEmpty(strValue))
				map.put(key, value);
		} else if (value != null)
			map.put(key, value);
	}

	public static void populateStringMap(Map<String, String> map, String key, String value) {
		if (value instanceof String) {
			String strValue = (String) value;
			if (StringUtils.isNotEmpty(strValue))
				map.put(key, value);
		} else if (value != null)
			map.put(key, value);
	}

	public static Map<String, String> getRequestMap(HttpServletRequest request) {
		Map<String, String> requestMap = new HashMap<String, String>();
		Enumeration e = request.getParameterNames();
		while (e.hasMoreElements()) {
			String fieldName = (String) e.nextElement();
			String fieldValue = request.getParameter(fieldName);

			if (StringUtils.isNotBlank(fieldValue))
				requestMap.put(fieldName, fieldValue);
		}
		return requestMap;
	}

	public static String getPaymentDivKey(String keyPartOne, String keyPartTwo) {
		return new StringBuilder().append(keyPartOne).append("_").append(keyPartTwo).toString();
	}

	public static String getPaymentDivKey(String keyPartOne, String keyPartTwo, String keyPartThree) {
		return new StringBuilder().append(keyPartOne).append("_").append(keyPartTwo).append("_").append(keyPartThree).toString();
	}

	public static Status getStatus(Status status) {
		if (status == null)
			return Status.ENABLED;
		return status;
	}

	public static Status getStatus(String status) {
		if (StringUtils.isEmpty(status))
			return Status.ENABLED;
		return Status.valueOf(status);
	}

	public static Status getStatus(Boolean status) {
		return (status != null && status) ? Status.ENABLED : Status.DISABLED;
	}

	public static boolean isEnabled(Status status) {
		return (status == null) || status.equals(Status.ENABLED);
	}

	public static boolean isEnabled(String status) {
		return (StringUtils.isEmpty(status)) || StringUtils.equalsIgnoreCase(status, Status.ENABLED.name());
	}

	public static String convertBeanToJsonString(Object bean) {
		try {
			return new ObjectMapper().writeValueAsString(bean);
		} catch (Exception e) {
			logger.error("Exception occurred while converting bean : " + bean, e);
			return null;
		}
	}

	public static String createPostDataFromMap(Map<String, String> fieldMap) {
		StringBuffer buf = new StringBuffer();

		String ampersand = "";

		if (fieldMap == null || fieldMap.isEmpty()) {
			return null;
		}

		// append all fields in a data string
		for (String key : fieldMap.keySet()) {
			String value = (String) fieldMap.get(key);
			try {
				if ((value != null) && (value.length() > 0)) {
					// append the parameters
					buf.append(ampersand);
					buf.append(URLEncoder.encode(key, "UTF-8"));
					buf.append('=');
					buf.append(URLEncoder.encode(value, "UTF-8"));
				}
				ampersand = "&";
			}
			catch(UnsupportedEncodingException e) {
				logger.error("Error while encoding ", e); 
			}
		}

		// return string
		return buf.toString();
	}
	
	public static String createPostDataFromMapWithoutMaskedCard(Map<String, String> fieldMap) {
		StringBuffer buf = new StringBuffer();

		String ampersand = "";

		if (fieldMap == null || fieldMap.isEmpty()) {
			return null;
		}

		// append all fields in a data string
		for (String key : fieldMap.keySet()) {
			if(PaymentUIUtil.CARD_MASK.equalsIgnoreCase(key))
				continue;
			String value = (String) fieldMap.get(key);
			try {
				if ((value != null) && (value.length() > 0)) {
					// append the parameters
					buf.append(ampersand);
					buf.append(URLEncoder.encode(key, "UTF-8"));
					buf.append('=');
					buf.append(URLEncoder.encode(value, "UTF-8"));
				}
				ampersand = "&";
			}
			catch(UnsupportedEncodingException e) {
				logger.error("Error while encoding ", e); 
			}
		}

		// return string
		return buf.toString();
	}

	public static String createStringDataFromMapWithSeparator(Map<String, String> fieldMap, String separator) throws UnsupportedEncodingException {
		StringBuffer buf = new StringBuffer();

		String symbol = "";

		if (fieldMap == null || fieldMap.isEmpty()) {
			return null;
		}

		// append all fields in a data string
		for (String key : fieldMap.keySet()) {
			String value = (String) fieldMap.get(key);

			if ((value != null) && (value.length() > 0)) {
				// append the parameters
				buf.append(symbol);
				buf.append(URLEncoder.encode(key, "UTF-8"));
				buf.append('=');
				buf.append(URLEncoder.encode(value, "UTF-8"));
			}
			symbol = separator;
		}

		// return string
		return buf.toString();
	}


	public static boolean areAmountEqual(String firstAmount, String secondAmount) {
		try {
			double firstAmountInDouble = Double.parseDouble(firstAmount);
			double secondAmountInDouble = Double.parseDouble(secondAmount);
			if (firstAmountInDouble == secondAmountInDouble) {
				return true;
			}
			return false;
		} catch (Exception ex) {
			logger.info("Parsing to double failed for values " + firstAmount + " , " + secondAmount);
			return false;
		}
	}

	public static boolean validForFullRemeption(String redeemedAmount, String amountChargeable) {
		try {
			logger.debug("comparing amounts for full wallet/voucher payment scenario .");
			double redeemedAmountInDouble = Double.parseDouble(redeemedAmount);
			double chargeableAmountInDouble = floorTillTwoDecimalPlaces(Double.parseDouble(amountChargeable));
			if (redeemedAmountInDouble >= chargeableAmountInDouble) {
				logger.debug("Complete wallet/ixigo/voucher payment");
				return true;
			} else {
				logger.debug("amount not eligible for full wallet/ixigo/voucher payment ");
				return false;
			}
		} catch (Exception ex) {
			logger.info("Parsing to double failed for values " + redeemedAmount + " , " + amountChargeable);
			return false;
		}
	}

	public static double parseAmount(String amount) {
		try {
			return Double.parseDouble(amount);
		} catch (Exception ex) {
			return 0;
		}
	}

	public static String getSso(String csrfToken) throws Exception {
		byte[] raw = Base64.decodeBase64(secretKey.getBytes());
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		byte[] encryptText = Base64.decodeBase64(csrfToken.getBytes());
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, skeySpec);
		return new String(cipher.doFinal(encryptText));
	}

	public static boolean isTokenValid(String ssoToken, String csrfToken) {
		if (csrfToken == null || csrfToken.trim().isEmpty()) {
			return false;
		}
		try {
			return ssoToken.equals(getSso(csrfToken));
		} catch (Exception e) {       
			logger.error("Exception occurred while validating csrf token for ssoToken : " + ssoToken, e);
			return false;
		}
	}

	public static String getToken(String sso) {
		try {
			byte[] raw = Base64.decodeBase64(secretKey.getBytes());
			SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
			return new String(
					Base64.encodeBase64(cipher.doFinal(sso.getBytes())));
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return "Error";
		}
	}

	public static String getPortalUrl(String payOp) {
		switch (payOp){
		case("cc") : 
			return "/checkout/pay/creditcard.htm";
		case("dc") :
			return "/checkout/pay/debitcard.htm";
		case("nb") : 
			return "/checkout/pay/ibanking.htm";
		case("qb") :
			return "/checkout/pay/quickbook.htm";
		case("rewards") :
			return "/checkout/pay/rewards.htm";
		case("mw") :
			return "/checkout/pay/mobileWallet.htm"; 
		case("atm") :
			return "/checkout/pay/atm.htm";
		case("emi") :
			return "/checkout/pay/creditcard.htm";
		case("ec") :
			return "/checkout/pay/creditcard.htm";
		case("ivr") : 
			return "/checkout/pay/ivr.htm";
		case("offline") : 
			return "/checkout/pay/creditcard.htm";
		case("cashCard") :
			return "/checkout/pay/cashCard.htm";
		case("itz") :
			return "/checkout/pay/itz.htm";
		case("payu") :
			return "/checkout/pay/PayU.htm";
		case("corpCard") :
			return "/checkout/pay/corporateCard.htm";
		case("cp") :
			return "/checkout/pay/creditPool.htm";
		case("upi") :
			return "/checkout/pay/upi.htm";
		case ("dcpin"):
			return "/checkout/pay/debitCardWithAtmPin.htm";
		case("jcash") :
			return "/checkout/pay/jaldicash.htm";
		case("bankIt") :
			return "/checkout/pay/bankIt.htm";
		case("payMonk") :
			return "/checkout/pay/payMonk.htm";
		case("tsiWallet") :
			return "/checkout/pay/tsiWallet.htm";
		case("phonePe") :
			return "/checkout/pay/phonePe.htm";
		case("payNearBy") :
			return "/checkout/pay/payNearBy.htm";
		case("fnp") :
			return "/checkout/pay/finopay.htm"; 
		case("oxigen_b2b") :
			return "/checkout/pay/oxigen.htm";
		case("indifi") :
			return "/checkout/pay/indifi.htm";
		case("paypal") :
			return "/checkout/pay/paypal.htm";
		case("gv") :
			return "/checkout/pay/gv.htm";
		case("payltr") :
			return "/checkout/pay/payLater.htm";
		case("tez") :
			return "/checkout/pay/googlePay.htm";
		case("spiceMoney") :
			return "/checkout/pay/spiceMoney.htm";
		case("cashPool") :
			return "/checkout/pay/cashPool.htm";
		case("zestMoney") :
			return "/checkout/pay/zestMoney.htm";
		case("pay4Biz") :
			return "/checkout/pay/pay4biz.htm";
		case("useLocator") :
			return "/checkout/pay/useLocator.htm";
		case("roinet") :
			return "/checkout/pay/roinet.htm";
		case("vakrangee") :
			return "/checkout/pay/vakrangee.htm";
		case("ebixCash") :
			return "/checkout/pay/ebixCash.htm";
		case("tez_pwa") :
            return "/checkout/pay/googlePay.htm";
		case("twid") :
            return "/checkout/pay/twid.htm";
		case("quickson") :
            return "/checkout/pay/quickson.htm";
		case("cred") :
			return "/checkout/pay/cred/initiate.htm";

		default : 
			return "/checkout/pay/creditcard.htm";

		}
	}

	public static String getPollStatusUrl() {
		return "/checkout/pay/pollForTxnStatus.htm";
	}
	
	public static String getCredPollStatusUrl() {
		return "/checkout/pay/cred/pollForCredTxnStatus.htm";
	}

	public static String getPollTransactionUrl() {
		return "/checkout/pay/transactionStatus.htm";
	}

	public static String extractBin(String cardNumber) {
		int bin_count = Integer.valueOf(YatraUtil.getPropertyManager().getProperty("bin.length.count"));
		if (StringUtils.isNotBlank(cardNumber) && cardNumber.length() > bin_count) {
			return cardNumber.substring(0, bin_count);
		}
		return null;
	}

	public static String getAmountInRupees(Long amountInPaise, int decimalPlaces) {
		try {
			Double amountInRupees = new Double(amountInPaise) / 100;
			return getAmountInRupeesStrTillSpecifiedDecimalPlaces(amountInRupees, decimalPlaces);

		} catch (Exception e) {
			logger.error("The value of amount --> " + amountInPaise + " is not proper.");
			throw new IllegalArgumentException("The value of amount --> " + amountInPaise + " is not proper.");
		}
	}

	public static String getAmountInRupeesStrTillSpecifiedDecimalPlaces(Double amountInRupees, int decimalPlaces) {

		if (amountInRupees >= 0d) {
			DecimalFormat df = new DecimalFormat("0.00");
			df.setMinimumIntegerDigits(1);
			df.setMaximumFractionDigits(decimalPlaces);
			return df.format(amountInRupees);
		}
		return null;
	}

	public static SessionStorageCardInfo getSessionStorageCardInfo(Map<String, String> parameterMap, boolean saveCardWithCVV) {
		try{

			//Get Details from parameterMap
			String cardType = parameterMap.get(PaymentUIUtil.CARD_TYPE);
			String cardBrand = parameterMap.get(QuickBookUIUtil.CARD_BRAND);
			String name = PaymentUIUtil.getFirstName(parameterMap.get(PaymentUIUtil.CARD_HOLDER_NAME));

			String cardExpiryYear = parameterMap.get(PaymentUIUtil.CARD_EXP_YEAR);
			String cardExpiryMonth = parameterMap.get(PaymentUIUtil.CARD_EXP_MONTH);
			String cardNumber = parameterMap.get(PaymentUIUtil.CARD_NO);
			String cvv = parameterMap.get(PaymentUIUtil.CARD_SECURITY_CODE);

			//Create UserCardInfo
			if(StringUtils.equals(cardType, PaymentUIUtil.PAYMENT_OPTION_CREDIT_CARD) )
				cardType = QuickBookUIUtil.CARD_TYPE_CREDIT_CARD;

			if(StringUtils.equals(cardType, PaymentUIUtil.PAYMENT_OPTION_DEBIT_CARD) )
				cardType = QuickBookUIUtil.CARD_TYPE_DEBIT_CARD;

			SessionStorageCardInfo userCardInfo = new SessionStorageCardInfo();
			userCardInfo.setCardType(cardType);
			userCardInfo.setCardBrand(cardBrand);
			userCardInfo.setExpiryMonth(cardExpiryMonth);
			userCardInfo.setExpiryYear(cardExpiryYear);
			userCardInfo.setName(name);
			userCardInfo.setUserId(MapUtils.getLong(parameterMap, PaymentUIUtil.TEMPORARY_USER_ID));
			userCardInfo.setCardNumber(cardNumber);
			if(saveCardWithCVV)
				userCardInfo.setCvv(cvv);

			return userCardInfo;

		}catch (Exception e) {
			logger.error("Exception occurred while saving user's card details for Quick Book : ", e);
			throw e;
		}
	}
	
	
	public static SessionStorageCardInfo getSessionStorageCardInfo(JSONObject cardInfoJson, String cvv, String userId, boolean saveCardWithCVV) {
		try{
			if(cardInfoJson==null || cardInfoJson.length() == 0)
				return null;
			//Get Details from parameterMap
			String cardType = cardInfoJson.optString(PaymentUIUtil.CARD_TYPE);
			String cardBrand = cardInfoJson.optString(QuickBookUIUtil.CARD_BRAND);
			String name = cardInfoJson.optString("name");

			String cardExpiryYear = cardInfoJson.optString(PaymentUIUtil.CARD_EXP_YEAR);
			String cardExpiryMonth = cardInfoJson.optString(PaymentUIUtil.CARD_EXP_MONTH);
			String cardNumber = cardInfoJson.optString(PaymentUIUtil.CARD_NO);

			SessionStorageCardInfo userCardInfo = new SessionStorageCardInfo();
			userCardInfo.setCardType(getDecryptionKey(cardType, "enCrypTcaRdBy16B"));
			userCardInfo.setCardBrand(getDecryptionKey(cardBrand, "enCrypTcaRdBy16B"));
			userCardInfo.setExpiryMonth(getDecryptionKey(cardExpiryMonth, "enCrypTcaRdBy16B"));
			userCardInfo.setExpiryYear(getDecryptionKey(cardExpiryYear, "enCrypTcaRdBy16B"));
			userCardInfo.setName(getDecryptionKey(name, "enCrypTcaRdBy16B"));
			Map<String,String> requestMap = new HashMap<String,String>();
			requestMap.put("userid",userId);
			userCardInfo.setUserId(MapUtils.getLong(requestMap,"userid"));
			userCardInfo.setCardNumber(getDecryptionKey(cardNumber, "enCrypTcaRdBy16B"));
			if(saveCardWithCVV)
				userCardInfo.setCvv(cvv);

			return userCardInfo;

		}catch (Exception e) {
			logger.error("Exception occurred while saving user's card details for Quick Book : ", e);
			return null;
		}
	}

	public static String chopExtraChars(String str, int maxReqLength){
		if(!YatraUtil.isNullOrEmpty(str) && str.length() > maxReqLength)
			return str.substring(0, maxReqLength - 1);
		return str;
	}

	private static Map<String, String> getBillingAddress(Map<String, String> parameterMap) {
		Map<String, String> cardProperties = new HashMap<String, String>();
		PaymentUIUtil.populateStringMap(cardProperties, TokenizedCardsPropertiesConstants.ADDRESS1, parameterMap.get(PaymentUIUtil.BILLING_ADDRESS_LINE_1));
		PaymentUIUtil.populateStringMap(cardProperties, TokenizedCardsPropertiesConstants.ADDRESS2, parameterMap.get(PaymentUIUtil.BILLING_ADDRESS_LINE_2));
		PaymentUIUtil.populateStringMap(cardProperties, TokenizedCardsPropertiesConstants.CITY, parameterMap.get(PaymentUIUtil.BILLING_ADDRESS_LINE_CITY));
		PaymentUIUtil.populateStringMap(cardProperties, TokenizedCardsPropertiesConstants.STATE, parameterMap.get(PaymentUIUtil.BILLING_ADDRESS_LINE_STATE));
		PaymentUIUtil.populateStringMap(cardProperties, TokenizedCardsPropertiesConstants.PINCODE, parameterMap.get(PaymentUIUtil.BILLING_ADDRESS_LINE_PIN));
		PaymentUIUtil.populateStringMap(cardProperties, TokenizedCardsPropertiesConstants.COUNTRY, parameterMap.get(PaymentUIUtil.BILLING_ADDRESS_LINE_COUNTRY));
		PaymentUIUtil.populateStringMap(cardProperties, TokenizedCardsPropertiesConstants.ISDCODE, parameterMap.get(PaymentUIUtil.BILLING_ADDRESS_ISD));
		PaymentUIUtil.populateStringMap(cardProperties, TokenizedCardsPropertiesConstants.MOBILE_NUMBER, parameterMap.get(PaymentUIUtil.BILLING_ADDRESS_MOBILE));
		return cardProperties;
	}

	public static boolean byPassUIChecksForWhitelistedEmail(String userEmail, String productCode, PropertyManager propertyManager, PayswiftStagesDAOImpl paymentDAO) throws Exception{
		Boolean isSAPGRiskCheckByepassEnabled = propertyManager.getPropertyAsBoolean("byepass.risk.check.sapg.enabled");
		Boolean isOnlineRiskCheckByepassEnabled = propertyManager.getPropertyAsBoolean("byepass.risk.check.online.enabled");
		Boolean isSAPG = isSAPGProduct(productCode, propertyManager);

		if(isSAPG && isSAPGRiskCheckByepassEnabled){
			return getSAPGRiskCheckDisabledEmailList(propertyManager, paymentDAO).contains(userEmail);
		} else if( !isSAPG && isOnlineRiskCheckByepassEnabled ){
			return getOnlineRiskCheckDisabledEmailList(propertyManager, paymentDAO).contains(userEmail);
		}
		return false;
	}

	public static boolean isSAPGProduct(String productCode, PropertyManager propertyManager){
		List<String> sapgProductsList = new ArrayList<>();
		String sapgProducts = propertyManager.getProperty("yt.sapg.products");
		if(StringUtils.isNotEmpty(sapgProducts)) {
			sapgProductsList.addAll(asList(sapgProducts.split(",")));
			return sapgProductsList.contains(productCode);
		}
		return false;
	}

	public static List<String> getSAPGRiskCheckDisabledEmailList(PropertyManager propertyManager, PayswiftStagesDAOImpl paymentDAO){
		List<String> sapgRiskCheckDisabledEmailsList = new ArrayList<>();
		String sapgRiskCheckDisabledEmails = paymentDAO.getYatraPropertyValueFromDB("sapg.risk.check.disabled.emails");  //propertyManager.getProperty("sapg.risk.check.disabled.emails");
		if(StringUtils.isNotEmpty(sapgRiskCheckDisabledEmails)) {
			sapgRiskCheckDisabledEmailsList.addAll(asList(sapgRiskCheckDisabledEmails.split(",")));
		}
		return sapgRiskCheckDisabledEmailsList;
	}

	public static List<String> getOnlineRiskCheckDisabledEmailList(PropertyManager propertyManager, PayswiftStagesDAOImpl paymentDAO){
		List<String> onlineRiskCheckDisabledEmailsList = new ArrayList<>();
		String onlineRiskCheckDisabledEmails = paymentDAO.getYatraPropertyValueFromDB("online.risk.check.disabled.emails"); // getpropertyManager.getProperty("online.risk.check.disabled.emails");
		if(StringUtils.isNotEmpty(onlineRiskCheckDisabledEmails)) {
			onlineRiskCheckDisabledEmailsList.addAll(asList(onlineRiskCheckDisabledEmails.split(",")));
		}
		return onlineRiskCheckDisabledEmailsList;
	}


	public static String getUrlString(final String url, final Map<String, String> fieldMap) {
		StringBuffer returnUrl = new StringBuffer(url);

		if (fieldMap != null && !fieldMap.isEmpty()) {

			if (!StringUtils.contains(returnUrl.toString(), "?")) {
				returnUrl.append("?");
			} else {
				returnUrl.append("&");
			}

			for (String key : fieldMap.keySet()) {
				returnUrl.append(key + "=" + fieldMap.get(key) + "&");

			}
		}

		String returnUrlString = returnUrl.toString();
		returnUrlString = StringUtils.chop(returnUrlString);

		return returnUrlString;
	}

	public static String appendSsoTokenInS2SCall(String s2surlString, String ssoToken) {
		if(StringUtils.isBlank(ssoToken) || StringUtils.isBlank(s2surlString)){
			logger.debug("Sso Token not found in request ");
		}
		else{
			logger.debug("Appending ssoToken to url , sso is : "+ssoToken);
			s2surlString+="&ssoToken="+ssoToken;
		}
		return s2surlString;
	}

	public static String sendRequest(String postUrl, String methodType) {
		URL url = null;
		BufferedReader reader = null;
		StringBuilder stringBuilder;
		try {
			// create the HttpURLConnection
			url = new URL(postUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			connection.setRequestMethod(methodType);

			// time-out of 10 sec
			connection.setReadTimeout(10 * 1000);
			connection.connect();

			// read the output from the server
			reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			stringBuilder = new StringBuilder();

			String line = null;
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line + "\n");
			}
			logger.info("Request sent to URL :"+extractBaseUrl(postUrl)+" : with params "+getMaskedQueryParamsMap(postUrl)+"   Response Code : " + connection.getResponseCode() + ". Response Message :  " + connection.getResponseMessage() + ". Response : "
					+ stringBuilder);
			return stringBuilder.toString();

		} catch (Exception ex) {
			logger.error("Exception occurred while sending post request on url "+extractBaseUrl(postUrl)+": with Params "+getMaskedQueryParamsMap(postUrl)+" "+ Arrays.toString(ex.getStackTrace()));
			return null;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ioe) {
					logger.error("Exception occurred while closing BufferedReader : " + ioe);
				}
			}
		}
	}
	
	public static String sendRequestWithoutResponseLogs(String postUrl, String methodType) {
		URL url = null;
		BufferedReader reader = null;
		StringBuilder stringBuilder;
		try {
			// create the HttpURLConnection
			url = new URL(postUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			connection.setRequestMethod(methodType);

			// time-out of 10 sec
			connection.setReadTimeout(10 * 1000);
			connection.connect();

			// read the output from the server
			reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			stringBuilder = new StringBuilder();

			String line = null;
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line + "\n");
			}
			logger.debug("Request sent to URL : " + postUrl + ". Response Code : " + connection.getResponseCode() + ". Response Message :  " + connection.getResponseMessage());
			return stringBuilder.toString();

		} catch (Exception e) {
			logger.error("Exception occurred while sending post request on URL : " + postUrl + " : " + e);
			return null;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ioe) {
					logger.error("Exception occurred while closing BufferedReader : " + ioe);
				}
			}
		}
	}


	public static String sendGetRequest(String url) throws Exception{
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(url);
		HttpResponse response = client.execute(request);
		StringBuilder textView = new StringBuilder();
		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		String line = "";
		while ((line = rd.readLine()) != null) {
			textView.append(line);
		}
		return textView.toString();
	}

	public static boolean checkS2sOnFailureEligibility(
			PayNowResponse payNowResponse) {
		if(payNowResponse!=null 
				&& payNowResponse.getValidationResponse()!=null 
				&& payNowResponse.getValidationResponse().getS2S()!=null 
				&& "true".equalsIgnoreCase(payNowResponse.getValidationResponse().getS2S()) 
				&& payNowResponse.getValidationResponse().getS2SOnFailure()!=null 
				&& "true".equalsIgnoreCase(payNowResponse.getValidationResponse().getS2SOnFailure()))
			return true;
		return false;
	}

	public static boolean checkS2sEligibility(PayNowResponse payNowResponse, boolean txnStatus) {
		if(payNowResponse!=null && payNowResponse.getValidationResponse()!=null){
			if(txnStatus && payNowResponse.getValidationResponse().getS2S()!=null && "true".equalsIgnoreCase(payNowResponse.getValidationResponse().getS2S())) 
				return true;
			if(!txnStatus && payNowResponse.getValidationResponse().getS2SOnFailure()!=null	&& "true".equalsIgnoreCase(payNowResponse.getValidationResponse().getS2SOnFailure()))
				return true;
		}
		return false;
	}

	public static Long convertToLongDefaultZero(String value){
		try{
			if(StringUtils.isEmpty(value)){
				return 0L;
			}
			return Long.parseLong(value.trim());
		} catch(Exception ex){
			logger.error("Exception occurred while coverting to String value to Long, value received : " + value);
			return 0L;
		}
	}

	public static List<String> getCrpProducts(){
		String crpProducts = YatraUtil.getPropertyManager().getProperty("crp.products");
		if(StringUtils.isNotBlank(crpProducts)){
			return Arrays.asList(crpProducts.split(","));
		}
		return null;
	}

	public static String getCardHashForQBTxn(String cardId, String ssoToken, String userId) {
		String url = "http://payment-tokenize-1/checkout/card/get-card-hash?cardid=" + cardId + "&ssoToken=" + ssoToken+"&userId="+userId;
		String resp = sendRequestWithoutResponseLogs(url, "POST");
		logger.info("got response for cardId: " + cardId );
		JSONObject respJson;
		try {
			respJson = new JSONObject(resp);
			if(respJson!=null && respJson.has("cardHash"))
				return respJson.getString("cardHash");
		} catch (JSONException e) {
			logger.error("Could not get valid/parsable response from get-card-number API", e);
		}
		return "";
	}


	public static String getCardNumberForQBTxn(String cardId, String ssoToken) {
		String url = YatraUtil.getPropertyManager().getProperty("payment.tokenize.qb.card.url");

		url = url+"?cardid=" + cardId + "&ssoToken=" + ssoToken;
		String resp = sendRequestWithoutResponseLogs(url, "POST");
		logger.info("got response for cardId: " + cardId );
		JSONObject respJson;
		try {
			respJson = new JSONObject(resp);
			if(respJson!=null && respJson.has("cardNo"))
				return respJson.getString("cardNo");
		} catch (JSONException e) {
			logger.error("Could not get valid/parsable response from get-card-number API", e);
		}
		return "";
	}
	public static String getTokenBinForPromo(String cardId, String ssoToken) {
		String url = YatraUtil.getPropertyManager().getProperty("payment.tokenize.card.token.bin.url");

		url = url + "?cardid=" + cardId + "&ssoToken=" + ssoToken;
		String resp = sendRequestWithoutResponseLogs(url, "POST");
		logger.info("got response for cardId: " + cardId);
		JSONObject respJson;
		try {
			respJson = new JSONObject(resp);
			if (respJson != null && respJson.has("tokenBin"))
				return respJson.getString("tokenBin");
		} catch (JSONException e) {
			logger.error("Could not get valid/parsable response from get-card-token-bin API", e);
		}
		return "";

	}



	public static JSONObject getSavedCardDetails(String cardId, String ssoToken) {
		String url = "http://payment.service/checkout/card/get-complete-card-details?cardid=" + cardId + "&ssoToken=" + ssoToken;
		String resp = sendRequest(url, "POST");
		JSONObject respJson;
		try {
			respJson = new JSONObject(resp);
			logger.info("got following response message for cardId: " + cardId + " : " + respJson.optString("message"));
			return respJson;
		} catch (JSONException e) {
			logger.error("Could not get valid/parsable response from get-card-number API", e);
		}
		return null;
	}
	
	
	public static String getMinkasuFlag(String superPnr, String payop, String bankCode, String bin, String paymentProcessType, String product) {
		/*if(!YatraUtil.isNullOrEmpty(product) && product.contains("ios"))
			return "false";*/
		String url = "http://payment.service/checkout/pay/getMinkasuFlag?superPnr=" + superPnr + "&payop=" + payop + "&bankCode=" + bankCode + "&bin=" + bin + "&paymentProcessType=" + paymentProcessType;
		logger.info("sending post request on url : http://payment.service/checkout/pay/getMinkasuFlag?superPnr=" + superPnr + "&payop=" + payop + "&bankCode=" + bankCode +"&paymentProcessType=" + paymentProcessType);
		String resp = sendRequest(url, "GET");
		logger.info("got following response for getMinkasuFlag for superPnr: " + superPnr + " : " + resp);
		JSONObject respJson;
		try {
			respJson = new JSONObject(resp);
			if(respJson!=null && respJson.has("status") && "true".equalsIgnoreCase(respJson.getString("status")))
				return "true";
		} catch (Exception e) {
			logger.error("Could not get valid/parsable response from getMinkasuFlag API", e);
			return "false";
		}
		return "false";
	}

	public static List<String> getCorporateProductList() {
		if (crpProductList == null || crpProductList.isEmpty()) {
			crpProductList = new ArrayList<String>();
			String crpProducts = YatraUtil.getPropertyManager().getProperty("crp.products");
			if (StringUtils.isNotEmpty(crpProducts))
				crpProductList.addAll(Arrays.asList(crpProducts.split(",")));
		}
		return crpProductList;
	}

	public static List<String> getInternationalProductList(YatraPropertiesDAO yatraPropertiesDAO) {
		if (internationalProductList == null || internationalProductList.isEmpty()) {
			internationalProductList = new ArrayList<String>();
			String internationalProducts = yatraPropertiesDAO.getYatraPropValue(INTERNATIONAL_PRODUCTS);
			if (StringUtils.isNotEmpty(internationalProducts))
				internationalProductList.addAll(Arrays.asList(internationalProducts.split(",")));
		}
		return internationalProductList;
	}

	public static boolean checkIfCorporateProduct(String product) {
		crpProductList = getCorporateProductList();
		if(crpProductList.contains(product))
			return true;
		return false;
	}

	public static boolean checkIfInternationalProduct(String product,YatraPropertiesDAO yatraPropertiesDAO) {
		internationalProductList = getInternationalProductList(yatraPropertiesDAO);
		if(internationalProductList.contains(product))
			return true;
		return false;
	}

	public static List<String> getSMEProductList() {
		if (smeProductList == null || smeProductList.isEmpty()) {
			smeProductList = new ArrayList<String>();
			String smeProducts = YatraUtil.getPropertyManager().getProperty("sme.master.product.list");
			if (StringUtils.isNotEmpty(smeProducts))
				smeProductList.addAll(Arrays.asList(smeProducts.split(",")));
		}
		return smeProductList;
	}

	public static boolean isSMEProduct(String productCode) {
		smeProductList = getSMEProductList();
		if(smeProductList.contains(productCode))
			return true;
		return false;
	}
	
	public static List<String> getDomesticFlightProductList(YatraPropertiesDAO yatraPropertiesDAO) {
		if (dflightProductList == null || dflightProductList.isEmpty()) {
			dflightProductList = new ArrayList<String>();
			String dflightProducts = yatraPropertiesDAO.getYatraPropValue("domestic.flight.products");
			if (StringUtils.isNotEmpty(dflightProducts))
				dflightProductList.addAll(Arrays.asList(dflightProducts.split(",")));
		}
		return dflightProductList;
	}
	
	public static List<String> getDomesticHotelProductList(YatraPropertiesDAO yatraPropertiesDAO) {
		if (dhotelProductList == null || dhotelProductList.isEmpty()) {
			dhotelProductList = new ArrayList<String>();
			String dHotelProducts = yatraPropertiesDAO.getYatraPropValue("domestic.hotel.products");
			if (StringUtils.isNotEmpty(dHotelProducts))
				dhotelProductList.addAll(Arrays.asList(dHotelProducts.split(",")));
		}
		return dhotelProductList;
	}
	
	public static List<String> getHolidayProductList(YatraPropertiesDAO yatraPropertiesDAO) {
		if (holidayProductList == null || holidayProductList.isEmpty()) {
			holidayProductList = new ArrayList<String>();
			String dHotelProducts = yatraPropertiesDAO.getYatraPropValue("yt.holiday.products");
			if (StringUtils.isNotEmpty(dHotelProducts))
				holidayProductList.addAll(Arrays.asList(dHotelProducts.split(",")));
		}
		return holidayProductList;
	}
	
	public static JSONObject validateVoucherErrorJson(int validateErrorCode) {
		String errorMsg = null;
		String displayMsg = null;
		switch (validateErrorCode) {
		case 1: errorMsg = GENERIC_VALIDATE_FAILURE_ERROR_MESSAGE;
				displayMsg = GENERIC_ERROR_DISPLAY_MESSAGE;
			break;
		case 2: errorMsg = GENERIC_VALIDATE_FAILURE_ERROR_MESSAGE;
				displayMsg = "This voucher cannot be used on this product.";
			break;
		case 3: errorMsg = GENERIC_VALIDATE_FAILURE_ERROR_MESSAGE;
				displayMsg = "This voucher cannot be used with any other voucher.";
			break;
		case 4: errorMsg = GENERIC_VALIDATE_FAILURE_ERROR_MESSAGE;
				displayMsg = GENERIC_ERROR_DISPLAY_MESSAGE;
			break;

		default: errorMsg = GENERIC_VALIDATE_FAILURE_ERROR_MESSAGE;
				 displayMsg = "This voucher cannot be applied with Promo Code.";
			break;
		}
		
		JSONObject errorJson = new JSONObject();
		try {
			errorJson.put("resCode", 1);
			errorJson.put("resMsg", errorMsg);
			errorJson.put("displayMessage", displayMsg);
			errorJson.put("status", "failure");
		} catch (JSONException ex) {
			logger.error("Error while creating error JSON for check balance API, Exception is : ", ex);
		}
		return errorJson;
	}
	
	public static String validateVoucherErrorMsg(int validateErrorCode) {
		String displayMsg = null;
		switch (validateErrorCode) {
		case 1: displayMsg = GENERIC_ERROR_DISPLAY_MESSAGE;
			break;
		case 2: displayMsg = "This voucher cannot be used on this product.";
			break;
		case 3: displayMsg = "This voucher cannot be used with any other voucher.";
			break;
		case 4: displayMsg = GENERIC_ERROR_DISPLAY_MESSAGE;
			break;

		default: displayMsg = "This voucher cannot be applied with Promo Code.";
			break;
		}
		
		return displayMsg;
	}
	
	public static String sendSSOJsonPostRequest(String postData, String url, String tenantId) throws IOException  {
		 URL urlObj = new URL(url);
	     HttpURLConnection   connection = (HttpURLConnection )urlObj.openConnection();
	     
	     connection.setDoOutput(true);
	     connection.setRequestMethod("POST");
	     connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
	     if(!YatraUtil.isNullOrEmpty(tenantId))
	    	 connection.setRequestProperty("com.yatra.tenant.header.tenantId", tenantId);
	
	     OutputStream out = connection.getOutputStream();
	     out.write(postData.getBytes("UTF-8"));
	     out.close();
	     
	     StringBuilder sb = new StringBuilder();
	     int responseCode = connection.getResponseCode();
	     logger.debug("Response code : "+responseCode);
	     BufferedReader reader = new BufferedReader(responseCode == 200 ? new InputStreamReader(connection.getInputStream()) : new InputStreamReader(connection.getErrorStream()));
	     String line = null;
	
	     while ((line = reader.readLine()) != null) {
	         sb.append(line);
	     }
	     String response= new String(sb);
	     logger.debug("Response : "+response);
		 return response;
	}
    
    public static String getEncryptedData(String data, String keyPhrase) throws Exception {
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

    public static String getDecryptionKey(String data, String keyPhrase) throws Exception {
        if (keyPhrase == null || data == null) {
        	return "";
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

	public static boolean isCardBasedPayop(String payop) {
		if("cc".equalsIgnoreCase(payop) || "dc".equalsIgnoreCase(payop)
				|| "qb".equalsIgnoreCase(payop)	|| "emi".equalsIgnoreCase(payop)
				|| "corpCard".equalsIgnoreCase(payop) || "rewards".equalsIgnoreCase(payop)
				|| "new_reward".equalsIgnoreCase(payop))	
		return true;
		else return false;
	}
	
	public static String makeCheckTwidEligibilityCall(String userMobile,
			String amountToCollect, String superPnr, String product) throws Exception {
		String url = "http://payment.service/checkout/pay/checkTwidEligibility?mob=" + userMobile + "&amount=" + amountToCollect + "&superPnr=" + superPnr + "&product=" + product;
		logger.info("Twid checkeligibility url : " + url);
		return sendGetRequest(url);
	}
	
	public static List<String> getTwidSpecificPromoCodeList(YatraPropertiesDAO yatraPropertiesDAO) {
		if (twidPromoCodeList == null || twidPromoCodeList.isEmpty()) {
			twidPromoCodeList = new ArrayList<String>();
			
			String twidPromoCodes = yatraPropertiesDAO.getYatraPropValue("twid.promo.codes.list");
			//String twidPromoCodes = YatraUtil.getPropertyManager().getProperty("twid.promo.codes.list","");
			
			if (StringUtils.isNotEmpty(twidPromoCodes)) {
				twidPromoCodeList.addAll(Arrays.asList(twidPromoCodes.split(",")));
			} else {
				twidPromoCodeList.addAll(Arrays.asList("NONE".split(",")));
			}
		}
		return twidPromoCodeList;
	}
	
	
	 public void validSpecialCharachers(Map<String, String> inputParameter)
		{
			String input = "";
			Pattern p = Pattern.compile(REGEX_SPECIAL_CHAR, Pattern.CASE_INSENSITIVE);
			for (Map.Entry<String, String> set : inputParameter.entrySet()) {
				input = set.getValue();
				if(input!=null) {
				Matcher m = p.matcher(input);
				boolean res = m.find();
				if (res) {
					//REGEX_SPECIAL_UPI_CHAR
					Pattern pupi = Pattern.compile(REGEX_SPECIAL_UPI_CHAR, Pattern.CASE_INSENSITIVE);
					if("payerVA".equalsIgnoreCase(set.getKey()) && input.split("@").length<3) {
						String[] vpaArr = input.split("@");
						for(String vpa : vpaArr) {
						Matcher mupi = pupi.matcher(vpa);
						System.out.println("vpa : "+vpa);
						boolean resupi = m.find();
						if(resupi) {
							logger.error("validation failed for key : " + set.getKey() + " value : " + input);
							throw new PayswiftException(REGEX_SPECIAL_EROOR, REGEX_SPECIAL_ERROR_MESSAGE);
						}
						}
					}
					else {
					logger.error("validation failed for key : " + set.getKey() + " value : " + input);
					throw new PayswiftException(REGEX_SPECIAL_EROOR, REGEX_SPECIAL_ERROR_MESSAGE);
					}
				}
				}
			}
		}

	public static JSONObject  getTokenizedCardDetail(String cardId, String ssoToken) {
		String url = YatraUtil.getPropertyManager().getProperty("payment.tokenized.card.details.endpoint");
		url = url+"?cardid=" + cardId + "&ssoToken=" + ssoToken;

		String resp = sendRequestWithoutResponseLogs(url, "POST");
		logger.info("got response for cardId: " + cardId+" : "+resp);
		JSONObject respJson = null ;
		try {
			respJson = new JSONObject(resp);
			if(respJson!=null)
				return respJson;
		} catch (JSONException e) {
			logger.error("Could not get valid/parsable response from get-tokenized-card-details API", e);
		}
		return respJson;
	}
	public static String createHash(String word,byte[] salt){
		return createHash(word, PaymentUIUtil.HASHING_ALGO_SHA_256,salt);
	}
	public static String createHash(String word,String algo,byte[] salt) {
		byte[] hashseq = word.getBytes();
		StringBuffer hexString = new StringBuffer();

		try {
			MessageDigest algorithm = MessageDigest.getInstance(algo);
			algorithm.reset();
			algorithm.update(salt);
			byte messageDigest[] = algorithm.digest(hashseq);

			for (int i = 0; i < messageDigest.length; i++) {
				String hex = Integer.toHexString(0xFF & messageDigest[i]);
				if (hex.length() == 1)
					hexString.append("0");
				hexString.append(hex);
			}

		} catch (NoSuchAlgorithmException nsae) {
			logger.error("Exception while generating hash : "+nsae.getMessage());
		}

		return hexString.toString();

	}



	public static List<String> getTrainLobProductList() {
		if (trainLobProductList == null || trainLobProductList.isEmpty()) {
			trainLobProductList = new ArrayList<String>();
			String trainLobProducts = YatraUtil.getPropertyManager().getProperty("train.products");
			if (StringUtils.isNotEmpty(trainLobProducts))
				trainLobProductList.addAll(Arrays.asList(trainLobProducts.split(",")));
		}
		return trainLobProductList;
	}

	public static boolean checkIfTrainLobProduct(String product) {
		trainLobProductList = getTrainLobProductList();
		if(trainLobProductList.contains(product))
			return true;
		return false;
	}
	
	public static JSONObject validateUpiId(String superPnr, String vpa, String merchantCode, String productCode) {
		String url = "http://payment.service/checkout/pay/validateUpiId?superPnr=" + superPnr +
				"&vpa=" + vpa + "&productCode=" + productCode + "&merchantCode="+merchantCode;
		String resp = sendRequest(url, "GET");
		logger.info("got following response for validateUpiId for superPnr: " + superPnr + " : " + resp);
		JSONObject respJson = null;
		try {
			respJson = new JSONObject(resp);
			return respJson;
		} catch (Exception e) {
			logger.error("Could not get valid/parsable response from validateUpiId API", e);
		}
		return respJson;
	}
	
	
	
	public static String checkEmiEligibility(String requestJson) {
		
		String url = "http://payment.service/checkout/pay/checkBajajEmiEligibility";

		return sendJsonRequest(url, requestJson, "POST");

	}

	public static String sendJsonRequest(String postUrl, String jsonInput, String methodType) {
	    URL url = null;
	    BufferedReader reader = null;
	    StringBuilder stringBuilder;
	    try {
	        // Create the HttpURLConnection
	        url = new URL(postUrl);
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

	        // Set the request method and content type for JSON
	        connection.setRequestMethod(methodType);
	        connection.setRequestProperty("Content-Type", "application/json");
	        connection.setDoOutput(true);

	        // Set a timeout (e.g., 10 seconds)
	        connection.setReadTimeout(10 * 1000);

	        // Write the JSON data to the request body
	        OutputStream os = connection.getOutputStream();
	        os.write(jsonInput.getBytes("UTF-8"));
	        os.flush();
	        os.close();

	        // Connect to the server
	        connection.connect();

	        // Read the response from the server
	        reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	        stringBuilder = new StringBuilder();

	        String line;
	        while ((line = reader.readLine()) != null) {
	            stringBuilder.append(line);
	        }

	        // Log the response information
	        logger.debug("Request sent to URL: " + postUrl + ". Response Code: " + connection.getResponseCode() + ". Response Message: " + connection.getResponseMessage() + ". Response: " + stringBuilder);

			String responseBody = stringBuilder.toString();
			//we need to manage the responseBody and returnType here also. 
			// Parse the JSON response manually
    		
            return responseBody;

	    } catch (Exception e) {
	        logger.error("Exception occurred while sending post request to URL: " + postUrl + " : " + e);
	        return "";

	    } finally {
	        if (reader != null) {
	            try {
	                reader.close();
	            } catch (IOException ioe) {
	                logger.error("Exception occurred while closing BufferedReader: " + ioe);
	            }
	        }
	    }
	}

	
	public static String getJson(Object request) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			
			return mapper.writeValueAsString(request);
			
		}catch(Exception e) {
			logger.error("Error occured while parsing json",e);
			return null;
		}
		
	}

	public static String getCardNumberForNonTokenizedQBTxn(String cardId, String ssoToken) {
		String url = YatraUtil.getPropertyManager().getProperty("payment.service.qb.card.url");

		url = url+"?cardid=" + cardId + "&ssoToken=" + ssoToken;
		String resp = sendRequestWithoutResponseLogs(url, "POST");
		logger.info("got response for cardId: " + cardId );
		JSONObject respJson;
		try {
			respJson = new JSONObject(resp);
			if(respJson!=null && respJson.has("cardNo"))
				return respJson.getString("cardNo");
		} catch (JSONException e) {
			logger.error("Could not get valid/parsable response from get-card-number API", e);
		}
		return "";
	}


	public static String extractCardBin(String cno) {
		String bin = null;
		int binSize = YatraUtil.getPropertyManager().getPropertyAsInt("bin.length.count");
		if (StringUtils.isNotBlank(cno)) {
			if (cno.length() >= binSize) {
				bin = cno.substring(0, binSize);
			} else {
				bin = getSuffixedBinOfProvidedSize(cno, binSize);
			}
		}
		return bin;

	}
	public static  String getSuffixedBinOfProvidedSize(String bin ,int binSize){
		StringBuilder suffixedBin = new StringBuilder();
		if( StringUtils.isNotBlank(bin) && binSize>bin.length()){
			suffixedBin.append(bin);
			while(suffixedBin.length()<binSize){
				suffixedBin.append("0");
			}
		}
		return suffixedBin.toString();
	}

	public static boolean checkIfInternationalCurrency(String currency,YatraPropertiesDAO yatraPropertiesDAO) {

		internationalCurrencyList = getInternationalCurrencyList(yatraPropertiesDAO);
		if(internationalCurrencyList.contains(currency))
			return true;
		return false;
	}

	public static List<String> getInternationalCurrencyList(YatraPropertiesDAO yatraPropertiesDAO) {
		if ( internationalCurrencyList== null || internationalCurrencyList.isEmpty()) {
			internationalCurrencyList = new ArrayList<String>();
			String internationalCurrencies = yatraPropertiesDAO.getYatraPropValue(INTERNATIONAL_CURRENCY);
			if (StringUtils.isNotEmpty(internationalCurrencies))
				internationalCurrencyList.addAll(Arrays.asList(internationalCurrencies.split(",")));
		}
		return internationalCurrencyList;
	}
	public static void maskCreditCardDetails(Map<String, String> paramMap) {
		if (MapUtils.getString(paramMap,PaymentUIUtil.CARD_NO)!=null)
			paramMap.put(PaymentUIUtil.CARD_NO, PaymentUIUtil.maskCreditCardNumber(paramMap.get(PaymentUIUtil.CARD_NO)));
		if (MapUtils.getString(paramMap, PaymentUIUtil.CARD_SECURITY_CODE)!=null)
			paramMap.put(PaymentUIUtil.CARD_SECURITY_CODE, paramMap.get(PaymentUIUtil.CARD_SECURITY_CODE).replaceAll(".", "x"));
		if (MapUtils.getString(paramMap, PaymentUIUtil.CARD_EXP_MONTH)!=null)
			paramMap.put(PaymentUIUtil.CARD_EXP_MONTH, paramMap.get(PaymentUIUtil.CARD_EXP_MONTH).replaceAll(".", "x"));
		if (MapUtils.getString(paramMap, PaymentUIUtil.CARD_EXP_YEAR)!=null)
			paramMap.put(PaymentUIUtil.CARD_EXP_YEAR, paramMap.get(PaymentUIUtil.CARD_EXP_YEAR).replaceAll(".", "x"));
		if (MapUtils.getString(paramMap, PaymentUIUtil.CARD_BIN)!=null)
			paramMap.put(PaymentUIUtil.CARD_BIN, paramMap.get(PaymentUIUtil.CARD_BIN).replaceAll(".", "x"));
		if (MapUtils.getString(paramMap, "bin")!=null)
			paramMap.put("bin", paramMap.get("bin").replaceAll(".", "x"));

	}

	public static Map<String,String> getMapForLoggingPurpose(Map<String,String> paramMap){
		Map<String,String> mapForLogging = new HashMap<>(paramMap);
		maskCreditCardDetails(mapForLogging);
		return  mapForLogging;

	}
	public static Map<String, String> getMaskedQueryParamsMap(String postUrlString) {
		Map<String, String> queryMap = new HashMap<>();
		try {
		if (StringUtils.isBlank(postUrlString)) {
			return queryMap;
		}
		postUrlString = postUrlString.split("\\?").length>2 ? postUrlString.split("\\?")[1]:postUrlString;
		String[] pairs = postUrlString.split("&");
		for (String pair : pairs) {

			String[] keyValue = pair.split("=", 2);

			String key = keyValue[0];
			String value = keyValue.length > 1 ? keyValue[1] : "";

			key = decode(key);
			value = decode(value);

			queryMap.put(key, value);
		}
		}
		catch(Exception ex) {
			logger.error("Exception occurred while preparing map from query string ",ex);;
		}

		return getMapForLoggingPurpose(queryMap);
	}
	public  static String decode(String value) {
		try {
			return java.net.URLDecoder.decode(value, "UTF-8");
		} catch (Exception e) {
			return value;
		}
	}
	public static  String extractBaseUrl(String url)  {
		try {
			return url.replace(new URL(url).getQuery(), "");
		}
		catch (Exception ex){
			logger.error("Exception occurred while fetching url from post url ",ex);
		}
		return  "";
	}
}
