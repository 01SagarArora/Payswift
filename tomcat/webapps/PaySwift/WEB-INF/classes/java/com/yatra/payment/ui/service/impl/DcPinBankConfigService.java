package com.yatra.payment.ui.service.impl;

import com.yatra.payment.ui.dao.bean.DcPinBankConfig;
import com.yatra.payment.ui.dao.impl.DcPinBankVsPgDAOImpl;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("dcPinBankConfigService")
public class DcPinBankConfigService {
    private static Logger logger = Logger.getLogger(DcPinBankConfigService.class);

    @Autowired
    private DcPinBankVsPgDAOImpl dcPinBankVsPgDAOImpl;

    public List<DcPinBankConfig> getAllConfigurations() {
        logger.info("Getting all dcpin bank vs gateway configs from service");
        return dcPinBankVsPgDAOImpl.getAllConfigurations();
    }

}
