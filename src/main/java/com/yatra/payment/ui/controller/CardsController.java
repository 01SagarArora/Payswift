package com.yatra.payment.ui.controller;

import com.yatra.express.cards.client.ExpressCardsServiceClient;
import com.yatra.express.cards.util.ExpressCardsPropertiesConstants;
import com.yatra.express.cards.util.RSAEncryptionUtil;
import com.yatra.express.cards.v3.beans.ResponseStatus;
import com.yatra.express.cards.v3.beans.UserCardInfo;
import com.yatra.express.cards.v3.beans.UserCardInfoResponse;
import com.yatra.express.cards.v3.beans.UserCardsDisplayResponse;
import com.yatra.payment.payswift.beans.PaytmResponseBean;
import com.yatra.payment.payswift.beans.SessionStorageCardInfo;
import com.yatra.payment.ui.enums.FailureCode;
import com.yatra.payment.ui.exceptions.PayswiftException;
import com.yatra.payment.ui.helper.PaymentUIHelper;
import com.yatra.payment.ui.payswift.service.BinDetailsService;
import com.yatra.payment.ui.payswift.service.CardsService;
import com.yatra.payment.ui.service.CardTypeIdentifier;
import com.yatra.payment.ui.util.CardsUtil;
import com.yatra.payment.ui.util.LoggingUtil;
import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.platform.util.YatraUtil;
import com.yatra.tokenized.cards.client.TokenizedCardsServiceClient;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.yatra.payment.ui.util.BinDetailsUtil.GET_BIN_DETAILS_API;
import static com.yatra.payment.ui.util.BinDetailsUtil.errorJson;
import static com.yatra.platform.commons.utils.RequestUtils.getParamaters;
import static com.yatra.platform.commons.utils.RequestUtils.getQueryString;

@Controller
public class CardsController {

	@Autowired
    private CardsService cardsService;
	@Autowired
    private PaymentUIHelper paymentUIHelper;
	@Autowired
    private CardTypeIdentifier mainCardIdentifier;
    @Autowired
    private BinDetailsService binDetailsService;
    @Autowired
    private ExpressCardsServiceClient expressCardsServiceClient;


	@Autowired
	private TokenizedCardsServiceClient tokenizedCardsServiceClient;

	private static Logger logger = Logger.getLogger(CardsController.class);

	public static final String DELETE_TOKENIZED_CARD = "deleteTokenisedCardWithUserId";

