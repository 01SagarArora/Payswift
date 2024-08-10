package com.yatra.payment.qb.corporate.manager;

import com.yatra.express.crp.cards.v3.beans.CorporateCardInfo;
import com.yatra.payment.offline.constants.GDSConstants;
import com.yatra.payment.offline.service.GDSService;
import com.yatra.payment.payswift.beans.B2BUserProfileDetailBean;
import com.yatra.payment.qb.corporate.bean.*;
import com.yatra.payment.qb.corporate.constant.QBConstant;
import com.yatra.payment.qb.corporate.factory.QBBeanFactory;
import com.yatra.payment.qb.corporate.helper.BTACTAHelperService;
import com.yatra.payment.qb.corporate.helper.QBHelper;
import com.yatra.payment.qb.corporate.service.QBService;
import com.yatra.payment.qb.corporate.service.impl.CorpQBserviceImpl;
import com.yatra.payment.qb.corporate.service.impl.PersonalQBServiceImpl;
import com.yatra.payment.ui.dao.impl.CorporateCardVaultDAOImpl;
import com.yatra.payment.ui.display.beans.QuickBookCard;
import com.yatra.payment.ui.service.TenantService;
import com.yatra.payment.ui.service.impl.CorporateProfileServiceImpl;
import com.yatra.payment.ui.util.HttpUtil;
import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.payment.ui.util.QuickBookUIUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import static com.yatra.payment.qb.corporate.constant.QBConstant.*;

@Component
public class QBManager {

	private static final Logger logger = Logger.getLogger(QBManager.class);
	private static final ObjectMapper ObjectMapper = new ObjectMapper();

	@Autowired
	private QBBeanFactory qbBeanFactory;
	@Autowired
	private QBHelper qbHelper;
	@Autowired
	private CorpQBserviceImpl corpQBserviceImpl;
	@Autowired
	private PersonalQBServiceImpl personalQBServiceImpl;
	@Autowired
	private GDSService gdsService;
	@Autowired
	private TenantService tenantService;
	@Autowired
	private CorporateProfileServiceImpl corporateProfileServiceImpl;
	@Autowired
	private BTACTAHelperService btactaHelperService;
	@Autowired
	private CorporateCardVaultDAOImpl corporateCardVaultDAO;

	@Autowired private HttpUtil httpUtil;

