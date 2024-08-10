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


var YatraPayment = {	
		
		initialisePage : function(){
		YatraPaymentUI.addAdobeProduct();
		YatraPaymentUI.getAdobeJSON(detailsJson);
		ShoppingCart.intialise();
		YatraPaymentUI.showHotelMessage(detailsJson.product, detailsJson.payAtHotel);
		YatraPaymentUI.showFareBreakUpMessage(detailsJson.fareBreakUpMsg);
		YatraPaymentUI.showYouPayMessage(detailsJson.youPayMsg);
		YatraPaymentUI.showStaticMessage(detailsJson);
		YatraPaymentUI.bankDownMesage();
		if(YatraPaymentUI.isTcsEnabled(detailsJson.isTcsEnabled)){
			YatraPaymentUI.callForRecalculatingTcs();
		}
		YatraPaymentUI.changePayNowButtonLabel(detailsJson.product);
		},
		
		getParamValue : function(formId, key) {
			return $("#" + formId + " input[name=" + key + "]").val();
		},
		
		getPayOp : function(){
			var payOptionDiv = "#tab_" + YatraPaymentUI.getPaymentOption();
			return $(payOptionDiv).find('input[name=payop]').val();
		},
		
		getPaymentDiv : function(){
			var payOptionDiv = "#tab_" + YatraPaymentUI.getPaymentOption();
			
			var payop=YatraPayment.getPayOp();
			if(payop == 'qb'){
				payOptionDiv = payOptionDiv + "_qb";
			}
			
			return payOptionDiv;
		},
		
		/**
		 * Function validating payment data
		 */

		validatePaymentDiv : function(formId){
			var isValid = PaymentValidation.validate('#' + formId);
			var validateCitiEmiTnc = false;
			if(isValid) {
				isValid = YatraPaymentUI.validateRewardPoints();
			}
			if(isValid) {
				isValid = YatraQBPaymentUI.validateQBAuth();
			}
			if(isValid){
				isValid = CreditPool.validate();
			}
			if(isValid){
				isValid = CashPool.validate();
			}
			if(isValid){
				validateCitiEmiTnc = YatraPaymentUI.validateCitiEmiTnc();
			}
			
			return validateCitiEmiTnc;
		},
		
		validatePaymentTab : function(){
			var isValid = PaymentValidation.validate("#tab_" + YatraPaymentUI.getPaymentOption());
			var validateRewardPoints = false;
			var validateCitiEmiTnc = false;
			if(isValid) {
				validateRewardPoints = YatraPaymentUI.validateRewardPoints();
			}
			if(validateRewardPoints){
				validateCitiEmiTnc = YatraPaymentUI.validateCitiEmiTnc();
			}
			
			return validateCitiEmiTnc;
		},
		appendByKeyValue : function(key, value ) {
			if((key != null && key != "" && key != "undefined") && (value != null && value != "" && value != "undefined")) {
				return key + "=" + value + "|";
			}
			return "";
		},
		
		getAppendedParamters : function(payOption) {
			var parameters = "";
			var paymentOptionDiv = YatraPayment.getPaymentDiv();
			var $inputs = $(paymentOptionDiv + " input");
			
			$inputs.each(function() {
				var eType = $(this).prop("type");
				
				switch(eType) {
					case "radio":
						if($(this).is(':checked')) {
							if(this.name != 'paypal'){
								parameters += YatraPayment.appendByKeyValue(this.name, $(this).val());
								}
								if(this.name == 'paypal'){
									parameters += YatraPayment.appendByKeyValue("currency", $(this).attr("id"));
									parameters += YatraPayment.appendByKeyValue("forexAmount", $(this).attr('forexamount').replace(',',''));
									parameters += YatraPayment.appendByKeyValue("forexRate", $(this).attr('forexrate'));
			
								}
							
						};
					break;
					default:
						// Remove default text in Internet explorer
						IE_Placeholder.removeDefaultText($(this));
						parameters += YatraPayment.appendByKeyValue(this.name, $(this).val());
					break;
				}
		    });
			
			$inputs = $(paymentOptionDiv + " select");
			$inputs.each(function() {
				parameters += YatraPayment.appendByKeyValue(this.name, $(this).val());
		    });
			
			return parameters;
		},
		
		getAppendedQBParamters : function() {
			var parameters = "";
			var paymentOptionDiv = YatraPayment.getPaymentDiv();
			var $inputs = $(paymentOptionDiv).children("input");
			
			$inputs.each(function() {
				parameters += YatraPayment.appendByKeyValue(this.name, $(this).val());
		    });
			
			// append the selected card id
			var cardIdElm = $(paymentOptionDiv +" input[name= cardid]:checked");
			parameters += YatraPayment.appendByKeyValue("cardid", $(cardIdElm).val());
			parameters += YatraPayment.appendByKeyValue("cardType", $(cardIdElm).attr('processcardid'));
			parameters += YatraPayment.appendByKeyValue("cardBrand", $(cardIdElm).attr('cardType'));
			parameters += YatraPayment.appendByKeyValue("cno",YatraQBPaymentUI.getQBCardNumber());
			
			// append the selected card's billing address details
			var addressDiv = $(cardIdElm).parent().parent().parent().find('#qb_pmt_addressId');
			var $addressInputs = $(addressDiv).find("input");

			$addressInputs.each(function() {
				parameters += YatraPayment.appendByKeyValue(this.name, $(this).val());
		    });

			return parameters;
		},
		
		getInternationalCardParameters : function(){
			var parameters = "";
			if(YatraPaymentUI.isInternationalSupportedOnProduct()){
				var paymentJSON = getPaymentJSON();
				parameters += YatraPayment.appendByKeyValue('sid', paymentJSON.international.sid);
				parameters += YatraPayment.appendByKeyValue('tdate', paymentJSON.international.travelDate);
				parameters += YatraPayment.appendByKeyValue('origin', paymentJSON.international.origin);
				parameters += YatraPayment.appendByKeyValue('destination', paymentJSON.international.destination);
				parameters += YatraPayment.appendByKeyValue('tripType', paymentJSON.international.tripType);
			}
			return parameters;
		},
		
		getPaymentOptionUrl : function(payOption) {
			var contextUrl = "https://secure.yatra.com"
			if (payOption == 'cc') {
				return contextUrl + '/checkout/pay/creditcard.htm';
			} else if (payOption == 'dc') {
				return contextUrl + '/checkout/pay/debitcard.htm';
			} else if (payOption == 'nb') {
				return contextUrl + '/checkout/pay/ibanking.htm';
			} else if (payOption == 'qb') {
				return contextUrl + '/checkout/pay/quickbook.htm';
			} else if (payOption == 'rewards') {
				return contextUrl + '/checkout/pay/rewards.htm';
			} else if (payOption == 'mw') {
				return contextUrl + '/checkout/pay/mobileWallet.htm';
			} else if (payOption == 'atm') {
				return contextUrl + '/checkout/pay/atm.htm';
			} else if (payOption == 'emi') {
				return contextUrl + '/checkout/pay/creditcard.htm';
			} else if (payOption == 'ec') {
				return contextUrl + '/checkout/pay/creditcard.htm';
			} else if (payOption == 'ivr') {
				return contextUrl + '/checkout/pay/ivr.htm';
			} else if (payOption == 'offline') {
				return contextUrl + '/checkout/pay/creditcard.htm';
			} else if (payOption == 'cashCard') {
				return contextUrl + '/checkout/pay/cashCard.htm';
			} else if (payOption == 'itz') {
				return contextUrl + '/checkout/pay/itz.htm';
			} else if (payOption == 'payu') {
				return contextUrl + '/checkout/pay/PayU.htm';
			} else if(payOption == 'corpCard'){
				return contextUrl + '/checkout/pay/corporateCard.htm';
			} else if(payOption == 'cp'){
				return contextUrl + '/checkout/pay/creditPool.htm';
			} else if (payOption == 'buddy') {
				return contextUrl + '/checkout/pay/mobileWallet.htm';
			} else if (payOption == 'amazonPay') {
				return contextUrl + '/checkout/pay/mobileWallet.htm';
			} else if (payOption == 'upi') {
				return contextUrl + '/checkout/pay/upi.htm';
			} else if (payOption == 'dcpin') {
             	return contextUrl + '/checkout/pay/debitCardWithAtmPin.htm';
            } else if (payOption == 'tez') {
				return contextUrl + '/checkout/pay/googlePay.htm';
            } 
			return contextUrl + '/checkout/pay/creditcard.htm';
		},
		
		getHashedFields : function(length) {
			
			if(length == 13) {
				return "XXX";
			} else if(length == 14) {
				return "XXXX";
			} else if(length == 15) {
				return "XXXXX";
			} else if(length == 18) {
				return "XXXXXXXX";
			} else if(length == 19) {
				return "XXXXXXXXX";
			} else {
				return "XXXXXX";
			}
		},

		getHashedParamters : function(parameters) {
			
			var parametersArray = parameters.split('|');
			var i=0;
			for (i=0;i<parametersArray.length;i++) {
				 parameter = parametersArray[i];
				 var paramArray = parameter.split('=');
				 var key = paramArray[0];
				 var value = paramArray[1];
				 if(key == 'cno') {
					 value = value.substring(0, 6) + YatraPayment.getHashedFields(value.length) + value.substring(value.length-4, value.length);
				 } else if (key == 'ccsc') {
					 if(value.length == 3) {
						 value = "XXX";
					 } else {
						 value = "XXXX";
					 }
				 } else if (key == 'cexpy') {
					 value = "XXXX";
				 } else if (key == 'cexpm') {
					 value = "XX";
				 }
				 paramArray[1] = value;
				 parametersArray[i] = paramArray.join("=");
			}
			 
			var hashedParams = parametersArray.join("|");
			if(hashedParams.substring(hashedParams.length-1, hashedParams.length) == '=') {
				hashedParams = hashedParams.substring(0,hashedParams.length - 1);
			}
			return hashedParams;
		},
		
		setFieldValue : function(formId, fieldName, value) {
			var field = $("#" + formId + " input[name=" + fieldName + "]");
			field.val(value);
		},
		
		getPaymentOptionParameters : function(formId) {
			var paymentJson = getPaymentJSON();
			var paymentOptionParameters = "";
			var paymentOptionUrl = "";
			var payOption = YatraPayment.getPayOp();
			// Set Merchant Code and Product Code
			paymentOptionParameters += YatraPayment.appendByKeyValue("merchant",paymentJson.merchant);
			paymentOptionParameters += YatraPayment.appendByKeyValue("product", paymentJson.product);

			// Payment parameters specific to complete payment through wallet
			// case.
			if(YatraEWalletPaymentUI.isCompleteEwalletPayment() && payOption != "gv") {
				// payop = 'ew' in case of complete payment through wallet.
				paymentOptionParameters += YatraPayment.appendByKeyValue("payop", 'ew');
				
				// previousPayOp is the payment option corresponding to the tab
				// that was selected when complete payment through wallet was
				// made.
				// This parameter is used to determine convenience fee /
				// discount that was applied when complete payment through
				// wallet was made.
				paymentOptionParameters += YatraPayment.appendByKeyValue("previousPayOp", $("#previousPayOp").val());
				
			} else {
				// Set Parameters based on Payment Option selected by User
				if(payOption == "qb") {
					paymentOptionParameters += YatraPayment.getAppendedQBParamters();
				} else {
					paymentOptionParameters += YatraPayment.getAppendedParamters(payOption);
				}
				// Set parameters for International card
				paymentOptionParameters += YatraPayment.getInternationalCardParameters();
			}
			
			// Appending ECash redemption paramters
			if($("#eCashRedeemed").val() != null && $("#eCashRedeemed").val() != "") {
				paymentOptionParameters += YatraPayment.getECashRedemptionParamters();
			}
			
			// Set the parameters in Form
			YatraPayment.setFieldValue(formId, "paymentOptionParameters", paymentOptionParameters);
			return paymentOptionParameters;
		},
		
		getParameterByName : function(fieldName) {
			var parameter = "";
			
			var paymentOptionDiv = YatraPayment.getPaymentDiv();
			var $inputs = $(paymentOptionDiv + " input");
			
			$inputs.each(function() {
				if(this.name == fieldName) {
					parameter = $(this).val();
				}
		    });
			return parameter;
		},
		
		getPromoCodePaymentParams : function () {
			
			var payOption = YatraPayment.getPayOp();
			
			var ecn = "";
			var bin = "XXXXXX";
			var paymentVendorName = "" ;
			var paymentOption = "";
			
			if (payOption == 'cc') {
				ecn = YatraPayment.getParameterByName("cno");
				bin = ecn.substring(0, 6);
				paymentVendorName = YatraPayment.getParameterByName("ctype");
				paymentOption = "Credit Card";
				
			} else if (payOption == 'dc') {
				ecn = YatraPayment.getParameterByName("cno");
				bin = ecn.substring(0, 6);
				paymentVendorName = YatraPayment.getParameterByName("ctype");
				paymentOption = "Debit Card";
				
			} else if (payOption == 'nb') {
				paymentVendorName = YatraPayment.getParameterByName("bankCode");
				paymentOption = "Net Banking";
				
			} else if (payOption == 'dcpin') {
              	paymentVendorName = YatraPayment.getParameterByName("bankCode");
              	paymentOption = "Debit Card + ATM Pin";
            } else if (payOption == 'atm') {
				paymentVendorName = YatraPayment.getParameterByName("bankCode");
				paymentOption = "atm";
				
			} else if (payOption == 'itz') {
				paymentVendorName = "ITZ Card";
				paymentOption = "ITZ Card";
				
			} else if (payOption == 'emi') {
				ecn = YatraPayment.getParameterByName("cno");
				bin = ecn.substring(0, 6);
				paymentVendorName = YatraPayment.getParameterByName("emiBank");
				paymentOption = "EMI Option";
				
			} else if(payOption == 'qb') {
				ecn = YatraQBPaymentUI.getQBCardNumber();
				bin = ecn.substring(0, 6);
				paymentVendorName = YatraQBPaymentUI.getQBCardType();
				paymentOption = "Quick Book";
			} 
			
			else if(payOption == 'mw') {
				paymentOption =  "MobileWallet";
				var bankCode = YatraPayment.getParameterByName("bankCode");
				switch(bankCode) {

					
			    case "MBK":
			    	paymentVendorName = "MobiKwik";
			        break;
			    case "PTM":
			    	paymentVendorName = "PayTM";
			        break;
				case "ATL":
					paymentVendorName = "AirtelMoney";
			        break;
				case "JIO":
					paymentVendorName = "RelianceJio";
					break;
				case "OXI":
					paymentVendorName = "OXIGEN";
					break;
				case "PYU":
					paymentVendorName = "PayU";
					break;
				case "BDY":
					paymentVendorName = "SBI BUDDY";
					break;
			    default:
			    	paymentVendorName =  "MobileWallet";
				} 
			}
			
			var params = "paymentOption="+paymentOption+"|ecn="+ecn+"|bin="+bin+"|paymentVendorName="+paymentVendorName;
			
			return params;
		},
		
		/**
		 * Function validating Terms & Conditions Checbox
		 */
		validateTnC : function(divId) {
			var validateDivId = "#" + divId;
			$(validateDivId).pmtValidation();
		    isValid =$(validateDivId).data('pmtValidator').isPMTValid();
		    return isValid;
		},
		
		getECashRedemptionParamters : function() {
			var parameters = "";
			if(redeemAuthJSON){
				var redeemedAmount = YatraEWalletPaymentUI.getAmountInRupee(redeemAuthJSON.amountInPaisa);
				parameters += YatraPayment.appendByKeyValue("wallet_amount", redeemedAmount);
				parameters += YatraPayment.appendByKeyValue("wallet_id", redeemAuthJSON.walletId);
			}
			return parameters;
		},
		
		formatAmount: function(amount) {

	        if (amount != undefined && amount > 0) {
	        	amount = parseFloat(amount);
	        	amount = Math.abs(amount);
	            if(amount <= 999) {
	                return amount;
	            };
	            var regexp = new RegExp(/(\d)(?=(\d\d\d)+(?!\d))/g);
	            var formattedAmount= amount.toString().replace(regexp, "$1,");            
	            return formattedAmount;
	        };
	        return 0;
	    },
	    
	    formatInterestAmount: function(amount, interestRate) {

	        if (amount != undefined && amount > 0) {
	        	amount = parseFloat(amount);
	        	amount = Math.abs(amount);
	        	if(amount < 1 && interestRate == 0) {
	        		return 0;
	        	}
	            if(amount <= 999) {
	                return amount;
	            };
	            var regexp = new RegExp(/(\d)(?=(\d\d\d)+(?!\d))/g);
	            var formattedAmount= amount.toString().replace(regexp, "$1,");            
	            return formattedAmount;
	        };
	        return 0;
	    },
	    
	    deformatAmount: function(amount) {
            var regexp = new RegExp(/,/g);
            var formattedAmount= amount.replace(regexp, "");            
            return formattedAmount;
	    },
		
	  processIFramePayment : function(formId) {
		  var eligibleForIframePayment = YatraPaymentUI.eligibleForIframePayment();
		  if(eligibleForIframePayment) {
			  var form = $('#' + formId);
			  form.attr("target", "cpmt-payment-iframe");
			  $("#iframe-div").show();
// setTimeout(function() {
// YatraPaymentUI.processPendingPayment();
// }, 60000);
			  var checkIsVisible = $("#cpmt-payment-iframe").is(":visible");
			  if(checkIsVisible == true){
				  $(".cpmt_Container").addClass("full-width");
				  $(".black-bg").show();
				  $("body").css("overflow-y","hidden");
			  }
		  }
		  return eligibleForIframePayment;
	  },
	  
	  closeIframe : function() {
		  $("#iframe-div").hide();
		  $("#cpmt-payment-iframe").attr("src", "");
		  $(".cpmt_Container").removeClass("full-width");
		  $(".black-bg").hide();
		  $("body").css("overflow-y","scroll");
		  YatraPaymentUI.trackGatewayWithStage("X");
		  notifyIframeClosed();
	  },
	  
    ifValueIsDefault : function (value) {
    	var ifValueIsDefault = false;
    	if(value == "Cardholder's Name") {
    		ifValueIsDefault = true;
    	} else if(value == "Credit Card Number") {
    		ifValueIsDefault = true;
    	}
    	return ifValueIsDefault; 
    }
};
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