	private static Map<String, String> cobrandedEmiBanksBinMapping = new HashMap<>();
	private static List<String> cobrandedCardIssuingBanks = Arrays.asList("icici","amex","indusind");
	private static List<String> amexBinIssuedByIcici = Arrays.asList("3747400","3747401","3747402","3747403","3747404","3747405",
			"3747406","3747407","3747408","3747409","3747410","3747411","3747412","3747413","3747414","3747415","3747416","3747417",
			"3747418","3747419","3747420","3747421","3747422","3747423","3747424","3747425","3747426","3747427","3747428","3747429",
			"3769440","3769441","3769443","3769444","3769445","3769446","3769447","3769448","3769449","3770402","3770403","3770404",
			"3770410","3770411","3770412","3770413","3770414","3770415","3770416","3770417","3770418","3770419");
	private static List<String> amexBinIssuedByIndusind = Arrays.asList("3771510","3771511","3771512","3771513","3771514","3771515",
			"3771516","3771517","3771518","3771519");
	private static List<String> amexBinIssuedByAmex = Arrays.asList("3769300","3769301","3769302","3769303","3769304","3769305",
			"3769306","3769307","3769308","3769309","3769310","3769311","3769312","3769313","3769314","3769315","3769316","3769317",
			"3769318","3769319","3769320","3769321","3769322","3769323","3769324","3769325","3769326","3769327","3769328","3769329",
			"3769330","3769331","3769332","3769333","3769334","3769335","3769336","3769337","3769338","3769339","3769340","3769341",
			"3769342","3769343","3769344","3769345","3769346","3769347","3769348","3769349","3769360","3769361","3769362","3769363",
			"3769364","3769365","3769366","3769367","3769368","3769369","3769370","3769371","3769372","3769373","3769374","3769375",
			"3769376","3769377","3769378","3769379","3769380","3769381","3769382","3769383","3769384","3769385","3769386","3769387",
			"3769388","3769389","3769390","3769391","3769392","3769393","3769394","3769395","3769396","3769397","3769398","3769399",
			"3798610","3798611","3798612","3798613","3798614","3798615","3798616","3798617","3798618","3798619","3798620","3798621",
			"3798622","3798623","3798624","3798625","3798626","3798627","3798628","3798629","3798630","3798631","3798632","3798633",
			"3798634","3798635","3798636","3798637","3798638","3798639","3798670","3798671","3798672","3798673","3798674","3798675",
			"3798676","3798677","3798678","3798679","3798680","3798681","3798682","3798683","3798684","3798685","3798686","3798687",
			"3798688","3798689","3798690","3798691","3798692","3798693","3798694","3798695","3798696","3798697","3798698","3798699",
			"3798700","3798701","3798702","3798703","3798704","3798705","3798706","3798707","3798708","3798709","3798710","3798711",
			"3798712","3798713","3798714","3798715","3798716","3798717","3798718","3798719","3798720","3798721","3798722","3798723",
			"3798724","3798725","3798726","3798727","3798728","3798729","3798760","3798761","3798762","3798763","3798764","3798765",
			"3798766","3798767","3798768","3798769","3798770","3798771","3798772","3798773","3798774","3798775","3798776","3798777",
			"3798778","3798779","3798780","3798781","3798782","3798783","3798784","3798785","3798786","3798787","3798788","3798789");


	@RequestMapping("get-saved-card-details")
	public  @ResponseBody
	String getSavedCardDetailsFromSession(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {

		String userId = CardsUtil.getUserId(request);
		logger.info("UserId received: " + userId);
		if(!YatraUtil.isNullOrEmpty(userId)) {
			SessionStorageCardInfo userCardInfo = cardsService.getCardFromSessionDB(userId);
			if(userCardInfo!=null) {
				ObjectWriter ow = new ObjectMapper().viewWriter(SessionStorageCardInfo.class);
				JSONObject resultJson = new JSONObject(ow.writeValueAsString(userCardInfo));
				String maskedCardDetails = CardsUtil.processCardInfoForSafeLogging(resultJson);
				logger.info("Card Details fetched from DB: " + maskedCardDetails);
				return resultJson.toString();
			}
		}
		logger.info("Either UserId was null or no saved card found against this userId");
		return "{}";

	}

	/**
	 * 
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "get-bin-details-v2", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<String> getNineBinDetails(Model model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		JSONObject resultJson = new JSONObject();
		String superPnr = null;
		
		try {
			//extracting values of request object.
			JSONObject reqJson = parseInputRequest(request);
			String cardNumber = reqJson.getString("cno");
			String product = reqJson.getString("product");
			String passThrough = reqJson.getString("passthrough");
			superPnr = reqJson.getString("superPnr");

			/* get request with masked sensitive info for safe db logging */
			String rawRequestToLog = prepareRequestForLogging(reqJson);

			paymentUIHelper.logIntoDB(rawRequestToLog, "REQUEST", superPnr, "GET_BIN_DETAILS_V2");

			//replaced the validateCardNumber with the validateCardNumberWithNineBin
			validateCardNumberNineBin(cardNumber);
			
			//taking bin as 9 digit number for further processing.
			String bin = cardNumber.substring(0, 9);
			
			bin = validatingForLastThreeDigit(bin);
			
			resultJson.put("cardType", mainCardIdentifier.getCardType(bin));

			/** populating Bin **/
			binDetailsService.populateBinDetails(resultJson, bin, product, passThrough, superPnr);

		} catch (Exception ex) {
			logger.error(GET_BIN_DETAILS_API + ":error: " + ex.getMessage() + ":superpnr:" + superPnr, ex);
			return new ResponseEntity<>(errorJson(ex), HttpStatus.BAD_REQUEST);
		}