	public String getCards(HttpServletRequest request) {
		String ssoToken = PaymentUIUtil.getSSOToken(request);
		try {
			Map<String, String> requestMap = PaymentUIUtil.getRequestMap(request);
			logger.info("get-cards request received ,ssoToken :" + ssoToken + " ,request parameters : " + requestMap);
			B2BUserProfileDetailBean profileBean = (B2BUserProfileDetailBean) request.getAttribute(QBConstant.USER_PROFILE_BEAN_KEY);
			GetCardsRequest personalQBGetCardRequest = qbBeanFactory.getGetCardsRequestBean(request, QB_TYPE_PERSONAL);

			GetCardsRequest corpQBGetcardRequest = qbBeanFactory.getGetCardsRequestBean(request, QB_TYPE_CORPORATE);
			Future<List<?>> corpQBCardFuture = qbHelper.executeServiceGetCardInThread(corpQBserviceImpl, corpQBGetcardRequest);
			Future<List<?>> personalQBCardFuture = qbHelper.executeServiceGetCardInThread(personalQBServiceImpl, personalQBGetCardRequest);
			@SuppressWarnings("unchecked")
			List<CorporateCardInfo> corporateCards = (List<CorporateCardInfo>) corpQBCardFuture.get();
			@SuppressWarnings("unchecked")
			List<QuickBookCard> personalCards = (List<QuickBookCard>) personalQBCardFuture.get();

			GetCardsResponse getCardsResponse = null;

			//BTA-CTA-Flow
			boolean btaCtaFlow = btactaHelperService.isBtaCtaFlow(ssoToken);
			boolean isMappedUserFlow = isMappedUserFlow(profileBean,requestMap);

			if (btaCtaFlow && !isMappedUserFlow) {
				return getCardsResponseForBTACTAFlow(request, profileBean, ssoToken);
			}

			if (StringUtils.isBlank(profileBean.getCorpUserRole())) {
				return NOT_ACCESS_JSON;
			} else if (USER_ROLE_ADMIN.equals(profileBean.getCorpUserRole())) {

				getCardsResponse = new GetCardsResponse(corporateCards, personalCards);
				corporateProfileServiceImpl.addUsersAndEntityData(corporateCards, getCardsResponse);
				getCardsResponse.setAllowOfflineBookings(gdsService.getSaveCardToGDSFlag(ssoToken));
			} else {

				List<CorporateCardInfo> filteredCorpCards = corporateProfileServiceImpl.filterEmployeeLevelPcc(corporateCards,profileBean);
				getCardsResponse = new GetBtaCtaCaseCardsResponse(filteredCorpCards,personalCards);
				corporateProfileServiceImpl.addUsersAndEntityData(filteredCorpCards, getCardsResponse);
				getCardsResponse.setAllowOfflineBookings(gdsService.getSaveCardToGDSFlag(ssoToken));
			}
			return ObjectMapper.writeValueAsString(getCardsResponse);
		} catch (Exception e) {
			logger.error("getCards request fail for ssoToken :" + ssoToken, e);
			return FATAL_ERROR_JSON;
		}
	}
	public boolean  isMappedUserFlow(B2BUserProfileDetailBean profileDetailBean , Map<String,String> requestMap ){
		boolean isMappedUserFlow = false;
		String role = profileDetailBean.getCorpUserRole();
		String ssoUserId = profileDetailBean.getUserId();
		String targerUserId = requestMap.get(PaymentUIUtil.TARGET_USER_ID);
		if (role.equalsIgnoreCase(USER_ROLE_EA)
				&& !StringUtils.isBlank(targerUserId)
				&& !StringUtils.equalsIgnoreCase(ssoUserId, targerUserId)) {
			isMappedUserFlow = true;
		}
		return isMappedUserFlow;

	}


	private String getCardsResponseForBTACTAFlow(HttpServletRequest request, B2BUserProfileDetailBean profileBean, String ssoToken) throws Exception {

		GetCardsRequest personalQBGetCardRequest = qbBeanFactory.getGetCardsRequestBean(request, QB_TYPE_PERSONAL);
		GetCardsRequest corpQBGetcardRequest = qbBeanFactory.getGetCardsRequestBean(request, QB_TYPE_CORPORATE);
		Future<List<?>> corpQBCardFuture = qbHelper.executeServiceGetCardInThread(corpQBserviceImpl, corpQBGetcardRequest);
		Future<List<?>> personalQBCardFuture = qbHelper.executeServiceGetCardInThread(personalQBServiceImpl, personalQBGetCardRequest);

		@SuppressWarnings("unchecked")
		List<CorporateCardInfo> corporateCards = (List<CorporateCardInfo>) corpQBCardFuture.get();
		@SuppressWarnings("unchecked")
		List<QuickBookCard> personalCards = (List<QuickBookCard>) personalQBCardFuture.get();


		GetCardsResponse getCardsResponse = null;

		List<String> btaCardsInPersonalCardIdsList = btactaHelperService.filterBtaCardsInPersonalCards(corporateCards, personalCards);

		if (StringUtils.isBlank(profileBean.getCorpUserRole())) {
			return NOT_ACCESS_JSON;
		} else if (USER_ROLE_ADMIN.equals(profileBean.getCorpUserRole())) {
			getCardsResponse = new GetBtaCtaCaseCardsResponse(corporateCards, personalCards);
			corporateProfileServiceImpl.addUsersAndEntityData(corporateCards, getCardsResponse);
			getCardsResponse.setAllowOfflineBookings(gdsService.getSaveCardToGDSFlag(ssoToken));
		} else {
			List<CorporateCardInfo> filteredCorpCards = corporateProfileServiceImpl.filterEmployeeLevelPcc(corporateCards,profileBean);
			getCardsResponse = new GetBtaCtaCaseCardsResponse(filteredCorpCards,personalCards);
			corporateProfileServiceImpl.addUsersAndEntityData(filteredCorpCards, getCardsResponse);
			getCardsResponse.setAllowOfflineBookings(gdsService.getSaveCardToGDSFlag(ssoToken));

		}

		((GetBtaCtaCaseCardsResponse) getCardsResponse).setBtaInPersonalCards(btaCardsInPersonalCardIdsList);
		return ObjectMapper.writeValueAsString(getCardsResponse);
	}

