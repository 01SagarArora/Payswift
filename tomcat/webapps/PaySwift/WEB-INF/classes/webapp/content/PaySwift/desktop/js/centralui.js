var intl_productsArr = new Array("dom2","us","uk","int","int2","dom","STAND_ALON","INT_HOTEL","HOTEL_IN","rescheduling","crpint","crpdom","crpdom_hotel","crpint_hotel","byop","bus","trainngetb2c","homestays","activities","metadom","metaint","busdesktop","buswap","trainngetwap","crpdomhotel","crpinthotel","yatramonuments","asimonuments","corpSAPG","corpInsurance","cinsweb","smeodom","smepdom","smeoint","smepint","frndom","frnint","aedint","aeddom","wegoaeddom","wegousddom","kayakusddom","kayakusdint","wegousdint","wegoaedint","FRN_HOTEL_IN","FRN_INT_HOTEL","INT_HOTEL_PREPAY","kwddom","kwdint","SmedoB2c","SmeioB2c","SmedpB2c","SmeipB2c","corpbusdesktop","googledom","googleint","trainb2c");
var int_card_two_days_allowing_products = new Array("INT_HOTEL","HOTEL_IN","homestays","FRN_HOTEL_IN","FRN_INT_HOTEL","INT_HOTEL_PREPAY","SmedoB2c","SmeioB2c","SmedpB2c","SmeipB2c");
var int_card_travel_date_ignore_products = new Array("crpdom","crpint","crpint_hotel","crpdom_hotel","crp","crpdomhotel","crpinthotel","corpSAPG","corpInsurance","cinsweb","corpbusdesktop");
var intlPayOpArr = new Array("cc", "dc");
var emiInterestRate = 12;
var partialPay_productsArr = new Array("HOLIDAY_IN","dom2","int2","HOLIDAYS_SAPG","HOLIDAYS_OB","HOLIDAYS_SAPG_OB","HOLIDAYS_MP_ONLINE","metadom","metaint","FR_HOLIDAYS_SAPG","FR_HOLIDAY_IN","smeodom","smepdom","smeoint","smepint","frndom ","frnint ","aedint ","aeddom ","wegoaeddom","wegousddom","kayakusddom","kayakusdint","wegousdint","wegoaedint");
var partial_payOpArr = new Array("cc", "dc", "nb", "cashCard","ec", "mw", "rewards", "atm","itz", "emi", "ew", "upi", "amazonPay", "gv","ebixcash");
var holdAndPay_productsArr = new Array("HOLIDAY_IN","HOLIDAYS_OB","HOLIDAYS_SAPG_OB","dom2","HOLIDAYS_SAPG","HOLIDAYS_MP_ONLINE","metadom","metaint","FR_HOLIDAYS_SAPG","FR_HOLIDAY_IN","smeodom","smepdom","smeoint","smepint","frndom ","frnint ","aedint ","aeddom ","wegoaeddom","wegousddom","kayakusddom","kayakusdint","wegousdint","wegoaedint");

var holdAndPay_payOpArr =  new Array("cc", "dc", "nb", "cashCard","ec", "mw", "rewards", "atm","itz", "emi", "ew", "upi", "amazonPay", "gv","ebixcash");

var rupayBinRange = new Array("508500-508999","606985-607384","607385-607484","607485-607984","608001-608100","608101-608200","608201-608300","608301-608350","608351-608500","652150-652849","652850-653049","653050-653149");
var productMap = new Object();
var adobeProductMap = new Object();
var productMapGAPageView = new Object();
var rwConversion = 0;
var continueClass;
var stageCounter = 0;
var isCitiEmiPopupTncValidated = false;
var dcPinBankVsPgData = new Object();
//var productsOnInternationalGateway = new Array();
var productsOnInternationalGateway = internationalProductsList.substring(1, internationalProductsList.length - 1).split(", ");
var sessionTimerInterval;
var digitalDataAdobe = new Object();
var page = new Object();
var binNumber = "000000";
var paymentDivHeight = parseInt($('.cpmt_Paytabbox').css('min-height'));
//Reset EMI Card details .. only below function need to be called when its rerendered ...
$("#emiBank_select").val('');
$('#TncAgree').prop('checked', false);

$(function(){
    $("#payerVA").autocomplete({
                 source: function(request, response) {
                     var term = request.term.trim();
                     if (!term.includes('@')) {
                         response([]);
                         return;
                     }
                     var searchTerm = term.substring(term.lastIndexOf('@'));
                     var filteredTerms = upiBankCodesList.filter(function(tag) {
                         return tag.startsWith(searchTerm);
                     });
                     response(filteredTerms);
                 },
                 focus: function(event, ui) {
                     event.preventDefault(); // Prevent the widget from updating the input value
                     var typedValue = $(this).val().split('@')[0];
                     var selectedOption = ui.item.value;
                     $(this).val(typedValue + selectedOption);
                 },
                 select: function(event, ui) {
                     event.preventDefault(); // Prevent the widget from updating the input value
                     var typedValue = $(this).val().split('@')[0];
                     var selectedOption = ui.item.value;
                     $(this).val(typedValue + selectedOption);
                     return false;
                 },
                 open: function(event, ui) {
                         // Check if upiBankCodes is empty
                         if (upiBankCodesList.length === 0) {
                             // Disable autocomplete if upiBankCodes is empty
                             $(this).autocomplete("disable");
                         }
                              // Add scrolling functionality to the autocomplete menu
                              var $menu = $(this).autocomplete("widget");
                              $menu.css("max-height", "200px"); // Set max height for the menu
                              $menu.css("overflow-y", "auto"); // Enable vertical scrolling
                 }
     });
 });

//making it generic for all banks
// making it global as it is getting used in more than 1 place
var maxBookingValuePossible = 999999999// giving it max booking value for making it generic // 99 crore


//var disableEMICondition =${disableEMICondition};
//disableEMICondition = {"amex":{"3":{"min":5000,"max":99999999},"6":{"min":5000,"max":99999999},"9":{"min":5000,"max":99999999},"12":{"min":5000,"max":99999999}},"bajaj":{"2":{"min":1998,"max":20000},"3":{"min":2997,"max":30000},"6":{"min":5994,"max":60000},"9":{"min":8991,"max":90000},"12":{"min":11988,"max":120000}},"icici":{"3":{"min":3000,"max":99999999},"6":{"min":3000,"max":99999999},"9":{"min":3000,"max":99999999},"12":{"min":3000,"max":99999999}},"axis":{"3":{"min":3000,"max":99999999},"6":{"min":3000,"max":99999999},"9":{"min":3000,"max":99999999},"12":{"min":3000,"max":99999999}},"hdfc":{"3":{"min":3000,"max":99999999},"6":{"min":3000,"max":99999999},"9":{"min":3000,"max":99999999},"12":{"min":3000,"max":99999999}},"yes":{"3":{"min":3000,"max":99999999},"6":{"min":3000,"max":99999999},"9":{"min":3000,"max":99999999},"12":{"min":3000,"max":99999999}},"bob":{"3":{"min":3000,"max":99999999},"6":{"min":3000,"max":99999999},"9":{"min":3000,"max":99999999},"12":{"min":3000,"max":99999999}},"hsbc":{"3":{"min":3000,"max":99999999},"6":{"min":3000,"max":99999999},"9":{"min":3000,"max":99999999},"12":{"min":3000,"max":99999999}},"standardchartered":{"3":{"min":3000,"max":99999999},"6":{"min":3000,"max":99999999},"9":{"min":3000,"max":99999999},"12":{"min":3000,"max":99999999}},"citi":{"3":{"min":3000,"max":99999999},"6":{"min":3000,"max":99999999},"9":{"min":3000,"max":99999999},"12":{"min":3000,"max":99999999}},"sbi":{"3":{"min":3000,"max":99999999},"6":{"min":3000,"max":99999999},"9":{"min":3000,"max":99999999},"12":{"min":3000,"max":99999999}},"kotak":{"3":{"min":3000,"max":99999999},"6":{"min":3000,"max":99999999},"9":{"min":3000,"max":99999999},"12":{"min":3000,"max":99999999}},"rbl":{"3":{"min":3000,"max":99999999},"6":{"min":3000,"max":99999999},"9":{"min":3000,"max":99999999},"12":{"min":3000,"max":99999999}},"indusind":{"3":{"min":3000,"max":99999999},"6":{"min":3000,"max":99999999},"9":{"min":3000,"max":99999999},"12":{"min":3000,"max":99999999}},"dkotak":{"3":{"min":4000,"max":99999999},"6":{"min":4000,"max":99999999},"9":{"min":4000,"max":99999999},"12":{"min":4000,"max":99999999}},"dhdfc":{"3":{"min":5000,"max":99999999},"6":{"min":5000,"max":99999999},"9":{"min":5000,"max":99999999},"12":{"min":5000,"max":99999999}},"dbob":{"3":{"min":4000,"max":99999999},"6":{"min":4000,"max":99999999},"9":{"min":4000,"max":99999999},"12":{"min":4000,"max":99999999}},"dfdrl":{"3":{"min":4000,"max":99999999},"6":{"min":4000,"max":99999999},"9":{"min":4000,"max":99999999},"12":{"min":4000,"max":99999999}},"onecard":{"3":{"min":2500,"max":99999999},"6":{"min":2500,"max":99999999},"9":{"min":2500,"max":99999999},"12":{"min":2500,"max":99999999}}};

//Change min_amount and max_amount in above disableEMICondition as shared by Marketing team

