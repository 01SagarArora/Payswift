package com.yatra.payment.ui.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yatra.payment.ui.service.MiniServiceI;
import com.yatra.payment.ui.service.impl.AgentSpecificPaymentOptionImpl;
import com.yatra.payment.ui.service.impl.BookingAmountServiceImpl;
import com.yatra.payment.ui.service.impl.CheckBalanceServiceImpl;
import com.yatra.payment.ui.service.impl.CorporateBinsConfigServiceImpl;
import com.yatra.payment.ui.service.impl.CorporateConfigurationServiceImpl;
import com.yatra.payment.ui.service.impl.CorporateQuickbookServiceImpl;
import com.yatra.payment.ui.service.impl.CurrencyConversionServiceImpl;
import com.yatra.payment.ui.service.impl.ECashJsonServiceImpl;
import com.yatra.payment.ui.service.impl.PassThroughServiceImpl;
import com.yatra.payment.ui.service.impl.QuickbookServiceImpl;
import com.yatra.payment.ui.service.impl.QuickbookExpressServiceImpl;
import com.yatra.payment.ui.service.impl.SurchargeServiceImpl;
import com.yatra.payment.ui.service.impl.FetchItineraryDetailsServiceImpl;

/*
 * Purpose of this class is to hold service references based on parameter keywords .
 */
@Component
public class MiniServiceFactory {
	private static HashMap<String,MiniServiceI> serviceRepository = new HashMap();

	@Autowired QuickbookServiceImpl quickbookServiceImpl;

	@Autowired QuickbookExpressServiceImpl quickbookExpressServiceImpl;
	@Autowired CorporateQuickbookServiceImpl corporateQuickbookServiceImpl;
	@Autowired CheckBalanceServiceImpl checkBalanceServiceImpl;
	@Autowired PassThroughServiceImpl passThroughServiceImpl;
	@Autowired AgentSpecificPaymentOptionImpl agentSpecificPaymentOptionImpl;
	@Autowired SurchargeServiceImpl surchargeServiceImpl;
	@Autowired BookingAmountServiceImpl bookingAmountServiceImpl;
	@Autowired FetchItineraryDetailsServiceImpl fetchItineraryDetailsServiceImpl;
	@Autowired CorporateConfigurationServiceImpl corporateConfigurationServiceImpl;
	@Autowired ECashJsonServiceImpl eCashJsonServiceImpl;
	@Autowired CorporateBinsConfigServiceImpl corporateBinsConfigServiceImpl;
	@Autowired CurrencyConversionServiceImpl currencyConversionServiceImpl;

	@PostConstruct
	public void populateRepository(){
		serviceRepository.put(quickbookServiceImpl.getResultKey(), quickbookServiceImpl);
		serviceRepository.put(corporateQuickbookServiceImpl.getResultKey(), corporateQuickbookServiceImpl);
		serviceRepository.put(checkBalanceServiceImpl.getResultKey(), checkBalanceServiceImpl);
		serviceRepository.put(passThroughServiceImpl.getResultKey(), passThroughServiceImpl);
		serviceRepository.put(agentSpecificPaymentOptionImpl.getResultKey(), agentSpecificPaymentOptionImpl);
		serviceRepository.put(surchargeServiceImpl.getResultKey(), surchargeServiceImpl);
		serviceRepository.put(bookingAmountServiceImpl.getResultKey(), bookingAmountServiceImpl);
		serviceRepository.put(fetchItineraryDetailsServiceImpl.getResultKey(), fetchItineraryDetailsServiceImpl);
		serviceRepository.put(corporateConfigurationServiceImpl.getResultKey(), corporateConfigurationServiceImpl);
		serviceRepository.put(eCashJsonServiceImpl.getResultKey(), eCashJsonServiceImpl);
		serviceRepository.put(corporateConfigurationServiceImpl.getResultKey(), corporateConfigurationServiceImpl);
		serviceRepository.put(corporateBinsConfigServiceImpl.getResultKey(), corporateBinsConfigServiceImpl);
		serviceRepository.put(currencyConversionServiceImpl.getResultKey(), currencyConversionServiceImpl);
	}

	public List<MiniServiceI> getRequestedServices(JSONArray requestedServices,String product) throws JSONException{

		List<MiniServiceI> resultList = new ArrayList(0);

		for(int i = 0 ; i < requestedServices.length() ; i++){
			resultList.add(serviceRepository.get(requestedServices.get(i)));
		}
		//updating requested service in case of corporate  to fetch cards from express checkout service
		if(!StringUtils.isEmpty(product) && PaymentUIUtil.checkIfCorporateProduct(product) && resultList.contains(quickbookServiceImpl)){
			resultList.remove(quickbookServiceImpl);
			resultList.add(quickbookExpressServiceImpl);
		}
		return resultList;
	}

}
