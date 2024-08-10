package com.yatra.payment.ui.dao.sql;

public class PaymentUISql {
	
	public static String SELECT_PRODUCTS = "SELECT `Key` from yp_payment_products_master";
  	
  	public static String SELCT_ALL_PAY_OPTS = "SELECT * from ui_payop";
  	
  	public static String SELECT_ALL_CARD_TYPES = "SELECT * from ui_cardtypes";
  	
  	public static String SELECT_ALL_NET_BANKS = "SELECT * from ui_nb_banks";

	public static String SELECT_ALL_DC_PIN_BANKS = "SELECT * from ui_dcpin_banks";

	public static String SELECT_ALL_DC_PIN_BANKS_VS_GATEWAY_CONFIG = "SELECT * from dcpin_cardinput_fields_config";

  	public static String SELECT_ALL_ATM_BANKS = "SELECT * from ui_atm_banks";
  	
 	public static String SELECT_ALL_QB_CARD_TYPES = "SELECT * from ui_qb_card_type_master";
  	
  	public static String SELECT_ALL_QB_CARD_BRANDS = "SELECT * from ui_qb_card_brand_master";
  	
  	public static String SELECT_PRODUCT_PAY_OPTS = "SELECT payment_option, status from ui_merchant_payop_mapping where merchant_code=? and product_code = ? ";
  	
  	public static String SELECT_CARD_TYPES = "SELECT card_type, payment_option, status from ui_payop_cardtypes_mapping where payment_option = ?";
  	
  	public static String SELECT_NON_BANKING_DETAILS = "SELECT option_code, payment_option, display_name, image_url, status from ui_payop_subtypes_mapping where payment_option = ?";
  	
  	public static String SELECT_EMI_BANKS = "select code, display_name, tenure, transaction_fee, card_types, status from ui_emi_banks";
  	
  	public static String SELECT_ALL_EMI_BANKS = "select code, display_name, tenure, interest_rate, transaction_fee, image_url, card_types, status, min_amount, max_amount, display_mobile from ui_emi_banks";
        
        public static String SELECT_ALL_NO_COST_EMI_BANKS = "select lob, products, is_promo_allowed, bank_configurations from ui_no_cost_emi";

  	public static String SELECT_CARD_OPTION_PARAMS = "SELECT param_value from ui_cardtypes_params_mapping where payment_option = ? and param_key=? limit 1";

  	public static String SELECT_CARD_TYPE_PARAM_PROP= "SELECT param_property from ui_cardtypes_params_mapping where card_type = ? and param_key=?";

  	public static String SELECT_CARD_TYPE_PARAM_VAL = "SELECT param_value from ui_cardtypes_params_mapping where card_type = ? and param_key=?";
  	
  	public static String SELECT_BIN_LIST = "select bin_number from yp_cc_bin_detail";
  	
  	public static String GET_CARD_BIN_COUNT = "select count(sno) total from yp_cc_nine_bin_detail where ? between from_bin and to_bin";
  	
  	public static String GET_BANK_NAME_FOR_BIN = "select bank_name from yp_cc_nine_bin_detail where ? between from_bin and to_bin";
  	
  	public static String GET_ATM_SUPPORTED_FLAG_FOR_BIN = "select atm_supported from yp_cc_nine_bin_detail where ? between from_bin and to_bin";
  	
  	public static String SELECT_ALL_ABT_ROLES = "SELECT * from ui_abt_role";
  	
  	public static String SELECT_ABT_PRODUCT_PAY_OPTS = "SELECT payment_option, status from ui_abt_payop_mapping where merchant_code=? and product_code = ? and role=? ";
  	
  	public static String SELECT_ALL_OFFLINE_OPTIONS = "SELECT * from ui_offline_subtypes_mapping";
  	
  	public static final String SELECT_PRODUCT_CARD_TYPES = "select card_type_code from ui_product_card_type_mapping where merchant_code=? and product_code=? and status='ENABLED'";
  	
