package com.yatra.payment.ui.dao;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Component;

import com.yatra.payment.ui.dao.sql.PaymentUISql;
import com.yatra.payment.ui.display.beans.ProductServiceUrlBean;

@Component("productMasterDAO")
public class ProductMasterDAO {
	
	private static Logger logger = Logger.getLogger(ProductMasterDAO.class);
	
	@Autowired private SimpleJdbcTemplate jdbcTemplate;
	
	public List<Map<String, Object>> getAllProductsDetails() {
		try {
			String query = PaymentUISql.GET_ALL_PRODUCT_DETAILS;
			return jdbcTemplate.queryForList(query);
		} catch (Exception e) {
			logger.error("Exception occurred while getting all product details", e);
			return null;
		}
	}
	
	public List<ProductServiceUrlBean> getAllProductServiceUrl(){
		try{
			String query = PaymentUISql.GET_ALL_PRODUCT_SERVICE_URL;
			return jdbcTemplate.query(query, new BeanPropertyRowMapper(ProductServiceUrlBean.class));
		} catch (Exception e) {
			logger.error("Exception occurred while getting all product details", e);
			return null;
		}
	}

	public boolean getMultiPayFlagForProduct(String product) {
		try {
			String query = PaymentUISql.GET_MULTIPAY_FLAG_FOR_PRODUCT;
			Object[] obj = new Object[]{product};

			Integer multiPayFlowFlag = jdbcTemplate.queryForObject(query, Integer.class, obj);
			return multiPayFlowFlag.equals(1);
		} catch (Exception e) {
			logger.error("Exception occurred while getting multipay flag for product " + product, e);
			return false;
		}
	}
	
}
