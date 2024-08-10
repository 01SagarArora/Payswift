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
