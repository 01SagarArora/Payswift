package com.yatra.payment.ui.controller;

import com.yatra.express.crp.cards.v3.beans.CorporateCardInfo;
import com.yatra.express.crp.cards.v3.beans.CorporateCardsDisplayResponse;
import com.yatra.payment.cache.service.RemoteCacheManager;
import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.core.service.EmailService;
import com.yatra.payment.core.service.PaymentUtil;
import com.yatra.payment.gv.GiftVoucherService;
import com.yatra.payment.payswift.FutureTasks.B2BAgentProfileData;
import com.yatra.payment.payswift.FutureTasks.CorporateBinsConfigServiceData;
import com.yatra.payment.payswift.FutureTasks.CorporateConfigServiceData;
import com.yatra.payment.payswift.FutureTasks.DebitCardPinBankVsPgData;
import com.yatra.payment.payswift.beans.*;
import com.yatra.payment.qb.corporate.constant.QBConstant;
import com.yatra.payment.qb.corporate.helper.QBHelper;
import com.yatra.payment.ui.beans.CorporateLoginResponseBean;
import com.yatra.payment.ui.builder.PaymentDivBuilder;
import com.yatra.payment.ui.cache.builder.NoCostEMICacheBuilder;
import com.yatra.payment.ui.dao.CompletePaymentInfoDAO;
import com.yatra.payment.ui.dao.YatraPropertiesDAO;
import com.yatra.payment.ui.dao.bean.DcPinBankConfig;
import com.yatra.payment.ui.dao.impl.PayswiftStagesDAOImpl;
import com.yatra.payment.ui.display.beans.*;
import com.yatra.payment.ui.enums.ClientType;
import com.yatra.payment.ui.enums.Status;
import com.yatra.payment.ui.exceptions.PayswiftException;
import com.yatra.payment.ui.helper.PaymentUIHelper;
import com.yatra.payment.ui.helper.QuickBookUIHelper;
import com.yatra.payment.ui.payswift.service.CashPoolPayOpService;
import com.yatra.payment.ui.payswift.service.LobDetailsService;
import com.yatra.payment.ui.payswift.service.LoggingProcessor;
import com.yatra.payment.ui.payswift.service.PaymentInfoService;
import com.yatra.payment.ui.payswift.service.ValidatorService;
import com.yatra.payment.ui.service.*;
import com.yatra.payment.ui.service.impl.CorporateBinsConfigServiceImpl;
import com.yatra.payment.ui.service.impl.CorporateConfigurationServiceImpl;
import com.yatra.payment.ui.service.impl.DcPinBankConfigService;
import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.payment.ui.util.QuickBookUIUtil;
import com.yatra.platform.commons.property.PropertyManager;
import com.yatra.platform.commons.role.CallContextKeeper;
import com.yatra.platform.ui.FrescoInteractionService;
import com.yatra.platform.util.YatraUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONUtils;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.FutureTask;
import java.util.Enumeration;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.OutputStream;
import java.io.ByteArrayOutputStream;


import java.util.List;


import static com.yatra.payment.ui.enums.FailureCode.*;
import static com.yatra.payment.ui.util.PaymentUIUtil.DEFAULT_YATRA_MERCHANT;
import static com.yatra.payment.ui.util.PaymentUIUtil.LOB_PAGE_LOAD_REQ;
import static com.yatra.payment.ui.util.PaymentUIUtil.byPassUIChecksForWhitelistedEmail;
import static com.yatra.payment.ui.util.PaymentUIUtil.getViewName;
import static com.yatra.payment.ui.enums.FailureCode.FETCH_AGENT_PAYMENT_OPTIONS_INVALID_RESPONSE_CODE;
import static com.yatra.payment.ui.util.PaymentUIUtil.*;

import com.yatra.payment.ui.util.ZipUtils;

import static java.util.Arrays.asList;


@Controller
public class PaymentUIController {

	private static Logger logger = Logger.getLogger(PaymentUIController.class);
	@Autowired private InternationalCardService internationalCardService;
	@Autowired private QuickBookUIService quickBookUIService;
	@Autowired private QuickBookExpressUIService quickBookExpressUIService;
	@Autowired private PropertyManager yatraPropertyReader;
	@Autowired private RewardBinsProvider rewardBinsProvider;
	@Autowired private UICacheManager<PaymentDiv> paymentDivCache;
	@Autowired private AgentProfileService agentProfileService;
	@Autowired private CorporateCardsUIService corporateCardsUIService;
	@Autowired private LobDetailsService lobDetailsService;
	@Autowired private UiEmiBank uiEmiBank;
	@Autowired FrescoInteractionService frescoInteractionService;
	@Autowired ThreadPoolTaskExecutor threadPoolTaskExecutor;
	@Autowired ValidatorService validatorService;
	@Autowired private PaymentDivBuilder paymentDivBuilder;
	@Autowired private PaymentDivUpdater paymentDivUpdater;
	@Autowired private RemoteCacheManager remoteCacheManager;
	@Autowired private LoggingProcessor loggingProcessor;
	@Autowired private UICacheManager<ProductServiceUrlBean> productServiceUrlCache;
	@Autowired private UICacheManager<UIMessage> payopMessageMappingCache;
	@Autowired private UICacheManager<NoCostEmiProduct> noCostEmiBanksCache;
	@Autowired private PaymentInfoService paymentInfoService;
	@Autowired private DcPinBankConfigService dcPinBankConfigService;
	@Autowired private PayswiftStagesDAOImpl paymentDAO;
	@Autowired private PaymentUIHelper paymentUIHelper;
	@Autowired private QBHelper corpQBHelper;
	@Autowired private PayNowController payNowController;
	@Autowired private GiftVoucherService voucherService;
	@Autowired private CorporateConfigurationServiceImpl corpConfigService;
	@Autowired private CorporateBinsConfigServiceImpl corpBinConfigService;
	@Autowired private QuickBookUIHelper quickbookUIHelper;
	@Autowired private NoCostEMICacheBuilder noCostCacheBuilder;
	@Autowired private CashPoolPayOpService cashPoolPayOpService;
	private static List<String> b2bArrayList = new ArrayList<String>();
	private static List<String> crpProductArrayList = new ArrayList<>();
	private static List<String> b2bProductArrayList = new ArrayList<>();
	private static List<String> paymentProcessTypeDisabledProductsArrayList = new ArrayList<>();
	@Autowired private YatraPropertiesDAO yatraPropertiesDAO;
	@Autowired private EmailService emailService;
	@Autowired private CompletePaymentInfoDAO completePaymentInfoDAO;
	private static List<String> corpDisableUpiArrayList = new ArrayList<>();

	private final ObjectMapper objectMapper = new ObjectMapper();

	static String blankJson = "{}";

	@PostConstruct
	public void setB2bProducts(){
		String b2bProducts = yatraPropertyReader.getProperty("b2b.channel.products");
		if(StringUtils.isNotBlank(b2bProducts)){
			b2bArrayList = asList(b2bProducts.split(","));
		}
	}

	@PostConstruct
	public void setPaymentProcessTypeDisabledProducts(){
		String paymentProcessTypeDisabledProducts = yatraPropertyReader.getProperty("paymentProcessType.disabled.products");
		if(StringUtils.isNotBlank(paymentProcessTypeDisabledProducts)){
			paymentProcessTypeDisabledProductsArrayList = asList(paymentProcessTypeDisabledProducts.split(","));
		}
	}

	@PostConstruct
	public void setCrpProducts(){
		String crpProducts = yatraPropertyReader.getProperty("crp.products");
		if(StringUtils.isNotBlank(crpProducts)){
			crpProductArrayList = asList(crpProducts.split(","));
		}
	}

	@PostConstruct
	public void setB2BDesktopProducts(){
		String b2bProducts = yatraPropertyReader.getProperty("b2b.products");
		if(StringUtils.isNotBlank(b2bProducts)){
			b2bProductArrayList = asList(b2bProducts.split(","));
		}
	}

	@PostConstruct
	public void setClientsForHidingUpi(){
		String clientsForUpiHandling = yatraPropertiesDAO.getYatraPropValue("force.disable.upi.client.list");
		if(StringUtils.isNotBlank(clientsForUpiHandling)){
			corpDisableUpiArrayList = asList(clientsForUpiHandling.split(","));
		}
	}


	@RequestMapping(value = "payment")
	public ModelAndView renderPaymentPage(HttpServletRequest req,HttpServletResponse res,ModelAndView model) {
		PaymentRequest paymentRequest = null;
		try {
			Long requestTime = System.currentTimeMillis();
			paymentRequest = new PaymentRequest(req);
			logger.debug("Inside /payment for SuperPnr :" + paymentRequest.getSuperPnr() + " and product Code " + paymentRequest.getProductCode() + "at :" + requestTime);

			logLOBPageLoadRequest(paymentRequest);

			validatorService.validatePageLoadRequest(paymentRequest, b2bArrayList);

			fetchDebitCardPinBankVsPgData(paymentRequest);

			PaymentPageResponse pageResponse = paymentPageResponse(paymentRequest);

			if(shouldPaySwiftPageBeBypassedForEcashGVFullWalletCase(paymentRequest, pageResponse)) {
				return byPassPaySwiftPage(paymentRequest, pageResponse, "byPassForEcashGV");
			}
			if("true".equalsIgnoreCase(paymentRequest.getSkipUI()) && !YatraUtil.isNullOrEmpty(new LobDetailResponse (pageResponse.getLobDetailsJson()).getIxigoMoney())) {
				return byPassPaySwiftPage(paymentRequest, pageResponse, "byPassForIxiGo");
			}

			paymentUIHelper.skipOtpFlagForB2C(paymentRequest);

			checkAndStoreUserIdCookieInResponse(req, res, pageResponse);

			//Updating Payment Div for Cash Pool Payment Option
			//	cashPoolPayOpService.updateDiv(pageResponse,paymentRequest);

			ModelAndView paymentPageModel = populateModel(pageResponse, paymentRequest);

			logResponseTimeInDB(paymentRequest, requestTime);
			return paymentPageModel;
		} catch (Exception ex) {
			String webEvent="RENDER_PAYMENT_PAGE_ERROR";
			paymentDAO.insertIntoWebEvents(paymentRequest.getSuperPnr(), webEvent, ex.getMessage(),
					paymentRequest.getMerchantCode(), paymentRequest.getProductCode());
		/*	if(StringUtils.isNotBlank(paymentRequest.getSuperPnr())) {

				emailService.sendEmailWithCodeNew("notification.paymentsdev@yatra.com", "notification.paymentsdev@yatra.com",
						"Alert!!! PAYSWIFT_EXCEPTION for : renderPaymentPage having super pnr : " + paymentRequest.getSuperPnr(),
						" : " + PaymentUtil.getLocalIPAdress() + " having exception "
								+ ExceptionUtils.getFullStackTrace(ex));
			} */
			
			logger.error("Render payment page api failed for product: " + paymentRequest.getProductCode() + ", Error: " + ex.getMessage() + ", superpnr: " + paymentRequest.getSuperPnr() + ExceptionUtils.getFullStackTrace(ex), ex);
			return populateErrorModel(paymentRequest);
		}
	}

	private ModelAndView byPassPaySwiftPage(PaymentRequest paymentRequest, PaymentPageResponse pageResponse, String byPassFlow) throws Exception {
		logger.info("Bypassing payswift page for ecashGV product - only ecash voucher case.");
		ModelAndView modelAndView = new ModelAndView();

		PayNowRequest payNowRequest = payNowRequest(paymentRequest, pageResponse, byPassFlow);
		PayNowResponse payNowResponse = payNowController.payNowResponse(payNowRequest);

		modelAndView.addObject("confirmationUrl", payNowResponse.getRurl());
		modelAndView.addObject("fieldMap", payNowResponse.getRedirectMap());
		modelAndView.setViewName("desktop/confirmationRedirect");

		return modelAndView;
	}

	private PayNowRequest payNowRequest(PaymentRequest paymentRequest, PaymentPageResponse pageResponse, String byPassFlow) {
		Map<String, String> payNowDataMap = new HashMap<>();

		LobDetailResponse bookingDetailsJson = new LobDetailResponse(pageResponse.getLobDetailsJson());

		payNowDataMap.put("product", paymentRequest.getProductCode());
		payNowDataMap.put("paymentMode", "FULL");
		payNowDataMap.put("client", paymentRequest.getClient().toString());
		payNowDataMap.put("superPnr", paymentRequest.getSuperPnr());
		payNowDataMap.put("ttid", paymentRequest.getTtid());
		payNowDataMap.put("ssoToken", paymentRequest.getSsoToken());
		payNowDataMap.put("uuid", paymentRequest.getUuid());
		payNowDataMap.put("amountDisplayed", "0");

		Map<String, String> paymentOptionParamMap = new HashMap<>();

		if(byPassFlow.equalsIgnoreCase("byPassForEcashGV")) {
			payNowDataMap.put("amountToRedeem", (String) pageResponse.getAdditionalParams().get(TOTAL_AMOUNT));
			payNowDataMap.put("email", (String) pageResponse.getAdditionalParams().get(EMAIL));
			payNowDataMap.put("paymentOptionParameters", payOpParamsString(paymentRequest, byPassFlow));
			paymentOptionParamMap.put("merchant", paymentRequest.getMerchantCode());
			paymentOptionParamMap.put("product", paymentRequest.getProductCode());
			paymentOptionParamMap.put("payop", "ew");
		}
		else {
			payNowDataMap.put("ixigoMoney", bookingDetailsJson.getIxigoMoney());
			payNowDataMap.put("email", (String) pageResponse.getAdditionalParams().get(EMAIL));
			payNowDataMap.put("paymentOptionParameters", payOpParamsString(paymentRequest, byPassFlow));
			paymentOptionParamMap.put("merchant", paymentRequest.getMerchantCode());
			paymentOptionParamMap.put("product", paymentRequest.getProductCode());
			paymentOptionParamMap.put("payop", PaymentUIUtil.PAYMENT_OPTION_IXIGO_MONEY);
		}

		PayNowRequest payNowRequest = new PayNowRequest(payNowDataMap);
		payNowRequest.setPaymentOptionParamMap(paymentOptionParamMap);
		return payNowRequest;
	}

