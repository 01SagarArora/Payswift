package com.yatra.payment.client.service;


import com.yatra.payment.ui.dao.impl.CorporateCardTokenDaoImpl;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service("corporateCardTokenManager")
public class CorporateCardTokenManager {
    @Autowired
    CorporateCardTokenDaoImpl corporateCardTokenDao;
    private Logger logger = Logger.getLogger(WalletService.class);


    public Map<String,Object> getCorpCardTokenInfo(String cardid){
        Map<String,Object> resultMap = null ;
        return  corporateCardTokenDao.getCorpCardTokenInfo(cardid);
    }

}
