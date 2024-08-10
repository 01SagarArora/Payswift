package com.yatra.payment.ui.cache.builder;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.dao.ProductMasterDAO;
import com.yatra.payment.ui.display.beans.ProductServiceUrlBean;

@Service("productServiceUrlCacheBuilder")
public class ProductServiceUrlCacheBuilder implements CacheBuilder {

	@Autowired	private UICacheManager<ProductServiceUrlBean> productServiceUrlCache;
	@Autowired private ProductMasterDAO productMasterDAO;
	@Override
	public void build() {
		List<ProductServiceUrlBean> productServiceUrlBeanList = productMasterDAO.getAllProductServiceUrl();
		if(productServiceUrlBeanList!=null){
			for(ProductServiceUrlBean productServiceUrlBean : productServiceUrlBeanList){
				productServiceUrlCache.set(productServiceUrlBean.getKey(), productServiceUrlBean);
			}
		}
	}

}