	private String payOpParamsString(PaymentRequest paymentRequest, String byPassFlow) {
		if(byPassFlow.equalsIgnoreCase("byPassForEcashGV"))
			return "merchant=" +
					paymentRequest.getMerchantCode() +
					"|product=" +
					paymentRequest.getProductCode() +
					"|payop=ew|";
		else return "merchant=" +
				paymentRequest.getMerchantCode() +
				"|product=" +
				paymentRequest.getProductCode() +
				"|payop=ixigoMoney|";
	}

	private boolean shouldPaySwiftPageBeBypassedForEcashGVFullWalletCase(PaymentRequest paymentRequest, PaymentPageResponse pageResponse) {
		Map<String, Object> pageResponseAdditionalParams = pageResponse.getAdditionalParams();
		if(PRODUCT_ECASH_GV.equalsIgnoreCase(paymentRequest.getProductCode()) &&
				pageResponseAdditionalParams.containsKey(ONLY_ECASH_GV)) {
			return (boolean) pageResponseAdditionalParams.get(ONLY_ECASH_GV);
		}
		return false;
	}

	private PaymentPageResponse paymentPageResponse(PaymentRequest paymentRequest) {
		LobDetailResponse lobDetailResponse = lobDetailsService.fetchItineraryDetailsFromLob(new LobDetailRequest(paymentRequest));

		if (b2bArrayList.contains(paymentRequest.getProductCode()) && DEFAULT_YATRA_MERCHANT.equalsIgnoreCase(paymentRequest.getMerchantCode())) {
			return getB2BPaymentPageResponse(paymentRequest, lobDetailResponse);
		}
		return getPaymentPageResponse(paymentRequest, lobDetailResponse);
	}

	private void logLOBPageLoadRequest(PaymentRequest paymentRequest) {
		loggingProcessor.logIntoPayswiftStages(paymentRequest.getSuperPnr(), LOB_PAGE_LOAD_REQ, paymentRequest.getQueryString(), paymentRequest.getProductCode());
	}

	private void checkAndStoreUserIdCookieInResponse(HttpServletRequest req, HttpServletResponse res, PaymentPageResponse pageResponse) {

		/*List<Cookie> cookies = Arrays.asList(req.getCookies());
		Cookie userIdCookie= cookies.parallelStream().filter(cookie -> PaymentUIUtil.UNIQUE_USER_ID_COOKIE.equalsIgnoreCase(cookie.getName())).findAny().orElse(null);
		Need to comment as Stream do not deploy in Spting older version dependencies , will take up migration some time . */
		LobDetailResponse bookingDetailsJson = new LobDetailResponse(pageResponse.getLobDetailsJson());
		if(bookingDetailsJson.saveGuestCard()){
			Cookie userIdCookie = PaymentUIUtil.getPerticularCookie(req, PaymentUIUtil.UNIQUE_USER_ID);
			if(userIdCookie == null){
				String userId = PaymentUIUtil.getUniqueRandomNumberOfFixedLength(9);
				userIdCookie = new Cookie(PaymentUIUtil.UNIQUE_USER_ID, userId);
				logger.debug("Saving "+ PaymentUIUtil.UNIQUE_USER_ID + " cookie , with value "+ userId+" into browser as cookie not found . ");
				userIdCookie.setMaxAge(60*60*24*7);
				userIdCookie.setSecure(true);
				res.addCookie(userIdCookie);
			}
		}
	}

	@RequestMapping(value ="getPaymentInfo", method=RequestMethod.POST)
	public @ResponseBody String getPaymentInfo(HttpServletRequest httpRequest){
		logger.debug("Entering the getPaymentInfo method");
		PaymentInfoBean paymentInfoBean = new PaymentInfoBean(httpRequest);

		logger.debug("Request:: Client:"+paymentInfoBean.getClient()+"superPnr"+paymentInfoBean.getSuperPnr()+"ssoToken"+paymentInfoBean.getSsoToken()
				+"productCode"+paymentInfoBean.getProductCode()+"merchantCode"+paymentInfoBean.getMerchantCode()+"uuid"+paymentInfoBean.getUuid()+"version"+paymentInfoBean.getVersionNo()+"userEmail"+paymentInfoBean.getUserEmail()+"appVersion"+paymentInfoBean.getAppVersion());
		paymentInfoService.logIntoDB(paymentInfoBean.toString(),"REQUEST",paymentInfoBean.getSuperPnr());

		logger.debug("getPaymentInfo Request received for super pnr "+paymentInfoBean.getSuperPnr()+" and product Code "+paymentInfoBean.getProductCode());
		String response = paymentInfoService.getPaymentInfo(paymentInfoBean);
		paymentInfoService.logIntoDB(ZipUtils.getZipped(response),"RESPONSE",paymentInfoBean.getSuperPnr());
		return response;
	}

	private ModelAndView populateErrorModel(PaymentRequest paymentRequest) {
		ClientType client = paymentRequest.getClient();
		ModelAndView errorModel = new ModelAndView();
		String viewName;
		switch (client) {
			case DESKTOP:
				if("gemdom".equalsIgnoreCase(paymentRequest.getProductCode()) || "gemint".equalsIgnoreCase(paymentRequest.getProductCode()))
					viewName = "desktop/payment-gem-error";
				else viewName =  "desktop/payment-error";
				break;
			case WAP:
				viewName =  "wap/error";
				break;
			default:
				viewName =  null;
		}
		errorModel.setViewName(viewName);
		errorModel.addObject("referenceNumber", paymentRequest.getSuperPnr());
		return errorModel;
	}

	private PaymentPageResponse getPaymentPageResponse(PaymentRequest paymentRequest, LobDetailResponse lobDetailResponse){
		PaymentPageResponse paymentPageResponse = new PaymentPageResponse();
		paymentPageResponse.setLobDetailsJson(lobDetailResponse.getRawResponse());
		paymentPageResponse.setCorpAgentProfileType(lobDetailResponse.getAgentProfileType());
		paymentPageResponse.addParam(ONLY_ECASH_GV, lobDetailResponse.isOnlyEcashGV());
		paymentPageResponse.addParam(TOTAL_AMOUNT, lobDetailResponse.getTotalAmount());
		paymentPageResponse.addParam(EMAIL, lobDetailResponse.getUserEmail());

		String key = PaymentUIUtil.getPaymentDivKey(paymentRequest.getMerchantCode(), lobDetailResponse.getProductCodeForUI());
		PaymentDiv paymentDiv = paymentDivCache.get(key);
		logger.info("Payment DIV received from cache : "+paymentDiv == null ?  "false":"true");
		if(!YatraUtil.isNullOrEmpty(lobDetailResponse.getPayLaterFlow()) && "true".equalsIgnoreCase(lobDetailResponse.getPayLaterFlow())) {
			List<PaymentOption> newPaymentOptions = new ArrayList<PaymentOption>();
			for (PaymentOption po : paymentDiv.getPaymentOptions()) {
				if ("cc".equalsIgnoreCase(po.getCode())) {
					newPaymentOptions.add(po);
				}
			}
			PaymentDiv updatedPaymentDiv = new PaymentDiv();
			updatedPaymentDiv.setPaymentOptions(newPaymentOptions);
			paymentPageResponse.setPaymentDiv(updatedPaymentDiv);
			return paymentPageResponse;
		}
		//remove GV payop from SAPG page
		if("STAND_ALON".equalsIgnoreCase(lobDetailResponse.getProductCode())) {
			List<PaymentOption> newPaymentOptions = new ArrayList<PaymentOption>();
			for (PaymentOption po : paymentDiv.getPaymentOptions()) {
				if (!"gv".equalsIgnoreCase(po.getCode())) {
					newPaymentOptions.add(po);
				}
			}
			PaymentDiv updatedPaymentDiv = new PaymentDiv();
			updatedPaymentDiv.setPaymentOptions(newPaymentOptions);
			paymentPageResponse.setPaymentDiv(updatedPaymentDiv);
			return paymentPageResponse;
		}
		paymentPageResponse.setPaymentDiv(paymentDiv);
		return paymentPageResponse;
	}

	public void fetchDebitCardPinBankVsPgData(PaymentRequest request){
		FutureTask<List<DcPinBankConfig>> futureTaskForDcPinBankVsPgData = new FutureTask<List<DcPinBankConfig>>(new DebitCardPinBankVsPgData(dcPinBankConfigService));
		threadPoolTaskExecutor.submit(futureTaskForDcPinBankVsPgData);
		request.addFutureTask(PaymentUIUtil.DC_PIN_BANK_VS_PG_DETAIL_FUTURE_TASK, futureTaskForDcPinBankVsPgData);
	}

	private PaymentPageResponse getB2BPaymentPageResponse(PaymentRequest paymentRequest, LobDetailResponse lobDetailResponse) {
		logger.debug("Inside B2B/CRP payment Page Response ");
		boolean isCrpProduct = isCrpProduct(paymentRequest.getProductCode());
		fetchB2BAgentSpecificData(paymentRequest, lobDetailResponse.getCorporateTripId());
		fetchConfigData(paymentRequest, isCrpProduct, lobDetailResponse);

		PaymentPageResponse paymentPageResponse = getPaymentPageResponse(paymentRequest, lobDetailResponse);

		JSONObject agentPaymentOptionJSON = lobDetailResponse.getAgentPaymentOptions();
		if(agentPaymentOptionJSON == null || agentPaymentOptionJSON.isEmpty()) {
			throw new PayswiftException(FETCH_AGENT_PAYMENT_OPTIONS_EMPTY.getCode(), "agentPaymentOptions is null or empty");
		}
		String bookingType = paymentUIHelper.getBookingType(paymentPageResponse.getCorpAgentProfileType());
		String entity = lobDetailResponse.getEntity();
		String product = lobDetailResponse.getProductCode();
		boolean passthrough = lobDetailResponse.isPassThrough();
		PaymentDiv paymentDiv = setAgentSpecificPaymentOptions(paymentPageResponse, paymentRequest, agentPaymentOptionJSON, bookingType, lobDetailResponse.getCrpInfoNode(), isCrpProduct, entity, product, passthrough);
		paymentPageResponse.setPaymentDiv(paymentDiv);

		paymentRequest.setSkipOtpFlagMap(paymentUIHelper.getSkipOtpFlagMap(agentPaymentOptionJSON, bookingType, isCrpProduct, entity, product, passthrough));
		return paymentPageResponse;
	}

	public void fetchB2BAgentSpecificData(PaymentRequest request, String ctripId){
		String ssoToken = request.getSsoToken();
		FutureTask<JSONObject> futureTaskForCreditPoolData = new FutureTask<JSONObject>(new B2BAgentProfileData(agentProfileService, ssoToken, true,request.getProductCode(), ctripId));
		threadPoolTaskExecutor.submit(futureTaskForCreditPoolData);
		request.addFutureTask(PaymentUIUtil.CREDIT_POOL_FUTURE_TASK, futureTaskForCreditPoolData);
	}

	public void fetchConfigData(PaymentRequest request, boolean isCrpProduct, LobDetailResponse lobDetailResponse){
		if(isCrpProduct) {
			fetchCorpBinConfigData(request, lobDetailResponse.getBookingType(), lobDetailResponse.getProductForFalcon());
			fetchCorpConfigData(request);
		}
	}

	public void fetchCorpConfigData(PaymentRequest request){
		FutureTask<JSONObject> futureTaskForCorpConfigData = new FutureTask<JSONObject>(new CorporateConfigServiceData(request.getSsoToken(), request.getProductCode(), corpConfigService));
		threadPoolTaskExecutor.submit(futureTaskForCorpConfigData);
		request.addFutureTask(PaymentUIUtil.CORP_CONFIG_FUTURE_TASK, futureTaskForCorpConfigData);
	}

	public void fetchCorpBinConfigData(PaymentRequest request, String bookingType, String productForFalcon){
		FutureTask<JSONObject> futureTaskForCorpBinConfigData = new FutureTask<JSONObject>(new CorporateBinsConfigServiceData(request.getSsoToken(), request.getSuperPnr(), corpBinConfigService, bookingType, productForFalcon));
		threadPoolTaskExecutor.submit(futureTaskForCorpBinConfigData);
		request.addFutureTask(PaymentUIUtil.CORP_BIN_CONFIG_FUTURE_TASK, futureTaskForCorpBinConfigData);
	}

	@RequestMapping(value = "pay-div")
	public ModelAndView getPaymentUI(HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		ModelAndView mv = createPayDivModelAndView(request, ClientType.DESKTOP);
		return mv;
	}

	@RequestMapping("test-wap-div")
	public ModelAndView testWapGetPaymentUI(HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		ModelAndView mv = createPayDivModelAndView(request, ClientType.WAP);
		mv.addObject("inclJsCss", true);
		String view = "desktop/testPaymentContainer";
		mv.setViewName(view);

		return mv;
	}
	@RequestMapping("test-web-div")
	public ModelAndView testWebGetPaymentUI(HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		ModelAndView mv = createPayDivModelAndView(request, ClientType.DESKTOP);
		mv.addObject("inclJsCss", true);
		// Set view.
		String view = "desktop/testPaymentContainer";
		mv.setViewName(view);
		return mv;
	}

	@RequestMapping("pay-div-wap")
	public ModelAndView getWapPaymentUI(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return createPayDivModelAndView(request, ClientType.WAP);
	}

	@ResponseBody
	@RequestMapping("HeartBeat")
	public String hearBeat(){
		return "Working";
	}

	@ResponseBody
	@RequestMapping("get-payment-info-details")
	public String getPaymentInfoDetailsJson(HttpServletRequest request){
		String superPnr = request.getParameter("superPnr");
		return paymentDAO.fetchParamForSuperPnrPaymentInfoAuditUnzipped(superPnr,"RESPONSE");
	}

	@ResponseBody
	@RequestMapping("get-booking-details")
	public String getBookingDetailsJson(HttpServletRequest request){
		String superPnr = request.getParameter("superPnr");
		String id = request.getParameter("id");
		if(YatraUtil.isNullOrEmpty(id))
			return paymentDAO.fetchParamForSuperPnrUnzipped(superPnr,"FETCH_ITINERARY_RES" );
		return paymentDAO.fetchParamForSuperPnrAndIdUnzipped(superPnr,"FETCH_ITINERARY_RES", id );
	}

