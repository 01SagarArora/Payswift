package com.yatra.payment.payswift.FutureTasks;


import com.yatra.payment.ui.dao.bean.DcPinBankConfig;
import com.yatra.payment.ui.service.impl.DcPinBankConfigService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class DebitCardPinBankVsPgData implements Callable<List<DcPinBankConfig>> {

    private DcPinBankConfigService dcPinBankConfigService;

    public DebitCardPinBankVsPgData(DcPinBankConfigService dcPinBankConfigService) {
        this.dcPinBankConfigService = dcPinBankConfigService;
    }

    @Override
    public List<DcPinBankConfig> call() throws Exception {
        return dcPinBankConfigService.getAllConfigurations();
    }
}