// ********************************* /
// FileName: centralui-validation.js
// Author: Marghoob Suelman (original)
// Modified By : Meenakshi (For Central UI Validation framework)
// Desc: This script needs tools.js
// Created: 17 May, 2011
// ********************************* /

/*************** FORM UTILS ***************/
//version : 2.5.3

$.extend($.expr[':'], {
    opacity: function(elem, i, attr){
      return( $(elem).css("opacity") === attr[3] + '' );
    }
});

PaymentValidation = {
	oldTextProp: {},
	counter:20,
	restorOldText: function (arg) {
		var nameKey = $(arg).attr("name");
		var obj = this.oldTextProp[nameKey];
		if($.trim($(obj.target).val()) == "") {
			$(obj.target).val(obj.value);
			$(obj.target).unbind("blur");
			$(obj.target).removeClass("blackText");
		}
	},
	manageInputText: function (arg, dontClear) {
		var target = $(arg);
		var nameKey = $(arg).attr("name");
		var curerntValue = $(arg).val();
		if(typeof(this.oldTextProp[nameKey]) == "undefined") {
			//store first value
			this.oldTextProp[nameKey] = {target:target, value:curerntValue};
			$(target).bind("focus", function(evt) {
											 $(target).addClass("blackText");
											 evt.target.select();
											 });
			
		}
		if(curerntValue == this.oldTextProp[nameKey].value) {
			if(typeof(dontClear) == "undefined") {
				$(target).val("");
			}
			$(target).bind("blur", function() {
				PaymentValidation.restorOldText(target);
			});		
		}
	},
	focus:function(id) {
		document.getElementById(id).focus();
		//document.getElementById(id).select();
	},
	selectCombo: function(t, s) {
		var target = document.getElementById(t);
		var allOptions = target.options;
		var searchFor = s;
		for(var icount=0;icount<allOptions.length;icount++) {
			if((allOptions[icount].text.toString().toLowerCase() == searchFor.toLowerCase()) || allOptions[icount].value==searchFor) {
				target.selectedIndex = icount;
				break;
			}
		};
	},
	validate_email: function(str) {
		var reg = /^([a-zA-Z0-9_\.\-])+\@(([a-zA-Z0-9\-])+\.)+([a-zA-Z0-9]{2,6})+$/;
		var isOk = reg.test(str);
		return isOk;
	},
	validate_numeric: function(str) {
		
		return !isNaN(str);
	},
	validate: function(frm, beforepost, onsuccess, onerror, validateOnly) {
		
		var form = $(frm);
		if(form.length==0) {
			$(frm).prop("id", "msForm_"+ (PaymentValidation.counter++));
			//throw "Form id is undefined: "+frm;
		};	
		var frmId = $(frm).prop("id");
		if(beforepost) {
			$("#"+frmId).data("beforepost", beforepost);
		};
		if(onsuccess) {
			$("#"+frmId).data("onsuccess", onsuccess);
		};
		if(onerror) {
			$("#"+frmId).data("onerror", onerror);
		};
		if(validateOnly) {
			$("#"+frmId).data("validateOnly", true);
		} else {
			$("#"+frmId).data("validateOnly", false);
		}
		
		var allElements = $("#"+frmId+" [data-validation]:visible").not(".nonValidate");
			
		var isDifferent = $("#"+frmId).data("isdifferent");
		$("#"+frmId).data("isdifferent",false);
		
		for(var i=0;i<allElements.length;i++) {
			var current = allElements[i];
			
			var eType = $(current).prop("type");
			var is_error = false;
			var field_name = current.name;
			var error_holder = field_name+"_error";
			
			//check if multiple
			if(field_name.indexOf("[]")>0) {
				eType = "multiple";
				if(typeof current.id=="undefined" || current.id=="") {
					//found multiple. Need id for the same;
					throw "Need id for the field : "+field_name;
					return false;
				} else {						
					field_name = current.id;
					error_holder = field_name+"_error";
				};
			};
			
			switch(eType) {
				case "radio":
					if($("input."+field_name+":checked").length==0) {
						is_error = true;
					};
				break;
				case "checkbox":
					
					if(typeof current["id"] != "undefined") {
						$('#'+field_name).val('checked');
						if(document.getElementById(field_name).checked==false) {
							is_error = true;
							$('#'+field_name).val('');
						};
					} else {
						$("input."+field_name).val('checked');
						if($("input."+field_name+":checked").length==0) {
							is_error = true;
							$("input."+field_name).val('');
						};
					}
				break;
				case "select":
				case "multiple":
					if($("#"+field_name).val()==null) {
						is_error = true;
					};
				break;					
				default:
					if($.trim($(current).val())=="" || $(current).val()==null) {
						is_error = true;
						
						//safe side;
						field_name = current["id"] || field_name;
					};
				break;
			}
			
			
			//check input type
			//get field ID if field Name not same as field ID;
			field_name = (current["id"] != field_name) ? current["id"] : field_name;
			error_holder = field_name+"_error";
			
			//var data = $("#"+field_name).data();
			//var ruletype = data["validation"];
			
			var ruletype = $("#"+field_name).attr('data-validation');
			
			// if no ruletype found continue to next element
			if(ruletype != null && ruletype != "" && ruletype != undefined) {
				var	ruleTypeArr = ruletype.split('|'),
				errorMsg = $("#"+field_name).attr('data-msginfo');
				var	errorMsgArr = errorMsg.split("|");
				
				IE_Placeholder.removeDefaultText($("#"+field_name));
				
				for(var index=0;index<ruleTypeArr.length;index++) {
					var value = $("#"+field_name).val();
					var rule = ruleTypeArr[index];
					var	ruleSplit = rule.split(/\[|,|\]/);  // split string ex: sameValue[origin,destination]
					//console.log("ruleSplit " + ruleSplit[0]);
					
					switch(ruleSplit[0]){
					
					
					case "required" :
							if(value == null || value == undefined) {
								return showValidationError(frmId,field_name, error_holder, errorMsgArr[index]);
							} else  {
								if($.trim($(current).val()) == "") {
									return showValidationError(frmId,field_name, error_holder, errorMsgArr[index]);
								}
							}
							break;
					case "regType" :
						
							var type = ruleSplit[1];
							var regex;
							if(type=="alpha"){
								regex = /^\s*[a-zA-Z\s]+\s*$/;
							}else if (type=="number") {
								regex = /^[0-9\ ]+$/;
							}else if (type=="onlyLetter") {
								regex = /^[a-zA-Z\ ]+$/;
							}
							
							if(regex.test(value) == false){
								return showValidationError(frmId,field_name, error_holder, errorMsgArr[index]);
							}
							break;
					case "validateLuhn" :
						
							var cardNo  = value.replace (/[\s-]/g, ""); // remove spaces and dashes
							
							var doubleDigit = false;
							var checkSum = 0;
							for(var j=cardNo.length - 1; j>=0; j--){
								var digit = value.substr(j,1);
								 if(doubleDigit == true)
								   {
									 	var pNum = parseInt(2*digit);
									 	var digitSum = 0;
										while(pNum > 0)
										{
											digitSum += pNum % 10;
											pNum = parseInt(pNum/10);
										}
									 	checkSum += digitSum;
								   }
								   else
								   {
									   	checkSum += parseInt(digit);
								   }   
								 doubleDigit = !doubleDigit;
							}
							if (checkSum % 10 != 0){
							    YatraPaymentUI.recordEventInSentry("Card Luhn Validation Failure", "info",
                                            								"invalid-card");
								return showValidationError(frmId,field_name, error_holder, errorMsgArr[index]);
							}
							break;
					case "cardRegType" : 
						
							var type = ruleSplit[1];
							var cardName = type.replace(/[[\]]/g,'');
							var cards = new Array();
							cards [0] = {cardName: "Visa", lengths: "13,16", prefixes: "4"};
							cards [1] = {cardName: "Master", lengths: "16", prefixes: "51,52,53,54,55,222"};
							cards [2] = {cardName: "Diners", lengths: "14", prefixes: "300,301,302,303,304,305,36,38,3095"};
							cards [3] = {cardName: "Amex", lengths: "15", prefixes: "34,37"};
							cards [4] = {cardName: "Maest", lengths: "16,18,19", prefixes: "50,56,57,58,6"};
							cards [5] = {cardName: "Dinners", lengths: "14", prefixes: "300,301,302,303,304,305,36,38,3095"};
							cards [6] = {cardName: "Rupay", lengths: "16", prefixes: "000000-999999"};
							cards [7] = {cardName: "BAJAJ", lengths: "16", prefixes: "203040"};
							
							var cardType = -1;
							for (var j=0; j<cards.length; j++) {
								if (cardName.toLowerCase() == cards[j].cardName.toLowerCase()) {
									cardType = j;
									break;
								}
							}

							if (cardType == -1) {
							    YatraPaymentUI.recordEventInSentry("Card Type Not Found", "info",
                                                                    "invalid-card", "cardName", cardName);
								return showValidationError(frmId,field_name, error_holder, errorMsgArr[index]);
							} // card type not found
								
							value = value.replace (/[\s-]/g, ""); // remove spaces and dashes
								
							if (value.length == 0) {
							    YatraPaymentUI.recordEventInSentry("Card Length Zero", "info", "invalid-card");
								return showValidationError(frmId,field_name, error_holder, errorMsgArr[index]);
							} // no length

							var cardNo = value;
							var cardexp = /^[0-9]{13,19}$/;
							if (!cardexp.exec(cardNo)) {
							    YatraPaymentUI.recordEventInSentry("Card Invalid - either has chars or wrong length", "info", "invalid-card");
								return showValidationError(frmId,field_name, error_holder, errorMsgArr[index]);
							} // has chars or wrong length

							cardNo = cardNo.replace(/\D/g, ""); // strip down to digits
								
							var lengthValid = false;
							var prefixValid = false;
							var prefix = new Array ();
							var lengths = new Array ();

							prefix = cards[cardType].prefixes.split(","); 
							if(cards[cardType].cardName == "Rupay") {
								var bin = parseInt(cardNo.substring(0, 6));
								for (var prefixIndex = 0; prefixIndex < prefix.length; prefixIndex++) {
									var lowerLimit = parseInt(prefix[prefixIndex].split("-")[0]);
									var upperLimit = parseInt(prefix[prefixIndex].split("-")[1]);
									if (bin >= lowerLimit && bin <= upperLimit) {
										prefixValid = true;
										break;
									}
								}	
							} else {
								for (var j=0; j<prefix.length; j++) {
									var exp = new RegExp ("^" + prefix[j]);
									if (exp.test (cardNo)) prefixValid = true;
								}	
							}
							
							if (!prefixValid) {
							    YatraPaymentUI.recordEventInSentry("Card Invalid Prefix", "info", "invalid-card");
								return showValidationError(frmId,field_name, error_holder, errorMsgArr[index]);
							} // invalid prefix

							lengths = cards[cardType].lengths.split(",");
							for (var j=0; j<lengths.length; j++) {
								if (cardNo.length == lengths[j]) lengthValid = true;
							}
							if (!lengthValid) {
							    YatraPaymentUI.recordEventInSentry("Card Invalid Length", "info", "invalid-card");
								return showValidationError(frmId,field_name, error_holder, errorMsgArr[index]);
							} // wrong length
							break;	
					case "supportedCard" : 
		 					
							var type = ruleSplit[1];
							var PaymentOption = ruleSplit[2];
		 					var cardName = type.replace(/[[\]]/g,'');
		 					var cards = new Array();
		 					cards [0] = {cardName: "Visa", payOption : "cc,dc,dcpin,emi,rewards"};
		 					cards [1] = {cardName: "Master", payOption : "cc,dc,dcpin,emi,rewards"};
		 					cards [2] = {cardName: "Diners", payOption : "cc,dc,dcpin,emi"};
		 					cards [3] = {cardName: "Amex", payOption : "cc,emi"};
		 					cards [4] = {cardName: "Maest", payOption : "dc,dcpin"};
		 					cards [5] = {cardName: "Dinners", payOption : "cc,dc,dcpin,emi"};
		 					cards [6] = {cardName: "Rupay", payOption : "cc,dc,dcpin"};
		 					cards [7] = {cardName: "BAJAJ", payOption : "emi"};
		 					
		 					
		 					var cardType = -1;
		 					for (var j=0; j<cards.length; j++) {
		 						if (cardName.toLowerCase() == cards[j].cardName.toLowerCase()) {
		 							cardType = j;
		 							break;
		 						}
		 					}
		 					
		 					if (cardType == -1) { 
		 						return showValidationError(frmId,field_name, error_holder, errorMsgArr[index]);
		 					} // card type not found
		 					
		 					var paymentOptionValid = false;
		 					var paymentOptions = new Array();
		 					paymentOptions = cards[cardType].payOption.split(",");
		 					for (var j=0; j<paymentOptions.length; j++) {
		 						if (PaymentOption == paymentOptions[j]) paymentOptionValid = true;
		 					}
		 					if (!paymentOptionValid) {
		 						return showValidationError(frmId,field_name, error_holder, errorMsgArr[index]);
		 					} // invalid paymentOption

		 					break;
					case "enabledCard" :
						
							var cardType = ruleSplit[1];
							var enabledCardList = ruleSplit[2];
							var cardName = cardType.replace(/[[\]]/g,'');

							var cardTypeEnabled = false;
							var enabledCards = new Array ();
							enabledCards = enabledCardList.split("/");
							
							for (var j=0; j<enabledCards.length; j++) {
								if (cardName == enabledCards[j]) cardTypeEnabled = true;
							}
							
							if (!cardTypeEnabled) { 
								return showValidationError(frmId,field_name, error_holder, errorMsgArr[index]);
							} // disabled cardType
						
							break;
					case "emiCardBankValidaton" :
						
						var cardNo = value;
						cardNo = cardNo.replace(/\D/g, "");
						
						var bank = $("#emiBank_select option:selected").val();
						var emiCardValidated = false;
						
						if(cardNo.length >=7){
							extendedBinNumber = cardNo.substr(0,7);
							var paymentOption = 'emi';
							var emiCardBankValidation = YatraPaymentUI.validateEMICardAndBank(extendedBinNumber, bank);
							if(emiCardBankValidation.is_successful != null && emiCardBankValidation.is_successful != '') {
								if(emiCardBankValidation.is_successful == "true") {
									emiCardValidated = true;
	                            }
	                        }
						}	
						if (!emiCardValidated) { 
							return showValidationError(frmId,field_name, error_holder, emiCardBankValidation.message);
						} 
					
						break;
					case "checkExpiryDate" : 
							
							var type = ruleSplit[1];
							var monthElm = $("#" + type + "_expm_id");
							var yearElm = $("#" + type + "_expy_id");
							
							expMonth = monthElm.val();
							expYear = yearElm.val();
							
							var val="";
							val+=expMonth+expYear;
							elmVal=val;
							
							maxLen = parseInt(monthElm.attr("maxlength"))+parseInt(yearElm.attr("maxlength"));
							
							// Should not check if no expiry month/year provided
							if(elmVal.length == 0){
								break;
							}else if(elmVal.length < maxLen){
								return showValidationError(frmId,field_name, error_holder, errorMsgArr[index]);
							}
							
							var expVal = elmVal;
							var regExp = /[0-9]*$/;
							if (!regExp.exec(expVal)) { 
								return showValidationError(frmId,field_name, error_holder, errorMsgArr[index]); 
							} // has chars or wrong length
							
							today = new Date();
							month = today.getMonth();
							if (navigator.appName == 'Microsoft Internet Explorer'){
								year = today.getYear();
							}else{
								year = today.getYear()+1900;
							}
							month = eval(month + 1);
							
							expMonth = eval(expMonth);
							expYear = eval(expYear);
							
							if ((expYear < year) || (expMonth > 12 || expMonth < 1) || (expYear == year && expMonth < month)) {
								return showValidationError(frmId,field_name, error_holder, errorMsgArr[index]);
							}
							
							break;
						
					case "lenCheck" : 
						
							var len = ruleSplit[1];
		 				   	if (len!= undefined) {
		 					   var checkLen = parseInt(len.replace(/[[\]]/g,''));
		 					   if (value.length != checkLen) {
		 						  return showValidationError(frmId,field_name, error_holder, errorMsgArr[index]);
		 					   }
		 				   	}
				 			break;
					case "minLenCheck" : 
						
						var len = ruleSplit[1];
	 				   	if (len!= undefined) {
	 					   var checkLen = parseInt(len.replace(/[[\]]/g,''));
	 					   if (value.trim().length < checkLen) {
	 						  return showValidationError(frmId,field_name, error_holder, errorMsgArr[index]);
	 					   }
	 				   	}
			 			break;

			 		// Check for reward amount
			 		case "internationalMobileRangeCheck" :
			 		    var payop = ruleSplit[1];
			 		    var isdValue = $("#dc_baisd").val();
			 		    if(payop == 'cc') {
			 		        isdValue = $("#cc_baisd").val();
			 		    } else if (payop == 'qbcc') {
                            isdValue = $("#temp_baisd").val();
			 		    }

			 		    if(isdValue == '91') {
			 		        //for indian isd, check mobile length should be 10
			 		        if(value.length != 10) {
			 		            return showValidationError(frmId,field_name, error_holder, errorMsgArr[index]);
			 		        }
			 		    } else {
			 		        if(value.length < 4 || value.length > 17) {
                            	return showValidationError(frmId,field_name, error_holder, errorMsgArr[index]);
                            }
			 		    }

			 		    break;

					case "checkReward" :
							var paymentJSON = getPaymentJSON();
							var totAmount = parseInt(paymentJSON.cartAmount);
							value = parseInt(value);
							if(value < 0 || value>totAmount) {
								return showValidationError(frmId,field_name, error_holder, errorMsgArr[index]);
						    } 
							break;	
					case "emailCheck" :
						if(PaymentValidation.validate_email(value)) {
							return showValidationError(frmId,field_name, error_holder, errorMsgArr[index]);
						}
						break;

					case "validBankSelectedCheck" :
					    var selectedOption = $("#dcpinBank option:selected").val();
                    	if(selectedOption == "") {
                    		return showValidationError(frmId,field_name, error_holder, errorMsgArr[index]);
                    	}
                    	break;

					case "resetPassCheck" :
						 var newPassword = $("#qb_newPassword_id").val();
						 var confirmPassword = $("#qb_confirmPassword_id").val();
						 if(newPassword != confirmPassword) {
							 return showValidationError(frmId, field_name, error_holder, errorMsgArr[index]);
						 }
						 break;
						 
					case "oldPassCheck" :
						 var oldPassword = $("#qb_oldPassword_id").val();
						 var newPassword = $("#qb_newPassword_id").val();
						 if(oldPassword == newPassword) {
							 return showValidationError(frmId, field_name, error_holder, errorMsgArr[index]);
						 }
						 break;
					case "lengthCheck" :
						
						if((value.length >= 8) && /^(?=.*[a-z])(?=.*[0-9]).+$/.test(value)) {
							break;
						} else { 
							return showValidationError(frmId, field_name, error_holder, errorMsgArr[index]);
						}
						break;
					case "checkCvv" : 
						
		                    if(value && value.indexOf("--") == -1){
		                    	 if(/^[0-9\ ]+$/.test(value)){
		                    		 if (value.length == 3) {
			    						  break;
			    					   }else{
			    						   return showValidationError(frmId,field_name, error_holder, errorMsgArr[index]);
			    					   }
		                    	 }else{
		                    		 return showValidationError(frmId,field_name, error_holder, errorMsgArr[index]);
		                    	 }
		                    }else{
		                    	break;
		                    }
					case "checkVA" :
						 var mobileNumberRegex = /^\d{10}$/;
						 var regex = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9]+$/;
                         if(regex.test(value) == false && mobileNumberRegex.test(value) == false){
							return showValidationError(frmId,field_name, error_holder, errorMsgArr[index]);
						 }
						 break;
					case "checkVAMobile" :
						 var regex = /^[0-9\ ]+$/;
						 if(regex.test(value)){
							 if (value.length == 10) {
		   						  break;
							 }else{
	                    		 return showValidationError(frmId,field_name, error_holder, errorMsgArr[index]);
	                    	 }
	                    } else {
	                    	return showValidationError(frmId,field_name, error_holder, errorMsgArr[index]);
	                    }
						 break;	 
					
					default:
						var regExp = new RegExp(regtype, "g");
						var isOkay = regExp.test($("#"+field_name).val()); //This is a fake call - It's bug of RegExp - Amazing? 
						//trace("isOkay "+isOkay);
						if(regExp.test($("#"+field_name).val())==false) {
							Utils.scrollWin(document.forms[frmId][field_name]);
							showErrorMessage(field_name, error_holder, errorMsgArr[index]);	
							return false;
						};
					break;
					}
				}
			}
		}
		
		var success = true;
		if($("#"+frmId).data("beforepost")) {
			success = $("#"+frmId).data("beforepost")();
		}
		var isajax = $("#"+frmId).data("isajax");
		var validateOnly = $("#"+frmId).data("validateOnly");
		if(validateOnly==true) {				
			if(onsuccess) {
				onsuccess("validated" ,isDifferent);
			}
			return false;
		}
		if(isajax && isajax.toString() == "true" && success==true) {
			var url = $("#"+frmId).prop("action");
			$.ajax({
				url:url,
				type:"post",
				data:$("#"+frmId).serialize(),
				success: function(res) {
					var onsuccess = $("#"+frmId).data("onsuccess");
					if(onsuccess) {
						onsuccess(res);
					}
				},
				error: function(res) {
					var onerror = $("#"+frmId).data("onerror");
					if(onerror) {
						onerror(res);
					}
				}
			});
			return false;
		} else if(success==true) {
			return true;
		} else {
			return false;
		}
		
		
		//adding onkey input box
		$("#"+frmId+" [data-validation]").bind("keydown", function() {
			$(".error:visible").hide();
		})
		
		
		function showValidationError(frmId,field_name, error_holder, errorMsg){
			var dd = $('#' + field_name).data("dd"); //hack for dropdown 
			if(dd) {
				field_name = dd.get("id")+"_msdd";
			};
			$("#"+field_name).addClass('error');
					setTimeout(function(){
						$("#"+field_name).removeClass('error');
			},4000);
			showErrorMessage(field_name, error_holder, errorMsg);	
			document.getElementById(field_name).focus();
			return false;
		}
		
		function showErrorMessage(field_name, error_holder, msg) {
			
			errormsg = "";
			
			// PRIORITY I : Precedence given to the data-msginfo attribute
			if(msg != null && msg != "" && msg != undefined)
			{
				errormsg = msg;
			}
			else
			{
				var htmlError = $("#"+error_holder).val();
				
				// PRIORITY II : Then, the error holder is checked
				if(htmlError != null && htmlError != "" && htmlError != undefined)
				{
					FX.come("#"+error_holder);
					return;
				}
				else
				{
					field_name = field_name.id || field_name.name;
					errormsg = field_name +" is required...";
				}
			}
			
			Toast.show(errormsg, field_name);
		}
		
	}
};
var appProp = appProp || {};
//CommonActions.enableCarousel();
var PaymentCommonActions = {
	ids: {
		module_tab_class: "jsModules"
	},
	constants: {
		module_ajaxURL: (appProp && appProp.frescoServiceUrl) ? appProp.frescoServiceUrl : ""
	},
	init: function() {		
		this.initAction();
		this.customSelect();
	},
	getCookie: function(name) {
		var i,x,y;
		var ARRcookies=document.cookie.split(";");
		for (i=0;i<ARRcookies.length;i++) {
		  x=ARRcookies[i].substr(0,ARRcookies[i].indexOf("="));
		  y=ARRcookies[i].substr(ARRcookies[i].indexOf("=")+1);
		  x=x.replace(/^\s+|\s+$/g,"");
		  if (x==name) {
			return unescape(y);
		  }
		}
	},
	getDeviceName: function() {
		var width = $(window).width();
		var device = "desktop";
		if(width>480 && width<=920) {
			device = "tab";
		}
		return device;
	},
	getCarouselProp: function() {
		var deviceName = this.getDeviceName();
		var carsouselProp = {desktop:{width:538, height:167, displayItems:3}, 
								 tab:{width:325, height:167, displayItems:2}};
		return carsouselProp[deviceName];
	},
	customSelect: function(){
		/************start Custom select***********/
		try {
			$('select:not(".template")').msDropdown({enableAutoFilter:true}).bind('change',function(evt){
				if(typeof(BE_Hotels) != "undefined" && typeof($(this).attr(BE_Hotels.ids.agegroup_attribute)) != "undefined"){
					BE_Hotels_Action.onChangeSelectValue(this);
				}else if(typeof(BE_FlightsHotels) != "undefined" && typeof($(this).attr(BE_FlightsHotels.ids.agegroup_attribute)) != "undefined"){
					BE_FlightsHotels_Action.onChangeSelectValue(this);
				}else if(typeof(BE_Flights) != "undefined" && typeof($(this).attr(BE_Flights.ids.agegroup_select_attribute)) != "undefined"){
					BE_Flights_Action.onChangeSelectValue(this);
				}else if(typeof(gMap) != "undefined" && typeof($(this).attr(gMap.ids.city_select_attribute)) != "undefined"){
					gMapAction.onChangeSelectCity(this);
				}
			});
			
		} catch(e) {
			//console.log(e);	
		}
		/************eof Custom select***********/
	},
	oTab:null,
	enableTouch: function(){
		//touch scroll
		var oTabs = this.oTabs;
		$("#touchScroll").msTouch(
			{
			onLeft:function() {
				$(".box").hide();
				$(".box:first").show(1000);
				oTabs.switchTabByCounter(2);
			},
			onRight:function() {
				$(".box").hide();
				$(".box:last").show(1000);
				oTabs.switchTabByCounter(0);
			}
		});
	},
	enableCarouselTouch: function() {		
		if($.fn.msTouch) {
			var carsouselProp = this.getCarouselProp();
			$(".mscarousel").msTouch({
				onLeft: function(res) {
					//$(this).find(".msCarouselNext, .next").trigger("click");
					var data = $(this).data();
					var carousel = data["msCarousel"];
					var items = carousel.items();
					var current = carousel.getCurrentId();
					var visibleItem = data["visibleitem"] || carsouselProp.displayItems;
					visibleItem = parseInt(visibleItem);
					var nextItem = (current+visibleItem);					
					//carousel.goto(nextItem);
				},
				onRight: function() {
					var data = $(this).data();
					var carousel = data["msCarousel"];
					var items = carousel.items();
					var current = carousel.getCurrentId();
					var visibleItem = data["visibleitem"] || 3;
					visibleItem = parseInt(visibleItem);					
					var nextItem = (current-visibleItem);
					nextItem = (nextItem < 0) ? 0 : nextItem;
					//carousel.goto(nextItem);
					//$(this).find(".msCarouselPrevious, .prev").trigger("click");
				}
			})
		}
	},
	enableCustomScroller: function() {
	// custom vertical scroller
		if($.fn.customScrollbar) {
			$(".scrollContainer").customScrollbar({
			  skin: "default-skin", 
			  hScroll: false,
			  updateOnWindowResize: true
			});
		};
	},
	fixUI: function() {
		this.enableCustomScroller();
		this.customSelect();
	},
	enableCarousel: function(div) {
			//Carousel
		try {
			var carsouselProp = this.getCarouselProp();
			$(".carouseldiv" || div).each(function() {
				$(this).msCarousel({
					boxClass:'div.singleOffer, div.comments, li.matrixTopCarousel',
					width:$(this).data("carouselwidth") || carsouselProp.width,
					height:$(this).data("carouselheight") || carsouselProp.height,
					scrollSpeed:$(this).data("carouselspeed") || 500,
					callback:function(arg) {
						var element_id = arg.prop.element_id;
						if(arg.getCurrentId()==0) {
							$("#"+element_id + " .prev").addClass("prevDisabled");
							$("#"+element_id + " .iconSpekPre").addClass("prevDisabled");
						} else {
							$("#"+element_id + " .prev").removeClass("prevDisabled");
							$("#"+element_id + " .iconSpekPre").removeClass("prevDisabled");
						};
					}
					
				});				
				//check if this has less item
				if($(this).find("li.matrixTopCarousel").length<=carsouselProp.displayItems) {
					$(this).find(".next, .prev").hide();
				};
			})			
			
			if(!this.enableCarousel.isInit) {
				this.enableCarousel.isInit = true;
				$(".carouseldiv").parent().find(".next, .iconSpekNxt").on("click", function() {
					var carousel = $(this).parent().find(".carouseldiv").data("msCarousel");
					var totalItem = carousel.item().length;
					var currentItem = carousel.getCurrentId();
					var dispalyedItem = $(this).data("carouseldisplayitem") || carsouselProp.displayItems;
					//console.log("dispalyedItem "+dispalyedItem)
					if(dispalyedItem!=1) {
					if(currentItem != totalItem-dispalyedItem) {
						carousel.next();
					};
					if(currentItem == totalItem-dispalyedItem) {
						//carousel.goto(0);
					};					
					} else {
						carousel.next();
					};
				})
				
				$(".carouseldiv").parent().find(".prev, .iconSpekNxt").on("click", function() {
					var carousel = $(this).parent().find(".carouseldiv").data("msCarousel");
					carousel.previous();
				})			
				this.enableCarouselTouch();
			}
			
		} catch(e) {
			//alert(e.message);
		}		
	},
	initAction: function(){		
		var $this = this;		
		//module loader
		$('.'+this.ids.module_tab_class).on("click",function(){
			var active_class = $(this).data('active');
			if(!$(this).hasClass(active_class)){
				MS_Loader.showLoader(this, "Please wait...", false);
				var parent_nav = $(this).data('parent');
				$(this).parents(parent_nav).find('a.'+$this.ids.module_tab_class).removeClass(active_class);
				$(this).addClass(active_class);
				var module_name = $(this).data('module');
				var module_ajaxURL = $this.constants.module_ajaxURL;
				var module_placer = $(this).data('place');
				var module_ajaxURL_parameter = {};
				module_ajaxURL_parameter.name = module_name;
				module_ajaxURL = module_ajaxURL + "?" + common_func.objToUrl(module_ajaxURL_parameter);
				CustomLoader.ajax({
					url:module_ajaxURL,
					noCache:false,
					type:"POST",
					dataType: "html",
					success:function(res) {
						MS_Loader.hideLoader();
						$('#'+module_placer).html(res);
						$this.fixUI();
					},
					error: function() {
						MS_Loader.hideLoader();
						console.log('module: '+module_name+'is not coming');						 
					}
				});
			}
		});
		
		//Home page big image carousal
		if($.fn.msTabs) {
		this.oTabs = $(".tabholder").msTabs({tabs:'li', effects:'none', speed:'fast', selected:'active', event:'mouseover'}).data("msTabs");
		}
		
		this.enableCarousel();
		//Header selected navs
		$('.nav-tabs li').on("click", function(){
			$('.nav li').removeClass('active');
		  $(this).addClass('active');
		});
		
		$('.jsWinopen').on("click", function(){
			window.location = $(this).data("url");
		});
		
		// custom vertical scroller
		this.fixUI();		
		
		//footer country dropdown
		$('.dropdown').hover(function(){
			$(this).addClass("active");
			$(this).find('ul:first').show();
		}, function() {
			$(this).removeClass("active");
			$(this).find('ul:first').hide();
		});
	}
};