	@RequestMapping("payment-json-app")
	public @ResponseBody String getAppPaymentJson(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
		return createPaymentOptionJson(request, ClientType.WAP);
	}

	@Deprecated
	@RequestMapping(value = "isCardInternational", method = RequestMethod.POST)
	public @ResponseBody
	String isCardInternational(@RequestParam("bin") String bin) throws Exception {
		return internationalCardService.getInternationalCardJSON(bin);
	}

	@RequestMapping("my-booking-qb")
	public ModelAndView getMyBookingQB(HttpServletRequest request, HttpServletResponse response) throws Exception {

		String viewName = "desktop/my-booking-qb";

		ModelAndView modelAndView = new ModelAndView();
		String staticContentURL = yatraPropertyReader.getProperty("static.content.url");
		modelAndView.addObject("static_content_url", staticContentURL);
		modelAndView.addObject("staticResourceVersion", yatraPropertyReader.getProperty("static.resource.version"));
		if(StringUtils.isNotBlank(PaymentUIUtil.getSSOToken(request))){
			modelAndView.addObject("csrfToken", PaymentUIUtil.getToken(PaymentUIUtil.getSSOToken(request)));
		}
		modelAndView.setViewName(viewName);
		return modelAndView;
	}

	@RequestMapping("personal-qb-cards")
	public ModelAndView getPersonalQBCards(HttpServletRequest request, HttpServletResponse response) throws Exception {

		String viewName = "desktop/my-booking-qb";

		ModelAndView modelAndView = new ModelAndView();
		String staticContentURL = yatraPropertyReader.getProperty("static.content.url");
		modelAndView.addObject("static_content_url", staticContentURL);
		modelAndView.addObject("staticResourceVersion", yatraPropertyReader.getProperty("static.resource.version"));

		if(StringUtils.isNotBlank(PaymentUIUtil.getSSOToken(request))){
			modelAndView.addObject("csrfToken", PaymentUIUtil.getToken(PaymentUIUtil.getSSOToken(request)));
		}

		String headerUrl = yatraPropertyReader.getProperty("b2c.header.url");
		String footerUrl = yatraPropertyReader.getProperty("b2c.footer.url");

		Map<String,String> cookieMap = PaymentUIUtil.getCookieMap(request);

		String header = frescoInteractionService.getContent(headerUrl, cookieMap);
		modelAndView.addObject("frescoHeader",header);
		String footer = frescoInteractionService.getContent(footerUrl, cookieMap);
		modelAndView.addObject("frescoFooter",footer);

		modelAndView.setViewName(viewName);
		return modelAndView;
	}

	@RequestMapping("corporate-qb-cards")
	public ModelAndView getCorporateQBCards(HttpServletRequest request, HttpServletResponse response) throws Exception {

		String viewName = "desktop/corp-qb";

		ModelAndView modelAndView = new ModelAndView();
		String staticContentURL = yatraPropertyReader.getProperty("static.content.url");
		modelAndView.addObject("static_content_url", staticContentURL);

		modelAndView.addObject("staticResourceVersion", yatraPropertyReader.getProperty("static.resource.version"));
		String ssoToken = PaymentUIUtil.getSSOToken(request);
		String merchant = "yatra";
		String product = "crpdom";
		corpQBHelper.addAuthorizationRelatedDataInModelCorp(modelAndView, ssoToken, merchant, product);

		if(StringUtils.isNotBlank(ssoToken)){
			modelAndView.addObject("csrfToken", PaymentUIUtil.getToken(PaymentUIUtil.getSSOToken(request)));
		}

		String headerUrl = yatraPropertyReader.getProperty("crp.header.url");
		String footerUrl = yatraPropertyReader.getProperty("crp.footer.url");

		Map<String,String> cookieMap = PaymentUIUtil.getCookieMap(request);

		String header = frescoInteractionService.getContent(headerUrl, cookieMap);
		modelAndView.addObject("frescoHeader",header);
		String footer = frescoInteractionService.getContent(footerUrl, cookieMap);
		modelAndView.addObject("frescoFooter",footer);
		modelAndView.addObject("corpTokenizationUrl",yatraPropertyReader.getProperty("crp.tokenize.url"));
		List <String> clientListToEnablePCCTabFor = Arrays.asList(yatraPropertyReader.getProperty("crp.pcc.show.tab.client.list").split("~"));
		boolean isUserAuthorized = Boolean.valueOf(modelAndView.getModelMap().get(QBConstant.IS_USER_AUTHORIZED).toString());
		String clientId ="";
		if(isUserAuthorized){
			clientId = modelAndView.getModelMap().get("clientId").toString();
		}
		modelAndView.addObject("showPccTab",false);
		if(!clientId.trim().equals("") && clientListToEnablePCCTabFor.contains(clientId)){
			modelAndView.addObject("showPccTab",true);
		}

		modelAndView.addObject("userRole", "ADMIN");
		modelAndView.setViewName(viewName);
		return modelAndView;
	}