		String responseValidated = StringEscapeUtils.escapeHtml4(resultJson.toString()).replaceAll(StringEscapeUtils.escapeHtml4("\""), "\"");
		paymentUIHelper.logIntoDB(responseValidated, "RESPONSE", superPnr, "GET_BIN_DETAILS_V2");
		
		
		return new ResponseEntity<>(responseValidated, HttpStatus.OK);
	}

	
	@RequestMapping(value = "get-bin-details", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<String> getBinDetails(Model model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		JSONObject resultJson = new JSONObject();
		String superPnr = null;
		try {
			JSONObject reqJson = parseInputRequest(request);
			String cardNumber = reqJson.getString("cno");
			String product = reqJson.getString("product");
			String passThrough = reqJson.getString("passthrough");
			superPnr = reqJson.getString("superPnr");


			/* get request with masked sensitive info for safe db logging */
			String rawRequestToLog = prepareRequestForLogging(reqJson);

			paymentUIHelper.logIntoDB(rawRequestToLog, "REQUEST", superPnr, "GET_BIN_DETAILS");
			//paymentUIHelper.logIntoDB(getQueryString(getParamaters(request)), "REQUEST", superPnr, "GET_BIN_DETAILS");

			validateCardNumber(cardNumber);

			String bin = cardNumber.substring(0, 6);

			resultJson.put("cardType", mainCardIdentifier.getCardType(bin));

			binDetailsService.populateBinDetails(resultJson, bin, product, passThrough, superPnr);

		} catch (Exception ex) {
			logger.error(GET_BIN_DETAILS_API + ":error: " + ex.getMessage() + ":superpnr:" + superPnr, ex);
			return new ResponseEntity<>(errorJson(ex), HttpStatus.BAD_REQUEST);
		}


		String responseValidated = StringEscapeUtils.escapeHtml4(resultJson.toString()).replaceAll(StringEscapeUtils.escapeHtml4("\""), "\"");
		paymentUIHelper.logIntoDB(responseValidated, "RESPONSE", superPnr, "GET_BIN_DETAILS");

		return new ResponseEntity<>(responseValidated, HttpStatus.OK);
	}
	
	@RequestMapping(value = "validate_emi_bank_details", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<String> validateEmiBankDetails(Model model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		JSONObject resultJson = new JSONObject();
		String superPnr = null;
		try {
			JSONObject reqJson = parseInputRequest(request);
			String cardNumber = reqJson.getString("cno");
			String selectedEmiBank = reqJson.getString("emiBank");
			superPnr = reqJson.getString("superPnr");

			paymentUIHelper.logIntoDB(getQueryString(getParamaters(request)), "REQUEST", superPnr, "VALIDATE_EMI_BANK_DETAILS");

			validateExtendedBinNumber(cardNumber);

			String bin = cardNumber.substring(0, 7);
			if(!cobrandedCardIssuingBanks.contains(selectedEmiBank) || isCardNonAmex(bin)) {
				resultJson.put("is_successful", "true");
				resultJson.put("message", "");
				paymentUIHelper.logIntoDB(resultJson.toString(), "RESPONSE", superPnr, "VALIDATE_EMI_BANK_DETAILS");
				return new ResponseEntity<>(resultJson.toString(), HttpStatus.OK);
			}
			if(cobrandedEmiBanksBinMapping.isEmpty()) {
				populateCobrandedEmiBanksBinMapping();
			}
			String binEmiBank = cobrandedEmiBanksBinMapping.get(bin);
			if(selectedEmiBank.equalsIgnoreCase(binEmiBank)) {
				resultJson.put("is_successful", "true");
				resultJson.put("message", "");
			} else {
				resultJson.put("is_successful", "false");
				if(selectedEmiBank.equalsIgnoreCase("amex"))
					resultJson.put("message", "EMI is not supported for this card");
				else resultJson.put("message", "Card not issued by selected bank. Please select correct EMI bank");
			}
		} catch (Exception ex) {
			logger.error("VALIDATE_EMI_BANK_DETAILS_API" + ":error: " + ex.getMessage() + ":superpnr:" + superPnr, ex);
			return new ResponseEntity<>(errorJson(ex), HttpStatus.BAD_REQUEST);
		}

		paymentUIHelper.logIntoDB(resultJson.toString(), "RESPONSE", superPnr, "VALIDATE_EMI_BANK_DETAILS");
		return new ResponseEntity<>(resultJson.toString(), HttpStatus.OK);
	}

	private boolean isCardNonAmex(String bin) {
		if(bin.startsWith("34") || bin.startsWith("37"))
			return false;
		else return true;
	}


	private void populateCobrandedEmiBanksBinMapping() {
		cobrandedEmiBanksBinMapping.clear();
		for (String bin : amexBinIssuedByIcici) {
			cobrandedEmiBanksBinMapping.put(bin, "icici");
		}
		for (String bin : amexBinIssuedByIndusind) {
			cobrandedEmiBanksBinMapping.put(bin, "indusind");
		}
		for (String bin : amexBinIssuedByAmex) {
			cobrandedEmiBanksBinMapping.put(bin, "amex");
		}
	}


	private void validateCardNumber(String cardNumber) {
		if (YatraUtil.isNullOrEmpty(cardNumber) || cardNumber.length() < 6) {
			throw new PayswiftException(FailureCode.GET_BIN_DETAILS_API_INVALID_CARD_NUMBER.getCode(), "Invalid card number");
		}
	}
	private void validateCardNumberNineBin(String cardNumber) {
		if(YatraUtil.isNullOrEmpty(cardNumber) || cardNumber.length() < 9) {
			throw new PayswiftException(FailureCode.GET_BIN_DETAILS_API_INVALID_CARD_NUMBER.getCode(),"Invalid card number");
			
		}
	}
	private String validatingForLastThreeDigit(String bin) {
		if(bin.endsWith("xxx")) {	
			bin = bin.replace("xxx", "000");
		}	
		return bin;
	}
	private void validateExtendedBinNumber(String cardNumber) {
		if (YatraUtil.isNullOrEmpty(cardNumber) || cardNumber.length() < 7) {
			throw new PayswiftException(FailureCode.GET_BIN_DETAILS_API_INVALID_CARD_NUMBER.getCode(), "Invalid card number");
		}
	}

	private JSONObject parseInputRequest(HttpServletRequest req) throws Exception {
		try{
			//
			/*if(!YatraUtil.isNullOrEmpty(req.getParameter("cno"))) {
				JSONObject requestJson = new JSONObject();
				requestJson.put("cno", req.getParameter("cno"));
				requestJson.put("product", req.getParameter("product"));
				requestJson.put("passthrough", req.getParameter("passthrough"));
				requestJson.put("superPnr", req.getParameter("superPnr"));
				logger.debug("Successfully read params from query string");
				return requestJson;
			}
			else {*/
				logger.debug("Reading params from request body");
				JSONObject requestJson ;
				StringBuilder buffer = new StringBuilder();
				BufferedReader reader = req.getReader();
				String line;
				while ((line = reader.readLine()) != null) {
					buffer.append(line);
				}
				String postBody = buffer.toString();

				requestJson = new JSONObject(postBody);
				return requestJson;
			//}
		}
		catch(Exception ex){
			logger.debug("Getting exception while parsing request body", ex);
				throw ex;
		}
	}

	@RequestMapping("getSavedGdsCardForUser")
	public  @ResponseBody
	String getSavedGdsCardForUser(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
		String userId = CardsUtil.getUserId(request);
		logger.info("UserId received: " + userId);
		try {
			String cardId = null;
			if(!YatraUtil.isNullOrEmpty(userId)) {
				UserCardsDisplayResponse userCardDisplayResponse = expressCardsServiceClient.getCardsForDisplayOfGuestCorporateUser(1012l, Long.valueOf(userId));
				if(userCardDisplayResponse != null && userCardDisplayResponse.getUserCards()!= null)
				{
					for(UserCardInfo userCardInfo : userCardDisplayResponse.getUserCards()) {
						if(userCardInfo.getCardProperties().containsKey(ExpressCardsPropertiesConstants.IS_GDS_CARD) 
								&& "true".equalsIgnoreCase(userCardInfo.getCardProperties().get(ExpressCardsPropertiesConstants.IS_GDS_CARD))) 
							cardId = userCardInfo.getCardId();
					}
				}
				if(YatraUtil.isNullOrEmpty(cardId)) {
					logger.info("No saved card found against this userId");
					return "{}";
				}
				UserCardInfoResponse userCardInfoResponse = expressCardsServiceClient.getCardForPaymentForGuestCorporateUser("PaymentPortal",Long.valueOf(userId), cardId, getPrivateKey());
				if(userCardInfoResponse!=null && "success".equalsIgnoreCase(userCardInfoResponse.getResponse().getStatus())) {
					JSONObject responseJson = new JSONObject();
					responseJson.put(PaymentUIUtil.USER_ID, userId);
					responseJson.put(PaymentUIUtil.CARD_HOLDER_NAME, userCardInfoResponse.getUserCardInfo().getFirstName());
					responseJson.put(PaymentUIUtil.CARD_TYPE, getCardType(userCardInfoResponse.getUserCardInfo().getCardType()));
					responseJson.put(PaymentUIUtil.CARD_CODE, getCardCodeForCardBrand(userCardInfoResponse.getUserCardInfo().getCardBrand()));
					responseJson.put(PaymentUIUtil.CARD_EXP_YEAR, RSAEncryptionUtil.rsaEncrypt(userCardInfoResponse.getUserCardInfo().getExpiryYear(), getGDSAppPublicKey()));
					responseJson.put(PaymentUIUtil.CARD_EXP_MONTH, RSAEncryptionUtil.rsaEncrypt(userCardInfoResponse.getUserCardInfo().getExpiryMonth(), getGDSAppPublicKey()));
					responseJson.put(PaymentUIUtil.CARD_NO, RSAEncryptionUtil.rsaEncrypt(getCardNumber(userCardInfoResponse.getUserCardInfo()), getGDSAppPublicKey()));
					
					String maskedCardDetails = CardsUtil.processCardInfoForSafeLogging(responseJson);
					logger.info("Card Details fetched from QB for userId " + userId + " is : " + maskedCardDetails);
					return responseJson.toString();
				}
			}
			logger.info("Either UserId was null or no saved card found against this userId");
			return "{}";		
		} catch(Exception e) {
			logger.error("Exception came up while trying to fetch GDS-saved card of userId : " + userId, e);
			return "{}";
		}
	}
	
	private String getCardType(String cardType) {
		if(!YatraUtil.isNullOrEmpty(cardType) && cardType.toLowerCase().contains("debit"))
			return "DEBIT";
		else return "CREDIT";			
	}

	
	private String getCardCodeForCardBrand(String cardBrand) {
		if("MASTER".equalsIgnoreCase(cardBrand))
			return "MA";
		if("DINERS".equalsIgnoreCase(cardBrand) || "DINNERS".equalsIgnoreCase(cardBrand))
			return "DI";
		if("AMEX".equalsIgnoreCase(cardBrand))
			return "AX";
		else return "VI";
	}

	private String getCardNumber(UserCardInfo userCard) {
		String cardNumber = userCard.getCardNumber1() + userCard.getCardNumber2() + userCard.getCardNumber3();
		if (!StringUtils.isBlank(userCard.getCardNumber4())) // null or blank("")
			cardNumber +=  userCard.getCardNumber4();
		return cardNumber;
	}

	private PrivateKey getPrivateKey() {
		PrivateKey privateKey = null;
		try {
			privateKey = RSAEncryptionUtil.readPrivateKeyFromFile("/PaySwiftPrivate.key");
		} catch (Exception exception) {
			logger.error("Private key could not be found..."+exception);
		}
		return privateKey;
	}
	
	private PublicKey getGDSAppPublicKey() {
		PublicKey publicKey = null;
		try {
			publicKey = RSAEncryptionUtil.readPublicKeyFromFile("/amadeusPublic.key");
		} catch (Exception exception) {
			logger.error("Public key could not be found..."+exception);
		}
		return publicKey;
	}

	@RequestMapping("deleteCardWithUserId")
	public @ResponseBody 
	String deletedCardWithUserId(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
		try {
			JSONObject reqJson = parseInputRequest(request);
			String cardId = reqJson.getString("cardId");
			String userId = reqJson.getString("userId");
			ResponseStatus responseStatus = expressCardsServiceClient.removeCardUsingUserId(1012l, userId, cardId);
			return responseStatus.getStatus();
		} catch(Exception e) {
			logger.error("Error while parsing request body");
			return "failure";
		}
	}

	@RequestMapping(value = "deleteTokenisedCardWithUserId", method = RequestMethod.POST)
	public @ResponseBody
	String deleteTokenisedCardWithUserId(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
		String res = "";
		try {
			JSONObject reqJson = parseInputRequest(request);
			String cardId = reqJson.getString("cardId");
			String userId = reqJson.getString("userId");
			paymentUIHelper.logIntoDB(reqJson.toString(), "REQUEST", "--", DELETE_TOKENIZED_CARD);


			if(StringUtils.isBlank(cardId) || StringUtils.isBlank(userId)){
				res = "CardId and UserId can not be empty";
			}else {
				com.yatra.tokenized.cards.v3.beans.ResponseStatus responseStatus = null;

				PaytmResponseBean paytmResponseBean = null;
				String paytmCardId = cardsService.getPaytmReferenceId(tokenizedCardsServiceClient.getCardsForDisplayOfGuestUser(1012l, Long.valueOf(userId)), cardId);
				if(StringUtils.isNotBlank(paytmCardId)) {
					paytmResponseBean = cardsService.deleteTokenisedCard(paytmCardId, userId);
				}

				if ((paytmResponseBean != null) && (paytmResponseBean.getResultStatus().equalsIgnoreCase("SUCCESS")))
					responseStatus = tokenizedCardsServiceClient.removeCardUsingUserId(1012l, userId, cardId);


				if((responseStatus != null) && StringUtils.isNotBlank(responseStatus.getStatus())){
					if(responseStatus.getStatus().equalsIgnoreCase("SUCCESS")){
						res = "Card with CardId " + cardId + " Deleted Successfully";
					}else if(responseStatus.getStatus().equalsIgnoreCase("FAILURE")){
						res = "Card with CardId " + cardId + " doesn't exists! Please check cardID";
					}else{
						res = "Issue in deleting card! Please try again later";
					}
				}else{
					res = "Issue in deleting card! Please try again later";
				}

			}

		} catch(Exception e) {
			logger.error("Error while parsing request body");
			res =  "failure";
		}

		paymentUIHelper.logIntoDB(res, "RESPONSE", "--", DELETE_TOKENIZED_CARD);
		return res;
	}
	private String prepareRequestForLogging(JSONObject jsonObject){

		HashMap<String,String> fieldToMaskMap = new HashMap<>();
		fieldToMaskMap.put("cno", LoggingUtil.REGEX_PRESERVE_NONE);
		return LoggingUtil.getMaskedJsonForLogging(jsonObject,fieldToMaskMap);

	}

}