var YatraPaymentUI = {
		d5errorMessage : "The date of travel should be at least 5 days from the date of booking in case of international cards. Please use some other mode of payment.",
		d3errorMessage : "The date of travel should be at least 3 days from the date of booking in case of international cards. Please use some other mode of payment.",
		d2errorMessage : "The date of travel should be at least 2 days from the date of booking in case of international cards. Please use some other mode of payment.",
		intlWarningMessage : 'International cards are not supported for this transaction. Please use a card issued in India, or a different mode of payment e.g. Net banking',
		intlerrorMessage : 'This transaction cannot be completed using an international credit card. Please use a card issued in India, or a different mode of payment e.g. Net banking',
		amexWarningMessage : 'We accept AMEX cards issued in India. If you have a AMEX card issued outside India, please use a different mode of payment e.g. Net Banking.',
		emiIntlErrorMessage : 'International credit cards are not supported in case of EMI payment option',
		rewardsIntlErrorMessage : 'International credit cards are not supported in case of Reward points payment option',
		paymentProcessType : "Secure",
		cvvRequired : "true",
		isNoCostEmiFlag : false,

		recordEventInSentry: function(message, logLevel, category, extraKey, extraValue) {
			var extraObj = {};
			if(extraKey != undefined && extraValue != undefined){
				extraObj[extraKey] = extraValue;
			}
			Raven.captureMessage(message, {
				level: logLevel,
				tags: {"category": category},
				extra: extraObj
			}
			);
		},

		recordEventInSentryWithObj: function(message, logLevel, category, extraObject) {
			Raven.captureMessage(message, {
				level: logLevel,
				tags: {"category": category},
				extra: extraObject
			}
			);
		},

		formEmptyCCorDC: function(formType) {
			if($("#" +formType +"_cno_id").val()!="" || $("#" +formType +"_cardholder_name_id").val()!="" || $("#" +formType +"_expMonth_id").val()!="" || $("#" +formType +"_cvv_id").val()!="" || $("#" +formType +"_expy_id").val()!=""){
				return true
			}else{
				return false
				}
		},


		trackGatewayWithoutStage : function() {
			stageCounter++;
			YatraPaymentUI.trackGatewayWithStage(stageCounter);
		},

		trackGatewayWithStage : function(stage) {
			try{
				var paymentJSON = getPaymentJSON();
				var tracking_id = paymentJSON.tracking_id;
				if(tracking_id != null && tracking_id != "" && tracking_id != "undefined") {
					$.ajax({
						url : "/checkout/gateway/tracking/save-url.htm?ttid=" + tracking_id + "&stage=" + stage, cache : false,
						success : function(response) {
						}, error : function(request, status, error) {
						}
					});
				}
			}catch(e){}
		},

		ewalletRedemptionUpdateOnGSTIncrease: function(prevTotalAmountPayable, prevGSTAmount, currentGSTAmount) {
			var nexpEcash = YatraEWalletPaymentUI.nexpEcash;
			if(nexpEcash > 0) {
				var currentTotalAmountPayable = prevTotalAmountPayable - prevGSTAmount + currentGSTAmount;
				var redeemedAmount = YatraEWalletPaymentUI.getAmountInRupee(redeemAuthJSON.amountInPaisa);
				if(currentTotalAmountPayable > prevTotalAmountPayable && redeemedAmount < currentTotalAmountPayable) {
					//reverse auth as well as set ewallet ui after reverse auth
					YatraEWalletPaymentUI.reverseAuth();
					YatraPaymentUI.recordEventInSentry("Ecash Reverse Auth due to updated GST", "info", "Ecash Redemption GST");
					$("#redeem-ecash-button").trigger('click', 'auto');
				}
			}
		},

		setExpiryYearData : function() {

			var currYear = new Date().getFullYear();

			var dropDown1 = $("#cc_expy_id")
			var dropDown2 = $("#dc_expy_id")
			var dropDown3 = $("#rw_expy_id")
			var dropDown4 = $("#emi_expy_id")
			var dropDown5 = $("#dcpin_expy_id")

			for ( var i = 1; i <= 40; i++) {
				var key = currYear;
				var value = currYear;

				if (dropDown1 != null && typeof (dropDown1) != "undefined") {
					dropDown1.append("<option value='" + key + "'>"+ value +"</option>");
				}
				if (dropDown2 != null && typeof (dropDown2) != "undefined") {
					dropDown2.append("<option value='" + key + "'>"+ value +"</option>");
				}
				if (dropDown3 != null && typeof (dropDown3) != "undefined") {
					dropDown3.append("<option value='" + key + "'>"+ value +"</option>");
				}
				if (dropDown4 != null && typeof (dropDown4) != "undefined") {
					dropDown4.append("<option value='" + key + "'>"+ value +"</option>");
				}
				if (dropDown5 != null && typeof (dropDown5) != "undefined") {
					dropDown5.append("<option value='" + key + "'>"+ value +"</option>");
				}

				currYear = currYear + 1;
			}
		},

		addDcPinBankVsPgData : function() {
			$.each(dcPinBankPgDataList, function( index, value ) {
				dcPinBankVsPgData[value.bank_code] = value.cardfields_show;
			});
		},

		generateDcPinLogos : function() {
			$("#dcpincardlogos").empty();
			$.each(dcPinCardLogoVsCode, function(logourl, cardcode) {
				$("#dcpincardlogos").append("<i class='PaymentSprite " + logourl + "' title='" + cardcode + "' ></i>");
			});
		},

		addProductsToGAMap : function () {
			productMap["HOLIDAY_IN"] = "Common Payment Page - HOLIDAYS";
			productMap["HOLIDAYS_OB"] = "Common Payment Page - HOLIDAYS";
			productMap["dom2"] = "Common Payment Page - DOM FLIGHTS";
			productMap["int2"] = "Common Payment Page - OB FLIGHTS";
			productMap["HOTEL_IN"] = "Common Payment Page - HOTELS";
			productMap["trainngetb2c"] = "Common Payment Page - TRAINS";
			productMap["RAIL"] = "Common Payment Page - TRAINS";
			productMap["bus"] = "Common Payment Page - BUS";
			productMap["metadom"] = "Common Payment Page - DOM FLIGHTS";
			productMap["metaint"] = "Common Payment Page - OB FLIGHTS";
			productMap["homestays"] = "Common Payment Page - HOMESTAYS";

			productMap["mdomwap"] = "WAP Common Payment Page - DOM FLIGHTS";
			productMap["mintwap"] = "WAP Common Payment Page - OB FLIGHTS";
			productMap["mdomhotelwap"] = "WAP Common Payment Page - HOTELS";
			productMap["trainngetwap"] = "WAP Common Payment Page - TRAINS";
			productMap["HOLIDAY_WAP"] = "WAP Common Payment Page - HOLIDAYS";

			productMapGAPageView["HOLIDAY_IN"] = "/vpv/coreTrans/Holidays/payment";
			productMapGAPageView["HOLIDAYS_OB"] = "/vpv/coreTrans/Holidays/payment";
			productMapGAPageView["dom2"] = "/vpv/coreTrans/DOMFlights/payment";
			productMapGAPageView["int2"] = "/vpv/coreTrans/OBFlights/payment";
			productMapGAPageView["HOTEL_IN"] = "/vpv/coreTrans/Hotels/payment";
			productMapGAPageView["trainngetb2c"] = "/vpv/coreTrans/Trains/payment";
			productMapGAPageView["RAIL"] = "/vpv/coreTrans/Trains/payment";
			productMapGAPageView["bus"] = "/vpv/coreTrans/Bus/payment";
			productMapGAPageView["metadom"] = "/vpv/coreTrans/DOMFlights/payment";
			productMapGAPageView["metaint"] = "/vpv/coreTrans/OBFlights/payment";

			productMapGAPageView["mdomwap"] = "/vpvw/coreTrans/DOMFlights/payment";
			productMapGAPageView["mintwap"] = "/vpvw/coreTrans/OBFlights/payment";
			productMapGAPageView["mdomhotelwap"] = "/vpvw/coreTrans/DOMHotels/payment";
			productMapGAPageView["trainngetwap"] = "/vpvw/coreTrans/Trains/payment";
			productMapGAPageView["HOLIDAY_WAP"] = "/vpvw/coreTrans/Holidays/payment";
			productMapGAPageView["homestays"] ="/vpv/coreTrans/HomeStays/payment";
		},

		pushGAInfoPageView : function(){
			try{
				var paymentJSON = getPaymentJSON();
				if(paymentJSON.gavpv){
					//_gaq.push(['_trackPageview', paymentJSON.gavpv]);
					if(typeof yt_ga != 'undefined'){
						yt_ga.trackPageView(paymentJSON.gavpv);
					}

				}
				else if(productMapGAPageView[paymentJSON.product]){
					//_gaq.push(['_trackPageview', productMapGAPageView[paymentJSON.product]]);
					if(typeof yt_ga != 'undefined'){
						yt_ga.trackPageView(productMapGAPageView[paymentJSON.product]);
					}
				}
			}catch(e){
				//console.log(e);
			}
		},

		pushGAInfoGeneric : function(paymentMethod, action) {
			try{
				var paymentJSON = getPaymentJSON();
				var productCode = paymentJSON.product;

				var paymentPageText = productMap[productCode];
				_gaq.push([ '_trackEvent', paymentPageText, paymentMethod, action]);

			}catch(e){
				//console.log(e);
			}
		},

		adobeTrackPaymentQBData : function(){
		var product = detailsJson.product;
     	 if(adobeProductMap[product] != '' && typeof adobeProductMap[product] != 'undefined'){
		try{

			   var lobVal = "";
	           var appName = 'yt';
	           var pageTenantAdobe = "";

	           var adobeFieldData = adobeProductMap[product];
               var fields = adobeFieldData.split("|");
               pageTenantAdobe=fields[0];
               lobVal=fields[2];

	             var pageNameVal = appName+":"+lobVal+":"+pageTenantAdobe+":checkout:payment";

	             if(typeof digitalDataAdobe.user=='undefined'){
	 				digitalDataAdobe.user={};
	 				}

	             if(typeof digitalDataAdobe.userProfile=='undefined'){
		 				digitalDataAdobe.userProfile={};
		 				}

	             digitalDataAdobe.user.emailID=detailsJson.userDetails.email;
	             digitalDataAdobe.user.mobileNumber= detailsJson.userDetails.mobileNoISD + detailsJson.userDetails.mob
	             digitalDataAdobe.user.loginStatus="logged-in";
	             digitalDataAdobe.userProfile.loginLocation=pageNameVal;
	             digitalDataAdobe.userProfile.loginType="normal";
	             digitalDataAdobe.userProfile.userType= "customer";

			if(typeof yt_adobe != 'undefined'){
				yt_adobe.track("successfulLogin", digitalDataAdobe);
		  }
		}catch(e){

		}
     	 }
	},
	adobeTrackPromoFaliure : function(){
		try{
			digitalDataAdobe.page.pageInfo.popName='promocode mismatch'


			if(typeof yt_adobe != 'undefined'){
				yt_adobe.track("popup-load", digitalDataAdobe);
		  }
		}catch(e){

		}
	},
	adobeTrackAfterPromoFaliure : function(option){
		try{
			digitalDataAdobe.customLinkName ='promocode mismatch: ' + option


			if(typeof yt_adobe != 'undefined'){
				yt_adobe.track("custom-link-click", digitalDataAdobe);
		  }
		}catch(e){

		}
	},
		adobeTrackPaymentLoadData : function(payop,ctype,payOpChange){
			try{
				var isQuickBookEnabled = "no";
				digitalDataAdobe.cardstatus = "unchecked"
				if(payop=="qb" || payop=="QB"){
					isQuickBookEnabled = "yes";
					digitalDataAdobe.cardstatus = "checked"
					ctype = YatraQBPaymentUI.getQBCardType();        //this would return us the cardTpye of saved card
				}
				if(typeof digitalDataAdobe.transaction=='undefined'){
				digitalDataAdobe.transaction={};
				}
				digitalDataAdobe.transaction.paymentMethod=payOpChange+"|"+ctype+"|"+isQuickBookEnabled;

				var sessionTimeoutData = detailsJson.sessionTimeoutData;
				if(sessionTimeoutData && sessionTimeoutData.pricingTime !=undefined && sessionTimeoutData.pricingTime != ""
					&& sessionTimeoutData.currentTime !=undefined && sessionTimeoutData.currentTime != ""
					&&	sessionTimeoutData.timeout > 0) {
					var currentTime = new Date(sessionTimeoutData.currentTime);
					var pricingTime = new Date(sessionTimeoutData.pricingTime);
					var systemCurrentTime = new Date();

					var totalTimeLapsedInAfterPricingInMillis = systemCurrentTime - pricingTime;
					var totalTimeLapsedInAfterPricingInSec =	 Math.floor(totalTimeLapsedInAfterPricingInMillis / 1000);
					//console.log("time lapsed after pricing", totalTimeLapsedInAfterPricingInSec)

					var timeLapsedOnPaymentPageInMillis = systemCurrentTime - currentTime;
					var timeLapsedOnPaymentPageInSec =	 Math.floor(timeLapsedOnPaymentPageInMillis / 1000);
					//console.log("time lapsed on payment page", timeLapsedOnPaymentPageInSec)

					// for testing
					var timeLapsedInMillis = currentTime - pricingTime;
					var timeLapsedInSecs = Math.floor(timeLapsedInMillis / 1000);
					//console.log("time from review to payment", timeLapsedInSecs)

					digitalDataAdobe.timeOnPayment = timeLapsedOnPaymentPageInSec;
					digitalDataAdobe.timePricingToPayment = totalTimeLapsedInAfterPricingInSec;

				}



				if(typeof yt_adobe != 'undefined'){
					yt_adobe.track("payNow", digitalDataAdobe);
			  }
			}catch(e){

			}
		},

		pushGAInfo : function(action) {
			YatraPaymentUI.pushGAInfoGeneric('Payment Method', action)
		},
		
		
		
		/*====================Rahul Patel Live Tracking Changes for Paynow Tracking starts here =============*/
	pushGTAPayswiftTrackingData: function(payOption) {
		var a = this.pushLivePayswiftTrakingdata(payOption);
		if (a && dataLayer) {
			dataLayer.push(a)
		}
	},

	pushGTAPayswiftTrakingHotelData: function(payOption) {
		var a = this.pushLivePayswiftTrakingHoteldata(payOption);
		if (a && dataLayer) {
			dataLayer.push(a)
		}
	},

	getCoupon: function(promo) {
		if (promo.code === "") {
			return "N/A"
		} else {
			return promo.code
		}
	},

	getCouponValue: function(promo) {
		if (promo.amount === 0) {
			return 0
		} else {
			return promo.amount
		}
	},
	pushLivePayswiftTrakingdata: function(payOption) {
		var temp = {},
			temp = {
				event: 'add_payment_info',
				trip_type: detailsJson && detailsJson.tripType ? detailsJson.tripType : "N/A",
				ecommerce: {
					currency: detailsJson && detailsJson.currencyName ? detailsJson.currencyName : "N/A",
					value: detailsJson && detailsJson.promo && detailsJson.promo.amount ? detailsJson.promo.amount : "N/A",
					coupon: detailsJson && detailsJson.promo && detailsJson.promo.code ? detailsJson.promo.code : "N/A",
					payment_type: payOption,
					items: [{
						item_name: detailsJson && detailsJson.uiParams && detailsJson.uiParams.gaTrackingData &&
							detailsJson.uiParams.gaTrackingData.ecommerce && detailsJson.uiParams.gaTrackingData.ecommerce.checkout && detailsJson.uiParams.gaTrackingData.ecommerce.checkout.products && detailsJson.uiParams.gaTrackingData.ecommerce.checkout.products[0].name ? detailsJson.uiParams.gaTrackingData.ecommerce.checkout.products[0].name : "N/A",

						item_id: detailsJson && detailsJson.uiParams && detailsJson.uiParams.gaTrackingData &&
							detailsJson.uiParams.gaTrackingData.ecommerce && detailsJson.uiParams.gaTrackingData.ecommerce.checkout && detailsJson.uiParams.gaTrackingData.ecommerce.checkout.products && detailsJson.uiParams.gaTrackingData.ecommerce.checkout.products[0].id ? detailsJson.uiParams.gaTrackingData.ecommerce.checkout.products[0].id : "N/A",

						price: detailsJson && detailsJson.uiParams && detailsJson.uiParams.gaTrackingData &&
							detailsJson.uiParams.gaTrackingData.ecommerce && detailsJson.uiParams.gaTrackingData.ecommerce.checkout && detailsJson.uiParams.gaTrackingData.ecommerce.checkout.products && detailsJson.uiParams.gaTrackingData.ecommerce.checkout.products[0].price ? detailsJson.uiParams.gaTrackingData.ecommerce.checkout.products[0].price : "N/A",

						item_brand: detailsJson && detailsJson.uiParams && detailsJson.uiParams.gaTrackingData &&
							detailsJson.uiParams.gaTrackingData.ecommerce && detailsJson.uiParams.gaTrackingData.ecommerce.checkout && detailsJson.uiParams.gaTrackingData.ecommerce.checkout.products && detailsJson.uiParams.gaTrackingData.ecommerce.checkout.products[0].brand ? detailsJson.uiParams.gaTrackingData.ecommerce.checkout.products[0].brand : "N/A",


						item_category: detailsJson && detailsJson.uiParams && detailsJson.uiParams.gaTrackingData &&
							detailsJson.uiParams.gaTrackingData.ecommerce && detailsJson.uiParams.gaTrackingData.ecommerce.checkout && detailsJson.uiParams.gaTrackingData.ecommerce.checkout.products && detailsJson.uiParams.gaTrackingData.ecommerce.checkout.products[0].category ? detailsJson.uiParams.gaTrackingData.ecommerce.checkout.products[0].category : "N/A",


						item_variant: detailsJson && detailsJson.uiParams && detailsJson.uiParams.gaTrackingData &&
							detailsJson.uiParams.gaTrackingData.ecommerce && detailsJson.uiParams.gaTrackingData.ecommerce.checkout && detailsJson.uiParams.gaTrackingData.ecommerce.checkout.products && detailsJson.uiParams.gaTrackingData.ecommerce.checkout.products[0].variant ? detailsJson.uiParams.gaTrackingData.ecommerce.checkout.products[0].variant : "N/A",
						item_list_name: 'Payment',
						index: 1,
						item_category2: detailsJson && detailsJson.tripType ? detailsJson.tripType : "N/A",
					}]

				}
			}
		return temp;

	},
	pushLivePayswiftTrakingHoteldata: function(payOption) {
		var temp = {},
			temp = {
				event: 'add_payment_info',
				ecommerce: {
					currency: detailsJson && detailsJson.currencyName ? detailsJson.currencyName : "N/A",
					value: detailsJson && detailsJson.promo && detailsJson.promo.amount ? detailsJson.promo.amount : "N/A",
					coupon: detailsJson && detailsJson.promo && detailsJson.promo.code ? detailsJson.promo.code : "N/A",
					payment_type: payOption,
					items: [{
						item_name: detailsJson && detailsJson.gaTrackingData && detailsJson.gaTrackingData.ecommerce && detailsJson.gaTrackingData.ecommerce.checkout && detailsJson.gaTrackingData.ecommerce.checkout.products && detailsJson.gaTrackingData.ecommerce.checkout.products[0].name ? detailsJson.gaTrackingData.ecommerce.checkout.products[0].name : "N/A",

						item_id: detailsJson && detailsJson.gaTrackingData && detailsJson.gaTrackingData.ecommerce && detailsJson.gaTrackingData.ecommerce.checkout && detailsJson.gaTrackingData.ecommerce.checkout.products && detailsJson.gaTrackingData.ecommerce.checkout.products[0].id ? detailsJson.gaTrackingData.ecommerce.checkout.products[0].id : "N/A",

						price: detailsJson && detailsJson.gaTrackingData && detailsJson.gaTrackingData.ecommerce && detailsJson.gaTrackingData.ecommerce.checkout && detailsJson.gaTrackingData.ecommerce.checkout.products && detailsJson.gaTrackingData.ecommerce.checkout.products[0].price ? detailsJson.gaTrackingData.ecommerce.checkout.products[0].price : "N/A",

						item_brand: detailsJson && detailsJson.gaTrackingData && detailsJson.gaTrackingData.ecommerce && detailsJson.gaTrackingData.ecommerce.checkout && detailsJson.gaTrackingData.ecommerce.checkout.products && detailsJson.gaTrackingData.ecommerce.checkout.products[0].brand ? detailsJson.gaTrackingData.ecommerce.checkout.products[0].brand : "N/A",

						item_category: detailsJson && detailsJson.gaTrackingData && detailsJson.gaTrackingData.ecommerce && detailsJson.gaTrackingData.ecommerce.checkout && detailsJson.gaTrackingData.ecommerce.checkout.products && detailsJson.gaTrackingData.ecommerce.checkout.products[0].category ? detailsJson.gaTrackingData.ecommerce.checkout.products[0].category : "N/A",


						item_variant: detailsJson && detailsJson.gaTrackingData && detailsJson.gaTrackingData.ecommerce && detailsJson.gaTrackingData.ecommerce.checkout && detailsJson.gaTrackingData.ecommerce.checkout.products && detailsJson.gaTrackingData.ecommerce.checkout.products[0].variant ? detailsJson.gaTrackingData.ecommerce.checkout.products[0].variant : "N/A",

						item_list_name: 'Payment',
						index: 1,
						item_category2: detailsJson && detailsJson.otherDetails && detailsJson.otherDetails.hotelGrade ? detailsJson.otherDetails.hotelGrade : "N/A",
					}]

				}
			}
		return temp;

	},
	/*====================Rahul Patel Live Tracking Changes for Paynow Tracking ends here =============*/




		//code changes for hotel and flight object
		pushGATrackingData:function(){
			var gaGTMData  =  detailsJson.gaTrackingData;
			if(!gaGTMData && detailsJson.uiParams && detailsJson.uiParams.gaTrackingData ){
				gaGTMData = detailsJson.uiParams.gaTrackingData;
				if(gaGTMData.ecommerce && gaGTMData.ecommerce.checkout && gaGTMData.ecommerce.checkout.products[0]){
					 gaGTMData.ecommerce.checkout.products[0].price= ShoppingCart.totalPayableAmount
				}
			}
			if(gaGTMData && dataLayer){
					dataLayer.push(gaGTMData);
			}
		},
		pushGAInfoForPaymentModes : function(action) {
			if(action == 'payFull') {
				action = "Paynow Now";
			} else if(action == 'payPartial') {
				action = "Partial Payment";
			} else if(action == 'holdAndPay') {
				action = "Hold Booking";
			}
			YatraPaymentUI.pushGAInfoGeneric('Scope of Payment', action)
		},

		pushGAInfoEWallet : function(action) {
			YatraPaymentUI.pushGAInfoGeneric('eCash', action)
		},

		pushGAInfoForQuickBook : function(action) {
			YatraPaymentUI.pushGAInfoGeneric('Quickbook', action)
		},

		enableFirstPaymentOption : function() {

			if(detailsJson.promo && detailsJson.promo.paymentOption){

				var firstPayOptionElem = $('#' + detailsJson.promo.paymentOption);
				var payOption = detailsJson.promo.paymentOption;
				var tabPayOption = "#tab_" + payOption;
				$("#cpmt_tabContainer").find('li').removeClass('active').addClass('dactive');
				$(firstPayOptionElem).parent('li').addClass('active').removeClass('dactive');
				$(tabPayOption).attr('style', 'display:block;');

				// Push GA Info For First Payment Option
//				YatraPaymentUI.pushGAInfo($(firstPayOptionElem).text());
					$('#other_payop').val(detailsJson.promo.paymentOption);

				if(redeemNow){
					firstPayOptionElem.click();
					$('#other_payop').trigger("change");

				}
				// Selection for drop down in mobile view


				// remove preset payment Details
				YatraPaymentUI.clearPaymentDetails(tabPayOption);

	// check for sub option, but its only in case of net banking and mobile wallet

				if(detailsJson.promo.paymentOption == "mw" && detailsJson.promo.subPaymentOption){
					$("#" + detailsJson.promo.subPaymentOption ).click();
				}

				if(detailsJson.promo.paymentOption == "nb" && detailsJson.promo.subPaymentOption){

					if($("#" + detailsJson.promo.subPaymentOption).length){
						$("#" + detailsJson.promo.subPaymentOption).click();
					}
						else {
							$('input:radio[name="prBank"]').prop('checked', false);
							$('#nprBank').val(detailsJson.promo.subPaymentOption);
							$('#nprBank').trigger("change");

						}
					$('#netBankDropDowm').val(detailsJson.promo.subPaymentOption);
					$('#netBankDropDowm').trigger("change");
						}


				}

			else {
				var firstPayOptionElem = $('#cpmt_tabmenu > li:first > a');
				var payOption = $(firstPayOptionElem).attr('paymentOption');
				var tabPayOption = "#tab_" + payOption;
				$("#other_payop").val(payOption);
				$("#cpmt_tabContainer").find('li').removeClass('active').addClass('dactive');
				$(firstPayOptionElem).parent('li').addClass('active').removeClass('dactive');
				$(tabPayOption).attr('style', 'display:block;');

				// Push GA Info For First Payment Option
//				YatraPaymentUI.pushGAInfo($(firstPayOptionElem).text());
				if(redeemNow){
					firstPayOptionElem.click();
				}
				// remove preset payment Details
				YatraPaymentUI.clearPaymentDetails(tabPayOption);
			}


			redeemNow = true;
		},

		clearValidationError : function() {
			$("body").find("div.pmt_errorlist").remove();
		},

		resetDropDown : function(div) {
			if ($(div).length > 0)
				$(div).val("");
		},

        getMultiPayOpt : function() {
		    var multiPayOption ;
		    var paymentOption = YatraPayment.getPayOp();
		    var selectedTab = YatraPaymentUI.getPaymentOption();

		    //If payOp selected is QuickBook
		    if (paymentOption != undefined && paymentOption.toUpperCase() == "QB") {
		    	var checkedQBCard ='';
		    	if (selectedTab == 'cc') {
		    		checkedQBCard = $('input:radio[processCardId=cc]:checked');
		    	} else {
		    		checkedQBCard = $('input:radio[processCardId=dc]:checked');
		    	}

		    	var cardId = $(checkedQBCard).val();
		    	var qbMultiPayRadioDiv = "#multiflow_type_div_"+ cardId;
		    	multiPayOption = $(qbMultiPayRadioDiv+ ' input:radio[name="processTyperdo"]:checked').val();
		    }

		    else if (selectedTab == 'cc'||selectedTab =='dc'){
		    	var divId = '#' + selectedTab + '_';
		    	var processTypeDiv = divId + 'process-pay-type';
                var processTypeRadioDiv = '#' + paymentOption + '_process_pay_type_div';
                multiPayOption = $(processTypeRadioDiv+ ' input:radio[name="processTyperdo"]:checked').val();
		    }

		    if (multiPayOption != undefined)
		    return multiPayOption;

		    //Fallback case
		    return "Secure";
	    },

		disableMultiFlowUiForCreditCard : function(qbElem) {
			$(".qb_multiFlowDiv").hide()
		},

		disableMultiFlowUiForDebitCard : function(qbElem) {
			$(".qb_multiFlowDiv").hide()
		},

		initializeBanksPaymentUI : function() {
			$('#tab_nb input:radio[name="prBank"]:nth(0)').trigger("click")
			$('#tab_nb input:radio[name="prBank"]:nth(0)').attr("checked", "checked");
			$('#tab_nb > input[name="bankCode"]').val($('input[name="prBank"]:checked').val());
			$('#iframeEnabledFor-nb-bank').val($('input[name="prBank"]:checked').attr('iframeEnabled'));

			$('#tab_atm input:radio[name="atmBank"]:nth(0)').attr("checked", "checked");
			$('#tab_atm > input[name="bankCode"]').val($('input[name="atmBank"]:checked').val());

			$('#tab_mw input:radio[name="walletService"]:nth(0)').attr("checked", "checked");
			$('#tab_mw > input[name="bankCode"]').val($('input[name="walletService"]:checked').val());

			$('#tab_payltr input:radio[name="payLaterService"]:nth(0)').attr("checked", "checked");
			$('#tab_payltr > input[name="bankCode"]').val($('input[name="payLaterService"]:checked').val());


//			$('#tab_emi input:radio[name="emiBank"]:nth(0)').attr("checked", "checked");

		},





		resetBillingDiv : function(payop) {
			var payopDiv = '#' + payop + '_';
			$(payopDiv + 'ba1').val("");
			IE_Placeholder.callBackPlaceDefaultText($(payopDiv + 'ba1'));
			$(payopDiv + 'bcity').val("");
			IE_Placeholder.callBackPlaceDefaultText($(payopDiv + 'bcity'));
			$(payopDiv + 'bstate').val("");
			IE_Placeholder.callBackPlaceDefaultText($(payopDiv + 'bstate'));
			$(payopDiv + 'bpin').val("");
			IE_Placeholder.callBackPlaceDefaultText($(payopDiv + 'bpin'));
			$(payopDiv + 'bcountry_id').val("");
			IE_Placeholder.callBackPlaceDefaultText($(payopDiv + 'bcountry_id'));
			YatraPaymentUI.resetDropDown(payopDiv + 'bcountry');
			$(payopDiv + 'baisd').val("");
			IE_Placeholder.callBackPlaceDefaultText($(payopDiv + 'baisd'));
			$(payopDiv + 'bamob').val("");
			IE_Placeholder.callBackPlaceDefaultText($(payopDiv + 'bamob'));
		},

		resetCreditCardCvv : function() {
			var cvvClass = $("#cc_CvvUrl_id").attr('class');
			$('#cc_CvvUrl_id').removeClass(cvvClass);
			$('#cc_CvvUrl_id').addClass($("#cc_CvvUrl_id").attr("defaultVal"));
			$(".cpmt_CVVtxt").text($(".cpmt_CVVtxt").attr("defaultVal"));
			$('#cc_cvv_id').attr("maxlength", 3);
			$('#cc_cvv_id').attr("data-validation", 'required|regType[number]|lenCheck[3]');
			$('#cc_cvv_id').prop("disabled", false);
			$('#cc_cvv_id').css('background-color' , '#FFFFFF');
		},

		resetEMICardCvv : function() {
			var cvvClass = $("#emi_CvvUrl_id").attr('class');
			$('#emi_CvvUrl_id').removeClass(cvvClass);
			$('#emi_CvvUrl_id').addClass($("#emi_CvvUrl_id").attr("defaultVal"));
			$("#emi_cvvImageText_id").text($("#emi_cvvImageText_id").attr("defaultVal"));
			$('#emi_cvv_id').attr("maxlength", 3);
			$('#emi_cvv_id').attr("data-validation", 'required|regType[number]|lenCheck[3]');
			$('#emi_cvv_id').prop("disabled", false);
			$('#emi_cvv_id').css('background-color' , '#FFFFFF');
		},

		resetCreditCard : function() {

			//Remove preset cvv Image and ImageText
			YatraPaymentUI.resetCreditCardCvv();

			//Reset Biling Address Div
			$("#cc_AddressShowHide").hide();
			$("#cc_pmt_addressId").hide();

			$("#cc_process-pay-type").hide();
			YatraPaymentUI.paymentProcessType = "Secure";

			//Reset Expiry Date dropdown
			YatraPaymentUI.resetDropDown('#cc_expm_id');
			YatraPaymentUI.resetDropDown('#cc_expy_id');
			$('#cc_expm_id').prop('selectedIndex', 0);
			$('#cc_expMonth_id').val("");
			$('#cc_expYear_id').val("");

			//Reset QuickBook Save card checkbox
			if(showSaveOption) {
				$('#cc_saveQBCard').attr('checked', true);
				$("#cc_saveQuickBookCard_id").val("true");
			}

			//clear card number Field .
			$("#cc_cno_id").val('');
			$("#cc_cardholder_name_id").val('');

			//Reset Billing Country
			YatraPaymentUI.resetDropDown('#cc_bcountry');
			$('#cc_bcountry_id').val("");
			$('#cc_bcountry_name').val("");

			// Set Error Block to Display None
			$('#cc_divErrMsgBlock').css('display', 'none');
		},

		resetDebitCard : function() {

			//Reset Biling Address Div
			$("#dc_pmt_addressId").hide();
			$("#dc_process-pay-type").hide();
			YatraPaymentUI.paymentProcessType = "Secure";

			//Reset Expiry Date dropdown
			YatraPaymentUI.resetDropDown('#dc_expm_id');
			YatraPaymentUI.resetDropDown('#dc_expy_id');
			$('#dc_expMonth_id').val("");
			$('#dc_expYear_id').val("");

			//clear card number Field .
			$("dc_cno_id").val('');
			$("#dc_cardholder_name_id").val('');

			//Reset QuickBook Save card checkbox
			if(showSaveOption) {
				$('#dc_saveQBCard').attr('checked', true);
				$("#dc_saveQuickBookCard_id").val("true");
			}

			//Reset Billing Country
			YatraPaymentUI.resetDropDown('#dc_bcountry');
			$('#dc_bcountry_id').val();
			$('#dc_bcountry_name').val();

			//Reset ExpiryDate Message
			YatraPaymentUI.setMaestroUI('VISA');

			// Set Error Block to Display None
			$('#dc_divErrMsgBlock').css('display', 'none');
		},

		resetPaymentOptionDivHieght : function(){
			var totalPaymentOptions = $('ul#cpmt_tabmenu li').length
			if($('li#cpmt_otherPayOp li').length){
			totalPaymentOptions = totalPaymentOptions - 1;
			}
			var heightMultiplier = detailsJson.convFeePercent ? 68 :50
			if(totalPaymentOptions >=14){
			var moreHeightRequired = (totalPaymentOptions - 14)* heightMultiplier;
			var currentHeight;
			if(paymentDivHeight){
				currentHeight = paymentDivHeight;
			}
			else{
				paymentDivHeight = parseInt($('.cpmt_Paytabbox').css('min-height'));
				currentHeight = paymentDivHeight;
			}

			$('.cpmt_Paytabbox').css('min-height', currentHeight + moreHeightRequired + 25 +'px');
			}
		},

		resetEMI : function() {
			YatraPaymentUI.resetEMITableDetails();
			YatraPaymentUI.resetEMICardCvv();
			if(showEMIInfo)
				$('#emi_msg').show();
			else $('#emi_msg').hide();

			//Reset Expiry Date dropdown
			YatraPaymentUI.resetDropDown('#emi_expm_id');
			YatraPaymentUI.resetDropDown('#emi_expy_id');
			$('#emi_expMonth_id').val("");
			$('#emi_expYear_id').val("");
			var optionSelected = $("#emiBank_select option:selected").val();
			$('#emi_mobile_number_id_'+optionSelected).val("");
			$('#emi_divErrMsgBlock').hide();
			$('#emi_divWarningMsgBlock').hide();
			YatraPaymentUI.enablePayNow(YatraPaymentUI.getPaymentOption());

			// Reset CardNumber and CVV
			$("#emi_cno_id").val("");
			IE_Placeholder.callBackPlaceDefaultText($("#emi_cno_id"));
			$("#emi_cvv_id").val("");
			IE_Placeholder.callBackPlaceDefaultText($("#emi_cno_id"));
		},

		resetEMITableDetails : function() {
			var optionSelected = $("#emiBank_select option:selected").val();
			if(optionSelected != "") {
				var radio = $('input:radio[name="' + optionSelected + '"]:nth(0)');
				radio.trigger('click');
			}
		},

		resetRewardPoint : function() {
			YatraPaymentUI.resetDropDown('#rw_expm_id');
			YatraPaymentUI.resetDropDown('#rw_expy_id');
			$('#rw_expMonth_id').val("");
			$('#rw_expYear_id').val("");
		},

		resetQuickBookCards : function(paymentOptiondiv) {

			var qbDiv = paymentOptiondiv + "QB";
			var firstCard = qbDiv + ' input:radio[name="cardid"]:nth(0)';
			if (qbDiv == '#tab_ccQB') {
				if ($('#cc_qbEnabled').val() == 'true') {
					$(firstCard).prop('checked', true);
					$(firstCard).parents().removeClass('selChecked');
					YatraPaymentUI.enableQBCVVForCheckedCreditCard($(firstCard));
					if($(firstCard).attr("cardNumberOriginal") != null && $(firstCard).attr("cardNumberOriginal") != undefined) {
					    var cardNo = $(firstCard).attr("cardNumberOriginal").replace(/-/g,'');
                    	var binDetails = YatraPaymentUI.getBinDetails(cardNo);
                    	YatraPaymentUI.setSkipOtpUiForQBIfRequired($(firstCard), "cc",binDetails);
                        YatraPaymentUI.setMultiPayUiForQBIfRequired($(firstCard), "cc",binDetails);
					}
					YatraPaymentUI.clearAddressDiv($('input:radio[processCardId=cc]'));
					YatraPaymentUI.handleInternationalQBCard($(firstCard));
					YatraPaymentUI.disableNewCreditCard();
				}
			} else if (qbDiv == '#tab_dcQB') {
				if ($('#dc_qbEnabled').val() == 'true') {
					$(firstCard).prop("checked", true);
					$(firstCard).parents().removeClass('selChecked');
					YatraPaymentUI.enableQBCVVForCheckedDebitCard($(firstCard));
					if($(firstCard).attr("cardNumberOriginal") != null && $(firstCard).attr("cardNumberOriginal") != undefined) {
						var cardNo = $(firstCard).attr("cardNumberOriginal").replace(/-/g,'');
                        var binDetails = YatraPaymentUI.getBinDetails(cardNo);
                        YatraPaymentUI.setSkipOtpUiForQBIfRequired($(firstCard), "dc",binDetails);
                        YatraPaymentUI.setMultiPayUiForQBIfRequired($(firstCard), "dc",binDetails);
					}
					YatraPaymentUI.clearAddressDiv($('input:radio[processCardId=dc]'));
					YatraPaymentUI.handleInternationalQBCard($(firstCard));
					YatraPaymentUI.disableNewDebitCard();
				}
			}

			YatraQBPaymentUI.resetQB(qbDiv);
			YatraQBPaymentUI.innerSwitch(qbDiv);
		},

		clearPaymentDetails : function(paymentOptiondiv) {
            binNumber = "000000";
			// Resetting default values
			$(paymentOptiondiv + " input:text").each(function() {
				var elementId = $(this).attr('id');
				if(elementId != 'qb_user_email_cc' && elementId != 'qb_user_email_dc' && elementId != 'corp_user_email') {
					$(this).val("");
					IE_Placeholder.callBackPlaceDefaultText($(this));
				}
			});

			$(paymentOptiondiv + " input:password").each(function() {
				$(this).val("");
				try{
					if ($.browser.msie && parseInt($.browser.version) > 9) {
						IE_Placeholder.callBackPlaceDefaultText($(this));
					}
				} catch(e){

				}
			});

			// Logic for removing fading
			$(paymentOptiondiv + " i.cpmt_fade").removeClass('cpmt_fade');

			// Reset Credit Card Details
			YatraPaymentUI.resetCreditCard();

			// Reset Debit Cards
			YatraPaymentUI.resetDebitCard();

			// Reset Banking options
			YatraPaymentUI.initializeBanksPaymentUI();

			// Reset NetBanking dropdown
			YatraPaymentUI.resetDropDown('#nprBank');

			//Reset EMI Card details ..
			YatraPaymentUI.resetEMI();

			// Reset Reward Points
			YatraPaymentUI.resetRewardPoint();

			//Reset Quickbook cards
			YatraPaymentUI.resetQuickBookCards(paymentOptiondiv);

			//Reset
			YatraCorpCardUI.initialise();

			YatraPaymentUI.resetDebitAtmPin();

			if(detailsJson.convFeePercent){
				var payOption = YatraPayment.getPayOp();
				for(x in detailsJson.convFeePercent){
					if(x != 'default'){

						if(detailsJson.convFeePercent[x] == 0){
							// show zero only for UPI
							if(x== "upi")
							$("#"+ x +" .convFeesPaymetOption").addClass("zeroConvFeePercent").html("Zero Conv. Fee");
						}
						else
						$("#"+ x +" .convFeesPaymetOption").html("Conv. Fee - "+detailsJson.convFeePercent[x] +"% ");


						$("#"+ x +" .convFeesPaymetOption").removeClass( "conFeesPO" );

					}
				}

				$(".conFeesPO").html("Conv. Fees - "+detailsJson.convFeePercent['default'] +"% ");
				//show the tool tip at you pay
				$(".you-pay-txt .ico_info_paySwift").css('display','inline-block');
				$(".you-pay-txt").addClass("hoverForConvFeesHolidays");

			}

			// show QR code
			if(showQRFlag){
				$(".QRDiv").show();
				$(".UPIor").show();
				$(".upiRow").hide();
			}
			// show the promo image
			if(showPromoImage){
			$(".promotionDiv").css("background-image", "url("+ showPromoImageUrl +")");
			$(".promotionDiv").show();
			}
			// resetpaymentOption div
			YatraPaymentUI.resetPaymentOptionDivHieght();

			// make 3 letter mandatory for cc name in booking.com hotel
			if(detailsJson.product == 'INT_HOTEL_BKG'){
				$('#cc_cardholder_name_id').attr('data-validation', 'required|regType[alpha]|minLenCheck[3]');
				$('#cc_cardholder_name_id').attr('data-msginfo', '* required|* alphabets only | Min 3 characters required');
			}
		},

		resetDebitAtmPin : function() {
			$("#dcpincardul").show();
			$("#dcpinpaylaterlabel").hide();

			YatraPaymentUI.resetDropDown('#dcpinBank');
			YatraPaymentUI.clearDcPinCardFields();
		},

		clearDcPinCardFields : function() {
			YatraPaymentUI.resetDropDown('#dcpin_expm_id');
			YatraPaymentUI.resetDropDown('#dcpin_expy_id');
			$('#dcpin_expMonth_id').val("");
			$('#dcpin_expYear_id').val("");
			$("#dcpin_cno_id").val('');
			$("#dcpin_cardholder_name_id").val('');
			$('#dcpin_cvv_id').val("");
		},

		startSessionTimer: function(){
			if(enableSessionTimer == "true" && detailsJson.sessionTimeoutData != undefined) {
				var sessionTimeoutData = detailsJson.sessionTimeoutData;
				if(sessionTimeoutData.pricingTime !=undefined && sessionTimeoutData.pricingTime != ""
					&& sessionTimeoutData.currentTime !=undefined && sessionTimeoutData.currentTime != ""
					&&	sessionTimeoutData.timeout > 0) {
					var currentTime = new Date(sessionTimeoutData.currentTime);
					var pricingTime = new Date(sessionTimeoutData.pricingTime);

					var sessionTimeoutInMins = sessionTimeoutData.timeout;
					var sessionTimeoutInMillis = sessionTimeoutInMins * 60000;
					var timeLapsedInMillis = currentTime - pricingTime;

					var timeoutMsg = sessionTimeoutData.timeoutMessage;

					if(timeLapsedInMillis >= sessionTimeoutInMillis) {
						YatraPaymentUI.showSessionTimerPopup(timeoutMsg);
					} else {
						var tickDuration = 1000;
						var timeLeftInMillis = sessionTimeoutInMillis - timeLapsedInMillis;
						sessionTimerInterval = setInterval(function(){
							var timeLeftInMinutes = Math.floor(timeLeftInMillis / 60000);
							var timeLeftInSeconds = ((timeLeftInMillis % 60000) / 1000).toFixed(0);
							document.getElementById("bookingCounterMins").innerHTML = timeLeftInMinutes;
							document.getElementById("bookingCounterSeconds").innerHTML = timeLeftInSeconds;
							if(timeLeftInMinutes == 0 && timeLeftInSeconds == 0) {
								$("#bookingCounter").hide();
								YatraPaymentUI.showSessionTimerPopup(timeoutMsg);
								YatraPaymentUI.recordEventInSentry("Session Timed out", "info", "session-timer-expired", "timeLapsedTillPaymentPage", timeLapsedInMillis);
							} else {
							    $('body').addClass('timer-on');
								$("#bookingCounter").css({"display":"inline-block"});
								$(".copyright").css('margin-bottom','18px');
							}
							timeLeftInMillis = timeLeftInMillis - tickDuration;
						}, tickDuration);
					}
				}

				else {
					YatraPaymentUI.recordEventInSentry("PricingTime or CurrentTime is null", "info", "session-timer-not-shown");
				}

			}
		},

		showSessionTimerPopup: function(timeoutMsg) {
			//var template = new Template();
			//template.set("session-timer-message-template");
			//$("#session-timeout-message").html(template.render({sessiontimeoutmessage:timeoutMsg}));
			//PaymentCommon.showBlackDiv();
			if(sessionTimerInterval != undefined) {
				clearInterval(sessionTimerInterval);
			}

			if(!(detailsJson && detailsJson.sessionTimeoutData && detailsJson.sessionTimeoutData.searchUrl)){
				$('#searchResult').parent().hide();
				$("#latestPricing").attr("value","Try Again");
			}
			$('body').removeClass('timer-on');
			$("#session_timer_pop_up").css({"display":"block"});
			 $('body').css({'overflow':'hidden'});
			  $(document).bind('scroll',function () {
			       window.scrollTo(0,0);
			  });
		},

		hideSessionTimerPopup: function() {
			if(sessionTimerInterval != undefined) {
				clearInterval(sessionTimerInterval);
			}
			$("#session_timer_pop_up").hide();
			 $(document).unbind('scroll');
			  $('body').css({'overflow':'visible'});
		},

		displayEmiTable : function(){
			$("citi_info_message").hide();
			$("#bajajWarningMsgDiv").hide();
			$('#emiBank_select').on('change keyup',function(){
				var optionSelected = $("#emiBank_select option:selected").val();
				$('#emi_emiBank_id').val(optionSelected);
				if(optionSelected != ''){
					finalName = optionSelected + '-emi';
					$('.get-logo').find('.'+finalName).removeClass('hide').siblings().addClass('hide');
					$('.cpmt_emiOptn').show();
					$('#emi-details-table').find('tr').addClass('hide');
					$('.tabel-header-footer').show();
					$('#emi-details-table').find('.'+optionSelected+'-emi-details').removeClass('hide');

					if($('.emi_mobile_'+optionSelected).length){
						$('.emi_mobile_'+optionSelected).removeClass('hide').siblings().addClass('hide');
						$('.emi_card_label').html("Debit Card Number")
					}
					else {
						$(".emi_moble_row div").addClass("hide");
						$('.emi_card_label').html("Credit Card Number")
					}

					var radio = $('input:radio[name="' + optionSelected + '"]:nth(0)');
					radio.attr("checked", "checked");
					YatraPaymentUI.setEMITableFooterDetails(radio);
					document.getElementById('citi_info_message').innerHTML="";
					document.getElementById('bajajWarningMsg').innerHTML="";
					$("#bajajWarningMsgDiv").hide();

					if(optionSelected=="citi"){
						document.getElementById('citi_info_message').innerHTML="Please visit <a target='_blank' style='color:#007ec8 !important' href='http://citibank.com/India'>Citibank.com/India </a> for more information.";
						document.getElementById('citi_info_message').style.display = 'inline';
					}
					/*if(optionSelected=="bajaj"){
						$("#bajajWarningMsgDiv").show();
						document.getElementById('bajajWarningMsg').innerHTML="For Bajaj EMI transactions, minimum monthly installment amount should be greater than Rs 1,500.";
						document.getElementById('bajajWarningMsg').style.display = 'inline';
					}*/
				}
				else{
					$('.get-logo label').addClass('hide')
					$('.cpmt_emiOptn').hide();

				}
			});
		},

		logPageLoadInSentry : function(){
			YatraPaymentUI.logUserLoginDetailsInSentry("PageLoad");
		},

		logUserLoginDetailsInSentry: function(category){
			var ssoToken = YatraQBPaymentUI.getSSOToken();
			if(ssoToken != undefined && ssoToken != null) {
				YatraPaymentUI.recordEventInSentry("User logged in", "info", category);
			} else {
				YatraPaymentUI.recordEventInSentry("User not logged in", "info", category);
			}
		},

		showAddressDivForProductRoutedOnInternationalGateway : function(isProductRoutedOnInternationalGateway) {
			if(isProductRoutedOnInternationalGateway == true) {
				var ccaddressDiv = '#cc_' + 'pmt_addressId';
				var dcaddressDiv = '#dc_' + 'pmt_addressId';
				$(ccaddressDiv).show();
				$(dcaddressDiv).show();
			}
		},

		contentSwitcher : function(settings) {

			var settings = {
					contentClass : '.cpmt_Paytabbox > .content', navigationId : '#cpmt_tabContainer', wapDropDown:'#other_payop'

			};

			$(settings.navigationId).find('a').click(function(e) {
				// in a GV if the amount payable is 0; block the user from going on any other menu
				if(ShoppingCart.getTotalCartAmount() == 0 && YatraPayment.getPayOp() == "gv" && !Number(ShoppingCart.tcsAmount)){
					return false
				}
				e.preventDefault();
				setTimeout(function(){$(".card-logos").removeClass('VISA');$(".card-logos").removeClass('MASTER');$(".card-logos").removeClass('DINERS');
				$(".card-logos").removeClass('AMEX');$(".card-logos").removeClass('RUPAY');  $(".card-logos").removeClass("MAEST")},10)
				var paymentOption = $(this).attr('paymentOption');
				//hide the pass through message on payment option change
				$("#passThroughMessage_id").hide();
				if(typeof paymentOption== 'undefined'){
					paymentOption = $('#cpmt_otherPayOp ul li:first a').attr("paymentoption")
				}
				YatraPaymentUI.pushGAInfo($(this).text());
				var contentToShowDiv = '#tab_' + paymentOption;
				$(settings.navigationId).find('li').removeClass('active').addClass('dactive');
				$(this).parent('li').addClass('active').removeClass('dactive');
				$(settings.contentClass).hide().removeClass('show');
				$('#tab_' + paymentOption).show().addClass('show');
				$("#other_payop").val(paymentOption);
				YatraPaymentUI.callOnCardEntry('RESET','','');

				// Enable Pay Now by default
				YatraPaymentUI.enablePayNow(paymentOption);

				IE_Placeholder.init();

				// remove preset payment Details
				YatraPaymentUI.clearPaymentDetails(contentToShowDiv);

				var isProductRoutedOnInternationalGateway = YatraPaymentUI.isProductRoutedOnInternationalGateway();
				YatraPaymentUI.showAddressDivForProductRoutedOnInternationalGateway(isProductRoutedOnInternationalGateway);

				YatraPaymentUI.clearValidationError();

				PaymentMode.checkPaymentModes();
				if(YatraPaymentUI.isTcsEnabled(detailsJson.isTcsEnabled)){
					YatraPaymentUI.callForRecalculatingTcs();
					if(GV.totalGVAmount() > 0){
						// if(ShoppingCart.getTotalCartAmount() > 0){
						// GV.balanceAmountAfterGV = ShoppingCart.getTotalCartAmount() + Number(ShoppingCart.tcsAmount)
						// }
						ShoppingCart.totalPayableAmount = GV.balanceAmountAfterGV;
						$("#fareBreakup-Payable").html(GV.balanceAmountAfterGV + GV.totalGVAmount() - ShoppingCart.tcsAmount);
						$("#"+ShoppingCart.totalAmountSpan).html(YatraHelper.formatPrice(GV.balanceAmountAfterGV));
						$("#totalAmountSpann").html(YatraHelper.formatPrice(GV.balanceAmountAfterGV));
						YatraEWalletPaymentUI.setEWalletUI();
					}
				}



				YatraPaymentUI.notifyPayopToLOB();

				LOBNotification.paymentOptionChange();
				YatraPaymentUI.changePayNowButtonLabel(detailsJson.product);

				// resetting the isNOcost emi flag
				YatraPaymentUI.isNoCostEmiFlag = false;

				if(paymentOption!='rewards') {
					$("#reward-point-tooltip").hide();
				}else{
					$("#reward-point-tooltip").show();
				}


				// initialise GV if payment option is GV
				if(paymentOption=='gv' && !GV.gv) {
					GV.initialise(5);
					}

				// keeping the hiding and show of pay now button seperate as GV initialise logic is different, cant combine both
				if(paymentOption=='gv') {

					// reseting the first 6  and 9 digits on switch of tab
					GV.addedGVFirst9 = '';
					 GV.addedGVFirst6 = '';
					 GV.clearErrors();
					$("#gvPinBlock").hide()
					// show error in GV if promo code is already applied --> now can use GV with promo so commenting code instead of removing it
					//if(detailsJson && detailsJson.promo && detailsJson.promo.code ){
					//	$("#gv-promo-error").show();
				//		$("#addGVDiv").hide();
				//	}else{
						$("#gv-promo-error").hide();
						$("#addGVDiv").show();
					//}
					if(ShoppingCart.totalPayableAmount==0){
						$("#"+getContinueButtonId()).show()
						}else{
							$("#"+getContinueButtonId()).hide()
						}
					if(GV.gv.length){
						$("#payNow").css("margin-top", "30px");
						$(".gv-bal-amount").show();
						}
					else{
						$("#payNow").css("margin-top", "0px");
						$(".gv-bal-amount").hide();
					}
					}
				else{
					$("#"+getContinueButtonId()).show()
				}


				if(paymentOption=='paypal') {
					// $("#convertedAmountPaypal, #mobile-conv").css('visibility', 'visible');
					 if($('input[name=paypal]:checked', '#paypalContainer').attr('id') != 'INR'){
						 $("#convertedAmountPaypal, #mobile-conv").css('visibility', 'visible');

						 $("#showConverstion").css('visibility', 'visible');
						 //$(".amount-big").removeClass("amount-big-inr");
					   }
					 if(!$("input[name=paypal]", "#paypalContainer").is(':checked')){
						  $("#convertedAmountPaypal, #mobile-conv").css('visibility', 'hidden');
						   $('#showConverstion').css('visibility', 'hidden');
					  }
				}else{
					$("#convertedAmountPaypal, #mobile-conv").css('visibility', 'hidden');
					$("#showConverstion").css('visibility', 'hidden');
					//$(".amount-big").addClass("amount-big-inr");
				}
				if(paymentOption == 'cc'){
					// if there is qb login show checkbox for new cc always
					if($("#tab_ccQB .qbLogin").length){
						 $("label[for=cc_newCard_id]").show();
						}

					if($('#cc_newCard_id').prop("checked")){
						$('#cc_newCard_id').trigger('click');
					}
					else{
						$("#cc_quickOrCard").trigger('click');
					}
				}
				if(paymentOption == 'dc'){
					// if there is qb login show checkbox for new dc always
					if($("#tab_dcQB .qbLogin").length){
						 $("label[for=dc_newCard_id]").show();
						}

					if($('#dc_newCard_id').prop("checked")){
						$('#dc_newCard_id').trigger('click');
					}
					else{
						$("#dc_quickOrCard").trigger('click');
					}
				}

				// if the condition disbaling emi is already true while moving to emi  payment option tab
				// disable the emi table and paynow button
				if(paymentOption == 'emi'){
					if($("#emiBank_select").val()=="bajaj"){
						$("#emiExpMonth").hide();
						$("#emiExpYear").hide();
						$("#emiCvv").hide();
					}else{
						$("#emiExpMonth").show();
						$("#emiExpYear").show();
						$("#emiCvv").show();
					}

			        if(disableEMICondition[$("#emiBank_select").val()] && !(disableEMICondition[$("#emiBank_select").val()][3].min <= ShoppingCart.getTotalCartAmount() &&  disableEMICondition[$("#emiBank_select").val()][12].max > ShoppingCart.getTotalCartAmount()) ){
						// show error message, disable the pay now button and disable the emi table
						YatraPaymentUI.disableEMI($("#emiBank_select").val(), disableEMICondition[$("#emiBank_select").val()][12].max <= ShoppingCart.getTotalCartAmount()?true:false)
					}else{
						// clear the error message, enable the paynow button and enable the emi table
						YatraPaymentUI.enableEMI($("#emiBank_select").val())
						let cAmt = ShoppingCart.getTotalCartAmount();
						let selEmiOpt = $("#emiBank_select").val();
						if (cAmt && selEmiOpt) {
							YatraPaymentUI.disableEmiDetails(cAmt, selEmiOpt);
						}
					}
				}
				if(YatraPaymentUI.isTcsEnabled(detailsJson.isTcsEnabled)){
					YatraPaymentUI.callForRecalculatingTcs();
				}
			});
			$(settings.wapDropDown).on("change",function(e) {
				e.preventDefault();

				var paymentOption = $(this).val();
				if(typeof paymentOption== 'undefined'){
					paymentOption = $('#cpmt_otherPayOp ul li:first a').attr("paymentoption")
				}
				if(paymentOption=='gv' && !GV.gv) {
					console.log('Init for mob')
					GV.initialise(5);
				}

				YatraPaymentUI.pushGAInfo($(this).text());
				var contentToShowDiv = '#tab_' + paymentOption;
				$(settings.navigationId).find('li').removeClass('active').addClass('dactive');
				var abc = $(settings.navigationId).find("#"+paymentOption);
				$(abc).parent("li").addClass("active").removeClass("dactive");
				$(settings.contentClass).hide().removeClass('show');
				$('#tab_' + paymentOption).show().addClass('show');

				YatraPaymentUI.callOnCardEntry('RESET','','');

				// Enable Pay Now by default
				YatraPaymentUI.enablePayNow(paymentOption);

				IE_Placeholder.init();

				// remove preset payment Details
				YatraPaymentUI.clearPaymentDetails(contentToShowDiv);

				YatraPaymentUI.clearValidationError();
				YatraPaymentUI.notifyPayopToLOB();
				PaymentMode.checkPaymentModes();
				LOBNotification.paymentOptionChange();

				if(paymentOption!='rewards') {
					$("#reward-point-tooltip").hide();
				}else{
					$("#reward-point-tooltip").show();
				}

				if(paymentOption=='paypal') {
					// $("#convertedAmountPaypal, #mobile-conv").css('visibility', 'visible');
					 if($('input[name=paypal]:checked', '#paypalContainer').attr('id') != 'INR'){
						 $("#convertedAmountPaypal, #mobile-conv").css('visibility', 'visible');
						 $("#showConverstion").css('visibility', 'visible');
						 //$(".amount-big").removeClass("amount-big-inr");
					   }
					 if(!$("input[name=paypal]", "#paypalContainer").is(':checked')){
						  $("#convertedAmountPaypal, #mobile-conv").css('visibility', 'hidden');
						   $('#showConverstion').css('visibility', 'hidden');
					  }
				}else{
					$("#convertedAmountPaypal, #mobile-conv").css('visibility', 'hidden');
					$("#showConverstion").css('visibility', 'hidden');
					//$(".amount-big").addClass("amount-big-inr");
				}
			});

		},

		getPaymentOption : function() {

			var payop = $('#cpmt_tabmenu > li.active > a').attr('paymentOption');
			if (payop == undefined)
				payop = $('#cpmt_tabmenu > li> ul> li.active > a').attr('paymentOption');

			return payop;
		},
		//not changing the function name. Now Payswift will do what LOBs used to do.
		notifyPayopToLOB : function () {

			var paymentOption = YatraPayment.getPayOp();
			if(paymentOption != undefined) {
				processPayOp(paymentOption);
				// YatraPaymentUI.showConvenienceFee();
			}

		},


		checkNaN : function(field) {
			var inputVal = $(field).val();
			$(field).val($(field).val().replace(/\D/g, ''));
			/*if (isNaN(inputVal)) {
			var newInputVal = parseInt(inputVal);
			if (isNaN(newInputVal)) {
				newInputVal = '';
			}
			$(field).val(newInputVal);
		}*/
		},

		disableSpace : function(field) {

			//Not allow spaces.
			$(field).val($(field).val().replace(/\s/g, ''));
		},

		getCardType : function(cardNumber) {
			var cno = (cardNumber + '').replace(/\s/g, ''); //remove space
			if ((/^(4)/).test(cno) && (cno.length == 13 || cno.length == 16)) {
				return 'VISA'; //VISA begins with 4, and length is 13 or 16.
			} else if ((/^(51|52|53|54|55|222)/).test(cno) && cno.length == 16) {
				return 'MASTER'; //MasterCard beigins with 51-55, and length is 16.
			} else if ((/^(34|37)/).test(cno) && cno.length == 15) {
				return 'AMEX'; //AMEX begins with 34 or 37, and length is 15.
			} else if ((/^(508[5-9][0-9]{12})|(6069[8-9][0-9]{11})|(607[0-8][0-9]{12})|(6079[0-8][0-9]{11})|(608[0-5][0-9]{12})|(6521[5-9][0-9]{11})|(652[2-9][0-9]{12})|(6530[0-9]{12})|(6531[0-4][0-9]{11})|(10101[0-9]{10})|(10000[1-8][0-9]{10})|(100011[0-9]{10})|(111111[0-9]{10})|(123456[0-9]{10})|(212112[0-9]{10})|(222222[0-9]{10})|(222444[0-9]{10})|(232325[0-9]{10})|(236253[0-9]{10})|(244335[0-9]{10})|(271189[0-9]{10})|(322222[0-9]{10})|(3528[0-9][0-9][0-9]{10})|(353[0-2][0-9][0-9][0-9]{10})|(35330[0-2][0-9]{10})|(353304[0-9]{10})|(353399[0-9]{10})|(354[0-3][0-9][0-9][0-9]{10})|(354999[0-9]{10})|(355[0-3][0-9][0-9][0-9]{10})|(356003[0-9]{10})|(356006[0-9]{10})|(35601[1-2][0-9]{10})|(35602[0-5][0-9]{10})|(356027[0-9]{10})|(35603[1-3][0-9]{10})|(356036[0-9]{10})|(356039[0-9]{10})|(35605[1-3][0-9]{10})|(35605[5-6][0-9]{10})|(35605[8-9][0-9]{10})|(35606[1-4][0-9]{10})|(35606[7-8][0-9]{10})|(35607[0-1][0-9]{10})|(356074[0-9]{10})|(356076[0-9]{10})|(35607[8-9][0-9]{10})|(356080[0-9]{10})|(35608[3-5][0-9]{10})|(35608[7-9][0-9]{10})|(35609[0-1][0-9]{10})|(35609[6-7][0-9]{10})|(356099[0-9]{10})|(35620[1-2][0-9]{10})|(35620[8-9][0-9]{10})|(35621[0-1][0-9]{10})|(35623[2-4][0-9]{10})|(356239[0-9]{10})|(35624[0-3][0-9]{10})|(356270[0-9]{10})|(356272[0-9]{10})|(35627[7-9][0-9]{10})|(35628[3-6][0-9]{10})|(356288[0-9]{10})|(35629[0-4][0-9]{10})|(35629[7-9][0-9]{10})|(356303[0-9]{10})|(356306[0-9]{10})|(35631[1-2][0-9]{10})|(35631[6-7][0-9]{10})|(35632[0-5][0-9]{10})|(356327[0-9]{10})|(35633[1-3][0-9]{10})|(356336[0-9]{10})|(356339[0-9]{10})|(356342[0-9]{10})|(35635[0-3][0-9]{10})|(35635[5-6][0-9]{10})|(356359[0-9]{10})|(356361[0-9]{10})|(356363[0-9]{10})|(356365[0-9]{10})|(35636[7-8][0-9]{10})|(35637[0-1][0-9]{10})|(356374[0-9]{10})|(356376[0-9]{10})|(35637[8-9][0-9]{10})|(356380[0-9]{10})|(35638[3-5][0-9]{10})|(35638[8-9][0-9]{10})|(35639[0-5][0-9]{10})|(35639[7-8][0-9]{10})|(35640[2-4][0-9]{10})|(35640[6-7][0-9]{10})|(35641[3-9][0-9]{10})|(356433[0-9]{10})|(356435[0-9]{10})|(356450[0-9]{10})|(356452[0-9]{10})|(35645[4-8][0-9]{10})|(35646[1-8][0-9]{10})|(356474[0-9]{10})|(35647[8-9][0-9]{10})|(35648[0-1][0-9]{10})|(35648[4-5][0-9]{10})|(35649[6-8][0-9]{10})|(35650[3-4][0-9]{10})|(35650[6-9][0-9]{10})|(35651[0-3][0-9]{10})|(35651[6-7][0-9]{10})|(35652[0-1][0-9]{10})|(35652[3-5][0-9]{10})|(356527[0-9]{10})|(35653[1-3][0-9]{10})|(35653[5-6][0-9]{10})|(356539[0-9]{10})|(356540[0-9]{10})|(35654[3-5][0-9]{10})|(35655[1-3][0-9]{10})|(35655[6-9][0-9]{10})|(35656[0-4][0-9]{10})|(35656[7-8][0-9]{10})|(35657[0-1][0-9]{10})|(356580[0-9]{10})|(35658[3-4][0-9]{10})|(35658[8-9][0-9]{10})|(35659[7-9][0-9]{10})|(35660[0-2][0-9]{10})|(35661[0-1][0-9]{10})|(356614[0-9]{10})|(356618[0-9]{10})|(356630[0-9]{10})|(35665[1-2][0-9]{10})|(35665[6-7][0-9]{10})|(356661[0-9]{10})|(356663[0-9]{10})|(356670[0-9]{10})|(35668[0-1][0-9]{10})|(35668[4-5][0-9]{10})|(35668[8-9][0-9]{10})|(35669[0-2][0-9]{10})|(35670[0-4][0-9]{10})|(35670[6-9][0-9]{10})|(35671[2-8][0-9]{10})|(35672[1-2][0-9]{10})|(35672[4-5][0-9]{10})|(356728[0-9]{10})|(35673[0-1][0-9]{10})|(35673[3-8][0-9]{10})|(356743[0-9]{10})|(35675[1-4][0-9]{10})|(356757[0-9]{10})|(356759[0-9]{10})|(35676[3-4][0-9]{10})|(35677[0-1][0-9]{10})|(356777[0-9]{10})|(356780[0-9]{10})|(35678[5-8][0-9]{10})|(35679[0-5][0-9]{10})|(35680[0-3][0-9]{10})|(35681[2-3][0-9]{10})|(356817[0-9]{10})|(356819[0-9]{10})|(356820[0-9]{10})|(356822[0-9]{10})|(35682[5-9][0-9]{10})|(356830[0-9]{10})|(35683[2-5][0-9]{10})|(35683[7-9][0-9]{10})|(35684[0-1][0-9]{10})|(35684[4-7][0-9]{10})|(35685[0-1][0-9]{10})|(35685[6-9][0-9]{10})|(356862[0-9]{10})|(35686[7-9][0-9]{10})|(356870[0-9]{10})|(356872[0-9]{10})|(356876[0-9]{10})|(35687[8-9][0-9]{10})|(35688[0-3][0-9]{10})|(356886[0-9]{10})|(356889[0-9]{10})|(356890[0-9]{10})|(35689[5-7][0-9]{10})|(356899[0-9]{10})|(35690[0-2][0-9]{10})|(35690[4-8][0-9]{10})|(35691[0-2][0-9]{10})|(35691[4-6][0-9]{10})|(356920[0-9]{10})|(35696[3-4][0-9]{10})|(356966[0-9]{10})|(356969[0-9]{10})|(35697[0-1][0-9]{10})|(35697[8-9][0-9]{10})|(356989[0-9]{10})|(356999[0-9]{10})|(357105[0-9]{10})|(35710[8-9][0-9]{10})|(357110[0-9]{10})|(357239[0-9]{10})|(357266[0-9]{10})|(357300[0-9]{10})|(357325[0-9]{10})|(35736[1-9][0-9]{10})|(35737[0-9][0-9]{10})|(357391[0-9]{10})|(35740[0-3][0-9]{10})|(35742[0-9][0-9]{10})|(35743[0-7][0-9]{10})|(357499[0-9]{10})|(35770[0-6][0-9]{10})|(35770[8-9][0-9]{10})|(357710[0-9]{10})|(357712[0-9]{10})|(35771[4-8][0-9]{10})|(35772[0-1][0-9]{10})|(35777[6-9][0-9]{10})|(357799[0-9]{10})|(358[0-1][0-9][0-9][0-9]{10})|(3582[0-1][0-9][0-9]{10})|(358228[0-9]{10})|(3582[3-9][0-9][0-9]{10})|(3583[0-9][0-9][0-9]{10})|(35840[0-1][0-9]{10})|(358403[0-9]{10})|(35841[0-9][0-9]{10})|(3584[3-5][0-9][0-9]{10})|(3584[7-9][0-9][0-9]{10})|(3585[0-9][0-9][0-9]{10})|(358600[0-9]{10})|(358619[0-9]{10})|(35862[0-4][0-9]{10})|(35863[0-9][0-9]{10})|(358640[0-9]{10})|(35868[1-9][0-9]{10})|(35870[1-6][0-9]{10})|(358708[0-9]{10})|(35871[0-9][0-9]{10})|(358731[0-9]{10})|(358738[0-9]{10})|(35874[0-7][0-9]{10})|(35875[0-9][0-9]{10})|(35876[0-1][0-9]{10})|(35878[0-4][0-9]{10})|(358787[0-9]{10})|(35879[0-1][0-9]{10})|(35879[3-4][0-9]{10})|(358796[0-9]{10})|(358818[0-9]{10})|(35882[1-4][0-9]{10})|(358831[0-9]{10})|(358838[0-9]{10})|(35884[2-3][0-9]{10})|(358845[0-9]{10})|(358847[0-9]{10})|(358849[0-9]{10})|(358851[0-9]{10})|(358860[0-9]{10})|(35886[2-4][0-9]{10})|(35886[7-9][0-9]{10})|(358870[0-9]{10})|(35887[2-5][0-9]{10})|(35887[7-9][0-9]{10})|(35888[0-5][0-9]{10})|(358888[0-9]{10})|(358890[0-9]{10})|(36000[1-9][0-9]{10})|(36001[0-9][0-9]{10})|(36002[1-9][0-9]{10})|(3600[3-9][0-9][0-9]{10})|(3601[0-7][0-9][0-9]{10})|(36018[0-1][0-9]{10})|(36018[6-7][0-9]{10})|(36019[3-9][0-9]{10})|(360[2-4][0-9][0-9][0-9]{10})|(3605[0-4][0-9][0-9]{10})|(36055[0-2][0-9]{10})|(36070[8-9][0-9]{10})|(3607[1-5][0-9][0-9]{10})|(36076[0-4][0-9]{10})|(360769[0-9]{10})|(3607[7-9][0-9][0-9]{10})|(3608[0-9][0-9][0-9]{10})|(3609[0-1][0-9][0-9]{10})|(36092[0-2][0-9]{10})|(36093[5-9][0-9]{10})|(3609[4-5][0-9][0-9]{10})|(36096[0-6][0-9]{10})|(3609[7-9][0-9][0-9]{10})|(36100[0-9][0-9]{10})|(36101[0-2][0-9]{10})|(361063[0-9]{10})|(36111[5-6][0-9]{10})|(361119[0-9]{10})|(361120[0-9]{10})|(36121[8-9][0-9]{10})|(36122[0-9][0-9]{10})|(36123[0-3][0-9]{10})|(36123[5-9][0-9]{10})|(3612[4-5][0-9][0-9]{10})|(36126[1-9][0-9]{10})|(36127[0-3][0-9]{10})|(36129[5-8][0-9]{10})|(3613[0-3][0-9][0-9]{10})|(36134[0-6][0-9]{10})|(361349[0-9]{10})|(3613[5-9][0-9][0-9]{10})|(3614[0-7][0-9][0-9]{10})|(361480[0-9]{10})|(36148[2-9][0-9]{10})|(36149[0-9][0-9]{10})|(36150[0-9][0-9]{10})|(36151[0-3][0-9]{10})|(361558[0-9]{10})|(3616[5-7][0-9][0-9]{10})|(361680[0-9]{10})|(36168[2-9][0-9]{10})|(36169[0-8][0-9]{10})|(36170[8-9][0-9]{10})|(3617[1-4][0-9][0-9]{10})|(36175[0-6][0-9]{10})|(36176[0-7][0-9]{10})|(3617[7-9][0-9][0-9]{10})|(36180[1-9][0-9]{10})|(3618[1-3][0-9][0-9]{10})|(361840[0-9]{10})|(36184[2-9][0-9]{10})|(3618[5-8][0-9][0-9]{10})|(361890[0-9]{10})|(36199[1-9][0-9]{10})|(3620[0-3][0-9][0-9]{10})|(36204[0-4][0-9]{10})|(36204[6-9][0-9]{10})|(36205[0-3][0-9]{10})|(36205[6-9][0-9]{10})|(36206[0-3][0-9]{10})|(36206[5-9][0-9]{10})|(3620[7-9][0-9][0-9]{10})|(3621[0-3][0-9][0-9]{10})|(36214[0-2][0-9]{10})|(36219[1-6][0-9]{10})|(3622[0-9][0-9][0-9]{10})|(3623[0-7][0-9][0-9]{10})|(36238[0-5][0-9]{10})|(36239[2-9][0-9]{10})|(3624[0-2][0-9][0-9]{10})|(36243[0-3][0-9]{10})|(3625[0-5][0-9][0-9]{10})|(362560[0-9]{10})|(36256[3-4][0-9]{10})|(36260[0-1][0-9]{10})|(36260[6-7][0-9]{10})|(36261[1-9][0-9]{10})|(36262[0-9][0-9]{10})|(36263[0-6][0-9]{10})|(362800[0-9]{10})|(363085[0-9]{10})|(363299[0-9]{10})|(3633[0-3][0-9][0-9]{10})|(36334[0-2][0-9]{10})|(36334[5-9][0-9]{10})|(36335[0-2][0-9]{10})|(363402[0-9]{10})|(36341[1-6][0-9]{10})|(363418[0-9]{10})|(363421[0-9]{10})|(363423[0-9]{10})|(36342[5-7][0-9]{10})|(363429[0-9]{10})|(363430[0-9]{10})|(36343[2-7][0-9]{10})|(363439[0-9]{10})|(363440[0-9]{10})|(36344[4-5][0-9]{10})|(363448[0-9]{10})|(36345[1-5][0-9]{10})|(36346[7-8][0-9]{10})|(363470[0-9]{10})|(363474[0-9]{10})|(36347[6-8][0-9]{10})|(36348[0-1][0-9]{10})|(36348[4-5][0-9]{10})|(36349[0-4][0-9]{10})|(363518[0-9]{10})|(363520[0-9]{10})|(363527[0-9]{10})|(36353[7-9][0-9]{10})|(36354[0-6][0-9]{10})|(363549[0-9]{10})|(363550[0-9]{10})|(363571[0-9]{10})|(36372[2-7][0-9]{10})|(363999[0-9]{10})|(3640[0-8][0-9][0-9]{10})|(36409[0-1][0-9]{10})|(36409[3-9][0-9]{10})|(364[1-3][0-9][0-9][0-9]{10})|(3644[0-2][0-9][0-9]{10})|(36443[0-6][0-9]{10})|(36444[1-9][0-9]{10})|(3644[5-7][0-9][0-9]{10})|(364480[0-9]{10})|(36448[2-9][0-9]{10})|(36449[0-9][0-9]{10})|(3645[0-9][0-9][0-9]{10})|(36460[0-9][0-9]{10})|(364610[0-9]{10})|(36461[2-9][0-9]{10})|(3646[2-9][0-9][0-9]{10})|(3647[0-9][0-9][0-9]{10})|(3648[0-2][0-9][0-9]{10})|(364830[0-9]{10})|(36483[3-9][0-9]{10})|(3648[4-6][0-9][0-9]{10})|(36487[0-8][0-9]{10})|(36488[0-8][0-9]{10})|(36490[0-5][0-9]{10})|(36491[0-3][0-9]{10})|(36494[0-2][0-9]{10})|(3650[0-1][0-9][0-9]{10})|(36502[0-3][0-9]{10})|(36502[5-9][0-9]{10})|(3650[3-4][0-9][0-9]{10})|(36510[6-9][0-9]{10})|(3651[1-9][0-9][0-9]{10})|(36520[0-7][0-9]{10})|(365209[0-9]{10})|(36521[0-1][0-9]{10})|(36521[4-5][0-9]{10})|(36521[7-9][0-9]{10})|(3652[2-4][0-9][0-9]{10})|(36525[0-6][0-9]{10})|(36529[8-9][0-9]{10})|(3653[0-8][0-9][0-9]{10})|(36539[0-4][0-9]{10})|(3654[0-6][0-9][0-9]{10})|(36547[0-5][0-9]{10})|(36547[7-9][0-9]{10})|(36548[0-8][0-9]{10})|(36549[4-9][0-9]{10})|(3655[0-3][0-9][0-9]{10})|(36554[0-7][0-9]{10})|(3655[6-7][0-9][0-9]{10})|(36558[0-6][0-9]{10})|(36558[8-9][0-9]{10})|(36559[0-9][0-9]{10})|(36560[0-3][0-9]{10})|(36560[5-9][0-9]{10})|(3656[1-3][0-9][0-9]{10})|(36564[0-3][0-9]{10})|(36564[5-9][0-9]{10})|(3656[5-6][0-9][0-9]{10})|(36567[0-3][0-9]{10})|(36570[0-2][0-9]{10})|(36577[0-3][0-9]{10})|(366[0-9][0-9][0-9][0-9]{10})|(36700[0-2][0-9]{10})|(36701[1-9][0-9]{10})|(3670[2-5][0-9][0-9]{10})|(36706[0-7][0-9]{10})|(367069[0-9]{10})|(36707[1-9][0-9]{10})|(36708[0-9][0-9]{10})|(36709[0-2][0-9]{10})|(36710[5-9][0-9]{10})|(36711[8-9][0-9]{10})|(36712[0-3][0-9]{10})|(36712[6-7][0-9]{10})|(368357[0-9]{10})|(368601[0-9]{10})|(369[0-1][0-9][0-9][0-9]{10})|(36920[0-6][0-9]{10})|(36921[0-5][0-9]{10})|(369[4-5][0-9][0-9][0-9]{10})|(36960[0-5][0-9]{10})|(36960[7-9][0-9]{10})|(36961[0-9][0-9]{10})|(369620[0-9]{10})|(369989[0-9]{10})|(508227[0-9]{10})|(50850[5-6][0-9]{10})|(508509[0-9]{10})|(508510[0-9]{10})|(50851[5-6][0-9]{10})|(508519[0-9]{10})|(50852[1-8][0-9]{10})|(508530[0-9]{10})|(508539[0-9]{10})|(50854[3-4][0-9]{10})|(508549[0-9]{10})|(508551[0-9]{10})|(508553[0-9]{10})|(508555[0-9]{10})|(50855[7-9][0-9]{10})|(508563[0-9]{10})|(50856[6-7][0-9]{10})|(508570[0-9]{10})|(50857[3-4][0-9]{10})|(508579[0-9]{10})|(508582[0-9]{10})|(50858[8-9][0-9]{10})|(50859[0-1][0-9]{10})|(508593[0-9]{10})|(508596[0-9]{10})|(50860[0-1][0-9]{10})|(508603[0-9]{10})|(508607[0-9]{10})|(50861[0-1][0-9]{10})|(508614[0-9]{10})|(508616[0-9]{10})|(508619[0-9]{10})|(508622[0-9]{10})|(508625[0-9]{10})|(508627[0-9]{10})|(508629[0-9]{10})|(508632[0-9]{10})|(50863[4-6][0-9]{10})|(508639[0-9]{10})|(50864[0-3][0-9]{10})|(50864[5-9][0-9]{10})|(50865[0-9][0-9]{10})|(50866[0-1][0-9]{10})|(50866[3-5][0-9]{10})|(508667[0-9]{10})|(508669[0-9]{10})|(50867[0-9][0-9]{10})|(50868[0-7][0-9]{10})|(508689[0-9]{10})|(50869[0-1][0-9]{10})|(50869[3-9][0-9]{10})|(50870[0-1][0-9]{10})|(50870[3-4][0-9]{10})|(50870[6-9][0-9]{10})|(50871[0-3][0-9]{10})|(50871[5-9][0-9]{10})|(5087[2-5][0-9][0-9]{10})|(50876[0-1][0-9]{10})|(50876[3-8][0-9]{10})|(50877[1-9][0-9]{10})|(50878[0-4][0-9]{10})|(508786[0-9]{10})|(50878[8-9][0-9]{10})|(50879[0-5][0-9]{10})|(50879[7-8][0-9]{10})|(50880[0-1][0-9]{10})|(50880[3-9][0-9]{10})|(50881[0-9][0-9]{10})|(50882[0-5][0-9]{10})|(50882[7-9][0-9]{10})|(50883[0-6][0-9]{10})|(508838[0-9]{10})|(50884[0-3][0-9]{10})|(50884[6-9][0-9]{10})|(508850[0-9]{10})|(50885[2-3][0-9]{10})|(50885[5-7][0-9]{10})|(50886[0-1][0-9]{10})|(50886[4-9][0-9]{10})|(50887[0-6][0-9]{10})|(508880[0-9]{10})|(508882[0-9]{10})|(50888[4-5][0-9]{10})|(50888[7-9][0-9]{10})|(50889[0-1][0-9]{10})|(508894[0-9]{10})|(50889[7-9][0-9]{10})|(50890[1-8][0-9]{10})|(50891[2-4][0-9]{10})|(50891[6-8][0-9]{10})|(5089[2-3][0-9][0-9]{10})|(50894[0-2][0-9]{10})|(50894[4-9][0-9]{10})|(50895[1-9][0-9]{10})|(50896[1-9][0-9]{10})|(508971[0-9]{10})|(50897[3-8][0-9]{10})|(50898[0-1][0-9]{10})|(50898[3-4][0-9]{10})|(50898[7-9][0-9]{10})|(50899[0-9][0-9]{10})|(546584[0-9]{10})|(586956[0-9]{10})|(60110[0-9][0-9]{10})|(6011[2-4][0-9][0-9]{10})|(601174[0-9]{10})|(60117[7-9][0-9]{10})|(60118[6-9][0-9]{10})|(60119[0-9][0-9]{10})|(601382[0-9]{10})|(601428[0-9]{10})|(602907[0-9]{10})|(602969[0-9]{10})|(603265[0-9]{10})|(603367[0-9]{10})|(603601[0-9]{10})|(603694[0-9]{10})|(603708[0-9]{10})|(606985[0-9]{10})|(60698[7-9][0-9]{10})|(60699[0-6][0-9]{10})|(60699[8-9][0-9]{10})|(60700[0-7][0-9]{10})|(607009[0-9]{10})|(60701[0-4][0-9]{10})|(607016[0-9]{10})|(60701[8-9][0-9]{10})|(6070[2-7][0-9][0-9]{10})|(607080[0-9]{10})|(60708[4-5][0-9]{10})|(60708[7-9][0-9]{10})|(60709[0-1][0-9]{10})|(60709[3-4][0-9]{10})|(607096[0-9]{10})|(60709[8-9][0-9]{10})|(60710[0-3][0-9]{10})|(60710[5-9][0-9]{10})|(60711[1-5][0-9]{10})|(607117[0-9]{10})|(607119[0-9]{10})|(6071[2-3][0-9][0-9]{10})|(60714[0-1][0-9]{10})|(60714[3-9][0-9]{10})|(60715[0-1][0-9]{10})|(60715[4-9][0-9]{10})|(60716[0-5][0-9]{10})|(607168[0-9]{10})|(6071[7-9][0-9][0-9]{10})|(6072[0-1][0-9][0-9]{10})|(60722[0-1][0-9]{10})|(60722[3-7][0-9]{10})|(607229[0-9]{10})|(60723[0-5][0-9]{10})|(60723[7-8][0-9]{10})|(60724[0-3][0-9]{10})|(60724[6-9][0-9]{10})|(60725[0-1][0-9]{10})|(60725[3-9][0-9]{10})|(60726[0-6][0-9]{10})|(60726[8-9][0-9]{10})|(60727[0-9][0-9]{10})|(60728[0-7][0-9]{10})|(607289[0-9]{10})|(60729[0-6][0-9]{10})|(60729[8-9][0-9]{10})|(60730[0-5][0-9]{10})|(60730[7-9][0-9]{10})|(60731[0-9][0-9]{10})|(60732[0-4][0-9]{10})|(60732[6-9][0-9]{10})|(60733[1-2][0-9]{10})|(60733[4-6][0-9]{10})|(607338[0-9]{10})|(607340[0-9]{10})|(60734[3-9][0-9]{10})|(607350[0-9]{10})|(607352[0-9]{10})|(607354[0-9]{10})|(607356[0-9]{10})|(60735[8-9][0-9]{10})|(60736[1-9][0-9]{10})|(60737[0-9][0-9]{10})|(607380[0-9]{10})|(607382[0-9]{10})|(60738[5-6][0-9]{10})|(60738[8-9][0-9]{10})|(60739[2-3][0-9]{10})|(60739[6-7][0-9]{10})|(60740[1-9][0-9]{10})|(60741[0-3][0-9]{10})|(60741[5-9][0-9]{10})|(60742[1-3][0-9]{10})|(60742[5-9][0-9]{10})|(60743[0-9][0-9]{10})|(607440[0-9]{10})|(60744[2-3][0-9]{10})|(60744[5-9][0-9]{10})|(60745[0-9][0-9]{10})|(60746[1-9][0-9]{10})|(60747[0-4][0-9]{10})|(60747[6-9][0-9]{10})|(607480[0-9]{10})|(607484[0-9]{10})|(60748[7-9][0-9]{10})|(60749[0-9][0-9]{10})|(60750[0-9][0-9]{10})|(60751[0-6][0-9]{10})|(60751[8-9][0-9]{10})|(6075[2-3][0-9][0-9]{10})|(60754[0-4][0-9]{10})|(607546[0-9]{10})|(60754[8-9][0-9]{10})|(6075[5-6][0-9][0-9]{10})|(607570[0-9]{10})|(607573[0-9]{10})|(60757[5-6][0-9]{10})|(60758[0-1][0-9]{10})|(60758[4-5][0-9]{10})|(60758[7-9][0-9]{10})|(607590[0-9]{10})|(60759[2-5][0-9]{10})|(607597[0-9]{10})|(60760[0-2][0-9]{10})|(607604[0-9]{10})|(60760[6-8][0-9]{10})|(60761[0-3][0-9]{10})|(607615[0-9]{10})|(607618[0-9]{10})|(607620[0-9]{10})|(60762[2-4][0-9]{10})|(60762[6-9][0-9]{10})|(60763[1-2][0-9]{10})|(607634[0-9]{10})|(60763[6-9][0-9]{10})|(60764[0-3][0-9]{10})|(607645[0-9]{10})|(60764[7-9][0-9]{10})|(607650[0-9]{10})|(607652[0-9]{10})|(607654[0-9]{10})|(607656[0-9]{10})|(60765[8-9][0-9]{10})|(60766[0-4][0-9]{10})|(60766[6-9][0-9]{10})|(60767[0-3][0-9]{10})|(60767[6-9][0-9]{10})|(60768[0-2][0-9]{10})|(60768[4-5][0-9]{10})|(60768[8-9][0-9]{10})|(60769[0-1][0-9]{10})|(60769[3-4][0-9]{10})|(607696[0-9]{10})|(607699[0-9]{10})|(607702[0-9]{10})|(607704[0-9]{10})|(60770[6-9][0-9]{10})|(6077[1-2][0-9][0-9]{10})|(60773[0-1][0-9]{10})|(60773[3-6][0-9]{10})|(60773[8-9][0-9]{10})|(60774[0-3][0-9]{10})|(60774[5-9][0-9]{10})|(60775[0-8][0-9]{10})|(60776[1-5][0-9]{10})|(607767[0-9]{10})|(60777[0-3][0-9]{10})|(60777[5-9][0-9]{10})|(60778[0-4][0-9]{10})|(60778[6-8][0-9]{10})|(60779[1-2][0-9]{10})|(60779[4-9][0-9]{10})|(60780[0-9][0-9]{10})|(60781[0-2][0-9]{10})|(607814[0-9]{10})|(60781[6-9][0-9]{10})|(60782[0-1][0-9]{10})|(60782[3-9][0-9]{10})|(6078[3-4][0-9][0-9]{10})|(60785[0-1][0-9]{10})|(60785[3-9][0-9]{10})|(607860[0-9]{10})|(60786[2-6][0-9]{10})|(60786[8-9][0-9]{10})|(60787[0-9][0-9]{10})|(60788[0-3][0-9]{10})|(60788[5-7][0-9]{10})|(607889[0-9]{10})|(60789[0-2][0-9]{10})|(607894[0-9]{10})|(607896[0-9]{10})|(60789[8-9][0-9]{10})|(60790[0-7][0-9]{10})|(607909[0-9]{10})|(60791[0-1][0-9]{10})|(607918[0-9]{10})|(60792[0-1][0-9]{10})|(60792[3-9][0-9]{10})|(607930[0-9]{10})|(60793[2-7][0-9]{10})|(607939[0-9]{10})|(60794[0-3][0-9]{10})|(60794[5-6][0-9]{10})|(60794[8-9][0-9]{10})|(60795[0-5][0-9]{10})|(60795[7-9][0-9]{10})|(6079[6-7][0-9][0-9]{10})|(60798[0-2][0-9]{10})|(60800[1-2][0-9]{10})|(60800[5-9][0-9]{10})|(60801[0-4][0-9]{10})|(60801[6-9][0-9]{10})|(60802[0-9][0-9]{10})|(60803[0-4][0-9]{10})|(60803[6-8][0-9]{10})|(60804[1-4][0-9]{10})|(60804[7-8][0-9]{10})|(608051[0-9]{10})|(60805[3-9][0-9]{10})|(60806[0-6][0-9]{10})|(60806[8-9][0-9]{10})|(60807[0-1][0-9]{10})|(60807[3-7][0-9]{10})|(608079[0-9]{10})|(6080[8-9][0-9][0-9]{10})|(60810[0-2][0-9]{10})|(60810[5-9][0-9]{10})|(60811[0-1][0-9]{10})|(608113[0-9]{10})|(60811[5-6][0-9]{10})|(60811[8-9][0-9]{10})|(6081[2-3][0-9][0-9]{10})|(60814[1-7][0-9]{10})|(608149[0-9]{10})|(60815[1-9][0-9]{10})|(60816[0-3][0-9]{10})|(60816[5-6][0-9]{10})|(60816[8-9][0-9]{10})|(60817[0-4][0-9]{10})|(60817[6-9][0-9]{10})|(60818[0-9][0-9]{10})|(60819[1-8][0-9]{10})|(608201[0-9]{10})|(60822[6-9][0-9]{10})|(60823[0-9][0-9]{10})|(60824[0-3][0-9]{10})|(608245[0-9]{10})|(60824[7-9][0-9]{10})|(608250[0-9]{10})|(60825[6-8][0-9]{10})|(60826[0-3][0-9]{10})|(60826[6-7][0-9]{10})|(608269[0-9]{10})|(60827[1-2][0-9]{10})|(608274[0-9]{10})|(608279[0-9]{10})|(60828[3-7][0-9]{10})|(608289[0-9]{10})|(60829[1-4][0-9]{10})|(60829[6-7][0-9]{10})|(608299[0-9]{10})|(608305[0-9]{10})|(608311[0-9]{10})|(60831[5-6][0-9]{10})|(608320[0-9]{10})|(608323[0-9]{10})|(60835[2-3][0-9]{10})|(608356[0-9]{10})|(60837[5-6][0-9]{10})|(608379[0-9]{10})|(6083[8-9][0-9][0-9]{10})|(60840[0-7][0-9]{10})|(608409[0-9]{10})|(60841[0-4][0-9]{10})|(60841[6-9][0-9]{10})|(60842[0-2][0-9]{10})|(60842[4-9][0-9]{10})|(608430[0-9]{10})|(60843[2-4][0-9]{10})|(60843[7-9][0-9]{10})|(60844[1-3][0-9]{10})|(60844[5-6][0-9]{10})|(60844[8-9][0-9]{10})|(608450[0-9]{10})|(60845[6-7][0-9]{10})|(60846[1-3][0-9]{10})|(608469[0-9]{10})|(608475[0-9]{10})|(60847[7-8][0-9]{10})|(620009[0-9]{10})|(62001[0-1][0-9]{10})|(620013[0-9]{10})|(620019[0-9]{10})|(620021[0-9]{10})|(620024[0-9]{10})|(620031[0-9]{10})|(620035[0-9]{10})|(620037[0-9]{10})|(620039[0-9]{10})|(620046[0-9]{10})|(620048[0-9]{10})|(620054[0-9]{10})|(620056[0-9]{10})|(62006[3-4][0-9]{10})|(620072[0-9]{10})|(620079[0-9]{10})|(620086[0-9]{10})|(620088[0-9]{10})|(620091[0-9]{10})|(620094[0-9]{10})|(620103[0-9]{10})|(62010[5-6][0-9]{10})|(620108[0-9]{10})|(620114[0-9]{10})|(620118[0-9]{10})|(620120[0-9]{10})|(62012[3-6][0-9]{10})|(620129[0-9]{10})|(62013[1-2][0-9]{10})|(620136[0-9]{10})|(62014[2-3][0-9]{10})|(620146[0-9]{10})|(62014[8-9][0-9]{10})|(62018[3-7][0-9]{10})|(62020[2-9][0-9]{10})|(62021[0-1][0-9]{10})|(620218[0-9]{10})|(620220[0-9]{10})|(620513[0-9]{10})|(62051[5-8][0-9]{10})|(620522[0-9]{10})|(62052[7-9][0-9]{10})|(62053[0-3][0-9]{10})|(620535[0-9]{10})|(62053[7-8][0-9]{10})|(62055[0-2][0-9]{10})|(620559[0-9]{10})|(620561[0-9]{10})|(620812[0-9]{10})|(62100[3-6][0-9]{10})|(621008[0-9]{10})|(62101[0-5][0-9]{10})|(62101[7-9][0-9]{10})|(62102[0-1][0-9]{10})|(62102[3-6][0-9]{10})|(62102[8-9][0-9]{10})|(62103[0-3][0-9]{10})|(62103[5-9][0-9]{10})|(62104[1-5][0-9]{10})|(621049[0-9]{10})|(621050[0-9]{10})|(62105[2-3][0-9]{10})|(62105[5-9][0-9]{10})|(62106[0-3][0-9]{10})|(62106[5-9][0-9]{10})|(62107[0-8][0-9]{10})|(62108[0-4][0-9]{10})|(621086[0-9]{10})|(62108[8-9][0-9]{10})|(62109[0-2][0-9]{10})|(62109[5-9][0-9]{10})|(62120[0-1][0-9]{10})|(62121[2-3][0-9]{10})|(62121[5-9][0-9]{10})|(62122[0-8][0-9]{10})|(62123[0-5][0-9]{10})|(62123[7-9][0-9]{10})|(62124[0-9][0-9]{10})|(62125[0-2][0-9]{10})|(62125[4-9][0-9]{10})|(621260[0-9]{10})|(62126[3-9][0-9]{10})|(62127[0-9][0-9]{10})|(62128[0-8][0-9]{10})|(62129[0-9][0-9]{10})|(621300[0-9]{10})|(621308[0-9]{10})|(621310[0-9]{10})|(621316[0-9]{10})|(62132[4-8][0-9]{10})|(6213[3-4][0-9][0-9]{10})|(62135[0-6][0-9]{10})|(621359[0-9]{10})|(62136[0-9][0-9]{10})|(62137[1-3][0-9]{10})|(62137[5-9][0-9]{10})|(6213[8-9][0-9][0-9]{10})|(621401[0-9]{10})|(621403[0-9]{10})|(62141[1-9][0-9]{10})|(621420[0-9]{10})|(62142[2-3][0-9]{10})|(621428[0-9]{10})|(62143[3-4][0-9]{10})|(62143[6-9][0-9]{10})|(62144[0-3][0-9]{10})|(62144[5-9][0-9]{10})|(62145[2-3][0-9]{10})|(62145[5-8][0-9]{10})|(62146[0-9][0-9]{10})|(62147[5-6][0-9]{10})|(62148[0-8][0-9]{10})|(62149[0-2][0-9]{10})|(62149[4-9][0-9]{10})|(62151[5-9][0-9]{10})|(62152[0-3][0-9]{10})|(62152[5-9][0-9]{10})|(62153[0-3][0-9]{10})|(62153[6-9][0-9]{10})|(62155[0-2][0-9]{10})|(62155[7-9][0-9]{10})|(62156[0-3][0-9]{10})|(621566[0-9]{10})|(62156[8-9][0-9]{10})|(62157[7-9][0-9]{10})|(621580[0-9]{10})|(621582[0-9]{10})|(621585[0-9]{10})|(62158[8-9][0-9]{10})|(62159[0-2][0-9]{10})|(62159[8-9][0-9]{10})|(62160[0-1][0-9]{10})|(621619[0-9]{10})|(621621[0-9]{10})|(62162[3-8][0-9]{10})|(62163[0-1][0-9]{10})|(621633[0-9]{10})|(62163[5-9][0-9]{10})|(62164[0-2][0-9]{10})|(621645[0-9]{10})|(62164[8-9][0-9]{10})|(62165[0-9][0-9]{10})|(62166[0-6][0-9]{10})|(62166[8-9][0-9]{10})|(62167[0-6][0-9]{10})|(62167[8-9][0-9]{10})|(62168[0-5][0-9]{10})|(62168[7-9][0-9]{10})|(62169[0-7][0-9]{10})|(621699[0-9]{10})|(621700[0-9]{10})|(621719[0-9]{10})|(62172[0-3][0-9]{10})|(62172[5-8][0-9]{10})|(62173[0-3][0-9]{10})|(62173[5-9][0-9]{10})|(62174[0-3][0-9]{10})|(62174[6-9][0-9]{10})|(6217[5-6][0-9][0-9]{10})|(62177[0-3][0-9]{10})|(621775[0-9]{10})|(62177[7-9][0-9]{10})|(6217[8-9][0-9][0-9]{10})|(621977[0-9]{10})|(62212[6-9][0-9]{10})|(62213[0-6][0-9]{10})|(62213[8-9][0-9]{10})|(62214[0-1][0-9]{10})|(62214[3-9][0-9]{10})|(62215[0-9][0-9]{10})|(62216[2-9][0-9]{10})|(62217[0-1][0-9]{10})|(62217[3-4][0-9]{10})|(62217[6-9][0-9]{10})|(622180[0-9]{10})|(62218[2-4][0-9]{10})|(62218[7-9][0-9]{10})|(622200[0-9]{10})|(62220[2-3][0-9]{10})|(622206[0-9]{10})|(622208[0-9]{10})|(62221[0-5][0-9]{10})|(622218[0-9]{10})|(622220[0-9]{10})|(62222[3-5][0-9]{10})|(62222[7-9][0-9]{10})|(62223[0-9][0-9]{10})|(622240[0-9]{10})|(62224[5-6][0-9]{10})|(62225[2-9][0-9]{10})|(62226[0-2][0-9]{10})|(62226[5-9][0-9]{10})|(62227[0-1][0-9]{10})|(622273[0-9]{10})|(62227[5-9][0-9]{10})|(6222[8-9][0-9][0-9]{10})|(62230[0-3][0-9]{10})|(622305[0-9]{10})|(62230[7-9][0-9]{10})|(62231[0-2][0-9]{10})|(62231[5-6][0-9]{10})|(62231[8-9][0-9]{10})|(62232[0-5][0-9]{10})|(62232[8-9][0-9]{10})|(62233[1-3][0-9]{10})|(62233[5-6][0-9]{10})|(62233[8-9][0-9]{10})|(62234[1-3][0-9]{10})|(622345[0-9]{10})|(622348[0-9]{10})|(622351[0-9]{10})|(62235[4-6][0-9]{10})|(62235[8-9][0-9]{10})|(62236[0-3][0-9]{10})|(62236[7-9][0-9]{10})|(622370[0-9]{10})|(62237[2-3][0-9]{10})|(62237[6-9][0-9]{10})|(622380[0-9]{10})|(62238[3-9][0-9]{10})|(62239[1-9][0-9]{10})|(622400[0-9]{10})|(622403[0-9]{10})|(62240[6-7][0-9]{10})|(622409[0-9]{10})|(62241[0-6][0-9]{10})|(62242[0-1][0-9]{10})|(62242[5-9][0-9]{10})|(62243[2-3][0-9]{10})|(62243[5-6][0-9]{10})|(622439[0-9]{10})|(62244[0-5][0-9]{10})|(62244[8-9][0-9]{10})|(62245[0-2][0-9]{10})|(622455[0-9]{10})|(62246[2-3][0-9]{10})|(62246[5-9][0-9]{10})|(622470[0-9]{10})|(62247[5-9][0-9]{10})|(62248[0-6][0-9]{10})|(62248[8-9][0-9]{10})|(62249[1-3][0-9]{10})|(62249[8-9][0-9]{10})|(622506[0-9]{10})|(62250[8-9][0-9]{10})|(62251[0-1][0-9]{10})|(62251[6-9][0-9]{10})|(62252[0-3][0-9]{10})|(62252[5-6][0-9]{10})|(62253[2-3][0-9]{10})|(62253[5-9][0-9]{10})|(62254[7-9][0-9]{10})|(622550[0-9]{10})|(62255[5-9][0-9]{10})|(62256[0-3][0-9]{10})|(62256[5-9][0-9]{10})|(62257[0-3][0-9]{10})|(62257[5-9][0-9]{10})|(62258[0-4][0-9]{10})|(622588[0-9]{10})|(62259[1-3][0-9]{10})|(62259[5-7][0-9]{10})|(622599[0-9]{10})|(62260[0-3][0-9]{10})|(622609[0-9]{10})|(622611[0-9]{10})|(622613[0-9]{10})|(62261[5-9][0-9]{10})|(62262[0-3][0-9]{10})|(62262[5-6][0-9]{10})|(62263[0-3][0-9]{10})|(62263[6-8][0-9]{10})|(622644[0-9]{10})|(622648[0-9]{10})|(62265[0-1][0-9]{10})|(62265[5-9][0-9]{10})|(6226[6-8][0-9][0-9]{10})|(62269[0-3][0-9]{10})|(62269[5-6][0-9]{10})|(622698[0-9]{10})|(622700[0-9]{10})|(62270[7-8][0-9]{10})|(622710[0-9]{10})|(62271[7-8][0-9]{10})|(622722[0-9]{10})|(622740[0-9]{10})|(62275[1-9][0-9]{10})|(62276[0-8][0-9]{10})|(62277[0-2][0-9]{10})|(622775[0-9]{10})|(62277[7-8][0-9]{10})|(622785[0-9]{10})|(62278[8-9][0-9]{10})|(622790[0-9]{10})|(622798[0-9]{10})|(62280[1-2][0-9]{10})|(622806[0-9]{10})|(62280[8-9][0-9]{10})|(62281[0-3][0-9]{10})|(62281[5-8][0-9]{10})|(62282[0-9][0-9]{10})|(622830[0-9]{10})|(62283[5-9][0-9]{10})|(62284[0-1][0-9]{10})|(62284[3-9][0-9]{10})|(62285[1-3][0-9]{10})|(62285[5-9][0-9]{10})|(622860[0-9]{10})|(622862[0-9]{10})|(62286[4-9][0-9]{10})|(622870[0-9]{10})|(622873[0-9]{10})|(62287[5-9][0-9]{10})|(62288[0-6][0-9]{10})|(62288[8-9][0-9]{10})|(62289[0-3][0-9]{10})|(622895[0-9]{10})|(62289[7-9][0-9]{10})|(62290[0-2][0-9]{10})|(622906[0-9]{10})|(62290[8-9][0-9]{10})|(62291[0-3][0-9]{10})|(622916[0-9]{10})|(62291[8-9][0-9]{10})|(62292[0-2][0-9]{10})|(622925[0-9]{10})|(62292[7-8][0-9]{10})|(62293[0-3][0-9]{10})|(62293[5-9][0-9]{10})|(622940[0-9]{10})|(62294[2-3][0-9]{10})|(62294[5-9][0-9]{10})|(62295[0-5][0-9]{10})|(622957[0-9]{10})|(622959[0-9]{10})|(62296[0-3][0-9]{10})|(62296[6-8][0-9]{10})|(62297[1-3][0-9]{10})|(62297[5-9][0-9]{10})|(62298[0-3][0-9]{10})|(622986[0-9]{10})|(62298[8-9][0-9]{10})|(62299[0-9][0-9]{10})|(62300[0-1][0-9]{10})|(62300[3-4][0-9]{10})|(623007[0-9]{10})|(623010[0-9]{10})|(62301[6-9][0-9]{10})|(6230[2-3][0-9][0-9]{10})|(62304[0-5][0-9]{10})|(62304[7-8][0-9]{10})|(62305[0-3][0-9]{10})|(62305[5-9][0-9]{10})|(62306[0-8][0-9]{10})|(6230[7-9][0-9][0-9]{10})|(62310[1-9][0-9]{10})|(62311[0-3][0-9]{10})|(62311[5-9][0-9]{10})|(62312[0-9][0-9]{10})|(62313[0-3][0-9]{10})|(62313[5-9][0-9]{10})|(623140[0-9]{10})|(62314[2-3][0-9]{10})|(62314[5-7][0-9]{10})|(623149[0-9]{10})|(62315[0-3][0-9]{10})|(62315[5-9][0-9]{10})|(6231[6-8][0-9][0-9]{10})|(623190[0-9]{10})|(62319[3-9][0-9]{10})|(62320[0-1][0-9]{10})|(623203[0-9]{10})|(62320[5-9][0-9]{10})|(62321[0-1][0-9]{10})|(623213[0-9]{10})|(623216[0-9]{10})|(62321[8-9][0-9]{10})|(62322[0-1][0-9]{10})|(623229[0-9]{10})|(623250[0-9]{10})|(623255[0-9]{10})|(62325[8-9][0-9]{10})|(62326[1-3][0-9]{10})|(623265[0-9]{10})|(623267[0-9]{10})|(623293[0-9]{10})|(623300[0-9]{10})|(62330[2-4][0-9]{10})|(62330[7-9][0-9]{10})|(62331[0-8][0-9]{10})|(623321[0-9]{10})|(62332[3-8][0-9]{10})|(62333[1-2][0-9]{10})|(623334[0-9]{10})|(62333[6-9][0-9]{10})|(623341[0-9]{10})|(62334[4-9][0-9]{10})|(62335[0-3][0-9]{10})|(623355[0-9]{10})|(62335[7-9][0-9]{10})|(623360[0-9]{10})|(623362[0-9]{10})|(62336[5-9][0-9]{10})|(62337[0-6][0-9]{10})|(623379[0-9]{10})|(62338[0-6][0-9]{10})|(62338[8-9][0-9]{10})|(62339[1-9][0-9]{10})|(623402[0-9]{10})|(62340[4-6][0-9]{10})|(62340[8-9][0-9]{10})|(62341[0-9][0-9]{10})|(62342[0-5][0-9]{10})|(62342[7-9][0-9]{10})|(62343[0-5][0-9]{10})|(62343[8-9][0-9]{10})|(6234[4-5][0-9][0-9]{10})|(62346[0-2][0-9]{10})|(62346[4-7][0-9]{10})|(62347[0-1][0-9]{10})|(62347[6-9][0-9]{10})|(62348[0-1][0-9]{10})|(62348[3-5][0-9]{10})|(62348[7-8][0-9]{10})|(62349[1-3][0-9]{10})|(62349[5-9][0-9]{10})|(62350[1-6][0-9]{10})|(62350[8-9][0-9]{10})|(62351[0-9][0-9]{10})|(62352[1-9][0-9]{10})|(62353[0-3][0-9]{10})|(623535[0-9]{10})|(62353[7-9][0-9]{10})|(62354[0-3][0-9]{10})|(62354[7-9][0-9]{10})|(62355[0-9][0-9]{10})|(62356[0-3][0-9]{10})|(62356[5-9][0-9]{10})|(6235[7-8][0-9][0-9]{10})|(623590[0-9]{10})|(62359[2-9][0-9]{10})|(62360[0-1][0-9]{10})|(62360[3-4][0-9]{10})|(62360[6-9][0-9]{10})|(62361[0-1][0-9]{10})|(623613[0-9]{10})|(62361[5-9][0-9]{10})|(62362[1-8][0-9]{10})|(62363[0-5][0-9]{10})|(623637[0-9]{10})|(623639[0-9]{10})|(62364[0-4][0-9]{10})|(62364[6-8][0-9]{10})|(62365[0-9][0-9]{10})|(62366[0-5][0-9]{10})|(62366[7-9][0-9]{10})|(623670[0-9]{10})|(62367[2-9][0-9]{10})|(623680[0-9]{10})|(623683[0-9]{10})|(62368[6-8][0-9]{10})|(623694[0-9]{10})|(62369[8-9][0-9]{10})|(624300[0-9]{10})|(62430[2-3][0-9]{10})|(62430[5-6][0-9]{10})|(62430[8-9][0-9]{10})|(624313[0-9]{10})|(624319[0-9]{10})|(62432[0-5][0-9]{10})|(624329[0-9]{10})|(62433[1-3][0-9]{10})|(62433[7-9][0-9]{10})|(62434[1-6][0-9]{10})|(62434[8-9][0-9]{10})|(62435[0-7][0-9]{10})|(62436[4-8][0-9]{10})|(62437[0-8][0-9]{10})|(62438[0-2][0-9]{10})|(62438[4-8][0-9]{10})|(62439[0-9][0-9]{10})|(62440[0-1][0-9]{10})|(62440[4-5][0-9]{10})|(62440[7-9][0-9]{10})|(62441[0-3][0-9]{10})|(62441[5-9][0-9]{10})|(624420[0-9]{10})|(62442[2-6][0-9]{10})|(62442[8-9][0-9]{10})|(62443[0-2][0-9]{10})|(62443[4-8][0-9]{10})|(62444[0-3][0-9]{10})|(62444[5-6][0-9]{10})|(624448[0-9]{10})|(62445[3-5][0-9]{10})|(62445[7-9][0-9]{10})|(624463[0-9]{10})|(62446[5-9][0-9]{10})|(624470[0-9]{10})|(624472[0-9]{10})|(624474[0-9]{10})|(62447[6-9][0-9]{10})|(62448[0-3][0-9]{10})|(624485[0-9]{10})|(62448[8-9][0-9]{10})|(624490[0-9]{10})|(624494[0-9]{10})|(62449[6-7][0-9]{10})|(624518[0-9]{10})|(624580[0-9]{10})|(624624[0-9]{10})|(62500[1-4][0-9]{10})|(62500[6-9][0-9]{10})|(62501[0-4][0-9]{10})|(62501[6-9][0-9]{10})|(62502[0-6][0-9]{10})|(62502[8-9][0-9]{10})|(62503[1-6][0-9]{10})|(62503[8-9][0-9]{10})|(625040[0-9]{10})|(625042[0-9]{10})|(625044[0-9]{10})|(625046[0-9]{10})|(625048[0-9]{10})|(625050[0-9]{10})|(62505[2-3][0-9]{10})|(625055[0-9]{10})|(625058[0-9]{10})|(625060[0-9]{10})|(62506[2-3][0-9]{10})|(62507[1-9][0-9]{10})|(625080[0-9]{10})|(62508[2-8][0-9]{10})|(62509[0-3][0-9]{10})|(62509[5-9][0-9]{10})|(62510[1-3][0-9]{10})|(62510[6-7][0-9]{10})|(62511[0-6][0-9]{10})|(625119[0-9]{10})|(62512[0-6][0-9]{10})|(62512[8-9][0-9]{10})|(62513[1-2][0-9]{10})|(62513[5-6][0-9]{10})|(62513[8-9][0-9]{10})|(62514[0-1][0-9]{10})|(625143[0-9]{10})|(625145[0-9]{10})|(625147[0-9]{10})|(625150[0-9]{10})|(62515[2-9][0-9]{10})|(62516[0-9][0-9]{10})|(62517[0-4][0-9]{10})|(62517[6-9][0-9]{10})|(625180[0-9]{10})|(62518[2-3][0-9]{10})|(62518[6-9][0-9]{10})|(62519[0-3][0-9]{10})|(62519[5-6][0-9]{10})|(625198[0-9]{10})|(62520[0-3][0-9]{10})|(62520[5-6][0-9]{10})|(62520[8-9][0-9]{10})|(625212[0-9]{10})|(625216[0-9]{10})|(625220[0-9]{10})|(62522[5-6][0-9]{10})|(62524[3-7][0-9]{10})|(625249[0-9]{10})|(625250[0-9]{10})|(625288[0-9]{10})|(625309[0-9]{10})|(625320[0-9]{10})|(62533[0-3][0-9]{10})|(62533[5-9][0-9]{10})|(62535[0-3][0-9]{10})|(62535[6-9][0-9]{10})|(62536[0-3][0-9]{10})|(62536[6-9][0-9]{10})|(625370[0-9]{10})|(625500[0-9]{10})|(62550[2-3][0-9]{10})|(625506[0-9]{10})|(625516[0-9]{10})|(625519[0-9]{10})|(625526[0-9]{10})|(625529[0-9]{10})|(625568[0-9]{10})|(625577[0-9]{10})|(625586[0-9]{10})|(625588[0-9]{10})|(625598[0-9]{10})|(625603[0-9]{10})|(62560[5-6][0-9]{10})|(62565[0-3][0-9]{10})|(62565[7-8][0-9]{10})|(62570[0-1][0-9]{10})|(62570[8-9][0-9]{10})|(62580[0-9][0-9]{10})|(62581[0-1][0-9]{10})|(625814[0-9]{10})|(62581[6-9][0-9]{10})|(62582[0-1][0-9]{10})|(62582[3-9][0-9]{10})|(625831[0-9]{10})|(62583[3-4][0-9]{10})|(62583[6-9][0-9]{10})|(62584[0-3][0-9]{10})|(625850[0-9]{10})|(62585[8-9][0-9]{10})|(625860[0-9]{10})|(62586[5-6][0-9]{10})|(625886[0-9]{10})|(62588[8-9][0-9]{10})|(625890[0-9]{10})|(625899[0-9]{10})|(6259[0-1][0-9][0-9]{10})|(62592[0-4][0-9]{10})|(62592[6-9][0-9]{10})|(62593[0-4][0-9]{10})|(625939[0-9]{10})|(62594[0-4][0-9]{10})|(625946[0-9]{10})|(625950[0-9]{10})|(62595[2-3][0-9]{10})|(62595[5-9][0-9]{10})|(6259[6-7][0-9][0-9]{10})|(62598[0-1][0-9]{10})|(62598[6-9][0-9]{10})|(62599[2-3][0-9]{10})|(62599[5-9][0-9]{10})|(62620[0-2][0-9]{10})|(62620[4-6][0-9]{10})|(626208[0-9]{10})|(626210[0-9]{10})|(626212[0-9]{10})|(626214[0-9]{10})|(62621[7-8][0-9]{10})|(62622[0-3][0-9]{10})|(62622[5-9][0-9]{10})|(626230[0-9]{10})|(62623[2-7][0-9]{10})|(626239[0-9]{10})|(62624[0-1][0-9]{10})|(62636[0-1][0-9]{10})|(626374[0-9]{10})|(62639[4-6][0-9]{10})|(62639[8-9][0-9]{10})|(62640[0-4][0-9]{10})|(62640[7-9][0-9]{10})|(62641[0-1][0-9]{10})|(62641[4-6][0-9]{10})|(62702[5-8][0-9]{10})|(627040[0-9]{10})|(62706[0-1][0-9]{10})|(627063[0-9]{10})|(62706[5-8][0-9]{10})|(627070[0-9]{10})|(627073[0-9]{10})|(62707[5-6][0-9]{10})|(627078[0-9]{10})|(62820[0-9][0-9]{10})|(62821[0-4][0-9]{10})|(62821[6-9][0-9]{10})|(62822[0-4][0-9]{10})|(62822[6-9][0-9]{10})|(62823[0-9][0-9]{10})|(62824[2-4][0-9]{10})|(62824[7-8][0-9]{10})|(62825[0-5][0-9]{10})|(62825[7-9][0-9]{10})|(62826[0-4][0-9]{10})|(62826[6-9][0-9]{10})|(6282[7-8][0-9][0-9]{10})|(62829[0-1][0-9]{10})|(62829[3-9][0-9]{10})|(62830[0-3][0-9]{10})|(62830[5-9][0-9]{10})|(6283[1-2][0-9][0-9]{10})|(62833[0-2][0-9]{10})|(628336[0-9]{10})|(628339[0-9]{10})|(62834[0-1][0-9]{10})|(628343[0-9]{10})|(62834[5-6][0-9]{10})|(628349[0-9]{10})|(62835[0-1][0-9]{10})|(62835[3-9][0-9]{10})|(62836[0-2][0-9]{10})|(62836[4-9][0-9]{10})|(62837[0-3][0-9]{10})|(628375[0-9]{10})|(62837[7-9][0-9]{10})|(62838[0-2][0-9]{10})|(62838[5-9][0-9]{10})|(62839[1-2][0-9]{10})|(628395[0-9]{10})|(62839[7-9][0-9]{10})|(62844[7-8][0-9]{10})|(62912[8-9][0-9]{10})|(62913[0-3][0-9]{10})|(629135[0-9]{10})|(62913[8-9][0-9]{10})|(62914[0-4][0-9]{10})|(62914[7-9][0-9]{10})|(62915[0-8][0-9]{10})|(62916[1-2][0-9]{10})|(629168[0-9]{10})|(629171[0-9]{10})|(62917[3-8][0-9]{10})|(629180[0-9]{10})|(62926[0-1][0-9]{10})|(629269[0-9]{10})|(62927[1-4][0-9]{10})|(62927[7-9][0-9]{10})|(629281[0-9]{10})|(629284[0-9]{10})|(632062[0-9]{10})|(64[4-9][0-9][0-9][0-9][0-9]{10})|(65[0-1][0-9][0-9][0-9][0-9]{10})|(6520[0-9][0-9][0-9]{10})|(6521[0-4][0-9][0-9]{10})|(65215[0-7][0-9]{10})|(652159[0-9]{10})|(652160[0-9]{10})|(65216[2-3][0-9]{10})|(65216[5-6][0-9]{10})|(65216[8-9][0-9]{10})|(65217[0-2][0-9]{10})|(65217[4-5][0-9]{10})|(65217[7-9][0-9]{10})|(65218[0-3][0-9]{10})|(65218[5-9][0-9]{10})|(652190[0-9]{10})|(65219[3-4][0-9]{10})|(652196[0-9]{10})|(652199[0-9]{10})|(65220[1-9][0-9]{10})|(65221[0-9][0-9]{10})|(652220[0-9]{10})|(652223[0-9]{10})|(65222[8-9][0-9]{10})|(65223[0-9][0-9]{10})|(65224[0-7][0-9]{10})|(652251[0-9]{10})|(65225[5-9][0-9]{10})|(65226[0-4][0-9]{10})|(652400[0-9]{10})|(65285[0-6][0-9]{10})|(65285[8-9][0-9]{10})|(65286[0-5][0-9]{10})|(652870[0-9]{10})|(65287[3-4][0-9]{10})|(652878[0-9]{10})|(6531[5-9][0-9][0-9]{10})|(653[2-9][0-9][0-9][0-9]{10})|(654[0-9][0-9][0-9][0-9]{10})|(655[0-5][0-9][0-9][0-9]{10})|(6556[0-7][0-9][0-9]{10})|(65568[0-4][0-9]{10})|(65568[6-9][0-9]{10})|(65569[2-3][0-9]{10})|(655695[0-9]{10})|(65569[7-9][0-9]{10})|(655[7-9][0-9][0-9][0-9]{10})|(656[0-9][0-9][0-9][0-9]{10})|(657[0-2][0-9][0-9][0-9]{10})|(6573[0-7][0-9][0-9]{10})|(65738[0-4][0-9]{10})|(65738[6-9][0-9]{10})|(657392[0-9]{10})|(65739[5-9][0-9]{10})|(657[4-9][0-9][0-9][0-9]{10})|(65[8-9][0-9][0-9][0-9][0-9]{10})|(685800[0-9]{10})|(690750[0-9]{10})|(690755[0-9]{10})|(703988[0-9]{10})|(757555[0-9]{10})|(817200[0-9]{10})|(878655[0-9]{10})|(888888[0-9]{10})|(900105[0-9]{10})|(900205[0-9]{10})|(900300[0-9]{10})|(905920[0-9]{10})|(911100[0-9]{10})|(940001[0-9]{10})|(940003[0-9]{10})|(940010[0-9]{10})|(940013[0-9]{10})|(940015[0-9]{10})|(940018[0-9]{10})|(94002[0-1][0-9]{10})|(940023[0-9]{10})|(94002[7-8][0-9]{10})|(94003[0-1][0-9]{10})|(940035[0-9]{10})|(940037[0-9]{10})|(940039[0-9]{10})|(940040[0-9]{10})|(94004[6-9][0-9]{10})|(940050[0-9]{10})|(94005[5-6][0-9]{10})|(94006[1-3][0-9]{10})|(940066[0-9]{10})|(940068[0-9]{10})|(94007[3-4][0-9]{10})|(955100[0-9]{10})|(955550[0-9]{10})|(95559[0-3][0-9]{10})|(95588[0-2][0-9]{10})|(955888[0-9]{10})|(955990[0-9]{10})|(966666[0-9]{10})|(96880[7-9][0-9]{10})|(98430[0-3][0-9]{10})|(987654[0-9]{10})|(990027[0-9]{10})|(353800[0-9]{10})/).test(cno) && (cno.length == 16)) {
				return 'RUPAY';
			} else if ((/^(50|56|57|58|6)/).test(cno) && (cno.length == 16 || cno.length == 18 || cno.length == 19)) {
				return 'MAEST'; //Maestro begins with 50 or 56 or 57 or 58 or 6, and length is 16 or 18 or 19.
			} else if ((/^(300|301|302|303|304|305|36|38|3095)/).test(cno) && cno.length == 14) {
				return 'DINERS'; //Diners Club begins with 300-305 or 36 or 38, and length is 14.
			}else if((/^(203040)/).test(cno) && (cno.length == 16)){
				return 'BAJAJ';
			}
			return null; //unknown type
		},

		isCardTypeRupay : function(cardNumber) {
			if(cardNumber.length >= 6) {
				var bin = parseInt(cardNumber.substring(0, 6));
				for (var i = 0; i < rupayBinRange.length; i++) {
					var lowerLimit = parseInt(rupayBinRange[i].split("-")[0]);
					var upperLimit = parseInt(rupayBinRange[i].split("-")[1]);
					if (bin >= lowerLimit && bin <= upperLimit) {
						return true;
					}
				}
			}
			return false;
		},

		setMaestroUI : function(cardType) {
			if (cardType == 'MAEST') {
				$('#dc_cvvMsg_id').show();

				$('#dc_cvv_id').attr('data-validation', 'checkCvv');
				$('#dc_cvv_id').attr('data-msginfo', 'Enter a valid cvv');

				$('#dc_expm_id').attr('data-validation', '');
				$('#dc_expm_id').attr('data-msginfo', '');

				$('#dc_expy_id').attr('data-validation', 'checkExpiryDate[dc]');
				$('#dc_expy_id').attr('data-msginfo', 'Invalid Date');
			} else {
				$('#dc_cvvMsg_id').hide();

				$('#dc_cvv_id').attr('data-validation', 'required|regType[number]|lenCheck[3]');
				$('#dc_cvv_id').attr('data-msginfo', '* required|* number only|* invalid cvv length');

				$('#dc_cvv_id').prop("disabled", false);
				$('#dc_cvv_id').css('background-color' , '#FFFFFF');

				$('#dc_expm_id').attr('data-validation', 'required');
				$('#dc_expm_id').attr('data-msginfo', '* required');

				$('#dc_expy_id').attr('data-validation', 'required|checkExpiryDate[dc]');
				$('#dc_expy_id').attr('data-msginfo', '* required|Invalid Date');
			}
		},

		setBajajUI : function(cardType) {
			if (cardType == 'BAJAJ') {

				$('#emi_cvv_id').attr('data-validation', '');
				$('#emi_cvv_id').attr('data-msginfo', '');

				$('#emi_expm_id').attr('data-validation', '');
				$('#emi_expm_id').attr('data-msginfo', '');

				$('#emi_expy_id').attr('data-validation', '');
				$('#emi_expy_id').attr('data-msginfo', '');
			}

		},

		setSkipOtpUiIfRequired : function(cardType, paymentOption, cardNo) {
			if(cardType == null
					|| typeof(withoutCVVSupportedCardTypeJSON[paymentOption]) == 'undefined'
						|| withoutCVVSupportedCardTypeJSON[paymentOption] == undefined) {
				YatraPaymentUI.cvvRequired = "true";
				return;
			}
			else {
				if(isZeroNonPassThroughPayment)
					YatraPaymentUI.disableCVVforSkipOtpFlow(paymentOption);
				else if("true" == withoutCVVSupportedCardTypeJSON[paymentOption][cardType]  && YatraPaymentUI.getBinDetails(cardNo)["skipOtpForBin"]) {
					YatraPaymentUI.disableCVVforSkipOtpFlow(paymentOption);
					YatraPaymentUI.cvvRequired = "false";
				}
				else {
					YatraPaymentUI.enableCVVforSkipOtpFlow(paymentOption, cardType);
					YatraPaymentUI.cvvRequired = "true";
				}

			}
		},

		setSkipOtpUiForQBIfRequired : function(qbElem, subType,binDetails) {
		    if(typeof(withoutCVVSupportedCardTypeJSON[subType]) == 'undefined'
        	    || withoutCVVSupportedCardTypeJSON[subType] == undefined) {
        		    YatraPaymentUI.cvvRequired = "true";
        		    return;
            }
			else {
				if(isZeroNonPassThroughPayment) {
					YatraPaymentUI.disableQBCVVForCreditCard(qbElem);
					return;
				}
				if(qbElem.attr("cardnumber") == null || qbElem.attr("cardnumber") == undefined)
					return;
				var cardNo = qbElem.attr("cardnumber").replace('-','');
				if("true" == withoutCVVSupportedCardTypeJSON[subType][qbElem.attr("cardtype")] && binDetails.skipOtpForBin) {
					if(subType == "cc")
						YatraPaymentUI.disableQBCVVForCreditCard(qbElem);
					else
						YatraPaymentUI.disableQBCVVForDebitCard(qbElem);
					YatraPaymentUI.cvvRequired = "false";
				}
				else {
					if(subType == "cc")
						YatraPaymentUI.enableQBCVVForCheckedCreditCard(qbElem);
					else
						YatraPaymentUI.enableQBCVVForCheckedDebitCard(qbElem);
					YatraPaymentUI.cvvRequired = "true";
				}
			}
		},

		setMultiPayUiForQBIfRequired : function(qbElem, subType,binDetails) {
        	if(subType == "cc")
        		YatraPaymentUI.enableMultiFlowUiForQBCard(qbElem,"cc",binDetails);
        	else
        		YatraPaymentUI.enableMultiFlowUiForQBCard(qbElem,"dc",binDetails);

        },

		getBinDetails : function(cardNo) {
		    var binDetailParams ={};
        	binDetailParams["cno"] = cardNo;
        	if(detailsJson.isPassthrough!= null){
        	    binDetailParams["passthrough"] = detailsJson.isPassthrough;
        	}else {
        	    binDetailParams["passthrough"] = "false";
        	}
        	binDetailParams["product"] = detailsJson.product;
        	binDetailParams["superPnr"] =  detailsJson.superPnr ;
        	var responseOb ='';
			$.ajax({
				url : "/PaySwift/get-bin-details-v2.htm",
				type : 'POST',
				async: false,
				timeout:"5000",
				data : JSON.stringify(binDetailParams),
				contentType:"application/json; charset=utf-8",
				dataType: 'json',
				cache : false,
				success : function(response) {
					
					responseOb = response;
                                        if (responseOb.binType != null && responseOb.binType != '') {
                                            cardBinTypeFromDB = responseOb.binType;
                                        }
				}, error : function(request, status, error) {
					YatraPaymentUI.recordEventInSentry("Error in receiving Bin Details Response", "info",
					"get-bin-details Error");
				}
			});
			return responseOb;
		},

		validateEMICardAndBank : function(extendedBin, emiBankName) {
			var emiCardBinDetailParams ={};
			var responseOb ='';
			emiCardBinDetailParams["cno"] = extendedBin;
			emiCardBinDetailParams["emiBank"] = emiBankName;
			emiCardBinDetailParams["superPnr"] = detailsJson.superPnr;

			$.ajax({
				url : "/PaySwift/validate_emi_bank_details.htm",
				type : 'POST',
				async: false,
				timeout:"5000",
				data : JSON.stringify(emiCardBinDetailParams),
				contentType:"application/json; charset=utf-8",
				dataType: 'json',
				cache : false,
				success : function(response) {
					responseOb = response;
				}, error : function(request, status, error) {
					responseOb = {"is_successful" : "true", "message" : ""};
				}
			});
			return responseOb;
		},

        enableMultiFlowUiForQBCard : function(qbElem, payOption, binDetails) {
		    if (binDetails != null) {
		    	var cardId = $(qbElem).val();
		    	$(".qb_multiFlowDiv").hide()
		    	var qbMultiPayFlowElem = $('#qb_pmt_MultiFlowId_'+cardId);
		    	//var qbMultiPayRadioDiv = "#" + "qb_pmt_MultiFlowId_"+cardId;
		    	var qbMultiPayRadioDiv = $(qbMultiPayFlowElem).find("#multiflow_type_div_"+ cardId);
		    	var qbMultiPayRadioElem = "#multiflow_type_div_"+ cardId;
		    	var html = "";

		    	if ("true" == binDetails.isMultiPayFlowEnabled && (binDetails.multiPayFlow != null) && (binDetails.multiPayFlow != '')) {
		    		var multiPayOpsArray = binDetails.multiPayFlow.split('|');
		    		html += YatraPaymentUI.getMultiFlowUiTemplate(binDetails.multiPayFlow,payOption,cardId);
		    		$(qbMultiPayRadioDiv).html(html);
		    		if (multiPayOpsArray.length > 1) {
		    			if (!$(qbMultiPayRadioDiv).is(':visible')) {
		    				$(qbMultiPayRadioElem+ ' input:radio[name="processTyperdo"]:nth(0)').attr("checked", "checked");
		    				YatraPaymentUI.paymentProcessType = $("input[name='processTyperdo']:checked").val();
		    			}
		    			$(qbMultiPayFlowElem).show();

		    			// Inserting below line so as to make sure that first option is selected everytime by default.
		    			$(qbMultiPayRadioElem+ ' input:radio[name="processTyperdo"]:nth(0)').click();
		    		} else if (multiPayOpsArray.length == 1) {
		    			$(qbMultiPayRadioElem+ ' input:radio[name="processTyperdo"]:nth(0)').attr("checked", "checked");
		    			$(qbMultiPayFlowElem).hide();
		    			YatraPaymentUI.paymentProcessType = multiPayOpsArray[0];
		    		} else {
		    			YatraPaymentUI.paymentProcessType = "Secure";
		    		}
		    	} else {
		    		YatraPaymentUI.paymentProcessType = "Secure";
		    	}

		    }
	    },

		getEnabledCards : function(paymentOption) {

			if (paymentOption == 'cc')
				return cc_enabledCards;
			else if (paymentOption == 'dc') {
				return dc_enabledCards;
			} else if (paymentOption == 'dcpin') {
				return dcpin_enabledCards;
			} else if (paymentOption == 'rewards') {
				return rw_enabledCards;
			} else if (paymentOption == 'emi') {

				var emiBank = $("#emiBank_select option:selected").val();;
				var emi_enabledCards = "";
				var bankList = "";
				if (banksArray == "")
					return;
				bankList = eval(banksArray);
				for ( var i = 0; i < bankList.length; i++) {
					if (emiBank == bankList[i].bankName) {
						emi_enabledCards = emi_enabledCards + "/" + bankList[i].code;
					}
				}
				emi_enabledCards = emi_enabledCards.substring(1);

				return emi_enabledCards;
			}

		},

		setMaxCreditCardNumberLengthForEMI : function(field, paymentOption, ctype) {
			if(paymentOption == 'emi') {
				var maxCreditCardNumberLength = '16';
				var emiBank = $("#emiBank_select option:selected").val();
				var bankList = "";
				if (banksArray == "")
					return;
				bankList = eval(banksArray);
				for ( var i = 0; i < bankList.length; i++) {
					if (emiBank == bankList[i].bankName && ctype == bankList[i].code) {
						maxCreditCardNumberLength = bankList[i].cardNumberLength;
						break;
					}
				}
				$(field).attr('maxlength', maxCreditCardNumberLength);
			}
			return;
		},

		setMaxCVVLengthForEMI : function(field, paymentOption, ctype) {
			if(paymentOption == 'emi') {
				var maxCVVLength = '3';
				var emiBank = $("#emiBank_select option:selected").val();
				var bankList = "";
				if (banksArray == "")
					return;
				bankList = eval(banksArray);
				for ( var i = 0; i < bankList.length; i++) {
					if (emiBank == bankList[i].bankName && ctype == bankList[i].code) {
						maxCVVLength = bankList[i].cvvLength;
						break;
					}
				}
				if(ctype == 'BAJAJ'){
					$(field).attr('cvvLength', '0');
					$('#emi_cvv_id').attr("maxlength", '0');
					$('#emi_cvv_id').attr('data-validation', '');
				}
				else {
					$(field).attr('cvvLength', maxCVVLength);
					$('#emi_cvv_id').attr("maxlength", maxCVVLength);
					$('#emi_cvv_id').attr('data-validation', 'required|regType[number]|lenCheck[' + maxCVVLength + ']');

				}
			}
			return;
		},


		setCardValidation : function(field, ctype, payOption, enabledCards) {
			$(field).attr('data-validation',
					'required|validateLuhn|cardRegType[VISA]|supportedCard[VISA,' + payOption + ']|enabledCard[VISA,\'' + enabledCards + '\']');
			if(ctype == 'MAEST' || ctype == 'BAJAJ') {
				var validationCriteria = 'required|cardRegType[' + ctype + ']|supportedCard[' + ctype + ',' + payOption + ']|enabledCard[' + ctype
				+ ',' + enabledCards + ']';
				$(field).attr('data-validation', validationCriteria);
			} else if (ctype != null) {
				if(payOption == 'emi') {
					var validationCriteria = 'required|validateLuhn|cardRegType[' + ctype + ']|supportedCard[' + ctype + ',' + payOption + ']|enabledCard[' + ctype
					+ ',' + enabledCards + ']|emiCardBankValidaton';
					$(field).attr('data-validation', validationCriteria);
				}
				else {
					var validationCriteria = 'required|validateLuhn|cardRegType[' + ctype + ']|supportedCard[' + ctype + ',' + payOption + ']|enabledCard[' + ctype
					+ ',' + enabledCards + ']';
					$(field).attr('data-validation', validationCriteria);
				}
			}
		},

		fadeCardLogo : function(ctype) {
			setTimeout(function(){$(".card-logos").removeClass('VISA');$(".card-logos").removeClass('MASTER');$(".card-logos").removeClass('DINERS');
			$(".card-logos").removeClass('AMEX');$(".card-logos").removeClass('RUPAY');  $(".card-logos").removeClass("MAEST")},10)
			setTimeout(function(){$(".card-logos").addClass(ctype)},10)
			$.each($("[cardAttr='pm_cards']:visible > i"), function(i, data) {
				$(this).removeClass('cpmt_fade');
				if (ctype != null && ctype != "undefined") {
					if ($(this).attr('title') != ctype)
						$(this).addClass('cpmt_fade');
				}
			});
		},

		setCardTypeParameter : function(ctype) {
			var payOption = YatraPaymentUI.getPaymentOption();
			if (payOption == 'cc') {
				$("#cc_ctype_id").val(ctype);
			} else if (payOption == 'dc') {
				$("#dc_ctype_id").val(ctype);
			} else if (payOption == 'emi') {
				$("#emi_ctype_id").val(ctype);
			} else if (payOption == 'rewards') {
				$("#rw_ctype_id").val(ctype);
			} else if (payOption == 'dcpin') {
				$("#dcpin_ctype_id").val(ctype);
			}
		},

		setCvvLogo : function(ctype) {
			var paymentOption = YatraPaymentUI.getPaymentOption();
			if (ctype == 'AMEX') {
				if(paymentOption == "emi") {
					var cvvClass = $("#emi_CvvUrl_id").attr('class');
					$('#emi_CvvUrl_id').removeClass(cvvClass);
					$('#emi_CvvUrl_id').addClass("PaymentSprite " + amexCvvURL);
					$('#emi_cvv_id').attr("maxlength", amexCvvLength);
					$('#emi_cvv_id').attr("data-validation", 'required|regType[number]|lenCheck[4]');
					$("#emi_cvvImageText_id").text(amexCvvImageText);
				}
				else {
				var cvvClass = $("#cc_CvvUrl_id").attr('class');
				$('#cc_CvvUrl_id').removeClass(cvvClass);
				$('#cc_CvvUrl_id').addClass("PaymentSprite " + amexCvvURL);
				$('#cc_cvv_id').attr("maxlength", amexCvvLength);
				$('#cc_cvv_id').attr("data-validation", 'required|regType[number]|lenCheck[4]');
				$(".cpmt_cvvBlock .cpmt_CVVtxt").text(amexCvvImageText);
				}
			} else if(ctype == "BAJAJ"){
				var cvvClass = $("#emi_CvvUrl_id").attr('class');
				$('#emi_CvvUrl_id').removeClass(cvvClass);
				$('#emi_CvvUrl_id').addClass("PaymentSprite " + amexCvvURL);
				$('#emi_cvv_id').attr("maxlength", 0);
				$('#emi_cvv_id').attr("data-validation", '');
				$("#emi_cvvImageText_id").text('');
			}else {
				if(paymentOption == "emi")
					YatraPaymentUI.resetEMICardCvv();
				else YatraPaymentUI.resetCreditCardCvv();
			}
		},

		ifArrayContains : function(array, string) {
			for (var j = 0; j < array.length; j++) {
				if (string == array[j])
					return true;
			}
			return false;
		},

		isInternationalSupportedOnProduct : function() {
			if(YatraPaymentUI.byPassInternationalSupportRestrictionOnProduct()) {
				return true;
			}
			var paymentJSON = getPaymentJSON();
			var productCode = paymentJSON.product;
			for (var j = 0; j < intl_productsArr.length; j++) {
				if (productCode == intl_productsArr[j])
					return true;
			}
			return false;
		},

		byPassInternationalSupportRestrictionOnProduct: function() {
			return byPassUIChecksForWhitelistedEmail == "true";
		},

		byPassTravelDateCheck: function() {
			return byPassUIChecksForWhitelistedEmail == "true";
		},

		isProductRoutedOnInternationalGateway : function() {
			var paymentJSON = getPaymentJSON();
			var productCode = paymentJSON.product;
			for (var index = 0; index < productsOnInternationalGateway.length; index++) {
				if (productCode == productsOnInternationalGateway[index]) {
					return true;
				}
			}
			return false;
		},

		disableEMI : function(selecteddd, bookingValueMoreThanMax){
			var payop = YatraPaymentUI.getPaymentOption();

			// show error message, disable the pay now button and disable the emi table
			if(selecteddd == 'dhdfc' || selecteddd == 'dkotak' || selecteddd == 'dbob' || selecteddd == 'dfdrl') {
				var bk = selecteddd;
				if(selecteddd == 'dhdfc')
					bk = 'hdfc';
				else if(selecteddd == 'dkotak')
					bk = 'kotak';
				else if(selecteddd == 'dbob')
					bk = 'bank of baroda';
				else if(selecteddd == 'dfdrl')
					bk = 'federal';

				if(bookingValueMoreThanMax){
					$("#emi_pmt_msg_WARNING").html("Only bookings with value " + (disableEMICondition[selecteddd][12].max - 1) + " and below can be paid in EMI using " + bk.toUpperCase() +"  Debit Card. Please select another bank or choose a different payment option");

				}else{
					$("#emi_pmt_msg_WARNING").html("Only bookings with value "+ disableEMICondition[selecteddd][3].min+ " and above can be paid in EMI using " + bk.toUpperCase() +" Debit Card. Please select another bank or choose a different payment option");
				}
			} else {
				if(bookingValueMoreThanMax){
					$("#emi_pmt_msg_WARNING").html("Only bookings with value " + (disableEMICondition[selecteddd][12].max - 1) + " and below can be paid in EMI using " + selecteddd.toUpperCase() +" Credit / Debit / EMI Card. Please select another bank or choose a different payment option");

				}else{
					$("#emi_pmt_msg_WARNING").html("Only bookings with value "+ disableEMICondition[selecteddd][3].min+ " and above can be paid in EMI using " + selecteddd.toUpperCase() +" Credit / Debit /EMI Card. Please select another bank or choose a different payment option");
				}
			}



			//Only bookings with value 5000 and above can be paid in EMI using Amex credit card. Please select another bank or choose a different payment option
			$("#emi_pmt_msg_WARNING").show();
			$('input[name="'+ selecteddd +'"]').attr('disabled', 'disabled');
			$( "#TncAgree" ).prop( "disabled", true );
			$( "#emi_cno_id" ).prop( "disabled", true );
			$( "#emi_mobile_number_id_"+selecteddd ).prop( "disabled", true );
			$( "#emi_cardholder_name_id" ).prop( "disabled", true );
			$( "#emi_expm_id" ).prop( "disabled", true );
			$( "#emi_expy_id" ).prop( "disabled", true );
			$( "#emi_cvv_id" ).prop( "disabled", true );
			$("." + selecteddd + "-emi-details").addClass("disabled-field");
			$("#emi_cno_id").addClass("disabled-field");
			$("#emi_mobile_number_id_"+selecteddd).addClass("disabled-field");
			$("#emi_cardholder_name_id").addClass("disabled-field");
			$("#emi_cvv_id").addClass("disabled-field");
			$("#emi_expm_id").addClass("disabled-field");
			$("#emi_expy_id").addClass("disabled-field");
			$("#expr_month").addClass("disabled-field");
			$("#expr_year").addClass("disabled-field");
			if(payop == "emi"){
				$("#payNow").addClass("emi-paynow-btn-disabled");
			}
			//clearing data in fields in case of disabled
			$("#emi_cno_id").val("");
			$("#emi_mobile_number_id_"+selecteddd).val("");
			$("#emi_cardholder_name_id").val("");
			$("#emi_expm_id").val("");
			$("#emi_expy_id").val("");
			$("#emi_cvv_id").val("");
			if(payop == "emi"){
				ProcessPayment.disablePayNow();
			}


		},
		enableEMI : function(selecteddd){
			// clear the error message, enable the paynow button and enable the emi table
			$("#emi_pmt_msg_WARNING").html("");
			$("#emi_pmt_msg_WARNING").hide();
			$('input[name="'+ selecteddd +'"]').removeAttr( "disabled" );

			$( "#TncAgree" ).prop( "disabled", false );
			$( "#emi_cno_id" ).prop( "disabled", false );
			$( "#emi_mobile_number_id_"+selecteddd).prop( "disabled", false );
			$( "#emi_cardholder_name_id" ).prop( "disabled", false );
			$( "#emi_expm_id" ).prop( "disabled", false );
			$( "#emi_expy_id" ).prop( "disabled", false );
			$( "#emi_cvv_id" ).prop( "disabled", false );
			$("." + selecteddd + "-emi-details").removeClass("disabled-field");
			$("#emi_cno_id").removeClass("disabled-field");
			$("#emi_mobile_number_id_"+selecteddd).removeClass("disabled-field");
			$("#emi_cardholder_name_id").removeClass("disabled-field");
			$("#emi_cvv_id").removeClass("disabled-field");
			$("#emi_expm_id").removeClass("disabled-field");
			$("#emi_expy_id").removeClass("disabled-field");
			$("#expr_month").removeClass("disabled-field");
			$("#expr_year").removeClass("disabled-field");
			$("#payNow").removeClass("emi-paynow-btn-disabled");

			ProcessPayment.enablePayNow();
		},
		disableEmiDetails: function(cAmt, selEmiOpt){
			let tenureList = Object.keys(disableEMICondition[selEmiOpt]);
			for(let i=0; i<tenureList.length;i++){
				let tenure = tenureList[i];
				if (cAmt < disableEMICondition[selEmiOpt][tenure].min){
					$("." + selEmiOpt + "-" + tenure).addClass("disabled-field");
					if (selEmiOpt == 'bajaj')	$("." + selEmiOpt + "-" + tenure).attr('title', 'For Bajaj EMI transactions, minimum monthly installment amount should be greater than Rs 1,500.')
					// $("#" + selEmiOpt + "-" + tenure + "-tooltip").show();
					$("#" + selEmiOpt + "-" + tenure).attr('disabled', 'disabled');
				}
			}
		},

                isIntCardTravelCheckIgnored: function() {
                    var paymentJSON = getPaymentJSON();
                    var productCode = paymentJSON.product;
                    for (var j = 0; j < int_card_travel_date_ignore_products.length; j++) {
                        if (productCode == int_card_travel_date_ignore_products[j])
                                return true;
                    }
                    return false;
                },

		doesProductAcceptIntCardTwoDaysPrior : function() {
			var paymentJSON = getPaymentJSON();
			var productCode = paymentJSON.product;
			for (var j = 0; j < int_card_two_days_allowing_products.length; j++) {
				if (productCode == int_card_two_days_allowing_products[j])
					return true;
			}
			return false;
		},

		isHoldAndPaySupportedOnProduct : function() {
			var paymentJSON = getPaymentJSON();
			var productCode = paymentJSON.product;
			for (var j = 0; j < holdAndPay_productsArr.length; j++) {
				if (productCode == holdAndPay_productsArr[j])
					return true;
			}
			return false;
		},

		showErrorMessage : function(divId, errorMsg) {
			$(divId + 'divWarningMsgBlock').css('display', 'none');
			$(divId + 'divErrMsgBlock').html(errorMsg);
			$(divId + 'divErrMsgBlock').css('display', '');
		},

		hideErrorMessage : function(divId){
			divId = '#' + divId;
			$(divId + '_divErrMsgBlock').css('display', 'none');
			$(divId + '_divWarningMsgBlock').css('display', 'none');
		},

		showInternationalWarning : function(payOption) {
			var divId = "#" + payOption + "_";

			$(divId + 'divErrMsgBlock').css('display', 'none');
			$(divId + 'divWarningMsgBlock').html(YatraPaymentUI.amexWarningMessage);
			$(divId + 'divWarningMsgBlock').css('display', '');
		},

		showBankMessage : function(payOption, errorMsg) {
			var divId = "#" + payOption + "_";
			var divClass = "." + payOption + "_";

			$(divId + 'divErrMsgBlock').css('display', 'none');
			$(divClass + 'payop_msg').css('display', 'none');
			$(divId+"bank_msg").html(errorMsg);
			$(divId+"bank_msg").css('display', '');

		},

		hideBankMessage : function(payOption) {
			var divId = "#" + payOption + "_";
			var divClass = "." + payOption + "_";

			$(divId + 'divErrMsgBlock').css('display', 'none');
			$(divId + 'bank_msg').css('display', 'none');
			$(divClass + 'payop_msg').css('display', '');
		},

		setDefaultClassForContinue : function() {

			var continueDivId = '#' + getContinueButtonId();
			continueClass = $(continueDivId).attr('class');
		},

		enablePayNow : function(payOption) {

			var divId = "#" + payOption + "_";
			$(divId + 'divErrMsgBlock').css('display', 'none');
			$(divId + 'divWarningMsgBlock').css('display', 'none');

			var continueDivId = '#' + getContinueButtonId();
			$(continueDivId).removeClass('cpmt_disable_button');
			$(continueDivId).prop("disabled", false);
			if($(continueDivId).hasClass("emi-paynow-btn-disabled"))
				$(continueDivId).removeClass("emi-paynow-btn-disabled")
		},

		disablePayNow : function(payOption, errorMsg) {

			var divId = "#" + payOption + "_";
			YatraPaymentUI.showErrorMessage(divId, errorMsg);

			var continueDivId = '#' + getContinueButtonId();

			$(continueDivId).addClass('cpmt_disable_button');
			$(continueDivId).prop("disabled", true);
		},

		showBillingAddressDiv : function(payOption, cardNo,binDetails) {

        	if(!YatraPaymentUI.ifArrayContains(intlPayOpArr, payOption)) {
        		return;
        	}

        	var ctype =  binDetails.cardType;
        	var divId = '#' + payOption + '_';
        	var isCardInternational_Id = divId + 'isCardInternational_id';
        	var addressDiv = divId + 'pmt_addressId';
        	var processTypeDiv = divId + 'process-pay-type';
                  var processTypeRadioDiv = '#' + payOption + '_process_pay_type_div';

        	if (ctype != null) {

        		//Add Product Check
        		var isProdInternational = YatraPaymentUI.isInternationalSupportedOnProduct();

        		// Amex Card Type
        		if (ctype == 'AMEX') {
        			YatraPaymentUI.enablePayNow(payOption);
        			if (isProdInternational == false) {
        				YatraPaymentUI.showInternationalWarning(payOption);
        				$(isCardInternational_Id).val('false');
        				return;
        			}

        			if (!$('#cc_AddressShowHide').is(':visible')) {
        				$('#cc_AddressShowHide input:radio[name="address_check_rdo"]:nth(0)').attr("checked", "checked");
        			}

        			$("#cc_AddressShowHide").show();
        		} else {

        			//Non Amex Card Type
        			$(divId + "AddressShowHide").hide();

        			if (ctype == 'VISA' || ctype == 'MASTER' || ctype == 'DINNERS' || ctype == 'DINERS' || ctype == 'RUPAY') {

        				var bin = parseInt(cardNo.substring(0, 6));
        				if(binDetails != null){

        					var html = "",recomended = "",finalMsg = "",res=binDetails;
        					//html += "<h3>Complete your payment using</h3>";
        					if("true" == binDetails.isMultiPayFlowEnabled && (binDetails.multiPayFlow != null) && (binDetails.multiPayFlow != '')){
        						var multiPayOpsArray = binDetails.multiPayFlow.split('|');
                                      html = YatraPaymentUI.getMultiFlowUiTemplate(binDetails.multiPayFlow,payOption,payOption);
        						$(processTypeRadioDiv).html(html);
        					    if (multiPayOpsArray.length > 1) {
        						    if (!$(processTypeDiv).is(':visible')) {
        							    $(processTypeDiv + ' input:radio[name="processTyperdo"]:nth(0)').attr("checked", "checked");
        							    YatraPaymentUI.paymentProcessType = $("input[name='processTyperdo']:checked").val();
        						    }

        						$(processTypeDiv).show();

        						// Inserting below line so as to make sure that first option is selected everytime by default.
        						$(processTypeDiv+ ' input:radio[name="processTyperdo"]:nth(0)').click();
        					    } else if (multiPayOpsArray.length == 1){
        					        //if multipayflow sequence is otp only or secure only
        					    	$(processTypeDiv + ' input:radio[name="processTyperdo"]:nth(0)').attr("checked", "checked");
        						    $(processTypeDiv).hide();
        						    YatraPaymentUI.paymentProcessType = multiPayOpsArray[0];
        					    } else{
        						    $(processTypeDiv).hide();
        						    YatraPaymentUI.paymentProcessType = "Secure";
        					    }
        					} else {
        					    $(processTypeDiv).hide();
        						YatraPaymentUI.paymentProcessType = "Secure";
        					}

        						var serverDate = res.serverDate;
        						if ("true" == res.isCardInternational) {

        							// If product does not support international payment
        							if (isProdInternational == false) {
        								var errorMsg = YatraPaymentUI.intlerrorMessage;
        								YatraPaymentUI.disablePayNow(payOption, errorMsg);
        								$(isCardInternational_Id).val('false');
        								return;
        							}

        							if (payOption == 'emi') {
        								YatraPaymentUI.disablePayNow(payOption, YatraPaymentUI.emiIntlErrorMessage);
        								$(isCardInternational_Id).val('false');
        								return;
        							} else if (payOption == 'rewards') {
        								YatraPaymentUI.disablePayNow(payOption, YatraPaymentUI.rewardsIntlErrorMessage);
        								$(isCardInternational_Id).val('false');
        								return;
        							}

        							// check for D-5 travelDate
        							var travelCheck = YatraPaymentUI.checkForTravelDate(payOption, serverDate);
        							if (travelCheck == "true") {
        								$(addressDiv).show();
        								$(isCardInternational_Id).val('true');
        							} else {
        								$(addressDiv).hide();
        								$(isCardInternational_Id).val('false');
        							}

        						} else if ("false" == res.isCardInternational) {
        							$(addressDiv).hide();
        							$(isCardInternational_Id).val('false');
        							YatraPaymentUI.enablePayNow(payOption);
        						} else if ("error" == res.isCardInternational) {
        							$(addressDiv).hide();
        							$(isCardInternational_Id).val('false');
        							YatraPaymentUI.enablePayNow(payOption);
        						}
        					} else{
        						$(addressDiv).hide();
        						$(isCardInternational_Id).val('false');
        						YatraPaymentUI.enablePayNow(payOption);
        					}
        			} else {
        				$(isCardInternational_Id).val('false');
        				$(processTypeDiv).hide();
        				YatraPaymentUI.paymentProcessType = "Secure";
        			}

        		}//else ends here
        	} else {

        		$("#cc_AddressShowHide").hide();
        		$(processTypeDiv).hide();
        		$(addressDiv).hide();
        		$(isCardInternational_Id).val('false');
        		YatraPaymentUI.enablePayNow(payOption);
        	}

        },

        getMultiFlowUiTemplate :function (multiPayFlowSequence,payOp,payOpId){
			var atmMessage = "ATM Pin";
			var otpMessage = "OTP";
			var secureMessage = "Secure Password";
			var recomended = "",finalMsg = "",html="";
			var multiPayOpsArray = multiPayFlowSequence.split('|');
            var isAtmOptionAvailable = multiPayOpsArray.indexOf('atm');

			for(var i = 0;i<multiPayOpsArray.length;i++){
			    var multiPayOptn=multiPayOpsArray[i];

			    if ((multiPayOptn != null) && (multiPayOptn == 'atm')){
					finalMsg = atmMessage + recomended;
					html +='<div class="list-b mb5 listopt_atm" id="'+payOp+'_multi-pay-atm-option-message-template_'+payOpId+'">'
					+'<label for="'+ payOp +'-Atm-Rdo_'+ payOpId +'"> <span'
					+'class="custom-Radiobox"> <input type="radio" class="cpmt_alignMid radioMid multipayRadio" value="ATM"'
					+'id="'+ payOp +'-Atm-Rdo_'+ payOpId +'" name="processTyperdo" > <span class="box"><b class="tick"></b></span>'
					+'</span>'+ finalMsg +'<span class="in-sm" id="in-sm"></span>'
					+'</label></div>'
					+"";
				}
				if ((multiPayOptn != null) && (multiPayOptn == 'secure')){
					if((multiPayOpsArray.length==2) && (isAtmOptionAvailable != -1)) {
					    //for any of atm,secure or secure,atm sequence, secure option name should be Secure/OTP
						secureMessage = "Secure/OTP";
					}
					finalMsg = secureMessage + recomended;
					html +='<div class="list-b mb5 listopt_secure" id="'+payOp+'_multi-pay-secure-option-message-template_'+ payOpId +'">'
					+'<label for="'+ payOp +'-Secure-Rdo_'+ payOpId +'"> <span'
					+'class="custom-Radiobox"> <input type="radio" class="cpmt_alignMid radioMid multipayRadio" value="Secure"'
					+'id="'+ payOp + '-Secure-Rdo_'+ payOpId +'" name="processTyperdo"> <span class="box"><b class="tick"></b></span>'
					+'</span>'+ finalMsg +'<span class="in-sm" id="in-sm"></span>'
					+'</label></div>'
					+"";
				}

				if ((multiPayOptn != null) && (multiPayOptn == 'otp')){
					finalMsg = otpMessage + recomended;
					html +='<div class="list-b mb5 listopt_otp" id="'+payOp+'_multi-pay-otp-option-message-template_'+ payOpId +'">'
					+'<label for="'+ payOp +'-Otp-Rdo_'+ payOpId +'"> <span'
					+'class="custom-Radiobox"> <input type="radio" class="cpmt_alignMid radioMid multipayRadio" value="OTP"'
					+'id="'+ payOp +'-Otp-Rdo_'+ payOpId +'" name="processTyperdo"> <span class="box"><b class="tick"></b></span>'
					+'</span>'+ finalMsg +'<span class="in-sm" id="in-sm"></span>'
					+'</label></div>'
					+" ";
				}
				recomended = "";
			}
			return html;
		},

		checkForTravelDate : function(payOption, serverDate) {
			if(YatraPaymentUI.byPassTravelDateCheck()) {
				return "true";
			}

                        if (YatraPaymentUI.isIntCardTravelCheckIgnored()) {
                            return "true";
                        }
			//Format : MM/dd/yyyy hh:mm:ss aa
			var paymentJSON = getPaymentJSON();
			var twoDaysSupport = YatraPaymentUI.doesProductAcceptIntCardTwoDaysPrior();
			var departDateVal = paymentJSON.international.travelDate;
			var departDate = '';

			if (departDateVal == null || departDateVal == '') {
				if(twoDaysSupport){
					YatraPaymentUI.disablePayNow(payOption, YatraPaymentUI.d2errorMessage);
					YatraPaymentUI.logDXErrorInSentry("InternationalCards:D-2 check failed", YatraPaymentUI.d2errorMessage);
				}
				else {
					YatraPaymentUI.disablePayNow(payOption, YatraPaymentUI.d3errorMessage);
					YatraPaymentUI.logDXErrorInSentry("InternationalCards:D-3 check failed", YatraPaymentUI.d3errorMessage);
				}
				return "false";
			} else {
				departDate = new Date(departDateVal);
			}

			var paymentDate;
			if (serverDate == null || serverDate == '')
				paymentDate = new Date();
			else
				paymentDate = new Date(serverDate);

			if(twoDaysSupport) {
				if (((departDate.getTime() - paymentDate.getTime()) / (24 * 3600 * 1000)) <= 2) {
					YatraPaymentUI.disablePayNow(payOption, YatraPaymentUI.d2errorMessage);
					YatraPaymentUI.logDXErrorInSentry("InternationalCards:D-2 check failed", YatraPaymentUI.d2errorMessage);
					return "false";
				} else {
					return "true";
				}
			}
			else {
				if (((departDate.getTime() - paymentDate.getTime()) / (24 * 3600 * 1000)) <= 3) {
					YatraPaymentUI.disablePayNow(payOption, YatraPaymentUI.d3errorMessage);
					YatraPaymentUI.logDXErrorInSentry("InternationalCards:D-3 check failed", YatraPaymentUI.d3errorMessage);
					return "false";
				} else {
					return "true";
				}
			}

			return "true";
		},

		logDXErrorInSentry : function(message, msgDisplayed) {
			YatraPaymentUI.recordEventInSentry(message, "info",
					"InternationalCards:DX-check-failed", "messageDisplayed", msgDisplayed);
		},

		interateRewardcardList : function(cardNum, len, binList) {
			for (var i = 0; i < len; i++) {
				var exp = new RegExp("^" + binList[i]);
				if (exp.test(cardNum))
					rwprefixValid = true;
			}
			return rwprefixValid;
		},

		validateRewardCardNumber : function(cardNo) {
			var rewardBinData = eval(rewardCardsBinList);
			rwprefixValid = false;

			for ( var i = 0; i < rewardBinData.length; i++) {

				var binList = rewardBinData[i][1];
				rwprefixValid = YatraPaymentUI.interateRewardcardList(cardNo, binList.length, binList);
				if (rwprefixValid) {
					rwConversion = rewardBinData[i][2];
					break;
				}
			}

			// not a valid reward card number
			if (!rwprefixValid) {
				$('#error-rwCnoValid').show();
				return false;
			}
			return true;
		},

		calRewardAmount : function(discountAMT) {
			var rewardsAmt = Math.floor(discountAMT * parseInt($("#rw_reward_points_id").val()) / 100);
			if (!isNaN(rewardsAmt)) {
				if(rewardsAmt == '0') {
					$("#rw_reward_amount_id").val('');
				}
				else {
					$("#rw_reward_amount_id").val(rewardsAmt);
				}
			}
		},

		getParamValue : function(key) {
			return $("#paymentForm input[name=" + key + "]").val();
		},

		generateEMILogos : function(emiBank) {

			var bankList = "";
			if (banksArray == "")
				return;

			emi_enabledCards = "";
				binNumber = "";
			bankList = eval(banksArray);
			$("#emiCardLogos").empty();
			$("#emiCardLogos").removeClass();
			$("#emiCardLogos").addClass("card-logos");
			for ( var i = 0; i < bankList.length; i++) {
				if (emiBank == bankList[i].bankName) {
					$("#emiCardLogos").append('<i class="PaymentSprite ' + bankList[i].logoURL + '" title="' + bankList[i].code + '" ></i> <script language="javascript"> emi_enabledCards = emi_enabledCards + "/" + "' + bankList[i].code + '";</script>	');
				}
			}

			$("#emiCardLogos").append(' <script language="javascript"> emi_enabledCards = emi_enabledCards.substring(1);</script>	');

		},

		disableNewCreditCard : function() {
			$('#cc_newCard_id').prop('checked', false);
			$('#cc_newCard_id + .box > .tick').parents('label').addClass('selChecked');
			//$("#cc_newCard").hide();
			if(showAuthDiv) {
				$('#tab_cc_qb > input[name="payop"]').val('cc');
			} else {
				$('#tab_cc_qb > input[name="payop"]').val('qb');
			}
			YatraPaymentUI.changePayNowButtonLabel(detailsJson.product);
		},

		disableNewDebitCard : function() {

			$('#dc_newCard_id').prop('checked', false);
			$('#dc_newCard_id + .box > .tick').parents('label').addClass('selChecked');
			// $("#dc_newCard").hide();
			if(showAuthDiv) {
				$('#tab_dc_qb > input[name="payop"]').val('dc');
			} else {
				$('#tab_dc_qb > input[name="payop"]').val('qb');
			}
			YatraPaymentUI.changePayNowButtonLabel(detailsJson.product);
		},
		setActiveClass: function(Elm){
			$(Elm).parent().parent().parent().addClass('active').siblings().removeClass('active')
		},
		disableQBCVVForCreditCard : function(qbElem) {
			var qbCVVElem = $(qbElem).parent().parent().parent().find('.qb_ccCVV');
			$(qbCVVElem).val('');
			$(qbCVVElem).prop("readonly", true);
			$(qbCVVElem).css('background-color' , '#DEDEDE');
			$(qbCVVElem).attr('data-validation', '');
			$(qbCVVElem).attr('data-msginfo', '');
		},

		disableCVVforGdsFlow : function(){
			var CVVElem = $('.cvvInput');
			$(CVVElem).val('');
			$(CVVElem).prop("readonly", true);
			$(CVVElem).css('background-color' , '#DEDEDE');
			$(CVVElem).attr('data-validation', '');
			$(CVVElem).attr('data-msginfo', '');
		},

		disableCVVforSkipOtpFlow : function(paymentOption){
			var divId = '#' + paymentOption + '_';
			var currentCVVElem = divId + 'cvv_id';
			var CVVElem = $(currentCVVElem);
			$(CVVElem).val('');
			$(CVVElem).prop("readonly", true);
			$(CVVElem).css('background-color' , '#DEDEDE');
			$(CVVElem).attr('data-validation', '');
			$(CVVElem).attr('data-msginfo', '');
		},

		enableCVVforSkipOtpFlow : function(paymentOption, cardType){
			var divId = '#' + paymentOption + '_';
			var currentCVVElem = divId + 'cvv_id';
			var CVVElem = $(currentCVVElem);
			$(CVVElem).val('');
			$(CVVElem).prop("readonly", false);
			$(CVVElem).css('background-color' , '#FFFFFF');
			var length = $(CVVElem).attr('maxlength');
			if (cardType == 'MAEST') {
				$(CVVElem).attr('data-validation', 'checkCvv');
				$(CVVElem).attr('data-msginfo', 'Enter a valid cvv');
			} else {
				$(CVVElem).attr('data-validation', 'required|regType[number]|lenCheck[' + length + ']');
				$(CVVElem).attr('data-msginfo', '* required|* number only|* invalid cvv length');
			}
		},

		disableQBCVVForDebitCard : function(qbElem) {
			var qbCVVElem = $(qbElem).parent().parent().parent().find('.qb_dcCVV');
			$(qbCVVElem).val('');
			$(qbCVVElem).prop("readonly", true);
			$(qbCVVElem).css('background-color' , '#DEDEDE');
			$(qbCVVElem).attr('data-validation', '');
			$(qbCVVElem).attr('data-msginfo', '');
		},

		enableQBCVVForCheckedCreditCard : function(qbCardElem) {

			var qbCardCVVElem = $(qbCardElem).parent().parent().parent().find('.qb_ccCVV');
			$(qbCardCVVElem).prop("readonly", false);
			$(qbCardCVVElem).css('background-color' , '#FFFFFF');
			$(qbCardCVVElem).focus();

			var length = $(qbCardCVVElem).attr('maxlength');
			$(qbCardCVVElem).parents('li').find('.selChecked').removeClass('selChecked');
			$(qbCardCVVElem).attr('data-validation', 'required|regType[number]|lenCheck[' + length + ']');
			$(qbCardCVVElem).attr('data-msginfo', '* required|* number only|* invalid cvv length');
			YatraPaymentUI.callOnCardEntry('QB',$(qbCardElem).attr('id'),$(qbCardElem).attr('cardtype'));
		},

		enableQBCVVForCheckedDebitCard : function(qbCardElem) {

			var cardType = $(qbCardElem).attr("cardType");
			var qbCardCVVElem = $(qbCardElem).parent().parent().parent().find('.qb_dcCVV');
			$(qbCardCVVElem).prop("readonly", false);
			$(qbCardCVVElem).css('background-color' , '#FFFFFF');
			$(qbCardCVVElem).focus();
			var length = $(qbCardCVVElem).attr('maxlength');
			$(qbCardCVVElem).parents('li').find('.selChecked').removeClass('selChecked');

			if (cardType == 'MAESTRO') {
				$(qbCardCVVElem).attr('data-validation', 'checkCvv');
				$(qbCardCVVElem).attr('data-msginfo', 'Enter a valid cvv');
			} else {
				$(qbCardCVVElem).attr('data-validation', 'required|regType[number]|lenCheck[' + length + ']');
				$(qbCardCVVElem).attr('data-msginfo', '* required|* number only|* invalid cvv length');
			}
			YatraPaymentUI.callOnCardEntry('QB',$(qbCardElem).attr('id'),$(qbCardElem).attr('cardtype'));
		},

		setElementInnerHtml : function(elemId, elemValue) {
			var elem = document.getElementById(elemId);
			if (elem != null) {
				$(elem).innerHTML = elemValue;
			}
		},

		setElementText : function(elemId, elemValue) {
			var elem = document.getElementById(elemId);
			if (elem != null) {
				$(elem).text(elemValue);
			}
		},



		getMonthlyInterest : function(amount, emiTenure, interestRate) {

			var monthlyInterest = interestRate / 12;
			if(monthlyInterest == 0) {
				var EMI = amount/emiTenure;
			}
			else {
				var R = monthlyInterest / 100;
				var partA = amount * R;
				var partB = Math.pow(1 + R, emiTenure);
				var partAB = partA * partB;
				var partC = partB - 1;
				var EMI = partAB / partC;
			}

			return Math.round(EMI * 100) / 100;
		},

		getBankInterest : function(emiInstallment, amount, emiTenure) {
			var bankInterest = (emiInstallment * emiTenure) - amount;
			return Math.round(bankInterest * 100) / 100;
		},

		generateEmiTable : function() {
			var amount = YatraEWalletPaymentUI.getCashAmount();
			if(amount == undefined || amount == null || amount == '' || amount <= 0) {
				var paymentJSON = getPaymentJSON();
				amount = paymentJSON.amount;
			}
			$("#emi-details-table tr").each(function() {
				var tenure = $(this).find(".emiTenure").html();
				if(tenure != null && tenure != '' && tenure != undefined) {
					var interestRate = $(this).find(".interestRate").html() == "Interest Free EMI" ? 0 : $(this).find(".interestRate").html();
					var emiInstallment = YatraPaymentUI.getMonthlyInterest(amount, tenure, interestRate);
					$(this).find(".monthlyEMI").html(YatraPayment.formatAmount(emiInstallment));
					var bankInterest = YatraPaymentUI.getBankInterest(emiInstallment, amount, tenure);
					$(this).find(".InterestPaid").html(YatraPayment.formatInterestAmount(bankInterest, interestRate));
					if(interestRate == 0)
						var totalAmount = amount;
					else
						var totalAmount = emiInstallment * tenure;
					totalAmount = Math.round(totalAmount * 100) / 100;
					$(this).find(".total-payable-amount").html(YatraPayment.formatAmount(totalAmount));
				}
			});

			var radioName = $("#emiBank_select option:selected").val();
			if(radioName != null && radioName != '' && radioName != undefined) {
				var selectedRadio = $('input[name="' + radioName + '"]:checked');
				YatraPaymentUI.setEMITableFooterDetails(selectedRadio);
			}
		},

	setEMITableFooterDetails : function (selectedRadio) {

			var tenure = selectedRadio.val();
			var bankCode = selectedRadio.attr("name");
			var interestPaid = $('.' + bankCode + '-' + tenure).find(".InterestPaid").html();
			var amount = YatraEWalletPaymentUI.getCashAmount();
			if(amount == undefined || amount == null || amount == '' || amount <= 0) {
				var paymentJSON = getPaymentJSON();
				amount = paymentJSON.amount;
			}
			$("#emiMsg").hide();
			$("#noCostEmiMsg").hide();


			if(interestPaid == 0){
				$(".taxmsg").hide();
				$(".taxmsg1").hide();
				var noCostEmiDivResponse = YatraPaymentUI.getNoCostEmiDiv(tenure, bankCode, amount);
				var parsedResponse, contentToShowEMIDiv;
					try{
						parsedResponse = JSON.parse(noCostEmiDivResponse);
					}catch(e){
						parsedResponse = noCostEmiDivResponse;
					}
					contentToShowEMIDiv = "<ul style='list-style-type:disc; margin-left:25px'>";
					if(parsedResponse.status && parsedResponse.status == "success" && parsedResponse.content && parsedResponse.content.length >0){
						$("#noCostEmiMsg").show();
								for(var i =0; i<parsedResponse.content.length; i++){
							contentToShowEMIDiv = contentToShowEMIDiv +"<li>"+ parsedResponse.content[i] + "</li>"
						}
								YatraPaymentUI.isNoCostEmiFlag = true;
								contentToShowEMIDiv = contentToShowEMIDiv +"</ul>";
								$("#noCostEmiMsg").html(contentToShowEMIDiv);
								$("#emi_emiTenure_id").val(tenure);

					}
					else{
						$("#emiMsg").show();
						$("#emi_emiTenure_id").val(tenure);
						$('#cpmt_emiInterest').html(interestPaid);

						$('#cpmt_tenure').html(YatraPayment.formatAmount(tenure));
						$('#cpmt_amount').html(YatraPayment.formatAmount(amount));
						YatraPaymentUI.isNoCostEmiFlag = false;
					}
				}else {
				$(".taxmsg").show();
				if(bankCode == "hdfc")
					$(".taxmsg1").show();
				else $(".taxmsg1").hide();
				$("#emiMsg").show();
				$("#emi_emiTenure_id").val(tenure);
				$('#cpmt_emiInterest').html(interestPaid);

				$('#cpmt_tenure').html(YatraPayment.formatAmount(tenure));
				$('#cpmt_amount').html(YatraPayment.formatAmount(amount));
				YatraPaymentUI.isNoCostEmiFlag = false;
			}
		},
		getNoCostEmiDiv : function(tenure, bankCode, amount){
			var res
			$.ajax({
				url : "/checkout/pay/getNoCostEmiDiv?amount="+amount+"&tenure="+tenure +"&bank="+ bankCode +"&merchantCode=yatra&productCode=dom2&super_pnr=" + detailsJson.superPnr,
				type : 'GET',
				async: false,
				timeout:"5000",
				cache : false,
				success : function(response) {
					console.log(response)
					res =  response;
				}, error : function(request, status, error) {
				console.log("error in get emi div",error )
				res = error
				}
			});
			return res;
		},
		onAmountChange : function() {
			YatraPaymentUI.generateEmiTable();
			if(exchangeRates && exchangeRates.status == "true"){
				YatraPaymentUI.changePaypalSection();
			}
			if(parseInt($("#eCashRedeemed").val()) > 0) {
				YatraEWalletPaymentUI.setRedeemUI();
			} else {
				YatraEWalletPaymentUI.setEWalletUI();
			}
		},

		onLogin : function() {
			YatraQBPaymentUI.updateQBDiv();
			YatraEWalletPaymentUI.updateEWalletHTML();
		},

		onLogout : function() {
			YatraQBPaymentUI.updateQBDiv();
			YatraEWalletPaymentUI.updateEWalletHTML();
			eCashJSON = null;
			redeemAuthJSON = null;
			reverseAuthJSON = null;
		},

		clearAddressDiv : function(qbCardElem){
			var addressDiv = $(qbCardElem).parent().parent().parent().find('#qb_pmt_addressId');
			var amexAddressDiv = $(qbCardElem).parent().parent().parent().find('#qb_AddressShowHide');
			$(addressDiv).hide();
			$(amexAddressDiv).hide();
		},

		handleAMEXQBCard : function(qbCardElem, isProdInternational,payOp,processCardId){

			var isCardInternational_Id = '#' + processCardId + '_isCardInternational_id';
			var saveQBCardAddress = '#' + processCardId + '_saveQBCardAddress_id';

			//1. Enable Pay Now for QuickBook
			YatraPaymentUI.enablePayNow(payOp);

			//2. International cards not eligible for product
			if (isProdInternational == false) {
				YatraPaymentUI.showInternationalWarning(payOp);
				$(isCardInternational_Id).val('false');
				$(saveQBCardAddress).val('false');
				return;
			}

			//3. Show Amex Address Div
			var amexAddressDiv = $(qbCardElem).parent().parent().parent().find('#qb_AddressShowHide');
			if (!$(amexAddressDiv).is(':visible')) {
				$('#qb_AddressShowHide input:radio[name="qb_address_check_rdo"]:nth(0)').attr("checked", "checked");
				$(isCardInternational_Id).val('false');
				$(saveQBCardAddress).val('false');
			}
			$(amexAddressDiv).show();
		},

		showQuickBookBillingDiv : function(addressDiv, isCardInternational_Id, payOp, saveQBCardAddress){

			//1. show Billing address div
			$(addressDiv).show();

			//2. set isCardInternational
			$(isCardInternational_Id).val('true');

			//3. enablePayNow
			YatraPaymentUI.enablePayNow(payOp);

			//4. set saveAddressCard flag
			$(saveQBCardAddress).val('true');
		},

		hideQuickBookBillingDiv : function(addressDiv, isCardInternational_Id,saveQBCardAddress){

			//1. hide Billing address div
			$(addressDiv).hide();

			//2. set isCardInternational
			$(isCardInternational_Id).val('false');

			//3. set saveAddressCard flag
			$(saveQBCardAddress).val('false');
		},

		handleDomesticQBCard : function(addressDiv, isCardInternational_Id, payOp, saveQBCardAddress){

			//1. hide Billing address div
			$(addressDiv).hide();

			//2. set isCardInternational false
			$(isCardInternational_Id).val('false');

			//3. enable PayNow
			YatraPaymentUI.enablePayNow(payOp);

			//4. set saveAddressCard flag as false
			$(saveQBCardAddress).val('false');
		},

		handleInternationalQBCard : function(qbCardElem){

			var processCardId = $(qbCardElem).attr("processCardId");
			var cardAccessType = $(qbCardElem).attr("cardAccessType");
			var isAddressSaved = $(qbCardElem).attr("isAddressSaved");
			var cardNumber = $(qbCardElem).attr("cardNumber");
			var cardType = $(qbCardElem).attr("cardType");
			var addressDiv = $(qbCardElem).parent().parent().parent().find('#qb_pmt_addressId');
			var isCardInternational_Id = '#' + processCardId + '_isCardInternational_id';
			var saveQBCardAddress = '#' + processCardId + '_saveQBCardAddress_id';
			var payOp = processCardId + '_qb';
			var isProdInternational = YatraPaymentUI.isInternationalSupportedOnProduct();

			if(cardAccessType == null || cardAccessType=="null" || cardAccessType == "" || cardAccessType == "undefined"){
				//Old cases where cardAccessType not available

				if(cardType == "AMEX"){

					YatraPaymentUI.handleAMEXQBCard(qbCardElem,isProdInternational,payOp,processCardId);

				}else if (cardType == 'VISA' || cardType == 'MASTER') {

					var bin = parseInt(cardNumber.substring(0, 6));
					var cardTypeURL = "/PaySwift/isCardInternational.htm";
					$.ajax({
						url : cardTypeURL, type : 'POST', data : "bin=" + bin, cache : false, success : function(resp) {

							var res = $.parseJSON(resp);
							var serverDate = res.serverDate;
							if ("true" == res.isCardInternational) {

								// If product does not support international payment
								if (isProdInternational == false) {

									var errorMsg = YatraPaymentUI.intlerrorMessage;
									YatraPaymentUI.disablePayNow(payOp, errorMsg);
									$(isCardInternational_Id).val('false');
									return;
								}

								// check for D-5 travelDate
								var travelCheck = YatraPaymentUI.checkForTravelDate(payOp , serverDate);
								if (travelCheck == "true") {

									YatraPaymentUI.showQuickBookBillingDiv(addressDiv, isCardInternational_Id, payOp, saveQBCardAddress);

								} else {

									YatraPaymentUI.hideQuickBookBillingDiv(addressDiv, isCardInternational_Id, saveQBCardAddress);
								}

							} else if ("false" == res.isCardInternational || "error" == res.isCardInternational) {
								YatraPaymentUI.handleDomesticQBCard(addressDiv, isCardInternational_Id, payOp, saveQBCardAddress);
							}
						}, error : function(request, status, error) {
							YatraPaymentUI.handleDomesticQBCard(addressDiv, isCardInternational_Id, payOp, saveQBCardAddress);
						}
					});

				}else {

					// Card is International or not, cannot be determined. Treat as Domestic Card
					YatraPaymentUI.handleDomesticQBCard(addressDiv, isCardInternational_Id, payOp, saveQBCardAddress);
				}

			}else{

				if("INT" == cardAccessType){

					if (cardType == 'VISA' || cardType == 'MASTER' || cardType == 'AMEX') {

						// check for D-5 travelDate
						var travelCheck = YatraPaymentUI.checkForTravelDate(payOp, null);

						if (travelCheck == "true") {

							if(isAddressSaved == "false"){
								YatraPaymentUI.showQuickBookBillingDiv(addressDiv, isCardInternational_Id, payOp, saveQBCardAddress);
							}
							else {
								YatraPaymentUI.hideQuickBookBillingDiv(addressDiv, isCardInternational_Id, saveQBCardAddress);
							}

						} else {
							YatraPaymentUI.hideQuickBookBillingDiv(addressDiv, isCardInternational_Id, saveQBCardAddress);
						}
					}
				}else if("DOM" == cardAccessType){
					YatraPaymentUI.handleDomesticQBCard(addressDiv, isCardInternational_Id, payOp, saveQBCardAddress);
				}

			}
		},
		generatePartialPaymentDiv : function() {
			PaymentMode.initialise();
		},
		checkPaymentModes : function(paymentMode) {
			PaymentMode.checkPaymentModes(paymentMode);
		},

		pushLiveTrackingPageLoadData : function() {
			try{
				var pageLoadData = {};
				pageLoadData.tenant=detailsJson.product;
				if(detailsJson.ci){
					pageLoadData.ci=detailsJson.ci;
				}
				if(detailsJson.pricingId){
					pageLoadData.pricingId=detailsJson.pricingId;
				}
				pageLoadData.ttv=detailsJson.totalAmount;
				pageLoadData.spnr=detailsJson.superPnr;
				pageLoadData.stage="payment";
				pageLoadData.previousStage="Traveller";
				pageLoadData.convFee = PaymentCommon.getConvienceFee("cc");
				pageLoadData.ttid=detailsJson.superPnr;
				pageLoadData.referrerURL=document.referrer;
				pageLoadData.screenHeight=screen.height;
				pageLoadData.screenWidth=screen.width;

				var url = "/livedata-analytics/trackLiveData";
				YatraPaymentUI.sendLiveTrackingAjax(JSON.stringify(pageLoadData),url);


			}catch(e){
				console.log(e);
			}

		},

		pushGTMData : function() {
			try{
				if(detailsJson.gtmTrackingData){
					dataLayer.push(detailsJson.gtmTrackingData);
				}

			}catch(e){
				console.log(e);
			}

		},

		sendLiveTrackingAjax : function(data,urlEndPoint) {
			try{
				var liveDataProduct;
				if(detailsJson.liveDataProduct){
					liveDataProduct = detailsJson.liveDataProduct;
				}
				else{
					liveDataProduct='payment';
				}
				$.ajax({
					type: "POST",
					url: urlEndPoint,
					data: {
						'trackingData':data,
						'product':liveDataProduct,
						'page':'payment'
					}
				});
			}catch(e){
				console.log(e);
			}
		},

		updateGSTCartUI : function(totalDiscount) {
			//Call Cart Update Code from here
			var gstAmount = 0;
			$.ajax({
				url : "/PaySwift/pricingUpdate/GST.htm",
				type : 'POST',
				async: false,
				timeout:"5000",
				data : {
					'totalDiscount' : totalDiscount, 'superPnr' : detailsJson.superPnr, 'product' : detailsJson.product, 'gstUrl' : detailsJson.gstUrl
				}, cache : false,
				success : function(response) {
					gstAmount = response;
				}, error : function(request, status, error) {
					YatraPaymentUI.recordEventInSentry("Error in receiving GST Controller Response", "info",
					"GST Error");
				}
			});
			ShoppingCart.updateGST(gstAmount);

		},

		isGSTEnabled : function(isGSTEnabled) {
			if(typeof(isGSTEnabled) != "undefined" && isGSTEnabled != undefined && isGSTEnabled != null && isGSTEnabled == "true")
				return true;
			else return false;
		},
		isTcsEnabled : function(isTcsEnabled) {
			if(typeof(isTcsEnabled) != "undefined" && isTcsEnabled != undefined && isTcsEnabled != null && isTcsEnabled == true)
				return true;
			else return false;
		},

		addEventsForQB : function(){
			// unbinding on both the elements below as these might be called two times
			$('#cc_quickOrCard').unbind();
			$('#dc_quickOrCard').unbind();
			$('#cc_quickOrCard').click(function(){
			    binNumber="000000";
				$('#qb_newCreditCard').show();
				$('label[for=cc_quickOrCard]').hide();

				// $('#payWithQB').html("Pay With QuickBook");
				// $('#cc_newCard .heading-p').show();

				$('#tab_ccQB .js-quick').slideDown('slow');
				//$('#cc_newCard').slideUp();
				$('#tab_ccQB').removeClass('border-bottom');
				$('#tab_ccQB ul li').removeClass('active');
				$('#tab_ccQB ul li:first').addClass('active')
				var firstCreditCard = $('#tab_ccQB input:radio[name="cardid"]:nth(0)');
				$(firstCreditCard).prop('checked', true);
				YatraPaymentUI.disableQBCVVForCreditCard($('input:radio[processCardId=cc]'));
				YatraPaymentUI.handleInternationalQBCard(firstCreditCard);
				YatraPaymentUI.enableQBCVVForCheckedCreditCard(firstCreditCard);
				if($(firstCreditCard).attr("cardNumberOriginal") != null && $(firstCreditCard).attr("cardNumberOriginal") != undefined) {
					var cardNumber = $(firstCreditCard).attr("cardNumberOriginal")
                    var cardNo = cardNumber.replace(/-/g,'');
                    var binDetails = YatraPaymentUI.getBinDetails(cardNo);
                    YatraPaymentUI.setSkipOtpUiForQBIfRequired(firstCreditCard, "cc",binDetails);
                    YatraPaymentUI.setMultiPayUiForQBIfRequired(firstCreditCard,"cc",binDetails);
				}
			});
			$('#dc_quickOrCard').click(function(){
			    binNumber="000000";
				$('#qb_newDebitCard').show();
				$('label[for=dc_quickOrCard]').hide();
				// $('#payWithQB').html("Pay With QuickBook");
				// $('#dc_newCard .heading-p').show();

				$('#tab_dcQB .js-quick').slideDown('slow');
				// $('#dc_newCard').slideUp();
				$('#tab_dcQB').removeClass('border-bottom');
				$('#tab_dcQB ul li').removeClass('active');
				$('#tab_dcQB ul li:first').addClass('active')
				var firstCreditCard = $('#tab_dcQB input:radio[name="cardid"]:nth(0)');
				$(firstCreditCard).prop('checked', true);
				YatraPaymentUI.disableQBCVVForCreditCard($('input:radio[processCardId=dc]'));
				YatraPaymentUI.handleInternationalQBCard(firstCreditCard);
				YatraPaymentUI.enableQBCVVForCheckedCreditCard(firstCreditCard);
				if($(firstCreditCard).attr("cardNumberOriginal") != null && $(firstCreditCard).attr("cardNumberOriginal") != undefined) {
				    var cardNumber = $(firstCreditCard).attr("cardNumberOriginal")
                    var cardNo = cardNumber.replace(/-/g,'');
                    var binDetails = YatraPaymentUI.getBinDetails(cardNo);
                	YatraPaymentUI.setSkipOtpUiForQBIfRequired(firstCreditCard, "dc",binDetails);
                	YatraPaymentUI.setMultiPayUiForQBIfRequired(firstCreditCard,"dc",binDetails);
				}


			});
		},

		initialise : function() {

			YatraPayment.initialisePage();

			//Fetch Default class of Continue butto
			YatraPaymentUI.setDefaultClassForContinue();

			// Logic for populating Expity Month Year Dropdown
			YatraPaymentUI.setExpiryYearData();

			// Add product to GA product map
			YatraPaymentUI.addProductsToGAMap();

			YatraPaymentUI.addDcPinBankVsPgData();

			YatraPaymentUI.pushGAInfoPageView();
			// Enable First Payment Option
			YatraPaymentUI.enableFirstPaymentOption();

			// Set default State for NetBanking/ATM/EMI/MW
			YatraPaymentUI.initializeBanksPaymentUI();

			// Logic to fetch Partial Payment Div
			PaymentMode.initialise();

			// Logic to generate EMI Interest Table
			YatraPaymentUI.generateEmiTable();

			//Logic to load eCash div
			YatraEWalletPaymentUI.init();

			// Logic for Payment Tab switching
			YatraPaymentUI.contentSwitcher();

			// Logic to get QuickBook cards
			YatraQBPaymentUI.updateQBDiv();

			// Logic for credit Pool
			CreditPool.initialise();

			CashPool.initialise();

			// Logic to get corporate cards
			YatraCorpCardUI.initialise();

			YatraCorpCardUI.updateCorpCardDiv();

			// Live tracking Page Load Data
			YatraPaymentUI.pushLiveTrackingPageLoadData();

			//GTM Tracking
			YatraPaymentUI.pushGTMData();

			YatraPaymentUI.displayEmiTable();

			YatraPaymentUI.logPageLoadInSentry();

			var isProductRoutedOnInternationalGateway = YatraPaymentUI.isProductRoutedOnInternationalGateway();
			YatraPaymentUI.showAddressDivForProductRoutedOnInternationalGateway(isProductRoutedOnInternationalGateway);

			YatraPaymentUI.startSessionTimer();

			LOBNotification.paymentOptionChange();

			YatraPaymentUI.addEventsForQB();

			$('#corpLoginContinueBtn').on('click',function(){
				YatraCorpCardUI.authenticateCorporateUser();
			});

			$('#dcpinBank').on('change', function(){
				var valueSelected = this.value;
				if(valueSelected == "") {
					$('#dcpinpaylaterlabel').hide();
					$('#dcpincardul').show();
					YatraPaymentUI.clearDcPinCardFields();
					YatraPaymentUI.generateDcPinLogos();
					return;
				}
				if(dcPinBankVsPgData[valueSelected] == true) {
					$('#dcpincardul').show();
					YatraPaymentUI.clearDcPinCardFields();
					YatraPaymentUI.generateDcPinLogos();
					$('#dcpinpaylaterlabel').hide();
				} else if (dcPinBankVsPgData[valueSelected] == false){
					$('#dcpincardul').hide();
					$('#dcpinpaylaterlabel').show();
				}
				$("#dcpin_bank_name").val(dcPinBankCodeVsBankName[valueSelected]);
				return;
			});

			$('.emi-tenure-radio').on('click',function(){
				YatraPaymentUI.setEMITableFooterDetails($(this));
			});

			$('#cc_cno_id, #dc_cno_id, #dcpin_cno_id, #emi_cno_id, #rw_cno_id').bind("contextmenu", function(e) {
				e.preventDefault();
			});

			$("#continue_invalid_visa").on("click",function(){
				$("#invalid_visa_discount_pop_up").hide();
				PaymentCommon.hideBlackDiv();
				ProcessPayment.payNow();
			});
			$('#spanEditStep1').on('click',function(){
				Ewallet.confirmPageChange("1");
			});

			$('#spanEditStep2').on('click',function(){
				Ewallet.confirmPageChange("2");
			});
			$("#undo_invalid_visa").on("click",function(){
				$("#invalid_visa_discount_pop_up").hide();
				PaymentCommon.hideBlackDiv();
				var paymentOption = YatraPaymentUI.getPaymentOption();
				var paymentOptionDiv = '#tab_' + paymentOption;
				YatraPaymentUI.clearPaymentDetails(paymentOptionDiv);
				YatraPaymentUI.clearValidationError();
				PaymentCommon.onAmountChange();
			});
			$("#ChangePromo").on("click",function(){
				YatraPaymentUI.adobeTrackAfterPromoFaliure("change promocode");
				PaymentCommon.hideBlackDiv();
				$("#invalid_promo_code_discount_pop_up").hide();
				var changePromoUrl = resp.changePromoUrl;
				var paymentOption = YatraPaymentUI.getPaymentOption();
				var paymentOptionDiv = '#tab_' + paymentOption;
				YatraPaymentUI.clearPaymentDetails(paymentOptionDiv);
				YatraPaymentUI.clearValidationError();
				PaymentCommon.onAmountChange();
				window.location = changePromoUrl;
			});

			$("#searchResult").on("click",function(){
				PaymentCommon.hideBlackDiv();
				$("#session_timer_pop_up").hide();
				var searchResultUrl = detailsJson.sessionTimeoutData.searchUrl;
				var paymentOption = YatraPaymentUI.getPaymentOption();
				var paymentOptionDiv = '#tab_' + paymentOption;
				YatraPaymentUI.clearPaymentDetails(paymentOptionDiv);
				YatraPaymentUI.clearValidationError();
				window.location = searchResultUrl;
			});

			$("#latestPricing").on("click",function(){
				PaymentCommon.hideBlackDiv();
				$("#session_timer_pop_up").hide();
				var latestPricingUrl = detailsJson.sessionTimeoutData.pricingUrl;
				var paymentOption = YatraPaymentUI.getPaymentOption();
				var paymentOptionDiv = '#tab_' + paymentOption;
				YatraPaymentUI.clearPaymentDetails(paymentOptionDiv);
				YatraPaymentUI.clearValidationError();
				window.location = latestPricingUrl;
			});

			$("#ChangePayOpt").on("click",function(){
				YatraPaymentUI.adobeTrackAfterPromoFaliure("change payment option");
				PaymentCommon.hideBlackDiv();
				$("#invalid_promo_code_discount_pop_up").hide();
				var paymentOption = YatraPaymentUI.getPaymentOption();
				var paymentOptionDiv = '#tab_' + paymentOption;
				YatraPaymentUI.clearPaymentDetails(paymentOptionDiv);
				YatraPaymentUI.clearValidationError();
				PaymentCommon.onAmountChange();
				if(paymentOption == 'cc'){
					// if there is qb login show checkbox for new cc always
					if($("#tab_ccQB .qbLogin").length){
						 $("label[for=cc_newCard_id]").show();
						}

					if($('#cc_newCard_id').prop("checked")){
						$('#cc_newCard_id').trigger('click');
					}
					else{
						$("#cc_quickOrCard").trigger('click');
					}
				}
				if(paymentOption == 'dc'){
					// if there is qb login show checkbox for new dc always
					if($("#tab_dcQB .qbLogin").length){
						 $("label[for=dc_newCard_id]").show();
						}

					if($('#dc_newCard_id').prop("checked")){
						$('#dc_newCard_id').trigger('click');
					}
					else{
						$("#dc_quickOrCard").trigger('click');
					}
				}
			});
			$("#ContinueWithoutPromo").on("click",function(){
				YatraPaymentUI.adobeTrackAfterPromoFaliure("continue without promocode");
				var isGSTEnabled = YatraPaymentUI.isGSTEnabled(detailsJson.isGSTEnabled);
				if(isGSTEnabled) {
					var discountAmount = 0;
					if(typeof(detailsJson.discount) != "undefined" && detailsJson.discount != undefined)
						discountAmount = ShoppingCart.json[ShoppingCart.labelIndex[detailsJson.discount.cartLabel]][detailsJson.discount.cartLabel];

					currentGSTAmount = ShoppingCart.json[ShoppingCart.labelIndex['GST']]["GST"];
					prevGSTAmount = currentGSTAmount;
					var prevTotalAmountPayable = ShoppingCart.totalPayableAmount;

					YatraPaymentUI.updateGSTCartUI(Number(YatraEWalletPaymentUI.getYLP()) + Number(Math.abs(discountAmount)));

					YatraPaymentUI.ewalletRedemptionUpdateOnGSTIncrease(prevTotalAmountPayable, prevGSTAmount, currentGSTAmount)
				}
				PaymentCommon.hideBlackDiv();
				ShoppingCart.updatePromocode(0);
				$("#invalid_promo_code_discount_pop_up").hide();
				ProcessPayment.proceedWithPromo=false;
				PaymentCommon.onAmountChange();
				if(!isGSTEnabled)
					ProcessPayment.payNow();


			});
			$("#ChangePayOptWithPartialPromo").on("click",function(){
				YatraPaymentUI.adobeTrackAfterPromoFaliure("change payment option");
				PaymentCommon.hideBlackDiv();
				$("#valid_warning_promo_code_discount_pop_up").hide();
				var paymentOption = YatraPaymentUI.getPaymentOption();
				var paymentOptionDiv = '#tab_' + paymentOption;
				YatraPaymentUI.clearPaymentDetails(paymentOptionDiv);
				YatraPaymentUI.clearValidationError();
				/*ShoppingCart.updatePromocode(-resp.promoAmount);*/
				PaymentCommon.onAmountChange();
			});
			$("#ContinueWithPartialPromo").on("click",function(){
				YatraPaymentUI.adobeTrackAfterPromoFaliure("continue without promocode'");
				PaymentCommon.hideBlackDiv();
				ShoppingCart.updatePromocode(-resp.promoAmount);
				$("#valid_warning_promo_code_discount_pop_up").hide();
				ProcessPayment.proceedWithPromo=true;
				PaymentCommon.onAmountChange();
				PaymentCommon.continueWithPayment(resp);
			});
			$("#ChangePartialPromo").on("click",function(){
				YatraPaymentUI.adobeTrackAfterPromoFaliure("change promocode");
				PaymentCommon.hideBlackDiv();
				$("#valid_warning_promo_code_discount_pop_up").hide();
				var changePromoUrl = resp.changePromoUrl;
				var paymentOption = YatraPaymentUI.getPaymentOption();
				var paymentOptionDiv = '#tab_' + paymentOption;
				YatraPaymentUI.clearPaymentDetails(paymentOptionDiv);
				YatraPaymentUI.clearValidationError();
				PaymentCommon.onAmountChange();
				window.location = changePromoUrl;
			});
			$("#ValidateFailureUrlBtn").on("click",function(){
				PaymentCommon.hideBlackDiv();
				$("#validate_error_pop_up").hide();
				var validationFailureUrl = resp.lobValidationFailureUrl;
				var paymentOption = YatraPaymentUI.getPaymentOption();
				var paymentOptionDiv = '#tab_' + paymentOption;
				YatraPaymentUI.clearPaymentDetails(paymentOptionDiv);
				YatraPaymentUI.clearValidationError();
				PaymentCommon.onAmountChange();
				window.location = validationFailureUrl;
			});
			$("#ChangeFlight").on("click",function(){
				PaymentCommon.hideBlackDiv();
				$("#pricing_error_pop_up").hide();
				var changeFlightUrl = resp.priceValidationFailureUrl;
				var paymentOption = YatraPaymentUI.getPaymentOption();
				var paymentOptionDiv = '#tab_' + paymentOption;
				YatraPaymentUI.clearPaymentDetails(paymentOptionDiv);
				YatraPaymentUI.clearValidationError();
				PaymentCommon.onAmountChange();
				window.location = changeFlightUrl;
			});
			$("#primeFailureReviewPage").on("click", function() {
	            PaymentCommon.hideBlackDiv();
	            $("#prime_error_pop_up").hide();
	            var b = resp.pricingUrl;
	            YatraPaymentUI.clearValidationError();
	            PaymentCommon.onAmountChange();
	            window.location = b
	        });
			$("#ContinueWithNewPrice").on("click",function(){
				PaymentCommon.hideBlackDiv();
				$("#pricing_error_pop_up").hide();
				PaymentCommon.onAmountChange();
				PaymentCommon.continueWithPayment(resp);
			});
			// Processing required on Card Number input
			$("input[processCardNo=true]").on('keyup', function(e) {
				if(e.keyCode == 37 || e.keyCode == 39 || e.keyCode == 8) {
					return false;
				}
				else {
					var start = this.selectionStart,
					end = this.selectionEnd;
					//1. Not allow spaces.
					YatraPaymentUI.disableSpace($(this));

					//2. Only digits allowed on cardNumber input
					YatraPaymentUI.checkNaN($(this));

					//3. Get CardNumber value
					var cardNo = "";
					cardNo = $(this).val();

					if(cardNo.length >=9 && cardNo.substr(0,9) != binNumber){

                    binNumber = cardNo.substr(0,9);

                    //4.1. Get Bin Details for Card No
                    var binDetails = YatraPaymentUI.getBinDetails(cardNo);

                    //4.2. Identify CardType$.parseJSON(resp);
                    var ctype = binDetails.cardType;

					//5. setMaestroUI
					YatraPaymentUI.setMaestroUI(ctype);

					YatraPaymentUI.setBajajUI(ctype);

					//6. Get Payment Option
					var paymentOption = YatraPaymentUI.getPaymentOption();

					//7. Get Enabled Cards for Payment Option
					var enabledCards = YatraPaymentUI.getEnabledCards(paymentOption);

					//8. Update Card Validations as per Card Type
					YatraPaymentUI.setCardValidation($(this), ctype, paymentOption, enabledCards);

					//8.1 Adding handling for EMI cardType
					YatraPaymentUI.setMaxCreditCardNumberLengthForEMI($(this), paymentOption, ctype);
					YatraPaymentUI.setMaxCVVLengthForEMI($(this), paymentOption, ctype);

					//9. Logic for fading
					YatraPaymentUI.fadeCardLogo(ctype);

					//10. Set 'ctype' parameter
					YatraPaymentUI.setCardTypeParameter(ctype);

					//11. Update cvvImage in case of Amex
					if (paymentOption == 'cc' || paymentOption == 'emi')
						YatraPaymentUI.setCvvLogo(ctype);

					if(isProductRoutedOnInternationalGateway == false) {
						//for products on int gateway, billing address div is already shown. Check div to be shown for other cases
						YatraPaymentUI.showBillingAddressDiv(paymentOption, cardNo,binDetails);
					}

					if(ctype){
						LOBNotification.paymentOptionChange(paymentOption,ctype);
					}
					// restore position from variables...
					this.setSelectionRange(start, end);
					//13. disable validation for GDS and/or SkipOtp flow
					var paymentJson = getPaymentJSON();
					if(detailsJson.isPassthrough=="true" && (paymentJson.product=="crpdom_hotel"||paymentJson.product=="crpint_hotel"||paymentJson.product=="crpinthotel"||paymentJson.product=="crpdomhotel")){
						YatraPaymentUI.disableCVVforGdsFlow();
					}
					else YatraPaymentUI.setSkipOtpUiIfRequired(ctype, paymentOption, cardNo);
					}
				}


			});

			$("input[processCardNo=true]").on('blur',function(){
				var cardNumber = $(this).val();
				if(cardNumber!='' && cardNumber!=undefined){
					var cardType = YatraPaymentUI.getCardType(cardNumber);
					if(cardType!='' && cardType!=undefined)
						YatraPaymentUI.callOnCardEntry('NEW',cardNumber,cardType);
				}
			});


			$("input[processCardNo=true]").on('blur',function(){
				var cardNumber = $(this).val();
				var topHeight = $(".cpmt_Paymentdiv").offset().top;
				PaymentCommon.handleVisaDiscountCommon(cardNumber,topHeight,"cc");
			});
			$('#cc_cvv_id').on('keyup', function() {

				YatraPaymentUI.disableSpace($(this));
				YatraPaymentUI.checkNaN($(this));
				$('#cc_cvv').val($('#cc_cvv_id').val());
			});

			$('#dc_cvv_id').on('keyup', function() {

				YatraPaymentUI.disableSpace($(this));
				YatraPaymentUI.checkNaN($(this));
				$('#dc_cvv').val($('#dc_cvv_id').val());
			});

			$('#dcpin_cvv_id').on('keyup', function() {

				YatraPaymentUI.disableSpace($(this));
				YatraPaymentUI.checkNaN($(this));
				$('#dcpin_cvv').val($('#dcpin_cvv_id').val());
			});

			$('#emi_cvv_id').on('keyup', function() {

				YatraPaymentUI.disableSpace($(this));
				YatraPaymentUI.checkNaN($(this));
			});

			$('input[name=baisd]').on('keyup', function() {
				YatraPaymentUI.checkNaN($(this));
			});

			$('input[name=bamob]').on('keyup', function() {
				YatraPaymentUI.checkNaN($(this));
			});

			// Logic for Amex Issue Check
			$('input:radio[name="address_check_rdo"]').on('change', function() {

				YatraPaymentUI.resetBillingDiv('cc');
				var amexCardType = $("input[name='address_check_rdo']:checked").val();
				var addressDiv = "#cc_pmt_addressId";
				var isCardInternational_Id = "#cc_isCardInternational_id";
				if (amexCardType == 'Domestic') {
					$(addressDiv).hide();
					$(isCardInternational_Id).val('false');
					YatraPaymentUI.enablePayNow('cc');
				} else if (amexCardType == 'International') {

					// check for D-5 travelDate
					var travelCheck = YatraPaymentUI.checkForTravelDate('cc', null);

					if (travelCheck == "true") {
						$(addressDiv).show();
						$(isCardInternational_Id).val('true');
						YatraPaymentUI.enablePayNow('cc');
					} else {
						$(addressDiv).hide();
						$(isCardInternational_Id).val('false');
					}

				}
			});

			$('input:radio[name="processTyperdo"]').on('change', function() {
				YatraPaymentUI.paymentProcessType = $("input[name='processTyperdo']:checked").val();
			});
			$('input:radio[name=".processTyperdo"]').on('change', function() {
				YatraPaymentUI.paymentProcessType = $("input[name='processTyperdo']:checked").val();
			});

			// Logic for QuickBook Amex Issue Check
			$('input:radio[name="qb_address_check_rdo"]').on('change', function() {

				YatraPaymentUI.resetBillingDiv('qb');
				var amexCardType = $("input[name='qb_address_check_rdo']:checked").val();
				var addressDiv = $(this).parent().parent().parent().parent().parent().parent().parent().find('#qb_pmt_addressId');
				var isCardInternational_Id = "#cc_isCardInternational_id";
				var saveQBCardAddress = "#cc_saveQBCardAddress_id";
				var payOp = 'cc_qb';

				if (amexCardType == 'Domestic') {
					YatraPaymentUI.handleDomesticQBCard(addressDiv, isCardInternational_Id, payOp, saveQBCardAddress);
				} else if (amexCardType == 'International') {

					// check for D-5 travelDate
					var travelCheck = YatraPaymentUI.checkForTravelDate(payOp, null);

					if (travelCheck == "true") {
						YatraPaymentUI.showQuickBookBillingDiv(addressDiv, isCardInternational_Id, payOp, saveQBCardAddress);
					} else {
						YatraPaymentUI.hideQuickBookBillingDiv(addressDiv, isCardInternational_Id, saveQBCardAddress);
					}

				}
			});

			$('input:radio[name=prBank]').click(function() {
				YatraPaymentUI.resetDropDown('#nprBank');
				$('#tab_nb > input[name="bankCode"]').val($('input[name="prBank"]:checked').val());
				$('#iframeEnabledFor-nb-bank').val($('input[name="prBank"]:checked').attr('iframeEnabled'));
				var sel = $('#tab_nb > input[name="bankCode"]').val($('input[name="prBank"]:checked').val()).attr('value');
				var uiBank = bankMessageJson[sel];
				if(uiBank!=null && uiBank!= "")
					YatraPaymentUI.showBankMessage("nb", uiBank.message);
				else YatraPaymentUI.hideBankMessage("nb");
				LOBNotification.paymentOptionChange();
			});

			$('select[name="nprBank"]').change(function(e) {
				$('input:radio[name="prBank"]').prop('checked', false);
				if ($('select[name="nprBank"]').val() == 'Select Your Bank' || $('select[name="nprBank"]').val() == '') {
					$('#tab_nb input:radio[name="prBank"]:nth(0)').attr("checked", "checked");
					$('#tab_nb > input[name="bankCode"]').val($('input[name="prBank"]:checked').val());
					$('#iframeEnabledFor-nb-bank').val($('input[name="prBank"]:checked').attr('iframeEnabled'));
					var sel = $('#tab_nb > input[name="bankCode"]').val($('input[name="prBank"]:checked').val()).attr('value');
					var uiBank = bankMessageJson[sel];
				} else {
					$('#tab_nb > input[name="bankCode"]').val($('select[name="nprBank"]').val());
					var selected = $('select[name="nprBank"]').find('option:selected');
					$('#iframeEnabledFor-nb-bank').val(selected.attr("iframeEnabled"));
					var sel = $('#tab_nb > input[name="bankCode"]').val($('select[name="nprBank"]').val()).attr('value')
					var uiBank = bankMessageJson[sel];
				}
				if(uiBank!=null && uiBank!= "")
					YatraPaymentUI.showBankMessage("nb", uiBank.message);
				else YatraPaymentUI.hideBankMessage("nb");
				LOBNotification.paymentOptionChange();
			});
			$('select[name="netBankDropDowm"]').change(function(e) {
				$('input:radio[name="prBank"]').prop('checked', false);
				if ($('select[name="netBankDropDowm"]').val() == 'Select Your Bank' || $('select[name="netBankDropDowm"]').val() == '') {
					$('#tab_nb input:radio[name="netBankDropDowm"]:nth(0)').attr("checked", "checked");
					$('#tab_nb > input[name="bankCode"]').val($('input[name="netBankDropDowm"]:checked').val());
					$('#iframeEnabledFor-nb-bank').val($('input[name="netBankDropDowm"]:checked').attr('iframeEnabled'));
					var sel = $('#tab_nb > input[name="bankCode"]').val($('input[name="netBankDropDowm"]:checked').val()).attr('value');
					var uiMessage = bankMessageJson[sel].message;
				} else {
					$('#tab_nb > input[name="bankCode"]').val($('select[name="netBankDropDowm"]').val());
					var selected = $('select[name="netBankDropDowm"]').find('option:selected');
					$('#iframeEnabledFor-nb-bank').val(selected.attr("iframeEnabled"));
					var sel = $('#tab_nb > input[name="bankCode"]').val($('select[name="netBankDropDowm"]').val()).attr('value')
					var uiBank = bankMessageJson[sel];
				}
				if(uiBank!=null && uiBank!= "")
					YatraPaymentUI.showBankMessage("nb", uiBank.message);
				else YatraPaymentUI.hideBankMessage("nb");
				LOBNotification.paymentOptionChange();
			});
			$('input:radio[name=atmBank]').click(function() {
				$('#tab_atm > input[name="bankCode"]').val($('input[name="atmBank"]:checked').val());
				LOBNotification.paymentOptionChange();
			});

			$('input:radio[name=walletService]').click(function() {
				$('#tab_mw > input[name="bankCode"]').val($('input[name="walletService"]:checked').val());
				var sel = $('#tab_mw > input[name="bankCode"]').val($('input[name="walletService"]:checked').val()).attr('value');
				var uiBank = bankMessageJson[sel];
				if(uiBank!=null && uiBank!= "")
					YatraPaymentUI.showBankMessage("mw", uiBank.message);
				else YatraPaymentUI.hideBankMessage("mw");
				LOBNotification.paymentOptionChange();
			});

			$('input:radio[name=payLaterService]').click(function() {
				$('#tab_payltr > input[name="bankCode"]').val($('input[name="payLaterService"]:checked').val());
				var sel = $('#tab_payltr > input[name="bankCode"]').val($('input[name="payLaterService"]:checked').val()).attr('value');
				var uiBank = bankMessageJson[sel];
				if(uiBank!=null && uiBank!= "")
					YatraPaymentUI.showBankMessage("payltr", uiBank.message);
				else YatraPaymentUI.hideBankMessage("payltr");
				LOBNotification.paymentOptionChange();
			});


			$('select[name=emiBank_select]').change(function() {
				debugger

				YatraPaymentUI.resetEMI();
				YatraPaymentUI.generateEMILogos($(this).val());

				if($(this).val()=="bajaj"){
					$("#emiExpMonth").hide();
					$("#emiExpYear").hide();
					$("#emiCvv").hide();
					$("#tncEMI").prop("href", "https://www.yatra.com/online/tnc-bajaj-finance")
				}else{
					$("#emiExpMonth").show();
					$("#emiExpYear").show();
					$("#emiCvv").show();
					$("#tncEMI").prop("href", "https://www.yatra.com/fresco/online/journal-article-emi-ax")
				}
				
				var minVal = 3;
				var maxVal = 12;
				if(disableEMICondition[$(this).val()] && disableEMICondition[$(this).val()][2])
				{
					minVal = 2;
				}
				if(disableEMICondition[$(this).val()] && disableEMICondition[$(this).val()][18])
				{
					maxVal = 18;
				}

				if(disableEMICondition[$(this).val()] && !(disableEMICondition[$(this).val()][minVal].min <= ShoppingCart.getTotalCartAmount() &&  disableEMICondition[$(this).val()][maxVal].max > ShoppingCart.getTotalCartAmount()) ){
					// show error message, disable the pay now button and disable the emi table
					YatraPaymentUI.disableEMI($(this).val(), disableEMICondition[$(this).val()][maxVal].max <= ShoppingCart.getTotalCartAmount()?true:false)
				}else{
					// clear the error message, enable the paynow button and enable the emi table
					YatraPaymentUI.enableEMI($(this).val());
					//disable specific tenure details in emi table
					let cAmt = ShoppingCart.getTotalCartAmount();
					let selEmiOpt = $(this).val();
					if (cAmt && selEmiOpt) {
						YatraPaymentUI.disableEmiDetails(cAmt, selEmiOpt);
					}
				}
				$("#emi_cardholder_name_id").val("");
			});

			$("#cc_saveQBCard").click(function() {
				if ($("#cc_saveQBCard").is(":checked"))
					$("#cc_saveQuickBookCard_id").val("true");
				else
					$("#cc_saveQuickBookCard_id").val("false");

			});

			$("#dc_saveQBCard").click(function() {
				if ($("#dc_saveQBCard").is(":checked"))
					$("#dc_saveQuickBookCard_id").val("true");
				else
					$("#dc_saveQuickBookCard_id").val("false");
			});

			$('select').change(function() {
				if ($(this).attr("paramId") != undefined) {
					$('#' + $(this).attr("paramId")).val($(this).val());
				}
			});

			$('#cc_bcountry').change(function(e) {
				$('#cc_bcountry_name').val($("#cc_bcountry option:selected").text());
			});

			$('#dc_bcountry').change(function(e) {
				$('#dc_bcountry_name').val($("#cc_bcountry option:selected").text());
			});

			$('#rw_cno_id').on('focus', function(e) {
				$('#error-rwCnoRequired').hide();
			});
			$('#other_payop').click(function() {
				var payOption = YatraPaymentUI.getPaymentOption();
				YatraPaymentUI.enablePayNow(payOption);
				YatraPaymentUI.notifyPayopToLOB();
				if(payOption !== "gv")
				{
					YatraPaymentUI.changePayNowButtonLabel(detailsJson.product);
				}
			});
			$('#other_payop').on('change',function(){
				var paymentOption = $('#other_payop option:selected').val();
				if(paymentOption == 'cc'){
					// if there is qb login show checkbox for new cc always
					if($("#tab_ccQB .qbLogin").length){
						 $("label[for=cc_newCard_id]").show();
						}
					if($('#cc_newCard_id').prop("checked")){
						$('#cc_newCard_id').trigger('click');
					}
					else{
						$("#cc_quickOrCard").trigger('click');
					}
				}
				if(paymentOption == 'dc'){
						// if there is qb login show checkbox for new dc always

					if($("#tab_dcQB .qbLogin").length){
						 $("label[for=dc_newCard_id]").show();
						}
					if($('#dc_newCard_id').prop("checked")){
						$('#dc_newCard_id').trigger('click');
					}
					else{
						$("#dc_quickOrCard").trigger('click');
					}
				}
			});
			$('#rw_cno_id').on('keyup', function(e) {

				var cno = $('#rw_cno_id').val();
				if (cno.length >= 8) {
					YatraPaymentUI.validateRewardCardNumber(cno);
				} else if (cno.length < 8) {
					$('#error-rwCnoValid').hide();
					rwConversion = "";
					$('#rw_reward_points_id').val('');
					$('#rw_reward_amount_id').val('');
				}

			});

			$("#rw_cno_id").bind({
				paste : function(){
					$('#rw_reward_points_id').val('');
					$('#rw_reward_amount_id').val('');
				}
			});

			$('#rw_reward_points_id').on('keyup', function(e) {

				YatraPaymentUI.checkNaN($(this));
				if ($('#rw_cno_id').val() == '' || $('#rw_cno_id').val().length < 8) {
					$('#error-rwCnoRequired').show();
					$($(this)).val('');
				} else if ($('#rw_reward_points_id').val() == null || $('#rw_reward_points_id').val() == '') {
					$('#rw_reward_amount_id').val('');
				} else {
					YatraPaymentUI.calRewardAmount(rwConversion);
				}
			});

			$('#cc_newCard_id').click(function() {
				if($('#qb_newCreditCard').is(':visible')){
					$('#tab_ccQB').addClass('border-bottom');
				}
				$('#payWithQB').html("Pay with Credit Card");
				if($("#payWithQB").is(':visible')){
					$('#cc_newCard .heading-p').hide();
				}
				$('#qb_newCreditCard').hide();
				$('label[for=cc_quickOrCard]').show();
				$('#tab_ccQB .js-quick').slideUp('slow');
				$('#tab_ccQB .js-quick li').removeClass('active');
				$('#tab_ccQB > input:radio[name=cardid]').prop('checked', false);
				$('#tab_ccQB .custom-Radiobox > input:checked + .box > .tick').parents('label').addClass('selChecked');
				$("#cc_newCard").show();
				$('#tab_cc_qb > input[name="payop"]').val('cc');
				$('#qb_newCreditCard').removeClass('selChecked');
				YatraPaymentUI.disableQBCVVForCreditCard($('input:radio[processCardId=cc]:checked'));
				YatraPaymentUI.disableMultiFlowUiForCreditCard($('input:radio[processCardId=cc]:checked'));
				YatraPaymentUI.clearAddressDiv($('input:radio[processCardId=cc]'));
				YatraPaymentUI.hideErrorMessage('cc_qb');
				$('#qb_password_cc').val("");

				YatraPaymentUI.resetCreditCard();
				YatraPaymentUI.notifyPayopToLOB();
				YatraPaymentUI.pushGAInfoForQuickBook("Use New Card");
				YatraPaymentUI.changePayNowButtonLabel(detailsJson.product);
				LOBNotification.paymentOptionChange();
				//hotel GDS flow
				try{
					var paymentJson = getPaymentJSON();
					if(detailsJson.isPassthrough=="true" && (paymentJson.product=="crpdom_hotel"||paymentJson.product=="crpint_hotel"||paymentJson.product=="crpinthotel"||paymentJson.product=="crpdomhotel")){
						YatraPaymentUI.disableCVVforGdsFlow();
					}
				}
				catch(e){
					console.log(e);
				}
			});

			$('#dc_newCard_id').click(function() {
				if($('#qb_newDebitCard').is(':visible')){
					$('#tab_dcQB').addClass('border-bottom');
				}
				$('#qb_newDebitCard').hide();
				$('label[for=dc_quickOrCard]').show();

				$('#payWithQB').html("Pay with Debit Card");
				if($("#payWithQB").is(':visible')){
					$('#dc_newCard .heading-p').hide();
				}


				$('#tab_dcQB .js-quick').slideUp('slow');
				$('#tab_dcQB .js-quick li').removeClass('active');
				$('#tab_dcQB > input:radio[name=cardid]').prop('checked', false);
				$('#tab_dcQB .custom-Radiobox > input:checked + .box > .tick').parents('label').addClass('selChecked');
				$("#dc_newCard").show();
				$('#tab_dc_qb > input[name="payop"]').val('dc');
				$('#qb_newDebitCard').removeClass('selChecked');
				YatraPaymentUI.disableQBCVVForDebitCard($('input:radio[processCardId=dc]:checked'));
				YatraPaymentUI.disableMultiFlowUiForDebitCard($('input:radio[processCardId=dc]:checked'));
				YatraPaymentUI.clearAddressDiv($('input:radio[processCardId=dc]'));
				YatraPaymentUI.hideErrorMessage('dc_qb');
				$('#qb_password_dc').val("");

				YatraPaymentUI.resetDebitCard();
				YatraPaymentUI.notifyPayopToLOB();
				YatraPaymentUI.pushGAInfoForQuickBook("Use New Card");
				YatraPaymentUI.changePayNowButtonLabel(detailsJson.product);
				LOBNotification.paymentOptionChange();
				//hotel GDS flow
				try{
					var paymentJson = getPaymentJSON();
					if(detailsJson.isPassthrough=="true" && (paymentJson.product=="crpdom_hotel"||paymentJson.product=="crpint_hotel"||paymentJson.product=="crpinthotel"||paymentJson.product=="crpdomhotel")){
						YatraPaymentUI.disableCVVforGdsFlow();
					}
				}
				catch(e){
					console.log(e);
				}
			});

			$('#tab_ccQB').on('click', '.selChecked', function() {
				$(this).removeClass('selChecked');
			});

			$('#tab_dcQB').on('click', '.selChecked', function() {
				$(this).removeClass('selChecked');
			});

			$(".qb_ccCVV").on('click',function() {
				var qb_ccid=$(this).attr("id");
				var data=$("#"+qb_ccid).parent().parent().attr("for");
				$("input[id="+ data +"]").prop("checked","true")
			    $("input[name=cardid]:checked").parents('li').addClass('active').siblings().removeClass('active');
				YatraPaymentUI.disableNewCreditCard();
				YatraPaymentUI.disableQBCVVForCreditCard($('input:radio[processCardId=cc]'));
				YatraPaymentUI.disableMultiFlowUiForCreditCard($('input:radio[processCardId=cc]'));
				YatraPaymentUI.setActiveClass($('input:radio[processCardId=cc]:checked'));
				YatraPaymentUI.enableQBCVVForCheckedCreditCard($('input:radio[processCardId=cc]:checked'));
				YatraPaymentUI.clearAddressDiv($('input:radio[processCardId=cc]'));
				YatraPaymentUI.handleInternationalQBCard($('input:radio[processCardId=cc]:checked'));
				YatraPaymentUI.clearValidationError();
				YatraPaymentUI.hideErrorMessage('cc');
				YatraPaymentUI.notifyPayopToLOB();
				var cardId = $(this).val();
				var topHeight = $(".cpmt_Paymentdiv").offset().top;
				PaymentCommon.handleVisaDiscountCommon(cardId,topHeight+35,"qb");

				LOBNotification.paymentOptionChange();
				var selectedCardId = $('input:radio[processCardId=cc]:checked').attr("id");

                $("#qb_pmt_MultiFlowId_" + selectedCardId).show()
				//hotel GDS flow
				try{
					var paymentJson = getPaymentJSON();
					if(detailsJson.isPassthrough=="true" && (paymentJson.product=="crpdom_hotel"||paymentJson.product=="crpint_hotel"||paymentJson.product=="crpinthotel"||paymentJson.product=="crpdomhotel")){
						YatraPaymentUI.disableCVVforGdsFlow();
					}
					else {
					    if($('input:radio[processCardId=cc]:checked').attr("cardNumberOriginal") != null && $('input:radio[processCardId=cc]:checked').attr("cardNumberOriginal") != undefined) {
					        var cardNo = $('input:radio[processCardId=cc]:checked').attr("cardNumberOriginal").replace(/-/g,'');
                            var binDetails = YatraPaymentUI.getBinDetails(cardNo);
                            YatraPaymentUI.setSkipOtpUiForQBIfRequired($('input:radio[processCardId=cc]:checked'), "cc",binDetails);
                            YatraPaymentUI.setMultiPayUiForQBIfRequired($('input:radio[processCardId=cc]:checked'), "cc",binDetails);
					    }
                    }
				}
				catch(e){
					console.log(e);
				}

			});

			$("#tab_ccQB").on('click', "input:radio[processCardId=cc]", function() {
			    $("input[name=cardid]:checked").parents('li').addClass('active').siblings().removeClass('active');
				YatraPaymentUI.disableNewCreditCard();
				YatraPaymentUI.disableQBCVVForCreditCard($('input:radio[processCardId=cc]'));
				YatraPaymentUI.disableMultiFlowUiForCreditCard($('input:radio[processCardId=cc]'));
				YatraPaymentUI.setActiveClass($('input:radio[processCardId=cc]:checked'));
				YatraPaymentUI.enableQBCVVForCheckedCreditCard($('input:radio[processCardId=cc]:checked'));
				YatraPaymentUI.clearAddressDiv($('input:radio[processCardId=cc]'));
				YatraPaymentUI.handleInternationalQBCard($('input:radio[processCardId=cc]:checked'));
				YatraPaymentUI.clearValidationError();
				YatraPaymentUI.hideErrorMessage('cc');
				YatraPaymentUI.notifyPayopToLOB();
				var cardId = $(this).val();
				var topHeight = $(".cpmt_Paymentdiv").offset().top;
				PaymentCommon.handleVisaDiscountCommon(cardId,topHeight+35,"qb");

				LOBNotification.paymentOptionChange();
				var selectedCardId = $('input:radio[processCardId=cc]:checked').attr("id");

                $("#qb_pmt_MultiFlowId_" + selectedCardId).show()
				//hotel GDS flow
				try{
					var paymentJson = getPaymentJSON();
					if(detailsJson.isPassthrough=="true" && (paymentJson.product=="crpdom_hotel"||paymentJson.product=="crpint_hotel"||paymentJson.product=="crpinthotel"||paymentJson.product=="crpdomhotel")){
						YatraPaymentUI.disableCVVforGdsFlow();
					}
					else {
					    if($('input:radio[processCardId=cc]:checked').attr("cardNumberOriginal") != null && $('input:radio[processCardId=cc]:checked').attr("cardNumberOriginal") != undefined) {
					        var cardNo = $('input:radio[processCardId=cc]:checked').attr("cardNumberOriginal").replace(/-/g,'');
                            var binDetails = YatraPaymentUI.getBinDetails(cardNo);
                            YatraPaymentUI.setSkipOtpUiForQBIfRequired($('input:radio[processCardId=cc]:checked'), "cc",binDetails);
                            YatraPaymentUI.setMultiPayUiForQBIfRequired($('input:radio[processCardId=cc]:checked'), "cc",binDetails);
					    }
                    }
				}
				catch(e){
					console.log(e);
				}

			});

			$(".qb_dcCVV").on('click',function() {
				var qb_dcid=$(this).attr("id");
				var data=$("#"+qb_dcid).parent().parent().attr("for");
				$("input[id="+ data +"]").prop("checked","true")
			    $("input[name=cardid]:checked").parents('li').addClass('active').siblings().removeClass('active');
				YatraPaymentUI.disableNewDebitCard();
				YatraPaymentUI.disableQBCVVForDebitCard($('input:radio[processCardId=dc]'));
				YatraPaymentUI.disableMultiFlowUiForDebitCard($('input:radio[processCardId=dc]'));
				YatraPaymentUI.enableQBCVVForCheckedDebitCard($('input:radio[processCardId=dc]:checked'));
				YatraPaymentUI.clearAddressDiv($('input:radio[processCardId=dc]'));
				YatraPaymentUI.handleInternationalQBCard($('input:radio[processCardId=dc]:checked'));
				YatraPaymentUI.clearValidationError();
				YatraPaymentUI.hideErrorMessage('dc');
				YatraPaymentUI.notifyPayopToLOB();
				var cardId = $(this).val();
				var topHeight = $(".cpmt_Paymentdiv").offset().top;
				PaymentCommon.handleVisaDiscountCommon(cardId,topHeight+35,"qb");
				LOBNotification.paymentOptionChange();

				var selectedCardId = $('input:radio[processCardId=dc]:checked').attr("id");

                $("#qb_pmt_MultiFlowId_" + selectedCardId).show()
				//hotel GDS flow
				try{
					if(detailsJson.isPassthrough=="true" && (paymentJson.product=="crpdom_hotel"||paymentJson.product=="crpint_hotel"||paymentJson.product=="crpinthotel"||paymentJson.product=="crpdomhotel")){
						YatraPaymentUI.disableCVVforGdsFlow();
					}
					else {
					    if($('input:radio[processCardId=dc]:checked').attr("cardNumberOriginal") != null && $('input:radio[processCardId=dc]:checked').attr("cardNumberOriginal") != undefined) {
					        var cardNo = $('input:radio[processCardId=dc]:checked').attr("cardNumberOriginal").replace(/-/g,'');
                            var binDetails = YatraPaymentUI.getBinDetails(cardNo);
                            YatraPaymentUI.setSkipOtpUiForQBIfRequired($('input:radio[processCardId=dc]:checked'), "dc",binDetails);
                            YatraPaymentUI.setMultiPayUiForQBIfRequired($('input:radio[processCardId=dc]:checked'), "dc",binDetails);
					    }
                    }
				}
				catch(e){
					console.log(e);
				}

			});

			$("#tab_dcQB").on('click', "input:radio[processCardId=dc]", function() {
			    $("input[name=cardid]:checked").parents('li').addClass('active').siblings().removeClass('active');
				YatraPaymentUI.disableNewDebitCard();
				YatraPaymentUI.disableQBCVVForDebitCard($('input:radio[processCardId=dc]'));
				YatraPaymentUI.disableMultiFlowUiForDebitCard($('input:radio[processCardId=dc]'));
				YatraPaymentUI.enableQBCVVForCheckedDebitCard($('input:radio[processCardId=dc]:checked'));
				YatraPaymentUI.clearAddressDiv($('input:radio[processCardId=dc]'));
				YatraPaymentUI.handleInternationalQBCard($('input:radio[processCardId=dc]:checked'));
				YatraPaymentUI.clearValidationError();
				YatraPaymentUI.hideErrorMessage('dc');
				YatraPaymentUI.notifyPayopToLOB();
				var cardId = $(this).val();
				var topHeight = $(".cpmt_Paymentdiv").offset().top;
				PaymentCommon.handleVisaDiscountCommon(cardId,topHeight+35,"qb");
				LOBNotification.paymentOptionChange();

				var selectedCardId = $('input:radio[processCardId=dc]:checked').attr("id");

                $("#qb_pmt_MultiFlowId_" + selectedCardId).show()
				//hotel GDS flow
				try{
					if(detailsJson.isPassthrough=="true" && (paymentJson.product=="crpdom_hotel"||paymentJson.product=="crpint_hotel"||paymentJson.product=="crpinthotel"||paymentJson.product=="crpdomhotel")){
						YatraPaymentUI.disableCVVforGdsFlow();
					}
					else {
					    if($('input:radio[processCardId=dc]:checked').attr("cardNumberOriginal") != null && $('input:radio[processCardId=dc]:checked').attr("cardNumberOriginal") != undefined) {
					        var cardNo = $('input:radio[processCardId=dc]:checked').attr("cardNumberOriginal").replace(/-/g,'');
                            var binDetails = YatraPaymentUI.getBinDetails(cardNo);
                            YatraPaymentUI.setSkipOtpUiForQBIfRequired($('input:radio[processCardId=dc]:checked'), "dc",binDetails);
                            YatraPaymentUI.setMultiPayUiForQBIfRequired($('input:radio[processCardId=dc]:checked'), "dc",binDetails);
					    }
                    }
				}
				catch(e){
					console.log(e);
				}

			});

			$('input:radio[processCardId=corpCard]').on('click', function() {
				YatraCorpCardUI.handleCorpUI($(this));
				YatraPaymentUI.clearValidationError();
				YatraPaymentUI.hideErrorMessage('corpCard');
				YatraPaymentUI.notifyPayopToLOB();
				LOBNotification.paymentOptionChange();
				$(this).parents("li").addClass("active").siblings().removeClass("active");
			});

			$("#tab_ccQB").on('keyup', "input[processCVV=ccCVV]", function() {
				YatraPaymentUI.checkNaN($(this));
				$('#cc_cvv').val($(this).val());
			});

			$("#tab_dcQB").on('keyup', "input[processCVV=dcCVV]", function() {
				YatraPaymentUI.checkNaN($(this));
				$('#dc_cvv').val($(this).val());
			});

			$('input[processCVV=corpCardCVV]').on('keyup', function() {
				YatraPaymentUI.checkNaN($(this));
				$('#corpCard_cvv_id').val($(this).val());
			});

			$("input[processQBPassword=true]").on('click', function() {
				YatraPaymentUI.disableNewCreditCard();
				YatraPaymentUI.disableNewDebitCard();
				// YatraPaymentUI.notifyPayopToLOB();
			});

			$("#rewards_pay_now").on('click', function() {
				if(typeof(proceedPayNow) == "function") {
					proceedPayNow();
				}
			});

			$("#rewards_enter_points").on('click', function() {
				$('#popoverWindowOverlay-rewards').hide();
				$('#popoverWindow-rewards').hide();
				$("#rw_reward_points_id").focus();
			});

			$("#citiEmiPopoupProceed").on('click', function() {
				if($("#citiEmiPopoupChecked").is(":checked")){
					isCitiEmiPopupTncValidated = true;
					var continueDivId = '#' + getContinueButtonId();
					$(continueDivId).click();
				}
			});
			$("#citiEmiPopoupCancel").on('click', function() {
				$('#citi-emi-popup-modal').hide();
				$('#citi-emi-popup').hide();
				isCitiEmiPopupTncValidated = false;
			});

			$('#itineraryDetailHeader .totalPayInf h2').on('click',function(){
				$('#containerScroll').slideToggle('slow');
				$(this).parents('#itineraryDetailHeader').toggleClass('hide-buk-sum');
			});
		},

		getPaymentOptionTab : function() {
			return $("#tab_" + YatraPaymentUI.getPaymentOption()).clone();
		},

		validateRewardPoints : function() {
			var validateRewardPoints = true;
			var payop = YatraPaymentUI.getPaymentOption();
			if(payop == 'rewards') {
				if (!YatraPaymentUI.validateRewardCardNumber($('#rw_cno_id').val())) {
					validateRewardPoints = false;
				} else if (typeof(proceedPayNow) == "function" && $("#rw_reward_points_id").val() == "") {
					$('#popoverWindowOverlay-rewards').show();
					$('#popoverWindow-rewards').show();
					validateRewardPoints = false;
				}
			}
			return validateRewardPoints;
		},
		validateCitiEmiTnc : function(){
			if(!YatraPaymentUI.isPayopCitiEmI()){
				return true;
			}
			if(isCitiEmiPopupTncValidated){
				return true;
			}
			$('#citi-emi-popup-modal').show();
			$('#citi-emi-popup').show();
		},

		isPayopCitiEmI : function(){
			if (YatraPaymentUI.getPaymentOption() == 'emi' && $("#emiBank_select option:selected").val()=="citi"){
				return true;
			}
			return false;
		},

		eligibleForIframePayment : function() {
			var eligibleForIframePayment = false;
			var payOp = YatraPayment.getPayOp();

			var iframeEnableForPayOp = $('#cpmt_tabmenu > li.active > a').attr('iframeEnabled');
			if (iframeEnableForPayOp == undefined) {
				iframeEnableForPayOp = $('#cpmt_tabmenu > li> ul> li.active > a').attr('iframeEnabled');
			}

			if(payOp == 'nb' && iframeEnableForPayOp == 'true') {
				if($('#iframeEnabledFor-nb-bank').val() == 'true') {
					eligibleForIframePayment = true;
				}
			}

			return eligibleForIframePayment;
		},

		processPendingPayment : function() {
			var isMoneyDeducted = confirm("Please confirm if your money is deducted ?");
			if(isMoneyDeducted) {
				YatraPaymentUI.queryPaymentPortal();
			}
		},

		queryPaymentPortal : function() {
			var paymentJSON = getPaymentJSON();
			var ttid = paymentJSON.superPNR;
			$.ajax({
				url : "/checkout/pay/query.htm?ttid=" + ttid + "&mode=qtxns", cache : false,
				success : function(response) {
					console.log(response);
				}, error : function(request, status, error) {
				}
			});
		},
		callOnCardEntry:function(action,cardNumber,cardType){
			if(typeof onCardEntry == 'function')
				onCardEntry(action,cardNumber,cardType);
		},

		bankDownMesage: function() {
			var fecthMessageURL = "/PaySwift/getBankDownMessage.htm";

			$.ajax({

				url: fecthMessageURL,
				timeout: (5 * 1000),
				type: 'POST',
				async: false,
				data: {
				},
				cache: false,

				success : function(response) {
					bankMessageJson = JSON.parse(response);
				},

				error: function (request, status, error) {
					alert("Error while getting quickbook cards.");
				}
			});
		},

		getPayAtHotelAmount : function(payAtHotel){
			if(!payAtHotel) return 0;
			return parseFloat(payAtHotel);
		},

		showHotelMessage : function(product, payAtHotel) {
			if(("HOTEL_IN" == product || "tgdomhotel" == product) && YatraPaymentUI.getPayAtHotelAmount(payAtHotel) > 0) {
				if("HOTEL_IN" == product)
					$('#pay-at-hotel-msg-box_id').show();
				else $('#pay-at-tg-msg-box_id').show();
				$('.totalPrice .you-pay-txt').text('Pay Now');
				$('#ecash_container').hide();
			}
		},

		showFareBreakUpMessage : function(message) {
			if(!message) {
				$('#farebreakup_msg_id').hide();
			}
			$('#farebreakup_subMsg_id').text(message);
		},

		showYouPayMessage : function(message) {
			if(!message) {
				$('#youPay_msg_id').hide();
			}
			$('#youPay_subMsg_id').text(message);
		},

		changePayNowButtonLabel:function(_product){
			$('#payNow').show();
			if("INT_HOTEL_BKG" == _product) {
				$('#payNow').val('Confirm Booking');
				$('.contiue-section .mobilePay').addClass('hideForHotel');
				$('body').addClass('pah-hack-only');
				$('#tcStrip_id').text("Confirm Booking");
				var depositRequired = detailsJson.hotelDepositRequired;
				if(typeof(depositRequired) != "undefined" && depositRequired != undefined && depositRequired != null && depositRequired == "true")
					$('.totalPrice .you-pay-txt').text('Pay Later');
				else
					$('.totalPrice .you-pay-txt').text('Pay at Hotel');
			}
		},

		showStaticMessage : function(detailsJson) {
			if(detailsJson.headerMessages){
				if(detailsJson.payLaterFlow && detailsJson.payLaterFlow == "true" ){
				$('#payLaterHeaderMessages_id').show();
				$('.totalPrice .you-pay-txt').text('Total Booking Amount');
				$(".totalPricenow").show()
				$(".totalPayInf").css("margin-top", "-35px")
				}
				else
					$('#headerMessage_id').show();
			}
			else{
				$('#payLaterHeaderMessages_id').hide();
				$('#headerMessage_id').hide();
				}
		},

		addAdobeProduct : function(){
				adobeProductMap["HOTEL_IN"] = "dom|domestic|hotel|web";
				adobeProductMap["HOLIDAY_IN"] = "dom|domestic|holiday|web";
				adobeProductMap["HOLIDAYS_OB"] = "dom|domestic|holiday|web";
				adobeProductMap["dom2"] = "dom|domestic|flight|web";
				adobeProductMap["int2"] = "int|international|flight|web";
				adobeProductMap["bus"] = "dom|domestic|bus|web";
				adobeProductMap["HOLIDAY_WAP"] = "dom|domestic|holiday|wap";
				adobeProductMap["trainngetb2c"] = "dom|domestic|train|web";
				adobeProductMap["trainngetwap"] = "dom|domestic|train|wap";
				adobeProductMap["INT_HOTEL_BKG"] = "int|international|hotel|web";
				adobeProductMap["homestays"] = "dom|domestic|homestay|web";
				adobeProductMap["busdesktop"] = "dom|domestic|bus|web";
				adobeProductMap["buswap"] = "dom|domestic|bus|wap|web";
				adobeProductMap["HOLIDAYS_MP_ONLINE"] = "dom|domestic|holiday|web";
				adobeProductMap["activities"] = "dom|domestic|activity|web";
				adobeProductMap["metadom"] = "dom|domestic|flight|web";
				adobeProductMap["metaint"] = "int|international|flight|web";
				adobeProductMap["HOLIDAYS_SAPG"] = "dom|domestic|holiday|web";
				adobeProductMap["HOLIDAYS_SAPG_OB"] = "dom|domestic|holiday|web";
				adobeProductMap["mdomsbi"] = "dom|domestic|flight|web";
				adobeProductMap["mintsbi"] = "int|international|flight|web";
				adobeProductMap["mdomhotelsbi"] = "dom|domestic|hotel|web";
				adobeProductMap["mbussbi"] = "dom|domestic|bus|web";
				adobeProductMap["INT_HOTEL"] = "int|international|hotel|web";
				adobeProductMap["INT_HOTEL_TRM"] = "int|international|hotel|web";
			},

		getAdobeJSON : function(detailJson){
			try{
			var product = detailJson.product;
           	 if(adobeProductMap[product] != '' && typeof adobeProductMap[product] != 'undefined'){

	            var lobVal = "";
	            var appName = 'yt';
	            var pageTenantAdobe = "";
	            var platformVal = "web";
	            var pageTypeVal = "business";
	            var sessionID = null;
	            var primaryCategoryVal = "";
	            var subCategory1Val = "";
	            var subCategory2Val = "payment";
	            var subCategory3Val = "";
	            var loginStatus = "Guest";
	            var adobeTrackPaymentLoad= "";

	            var sessionVxVal = YatraPaymentUI.getCookie('SessionVX') ? YatraPaymentUI.getCookie('SessionVX'): "" ;
	            var ssoTokenVal = YatraPaymentUI.getCookie('userName');
	            var adobeTrackPaymentLoad= "";


	            if(ssoTokenVal != null && ssoTokenVal != "" && ssoTokenVal != 'undefined'){
	            	loginStatus="logged-in";
	            }

                var adobeFieldData = adobeProductMap[product];
                var fields = adobeFieldData.split("|");
                pageTenantAdobe=fields[0];
                lobVal=fields[2];
                platformVal=fields[3];
                primaryCategoryVal = lobVal+" checkout";
                subCategory1Val = fields[1]+" "+lobVal;

	             var pageNameVal = appName+":"+lobVal+":"+pageTenantAdobe+":checkout:payment";
	             var pageInfo = {
			             pageName:pageNameVal,
			             lob:lobVal,
			             sessionID:sessionVxVal
	             };
	             var category = {
			              pageType:pageTypeVal,
			              primaryCategory:primaryCategoryVal,
			              subCategory1:subCategory1Val,
			              subCategory2:subCategory2Val,
			              subCategory3:subCategory3Val
	             };
	            var user = {
	                 loginStatus:loginStatus,
	                 userID:""
	             };

				page.pageInfo = pageInfo;
				page.category = category;
				//page.user = user;
				digitalDataAdobe.page=page;

				if(typeof detailJson.adobeTrackPaymentLoad != 'undefined' && typeof detailJson.adobeTrackPaymentLoad[lobVal] != 'undefined'){
		            	adobeTrackPaymentLoad = detailJson.adobeTrackPaymentLoad[lobVal];
		            	if(lobVal && lobVal.toLowerCase() === "hotel"){
							var isPayPerUseAvaliable=(detailJson.adobeTrackPaymentLoad[lobVal].hotelInfo && detailJson.adobeTrackPaymentLoad[lobVal].hotelInfo.isPayPerUse === "true")?"yes":"no";
							adobeTrackPaymentLoad.hotelInfo.ppuused=isPayPerUseAvaliable;
							if(isPayPerUseAvaliable === "yes"){
								adobeTrackPaymentLoad.hotelInfo.hotelflowtype="hotel flow type:ppu";
							}else{
								adobeTrackPaymentLoad.hotelInfo.hotelflowtype="hotel flow type:normal";
							}
							adobeTrackPaymentLoad.hotelInfo.weekendWidget=(detailJson.adobeTrackPaymentLoad[lobVal].hotelInfo && detailJson.adobeTrackPaymentLoad[lobVal].hotelInfo.isWeekendgetAway === "true")?"selected":"not selected";
							adobeTrackPaymentLoad.hotelInfo.smartHotel=(detailJson.adobeTrackPaymentLoad[lobVal].hotelInfo && detailJson.adobeTrackPaymentLoad[lobVal].hotelInfo.isYatraSmart === "true")?"selected":"not selected";
						}
		        }
				if(typeof digitalDataAdobe.transaction == 'undefined'){
					digitalDataAdobe.transaction={};
				}
				digitalDataAdobe[lobVal]=adobeTrackPaymentLoad;
				digitalDataAdobe.events="Payment Page";

				var lobInfo = lobVal + "Info";
				try {
					var vendorId = digitalDataAdobe[lobVal][lobInfo]["tenant"];
				} catch(e) {
					console.log("tenant not present in lob info from booking details");
				}
				if(typeof vendorId == 'undefined') {
					vendorId = "B2C";
				}
				digitalDataAdobe.transaction["thirdPartyVendorID"] = vendorId;

var showingQBCard = false;

				if(qbJson != null && typeof(qbJson) != "undefined" && qbJson != "" && qbJson != "null") {
					var qbJsonObject = $.parseJSON(qbJson);
					if(qbJsonObject != null && typeof(qbJsonObject.status) != "undefined") {
							if(qbJsonObject.status == 'ENABLED') {

						if(qbJsonObject.authenticationFailed || qbJsonObject.authenticationRequired) {

						}
						// Get Quick Book Cards div.
						else {
							showingQBCard = true;
								}

					}
					}
				}

				//getting the time spent on review page
				var sessionTimeoutData = detailsJson.sessionTimeoutData;
				if(sessionTimeoutData && sessionTimeoutData.pricingTime !=undefined && sessionTimeoutData.pricingTime != ""
					&& sessionTimeoutData.currentTime !=undefined && sessionTimeoutData.currentTime != ""
					&&	sessionTimeoutData.timeout > 0) {
					var currentTime = new Date(sessionTimeoutData.currentTime);
					var pricingTime = new Date(sessionTimeoutData.pricingTime);

					var timeLapsedInMillis = currentTime - pricingTime;
					var timeLapsedInSecs = Math.floor(timeLapsedInMillis / 1000);
					digitalDataAdobe.timeOnReview= timeLapsedInSecs;

				}

				digitalDataAdobe.cardshown = showingQBCard? "shown" : "not shown"

				if(typeof yt_adobe != 'undefined'){
					yt_adobe.track("virtualPageView", digitalDataAdobe);
			  }
			}
			}
			catch(e){
				console.log("some issue in sending adobe data on load")
				}
		},


		getCookie: function(c_name) {
			var i,x,y,ARRcookies=document.cookie.split(";");
			for (i=0;i<ARRcookies.length;i++) {
			  x=ARRcookies[i].substr(0,ARRcookies[i].indexOf("="));
			  y=ARRcookies[i].substr(ARRcookies[i].indexOf("=")+1);
			  x=x.replace(/^\s+|\s+$/g,"");
			  if (x==c_name) {
				return unescape(y);
			  }
			}
		},
		computeAmount : function(rate){
			var totalAmount = ShoppingCart.getTotalCartAmount();
		        return YatraPayment.formatAmount((totalAmount/rate).toFixed(2));
		},
		showConvertedAmount : function(){
			   $('#paypalContainer input').on('click', function() {
				   $('.paypal_pmt_msg_INFO').hide()
			   	var paymentOption = YatraPaymentUI.getPaymentOption();
				   $("input[name=paypal]:checked", "#paypalContainer").parents('.paypal-block-sel').addClass('active').siblings().removeClass('active')
				   $("#convertedAmountPaypal, #mobile-conv")
				   .html("<p id='showConverstion'><span> 1 "+ $('input[name=paypal]:checked', '#paypalContainer').attr('id') +" = </span><span>"+$('input[name=paypal]:checked', '#paypalContainer').attr("forexrate") +" </span><span>INR</span></p>" );

				 if(paymentOption === 'paypal'){
				  	 if($('input[name=paypal]:checked', '#paypalContainer').attr('id') == 'INR'){
				  		$("#paypalINRHelper").show();
					   $("#convertedAmountPaypal, #mobile-conv").css('visibility', 'hidden');
					   $('#showConverstion').css('visibility', 'hidden');
				   	}else {
				   		$("#paypalINRHelper").hide();
				   		$("#convertedAmountPaypal, #mobile-conv").css('visibility', 'visible');
					   $('#showConverstion').css('visibility', 'visible');
				   	}
				  }


			   });
		},

		populatePaypalSection : function(){
			 if(exchangeRates && exchangeRates.status == "true"){
		        var wrapperContainer = $("#paypalContainer");
		        // Swapping the default currency to top.
		        /*for(var i=0;i<exchangeRates.currencies.length;i++){
		        	if(exchangeRates.currencies[i].code == exchangeRates["defaultCurrency"]){
		        		exchangeRates.currencies[0] = exchangeRates.currencies.splice(i, 1, exchangeRates.currencies[0])[0];
		        		break;
		        		}
		        }*/

		        for(var i=0;i<exchangeRates.currencies.length;i++){
		             var paypalTemplate = $(".paypal-block-sel").first().clone();
		             paypalTemplate.find("label").attr("for",exchangeRates.currencies[i].code);
		             paypalTemplate.find("input").attr("id",exchangeRates.currencies[i].code);
		             paypalTemplate.find("input").attr("forexrate",exchangeRates.currencies[i].rate);
		             paypalTemplate.find("input").attr("forexamount",this.computeAmount(exchangeRates.currencies[i].rate));
		             paypalTemplate.find(".currency-name").append(exchangeRates.currencies[i].code + "  <span class='cur-name'>("+exchangeRates.currencies[i].name+") </span>");
		             var cur=exchangeRates.currencies[i].unicode.replace("U+","&#x");
		             paypalTemplate.find(".currency-sign").html(cur);
		             paypalTemplate.find("input").attr("currencysymbol",cur);
		             var amount = this.computeAmount(exchangeRates.currencies[i].rate);
		             paypalTemplate.find(".amount-usd").text(amount);
		             wrapperContainer.append(paypalTemplate);

		             if(exchangeRates.currencies[i].code == 'INR'){
		            	   wrapperContainer.append('<div id="paypalINRHelper"  class="paypalINRHelper" style="display:none"> <p> <i class="ico_info_paySwift PaymentSprite"></i> INR is allowed only for Indian registered PayPal accounts</p> </div>');
		             }
		        }
		    }

			 		var defaultCurrency = (exchangeRates["defaultCurrency"])?exchangeRates["defaultCurrency"]: exchangeRates.currencies[0].code;

			 		this.showConvertedAmount();
				    var $selectedCurrency = $("#paypalContainer").find("#"+ defaultCurrency );
				    if($selectedCurrency.length){
				    	// $selectedCurrency.prop("checked",true).click();
				    }
				    else{
				    	// $("#paypalContainer input[type='radio']").first().prop("checked",true).click();
				    }
		},

		changePaypalSection : function(){
			var that=this;
			var paymentOption = YatraPaymentUI.getPaymentOption();
			$("#paypalContainer .amount-usd").each(function(index,val){
				$(this).text(that.computeAmount(exchangeRates.currencies[index].rate));
			});
			$("#paypalContainer input").each(function(index,val){
				$(this).attr("forexamount",that.computeAmount(exchangeRates.currencies[index].rate));
			});
			$("#convertedAmountPaypal, #mobile-conv")
			   .html( "<p id='showConverstion'><span> 1 "+ $('input[name=paypal]:checked', '#paypalContainer').attr('id') +" = </span><span>"+$('input[name=paypal]:checked', '#paypalContainer').attr("forexrate") +" </span><span>INR</span></p>" );
			  if(paymentOption === 'paypal'){
			  	 if($('input[name=paypal]:checked', '#paypalContainer').attr('id') == 'INR'){
			  		$("#paypalINRHelper").show()
				   $("#convertedAmountPaypal, #mobile-conv").css('visibility', 'hidden');
				   $('#showConverstion').css('visibility', 'hidden');
				 }else {
						$("#paypalINRHelper").hide()
				   	$("#convertedAmountPaypal, #mobile-conv").css('visibility', 'visible');

					   $('#showConverstion').css('visibility', 'visible');
				   }
			  }


		},
		callForRecalculatingTcs: function(nonExpEcash) {
			var amnt = nonExpEcash ? (ShoppingCart.getTotalCartAmount() + nonExpEcash - ShoppingCart.tcsAmount) : ShoppingCart.getTotalCartAmount();
				amnt = GV.totalGVAmount() > 0 ? (ShoppingCart.getTotalCartAmount() + nonExpEcash - ShoppingCart.tcsAmount) : ShoppingCart.getTotalCartAmount();

			if(GV.totalGVAmount() >0 && ShoppingCart.getTotalCartAmount() < ShoppingCart.tcsAmount && GV.balanceAmountAfterGV < 2){
				return ;
			}else if (GV.totalGVAmount() >0 && ShoppingCart.getTotalCartAmount() == 0){
				if($('input:radio[name="payOption"]:checked').val() == "payPartial"){
					amnt = detailsJson.partialPayment.partialPayAmount ;
				}else if($('input:radio[name="payOption"]:checked').val() == "holdAndPay"){
					amnt = detailsJson.holdPayment.holdAmount;
				}else{
					amnt = detailsJson.totalAmount ;
				}
			}
			else{
				amnt = ShoppingCart.getTotalCartAmount()+ GV.totalGVAmount()
			}
			var fecthMessageURL = "/holidays/recalculateTCS.htm";
			$.ajax({
				url: fecthMessageURL,
				timeout: (5 * 1000),
				type: 'POST',
				async: false,
				data: {
					superPnr : detailsJson.superPnr ,
					totalPayableAmount : amnt


				},
				cache: false,
				success : function(response) {

					$("#fareBreakup-TCS")[0].textContent = YatraHelper.formatPrice(response.tcs.totalTCSAmount);
					totall = Number(ShoppingCart.getTotalCartAmount()) + Number(response.tcs.totalTCSAmount);

					if(Ewallet.ecashRedeemedAmount > 0 && Ewallet.ecashRedeemedAmount > Number(ShoppingCart.getTotalCartAmount())){
						totall = ShoppingCart.getTotalCartAmount();
						var partofTCSRedeemed =  Ewallet.ecashRedeemedAmount - (ShoppingCart.getTotalCartAmount() - ShoppingCart.tcsAmount) ;
						console.log("You Pay" , response.tcs.totalTCSAmount - ShoppingCart.tcsAmount )
					//	console.log("You Pay ......" , response.tcs.totalTCSAmount - partofTCSRedeemed )

					}
					if(GV.totalGVAmount() > 0 && ShoppingCart.getTotalCartAmount() == 0){
						var selMode = $('input:radio[name="payOption"]:checked').val();

						if(selMode == "payPartial"){
							totall = Number(detailsJson.partialPayment.partialPayAmount) + Number(response.tcs.totalTCSAmount) - GV.totalGVAmount() -Ewallet.ecashRedeemedAmount ;
						}else if(selMode == "holdAndPay"){
							totall = Number(detailsJson.holdPayment.holdAmount) + Number(response.tcs.totalTCSAmount) - GV.totalGVAmount()-Ewallet.ecashRedeemedAmount ;
						}else{
							totall = Number(detailsJson.totalAmount) + Number(ShoppingCart.tcsAmount) - GV.totalGVAmount() - Ewallet.ecashRedeemedAmount ;
						}
						try{
							console.log($('input:radio[name="payOption"]:checked').val());
							console.log(Number(detailsJson.partialPayment.partialPayAmount) + Number(response.tcs.totalTCSAmount) - GV.totalGVAmount())
							console.log(Number(detailsJson.holdPayment.holdAmount) + Number(response.tcs.totalTCSAmount) - GV.totalGVAmount())
						} catch(err){
							console.log(err)
						}
					}
					GV.balanceAmountAfterGV = totall;
					ShoppingCart.totalPayableAmount = totall;
					PaymentCommon.changePayNowLabel(detailsJson.product);
					if(totall == 0){
						YatraEWalletPaymentUI.hidePaymentDiv()

					}

					if(GV.totalGVAmount() > 0){
						YatraEWalletPaymentUI.setEWalletUI();
					}

					$("#"+ShoppingCart.totalAmountSpan).html(YatraHelper.formatPrice(totall));
					$("#totalAmountSpann").html(YatraHelper.formatPrice(totall));

					ShoppingCart.tcsAmount = response.tcs.totalTCSAmount;


				},
				error: function (request, status, error) {
					alert("Error while getting recalculatd TCS value.");
				}
			});
		}



};

