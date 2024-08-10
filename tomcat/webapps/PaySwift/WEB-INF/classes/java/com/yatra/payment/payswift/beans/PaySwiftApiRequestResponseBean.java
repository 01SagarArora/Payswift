package com.yatra.payment.payswift.beans;

public class PaySwiftApiRequestResponseBean {
    private boolean success;
    private String superPnr;
    private String tripId;
    private String email;
    private String productCode;
    private String productLOB;
    private String channel;
    private String device;
    private String business;
    private long requestStartTime;
    private long totalTimeTaken;
    private String serverIp;
    private String clientIp;
    private boolean isLoggedIn;
    private boolean staleSsoToken;

    private boolean fetch_itinerary_success;
    private long fetch_itinerary_response_time;
    private String fetch_itinerary_error;

    private boolean b2b_payment_options_success;
    private long b2b_payment_options_response_time;
    private String b2b_payment_options_error;

    private boolean b2b_credit_pool_success;
    private long b2b_credit_pool_response_time;
    private String b2b_credit_pool_error;

    private String qb_get_cards_success;
    private long qb_get_cards_response_time;
    private boolean qb_shown;
    private boolean qb_should_guest_card_be_saved;
    private boolean qb_guest_card_saved;
    private String qb_error;

    private String corp_qb_get_cards_success;
    private long corp_qb_get_cards_response_time;
    private boolean corp_qb_shown;
    private String corp_qb_error;

    private String fatalError;
    
    private boolean validate_payment_success;
    private long validate_payment_response_time;
    private String validate_payment_error;

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setSuperPnr(String superPnr) {
        this.superPnr = superPnr;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public void setProductLOB(String productLOB) {
        this.productLOB = productLOB;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public void setBusiness(String business) {
        this.business = business;
    }

    public void setRequestStartTime(long requestStartTime) {
        this.requestStartTime = requestStartTime;
    }

    public void setTotalTimeTaken(long totalTimeTaken) {
        this.totalTimeTaken = totalTimeTaken;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }

    public void setStaleSsoToken(boolean staleSsoToken) {
        this.staleSsoToken = staleSsoToken;
    }

    public void setFetch_itinerary_success(boolean fetch_itinerary_success) {
        this.fetch_itinerary_success = fetch_itinerary_success;
    }

    public void setFetch_itinerary_response_time(long fetch_itinerary_response_time) {
        this.fetch_itinerary_response_time = fetch_itinerary_response_time;
    }

    public void setFetch_itinerary_error(String fetch_itinerary_error) {
        this.fetch_itinerary_error = fetch_itinerary_error;
    }

    public void setB2b_payment_options_success(boolean b2b_payment_options_success) {
        this.b2b_payment_options_success = b2b_payment_options_success;
    }

    public void setB2b_payment_options_response_time(long b2b_payment_options_response_time) {
        this.b2b_payment_options_response_time = b2b_payment_options_response_time;
    }

    public void setB2b_payment_options_error(String b2b_payment_options_error) {
        this.b2b_payment_options_error = b2b_payment_options_error;
    }

    public void setB2b_credit_pool_success(boolean b2b_credit_pool_success) {
        this.b2b_credit_pool_success = b2b_credit_pool_success;
    }

    public void setB2b_credit_pool_response_time(long b2b_credit_pool_response_time) {
        this.b2b_credit_pool_response_time = b2b_credit_pool_response_time;
    }

    public void setB2b_credit_pool_error(String b2b_credit_pool_error) {
        this.b2b_credit_pool_error = b2b_credit_pool_error;
    }

    public void setQb_get_cards_success(String qb_get_cards_success) {
        this.qb_get_cards_success = qb_get_cards_success;
    }

    public void setQb_get_cards_response_time(long qb_get_cards_response_time) {
        this.qb_get_cards_response_time = qb_get_cards_response_time;
    }

    public void setQb_shown(boolean qb_shown) {
        this.qb_shown = qb_shown;
    }

    public void setQb_should_guest_card_be_saved(boolean qb_should_guest_card_be_saved) {
        this.qb_should_guest_card_be_saved = qb_should_guest_card_be_saved;
    }

    public void setQb_guest_card_saved(boolean qb_guest_card_saved) {
        this.qb_guest_card_saved = qb_guest_card_saved;
    }

    public void setQb_error(String qb_error) {
        this.qb_error = qb_error;
    }

    public void setCorp_qb_get_cards_success(String corp_qb_get_cards_success) {
        this.corp_qb_get_cards_success = corp_qb_get_cards_success;
    }

    public void setCorp_qb_get_cards_response_time(long corp_qb_get_cards_response_time) {
        this.corp_qb_get_cards_response_time = corp_qb_get_cards_response_time;
    }

    public void setCorp_qb_shown(boolean corp_qb_shown) {
        this.corp_qb_shown = corp_qb_shown;
    }

    public void setCorp_qb_error(String corp_qb_error) {
        this.corp_qb_error = corp_qb_error;
    }

    public void setFatalError(String fatalError) {
        this.fatalError = fatalError;
    }
}
