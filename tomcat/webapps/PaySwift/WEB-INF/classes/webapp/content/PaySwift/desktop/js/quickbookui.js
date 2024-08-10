var enableQuickBook = false;
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
};