LOBNotification = {
		paymentChangeNotification : b2bProducts.split(","),
		previousPaymentType : null,
		previousPaymentOption : null,
		paymentType : "",
		paymentOption : "",
		cardBrand : "",

		paymentOptionChange : function (paymentOption,cardType){
			var paymentJSON = getPaymentJSON();
			var productCode = paymentJSON.product;
			if(!notificationRequired(productCode))return;
			this.paymentType = cardType;
			this.paymentOption = paymentOption;
			this.cardBrand= "";
			if(!this.paymentOption){
				this.paymentOption=YatraPayment.getPayOp();
			}


			switch(this.paymentOption){
			case "cc":
				enteredCardChanged();
				break;
			case "dc":
				enteredCardChanged();
				break;
			case "qb":
				var paymentOptionDiv = YatraPayment.getPaymentDiv();
				var cardIdElm = $(paymentOptionDiv +" input[name= cardid]:checked");
				LOBNotification.paymentType = $(cardIdElm).attr('processcardid');
				LOBNotification.cardBrand =  $(cardIdElm).attr('cardType');
				LOBNotification.sendNotification();
				break;
			case "corpCard":
				//cardBrand
				LOBNotification.cardBrand = $('#corpCard_cardBrand_id').val();
				LOBNotification.paymentType = $('#corpCard_cardType_id').val();
				//cardType
				LOBNotification.sendNotification();
				break;
			case "emi":
				enteredCardChanged();
				break;
            case "paypal":
                    LOBNotification.paymentType = "PAYPAL";
                    LOBNotification.sendNotification();
                    break;
            case "upi":
                    LOBNotification.paymentType = "UPI";
                    LOBNotification.sendNotification();
                    break;
            case "tez":
                LOBNotification.paymentType = "TEZ";
                LOBNotification.sendNotification();
                break;
            case "rewards":
				enteredCardChanged();
				break;
			case "cashCard":
				LOBNotification.paymentType = $('#tab_'+LOBNotification.paymentOption+' > input[name="payop"]').val();
				LOBNotification.sendNotification();
				break;
			case "mw":
				handleNetbankingCases();
				break;
			case "itz":
				handleNetbankingCases();
				break;
			case "atm":
				handleNetbankingCases();
				break;
			case "nb":
				handleNetbankingCases();
				break;
			case "cp":
				LOBNotification.paymentType = "NA";
				LOBNotification.cardBrand = "NA";
				LOBNotification.sendNotification();
				break;
			case "ec":
				handleNetbankingCases();
				break;

			default :


			}
			function notificationRequired(productCode) {
				for (var j = 0; j < LOBNotification.paymentChangeNotification.length; j++) {
					if (productCode == LOBNotification.paymentChangeNotification[j])
						return true;
				}
				return false;
			}

			function handleNetbankingCases(){
				LOBNotification.paymentType = $('#tab_'+LOBNotification.paymentOption+' > input[name="bankCode"]').val();
				LOBNotification.sendNotification();
			}
			//For entered cards, cardType will be received only after full card is entered
			function enteredCardChanged(){
				LOBNotification.paymentType = typeof LOBNotification.paymentType == "undefined" ? "NA" : LOBNotification.paymentType;
				LOBNotification.sendNotification();
			}
		},
		sendNotification: function(){
			//if(this.previousPaymentOption==this.paymentOption && this.previousPaymentType==this.paymentType)
			//	return;
			console.log("Payment option changed "+LOBNotification.paymentType  +"  "+LOBNotification.paymentOption );
			try{
				var payOpChange = {
						paymentOption : LOBNotification.paymentOption,
						paymentType : LOBNotification.paymentType,
						cardBrand : ""
				}
				if(LOBNotification.cardBrand)
					payOpChange.cardBrand= LOBNotification.cardBrand;
				paymentOptionChange(payOpChange);
			}
			catch(e){console.log(e)}
			LOBNotification.previousPaymentOption=LOBNotification.paymentOption;
			LOBNotification.previousPaymentType=LOBNotification.paymentType;
		}
}


