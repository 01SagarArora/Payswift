var YatraPayment = {	
		
		initialisePage : function(){
			ShoppingCart.intialise();
			},
			
		getParamValue : function(formId, key) {
			return $("#" + formId + " input[name=" + key + "]").val();
		},
		
		getPayOp : function(){				//modifying code to handle WAP Quickbook delete case, currently payOp returning undefined value
			var payOp = YatraPaymentUI.getPaymentOption();
			if(payOp == "")
				return payOp;
			else {
				var payOptionDiv = "#tab_" + YatraPaymentUI.getPaymentOption();
				return $(payOptionDiv).find('input[name=payop]').val();
			}
		},
		
		getPaymentDiv : function(){
			var payOptionDiv = "#tab_" + YatraPaymentUI.getPaymentOption();
			return payOptionDiv;
		},
		
		/**
		 * Function validating payment data
		 */
		validatePaymentDiv : function(formId){
			var isValid = PaymentValidation.validate('#' + formId);
			if(isValid) {
				isValid = YatraQBPaymentUI.validateQBAuth();
			}
			return isValid;
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
					parameters += YatraPayment.appendByKeyValue(this.name, $(this).val());
					};
				break;
				default:
					parameters += YatraPayment.appendByKeyValue(this.name, $(this).val());
				break;
				}
		    });
			if(payOption == 'emi') {			
				parameters += YatraPayment.appendByKeyValue('emiBank', $('#emiDropdown').val());
			} else if(payOption == 'atm'){
				parameters += YatraPayment.appendByKeyValue('atmOptns', $('#atmDropdown').val());
			}
			
			//Fix for ctype error
			try {
			    var payOption = YatraPaymentUI.getPaymentOption();
			    if (payOption == 'cc' || payOption == 'dc' || payOption == 'emi' || payOption == 'rewards') {
			        var parametersArray = parameters.split('|');
			        var paramMap = {};
			        for (var i = 0; i < parametersArray.length; i++) {
			            parameter = parametersArray[i];
			            var paramArray = parameter.split('=');
			            paramMap[paramArray[0]] = paramArray[1];
			        }
			        if (typeof paramMap['ctype'] == 'undefined') {
			            var ctype = YatraPaymentUI.getCardType(paramMap['cno']);
			            parameters += YatraPayment.appendByKeyValue('ctype', ctype);
			        }
			    }
			} catch (e) {}
			
			return parameters;
		},
		
		getAppendedQBParamters : function() {
			var parameters = "";
			var paymentOptionDiv = YatraPayment.getPaymentDiv();
			var $inputs = $(paymentOptionDiv+" input");
			
			$inputs.each(function() {
				var eType = $(this).prop("type");
				switch(eType) {
				case "radio":
					break;
				default:
					parameters += YatraPayment.appendByKeyValue(this.name, $(this).val());
					break;
				}
		    });
			
			return parameters;
		},
		
		getInternationalCardParameters : function(){
			
			var parameters = "";
			if(YatraPaymentUI.isInternationalSupportedOnProduct()){
				var paymentsInternationJson =getPaymentJSON().international; 
				parameters += YatraPayment.appendByKeyValue('sid', paymentsInternationJson.sid);
				parameters += YatraPayment.appendByKeyValue('tdate', paymentsInternationJson.travelDate);
				parameters += YatraPayment.appendByKeyValue('origin', paymentsInternationJson.origin);
				parameters += YatraPayment.appendByKeyValue('destination', paymentsInternationJson.destination);
				parameters += YatraPayment.appendByKeyValue('tripType', paymentsInternationJson.tripType);
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
			} else if (payOption == 'cashCard') {
				return contextUrl + '/checkout/pay/itz.htm';
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
			} else if (payOption == 'itz') {
				return contextUrl + '/checkout/pay/itz.htm';
			} else if (payOption == 'payu') {
				return contextUrl + '/checkout/pay/PayU.htm';
			}else if(payOption == 'cp'){
				return contextUrl + '/checkout/pay/creditPool.htm';
			}else if(payOption == 'corpCard'){
				return contextUrl + '/checkout/pay/corporateCard.htm';
			}else if (payOption == 'buddy') {
				return contextUrl + '/checkout/pay/mobileWallet.htm';
			}else if (payOption == 'amazonPay') {
				return contextUrl + '/checkout/pay/mobileWallet.htm';
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
			var fieldValue = field.val(value);
		},
		
		getPaymentOptionParameters : function(formId, merchantCode, productCode) {
			var paymentOptionParameters = "";
			var paymentJson = getPaymentJSON();
			var payOption = YatraPaymentUI.getPaymentOption();
			
			// 1. Set Merchant Code and Product Code
			paymentOptionParameters += YatraPayment.appendByKeyValue("merchant", paymentJson.merchant);
			paymentOptionParameters += YatraPayment.appendByKeyValue("product", paymentJson.product);
			
			// Payment parameters specific to complete payment through wallet case.
			if(YatraEWalletPaymentUI.isCompleteEwalletPayment()) {
				
				// payop = 'ew' in case of complete payment through wallet.
				paymentOptionParameters += YatraPayment.appendByKeyValue("payop", 'ew');
				
				// previousPayOp is the payment option corresponding to the tab that was selected when complete payment through wallet was made.
				// This parameter is used to determine convenience fee / discount that was applied when complete payment through wallet was made.
				if(!payOption){
					payOption = "cc";
				}
				paymentOptionParameters += YatraPayment.appendByKeyValue("previousPayOp", payOption);
				
			} else {
				
				// 3. Set Parameters based on Payment Option selected by User
				if(payOption == "qb") {
					paymentOptionParameters += YatraPayment.getAppendedQBParamters();
				} else {
					paymentOptionParameters += YatraPayment.getAppendedParamters(payOption);
				}
				
				// 3. Set parameters for International card
				paymentOptionParameters += YatraPayment.getInternationalCardParameters();
					
				// 4. Set Payment Option URL based on payment option
				var paymentOptionUrl = YatraPayment.getPaymentOptionUrl(payOption);
			}
			
			// Appending ECash redemption paramters
			if($("#eCashRedeemed").val() != null && $("#eCashRedeemed").val() != "") {
				paymentOptionParameters += YatraPayment.getECashRedemptionParamters();
			}
			
			return paymentOptionParameters;
//			// 5. Hash the Parameters
//			var paymentOptionHashedParameters = YatraPayment.getHashedParamters(paymentOptionParameters);
//			
//			// 6. Set the parameters in Form
//			YatraPayment.setFieldValue(formId, "paymentOptionUrl", paymentOptionUrl);
//			YatraPayment.setFieldValue(formId, "paymentOptionParameters", paymentOptionParameters);
//			YatraPayment.setFieldValue(formId, "paymentOptionHashedParameters", paymentOptionHashedParameters);
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
			
			var payOption = YatraPaymentUI.getParamValue("payop");
			
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
				
			} else if (payOption == 'atm') {
				paymentVendorName = YatraPayment.getParameterByName("bankCode");
				paymentOption = "atm";
				
			} else if (payOption == 'cashCard') {
				paymentVendorName = "ITZ Card";
				paymentOption = "ITZ Card";
				
			} else if (payOption == 'emi') {
				ecn = YatraPayment.getParameterByName("cno");
				bin = ecn.substring(0, 6);
				paymentVendorName = YatraPayment.getParameterByName("emiBank");
				paymentOption = "EMI Option";
			} else if(payOption == 'qb') {
				var isValid = YatraQBPaymentUI.validateQBAuth();
				if(isValid) {
					ecn = YatraQBPaymentUI.getQBCardNumber();
					bin = ecn.substring(0, 6);
					paymentVendorName = YatraQBPaymentUI.getQBCardType();
					paymentOption = "Quick Book";	
				} else {
					return null;
				}
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
			if(YatraEWalletPaymentUI.redeemAuthJSON){
				var redeemedAmount = YatraEWalletPaymentUI.getAmountInRupee(YatraEWalletPaymentUI.redeemAuthJSON.amountInPaisa);
				parameters += YatraPayment.appendByKeyValue("wallet_amount", redeemedAmount);
				parameters += YatraPayment.appendByKeyValue("wallet_id", YatraEWalletPaymentUI.redeemAuthJSON.walletId);
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
	    }
	    
}