	public String saveCard(HttpServletRequest request) {
		String returnValue = null;
		String ssoToken = PaymentUIUtil.getSSOToken(request);
		try {
			//Map<String, String> requestMap = PaymentUIUtil.getRequestMap(request);
			logger.info("save-card request received ,ssoToken :" + ssoToken);
			B2BUserProfileDetailBean profileBean = (B2BUserProfileDetailBean) request.getAttribute(QBConstant.USER_PROFILE_BEAN_KEY);
			String userRole = profileBean.getCorpUserRole();
			String qbType = (String) request.getParameter(QBConstant.QB_TYPE_KEY);
			String saveCardToGDS = request.getParameter(GDSConstants.SAVE_CARD_TO_GDS);
			String cType =request.getParameter(QBConstant.C_TYPE);
			SaveCardRequest saveCardRequest = qbBeanFactory.getSaveCardRequestBean(request, qbType, profileBean.getUserId());
			QBService qbService = qbHelper.getQBServiceByUserRoleQBTypeAndCType(userRole, qbType,cType);
			if (qbService != null) {
				if (!StringUtils.isEmpty(saveCardToGDS)) {
					if (saveCardRequest instanceof PersonalQBSaveCardRequest) {
						returnValue = qbService.addCardToGDS(ssoToken, (PersonalQBSaveCardRequest) saveCardRequest, profileBean.getUserId());
					} else {
						returnValue = qbService.saveCard(saveCardRequest);
					}
				} else {
					returnValue = qbService.saveCard(saveCardRequest);
				}
			} else {
				returnValue = NOT_ACCESS_JSON;
			}
			logger.info("save-card API response :" + returnValue);
			return returnValue;
		} catch (Exception e) {
			logger.error("saveCard request fail for ssoToken :" + ssoToken, e);
			return FATAL_ERROR_JSON;
		}
	}


	public String getYatraCards(HttpServletRequest request) {
		String accessToken = PaymentUIUtil.getAccessToken(request);

		try {
			logger.info("get-yatra-cards request received ,accessToken :" + accessToken);
			if (!qbHelper.getAgentAclDataForYatraCards(accessToken, QBConstant.ROLE_VIEW)) {
				return QBConstant.NOT_ACCESS_YATRA_CARD_JSON;
			}
			QBService qbService = qbHelper.getQBServiceByUserRoleAndQBType(USER_ROLE_ADMIN, QBConstant.QB_TYPE_CORPORATE);

			GetCardsResponse getCardsResponse = null;
			GetCardsRequest corpQBGetcardRequest = qbBeanFactory.getGetCardsRequestBean(request, QB_TYPE_CORPORATE);
			corpQBGetcardRequest.setSsoToken(accessToken);
			List<?> corporateCards = qbService.getYatraCards(corpQBGetcardRequest);
			getCardsResponse = new GetCardsResponse((List<CorporateCardInfo>) corporateCards, null);
			return ObjectMapper.writeValueAsString(getCardsResponse);
		} catch (Exception e) {
			logger.error("getYatraCards request fail for accessToken :" + accessToken, e);
			return FATAL_ERROR_JSON;
		}
	}

