package com.yatra.payment.qb.corporate.factory;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.yatra.payment.payswift.beans.B2BUserProfileDetailBean;
import com.yatra.payment.qb.corporate.service.impl.PersonalQBServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yatra.express.crp.cards.v3.beans.CorporateCardInfo;
import com.yatra.payment.qb.corporate.bean.CorpQBDeleteCardRequest;
import com.yatra.payment.qb.corporate.bean.CorpQBGetCardsRequest;
import com.yatra.payment.qb.corporate.bean.CorpQBSaveCardRequest;
import com.yatra.payment.qb.corporate.bean.DeleteCardRequest;
import com.yatra.payment.qb.corporate.bean.GetCardsRequest;
import com.yatra.payment.qb.corporate.bean.PersonalQBDeleteCardRequest;
import com.yatra.payment.qb.corporate.bean.PersonalQBGetCardsRequest;
import com.yatra.payment.qb.corporate.bean.PersonalQBSaveCardRequest;
import com.yatra.payment.qb.corporate.bean.SaveCardRequest;
import com.yatra.payment.qb.corporate.constant.QBConstant;
import com.yatra.payment.ui.dao.YatraPropertiesDAO;
import com.yatra.payment.ui.service.CorporateCardsUIService;
import com.yatra.payment.ui.service.QuickBookSaveCardService;
import com.yatra.payment.ui.service.TenantService;
import com.yatra.payment.ui.util.CorporateQuickBookUIUtil;
import com.yatra.payment.ui.util.PaymentUIUtil;
import com.yatra.payment.ui.util.QuickBookUIUtil;
import com.yatra.platform.util.YatraUtil;
import com.yatra.express.cards.v3.beans.UserCardInfo;

import static com.yatra.payment.qb.corporate.constant.QBConstant.USER_ROLE_EA;

@Component
public class QBBeanFactory {
	
	@Autowired private TenantService tenantService;
	@Autowired private CorporateCardsUIService corporateCardsUIService;
	@Autowired private QuickBookSaveCardService quickBookSaveCardService;
	@Autowired private PersonalQBServiceImpl personalQBServiceImpl;
	@Autowired private YatraPropertiesDAO yatraPropertiesDAO;
	
	public DeleteCardRequest getDeleteCardRequestBean(HttpServletRequest request , String qbType){
		
		String ssoToken = PaymentUIUtil.getSSOToken(request);
		String cardId = request.getParameter(QBConstant.CARDID_KEY);
		DeleteCardRequest bean = null;
		if(QBConstant.QB_TYPE_CORPORATE.equals(qbType)){
			bean = new CorpQBDeleteCardRequest();
		}else{
			bean = new PersonalQBDeleteCardRequest();
			String merchant = request.getParameter(PaymentUIUtil.MERCHANT_CODE);
			String product = request.getParameter(PaymentUIUtil.PRODUCT_CODE);
			Long tenantId = tenantService.getTenantId(merchant, product);
			B2BUserProfileDetailBean profileBean = (B2BUserProfileDetailBean) request.getAttribute(QBConstant.USER_PROFILE_BEAN_KEY);
			((PersonalQBDeleteCardRequest)bean).setMerchant(merchant);
			((PersonalQBDeleteCardRequest)bean).setProduct(product);
			((PersonalQBDeleteCardRequest)bean).setTenantId(tenantId);
			if(isMappedUserFlow(profileBean,request)){
				alterDeleteCardRequestForMappedUserFlow(request,bean);
			}
		}		
		bean.setSsoToken(ssoToken);		
		bean.setCardId(cardId);
		return bean;
	}

	public SaveCardRequest getSaveCardRequestBean(HttpServletRequest request , String qbType, String userId){
		String ssoToken = PaymentUIUtil.getSSOToken(request);
		SaveCardRequest bean = null;
		Map<String,String> requestMap = PaymentUIUtil.getRequestMap(request);
		if(QBConstant.QB_TYPE_CORPORATE.equals(qbType)){
			bean = new CorpQBSaveCardRequest();
			if(!YatraUtil.isNullOrEmpty(userId))
				requestMap.put("usId", userId);
			CorporateCardInfo corporateCardInfo = corporateCardsUIService.getCorporateCardInfo(requestMap);
			((CorpQBSaveCardRequest)bean).setCorporateCardInfo(corporateCardInfo);
			((CorpQBSaveCardRequest)bean).setTarget(CorporateQuickBookUIUtil.getAddCardTarget(requestMap.get(QuickBookUIUtil.TARGET)));
		}else{
			String merchant = request.getParameter(PaymentUIUtil.MERCHANT_CODE);
			String product = request.getParameter(PaymentUIUtil.PRODUCT_CODE);
			Long tenantId = tenantService.getTenantId(merchant, product);
			B2BUserProfileDetailBean profileBean = (B2BUserProfileDetailBean) request.getAttribute(QBConstant.USER_PROFILE_BEAN_KEY);
			UserCardInfo userCardInfo = quickBookSaveCardService.getUserCardInfo(requestMap);
			bean = new PersonalQBSaveCardRequest();
			((PersonalQBSaveCardRequest)bean).setMerchant(merchant);
			((PersonalQBSaveCardRequest)bean).setProduct(product);
			((PersonalQBSaveCardRequest)bean).setTenantId(tenantId);
			((PersonalQBSaveCardRequest)bean).setUserCardInfo(userCardInfo);
			if(isMappedUserFlow(profileBean,request)){
				alterSaveCardRequestForMappedUserFlow(request,bean);
			}

		}
		bean.setSsoToken(ssoToken);
		return bean;
	}


