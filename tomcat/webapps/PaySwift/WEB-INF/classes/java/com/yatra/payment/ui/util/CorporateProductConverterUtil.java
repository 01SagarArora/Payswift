package com.yatra.payment.ui.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class CorporateProductConverterUtil {
	private static Map<String, String> crpProductVSCorpQBProductMap;
	private static Map<String, String> crpProductVSFalconServiceProductMap;
	
	static {
		prepareCrpProductVSCorpQBProductMap();
		prepareCrpProductVSFalconServiceProductMap();
	}

	private static void prepareCrpProductVSCorpQBProductMap() {
		crpProductVSCorpQBProductMap = new HashMap<>();
		crpProductVSCorpQBProductMap.put("crpdom", "DOM_FLIGHT");
		crpProductVSCorpQBProductMap.put("crpint", "INT_FLIGHT");
		crpProductVSCorpQBProductMap.put("crpint_hotel", "INT_HOTEL");
		crpProductVSCorpQBProductMap.put("crpdom_hotel", "DOM_HOTEL");
		crpProductVSCorpQBProductMap.put("crpinthotel", "INT_HOTEL");
		crpProductVSCorpQBProductMap.put("crpdomhotel", "DOM_HOTEL");
		crpProductVSCorpQBProductMap.put("other", "OTHER");
		crpProductVSCorpQBProductMap.put("corpSAPG", "corpSAPG");
		crpProductVSCorpQBProductMap.put("corpInsurance", "insurance");
		crpProductVSCorpQBProductMap.put("cinsweb", "insurance");
		crpProductVSCorpQBProductMap.put("corpbusdesktop", "bus");
	}

	private static void prepareCrpProductVSFalconServiceProductMap() {
		crpProductVSFalconServiceProductMap = new HashMap<>();
		crpProductVSFalconServiceProductMap.put("crpdom", "domFlight");
		crpProductVSFalconServiceProductMap.put("crpint", "intFlight");
		crpProductVSFalconServiceProductMap.put("crpint_hotel", "intHotel");
		crpProductVSFalconServiceProductMap.put("crpdom_hotel", "domHotel");
		crpProductVSFalconServiceProductMap.put("crpinthotel", "intHotel");
		crpProductVSFalconServiceProductMap.put("crpdomhotel", "domHotel");
		crpProductVSFalconServiceProductMap.put("corpbusdesktop","bus");
		//Adding for bin config service for App flow
		crpProductVSFalconServiceProductMap.put("cdomandroid", "domFlight");
		crpProductVSFalconServiceProductMap.put("cdomhotelandroid", "domHotel");
		crpProductVSFalconServiceProductMap.put("cdomhotelios", "domHotel");
		crpProductVSFalconServiceProductMap.put("cdomios", "domFlight");
		crpProductVSFalconServiceProductMap.put("cintandroid", "intFlight");
		crpProductVSFalconServiceProductMap.put("cinthotelandroid", "intHotel");
		crpProductVSFalconServiceProductMap.put("cinthotelios", "intHotel");
		crpProductVSFalconServiceProductMap.put("cintios", "intFlight");
		crpProductVSFalconServiceProductMap.put("corpInsurance", "insurance");
		crpProductVSFalconServiceProductMap.put("cinsweb", "insurance");
		crpProductVSFalconServiceProductMap.put("cinsios", "insurance");
		crpProductVSFalconServiceProductMap.put("cinsandroid", "insurance");
		crpProductVSFalconServiceProductMap.put("corpSAPG", "corpSAPG");
		crpProductVSFalconServiceProductMap.put("cbusandroid", "bus");
		crpProductVSFalconServiceProductMap.put("cbusios", "bus");
	}
	
	public static String getQBProduct(String product) {
		if(StringUtils.isEmpty(product))
			return null;
		String returnValue = crpProductVSCorpQBProductMap.get(product);
		if(StringUtils.isEmpty(returnValue))
			returnValue = "OTHER";
		return returnValue;
	}
	
	public static String getFalconProduct(String product) {
		if(StringUtils.isEmpty(product))
			return null;
		return crpProductVSFalconServiceProductMap.get(product);
	}
}
