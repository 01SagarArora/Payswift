var prevGSTAmount = 0;
var currentGSTAmount = 0;
var totalPayableAmount = 0;
var gvRedeemedAmount = 0;
var isZeroNonPassThroughPayment = false;
var cardBinTypeFromDB = 'CREDIT';
var redeemNow = true;
var totalCartAmountWithoutConvFeesPercentage = 0;
var payNowCallFromQRCode = false;
var forceStopPolling = false;
var pollPayStatInterval;

window.onerror = function(error, url, line) {
    var err = "ERROR : " + error + ", URL : "+ url + ", LINENO : " + line;
    var ttid = (detailsJson && detailsJson.superPnr !== null && detailsJson.superPnr !== undefined) ? detailsJson.superPnr : "-1";
    if(!url.includes("raven.min.js"))
    {
        PaymentCommon.sendJSerror(err , ttid);
    }
};

ProcessPayment = {
		paymentMode : 'FULL',
		proceedWithPromo:true,
		portalUrl : "",
		payNowUrl : "payNow",
		getQRCode : function(mtxnId, uid, response, swiftResp ){
			$("#QRCodeDiv").find(".cpmt_cardNameLabel").text("Scan below QR with your UPI app");
			$("#QRCodeImage").attr("src", "/PaySwift/getQRImage?mtxnId="+ mtxnId +"&uid="+uid);
			$('#QRCodeImage').off('load').on('load',function(){
				$("#loadingQR").hide()
				$("#QRCodeImage").show();
				ProcessPayment.enablePayNow();
				PaymentCommon.handlePaymentInitResponse(response, swiftResp);
			});
		},
		stopPollingForQRCode : function(){
			$("#QRCodeDiv").find(".cpmt_cardNameLabel").text("Don't have VPA? Scan QR with your UPI app");
			$("#loadingQR").hide()
			$("#QRCodeImage").hide();
			$("#showQRButton").show();
			forceStopPolling = true;
			clearInterval(pollPayStatInterval);
		},
		QRPayNowRequest : function(){
			payNowCallFromQRCode = true;
			forceStopPolling = false;
			$("#showQRButton").hide();
			$("#loadingQR").show();
			$("#sub_payopQR").val("qr");
			this.payNow();
		},
		payNow : function(){
			YatraPaymentUI.pushGAInfoGeneric("Payment","Paynow:"+YatraPayment.getPayOp());
			var payOption = YatraPayment.getPayOp();
			// force stop the polling -> if payNow is called not from QR code
			if(arguments.callee.caller.name != "QRPayNowRequest" ){
				if(payOption !== 'upi' || (payOption == 'upi' && YatraPayment.validatePaymentDiv('paymentForm'))){
					ProcessPayment.stopPollingForQRCode();
					$("#sub_payopQR").val(undefined)

				}
			 	payNowCallFromQRCode = false;
			}
			if(!payNowCallFromQRCode){
				if(!YatraPayment.validatePaymentDiv('paymentForm'))
					return
			}

			var payOption = YatraPayment.getPayOp();
			payOption = YatraEWalletPaymentUI.isCompleteEwalletPayment() && payOption != "gv" ? 'ew':payOption;
			// validation for paypal // we have removed any default selection // writing it seperate as current one wont be able to acumlate this in current implementataion
			if(payOption=='paypal' && !$("input[name=paypal]", "#paypalContainer").is(':checked')){
				  $('.paypal_pmt_msg_INFO').show()
				return
			  }
			if(!payNowCallFromQRCode){
				if(payOption=='upi'){  // in case of UPI show payment initiated message from the start
					// has to make it false in case of UPI as otherwise polling will not start in UPI
					forceStopPolling = false;
					Popup.showPopup("Enter mpin on your UPI app to approve payment.","Payment Initiated");
				} else if(payOption=='tez'){  // in case of UPI show payment initiated message from the start
					forceStopPolling = false;
					Popup.showPopup("Enter mpin in your Google Pay app to authorize payment.","Payment Initiated");
				} else if(payOption=='tez_pwa'){  // in case of UPI show payment initiated message from the start
					forceStopPolling = false;
					Popup.showPopup("Enter mpin in your Google Pay app to authorize payment.","Payment Initiated");
				} else if (payOption =='cred'){
					forceStopPolling = false;
					Popup.showPopup("Enter mpin in your Cred app to authorize payment.","Payment Initiated");
				} else {
					Popup.processingPayment();
				}
			}

			this.portalUrl = YatraPayment.getPaymentOptionUrl(payOption);
			var payNowData = ProcessPayment.createPayNowForm();
			ProcessPayment.disablePayNow();

			// push Live Tracking Data
			ProcessPayment.updatePayNowTrackingData(YatraPaymentUI.getPaymentOption());
			YatraPaymentUI.adobeTrackPaymentLoadData(YatraPayment.getPayOp(),YatraPayment.getParameterByName("ctype"),YatraPaymentUI.getPaymentOption());
			YatraPaymentUI.pushGATrackingData();


			try {
				if (detailsJson && detailsJson.liveDataProduct === "flights") {
					YatraPaymentUI.pushGTAPayswiftTrackingData(payOption);
				} else {
					YatraPaymentUI.pushGTAPayswiftTrakingHotelData(payOption)
				}
			} catch (error) {
				log_error(error);

			}
			var postData =  $(payNowData).serialize();
			$.ajax({
				url : ProcessPayment.payNowUrl,
				type : 'POST',
				data : postData,
				success : function(pdata) {
					try {

						Popup.hidePopup();
						if (!pdata) {
							alert("It seems there is some issue with the Site currently. Please try after some time.");
							return false;
							ProcessPayment.enablePayNow();
						}
						resp = pdata;
						if (resp.success || resp.redirect) {
							if(resp.promoSuccess && resp.promoWarning){
								//ShoppingCart.updatePromocode(0);
								PaymentCommon.onAmountChange();
								var template = new Template();
								template.set("promo-code-warning-message-template");
								//var promoAmount = detailsJson.promo.amount;
								var partPromoAmount;
								var amountToCollect;
								var currencySymbol;

								var promoWarningMessage = resp.promoWarning;
								var payOption = YatraPayment.getPayOp();

								// validation for paypal // we have removed any default selection // writing it seperate as current one wont be able to acumlate this in current implementataion


								YatraPaymentUI.adobeTrackPromoFaliure();
								if(payOption=='paypal' && $("input[name=paypal]", "#paypalContainer").is(':checked')){
										currencySymbol = "<span >"+ $('input[name=paypal]:checked', '#paypalContainer').attr('currencysymbol') +"</span>"
										partPromoAmount = YatraPayment.formatAmount((resp.promoAmount/( $('input[name=paypal]:checked', '#paypalContainer').attr('forexrate'))).toFixed(2));
										amountToCollect = YatraPayment.formatAmount((resp.redirectMap.amount/( $('input[name=paypal]:checked', '#paypalContainer').attr('forexrate'))).toFixed(2));
								}
							else{
									currencySymbol = "<span class='RupeeSign'>Rs</span>"
										partPromoAmount = resp.promoAmount;
									amountToCollect = resp.redirectMap.amount;

								}
								$("#promo-code-warning-message").html(template.render({discountedAmount:YatraHelper.formatPrice(amountToCollect),
								promoWarn:promoWarningMessage,
								promoAmount:partPromoAmount,
								currencySymbol:currencySymbol,
								gstMessage :  (resp.showGSTMessage) ?". Any applicable GST will also be adjusted.":""
								}));

								PaymentCommon.showBlackDiv();
								$("#valid_warning_promo_code_discount_pop_up").css({"display":"block"});
								ProcessPayment.enablePayNow();
							}
							else if(!resp.priceValidationSuccess) {
								PaymentCommon.onAmountChange();
								var template = new Template();
								template.set("price-change-warning-message-template");

								var payOption = YatraPayment.getPayOp();
								if(payOption=='paypal' && $("input[name=paypal]", "#paypalContainer").is(':checked')){
									currencySymbol = "<span >"+ $('input[name=paypal]:checked', '#paypalContainer').attr('currencysymbol') +"</span>"
									amountToCollect = YatraPayment.formatAmount((resp.redirectMap.amount/( $('input[name=paypal]:checked', '#paypalContainer').attr('forexrate'))).toFixed(2));
								}
								else{
									currencySymbol = "<span class='RupeeSign'>Rs</span>"
										amountToCollect = resp.redirectMap.amount;

								}

								var pricingErrorMessages=PaymentCommon.fetchPriceValidateErrorMessages(resp);
								$("#pricing-failure-error-message").html(template.render({newAmount:YatraHelper.formatPrice(amountToCollect),
									pricingErrorMessages:pricingErrorMessages,
									currencySymbol:currencySymbol}));

								PaymentCommon.showBlackDiv();
								$("#pricing_error_pop_up").css({"display":"block"});
								ProcessPayment.enablePayNow();
							}
							else if (resp.primeWarning) {
                                var w = new Template();
                                w.set("prime-warning-message-template");
                                $("#prime-failure-error-message").html(w.render({
                                    pricingErrorMessages: resp.primeErrorMessage
                                }));
                                PaymentCommon.showBlackDiv();
                                $("#prime_error_pop_up").css({
                                    display: "block"
                                });
                                ProcessPayment.enablePayNow()
                            }
							else {
								if(!payNowCallFromQRCode){
									YatraPaymentUI.hideSessionTimerPopup();
								}
								PaymentCommon.continueWithPayment(resp);
							}
						}
						//Handle Discounting(Visa) Fail Case
						else if(!resp.discountSuccess){
							ShoppingCart.updateVisaDiscount(0);
							PaymentCommon.onAmountChange();
							PaymentCommon.showBlackDiv();
							$("#invalid_visa_discount_pop_up").css({"display":"block"});
							ProcessPayment.enablePayNow();
						}
						else if(!resp.lobValidationSuccess){
							//ShoppingCart.updatePromocode(0);
							PaymentCommon.onAmountChange();
							var template = new Template();
							template.set("validate-error-message-template");

							var validateErrorMessages=PaymentCommon.fetchValidateErrorMessages(resp);
							$("#validate-error-message").html(template.render({validateErrorMessages:validateErrorMessages}));

							PaymentCommon.showBlackDiv();
							$("#validate_error_pop_up").css({"display":"block"});
							ProcessPayment.enablePayNow();
						}

						//Handle Promo Fail Case
						else if(!resp.promoSuccess){
							//ShoppingCart.updatePromocode(0);
							PaymentCommon.onAmountChange();
							var template = new Template();
							template.set("promo-code-failure-message-template");
							var promoAmount = detailsJson.promo.amount;

							 var  paymentMode = ProcessPayment.paymentMode;
							 if(paymentMode == "PARTIAL"){
								 promoAmount =  -parseInt(detailsJson.partialPayment.partialPromoAmount);
							 }
							 if(paymentMode == "HOLDANDPAY"){
								 promoAmount =  -parseInt(detailsJson.holdPayment.hnpPromoAmount);
							 }

							var promoAmountNumeric;
							var fullAmount;
							var currencySymbol;
							if(typeof promoAmount != "number"){
								promoAmountNumeric = parseInt(promoAmount);
							}
							else{
								promoAmountNumeric=promoAmount;
							}



							var payOption = YatraPayment.getPayOp();

							if(payOption=='paypal' && $("input[name=paypal]", "#paypalContainer").is(':checked')){
									currencySymbol = "<span >"+ $('input[name=paypal]:checked', '#paypalContainer').attr('currencysymbol') +"</span>"
									promoAmount = YatraPayment.formatAmount((promoAmount/( $('input[name=paypal]:checked', '#paypalContainer').attr('forexrate'))).toFixed(2));
									fullAmount = YatraPayment.formatAmount(((ShoppingCart.totalPayableAmount+promoAmountNumeric)/( $('input[name=paypal]:checked', '#paypalContainer').attr('forexrate'))).toFixed(2));
							}
						else{
								currencySymbol = "<span class='RupeeSign'>Rs</span>"
									promoAmount = promoAmount;
								fullAmount = ShoppingCart.totalPayableAmount+promoAmountNumeric;

							}

							YatraPaymentUI.adobeTrackPromoFaliure();

							var promoErrorMessages=PaymentCommon.fetchPromoErrorMessages(resp);
							$("#promo-code-failure-message").html(template.render({fullAmount:fullAmount,
							promoMessages:promoErrorMessages,
							promoAmount:promoAmount,
							currencySymbol:currencySymbol,
							gstMessage :  (resp.showGSTMessage) ?"and any applicable GST will also be adjusted":""}));

							if(resp.changePromoUrl && resp.changePromoUrl != "" ){

							}else{
								$("#ChangePromo").parent().hide();
							}
							PaymentCommon.showBlackDiv();
							$("#invalid_promo_code_discount_pop_up").css({"display":"block"});
							ProcessPayment.enablePayNow();
						}
						//Handle Change in Promo amount case
						else if(resp.promoSuccess && resp.promoWarning){
							//ShoppingCart.updatePromocode(0);
							PaymentCommon.onAmountChange();
							var template = new Template();
							template.set("promo-code-warning-message-template");
							//var promoAmount = detailsJson.promo.amount;
							var partPromoAmount = resp.promoAmount;
							var amountToCollect = resp.amountToCollect;
							var promoWarningMessage = resp.promoWarning;
							var currencySymbol;

							var payOption = YatraPayment.getPayOp();

							if(payOption=='paypal' && $("input[name=paypal]", "#paypalContainer").is(':checked')){
									currencySymbol = "<span >"+ $('input[name=paypal]:checked', '#paypalContainer').attr('currencysymbol') +"</span>"
									partPromoAmount = YatraPayment.formatAmount((partPromoAmount/( $('input[name=paypal]:checked', '#paypalContainer').attr('forexrate'))).toFixed(2));
									amountToCollect = YatraPayment.formatAmount((amountToCollect/( $('input[name=paypal]:checked', '#paypalContainer').attr('forexrate'))).toFixed(2));
							}
						else{
								currencySymbol = "<span class='RupeeSign'>Rs</span>"

							}
							YatraPaymentUI.adobeTrackPromoFaliure();


							$("#promo-code-warning-message").html(template.render({discountedAmount:amountToCollect,
							promoWarn:promoWarningMessage,
							promoAmount:partPromoAmount,
							currencySymbol:currencySymbol,
							gstMessage :  (resp.showGSTMessage) ?"and any applicable GST will also be adjusted":""}));


							PaymentCommon.showBlackDiv();
							$("#valid_warning_promo_code_discount_pop_up").css({"display":"block"});
							ProcessPayment.enablePayNow();
						}
						else{
							if(resp.message == undefined){
								resp["message"] = "Something went wrong."
							}
							$("#failed_msg_container").html(resp.message);
							$("#failed_payment_res").show();
							// Scroll to the container using smooth behavior
							var failedPaymentContainer = document.getElementById("failed_payment_res");
                             if (failedPaymentContainer) {
                             failedPaymentContainer.scrollIntoView({ behavior: "smooth" });
                            }
							ProcessPayment.enablePayNow();
						}


					} catch(e) {
						log_error(e);
					}
				},
				error : function(request, status, error) {
					alert("It seems there is some issue with the Site currently. Please try after some time.");
					ProcessPayment.enablePayNow();
					Popup.hidePopup();
				}
			});


			//pushing universal GA data
			if(detailsJson && (!$.isEmptyObject(detailsJson.gaResponse))){
			try{
				var paymentOption = ProcessPayment.getGAPaymentOption(payOption);
				var actionOptionValue = '{"option": "Pay Now : '+ paymentOption +'"}';
				var gaData = detailsJson.gaResponse;
				gaData.actionObj.option = actionOptionValue;
				if ( typeof yt_ga != 'undefined' && typeof yt_ga.addProduct != 'undefined') {
					yt_ga.addProduct(gaData);
				}

			}
			catch(e){
				console.log(e);
			}
		}
	},

	updatePayNowTrackingData : function(paymentOption) {
		try{
			var trackingData = {};
			trackingData.tenant = detailsJson.product;
			if(detailsJson.ci){
				trackingData.ci = detailsJson.ci;
			}
			if(detailsJson.uuid){
				trackingData.pricingId = detailsJson.uuid;
			}
			trackingData.ttv=detailsJson.totalAmout;
			trackingData.spnr=detailsJson.superPnr;
			trackingData.stage="payment";
			trackingData.paymentType=ProcessPayment.paymentMode;
			trackingData.convFee = PaymentCommon.getConvienceFee(paymentOption);
			trackingData.ttid=detailsJson.superPnr;
			trackingData.yaaction = "continue";
			trackingData.yaValue = "pay now";
			trackingData.referrerURL=document.referrer;
			trackingData.screenHeight=screen.height;
			trackingData.screenWidth=screen.width;
			var url ="/livedata-analytics/updateLiveData";
			YatraPaymentUI.sendLiveTrackingAjax(JSON.stringify(trackingData),url);
		}
		catch(e){
			console.log(e);
		}
	},

	getGAPaymentOption :  function(payOption) {
		if (payOption == 'cc') {
			paymentOption = "Credit Card";

		} else if (payOption == 'dc') {
			paymentOption = "Debit Card";

		} else if (payOption == 'nb') {
			paymentOption = "Net Banking";

		} else {
			paymentOption = "Others";
		}
		return paymentOption;
	},

	disablePayNow : function() {
		try {
			var continueDivId = '#' + getContinueButtonId();
			$(continueDivId).addClass('cpmt_disable_button');
			$(continueDivId).prop("disabled", true);
		} catch(e) {
			log_error(e);
		}
	},
	enablePayNow : function() {
		try {
			var continueDivId = '#' + getContinueButtonId();
			$(continueDivId).removeClass('cpmt_disable_button');

			if($(continueDivId).hasClass("emi-paynow-btn-disabled"))
			$(continueDivId).removeClass("emi-paynow-btn-disabled")

			$(continueDivId).prop("disabled", false);
		} catch(e) {
				log_error(e);
		}
	},
	createPayNowForm : function (){
		var payNowParams = {};
		/*if(redirectToLOB != undefined && redirectToLOB != null)
			redirect = "false";
		else redirect = "true";

		payNowParams["redirectToLOB"] = redirect;*/
		payNowParams["paymentOptionParameters"] = YatraPayment.getPaymentOptionParameters("paymentForm");
		payNowParams["amountDisplayed"] = (detailsJson.payLaterFlow && detailsJson.payLaterFlow == "true") ? 1 : ((detailsJson.currencyName && detailsJson.currencyName != "INR"  && !internationalCurrency && detailsJson.currencyConversionRate) ? detailsJson.totalAmount : (detailsJson.isTcsEnabled ? ( Ewallet.ecashRedeemedAmount > 0 ? ShoppingCart.totalPayableAmount : (GV.totalGVAmount() > 0  ?GV.balanceAmountAfterGV :ShoppingCart.getTotalCartAmount() + Number(ShoppingCart.tcsAmount))) : ShoppingCart.totalPayableAmount));
		payNowParams["superPnr"] = detailsJson.superPnr;
		payNowParams["ttid"] =  detailsJson.ttid;
		payNowParams["paymentMode"] =  ProcessPayment.paymentMode;
		payNowParams["client"] = client;
    	payNowParams["card_bin_type"] = cardBinTypeFromDB;
		payNowParams["cardDiscountingStatus"] = cardDiscountingStatus;
		payNowParams["otherDiscountingStatus"] = otherDiscountingStatus;
		payNowParams["additionalPayment"] = isAdditionalPaymentAttempt;
		payNowParams["ylp"] = YatraEWalletPaymentUI.getYLP();
		payNowParams["paymentProcessType"] = YatraPaymentUI.getMultiPayOpt();
		payNowParams["cvvRequired"] = YatraPaymentUI.cvvRequired;

		if(YatraPayment.getPayOp() == "emi")
			payNowParams["ncemi"] = YatraPaymentUI.isNoCostEmiFlag;

		if(GV && GV.voucherNodes){
			payNowParams["vouchers"] = JSON.stringify(GV.voucherNodes);
			payNowParams["voucherAmount"] = GV.totalGVAmount();
			payNowParams["isPGPaymentRequiredForVoucher"] = GV.isPGPaymentRequiredForVoucher;

		}

        if(YatraEWalletPaymentUI.isBookingForEcashGV() && redeemAuthJSON == null) {
            var paymentJSON = getPaymentJSON();
            payNowParams["amountToRedeem"] = paymentJSON.wallet.maxRedeemableECash;
            var userDetails = detailsJson.userDetails;
            if(userDetails != undefined && userDetails != null) {
                payNowParams["email"] = userDetails.email;
            }
        }
		if(typeof(uuid) != "undefined" && uuid != undefined && uuid != null)
			payNowParams["uuid"] = uuid;

		payNowParams["product"] = detailsJson.product;
		if(detailsJson.promo && ProcessPayment.proceedWithPromo &&
			(ShoppingCart.json[ShoppingCart.labelIndex[detailsJson.promo.cartLabel]][detailsJson.promo.cartLabel]!=0 || (detailsJson.promo.ecashAmount && detailsJson.promo.ecashAmount>0))){
			payNowParams["promoCode"] = detailsJson.promo.code;
		}
		var paynowForm = document.createElement("form");
		paynowForm.setAttribute("method", "post");
		for(var key in payNowParams) {
		       if(payNowParams.hasOwnProperty(key)) {
		           var hiddenField = document.createElement("input");
		           hiddenField.setAttribute("type", "hidden");
		           hiddenField.setAttribute("name", key);
		           hiddenField.setAttribute("value", payNowParams[key]);
                   paynowForm.appendChild(hiddenField);
		         }
		    }
		    document.body.appendChild(paynowForm);
		    return paynowForm;
	}
};

