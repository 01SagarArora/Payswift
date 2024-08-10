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
