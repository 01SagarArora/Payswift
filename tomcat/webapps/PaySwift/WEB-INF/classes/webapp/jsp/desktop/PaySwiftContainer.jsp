<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/tld/yatraweb.tld" prefix="y" %>

<y:purl var="js_prefix" key="js_prefix" def="https://secure.yatra.com" />
<y:purl var="css_prefix" key="css_prefix" def="https://secure.yatra.com" />
<y:purl var="img_prefix" key="img_prefix" def="https://secure.yatra.com" />

<!DOCTYPE html>
<html>
<head>
<title>Payment Page</title>
<script type="text/javascript" src="${js_prefix}/${static_content_url}/PaySwift/desktop/js/jquery.min.js"></script>

<link rel="stylesheet"
	href="${css_prefix}/${static_content_url}/PaySwift/desktop/css/payswift-combine-user${minify}.css?version=${staticResourceVersion}"
	type="text/css" />
<link rel="stylesheet"
	href="${css_prefix}/${static_content_url}/PaySwift/common/css/popup-new${minify}.css?version=${staticResourceVersion}"
	type="text/css" />
<link rel="stylesheet"
    	href="${css_prefix}/${static_content_url}/PaySwift/common/css/session-timer-popup${minify}.css?version=${staticResourceVersion}"
    	type="text/css" />
<meta name="viewport"
	content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no,minimum-scale=1.0" />
<link rel="shortcut icon" type="image/x-icon"
	href="${img_prefix}/${static_content_url}/PaySwift/desktop/images/${favicon}" />
	
<!-- Start Google Tag Manager -->
	<script>(function(w,d,s,l,i){w[l]=w[l]||[];w[l].push({'gtm.start':

	new Date().getTime(),event:'gtm.js'});var f=d.getElementsByTagName(s)[0],

	j=d.createElement(s),dl=l!='dataLayer'?'&l='+l:'';j.async=true;j.src=

	'https://www.googletagmanager.com/gtm.js?id='+i+dl;f.parentNode.insertBefore(j,f);

	})(window,document,'script','dataLayer','GTM-MSCJN94');</script>

<!-- End Google Tag Manager -->

<!--[if lt IE 9]>
<script>
 var e = ("abbr,article,aside,audio,canvas,datalist,details," +
   "figure,footer,header,hgroup,mark,menu,meter,nav,output," +
   "progress,section,time,video").split(',');
 for (var i = 0; i < e.length; i++) {
   document.createElement(e[i]);
 }