var eCashJSON = null;
var redeemAuthJSON = null;
var reverseAuthJSON = null;
var breakUpJson = null;
var disableEcash = false;

var YatraEWalletPaymentUI = {

		eCashJsonURL : "/checkout/wallet/get-ecash.htm",
		redeemAuthURL : "/checkout/wallet/auth.htm",
		reverseAuthURL :  "/checkout/wallet/reverse-auth.htm",
		redeemCapURL : "/checkout/wallet/cap.htm",
		breakUpInfoURL : "/checkout/wallet/get-breakup-info.htm",
		sliderMaxLimit : 0,
		expEcash : 0,
		nexpEcash : 0,
		isReverseAuthWhenFullEcashAndGST : false,

		init : function() {
			if(enableEWallet && isAdditionalPaymentAttempt!="true") {
				this.updateEWalletHTML();

				$("#redeem-ecash-button").on('click', function(e, mode) {
					var amountTobeRedeemed = YatraEWalletPaymentUI.getAmountToBeRedeemed();
					
					if(detailsJson.convFeePercent && totalCartAmountWithoutConvFeesPercentage <= amountTobeRedeemed){
						$("#upi").trigger( "click" );
					}
					// need to update the amount to be redeemed after change in payment option
					var amountTobeRedeemed = YatraEWalletPaymentUI.getAmountToBeRedeemed();
					$('#ecash-div .eCash-loader').show()
					var paymentJSON = getPaymentJSON();
					if(roundOffCart){
						var amount = parseInt(paymentJSON.amount);
	       			 }else{
	        	var amount = paymentJSON.amount.toFixed(2);
	        }
					
				
					if(YatraEWalletPaymentUI.isBookingForEcashGV()) {
					    YatraEWalletPaymentUI.redeemForEcashGV(mode);
					    return;
					}
					// for ecash redemption in bin base voucher
					if(GV.isPGPaymentRequiredForVoucher){
						amountTobeRedeemed = amountTobeRedeemed -1;
						}
					if(amountTobeRedeemed > 0) {
						if (amount >= amountTobeRedeemed) {
							YatraEWalletPaymentUI.redeemAuth(amountTobeRedeemed);
							YatraEWalletPaymentUI.logWalletRedemptionInSentry(mode, amountTobeRedeemed);
						}
					} else {
						YatraEWalletPaymentUI.setErrorMessage("Amount must be greater than 0.");
					}
				});

				$("#cancelRedemption").on('click', function() {
					$('#ecash-div .eCash-loader').show();
					YatraEWalletPaymentUI.reverseAuth();
					YatraPaymentUI.recordEventInSentry("Ecash Cancel Redemption", "info", "Ecash Redemption");
				});

				$("#okgotitlink").click(function(){
					$("#ecash-tupple").hide();
				});
			}
		},

        redeemForEcashGV : function(mode) {
            var amountTobeRedeemed = YatraEWalletPaymentUI.getEcashForGVAmountToBeRedeemed();
            if(amountTobeRedeemed > 0) {
                YatraEWalletPaymentUI.redeemAuth(amountTobeRedeemed);
            	YatraEWalletPaymentUI.logWalletRedemptionInSentry(mode, amountTobeRedeemed);
            } else {
                YatraEWalletPaymentUI.setErrorMessage("Amount must be greater than 0.");
            }
        },

		logWalletRedemptionInSentry : function (mode, amountRedeemed) {
			if(mode != undefined && mode == "auto"){
				YatraPaymentUI.recordEventInSentry("Ecash Auto Redemption", "info",
						"Ecash Redemption", "amount", amountRedeemed);
			} else {
				YatraPaymentUI.recordEventInSentry("Ecash Manual Redemption", "info",
						"Ecash Redemption", "amount", amountRedeemed);
			}
		},

		updateEWalletHTML : function() {
			// Get ssoToken from cookies.
			var ssoToken = YatraQBPaymentUI.getSSOToken();
			if(enableEWallet && ssoToken != undefined && ssoToken != null) {
				this.getEWalletJSON();
			} else {
				$("#ecash-div").hide();
			}
		},

		getEWalletJSON : function() {
			var paymentJSON = getPaymentJSON();
			if(paymentJSON != undefined && paymentJSON != null) {
				var d = new Date();
				var dt = d.getTime();
				try{
					$.ajax({
						url : this.eCashJsonURL + "?date="+dt,
						type : 'POST', 
						data : {'cust_email' : paymentJSON.userEmail, 'merchant_code' : paymentJSON.merchant, 'product_code' : paymentJSON.product, 'super_pnr' : paymentJSON.superPNR}, 
						cache : false, 
						success : function(response) {
							eCashJSON = $.parseJSON(response);
							YatraEWalletPaymentUI.setEWalletUI();
							YatraEWalletPaymentUI.automaticallyRedeemECash();
						}, error : function(request, status, error) {
							eCashJSON = YatraEWalletPaymentUI.getErrorJSON("Error occurred while getting eCash JSON");
						}
					});		
				}catch (e){

					console.log(e);	
				}			
			}
		},

		automaticallyRedeemECash : function (){
			var paymentJSON = getPaymentJSON();
			if(enableEWallet && ((detailsJson.autoRedeemECash && detailsJson.autoRedeemECash == "true") ||
			    (YatraEWalletPaymentUI.isBookingForEcashGVAndToHideEcashSlider())||
			    (paymentJSON.wallet.previousEcashRedeemed && parseInt(paymentJSON.wallet.previousEcashRedeemed) > 0 ))){
				$("#redeem-ecash-button").trigger('click', 'auto');
			}	
		},

		setEWalletUI : function() {
			if (enableEWallet && eCashJSON != null && eCashJSON.status == true) {
				var totalRedeemableEcash = YatraEWalletPaymentUI.getAmountInRupee(eCashJSON.totalEcash.redeemableECashInPaisa);
				if (totalRedeemableEcash > 0) {
					// Setting total amount
					var paymentJSON = getPaymentJSON();
					if(roundOffCart){
	        	 }else{
	        	paymentJSON.amount = paymentJSON.amount.toFixed(2)
	        }
					
					
					$(".total-amount-due").html(YatraPayment.formatAmount(paymentJSON.amount));

					// Setting redeemable eCash for this booking
					var maxECashRedeemableForThisBooking = 0;
					var maxNonExpirableRedeemableForGV = 0;
					var redeemableExpirableECash = 0;
					var redeemableNonExpirableECash = 0;
					var eCashToBeRedeemed = 0;
					var node1 = eCashJSON.ECashList[0];
					var node2 = eCashJSON.ECashList[1];

					if (node1 != null) {
						if (node1.eCashType == "EXPIRABLE") {
							redeemableExpirableECash =  YatraEWalletPaymentUI.getAmountInRupee(node1.redeemableECashInPaisa);
						} else {
							redeemableNonExpirableECash = YatraEWalletPaymentUI.getAmountInRupee(node1.redeemableECashInPaisa);
						}
					}

					if (node2 != null) {
						if (node2.eCashType == "EXPIRABLE") {
							redeemableExpirableECash = YatraEWalletPaymentUI.getAmountInRupee(node2.redeemableECashInPaisa);
						} else {
							redeemableNonExpirableECash = YatraEWalletPaymentUI.getAmountInRupee(node2.redeemableECashInPaisa);
						}
					}
					
					

					if (redeemableExpirableECash < paymentJSON.wallet.maxRedeemableECash) {
						maxECashRedeemableForThisBooking = parseInt(redeemableExpirableECash) + parseInt(redeemableNonExpirableECash);
					} else {
						maxECashRedeemableForThisBooking = parseInt(paymentJSON.wallet.maxRedeemableECash) + parseInt(redeemableNonExpirableECash);
					}

					if(YatraEWalletPaymentUI.isBookingForEcashGV()) {
                        var totalCartAmount = parseInt(paymentJSON.wallet.maxRedeemableECash) + paymentJSON.amount;
                    	if (maxECashRedeemableForThisBooking > totalCartAmount) {
                    	    maxECashRedeemableForThisBooking = totalCartAmount;
                    	}
                    } else {
                        if (maxECashRedeemableForThisBooking > paymentJSON.amount) {
                    		maxECashRedeemableForThisBooking = paymentJSON.amount;
                    	}
                    }

                    maxNonExpirableRedeemableForGV = parseInt(redeemableNonExpirableECash);

                    var shouldbeMaxNonExpirableForGV = ShoppingCart.totalPayableAmount;
                    if (maxNonExpirableRedeemableForGV > shouldbeMaxNonExpirableForGV) {
						maxNonExpirableRedeemableForGV = shouldbeMaxNonExpirableForGV;
					}

					eCashToBeRedeemed = maxECashRedeemableForThisBooking;
					// override eCashToBeRedeemed in case customer had already tried redeeming ecash in previous payment attempt
					if(paymentJSON.wallet.previousEcashRedeemed > 0){
						eCashToBeRedeemed = parseInt(paymentJSON.wallet.previousEcashRedeemed);
						if(eCashToBeRedeemed > maxECashRedeemableForThisBooking){
							eCashToBeRedeemed = maxECashRedeemableForThisBooking;
						}
						YatraPaymentUI.recordEventInSentry("Override EcashToBeRedeemed", "info", "Ecash", "amountOverriden", eCashToBeRedeemed);
					}

					sliderMaxLimit = maxECashRedeemableForThisBooking;
					if(YatraEWalletPaymentUI.isBookingForEcashGV() && maxNonExpirableRedeemableForGV > 0) {
					    sliderMaxLimit = maxNonExpirableRedeemableForGV;
					}
					
					// for ecash redemption in bin base voucher
					if(GV.isPGPaymentRequiredForVoucher){
						maxECashRedeemableForThisBooking = maxECashRedeemableForThisBooking -1;
						sliderMaxLimit = sliderMaxLimit -1;
					}
					
					if(parseInt(redeemableNonExpirableECash) > 0 && detailsJson.maxRedeemableECash == 0){
						disableEcash = false;
						if(detailsJson.disableYlp)
							$("#ecash-warning-message").show();
						else $("#ecash-warning-message").hide();
					}else{
						$("#ecash-warning-message").hide();
						disableEcash = true;
					}

					if(maxECashRedeemableForThisBooking > 0) {
						YatraEWalletPaymentUI.resetSlider();
						$("#total-redeemable-ecash").html(YatraPayment.formatAmount(maxECashRedeemableForThisBooking));
						$("#totalECash").val(totalRedeemableEcash);
						$('#totalECash-Bal').html(parseInt(totalRedeemableEcash));
						$('#totalECash-Balj').html(parseInt(totalRedeemableEcash));
						$("#totalRedeemableECash").val(maxECashRedeemableForThisBooking);
						$("#eCashToBeRedeemed").val(eCashToBeRedeemed);

                        if(YatraEWalletPaymentUI.isBookingForEcashGV() && maxNonExpirableRedeemableForGV > 0) {
                            $("#total-redeemable-ecash").html(YatraPayment.formatAmount(maxNonExpirableRedeemableForGV));
                        }
						$("#nexpEcashForGVToRedeem").val(maxNonExpirableRedeemableForGV);
						$("#expEcashForGVToRedeem").val(parseInt(paymentJSON.wallet.maxRedeemableECash));

						if(YatraEWalletPaymentUI.isBookingForEcashGV()) {
						    if(YatraEWalletPaymentUI.toHideEcashSliderForEcashGV() && maxNonExpirableRedeemableForGV == 0) {
						        $("#ecash-div").hide();
						    } else {
						        $("#ecash-div").show();
						    }
						} else {
						    $("#ecash-div").show();
						}

                        if(YatraEWalletPaymentUI.isBookingForEcashGV()) {
                            $("#ecashGVAmount").text($("#expEcashForGVToRedeem").val());
                            $("#ecashGV").show();
                        }else {
                            $("#ecashGV").hide();
                        }

						var objForSentry = {
								"totalRedeemableEcash": maxECashRedeemableForThisBooking,
								"totalEcash": totalRedeemableEcash,
								"eCashToBeRedeemed": eCashToBeRedeemed,
								"ylpMax" : paymentJSON.wallet.maxRedeemableECash,
								"autoRedeemECash" : detailsJson.autoRedeemECash
						};
						YatraPaymentUI.recordEventInSentryWithObj("Ecash Div Load", "info", "Ecash", objForSentry);
					} else {
						 $("#ecash-div").hide();
					}
				}
			} 
		},

        isBookingForEcashGV: function() {
            return (typeof(detailsJson.isOnlyEcashGV) != "undefined" && detailsJson.isOnlyEcashGV != undefined);
        },

        toHideEcashSliderForEcashGV: function() {
            return ( typeof(detailsJson.hideEcashSlider) != "undefined" && detailsJson.hideEcashSlider != undefined &&
                    detailsJson.hideEcashSlider == "true");
        },

        isBookingForEcashGVAndToHideEcashSlider: function() {
            return (YatraEWalletPaymentUI.isBookingForEcashGV() && YatraEWalletPaymentUI.toHideEcashSliderForEcashGV());
        },

		redeemAuth : function(amountToBeRedeemed) {
			YatraEWalletPaymentUI.disableRedeemButton();
			var paymentJSON = getPaymentJSON();
			var d = new Date();
			var dt = d.getTime();
			var makeAsync = true;
			var isGSTEnabled = YatraPaymentUI.isGSTEnabled(detailsJson.isGSTEnabled);
			if(isGSTEnabled){
				makeAsync = false;
			}
			$('#ecash-div .eCash-loader').show();
			$.ajax({
				url : this.redeemAuthURL + "?date="+dt,
				type : 'POST',
				async: makeAsync,
				timeout:"5000", 
				data : {
					'cust_email' : paymentJSON.userEmail, 'amount' : amountToBeRedeemed, 'ttid' : paymentJSON.superPNR,
					'merchant_code' : paymentJSON.merchant, 'product_code' : paymentJSON.product, 'ylp_max' : paymentJSON.wallet.maxRedeemableECash,
					'product' : paymentJSON.wallet.product, 'channel' : paymentJSON.wallet.channel, 'device' : paymentJSON.wallet.device
				}, cache : false, 
				success : function(response) {
					redeemNow = false;
					redeemAuthJSON = $.parseJSON(response);
					YatraEWalletPaymentUI.handleRedeemAuth();
				}, error : function(request, status, error) {
					redeemAuthJSON = YatraEWalletPaymentUI.getErrorJSON("Error occurred while redemption of eCash.");
					YatraEWalletPaymentUI.enableRedeemButton();
					$('#ecash-div .eCash-loader').hide();
				}
			});
		},

		getRedeemedECashBreakUp : function(walletId) {
			var paymentJSON = getPaymentJSON();
			$.ajax({
				url : this.breakUpInfoURL,
				type : 'POST',
				async: false,
				timeout:"5000",
				data : {
					'wallet_id' : walletId, 'ttid' : paymentJSON.superPNR, 'validate' : 'true', 'cust_email' : paymentJSON.userEmail
				}, cache : false, 
				success : function(response) {
					breakUpJson = $.parseJSON(response);
				}, error : function(request, status, error) {
					breakUpJson = YatraEWalletPaymentUI.getErrorJSON("Error occurred while trying to get BreakUp of eCash.");
					YatraPaymentUI.recordEventInSentry("Error in receiving walletBreakUp Info Controller Response", "info",
					"walletBreakUp Info Error");
				}
			});
		},

		handleRedeemAuth : function() {
			$('#ecash-div .eCash-loader').show();
			console.log('here')
			if (redeemAuthJSON.status == true) {
				var redeemedAmount = YatraEWalletPaymentUI.getAmountInRupee(redeemAuthJSON.amountInPaisa);
				var isGSTEnabled = YatraPaymentUI.isGSTEnabled(detailsJson.isGSTEnabled);
				YatraEWalletPaymentUI.getRedeemedECashBreakUp(redeemAuthJSON.walletId);
				if(breakUpJson.status == true) {
					this.expEcash = breakUpJson.ExpEcash;
					this.nexpEcash = breakUpJson.NexpEcash;
				}

				if(isGSTEnabled) {
					currentGSTAmount = ShoppingCart.json[ShoppingCart.labelIndex["GST"]]["GST"];
					prevGSTAmount = currentGSTAmount;
					totalPayableAmount = ShoppingCart.totalPayableAmount;
				}
				processWalletPayment("REDEEM", redeemedAmount);

				if(isGSTEnabled){
					var promoAmount = 0;
					if(typeof(detailsJson.promo) != "undefined" && detailsJson.promo != undefined)
						promoAmount = ShoppingCart.json[ShoppingCart.labelIndex[detailsJson.promo.cartLabel]][detailsJson.promo.cartLabel];
					var discountAmount = 0;
					if(typeof(detailsJson.discount) != "undefined" && detailsJson.discount != undefined)
						discountAmount = ShoppingCart.json[ShoppingCart.labelIndex[detailsJson.discount.cartLabel]][detailsJson.discount.cartLabel];

					if(detailsJson.product != "HOLIDAY_IN" && detailsJson.product != "HOLIDAYS_OB"){
						YatraPaymentUI.updateGSTCartUI(Number(this.expEcash) + Number(Math.abs(promoAmount)) + Number(Math.abs(discountAmount)));
					}

					if(this.nexpEcash > 0) {
						var currentTotalAmountPayable = Number(totalPayableAmount) - Number(prevGSTAmount) + Number(currentGSTAmount);
						if(redeemedAmount > currentTotalAmountPayable) {
						    this.isReverseAuthWhenFullEcashAndGST = true;
							//reverse auth as well as set ewallet ui after reverse auth
							YatraEWalletPaymentUI.reverseAuth();

							YatraPaymentUI.recordEventInSentry("Ecash Reverse Auth due to updated GST", "info", "Ecash Redemption GST");
							$("#redeem-ecash-button").trigger('click', 'auto');
						}
						else {
							YatraEWalletPaymentUI.setRedeemUI();
						}
					}
					else {
						YatraEWalletPaymentUI.setRedeemUI();
					}
				}
				else
					YatraEWalletPaymentUI.setRedeemUI();
			} else {
				YatraEWalletPaymentUI.setErrorMessage("Error occurred while redemption of eCash.");
				YatraEWalletPaymentUI.enableRedeemButton();
			}
		},

		setRedeemUI : function() {
		    ShoppingCart.update();
			var paymentJSON = getPaymentJSON();
			var redeemedAmount = YatraEWalletPaymentUI.getAmountInRupee(redeemAuthJSON.amountInPaisa);
			var cartAmount = parseInt(paymentJSON.cartAmount);
			if(YatraEWalletPaymentUI.isBookingForEcashGV()) {
			    YatraEWalletPaymentUI.getRedeemedECashBreakUp(redeemAuthJSON.walletId);
                if(breakUpJson.status == true) {
                	this.expEcash = breakUpJson.ExpEcash;
                	this.nexpEcash = breakUpJson.NexpEcash;
                }
			    $(".total-redeemed-ecash").html(YatraPayment.formatAmount(YatraEWalletPaymentUI.getUserMoney()));
			} else {
			    $(".total-redeemed-ecash").html(YatraPayment.formatAmount(redeemedAmount));
			}
			$(".total-amount-due").html(YatraPayment.formatAmount(paymentJSON.amount));
			$("#eCashRedeemed").val(redeemedAmount);
			$("#available-bal").html(YatraPayment.formatAmount(parseInt(paymentJSON.amount) - parseInt(redeemedAmount)));
			var isGSTEnabled = YatraPaymentUI.isGSTEnabled(detailsJson.isGSTEnabled);
			var isTcsEnabled = YatraPaymentUI.isTcsEnabled(detailsJson.isTcsEnabled);
			
			setTimeout(function(){
				$('#ecash-div .eCash-loader').hide();
			},1000)
			if(isGSTEnabled)
				$("#gst_text_id").show();
			else $("#gst_text_id").hide();

 			if(isTcsEnabled){
				YatraPaymentUI.callForRecalculatingTcs(this.nexpEcash);
				paymentJSON = getPaymentJSON();
				cartAmount = parseInt(paymentJSON.cartAmount);
			 }

			redeemWrapper = $('#redeem-block-id');
			redeemApplied = $('#redeem-applied-id');
			$(redeemWrapper).hide();
			$(redeemApplied).show();
			YatraEWalletPaymentUI.enableRedeemButton();

			// Notifying LOB for eCash redemption
			YatraPaymentUI.generateEmiTable();
			if(cartAmount == 0) {
				// if GV is applied, go to GV menu and dont hide anything
				if(GV && GV.gv && GV.gv.length !=0){
					$("#gv").click()
				}else{
					// if gv is not applied go with normal flow
				YatraEWalletPaymentUI.hidePaymentDiv();

				// Set previousPayop
				$("#previousPayOp").val(YatraPayment.getPayOp());

				// Hide selected Tab contents.
				$(".cpmt_Paytabbox > .content").hide().removeClass('show');
				redeemNow = false;
				// remove preset payment Details
				YatraPaymentUI.enableFirstPaymentOption();
				}
			} else {
				YatraEWalletPaymentUI.showPaymentDiv();

				// Reset previousPayop
				$("#previousPayOp").val("");
			}

			// GA Event to track redemption
			YatraPaymentUI.pushGAInfoEWallet("Redeem Now");

			// GA Event to track partial Redemption
			var totalRedeemableEcashFloat = parseFloat($("#totalRedeemableECash").val());
			var redeemedEcashFloat = parseFloat(redeemedAmount);
			if(redeemedEcashFloat < totalRedeemableEcashFloat){
				YatraPaymentUI.pushGAInfoEWallet("Redeem Partial - "+redeemedEcashFloat+" - "+totalRedeemableEcashFloat);
			}
			

		},

		reverseAuth : function(isAsync) {
			if(isAsync == undefined) {
				isAsync = true;
			}
			var paymentJSON = getPaymentJSON();
			YatraEWalletPaymentUI.disableCancelRedemption();
			var d = new Date();
			var dt = d.getTime();
			var isGSTEnabled = YatraPaymentUI.isGSTEnabled(detailsJson.isGSTEnabled);
			if(isGSTEnabled)
				isAsync = false;
			$.ajax({
				url : this.reverseAuthURL + "?date="+dt,
				type : 'POST',
				async: isAsync,
				data : {
					'cust_email' : paymentJSON.userEmail, 'ttid' : paymentJSON.superPNR, 'amount' : YatraEWalletPaymentUI.getAmountInRupee(redeemAuthJSON.amountInPaisa), 
					'wallet_id' : redeemAuthJSON.walletId, 'merchant_code' : paymentJSON.merchant, 'product_code' : paymentJSON.product
				}, cache : false,

				success : function(response) {
					reverseAuthJSON = $.parseJSON(response);
					YatraEWalletPaymentUI.setReverseAuthUI();
					YatraEWalletPaymentUI.expEcash = 0;
					$('#ecash-div .eCash-loader').hide();
				}, error : function(request, status, error) {
					reverseAuthJSON = YatraEWalletPaymentUI.getErrorJSON("Error occurred while cancelling eCash redeemed.");
					YatraEWalletPaymentUI.enableCancelRedemption();
					$('#ecash-div .eCash-loader').hide();
				}
			});
		},

		setReverseAuthUI : function() {
			if (reverseAuthJSON.status == true) {
				$(".total-redeemed-ecash").html("");
				$("#eCashRedeemed").val("");
				$("#available-bal").html("");
				$("#gst_text_id").hide();

				// Notifying LOB for eCash redemption
				processWalletPayment("CANCEL-REDEEM", YatraEWalletPaymentUI.getAmountInRupee(reverseAuthJSON.amountInPaisa));
				YatraPaymentUI.generateEmiTable();

				var isGSTEnabled = YatraPaymentUI.isGSTEnabled(detailsJson.isGSTEnabled);
				if(isGSTEnabled && !this.isReverseAuthWhenFullEcashAndGST){
					var promoAmount = 0;
					var discountAmount = 0;
					if(typeof(detailsJson.promo) != "undefined" && detailsJson.promo != undefined)
						promoAmount = ShoppingCart.json[ShoppingCart.labelIndex[detailsJson.promo.cartLabel]][detailsJson.promo.cartLabel];
					if(typeof(detailsJson.discount) != "undefined" && detailsJson.discount != undefined)
						discountAmount = ShoppingCart.json[ShoppingCart.labelIndex[detailsJson.discount.cartLabel]][detailsJson.discount.cartLabel];
						if(detailsJson.product != "HOLIDAY_IN" && detailsJson.product != "HOLIDAYS_OB"){
							YatraPaymentUI.updateGSTCartUI(Number(Math.abs(promoAmount)) + Number(Math.abs(discountAmount)));
						}
				}

				var isTcsEnabled = YatraPaymentUI.isTcsEnabled(detailsJson.isTcsEnabled);
				if(isTcsEnabled){
					YatraPaymentUI.callForRecalculatingTcs();
				 }
	
                this.isReverseAuthWhenFullEcashAndGST = false;

				YatraEWalletPaymentUI.resetPreviousEcashRedeemed();
				YatraEWalletPaymentUI.setEWalletUI();
				YatraEWalletPaymentUI.resetSlider();
				YatraEWalletPaymentUI.showPaymentDiv();

				// $('#tab_' + YatraPaymentUI.getPaymentOption() + ' input[name="payop"]').val(YatraPaymentUI.getPaymentOption());
				YatraPaymentUI.notifyPayopToLOB(); // In case user cancels redemption after complete payment through wallet, we need to notify payOp to LOB.
				redeemWrapper = $('#redeem-block-id');
				redeemApplied = $('#redeem-applied-id');
				$(redeemWrapper).show();
				$(redeemApplied).hide();

				YatraPaymentUI.pushGAInfoEWallet("Cancel Redemption");
				YatraEWalletPaymentUI.enableCancelRedemption();
				redeemAuthJSON = null;
			} else {
				YatraEWalletPaymentUI.setErrorMessage("Error occurred while cancelling eCash redeemed.");
				YatraEWalletPaymentUI.enableCancelRedemption();
			}
		},

		refreshEWalletUI : function() {
			YatraEWalletPaymentUI.updateEWalletHTML();
		},

		resetSlider : function() {
			$('.redeem-slider').slider({
				orientation: 'horizontal',
				range: 'min',
				value : sliderMaxLimit,
				min: 0,
				max: sliderMaxLimit,
				step : 1,
				slide: function(event, ui){
					// YatraPaymentUI.pushGAInfoEWallet("Adjust Redemption Amount");
					$("#total-redeemable-ecash").html(ui.value);
					$("#eCashToBeRedeemed").val(ui.value);
					if(YatraEWalletPaymentUI.isBookingForEcashGV()) {
					    $("#nexpEcashForGVToRedeem").val(ui.value);
					}
				}
			});
		},

		resetPreviousEcashRedeemed : function() {
			previousEcashRedeemed = 0;
		},

		hidePaymentDiv : function() {
			$(".cpmt_Paymentdiv").addClass('fadeOut-payment');
		},

		showPaymentDiv : function() {
			$(".cpmt_Paymentdiv").removeClass('fadeOut-payment');
		},

		getAmountToBeRedeemed : function() {
			var val = $("#eCashToBeRedeemed").val();
			if(isNaN(val)){
				return 0;
			}else {
			if(roundOffCart){
	        	 return  parseInt(val);
	        	 }else{
	        	return val;
	        }
			}
		},

        getEcashForGVAmountToBeRedeemed : function() {
			var val = parseInt($("#expEcashForGVToRedeem").val()) + parseInt($("#nexpEcashForGVToRedeem").val());
			if(isNaN(val)){
				return 0;
			}else {
				return parseInt(val);
			}
		},

		getErrorJSON : function(responseMsg) {
			return {"status" : "false", "responseMsg" : responseMsg};
		},

		getYLP : function() {
			return this.expEcash;
		},

		getUserMoney : function() {
			return this.nexpEcash;
		},

		getErrorJSON : function(responseMsg) {
			return {"status" : "false", "responseMsg" : responseMsg};
		},

		setErrorMessage : function(errorMessage) {
			$("#ecash-error-message").html(errorMessage);
			$("#ecash-error-message").show();
			setTimeout(function() {
				$("#ecash-error-message").hide('blind', {}, 500);
			}, 5000);
		},

		getAmountInPaise : function (amountInRupee) {
			return parseInt(amountInRupee)*100;
		},

		getAmountInRupee : function (amountInPaise) {
			return parseInt(amountInPaise)/100;
		},

		getCashAmount : function() {
			var eCashRedeemed = $("#eCashRedeemed").val();
			if(eCashRedeemed != undefined && eCashRedeemed != null && eCashRedeemed != "") {
				var redeemedAmount = parseInt(eCashRedeemed);
				var paymentJSON = getPaymentJSON();
				var amount = parseInt(paymentJSON.amount);
				return parseInt(amount - redeemedAmount);
			}
		},

		disableRedeemButton : function() {
			$("#redeem-ecash-button").addClass('cpmt_disable_button');
			$("#redeem-ecash-button").prop("disabled", true);
		},

		enableRedeemButton : function() {
			$("#redeem-ecash-button").removeClass('cpmt_disable_button');
			$("#redeem-ecash-button").addClass('ytBtn ytBtnOrange');
			$("#redeem-ecash-button").prop("disabled", false);
		},

		disableCancelRedemption : function() {
			$(".cancel-redemption-active").hide();
			$(".cancel-redemption-inactive").show();
		},

		enableCancelRedemption : function() {
			$(".cancel-redemption-active").show();
			$(".cancel-redemption-inactive").hide();
		},

		isCompleteEwalletPayment : function() {
			var paymentJSON = getPaymentJSON();
			if(paymentJSON.cartAmount != undefined) {
				var cartAmount = parseInt(paymentJSON.cartAmount);
				return (cartAmount == 0);
			}
			return false;
		}
};var YatraFBPaymentUI = {
		
		initialiseFB : false,
		
		postLogin : function(authResponse,fbResponse){
			
			var authURL = YatraFBPaymentUI.getQBAuthURLForFBUser();
			var paymentJSON = getPaymentJSON();
			var merchant = paymentJSON.merchant;
			var product = paymentJSON.product;
			var token = null
			
			if (fbResponse != null || fbResponse != undefined) {
		    	token = fbResponse.authResponse.accessToken;		
			}
			
			$.ajax({
				url: authURL,
				timeout: (5 * 1000),
				type: 'POST',
				data: { 
				        'merchant': merchant, 
				        'product': product,
				        'accessToken' : token,
				        'sourcepage' : window.location.href
				},
				cache: false,
				
				success : function(response){
					YatraQBPaymentUI.generateQBHtmlFromJson(response);
				}, 
				
				error: function (request, status, error) {
					alert("Error occurred while authenticating via facebook.. Please try again or use some other payment option..")
				}
			});	
		},

		getQBAuthURLForFBUser : function(){
			return "/PaySwift/authenticate-fb-user.htm";
		},
		
		executeFn : function(f, c, p) {
		    return function () {
		        f.apply(c, p);
		    };
		},
		
		initFB : function() {
			YatraFBPaymentUI.initialiseFB = true;
		    FB.init({
		        appId: '326464424135609',
		        cookie: true,
		        status: true,
		        xfbml: true,
		        oauth: true
		    });
		},
		
		getLoggedInFb : function(fn, arr, src) {
		    var resp = null;
		    if(!YatraFBPaymentUI.initialiseFB){
		    	YatraFBPaymentUI.initFB();
		    }
		    
		    FB.login(
				function (response) {
					if (response != undefined && response.status == "connected" && response.authResponse != null) {
						resp = response;
					}
					arr[arr.length] = resp;
					YatraFBPaymentUI.executeFn(fn, this, arr)();
				}, 
				{
					scope: 'email'
				}
			);
		},
		
		fbLogin : function(r, params,src) {
			if (r == null || r == "undefined") {
				YatraFBPaymentUI.getLoggedInFb(YatraFBPaymentUI.postLogin, params, src);
		    } 
		},
		
		loginByFacebook : function() {
			authResponse={status:"noResponse"};
			YatraFBPaymentUI.fbLogin(null,[authResponse],'signin');
		}
};var enableQuickBook = false;
var showQB = false;
var showQBCards = false;
var showSaveOption = false;
var showAuthDiv = false;
var qbAuthFailed = false;

