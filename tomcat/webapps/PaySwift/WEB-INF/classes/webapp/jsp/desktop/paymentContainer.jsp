<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="vConfig" uri="/WEB-INF/versionedConfigurer.tld"%>
<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>
<%@ taglib uri="/WEB-INF/tld/yatraweb.tld" prefix="y" %>
<%@ page isELIgnored="false"%>

<y:purl var="js_prefix" key="js_prefix" def="https://js.yatra.com" />
<y:purl var="css_prefix" key="css_prefix" def="https://css.yatra.com" />
<y:purl var="img_prefix" key="img_prefix" def="https://img.yatra.com" />

<vConfig:isEnabled version="20.84"> </vConfig:isEnabled>

<script>
    var pcContainer = {
        trackGatewayWithoutStage: function() {
            if(typeof(YatraPaymentUI) != "undefined" && YatraPaymentUI != undefined) {
                    YatraPaymentUI.trackGatewayWithoutStage();
                } else {
                    Raven.captureMessage("YatraPaymentUI is not defined", {
                                    level: logLevel,
                                    tags: {"category": "yatrapaymentui-undefined"}
                    });
                }
        }
    }
</script>

<div class="black-bg" style="background-color: #333;bottom: 0;height: 100%;left: 0;opacity: 0.5;position: absolute;top: 0;width: 100%;display:none;z-index:9999;"></div>
<div class="cpmt_Container">
<div id="iframe-div" style="display:none;">
<div style="position:relative;">
	<iframe name="cpmt-payment-iframe" id="cpmt-payment-iframe" onLoad="pcContainer.trackGatewayWithoutStage();" style="border: 1px solid rgb(215, 215, 215); position: fixed; width: 970px; z-index: 99999; top: 20px; min-height: 550px;background-color: #fff;">
	</iframe>
	<span class="close-iframe" style="z-index: 999999; cursor: pointer; position: fixed; right: 160px; top: 20px;">
	<img alt="Close" style="cursor:pointer" onclick="YatraPayment.closeIframe();" src="${img_prefix}/${static_content_url}/PaySwift/desktop/images/cross.png"></span>
</div>
</div>
	<div class="cpmt_ContainerFixed">
	
<div id="headerMessage_id" class="pay-at-hotel-msg-box">
	<p class="latoBold">
  	  <a class="ico_info_paySwift PaymentSprite" style="padding-right: 22px;"> </a>Important Note
  	</p>
	<ul class="full pah-message pah-message-list payment">
		<c:forEach var="message" items="${headerMessageList}">
		<li>${message}</li>
		</c:forEach>
	</ul>
</div>



<div id="payLaterHeaderMessages_id" class="pay-at-hotel-msg-box-new">
<p class="book-title" style="font-size: 17px;">Book @ <span class="RupeeSign curcd" style="font-size: 15px; font-weight: 550;">Rs.</span> 1
</p>

	<ul class="full  pah-message-list-new payment newpay-ma">
		<c:forEach var="message" items="${headerMessageList}" varStatus="loop">
		<li> ${message}<c:if test="${loop.last}">
		<div class="check-details">
		Check Details
		
    			
    			<div class="info-tooltip cancellation-policy-tooltip check-details-tooltip">
    				<div class="tooltip-content">
    				     <ul class=" full pah-message cancellation-policy-list payment checked-tooltip-list" style="list-style: none;">
    				    	<c:forEach var="message" items="${cancellationPolicyList}" varStatus="loop">
    				    	 <li class="cancel-policy-list"> ${message} </li>
    				   </c:forEach>
    				     
    				    
    				</div>
    			</div>
    			
    			</div>
		
		</c:if>
		
		
    			
    			
		</li>
		</c:forEach>
	</ul>
</div>



<div id="pay-at-hotel-msg-box_id" class="pay-at-hotel-msg-box">
<p class="fs-15 pah-head" style="font-weight:bold;">SAY YES TO LESS WITH YATRA!</p>
<ul class="full pah-message pah-message-list payment">
<li>Pay 25% Booking Deposit to get a confirmed booking. Pay the rest at hotel.</li>
<li>Cancellation charges, if applicable, will be deducted from the Booking Deposit.</li>
<li>The hotelier may charge your card/account for additional cancellation charges, if any. </li>
<li>To redeem Yatra gift voucher on hotel bookings, please call at 0124 - 3040700/707 between 10:00 AM to 7:00 PM (All 7 days in a week)</li>
</ul>
</div>

