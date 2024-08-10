package com.yatra.payment.ui.builder;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.ui.cache.builder.CacheBuilder;

@Service
public class PaymentDivBuilder {

	@Autowired private CacheBuilder merchantCacheBuilder;
	@Autowired private CacheBuilder cardTypeCacheBuilder;
	@Autowired private CacheBuilder netBankCacheBuilder;
	@Autowired private CacheBuilder atmBankCacheBuilder;
	@Autowired private CacheBuilder emiBankCacheBuilder;
	@Autowired private CacheBuilder paymentOptionCacheBuilder;
	@Autowired private CacheBuilder cardTypeParamMappingCacheBuilder;
	@Autowired private CacheBuilder cardTypeParamMappingCentralCacheBuilder;
	@Autowired private CacheBuilder productCardTypeMappingCacheBuilder;
	@Autowired private CacheBuilder payopCardTypesMappingCacheBuilder;
	@Autowired private CacheBuilder payopMessageMappingCacheBuilder;
	@Autowired private CacheBuilder productBanksCacheBuilder;
	@Autowired private CacheBuilder payopSubTypesCacheBuilder;
	@Autowired private CacheBuilder payopSuboptionCacheBuilder;
	@Autowired private CacheBuilder merchantPayopCacheBuilder;
	@Autowired private CacheBuilder qbCardTypeCacheBuilder;
	@Autowired private CacheBuilder qbCardBrandCacheBuilder;
	@Autowired private CacheBuilder paymentDivCacheBuilder;
	@Autowired private CacheBuilder productServiceUrlCacheBuilder;
	@Autowired private CacheBuilder debitCardPinBankCacheBuilder;
        @Autowired private CacheBuilder noCostCacheBuilder;
        
	@PostConstruct
	public void build() {
		buildMerchantCache();
		buildCardTypeCache();
		buildNetBankCache();
		buildDebitCardBankCache();
		buildATMBankCache();
		buildEMIBankCache();
		buildCardTypeParamMappingCache();
		buildCardTypeParamMappingCentralCache();
		buildProductCardTypeMappingCache();
		buildPayopCardTypesMappingCache();
		buildPayopSubTypeMappingCache();
		buildPayopSuboptionMappingCache();
		buildProductBanksCache();
		buildPayopMessageMappingCache();
		buildPaymentOptionCache();
		buildMerchantPayopCache();
		buildQBCardTypeCache();
		buildQBCardBrandCache();
		buildPaymentDivCache();
		buildProductServiceUrlCache();
                buildNoCostEmiCache();
	}

	private void buildProductServiceUrlCache() {
		productServiceUrlCacheBuilder.build();
	}

	private void buildMerchantCache() {
		merchantCacheBuilder.build();
	}

	private void buildCardTypeCache() {
		cardTypeCacheBuilder.build();
	}

	private void buildNetBankCache() {
		netBankCacheBuilder.build();
	}

	private void buildDebitCardBankCache() {
		debitCardPinBankCacheBuilder.build();
	}

	private void buildATMBankCache() {
		atmBankCacheBuilder.build();
	}
	
	private void buildEMIBankCache() {
		emiBankCacheBuilder.build();
	}

	private void buildCardTypeParamMappingCache() {
		cardTypeParamMappingCacheBuilder.build();
	}

	private void buildCardTypeParamMappingCentralCache() {
		cardTypeParamMappingCentralCacheBuilder.build();
	}

	private void buildProductCardTypeMappingCache() {
		productCardTypeMappingCacheBuilder.build();
	}

	private void buildProductBanksCache() {
		productBanksCacheBuilder.build();
	}

	private void buildPayopCardTypesMappingCache() {
		payopCardTypesMappingCacheBuilder.build();
	}
	
	private void buildPayopSubTypeMappingCache() {
		payopSubTypesCacheBuilder.build();
	}
	
	private void buildPayopSuboptionMappingCache() {
		payopSuboptionCacheBuilder.build();
	}

	private void buildPayopMessageMappingCache() {
		payopMessageMappingCacheBuilder.build();
	}

	private void buildPaymentOptionCache() {
		paymentOptionCacheBuilder.build();
	}

	private void buildMerchantPayopCache() {
		merchantPayopCacheBuilder.build();
	}
	
	private void buildQBCardTypeCache() {
		qbCardTypeCacheBuilder.build();
	}
	
	private void buildQBCardBrandCache() {
		qbCardBrandCacheBuilder.build();
	}

	private void buildPaymentDivCache() {
		paymentDivCacheBuilder.build();
	}
        
        private void buildNoCostEmiCache() {
                noCostCacheBuilder.build();
        }
}
