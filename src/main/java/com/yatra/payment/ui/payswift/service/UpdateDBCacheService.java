package com.yatra.payment.ui.payswift.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.ui.cache.builder.PayopMessageMappingCacheBuilder;

@Service
public class UpdateDBCacheService {
	
	@Autowired PayopMessageMappingCacheBuilder payopMessageMappingCacheBuilder;
	
	public String updatePayopMessageMappingCache() {
		
		payopMessageMappingCacheBuilder.build();
		
		return "PayOp Message Cache Updated Successfully";
	}

}