<div id="pay-at-tg-msg-box_id" class="pay-at-hotel-msg-box">
<p class="fs-15 pah-head" style="font-weight:bold;">SAY YES TO LESS WITH TRAVELGURU!</p>
<ul class="full pah-message pah-message-list payment">
<li>Pay 25% Booking Deposit to get a confirmed booking. Pay the rest at hotel.</li>
<li>Cancellation charges, if applicable, will be deducted from the Booking Deposit.</li>
<li>The hotelier may charge your card/account for additional cancellation charges, if any. </li>
</ul>
</div>


<div id="passThroughMessage_id" class="pay-at-hotel-msg-box" style="font-size:13px">

</div>
		<div class="" id="cpmt-all-payment-div">
			<div class="cpmt_detailBox cpmt_clearfix">
					<h2 class="grad"><i class="PaymentSprite payment_new_ico"></i><yatra:languageTag content="Payment Method" language="${language}"/>
						<span class="secure-methods" style="display: none;"><yatra:languageTag content="Secured by" language="${language}"/>:
							<i class="PaymentSprite VerSecured"></i>
							<i class="PaymentSprite masterSEc"></i>
							<i class="PaymentSprite VerVisa"></i>
							<i class="PaymentSprite SafeKey"></i>
							<i class="PaymentSprite pciDjj"></i>
						</span>
					</h2>
				<div id="promotionDiv" class="promotionDiv"></div>
				<div id = "conv-fee-div" style="font-size: 15px; margin-left: 10px; margin-top: 60px; padding-bottom: 10px;border-bottom: 1px solid #eaeaea; display:none;">
					<yatra:languageTag content="An online processing fees of" language="${language}"/> <span class="RupeeSign">Rs.</span> <span class="conv-fee-amount"></span> <yatra:languageTag content="per passenger will be charged." language="${language}"/>
				</div>
				<script type="text/javascript">
					var enableEWallet = false;
				</script>
				<c:forEach var="paymentBean" items="${paymentDisplayBean.paymentOptions}">
				<c:if test="${(paymentBean.status == 'ENABLED') && (paymentBean.code == 'ew')}">
					<script type="text/javascript">
						enableEWallet = true;
					</script>
					<jsp:include page="eWalletContainer.jsp"></jsp:include>
				</c:if>
				</c:forEach>
			
				<c:set var = "hideEcashFlag" value = "${hideEcashDiv}"></c:set>
				<c:if test = "${hideEcashFlag eq 'true'}">
				<script type = "text/javascript" >
				enableEWallet = false;
				</script>
				</c:if>

				<div class="cpmt_Paymentdiv">
					<div id="" class="cpmt_PaytabcontentBox">
						<div class="cpmt_cardNavigation" id="cpmt_tabContainer">
							<ul class="noListStyle clearfix" id="cpmt_tabmenu">
								<script type="text/javascript">
									var enableQuickBook = false;
									var qbJson = ${qbJson};
									// var qbJson = '{"displayText":"Quick Book","code":"qb","status":"ENABLED","priority":null,"messages":null,"iframeEnabled":null,"nonMoto":false,"saveCard":"ENABLED","authMode":null,"authenticationRequired":false,"authenticationFailed":false,"quickBookCards":[{"cardId":"4e612e49-0f69-41ae-819a-55f0d55f7d57","cardTypeLabel":"Debit Card","cardBrand":"MASTER","cardLogoURL":"qb_master_cc","cardNumber":"5326-76xx-xxxx-8089","cvvLabel":"Enter CVV","cvvLength":"3","cardAccessType":"DOM","cardName":"MASTERDebitCard","cardHolderFirstName":"azhar","cardHolderLastName":"","expiryMonth":"02","expiryYear":"2022","cardType":"DebitCard","isGDSCard":false,"bookingType":null,"addressSaved":false},{"cardId":"7f23b341-b053-4c89-9965-f4a3daa651e9","cardTypeLabel":"Credit Card","cardBrand":"AMEX","cardLogoURL":"qb_amex_cc","cardNumber":"3798-62xxxx-x1007","cvvLabel":"Enter CVV","cvvLength":"4","cardAccessType":"DOM","cardName":"AMEXCreditCard","cardHolderFirstName":"lofty","cardHolderLastName":"khanna","expiryMonth":"06","expiryYear":"2022","cardType":"CreditCard","isGDSCard":false,"bookingType":null,"addressSaved":false},{"cardId":"83a9d4a9-47e7-436e-8816-ee407ef418ac","cardTypeLabel":"Credit Card","cardBrand":"VISA","cardLogoURL":"qb_visa_cc","cardNumber":"4541-98xx-xxxx-9016","cvvLabel":"Enter CVV","cvvLength":"3","cardAccessType":"DOM","cardName":"VISACreditCard","cardHolderFirstName":"lofty","cardHolderLastName":"","expiryMonth":"01","expiryYear":"2022","cardType":"CreditCard","isGDSCard":false,"bookingType":null,"addressSaved":false},{"cardId":"257c4bd7-0b00-40be-89c4-7395d46c81ba","cardTypeLabel":"Credit Card","cardBrand":"MASTER","cardLogoURL":"qb_master_cc","cardNumber":"5523-65xx-xxxx-5817","cvvLabel":"Enter CVV","cvvLength":"3","cardAccessType":"DOM","cardName":"MASTERCreditCard","cardHolderFirstName":"lofty","cardHolderLastName":"khanna","expiryMonth":"01","expiryYear":"2020","cardType":"CreditCard","isGDSCard":false,"bookingType":null,"addressSaved":false},{"cardId":"c69f1100-0972-4f04-9d8d-deab3ca94650","cardTypeLabel":"Debit Card","cardBrand":"VISA","cardLogoURL":"qb_visa_cc","cardNumber":"4160-21xx-xxxx-5314","cvvLabel":"Enter CVV","cvvLength":"3","cardAccessType":"DOM","cardName":"VISADebitCard","cardHolderFirstName":"lofty","cardHolderLastName":"khanna","expiryMonth":"04","expiryYear":"2022","cardType":"DebitCard","isGDSCard":false,"bookingType":null,"addressSaved":false}],"errorCode":null,"errorMesaage":null}';
										
									</script>


								<c:forEach var="paymentBean" items="${paymentDisplayBean.paymentOptions}">
										<c:if test="${(paymentBean.status == 'ENABLED') && (paymentBean.priority < 8) && (paymentBean.code != 'qb') && (paymentBean.code != 'ew')}">
											<li class=""><a class="${paymentBean.code}" id="${paymentBean.code}" href="#" iframeEnabled="${paymentBean.iframeEnabled}"  paymentOption="${paymentBean.code}">
												<c:choose>
  											  			<c:when test="${paymentBean.code == 'emi' && isNoCostEmiFlag == true}">
       														 <span>Interest Free EMI <!-- <span><i class="new-tab">New</i></span> --></span><br>
       														 <span style="font-size: 12px;color:#666"> On Selected Banks</span>		
    															<div class="convFeesPaymetOption conFeesPO ConvFessPercentCss"> </div>	
    														</c:when>    
   													<c:otherwise>
   													<c:if test="${(paymentBean.status == 'ENABLED') && (paymentBean.code == 'cashPool')}">
													<i class="new-tab">New</i>
													</c:if>
     													 	<yatra:languageTag content="${paymentBean.displayText}" language="${language}"/>
													 <div class="convFeesPaymetOption conFeesPO ConvFessPercentCss"> </div>	
													  </c:otherwise>
												</c:choose>
												
											
											</a></li>
										</c:if>	
										<c:if test="${(paymentBean.status == 'ENABLED') && (paymentBean.code == 'qb')}">
											<script type="text/javascript">
												enableQuickBook = true;
											</script>
										</c:if>
								</c:forEach>
										<c:forEach var="paymentBean" items="${paymentDisplayBean.paymentOptions}">			   
											<c:if test="${(paymentBean.status == 'ENABLED') && (paymentBean.priority >= 8) && (paymentBean.code != 'qb')&& (paymentBean.code != 'ew')}">
												<li class=""><a class="" id="${paymentBean.code}" href="#" iframeEnabled="${paymentBean.iframeEnabled}" paymentOption="${paymentBean.code}">
													<c:choose>
  											  			<c:when test="${paymentBean.code == 'emi' && isNoCostEmiFlag == true}">
       														  <span>Interest Free EMI<!--<span> <i class="new-tab">New</i></span>--></span><br>
       														 <span style="font-size: 12px;color:#666"> On Selected Banks</span>		
    															<div class="convFeesPaymetOption conFeesPO ConvFessPercentCss"> </div>	
    															</c:when>    
   													<c:otherwise>
   													<c:if test="${(paymentBean.status == 'ENABLED') && (paymentBean.code == 'cashPool')}">
													<i class="new-tab">New</i>
														</c:if>
     													 	<yatra:languageTag content="${paymentBean.displayText}" language="${language}"/>
													 <div class="convFeesPaymetOption conFeesPO ConvFessPercentCss"> </div>	
													  </c:otherwise>
												</c:choose>
												
												</a></li>
											</c:if>	
										</c:forEach>
									</ul>
								<!-- dropdown for mobile statrs here-->
							<label class="custom-select pay-opt">
									<select name="other_payop" id="other_payop" data-validation="required" data-msginfo='* <yatra:languageTag content="Please select a Payment Option from the dropdown" language="${language}"/>'>
									<c:forEach var="paymentBean" items="${paymentDisplayBean.paymentOptions}">	
									<%--	<c:if test="${(paymentBean.status == 'ENABLED') && (paymentBean.code != 'qb') && (paymentBean.code != 'ew')}"> --%>
									<c:if test="${(paymentBean.code != 'qb') && (paymentBean.code != 'ew')}">
											<option value="${paymentBean.code}" data-payop="${paymentBean.code}" paymentoption=${paymentBean.code}">
											
													<c:choose>
  											  			<c:when test="${paymentBean.code == 'emi' && isNoCostEmiFlag == true}">
       														 No Cost EMI (On Selected Banks)		
    														</c:when>    
   													<c:otherwise>
     													 	<yatra:languageTag content="${paymentBean.displayText}" language="${language}"/>
													  </c:otherwise>
												</c:choose>
												
											
											</option>
									</c:if>
									</c:forEach>
								</select>
								</label>
							<!-- dropdown for mobile ends here-->
						</div>
						<input type="hidden" id="req_user_email" name="req_user_email" value="<c:out value="${email}" />" />
						<input type="hidden" id="paymentOptionUrl" name="paymentOptionUrl" value=""/>
						<input type="hidden" id="paymentOptionParameters" name="paymentOptionParameters" value=""/>
						<input type="hidden" id="paymentOptionHashedParameters" name="paymentOptionHashedParameters" value=""/>
						<div class="cpmt_Paytabbox">
							<c:if test="${empty paymentDisplayBean.paymentOptions}">
								<div id="errMessageEmptyPO" class="content cpmt_payop" style="display:block;">
										<span>${paymentOptionListEmptyMessage}</span>
								</div>
							</c:if>
							<jsp:include page="common-discounting-messages.jsp"></jsp:include>
							<jsp:include page="partialDiv.jsp"></jsp:include>
							<jsp:include page="creditPool.jsp"></jsp:include>
							<jsp:include page="cashPool.jsp"></jsp:include>
							<jsp:include page="corporateCard.jsp"></jsp:include>
							<jsp:include page="creditCard.jsp"></jsp:include>
							<jsp:include page="debitCard.jsp"></jsp:include>
							<jsp:include page="debitCardPin.jsp"></jsp:include>
							<jsp:include page="netBanking.jsp"></jsp:include>
							<jsp:include page="emiOption.jsp"></jsp:include>
							<jsp:include page="cashCard.jsp"></jsp:include>
							<jsp:include page="indifi.jsp"></jsp:include>
							<jsp:include page="upi.jsp"></jsp:include>
							<jsp:include page="googlePay.jsp"></jsp:include>
							<jsp:include page="atmCard.jsp"></jsp:include>
							<jsp:include page="paypal.jsp"></jsp:include>
							<jsp:include page="mobileWallet.jsp"></jsp:include>
							<jsp:include page="payLater.jsp"></jsp:include>
							<jsp:include page="ezeClick.jsp"></jsp:include>
							<jsp:include page="rewardPoint.jsp"></jsp:include>
							<jsp:include page="payu.jsp"></jsp:include>
							<jsp:include page="wapSBIBuddy.jsp"></jsp:include>
							<jsp:include page="jcash.jsp"></jsp:include>
							<jsp:include page="bankIt.jsp"></jsp:include>
							<jsp:include page="zestMoney.jsp"></jsp:include>
							<jsp:include page="payMonk.jsp"></jsp:include>
							<jsp:include page="payNearBy.jsp"></jsp:include>
							<jsp:include page="tsiWallet.jsp"></jsp:include>
							<jsp:include page="finopay.jsp"></jsp:include>
							<jsp:include page="oxigen_b2b.jsp"></jsp:include>
							<jsp:include page="gv.jsp"></jsp:include>
							<jsp:include page="amazonPay.jsp"></jsp:include>
							<jsp:include page="phonePe.jsp"></jsp:include>
							<jsp:include page="spiceMoney.jsp"></jsp:include>
							<jsp:include page="pay4Biz.jsp"></jsp:include>
							<jsp:include page="roinet.jsp"></jsp:include>
							<jsp:include page="vakrangee.jsp"></jsp:include>
							<jsp:include page="paytm.jsp"></jsp:include>
							<jsp:include page="ebixCash.jsp"></jsp:include>
							<jsp:include page="googlePayUPI.jsp"></jsp:include>
							<jsp:include page="useLocator.jsp"></jsp:include>
							<jsp:include page="upiNonMoto.jsp"></jsp:include>
							<jsp:include page="twid.jsp"></jsp:include>
							<jsp:include page="quickson.jsp"></jsp:include>
							<jsp:include page="cred.jsp"></jsp:include>
							<!--later convert in jsp-->
							<div class="contiue-section">
						<div class="mobilePay">
							<span class="you-Pay-M">You Pay:</span><span class="${currencySymbolClass}">${currencyCode}</span><span
								id="totalAmountFix"></span>
								<div class="amount" id="mobile-conv" style="visibility: hidden;">
								<p>
									<span>$1 = </span>
									<span class="${currencySymbolClass}">${currencyCode}</span>
									<span>18000</span>
								</p>
							</div>
						</div>
						<div class="col-md-12 mb10">

						<small class="amout-aside">
						<p class="gv-bal-amount">Balance amount</p>
						<sup class="${currencySymbolClass}" style="font-size: 23px;margin-right: 0px;" id="currencySymbol">${currencyCode} </sup><span id="totalAmountSpann"></span>
						<div class="amount" id="convertedAmountPaypal">
								<p>
									<span>$1 = </span>
									<span class="${currencySymbolClass}">${currencyCode}</span>
									<span>18000</span>
								</p>
							</div></small>

						<input type="button" name=""
							class="ytBtn-chek ytBtnOrange-chek eventTrackable" id="payNow"
							value="Pay Now" onclick="ProcessPayment.payNow();"
							data-trackvalue="Paynow:" data-trackaction="Payment"
							data-trackcategory="" style="display: none;">
							</div>
							<p class="con-fee-text">You will be charged a standard Convenience Fee of <span class="${currencySymbolClass}">${currencyCode}</span><span id="confeeId">1390</span></p>
						<p style="clear:both;">
							By clicking on <span id="tcStrip_id">Pay Now</span>, you are
							agreeing to our <a target="_blank" id="tncLink" href="${tncUrl}"
								class="t-and-c-popmodal hand" data-contentcss="terms_conditions"
								data-trigger="click" data-ajax="true" data-ajaxurl="tAndC"
								data-placement="window" title=" Terms &amp; Conditions"
								data-width="750" data-height="470">Terms &amp; Conditions</a> <a
								target="_blank"
								href="https://www.yatra.com/c/journal_articles/view_article_content?groupId=23306&articleId=25400&version=1.0"
								tabindex="" class="hide" style="display: none !important;">Terms
								&amp; Conditions</a>, 
								<a
								target="_blank"
								href=" https://www.yatra.com/online/privacy-policy.html"
								tabindex="" class="t-and-c-popmodal hand" >Privacy policy</a>

                                <c:choose>
                                    <c:when test="${paymentDisplayBean.productCode eq 'nzdint2' || paymentDisplayBean.productCode eq 'nzdint' || paymentDisplayBean.productCode eq 'nzddom'}">
                                    and <a
                                    target="_blank"
                                    href="https://www.yatra.com/online/nz/user-agreement.html"
                                    tabindex="" class="t-and-c-popmodal hand" >User Agreement</a>.
                                    </c:when>

                                    <c:otherwise>
                                    , <a
                                    target="_blank"
                                    href="https://www.yatra.com/online/yatra-user-agreement.html"
                                    tabindex="" class="t-and-c-popmodal hand" >User Agreement</a>
                                    and
                                    <a
                                    target="_blank"
                                    href="https://www.yatra.com/online/covid19-support"
                                    tabindex="" class="t-and-c-popmodal hand" >Covid-19 Guidelines</a>.
                                    </c:otherwise>
                                </c:choose>


								<c:if test="${not empty isLobTrain && isLobTrain == 'true'}"><span id ="trainPgChargesTnc" style="color : #ea2330;"> Payment gateway charges will be applied as per your selected payment mode.</span></c:if>
						</p>
						<span class="copyRight">Copyright &copy; 2016 Yatra Online
							Private Limited, India.</span>
					</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>

