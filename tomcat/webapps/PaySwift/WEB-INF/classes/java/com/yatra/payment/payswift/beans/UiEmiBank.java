package com.yatra.payment.payswift.beans;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yatra.payment.ui.dao.PaymentUIGenericDAO;
import com.yatra.payment.ui.dao.bean.UIEmiBanks;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UiEmiBank {

    private static final Logger logger = Logger.getLogger(UiEmiBank.class);
    private String code;
    private String tenure;
    private int min;
    private int max;

    @Autowired
    private PaymentUIGenericDAO<UIEmiBanks> emiBanksDAO;

    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public String getTenure() {
        return tenure;
    }
    public void setTenure(String tenure) {
        this.tenure = tenure;
    }
    public int getMin() {
        return min;
    }
    public void setMin(int min) {
        this.min = min;
    }
    public int getMax() {
        return max;
    }
    public void setMax(int max) {
        this.max = max;
    }

    public  String fetchDatafromUiEmiBankTable() {
       //  Map structure bank --> tenure --> (min amount ,max amount)
        Map<String, Map> pgMap = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        String emiEligibilityJson = "";
        try{
            List<UIEmiBanks> emiBanks = emiBanksDAO.getAll();
            for(UIEmiBanks emiBank : emiBanks ){
                UiEmiBank bank = new UiEmiBank();
                bank.setMin(Integer.valueOf(emiBank.getMinAmount()));
                bank.setMax(Integer.valueOf(emiBank.getMaxAmount()));
                bank.setCode(emiBank.getCode());
                bank.setTenure(emiBank.getTenure());
                updateMap(pgMap,bank);
                emiEligibilityJson = objectMapper.writeValueAsString(pgMap);
            }
        }catch (Exception e){
            logger.info("Exception occured while creating emiEligibility json from Ui emi banks table",e);
        }
        return emiEligibilityJson;
    }

    private void updateMap(Map<String,Map> pgMap, UiEmiBank bank)
    {
        Map<String,Integer> minMaxMap = new HashMap<>();
        minMaxMap.put("min", Integer.valueOf(bank.getMin()));
        minMaxMap.put("max", Integer.valueOf(bank.getMax()));

        Map<String,Map> tenureMap =  pgMap.get(bank.getCode());
        if(tenureMap == null)
        {
            tenureMap = new HashMap<>();
            pgMap.put(bank.getCode(), tenureMap);
        }
        tenureMap.put(bank.getTenure(), minMaxMap);
    }
}
