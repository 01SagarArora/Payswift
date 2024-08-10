package com.yatra.payment.ui.service;

import com.yatra.payment.ui.beans.BinDetails;
import com.yatra.payment.ui.dao.BinDetailsDAO;
import com.yatra.payment.ui.dao.ProductMasterDAO;
import com.yatra.payment.ui.dao.YatraPropertiesDAO;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.yatra.platform.util.YatraUtil.isNullOrEmpty;

@Service
public class MultiPayProcessService {
    private static Logger logger = Logger.getLogger(MultiPayProcessService.class);

    @Autowired
    private YatraPropertiesDAO yatraPropertiesDAO;
    @Autowired
    private ProductMasterDAO productMasterDAO;
    @Autowired
    private BinDetailsDAO binDetailsDAO;
    @Autowired
    private TenantService tenantService;
    @Autowired
    private CacheService cacheService;
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    private static volatile Boolean isMultiPayFlowGlobalFlagEnabled;
    private static volatile Boolean isCachingEnabled;

    @PostConstruct
    private void setMultiPayFlowDetails() throws Exception {
        updateMultiPayFlowGlobalFlag();
        updateMultiPayFlowCachingFlag();
    }

    private void updateMultiPayFlowGlobalFlag() {
        String enableMultiPayFlow = yatraPropertiesDAO.getYatraPropValue("enableMultiPayFlow");
        isMultiPayFlowGlobalFlagEnabled = !isNullOrEmpty(enableMultiPayFlow) && "true".equalsIgnoreCase(enableMultiPayFlow);
    }

    private void updateMultiPayFlowCachingFlag() {
        String enableRedisCaching = yatraPropertiesDAO.getYatraPropValue("enableRedisCaching");
        isCachingEnabled = !isNullOrEmpty(enableRedisCaching) && "true".equalsIgnoreCase(enableRedisCaching);
    }

    public Map<String, Object> getmultiPayFlowBinInfo(String bin, String product) {
        Map<String, Object> multiPayFlowInfo = new HashMap<>();
        Optional<BinDetails> binDetails;
        String isMultiPayFlowEnabled;

        if (isCachingEnabled) {
            logger.info("Caching is enabled, getting bin details and product info");

            Map<String, Object> multiPayFlowBinInfo = cacheService.multiPayFlowBinInfo(bin, product);
            binDetails = (Optional<BinDetails>) multiPayFlowBinInfo.get("binDetails");
            isMultiPayFlowEnabled = (String) multiPayFlowBinInfo.get("isMultiPayFlowEnabled");

            if (binDetails == null) {
                binDetails = binDetailsFromDb(bin);
                if (binDetails.isPresent()) {
                    saveBinInfo(binDetails.get());
                }
            }

            if (isMultiPayFlowEnabled == null) {
                isMultiPayFlowEnabled = String.valueOf(productMasterDAO.getMultiPayFlagForProduct(product));
                saveProductInfo(product, isMultiPayFlowEnabled);
            }
        } else {
            logger.info("Caching is disabled, getting bin details from db");
            binDetails = binDetailsFromDb(bin);
            isMultiPayFlowEnabled = String.valueOf(isMultiPayFlowEnabled(product));
        }

        multiPayFlowInfo.put("isMultiPayFlowEnabled", isMultiPayFlowEnabled);
        multiPayFlowInfo.put("binDetails", binDetails);
        return multiPayFlowInfo;
    }

    private Optional<BinDetails> binDetailsFromDb(String bin) {
        return binDetailsDAO.getOneBinBankDetails(bin);
    }

    private boolean isMultiPayFlowEnabled(String productCode) {
        Boolean multiPayEnabled = tenantService.isProductMultiPayFlowEnabled(productCode);
        return isMultiPayFlowGlobalFlagEnabled && multiPayEnabled;
    }

    private void saveBinInfo(BinDetails binDetails) {
        threadPoolTaskExecutor.submit(new Runnable() {
            @Override
            public void run() {
                logger.info("Saving bin details for bin: " + binDetails.getBinNumber());
                cacheService.saveBinDetails(binDetails);
            }
        });
    }

    private void saveProductInfo(String product, String isMultiPayFlowEnabled) {
        threadPoolTaskExecutor.submit(new Runnable() {
            @Override
            public void run() {
                logger.info("Saving IsProductMultiPayFlowEnabled info for product: " + product);
                cacheService.saveIsProductMultiPayFlowEnabled(product, isMultiPayFlowEnabled);
            }
        });
    }
}
