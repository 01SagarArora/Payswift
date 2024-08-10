package com.yatra.payment.ui.payswift.service;

import static com.yatra.payment.client.utils.PaymentConstants.MULTI_PAY_OPTION_ATM;
import static com.yatra.payment.client.utils.PaymentConstants.MULTI_PAY_OPTION_DEFAULT_SECURE;
import static com.yatra.payment.client.utils.PaymentConstants.MULTI_PAY_OPTION_OTP;
import static java.util.Arrays.asList;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.client.utils.PaymentConstants;
import com.yatra.payment.ui.beans.BinDetails;
import com.yatra.payment.ui.controller.PaymentUIController;
import com.yatra.payment.ui.dao.ApiInfoDAO;
import com.yatra.payment.ui.dao.BinDetailsDAO;
import com.yatra.payment.ui.dao.ProductMasterDAO;
import com.yatra.payment.ui.dao.YatraPropertiesDAO;
import com.yatra.payment.ui.service.MultiPayProcessService;
import com.yatra.payment.ui.util.HttpUtil;
import com.yatra.payment.ui.util.PaymentUIUtil;


@Service
public class BinDetailsService {

    Logger logger = Logger.getLogger(BinDetailsService.class);

    @Autowired
    private BinDetailsDAO binDetailsDAO;
    @Autowired
    private ProductMasterDAO productMasterDAO;
    @Autowired
    private YatraPropertiesDAO yatraPropertiesDAO;
    @Autowired
    private MultiPayProcessService multiPayProcessService;
    @Autowired private ApiInfoDAO apiInfoDAO;
    @Autowired
   	private PaymentUIController paymentUIController;
    
    
    public void populateBinDetails(JSONObject resultJson, String bin, String product, String passThrough, String superPnr) throws Exception {
        boolean isGDSHotel = isProductGDSHotel(product, passThrough);

        /** CHECKING THE BANK DOWN STATUS WETHER IT'S TRUE OR FALSE**/
        
		String bankStatus = "true";
		resultJson.put("status", bankStatus);
        
        Map<String, Object> multiPayFlowBinInfo = multiPayProcessService.getmultiPayFlowBinInfo(bin, product);

        resultJson.put("isMultiPayFlowEnabled", multiPayFlowBinInfo.get("isMultiPayFlowEnabled"));
        Optional<BinDetails> binInfo = (Optional<BinDetails>)multiPayFlowBinInfo.get("binDetails");

        if (!binInfo.isPresent()) {
            resultJson.put("isCardInternational", "true");
            resultJson.put("skipOtpForBin", isGDSHotel);
            resultJson.put("multiPayFlow", MULTI_PAY_OPTION_DEFAULT_SECURE);
            resultJson.put("bankName", "International Bank");
            return;
        }

        BinDetails binDetails = binInfo.get();

        resultJson.put("isCardInternational", String.valueOf(binDetails.isCardInternational()));
        resultJson.put("skipOtpForBin", getOverAllSkipOtpForBinFlag(isGDSHotel, binDetails, product, superPnr));
        Map<String,Object> multiPayDetailsMap = getMultiPayDetailsMap(binDetails);
        resultJson.put("multiPayFlow", multiPayDetailsMap.get("multiPayFlow"));
        resultJson.put("multiPayLabel", multiPayDetailsMap.get("multiPayLabel"));
        resultJson.put("bankName", binDetails.getBankName());
        resultJson.put("binType", binDetails.getBinType());
    }

    private Map<String, Object> getMultiPayDetailsMap(BinDetails binDetails) throws JSONException {
    	Map<String,Object> resultMap = new HashMap<String, Object>();
    	JSONObject label = new JSONObject();
        label.put(MULTI_PAY_OPTION_ATM, "ATM Pin");
        label.put(MULTI_PAY_OPTION_OTP, "OTP");
        label.put(MULTI_PAY_OPTION_DEFAULT_SECURE, "Secure Password");
        
    	ArrayList<String> multiPayOptionsList = new ArrayList<String>(asList(binDetails.getMultiPayFlowSequence().split(",")));
        if (multiPayOptionsList.contains(MULTI_PAY_OPTION_ATM) && (!binDetails.isAtmSupportedOnBank() || !binDetails.isAtmSupportedOnBin())) {
            multiPayOptionsList.remove(MULTI_PAY_OPTION_ATM);
            label.remove(MULTI_PAY_OPTION_ATM);
        }
        if (multiPayOptionsList.contains(MULTI_PAY_OPTION_OTP) && !binDetails.isOtpSupportedOnBank()) {
            multiPayOptionsList.remove(MULTI_PAY_OPTION_OTP);
            label.remove(MULTI_PAY_OPTION_OTP);
        }
        
        if(label.length() == 2 && !label.has(MULTI_PAY_OPTION_OTP))
        	label.put(MULTI_PAY_OPTION_DEFAULT_SECURE, "Secure/OTP");
        
        resultMap.put("multiPayFlow", StringUtils.join(multiPayOptionsList, PaymentConstants.SEPARATOR_PIPE));
        resultMap.put("multiPayLabel", label);
        
		return resultMap;
	}

