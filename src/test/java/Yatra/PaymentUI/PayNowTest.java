package Yatra.PaymentUI;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.runners.JUnit4ClassRunner;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;

import com.yatra.payment.payswift.beans.PayNowRequest;
import com.yatra.payment.ui.exceptions.PayswiftException;
import com.yatra.payment.ui.util.PaymentUIUtil;

import junit.framework.TestCase;

@RunWith(JUnit4ClassRunner.class)
@ContextConfiguration
public class PayNowTest extends TestCase{
	
	
	@Mock
	private PayNowRequest payNowRequestSuccess;
	@Mock
	private PayNowRequest payNowRequestUPISuccess;
	@Mock
	private PayNowRequest payNowRequestNBSuccess;
	@Mock
	private PayNowRequest payNowRequestFailure;
	private static final String ERROR_MSG = "403:Invalid Input parameters";
	
	@Before
	public void setup(){
		try {
			payNowRequestSuccess = new PayNowRequest(correctRequest());
			payNowRequestUPISuccess = new PayNowRequest(correctUPIRequest());
			payNowRequestNBSuccess = new PayNowRequest(correctNBRequest());
			payNowRequestFailure = new PayNowRequest(manuplatedRequest());
			
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	  public void getException() {
	   try {
		   payNowRequestFailure.validatePayNowRequest();
	        fail("Exception not thrown");
	    } catch (PayswiftException e) {
	        assertEquals(ERROR_MSG, e.getMessage());
	    }
	  }
	  
	
	@Test
	  public void testcard() {
	    payNowRequestSuccess.validatePayNowRequest();
	    boolean flag = true;
	    assertTrue(flag);
	  } 
	@Test
	  public void getNB() {
		payNowRequestNBSuccess.validatePayNowRequest();
	    boolean flag = true;
	    assertTrue(flag);
	  } 
	@Test
	  public void testUpi() {
		payNowRequestUPISuccess.validatePayNowRequest();
	    boolean flag = true;
	    assertTrue(flag);
	  } 
	
	
	private Map<String, String> correctRequest() {
		Map<String, String> parameterMap = new HashMap<String, String>();
		String payop = "cc";
		String paymentOptionParameters = "merchant=yatra|product=dom2|payop=cc|ctype=MASTER|isCardInternational=false|saveQBCard=false|ccsc=333|cno=5419190507178203|cardholder_name=sahil|cexpm=05|cexpy=2025|address_check_rdo=Domestic|processTyperdo=Secure|tdate=2020-07-11T10:15:00|origin=MAA|destination=VNS|tripType=O|";	
		parameterMap.put(PaymentUIUtil.SUPER_PNR,"12465376_3872298");
		parameterMap.put(PaymentUIUtil.PAYMENT_OPTION,"cc");
		parameterMap.put("paymentType","card");
		parameterMap.put("uuid","8b11ed72-dbd4-4df2-99d2-882e1bb15826");
		parameterMap.put(PaymentUIUtil.PAYMENT_MODE,"nkfjn");
		if (PaymentUIUtil.isCardBasedPayop(payop)) {
			parameterMap.put(PaymentUIUtil.PAYMENT_OPTION_PARAMETERS,paymentOptionParameters);	
		}
		return parameterMap;
	}
	private Map<String, String> correctUPIRequest() {
		Map<String, String> parameterMap = new HashMap<String, String>();
		String payop = "cc";
		String paymentOptionParameters = "merchant=yatra|product=dom2|payop=upi|payerVA=8383801118@icicicom|tdate=2020-07-11T10:15:00|origin=MAA|destination=VNS|tripType=O|";	
		parameterMap.put(PaymentUIUtil.SUPER_PNR,"124653763872298");
		parameterMap.put(PaymentUIUtil.PAYMENT_OPTION,"upi");
		parameterMap.put("paymentType","card");
		parameterMap.put("uuid","8b11ed72-dbd4-4df2-99d2-882e1bb15826");
		parameterMap.put(PaymentUIUtil.PAYMENT_MODE,"nkfjn");
		if (PaymentUIUtil.isCardBasedPayop(payop)) {
			parameterMap.put(PaymentUIUtil.PAYMENT_OPTION_PARAMETERS,paymentOptionParameters);	
		}
		return parameterMap;
	}
	private Map<String, String> correctNBRequest() {
		Map<String, String> parameterMap = new HashMap<String, String>();
		String payop = "cc";
		String paymentOptionParameters = "merchant=yatra|product=dom2|payop=nb|bankCode=HDF|prBank=HDF|tdate=2020-07-11T10:15:00|origin=MAA|destination=VNS|tripType=O|";
		parameterMap.put(PaymentUIUtil.SUPER_PNR,"124653763872298");
		parameterMap.put(PaymentUIUtil.PAYMENT_OPTION,"nb");
		parameterMap.put("paymentType","card");
		parameterMap.put("uuid","8b11ed72-dbd4-4df2-99d2-882e1bb15826");
		parameterMap.put(PaymentUIUtil.PAYMENT_MODE,"nkfjn");
		if (PaymentUIUtil.isCardBasedPayop(payop)) {
			parameterMap.put(PaymentUIUtil.PAYMENT_OPTION_PARAMETERS,paymentOptionParameters);	
		}
		return parameterMap;
	}
	
	private Map<String, String> manuplatedRequest() {
		Map<String, String> parameterMap = new HashMap<String, String>();
		String payop = "cc";
		String paymentOptionParameters = "merchant=yatra|product=dom2|payop=cc|ctype=MASTER|isCardInternational=false|saveQBCard=false|ccsc=333|cno=5419190507178203|cardholder_name=select * from paymentrequestaudit|cexpm=05|cexpy=2025|address_check_rdo=Domestic|processTyperdo=Secure|tdate=2020-07-11T10:15:00|origin=MAA|destination=VNS|tripType=O|";
		parameterMap.put(PaymentUIUtil.SUPER_PNR,"(dec >>> 0).toString(2)");
		parameterMap.put(PaymentUIUtil.PAYMENT_OPTION,payop);
		parameterMap.put("paymentType","card");
		parameterMap.put("uuid","8b11ed72-dbd4-4df2-99d2-882e1bb15826");
		parameterMap.put(PaymentUIUtil.PAYMENT_MODE,"nkfjn");
		if (PaymentUIUtil.isCardBasedPayop(payop)) {
      		parameterMap.put(PaymentUIUtil.PAYMENT_OPTION_PARAMETERS,paymentOptionParameters);	
			
		}
		return parameterMap;
	}
	

}