  	public static final String SELECT_PRODUCT_BANKS = "select bank_code from ui_product_banks_mapping where merchant_code=? and product_code=? and status='ENABLED'";
  	
  	public static String SELECT_CENTRAL_CARD_OPTION_PARAMS = "SELECT param_value from ui_cardtypes_params_mapping_central where payment_option = ? and param_key=? limit 1";
  	
  	public static String SELECT_CENTRAL_CARD_TYPE_PARAM_VAL = "SELECT param_value from ui_cardtypes_params_mapping_central where card_type = ? and param_key=?";
  	
  	public static String SELECT_PAYMENT_OPTION_MESSAGES = "SELECT message, messageType, status, payment_option from ui_payop_message_mapping where status = 'ENABLED' and payment_option = ?";
  	
  	public static String SELECT_MERCHANT_PRODUCTS = "SELECT * from merchant_master";
  	
  	public static final String GET_ALL_PRODUCT_DETAILS = "select * from yp_payment_products_master";
  	
	public static String SELECT_CARD_TYPE_PARAMS_MAPPING = "SELECT * from ui_cardtypes_params_mapping_central";
	
	public static String SELECT_CENTRAL_PARAMS_VALUE = "SELECT param_value from ui_cardtypes_params_mapping_central";
	
	public static String SELECT_PARAM_VALUE = "SELECT param_value from ui_cardtypes_params_mapping";
	
	public static String SELECT_PAYMENT_OPTIONS_MAPPING = "SELECT * from ui_merchant_payop_mapping";
	
	public static String SELECT_ENABLED_CURRENCY_CONFIG_MAPPING = "SELECT * from ui_merchant_currency_mapping where is_enabled = 1";
	
	public static String SELECT_ALL_PAYOP_CARD_TYPES = "SELECT card_type, payment_option, status from ui_payop_cardtypes_mapping";
	
	public static String SELECT_ALL_PAYMENT_OPTION_MESSAGES = "SELECT message, messageType, status, payment_option from ui_payop_message_mapping";
	
	public static String SELECT_ALL_BANK_DOWN_MESSAGES = "SELECT name, code, message, payment_status, message_status from ui_bank_down_message_mapping";
	
	public static String SELECT_ALL_NON_BANKING_DETAILS = "SELECT option_code, payment_option, display_name, image_url, status from ui_payop_subtypes_mapping";
	
	public static String SELECT_ALL_SUBOPTION_DETAILS = "SELECT sub_option, payment_option, display_name, image_url, status, merchant_code,product_code,priority from ui_payop_suboption_mapping";
	
	public static final String SELECT_ALL_PRODUCT_BANKS = "select * from ui_product_banks_mapping where status='ENABLED'";
	
	public static final String SELECT_ALL_PRODUCT_CARD_TYPES = "select * from ui_product_card_type_mapping where status='ENABLED'";

	public static final String SELECT_ITINERARY_DETAILS_URL = " select itinerary_details_url from yp_payment_products_master where yp_payment_products_master.Key = ? order by id desc limit 1";
	
	public static final String SELECT_PAY_NOW_VALIDATION_URL = " select lob_validate_url from yp_payment_products_master where yp_payment_products_master.Key = ? order by id desc limit 1";
	
	public static final String SELECT_PARAM_FROM_PAYSWIFT_STAGES = " select params from payswift_stages where super_pnr = ? and stage = ? order by id desc limit 1";
	
        public static final String SELECT_PARAM_FROM_PAYMENT_INFO_AUDIT = " select params from payment_info_audit where super_pnr = ? and stage = ? order by id desc limit 1";
        
        public static final String SELECT_PARAM_FROM_PAYMENT_INFO_AUDIT_ON_ID = " select params from payment_info_audit where super_pnr = ? and stage = ? and id = ? order by id desc limit 1";
        
