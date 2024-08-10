package com.yatra.payment.ui.builder;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.ui.util.PaymentUIUtil;

@Service("paymentOptionBuilderFactory")
public class PaymentOptionBuilderFactory {

	private static Logger logger = Logger.getLogger(PaymentOptionBuilderFactory.class);
	
	@Autowired	private PaymentOptionBuilder creditCardOptionBuilder;
	@Autowired	private PaymentOptionBuilder debitCardOptionBuilder;
	@Autowired	private PaymentOptionBuilder netBankingOptionBuilder;
	@Autowired	private PaymentOptionBuilder cashCardOptionBuilder;
	@Autowired	private PaymentOptionBuilder atmCardOptionBuilder;
	@Autowired	private PaymentOptionBuilder emiOptionBuilder;
	@Autowired	private PaymentOptionBuilder mobileWalletOptionBuilder;
	@Autowired	private PaymentOptionBuilder ezeClickOptionBuilder;
	@Autowired	private PaymentOptionBuilder rewardsPaymentOptionBuilder;
	@Autowired	private PaymentOptionBuilder eWalletOptionBuilder;
	@Autowired	private PaymentOptionBuilder quickBookOptionBuilder;
	@Autowired	private PaymentOptionBuilder payuOptionBuilder;
	@Autowired	private PaymentOptionBuilder creditPoolOptionBuilder;
	@Autowired  private PaymentOptionBuilder corporateCardOptionBuilder;
	@Autowired	private PaymentOptionBuilder sbiBuddyOptionBuilder;
	@Autowired  private PaymentOptionBuilder upiPaymentOptionBuilder;
	@Autowired  private PaymentOptionBuilder debitCardPinPaymentOptionBuilder;
	@Autowired  private PaymentOptionBuilder indifiPaymentOptionBuilder;
	@Autowired  private PaymentOptionBuilder paypalPaymentOptionBuilder;
	@Autowired  private PaymentOptionBuilder jaldiCashOptionBuilder;
	@Autowired  private PaymentOptionBuilder finoPayOptionBuilder;
	@Autowired  private PaymentOptionBuilder oxigenB2BOptionBuilder;
	@Autowired  private PaymentOptionBuilder gvPaymentOptionBuilder; 
	@Autowired  private PaymentOptionBuilder bankItOptionBuilder;
	@Autowired  private PaymentOptionBuilder spiceMoneyOptionBuilder;
	@Autowired  private PaymentOptionBuilder payNearByOptionBuilder;
	@Autowired  private PaymentOptionBuilder payMonkOptionBuilder;
	@Autowired  private PaymentOptionBuilder tsiWalletPayOptionBuilder;
    @Autowired  private PaymentOptionBuilder googleTezPaymentOptionBuilder;
	@Autowired  private PaymentOptionBuilder payLaterOptionBuilder; 
	@Autowired  private PaymentOptionBuilder amazonPayOptionBuilder;
	@Autowired  private PaymentOptionBuilder phonePeOptionBuilder;
	@Autowired  private PaymentOptionBuilder cashPoolPayOpBuilder;
	@Autowired  private PaymentOptionBuilder zestMoneyOptionBuilder;
	@Autowired  private PaymentOptionBuilder pay4BizOptionBuilder;
	@Autowired  private PaymentOptionBuilder roinetOptionBuilder;
	@Autowired  private PaymentOptionBuilder vakrangeeOptionBuilder;
	@Autowired private PaymentOptionBuilder ebixCashOptionBuilder;
	@Autowired private PaymentOptionBuilder paytmOptionBuilder;
	@Autowired private PaymentOptionBuilder googlePayUPIOptionBuilder;
	@Autowired private PaymentOptionBuilder useLocatorOptionBuilder;
	@Autowired private PaymentOptionBuilder upiNonMotoOptionBuilder;
	@Autowired private PaymentOptionBuilder googleTezPWAPaymentOptionBuilder;
	@Autowired private PaymentOptionBuilder twidPaymentOptionBuilder;
	@Autowired private PaymentOptionBuilder quicksonOptionBuilder;
	@Autowired private PaymentOptionBuilder credPaymentOptionBuilder;
	