PaymentMode = {
		partialEnabledForProduct : false,
		holdAndPayEnabledForProduct : false,

		checkPaymentModes : function(paymentMode) {
			var selPaymentOption = YatraPaymentUI.getPaymentOption();
			var partialPay = PaymentMode.partialEnabledForProduct && partial_payOpArr.indexOf(selPaymentOption)>= 0 ;
			var holdAndPay = PaymentMode.holdAndPayEnabledForProduct && holdAndPay_payOpArr.indexOf(selPaymentOption)>= 0 ;

			(partialPay) ? $('#partial_payment_div').show(): $('#partial_payment_div').hide();
			(holdAndPay) ? $('#hold_pay_div').show(): $('#hold_pay_div').hide();

			$('#partialPaymentDiv input:radio[name="payOption"]').each(function(){
				$(this).prop('checked', false);
			});

			if(!partialPay && !holdAndPay){
				$('#partialPaymentDiv').hide();
				$('input:radio[name="payOption"]:first').prop("checked", true);
			}
			else {
				$('#partialPaymentDiv').show();
				var selectedMode = PaymentMode.selPaymentMode;
				var selectedModeValid = ( partialPay && selectedMode=="partPayOption") || (holdAndPay && selectedMode=="holdAndPayOption");
				if(!selectedModeValid){
					var ele = $('input:radio[name="payOption"]:nth(0)');
					$(ele).prop("checked", true);
				}else{
					$("#"+ PaymentMode.selPaymentMode).prop("checked", true);
				}
			}
			PaymentMode.notifyPartial();
		},
		initialise : function() {
			var paymentJson = getPaymentJSON();
			var productCode = paymentJson.product;
			var holdAndPayEnabled = paymentJson.holdAndPay && (paymentJson.holdAndPay.isEligibleForHold=="true" || paymentJson.holdAndPay.isEligibleForHold===true);
			var partialPaymentEnabled = paymentJson.partialpayment && (paymentJson.partialpayment.isEligibleForPartial==="true" || paymentJson.partialpayment.isEligibleForPartial===true);
			this.partialEnabledForProduct = partialPaymentEnabled && PaymentMode.isPayModeSupportedOnProduct(partialPay_productsArr,productCode) ;
			this.holdAndPayEnabledForProduct = holdAndPayEnabled && PaymentMode.isPayModeSupportedOnProduct(holdAndPay_productsArr,productCode);
			if (this.partialEnabledForProduct) {
				YatraPaymentUI.setElementText('payNowAmount', paymentJson.partialpayment.payNowAmount);
				YatraPaymentUI.setElementText('partialPayNowAmount', paymentJson.partialpayment.partialPayNowAmount);
				YatraPaymentUI.setElementText('partialPayLaterAmount', paymentJson.partialpayment.partialPayLaterAmount);
				$('#partial_payment_div').show();
				//$('input:radio[name="payOption"]:nth(1)').attr("checked", "checked");
			}
			if (this.holdAndPayEnabledForProduct)  {
				YatraPaymentUI.setElementText('payNowAmount', paymentJson.holdAndPay.payNowAmount);
				YatraPaymentUI.setElementText('holdAndPayNowAmount', paymentJson.holdAndPay.holdAndPayNowAmount);
				$('#hold_pay_div').show();
				//$('input:radio[name="payOption"]:nth(2)').attr("checked", "checked");
			}
			/*Need to check this block is not in current live was removed fot holidays*/
			if(paymentJson.partialpayment && paymentJson.partialpayment.isSelected && paymentJson.partialpayment.isSelected=='true'){
				$('input:radio[name="payOption"]:nth(1)').attr("checked", "checked");
			}
			else if(paymentJson.holdAndPay && paymentJson.holdAndPay.isSelected && paymentJson.holdAndPay.isSelected=='true'){
				$('input:radio[name="payOption"]:nth(2)').attr("checked", "checked");
			}
			else{
				$('input:radio[name="payOption"]:nth(0)').attr("checked", "checked");
			}
			/*************************************************************************/
			this.selPaymentMode = $("input:radio[name=payOption]:checked").attr("id");
			PaymentMode.checkPaymentModes();
			this.payModeActions();
		},
		notifyPartial : function() {
			try{
				var partialPayOption = $('input:radio[name="payOption"]:checked').val();
				processPartialPayment(partialPayOption);
			}catch(e){}
		},
		isPayModeSupportedOnProduct : function(productArray, productCode) {
			for (var j = 0; j < productArray.length; j++) {
				if (productCode == productArray[j])
					return true;
			}
			return false;
		},
		payModeActions : function(){
			$("input:radio[name=payOption]").on('click', function() {
				if(Ewallet.ecashRedeemedAmount>0 && GV.totalGVAmount() > 0 && detailsJson.isTcsEnabled == true){
					Ewallet.showAlert();
					YatraEWalletPaymentUI.reverseAuth();
				}
				var partialPayOption = $(this).val();
				ShoppingCart.addConvFee();
				PaymentMode.notifyPartial();
				PaymentMode.selPaymentMode = $(this).attr("id");
				YatraPaymentUI.pushGAInfoForPaymentModes(partialPayOption);
			});
			//Was forced to do this.
			$("input:radio[name=paymentOption]").on('click', function() {
				var partialPayOption = $(this).val();
				var selectedPayMode = $(this).attr("id");
				if(selectedPayMode=="payNowPaymentOption"){
					PaymentMode.selPaymentMode = "payNowOption";
				}
				else if(selectedPayMode=="partialPayPaymentOption"){
					PaymentMode.selPaymentMode = "partPayOption";
				}
				else if(selectedPayMode=="holdAndPayPaymentOption"){
					PaymentMode.selPaymentMode = "holdAndPayOption";
				}
				$('input:radio[name="payOption"][value="'+partialPayOption+'"]').attr("checked", "checked");
				YatraPaymentUI.pushGAInfoForPaymentModes(partialPayOption);
			});
		}
},


