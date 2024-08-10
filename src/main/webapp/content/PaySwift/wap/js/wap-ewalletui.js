var YatraEWalletPaymentUI = {

	eCashJsonURL : "/checkout/wallet/get-ecash.htm",
	redeemAuthURL : "/checkout/wallet/auth.htm",
	reverseAuthURL :  "/checkout/wallet/reverse-auth.htm",
	redeemCapURL : "/checkout/wallet/cap.htm",
	sliderMaxLimit : 0,
	
	eCashJSON : null,
	redeemAuthJSON : null,
	reverseAuthJSON : null,

	init : function() {
		if(enableEWallet) {
			this.updateEWalletHTML();

			$("#redeem-ecash-button").on('click', function() {
				$("#ecash-error-message").hide();
				$(".redeem-btn .ui-btn").removeClass("ui-focus");
				var paymentJSON = getPaymentJSON();
				var amount = parseInt(paymentJSON.amount);
				var amountTobeRedeemed = parseInt(YatraEWalletPaymentUI.getAmountToBeRedeemed());
				if(amountTobeRedeemed > 0) {
					if (amount >= amountTobeRedeemed) {
						YatraEWalletPaymentUI.redeemAuth();
					//	YatraEWalletPaymentUI.disableButton("redeem-ecash-button");
					}
				} else {
					YatraEWalletPaymentUI.setErrorMessage("Redeemption Amount must be greater than 0.");
				}
			});
			
			$("#cancel-redemption-button").on('click', function() {
				$("#ecash-error-message").hide();
				$(".cancel-redem .ui-btn").removeClass("ui-focus");
				YatraEWalletPaymentUI.reverseAuth();
			});
		}
	},

	updateEWalletHTML : function() {
		var ssoToken = YatraQBPaymentUI.getSSOToken();
		if(enableEWallet && ssoToken != undefined && ssoToken != null) {
			this.getEWalletJSON();
		} else {
			$("#ewallet_div").hide();
		}
	},

	getEWalletJSON : function() {
		var paymentJSON = getPaymentJSON();
		if(paymentJSON != undefined && paymentJSON != null) {
			$.ajax({
				url : this.eCashJsonURL, type : 'POST', data : {
					'cust_email' : paymentJSON.userEmail, 'merchant_code' : paymentJSON.merchant, 'product_code' : paymentJSON.product
				}, cache : false, 
				async : false,
				success : function(response) {
					YatraEWalletPaymentUI.eCashJSON = $.parseJSON(response);
					YatraEWalletPaymentUI.setEWalletUI();
				}, error : function(request, status, error) {
					YatraEWalletPaymentUI.eCashJSON = YatraEWalletPaymentUI.getErrorJSON("Error occurred while getting eCash JSON");
				}
			});			
		}
	},

	setEWalletUI : function() {
		
		var ssoToken = YatraQBPaymentUI.getSSOToken();
		if(ssoToken == undefined || ssoToken == null) {
			return;
		}
		
		if (enableEWallet && YatraEWalletPaymentUI.eCashJSON != null && YatraEWalletPaymentUI.eCashJSON.status == true) {
			var totalRedeemableEcash = YatraEWalletPaymentUI.getAmountInRupee(YatraEWalletPaymentUI.eCashJSON.totalEcash.redeemableECashInPaisa);
			if (totalRedeemableEcash > 0) {
				// Setting total amount
				var paymentJSON = getPaymentJSON();
				$(".total-amount-due").html(YatraHelper.formatPrice(paymentJSON.amount));

				// Setting redeemable eCash for this booking
				var maxECashRedeemableForThisBooking = 0;
				var redeemableExpirableECash = 0;
				var redeemableNonExpirableECash = 0;
				var node1 = YatraEWalletPaymentUI.eCashJSON.ECashList[0];
				var node2 = YatraEWalletPaymentUI.eCashJSON.ECashList[1];

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

				if (maxECashRedeemableForThisBooking > paymentJSON.amount) {
					maxECashRedeemableForThisBooking = paymentJSON.amount;
				}
				
				sliderMaxLimit = maxECashRedeemableForThisBooking;
				
				$("#total-redeemable-ecash").html(YatraHelper.formatPrice(maxECashRedeemableForThisBooking));
				$("#totalECash").val(YatraHelper.formatPrice(totalRedeemableEcash));
				$("#totalRedeemableECash").val(YatraHelper.formatPrice(maxECashRedeemableForThisBooking));
				$("#eCashToBeRedeemed").val(maxECashRedeemableForThisBooking);
				$("#eCashToBeRedeemed").attr("max",maxECashRedeemableForThisBooking);
				if($("#eCashToBeRedeemed").is(":visible") && $("#eCashToBeRedeemed").hasClass("ui-slider-input")){
					$("#eCashToBeRedeemed").on('slidestop',function(event){
						$("#total-redeemable-ecash").html(YatraHelper.formatPrice(event.target.value));
					});
				}
				try{
				$("#eCashToBeRedeemed").slider("refresh");
				}catch(e){}
				YatraEWalletPaymentUI.setRedeemSlider();
				YatraEWalletPaymentUI.setPaymentOptionDiv();
				$("#ewallet_div").show();

			}
		} 
	},
	
	redeemAuth : function() {
		var paymentJSON = getPaymentJSON();
		$.ajax({
			url : this.redeemAuthURL,
			type : 'POST',
			data : {
				'cust_email' : paymentJSON.userEmail, 'amount' : YatraEWalletPaymentUI.getAmountToBeRedeemed(), 'ttid' : paymentJSON.superPNR, 
				'merchant_code' : paymentJSON.merchant, 'product_code' : paymentJSON.product, 'ylp_max' : paymentJSON.wallet.maxRedeemableECash
			}, cache : false, 
			async : false,
			success : function(response) {
				YatraEWalletPaymentUI.redeemAuthJSON = $.parseJSON(response);
				YatraEWalletPaymentUI.setRedeemUI();
				YatraPaymentUI.onAmountChange();
			}, error : function(request, status, error) {
				YatraEWalletPaymentUI.redeemAuthJSON = YatraEWalletPaymentUI.getErrorJSON("Error occurred while redemption of eCash.");
				YatraEWalletPaymentUI.setErrorMessage("Error occurred while redeemimg eCash.");
			}
		});
	},
	
	setRedeemSlider : function(){
		
		var paymentJSON = getPaymentJSON();
		if(YatraEWalletPaymentUI.redeemAuthJSON != null && YatraEWalletPaymentUI.redeemAuthJSON!= undefined){
			var redeemedAmount = YatraEWalletPaymentUI.getAmountInRupee(YatraEWalletPaymentUI.redeemAuthJSON.amountInPaisa);
			$(".total-redeemed-ecash").html(YatraHelper.formatPrice(redeemedAmount));
			$("#eCashRedeemed").val(YatraHelper.formatPrice(redeemedAmount));
			$("#amount-balance").html(YatraHelper.formatPrice(parseInt(paymentJSON.amount) - parseInt(redeemedAmount)));
		}
	},
	
	setPaymentOptionDiv : function(){
		var paymentJSON = getPaymentJSON();
		if(YatraEWalletPaymentUI.redeemAuthJSON != null && YatraEWalletPaymentUI.redeemAuthJSON!= undefined){
			var redeemedAmount = YatraEWalletPaymentUI.getAmountInRupee(YatraEWalletPaymentUI.redeemAuthJSON.amountInPaisa);
			var amount = parseInt(paymentJSON.amount);
			
			if(amount == redeemedAmount) {
				YatraEWalletPaymentUI.hidePaymentDiv();
				$('#tab_' + YatraPaymentUI.getPaymentOption() + ' > input[name="payop"]').val('ew');
			}else{
				YatraEWalletPaymentUI.showPaymentDiv();
				$('#tab_' + YatraPaymentUI.getPaymentOption() + ' > input[name="payop"]').val(YatraPaymentUI.getPaymentOption());
			}
		}
	},

	setRedeemUI : function() {
		var paymentJSON = getPaymentJSON();
		if(YatraEWalletPaymentUI.redeemAuthJSON != null && YatraEWalletPaymentUI.redeemAuthJSON!= undefined){
			if (YatraEWalletPaymentUI.redeemAuthJSON.status == true) {
				var redeemedAmount = YatraEWalletPaymentUI.getAmountInRupee(YatraEWalletPaymentUI.redeemAuthJSON.amountInPaisa);
				YatraEWalletPaymentUI.setRedeemSlider();

				$("#redeem-slider").fadeOut('fast');
				$(".redeem-block .ui-slider").fadeOut('fast');
				$("#redeem_label").fadeOut('fast');
				$('#redeem-success-block').fadeIn('fast');
				
				// Notify LOB for eCash redemption
				processWalletPayment("REDEEM", redeemedAmount);
				
				//Hide Payment Options in case of complete payment through eCash
				YatraEWalletPaymentUI.setPaymentOptionDiv();
				
			} else {
				YatraEWalletPaymentUI.setErrorMessage("Error occurred while redeemimg eCash.");
			}
		}
	},

	reverseAuth : function() {
		var paymentJSON = getPaymentJSON();
		$.ajax({
			url : this.reverseAuthURL,
			type : 'POST',
			data : {
				'cust_email' : paymentJSON.userEmail, 'ttid' : paymentJSON.superPNR, 'amount' : YatraEWalletPaymentUI.getAmountInRupee(YatraEWalletPaymentUI.redeemAuthJSON.amountInPaisa), 
				'wallet_id' : YatraEWalletPaymentUI.redeemAuthJSON.walletId, 'merchant_code' : paymentJSON.merchant, 'product_code' : paymentJSON.product
			}, cache : false,
			async : false,
			success : function(response) {
				YatraEWalletPaymentUI.reverseAuthJSON = $.parseJSON(response);
				YatraEWalletPaymentUI.setReverseAuthUI();
				YatraEWalletPaymentUI.redeemAuthJSON = null;
				YatraPaymentUI.onAmountChange();
			}, error : function(request, status, error) {
				YatraEWalletPaymentUI.reverseAuthJSON = YatraEWalletPaymentUI.getErrorJSON("Error occurred while cancelling eCash redeemed.");
				YatraEWalletPaymentUI.setErrorMessage("Error occurred while cancelling redeemed eCash.");
			}
		});
	},

	setReverseAuthUI : function() {
		if (YatraEWalletPaymentUI.reverseAuthJSON.status == true) {

			$(".total-redeemed-ecash").html("");
			$("#eCashRedeemed").val("");
			$("#amount-balance").html("");

			// Notifying LOB for eCash redemption
			processWalletPayment("CANCEL-REDEEM", YatraEWalletPaymentUI.getAmountInRupee(YatraEWalletPaymentUI.reverseAuthJSON.amountInPaisa));
			YatraEWalletPaymentUI.redeemAuthJSON.amountInPaisa = 0;
			YatraEWalletPaymentUI.redeemAuthJSON.status = false;
			//Show Payment Options in case of reverse auth
			YatraEWalletPaymentUI.setPaymentOptionDiv();
			
			$('#redeem-success-block').fadeOut('fast');
			$('#redeem-slider').fadeIn('fast');
			$('.redeem-block .ui-slider').fadeIn('fast');
			$("#redeem_label").fadeIn('fast');
			
		} else {
			YatraEWalletPaymentUI.setErrorMessage("Error occurred while cancelling redeemed eCash.");
		}
	},

	refreshEWalletUI : function() {
		YatraEWalletPaymentUI.updateEWalletHTML();
	},

	resetSlider : function() {
		$('#redeem-slider').slider({
			orientation: 'horizontal',
			range: 'min',
			value : 0,
			min: 0,
			max: sliderMaxLimit,
			step : 1,
			slide: function(event, ui){
				$("#eCashToBeRedeemed").val(ui.value);
				$("#eCashToBeRedeemed").slider("refresh");
			}
		});
	},
	
	hidePaymentDiv : function() {
		$("#cpmt_payment").hide();
	},
	
	showPaymentDiv : function() {
		$("#cpmt_payment").show();
	},
	
	disableButton: function(buttonId){
		$(buttonId).attr("disabled",true);
		$(buttonId).attr("style","cursor:default;");
		$(buttonId).parent().addClass("ui-focus");
	},
	
	enableButton: function(buttonId){
		$(buttonId).attr("disabled",false);
		$(buttonId).attr("style","cursor:pointer;");
		$(buttonId).parent().removeClass("ui-focus");
	},

	getAmountToBeRedeemed : function() {
		return $("#eCashToBeRedeemed").val();
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
	
	isCompleteEwalletPayment : function() {
		var paymentJSON = getPaymentJSON();
		if(YatraEWalletPaymentUI.redeemAuthJSON != null && YatraEWalletPaymentUI.redeemAuthJSON!= undefined){
			var redeemedAmount = YatraEWalletPaymentUI.getAmountInRupee(YatraEWalletPaymentUI.redeemAuthJSON.amountInPaisa);
			if(redeemedAmount != undefined){
				var amount = parseInt(paymentJSON.amount);
				return (amount == redeemedAmount);
			}
		}
		
		return false;
	}
};