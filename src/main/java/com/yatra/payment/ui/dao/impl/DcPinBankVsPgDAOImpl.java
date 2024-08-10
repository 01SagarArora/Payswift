package com.yatra.payment.ui.dao.impl;

import com.yatra.payment.ui.dao.bean.DcPinBankConfig;
import com.yatra.payment.ui.dao.sql.PaymentUISql;
import com.yatra.payment.ui.exceptions.PayswiftException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static com.yatra.payment.ui.enums.FailureCode.FETCH_DCPIN_CONFIG_DETAILS_EXCEPTION;

@Component("dcPinBankVsPgDAOImpl")
public class DcPinBankVsPgDAOImpl {

    @Autowired
    private SimpleJdbcTemplate jdbcTemplate;

    private static Logger logger = Logger.getLogger(DcPinBankVsPgDAOImpl.class);

    public List<DcPinBankConfig> getAllConfigurations() {
        List<DcPinBankConfig> dcPinBanks = null;
        ParameterizedBeanPropertyRowMapper<DcPinBankConfig> dcPinBankMapper = new ParameterizedBeanPropertyRowMapper<DcPinBankConfig>() {
            public DcPinBankConfig mapRow(ResultSet rs, int rowNum) throws SQLException {
                DcPinBankConfig bankDO = new DcPinBankConfig();
                bankDO.setBank_code(rs.getString("dcpin_bank_code"));
                bankDO.setGateway_code(rs.getString("gateway_code"));
                bankDO.setCardfields_show(rs.getBoolean("cardfields_show"));
                return bankDO;
            }
        };

        try {
            logger.debug("Fetching dcpin bankvspg configs from dcpin_cardinput_fields_config.");
            dcPinBanks = jdbcTemplate.query(PaymentUISql.SELECT_ALL_DC_PIN_BANKS_VS_GATEWAY_CONFIG, dcPinBankMapper);
        } catch (Exception e) {
            throw new PayswiftException(FETCH_DCPIN_CONFIG_DETAILS_EXCEPTION.getCode(), "Exception occurred while fetching debitpin config details from db.", e);
        }
        return dcPinBanks;

    }

}
