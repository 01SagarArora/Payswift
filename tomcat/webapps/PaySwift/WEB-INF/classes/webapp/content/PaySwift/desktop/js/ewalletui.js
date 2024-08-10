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
};