	public static final String SELECT_PARAM_FROM_PAYSWIFT_STAGES_ON_ID = " select params from payswift_stages where super_pnr = ? and stage = ? and id = ?";
	
	public static final String SELECT_PARAM_FROM_PAYMENT_STAGES = " select params from payment_stages where ttid = ? and stage = ? order by id desc limit 1";
	
	public static final String INSERT_INTO_PAYSWIFT_STAGE = "insert into payswift_stages (super_pnr,ttid,stage,params,server_ip_address,product,response_status,failure_desc) values (?,?,?,?,?,?,?,?)";

	public static final String GET_ALL_PRODUCT_SERVICE_URL = "select `key`,itinerary_details_url,lob_validate_url,header_url,footer_url,is_responsive,booking_details_required,lob from yp_payment_products_master where itinerary_details_url !='NULL' and lob_validate_url !='NULL'";
	
	public static final String GET_BANK_DOWN_MESSAGES = "SELECT bank_code, message from ui_bank_down_message_mapping where message_status = ?";
	
	public static final String GET_PAYOP_MESSAGES = "SELECT payment_option, message from ui_payop_message_mapping where status = ?";

	public static final String GET_YATRA_PROPERTY_VALUE = "SELECT prop_value from yatra_properties where prop_key = ?";

	public static final String INSERT_INTO_PAYMENT_INFO_AUDIT = "insert into payment_info_audit (stage,super_pnr,params) values (?,?,?)";
	
	public static final String GET_CARD_FROM_SESSION_OBJECT = "select session_data from session_master where session_uuid = ? order by id desc limit 1";
	
	public static final String INSERT_INTO_PAYSWIFT_LATENCY = "insert into payswift_latency (super_pnr,ttid,api,product,latency) values (?,?,?,?,?)";

	public static final String CREATE_S2S_RESPONSE_AUDIT = "INSERT INTO s2s_response_audit(super_pnr_code, merchant_txn_ref_id, product, s2s_request, s2s_response, status) VALUES (?,?,?,?,?,?)" ;
        
        public static final String CREATE_NO_COST_EMI_AUDIT = "INSERT INTO no_cost_emi_audit(super_pnr_code, params) VALUES (?,?)" ;
	
        public static final String INSERT_INTO_API_INFO_AUDIT = "insert into api_info_audit (super_pnr,api_name,stage,params,server_ip_address) values (?,?,?,?,?)";
	
	public static final String GET_CARD_TYPE_FOR_BIN = "select card_type from yp_cc_nine_bin_detail where ? between from_bin and to_bin";
	
	public static final String GET_BIN_DETAILS = "SELECT bin_number, atm_supported, bank_id, is_corporate from yp_cc_bin_detail where bin_number = ?";

	public static final String GET_BIN_TYPE_FOR_BIN = "select bin_type from yp_cc_nine_bin_detail where ? between from_bin and to_bin";
	
    public static final String GET_NO_COST_EMI_DETAILS = "select params from api_info_audit where super_pnr= ? and api_name = 'NO_COST_EMI' and stage = 'RESPONSE' order by id desc limit 1";
        
	public static final String GET_SKIP_OTP_RESPONSE_FOR_SUPERPNR = "select params from api_info_audit where super_pnr = ? and api_name = 'SkipOtpDecision' and stage = 'RESPONSE' order by id desc limit 1";

	public static final String GET_SKIP_OTP_BINS_FOR_SUPERPNR = "select params from api_info_audit where super_pnr = ? and api_name = 'SkipOTPBinList' and stage = 'RESPONSE' order by id desc limit 1";

	public static final String SELECT_ALL_ENABLED_CURRENCIES = "select * from yp_payment_currency_master where enabled = 1";