PaymentCommon={
		isVisaApplied : function(){
			if((!$.isEmptyObject(detailsJson.discount))
					&& (detailsJson.discount.type.toUpperCase() == "VISA" || detailsJson.discount.type.toUpperCase() == "CARD")
					/*&& ShoppingCart.json[ShoppingCart.labelIndex[detailsJson.discount.cartLabel]][detailsJson.discount.cartLabel] != 0*/){
				return true;
			}
			else{
				return false;
			}


		},

		isDiscountApplied : function(){
			if((!$.isEmptyObject(detailsJson.discount))
					&& (detailsJson.discount.type.toUpperCase() != "VISA" && detailsJson.discount.type.toUpperCase() != "CARD")
					/*&& ShoppingCart.json[ShoppingCart.labelIndex[detailsJson.discount.cartLabel]][detailsJson.discount.cartLabel] != 0*/){
				return true;
			}
			else{
				return false;
			}
		},

		isPayOpValidForVisa : function() {
			var payOp = YatraPayment.getPayOp();
			if(payOp == "cc" || payOp == "dc" || payOp =="qb"){
				return true;
			}
			return false;
		},

		handleVisa : function(validPayOp,messageDiv){
			var paymentMode = $('input:radio[name="payOption"]:checked').val();
			if(paymentMode == "payPartial"){
				validPayOp=false;
				messageDiv = "partial_payment_visa_removed_pop_up";
			}
			// for visa discount -> not to give visa dicount when payable amount is less than visa discount
			var totalCartAmount = ShoppingCart.getTotalCartAmount();
			if(validPayOp){
			if(detailsJson.discount && detailsJson.discount.type && (detailsJson.discount.type.toUpperCase() == "VISA" || detailsJson.discount.type.toUpperCase() == "CARD")
				&& ShoppingCart.json[ShoppingCart.labelIndex[detailsJson.discount.cartLabel]][detailsJson.discount.cartLabel] == 0	&& totalCartAmount > detailsJson.discount.amount) {
					var topOffset = $(".cpmt_Paymentdiv").offset().top;
					messageDiv = "visa_discount_applied_pop_up";
					PaymentCommon.displayPartialVisaFailureMessage(messageDiv,topOffset);
					ShoppingCart.updateVisaDiscount("-"+detailsJson.discount.amount);
					cardDiscountingStatus = "true";
					return;
				}
			else if(detailsJson.discount && detailsJson.discount.type &&
					ShoppingCart.json[ShoppingCart.labelIndex[detailsJson.discount.cartLabel]][detailsJson.discount.cartLabel] == 0	&& totalCartAmount > detailsJson.discount.amount) {
					messageDiv = "other_discount_applied_pop_up";
						var topOffset = $(".cpmt_Paymentdiv").offset().top;
						PaymentCommon.displayPartialVisaFailureMessage(messageDiv,topOffset);
						ShoppingCart.updateOtherDiscount("-"+detailsJson.discount.amount);
						otherDiscountingStatus = "true";
						return;
					}
			}
			else{
				if(paymentMode == "payFull"){
					messageDiv = "payop_changed_visa_discount_pop_up";
					if(PaymentCommon.isDiscountApplied()) {
						messageDiv = "other_discount_applied_pop_up";
						var topOffset = $(".cpmt_Paymentdiv").offset().top;
						PaymentCommon.displayPartialOtherFailureMessage(messageDiv,topOffset);
						ShoppingCart.updateOtherDiscount("-"+detailsJson.discount.amount);
						otherDiscountingStatus = "true";
					}
					if(PaymentCommon.isVisaApplied()){
						var topOffset = $(".cpmt_Paymentdiv").offset().top;
						PaymentCommon.displayPartialVisaFailureMessage(messageDiv,topOffset);
					cardDiscountingStatus = "false";
					ShoppingCart.updateVisaDiscount(0);
					}
				}
				else {
				if(PaymentCommon.isVisaApplied()){
					var topOffset = $(".cpmt_Paymentdiv").offset().top;
					PaymentCommon.displayPartialVisaFailureMessage(messageDiv,topOffset);
				cardDiscountingStatus = "false";
				ShoppingCart.updateVisaDiscount(0);
				}
				else if(PaymentCommon.isDiscountApplied()) {
					messageDiv = "partial_payment_discount_removed_pop_up";
					var topOffset = $(".cpmt_Paymentdiv").offset().top;
					PaymentCommon.displayPartialOtherFailureMessage(messageDiv,topOffset);
				otherDiscountingStatus = "false";
				ShoppingCart.updateOtherDiscount(0);
				}
			} }
			//PaymentCommon.onAmountChange();
		},

		getConvienceFee : function(paymentOption){
			if(!detailsJson.convFee) return 0;
			var convFeeNode = detailsJson['convFee'];

			var partialPayEnable = PaymentMode.partialEnabledForProduct && partial_payOpArr.indexOf(paymentOption)>= 0 ;
			var holdAndPayEnable = PaymentMode.holdAndPayEnabledForProduct && holdAndPay_payOpArr.indexOf(paymentOption)>= 0 ;

			if(detailsJson.useModeSpecificConvFee == 'true' && (partialPayEnable ||  holdAndPayEnable)){
				var payMode = $('input:radio[name="payOption"]:checked').val();
				if(payMode == 'payPartial' && detailsJson.convFee_partial) convFeeNode =detailsJson['convFee_partial'];
				else if (payMode == 'holdAndPay' && detailsJson.convFee_hnp) convFeeNode = detailsJson['convFee_hnp'];
			}
			return convFeeNode[paymentOption]? parseFloat(convFeeNode[paymentOption]): parseFloat(convFeeNode["default"]);
		},

		getPayAtHotel : function(){
			if(!detailsJson.payAtHotel) return 0;
			return parseFloat(detailsJson.payAtHotel);
		},
		getConvFeeTaxes : function(){
			if(!detailsJson.convenienceFeeTaxes) return 0;
			return parseFloat(detailsJson.convenienceFeeTaxes);
		},
		getConvFeeGST : function(){
			if(!detailsJson.convFeeGST) return 0;
			return parseFloat(detailsJson.convFeeGST);
		},
		changePayNowLabel : function(_product){
			//in case of booking.com hotel, pay now button to be renamed to Confirm Booking
			if("INT_HOTEL_BKG" == _product) {
				$('#payNow').val('Confirm Booking');
				$('.contiue-section .mobilePay').addClass('hideForHotel');
				$('body').addClass('pah-hack-only');
				$('#tcStrip_id').text("Confirm Booking");
			}
			else {
				if(ShoppingCart.totalPayableAmount==0){
					// handling the GV show and hide of pay now button here as this function will be often called
					$("#"+getContinueButtonId()).css('display', 'inline-block');
					$("#"+getContinueButtonId()).val('Complete Booking');
					$('#tcStrip_id').text("Complete Booking");

					$("#gv-promo-msg-completeBooking").html(" To use any other payment method, please remove voucher(s) ")


					if (enableEWallet && eCashJSON != null && eCashJSON.status == true) {
						var totalRedeemableEcash = YatraEWalletPaymentUI.getAmountInRupee(eCashJSON.totalEcash.redeemableECashInPaisa);
						if (parseInt(totalRedeemableEcash) > 0) {
							// if ecash is already reddemed then show ecash div else hide it
							var redeemedAmount = YatraEWalletPaymentUI.getAmountInRupee(redeemAuthJSON?redeemAuthJSON.amountInPaisa?redeemAuthJSON.amountInPaisa:0:0);
							if(parseInt(redeemedAmount) > 0){
								$("#ecash-div").show();
								$("#gv-promo-msg-completeBooking").html("To use any other payment method, please remove eCash or Gift Voucher(s)");
							}else{
								$("#ecash-div").hide();
							}
						}
				}
					if(YatraPayment.getPayOp() == "gv"){
						$("#gv-promo-msg-completeBooking").show()
					}
				}
				else{
					$("#gv-promo-msg-completeBooking").hide()
					if(YatraPayment.getPayOp() == "gv"){
						$("#"+getContinueButtonId()).hide()
					}
					$("#"+getContinueButtonId()).val('Pay Now');
					this.setPayNowLabelforCorp(_product)
					$('#tcStrip_id').text("Pay Now");
					// cart amount is more than 0 and echash is appllicable thenn show it
					if (enableEWallet && eCashJSON != null && eCashJSON.status == true) {
						var totalRedeemableEcash = YatraEWalletPaymentUI.getAmountInRupee(eCashJSON.totalEcash.redeemableECashInPaisa);
						if (parseInt(totalRedeemableEcash) > 0 && disableEcash == false) {
					        $("#ecash-div").show();
						}
			}
				}
			}
		},
		//set book now for all corporate product
		setPayNowLabelforCorp : function(_product){
			if(_product == 'crpdom'|| _product == 'crpint'|| _product == 'crpdom_hotel'|| _product == 'crpint_hotel'|| _product == 'crpinthotel'|| _product == 'crpdomhotel'|| _product == 'cinsweb'|| _product == 'corpInsurance'|| _product == 'corpSAPG'|| _product == 'crp'|| _product == 'corpbusdesktop'){
				$("#"+getContinueButtonId()).val('Book Now');
			}else{
				$("#"+getContinueButtonId()).val('Pay Now');
			}
		},
		showBlackDiv: function(){
			$("body").addClass("show-promo-pop-body");
			$(".black_div_pop").show();
		},
		hideBlackDiv: function(){
			$("body").removeClass("show-promo-pop-body");
			$(".black_div_pop").hide();
		},
		fetchPromoErrorMessages : function(resp){
			var messages = resp.promoMessages;
			var numberOfMessages = messages.length;
			var innerHtml = "";
			for(var i=0;i<numberOfMessages;i++){
				innerHtml+="<p> ";
				innerHtml+=messages[i];
				innerHtml+="</p>";
			}
			return innerHtml;
		},
		fetchValidateErrorMessages : function(resp){
			var messages = resp.lobValidationMessage;
			var numberOfMessages = messages.length;
			var innerHtml = "";
			for(var i=0;i<numberOfMessages;i++){
				innerHtml+="<p> ";
				innerHtml+=messages[i];
				innerHtml+="</p>";
			}
			return innerHtml;
		},

		fetchPriceValidateErrorMessages : function(resp){
			var messages = resp.priceValidationMessage;
			var numberOfMessages = messages.length;
			var innerHtml = "";
			for(var i=0;i<numberOfMessages;i++){
				innerHtml+="<p> ";
				innerHtml+=messages[i];
				innerHtml+="</p>";
			}
			return innerHtml;
		},

		continueWithPayment: function(resp){
			try{
				if(resp.ajax)// handle case where payment is to be initiated in ajax
				{
					if( resp.redirectMap && (resp.redirectMap.walletService == "PTM" || resp.redirectMap.bankCode == "PTM"))
						PaymentCommon.loadPaytm(resp);
					else
						PaymentCommon.postPaymentAjax(resp);
				}
				else
					PaymentCommon.generateFormAndPost(resp.rurl,resp.redirectMap);
			}catch(err){
				console.log(err);
				PaymentCommon.sendJSerror(err , resp.redirectMap.ttid);
			}
		},
		onScriptLoad : function(info){
			// info.mtxnid = "123";
			// info.txnToken ="456";

			var config = {
			  "root": "",
			  "flow": "DEFAULT",
			  "data": {
			  "orderId": info.mtxnid, /* update order id */
			  "token": info.txnToken, /* update token value */
			  "tokenType": "TXN_TOKEN",
			  "amount": info.amount /* update amount */
			  },
			  "handler": {
				"notifyMerchant": function(eventName,data){
				//   console.log("notifyMerchant handler function called");
				//   console.log("eventName => ",eventName);
				//   console.log("data => ",data);
				  if(eventName == "APP_CLOSED"){
					  ProcessPayment.enablePayNow();
				  }
				}
			  }
			};

			if(window.Paytm && window.Paytm.CheckoutJS){
				window.Paytm.CheckoutJS.onLoad(function excecuteAfterCompleteLoad() {
					// initialze configuration using init method
					window.Paytm.CheckoutJS.init(config).then(function onSuccess() {
						// after successfully updating configuration, invoke JS Checkout
						window.Paytm.CheckoutJS.invoke();
					})
					// .catch(function onError(error){
					// 	console.log("error => ",error);
					// });
				});
			}
		},

		loadPaytm : function(data){
			var script = document.createElement("script")
			script.type = "application/javascript";
			script.crossOrigin = "anonymous"

			if (script.readyState){  //IE
				script.onreadystatechange = function(){
					if (script.readyState == "loaded" ||
							script.readyState == "complete"){
						script.onreadystatechange = null;
						PaymentCommon.onScriptLoad(data.redirectMap);
					}
				};
			} else {  //Others
				script.onload = function(){
					PaymentCommon.onScriptLoad(data.redirectMap);
				};
			}

			script.src = "https://secure.paytmpayments.com/merchantpgpui/checkoutjs/merchants/" + data.redirectMap.mid +".js";
			document.getElementsByTagName("head")[0].appendChild(script);


		},
		generateFormAndPost : function (path, params) {
		    var form = document.createElement("form");
		    form.setAttribute("method", "post");
		    form.setAttribute("action", path);

		    for(var key in params) {
		        if(params.hasOwnProperty(key)) {
		            var hiddenField = document.createElement("input");
		            hiddenField.setAttribute("type", "hidden");
		            hiddenField.setAttribute("name", key);
		            hiddenField.setAttribute("value", params[key]);

		            form.appendChild(hiddenField);
		         }
		    }
		    document.body.appendChild(form);
		    form.submit();
		},

		postPaymentAjax : function (swiftResp) {
			try{
			// show popup but not for QR code
			if(!payNowCallFromQRCode){
				if(resp.googlePay)// handle case where payment is to be initiated in ajax
					Popup.showPopup("Enter mpin in your Google Pay app to authorize payment.","Payment Initiated");
				else if (resp.cred)
					Popup.showPopup("A notification has been sent to your mobile phone. Please complete payment on Cred App","Payment Initiated");
				else
					Popup.showPopup("Enter mpin in your UPI app to authorize payment.","Payment Initiated");
			}
			// post Payment
			$.ajax({
				url : swiftResp.rurl,
				type: 'POST',
				cache: false,
				data: swiftResp.redirectMap,
				success : function(response) {
					try{
						response = JSON.parse(response);
					}catch(err){
						console.log("Error reading Txn Init Response");
					}
					if(response.suc && response.suc =="true"){
						if(payNowCallFromQRCode){
							 ProcessPayment.getQRCode(response.mtxnid, response.uid, response, swiftResp)
						}else{
							PaymentCommon.handlePaymentInitResponse(response, swiftResp);
						}
					} else PaymentCommon.handlePaymentInitResponse(response, swiftResp);
				},
				error : function(request, status, error) {
					PaymentCommon.handleErrorRedirect(swiftResp.failRedirectMap);
				}
			});
		}catch(err){
			console.log(err);
			PaymentCommon.sendJSerror(err ,swiftResp.redirectMap.ttid);

		}

		},

		handlePaymentInitResponse:  function(response, initData){
			try {

			if(response){
				if(response.suc && response.suc =="true"){
					var pollInterval = 10*1000;
					var pollingStopDuration = 120 + (2*10*1000) ;
					if(response.pollIntervalInSec && response.payByDurationInSec){
						var payByDurationInSec = response.payByDurationInSec;
						pollInterval = 	response.pollIntervalInSec * 1000;
						pollingStopDuration = (payByDurationInSec + 2*(pollInterval))*1000;
					}

					var pollPayStartTime = new Date().getTime();
					pollPayStatInterval = setInterval(function(){
						//forceStop Polling
						if(forceStopPolling){
					        clearInterval(pollPayStatInterval);
					        payNowCallFromQRCode = false;
					         return;
					    }
						// stop the loop after pay by duration is passed
					    if((new Date().getTime() - pollPayStartTime) > pollingStopDuration){

					        clearInterval(pollPayStatInterval);
					        payNowCallFromQRCode = false;
					        PaymentCommon.handleErrorRedirect(initData.failRedirectMap);
					        return;
					    }

					    // poll for txn status
					    initData.redirectMap.mtxnid = response.mtxnid;
					    initData.pollPayStatInterval = pollPayStatInterval;
					    PaymentCommon.postPollStatusAjax(initData);

					}, pollInterval);
				}
				else if(response.suc && response.suc =="false"){
					PaymentCommon.generateFormAndPost(response.redirectURL,response.fieldMap );
				}
				return;
			}
			handleErrorRedirect(initData.failRedirectMap);
		} catch(err){
			console.log(err);
			PaymentCommon.sendJSerror(err , initData.redirectMap.ttid);

		}
		},

		handleErrorRedirect: function(redirectMap) {
			var url = redirectMap.rurl;
			delete redirectMap['rurl'];
			PaymentCommon.generateFormAndPost(url,redirectMap );
		},

		postPollStatusAjax : function (initData) {
			$.ajax({
				url : initData.pollStatusUrl,
				type: 'POST',
				cache: false,
				data: initData.redirectMap,
				success : function(response) {
					PaymentCommon.handlePaymentStatusResponse(response, initData.pollPayStatInterval);
				},
				error : function(request, status, error) {
					// do nothing
				}
			});

		},

		handlePaymentStatusResponse : function(response, pollPayStatInterval) {
			try{
				response = JSON.parse(response);
			}catch(err){
				console.log("Error reading Txn Status Response");
			}
			if(response && response.txnComplete=="true" ){
				clearInterval(pollPayStatInterval);
				PaymentCommon.generateFormAndPost(response.rurl,response.fieldMap);
			}
		},


		paymentTimer: function(d, c) {
	        var f = d, a, e;
	        var b = 0;
	        setInterval(function() {
	            timeLeft = f;
	            a = parseInt(f / 60, 10);
	            e = parseInt(f % 60, 10);
	            a = a < 10 ? "0" + a : a;
	            e = e < 10 ? "0" + e : e;
	            c.text(a + ":" + e);
	            if (--f < 0) {
	                f = d;
	            }
	            if (b == timeLeft) {
	            	Popup.showSessionExpire();
	            	setTimeout(function(){ PaymentCommon.sessionEndRedirect(); }, 800);
	            }
	        }, 1000);
	    },
	    showTimer: function(time) {
	        $("#time-label").show();
	        var a = 60 * time , b = $("#time");
	        this.paymentTimer(a, b);
	    },
	    sessionEndRedirect : function (){
	    	PaymentCommon.generateFormAndPost(detailsJson.session.rurl,detailsJson.session.params);
	    },

		displayPartialVisaFailureMessage : function(visaMessageDiv,topOffset){
		$(".payment_popUp").hide();
		$("#"+visaMessageDiv).css("top",topOffset).show();
	//	setTimeout(function(){$("#"+visaMessageDiv).hide();}, 7000);
		},

		displayPartialOtherFailureMessage : function(otherMessageDiv,topOffset){
			$(".payment_popUp").hide();
			$("#"+otherMessageDiv).css("top",topOffset).show();
		//	setTimeout(function(){$("#"+visaMessageDiv).hide();}, 7000);
			},

		handleVisaDiscountCommon : function(cardData,topOffset,payOp){
			var discountAmount;
			var totalCartAmount = ShoppingCart.getTotalCartAmount();
			// checking condition to call api or not
			var conditionToHandleVisaDiscount
			// if visa discount is already applied // should be called to remove it
				if(detailsJson.discount && detailsJson.discount.cartLabel && ShoppingCart.json[ShoppingCart.labelIndex[detailsJson.discount.cartLabel]][detailsJson.discount.cartLabel] != 0){
					// nothing maters it should be true
					conditionToHandleVisaDiscount = true;
				} else {
					// now visa discount is not applied, check if cart value is more than visa discount to make condition true
					if(detailsJson && detailsJson.discount && detailsJson.discount.amount)
					conditionToHandleVisaDiscount = totalCartAmount > detailsJson.discount.amount?true: false;
					else
						conditionToHandleVisaDiscount = true;
				}

			if(detailsJson.discount && cardData && (!$.isEmptyObject(detailsJson.discount))){
				if((detailsJson.discount.type.toUpperCase() == "VISA" || detailsJson.discount.type.toUpperCase() == "CARD") && conditionToHandleVisaDiscount){
					var visaMessageDiv;
					$.ajax({url:"/PaySwift/Discounting/validateVisaCard.htm",type : 'POST',async: false,timeout:"5000",data : "cardData="+cardData +"&ttid="+ detailsJson.superPnr +"&product="+ detailsJson.product+"&payop="+payOp,success : function(resp){
						 // handle VISA Response here .
			        	  if(resp.status.toUpperCase() == "TRUE"){
			        		  if(detailsJson.discount && ShoppingCart.json[ShoppingCart.labelIndex[detailsJson.discount.cartLabel]][detailsJson.discount.cartLabel] == 0)
			        		  {
			        			  visaMessageDiv="valid_partial_visa_discount_pop_up";
			        		  }
			        		  if(ShoppingCart.totalPayableAmount < detailsJson.discount.amount && Ewallet.ecashRedeemedAmount>0){
			        			  Ewallet.showAlert();
			        			  YatraEWalletPaymentUI.reverseAuth();
			        		  }
			        		  discountAmount = detailsJson.discount.amount;
							  ShoppingCart.updateVisaDiscount("-"+discountAmount);
							  cardDiscountingStatus = resp.status.toLowerCase();
							  GV.maintainState= false;
			        	  }
			        	  else{
			        		  if(detailsJson.discount && ShoppingCart.json[ShoppingCart.labelIndex[detailsJson.discount.cartLabel]][detailsJson.discount.cartLabel] != 0)
			        		  {
			        			  visaMessageDiv="invalid_partial_visa_discount_pop_up";
			        		  }
			        		  discountAmount = 0;
			        		  ShoppingCart.updateVisaDiscount(discountAmount);
				        	  cardDiscountingStatus = "false";
				        	  if(GV.gv)
					        	  GV.maintainState= true;
					        	  else
					        		  GV.maintainState= false;

			        	  }
			        	  PaymentCommon.displayPartialVisaFailureMessage(visaMessageDiv,topOffset);
			        	  var isGSTEnabled = YatraPaymentUI.isGSTEnabled(detailsJson.isGSTEnabled);
			        	  if(isGSTEnabled){
			        		  var promoAmount = 0;
			        		  if(typeof(detailsJson.promo) != "undefined" && detailsJson.promo != undefined)
			        			  promoAmount = ShoppingCart.json[ShoppingCart.labelIndex[detailsJson.promo.cartLabel]][detailsJson.promo.cartLabel];

			        		  currentGSTAmount = ShoppingCart.json[ShoppingCart.labelIndex['GST']]["GST"];
                              prevGSTAmount = currentGSTAmount;
                              var prevTotalAmountPayable = ShoppingCart.totalPayableAmount;

			        		  YatraPaymentUI.updateGSTCartUI(Number(YatraEWalletPaymentUI.getYLP()) + Number(Math.abs(promoAmount)) + Number(Math.abs(discountAmount)));

			        		  YatraPaymentUI.ewalletRedemptionUpdateOnGSTIncrease(prevTotalAmountPayable, prevGSTAmount, currentGSTAmount)
			        	  }
			        	  PaymentCommon.onAmountChange();
					}});
				}
			}
		},

		onAmountChange : function(){
			ShoppingCart.update();
			YatraPaymentUI.onAmountChange();
			PaymentCommon.changePayNowLabel(detailsJson.product);
			YatraPaymentUI.setElementText('partialPayNowAmount', ShoppingCart.totalPayableAmount);
		},

		sendJSerror : function(err , ttid){
			console.log(err , ttid)
			$.ajax({
				url : "/PaySwift/trackUPIJSError?ttid="+ttid+"&description="+err.toString(),
				type : 'GET',
				async: false,
				timeout:"5000",
				cache : false,
				success : function(response) {
					console.log(response)
				}, error : function(error) {
				console.log("error",error )
				}
			});
		}
};