	private boolean isProductGDSHotel(String product, String passThrough) {
        if ("true".equalsIgnoreCase(passThrough)) {
            if (PaymentUIUtil.PRODUCT_CORP_APP_DOM_HOTEL_ANDROID.equalsIgnoreCase(product)
                    || PaymentUIUtil.PRODUCT_CORP_APP_INT_HOTEL_ANDROID.equalsIgnoreCase(product)
                    || PaymentUIUtil.PRODUCT_CORP_APP_DOM_HOTEL_IOS.equalsIgnoreCase(product)
                    || PaymentUIUtil.PRODUCT_CORP_APP_INT_HOTEL_IOS.equalsIgnoreCase(product))
                return true;
        }
        return false;
    }

	private boolean getOverAllSkipOtpForBinFlag(boolean isGDSHotel, BinDetails binDetails, String product, String superPnr) {
		if(paymentUIController.isCrpProduct(product)) {
			ArrayList<String> corporateBinsList = getCorporateBins(superPnr);
			String binNumber = binDetails.getBinNumber();
			if(binNumber.length() > 6)
			{
				binNumber = binNumber.substring(0,6);
			}
			
			if(corporateBinsList.size() > 0 && corporateBinsList.contains(binNumber))
				binDetails.setIsCorporateSupported(true);
			else binDetails.setIsCorporateSupported(false);
		}
		else binDetails.setIsCorporateSupported(false);
		if (isGDSHotel || binDetails.isCorporateSupported()) {
			return true;
		}
		return false;
	}

    private ArrayList<String> getCorporateBins(String superPnr) {
    	ArrayList<String> binList = new ArrayList<String>();
    	try {
    		JSONObject corporateBinsForSuperPnr = new JSONObject(apiInfoDAO.getSkipOtpBinsForSuperPnr(superPnr));
    		if(corporateBinsForSuperPnr!=null && corporateBinsForSuperPnr.getJSONArray("binsForSkipOtp").length() > 0 ) {
    			JSONArray binsForSkipOtp = corporateBinsForSuperPnr.getJSONArray("binsForSkipOtp");
    			for(int i = 0; i<binsForSkipOtp.length(); i++) {
    				binList.add(binsForSkipOtp.getString(i));
    			}
    		}
    	}
    	catch(Exception e) {
    		logger.error("Exception while coverting skipOtpBins resppnse to ArrayList binList ", e);
    	}
    	return binList;
    }
    
    public static String sendPost(String postData,String urlStr) throws IOException    {
    	
		URL url = new URL(urlStr);
		URLConnection conn = url.openConnection();
		conn.setDoOutput(true);
		conn.setConnectTimeout(2*1000);
		conn.setReadTimeout(2*1000);
		BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
		wr.write(postData);
		wr.flush();

		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line;
		String response = "";
		while ((line = rd.readLine()) != null) {
			response += line;
		}
		return response;
		
	}

    /*private String getMultiPayFlowOptionsForBin(BinDetails binDetails) throws Exception {
        ArrayList<String> multiPayOptionsList = new ArrayList<String>(asList(binDetails.getMultiPayFlowSequence().split(",")));
        if (multiPayOptionsList.contains(MULTI_PAY_OPTION_ATM) && (!binDetails.isAtmSupportedOnBank() || !binDetails.isAtmSupportedOnBin())) {
            multiPayOptionsList.remove(MULTI_PAY_OPTION_ATM);
        }
        if (multiPayOptionsList.contains(MULTI_PAY_OPTION_OTP) && !binDetails.isOtpSupportedOnBank()) {
            multiPayOptionsList.remove(MULTI_PAY_OPTION_OTP);
        }
        return StringUtils.join(multiPayOptionsList, PaymentConstants.SEPARATOR_PIPE);
    }*/
}
