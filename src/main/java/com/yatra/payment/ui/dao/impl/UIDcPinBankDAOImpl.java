package com.yatra.payment.ui.dao.impl;

import com.yatra.payment.ui.dao.PaymentUIGenericDAO;
import com.yatra.payment.ui.dao.bean.UIDcPinBank;
import com.yatra.payment.ui.dao.sql.PaymentUISql;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component("dcPinBanksDAO")
public class UIDcPinBankDAOImpl implements PaymentUIGenericDAO<UIDcPinBank> {

    @Autowired
    private SimpleJdbcTemplate jdbcTemplate;

    private static Logger logger = Logger.getLogger(UIDcPinBankDAOImpl.class);

    @Override
    public List<UIDcPinBank> getAll() throws Exception {
        List<UIDcPinBank> dcPinBanks = null;

        ParameterizedBeanPropertyRowMapper<UIDcPinBank> dcPinBankMapper = new ParameterizedBeanPropertyRowMapper<UIDcPinBank>() {
            public UIDcPinBank mapRow(ResultSet rs, int rowNum) throws SQLException {
                UIDcPinBank bankDO = new UIDcPinBank();
                bankDO.setBankCode(rs.getString("bank_code"));
                bankDO.setDisplayName(rs.getString("display_name"));
                bankDO.setPreferred(rs.getBoolean("preferred"));
                bankDO.setImageURL(rs.getString("image_url"));
                bankDO.setStatus(rs.getString("status"));
                return bankDO;
            }
        };

        try {
            logger.debug("Fetching banks from ui_dcpin_banks.");
            dcPinBanks = jdbcTemplate.query(PaymentUISql.SELECT_ALL_DC_PIN_BANKS, dcPinBankMapper);
        } catch (Exception e) {
            logger.error("Exception occurred while fetching from ui_dcpin_banks." , e);
            throw e;
        }

        return dcPinBanks;
    }

    @Override
    public List<UIDcPinBank> getList(UIDcPinBank uiDcPinBank) throws Exception {
        return null;
    }

    @Override
    public UIDcPinBank get(UIDcPinBank uiDcPinBank) throws Exception {
        return null;
    }
}