CreditPool = {
		rowTemplateStringSecondary : "rowTemplateStringSecondary",
		initialise : function() {

			var paymentJson = getPaymentJSON();
			// here poolType should be either 'CREDIT','CASH' or 'PERSONAL'. This will be sent by the LOB in dualPool case.
			var poolType = paymentJson["agentProfileType"];
			//If poolType not received show error message. Paynow click with cp as payment option should not go forward
			if(!poolType){
				$("#cp_divErrMsgBlock").attr('style', 'display:block;');
				$("#cp_divErrMsgBlock").html('Sorry, Error getting credit pool information!');
				return;
			}

			poolType = poolType.toUpperCase();
			var  hotelGds = detailsJson.isPassthrough=="true" && (paymentJson.product=="crpdom_hotel"||paymentJson.product=="crpint_hotel"||paymentJson.product=="crpinthotel"||paymentJson.product=="crpdomhotel");
			// if poolType is personal remove all payment options except credit card.

			/*	if(hotelGds){
				var msgBox = $("#cc_divErrMsgBlock_hotelGDS");
				$(msgBox).html('The card will not be charged - it is only needed to guarantee your booking . CVV is not required .');
				$(msgBox).show();
				YatraPaymentUI.disableCVVforGdsFlow();
			}*/

			if(detailsJson.secondaryCurrencyJSON){
				var secondaryCurrencyJSON = detailsJson.secondaryCurrencyJSON;
				var paymentType = detailsJson.secondaryCurrencyJSON.paymentType;
				// Now code here to populate the logic same as in shipping cart using the row template .

				var secTemplateString = new Template();
				secTemplateString.set(this.rowTemplateStringSecondary);

				var html = "";
				if(secondaryCurrencyJSON.currencies){
					for(var i in secondaryCurrencyJSON.currencies){
						html += secTemplateString.render({label:secondaryCurrencyJSON.currencies[i]["currencyKey"],amount:secondaryCurrencyJSON.currencies[i]["currencyValue"]});
					}
					$("#breakDownContainerSecondary").html(html);
					$("#secondaryCurrrencyContainer").removeClass("hide");
				}



			}

			if(detailsJson.notifyUserMessage){
				var msgBox = $("#cc_divErrMsgBlock_hotelGDS");
				$(msgBox).html(detailsJson.notifyUserMessage);
				$(msgBox).show();
				YatraPaymentUI.disableCVVforGdsFlow();
			}

			if(hotelGds){
				try{
					$("#cpmt_tabmenu li").not( $("#cc").parent() ).remove();
					$("div .cpmt_payop").filter(":not(#tab_cc)").remove();
					YatraPaymentUI.enableFirstPaymentOption();
				}
				catch(e){
					console.log(e);
				}


				YatraPaymentUI.disableCVVforGdsFlow();
				YatraPaymentUI.disableQBCVVForCreditCard($('input:radio[processCardId=cc]'));
                YatraPaymentUI.disableMultiFlowUiForCreditCard($('input:radio[processCardId=cc]'));
				return;
			}

			$('#'+poolType + "PoolDiv").attr('style', 'display:block;');
			//set pool type from LOB(getPaymentJSON())
			var poolTypeString = poolType == "CREDIT" ? "creditPool" : "cashPool";
			$('#cp_poolType_id').val(poolTypeString);

		},

		validate : function (){
			var payOp = YatraPaymentUI.getPaymentOption();
			var paymentJson = getPaymentJSON();
			if(payOp=="cp"){
				var poolType = $('#cp_poolType_id').val();

				// if poolType not set or set value is wrong then return false
				if(!poolType || !(poolType=="creditPool" || poolType == "cashPool")){
					$("#cp_divErrMsgBlock").attr('style', 'display:block;');
					$("#cp_divErrMsgBlock").html('Sorry, Error getting credit pool information!');
					$('#cp_divErrMsgBlock').delay(3000).queue(function (next) {
						$(this).css('display', 'none');
						next();
					});
					return false;
				}
				var poolTypeFromPaymentJson = paymentJson["agentProfileType"];
				var amount = paymentJson['amount'];
				var poolBalance = $('#'+poolTypeFromPaymentJson + "PoolDiv").attr('amount');

				if(amount > poolBalance ){
					$("#cp_divWarningMsgBlock").attr('style', 'display:block;');
					$("#cp_divWarningMsgBlock").html('Insufficient Balance !');
					$('#cp_divWarningMsgBlock').delay(3000).queue(function (next) {
						$(this).css('display', 'none');
						next();
					});
					return false;
				}
			}
			return true;
		}
}