	public String saveYatraCard(HttpServletRequest request) {
		String returnValue = null;
		String accessToken = PaymentUIUtil.getAccessToken(request);
		try{
			logger.info("save-yatra-card request received , accessToken :" + accessToken );
			if (!qbHelper.getAgentAclDataForYatraCards(accessToken, QBConstant.ROLE_EDIT)) {
				return QBConstant.NOT_ACCESS_YATRA_CARD_JSON;
			}
			SaveCardRequest saveCardRequest = qbBeanFactory.getSaveCardRequestBean(request, QBConstant.QB_TYPE_CORPORATE, null);
			QBService qbService = qbHelper.getQBServiceByUserRoleAndQBType(USER_ROLE_ADMIN, QBConstant.QB_TYPE_CORPORATE);
			if (qbService != null) {
				returnValue = qbService.saveYatraCard(saveCardRequest);
			}

			logger.info("save-yatra-card API response :"+returnValue);
			return returnValue;
		}catch (Exception e) {
			logger.error("saveYatraCard request fail for accessToken :" + accessToken, e);
			return FATAL_ERROR_JSON;
		}
	}

	public String deleteYatraCard(HttpServletRequest request) {
		String returnValue = null;
		String accessToken = PaymentUIUtil.getAccessToken(request);
		try {
			//			Map<String, String> requestMap = PaymentUIUtil.getRequestMap(request);
			logger.info("delete-yatra-card request received ,accessToken :" + accessToken);
			if (!qbHelper.getAgentAclDataForYatraCards(accessToken, QBConstant.ROLE_DELETE)) {
				return QBConstant.NOT_ACCESS_YATRA_CARD_JSON;
			}
			DeleteCardRequest deleteCardRequest = qbBeanFactory.getDeleteCardRequestBean(request, QBConstant.QB_TYPE_CORPORATE);
			QBService qbService = qbHelper.getQBServiceByUserRoleAndQBType(USER_ROLE_ADMIN, QBConstant.QB_TYPE_CORPORATE);
			if (qbService != null) {
				returnValue = qbService.deleteYatraCard(deleteCardRequest);
			} else {
				returnValue = NOT_ACCESS_JSON;
			}
			logger.info("delete-yatra-card API response :" + returnValue);
			return returnValue;
		} catch (Exception e) {
			logger.error("deleteCard request fail for ssoToken :" + accessToken, e);
			return FATAL_ERROR_JSON;
		}

	}

	public String deleteCard(HttpServletRequest request) {
		String returnValue = null;
		String ssoToken = PaymentUIUtil.getSSOToken(request);
		try {
			Map<String, String> requestMap = PaymentUIUtil.getRequestMap(request);
			logger.info("delete-card request received ,ssoToken :" + ssoToken + " ,request parameters : " + requestMap);
			B2BUserProfileDetailBean profileBean = (B2BUserProfileDetailBean) request.getAttribute(QBConstant.USER_PROFILE_BEAN_KEY);
			String userRole = profileBean.getCorpUserRole();
			String qbType = (String) request.getParameter(QBConstant.QB_TYPE_KEY);
			String ctype = (String) request.getParameter(C_TYPE);
			DeleteCardRequest deleteCardRequest = qbBeanFactory.getDeleteCardRequestBean(request, qbType);
			QBService qbService = qbHelper.getQBServiceByUserRoleQBTypeAndCType(userRole, qbType,ctype);
			if (qbService != null) {
				returnValue = qbService.deleteCard(deleteCardRequest, gdsService.isGDSCard(request), profileBean.getUserId());
			} else {
				returnValue = NOT_ACCESS_JSON;
			}
			logger.info("delete-card API response :" + returnValue+", role : "+userRole+", request parameters : "+requestMap);
			return returnValue;
		} catch (Exception e) {
			logger.error("deleteCard request fail for ssoToken :" + ssoToken, e);
			return FATAL_ERROR_JSON;
		}

	}

