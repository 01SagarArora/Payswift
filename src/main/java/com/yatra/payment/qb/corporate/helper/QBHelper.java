package com.yatra.payment.qb.corporate.helper;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import com.yatra.payment.qb.corporate.service.impl.PersonalQBServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import com.yatra.payment.payswift.beans.B2BUserProfileDetailBean;
import com.yatra.payment.qb.corporate.bean.GetCardsRequest;
import com.yatra.payment.qb.corporate.constant.QBConstant;
import com.yatra.payment.qb.corporate.service.QBService;
import com.yatra.payment.qb.corporate.service.impl.CorpQBserviceImpl;
import com.yatra.payment.ui.service.AgentProfileService;
import org.json.JSONArray;
import org.json.JSONObject;

import static com.yatra.payment.qb.corporate.constant.QBConstant.*;

@Component
public class QBHelper {

	@Autowired
	private CorpQBserviceImpl corpQBserviceImpl;
	@Autowired
	private PersonalQBServiceImpl personalQBServiceImpl;
	@Autowired
	private AgentProfileService agentProfileService;
	@Autowired 
	ThreadPoolTaskExecutor threadPoolTaskExecutor;

	private Logger logger = Logger.getLogger(QBHelper.class);
	
	public QBService getQBServiceByUserRoleAndQBType(String userRole, String qbType) {
		
		if (USER_ROLE_ADMIN.equals(userRole) && QB_TYPE_PERSONAL.equals(qbType)) {
			return personalQBServiceImpl;
		} else if (USER_ROLE_ADMIN.equals(userRole) && QB_TYPE_CORPORATE.equals(qbType)) {
			return corpQBserviceImpl;
		}  else if (!USER_ROLE_EMPTY.equals(userRole) && !QBConstant.USER_ROLE_NULL.equals(userRole) && QB_TYPE_PERSONAL.equals(qbType)) {
			return personalQBServiceImpl;
		} else {
			return null;
		}
	}
	public QBService getQBServiceByUserRoleQBTypeAndCType(String userRole, String qbType,String ctype) {

		if (USER_ROLE_ADMIN.equals(userRole) && QB_TYPE_PERSONAL.equals(qbType)) {
			return personalQBServiceImpl;
		}
		else if (USER_ROLE_ADMIN.equals(userRole) && QB_TYPE_CORPORATE.equals(qbType)) {
			return corpQBserviceImpl;
		}
		else if (QB_TYPE_CORPORATE.equals(qbType) && "PCC".equals(ctype)) {
			return corpQBserviceImpl;
		}
		else if (!USER_ROLE_EMPTY.equals(userRole) && !QBConstant.USER_ROLE_NULL.equals(userRole) && QB_TYPE_PERSONAL.equals(qbType)) {
			return personalQBServiceImpl;
		} else {
			return null;
		}
	}
	
	public Future<List<?>> executeServiceGetCardInThread(final QBService qbService,final GetCardsRequest getCardsRequest){
		return threadPoolTaskExecutor.submit(new Callable<List<?>>() {
			@Override
			public List<?> call() throws Exception {
				return qbService.getCards(getCardsRequest);
			}
		});
	}
	
    public B2BUserProfileDetailBean getAgentProfileDataBeanCorp(String ssoToken, String merchant, String product){
		B2BUserProfileDetailBean userProfileDetail = null;
		try {
			userProfileDetail = agentProfileService.getUserProfileDetailCorp(ssoToken, merchant, product);
		} catch (Exception ex){
			logger.error("Error wile retrieving userProfileDetail for ssoToken : " + ssoToken, ex);
		}
		return userProfileDetail;
	}
    
	public B2BUserProfileDetailBean getAgentProfileDataBean(String ssoToken){
		B2BUserProfileDetailBean userProfileDetail = null;
		try {
			userProfileDetail = agentProfileService.getUserProfileDetail(ssoToken);
		} catch (Exception ex){
			logger.error("Error wile retrieving userProfileDetail for ssoToken : " + ssoToken, ex);
		}
		return userProfileDetail;
	}
	
    public boolean isUserAuthorizedCorp(B2BUserProfileDetailBean userProfileData){
		if(userProfileData == null)
			return false;
		
		String userRole = userProfileData.getCorpUserRole();
		if(StringUtils.isEmpty(userRole))
			return false;
		return true;
	}
    
	public boolean isUserAuthorized(B2BUserProfileDetailBean userProfileData){
		if(userProfileData == null)
			return false;
		
		String userRole = userProfileData.getUserRole();
		if("null".equalsIgnoreCase(userRole) || StringUtils.isEmpty(userRole))
			return false;
		return true;
	}
	
    public void addAuthorizationRelatedDataInModelCorp(ModelAndView modelAndView, String ssoToken, String merchant, String product){
		B2BUserProfileDetailBean profileData = getAgentProfileDataBeanCorp(ssoToken, merchant, product);
		boolean isUserAuthorized = isUserAuthorizedCorp(profileData);
		modelAndView.addObject(IS_USER_AUTHORIZED, isUserAuthorized);
		if(!isUserAuthorized)	return;
		String clientId = profileData.getClientId();
		String corpUserId = profileData.getUserId();
		String corpUserRole = profileData.getUserRole();
		modelAndView.addObject(CLIENT_ID, clientId);
		modelAndView.addObject(CORP_USER_ID, corpUserId);
		modelAndView.addObject(CORP_USER_ROLE,profileData.getCorpUserRole());
		if(isUserAuthorized) {
			String userRole = profileData.getCorpUserRole();
			if(USER_ROLE_ADMIN.equals(userRole)){
				modelAndView.addObject(SHOW_CORPORATE_CARDS_TAB, "true");
				modelAndView.addObject(SHOW_PERSONAL_CARDS_TAB, "true");
			} else {
				modelAndView.addObject(SHOW_CORPORATE_CARDS_TAB, "false");
				modelAndView.addObject(SHOW_PERSONAL_CARDS_TAB, "true");
			}
		}
	}
    
        public Boolean getAgentAclDataForYatraCards(String accessToken, String accessRole){
            try {
                String response = agentProfileService.getAclDetailsForYatraCards(accessToken);
                if (StringUtils.isEmpty(response)) {
                    return false;
                }
                JSONObject jsonObject = new JSONObject(response);
                JSONArray array = jsonObject.getJSONArray("roles");
                if (array.length() == 0) {
                    return false;
                }
                for (int i=0;i<array.length();i++) {
                    String value = array.getString(i);
                    if (accessRole.equalsIgnoreCase(value)) {
                        return true;
                    }
                }
            } catch (Exception ex){
                logger.error("Error wile retrieving userProfileDetail for accessToken : " + accessToken, ex);
            }
            return false;
	}
        
	public void addAuthorizationRelatedDataInModel(ModelAndView modelAndView, String ssoToken){
		B2BUserProfileDetailBean profileData = getAgentProfileDataBean(ssoToken);
		boolean isUserAuthorized = isUserAuthorized(profileData);
		modelAndView.addObject(IS_USER_AUTHORIZED, isUserAuthorized);
		if(isUserAuthorized) {
			String userRole = profileData.getUserRole();
			if(USER_ROLE_ADMIN.equals(userRole)){
				modelAndView.addObject(SHOW_CORPORATE_CARDS_TAB, "true");
				modelAndView.addObject(SHOW_PERSONAL_CARDS_TAB, "true");
			} else {
				modelAndView.addObject(SHOW_CORPORATE_CARDS_TAB, "false");
				modelAndView.addObject(SHOW_PERSONAL_CARDS_TAB, "true");
			}
		}
	}
}