	public SaveCardRequest getSaveCardRequestBeanForCorporateViaPersonalFlow(HttpServletRequest request, String userId, boolean alreadySavedinPersonalQB) {
		String ssoToken = PaymentUIUtil.getSSOToken(request);
		SaveCardRequest bean = null;
		Map<String,String> requestMap = PaymentUIUtil.getRequestMap(request);
		Map<String,String> parameterMap = new HashMap<String, String>();
		if(alreadySavedinPersonalQB)
			parameterMap = personalQBServiceImpl.createParameterMapUsingRequestMap(ssoToken, requestMap);
		else parameterMap = requestMap;
		bean = new CorpQBSaveCardRequest();
		if(!YatraUtil.isNullOrEmpty(userId))
			parameterMap.put("usId", userId);
		CorporateCardInfo corporateCardInfo = corporateCardsUIService.getCorporateCardInfo(parameterMap);
		((CorpQBSaveCardRequest)bean).setCorporateCardInfo(corporateCardInfo);
		((CorpQBSaveCardRequest)bean).setTarget(CorporateQuickBookUIUtil.getAddCardTarget(requestMap.get(QuickBookUIUtil.TARGET)));
		bean.setSsoToken(ssoToken);	
		return bean;
	}
	
	

	public GetCardsRequest getGetCardsRequestBean(HttpServletRequest request , String qbType){
		String ssoToken = PaymentUIUtil.getSSOToken(request);
		GetCardsRequest bean = null;
		if(QBConstant.QB_TYPE_CORPORATE.equals(qbType)){
			bean = new CorpQBGetCardsRequest();
		}else{
			bean = new PersonalQBGetCardsRequest();
			String merchant = request.getParameter(PaymentUIUtil.MERCHANT_CODE);
			String product = request.getParameter(PaymentUIUtil.PRODUCT_CODE);
			Long tenantId = tenantService.getTenantId(merchant, product);

			B2BUserProfileDetailBean profileBean = (B2BUserProfileDetailBean) request.getAttribute(QBConstant.USER_PROFILE_BEAN_KEY);

			((PersonalQBGetCardsRequest)bean).setMerchant(merchant);
			((PersonalQBGetCardsRequest)bean).setProduct(product);
			((PersonalQBGetCardsRequest)bean).setTenantId(tenantId);

			if(isMappedUserFlow(profileBean,request)){
				alterGetCardRequestForMappedUserFlow(request,bean);
			}
		}		
		bean.setSsoToken(ssoToken);		
		return bean;
	}
	public void alterGetCardRequestForMappedUserFlow(HttpServletRequest request , GetCardsRequest bean){

		Long targerUserId = 0L;
		if(!StringUtils.isBlank(request.getParameter(PaymentUIUtil.TARGET_USER_ID))){
			targerUserId = Long.valueOf(request.getParameter(PaymentUIUtil.TARGET_USER_ID));
		}
		/* setting default bookingType for fetching cards for display */
		String bookingType = "ALL";

		((PersonalQBGetCardsRequest)bean).setSsoUserId(targerUserId);
		((PersonalQBGetCardsRequest)bean).setBookingType(bookingType);
	}

	public void alterSaveCardRequestForMappedUserFlow(HttpServletRequest request , SaveCardRequest bean){

		Long targerUserId = 0L;
		if(!StringUtils.isBlank(request.getParameter(PaymentUIUtil.TARGET_USER_ID))){
			targerUserId = Long.valueOf(request.getParameter(PaymentUIUtil.TARGET_USER_ID));
		}
		/* setting default bookingType for fetching cards for display */
		String bookingType = "ALL";

		((PersonalQBSaveCardRequest)bean).setSsoUserId(targerUserId);

	}
	public void alterDeleteCardRequestForMappedUserFlow(HttpServletRequest request , DeleteCardRequest bean){

		Long targerUserId = 0L;
		if(!StringUtils.isBlank(request.getParameter(PaymentUIUtil.TARGET_USER_ID))){
			targerUserId = Long.valueOf(request.getParameter(PaymentUIUtil.TARGET_USER_ID));
		}

		((DeleteCardRequest)bean).setSsoUserId(targerUserId);

	}

	public Boolean isMappedUserFlow(B2BUserProfileDetailBean userProfileDetailBean , HttpServletRequest request){
		boolean isMappedUserFlow = false;
		String role = userProfileDetailBean.getCorpUserRole();
		String ssoUserId = userProfileDetailBean.getUserId();
		String targerUserId = request.getParameter(PaymentUIUtil.TARGET_USER_ID);
		if (role.equalsIgnoreCase(USER_ROLE_EA)
				&& !StringUtils.isBlank(targerUserId)
				&& !StringUtils.equalsIgnoreCase(ssoUserId, targerUserId)) {
			isMappedUserFlow = true;
		}
		return isMappedUserFlow;

	}
	
}