	/* below controller is used for fetching user's quickbook cards  */
	@RequestMapping("qb")
	public @ResponseBody
	String getQuickBookJSON(HttpServletRequest request, HttpServletResponse response)  {

		// Fetch Request Params.
		String merchant = request.getParameter("merchant");
		String product = request.getParameter("product");

		// For Cross Domain Ajax Call.
		response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));

		// Get ssoToken.
		String ssoToken = PaymentUIUtil.getSSOToken(request);

		// Get QuickBook JSON.
		QuickBookPaymentOption quickBookPaymentOption = null;
		try{
			quickBookPaymentOption = quickBookExpressUIService.getQuickBookBean(merchant, product, ssoToken, 0L, null);

		} catch (Exception e) {
			logger.error("Exception occurred while getting QuickBook Bean : ", e);
			quickBookPaymentOption = quickBookExpressUIService.getQBErrorBean(QuickBookUIUtil.ERROR_CODE_EXCEPTION, e.getMessage());

		}
		try {
			return objectMapper.writeValueAsString(quickBookPaymentOption);
		} catch (IOException e) {
			return "";
		}
	}

	/* below controller is used for fetching user's tokenized card */
	@RequestMapping(value ="tqb", method=RequestMethod.POST)
	public @ResponseBody
	String getTokenizedQuickBookJSON(HttpServletRequest request, HttpServletResponse response)  {

		// Fetch Request Params.
		String merchant = request.getParameter("merchant");
		String product = request.getParameter("product");

		// For Cross Domain Ajax Call.
		response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));

		// Get ssoToken.
		String ssoToken = PaymentUIUtil.getSSOToken(request);

		// Get QuickBook JSON.
		QuickBookPaymentOption quickBookPaymentOption = null;
		try{
			quickBookPaymentOption = quickBookUIService.getQuickBookBean(merchant, product, ssoToken, 0L, null);
		} catch (Exception e) {
			logger.error("Exception occurred while getting QuickBook Bean : ", e);
			quickBookPaymentOption = quickBookUIService.getQBErrorBean(QuickBookUIUtil.ERROR_CODE_EXCEPTION, e.getMessage());
		}
		try {
			return objectMapper.writeValueAsString(quickBookPaymentOption);
		} catch (IOException e) {
			return "";
		}
	}



	@RequestMapping("yatra-qb-cards")
	public ModelAndView getYatraQBCards(HttpServletRequest request, HttpServletResponse response) throws Exception {

		String viewName = "desktop/corp-yatra-card";

		ModelAndView modelAndView = new ModelAndView();
		String staticContentURL = yatraPropertyReader.getProperty("static.content.url");
		modelAndView.addObject("static_content_url", staticContentURL);
		modelAndView.addObject("staticResourceVersion", yatraPropertyReader.getProperty("static.resource.version"));

		//		String ssoToken = PaymentUIUtil.getSSOToken(request);
		//		String merchant = "yatra";
		//                String product = "crpdom";

		//		corpQBHelper.addAuthorizationRelatedDataInModelCorp(modelAndView, ssoToken, merchant, product);

		//		if(StringUtils.isNotBlank(ssoToken)){
		//			modelAndView.addObject("csrfToken", PaymentUIUtil.getToken(PaymentUIUtil.getSSOToken(request)));
		//		}

		String headerUrl = yatraPropertyReader.getProperty("crp.header.url");
		String footerUrl = yatraPropertyReader.getProperty("crp.footer.url");

		Map<String,String> cookieMap = PaymentUIUtil.getCookieMap(request);

		String header = frescoInteractionService.getContent(headerUrl, cookieMap);
		modelAndView.addObject("frescoHeader",header);
		String footer = frescoInteractionService.getContent(footerUrl, cookieMap);
		modelAndView.addObject("frescoFooter",footer);

		modelAndView.setViewName(viewName);
		return modelAndView;
	}

	@RequestMapping("corporate-cards-json")
	public @ResponseBody
	String getcorporateCardsJSON(HttpServletRequest request, HttpServletResponse response) {
		String ssoToken = PaymentUIUtil.getSSOToken(request);
		// For Cross Domain Ajax Call.
		response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
		return corporateCardsUIService.getCorporateCardsJson(ssoToken);
	}

	@RequestMapping("corporate-cards-json-corpQB")
	public @ResponseBody
	String getcorporateCardsJSONWithAuthorization(HttpServletRequest request, HttpServletResponse response) {
		String ssoToken = PaymentUIUtil.getSSOToken(request);
		if(!corporateCardsUIService.isUserAuthorized(ssoToken))
			return blankJson;
		// For Cross Domain Ajax Call.
		response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
		return corporateCardsUIService.getCorporateCardsJson(ssoToken);
	}

	@RequestMapping("corporate-login")
	public @ResponseBody
	CorporateLoginResponseBean loginCorporateUser(HttpServletRequest request, HttpServletResponse response) {

		// For Cross Domain Ajax Call.
		response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
		String emailId = request.getParameter("emailId");
		String password = request.getParameter("password");
		return corporateCardsUIService.loginCorporateUser(emailId, password);
	}

	@RequestMapping("agent-login")
	public @ResponseBody
	JSONObject agentLogin(HttpServletRequest request, HttpServletResponse response) {

		response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
		String emailId = request.getParameter("email");
		String ssoToken = request.getParameter("ssoToken");
		JSONObject json = agentProfileService.agentLogin(emailId, ssoToken);
		if(json.getInt("resCode") == 200) {
			updateSsoTokenCookie(request, response, ssoToken);
		}
		return json;
	}

	private void updateSsoTokenCookie(HttpServletRequest request, HttpServletResponse response, String ssoToken) {
		Cookie[] cookies = request.getCookies();
		Cookie ssoTokenCookie = null;
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
				if (cookies[i].getName().equals("ssoToken")) {
					ssoTokenCookie = cookies[i];
					break;
				}
			}
		}
		if (ssoTokenCookie != null) {
			ssoTokenCookie.setValue(ssoToken);
			ssoTokenCookie.setDomain(".yatra.com");
			ssoTokenCookie.setPath("/");
			ssoTokenCookie.setMaxAge(365*24*60*60);
			ssoTokenCookie.setSecure(true);
			response.addCookie(ssoTokenCookie);
			logger.info("Updated sso token cookie value to: " + ssoToken);
		}
	}

	@RequestMapping("corporate-cards-html")
	public ModelAndView getcorporateCardsHTML(HttpServletRequest request, HttpServletResponse response) {

		String ssoToken = PaymentUIUtil.getSSOToken(request);
		String type = request.getParameter("type");
		String viewName = "desktop/corpCard";

		if(!YatraUtil.isNullOrEmpty(type) && type.equalsIgnoreCase("mobile")){
			viewName = "wap/wapCorpCard";
		}

		List<CorporateCardInfo> corpCardList= corporateCardsUIService.getCorporateCardList(ssoToken);

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("corpCardList", corpCardList);
		modelAndView.setViewName(viewName);

		return modelAndView;
	}

	@RequestMapping("authenticate-yatra-user")
	public @ResponseBody
	String getAuthenticateQBYatraUserJSON(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {

		// Fetch Request Params.
		String merchant = request.getParameter("merchant");
		String product = request.getParameter("product");
		String email = request.getParameter("email");
		String password = request.getParameter("password");

		return quickBookUIService.getYatraQBAuthBeanJSON(merchant, product, email, password, response);
	}

	@RequestMapping("authenticate-fb-user")
	public @ResponseBody
	String getAuthenticateQBFacebookUser(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {

		// Fetch Request Params.
		String merchant = request.getParameter("merchant");
		String product = request.getParameter("product");
		String accessToken = request.getParameter("accessToken");
		String sourcePage = request.getParameter("sourcepage");

		return quickBookUIService.getFacebookQBAuthBeanJSON(merchant, product, accessToken, sourcePage, response);
	}

	@RequestMapping("delete-qb-card")
	public @ResponseBody
	String deletedQuickBookCard(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
		String ssoToken = PaymentUIUtil.getSSOToken(request);
		Map<String,String> requestMap = PaymentUIUtil.getRequestMap(request);
		//if(yatraPropertiesDAO.getBooleanPropValue(PaymentUIUtil.PAY_BY_TOKEN)){
		return quickBookUIService.deleteTokenizedCard(ssoToken, requestMap);
	}

	@RequestMapping("delete-corp-qb-card")
	public @ResponseBody
	String deletedCorporateQuickBookCard(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
		String ssoToken = PaymentUIUtil.getSSOToken(request);
		Map<String,String> requestMap = PaymentUIUtil.getRequestMap(request);
		return corporateCardsUIService.deleteQuickBookCard(ssoToken, requestMap);
	}

	@RequestMapping("save-qb-card")
	public  @ResponseBody
	String saveQuickBookCard(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
		String ssoToken = PaymentUIUtil.getSSOToken(request);
		Map<String,String> requestMap = PaymentUIUtil.getRequestMap(request);
		return quickBookUIService.saveQuickBookCard(ssoToken, requestMap);
	}

	@RequestMapping("save-corporate-qb-card")
	public  @ResponseBody
	String saveCorporateQuickBookCard(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
		String ssoToken = PaymentUIUtil.getSSOToken(request);
		Map<String,String> requestMap = PaymentUIUtil.getRequestMap(request);
		return corporateCardsUIService.saveCorporateQuickBookCard(ssoToken, requestMap);
	}

	@RequestMapping("update-password")
	public  @ResponseBody
	String updatePassword(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
		String ssoToken = PaymentUIUtil.getSSOToken(request);
		Map<String,String> requestMap = PaymentUIUtil.getRequestMap(request);
		return quickBookUIService.updatePassword(ssoToken, requestMap);
	}

	@RequestMapping("is-pass-compromised")
	public  @ResponseBody
	String isPasswordCompromised(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
		String ssoToken = PaymentUIUtil.getSSOToken(request);
		return quickBookUIService.isPasswordCompromised(ssoToken);
	}

	@RequestMapping("checkHeader")
	public  @ResponseBody
	String checkHeader(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {

		Map<String, String> map = new HashMap<String, String>();

		Enumeration headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String key = (String) headerNames.nextElement();
			String value = request.getHeader(key);
			map.put(key, value);
		}
		logger.info("HeaderMap is :" + map);
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			logger.info("Cookies are present");
			for (int i = 0; i < cookies.length; i++) {
				logger.info(cookies[i].getName());
			}
		}
		Map<String, String> fieldMap = new HashMap<String, String>();
		Enumeration e = request.getParameterNames();

		while (e.hasMoreElements()) {
			String fieldName = (String) e.nextElement();
			String fieldValue = request.getParameter(fieldName);

			if (StringUtils.isNotBlank(fieldValue))
				fieldMap.put(fieldName, fieldValue);
		}
		logger.info("FieldMap is " + fieldMap);
		return map.toString();
		//		return "checkHeader complete";
	}


	@RequestMapping("updatePaymentDiv")
	public @ResponseBody String updatePaymentDiv(HttpServletRequest request, HttpServletResponse response) throws Exception {

		logger.debug("call to update payment div");

		// build complete set of payment div and set it in the in memory
		paymentDivBuilder.build();

		// return if the call was from internal payment server
		String isInternal = request.getParameter("internal");
		if(isInternal!=null && isInternal.equals("true")){
			return "CURRENT SERVER UPDATED";
		}

		// flush the remote cash
		String cacheName = yatraPropertyReader.getProperty("cache.name.central.payment");
		remoteCacheManager.removeAllDiv(cacheName);

		// If the call was from external source, call update div on all the payment servers
		InetAddress addr = InetAddress.getLocalHost();
		paymentDivUpdater.updatePaymentDiv(addr.getHostAddress());

		return "ALL SERVERS UPDATED";

	}

	private ModelAndView createPayDivModelAndView(HttpServletRequest request, ClientType clientType) throws Exception {

		// Get parameters from HTTP request.
		String merchant = request.getParameter("merchant");
		String product = request.getParameter("product");
		String ssoToken = PaymentUIUtil.getSSOToken(request);
		String inclJsCss = request.getParameter("inclJsCss");
		String language = StringUtils.isEmpty(request.getParameter("language")) ? "en" : request.getParameter("language");
		logger.info("Language selected: "+ language);

		// Create ModelAndView.
		ModelAndView modelAndView = new ModelAndView();

		// Get Payment div for given merchant and product and set it in model.
		String key = PaymentUIUtil.getPaymentDivKey(merchant, product);
		PaymentDiv paymentDiv = paymentDivCache.get(key);

		// Agent specific payment options
		String b2bProducts = yatraPropertyReader.getProperty("b2b.channel.products");
		if(StringUtils.isNotBlank(b2bProducts) && StringUtils.isNotBlank(ssoToken)){
			List<String> b2bArrayList = asList(b2bProducts.split(","));
			if(b2bArrayList.contains(product) && DEFAULT_YATRA_MERCHANT.equalsIgnoreCase(merchant)){
				paymentDiv = setAgentSpecificPaymentOptions(paymentDiv,ssoToken,"");
			}
		}
		modelAndView.addObject("paymentOptionListEmptyMessage", yatraPropertyReader.getProperty("payment.options.empty.message"));
		modelAndView.addObject("paymentDisplayBean", paymentDiv);

		QuickBookPaymentOption quickBookPaymentOption;
		try {
			quickBookPaymentOption = quickBookUIService.quickBookPaymentOptionBean(merchant, product, ssoToken, 0L, null);
		}catch (Exception ex) {
			logger.error("Error: " + ex.getMessage(), ex);
			quickBookPaymentOption = quickBookUIService.getQBErrorBean(QuickBookUIUtil.ERROR_CODE_EXCEPTION, ex.getMessage());
		}
		// If single quotes are not added, then while using this value on view from model, it will be directly converted to JSON object. This value is to be used as String however.
		String qbJson = "'" + objectMapper.writeValueAsString(quickBookPaymentOption) + "'";;
		modelAndView.addObject("qbJson", qbJson);

		if(StringUtils.isNotBlank(PaymentUIUtil.getSSOToken(request))){
			modelAndView.addObject("csrfToken", PaymentUIUtil.getToken(PaymentUIUtil.getSSOToken(request)));
		}

		// Get Reward Bins and set it in model.
		modelAndView.addObject("rewardCardsBinList", rewardBinsProvider.getRewardCardBinList());

		// Set static content URL and version in model.
		modelAndView.addObject("static_content_url", yatraPropertyReader.getProperty("static.content.url"));
		if(StringUtils.isNotBlank(inclJsCss) && inclJsCss.equalsIgnoreCase("true")){
			modelAndView.addObject("inclJsCss", inclJsCss);
		}

		// Get view.
		String view = PaymentUIUtil.DESKTOP_RESPONSIVE_PAGE;
		modelAndView.setViewName(view);
		modelAndView.addObject("staticResourceVersion", yatraPropertyReader.getProperty("static.resource.version"));
		modelAndView.addObject("isssoTokenStaleForCorpCards",
				corporateCardsUIService.getSsoTokenStaleInfo(ssoToken));
		// Return ModelAndView.
		return modelAndView;
	}

	private ModelAndView populateModel(PaymentPageResponse pageResponse, PaymentRequest paymentRequest) {
		logger.info("inside populate model ");
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName(getViewName(paymentRequest.getClient()));

		LobDetailResponse bookingDetailsJson = new LobDetailResponse(pageResponse.getLobDetailsJson());
		modelAndView.addObject("hideEcashDiv", bookingDetailsJson.getHideEcashDivFlag());
		modelAndView.addObject("previousEcashRedeemed", paymentUIHelper.getPreviousRedeemedEcash(paymentRequest.getSuperPnr(), bookingDetailsJson.getUserEmail()));

		String currencyCode = "Rs.";
		String currencySymbolClass = "RupeeSign curcd newCurr";

		if(bookingDetailsJson.getCrpInfoNode()!=null && !StringUtils.isEmpty(bookingDetailsJson.getCrpInfoNode().get("CURRENCYCODE")) && !StringUtils.equalsIgnoreCase(bookingDetailsJson.getCrpInfoNode().get("CURRENCYCODE"),"INR")){
			currencyCode = bookingDetailsJson.getCrpInfoNode().get("CURRENCYCODE");
			currencySymbolClass = "";
		}
		if(PaymentUIUtil.checkIfInternationalProduct(bookingDetailsJson.getProductCode(),yatraPropertiesDAO)){
			//getNZDCurrency will only give the currencyName send by the LOB.
			currencyCode = bookingDetailsJson.getNZDCurrency();
			currencySymbolClass = "";
		}
		modelAndView.addObject("currencyCode",currencyCode);
		modelAndView.addObject("currencySymbolClass",currencySymbolClass);

		/* adding payLater flow to view for hotel  */
		/*if(bookingDetailsJson.getPayLaterFlow()!=null){
			modelAndView.addObject("payLaterFlow",bookingDetailsJson.getPayLaterFlow());
		}*/

		reorderPaymentDivForSME(pageResponse);
		PaymentDiv updatedCashDiv = cashPoolPayOpService.updateDiv(pageResponse,paymentRequest);
		if(updatedCashDiv != null) {
			pageResponse.setPaymentDiv(updatedCashDiv);
		}
		PaymentDiv updatedPrivyDiv = updatedPrivyDiv(pageResponse,bookingDetailsJson);
		if(updatedPrivyDiv != null) {
			pageResponse.setPaymentDiv(updatedPrivyDiv);
		}
		PaymentDiv restrictBajajEMIDiv = restrictBajajEMIDiv(pageResponse, bookingDetailsJson.getProductCodeForUI(), bookingDetailsJson.isTCSEnabled());
		if(restrictBajajEMIDiv != null) {
			pageResponse.setPaymentDiv(restrictBajajEMIDiv);
		}
		/*PaymentDiv restrictTwid = restrictCred(pageResponse, bookingDetailsJson.getUserEmail());
		if(restrictTwid != null) {
			pageResponse.setPaymentDiv(restrictTwid);
		}*/

		/*PaymentDiv updatedTwidDiv = updateBalanceInTwidPayop(pageResponse, bookingDetailsJson, paymentRequest.getSsoToken());
		if(updatedTwidDiv != null) {
			pageResponse.setPaymentDiv(updatedTwidDiv);
		}*/
		modelAndView.addObject("paymentDisplayBean", pageResponse.getPaymentDiv());
		modelAndView.addObject(PaymentUIUtil.BOOKING_DETAIL, pageResponse.getLobDetailsJson());

		JSONObject withoutCVVSupportedCardTypeJSON = getCVVSupportedCardTypeJSON(paymentRequest, pageResponse);
		modelAndView.addObject("withoutCVVSupportedCardTypeJSON", withoutCVVSupportedCardTypeJSON.toString());

		String qbJson="";
		if(!StringUtils.isEmpty(paymentRequest.getProductCode())
				&& !StringUtils.equalsIgnoreCase(paymentRequest.getProductCode(),"null")
				&& PaymentUIUtil.checkIfCorporateProduct(paymentRequest.getProductCode())){
			qbJson = qbJsonCorp(paymentRequest, bookingDetailsJson);
		}
		else{
			qbJson = qbJson(paymentRequest, bookingDetailsJson);
		}

		modelAndView.addObject("qbJson",qbJson);

		/* Setting savecardMessage*/
		String saveCardMsg = "Save your card for faster payment. CVV will not be saved.";
		if(!PaymentUIUtil.checkIfCorporateProduct(paymentRequest.getProductCode())){
			saveCardMsg = "Tokenize your card as per the latest RBI guidelines";
		}
		modelAndView.addObject("saveCardMsg",saveCardMsg);
		modelAndView.addObject("exchangeRates", paymentUIHelper.getExchangeRates(paymentRequest.getMerchantCode(), paymentRequest.getProductCode(), paymentRequest.getSuperPnr()));

		if(StringUtils.isNotBlank(paymentRequest.getSsoToken())){
			modelAndView.addObject("csrfToken", PaymentUIUtil.getToken(paymentRequest.getSsoToken()));
		}

		modelAndView.addObject("rewardCardsBinList", rewardBinsProvider.getRewardCardBinList());

		modelAndView.addObject("staticResourceVersion", yatraPropertyReader.getProperty("static.resource.version"));
		modelAndView.addObject("static_content_url", yatraPropertyReader.getProperty("static.content.url"));
		modelAndView.addObject("minify", minifyExtension(paymentRequest.getMinifyInfo()));
		String inclJsCss = paymentRequest.getParameter("inclJsCss");
		if(StringUtils.isNotBlank(inclJsCss) && inclJsCss.equalsIgnoreCase("true")){
			modelAndView.addObject("inclJsCss", inclJsCss);
		}

		modelAndView.addObject("client", paymentRequest.getClient());
		//modelAndView.addObject("serverIpAddress", CallContextKeeper.getCallContext().getHostIPAddress());
		//modelAndView.addObject("ssoId", paymentRequest.getSsoToken());
		modelAndView.addObject("message", paymentRequest.getMessageForDisplay());
		modelAndView.addObject("payopMessage", payopMessageMappingCache.getCache());
		modelAndView.addObject("product", paymentRequest.getProductCode());
		modelAndView.addObject("b2bProducts",yatraPropertyReader.getProperty("b2b.channel.products"));
		modelAndView.addObject("isssoTokenStaleForCorpCards",
				corporateCardsUIService.getSsoTokenStaleInfo(paymentRequest.getSsoToken()));
		modelAndView.addObject("tncUrl",yatraPropertyReader.getProperty("tnc.default.url"));
		modelAndView.addObject("dcPinBankVsPgData", getDcPinBankVsPgData(paymentRequest));
		modelAndView.addObject("favicon", favicon(paymentRequest.getHost()));
		modelAndView.addObject("voucherSource", voucherService.getGVSource(paymentRequest.getMerchantCode()));
		modelAndView.addObject("voucherLOB", voucherService.getGVProduct(paymentRequest.getProductCode()));
		modelAndView.addObject("disableEMICondition", uiEmiBank.fetchDatafromUiEmiBankTable());

		if(paymentRequest.getAdditionalPayment() != null) {
			modelAndView.addObject("isAdditionalPaymentAttempt", "true");
			modelAndView.addObject("uuid", paymentRequest.getUuid());
		}
		else {
			modelAndView.addObject("isAdditionalPaymentAttempt", "false");
		}

		if (bookingDetailsJson.getHeaderMessages() != null && bookingDetailsJson.getHeaderMessages().size() > 0) {
			modelAndView.addObject("headerMessageList", bookingDetailsJson.getHeaderMessages());
		}

		if (bookingDetailsJson.getCancellationPolicy() != null && bookingDetailsJson.getCancellationPolicy().size() > 0) {
			modelAndView.addObject("cancellationPolicyList", bookingDetailsJson.getCancellationPolicy());
		}

		addCybersourceFingerPrintingData(modelAndView);
		popuLateRenderingClassAndUpdateView(modelAndView,paymentRequest);
		populateFrescoHeaderFooter(modelAndView, paymentRequest, bookingDetailsJson.isGAEnabled());

		try {
			modelAndView.addObject("byPassUIChecksForWhitelistedEmail",
					byPassUIChecksForWhitelistedEmail(bookingDetailsJson.getUserEmail(), paymentRequest.getProductCode(), yatraPropertyReader, paymentDAO));
		} catch (Exception ex) {
			logger.error("For superpnr: " + paymentRequest.getSuperPnr() + ", product: " + paymentRequest.getProductCode() + ", Error in determining byPassUIChecksForWhitelistedEmail: " + ex.getMessage(), ex);
		}

		String showQBCheckBox = "true";
		String skipOtpList = "{}";
		if(crpProductArrayList.contains(paymentRequest.getProductCode())) {
			showQBCheckBox = "false";
			try {
				skipOtpList = ((JSONObject) paymentRequest.getFutureTask(CORP_CONFIG_FUTURE_TASK).get()).toString();
				logger.info("SkipOtpBinList for superPnr " + paymentRequest.getSuperPnr() + " is : " + skipOtpList);
			}
			catch(Exception e) {
				logger.error("Couldn't populate skipOtpList for superPnr : " + paymentRequest.getSuperPnr() + " due to some exception :", e);
			}
		}

		modelAndView.addObject("showQBCheckBox", showQBCheckBox);
		modelAndView.addObject("enableSessionTimer", yatraPropertyReader.getProperty("enableSessionTimer"));
		PaymentDiv updatedDiv = overrideNoCostEmiValues(paymentRequest, pageResponse);
		if (updatedDiv != null) {
			modelAndView.addObject("isNoCostEmiFlag", true);
			modelAndView.addObject("paymentDisplayBean", updatedDiv);
			modelAndView.addObject("showEMIInfo", true);
		} else {
			modelAndView.addObject("isNoCostEmiFlag", false);
			modelAndView.addObject("showEMIInfo", false);
		}
		boolean  showSaveCardCorpcheckBox = PaymentUIUtil.checkIfCorporateProduct(paymentRequest.getProductCode()) ? false : true;
		showSaveCardCorpcheckBox = showSaveCardCorpcheckBox && hideSaveCardCheckBoxForInternationalProducts(paymentRequest,yatraPropertiesDAO);
		modelAndView.addObject("showSaveCardCorpcheckBox",showSaveCardCorpcheckBox);
		modelAndView.addObject("showQR", isQRFlowEnabled(paymentRequest.getProductCode()));
		modelAndView.addObject("showBanner", isStaticBannerToBeShownForProduct(paymentRequest.getProductCode()));
		modelAndView.addObject("internationalProduct", PaymentUIUtil.checkIfInternationalProduct(paymentRequest.getProductCode(),yatraPropertiesDAO));
		modelAndView.addObject("internationalCurrency", PaymentUIUtil.checkIfInternationalCurrency(currencyCode,yatraPropertiesDAO));
		modelAndView.addObject("internationalProductsList",PaymentUIUtil.getInternationalProductList(yatraPropertiesDAO));

		/*Fetching the bankCodes for Upi Dropdown suggestions */
		List<String> listOfBanksCodes = completePaymentInfoDAO.getUpiBankCodes();

		if (!listOfBanksCodes.isEmpty()) {
			JSONArray upiBankCodesList = new JSONArray();
			for (String bankCode : listOfBanksCodes) {
				upiBankCodesList.add("@" + bankCode);
			}
			modelAndView.addObject("upiBankCodesList", upiBankCodesList);
		} else {
			modelAndView.addObject("upiBankCodesList", new JSONArray());
		}



		populateAdBannerData(modelAndView);
		/* checking if train lob */
		if(PaymentUIUtil.checkIfTrainLobProduct(paymentRequest.getProductCode())){
			modelAndView.addObject("isLobTrain","true");
		}
		return modelAndView;
	}

	private PaymentDiv updateBalanceInTwidPayop(
			PaymentPageResponse pageResponse,
			LobDetailResponse bookingDetailsJson, String ssoToken) {
		//return pageResponse.getPaymentDiv();
		if(YatraUtil.isNullOrEmpty(ssoToken))
			return pageResponse.getPaymentDiv();
		String balance = null;
		try {
			String response = PaymentUIUtil.makeCheckTwidEligibilityCall(bookingDetailsJson.getUserMobile(), bookingDetailsJson.getTotalAmount(), bookingDetailsJson.getSuperPnr(), bookingDetailsJson.getProductCode());
			logger.info("twid eligibility response received : " + response);
			balance = new org.json.JSONObject(response).optString("pointRedeemable"); //use total_points_redeemable balance to display UI
			//balance = new org.json.JSONObject(response).optString("amountRedeemable"); //use total_amount_redeemable balance to display UI
			logger.info("balance : " + balance);
			if(YatraUtil.isNullOrEmpty(balance))
				return pageResponse.getPaymentDiv();
		} catch(Exception e) {
			logger.error("Could not get balance, hence returning same div");
			return pageResponse.getPaymentDiv();
		}
		logger.info("Updating twid Div");
		List<PaymentOption> updatedPayOps = new ArrayList<>();
		TwidPaymentOption twidPayop = null;
		PaymentDiv updatedDiv = new PaymentDiv();
		updatedDiv.setMerchantCode(pageResponse.getPaymentDiv().getMerchantCode());
		updatedDiv.setProductCode(pageResponse.getPaymentDiv().getProductCode());
		updatedDiv.setVersion(pageResponse.getPaymentDiv().getVersion());
		for(PaymentOption payOp : pageResponse.getPaymentDiv().getPaymentOptions()) {
			if(!payOp.getCode().equalsIgnoreCase("twid"))
				updatedPayOps.add(payOp);
			else {
				twidPayop = new TwidPaymentOption(payOp);
				twidPayop.setBalance(balance);
				updatedPayOps.add(twidPayop);
			}
		}
		updatedDiv.setPaymentOptions(updatedPayOps);
		return updatedDiv;
	}


	private PaymentDiv restrictCred(PaymentPageResponse pageResponse, String emailId) {
		if("yatratestbookings@gmail.com".equalsIgnoreCase(emailId)) {
			return pageResponse.getPaymentDiv();
		} else {
			List<PaymentOption> updatedPayOps = new ArrayList<>();
			PaymentDiv updatedDiv = new PaymentDiv();
			updatedDiv.setMerchantCode(pageResponse.getPaymentDiv().getMerchantCode());
			updatedDiv.setProductCode(pageResponse.getPaymentDiv().getProductCode());
			updatedDiv.setVersion(pageResponse.getPaymentDiv().getVersion());
			for(PaymentOption payOp : pageResponse.getPaymentDiv().getPaymentOptions()) {
				if(!payOp.getCode().equalsIgnoreCase("cred"))
					updatedPayOps.add(payOp);
			}
			updatedDiv.setPaymentOptions(updatedPayOps);
			return updatedDiv;
		}
	}

	private PaymentDiv restrictBajajEMIDiv(PaymentPageResponse pageResponse,
										   String productCodeForUI, boolean isTcsEnabled) {
		if("false".equalsIgnoreCase(yatraPropertiesDAO.getYatraPropValue("bajaj.emi.restricted"))
				|| PaymentUIUtil.getDomesticFlightProductList(yatraPropertiesDAO).contains(productCodeForUI)
				|| PaymentUIUtil.getDomesticHotelProductList(yatraPropertiesDAO).contains(productCodeForUI)
				|| (PaymentUIUtil.getHolidayProductList(yatraPropertiesDAO).contains(productCodeForUI) && !isTcsEnabled)) {
			return pageResponse.getPaymentDiv();
		} else {
			List<PaymentOption> updatedPayOps = new ArrayList<>();
			EMIPaymentOption emiPayop = null;
			PaymentDiv updatedDiv = new PaymentDiv();
			updatedDiv.setMerchantCode(pageResponse.getPaymentDiv().getMerchantCode());
			updatedDiv.setProductCode(pageResponse.getPaymentDiv().getProductCode());
			updatedDiv.setVersion(pageResponse.getPaymentDiv().getVersion());
			for(PaymentOption payOp : pageResponse.getPaymentDiv().getPaymentOptions()) {
				if(!payOp.getCode().equalsIgnoreCase("emi"))
					updatedPayOps.add(payOp);
				else if(payOp.getCode().equalsIgnoreCase("emi")) {
					EMIPaymentOption emiPayopNew = new EMIPaymentOption(payOp);
					emiPayop = emiPayopNew;

					List<EMIBank> pbanks = ((EMIPaymentOption)emiPayop).getBanks();
					List<EMIBank> newPBanks = new ArrayList<>();
					for (EMIBank pBnk : pbanks) {
						EMIBank pb = new EMIBank(pBnk);
						if(pb.getCode().equalsIgnoreCase("bajaj"))
							pb.setStatus(Status.DISABLED);
						newPBanks.add(pb);
					}
					((EMIPaymentOption)emiPayop).setBanks(newPBanks);

					updatedPayOps.add(emiPayop);
				}
			}
			updatedDiv.setPaymentOptions(updatedPayOps);
			return updatedDiv;
		}
	}

	private boolean isStaticBannerToBeShownForProduct(String productCode) {
		/*if("dom2".equalsIgnoreCase(productCode) || "int2".equalsIgnoreCase(productCode)
				|| "metadom".equalsIgnoreCase(productCode) || "metaint".equalsIgnoreCase(productCode))
			return true;
		else */return false;
	}

	private void populateAdBannerData(ModelAndView modelAndView) {
		boolean isAdBannerEnabled = false;
		String response = paymentDAO.isAdBannerEnabled();
		if("true".equalsIgnoreCase(response)) {
			isAdBannerEnabled = true;
			modelAndView.addObject("adBannerUrl", paymentDAO.getAdBannerImageUrl());
		}
		modelAndView.addObject("isAdBannerEnabled", isAdBannerEnabled);
	}

	private PaymentDiv overrideNoCostEmiValues(PaymentRequest paymentRequest, PaymentPageResponse pageResponse) {
		try {
			if (noCostEmiBanksCache.get(paymentRequest.getProductCode()) == null) {
				return null;
			}
			NoCostEmiProduct noCostProduct = noCostEmiBanksCache.get(paymentRequest.getProductCode());
			List<NoCostEmiBank> banks = noCostProduct.getEmiBanks();
			if (banks == null || banks.isEmpty()) {
				return null;
			}

			boolean flag = false;
			if (!noCostProduct.getIsPromoAllowed()) {
				String promo = new LobDetailResponse (pageResponse.getLobDetailsJson()).getPromoCode();
				if (!StringUtils.isEmpty(promo))
					return null;
			}
			PaymentOption emiPayOp = null;
			List<PaymentOption> updatedPayOps = new ArrayList<>();

			for(PaymentOption p : pageResponse.getPaymentDiv().getPaymentOptions()) {
				if("emi".equalsIgnoreCase(p.getCode())) {
					EMIPaymentOption emiPayOpNew = new EMIPaymentOption((EMIPaymentOption) p);
					emiPayOp = emiPayOpNew;
					updatedPayOps.add(emiPayOpNew);
				} else {
					updatedPayOps.add(p);
				}
			}
			if (emiPayOp == null) {
				return null;
			}
			PaymentDiv updatedDiv = new PaymentDiv();
			updatedDiv.setMerchantCode(pageResponse.getPaymentDiv().getMerchantCode());
			updatedDiv.setProductCode(pageResponse.getPaymentDiv().getProductCode());
			updatedDiv.setPaymentOptions(updatedPayOps);
			List<EMIBank> emiBanks = ((EMIPaymentOption)emiPayOp).getBanks();
			List<EMIBank> newEmiBanks = new ArrayList<>();
			for (EMIBank emiBnk : emiBanks) {
				EMIBank eb = new EMIBank(emiBnk);
				newEmiBanks.add(eb);
				copyList(eb);
			}
			((EMIPaymentOption)emiPayOp).setBanks(newEmiBanks);

			for (NoCostEmiBank noCostEmiBank: banks) {
				for (EMIBank emiBank : newEmiBanks) {
					if (emiBank.getCode().equalsIgnoreCase(noCostEmiBank.getBank())) {
						List<EmiType> emiTypes = emiBank.getEmiTypes();
						List<EmiType> noCostEmiTypes = noCostEmiBank.getEmiTypes();
						for (EmiType noCostEmiType : noCostEmiTypes) {
							for (EmiType emiType : emiTypes) {
								if (noCostEmiType.getEmiTenure().equalsIgnoreCase(emiType.getEmiTenure())) {
									flag = true;
									emiType.setInterestRate(noCostEmiType.getInterestRate());
								}
							}

						}
					}

				}
			}
			if (flag) {
				return updatedDiv;
			} else {
				return null;
			}
		} catch (Exception ex) {
			logger.error("Error while overriding no cost emi values " + paymentRequest.getSuperPnr() + " error: " + ex.getMessage());
			return null;
		}
	}

	private void copyList(EMIBank eb) {
		List<EmiType> newEmiTypes = new ArrayList<>();
		for (EmiType emiType : eb.getEmiTypes()) {
			EmiType newEmiType = new EmiType(emiType.getEmiTenure(), emiType.getInterestRate(), emiType.getMinAmount(), emiType.getMaxAmount());
			newEmiTypes.add(newEmiType);
		}
		eb.setEmiTypes(newEmiTypes);
	}
	private JSONObject getCVVSupportedCardTypeJSON(
			PaymentRequest paymentRequest, PaymentPageResponse pageResponse) {
		JSONObject withoutCVVSupportedCardTypeJson = new JSONObject();
		for(PaymentOption p : pageResponse.getPaymentDiv().getPaymentOptions()) {
			if(p.getCode().equalsIgnoreCase("cc")) {
				withoutCVVSupportedCardTypeJson.put("cc", new JSONObject());
				for(CardType cardType: ((CreditCardPaymentOption) p).getCardTypes()) {
					withoutCVVSupportedCardTypeJson.getJSONObject("cc").put(cardType.getCode(), withoutCVVsupported(paymentRequest.getSkipOtpFlagMap().get("cc"), cardType.getCardDetail().getWithoutCVVSupported()));
				}
			}
			if(p.getCode().equalsIgnoreCase("dc")) {
				withoutCVVSupportedCardTypeJson.put("dc", new JSONObject());
				for(CardType cardType: ((DebitCardPaymentOption) p).getCardTypes()) {
					withoutCVVSupportedCardTypeJson.getJSONObject("dc").put(cardType.getCode(), withoutCVVsupported(paymentRequest.getSkipOtpFlagMap().get("dc"), cardType.getCardDetail().getWithoutCVVSupported()));
				}
			}
		}
		paymentUIHelper.logIntoDB(withoutCVVSupportedCardTypeJson.toString(), "RESPONSE", paymentRequest.getSuperPnr(), "SkipOtpDecision");
		return withoutCVVSupportedCardTypeJson;
	}

	private String withoutCVVsupported(String skipOtpFlagForPayop, String withoutCVVSupported) {
		String skipOtpFlagPayments = paymentUIHelper.getSkipOtpFlagPayments();
		if("false".equalsIgnoreCase(skipOtpFlagPayments) || YatraUtil.isNullOrEmpty(skipOtpFlagForPayop)) {
			return String.valueOf(false);
		}
		return String.valueOf(("true".equalsIgnoreCase(skipOtpFlagForPayop) && "true".equalsIgnoreCase(withoutCVVSupported)));
	}

	private String qbJson(PaymentRequest paymentRequest, LobDetailResponse bookingDetailsJson) {
		QuickBookPaymentOption quickBookPaymentOption;
		try {
			boolean isCrpProduct = crpProductArrayList.contains(paymentRequest.getProductCode());
			if(isCrpProduct && (bookingDetailsJson.getTravellerSSOId() == null || "null".equalsIgnoreCase(bookingDetailsJson.getTravellerSSOId()))){
				logger.error("travellerSSOId is null for Corporate flow for SuperPNR " + paymentRequest.getSuperPnr());
				quickBookPaymentOption = quickBookUIService.getQBErrorBean(QuickBookUIUtil.ERROR_CODE_INVALID, QuickBookUIUtil.TRAVELLER_SSO_USER_ID_ABSENT);
			} else {
				quickBookPaymentOption = quickBookUIService.quickBookPaymentOptionBean(paymentRequest.getMerchantCode(), paymentRequest.getProductCode(), paymentRequest.getSsoToken(), PaymentUIUtil.convertToLongDefaultZero(bookingDetailsJson.getTravellerSSOId()), bookingDetailsJson.getBookingType());
				if(isCrpProduct)
					quickbookUIHelper.filerCardsBasedOnConfig(quickBookPaymentOption, ((JSONObject) paymentRequest.getFutureTask(CORP_CONFIG_FUTURE_TASK).get()).toString(), bookingDetailsJson.getBookingType(), bookingDetailsJson.getProductForFalcon());
			}
		} catch (Exception ex) {
			logger.error("For superpnr: " + paymentRequest.getSuperPnr() + ", product: " + paymentRequest.getProductCode() + ", Error: " + ex.getMessage(), ex);
			quickBookPaymentOption = quickBookUIService.getQBErrorBean(QuickBookUIUtil.ERROR_CODE_EXCEPTION, ex.getMessage());
		}
		String quickBookJson = "";
		try {
			quickBookJson = objectMapper.writeValueAsString(quickBookPaymentOption);
		} catch (IOException e) {
			logger.error("For superpnr: " + paymentRequest.getSuperPnr() + ", product: " + paymentRequest.getProductCode() + ", Error while converting qb payment option bean to json", e);
		}
		// If single quotes are not added, then while using this value on view from model, it will be directly converted to JSON object. This value is to be used as String however.
		return "'" + quickBookJson + "'";
	}




	/* this method is added to fetch corp personal cards from express*/
	private String qbJsonCorp(PaymentRequest paymentRequest, LobDetailResponse bookingDetailsJson) {
		QuickBookPaymentOption quickBookPaymentOption;
		try {
			boolean isCrpProduct = crpProductArrayList.contains(paymentRequest.getProductCode());
			if(isCrpProduct && (bookingDetailsJson.getTravellerSSOId() == null || "null".equalsIgnoreCase(bookingDetailsJson.getTravellerSSOId()))){
				logger.error("travellerSSOId is null for Corporate flow for SuperPNR " + paymentRequest.getSuperPnr());
				quickBookPaymentOption = quickBookExpressUIService.getQBErrorBean(QuickBookUIUtil.ERROR_CODE_INVALID, QuickBookUIUtil.TRAVELLER_SSO_USER_ID_ABSENT);
			} else {
				quickBookPaymentOption = quickBookExpressUIService.quickBookPaymentOptionBean(paymentRequest.getMerchantCode(), paymentRequest.getProductCode(), paymentRequest.getSsoToken(), PaymentUIUtil.convertToLongDefaultZero(bookingDetailsJson.getTravellerSSOId()), bookingDetailsJson.getBookingType());
				if(isCrpProduct)
					quickbookUIHelper.filerCardsBasedOnConfig(quickBookPaymentOption, ((JSONObject) paymentRequest.getFutureTask(CORP_CONFIG_FUTURE_TASK).get()).toString(), bookingDetailsJson.getBookingType(), bookingDetailsJson.getProductForFalcon());
			}
		} catch (Exception ex) {
			logger.error("For superpnr: " + paymentRequest.getSuperPnr() + ", product: " + paymentRequest.getProductCode() + ", Error: " + ex.getMessage(), ex);
			quickBookPaymentOption = quickBookExpressUIService.getQBErrorBean(QuickBookUIUtil.ERROR_CODE_EXCEPTION, ex.getMessage());
		}
		String quickBookJson = "";
		try {
			quickBookJson = objectMapper.writeValueAsString(quickBookPaymentOption);
		} catch (IOException e) {
			logger.error("For superpnr: " + paymentRequest.getSuperPnr() + ", product: " + paymentRequest.getProductCode() + ", Error while converting qb payment option bean to json", e);
		}
		// If single quotes are not added, then while using this value on view from model, it will be directly converted to JSON object. This value is to be used as String however.
		return "'" + quickBookJson + "'";
	}

	private String minifyExtension(Optional<String> minifyInfo) {
		if(minifyInfo.isPresent()) {
			return minifyInfo.get().equals("true")? ".min" : "";
		}
		return yatraPropertyReader.getProperty("minify");
	}

	private String favicon(String host) {
		if(host != null) {
			if(host.contains(PaymentUIUtil.TG_HOST)) {
				return "tg-favicon.ico";
			}
		}
		return "yatra-favicon.ico";
	}

	private String getDcPinBankVsPgData(PaymentRequest paymentRequest) {
		List<DcPinBankConfig> dcPinBankVsPgData = null;
		String dcPinBankData = null;
		try {
			dcPinBankVsPgData = (List<DcPinBankConfig>) paymentRequest.getFutureTask(PaymentUIUtil.DC_PIN_BANK_VS_PG_DETAIL_FUTURE_TASK).get();
			dcPinBankData = objectMapper.writeValueAsString(dcPinBankVsPgData);
		} catch (Exception ex) {
			logger.error("For superpnr: " + paymentRequest.getSuperPnr() + ", product: " + paymentRequest.getProductCode() + ", Error: " + ex.getMessage(), ex);
			return null;
		}
		logger.debug("Debit Card pin bank vs gateway configurations are : "+ dcPinBankVsPgData);
		return dcPinBankData;
	}

	private void setMessageForPayop(ModelAndView modelAndView) {
		//TO DO - check for ENABLED status
		List<UIMessage> fetchmwMessage = payopMessageMappingCache.getAll("mw");
		if(fetchmwMessage!=null && fetchmwMessage.size()>0) {
			Status mwStatus = fetchmwMessage.get(0).getStatus();
			String mwMsg =  fetchmwMessage.get(0).getMessageText();
			if(mwStatus!=null && Status.ENABLED.compareTo(mwStatus) == 0)
				modelAndView.addObject("mwMessage",mwMsg);
		}
		List<UIMessage> fetchnbMessage = payopMessageMappingCache.getAll("nb");
		if(fetchnbMessage!=null && fetchnbMessage.size()>0) {
			String nbMsg =  fetchnbMessage.get(0).getMessageText();
			Status nbStatus = fetchnbMessage.get(0).getStatus();
			if(nbStatus!=null && Status.ENABLED.compareTo(nbStatus) == 0)
				modelAndView.addObject("nbMessage",nbMsg);
		}

	}

	private void addCybersourceFingerPrintingData(ModelAndView modelAndView) {
		String mid = yatraPropertyReader.getProperty("cybersource.mid");
		String cookieSession = CallContextKeeper.getSessionId();
		String fingerPrintId = mid+cookieSession;
		String orgID = yatraPropertyReader.getProperty("cybersource.orgId");
		modelAndView.addObject("cybsFingerprint", fingerPrintId);
		modelAndView.addObject("cybsOrgId", orgID);
		String pUrl = "background:url(https://h.online-metrix.net/fp/clear.png?org_id=" + orgID + "&amp;session_id=" + fingerPrintId + "&amp;m=1)";
		String iUrl = "https://h.online-metrix.net/fp/clear.png?org_id=" + orgID + "&amp;session_id=" + fingerPrintId + "&amp;m=2";
		String sUrl = "https://h.online-metrix.net/fp/check.js?org_id=" + orgID + "&amp;session_id="+ fingerPrintId;
		String fUrl = "https://h.online-metrix.net/fp/fp.swf?org_id=" + orgID + "&amp;session_id="+ fingerPrintId;
		modelAndView.addObject("pUrl", pUrl);
		modelAndView.addObject("iUrl", iUrl);
		//modelAndView.addObject("sUrl", sUrl);
		modelAndView.addObject("fUrl", fUrl);
	}

	private void popuLateRenderingClassAndUpdateView(ModelAndView modelAndView, PaymentRequest paymentRequest) {
		logger.info("populate rendering class started  ");
		String product = paymentRequest.getProductCode();
		ProductServiceUrlBean productServiceUrlBean	= productServiceUrlCache.get(product);
		String isResponsive = productServiceUrlBean.getIsResponsive();
		String renderingClass;
		if(product.equalsIgnoreCase("tgdomhotel") || product.equalsIgnoreCase("tghomestays")){
			if("true".equalsIgnoreCase(isResponsive))
				renderingClass = "Bongo-ui tgPayswift" ;
			else
				renderingClass = "Bongo-ui tgPayswift show-old-pay" ;
		}
		else if(product.equalsIgnoreCase("asimonuments")) {
			renderingClass = "Bongo-ui asiPaySwift" ;
		}
		else {
			if("true".equalsIgnoreCase(isResponsive)){
				renderingClass = "Bongo-ui" ;
			}
			else{
				renderingClass = "Current-payswift";
			}
		}
		modelAndView.setViewName(getViewName(ClientType.DESKTOP));
		modelAndView.addObject("renderingClass",renderingClass);

	}

	private void populateFrescoHeaderFooter(ModelAndView modelAndView, PaymentRequest request, boolean isGAEnabled) {
		logger.info("populate header footer started ");
		ProductServiceUrlBean productServiceUrlBean	= productServiceUrlCache.get(request.getProductCode());

		String headerUrl = StringUtils.isBlank(productServiceUrlBean.getHeaderUrl())?yatraPropertyReader.getProperty("fresco.header.url"):productServiceUrlBean.getHeaderUrl();
		String footerUrl = StringUtils.isBlank(productServiceUrlBean.getFooterUrl())?yatraPropertyReader.getProperty("fresco.footer.url"):productServiceUrlBean.getFooterUrl();
		if(!isGAEnabled){
			logger.debug("GA is Disabled for this Booking, Overriding gaResponsive value(if present) to false in footer URL");
			footerUrl = footerUrl.replaceAll("gaResponsive=[^&]+","gaResponsive=" + false) + "&gaTrackingOff=true&ugaTrackingOff=true";
		}
		logger.info("header url : "+headerUrl);
		logger.info("footer url : "+footerUrl);
		Map<String,String> cookieMap = request.getCookieMap();

		String header = frescoInteractionService.getContent(headerUrl, cookieMap);
		modelAndView.addObject("frescoHeader",header);
		String footer = frescoInteractionService.getContent(footerUrl, cookieMap);
		modelAndView.addObject("frescoFooter",footer);

	}

	private String createPaymentOptionJson(HttpServletRequest request, ClientType clientType) throws Exception {
		// Get parameters from HTTP request.
		String merchant = request.getParameter("merchant");
		String product = request.getParameter("product");

		ObjectWriter ow = new ObjectMapper().viewWriter(PaymentDiv.class);


		String version = request.getParameter("version");
		String currentVersion = yatraPropertyReader.getProperty("app.json.version");
		// Get version.

		if(StringUtils.isNotBlank(version) && version.equalsIgnoreCase(currentVersion)){
			//sent blank json
			return "{}";
		}
		// Get Payment div for given merchant and product and set it in model.
		String key = PaymentUIUtil.getPaymentDivKey(merchant, product);
		PaymentDiv paymentDiv = paymentDivCache.get(key);
		paymentDiv.setVersion(currentVersion);

		return ow.writeValueAsString(paymentDiv);

	}

	private PaymentDiv setAgentSpecificPaymentOptions(PaymentDiv paymentDiv,String ssoToken, String ctripId) {

		List<PaymentOption> newPaymentOptions = new ArrayList<PaymentOption>();
		try {
			List<String> paymentOptions = agentProfileService.getAgentPaymentOptions(ssoToken);

			// creating agent specific payment options list
			List<PaymentOption> availablePaymentOptions = paymentDiv.getPaymentOptions();

			for (String paymentOption : paymentOptions) {
				// add credit pool payment option
				if (paymentOption.equalsIgnoreCase("cp")) {

					JSONObject creditPoolInfo = agentProfileService.getCreditPoolDetails(ssoToken, paymentDiv.getProductCode(), ctripId);

					if(YatraUtil.isNullOrEmpty(creditPoolInfo)){
						continue;
					}

					CreditPoolPaymentOption creditPool = new CreditPoolPaymentOption();
					creditPool.setCode("cp");
					creditPool.setDisplayText("Credit Pool");
					creditPool.setIframeEnabled("true");
					creditPool.setPriority(0);
					creditPool.setMessages(null);
					creditPool.setStatus(Status.ENABLED);

					creditPool.setCashAmount(getValidAmount(creditPoolInfo.get("cashPool")));
					creditPool.setCreditAmount(getValidAmount(creditPoolInfo.get("creditPool")));
					String showBal = creditPoolInfo.getOrDefault("showBal", "1").toString();
					creditPool.setShowBalance(showBal);

					newPaymentOptions.add(creditPool);

				} else if(paymentOption.equalsIgnoreCase("corpCard")){
					CorporateCardPaymentOption corpCardPaymentOption = new CorporateCardPaymentOption();
					CorporateCardsDisplayResponse response = null;
					try {
						response = corporateCardsUIService.getCorporateCardsResponse(ssoToken);
					} catch (PayswiftException ex) {
						continue;
					}

					boolean isSsoTokenStale = corporateCardsUIService.checkAndUpdateSsoTokenStaleInfo(ssoToken, response);
					if(isSsoTokenStale) {
						corpCardPaymentOption.setCode("corpCard");
						corpCardPaymentOption.setIframeEnabled("true");
						corpCardPaymentOption.setDisplayText("Corporate card");
						corpCardPaymentOption.setStatus(Status.ENABLED);
						corpCardPaymentOption.setIframeEnabled("true");
						corpCardPaymentOption.setMessages(null);
						corpCardPaymentOption.setPriority(2);
						corpCardPaymentOption.setCorporateCards(Collections.emptyList());
						newPaymentOptions.add(corpCardPaymentOption);
						continue;
					}
					List<CorporateCardInfo> corporateCardList = corporateCardsUIService.getCorpCards(response);

					if(YatraUtil.isNullOrEmpty(corporateCardList)){
						continue;
					}

					corpCardPaymentOption.setCode("corpCard");
					corpCardPaymentOption.setIframeEnabled("true");
					corpCardPaymentOption.setDisplayText("Corporate card");
					corpCardPaymentOption.setStatus(Status.ENABLED);
					corpCardPaymentOption.setIframeEnabled("true");
					corpCardPaymentOption.setMessages(null);
					corpCardPaymentOption.setPriority(2);
					corpCardPaymentOption.setCorporateCards(corporateCardList);
					newPaymentOptions.add(corpCardPaymentOption);

				} else {
					// add other payment options
					for (PaymentOption po : availablePaymentOptions) {
						if (paymentOption.equalsIgnoreCase(po.getCode())) {
							newPaymentOptions.add(po);
						}
					}
				}

			}

		} catch (Exception e) {
			logger.error("Exception while creating agent specific payment options so setting default payment option " + e);
			//TODO check later
			newPaymentOptions.add(paymentDiv.getPaymentOptions().get(0));
		}

		// adding payment options to payment div
		PaymentDiv updatedPaymentDiv = new PaymentDiv();
		updatedPaymentDiv.setPaymentOptions(newPaymentOptions);

		return updatedPaymentDiv;
	}

	private CreditPoolPaymentOption toCreditPoolPaymentOption(JSONObject creditPoolInfo) {
		CreditPoolPaymentOption creditPool = new CreditPoolPaymentOption();
		creditPool.setCode("cp");
		creditPool.setDisplayText("Credit Pool");
		creditPool.setIframeEnabled("true");
		creditPool.setPriority(0);
		creditPool.setMessages(null);
		creditPool.setStatus(Status.ENABLED);
		String creditPoolAmount = JSONUtils.isNull(creditPoolInfo.get("creditPool")) ? "" : (String) creditPoolInfo.get("creditPool");
		String cashPoolAmount = JSONUtils.isNull(creditPoolInfo.get("cashPool")) ? "" : (String) creditPoolInfo.get("cashPool");
		String showBal = creditPoolInfo.getOrDefault("showBal", "1").toString();
		creditPool.setShowBalance(showBal);
		creditPool.setCashAmount(cashPoolAmount);
		creditPool.setCreditAmount(creditPoolAmount);

		if (checkValidAmount(creditPoolAmount) && checkValidAmount(cashPoolAmount)) {
			creditPool.setPoolType("dualPool");
		} else if (checkValidAmount(cashPoolAmount)) {
			creditPool.setPoolType("cashPool");
		} else {
			creditPool.setPoolType("creditPool");
		}
		return creditPool;
	}

	private CorporateCardPaymentOption setCorpCardPayOpWithEmptyCards(CorporateCardPaymentOption corpCardPaymentOption) {
		corpCardPaymentOption.setCode("corpCard");
		corpCardPaymentOption.setIframeEnabled("true");
		corpCardPaymentOption.setDisplayText("Corporate card");
		corpCardPaymentOption.setStatus(Status.ENABLED);
		corpCardPaymentOption.setIframeEnabled("true");
		corpCardPaymentOption.setMessages(null);
		corpCardPaymentOption.setPriority(2);
		corpCardPaymentOption.setCorporateCards(Collections.emptyList());
		return corpCardPaymentOption;
	}

	private CorporateCardPaymentOption setCorpCardPayOpWithCards(CorporateCardPaymentOption corpCardPaymentOption, List<CorporateCardInfo> filteredCardList) {
		corpCardPaymentOption.setCode("corpCard");
		corpCardPaymentOption.setIframeEnabled("true");
		corpCardPaymentOption.setDisplayText("Corporate Card");
		corpCardPaymentOption.setStatus(Status.ENABLED);
		corpCardPaymentOption.setIframeEnabled("true");
		corpCardPaymentOption.setMessages(null);
		corpCardPaymentOption.setPriority(2);
		corpCardPaymentOption.setCorporateCards(overrideCardBrandForPCard(filteredCardList));
		return corpCardPaymentOption;
	}

	private PaymentDiv setAgentSpecificPaymentOptions(PaymentPageResponse paymentPageResponse, PaymentRequest paymentRequest,
													  JSONObject agentPaymentOptionJSON, String bookingType, HashMap<String, String> crpInfo, boolean isCrpProduct, String entity, String product, boolean passthrough) {
		List<String> agentPaymentOptions = getPaymentOptionsListFromJSON(agentPaymentOptionJSON, bookingType, isCrpProduct, entity, product, passthrough);
		disableUpiBasedOnClientIdForPersonalFlow(agentPaymentOptions,crpInfo,paymentRequest.getSsoToken(),bookingType);
		List<PaymentOption> newPaymentOptions = new ArrayList<PaymentOption>();

		String ssoToken = paymentRequest.getSsoToken();
		List<PaymentOption> availablePaymentOptions = paymentPageResponse.getPaymentDiv().getPaymentOptions();

		for (String paymentOption : agentPaymentOptions) {
			if (paymentOption.equalsIgnoreCase("cp")) {
				JSONObject creditPoolInfo = null;
				try {
					creditPoolInfo = (JSONObject) paymentRequest.getFutureTask(PaymentUIUtil.CREDIT_POOL_FUTURE_TASK).get();
				} catch (Exception ex) {
					logger.error("For superpnr: " + paymentRequest.getSuperPnr() + ", product: " + paymentRequest.getProductCode() + ", Error: " + ex.getMessage(), ex);
					continue;
				}
				newPaymentOptions.add(toCreditPoolPaymentOption(creditPoolInfo));
			} else if (paymentOption.equalsIgnoreCase("corpCard")) {
				logger.info("inside corp card");
				CorporateCardPaymentOption corpCardPaymentOption = new CorporateCardPaymentOption();
				CorporateCardsDisplayResponse response = null;
				try {
					response = corporateCardsUIService.getCorporateCardsResponseLevelWise(ssoToken, crpInfo);
				}catch (PayswiftException ex) {
					logger.error("For superpnr: " + paymentRequest.getSuperPnr() + ", product: " + paymentRequest.getProductCode() + ", Error: " + ex.getMessage(), ex);
					continue;
				}
				logger.info(response);

				boolean isSsoTokenStale = corporateCardsUIService.checkAndUpdateSsoTokenStaleInfo(ssoToken, response);
				logger.info("isSsoTokenStale = " + isSsoTokenStale);
				if (isSsoTokenStale) {
					newPaymentOptions.add(setCorpCardPayOpWithEmptyCards(corpCardPaymentOption));
					continue;
				}
				List<CorporateCardInfo> corporateCardList = new ArrayList<>();
				try {
					corporateCardList.addAll(corporateCardsUIService.getCorpCards(response));
					if (corporateCardList.isEmpty()) {
						continue;
					}
				} catch (PayswiftException ex) {
					logger.error("For superpnr: " + paymentRequest.getSuperPnr() + ", product: " + paymentRequest.getProductCode() + ", Error: " + ex.getMessage(), ex);
					continue;
				}
				List<CorporateCardInfo> filteredCardList = filterCards(corporateCardList, getCorpCardToDisplayListFromJSON(agentPaymentOptionJSON, isCrpProduct, bookingType, entity, product, passthrough));
				if(filteredCardList!=null && filteredCardList.size()>0)
					newPaymentOptions.add(setCorpCardPayOpWithCards(corpCardPaymentOption, filteredCardList));
				else {
					logger.error("For superpnr: " + paymentRequest.getSuperPnr() + ", product: " + paymentRequest.getProductCode() + " no card of relevatnt displayType present in user's account");
					continue;
				}
			} else {
				// add other payment options
				for (PaymentOption po : availablePaymentOptions) {
					if (paymentOption.equalsIgnoreCase(po.getCode())) {
						newPaymentOptions.add(po);
					}
				}
			}
		}

		//Adding quickbook if qb enabled
		//needed to reassign to avoid concurrent modification Exception , no time to test in QA or RFS .
		for (PaymentOption po : availablePaymentOptions) {
			if (po.getCode().equalsIgnoreCase("qb")) {
				if(isCrpProduct) {
					if(quickbookUIHelper.displayQBCardsCRP(agentPaymentOptionJSON, bookingType, entity, product, passthrough)) {
						newPaymentOptions.add(po);
					}
				} else {
					if(agentPaymentOptions.contains("CC") || agentPaymentOptions.contains("DC")) {
						newPaymentOptions.add(po);
					}
				}
			}
		}

		// adding payment options to payment div
		PaymentDiv updatedPaymentDiv = new PaymentDiv();
		updatedPaymentDiv.setPaymentOptions(newPaymentOptions);
		return updatedPaymentDiv;
	}

	//TO DO : try catch if applicableFor not found .
	public List<PaymentOption> filterPaymentOptionsBasedOnBookingType(List<PaymentOption> newPaymentOptions,
																	  JSONObject agentPaymentOptionJSON, String bookingType) {
		if(newPaymentOptions == null || agentPaymentOptionJSON == null){
			return null;
		}
		logger.debug("Booking type retrieved from payment Options is " + bookingType);
		List<PaymentOption> finalPaymentOptions = new ArrayList();
		JSONObject applicableFor = agentPaymentOptionJSON.getJSONObject("applicableFor");
		logger.debug("Applicable for JSON "+ applicableFor);
		for (PaymentOption po : newPaymentOptions){
			String code = po.getCode();
			if(applicableFor.getString(code.toUpperCase()).equalsIgnoreCase("ALL")){
				logger.debug("Payment Option "+po.getCode()+" added because it all under applicable all section ");
				finalPaymentOptions.add(po);
			}
			else if (applicableFor.getString(code.toUpperCase()).equalsIgnoreCase(bookingType)){
				logger.debug("Payment Option "+po.getCode()+" added because of booking Type .");
				finalPaymentOptions.add(po);
			}
		}
		return finalPaymentOptions;

	}

	public List<String> getPaymentOptionsListFromJSON(JSONObject agentPaymentOptionJSON, String bookingType, boolean isCrpProduct, String entity, String product, boolean passthrough) {
		if(isCrpProduct) {
			return paymentOptionsForCrp(agentPaymentOptionJSON, bookingType, entity, product, passthrough);
		}
		return paymentOptionsForB2B(agentPaymentOptionJSON);
	}

	public boolean isCrpProduct(String productCode) {
		return crpProductArrayList.contains(productCode);
	}

	private List<String> paymentOptionsForCrp(JSONObject agentPaymentOptionJSON, String bookingType, String entity, String product, boolean passthrough) {
		JSONObject payOpsByBookingType = paymentUIHelper.crpPaymentOptionsByBookingType(agentPaymentOptionJSON, bookingType, entity, product, passthrough);
		return new ArrayList<String>(payOpsByBookingType.keySet());
	}

	private List<String> paymentOptionsForB2B(JSONObject agentPaymentOptionJSON) {
		if(agentPaymentOptionJSON.getInt("resCode") != 200){
			throw new PayswiftException(FETCH_AGENT_PAYMENT_OPTIONS_INVALID_RESPONSE_CODE.getCode(), "Invalid response code for get agentPaymentOptions ");
		}
		String paymentOptionString = agentPaymentOptionJSON.getString("payOpt");
		return asList(paymentOptionString.split(","));
	}

	private List<String> getCorpCardToDisplayListFromJSON(JSONObject agentPaymentOptionJSON, boolean isCrpProduct, String bookingType, String entity, String product, boolean passthrough) {
		if(isCrpProduct) {
			return corpCardToDisplayListForCrp(agentPaymentOptionJSON, bookingType, entity, product, passthrough);
		}
		return corpCardToDisplayListForB2B(agentPaymentOptionJSON);
	}

	private List<String> corpCardToDisplayListForB2B(JSONObject agentPaymentOptionJSON) {
		String corpCrdsToDisplayString = agentPaymentOptionJSON.getString("corpCardsToDisplay");
		logger.debug("Value of corpCardsToDisplay from JSON is :: " + corpCrdsToDisplayString);
		if(YatraUtil.isNullOrEmpty(corpCrdsToDisplayString))
			return new ArrayList<String>();
		ArrayList<String> corpCrdsToDisplayList =  new ArrayList<String>(asList(corpCrdsToDisplayString.split("\\s*,\\s*")));
		return corpCrdsToDisplayList;
	}

	private List<String> corpCardToDisplayListForCrp(JSONObject agentPaymentOptionJSON, String bookingType, String entity, String product, boolean passthrough) {
		List<String> displayList = new ArrayList<>();
		JSONObject paymentOptionsByBookingType = paymentUIHelper.crpPaymentOptionsByBookingType(agentPaymentOptionJSON, bookingType, entity, product, passthrough);
		if(paymentOptionsByBookingType.has("CORPCARD")) {
			JSONObject corpCardJson = paymentOptionsByBookingType.getJSONObject("CORPCARD");

			if(corpCardJson.has("displayTypes")) {
				JSONArray displayTypes = corpCardJson.getJSONArray("displayTypes");
				logger.debug("Value of displayTypes: " + displayTypes.toString());
				for(int index = 0; index < displayTypes.size(); index++) {
					displayList.add(displayTypes.getString(index));
				}
			}
		}
		return displayList;
	}

	private List<CorporateCardInfo> filterCards(List<CorporateCardInfo> corporateCardList, List<String> corpCardToDisplayListFromJSON) {
		// Adding all the cards if empty list returned in response
		if(YatraUtil.isNullOrEmpty(corpCardToDisplayListFromJSON))
			return corporateCardList;
		else{
			List<CorporateCardInfo> filteredCardsList = new ArrayList<CorporateCardInfo>();
			for(CorporateCardInfo card : corporateCardList){
				if(corpCardToDisplayListFromJSON.contains(card.getCardType()))
					filteredCardsList.add(card);
			}
			return filteredCardsList;
		}
	}

	private List<CorporateCardInfo> overrideCardBrandForPCard(List<CorporateCardInfo> corporateCardList){
		for(int i = 0; i < corporateCardList.size(); i++){
			if(corporateCardList.get(i).getCardType().equalsIgnoreCase("PCard"))
				corporateCardList.get(i).setCardBrand("PCARD");
		}
		return corporateCardList;
	}

	private String getValidAmount(Object poolAmount) {
		String amount = "0";
		try{
			String poolAmountString = (String) poolAmount;
			if(YatraUtil.isNullOrEmpty(poolAmountString))return amount;
			float amountInFloat = Float.parseFloat(poolAmountString);
			if(amountInFloat<=0.0)return amount;
			return Integer.toString((int)amountInFloat);
		}catch(Exception e){
			logger.error("Invalid pool amount "+ e.getMessage());
			return amount;
		}
	}
	private PaymentDiv getDefaultPaymentDiv(String merchant, String product) {
		String defaultPayop = PaymentUIUtil.PAYMENT_OPTION_CREDIT_CARD;
		List<PaymentOption> defaultPaymentOptions = new ArrayList<PaymentOption>();
		String key = PaymentUIUtil.getPaymentDivKey(merchant,product);
		PaymentDiv paymentDiv = paymentDivCache.get(key);
		for (PaymentOption po : paymentDiv.getPaymentOptions()) {
			if (defaultPayop.equalsIgnoreCase(po.getCode())) {
				defaultPaymentOptions.add(po);
			}

		}
		PaymentDiv div = new PaymentDiv();
		div.setMerchantCode(merchant);
		div.setProductCode(product);
		div.setPaymentOptions(defaultPaymentOptions);
		return div;
	}
	private boolean checkValidAmount(String poolAmount) {
		try{
			if(YatraUtil.isNullOrEmpty(poolAmount))return false;
			if(Float.parseFloat(poolAmount)<=0.0)return false;
			return true;
		}catch(Exception e){
			logger.error("Invalid pool amount "+ e.getMessage());
			return false;
		}

	}

	public void logResponseTimeInDB(PaymentRequest paymentRequest,Long requestTime){
		Long responseTime = System.currentTimeMillis();
		Long responseTimeInMilliSeconds = (responseTime - requestTime);
		logger.debug("Logging Response in PayswiftLatency for SuperPnr :" +paymentRequest.getSuperPnr()  + "at :" +responseTime);
		loggingProcessor.logIntoPayswiftLatency(paymentRequest.getSuperPnr(), paymentRequest.getTtid(), PaymentUIUtil.LOB_PAGE_LOAD, paymentRequest.getProductCode(), responseTimeInMilliSeconds.toString());
	}

	@RequestMapping(value = "clearObjs")
	public @ResponseBody String clearObjs(HttpServletRequest request){
		try{
			logger.info("Going to Clear the Objects from JVM.");
			paymentUIHelper.clearLists();
			logger.info("Clearing of the Objects from JVM COMPLETED !!");
			return("SUCCESS");
		}catch(Exception ex){
			logger.error("Exception while clearing Objects from JVM: ", ex);
			return("FAILED");
		}
	}

	@RequestMapping(value = "reloadNoCostEmiData")
	@ResponseBody
	public void reloadNoCostEmiData() throws Exception {
		noCostCacheBuilder.reload();
	}

	@RequestMapping(value = "isNoCostEmiSupported")
	@ResponseBody
	public String isNoCostEmiSupported(HttpServletRequest request) throws Exception {
		String superPnr = request.getParameter("superPnr");
		String bankCode = request.getParameter("bank");
		String tenure =request.getParameter("tenure");
		try {
			String bookingDetailJson = paymentDAO.fetchParamForSuperPnrUnzipped(superPnr,"FETCH_ITINERARY_RES" );
			org.json.JSONObject bookingDetailJsonObject = new org.json.JSONObject(bookingDetailJson);
			String product = bookingDetailJsonObject.getString("product");
			if (YatraUtil.isNullOrEmpty(product) || noCostEmiBanksCache.get(product) == null)
				return "false";
			else if(YatraUtil.isNullOrEmpty(noCostEmiBanksCache.get(product).getEmiBanks()))
				return "false";
			else {
				List<NoCostEmiBank> banks = noCostEmiBanksCache.get(product).getEmiBanks();
				for(NoCostEmiBank bank : banks) {
					if(bankCode.equalsIgnoreCase(bank.getBank())) {
						List<EmiType> emiTypes = bank.getEmiTypes();
						for(EmiType emiType : emiTypes) {
							if(tenure.equalsIgnoreCase(emiType.getEmiTenure()))
								return "true";
						}
						return "false";
					}
				}
				return "false";
			}
		} catch(Exception e) {
			return "false";
		}

	}

	@RequestMapping(value = "testCruise")
	@ResponseBody
	public String testCruise(HttpServletRequest request) throws Exception {
		String url = "https://cruise.yatra.com/web/payswift.ashx?method=BookingDetail&client=DESKTOP&";
		String superPnr= request.getParameter("superPnr");
		String uuid = request.getParameter("uuid");
		String completeUrl = url + "superPnr=" + superPnr + "&uuid=" + uuid ;
		logger.info("Complete url is : " + completeUrl);
		return PaymentUIUtil.sendGetRequest(completeUrl);
	}

	private void reorderPaymentDivForSME(PaymentPageResponse pageResponse) {
		if(PaymentUIUtil.isSMEProduct(pageResponse.getPaymentDiv().getProductCode())) {
			List<PaymentOption> paymentOptionList = pageResponse.getPaymentDiv().getPaymentOptions();
			for (int i = 0; i < paymentOptionList.size(); i++) {
				PaymentOption payop = paymentOptionList.get(i);
				if(payop.getCode().equalsIgnoreCase("nb")) {
					Collections.swap(paymentOptionList, i, 0);
				}
			}
			pageResponse.getPaymentDiv().setPaymentOptions(paymentOptionList);
		}
	}

	private PaymentDiv updatedPrivyDiv(PaymentPageResponse pageResponse, LobDetailResponse bookingDetailsJson) {
		if(bookingDetailsJson.isPrivyFlow()) {
			List<PaymentOption> updatedPayOps = new ArrayList<>();
			NetBankingPaymentOption nbPayop = null;
			PaymentDiv updatedDiv = new PaymentDiv();
			updatedDiv.setMerchantCode(pageResponse.getPaymentDiv().getMerchantCode());
			updatedDiv.setProductCode(pageResponse.getPaymentDiv().getProductCode());
			updatedDiv.setVersion(pageResponse.getPaymentDiv().getVersion());
			for(PaymentOption payOp : pageResponse.getPaymentDiv().getPaymentOptions()) {
				if(payOp.getCode().equalsIgnoreCase("cc") || payOp.getCode().equalsIgnoreCase("dc"))
					updatedPayOps.add(payOp);
				else if(payOp.getCode().equalsIgnoreCase("nb")) {
					NetBankingPaymentOption nbPayopNew = new NetBankingPaymentOption(payOp);
					nbPayop = nbPayopNew;

					List<Bank> pbanks = ((NetBankingPaymentOption)nbPayop).getPreferredBanks();
					List<Bank> newPBanks = new ArrayList<>();
					for (Bank pBnk : pbanks) {
						Bank pb = new Bank(pBnk);
						if(!pb.getCode().equalsIgnoreCase("icicinet"))
							pb.setStatus(Status.DISABLED);
						newPBanks.add(pb);
					}
					((NetBankingPaymentOption)nbPayop).setPreferredBanks(newPBanks);

					List<Bank> banks = ((NetBankingPaymentOption)nbPayop).getBanks();
					List<Bank> newBanks = new ArrayList<>();
					for (Bank Bnk : banks) {
						Bank b = new Bank(Bnk);
						b.setStatus(Status.DISABLED);
						newBanks.add(b);
					}
					((NetBankingPaymentOption)nbPayop).setBanks(newBanks);

					updatedPayOps.add(nbPayop);
				}
			}
			updatedDiv.setPaymentOptions(updatedPayOps);
			return updatedDiv;

		} else return pageResponse.getPaymentDiv();
	}

	private boolean isQRFlowEnabled(String product) {
		boolean isQRFlowEnabled = false;
		if(product.toLowerCase().contains("holiday"))
			return isQRFlowEnabled;
		try {
			String response = PaymentUIUtil.sendGetRequest(yatraPropertyReader.getProperty("is.qr.flow.enabled.checker.url"));
			if("true".equalsIgnoreCase(response))
				isQRFlowEnabled = true;
		} catch (Exception e) {
			logger.error("Got exception while verifying isQRFlowEnabled, hence setting it to false", e);
		}
		return isQRFlowEnabled;
	}

	@RequestMapping(value = "getQRImage")
	@ResponseBody
	public void getQRImage(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String mtxnId = request.getParameter("mtxnId");
		String uid = request.getParameter("uid");
		boolean isValid = validateInput(mtxnId, uid);
		String link = null;
		if(isValid) {
			link = paymentDAO.getIntentUrl(mtxnId, uid);
		}
		if(YatraUtil.isNullOrEmpty(link))
			link = ".";
		QRCodeWriter qrCodeWriter = new QRCodeWriter();
		BitMatrix bitMatrix = qrCodeWriter.encode(link, BarcodeFormat.QR_CODE, 150, 170);
		ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
		MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);

		response.setContentType("image/png");
		response.setContentLength(pngOutputStream.size());

		OutputStream outStream = response.getOutputStream();

		outStream.write(pngOutputStream.toByteArray());

		outStream.flush();
		outStream.close();
	}


	private boolean validateInput(String mtxnId, String uid) {
		if(YatraUtil.isNullOrEmpty(mtxnId)
				|| YatraUtil.isNullOrEmpty(uid)
				|| !mtxnId.replace("-", "").matches("[A-Za-z0-9]+")
				|| !uid.matches("[A-Za-z0-9]+"))
			return false;
		else return true;
	}

	@RequestMapping(value = "updateHDFCEMIRate")
	@ResponseBody
	public String updateHDFCEMIRate(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			paymentDAO.updateHDFCEMIRate("15.00");
			return "Updated";
		} catch(Exception e) {
			logger.error("Exception while updating hdfc emmi rate", e);
			return "Not Updated, please run queries";
		}

	}

	@RequestMapping(value = "trackUPIJSError")
	@ResponseBody
	public String trackUPIJSError(HttpServletRequest request) {
		try {
			String ttid = request.getParameter("ttid");
			logger.info("Inside trackUPIJSError() for ttid : " + ttid);
			String description = request.getParameter("description");
			logger.info("Exception logged is " + description);
		} catch(Exception e) {
			logger.error("Exception in parsing response");
		}
		return "Success";
	}

	private boolean hideSaveCardCheckBoxForInternationalProducts(PaymentRequest paymentRequest,YatraPropertiesDAO yatraPropertiesDAO) {
		boolean showSaveCardCorpcheckBox = true;
		boolean isInternationalProduct = PaymentUIUtil.checkIfInternationalProduct(paymentRequest.getProductCode(),yatraPropertiesDAO);
		if (isInternationalProduct) {
			showSaveCardCorpcheckBox = false;
		}
		return showSaveCardCorpcheckBox;
	}

	private void disableUpiBasedOnClientIdForPersonalFlow(List<String> agentPaymentOptions, HashMap<String,String> crpInfo, String ssoToken , String bookingType){
		try{
			if(StringUtils.equalsIgnoreCase(bookingType,"PERSONAL")) {
				if (CollectionUtils.isEmpty(corpDisableUpiArrayList)) {
					String clientsForUpiHandling = yatraPropertiesDAO.getYatraPropValue("force.disable.upi.client.list");
					if (StringUtils.isNotBlank(clientsForUpiHandling)) {
						corpDisableUpiArrayList = asList(clientsForUpiHandling.split(","));
					}
				}
				String clientId = MapUtils.isNotEmpty(crpInfo) ? crpInfo.get("CLIENTID") : null;
				if (CollectionUtils.isNotEmpty(corpDisableUpiArrayList)) {
					if (StringUtils.isBlank(clientId)) {
						JSONObject B2BUserProfileJson = agentProfileService.getUserDetailsCorp(ssoToken);
						if (B2BUserProfileJson != null && !B2BUserProfileJson.isEmpty() && B2BUserProfileJson.containsKey("clientId")) {
							clientId = B2BUserProfileJson.getString("clientId");
						}
					}
					if (corpDisableUpiArrayList.contains(clientId)) {
						/* removing upi for specific client need to remove once driven from corporate config */
						agentPaymentOptions.remove("UPI");
					}
				}
			}


		}
		catch (Exception ex){
			logger.error("Exception Occurred while hiding  upi payop for specific clients ",ex);
		}
	}

}
