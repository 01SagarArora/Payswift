package com.yatra.payment.ui.dao;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Component;

import com.yatra.payment.ui.dao.impl.UIPayopDAOImpl;
import com.yatra.payment.ui.dao.sql.PaymentUISql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class CompletePaymentInfoDAO {
	@Autowired
	private SimpleJdbcTemplate jdbcTemplate;

	@Autowired
	private JdbcTemplate jdbcConnectionTemplate;

	private static Logger logger = Logger.getLogger(UIPayopDAOImpl.class);
	

	public void logIntoPaymentInfoAudit(String superPnr,String params, String stage){
		logger.debug("Now logging into payment_info_audit with params "+params
				+" and stage "+stage);
		String sql = PaymentUISql.INSERT_INTO_PAYMENT_INFO_AUDIT;
		try{
			jdbcTemplate.update(sql, new Object[]{stage,superPnr, params});
		}
		catch(Exception ex){
			logger.error("Error occurred while inserting into payment info audit ",ex);
		}
	}

	public List<String> getUpiBankCodes(){

		String query = PaymentUISql.SELECT_ENTITY_FROM_UPI_HANDLE;
		logger.info("executing query : " + query);
		try{
			List<String> handleNamesList = jdbcConnectionTemplate.queryForList(query,String.class);
			return handleNamesList;
		} catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