var YatraQBPaymentUI = {
		
		updateQBDiv : function() {
		
			// Get ssoToken from cookies.
			var ssoToken = YatraQBPaymentUI.getSSOToken();

			// Get Quick Book Cards if quick book is enabled for merchant - product and ssoToken is present.
			if(enableQuickBook && ssoToken != undefined && ssoToken != null) {
				YatraQBPaymentUI.getQuickBookCards();
			} else {
				// If user is not logged in or he logs out, remove QB Cards.
				YatraQBPaymentUI.removeQuickBookCards();
			}
		},
		
		getQuickBookCards : function() {
			try{
			// If QB JSON available, generate QB HTML
			if(qbJson != undefined && qbJson != null && qbJson != '' && qbJson != "null") {
				YatraQBPaymentUI.generateQBHtmlFromJson(qbJson);
			} else {
				// Make an ajax call to get QB JSON and generate QB HTML from JSON.
				YatraQBPaymentUI.getQBJsonAndGenerateHtml();
			}
			}catch(e){
				log_error(e);
			}
		},
		
		getQBJsonAndGenerateHtml : function() {
			var paymentJSON = getPaymentJSON();
			if(paymentJSON != undefined && paymentJSON != null) {
				var merchant = paymentJSON.merchant;
				var product = paymentJSON.product;
				var qbJsonURL = YatraQBPaymentUI.getQBJsonURL();
				
				$.ajax({
					
					url: qbJsonURL,
					timeout: (5 * 1000),
					type: 'POST',
					async: false, 
					data: { 
					        'merchant': merchant, 
					        'product': product
					},
					cache: false,

					success : function(response) {
						qbJson = response;
						YatraQBPaymentUI.generateQBHtmlFromJson(response);
					}, 
					
					error: function (request, status, error) {
						alert("Error while getting quickbook cards.");
					}
				});
			}
		},
		
		getQBJsonURL : function(){
			return "/PaySwift/qb.htm";
		},
		
		generateQBHtmlFromJson : function(qbJson) {

			YatraQBPaymentUI.processQuickBookResponse(qbJson);

			//Disable Custom RadioBox and CheckBox on IE version lesser than 9
			try{
				if ($.browser.msie && parseInt($.browser.version) < 9) {
					$(".cpmt_quickBook").find("span.custom-Radiobox").removeClass("custom-Radiobox");
				}
			}
			catch(e) {}
		},
		
		removeQuickBookCards : function() {
			
			// Set all QB specific variables to false.
			showQB = false;
			showQBCards = false;
			showSaveOption = false;
			showAuthDiv = false;
			qbAuthFailed = false;
			
			// Reset QB JSON variable.
			qbJson = "";
			
			// Reset QB HTML.
			$('#tab_ccQB').html("");
			$('#tab_dcQB').html("");
			
			// Hide save CC check box.
			$('#cc_SaveOptionDiv').hide();
			$("#cc_saveQuickBookCard_id").val("false");
							
			// Hide save DC check box.
			$('#dc_SaveOptionDiv').hide();
			$("#dc_saveQuickBookCard_id").val("false");
			
			// Hide Use new CC radio button and show CC div.
			$("#qb_newCreditCard").hide();
			$("#cc_newCard").show();
			$('#tab_cc_qb > input[name="payop"]').val('cc'); 
			
			// Hide Use new DC radio button and show DC div.
			$("#qb_newDebitCard").hide();
			$("#dc_newCard").show();	
			$('#tab_dc_qb > input[name="payop"]').val('dc');
			
			// Set QB Enabled to false.
			$('#cc_qbEnabled').val('false');
			$('#dc_qbEnabled').val('false');
			
			YatraPaymentUI.notifyPayopToLOB();				
		},
		
		getSSOToken : function(){
			var i,x,y;
			var ARRcookies=document.cookie.split(";");
			for (i=0;i<ARRcookies.length;i++) {
			  x=ARRcookies[i].substr(0,ARRcookies[i].indexOf("="));
			  y=ARRcookies[i].substr(ARRcookies[i].indexOf("=")+1);
			  x=x.replace(/^\s+|\s+$/g,"");
			  if (x=="userName") {
				return unescape(y);
			  }
			}
		},
		
		processQuickBookResponse : function(response){
			
			// Create HTML from JSON received in response.
			var qbDivObj = YatraQBPaymentUI.getQBDiv(response);
			
			if(typeof(qbDivObj) != "undefined" && qbDivObj != null) {
				
				if(showQBCards) {
					
					if(typeof(qbDivObj.qbCcCards) != "undefined" && qbDivObj.qbCcCards != null) {
						
						$('#tab_ccQB').html(qbDivObj.qbCcCards);
						$('#cc_newCard .save_card').show();
						$("#qb_newCreditCard").show();
						$('#cc_newCard_id').prop('checked', false);
						//$("#cc_newCard").hide();
						$("#cc_newCard .heading-p").hide();
						$('#cc_qbEnabled').val('true');
						$('#tab_cc_qb > input[name="payop"]').val('qb');
						$(".qb_ccCVV").focus(function(){
							$(".card-logos").addClass('dummy');
							$(".card-logos").removeClass('VISA');$(".card-logos").removeClass('MASTER');$(".card-logos").removeClass('DINERS');
							$(".card-logos").removeClass('AMEX');$(".card-logos").removeClass('RUPAY');  $(".card-logos").removeClass("MAEST")
							binNumber="";
							$('#cc_cno_id').val("");
							$('#cc_cardholder_name_id').val("");
							$('#cc_expm_id').val("");
							$('#cc_expy_id').val("");
							$('#cc_cvv_id').val("");
							$("#cc_process-pay-type").hide();
							$(".qb_ccCVV").removeClass("nonValidate");
							$('#cc_cno_id').addClass("nonValidate");
							$('#cc_cardholder_name_id').addClass("nonValidate");
							$('#cc_expm_id').addClass("nonValidate");
							$('#cc_expy_id').addClass("nonValidate");
							$('#cc_cvv_id').addClass("nonValidate");
						});
						$(".qb_ccCVV").click(function(){
							$(".qb_ccCVV").removeClass("nonValidate");
							$('#cc_cno_id').addClass("nonValidate");
							$('#cc_cardholder_name_id').addClass("nonValidate");
							$('#cc_expm_id').addClass("nonValidate");
							$('#cc_expy_id').addClass("nonValidate");
							$('#cc_cvv_id').addClass("nonValidate");
							
						});
						$("#cc_cno_id,#cc_cardholder_name_id,#cc_expm_id,#cc_expy_id,#cc_cvv_id").focus(function(){
							$(".qb_ccCVV").val("");
							$('#tab_cc_qb > input[name="payop"]').val('cc');
							$('#cc_cno_id').removeClass("nonValidate");
							$('#cc_cardholder_name_id').removeClass("nonValidate");
							$('#cc_expm_id').removeClass("nonValidate");
							$('#cc_expy_id').removeClass("nonValidate");
							$('#cc_cvv_id').removeClass("nonValidate");
							$(".qb_ccCVV").addClass("nonValidate");
							$(".qb_multiFlowDiv").hide();
							$("#tab_cc_qb ul").find("li.active input[name=qb_cvv]").css({"background":"#dedede"});
							$(".cpmt_quickBook ul li.active").removeClass("active").addClass("deactive");
						});
						
					} else {
						$('#tab_ccQB').html("");
						$("#cc_newCard .save_card").hide();
						// Hide Use new CC radio button and show CC div.
						$("#qb_newCreditCard").hide();
						$("#cc_newCard").show();
						$('#tab_cc_qb > input[name="payop"]').val('cc'); 
						
						// Set QB Enabled to false.
						$('#cc_qbEnabled').val('false');
					}
					
					if(typeof(qbDivObj.qbDcCards) != "undefined" && qbDivObj.qbDcCards != null) {
						
						$('#tab_dcQB').html(qbDivObj.qbDcCards);
						$('#dc_newCard .save_card').show();
						$("#qb_newDebitCard").show();
						$('#dc_newCard_id').prop('checked', false);
						// $("#dc_newCard").hide();
						$("#dc_newCard .heading-p").hide();
						$('#dc_qbEnabled').val('true');
						$('#tab_dc_qb > input[name="payop"]').val('qb'); 
						$(".qb_dcCVV").focus(function(){
							$(".card-logos").addClass('dummy');
							$(".card-logos").removeClass('VISA');$(".card-logos").removeClass('MASTER');$(".card-logos").removeClass('DINERS');
							$(".card-logos").removeClass('AMEX');$(".card-logos").removeClass('RUPAY');  $(".card-logos").removeClass("MAEST")
							binNumber="";
							$('#dc_cno_id').val("");
							$('#dc_cardholder_name_id').val("");
							$('#dc_expm_id').val("");
							$('#dc_expy_id').val("");
							$('#dc_cvv_id').val("");
							$("#dc_process-pay-type").hide();
							$(".qb_dcCVV").removeClass("nonValidate");
							$('#dc_cno_id').addClass("nonValidate");
							$('#dc_cardholder_name_id').addClass("nonValidate");
							$('#dc_expm_id').addClass("nonValidate");
							$('#dc_expy_id').addClass("nonValidate");
							$('#dc_cvv_id').addClass("nonValidate");
							
						});
						$(".qb_dcCVV").click(function(){
							$(".qb_dcCVV").removeClass("nonValidate");
							$('#dc_cno_id').addClass("nonValidate");
							$('#dc_cardholder_name_id').addClass("nonValidate");
							$('#dc_expm_id').addClass("nonValidate");
							$('#dc_expy_id').addClass("nonValidate");
							$('#dc_cvv_id').addClass("nonValidate");
							
						});
						$("#dc_cno_id,#dc_cardholder_name_id,#dc_expm_id,#dc_expy_id,#dc_cvv_id").focus(function(){
							$(".qb_dcCVV").val("");
							$('#tab_dc_qb > input[name="payop"]').val('dc');
							$('#dc_cno_id').removeClass("nonValidate");
							$('#dc_cardholder_name_id').removeClass("nonValidate");
							$('#dc_expm_id').removeClass("nonValidate");
							$('#dc_expy_id').removeClass("nonValidate");
							$('#dc_cvv_id').removeClass("nonValidate");
							$(".qb_dcCVV").addClass("nonValidate");
							$(".qb_multiFlowDiv").hide();
							$("#tab_dc_qb ul").find("li.active input[name=qb_cvv]").css({"background":"#dedede"});
							$(".cpmt_quickBook ul li.active").removeClass("active").addClass("deactive");
						});
						
					} else {
						$('#tab_dcQB').html("");
						$('#dc_newCard .save_card').hide();
						// Hide Use new DC radio button and show DC div.
						$("#qb_newDebitCard").hide();
						$("#dc_newCard").show();	
						$('#tab_dc_qb > input[name="payop"]').val('dc');
						
						// Set QB Enabled to false.
						$('#dc_qbEnabled').val('false');
					}
					
					showAuthDiv = false;
					
					// register events
					YatraQBPaymentUI.registerDeleteCardEvent();
					
					var selectedTab = YatraPaymentUI.getPaymentOption();
					if(selectedTab == "cc"){

						// Enable radio button for first CC.
						var firstCreditCard = $('#tab_ccQB input:radio[name="cardid"]:nth(0)');
						$(firstCreditCard).attr("checked","checked");
						YatraPaymentUI.enableQBCVVForCheckedCreditCard(firstCreditCard);
						if($(firstCreditCard).attr("cardNumberOriginal") != null && $(firstCreditCard).attr("cardNumberOriginal") != undefined) {
						    var cardNumber = $(firstCreditCard).attr("cardNumberOriginal")
                            var cardNo = cardNumber.replace(/-/g,'');
                            var binDetails = YatraPaymentUI.getBinDetails(cardNo);
                            YatraPaymentUI.setSkipOtpUiForQBIfRequired(firstCreditCard, "cc",binDetails);
                            YatraPaymentUI.setMultiPayUiForQBIfRequired(firstCreditCard, "cc",binDetails);
						}
						YatraPaymentUI.handleInternationalQBCard(firstCreditCard);
					}
					else if(selectedTab == "dc"){
						
						// Enable radio button for first DC.
						var firstDebitCard = $('#tab_dcQB input:radio[name="cardid"]:nth(0)');
						$(firstDebitCard).attr("checked","checked");
						YatraPaymentUI.enableQBCVVForCheckedDebitCard(firstDebitCard);
						if($(firstDebitCard).attr("cardNumberOriginal") != null && $(firstDebitCard).attr("cardNumberOriginal") != undefined) {
						    var cardNumber = $(firstDebitCard).attr("cardNumberOriginal")
                            var cardNo = cardNumber.replace(/-/g,'');
                            var binDetails = YatraPaymentUI.getBinDetails(cardNo);
                            YatraPaymentUI.setSkipOtpUiForQBIfRequired(firstDebitCard, "dc",binDetails);
                            YatraPaymentUI.setMultiPayUiForQBIfRequired(firstDebitCard, "dc",binDetails);
						}
						YatraPaymentUI.handleInternationalQBCard(firstDebitCard);
					}
					
					// Hide QB auth fail message, if cards are returned.
					YatraQBPaymentUI.hideQBAuthFailMessage();
					
					// Hide "Enter password" message in case of IE < 10, if cards are returned. 
					YatraQBPaymentUI.IE_hideQBPassMessage();
				}
				
				if(typeof(qbDivObj.authDivCC) != "undefined" && typeof(qbDivObj.authDivDC) != "undefined"){
					
					showAuthDiv = true;
					
					$('#tab_ccQB').html(qbDivObj.authDivCC);
					$('#tab_dcQB').html(qbDivObj.authDivDC);
					
					//Logic for Credit Cards
					$("#qb_newCreditCard").show();
					$('#cc_newCard_id').prop('checked', false);
					//$("#cc_newCard").hide();
					$('#cc_qbEnabled').val('true');
					$('#tab_cc_qb > input[name="payop"]').val('cc');
					
					//Logic for Debit Cards
					$("#qb_newDebitCard").show();
					$('#dc_newCard_id').prop('checked', false);
					// $("#dc_newCard").hide();
					$('#dc_qbEnabled').val('true');
					$('#tab_dc_qb > input[name="payop"]').val('dc');
					
					// If authentication has failed, show auth fail message.
					if(qbAuthFailed) {
						YatraQBPaymentUI.showQBAuthFailMessage();
					} else {
						
						// Show "Enter password" message for IE < 10.
						YatraQBPaymentUI.IE_showQBPassMessage();
					}
				}
				
				YatraQBPaymentUI.innerSwitch('#tab_ccQB');
				YatraQBPaymentUI.innerSwitch('#tab_dcQB');
				// unbinding on both the elements below as these might be called two times 
				// we are adding it to fix the bug after login in QB, its failing as events are not attached to below element as they are hidden
				YatraPaymentUI.addEventsForQB();


				
			}
			
			if(showSaveOption && "true" == showQBCheckBox) {
				//Show Save Card option at Credit Card Tab
				$('#cc_SaveOptionDiv').show();
				$("#cc_saveQuickBookCard_id").val("true");
				
				//Show Save Card option at Debit Card Tab
				$('#dc_SaveOptionDiv').show();
				$("#dc_saveQuickBookCard_id").val("true");
			}
			YatraPaymentUI.notifyPayopToLOB();
		},
		
		getQBDiv : function(qbJson) {
			if(qbJson != null && typeof(qbJson) != "undefined" && qbJson != "" && qbJson != "null") {
				
				var responseObj = new Object();
				var qbJsonObject = $.parseJSON(qbJson);
				if(qbJsonObject != null && typeof(qbJsonObject.status) != "undefined") {

					// If saveCard = 'ENABLED', show save card check box.
					if(qbJsonObject.saveCard == 'ENABLED') {
						showSaveOption = true;
					}
					if(qbJsonObject.status == 'ENABLED') {
						
						// Set showQB to true if QB is enabled.
						showQB = true;
						
						// Check if authentication required / failed. If yes, show authentication div.
						if(qbJsonObject.authenticationFailed || qbJsonObject.authenticationRequired) {
							responseObj.authDivCC = YatraQBPaymentUI.getQBAuthenticationDiv(qbJsonObject, 'cc');
							responseObj.authDivDC = YatraQBPaymentUI.getQBAuthenticationDiv(qbJsonObject, 'dc');
							 var emailId = "";
					            if(detailsJson != undefined && detailsJson != null) {
					                var userDetails = detailsJson.userDetails;
					                if(userDetails != undefined && userDetails != null) {
					                    emailId = userDetails.email;
					                }
					            }
							if(qbJsonObject.authenticationFailed) {
								qbAuthFailed = true;
								YatraPaymentUI.recordEventInSentry("QuickBook: Failed to authenticate", "error",
								"qb-access-auth-fail", "emailId", emailId);
							} else {
							    YatraPaymentUI.recordEventInSentry("QuickBook: ssoToken stale, Authentication Required", "info",
							    "qb-access-auth-require", "emailId", emailId);
							}
						}
						
						// Get Quick Book Cards div.
						else {
							showQBCards = true;
							responseObj.qbCcCards = YatraQBPaymentUI.getQBCardsDiv(qbJsonObject, 'Credit Card');
							responseObj.qbDcCards = YatraQBPaymentUI.getQBCardsDiv(qbJsonObject, 'Debit Card');
						}
						return responseObj;
					}
				}
			}
		},
		
		getQBAuthenticationDiv : function(qbJsonObject, payOp) {

		    var emailId = "";
            if(detailsJson != undefined && detailsJson != null) {
                var userDetails = detailsJson.userDetails;
                if(userDetails != undefined && userDetails != null) {
                    emailId = userDetails.email;
                }
            }

			// Set message.
            var message = YatraQBPaymentUI.getQBAuthRequiredMessage();
			var div = '<p class="qblogin_heading"><span>Pay Using Saved Cards</span></p><p style="margin-top:20px"><span>' + message + '</span></p>';
			var authFailMsgDiv = '<div id="qb_auth_fail_msg' + '_' +  payOp + '" style="font-size: 13px; color : red; display:none;float:left">';
			authFailMsgDiv += YatraQBPaymentUI.getQBAuthFailedMessage() + '</div>';
			
			var IE_qbPassMsgDiv = '<div id="qb_pass_msg' + '_' +  payOp + '" style="font-size: 13px; color : black; display:none;clear:both">';
			IE_qbPassMsgDiv += YatraQBPaymentUI.IE_getQBPassMessage() + '</div>';
			

			// Create div when user needs to authenticate using FB login
			if(qbJsonObject.authMode == 'FACEBOOK') {
				div += '<input type="button" class="PaymentSprite FbLogin" onclick="YatraQBPaymentUI.authenticateFBUserForQB()"><div class="clear">&nbsp;</div>';
			} else {
				div += '<div class="new_cc_ui"><div class="qbLogin loginContainer"><div class="mt5 qb_mail">';
            	div += '<input type="text" id="qb_user_email' + '_' +  payOp + '" class="ie6Input qb_mail_input" disabled="disabled" value="' + emailId + '" /></div><div class="mt5 qb_pwd">';
            	div += '<input type="password" placeholder="Enter your password" class="ie6Input qb_pwd_input" id="qb_password' + '_' +  payOp + '" processQBPassword="true"/></div>';
            	div += '<div class="qb_loginbtn"><input type="button" class="cpmt_Btn cpmt_BtnOrange latoBold qb_loginbtn_input" value="Login" onclick="YatraQBPaymentUI.authenticateYatraUserForQB()">';
            	div += '</div></div><div class="clear">&nbsp;</div>';
			}
			div += authFailMsgDiv + IE_qbPassMsgDiv;
			div += '</div>';
			return div;	
		},
		
		getAMEXAddressDiv : function(qbCards){

			qbCards += '<div id="qb_AddressShowHide" class="flL" style="display:none;">';
			qbCards += '<article class="cpmt_amexCreditC" >';
			qbCards += '<p class="block">Where was your Amex card issued?</p>';
			qbCards += '<div class="mt10"><div class="cpmt_countryBox"><label for="qb_domestic_address_rdo">';
			qbCards += '<span class="custom-Radiobox">';
			qbCards += '<input type="radio" checked="checked" class="cpmt_alignMid radioMid" value="Domestic" id="qb_domestic_address_rdo" name="qb_address_check_rdo">';
			qbCards += '<span class="box"><b class="tick"></b></span></span>India</label></div>';
			qbCards += '<div class="cpmt_countryBox cpmt_international"><label for="qb_international_address_rdo">';
			qbCards += '<span class="custom-Radiobox">';
			qbCards += '<input type="radio" class="cpmt_alignMid radioMid" value="International" id="qb_international_address_rdo" name="qb_address_check_rdo">';
			qbCards += '<span class="box"><b class="tick"></b></span></span>Outside India</label></div></div></article></div>';

			return qbCards;
	    },

	     getBillingAdddressDiv : function(qbCards, index){

			qbCards += '<div class="cpmt_IntCardDetails" id="qb_pmt_addressId" style="display:none;">';
			var billingAddressDiv = $('#cc_pmt_addressId_template').html();
			billingAddressDiv = billingAddressDiv.replace(/temp/g,'qb_' + index);
	    	qbCards += billingAddressDiv;
	    	qbCards += '</div>';
	    	return qbCards;
	    },
		
		getQBCardsDiv : function(qbJsonObject, cardType) {

			var cvvId;
			var processCardId;
			var processCVV;
			var multiFlowId;
			
			if(cardType == 'Credit Card') {
				cvvId = 'qb_ccCVV';
				processCardId = 'cc';
				processCVV = 'ccCVV';
				multiFlowId = 'ccMultiFlowId';
			} 
			
			if(cardType == 'Debit Card') {
				cvvId = 'qb_dcCVV';
				processCardId = 'dc';
				processCVV = 'dcCVV';
				multiFlowId = 'dcMultiFlowId';
			}
			
			var qbCards = '<div id="' + processCardId + '_qb_divErrMsgBlock" class="pmt_errMsgBlock qb_msg" style="display:none;"></div>';
			qbCards += '<div id="' + processCardId + '_qb_divWarningMsgBlock" class="pmt_warningMsgBlock qb_msg" style="display:none;"></div>';
			qbCards += '<p class="div_quickBook"><span class="mb-35" style="display:block;width:100%;float:left;font-size:18px;color:#666;" id="payWithQB">'+
'Your Tokenized Cards</span><label style="display:none;float:left;margin-bottom:10px;font-size:16px;cursor:pointer;" for="' + processCardId +'_quickOrCard"><span class="custom-Radiobox" style="left:-1px">'+
			'<input type="radio" class="cpmt_alignMid" id="' + processCardId +'_quickOrCard" name="' + processCardId +'_quickOrCard" checked="true">'+
			'<span class="box"><b class="tick"></b></span>'+
		'</span><span style="margin-top:8px;margin-left:5px;float:left;font-size:16px;">Saved Cards</span></label></span></span></p><ul class="js-quick noListStyle div_quickBook pd-btm-0">';
			
			// Get QB Cards array from JSON Object.
			var qbCardsArray = qbJsonObject.quickBookCards;
			var noOfCards = 0;
			var activeClass = ' ';
			var counter = 0;
			for (var i = 0; i < qbCardsArray.length; i++) {
				if(counter ==0){
					activeClass = 'active';
				}
				else{
					activeClass = ' ';
				}
				if(qbCardsArray[i].cardTypeLabel == cardType) {
				// changes for display starting 6 digit of corporate personal cards to xxxxxxx
                   var corpFlag = detailsJson.hasOwnProperty('crpInfo');
                   if(corpFlag) {
                     var cardNumber = qbCardsArray[i].cardNumber;
                     var cardNumberOriginal = qbCardsArray[i].cardNumber;
                     cardNumber = cardNumber.replace(/^.{7}/g, 'xxxx-xx');
                     }else{
                     var cardNumber = qbCardsArray[i].cardNumber;
                     }
					qbCards += '<li li_index="' + noOfCards + '" class="' + processCardId + '_' + noOfCards + ' ' + activeClass +'">';
					qbCards += '<label for=' + qbCardsArray[i].cardId +'><span class="custom-Radiobox" style="visibility:hidden;margin-left:-10px;">';
					qbCards += '<input type="radio" name="cardid" onclick=\'YatraPaymentUI.pushGAInfoForQuickBook("Use Saved Quickbook Card")\' id=' + qbCardsArray[i].cardId + ' value=' + qbCardsArray[i].cardId;
					qbCards += ' cardType=' + qbCardsArray[i].cardBrand + ' processCardId="' + processCardId + '"';
					qbCards += ' cardNumber=' + cardNumber;
					qbCards += ' cardNumberOriginal=' + cardNumberOriginal;
					qbCards += ' isAddressSaved="' + qbCardsArray[i].addressSaved + '" cardAccessType="' + qbCardsArray[i].cardAccessType + '"><span class="box"><b class="tick"></b></span></span>';
					qbCards += '<i class="PaymentSprite ' + qbCardsArray[i].cardLogoURL + '"></i>';
					qbCards += '<div class="block-quickBook"><!--span class="wfull"> ' + qbCardsArray[i].cardBrand + '</span-->';
					qbCards += '<span class="wfull" style="font-size:14px;margin-top:7px;"> ' + cardNumber + '</span></div>';
					qbCards += '<div class="flR" style="margin-right:10px;"><span class="cpmt_CVVno">' + qbCardsArray[i].cvvLabel;
					qbCards += '<small class="cpmt_cvvLength" style="display:none;">' + qbCardsArray[i].cvvLength + '-digit Number</small><i class="ico_info_cvv PaymentSprite" style="float:right;margin-right:5px;margin-top:3px;"></i><div class="info-tooltip"><div class="tooltip-content"><i class="PaymentSprite pay_cvvno"></i><div class="cpmt_CVVtxt" id="cc_cvvImageText_id" defaultval="3 digit CVV Number">3 digit CVV Number</div></div></div></span>';
					qbCards += '<input type="password" name="qb_cvv" value="" id="' + cvvId + noOfCards + '" readonly autocomplete="new-password" style="background-color: #DEDEDE;padding:7px"';
					qbCards += ' processCVV="' + processCVV + '" class="w45 cpmt_alignMid ie6Input cvvInput ' + cvvId + '" maxlength=' + qbCardsArray[i].cvvLength + '/></div></label>';
					// comenting the below line to remove the cross button on saved caads
					// qbCards += '<span class="remove" style="display:none"><i class="PaymentSprite removeIcon"></i></span>';
					
					if(qbCardsArray[i].cardBrand == 'AMEX'){
						qbCards = YatraQBPaymentUI.getAMEXAddressDiv(qbCards);
					}
					
					qbCards = YatraQBPaymentUI.getBillingAdddressDiv(qbCards,i);
					qbCards += '</li>';
					qbCards = YatraQBPaymentUI.getMultiPayFlowDiv(qbCards,qbCardsArray[i].cardId);
					
					
					
					noOfCards++;
					counter++;
				}	
			}
			if(noOfCards == 0)
				return null;
			
			if(noOfCards > 2) {
				qbCards += '<a class="cpmt_flR" href="javascript:void(0);" onclick=\'YatraPaymentUI.pushGAInfoForQuickBook("View More Quickbook Cards")\' attribute="view_more">View More</a>';
			}
			
			qbCards += '</ul>';
			return qbCards;
		},

		validateQBAuth : function() {
			var selectedTab = YatraPaymentUI.getPaymentOption();
			
			// Validate QB auth only when selected tab is either cc or dc.
			if(selectedTab != 'cc' && selectedTab != 'dc') {
				return true;
			}
			
			// If user is making complete payment through wallet, return true (No validation required).
			if(YatraEWalletPaymentUI.isCompleteEwalletPayment()) {
				return true;
			}
				
			var newCardSelected = false;
			if((selectedTab == "cc") || (selectedTab == "dc")) {
				newCardSelected = true;
			}
			
			if(showAuthDiv && !newCardSelected) {
				YatraQBPaymentUI.showQBAuthFailMessage();
				YatraQBPaymentUI.showQBPassReqdMessage();
				return false;
			}
			return true;
		},
		
		showQBAuthFailMessage : function() {
			
			// Hide "Enter password" message in case of IE < 10
			YatraQBPaymentUI.IE_hideQBPassMessage();
			
			$("#qb_auth_fail_msg_cc").show();
			$("#qb_auth_fail_msg_dc").show();
		},
		
		showQBPassReqdMessage : function() {
			var selectedTab = YatraPaymentUI.getPaymentOption();
			if(selectedTab == "cc") {
				Toast.show('* required', 'qb_password_cc');
				$("#qb_password_cc").focus();
			}
			if(selectedTab == "dc") {
				Toast.show('* required', 'qb_password_dc');
				$("#qb_password_dc").focus();
			}
		},

		hideQBAuthFailMessage : function() {
			if ($("#qb_auth_fail_msg_cc")) {
				$("#qb_auth_fail_msg_cc").hide();
			}
			if ($("#qb_auth_fail_msg_dc")) {
				$("#qb_auth_fail_msg_dc").hide();
			}
		},
		
		getQBAuthRequiredMessage : function() {
			var message = 'For security reasons please log in to proceed';
			return message;
		},

		getQBAuthFailedMessage : function() {
			var message = 'Your Yatra login failed. Please try again or proceed with some other payment option.';
			return message;
		},
		
		IE_showQBPassMessage : function() {
			if ($.browser.msie && parseInt($.browser.version) < 10) {
				$("#qb_pass_msg_cc").show();
				$("#qb_pass_msg_dc").show();
			}
		},
		
		IE_hideQBPassMessage : function() {
			if ($("#qb_pass_msg_cc")) {
				$("#qb_pass_msg_cc").hide();
			}
			if ($("#qb_pass_msg_dc")) {
				$("#qb_pass_msg_dc").hide();
			}
		},
		
		IE_getQBPassMessage : function() {
			var message = 'Enter your password.';
			return message;
		},

		registerDeleteCardEvent : function() {
			
			$('.js-quick li .removeIcon').each(function(index) {
				
				$(this).on('click', function() {
					
					var thisIndex = $(this).parents("li").attr("li_index");
					var cardId = $(this).parents("li").find(".custom-Radiobox input").attr("value");
					var thisLiClass = $(this).parents("li").attr("class");
					thisLiClass = thisLiClass.substring(0, thisLiClass.indexOf("_"));
					
					// reset card entry
					YatraPaymentUI.callOnCardEntry('RESET','','');

					// Get no of cards.
					var noOfCards = $(this).parents("ul").children("li").length;
					
					if(noOfCards > 1) {
					
						// Remove the current card.
						$(this).parents("li").remove();
							
						// Show the next card.
						$('.js-quick .' + thisLiClass + '_' + (parseInt(thisIndex) + 1)).show();
						
						// Logic to enable radio button of first CC / DC.
						var payOp = YatraPayment.getPayOp();
						if(thisLiClass == 'cc') {
							
							// This condition is required when user deletes QB card while he has selected to pay using new card.
							if(payOp == 'qb') {
								var firstCreditCard = $('#tab_ccQB input:radio[name="cardid"]:nth(0)');
								$(firstCreditCard).prop('checked', true);
								YatraPaymentUI.disableQBCVVForCreditCard($('input:radio[processCardId=cc]'));
                                YatraPaymentUI.disableMultiFlowUiForCreditCard($('input:radio[processCardId=cc]'));
                                YatraPaymentUI.handleInternationalQBCard(firstCreditCard);
                                YatraPaymentUI.enableQBCVVForCheckedCreditCard(firstCreditCard);
								if($(firstCreditCard).attr("cardNumberOriginal") != null && $(firstCreditCard).attr("cardNumberOriginal") != undefined) {
								    var cardNo = $(firstCreditCard).attr("cardNumberOriginal").replace(/-/g,'');
                                    var binDetails = YatraPaymentUI.getBinDetails(cardNo);
                                    YatraPaymentUI.setSkipOtpUiForQBIfRequired(firstCreditCard, "cc", binDetails);
                                    YatraPaymentUI.setMultiPayUiForQBIfRequired(firstCreditCard, "cc",binDetails);
								}
							}
							if(noOfCards == 2) {
								$("#tab_ccQB").find(".cpmt_flR").hide();
							}
						}
						if(thisLiClass == 'dc') {
							
							// This condition is required when user deletes QB card while he has selected to pay using new card.
							if(payOp == 'qb') {
								var firstDebitCard = $('#tab_dcQB input:radio[name="cardid"]:nth(0)');
								$(firstDebitCard).prop('checked', true);
								YatraPaymentUI.disableQBCVVForDebitCard($('input:radio[processCardId=dc]'));
								YatraPaymentUI.disableMultiFlowUiForDebitCard($('input:radio[processCardId=dc]'));
								YatraPaymentUI.handleInternationalQBCard(firstDebitCard);
								YatraPaymentUI.enableQBCVVForCheckedDebitCard(firstDebitCard);
								if($(firstDebitCard).attr("cardNumberOriginal") != null && $(firstDebitCard).attr("cardNumberOriginal") != undefined) {
									var cardNo = $(firstDebitCard).attr("cardNumberOriginal").replace(/-/g,'');
                                    var binDetails = YatraPaymentUI.getBinDetails(cardNo);
                                	YatraPaymentUI.setSkipOtpUiForQBIfRequired(firstDebitCard, "dc",binDetails);
                                    YatraPaymentUI.setMultiPayUiForQBIfRequired(firstDebitCard, "dc",binDetails);
								}
							}
							if(noOfCards == 2) {
								$("#tab_dcQB").find(".cpmt_flR").hide();
							}
						}
						
					} else {
					
						// Remove the current card.
						$(this).parents("li").remove();
						
						if(thisLiClass == "cc") {
							
							// Hide QB logo.
							$("#tab_ccQB").find(".div_quickBook").hide();
							$('#payWithQB').html("Pay with Credit Card");
							$('#cc_newCard .heading-p').show();
							
							// Set QB enabled to false.
							$('#cc_qbEnabled').val('false');
							
							// Hide "use new CC" radio button and show CC div.
							$("#qb_newCreditCard").hide();
							$("#cc_newCard").show();
							$('#tab_cc_qb > input[name="payop"]').val('cc'); 

							// The payment option now is CC. Notify it to LOB.
							YatraPaymentUI.notifyPayopToLOB();
						}
						if(thisLiClass == "dc") {
							
							// Hide QB logo.
							$("#tab_dcQB").find(".div_quickBook").hide();
							$('#payWithQB').html("Pay with Debit Card");
							$('#dc_newCard .heading-p').show();

							// Set QB enabled to false.
							$('#dc_qbEnabled').val('false');
							
							// Hide "use new DC" radio button and show DC div.
							$("#qb_newDebitCard").hide();
							$("#dc_newCard").show();	
							$('#tab_dc_qb > input[name="payop"]').val('dc');

							// The payment option now is DC. Notify it to LOB.
							YatraPaymentUI.notifyPayopToLOB();
						}
					}
					
					var paymentJSON = getPaymentJSON();
					var merchant = paymentJSON.merchant;
					var product = paymentJSON.product;
					YatraQBPaymentUI.deleteQBCard(cardId, merchant, product);
					
				})
			});
		},
		
		innerSwitch : function(qbDiv){
			
			var quickBlock = $(qbDiv);
			$('#dc_quickOrCard').prop('checked',true);
			$('#cc_quickOrCard').prop('checked',true);
			setTimeout( function(){
				quickBlock.find('ul.noListStyle li').not('li:eq(0), li:eq(1)').slideUp();
				//quickBlock.find('ul.noListStyle > li:eq(1)').show();
				//$('#tab_ccQB .js-quick li').not('li:eq(0), li:eq(1)').slideUp()
				//quickBlock.find('ul.noListStyle > li:first').show();				
			}, 100);
			quickBlock.find('.noListStyle').off("click");
			quickBlock.find('.noListStyle').on('click', 'a.cpmt_flR', function(e){
				var _this = this;
				if($(this).attr("attribute") == "view_more"){
					$(this).parents('.noListStyle').find('li:not(:visible)').slideDown();
					$(this).attr("attribute","view_less");
					$(this).text("View Less");
				}else if($(this).attr("attribute") == "view_less"){
					var liLength = $(this).parents('.noListStyle').find('li').length;
					for(var i = 2;i<liLength;i++){
						$(_this).parents('.noListStyle').find('li:eq'+'('+i+')').slideUp();
					}
					
					//quickBlock.find('ul.noListStyle > li:eq(0)').show();
				//quickBlock.find('ul.noListStyle > li:eq(1)').show();
					
					
					$(this).attr("attribute","view_more");
					$(this).text("View More");
					// Enable the firct CC or DC saved card, depending upon the selected tab.
					var paymentOption = YatraPayment.getPayOp();
					if(paymentOption == 'qb') {
					
						var selectedTab = YatraPaymentUI.getPaymentOption();

						if(selectedTab == "cc"){
							// Enable radio button for first CC.
							var firstCreditCard = $('#tab_ccQB input:radio[name="cardid"]:nth(0)');
							$(firstCreditCard).attr("checked","checked");
							firstCreditCard.click();
                            if($(firstCreditCard).attr("cardNumberOriginal") != null && $(firstCreditCard).attr("cardNumberOriginal") != undefined) {
                                var cardNo = $(firstCreditCard).attr("cardNumberOriginal").replace(/-/g,'');
                                var binDetail = YatraPaymentUI.getBinDetails(cardNo);
                                YatraPaymentUI.disableMultiFlowUiForCreditCard($('input:radio[processCardId=cc]'));
                            }

							$('#tab_ccQB ul li:first').addClass('active').siblings().removeClass('active');
							YatraPaymentUI.disableQBCVVForCreditCard($('input:radio[processCardId=cc]'));
							YatraPaymentUI.handleInternationalQBCard(firstCreditCard);
							YatraPaymentUI.enableQBCVVForCheckedCreditCard(firstCreditCard);
							YatraPaymentUI.setSkipOtpUiForQBIfRequired(firstCreditCard, "cc", binDetail);
							YatraPaymentUI.setMultiPayUiForQBIfRequired(firstCreditCard, "cc",binDetail);
						}
						else if(selectedTab == "dc"){
							// Enable radio button for first DC.
							var firstDebitCard = $('#tab_dcQB input:radio[name="cardid"]:nth(0)');
							$(firstDebitCard).attr("checked","checked");
							firstDebitCard.click();
                            if($(firstDebitCard).attr("cardNumberOriginal") != null && $(firstDebitCard).attr("cardNumberOriginal") != undefined) {
                            	var cardNo = $(firstDebitCard).attr("cardNumberOriginal").replace(/-/g,'');
                                var binDetail = YatraPaymentUI.getBinDetails(cardNo);
                                YatraPaymentUI.disableMultiFlowUiForDebitCard($('input:radio[processCardId=dc]'));
                            }

							$('#tab_dcQB ul li:first').addClass('active').siblings().removeClass('active')
							YatraPaymentUI.disableQBCVVForDebitCard($('input:radio[processCardId=dc]'));
							YatraPaymentUI.handleInternationalQBCard(firstDebitCard);
							YatraPaymentUI.enableQBCVVForCheckedDebitCard(firstDebitCard);
							YatraPaymentUI.setSkipOtpUiForQBIfRequired(firstDebitCard, "dc",binDetail);
							YatraPaymentUI.setMultiPayUiForQBIfRequired(firstDebitCard, "dc",binDetail);
						}
					}
					//$(this).text("View More");
				}
				
			});
		},
		
		authenticateYatraUserForQB : function(){
			
			var authURL = YatraQBPaymentUI.getQBAuthURLForYatraUser();
			var paymentJSON = getPaymentJSON();
			var merchant = paymentJSON.merchant;
			var product = paymentJSON.product;
					
			var selectedTab = YatraPaymentUI.getPaymentOption();
			var passwordFieldId = "qb_password" + "_" + selectedTab;
			var emailFieldId = "qb_user_email" + "_" + selectedTab;
			var password = $("#" + passwordFieldId).val();
			var email = $("#" + emailFieldId).val();
			
			if(password == '' || password == undefined || password == null) {
				YatraQBPaymentUI.showQBPassReqdMessage();
				return;
			}
			
			YatraPaymentUI.disableNewCreditCard();
			YatraPaymentUI.disableNewDebitCard();
			// YatraPaymentUI.notifyPayopToLOB();
			
			$.ajax({
				url: authURL,
				timeout: (5 * 1000),
				type: 'POST',
				data: { 
				        'merchant': merchant, 
				        'product': product,
				        'email' : email,
				        'password' : password
				},
				cache: false,
				
				success : function(response){
					if(JSON.parse(response).status == "ENABLED" && JSON.parse(response).quickBookCards.length)
						YatraPaymentUI.adobeTrackPaymentQBData();
						
					
					YatraQBPaymentUI.generateQBHtmlFromJson(response);
					//Checking if Auth DIv present for Corp, if yes, rendering those cards as well
					 if(isssoTokenStaleForCorpCards != undefined &&  isssoTokenStaleForCorpCards == true)
						YatraCorpCardUI.getCorpCardsAfterReLogin();
				}, 
				
				error: function (request, status, error) {
					alert("Error occurred while authenticating.. Please try again or use some other payment option..")
				}
			});	
		},
		
		getQBAuthURLForYatraUser : function(){
			return "/PaySwift/authenticate-yatra-user.htm";
		},

		authenticateFBUserForQB: function(){
			YatraPaymentUI.disableNewCreditCard();
			YatraPaymentUI.disableNewDebitCard();
			YatraFBPaymentUI.loginByFacebook();
		}, 
		
		resetQB : function(qbDiv){
			
			var quickBlock = $(qbDiv);
						
			setTimeout( function(){
				quickBlock.find('ul.noListStyle li').not('li:eq(0), li:eq(1)').slideUp();
				//quickBlock.find('ul.noListStyle > li:first').show();				
			}, 100);
			
			quickBlock.find('ul.noListStyle a').attr("attribute", "view_more");
			quickBlock.find('ul.noListStyle a').text("View More");
		},
		
		deleteQBCard : function(cardId, merchant, product) {
			var qbDeleteCardURL = YatraQBPaymentUI.getQBDeleteCardURL();
			
			$.ajax({
				
				url: qbDeleteCardURL,
				type: 'POST',
				data: { 
				        'merchant': merchant, 
				        'product': product,
				        'cardid' : cardId,
				        'csrfToken' : csrfToken
				},
				cache: false,

				success : function(response) {
				}, 
				
				error: function (request, status, error) {
					alert("Error while deleting quickbook card.");
				}
			});
		},
		
		getQBDeleteCardURL : function () {
			return "/PaySwift/delete-qb-card.htm"
		},
		
		getQBCardId : function() {
			var paymentOptionDiv = YatraPayment.getPaymentDiv();
			return $(paymentOptionDiv +" input[name= cardid]:checked").val();
		},
		
		getQBCardNumber : function() {
			var paymentOptionDiv = YatraPayment.getPaymentDiv();
			return $(paymentOptionDiv +" input[name= cardid]:checked").attr("cardNumberOriginal").trim().replace(/-/g, "");
			//return $(paymentOptionDiv +" input[name= cardid]:checked").parent().parent().find(".cpmt_qbCardNo").text().trim().replace(/-/g, "");
		},
		
		getQBCardType : function() {
			var paymentOptionDiv = YatraPayment.getPaymentDiv();
			return $(paymentOptionDiv +" input[name= cardid]:checked").attr("cardtype");
		},

		getMultiPayFlowDiv : function(qbCards, cardId){
			qbCards += '<div class="qb_multiFlowDiv" id="qb_pmt_MultiFlowId_'+ cardId +'" style="display:none;font-size: 12px;margin-top: 0px;position: relative;top: -6px;clear:both">';
			qbCards +='<span style="font-weight: 600;color: #666;">Complete your payment using</span>';
			qbCards += '<div class=" wfull" style="margin-bottom: 10px;margin-top: 8px;color: #666;" id="multiflow_type_div_'+ cardId+'">';
	    	qbCards += '</div></div>';
	    	return qbCards;
	    }
};var YatraCorpCardUI = {
        cardList: [],
        cardListUrl:"/PaySwift/corporate-quickbook/corporateCardsTokenizationStatus?corporateId",
		updateCorpCardDiv : function() {
		    if(isssoTokenStaleForCorpCards != undefined &&  isssoTokenStaleForCorpCards == true) {
                YatraCorpCardUI.getCorpCardAuthenticationDiv();
            }
		},
		
		initialise : function() {
		    //YatraCorpCardUI.getSavedCards();
			var firstCard = $('#corpCard input[type="radio"]')[0];
			$(firstCard).attr("checked","checked");
			YatraCorpCardUI.handleCorpUI(firstCard);
			
		},
		
		getCorporateCards : function(ssoToken){},
		
		getCorpCardURL : function(){
			return "/PaySwift/corporate-cards-html";
		},

		getCorpCardAuthenticationDiv : function(){
			var emailId = "";
			if(detailsJson != undefined && detailsJson != null) {
				var userDetails = detailsJson.userDetails;
				if(userDetails != undefined && userDetails != null) {
					emailId = userDetails.email;
				}
			}
			$("#corp_user_email").val(emailId);
			$("#corpCardAuthRequire").show();
			$("#corpLogin").show();
			YatraPaymentUI.recordEventInSentry("Corp Card: ssotoken stale, Authentication Required", "info",
					"corpcard-access-auth-require", "emailId", emailId);
		},

		setUserEmailForAuth: function(){
		    var emailId = "";
		    if(detailsJson != undefined && detailsJson != null) {
		        var userDetails = detailsJson.userDetails;
		        if(userDetails != undefined && userDetails != null) {
		            emailId = userDetails.email;
		        }
		    }
        	$("#corp_user_email").val(emailId);
        },

		authenticateCorporateUser : function(){
		    var emailId = $("#corp_user_email").val();
		    var password = $("#corp_user_password").val();

			$.ajax({
				url : '/PaySwift/corporate-login.htm',
				type : 'POST',
				data : "emailId="+emailId+"&password="+password,
				success : function(response) {
				    if(response != null) {
				         if(response.status == 'SUCCESS') {
				            YatraCorpCardUI.updateCorpCardDivOnLoginSuccess(emailId, response.tokenId);
				         }
				         if((response.status == 'FAIL')) {
				            YatraCorpCardUI.updateCorpCardDivOnLoginFailure();
				            YatraPaymentUI.recordEventInSentry("Corp Card: Failed to authenticate", "error",
                                        								"corpcard-access-auth-fail", "emailId", emailId);
				         }
				    }
				}
			});
		},

		updateCorpCardDivOnLoginSuccess : function(emailId, ssoToken) {
            $("#corpCardAuthRequire").hide();
            $("#corpLogin").hide();
            $("#corpAuthFailMsg").hide();
            $('#waitCorpCards').show();
            YatraCorpCardUI.updatessoTokenInB2BService(emailId, ssoToken);
		},

		updateCorpCardDivOnLoginFailure : function() {
		    $("#corpAuthFailMsg").show();
        },

		updatessoTokenInB2BService : function(email, ssoToken){
			$.ajax({
				url : '/PaySwift/agent-login.htm',
				type : 'POST',
				data : {"email":email, "ssoToken":ssoToken},
				success : function(response) {
				    if(response == null || response.resCode != '200') {
				        $("#corpAuthFailMsg").text('Your login failed. Please proceed with some other payment option');
				        $("#corpAuthFailMsg").show();
				    }
				    if(response != null && response.resCode == '200'){
				    	
				        YatraCorpCardUI.getCorpCardsAfterReLogin(ssoToken);
				        YatraQBPaymentUI.getQBJsonAndGenerateHtml();
				    }
				}
			});
		},

        getCorpCardsAfterReLogin : function(ssoToken) {
			$.ajax({
				url : '/PaySwift/corporate-cards-json.htm',
				type : 'POST',
				data : "ssoToken="+ssoToken,
				success : function(response) {
				    var jsonObject = $.parseJSON(response);
                    var div = YatraCorpCardUI.getCorpCardsDiv(jsonObject);
                    $('.corpCardArticleTab').html('');
                    $('#waitCorpCards').hide();
                    $("#corpCardAuthRequire").hide();
                    $("#corpLogin").hide();
                    $("#corpAuthFailMsg").hide();
                    $('.corpCardArticleTab').html(div);
                    YatraCorpCardUI.initialise();
				}
			});
        },

        getCorpCardsDiv : function(jsonObject) {
            var output = '<ul class="noListStyle">';
            for(var key in jsonObject) {
                var cid = jsonObject[key]['cardId'];
                var cType = jsonObject[key]['cardType'];
                var cardNumber1 = jsonObject[key]['cardNumber1'];
                var cardNumber2 = jsonObject[key]['cardNumber2'];
                var cardNumber3 = jsonObject[key]['cardNumber3'];
                var cardNumber4 = jsonObject[key]['cardNumber4'];
                var firstName =  jsonObject[key]['firstName'];
                var lastName =  jsonObject[key]['lastName'];

                output += '<li>';
                output += '<label style="width:82%" for=' + cid + '>';
                output += '<span class="custom-Radiobox" style="visibility:hidden;margin-left:-10px;"><input type="radio" name="cardId" id=' + cid + ' value=' + cid + ' cardType =' + cType + ' cardBrand=' + jsonObject[key]['cardBrand'] + ' cardNumber =' + cardNumber1 + cardNumber2 + cardNumber3 + cardNumber4 + ' processCardId="corpCard">';
                output += '<span class="box"><b class="tick"></b></span></span>';
                output += '<i class="PaymentSprite ' + jsonObject[key]['cardBrand'] + '_cc"></i>';
                output += '<span class="cpmt_qbCardType">' + cType + '</span><span class="cpmt_qbCardNo">' + cardNumber1 + '-' + cardNumber2 + '-' + cardNumber3 + '-' + cardNumber4 + '</span></label>';
                if(cType == 'CORPORATE') {
                    output += '<span class="corporateCvv"><label for="corpCard_ccCVV ie6"><span class="cpmt_CVVno" style="display:none">CVV<small class="cpmt_cvvLength">3-digit Number</small></span>';
                    output += '<input type="password" value="" id="corporate_cvv_id" placeholder="CVV" name="corpCard_ccCVV" processCVV="corpCardCVV" class="w45 cvvInput cpmt_alignMid ie6Input" maxlength=3  data-validation="required|regType[number]|lenCheck[3]" data-msginfo="* required|* number only|* invalid cvv length" />';
                    output += '</label></span>';
                }
            output += '<div style="display: block; width: 50%; margin-left: 170px; ">'+ firstName + ' ' + lastName +'</div></li>'
            
            }
            output += '</ul>';
            return output;
        },
		handleCorpUI : function (ele){
			$('#corpCard_cardType_id').val($(ele).attr("cardType"));
			$('#corpCard_cno_id').val($(ele).attr("cardNumber"));
			$('#corpCard_cardBrand_id').val($(ele).attr("cardBrand"));
			$('#corpCard_email_id').val($(ele).attr("email"));
			$('#corpCard_mob_id').val($(ele).attr("mob"));
			$('#corpCard_cvv_id').val("");
			$(".corporateCvv").hide()
			var li = $(ele).parents().eq(2);
			$(li).find('.corporateCvv').show();
			var $allCvvInputs = $('#tab_corpCard input[name=corpCard_ccCVV]');
			// clear all ccv inputs
			$allCvvInputs.each(function() {
				$(this).val("");
			});
		},
         getTokenizedCardList: function() {
            var tokenCardList = YatraCorpCardUI.cardListUrl;
            $.ajax({
                url: tokenCardList + "=" + detailsJson.crpInfo.clientId,
                type: "GET",
                cache: false,
                success: function(res) {
                    if(res.corpTokenizedCards && res.corpTokenizedCards.length>0) {
                        YatraCorpCardUI.cardList.push(res.corpTokenizedCards);
                    }
                    YatraCorpCardUI.tagTokenizedCardsList();
                },
                error: function(request, status, error) {
                    alert("Error occurred while getting saved cards.");

                }
            });
        },
        tagTokenizedCardsList: function() {
            if (YatraCorpCardUI.cardList && YatraCorpCardUI.cardList[0].length > 0) {
                for (var c = 0; c < YatraCorpCardUI.cardList[0].length; c++) {
                    var cardId = YatraCorpCardUI.cardList[0][c].cardId;
                    if (YatraCorpCardUI.cardList[0][c].cardtype === "PCC" && YatraCorpCardUI.cardList[0][c].tokenStatus === "INAC") {
                        $("[li-card-id=" + cardId + "]").hide()
                    }
                    else if (YatraCorpCardUI.cardList[0][c].tokenStatus === "ACTI") {
                        $("[card-id=" + cardId + "]").show()
                    }
                }
            }
        },
		getSSOToken : function(){}
};