	public String saveGDSCard(HttpServletRequest request) {
		// Flow for saving an existing card to GDS.
		logger.info("saveGDSCard() initiated with request: "+request); //To be removed
		String returnValue = null;
		String ssoToken = PaymentUIUtil.getSSOToken(request);
		logger.info("ssoToken in saveGDSCard(): "+ssoToken);
		try {
			String merchant = request.getParameter(PaymentUIUtil.MERCHANT_CODE);			
			String product = request.getParameter(PaymentUIUtil.PRODUCT_CODE);
			
			Long tenantId = tenantService.getTenantId(merchant, product);
			logger.info("tenantId for ssoToken "+ssoToken+" with merchant "+merchant+" and product "+product+" is: "+tenantId);
			
			B2BUserProfileDetailBean profileBean = (B2BUserProfileDetailBean) request.getAttribute(QBConstant.USER_PROFILE_BEAN_KEY);
			logger.info("profileBean for ssoToken "+ssoToken+" is: "+profileBean); //To be removed
			
			String userRole = profileBean.getCorpUserRole();
			logger.info("userRole for ssoToken "+ssoToken+" is: "+userRole);
			
			String cardId = (String) request.getParameter(QBConstant.CARDID_KEY);
			logger.info("cardId for ssoToken "+ssoToken+" is: "+cardId); //To be removed
			
			String qbType = (String) request.getParameter(QBConstant.QB_TYPE_KEY);
			QBService qbService = qbHelper.getQBServiceByUserRoleAndQBType(userRole, qbType);
			logger.info("qbService for ssoToken "+ssoToken+" with userRole "+userRole+" and qbType "+qbType+" is: "+qbService);
			
			if (qbService != null) {
				returnValue = qbService.saveCardToGDS(ssoToken, cardId, tenantId, profileBean.getUserId());
				logger.info("Response for saveCardToGDS for ssoToken "+ssoToken+" is: "+returnValue);
				if (returnValue == null) {
					return FATAL_ERROR_JSON;
				}
			} else {
				logger.info("qbService for ssoToken "+ssoToken+" is null, "+NOT_ACCESS_JSON);
				returnValue = NOT_ACCESS_JSON;
			}
		} catch (Exception ex) {
			logger.error("save-card GDS request failed for ssoToken: " + ssoToken, ex);
			return FATAL_ERROR_JSON;
		}
		return returnValue;
	}

	public String deleteGDSCard(HttpServletRequest request) {
		String returnValue = null;
		String ssoToken = PaymentUIUtil.getSSOToken(request);
		String deletedGDSPropertyId = null;
		QBService qbService = null;
		try {
			B2BUserProfileDetailBean profileBean = (B2BUserProfileDetailBean) request.getAttribute(QBConstant.USER_PROFILE_BEAN_KEY);
			String userRole = profileBean.getCorpUserRole();
			String qbType = (String) request.getParameter(QBConstant.QB_TYPE_KEY);
			qbService = qbHelper.getQBServiceByUserRoleAndQBType(userRole, qbType);
			if (qbService != null) {
				deletedGDSPropertyId = qbService.addGDSCardProperty(ssoToken, null);
				if (deletedGDSPropertyId == null) {
					return FATAL_ERROR_JSON;
				}
				JSONObject params = new JSONObject();
				params.put("ssoToken", ssoToken);
				String response = gdsService.deleteCard(params, profileBean.getUserId());
				if (StringUtils.isEmpty(response)) {
					if (!StringUtils.isEmpty(deletedGDSPropertyId) && !"-1".equals(deletedGDSPropertyId)) {
						qbService.addGDSCardProperty(ssoToken, deletedGDSPropertyId);
					}
					return FATAL_ERROR_JSON;
				}
				return QuickBookUIUtil.getResponseJSON(true);
			}
			returnValue = NOT_ACCESS_JSON;

		} catch (Exception ex) {
			logger.error("delete-card GDS request failed for ssoToken: " + ssoToken, ex);
			if (!StringUtils.isEmpty(deletedGDSPropertyId) && qbService != null) {
				qbService.addGDSCardProperty(ssoToken, deletedGDSPropertyId);
			}
			return FATAL_ERROR_JSON;
		}
		return returnValue;
	}

