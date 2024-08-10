package com.yatra.payment.ui.dao;

import com.yatra.payment.ui.beans.BinDetails;
import com.yatra.payment.ui.dao.sql.PaymentUISql;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.valueOf;

@Component("binDetailsDAO")
public class BinDetailsDAO {


    private static Logger logger = Logger.getLogger(BinDetailsDAO.class);

    @Autowired
    private SimpleJdbcTemplate jdbcTemplate;

    public Integer getCardBinCount(String bin) {
        try {
            logger.info("Fetching card bin count from yp_cc_nine_bin_detail for bin_number = " + bin);
            List<Number> result = jdbcTemplate.query(PaymentUISql.GET_CARD_BIN_COUNT, new ParameterizedBeanPropertyRowMapper<Number>() {
                public Number mapRow(ResultSet rs, int rowNum) throws SQLException {
                    Number binNumber = rs.getInt("total");
                    return binNumber;
                }
            }, bin);

            logger.debug("Bin count : " + result.get(0));
            return result.get(0).intValue();

        } catch (Exception e) {
            logger.error("Exception occurred while fetching card bin count from yp_cc_nine_bin_detail table : ", e);
            return null;
        }
    }

    public String getBankNameForBin(String bin) {
        try {
            logger.info("Fetching bank name from yp_cc_nine_bin_detail for bin_number = " + bin);
            List<String> result = jdbcTemplate.query(PaymentUISql.GET_BANK_NAME_FOR_BIN, new ParameterizedBeanPropertyRowMapper<String>() {
                public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                    String bankName = rs.getString("bank_name");
                    return bankName;
                }
            }, bin);

            logger.debug("Bank Name : " + result.get(0));
            return result.get(0).toString();

        } catch (Exception e) {
            logger.error("Exception occurred while fetching bank name from yp_cc_nine_bin_detail table : ", e);
            return null;
        }
    }

    public String getAtmSupportedFlagForBin(String bin) {
        try {
            logger.info("Fetching atm_supported flag from yp_cc_nine_bin_detail for bin_number = " + bin);
            List<String> result = jdbcTemplate.query(PaymentUISql.GET_ATM_SUPPORTED_FLAG_FOR_BIN, new ParameterizedBeanPropertyRowMapper<String>() {
                public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                    String atmSupported = rs.getString("atm_supported");
                    return atmSupported;
                }
            }, bin);

            logger.debug("atmSupported : " + result.get(0));
            return result.get(0).toString();

        } catch (Exception e) {
            logger.error("Exception occurred while fetching atm_supported flag from yp_cc_nine_bin_detail table : ", e);
            return null;
        }
    }

    public String getCardTypeForBin(String bin) {

        try {
            logger.info("Fetching cardType flag from yp_cc_nine_bin_detail for bin_number = " + bin);
            List<String> result = jdbcTemplate.query(PaymentUISql.GET_CARD_TYPE_FOR_BIN, new ParameterizedBeanPropertyRowMapper<String>() {
                public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                    String cardType = rs.getString("card_type");
                    return cardType;
                }
            }, bin);
            if (result == null || result.isEmpty()) {
                return null;
            }

            logger.debug("cardType : " + result.get(0));
            return result.get(0).toString();

        } catch (Exception e) {
            logger.error("Exception occurred while fetching cardType from yp_cc_nine_bin_detail table : ", e);
            return null;
        }
    }


    public List<String> getOfficialCorporateBins() {
        try {
            String sql = PaymentUISql.GET_OFFICIAL_CORPORATE_BINS;
            List<String> result = jdbcTemplate.query(sql, new ParameterizedBeanPropertyRowMapper<String>() {
                public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                    String cardType = rs.getString("bin_number");
                    return cardType;
                }
            });
            return result;
        } catch (Exception ex) {
            logger.error("Error while fetching official corporate bins");
            return new ArrayList<>();
        }
    }

    public boolean isOfficialCorporateBin(String bin) {
        try {
            String sql = PaymentUISql.IS_OFFICIAL_CORPORATE_BIN;
            int result = jdbcTemplate.queryForInt(sql, bin);
            return result == 0 ? false : true;
        } catch (Exception ex) {
            logger.error("Error while fetching official corporate bins", ex);
            return false;
        }
    }

	

	public String getCardTypeForNineBin(String bin) {
		try {
			logger.info("Fetching cardType flag from yp_cc_nine_bin_detail for bin_number = " + bin);
			List<String> result = jdbcTemplate.query(PaymentUISql.GET_CARD_TYPE_FOR_NINE_BIN, new ParameterizedBeanPropertyRowMapper<String>() {
				public String mapRow(ResultSet rs, int rowNum) throws SQLException {
					String cardType = rs.getString("card_type");
					return cardType;
				}
			}, bin);
			if(result == null || result.isEmpty())
			{
				return null;
			}

			logger.debug("cardType : " + result.get(0));
			return result.get(0).toString();
			
		} catch (Exception e) {
			logger.error("Exception occurred while fetching cardType from yp_cc_nine_bin_detail table : ", e);
			return null;
		}

	}


	public List<Map<String, Object>> getAllBinBankDetails() {
		try {
			String query = PaymentUISql.GET_ALL_BIN_BANK_DETAILS;
			return jdbcTemplate.queryForList(query);
		} catch (Exception e) {
			logger.error("Exception occurred while getting all bin details", e);
			return null;
		}
	}

	public Optional<BinDetails> getOneBinBankDetails(final String bin) {
		try {
			
			String sql;
			
			Object[] obj = new Object[]{bin};
			List<BinDetails> binDetailsList;
			if(bin.length() == 9){
				sql = PaymentUISql.GET_NINE_BIN_INFORMATION;
				//For ninebin details mapper we don't need BinNo in builder function.
				
				RowMapper<BinDetails> nineDetailsMapper = new RowMapper<BinDetails>() {
					public BinDetails mapRow(ResultSet rs, int rowNum) throws SQLException {
						BinDetails binDetails = new BinDetails.BinDetailsBuilder()
								.withBinNumber(bin)
								.withBankId(valueOf(rs.getInt("bankId")))
	                                                        .withBinType(rs.getString("bin_type"))
								.withBankName(rs.getString("bankName"))
								.withAtmSupportedOnBin("1".equalsIgnoreCase(valueOf(rs.getInt("isAtmSupportedOnBin"))))
								.withCorporateSupported("true".equalsIgnoreCase(rs.getString("isCorporateSupported")))
								.withAtmSupportedOnBank("1".equalsIgnoreCase(valueOf(rs.getInt("isAtmSupportedOnBank"))))
								.withOtpSupportedOnBank("1".equalsIgnoreCase(valueOf(rs.getInt("isOtpSupportedOnBank"))))
								.withMultiPayFlowSequence(rs.getString("multiPayFlowSequence"))
								.withCardInternational(false)
								.withBankCode(rs.getString("code"))
								.build();
						return binDetails;
					}
					
				
				};
				
				binDetailsList = jdbcTemplate.query(sql, nineDetailsMapper, obj);
			}else {
				
				RowMapper<BinDetails> binDetailsMapper = new RowMapper<BinDetails>() {
					public BinDetails mapRow(ResultSet rs, int rowNum) throws SQLException {
						BinDetails binDetails = new BinDetails.BinDetailsBuilder()
								.withBinNumber(rs.getString("binNo"))
								.withBankId(valueOf(rs.getInt("bankId")))
	                                                        .withBinType(rs.getString("bin_type"))
								.withBankName(rs.getString("bankName"))
								.withAtmSupportedOnBin("1".equalsIgnoreCase(valueOf(rs.getInt("isAtmSupportedOnBin"))))
								.withCorporateSupported("true".equalsIgnoreCase(rs.getString("isCorporateSupported")))
								.withAtmSupportedOnBank("1".equalsIgnoreCase(valueOf(rs.getInt("isAtmSupportedOnBank"))))
								.withOtpSupportedOnBank("1".equalsIgnoreCase(valueOf(rs.getInt("isOtpSupportedOnBank"))))
								.withMultiPayFlowSequence(rs.getString("multiPayFlowSequence"))
								.withCardInternational(false)
								.withBankCode(rs.getString("code"))
								.build();

						return binDetails;
					}
					
				
				};

				sql = PaymentUISql.GET_ONE_BIN_BANK_DETAILS;
				binDetailsList = jdbcTemplate.query(sql, binDetailsMapper, obj);
			}
			
			if (binDetailsList != null && !binDetailsList.isEmpty()) {
				return Optional.of(binDetailsList.get(0));
			}
		} catch (Exception e) {
			logger.error("Exception occurred while fetching bin details for: " + bin, e);
		}
		return Optional.empty();
	}

}