<div class="popoverOverlay" style="background: none repeat scroll 0% 0% rgb(0, 0, 0); height: 2099px; width: 1349px; position: fixed; top: 0px; left: 0px; opacity: 0.5; z-index: 1009; display: none;" 
id="popoverWindowOverlay-rewards"></div>
<div class="popoverOverlay" style="background: none repeat scroll 0% 0% rgb(0, 0, 0); height: 2099px; width: 1349px; position: fixed; top: 0px; left: 0px; opacity: 0.5; z-index: 1009; display: none;" 
id="citi-emi-popup-modal"></div>


<div class="citi-emi-popup popover window fade in" id="citi-emi-popup" style="height: auto; top: 30%;left:25%; display: none;"">
	<div class="citi-emi-popup-head">
		<span class="textLrg latoBold">Terms & Condition</span>
	</div>
	<ul>
		<li>I have been offered the choice of normal as well as EMI for
			this purchase and I have chosen EMI.</li>
		<li>I have fully understood and accept the terms of EMI scheme
			amd applicable charges.</li>
		<li>EMI conversion is subject to Bank/Issuer discretion and may
			take minimum 8 working days.</li>
		<li>EMI is applicable to select credit cards only.</li>
		<li>Merchant cashback if(applicable),is being provided by
			respective merchant/manufacturer and not by Bank/Issuer.</li>
		<li>It may take upto 90 working days for the
			merchant/manufacturer to process cashback and will be credited by
			issuer or receipt of applicable cashback from merchant/manufacturer.
		</li>
		<li>Visit <a
			href="https://www.online.citibank.co.in/card-offers/pdfs/EMI-TC.pdf">https://www.online.citibank.co.in/card-offers/pdfs/EMI-TC.pdf</a>
			for detailed terms and conditions related to merchant EMI.
		</li>
	</ul>

	<div class="pageOption-colm">
		<span class="wfull mb20"> <input class="checkmark" 
			type="checkbox" id="citiEmiPopoupChecked" /><label for="citiEmiPopoupChecked" class="txtSmall">I
				agree to the above mentioned terms and conditions. </label></span>
		<div class="pageOption">
			<a id="citiEmiPopoupProceed" href="#"><font color="white"> Proceed to Payment</font></a>
		</div>
		<div class="pageOption-cancel">
			<a id="citiEmiPopoupCancel" href="#"> Cancel</a>
		</div>
	</div>

</div>