	public GetEntityGroupsResponse getEntityGroups(HttpServletRequest request) {
		try {
			B2BUserProfileDetailBean profileBean = (B2BUserProfileDetailBean) request.getAttribute(QBConstant.USER_PROFILE_BEAN_KEY);
			JSONObject jsonResponseObj = corporateProfileServiceImpl.getEntityGroupsJson(profileBean.getClientId());
			jsonResponseObj.has("status");
			if (jsonResponseObj.has("responseStatus")) {
				List<String> entityGroupList = new ArrayList<>();
				JSONObject responseStatusObj = jsonResponseObj.getJSONObject("responseStatus");
				if (responseStatusObj.has("status") && responseStatusObj.getBoolean("status")) {
					JSONArray groupCodesArr = jsonResponseObj.getJSONArray("groupCodes");
					if (groupCodesArr != null) {
						for (int i = 0; i < groupCodesArr.length(); i++) {
							entityGroupList.add(groupCodesArr.getString(i));
						}
					}
				}
				return new GetEntityGroupsResponse(entityGroupList);
			} else {
				logger.error("Received failure response from the service");
				return new GetEntityGroupsResponse(QBConstant.FATAL_ERROR_MESSAGE);
			}
		} catch (Exception ex) {
			logger.error("Exception occourred while fetching Entity Groups, Exception is : ", ex);
			return new GetEntityGroupsResponse(QBConstant.FATAL_ERROR_MESSAGE);
		}
	}


	public String deleteBtaCard(HttpServletRequest request) throws Exception {
		String ssoToken = PaymentUIUtil.getSSOToken(request);
		String returnValue = null;
		try {

			boolean btaCtaFlow = btactaHelperService.isBtaCtaFlow(ssoToken);
			if (!btaCtaFlow) {
				return FATAL_ERROR_JSON;
			}

			String cardId = request.getParameter(QBConstant.CARDID_KEY);
			B2BUserProfileDetailBean profileBean = (B2BUserProfileDetailBean) request.getAttribute(QBConstant.USER_PROFILE_BEAN_KEY);
			DeleteCardRequest deleteCardRequest = qbBeanFactory.getDeleteCardRequestBean(request, QBConstant.QB_TYPE_CORPORATE);
			QBService qbService = qbHelper.getQBServiceByUserRoleAndQBType(USER_ROLE_ADMIN, QBConstant.QB_TYPE_CORPORATE);


			GetCardsRequest personalQBGetCardRequest = qbBeanFactory.getGetCardsRequestBean(request, QB_TYPE_PERSONAL);
			GetCardsRequest corpQBGetcardRequest = qbBeanFactory.getGetCardsRequestBean(request, QB_TYPE_CORPORATE);
			Future<List<?>> corpQBCardFuture = qbHelper.executeServiceGetCardInThread(corpQBserviceImpl, corpQBGetcardRequest);
			Future<List<?>> personalQBCardFuture = qbHelper.executeServiceGetCardInThread(personalQBServiceImpl, personalQBGetCardRequest);

			@SuppressWarnings("unchecked")
			List<CorporateCardInfo> corporateCards = (List<CorporateCardInfo>) corpQBCardFuture.get();
			@SuppressWarnings("unchecked")
			List<QuickBookCard> personalCards = (List<QuickBookCard>) personalQBCardFuture.get();

			CorporateCardInfo corporateCardToBeDeleted = btactaHelperService.getCorporateCardForPersonalCardInBtaCards(corporateCards, personalCards, cardId);

			if (corporateCardToBeDeleted == null){
				return "NO BTA CARD DELETION";
			}

			deleteCardRequest.setCardId(corporateCardToBeDeleted.getCardId());

			if (qbService != null) {
				returnValue = qbService.deleteCard(deleteCardRequest, gdsService.isGDSCard(request), profileBean.getUserId());
			} else {
				returnValue = NOT_ACCESS_JSON;
			}
			logger.info("delete-card API response :" + returnValue);
			return returnValue;
		} catch (Exception e) {
			logger.error("deleteCard request fail for ssoToken :" + ssoToken, e);
			return FATAL_ERROR_JSON;
		}
	}

	public String deleteBtaAndPersonalCard(HttpServletRequest request) throws Exception{

		boolean isBTACard = "true".equalsIgnoreCase(String.valueOf(request.getParameter("saveAsCorpCard")));
		String responseForCorpDelete = null;
		if(isBTACard) {
			 responseForCorpDelete = deleteBtaCard(request);
		}
		String responseForPersonalCard = deleteCard(request);

		logger.debug("Response for corpDelete : " + responseForCorpDelete + " response for personalDelete : " + responseForPersonalCard);
		return responseForPersonalCard;
	}