CashPool = {
		initialise : function() {

			var paymentJson = getPaymentJSON();
			$('#'+"CashPoolAmountDiv").attr('style', 'display:block;');

		},

		validate : function (){
			var payOp = YatraPaymentUI.getPaymentOption();
			var paymentJson = getPaymentJSON();
			if(payOp=="cashPool"){

				var amount = paymentJson['amount'];
				var poolBalance = $('#'+"CashPoolAmountDiv").attr('amount');

				if(amount > poolBalance ){
					$("#cashPool_divWarningMsgBlock").attr('style', 'display:block;');
					$("#cashPool_divWarningMsgBlock").html('Insufficient Balance !');
					$('#cashPool_divWarningMsgBlock').delay(3000).queue(function (next) {
						$(this).css('display', 'none');
						next();
					});
					return false;
				}
			}
			return true;
		}
}



$(document).ready(function() {
	$( "#cc_cno_id,#cc_cardholder_name_id,#cc_expm_id,#cc_expy_id,#cc_cvv_id" ).focus(function() {
		  $("#qb_password_cc").val("");
	});
	$( "#dc_cno_id,#dc_cardholder_name_id,#dc_expm_id,#dc_expy_id,#dc_cvv_id" ).focus(function() {
			  $("#qb_password_dc").val("");
	});
	try{
		$('.popmodal').popmodal();
	}catch(e){console.log(e)}
	jQuery.support.cors = true;
	setTimeout(function() {
		YatraPaymentUI.initialise();
		if(exchangeRates && exchangeRates.status == "true"){
			YatraPaymentUI.populatePaypalSection();
		}
		IE_Placeholder.init();
		$("#partialPaymentDiv input").trigger('change');
	}, 200);
	setTimeout(function() {
		$("#containerScroll").slideUp("slow")
	},3000)
	try{
		if ($.browser.msie && parseInt($.browser.version) < 9) {
			$("input").parent().removeClass("custom-checkbox");
			$("input").parent().removeClass("custom-Radiobox");
		}
	} catch(e){}
	if(detailsJson.crpInfo != undefined) {
        YatraCorpCardUI.getTokenizedCardList();
    }
});