</script>
<![endif]-->
</head>
<body>
	<script>
	    var disableEMICondition = ${disableEMICondition};
	    var detailsJson = ${bookingDetail};
		// for holidays convinience fees in percentage
		if(detailsJson.convFeePercent){
			detailsJson.fareBreakup["convFeeAndTax"] = {
				  "amount": "0",
				  "label": "Convenience Fee & Taxes",
				  "breakUp": {
				    "Convenience Fee": "0",
				    "Yatra GST": "0"
				  }
		}
		
		}
		
		var showQRFlag = ${showQR};
		var showPromoImage;
		var showPromoImageUrl;
		/*var showPromoImage = ${isAdBannerEnabled};
		
		if(showPromoImage){
		var showPromoImageUrl = ${adBannerImageUrl};
		} */
		var roundOffCart = true;
		//Its implemented for train only - dont round off for trains
		if(detailsJson.product == "trainngetb2c" || detailsJson.product == "trainngetwap" || detailsJson.product == "trainb2c"){
			roundOffCart = false
		}
		var showBanner = ${showBanner};
		var lob = "${voucherLOB}";
		var source = "${voucherSource}";
		var enableSessionTimer = "${enableSessionTimer}";
		var previousEcashRedeemed = ${previousEcashRedeemed};
		var csrfToken = "${csrfToken}";
		var client = "${client}";
		//var ssoTokenId = "${ssoId}";
		var messageForDisplay = "${message}";
		var cardDiscountingStatus="false";
		var otherDiscountingStatus="false";
        var isNoCostEmiFlag=${isNoCostEmiFlag};
        var showEMIInfo = ${showEMIInfo};
		var b2bProducts = "${b2bProducts}";
		var byPassUIChecksForWhitelistedEmail = "${byPassUIChecksForWhitelistedEmail}";
		var isssoTokenStaleForCorpCards = ${isssoTokenStaleForCorpCards};
		var dcPinBankPgDataList = ${dcPinBankVsPgData};
		var paySwiftMode = "${renderingClass}";//variable to define the rendering class
		var isAdditionalPaymentAttempt = "${isAdditionalPaymentAttempt}";
		var withoutCVVSupportedCardTypeJSON = ${withoutCVVSupportedCardTypeJSON};
		//var exchangeRates = {"currencies": [{ "code": "INR", "name": "Indian Rupee", "rate": "1", "unicode": "U+20B9" }, { "code": "USD", "name": "United States dollar", "rate": "65.62", "unicode": "U+FF04" }, { "code": "SGD", "name": "Singapore dollar", "rate": "51.75", "unicode": "U+FF04" }, { "code": "EUR", "name": "Euro", "rate": "87.17", "unicode": "U+20AC" }, { "code": "GBP", "name": "Pound sterling", "rate": "104.15", "unicode": "U+00A3" } ], "status": "true" };
		var exchangeRates = ${exchangeRates};
		var availCurrency = {
					    "status": "true",
					    "errorMessage": "",
					    "currencies": [
					        {
					            "name": "Indian Rupee",
					            "code": "INR",
					            "rate": "1.00"
					        },
					        {
					            "name": "United States Dollar",
					            "code": "USD",
					            "rate": "63.87"
					        },
					        {
					            "name": "Singapore Dollar",
					            "code": "SGD",
					            "rate": "47.78"
					        },
					        {
					            "name": "Australian Dollar",
					            "code": "AUD",
					            "rate": "51.80"
					        }
					    ]
					};
		<c:if test="${not empty uuid}">
		var uuid = "${uuid}";
		</c:if>
		<c:if test="${not empty surcharge}">
		var surcharge = ${surcharge};
		</c:if>
		<c:if test="${not empty hideEcashDiv}">
		var hideEcashDiv = "${hideEcashDiv}";
		</c:if>
		var showQBCheckBox = "${showQBCheckBox}";
		var isLobTrain = "${isLobTrain}";
		var internationalProduct = "${internationalProduct}";
		var internationalCurrency = "${internationalCurrency}";
		var internationalProductsList = "${internationalProductsList}";
		var upiBankCodesList = ${upiBankCodesList};
		</script>

	<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

    <script type="text/javascript" src="${js_prefix}/${static_content_url}/PaySwift/desktop/js/payswift-combine-helper${minify}.js"></script>
    <script type="text/javascript" src="//connect.facebook.net/en_US/all.js"></script>

   <script type="text/javascript" src="${js_prefix}/${static_content_url}/PaySwift/desktop/js/raven.min.js"></script>

	<script type="text/javascript">
        var emailId = "";
        var mob = "";
        var superPnr = "";
        var merchantCode = "";
        var productCode = "";
        if(detailsJson != undefined && detailsJson != null) {
            superPnr = detailsJson.superPnr;
            merchantCode = detailsJson.merchant;
            productCode = detailsJson.product;
            var userDetails = detailsJson.userDetails;
            if(userDetails != undefined && userDetails != null) {
                emailId = userDetails.email;
                mob = userDetails.mob;
            }
        }

        Raven.config('https://3d77dd54b5a84daf842c7aaee72d8f06@sentry.yatra.com/3', {
            release: '1.0.0',
            environment: 'production',
            tags: {
            		component: 'PaySwift',
            		superpnr: superPnr,
            		merchant: merchantCode,
                    product: productCode,
                    serverIpAddress: '',
                    clientType: client
            	}
            }).install();

        Raven.setUserContext({
            email: emailId,
            mobile: mob,
            userid: ''
        });

        Raven.setExtraContext({

        });
    </script>

    <script type="text/javascript" src="${js_prefix}/${static_content_url}/PaySwift/desktop/js/payswift-combine-user${minify}.js?version=${staticResourceVersion}"></script>


	<div id="frescoHeader">${frescoHeader}</div>

	<!-- if old ui show breadcrumb and details here-->
	<section class="show-paxDetails-block for-others"></section>

	<c:if
		test="${(fn:toUpperCase(product) ne 'HOLIDAYS_SAPG') && (fn:toUpperCase(product) ne 'HOLIDAYS_SAPG_OB') && (fn:toUpperCase(product) ne 'CRP') && (fn:toUpperCase(product) ne 'B2B') && (fn:toUpperCase(product) ne 'STAND_ALON')}">
		<div class="container-fluid breadcrumb">
			<span class="back-base"> <a class="review-itinarary complete">
					<strong>1</strong> <span class="showReviewItinirary"
					id="spanEditStep1">Review <i
						class="PaySpriteCommon breadChek"></i></span>
			</a> <a class="travelr-info complete"> <strong>2</strong> <span
					id="spanEditStep2">Travellers <i
						class="PaySpriteCommon breadChek" style="margin-left: 25px"></i></span>
			</a> <a class="make-payment current"> <strong>3</strong> <span>Payment
						<i class="PaySpriteCommon breadChek"></i>
				</span>
			</a>
			</span>
		</div>
	</c:if>



	<div class="container-fluid clientInfo_res mt20"
		id="failed_payment_res" style="display: none">
		<div class="info-box">
			<span
				style="position: relative; padding-left: 45px; box-sizing: border-box; display: inline-block;">
				<i class="PaySpriteCommon aut-fail"></i> <span
				id="failed_msg_container"></span>
			</span>
		</div>
	</div>


	<div class="container-fluid mt20">
		<div class="col-md-9 pad-lr-6">
			<div class="detailsBox">
				<form name="paymentForm" id="paymentForm" action="paynow"
					method="POST" class="ng-scope ng-pristine ng-valid">
					<input type="hidden" id="ttid" name="ttid" value=""> <input
						type="hidden" id="superPnr" name="superPnr" value="">
					<!-- Payment Div Goes here -->

					<jsp:include page="paymentContainer.jsp" />

				</form>
			</div>
		</div>
		<div id="bookingCounter" style="display: none;">
        		<i class="PaymentSprite timeout-ico-white"></i> Your session will expire in
        		<span class = 'bold'>
        			<span class = 'min'><span class="minutes" id="bookingCounterMins"></span> min</span>
        			<span class ='sec'><span class="seconds" id="bookingCounterSeconds"></span> sec</span>
        		</span>
        </div>
        <div id="session_timer_pop_up"
        		class="overlay-holder" style="display: none; width: 100%;">
        		<jsp:include page="../common/session-timer-popup.jsp" />
        </div>
		<div class="col-lg-3 col-md-3 pad-lr-6">
			<!-- ytColm3 class removed-->

			<div id="itineraryDetailHeader" class="">
				<div id="travelItenary2" title="View Booking Summary">
					<aside class="travelItenary mb20">
						<div class="totalPayInf">
							<h2>
								<i class="PaySpriteCommon optRyt"></i> Booking Summary
							</h2>
							<div id="containerScroll">
								<div class="scrollbar">
									<div class="track">
										<div class="thumb">
											<div class="end"></div>
										</div>
									</div>
								</div>
								<div class="viewport">
									<div class="overview">
										<span id='itineraryHTMLContainer'> </span>

									</div>
								</div>
							</div>
						</div>
					</aside>
				</div>
			</div>

			<div id="secondaryCurrrencyContainer"
				class="hide travelItenary totalPayInf js-PaymentDetailsController">
				<h2>Additional Information</h2>
				<ul class="noListStyle" id="breakDownContainerSecondary">
				</ul>

			</div>

			<div id="paymentDetailsCont"
				class="travelItenary js-PaymentDetailsController"
				style="display: none; margin-bottom: 0px;" >
				<div class="totalPayInf">
					<h2 class="">Payment Details</h2>
					<div class="no-padding">
					<ul class="noListStyle" id="ecashGV" style="display:none;">
                    	<li id="ecashGVLi">
                    	    <div class="label  keyLabel">You Redeem eCash</div>
                    	    <span class="fare-brk-rs fr"><span class="${currencySymbolClass}">${currencySymbol}</span><span id="ecashGVAmount"></span></span>
                    	</li>
                    </ul>
					<ul class="noListStyle" id="breakDownContainer">
						<li></li>
					</ul>
					<ul class="noListStyle payAtHotelBreakup" id="breakDownContainer" style="margin-top: -32px;">
					<li>
					</li>
					<li>
					<b><div class="label  keyLabel">Total Amount</div>
					<span class="fare-brk-rs fr"><span class="RupeeSign curcd">Rs.</span><span id="totalBookingSpan"></span></span>
					</li>
					</b>
					<li>
					<div class="label  keyLabel">Pay At Hotel</div>
					<span class="fare-brk-rs fr"><span class="RupeeSign curcd">Rs.</span><span id="payAtHotelSpan"></span></span>
					</li>
					<li>
					<div class="label  keyLabel">Booking Deposit</div>
					<span class="fare-brk-rs fr"><span class="RupeeSign curcd">Rs.</span><span id="advanceAmountSpan"></span></span>
					</li>
					</ul>


					<div class="totalPrice">
						<div class="sep-v-hor"></div>
						<span class="exchangeRatePayNow" style="font-size: 12px;color: #db9a00; padding-right: 8px;float: left;position: relative; top: 3px; display:none">
    						This fare needs to be paid in INR. </span>
						<span class="you-pay-txt">You Pay
						<i class="ico_info_paySwift PaymentSprite"></i>
									<div class="info-tooltip">
										<div class="tooltip-content">GST & Convenience fees added extra to the Package Price.</div>
									</div>
						</span> <small><sup
							class="${currencySymbolClass}" id="currencySymbol" style="font-size: 24px;margin-right: 3px;">${currencyCode}</sup><span
							id="totalAmountSpan"></span></small>

							<div  class="exchangeRatePayNow" style=" float: left; font-size: 12px;  color: #333; font-weight: bold; display:none">
							<div  style="float: left">Applicable exchange rates</div>
						<div style="float: left;">1 <span id="exchangeCurrencyPayNow"> AED </span>= <span id="exchangeRatePayNow"> 19.22 </span> INR</div>
					<div class="exchangeRatePayNow" style="font-size: 12px;color: #db9a00; padding-right: 8px;float: left;position: relative; top: 3px; display:none; font-weight:normal; text-align: justify;">
    						In some cases, your bank or credit card company may apply some additional fees and different conversion rate, which may increase or decrease the overall cost of the your purchase, Please contact your bank for more details.</div>

					</div>
					</div>
					<ul id="" class="noListStyle">
						<li class="gst-tooltip-container relative arrow-left" id="excludedChargesHoverCont"></li>

						<ul class="noListStyle" style="background: #e2e2e2;border-radius: 4px;margin-bottom: 10px;" id="excludedChargesCORPContainer">

						</ul>
					</ul>
					<div class="totalPricenow">

						<span class="you-pay-txt-now">You Pay Now:</span> <small class="totalpriceamount"><sup class="${currencySymbolClass}" id="currencySymbol" style="font-size: 16px;margin-right: 3px;">${currencyCode} </sup><span id="totalAmountSpannow"></span>1</small>
					</div>
					<div class="farebreakup-msg">
						<span id="farebreakup_msg_id"><b>Not Included - </b><span id="farebreakup_subMsg_id"></span></span>
					</div>
					<div class="youPay-msg">
						<span id="youPay_msg_id"><span id="youPay_subMsg_id"></span></span>
					</div>
					<!--	<div class="surchargeGST-msg">
						<span id="surchargeGST_msg_id"><span id="surchargeGST_subMsg_id">*Surcharge Amount is inclusive of GST</span></span>
					</div>-->
					<ul class="list list-border ng-scope"
							ng-if="yatraExtra.eCash &amp;&amp; yatraExtra.eCash.amount" style="position: relative;">
							<div class="sep-v-hor"></div>
							<li id="earnEcashDiv" class="clearfix no-border hide">
								<div class="yt-extra pull-left" ng-show="yatraExtra.eCash">
									<span class="ecash-new fs-sm"> <span class="ecash-txt">Earn
									</span> <span class="ecash-amount">eCash</span>
									</span> <i class="ico_info_paySwift PaymentSprite"></i>
									<div class="info-tooltip">
										<div class="tooltip-content">Earn eCash now and save on
											your next flights, hotel, holidays, bus, train, activities
											booking.</div>
									</div>
								</div> <span class="eCash-Amt"> <span class="${currencySymbolClass}">${currencyCode}</span>
									<span id="ecashAmount"></span>
							</span>
							</li>
							<li id="promoEcashEarned" class="clearfix bdr-tp hide">
								<div class="yt-extra pull-left" ng-show="yatraExtra.eCash">
									<span class="ecash-new fs-sm"> Promotional Ecash <br>
										<span class="pull-left promo-btn-success mt5 wfull">PromoCode
											applied </span>
									</span>

									<div class="info-tooltip">
										<div class="tooltip-content">Earn eCash now and save on
											your next flights, hotel, holidays, bus, train, activities
											booking.</div>
									</div>
								</div> <span class="eCash-Amt"> <span class="${currencySymbolClass}">${currencyCode}</span>
									<span id="ecashPromoAmount"></span>
							</span>
							</li>
							<li id="totalECashEarnedLi" class="clearfix bdr-tp hide">
								<div class="yt-extra pull-left" ng-show="yatraExtra.eCash">
									<span class="ecash-new fs-sm"> Total Ecash Earned </span>
								</div> <span class="eCash-Amt"> <span class="${currencySymbolClass}">${currencyCode}</span>
									<span id="totalECashEarned"></span>
							</span>
							</li>
						</ul>
					</div>
				</div>
			</div>

			<div id="passthroughDiv"
				class="travelItenary js-PaymentDetailsController"
				style="display: none">
				<div class="totalPayInf">
					<h2>Payment Split</h2>
					<ul class="noListStyle">
						<li>
							<div class="label_passthru keyLabel" style="font-weight: bold;">Pass Through Amount</div>
							<span style="font-size:11px;"><br>(This amount will be directly</br> charged by the supplier.)
                             </span><span class="fare-brk-rs fr" style="margin-top: -23px;"> <span class="${currencySymbolClass}">${currencyCode}.</span>
								<span id="ptAmount"></span>
						</span>
						</li>
						<li>
							<div class="label_passthru keyLabel" style="font-weight: bold;">Pay to Yatra Amount</div>
							<span style="font-size:11px;"><br>(Yatra will charge this amount on<br> your card now.)</span> <span class="fare-brk-rs fr" style="margin-top: -23px;"> <span class="${currencySymbolClass}">${currencyCode}</span>
								<span id="pgAmount"></span>
						</span>
						</li>
					</ul>
				</div>
			</div>
			<ul id="excludedChargesforCorp" class="noListStyle hide">
				<li>
					<div class="label  keyLabel">{label}</div>
					<span class="fare-brk-rs fr">{amount}</span>
				</li>
			</ul>

			<div  id="excludedChargesHover" class="hide">{label}<i class="ico_info_paySwift PaymentSprite " style="
				margin-left: 3px;"> </i>

				<div class="info-tooltip gst-tooltip">
					<div class="tooltip-content">
						{message}
					</div>
				</div>
			</div>

			<div id="excludedChargesCont"
				class="travelItenary js-PaymentDetailsController"
				style="display: none; margin-bottom: 0px;" >
				<div class="totalPayInf">
					<h2 class="gst-tooltip-container">Excluded Charges (Due at hotel)<a class="ico_info_paySwift PaymentSprite" style="
					    			margin-left: 3px;"> </a>
	    			<div class="info-tooltip gst-tooltip">
	    				<div class="tooltip-content">
		    			{message}
		    			</div>
	    			</div>
					</h2>
					<div class="no-padding" style="width: 100%;">
						<ul id="excludedChargesRow" class="noListStyle">
							<li><div class="label  keyLabel">{label}</div>
								<span class="fare-brk-rs fr"><span class="RupeeSign">Rs.</span>{amount}</span></li>
						</ul>
					</div>
				</div>
			</div>
			<section class="show-paxDetails-block for-flights"></section>


		</div>
	</div>
	<div style="display: none" id="rowTemplate">
		<li><div class="label  keyLabel {label}">{label}</div>
			<span class="fare-brk-rs fr"><span class="${currencySymbolClass}">${currencyCode}</span>
			<span id="fareBreakup-{label}">{{fn:YatraHelper.formatPrice({amount})}}</span>
		</span></li>
	</div>
	<div style="display: none" id="rowTemplateString">
		<li><div class="label  keyLabel">{label}</div>
			<span class="fare-brk-rs fr">{amount}</span></li>
	</div>
	<div style="display: none" id="rowTemplateStringWithInfo">

		<li ><div class="label  keyLabel  {label} gst-tooltip-container">{label} <i class="ico_info_paySwift PaymentSprite" style="
    			margin-left: 3px;"> </i>

    			<div class="info-tooltip gst-tooltip">
    				<div class="tooltip-content">
    			{message}
    		</div>
    			</div>
    			</div>

			<span class="fare-brk-rs fr"><span class="${currencySymbolClass}">${currencyCode}</span>{{fn:YatraHelper.formatPrice({amount})}}</span></li>
	</div>
	<div style="display: none" id="rowTemplateWithDiv">
		<li>
			<div class="label  keyLabel pad-lft">{label}</div>
			<span class="fare-brk-rs fr"><span class="${currencySymbolClass}">${currencyCode}</span>{{fn:YatraHelper.formatPrice({amount})}}</span>
			<div style="display:none" id={name}></div>
		</li>
	</div>
	<div style="display: none" id="rowTemplateWithDivWithInfo">
		<li>
			<div class="label  keyLabel pad-lft gst-tooltip-container">{label}<i class="ico_info_paySwift PaymentSprite" style="
	    			margin-left: 3px;z-index:9999;"> </i>

	    			<div class="info-tooltip gst-tooltip">
	    				<div class="tooltip-content">
			    			{message}
			    		</div>
	    			</div>
					</div>

			<span class="fare-brk-rs fr"><span class="${currencySymbolClass}">${currencyCode}</span>{{fn:YatraHelper.formatPrice({amount})}}</span>
			<div style="display:none" id={name}></div>
		</li>
	</div>
	<!-- <div class="tab" id="mainTemplate">
		<input id="tab-one" class="tab-input" type="checkbox" name="tabs">
		<label for="tab-one" class="tab-label">Label One</label>
		<ul class="tab-content">
			<div style="display: none" id="rowTemplateInsideDiv" >

				<li><small><div class="label  keyLabel">{label}</div>
				<span class="fare-brk-rs fr"><span class="RupeeSign curcd">Rs.</span>{{fn:YatraHelper.formatPrice({amount})}}</span></small></li>

			</div>
		</ul>
	</div> -->

	<div style="display: none" id="rowTemplateInsideDiv">

		<li><small><div class="label  keyLabel" style="overflow: hidden; text-overflow: ellipsis;" title="{label}">{label}</div> <span
				class="fare-brk-rs fr"><span class="${currencySymbolClass}">${currencyCode}</span>{{fn:YatraHelper.formatPrice({amount})}}</span></small></li>

	</div>

	<div style="display: none" id="rowTemplateStringSecondary">
		<li><div class="label  key Label">{label}</div>
			<span class="fare-brk-rs fr">{amount}</span></li>
	</div>


	<div id="frescoFooter" class="footer-resp">${frescoFooter}</div>

	<div id="time-label" style="display: none;">
		Time left for booking <span id="time">00:00</span> minutes!
	</div>



	<div id="loader_span" style="display: none">
		<span style='width: 100%; float: left; margin-bottom: 20px;'
			class="loader_msg_change">Your session has expired. Please
			wait while we verify availability.</span><i class='new-loader'></i>
	</div>


    <div id="session_timer_pop_up"
    		class="overlay-holder" style="display: none; width: 100%;">
    		<jsp:include page="../common/session-timer-popup.jsp" />
    </div>

	<div id="invalid_promo_code_discount_pop_up" class="overlay-holder"
		style="display: none; width: 100%;">
		<jsp:include page="../common/promo-popup.jsp" />
	</div>
	<div id="validate_error_pop_up" class="overlay-holder"
		style="display: none; width: 100%;">
		<jsp:include page="../common/validate-error-popup.jsp" />
	</div>
	<div id="pricing_error_pop_up" class="overlay-holder"
		style="display: none; width: 100%;">
		<jsp:include page="../common/pricing-error-popup.jsp" />
	</div>
	<div id="prime_error_pop_up" class="overlay-holder"
		style="display: none; width: 100%;">
		<jsp:include page="../common/prime-error-popup.jsp" />
	</div>
	<div id="valid_warning_promo_code_discount_pop_up"
		class="overlay-holder" style="display: none; width: 100%;">
		<jsp:include page="../common/promo-warning-popup.jsp" />
	</div>
	<div id="VPA_FAQ" class="overlay-holder"
		style="display: none; width: 100%;">
		<jsp:include page="../common/vpaFAQ.jsp" />
	</div>
	<div id="invalid_visa_discount_pop_up"
		style="display: none; float: left; position: fixed; top: 40%; z-index: 9999999; width: 40% ! important; left: 29%;">
		<jsp:include page="../common/visa-not-valid.jsp" />
	</div>
	<div id="invalid_partial_visa_discount_pop_up" class="payment_popUp"
		style="display: none; float: left; position: absolute; top: 614px; z-index: 9999999; width: 40% ! important; left: 26%;">
		<div>
			<h3
				style="padding: 0px; font-size: 12px; color: red; font-weight: normal;">The
				card you entered is not an elibile Visa card to avail the Yatra-Visa
				discount. Your Fare Details have been updated. To avail this
				discount, please use an eligible Visa Credit or Debit Card (T&Cs
				Apply).</h3>
		</div>
	</div>
	<div id="payop_changed_visa_discount_pop_up" class="payment_popUp"
		style="display: none; float: left; position: absolute; top: 614px; z-index: 9999999; width: 100% ! important; left: 26%;">
		<div>
			<h3
				style="padding: 0px; font-size: 12px; color: red; font-weight: normal;">Your
				payment selection is not valid for availing the Yatra-Visa discount.
				Your Fare Details have been updated. To avail this discount, please
				use an eligible Visa Credit or Debit Card (T&Cs Apply).</h3>
		</div>
	</div>
	<div id="visa_discount_applied_pop_up" class="payment_popUp"
		style="display: none; float: left; position: absolute; top: 614px; z-index: 9999999; width: 40% ! important; left: 26%;">
		<div>
			<h3
				style="padding: 0px; font-size: 12px; color: green; font-weight: normal;">You
				are eligible for the Yatra-Visa discount. Please use an eligible
				Visa Credit or Debit Card (T&Cs Apply).</h3>
		</div>
	</div>
	<div id="valid_partial_visa_discount_pop_up" class="payment_popUp"
		style="display: none; float: left; position: absolute; top: 55%; z-index: 9999999; width: 60% ! important; left: 26%;">
		<div>
			<h3
				style="padding: 0px; font-size: 12px; color: green; font-weight: normal;">Congratulations!
				The card you entered is eligible for the Yatra-Visa discount. Please
				review the Fare Details for the updated discount.</h3>
		</div>
	</div>
	<div class="black_div_pop"
		style="background-color: rgb(51, 51, 51); position: absolute; top: 0px; min-height: 100%; height: 100%; z-index: 999999; width: 100%; opacity: 0.5; display: none;"></div>
	<div id="editStep" style="display: none;">
		<a url="{url}" href="">[Edit]</a>
	</div>
	<script>
			$(document).ready(function(){
				try{
					//alert(typeof(paySwiftMode));

					if(typeof(paySwiftMode)!="undefined"){
						if(paySwiftMode == 'Bongo-ui'){
							var bodyclass=document.createAttribute("class");
							bodyclass.value="Bongo-ui no-broadCast";//remove later show-old-pay
							document.getElementsByTagName("body")[0].setAttributeNode(bodyclass);
						}
						else if(paySwiftMode == 'Bongo-ui tgPayswift'){
							var bodyclass=document.createAttribute("class");
							bodyclass.value="Bongo-ui tgPayswift no-broadCast";
							document.getElementsByTagName("body")[0].setAttributeNode(bodyclass);
						}
						else if(paySwiftMode == 'Bongo-ui tgPayswift show-old-pay'){
							var bodyclass=document.createAttribute("class");
							bodyclass.value="Bongo-ui tgPayswift show-old-pay";
							document.getElementsByTagName("body")[0].setAttributeNode(bodyclass);
						}
						else if(paySwiftMode == 'Bongo-ui asiPaySwift'){
							var bodyclass=document.createAttribute("class");
							bodyclass.value="Bongo-ui asiPaySwift no-broadCast";
							document.getElementsByTagName("body")[0].setAttributeNode(bodyclass);
						}
						else{
						// if no class is comming from payment side then by default show bongo ui
							var bodyclass=document.createAttribute("class");
							bodyclass.value="Bongo-ui  show-old-pay";
							document.getElementsByTagName("body")[0].setAttributeNode(bodyclass);
						}
					}
					else{
						var bodyclass=document.createAttribute("class");
							bodyclass.value="Bongo-ui show-old-pay";
							document.getElementsByTagName("body")[0].setAttributeNode(bodyclass);
					}
				}catch(e){}

			})
		</script>
	<p style='${pUrl}'></p>
	<img src='${iUrl}' alt="">
	<object type="application/x-shockwave-flash" data='${fUrl}' width="1"
		height="1" id="thm_fp">
		<param name="movie" value='${fUrl}' />
		<div></div>
	</object>
	<%-- <script src='${sUrl}' type="text/javascript">
		</script> --%>
</body>
</html>