	public PaymentOptionBuilder createPaymentOptionBuilder(String paymentOptionCode) {

		logger.info("createPaymentOptionBuilder() invoked for : " + paymentOptionCode);

		if (paymentOptionCode.equalsIgnoreCase(PaymentUIUtil.PAYMENT_OPTION_CREDIT_CARD))
			return creditCardOptionBuilder;

		if (paymentOptionCode.equalsIgnoreCase(PaymentUIUtil.PAYMENT_OPTION_DEBIT_CARD))
			return debitCardOptionBuilder;

		if (paymentOptionCode.equalsIgnoreCase(PaymentUIUtil.PAYMENT_OPTION_NET_BANKING))
			return netBankingOptionBuilder;

		if (paymentOptionCode.equalsIgnoreCase(PaymentUIUtil.PAYMENT_OPTION_CASH_CARD))
			return cashCardOptionBuilder;

		if (paymentOptionCode.equalsIgnoreCase(PaymentUIUtil.PAYMENT_OPTION_ATM_CARD))
			return atmCardOptionBuilder;

		if (paymentOptionCode.equalsIgnoreCase(PaymentUIUtil.PAYMENT_OPTION_EMI))
			return emiOptionBuilder;

		if (paymentOptionCode.equalsIgnoreCase(PaymentUIUtil.PAYMENT_OPTION_MOBILE_WALLET))
			return mobileWalletOptionBuilder;

		if (paymentOptionCode.equalsIgnoreCase(PaymentUIUtil.PAYMENT_OPTION_EZE_CLICK))
			return ezeClickOptionBuilder;

		if (paymentOptionCode.equalsIgnoreCase(PaymentUIUtil.PAYMENT_OPTION_REWARDS))
			return rewardsPaymentOptionBuilder;

		if (paymentOptionCode.equalsIgnoreCase(PaymentUIUtil.PAYMENT_OPTION_QUICK_BOOK))
			return quickBookOptionBuilder;

		if (paymentOptionCode.equalsIgnoreCase(PaymentUIUtil.PAYMENT_OPTION_EWALLET))
			return eWalletOptionBuilder;
		
		if (paymentOptionCode.equalsIgnoreCase(PaymentUIUtil.PAYMENT_OPTION_PAYU))
			return payuOptionBuilder;
		
		if (paymentOptionCode.equalsIgnoreCase(PaymentUIUtil.PAYMENT_OPTION_CREDIT_POOL))
			return creditPoolOptionBuilder;
		
		if (paymentOptionCode.equalsIgnoreCase(PaymentUIUtil.PAYMENT_OPTION_CORPORATE_CARD))
			return corporateCardOptionBuilder;
		
		if (paymentOptionCode.equalsIgnoreCase(PaymentUIUtil.PAYMENT_OPTION_BUDDY))
			return sbiBuddyOptionBuilder;
	
		if(paymentOptionCode.equals(PaymentUIUtil.PAYMENT_OPTION_UPI))
			return upiPaymentOptionBuilder;
		
		if(paymentOptionCode.equals(PaymentUIUtil.PAYMENT_OPTION_GOOGLE_PAY_UPI))
			return googlePayUPIOptionBuilder;
		
		if(paymentOptionCode.equals(PaymentUIUtil.PAYMENT_OPTION_GOOGLE_TEZ_PWA))
			return googleTezPWAPaymentOptionBuilder;

		if(paymentOptionCode.equals(PaymentUIUtil.PAYMENT_OPTION_DEBIT_PIN))
			return debitCardPinPaymentOptionBuilder;
		
		if(PaymentUIUtil.PAYMENT_OPTION_JCASH.equals(paymentOptionCode))
			return jaldiCashOptionBuilder;
		
		if(PaymentUIUtil.PAYMENT_OPTION_FINOPAY.equals(paymentOptionCode))
			return finoPayOptionBuilder;
		
		if(PaymentUIUtil.PAYMENT_OPTION_OXIGEN_B2B.equals(paymentOptionCode))
			return oxigenB2BOptionBuilder;
		
		if(PaymentUIUtil.PAYMENT_OPTION_INDIFI.equals(paymentOptionCode))
			return indifiPaymentOptionBuilder;
		
		if(PaymentUIUtil.PAYMENT_OPTION_PAYPAL.equals(paymentOptionCode))
			return paypalPaymentOptionBuilder;
		
		if(PaymentUIUtil.PAYMENT_OPTION_GV.equals(paymentOptionCode))
			return gvPaymentOptionBuilder;
		
		if(PaymentUIUtil.PAYMENT_OPTION_BANKIT.equals(paymentOptionCode))
			return bankItOptionBuilder;
		
		if(PaymentUIUtil.PAYMENT_OPTION_PAYNEARBY.equals(paymentOptionCode))
			return payNearByOptionBuilder;
		
		if(PaymentUIUtil.PAYMENT_OPTION_PHONEPE.equals(paymentOptionCode))
			return phonePeOptionBuilder;
		
		if(PaymentUIUtil.PAYMENT_OPTION_TSIWALLET.equals(paymentOptionCode))
			return tsiWalletPayOptionBuilder;
		
		if(PaymentUIUtil.PAYMENT_OPTION_PAYMONK.equals(paymentOptionCode))
			return payMonkOptionBuilder;
                
		if (PaymentUIUtil.PAYMENT_OPTION_GOOGLE_TEZ.equalsIgnoreCase(paymentOptionCode))
			return googleTezPaymentOptionBuilder;
                
		if(PaymentUIUtil.PAYMENT_OPTION_PAY_LATER.equals(paymentOptionCode))
			return payLaterOptionBuilder;
		
		if(PaymentUIUtil.PAYMENT_OPTION_AMAZONPAY.equals(paymentOptionCode))
			return amazonPayOptionBuilder;
		if(PaymentUIUtil.PAYMENT_OPTION_CASHPOOL.equals(paymentOptionCode))
			return cashPoolPayOpBuilder;
		
		if(PaymentUIUtil.PAYMENT_OPTION_SPICEMONEY.equals(paymentOptionCode))
			return spiceMoneyOptionBuilder;
		
		if(PaymentUIUtil.PAYMENT_OPTION_ZESTMONEY.equals(paymentOptionCode))
			return zestMoneyOptionBuilder;
		
		if(PaymentUIUtil.PAYMENT_OPTION_PAY4BIZ.equals(paymentOptionCode))
			return pay4BizOptionBuilder;
		
		if(PaymentUIUtil.PAYMENT_OPTION_ROINET.equals(paymentOptionCode))
			return roinetOptionBuilder;
		
		if(PaymentUIUtil.PAYMENT_OPTION_EBIXCASH.equals(paymentOptionCode))
			return ebixCashOptionBuilder;
		
		if(PaymentUIUtil.PAYMENT_OPTION_PAYTM.equals(paymentOptionCode))
			return paytmOptionBuilder;
		
		if(PaymentUIUtil.PAYMENT_OPTION_VAKRANGEE.equals(paymentOptionCode))
			return vakrangeeOptionBuilder;
		
		if(PaymentUIUtil.PAYMENT_OPTION_USELOCATOR.equals(paymentOptionCode))
			return useLocatorOptionBuilder;
		
		if(PaymentUIUtil.PAYMENT_OPTION_UPI_NONMOTO.equals(paymentOptionCode))
			return upiNonMotoOptionBuilder;
		
		if (PaymentUIUtil.PAYMENT_OPTION_TWID.equalsIgnoreCase(paymentOptionCode))
			return twidPaymentOptionBuilder;
		
		if(PaymentUIUtil.PAYMENT_OPTION_QUICKSON.equals(paymentOptionCode))
			return quicksonOptionBuilder;
		
		if (PaymentUIUtil.PAYMENT_OPTION_CRED.equalsIgnoreCase(paymentOptionCode))
			return credPaymentOptionBuilder;
		
		return null;
	
	}

}