/* IE PlaceHolder text */
var IE_Placeholder = IE_Placeholder || {
	config: {
		placeholder_color_class:'placeholderColor',
		normaltext_color_class:'defaultColor',
		isIE:0
	},
	init: function(elemStr) {

		var $this = this,
		ua = window.navigator.userAgent;

		$this.config.isIE = ua.match(/msie/i);

		if($this.config.isIE) {
			// set default values for each input element

			if(elemStr == "" || elemStr == undefined || elemStr == null)
			{
				elemStr = "input[type=text]";
			}

			$(elemStr).each(function() {

				var jElem = $(this);

				$this.placeDefaultText(jElem);
				document.getElementById(jElem.prop('id')).onfocus = function() {
					$this.removeDefaultText($(this));
				};

				document.getElementById(jElem.prop('id')).onblur = function() {
					$this.placeDefaultText($(this));
				};

				document.getElementById(jElem.prop('id')).onchange = function() {
					$this.placeDefaultText($(this));
				};

				$this.callBackPlaceDefaultText($(this));
			});
		};
	},
	removeDefaultText:function(jElem) {

		var $this = this,
		placeholder = jElem.attr('placeholder');

		if(jElem.val() == placeholder)
		{
			jElem.val("");
			jElem.addClass($this.config.normaltext_color_class);
			jElem.removeClass($this.config.placeholder_color_class);
		}
	},
	placeDefaultText:function(jElem) {

		var $this = this,
		placeholder = jElem.attr('placeholder');

		if(jElem.val() == "" || jElem.val() == null || jElem.val() == undefined || (jElem.val() == jElem.attr('placeholder') && $this.config.isIE == 'MSIE'))
		{
			jElem.removeClass($this.config.normaltext_color_class);
			jElem.addClass($this.config.placeholder_color_class);
			jElem.val(placeholder);
		}
		else
		{
			jElem.removeClass($this.config.placeholder_color_class);
			jElem.addClass($this.config.normaltext_color_class);
		}
	},
	callBackPlaceDefaultText:function(jElem) {

		var $this = this;

		if($this.config.isIE) {
			$this.placeDefaultText(jElem);
		};
	},
	validateDefaultText:function(elem) {
		var jElem = $(elem);
		var $this = this;
		if(jElem.val() == 0 || jElem.val() == "" || jElem.val() == undefined || (jElem.val() == jElem.attr('placeholder') && $this.config.isIE == 'MSIE'))
		{
			return 0;
		}

		return 1;
	},
	manageValueChange:function(jElem) {

		/*var $this = this;

            if(jElem.val() == "" || jElem.val() == jElem.attr('placeholder'))
            {
                jElem.addClass($this.config.placeholder_color_class);
            }
            else
            {
                jElem.removeClass($this.config.normaltext_color_class);
            }*/
	}


};