	public static final String GET_ALL_BIN_BANK_DETAILS = "select a.bin_number as binNo, a.bank_id as bankId, a.atm_supported as isAtmSupportedOnBin, a.is_corporate as isCorporateSupported, b.atm_supported as isAtmSupportedOnBank, b.otp_supported as isOtpSupportedOnBank, b.multi_pay_flow as multiPayFlowSequence from yp_cc_bin_detail a join yp_payment_banks_master b on a.bank_id = b.id";

	public static final String GET_ONE_BIN_BANK_DETAILS = "select a.bin_number as binNo, a.bank_name as bankName, a.bank_id as bankId, a.atm_supported as isAtmSupportedOnBin, a.bin_type as bin_type, a.is_corporate as isCorporateSupported, b.atm_supported as isAtmSupportedOnBank, b.otp_supported as isOtpSupportedOnBank, b.multi_pay_flow as multiPayFlowSequence,b.code from yp_cc_bin_detail a join yp_payment_banks_master b on a.bank_id = b.id where a.bin_number = ? order by id desc";

	public static final String GET_MULTIPAY_FLAG_FOR_PRODUCT = "select multi_pay_flow_enabled from yp_payment_products_master where `Key` = ?";
	
	public static final String GET_OFFICIAL_CORPORATE_BINS = "select bin_number from yp_cc_bin_detail where is_corporate = 'TRUE'";
	public static final String IS_OFFICIAL_CORPORATE_BIN = "select count(*) from yp_cc_nine_bin_detail where is_corporate = 'TRUE' and ? between from_bin and to_bin";

	public static final String GET_LINK_FROM_INTENT_URL_AUDIT = " select link from intent_url_audit where mtxn_id = ? and uid = ? order by id desc limit 1";

	public static final String SELECT_GATEWAY_FOR_BANK = "select b.name bank_name, g1.gateway_code code, g2.gateway_code code2, b.primary_share from yp_payment_banks_master b  left join yp_payment_gateway_master g1 on b.primary_gateway = g1.id left join yp_payment_gateway_master g2 on b.secondary_gateway = g2.id where b.code = ? ";

	public static final String GET_QC_VOUCHER_CONFIG = "SELECT * FROM qc_voucher_config";

	public static final String SELECT_ACTIVE_CORP_CARD_TOKEN_REF = "select tokenRefId from corporate_card_vault where card_id = ?  and token_status ='ACTI' order by id desc limit 1";
	public static final String SELECT_ACTIVE_CORP_CARD_TOKEN_INFO = "select *  from corporate_card_vault where card_id = ?  and token_status ='ACTI' order by id desc limit 1";

	public static final String GET_CORP_CARD_TOKEN_STATUS = "select card_id,token_status, card_type from corporate_card_vault where corporate_id = ?";
	
	public static final String GET_CARD_TYPE_FOR_NINE_BIN = "select card_type from yp_cc_nine_bin_detail where ? BETWEEN from_bin and to_bin";
	
	public static final String GET_NINE_BIN_INFORMATION = "select a.bank_name as bankName, a.bank_id as bankId, a.atm_supported as isAtmSupportedOnBin, a.bin_type as bin_type, a.is_corporate as isCorporateSupported, b.atm_supported as isAtmSupportedOnBank, b.otp_supported as isOtpSupportedOnBank, b.multi_pay_flow as multiPayFlowSequence,b.code from yp_cc_nine_bin_detail a join yp_payment_banks_master b on a.bank_id = b.id where ?  BETWEEN a.from_bin and a.to_bin order by id desc";

	public static final String SELECT_ENTITY_FROM_UPI_HANDLE = "select handle_name from upi_handle";

	public static final String INSERT_INTO_WEB_EVENTS="INSERT INTO web_events(super_pnr,event_type,web_event_msg,merchant_code,product_code)values(?,?,?,?,?)";

	//Paytm Url fetch from merchant_pg_mapper_properties
	public static final String FETCH_BIN_DETAIL = "SELECT property_value from merchant_pg_mapper_properties WHERE merchant_pg_key = 'yt_paytm_tr' and property_key = ? limit 1";

}