ShoppingCart = {
		json : [],
		labelIndex:{},
		rowTemplateId : "rowTemplate",
		rowTemplateString:"rowTemplateString",
		rowTemplateStringWithInfo : "rowTemplateStringWithInfo",
		rowTemplateWithDiv:"rowTemplateWithDiv",
		rowTemplateWithDivWithInfo:"rowTemplateWithDivWithInfo",
		rowTemplateInsideDiv:"rowTemplateInsideDiv",
		breakDownContainer : "breakDownContainer",
		totalAmountSpan : "totalAmountSpan",
		totalBookingSpan : "totalBookingSpan",
		payAtHotelSpan : "payAtHotelSpan",
		advanceAmountSpan : "advanceAmountSpan",
		totalAmtInPayBlk : "totalAmountFix",
		fareElementId : "paymentDetailsCont",
		excludedChargesContId : "excludedChargesCont",
		excludedChargesRow :"excludedChargesRow",
		itineraryHTMLContainer : "itineraryHTMLContainer",
		itineraryDetailHeader : "itineraryDetailHeader",
		paxDetailContainer : "show-paxDetails-block",
		totalPayableAmount : 0,
		tcsAmount : 0,
		intialise : function (){
			//handeling for ecash container
			/*if(detailsJson.earnEcash != "undefined"){
				if(detailsJson.earnEcash && detailsJson.earnEcash>0){
				   var ecashAmt=detailsJson.earnEcash;
				   $('#ecash_container').removeClass('hide');
				   $("#ecashAmount").html(YatraHelper.formatPrice(ecashAmt));
				}
			}*/

			/*$("#partialPaymentDiv input").on("change",function() {
				var val = $("#partialPaymentDiv input:checked").val();
				if(val == "holdAndPay") {
					if((typeof detailsJson.holdPayment != "undefined") && (typeof detailsJson.holdPayment.earnEcash != "undefined")) {
						var ecashAmt=detailsJson.holdPayment.earnEcash;
						$('#ecash_container').removeClass('hide');
						$("#ecashAmount").html(YatraHelper.formatPrice(ecashAmt));
					}
				} else if(val == "payPartial") {
					if((typeof detailsJson.partialPayment != "undefined") && (typeof detailsJson.partialPayment.earnEcash != "undefined")) {
						var ecashAmt=detailsJson.partialPayment.earnEcash;
						$('#ecash_container').removeClass('hide');
						$("#ecashAmount").html(YatraHelper.formatPrice(ecashAmt));
					}
				} else if(val == "payFull"){
					if(typeof detailsJson.earnEcash != "undefined") {
						var ecashAmt=detailsJson.earnEcash;
						$('#ecash_container').removeClass('hide');
						$("#ecashAmount").html(YatraHelper.formatPrice(ecashAmt));
					}
			   }
			});
			$("#partialPaymentDiv input").trigger('change');
			*/

            Ewallet.maxRedeemableECash = (detailsJson.maxRedeemableECash)?parseFloat(detailsJson.maxRedeemableECash):((detailsJson.walletDetails) && (detailsJson.walletDetails.maxRedeemableECash))?parseFloat(detailsJson.walletDetails.maxRedeemableECash):0;
			this.generateFareBreakupArray();
			this.addConvFee();
			this.addConvFeeTax();
			if(detailsJson.convFeeGST)
				this.addConvFeeGST();
			this.update();
			$("#"+this.fareElementId).show();
			this.generateExcludedCharges();
			this.showExcludedChargesForCorporate();


			if(detailsJson.itenaryHtml && !(detailsJson.product == "HOTEL_IN" || detailsJson.product == "homestays" || detailsJson.product == "INT_HOTEL_BKG" || detailsJson.product == "tghomestays" || detailsJson.product == "tgdomhotel")){
				$("#"+this.itineraryDetailHeader).show();
				$("#"+this.itineraryHTMLContainer).html(detailsJson.itenaryHtml.replace(/[\n\t\r]/g,"").replace(/\\"/g, '"'));
				try{
				$('#containerScroll').tinyscrollbar();
				}catch(e){}
			}else{
				//IF ITS HOTELS AND HAS NEW ui THEN HIDE ELSE SHOW AS IT IS
				if(detailsJson.itenaryHtml && !$("body").hasClass("no-broadCast")){
					$("#"+this.itineraryDetailHeader).show();
					$("#"+this.itineraryHTMLContainer).html(detailsJson.itenaryHtml.replace(/[\n\t\r]/g,"").replace(/\\"/g, '"'));
					try{
					$('#containerScroll').tinyscrollbar();
					}catch(e){}
				}else{
					$("#"+this.itineraryDetailHeader).hide();
				}


			}

			if(detailsJson.paxDetails){
				$("."+this.paxDetailContainer).show();
				$("."+this.paxDetailContainer).html(detailsJson.paxDetails.replace(/[\n\t\r]/g,"").replace(/\\"/g, '"'));
			}
			if(detailsJson.session && detailsJson.session.exp && detailsJson.session.exp=='true'){
				PaymentCommon.showTimer(detailsJson.session.timer);
			}
			if(detailsJson.step1Url){
				var template = new Template();
				template.set("editStep");
				$("#spanEditStep1").html($("#spanEditStep1").html()+" "+template.render({url:detailsJson.step1Url}));
			}
			if(detailsJson.step2Url){
				var template = new Template();
				template.set("editStep");
				$("#spanEditStep2").html($("#spanEditStep2").html()+" "+template.render({url:detailsJson.step2Url}));
			}
			if(detailsJson.tncUrl){
				$("#tncLink").attr("href", detailsJson.tncUrl);
			}
			if(messageForDisplay){
				$("#failed_msg_container").html(messageForDisplay);
				$("#failed_payment_res").show();
			}
			if(detailsJson.isPassthrough){
				$("#passthroughDiv").show();
			}
			if(detailsJson.merchant && detailsJson.merchant=='travelguru'){
				$('.pciDjj').hide();
			}
			if(!$.isEmptyObject(detailsJson.discount)){
				if(detailsJson.discount.type.toUpperCase() == "VISA" || detailsJson.discount.type.toUpperCase() == "CARD")
					cardDiscountingStatus = true;
				else otherDiscountingStatus = true;
			}
		},
		update : function (){
			// force stop the polling
			//ProcessPayment.stopPollingForQRCode();

			this.totalPayableAmount = Number(0);
			this.payAtHotelAmount = Number(0);
			var template = new Template();
			template.set(this.rowTemplateId);
			var currencySymbol = "Rs.";
			if(detailsJson.curcd) {
			    var currency = detailsJson.curcd;
			    if('usd' == currency.toLowerCase()) {
			        $("span").removeClass('RupeeSign');
			        $(".curcd").text("$");
			    }
			    if('sgd' == currency.toLowerCase()) {
                    $("span").removeClass('RupeeSign');
                    $(".curcd").text("S$");
                }
			}

			// Hanlding the holidays conv fees in percentage
			ShoppingCart.handleConvFeesPercentage();

			if(detailsJson.currencyName && detailsJson.currencyName != "INR" && detailsJson.currencyConversionRate && !internationalProduct){
				$(".newCurr").removeClass('RupeeSign');
                $(".newCurr").text(detailsJson.currencyName + " ");
                $(".exchangeRatePayNow").show()
                $("#exchangeCurrencyPayNow").html(detailsJson.currencyName);
                $("#exchangeRatePayNow").html(detailsJson.currencyConversionRate);
                $(".totalPayInf .totalPrice small").css('width', '70%');
                $(".totalPrice .you-pay-txt").css('width', '28%');
			    if(!internationalProduct){
                   $(".exchangeRatePayNow").show()
			      }
			}


			var templateString = new Template();
			templateString.set(this.rowTemplateString);

			var templateStringWithInfo = new Template();
			templateStringWithInfo.set(this.rowTemplateStringWithInfo);

			var html = "";
	        for(var i in ShoppingCart.json){
	        	for(var key in ShoppingCart.json[i]){
	        		if(key == 'Convenience Fee'){
	        			$('#confeeId').html(ShoppingCart.json[i][key]);
	        		}
		        	var value = ShoppingCart.json[i][key];
					if(detailsJson.isTcsEnabled && key == "Voucher Redeemed (-)"){
						continue ;
					}
		        	//In case of hotel where fareBreak Up would contain expandable/scrollable div, amount would be Json Object instead of String
		        	if(value!=0 && typeof value !="object"){
		        		if(!isNaN(value)){
		        			if((key == "Hotel Taxes" && detailsJson.adobeTrackPaymentLoad &&  detailsJson.adobeTrackPaymentLoad.hotel && detailsJson.adobeTrackPaymentLoad.hotel.hotelInfo && detailsJson.adobeTrackPaymentLoad.hotel.hotelInfo.supplierDetails == "EXP_DOM") || (key == "Yatra Promo Offer (-)" && detailsJson.fareBreakup && detailsJson.fareBreakup.messages && detailsJson.fareBreakup.messages.adjConvFeeAndTax ) ){
		        				var message =  (key == "Yatra Promo Offer (-)" && detailsJson.fareBreakup && detailsJson.fareBreakup.messages && detailsJson.fareBreakup.messages.adjConvFeeAndTax) ? detailsJson.fareBreakup.messages.adjConvFeeAndTax : "Tax Recovery Charges and Service Fees"
		        				html += templateStringWithInfo.render({label:key,amount:Math.abs(value), message:message});
		        			}else{
		       		        			html += template.render({label:key,amount:Math.abs(value)});
		        			}
		        		}
		        		else if(value!=''){
		        			html += templateString.render({label:key,amount:value});
		        		}
		        	}
		        	else if(typeof value != "object" && value == 0 && key == "Convenience Fee & Taxes"){
                            if(detailsJson.product == "HOTEL_IN"){
                               if (!isNaN(value)) {
                                  html += template.render({label:key,amount:Math.abs(value)});
                               }
                            }
                    }
					//For flights SSR case where we need to show ) values also
					else {
					     if((detailsJson.productlob == "flight-ssr" && value == 0 && typeof value !="object" && (key.includes("Seat")|| key.includes("Meal") || key.includes("Baggage"))) || (value == 0 && typeof value !="object" && key == "Convenience Fee" && detailsJson.product == "ytprimedom")){
						    if (!isNaN(value)) {
							     html += template.render({label:key,amount:Math.abs(value)});
						    }
					     }
		        	else if( typeof value ==="object" && value.amount){

		        		var templateWithDivString = new Template();
						var totalConvAmount = value.amount;
						if(!isNaN(totalConvAmount)){
							var name=value.key;
							if(value.description){
								templateWithDivString.set(this.rowTemplateWithDivWithInfo);
							}else{
								templateWithDivString.set(this.rowTemplateWithDiv);
							}
							html += templateWithDivString.render( {label:key,amount:Math.abs(totalConvAmount),name:name,message:value.description} );
							html= this.makeExpandableDiv(value.breakup,key,name,html);
                            }
						}
		        	}
	        	}
	        }
	        if(roundOffCart){
	        	this.totalPayableAmount = Math.round(this.getTotalCartAmount());
	        }else{
	        	this.totalPayableAmount =this.getTotalCartAmount();
	        }

	        this.payAtHotelAmount = Number(PaymentCommon.getPayAtHotel());
	        var emi_select = $("#emiBank_select").val();
	        var paymentOption = YatraPaymentUI.getPaymentOption();
	        if( this.totalPayableAmount<disableEMICondition.amex[3].min && paymentOption == "emi" && emi_select == "amex"){
	        	YatraPaymentUI.disableEMI($("#emiBank_select").val());
	        }
	        else{
				YatraPaymentUI.enableEMI($("#emiBank_select").val());
				let cAmt = ShoppingCart.getTotalCartAmount();
				let selEmiOpt = $("#emiBank_select").val();
				let selectedTenure = $( ".emi-tenure-radio:checked" ).prop("name",selEmiOpt).val();
				if (cAmt && selEmiOpt) {
					YatraPaymentUI.disableEmiDetails(cAmt, selEmiOpt);
					if(cAmt < disableEMICondition[selEmiOpt][selectedTenure].min){
						YatraPaymentUI.disableEMI(selEmiOpt);
					}
				}
	        }

	        if( (this.totalPayableAmount<disableEMICondition.bajaj[3].min || this.totalPayableAmount>(disableEMICondition.bajaj[12].max-1)) && paymentOption == "emi" && emi_select == "bajaj"){
	        	YatraPaymentUI.disableEMI($("#emiBank_select").val(), this.totalPayableAmount>(disableEMICondition.bajaj[12].max-1)?true:false);
	        }
	        else{
				YatraPaymentUI.enableEMI($("#emiBank_select").val());
				let cAmt = ShoppingCart.getTotalCartAmount();
				let selEmiOpt = $("#emiBank_select").val();
				let selectedTenure = $( ".emi-tenure-radio:checked" ).prop("name",selEmiOpt).val();
				if (cAmt && selEmiOpt) {
					YatraPaymentUI.disableEmiDetails(cAmt, selEmiOpt);
					if(cAmt < disableEMICondition[selEmiOpt][selectedTenure].min){
						YatraPaymentUI.disableEMI(selEmiOpt);
					}
				}
			}
			if(detailsJson.isTcsEnabled){
				if($("input:radio[name=payOption]:checked").attr("id") != "partPayOption" && $("input:radio[name=payOption]:checked").attr("id") != "holdAndPayOption"){
					html += template.render({label:"Payable",amount: (GV.totalGVAmount() >0 ? (GV.balanceAmountAfterGV + GV.totalGVAmount() - ShoppingCart.tcsAmount): ShoppingCart.getTotalCartAmount())});
				}
				html += template.render({label:"TCS",amount: ShoppingCart.tcsAmount});
				if(GV.totalGVAmount() >0){
					html += template.render({label:"Voucher Redeemed (-)",amount: GV.totalGVAmount()});

				}
				this.totalPayableAmount = (GV.totalGVAmount() > 0) ? (Ewallet.ecashRedeemedAmount > 0 ? (GV.balanceAmountAfterGV - Ewallet.ecashRedeemedAmount < 0 ? Number(detailsJson.totalAmount) + Number(ShoppingCart.tcsAmount) - GV.totalGVAmount() - Ewallet.ecashRedeemedAmount
				:(GV.balanceAmountAfterGV - Ewallet.ecashRedeemedAmount)) : GV.balanceAmountAfterGV) : Number(ShoppingCart.tcsAmount)+ this.totalPayableAmount ;
	        }


	        if(detailsJson.currencyName && detailsJson.currencyName != "INR" && detailsJson.currencyConversionRate){
	        	html += template.render({label:"Total Amount",amount:this.totalPayableAmount});
	        }

	        $("#"+ShoppingCart.breakDownContainer).html(html);
	        //Assign expandable div to correct position
	        var element = document.getElementById("expandableDiv");
	        if(typeof(element) != "undefined" && element != undefined && element != null) {
	        	if(typeof(x) != "undefined" && x != undefined && x != null) {
	        		element.innerHTML = x;
	        		element.style.display = "block";
	        	}
	        }
	        if(this.payAtHotelAmount != 0) {
	        	$("#"+ShoppingCart.totalBookingSpan).html(YatraHelper.formatPrice(this.totalPayableAmount));
	        	$("#"+ShoppingCart.payAtHotelSpan).html(YatraHelper.formatPrice(Number(PaymentCommon.getPayAtHotel())));
	        	this.totalPayableAmount -= Number(PaymentCommon.getPayAtHotel());
	        	$("#"+ShoppingCart.advanceAmountSpan).html(YatraHelper.formatPrice(this.totalPayableAmount));
	        }
	        else{
	        	$('.payAtHotelBreakup').hide();
	        }

	        if(detailsJson.currencyName && detailsJson.currencyName != "INR" && !internationalCurrency && detailsJson.currencyConversionRate){
	        	$("#"+ShoppingCart.totalAmountSpan).html(YatraHelper.formatPrice(detailsJson.totalAmount));
	        	$("#totalAmountSpann").html(YatraHelper.formatPrice(detailsJson.totalAmount));
	        }else{
				 $("#"+ShoppingCart.totalAmountSpan).html(YatraHelper.formatPrice(this.totalPayableAmount));
			        if(detailsJson.payLaterFlow && detailsJson.payLaterFlow == "true"){
				        $("#totalAmountSpann").html(YatraHelper.formatPrice(1));
				        $(".amout-aside").css({left: 59});
				       $("#"+ShoppingCart.totalAmtInPayBlk).html(YatraHelper.formatPrice(1));
				       } else{
				         $("#totalAmountSpann").html(YatraHelper.formatPrice(this.totalPayableAmount));
			         $("#"+ShoppingCart.totalAmtInPayBlk).html(YatraHelper.formatPrice(this.totalPayableAmount));
			        }
			}
		},
		makeExpandableDiv : function (breakup,key,id,html) {
		    var templateInsideDiv = new Template();
			templateInsideDiv.set(this.rowTemplateInsideDiv);
			var expandHtml = "<div class='tab' id='mainTemplate'><input id='"+'tab-'+id+"' class='tab-input' type='checkbox' name='tabs'> <label for='"+'tab-'+id+"' class='tab-label'>Label One</label>	<ul class='tab-content'>".replace(/[\n\t\r]/g,"").replace(/\\"/g, '"');
		    for(var key in breakup){
	        	var amount = breakup[key];
	        	if(amount!=0)
	        		expandHtml += templateInsideDiv.render({label:key,amount:Math.abs(amount)});
		    }
		    expandHtml += "</ul> </div>".replace(/[\n\t\r]/g,"").replace(/\\"/g, '"');
			   // return html;
					var ht = '';
					$.each($(html), function (index, obj) {
						if(this.firstChild){
								var $this = $(this);
							var node = $this.find('#'+id);
							if(node.length){
								$this.append(expandHtml);
								ht+=$this.prop('outerHTML')
							}else{
								ht+=$this.prop('outerHTML')
							}
						}
					});
					return ht;
		},
		handleConvFeesPercentage : function(){
			if(detailsJson.convFeePercent){
				ShoppingCart.updateConvFeesExpandable(0, 0, 0);
				var totalCartAmount = Math.round(this.getTotalCartAmount());
				totalCartAmountWithoutConvFeesPercentage = totalCartAmount;
				var payOp = YatraPayment.getPayOp();
				var GSTOnConvFeesPer = detailsJson.convFeeGSTPercent ? detailsJson.convFeeGSTPercent : 5;
				console.log("selected pay op in update ",  payOp)
				if(payOp){
					var perConvFees = detailsJson.convFeePercent[payOp]?detailsJson.convFeePercent[payOp] : detailsJson.convFeePercent["default"];
					var convFees = Math.round((totalCartAmount * perConvFees) / 100 );
					var GSTOnConvFees = Math.round((convFees * GSTOnConvFeesPer) / 100 );
					var TotalConvFees = convFees + GSTOnConvFees;
					ShoppingCart.updateConvFeesExpandable(TotalConvFees, convFees, GSTOnConvFees);
				}
			}

		},
		getTotalCartAmount : function(){
			var totalPayableAmount = 0;
			 for(var i in ShoppingCart.json){
		        	for(var key in ShoppingCart.json[i]){
						var amount = ShoppingCart.json[i][key];
						if(key !== "Payable"){
							if(amount!=0 && typeof amount !="object"){
								if(!isNaN(amount)){
									totalPayableAmount += Number(amount);
								}
							}
							else if( typeof amount ==="object" && amount.amount){
								var temp = amount.amount;
								totalPayableAmount += Number(temp);
							}
						}
		        	}
		        }


			 if(totalPayableAmount<0){
				 return 0;
			 }
			 return totalPayableAmount;
		},
		// only applicable for display
		getTotalCartAmountForTCS: function() {
	        var b = 0;
	        for (var e in ShoppingCart.json) {
	            for (var d in ShoppingCart.json[e]) {
	                var c = ShoppingCart.json[e][d];
	                if (d !== "Payable") {
	                    if (c != 0 && typeof c != "object") {
	                        if (!isNaN(c)) {
	                            b += Number(c)
	                        }
	                    } else {
	                        if (typeof c === "object" && c.amount) {
	                            var a = c.amount;
	                            b += Number(a)
	                        }
	                    }
	                }
	            }
	        }
			if(YatraPaymentUI.isTcsEnabled(detailsJson.isTcsEnabled)){
				return b
			}
	        if (b < 0) {
	            return 0
	        }
	        return b
	    },
		updateVisaDiscount : function (amount){
			if(detailsJson.discount && (!$.isEmptyObject(detailsJson.discount)) && (detailsJson.discount.type.toUpperCase() == "VISA" || detailsJson.discount.type.toUpperCase() == "CARD")){
				this.json[this.labelIndex[detailsJson.discount.cartLabel]][detailsJson.discount.cartLabel] = amount;

			}
		},
		updateOtherDiscount : function (amount){
			if(detailsJson.discount && (!$.isEmptyObject(detailsJson.discount))){
				this.json[this.labelIndex[detailsJson.discount.cartLabel]][detailsJson.discount.cartLabel] = amount;

			}
		},
		updatePromocode : function (amount){
			this.json[this.labelIndex[detailsJson.promo.cartLabel]][detailsJson.promo.cartLabel] = amount;
		},
		updateConvFeesExpandable : function(Totalamount, amount, Taxes ){
			ShoppingCart.json[ShoppingCart.labelIndex["Convenience Fee & Taxes"]]["Convenience Fee & Taxes"].amount = Totalamount;
			ShoppingCart.json[ShoppingCart.labelIndex["Convenience Fee & Taxes"]]["Convenience Fee & Taxes"].breakup["Convenience Fee"] = amount;
			ShoppingCart.json[ShoppingCart.labelIndex["Convenience Fee & Taxes"]]["Convenience Fee & Taxes"].breakup["Yatra GST"] = Taxes;

		},
		updateGST : function (amount){
			this.json[this.labelIndex["GST"]]["GST"] = amount;
			currentGSTAmount = amount;
		},
		updateGVRedeemed : function (amount){
			this.json[this.labelIndex["Voucher Redeemed (-)"]]["Voucher Redeemed (-)"] = -amount;
			gvRedeemedAmount = amount;
		},
		addConvFee : function (){
			this.json[this.labelIndex["Convenience Fee"]]["Convenience Fee"] = PaymentCommon.getConvienceFee(YatraPaymentUI.getPaymentOption());;
		},
		addWalletInfo : function(){
			this.json[this.labelIndex["eCash Redeemed (-)"]]["eCash Redeemed (-)"] = -Ewallet.ecashRedeemedAmount;
		},
		addEcashGVWalletInfo : function(){
        	var umToRedeem = parseInt($("#nexpEcashForGVToRedeem").val());
        	this.json[this.labelIndex["Paid with eCash (-)"]]["Paid with eCash (-)"] = -umToRedeem;
        },
		addConvFeeTax : function (){
			this.json[this.labelIndex["Taxes (Including ST, SBC, KKC)"]]["Taxes (Including ST, SBC, KKC)"] = PaymentCommon.getConvFeeTaxes();
		},
		addConvFeeGST : function (){
			this.json[this.labelIndex["GST"]]["GST"] = PaymentCommon.getConvFeeGST();
		},
		addPartialOrHold : function(amount , dueDate , payableAmount){
			this.json[this.labelIndex["Pay Later"]]["Pay Later"] = -amount;
			this.json[this.labelIndex["Payable"]]["Payable"] = payableAmount;
			this.json[this.labelIndex["Due Date"]]["Due Date"] = dueDate;
		},
		generateFareBreakupArray : function(){
			 var i = 0;
			 for(var key in detailsJson.fareBreakup){
				 if(typeof detailsJson.fareBreakup[key] ==="object" && detailsJson.fareBreakup[key].breakUp) {
					 this.json[i] = {};
					 if (
								this.moreThanOneNonZeroElements(
								  detailsJson.fareBreakup[key].breakUp
								) ||
								(detailsJson.convFeePercent && key == "convFeeAndTax") ||
								key == "additionalTaxes"
							  ) {
								this.json[i][detailsJson.fareBreakup[key].label] = {
								  amount: detailsJson.fareBreakup[key].amount
								};
								if (detailsJson.fareBreakup[key].description) {
								  this.json[i][detailsJson.fareBreakup[key].label].description =
									detailsJson.fareBreakup[key].description;
								}

								this.json[i][detailsJson.fareBreakup[key].label].breakup =
								  detailsJson.fareBreakup[key].breakUp;
							  } else {
								this.json[i][detailsJson.fareBreakup[key].label] =
								  detailsJson.fareBreakup[key].amount;
							  }
					 this.labelIndex[detailsJson.fareBreakup[key].label] = i;
					 i++;
				 }
				 else {
					 this.json[i] = {};
					 this.json[i][key] = detailsJson.fareBreakup[key];
					 this.labelIndex[key] = i;
					 i++;
				 }

			 }
			 this.json[i] = {"Convenience Fee":0};
			 this.labelIndex["Convenience Fee"] = i;
			 i++;

			 this.json[i] = {"Taxes (Including ST, SBC, KKC)":0};
			 this.labelIndex["Taxes (Including ST, SBC, KKC)"] = i;
			 i++;

			if(this.labelIndex.GST==undefined) {
				 this.json[i] = {"GST":0};
				 this.labelIndex["GST"] = i;
				 i++;
			 }

			 if(!detailsJson.isTcsEnabled){
				this.json[i] = {"Voucher Redeemed (-)":0};
				this.labelIndex["Voucher Redeemed (-)"] = i;
				i++;
			 }

			 this.json[i] = {"eCash Redeemed (-)":0};
			 this.labelIndex["eCash Redeemed (-)"] = i;
			 i++;

			 this.json[i] = {"Payable":0};
			 this.labelIndex["Payable"] = i;
			 i++;

			 this.json[i] = {"Pay Later":0};
			 this.labelIndex["Pay Later"] = i;
			 i++;

			 this.json[i] = {"Due Date":''};
			 this.labelIndex["Due Date"] = i;
			 i++;

			 this.json[i] = {"Payment Surcharge":0};
			 this.labelIndex["Payment Surcharge"] = i;
			 i++;

			 this.json[i] = {"Paid with eCash (-)":0};
             this.labelIndex["Paid with eCash (-)"] = i;
             i++;

			 if(detailsJson.isTcsEnabled){
				this.json[i] = {"Voucher Redeemed (-)":0};
				this.labelIndex["Voucher Redeemed (-)"] = i;
				i++;
			 }

		},
		showExcludedChargesForCorporate : function(){
			var html = "";
			var template = new Template();
			template.set("excludedChargesforCorp");
			if(detailsJson.taxBreakUp && detailsJson.taxBreakUp.breakUp){
				for(var i in detailsJson.taxBreakUp.breakUp){
					html += template.render({label:i,amount:Math.abs(detailsJson.taxBreakUp.breakUp[i])});
				}
				$("#excludedChargesCORPContainer").html(html);
				var template1 = new Template();
				template1.set("excludedChargesHover");
				html = template1.render({label:detailsJson.taxBreakUp.label,message:detailsJson.taxBreakUp.description});

				$("#excludedChargesHoverCont").html(html);
			}
		},

		generateExcludedCharges : function(){
			this.exchangeChargesArr = [];
			if(detailsJson.extraCharges && detailsJson.extraCharges.breakdown && detailsJson.extraCharges.breakdown.excluded
					&& detailsJson.extraCharges.breakdown.excluded.length){
				for(var key in detailsJson.extraCharges.breakdown.excluded){
					this.exchangeChargesArr[key] = {
						label:detailsJson.extraCharges.breakdown.excluded[key].name,
						amount:detailsJson.extraCharges.breakdown.excluded[key].charge
					}
				}
				var html = "";
				var template = new Template();
				template.set(ShoppingCart.excludedChargesRow);
				for(var i in ShoppingCart.exchangeChargesArr){
					html += template.render({label:ShoppingCart.exchangeChargesArr[i].label,amount:Math.abs(ShoppingCart.exchangeChargesArr[i].amount)});
				}
				$("#"+ShoppingCart.excludedChargesRow).html(html);
				detailsJson.excludedChargesBreakUpInfo = detailsJson.excludedChargesBreakUpInfo||" Please note: Due to fluctuations in currency, the price shown may change at the time of payment";
				if(detailsJson.excludedChargesBreakUpInfo){
					var html="";
					var template = new Template();
					template.set(ShoppingCart.excludedChargesContId);
					html += template.render({message:detailsJson.excludedChargesBreakUpInfo});
					$("#"+ShoppingCart.excludedChargesContId).html(html);
				}
				$("#"+ShoppingCart.excludedChargesContId).show();
			}
		},
		moreThanOneNonZeroElements:function(breakUp) {
			var i = 0;
			 for(var key in breakUp){
				 if(breakUp[key]!=0)
					 i++;
			 }
			 if(i>1)
				 return true;
			 else return false;
		},
		addPGSurcharge:function(paymentOptionChange){
            var paymentJson = getPaymentJSON();
			//if surcharge is null or product is GDS hotel
			if(!detailsJson.agentPaymentOptions.amountDetails || (detailsJson.isPassthrough=="true" && (paymentJson.product=="crpdom_hotel"||paymentJson.product=="crpint_hotel"||paymentJson.product=="crpdomhotel"||paymentJson.product=="crpinthotel"))) {
				//$('#surchargeGST_msg_id').hide();
				return;
			}
			try{
				this.json[this.labelIndex["Payment Surcharge"]]["Payment Surcharge"] = 0;
				var totalPayableAmount = this.getTotalCartAmount();
				/*if(this.passThroughAmount && this.passThroughAmount>0){
					totalPayableAmount = totalPayableAmount - this.passThroughAmount;
				}*/
				var surchargeAmount = 0;
				var paymentOption =paymentOptionChange.paymentOption.toUpperCase();
				var paymentType = paymentOptionChange.paymentType;
				if(paymentOption=="QB"){
					paymentOption = paymentType.toUpperCase();
				}
				if(paymentOptionChange.cardBrand){
					paymentType = paymentOptionChange.cardBrand.toUpperCase();
				}
                                if (!detailsJson.agentPaymentOptions.amountDetails[paymentOption] || !detailsJson.agentPaymentOptions.amountDetails[paymentOption][paymentType]){
                                    this.json[this.labelIndex["Payment Surcharge"]]["Payment Surcharge"] = 0;
                                    PaymentCommon.onAmountChange();
					//$('#surchargeGST_msg_id').hide();
                                    return;
				}
				var surchargeNode = detailsJson.agentPaymentOptions.amountDetails[paymentOption][paymentType];
				surchargeAmount = parseInt(Math.round(surchargeNode.surchargeWithGST));
				//if(surchargeAmount > 0)
					//$('#surchargeGST_msg_id').show();
				//else $('#surchargeGST_msg_id').hide();
				this.json[this.labelIndex["Payment Surcharge"]]["Payment Surcharge"] = surchargeAmount;
				}catch(e){
					this.json[this.labelIndex["Payment Surcharge"]]["Payment Surcharge"] = 0;
				}
				PaymentCommon.onAmountChange();
		},
		handlePassthroughMessage : function(paymentOptionChange){
			var paymentOption =paymentOptionChange.paymentOption.toUpperCase();
			var paymentType = paymentOptionChange.paymentType.toUpperCase();
			var cardBrand =  paymentOptionChange.cardBrand.toUpperCase();
			if(paymentOption=="CC" || paymentOption=="DC" ){
			   var key = paymentOption;
			               var key2 = paymentType;
			}
			else if(paymentOption=="CORPCARD"){
			   var key = paymentOption;
			               var key2 = cardBrand;
			}
			else{
			var key = paymentType;
			               var key2 = cardBrand;
			}
			if(detailsJson.agentPaymentOptions.amountDetails[key] && detailsJson.agentPaymentOptions.amountDetails[key][key2]){
			var amountDetailsNode = detailsJson.agentPaymentOptions.amountDetails[key][key2];
			var passAmount = amountDetailsNode.passthroughAmount;
			var passMessage = amountDetailsNode.message?amountDetailsNode.message:"";
			if(passMessage != "") {
			$("#passThroughMessage_id").show();
			$("#passThroughMessage_id").html(passMessage)
			}
			else {
			$("#passThroughMessage_id").hide();
			}
			}

			},
		handlePassthrough : function (paymentOptionChange){
			this.passThroughAmount=0;
			if(!detailsJson.isPassthrough){
				$("#passthroughDiv").hide();
				return;
			}
			var paymentOption =paymentOptionChange.paymentOption.toUpperCase();
			var paymentType = paymentOptionChange.paymentType.toUpperCase();
			var cardBrand =  paymentOptionChange.cardBrand.toUpperCase();
			if(paymentOption=="CC" || paymentOption=="DC" ){
			    var key = paymentOption;
                var key2 = paymentType;
			}
			else if(paymentOption=="CORPCARD"){
			    var key = paymentOption;
                var key2 = cardBrand;
			}
			else{
				var key = paymentType;
                var key2 = cardBrand;
			}
			if(detailsJson.agentPaymentOptions.amountDetails[key] && detailsJson.agentPaymentOptions.amountDetails[key][key2]){
				var amountDetailsNode = detailsJson.agentPaymentOptions.amountDetails[key][key2];
				var passAmount = amountDetailsNode.passthroughAmount;
				if (passAmount == undefined || passAmount == "undefined") {
					$("#passthroughDiv").hide();
					return;
				}
				var totalCartAmount = this.getTotalCartAmount();
				this.passThroughAmount= parseInt(passAmount);
				$("#ptAmount").html(YatraHelper.formatPrice(passAmount));
				var pgAmount = totalCartAmount-passAmount>0?totalCartAmount-passAmount:0;
				if(pgAmount == 0) {
					isZeroNonPassThroughPayment = true;
					YatraPaymentUI.disableCVVforGdsFlow();
					YatraPaymentUI.disableQBCVVForCreditCard($('input:radio[processCardId=cc]'));
	                YatraPaymentUI.disableMultiFlowUiForCreditCard($('input:radio[processCardId=cc]'));

				}
				else isZeroNonPassThroughPayment = false;
				$("#pgAmount").html(YatraHelper.formatPrice(pgAmount));
				$("#passthroughDiv").show();
				return;
			}
			$("#passthroughDiv").hide();
		}

	};
var Ewallet = {
		ecashRedeemedAmount : 0,
		maxRedeemableECash : 0,
		showAlert:function(){
			alert("Since your payment amount has changed, please cancel the eCash redemption and redeem it again.");
		},
		confirmPageChange : function(stepNumber){
			if(Ewallet.ecashRedeemedAmount > 0){
				YatraEWalletPaymentUI.reverseAuth(false);
			}
				if(stepNumber == "1"){
					var anchor = $("#spanEditStep1 a");
					var url = anchor.attr("url");
				}
				else if (stepNumber == "2"){
					var anchor = $("#spanEditStep2 a");
					var url = anchor.attr("url");
				}
			anchor.removeAttr("href");
			if(!$.isEmptyObject(url)){
				window.location=url;
			}
		}


};

/*** Callback Methods Expected by Common Checkout */

function getPaymentJSON(){
	var paymentJson = {
			"amount" : ShoppingCart.totalPayableAmount+Ewallet.ecashRedeemedAmount,
			"cartAmount": ShoppingCart.totalPayableAmount,
			"superPNR" : detailsJson.superPnr,
			"merchant" : (detailsJson.merchant)?detailsJson.merchant:'yatra',
			"product" : detailsJson.product,
			"international" : {},
            "wallet" : {
                "maxRedeemableECash":Ewallet.maxRedeemableECash,
                "product":"",
                "channel":"",
                "device":"",
                "previousEcashRedeemed": previousEcashRedeemed
            }
		};
		if(detailsJson.userDetails){
			paymentJson.userEmail = detailsJson.userDetails.email;
		}
		if(detailsJson.travelDate){
			paymentJson.international.travelDate = detailsJson.travelDate;
		}
		if(detailsJson.origin){
			paymentJson.international.origin = detailsJson.origin;
		}
		if(detailsJson.destination){
			paymentJson.international.destination = detailsJson.destination;
		}
		if(detailsJson.tripType){
			paymentJson.international.tripType = detailsJson.tripType;
		}
		if(detailsJson.agentProfileType){
			paymentJson.agentProfileType = detailsJson.agentProfileType;
		}
		if(detailsJson.gavpv){
			paymentJson.gavpv=detailsJson.gavpv;
		}
		if(detailsJson.partialPayment){
			var totalAmount = Number(detailsJson.totalAmount);
			var payNowAmount = Number( detailsJson.partialPayment.partialPayAmount);
			var partialpayment =  {
					"isEligibleForPartial" :  detailsJson.partialPayment.isEligible,
					"payNowAmount" : totalAmount,
					"partialPayNowAmount" : payNowAmount,
					"partialPayLaterAmount" : totalAmount-payNowAmount,
					"isSelected":(detailsJson.partialPayment.isSelected)?detailsJson.partialPayment.isSelected:"false"
			};
			paymentJson.partialpayment = partialpayment;
		}

		if(detailsJson.holdPayment){
			var totalAmount = Number(detailsJson.totalAmount);
			var payNowAmount = Number( detailsJson.holdPayment.holdAmount);
			var holdAndPay =  {
					"isEligibleForHold" : detailsJson.holdPayment.isEligible,
					"payNowAmount" : totalAmount,
					"holdAndPayNowAmount" : payNowAmount,
					"holdAndPayLaterAmount" : totalAmount-payNowAmount,
					"isSelected":(detailsJson.holdPayment.isSelected)?detailsJson.holdPayment.isSelected:"false"
			};
			paymentJson.holdAndPay = holdAndPay;
		}

        if(detailsJson.walletDetails){
            if(detailsJson.walletDetails.product)
                paymentJson.wallet.product= detailsJson.walletDetails.product;
            if(detailsJson.walletDetails.channel)
                paymentJson.wallet.channel= detailsJson.walletDetails.channel;
            if(detailsJson.walletDetails.device)
                paymentJson.wallet.device= detailsJson.walletDetails.device;
        }
		return paymentJson;
}

function updateLiveDataOnPayOpChanged(paymentOption){
	try{
	var trackingData = {};
	trackingData.tenant = detailsJson.product;
	if(detailsJson.ci){
		trackingData.ci = detailsJson.ci;
	}
	if(detailsJson.pricingId){
		trackingData.pricingId = detailsJson.pricingId;
	}
	trackingData.ttv=detailsJson.totalAmout;
	trackingData.spnr=detailsJson.superPnr;
	trackingData.stage="payment";
	trackingData.paymentType=ProcessPayment.paymentMode;
	trackingData.convFee = PaymentCommon.getConvienceFee(paymentOption);
	trackingData.ttid=detailsJson.superPnr;
	trackingData.yaaction = "PaymentMode";
	trackingData.yaValue = paymentOption;
	trackingData.referrerURL=document.referrer;
	trackingData.screenHeight=screen.height;
	trackingData.screenWidth=screen.width;
	var url ="/livedata-analytics/updateLiveData";
	YatraPaymentUI.sendLiveTrackingAjax(JSON.stringify(trackingData),url);

	}
	catch(e){
		console.log(e);
	}
}

function processPayOp(paymentOption){
	var oldConvFee = ShoppingCart.json[ShoppingCart.labelIndex["Convenience Fee"]]["Convenience Fee"];
	var newCovFee = PaymentCommon.getConvienceFee(paymentOption);
	if(newCovFee < oldConvFee && (oldConvFee-newCovFee) > ShoppingCart.totalPayableAmount && Ewallet.ecashRedeemedAmount>0){
		Ewallet.showAlert();
		YatraEWalletPaymentUI.reverseAuth();
	}

	if (paymentOption.toUpperCase() == "QB"
		&& detailsJson.discount
		&& (!$.isEmptyObject(detailsJson.discount))
		&& ShoppingCart.json[ShoppingCart.labelIndex[detailsJson.discount.cartLabel]][detailsJson.discount.cartLabel] != 0){
		var checkedQBCard = $('input:radio[name="cardid"][checked="checked"]');
		var cardId = $(checkedQBCard).val();
		var topHeight =  $(".cpmt_Paymentdiv").offset().top;
		PaymentCommon.handleVisaDiscountCommon(cardId,topHeight,paymentOption);
	}
	else if(!(paymentOption.toUpperCase() == "CC" || paymentOption.toUpperCase() == "DC" ||  paymentOption.toUpperCase() == "GV"  )){
		if(!GV.maintainState){
		if(detailsJson
			&& (!$.isEmptyObject(detailsJson.discount))
			&& (detailsJson.discount.type.toUpperCase() == "VISA" || detailsJson.discount.type.toUpperCase() == "CARD")){
			var topOffset = $(".cpmt_Paymentdiv").offset().top;
			PaymentCommon.displayPartialVisaFailureMessage("payop_changed_visa_discount_pop_up",topOffset);
		}
		cardDiscountingStatus = "false";
		ShoppingCart.updateVisaDiscount(0);
		}
	}
	else {
		if(!GV.maintainState){
		cardDiscountingStatus = "false";
		// for visa discount --> not to give visa discount when payable amount is less than visa discount
		var totalCartAmount = ShoppingCart.getTotalCartAmount();
		if(detailsJson.discount && detailsJson.discount.type && (detailsJson.discount.type.toUpperCase() == "VISA" || detailsJson.discount.type.toUpperCase() == "CARD")
								&& ShoppingCart.json[ShoppingCart.labelIndex[detailsJson.discount.cartLabel]][detailsJson.discount.cartLabel] == 0 && totalCartAmount > detailsJson.discount.amount	) {
			var topOffset = $(".cpmt_Paymentdiv").offset().top;
			PaymentCommon.displayPartialVisaFailureMessage("visa_discount_applied_pop_up",topOffset);
			ShoppingCart.updateVisaDiscount("-"+detailsJson.discount.amount);
		}
		}
	}

	ShoppingCart.addConvFee();
	PaymentCommon.onAmountChange();
	updateLiveDataOnPayOpChanged(paymentOption);
}

function paymentOptionChange(payOpChange){
	ShoppingCart.addPGSurcharge(payOpChange);
	ShoppingCart.handlePassthrough(payOpChange);
	ShoppingCart.handlePassthroughMessage(payOpChange);
}


function processPartialPayment(partialPayOption) {
	try {
		if(partialPayOption == 'payPartial') {
			Ewallet.maxRedeemableECash = (detailsJson.partialPayment.maxRedeemableECash)?parseFloat(detailsJson.partialPayment.maxRedeemableECash):0;
			ProcessPayment.paymentMode = 'PARTIAL';
			ShoppingCart.addPartialOrHold(detailsJson.totalAmount - detailsJson.partialPayment.partialPayAmount ,  detailsJson.partialPayment.payByDate?detailsJson.partialPayment.payByDate:'' , detailsJson.partialPayment.partialPayAmount);
			if(Ewallet.ecashRedeemedAmount>0 && ShoppingCart.totalPayableAmount < (detailsJson.totalAmount - detailsJson.partialPayment.partialPayAmount)){
				Ewallet.showAlert();
				YatraEWalletPaymentUI.reverseAuth();
			}
			if(detailsJson.promo && detailsJson.partialPayment.partialPromoAmount)
				ShoppingCart.updatePromocode(detailsJson.partialPayment.partialPromoAmount);

			if((typeof detailsJson.partialPayment != "undefined") && (typeof detailsJson.partialPayment.earnEcash != "undefined") && (detailsJson.partialPayment.earnEcash != null)) {
				var ecashAmt=detailsJson.partialPayment.earnEcash;
				$('#ecash_container').removeClass('hide');
				$("#ecashAmount").html(YatraHelper.formatPrice(ecashAmt));
				$('#earnEcashDiv').removeClass('hide');
			}
			//Visa inapplicable for partial payment
			PaymentCommon.handleVisa(false,"partial_payment_visa_removed_pop_up");
			/*if(PaymentCommon.isVisaApplied()){
				var topOffset = $(".cpmt_Paymentdiv").offset().top;
				PaymentCommon.displayPartialVisaFailureMessage("partial_payment_visa_removed_pop_up",topOffset);
			cardDiscountingStatus = "false";
			ShoppingCart.updateVisaDiscount(0);
			}*/


		}
		else if(partialPayOption == 'holdAndPay'){
			ProcessPayment.paymentMode = 'HOLDANDPAY';
			Ewallet.maxRedeemableECash = (detailsJson.holdPayment.maxRedeemableECash)?parseFloat(detailsJson.holdPayment.maxRedeemableECash):0;
			ShoppingCart.addPartialOrHold(detailsJson.totalAmount - detailsJson.holdPayment.holdAmount , detailsJson.holdPayment.payByDate?detailsJson.holdPayment.payByDate:'', detailsJson.holdPayment.holdAmount);
			if(Ewallet.ecashRedeemedAmount>0 && ShoppingCart.totalPayableAmount < (detailsJson.totalAmount - detailsJson.holdPayment.holdAmount)){
				Ewallet.showAlert();
				YatraEWalletPaymentUI.reverseAuth();
			}

			if(detailsJson.promo && detailsJson.holdPayment.hnpPromoAmount)
				ShoppingCart.updatePromocode(detailsJson.holdPayment.hnpPromoAmount);


			if((typeof detailsJson.holdPayment != "undefined") && (typeof detailsJson.holdPayment.earnEcash != "undefined") && (detailsJson.holdPayment.earnEcash != null)) {
				var ecashAmt=detailsJson.holdPayment.earnEcash;
				$('#ecash_container').removeClass('hide');
				$("#ecashAmount").html(YatraHelper.formatPrice(ecashAmt));
				$('#earnEcashDiv').removeClass('hide');
			}

		}
		else {
			Ewallet.maxRedeemableECash = (detailsJson.maxRedeemableECash)?parseFloat(detailsJson.maxRedeemableECash):((detailsJson.walletDetails) && (detailsJson.walletDetails.maxRedeemableECash))?parseFloat(detailsJson.walletDetails.maxRedeemableECash):0;
			ProcessPayment.paymentMode = 'FULL';
			if(detailsJson.promo)
				ShoppingCart.updatePromocode("-"+detailsJson.promo.amount);
			ShoppingCart.addPartialOrHold(0,'' , 0);

			if((typeof detailsJson.earnEcash != "undefined")  && (detailsJson.earnEcash != null) && (detailsJson.earnEcash != 0)) {
				var ecashAmt=detailsJson.earnEcash;
				$('#ecash_container').removeClass('hide');
				$("#ecashAmount").html(YatraHelper.formatPrice(ecashAmt));
				$('#earnEcashDiv').removeClass('hide');
			}
			var validPayop = YatraPayment.getPayOp();
			if(validPayop.toUpperCase() == "CC" || validPayop.toUpperCase() == "DC" || validPayop.toUpperCase() == "GV"){
				if(!GV.maintainState){
					PaymentCommon.handleVisa(true,"visa_discount_applied_pop_up");
				}
				}
				else PaymentCommon.handleVisa(false,"payop_changed_visa_discount_pop_up");

		}
		// if(Ewallet.ecashRedeemedAmount>0 && GV.totalGVAmount() > 0 && detailsJson.isTcsEnabled == true){
		// 	Ewallet.showAlert();
		// 	YatraEWalletPaymentUI.reverseAuth();
		// }

		if(detailsJson.promo && detailsJson.promo.ecashAmount && detailsJson.promo.ecashAmount>0){
			$("#ecash_container").removeClass('hide');
			var promoEcashAmount = detailsJson.promo.ecashAmount;
			$("#ecashPromoAmount").html(YatraHelper.formatPrice(promoEcashAmount));
			$("#promoEcashEarned").removeClass('hide');
			if((typeof detailsJson.earnEcash != "undefined") && (detailsJson.earnEcash != null)){
				var totalECash = parseInt(detailsJson.promo.ecashAmount)+parseInt(detailsJson.earnEcash);
				$("#totalECashEarned").html(YatraHelper.formatPrice(totalECash));
				$("#totalECashEarnedLi").removeClass('hide');
			}
		}
		if(detailsJson.isTcsEnabled){
			// if(GV.totalGVAmount() > 0 && (PaymentMode.partialEnabledForProduct == true || PaymentMode.holdAndPayEnabledForProduct == true)){
			// 	GV.balanceAmountAfterGV = ShoppingCart.getTotalCartAmount() + Number(ShoppingCart.tcsAmount)
			// }
			YatraPaymentUI.callForRecalculatingTcs();
		}
		PaymentCommon.onAmountChange();
	} catch(e) {
		log_error(e);
	}
}
function processWalletPayment(action, amount) {
	try {
		if(action == 'REDEEM') {
		    Ewallet.ecashRedeemedAmount = amount;
		}else {
		    Ewallet.ecashRedeemedAmount = 0;
		    $("#nexpEcashForGVToRedeem").val(0);
		}
		if(YatraEWalletPaymentUI.isBookingForEcashGV()) {
		    ShoppingCart.addEcashGVWalletInfo();
		} else {
		    ShoppingCart.addWalletInfo();
		}
		ProcessPayment.stopPollingForQRCode();
		PaymentCommon.onAmountChange();
	} catch(e) {
		log_error(e);
	}
}
function getContinueButtonId() {
	return "payNow";
}
function log_error(e){
	//console.log(e);
}

function myFunction(event) {
  // Get the checkbox
  console.log("// Get the checkbox",event);
  if(event.checked){
  var payNowButton = document.getElementById("payNow");
  payNowButton.disabled = false;

  }
}

function onChangeBnpl(selectionValue){
if(selectionValue=='SPMT'){
$('.cpmt_tnCbnplstrip').attr('style','display: block');
  var payNowButton = document.getElementById("payNow");
  payNowButton.disabled = true;
}

else {
$('.cpmt_tnCbnplstrip').attr('style','display: none');
}
}