	public String deleteCardBifurcator(HttpServletRequest request) throws Exception {
		String ssoToken = PaymentUIUtil.getSSOToken(request);

		boolean btaCtaFlow = btactaHelperService.isBtaCtaFlow(ssoToken);



		if (!btaCtaFlow){
			return deleteCard(request);
		}else
			return deleteBtaAndPersonalCard(request);
	}


	public String saveCorpCardViaPersonalFlow(HttpServletRequest request, boolean alreadySavedinPersonalQB) {
		String returnValue = null;
		String ssoToken = PaymentUIUtil.getSSOToken(request);
		B2BUserProfileDetailBean profileBean = (B2BUserProfileDetailBean) request.getAttribute(QBConstant.USER_PROFILE_BEAN_KEY);
		try{
			SaveCardRequest saveCardRequest = qbBeanFactory.getSaveCardRequestBeanForCorporateViaPersonalFlow(request, profileBean.getUserId(), alreadySavedinPersonalQB);
			QBService qbService = qbHelper.getQBServiceByUserRoleAndQBType(USER_ROLE_ADMIN, QBConstant.QB_TYPE_CORPORATE);
			if (qbService != null) {
				returnValue = qbService.saveCorpCardViaPersonalFlow(ssoToken, saveCardRequest);
			}

			logger.info("save-corp-card-via-personal-flow API response :"+returnValue);
			return returnValue;
		}catch (Exception e) {
			logger.error("save-corp-card-via-personal-flow request fail for ssoToken :" + ssoToken, e);
			return FATAL_ERROR_JSON;
		}
	}

	public String saveCardBifurcator(HttpServletRequest request) throws Exception {
		String ssoToken = PaymentUIUtil.getSSOToken(request);

		logger.info("choosing save card using saveCardBifurcator");
		boolean isBtaCtaFlow = btactaHelperService.isBtaCtaFlow(ssoToken);
		Map<String, String> requestMap = PaymentUIUtil.getRequestMap(request);
		B2BUserProfileDetailBean profileBean = (B2BUserProfileDetailBean) request.getAttribute(QBConstant.USER_PROFILE_BEAN_KEY);
		boolean isMappedUserFlow = isMappedUserFlow(profileBean,requestMap);
		if (isBtaCtaFlow && !isMappedUserFlow){
			String saveCardResponse = saveCard(request);
			//saveCorpCardViaPersonalFlow(request);
			boolean isBTACard = "true".equalsIgnoreCase(String.valueOf(request.getParameter("saveAsCorpCard")));
				if(isBTACard)
					saveCorpCardViaPersonalFlow(request, false);
			return saveCardResponse;
		}else{
			return saveCard(request);
		}
	}
	public String saveAndTokenizeCorpCards(HttpServletRequest request) throws Exception {
		String ssoToken=PaymentUIUtil.getSSOToken(request);
		String saveCardResponse = saveCard(request);
		JSONObject saveCardJson = new JSONObject(saveCardResponse);
		String card_id = saveCardJson.get("uniqueCID").toString();
		Map<String,String> paramMap=PaymentUIUtil.getFieldMap(request);
		paramMap.put("ssoToken",ssoToken);
		paramMap.put("cardid",card_id);
		//Map<String,String> headersMap = new HashMap<String,String>();
		//headersMap.put("httpMethod","POST");

		//String response = httpUtil.invoke("http://payment.service/checkout/pay/ccAvenueCorpCardTokenize",paramMap,"payment-portal");

		return saveCardResponse;

	}


	public CorporateTokenizedCards getCorporateCardsTokenizationStatus(String corporateId)
	{
		logger.info("Entry getCorporateCardsTokenizationStatus with corporateId :" + corporateId);
		CorporateTokenizedCards cards = new CorporateTokenizedCards();
		List<CorporateTokenizedCard> list = corporateCardVaultDAO.getTokenizedCards(corporateId);
		if(list != null)
		{
			cards.setCorpTokenizedCards(list);
		}
		logger.info("Exit getCorporateCardsTokenizationStatus with corporateId :" + corporateId);
		return cards;
	}
}
