package com.yatra.payment.ui.beans.convertor.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.yatra.payment.ui.beans.convertor.BeanConvertor;
import com.yatra.payment.ui.dao.bean.UIEmiBanks;
import com.yatra.payment.ui.dao.bean.UINoCostEmiBanks;
import com.yatra.payment.ui.display.beans.CardType;
import com.yatra.payment.ui.display.beans.EMIBank;
import com.yatra.payment.ui.display.beans.EmiType;
import com.yatra.payment.ui.display.beans.NoCostEmiBank;
import com.yatra.payment.ui.display.beans.NoCostEmiProduct;
import com.yatra.payment.ui.enums.Status;
import java.util.Iterator;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Service("noCostEmiBankConvertor")
public class NoCostEMIBankConvertor implements BeanConvertor<UINoCostEmiBanks, NoCostEmiProduct> {

        private static Logger logger = Logger.getLogger(NoCostEMIBankConvertor.class);
	@Override
	public NoCostEmiProduct convert(UINoCostEmiBanks input) {
                NoCostEmiProduct product = new NoCostEmiProduct();
                product.setIsPromoAllowed(input.getIsPromoAllowed() == 1);
                List<NoCostEmiBank> noCostBanks = new ArrayList<>();
                
                boolean flag = false;
                try {
                    JSONObject banks = new JSONObject(input.getBankConfigurations());
                    Iterator<?> keys = banks.keys();
                    
                    while(keys.hasNext() ) {
                        String key = (String)keys.next();
                        NoCostEmiBank bank = new NoCostEmiBank();
                        bank.setBank(key);
                        List<EmiType> types = new ArrayList<>();
                        boolean bankFlag = false;
                        JSONArray array = banks.getJSONArray(key);
                        for (int i=0;i<array.length();i++) {
                            Integer value = array.getInt(i);
                            if (value == 1) {
                                bankFlag = true;
                                flag = true;
                                EmiType type = null;
                                switch (i) {
                                    case 0:
                                        type = new EmiType();
                                        type.setEmiTenure("3");
                                        type.setInterestRate("0");
                                        types.add(type);
                                        break;
                                    case 1:
                                        type = new EmiType();
                                        type.setEmiTenure("6");
                                        type.setInterestRate("0");
                                        types.add(type);
                                        break;
                                    case 2:
                                        type = new EmiType();
                                        type.setEmiTenure("9");
                                        type.setInterestRate("0");
                                        types.add(type);
                                        break;
                                    case 3:
                                        type = new EmiType();
                                        type.setEmiTenure("12");
                                        type.setInterestRate("0");
                                        types.add(type);
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }
                        if (!types.isEmpty()) {
                            bank.setEmiTypes(types);
                            noCostBanks.add(bank);
                        }
                    }
                }catch (Exception ex) {
                    logger.error("Error while converting no cost emi bean: " + ex.getMessage());
                }
                if (flag == false) {
                    return null;
                }
                product.setEmiBanks(noCostBanks);
                return product;
	}

}
