package com.yatra.payment.ui.dao;

import com.yatra.payment.ui.dao.sql.PaymentUISql;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Component;

@Component("yatraPropertiesDAO")
public class YatraPropertiesDAO {
    @Autowired
    private SimpleJdbcTemplate jdbcTemplate;

    private static Logger logger = Logger.getLogger(YatraPropertiesDAO.class);

    public String getYatraPropValue(String propertyKey) {
        try {
            String sql = PaymentUISql.GET_YATRA_PROPERTY_VALUE;
            Object[] obj = new Object[]{propertyKey};

            return jdbcTemplate.queryForObject(sql, String.class, obj);
        } catch (Exception e) {
            logger.error("Exception occurred while getting property value for property key: " + propertyKey, e);
            return null;
        }
    }
    
    public Boolean getBooleanPropValue(String propertyKey) {
        try {
            String sql = PaymentUISql.GET_YATRA_PROPERTY_VALUE;
            Object[] obj = new Object[]{propertyKey};

            String value = jdbcTemplate.queryForObject(sql, String.class, obj);
            return Boolean.valueOf(value);
        } catch (Exception e) {
            logger.error("Exception occurred while getting boolean property value for property key: " + propertyKey, e);
            return null;
        }
    }
}