Popup = {
		loaderContent: "loader_span",
		loaderSpanClass : "loader_msg_change",
		loaded: false,
		showPopup :function(message, headerTitle){
			$("."+this.loaderSpanClass).html(message);
			var contentHtml= $("#"+this.loaderContent).html();
			if(!this.loaded)PopModal();
			Modal.show({title: headerTitle,
				content : contentHtml,
				width : "100px",
				manual : true});
		},
		showSessionExpire: function(){
			Popup.showPopup("Your session has been expired. Please wait while we reconfirm your itinerary","Session Expire")
		},
		processingPayment: function(){
			Popup.showPopup("Please give us some time","Processing Payment")
		},
		hidePopup : function (){
			Modal.hide();
		}
};

var GV = {
		initialise : function(maxGV){
			GV.gv = [];
			GV.recentGvType;
			GV.validGVType;
			GV.balanceAmountAfterGV ;
			// config for max no. of GV allowed and others
			GV.config = {
					maxGV : maxGV
			};
			GV.registerEvents();
			GV.voucherNodes = [];
			GV.addedGVFirst6;

		},
		maintainState : false,
		isPGPaymentRequiredForVoucher : false,
		registerEvents : function(){
			// adding the prototype method to calculate legth of object.
			Object.size = function(obj) {
			    var size = 0, key;
			    for (key in obj) {
			        if (obj.hasOwnProperty(key)) size++;
			    }
			    return size;
			};


			// only allowing numeric in box
			$('#gvNo').keypress(function (e) {
				// Clear the errors
				GV.clearErrors();
			    var regex = new RegExp("^[0-9a-zA-Z]+$");
			    var str = String.fromCharCode(!e.charCode ? e.which : e.charCode);
			    var key = e.which || e.keyCode || e.charCode;
			     if (regex.test(str) || key == 8) {
			    	 console.log("gv value", $("#gvNo").val())
			    	 return true;
			    }
			    e.preventDefault();
			    return false;
			});

			// when click on redeem GV
			$("#redeem-gv-button").click(function(e){
				GV.clearErrors();
				if(!GV.validateGV($("#gvNo").val(), $("#gvPin").val())){
					return false;
				}
				var gvNo = $("#gvNo").val();
				var gvPin =  $("#gvPin").val();
					GV.addGV(gvNo, gvPin)
			})

			// when GV is changed

			$("#gvNo").on('input propertychange paste', function(e) {
				if(e.type=='input'){

				GV.clearErrors();
			    var regex = new RegExp("^[0-9a-zA-Z]+$");
			    var trimText = $(this).val().trim();
			    $(this).val(trimText);
			    var str = trimText;
			    console.log("gv value1", str);
			     if (regex.test(str)) {
			    	 console.log("regex passed", $("#gvNo").val())

			    	 if(str.length >= 6){
		    		 		if(str.substring(0, 6) == GV.addedGVFirst6 && GV.recentGvType != ""){
		    		 			// $("#redeem-gv-button").removeClass("gv-btn-disabled")
		    		 		}else{
		    		 			GV.addedGVFirst6 = str.substring(0, 6);
		    		 			console.log("calling the GV type", GV.addedGVFirst6);
								GV.getGvType(GV.addedGVFirst6)
		    		 		}

					}


			    	 if(str.length >= 9){
			    		 if(str.substring(0, 9) == GV.addedGVFirst9 && GV.validGVType != ""){
		    		 			$("#redeem-gv-button").removeClass("gv-btn-disabled")
		    		 		}else{
		    		 			GV.addedGVFirst9 = str.substring(0, 9);
		    		 			console.log("calling the GV type", GV.addedGVFirst9);
								GV.checkGvType(GV.addedGVFirst9)
		    		 		}

						}
			    	 else {
			    		 $("#redeem-gv-button").addClass("gv-btn-disabled")
			    	 }
			    	 //return true;
			    } else
			    	{
			    	$("#errorGV").html("Please enter the valid GV");
					$("#errorGV").show();
					// return false;
			    	}
				}

			});



		},
		addGV: function(gvNo, gvPin){
			ProcessPayment.stopPollingForQRCode();
			// check if GV value is more than cart value
			var totalCartAmount = ShoppingCart.getTotalCartAmount();
			if(detailsJson.isTcsEnabled){
				totalCartAmount+= Number(ShoppingCart.tcsAmount);
			}
			if(totalCartAmount == 0){
				console.log("cart balance is already 0, cannot add more coupons");
				$("#errorGV").html("Cart balance is already 0, cannot add more coupons");
				$("#errorGV").show();
				return false;
			}

			//check if how many coupons have already been reedemed.
			var totalAddedGvs = Object.size(GV.gv);
			if((totalAddedGvs +1) > GV.config.maxGV){
				console.log("Max you can redeem "+ GV.config.maxGV + " GVs." );
				$("#errorGV").html("Max you can redeem "+ GV.config.maxGV + " GVs.");
				$("#errorGV").show();
				return false;
			}

			// add in gv JSON and call the render method

			if(GV.alreadyAdded(gvNo)){
			console.log("You have already applied the GV")
			$("#errorGV").html("You have already added this GV, Please add another one")
			$("#errorGV").show();
			}
			else {
				console.log("add the GV")

				var request= GV.requestBuilder(gvNo, gvPin, totalCartAmount);
				// Ajax call for check balance.
				GV.checkBalanceRequest(gvNo, request);

			}


		},
		deleteGV : function(gvNo){
			ProcessPayment.stopPollingForQRCode();
			var gvIndex = GV.alreadyAdded(gvNo);
			// delete the GV in JSON and call the render method
			GV.gv.splice((gvIndex -1), 1);
			// As we are maintaining the consistency in order of GV.gv and GV.vouchernode, same order of GV in both
			GV.voucherNodes.splice((gvIndex -1), 1);
			if(GV.gv.length == 0){
				GV.isPGPaymentRequiredForVoucher = false
			}

			GV.renderGV();
			if (YatraPaymentUI.isTcsEnabled(detailsJson.isTcsEnabled)) {
				GV.balanceAmountAfterGV = ShoppingCart.getTotalCartAmountForTCS() + Number(ShoppingCart.tcsAmount);
				ShoppingCart.totalPayableAmount = GV.balanceAmountAfterGV;
				$("#fareBreakup-Payable").html(GV.balanceAmountAfterGV + GV.totalGVAmount() - ShoppingCart.tcsAmount);
				$("#" + ShoppingCart.totalAmountSpan).html(YatraHelper.formatPrice(GV.balanceAmountAfterGV));
				$("#totalAmountSpann").html(YatraHelper.formatPrice(GV.balanceAmountAfterGV));
			}
			// if(YatraPaymentUI.isTcsEnabled(detailsJson.isTcsEnabled)){
			// 	YatraPaymentUI.callForRecalculatingTcs();
			// }
			// YatraEWalletPaymentUI.setEWalletUI();
			// if(!GV.totalGVAmount()){
			// 	GV.balanceAmountAfterGV = 0 ;
			// }



		},
		renderGV:function(){

			if(GV.gv.length){
				$(".applied-gift-vouc-txt").css("visibility", "visible");
				$("#payNow").css("margin-top", "30px");
				$(".gv-bal-amount").show();
				}
			else{
				$(".applied-gift-vouc-txt").css("visibility", "hidden");
				$("#payNow").css("margin-top", "0px");
				$(".gv-bal-amount").hide();
			}

			var gvs = "";
			var i = 0;
			for(var gv in GV.gv){
				i++;
				gvs = gvs + GV.addedGVDiv(GV.gv[gv].gvNo, GV.gv[gv].maskedGvNo, GV.gv[gv].amountRedeemed, GV.gv[gv].amountBalance)

				}

			$("#appliedGvBlock").html(gvs)

			// update the shopping cart
			GV.updatePaymentDetails();

			// after the UI is rendered adding delete event, Delete the GV remove-vouc
			$(".remove-voucher").click(function(e){
				e.preventDefault();
				console.log("deleting the GV" + this.id);
				GV.deleteGV(this.id);
			})

		},
		addedGVDiv: function(gvNo, maskedGvNo, amountRedeemed, amountBalance, totalBlance ){
			// return the div
					var balanceAmountDiv = amountBalance > 0?'<span class="right"><span>Balance:</span><span class="RupeeSign curcd">Rs.</span><span>'+amountBalance+'</span> </span>' :''

					var gvDiv = ''
						gvDiv = gvDiv + '<li class="gift-voucher-redeem-box">'
						gvDiv = gvDiv + '<div>'
						gvDiv = gvDiv + '<i class="check-success"></i>'
						gvDiv = gvDiv + '<p>'
						gvDiv = gvDiv + '<span>Redeemed:</span> <span class=" right"><span class="RupeeSign curcd">Rs.</span>'+amountRedeemed+'</span> </p>'
						gvDiv = gvDiv + '<p>'
						gvDiv = gvDiv + '<span class="fs12"> '+maskedGvNo + '</span>' + balanceAmountDiv + '</p>'
						gvDiv = gvDiv + '</div><span class="remove-voucher" id='+gvNo +'>X</span></li>'

						return gvDiv
		},
		clearErrors : function(){
		 $("#errorGV").removeClass("warningGV");
			$("#errorGV").hide();
			$("#gv_pmt_msg_WARNING").hide();
		},
		updatePaymentDetails : function(){
			var gvTotalAmount = GV.totalGVAmount()
			//add the GV amount in shopping cart JSON
			ShoppingCart.updateGVRedeemed(gvTotalAmount);
			// update the balance everywhere
			PaymentCommon.onAmountChange();

		},
		totalGVAmount : function(){
			var gvAmount= 0;
			for(var gv in GV.gv){
				gvAmount = gvAmount + GV.gv[gv].amountRedeemed;
			}

			return gvAmount;
		},
		maskGV : function(gvNo){
			var first3 = gvNo.substring(0, 3);
			var last4 = gvNo.substring(gvNo.length - 4);

			var mask = gvNo.substring(3, gvNo.length - 4).replace(/\w/g,"*");
			console.log(first3 + mask + last4);
			return (first3 + mask + last4)
		},
		validateGV  : function(gvNo, pin){
			var regex = new RegExp("^[0-9a-zA-Z]+$");
		     if (regex.test(gvNo)) {
		    	 //return true;
		    } else
		    	{
		    	$("#errorGV").html("Please enter the valid GV");
				$("#errorGV").show();
				return false;
		    	}





			if(GV.recentGvType == "QC"){
				if(gvNo.length!= 16){
					$("#errorGV").html("Please enter the valid GV")
					$("#errorGV").show();
					return false;
				}
				 var regex1 = new RegExp("^[0-9]+$");
			     if (regex.test(pin)) {
			    	 if(pin.length!= 6){
							$("#errorGV").html("Please enter the valid PIN")
							$("#errorGV").show();
							return false;
			    	 }
			    } else
			    	{
			    	$("#errorGV").html("Please enter the valid Pin");
					$("#errorGV").show();
					return false;
			    	}
			}
			if(GV.recentGvType == "PROMO"){
				if(gvNo.length < 10 && gvNo.length > 20){
					$("#errorGV").html("Please enter the valid GV")
					$("#errorGV").show();
					return false;
				}

			}
			return true;
		},
		getGvType : function(gvNo){
			//$("#redeem-gv-button").addClass("gv-btn-disabled")
			$.ajax({
				url : "/PaySwift/gift-voucher/type?voucherCode="+gvNo+"&product="+detailsJson.product+"&merchant=" + detailsJson.merchant,
				type : 'GET',
				contentType: "application/json",
				dataType: 'json',
				cache : false,
				success : function(response) {
					if(response.status == "success"){
						//$("#redeem-gv-button").removeClass("gv-btn-disabled")
						GV.recentGvType = response.type;
						if(GV.recentGvType == "QC"){
							//clearing the PIN
							$("#gvPin").val("");

							$("#gvPinBlock").show()
							// only allowing numeric in GV PIn
							$('#gvPin').keypress(function (e) {
								GV.clearErrors();
							    var regex = new RegExp("^[0-9]+$");
							    var str = String.fromCharCode(!e.charCode ? e.which : e.charCode);
							    var key = e.which || e.keyCode || e.charCode;
							     if (regex.test(str) || key == 8 || key == 118) {
							    	 console.log("gv PIN", $("#gvPin").val())
							    	 return true;
							    }
							    e.preventDefault();
							    return false;
							});

							// on paste of GV PIN
							$("#gvPin").on('input propertychange paste', function(e) {
								if(e.type=='input'){

								GV.clearErrors();
							    var regex = new RegExp("^[0-9]+$");
							    var trimText = $(this).val().trim();
							    $(this).val(trimText);
							    var str = trimText;
							    console.log("PIN value1", str);
							     if (regex.test(str)) {
							    	 console.log("regex passed PIN", $("#gvNo").val())

							    	 //return true;
							    } else
							    	{
							    	$("#errorGV").html("Please enter the valid PIN");
									$("#errorGV").show();
									// return false;
							    	}
								}

							});
						}else {
							$("#gvPinBlock").hide()
						}
					}else {
						GV.recentGvType = "";
							$("#gvPinBlock").hide()
					}
				}, error : function(request, status, error) {

					GV.recentGvType = "";
					console.log("fail gv type", error)
				}
			});
		},
		checkGvType : function(gvNo){
			$.ajax({
				url : "/PaySwift/gift-voucher/checkVocherType?voucherCode="+gvNo+"&superPnr="+detailsJson.superPnr,
				type : 'GET',
				contentType: "application/json",
				dataType: 'json',
				cache : false,
				success : function(response) {
					if(response.resCode == "0"){
						$("#redeem-gv-button").removeClass("gv-btn-disabled")
						GV.validGVType = true;
						}
					else {
							$("#errorGV").html(response.displayMessage);
							$("#errorGV").show();
							GV.validGVType = false;
					}
				}, error : function(request, status, error) {
					GV.validGVType = "";
					console.log("fail check gv", error)
				}
			});

		},
		checkBalanceRequest : function(gvNo, request){
			$.ajax({
				url : "/PaySwift/gift-voucher/"+ detailsJson.merchant+"/"+detailsJson.product +"/"+detailsJson.superPnr+"/check-balance",
				type : 'POST',
				data : JSON.stringify(request),
				contentType: "application/json",
				dataType: 'json',
				cache : false,
				success : function(response) {
					console.log("success add coupon", response)
					// redeemedBalance - > amount that can be redeemed from GV
					// availableBalance -> balance amount that is left after redemption
					// totalBalance -> total value of GV.
					if(response.status == "success"){
						GV.config.maxGV = response.maxVoucherAllowed
						GV.gv.push({"gvNo" : gvNo, "maskedGvNo": GV.maskGV(gvNo), "gvPin" : gvPin, "amountRedeemed" : response.vouchers[GV.voucherNodes.length].redeemedBalance, "amountBalance" : response.vouchers[GV.voucherNodes.length].availableBalance, "totalBalance" : response.vouchers[GV.voucherNodes.length].totalBalance});
						GV.isPGPaymentRequiredForVoucher = response.isPaymentRequired ? response.isPaymentRequired : false;

						GV.renderGV();
						GV.balanceAmountAfterGV = ShoppingCart.getTotalCartAmountForTCS() + Number(ShoppingCart.tcsAmount);
						// if(YatraPaymentUI.isTcsEnabled(detailsJson.isTcsEnabled)){
						// 	YatraPaymentUI.callForRecalculatingTcs(0 , GV.balanceAmountAfterGV);
						// }



						if(YatraPaymentUI.isTcsEnabled(detailsJson.isTcsEnabled)){
							ShoppingCart.totalPayableAmount = GV.balanceAmountAfterGV;
							$("#fareBreakup-Payable").html(GV.balanceAmountAfterGV + GV.totalGVAmount() - ShoppingCart.tcsAmount							);
							$("#"+ShoppingCart.totalAmountSpan).html(YatraHelper.formatPrice(GV.balanceAmountAfterGV));
							$("#totalAmountSpann").html(YatraHelper.formatPrice(GV.balanceAmountAfterGV));
						}
						YatraEWalletPaymentUI.setEWalletUI();

						// clear the input box and PIN after addition
						$("#gvNo").val('')
						$("#gvPin").val('')
						 $("#redeem-gv-button").addClass("gv-btn-disabled")
						// hiding the pin for next coupon
						$("#gvPinBlock").hide()

						// adding the vouchers response to the voucher nodes
						GV.voucherNodes = response.vouchers.slice();

						// clearing the first 6 digit in GV.addedGVFirst6, in order to make ajax check GV type for next voucher
						GV.addedGVFirst6 = "";

						// clearing the GV type for next GV
						GV.recentGvType = "";
						GV.validGVType = "";

						if(response.displayMessage && response.displayMessage != ""){
							$("#gv_pmt_msg_WARNING").html(response.displayMessage);
							$("#gv_pmt_msg_WARNING").show();
						}

					} else {
						console.log("Error in adding the GV")
						 $("#errorGV").removeClass("warningGV")
							$("#errorGV").html(response.displayMessage)
						$("#errorGV").show();
						$("#gv_pmt_msg_WARNING").hide();

					}

				}, error : function(request, status, error) {
					console.log("fail add coupon", error);
					 $("#errorGV").removeClass("warningGV")
					$("#errorGV").html("There is some problem in adding the voucher, please try again later")
					$("#errorGV").show();
					 $("#gv_pmt_msg_WARNING").hide();
				}
			});

		},
		alreadyAdded : function(gvNo){

			for(var i=0; i<GV.gv.length; i++){
				if(gvNo == GV.gv[i].gvNo){
					return i+1;
				}
			}
			return false;
		},
		requestBuilder : function(gvNo, gvPin, totalCartAmount){
			// coping the voucher nodes to new variable
			var requestVouchers = GV.voucherNodes.slice()
			requestVouchers.push({
			      "code": gvNo,
			      "type": GV.recentGvType,
			      "pin" : gvPin
			    })

			    var amount
			  //amount depends on so many factors, -- visa discount is applie or has been removed
			    // checking if visa discount is not applied
			    if(detailsJson.discount && detailsJson.discount.cartLabel && ShoppingCart.json[ShoppingCart.labelIndex[detailsJson.discount.cartLabel]][detailsJson.discount.cartLabel] == 0){
			    	amount = totalCartAmount + GV.totalGVAmount();
			    }
			    else{
			    	// visa discount is applied, double checking with PaymentCommon.isVisaApplied()
			    	amount = PaymentCommon.isVisaApplied() ? totalCartAmount + GV.totalGVAmount() -1 :totalCartAmount + GV.totalGVAmount(); // booking amount - wallet amount redeeme
			    }

				var request =	{
				  "superPnr": detailsJson.superPnr,
				  "emailId": detailsJson.userDetails.email,
				  "amount": amount,
				  "lob": lob,
				  "isd": detailsJson.userDetails.mobileNoISD,
				  "mobile": detailsJson.userDetails.mob,
				  "source": source,
				  "context": "REVIEW", //hardcoded
				  "vouchers": requestVouchers
				}
			return request;
		}

}

