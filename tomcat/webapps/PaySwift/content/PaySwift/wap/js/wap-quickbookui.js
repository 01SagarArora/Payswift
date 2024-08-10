var showQB = false;
var showQBCards = false;
var showSaveOption = false;
var showAuthDiv = false;
var qbAuthFailed = false;



var YatraQBPaymentUI = {
		
		qbJsonURL : "/PaySwift/qb.htm",
		qbDeleteCardURL : "/PaySwift/delete-qb-card.htm",
		forgotPasswordURL : "#forgotPassword",
		qbAuthURLForYatraUser : "/PaySwift/authenticate-yatra-user.htm",
		/*qbAuthRequiredMessage : "For security reasons please signin again to proceed with",
		qbAuthFailedMessage : 'Your Yatra login/authentication failed. Please re-try login or proceed with some other payment option.',*/
		qbAuthRequiredMessage : qbAuthRequiredMessageJSP,
		qbAuthFailedMessage : qbAuthFailedMessageJSP,
		
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
			
		//	qbJson='{"errorCode":null,"authMode":null,"quickBookCards":[{"addressSaved":false,"cardBrand":"AMEX","cardAccessType":"" ,"cardName":"Test Amex card","cardTypeLabel":"Credit Card","cvvLabel":"Enter CVV","cvvLength":"4","cardId":"702cb342-f7df-43d1-82cc-9869bf8a7099","cardNumber":"3782-82xxxx-x0005","cardLogoURL":"qb_amex_cc","cardHolderFirstName":"Test","cardHolderLastName":""},{"addressSaved":false,"cardBrand":"VISA","cardAccessType":"INT","cardName":"My Int Card","cardTypeLabel":"Credit Card","cvvLabel":"Enter CVV","cvvLength":"3","cardId":"61082fa3-3ecb-4fa5-8343-5f94635bf259","cardNumber":"4111-11xx-xxxx-1111","cardLogoURL":"qb_visa_cc","cardHolderFirstName":"Test","cardHolderLastName":"Internaional"},{"addressSaved":false,"cardBrand":"MAESTRO","cardAccessType":"","cardName":"MAESTDebitCard","cardTypeLabel":"Debit Card","cvvLabel":"Enter CVV","cvvLength":"3","cardId":"734bafc9-8b75-4dd0-8cc5-e4a528a1159a","cardNumber":"50815-9xxxx-xxxxx-7777","cardLogoURL":"qb_maest_cc","cardHolderFirstName":"Test","cardHolderLastName":""},{"addressSaved":false,"cardBrand":"MAESTRO","cardAccessType":"DOM","cardName":"MAESTDebitCard","cardTypeLabel":"Debit Card","cvvLabel":"Enter CVV","cvvLength":"3","cardId":"71a4fe93-ce37-4b56-9071-786b7248f7b2","cardNumber":"50815-9xxxx-xxxxx-1111","cardLogoURL":"qb_maest_cc","cardHolderFirstName":"Test","cardHolderLastName":""},{"addressSaved":false,"cardBrand":"AMEX","cardAccessType":"","cardName":"Sri AMex Card","cardTypeLabel":"Credit Card","cvvLabel":"Enter CVV","cvvLength":"4","cardId":"94cbb35f-4e37-4907-b3e1-2a7f57909c1f","cardNumber":"3714-49xxxx-x8431","cardLogoURL":"qb_amex_cc","cardHolderFirstName":"Sri","cardHolderLastName":""}],"saveCard":"ENABLED","authenticationRequired":false,"authenticationFailed":false,"priority":null,"status":"ENABLED","messages":null,"code":"qb","displayText":"Quick Book"}';

			// If QB JSON available, generate QB HTML
			try{
			if(qbJson != undefined && qbJson != null && qbJson != '' && qbJson != "null") {
				YatraQBPaymentUI.processQuickBookResponse(qbJson);
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
				var qbJsonURL = YatraQBPaymentUI.qbJsonURL;
				
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
						YatraQBPaymentUI.processQuickBookResponse(response);
					}, 
					error: function (request, status, error) {
						alert("Error while getting quickbook cards.");
					}
				});
			}
		},
		
		processQuickBookResponse : function(qbJson) {
			
			// Create HTML from JSON received in response.
			var qbDivObj = YatraQBPaymentUI.getQBDiv(qbJson);
			
			
			if(typeof(qbDivObj) != "undefined" && qbDivObj != null && showQB) {
				
					if(showQBCards && typeof(qbDivObj.qbCards) != "undefined" && qbDivObj.qbCards != null) {
						showAuthDiv = false;
						
						$('#tab_qb > #qb_section').html(qbDivObj.qbCards);
						$("#tab_qb").enhanceWithin();
						$('#tab_qb').removeClass("hide");
						
						// check the radio button for first QBCard.
						var firstQBRadio = $('#tab_qb input:radio[class="qb_radio"]:nth(0)');
						YatraQBPaymentUI.selectQBCard(firstQBRadio);
						YatraQBPaymentUI.enableQBCVVValidation(firstQBRadio);
						
						// Set QB selected to true.
						$('#qb_selected').val('true');
						
						// collapse the default payment option which is selected
						YatraQBPaymentUI.uncheckAndCollapsePaymentOption($("#cpmt_tabmenu li.showcardInfo"));
						
						$("#cc > div > label > span.label-align").html("New Credit Card");
						$("#dc > div > label > span.label-align").html("New Debit Card");
						
						// register events
						YatraQBPaymentUI.registerDeleteCardEvent();
						
						
					}
					else if(typeof(qbDivObj.authDiv) != "undefined" && typeof(qbDivObj.authDiv) != "undefined"){
						showAuthDiv = true;
						
						$('#tab_qb > #qb_section').html(qbDivObj.authDiv);
						
						// Set QB selected to true.
						$('#qb_selected').val('true');
						
						//set user email
						YatraQBPaymentUI.setUserEmailForReAuth(); 
						
						// collapse the default payment option which is selected
						YatraQBPaymentUI.uncheckAndCollapsePaymentOption($("#cpmt_tabmenu li.showcardInfo"));
						
						$("#tab_qb").enhanceWithin();
						$('#tab_qb').removeClass("hide");
						
						
						// If authentication has failed, show auth fail message.
						if(qbAuthFailed) {
							YatraQBPaymentUI.showQBAuthFailMessage();
						} 
						
						// register events
						YatraQBPaymentUI.registerQBAuthEvents();
						
						$("#cc > div > label > span.label-align").html("New Credit Card");
						$("#dc > div > label > span.label-align").html("New Debit Card");
						
					}
			} 
			else {
				$("#cc > div > label > span.label-align").html("Credit Card");
				$("#dc > div > label > span.label-align").html("Debit Card");
			}
			
		//	YatraPaymentUI.notifyPayopToLOB();
			
			if(showSaveOption) {
				//Show Save Card option for Credit Card 
				$('#cc_SaveOptionDiv').removeClass("hide");
				$("#cc_saveQuickBookCard_id").val("true");
				
				//Show Save Card option for Debit Card 
				$('#dc_SaveOptionDiv').removeClass("hide");
				$("#dc_saveQuickBookCard_id").val("true");
				
				//Show Save Card option for emi
				/*$('#emi_SaveOptionDiv').removeClass("hide");
				$("#emi_saveQuickBookCard_id").val("true");*/
				
				$('#tab_qb #qb_saveQuickBookCard').attr('value','true');
			}
		
			YatraQBPaymentUI.registerQuickbookEvents();
			
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
			$('#tab_qb > #qb_section > ul').html("");
			$('#tab_qb').addClass("hide");
			
			// Hide save CC check box.
			$('#cc_SaveOptionDiv').addClass("hide");
			$("#cc_saveQuickBookCard_id").val("false");

			// Hide save DC check box.
			$('#dc_SaveOptionDiv').addClass("hide");
			$("#dc_saveQuickBookCard_id").val("false");
			
			if($('#qb_selected').val() == 'true'){
				// show and select credit card div if quickbook was selected before
				$("li#cc").addClass("showcardInfo");
			}
			
			// change the labels
			$("#cc > div > label > span.label-align").html("Credit Card");
			$("#dc > div > label > span.label-align").html("Debit Card");
			
			// Set QB selected to false.
			$('#qb_selected').val('false');
			$("#qb_card_id").attr('value','');
			$("#qb_saveQuickBookCard").attr('value','false');
			$("#qb_isCardInternational").attr('value','false');
			
			//YatraPaymentUI.notifyPayopToLOB();				
		},
		
		getQBDiv : function(qbJson) {
			if(qbJson != null && typeof(qbJson) != "undefined" && qbJson != "" && qbJson != "null") {
				var qbJsonObject = $.parseJSON(qbJson);
				if(qbJsonObject != null && typeof(qbJsonObject.status) != "undefined") {
					var responseObj = new Object();
					
					// If saveCard = 'ENABLED', show save card check box.
					if(qbJsonObject.saveCard == 'ENABLED') {
						showSaveOption = true;
					}
					
					if(qbJsonObject.status == 'ENABLED') {
						showQB = true; // Set showQB to true if QB is enabled.

						// Check if authentication required / failed. If yes, show authentication div.
						if(qbJsonObject.authenticationFailed || qbJsonObject.authenticationRequired) {
							responseObj.authDiv = YatraQBPaymentUI.getQBAuthenticationDiv(qbJsonObject);
							if(qbJsonObject.authenticationFailed) {
								qbAuthFailed = true;
							}
						}
						// Get Quick Book Cards div.
						else {
							showQBCards = true;
							responseObj.qbCards = YatraQBPaymentUI.getQBCardsDiv(qbJsonObject);
						}
						
						return responseObj;
					}
				}
			}
		}, 
		
		getQBCardsDiv : function(qbJsonObject) {// Get QB Cards array from JSON Object.
			var qbCardsArray = qbJsonObject.quickBookCards;
			var qbCardslis = '<p><span class="quick-text">Your Saved Cards Through</span> <i class="payment-icon qb-logo"></i></p><ul><div id="qb_divErrMsgBlock" class="pmt_errMsgBlock" style="display:none;"></div>';
			var card_logo_list={"MASTER":"master-card","VISA":"visa-quick","MAESTRO":"maestro_cc","AMEX":"qb_amex_cc","DINERS":"diners_club_international_cc"}
			
			for (var i = 0; i < qbCardsArray.length; i++) {
				var qbCard = qbCardsArray[i];
				var liDisplayStyle="";
				if(i!=0){
					liDisplayStyle = 'style="display:none"';
				}
				var isCardInternational = false;
				if(qbCard.cardAccessType == 'INT'){
					isCardInternational = true;
				}
				
				qbCardslis += '<li index="'+i+'" class="qbli_'+i+'" '+liDisplayStyle+'>'
					+'<label for="qb_card_'+i+'" class="ui-btn ui-corner-all ui-btn-inherit ui-btn-icon-left ui-radio-off">'
					+'<div class="cardInfo"><span class="cardName"><i class="payment-icon '+card_logo_list[qbCard.cardBrand]+'"></i><span>'+qbCard.cardName+'</span></span>'
					+'<span class="cardNumber">'+qbCard.cardNumber+'</span></div></label>'
					+'<input type="radio" class="qb_radio" name="radio-common" id="qb_card_'+i+'" value="quickBook" cardId="'+qbCard.cardId+'" cardBrand="'+qbCard.cardBrand+'" cardType="'+qbCard.cardTypeLabel+'" isCardInternational="'+isCardInternational+'" cardAccessType="'+qbCard.cardAccessType+'" isAddressSaved="'+qbCard.addressSaved+'" cardNumber="'+qbCard.cardNumber+'">'
					+'<div class="cvvNo"><input type="password" id="qb_cvv_'+i+'" class="qb_CVV_input" disabled="true" placeholder="CVV" maxlength="'+qbCard.cvvLength+'"></div></div>'
					+'<span class="cross-icon">X</span><div class="detail-holder international-cards">';
				
				if(qbCard.cardBrand == 'AMEX' && !qbCard.addressSaved){
					qbCardslis += YatraQBPaymentUI.getAMEXAddressDiv(i);
				}
				
				if(qbCard.cardAccessType != "DOM" && !qbCard.addressSaved){
					qbCardslis += YatraQBPaymentUI.getBillingAdddressDiv();
				}
				
				qbCardslis += '</div></li>';
			}
			
			// attach view more anchor tag
			if(qbCardsArray.length>1){
				qbCardslis +='<a id="view_qbs" class="cpmt_flR" href="javascript:void(0);" attribute="view_more" >View More &gt;&gt;</a>';
			}
			
			qbCardslis +='</ul>'
			
			return qbCardslis;
		},
		
		getQBAuthenticationDiv : function(qbJsonObject) {
			var authFailMsg = '<div class="failed-msg">'+YatraQBPaymentUI.qbAuthRequiredMessage+' <i class="payment-icon qb-logo"></i></div>';
			var authPassFail = '<div class="re-login" id="qb-auth-fail-msg" style="display:none">'+YatraQBPaymentUI.qbAuthFailedMessage+'</div>';
			
			var qbAuthDiv = '<div class="login-falied" id="qb-auth-msg-div" >'+authFailMsg+authPassFail+'</div><section class="login-cont">';
			
			// Create div when user needs to authenticate using Yatra login
			if(qbJsonObject.authMode == 'YATRA') {
				
				qbAuthDiv += '<section class="login-block ui-content" id="qb-auth-div">'
				+'<div class="ui-input block wfull">'
				+'<label class="field-label">Email id</label>'
				+'<input type="text" id="qb_auth_email" placeholder="" value="" disabled="disabled">'
				+'</div><div class="ui-input block wfull">'
				+'<label class="field-label">Password</label>'
				+'<input type="password" id="qb_auth_password" placeholder="" value="" id="jangid" class="password confirm-pwd">'
				+'<span data-rol="show-password" data-rel=".confirm-pwd" class="show-txt">Show<i class="ui-icon ui-icon-eye" data-inline="true"></i></span>'
				+'</div><section class="forgot-pwd"><a href='+YatraQBPaymentUI.forgotPasswordURL+' class="ui-link">Forgot Password?</a></section>'
				+'<div class="button-outer">	<input id="qb_auth_button" type="button" data-role="button" data-theme="orange large" value="Sign In"></div>'
			}
			
			// Create div when user needs to authenticate using FB login
			if(qbJsonObject.authMode == 'FACEBOOK') {
				qbAuthDiv += '<div class="button-outer" id="qb_auth_fb"><span class="ui-icon ui-icon-arrow-fb"></span><input type="submit" data-icon="ui-icon ui-icon-arrow-fb" data-role="button" data-theme="fblue large" value="login with Facebook" data-iconpos="left"></div>';
			} 
			
			qbAuthDiv += '</section>';
			return qbAuthDiv;	
		},
		
		getAMEXAddressDiv : function(i){
			var amexQuestnDiv = '<div id="qb_AddressShowHide" style="display:none;"><h3>Where was your Amex card issued?</h3><ul>'
				+'<li><input type="radio" name="qb_address_check_rdo" id="qb_india_'+i+'" class="custom" value="Domestic"/><label for="qb_india_'+i+'">India</label></li>'
				+'<li><input type="radio" name="qb_address_check_rdo" id="qb_outside-india_'+i+'" class="custom"  value="International"/><label for="qb_outside-india_'+i+'">Outside India</label></li>'
				+'</ul></div>';
			return amexQuestnDiv;
	    },

	    getBillingAdddressDiv : function(){
	    	var billingAddressDiv = '<section id="qb_pmt_addressId" style="display:none;">'
	    	+'<div class="wfull card-number"><label class="field-label">Card Holder\'s Billing Address</label><input  type="text" id="qb_ba1" name="ba1" class="cardVal" data-validation="required" data-msginfo="* required" placeholder="billing address"/>'
			+'</div><div class="extra-indo-amex" ><ul>'
			+'<li><label class="field-label">city</label><input  type="text" id="qb_bcity" name="bacy" placeholder="city" data-validation="required|regType[onlyLetter]" data-msginfo="* required|* alphabets only" class="cardVal"/></li>'
			+'<li><label class="field-label">state</label><input type="text" id="qb_bstate" name="bast" placeholder="state" data-validation="required|regType[onlyLetter]" data-msginfo="* required|* alphabets only" class="cardVal"/></li></ul><ul>'
			+'<li><label class="field-label">Pin/Zip code</label><input type="text" id="qb_bpin" name="bapi" placeholder="Pin/Zip code" data-validation="required" data-msginfo="* required" maxlength="10" class="cardVal"/></li>'
			+'<li><label class="field-label">Country</label><div class="drop-menu ml0"><input type="hidden" id="qb_bcountry_id" name="bacu" value=""/><select id="qb_bcountry" paramId="qb_bcountry_id" data-validation="required" data-msginfo="* select an option">'
			+$('#cc_bcountry').html()+'</select></div></li></ul><ul>'
	    	+'<li><label class="field-label">ISD</label><input  type="text" id="qb_baisd" name="baisd" placeholder="ISD" data-validation="required|regType[number]|lenCheck[3]" data-msginfo="* required|* numbers only|Invalid ISD length" maxlength="3" class="cardVal"/></li>'
	    	+'<li><label class="field-label">Mobile Number</label><input type="text" id="qb_bamob" name="bamob" placeholder="Mobile Number" data-validation="required|regType[number]|lenCheck[10]" data-msginfo="* required|* Please enter a valid number|* Please enter a valid number" maxlength="10" class="cardVal"/></li></ul></div></section>';
			
	    	/*var billingAddressHtml = $('#cc_pmt_addressId').html();
			billingAddressDiv += billingAddressHtml.replace(/cc/g,'qb');
			billingAddressDiv += '</section>';*/
	    	return billingAddressDiv;
	    },
		
		validateQBAuth : function() {
			if(YatraEWalletPaymentUI.isCompleteEwalletPayment()) {
				return true;
			}
			var paymentOption = YatraPayment.getPayOp();
			if(showAuthDiv && (paymentOption == "qb")) {
				YatraQBPaymentUI.showQBAuthFailMessage();
				YatraQBPaymentUI.showQBPassReqdMessage();
				return false;
			}
			return true;
		},
		
		showQBAuthFailMessage : function() {
			$("#qb-auth-fail-msg").show();
		},
		
		hideQBAuthFailMessage : function() {
			$("#qb-auth-div").hide();
			$("#qb-auth-div-msg").hide();
		},
		
		showQBPassReqdMessage : function() {
			Toast.show('* required', 'qb_auth_password');
			$("#qb_auth_password").focus();
		},

		setUserEmailForReAuth: function(){
			$("#qb_auth_email").val(getPaymentJSON().userEmail);
		},
		
		resetViewMore : function(){
			setTimeout( function(){
				$('#tab_qb #qb_section > ul >  li').hide();
				$('#tab_qb #qb_section > ul >  li:first').show();	
			}, 100);
			
			$('#view_qbs').attr("attribute","view_more");
			$('#view_qbs').text("View More >>");
			
		},
		
		deleteQBCard : function(cardId) {
			var paymentJSON = getPaymentJSON();
			var merchant = paymentJSON.merchant;
			var product = paymentJSON.product;
			var qbDeleteCardURL = YatraQBPaymentUI.qbDeleteCardURL;
			
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
		
		selectQBCard : function(selected_qb){
			$('#qb_selected').val('true');
			$(selected_qb).attr("checked","checked");
			$(selected_qb).prop("checked",true);
			var parent_qb_li = $(selected_qb).parents('li');
			parent_qb_li.children('.ui-radio').children("label.ui-btn").removeClass("ui-radio-off").addClass("ui-radio-on");
			parent_qb_li.children('.cvvNo').find('input[type=password]').prop("disabled", false).parent().removeClass("ui-state-disabled");
			var selected_radio = parent_qb_li.children('.ui-radio').children('.qb_radio');
			$('#tab_qb #qb_card_id').attr('value',selected_radio.attr('cardid'));
			$('#tab_qb #qb_ctype').attr('value',selected_radio.attr('cardtype'));
			$('#tab_qb #qb_isCardInternational').attr('value',selected_radio.attr('isCardInternational'));
			YatraPaymentUI.handleInternationalQBCard(selected_qb);			
			$("#other_payop option:eq(0)").prop("selected", true);
			$('#other_payop').selectmenu();
			$("#other_payop").selectmenu('refresh', true);
			$('.other_option_div').hide();
			YatraPaymentUI.clearValidationError();
			YatraPaymentUI.addDropDownValidation();
		},
		
		deselectQBCard : function(selected_qb) {
			var parent_qb_li = $(selected_qb).parents('li');
			parent_qb_li.children('.ui-radio').children('label.ui-btn').removeClass('ui-radio-on').addClass('ui-radio-off');
			parent_qb_li.children('.cvvNo').find('input[type=password]').prop("disabled", true).prop("value","").parent().addClass("ui-state-disabled");
			YatraPaymentUI.clearAddressDiv(parent_qb_li);
			YatraPaymentUI.addDropDownValidation();
		},
		
		uncheckAndCollapsePaymentOption : function(payemntLi){
			$(payemntLi).removeClass("showcardInfo");
			$(payemntLi).find('input:radio[name="radio-common"]').prop('checked', false);
			$(payemntLi).find('.ui-radio > label.ui-btn').removeClass('ui-radio-on').addClass('ui-radio-off');
		},
		
		disableQBCVVValidation : function(qbElem) {
			var qbCVVElem = $(qbElem).parents('li').children('.cvvNo').find('input[type=password]');
			$(qbCVVElem).val('');
			$(qbCVVElem).prop("disabled", true);
			$(qbCVVElem).attr('data-validation', '');
			$(qbCVVElem).attr('data-msginfo', '');
		},

		enableQBCVVValidation : function(qbCardRadioElem) {
			var cardType = $(qbCardRadioElem).attr("cardType");
			var cardBrand = $(qbCardRadioElem).attr("cardBrand");
			var qbCardCVVElem = $(qbCardRadioElem).parents('li').children('.cvvNo').find('input[type=password]');
			var length = qbCardCVVElem.attr('maxlength');
			
			if (cardType == 'Debit Card' && cardBrand == 'MAESTRO') {
				$(qbCardCVVElem).attr('data-validation', 'checkCvv');
				$(qbCardCVVElem).attr('data-msginfo', 'Enter a valid cvv');
			} else {
				$(qbCardCVVElem).attr('data-validation', 'required|regType[number]|lenCheck[' + length + ']');
				$(qbCardCVVElem).attr('data-msginfo', '* required|* number only|* invalid cvv length');
			}
		},
		
		getQBCardNumber : function() {
			return $('#tab_qb input:radio[class="qb_radio"]:checked').attr('cardnumber').replace(/-/g, "");
		},
		
		getQBCardType : function() {
			return $('#tab_qb input:radio[class="qb_radio"]:checked').attr('cardtype');
		},
		
		registerQuickbookEvents :function(){
			
			$("#cc_saveQBCard").on("click",function() {
				if ($("#cc_saveQBCard").is(":checked"))
					$("#cc_saveQuickBookCard_id").val("true");
				else
					$("#cc_saveQuickBookCard_id").val("false");

			});

			$("#dc_saveQBCard").on("click",function() {
				if ($("#dc_saveQBCard").is(":checked"))
					$("#dc_saveQuickBookCard_id").val("true");
				else
					$("#dc_saveQuickBookCard_id").val("false");
			});
			
			/*$("#emi_saveQBCard").on("click",function() {
				if ($("#emi_saveQBCard").is(":checked"))
					$("#emi_saveQuickBookCard_id").val("true");
				else
					$("#emi_saveQuickBookCard_id").val("false");
			});*/
			
			$('#view_qbs').on('click', function(){
				var this_attr = $(this).attr("attribute");
				if(this_attr == "view_more"){
					$(this).parents('ul').find('li:not(:visible)').slideDown();
					$(this).attr("attribute","view_less");
					$(this).text("View Less <<");
				}
				else if(this_attr == "view_less"){
					$(this).parents('ul').find('li:not(:first)').slideUp();
					$(this).attr("attribute","view_more");
					$(this).text("View More >>");
					
					// Enable radio button for first QB Card.
					var selectedTab = YatraPaymentUI.getPaymentOption();
					if(selectedTab == "qb"){
						var selected_qb = $('#tab_qb input:radio[class="qb_radio"]:checked');
						if(selected_qb){
							YatraQBPaymentUI.disableQBCVVValidation(selected_qb);
						}
						var firstQBRadio = $('#tab_qb input:radio[class="qb_radio"]:nth(0)');
						YatraQBPaymentUI.selectQBCard(firstQBRadio);
						YatraQBPaymentUI.enableQBCVVValidation(firstQBRadio);
					}
				}
			});
			
			$("#tab_qb input:radio[class='qb_radio']").bind( "change", function(event, ui) {
				var qb_list = $('#tab_qb input:radio[class="qb_radio"]');
				$(qb_list).each(function() {
					YatraQBPaymentUI.disableQBCVVValidation($(this));
					YatraQBPaymentUI.deselectQBCard($(this));
				});
				
				var this_radio = $(this);
				YatraQBPaymentUI.selectQBCard(this_radio);
				YatraQBPaymentUI.enableQBCVVValidation(this_radio);
				YatraQBPaymentUI.uncheckAndCollapsePaymentOption($('#cpmt_tabmenu > li.showcardInfo'));
				
				YatraPaymentUI.notifyPayopToLOB();
			});
			
			/*$('#tab_qb  #qb_section ul li .ui-radio').on('click',function(){
				
				var this_radio = $(this).children('input[name=radio-common]');
				
				if(this_radio){
				
					var qb_list = $('#tab_qb input:radio[class="qb_radio"]');
					$(qb_list).each(function() {
						YatraQBPaymentUI.disableQBCVVValidation($(this));
						YatraQBPaymentUI.deselectQBCard($(this));
					});
					
					var this_radio = $(this).children('input[name=radio-common]');
					YatraQBPaymentUI.selectQBCard(this_radio);
					YatraQBPaymentUI.enableQBCVVValidation(this_radio);
					YatraQBPaymentUI.uncheckAndCollapsePaymentOption($('#cpmt_tabmenu > li.showcardInfo'));
				}*/
			  /*if($(this).parents('#tab_qb') && $(this).parents('#tab_qb').length ==1){ // clicked on qb
				}
				else if($(this).parent().parent().attr('id') == 'cpmt_tabmenu'){ // clicked on other payment options
					var selectedTab = YatraPaymentUI.getPaymentOption();
					if(selectedTab == "qb"){
						var selected_qb = $('#tab_qb input:radio[class="qb_radio"]:checked');
						if(selected_qb){
							YatraQBPaymentUI.disableQBCVVValidation(selected_qb);
							YatraQBPaymentUI.deselectQBCard(selected_qb);
						}
					}
				}*/
				
		//	});
			
			$('.qb_CVV_input').on('keyup', function() {
				YatraPaymentUI.disableSpace($(this));
				YatraPaymentUI.checkNaN($(this));
				$('#qb_cvv').val($(this).val());
			});
			
			$('input:radio[name="qb_address_check_rdo"]').on('change', function() {

				YatraPaymentUI.resetBillingDiv('qb');
				var amexCardType = $("input[name='qb_address_check_rdo']:checked").val();
				var addressDiv = $(this).parents('#qb_AddressShowHide').parent().find('#qb_pmt_addressId');
				var isCardInternational_Id = "#qb_isCardInternational_id";
				var saveQBCardAddress = "#qb_saveQBCardAddress_id";
				var payOp = 'qb';

				if (amexCardType == 'Domestic') {
					YatraPaymentUI.handleDomesticQBCard(addressDiv, isCardInternational_Id, payOp, saveQBCardAddress);
				} 
				else if (amexCardType == 'International') {
					// check for D-5 travelDate
					var travelCheck = YatraPaymentUI.checkForTravelDate(payOp, null);

					if (travelCheck == "true") {
						YatraPaymentUI.showQuickBookBillingDiv(addressDiv, isCardInternational_Id, payOp, saveQBCardAddress);
					} else {
						YatraPaymentUI.hideQuickBookBillingDiv(addressDiv, isCardInternational_Id, saveQBCardAddress);
					}
				}
			});
		},

		registerDeleteCardEvent : function() {
			
			$("#tab_qb ul li .cross-icon").on('click', function() {
				var parent_li = $(this).parents("li");
				var thisIndex = parent_li.attr("index");
				var cardId = parent_li.find(".qb_radio").attr("cardid");
				var thisLiClass = parent_li.attr("class");
				thisLiClass = thisLiClass.substring(0, thisLiClass.indexOf("_"));
				var noOfCards = parent_li.parents("ul").children("li").length; // Get no of cards.
				
				if(noOfCards > 1) {
					parent_li.remove();// Remove the current card.
						
					// Show the next card.
					$('#tab_qb li.' + thisLiClass + '_' + (parseInt(thisIndex) + 1)).show();
					
					// Logic to enable radio button of firstQB Card if the selected qb was deleted
					// Also handle when user deletes QB card while he has selected to pay using new card.
					var payOp = YatraPayment.getPayOp();
					if(payOp == "" && $('#tab_qb input:radio[class="qb_radio"]:checked') &&  $('#tab_qb input:radio[class="qb_radio"]:checked').length<1) {
						var firstQBRadio = $('#tab_qb input:radio[class="qb_radio"]:nth(0)');
						YatraQBPaymentUI.selectQBCard(firstQBRadio);
						YatraQBPaymentUI.enableQBCVVValidation(firstQBRadio);
					}
					
					// remove view more link if there is only one card left
					if(noOfCards == 2) {
						$("#tab_qb #view_qbs").remove();
					}
				} 
				else {
					// Remove the current card.
					$(this).parents("li").remove();
					
					// Hide QB 
					$("#tab_qb").addClass("hide");
					
					// Set QB selected to false.
					$('#qb_selected').val('false');
					
					// show credit card div
					/*$("li#cc").addClass("showcardInfo");*/
					
					
					YatraPaymentUI.enableFirstPaymentOption();
					
					// The payment option now is CC. Notify it to LOB.
					YatraPaymentUI.notifyPayopToLOB();
					
					$("#cc > div > label > span.label-align").html("Credit Card");
					$("#dc > div > label > span.label-align").html("Debit Card");
					
				}
				
				// server call to delete the card
				YatraQBPaymentUI.deleteQBCard(cardId);
				YatraPaymentUI.addDropDownValidation();
			})
		},
		
		
		registerQBAuthEvents : function() {
			
			$("#qb_auth_button").on('click', function() {
				var authURL = YatraQBPaymentUI.qbAuthURLForYatraUser;
				var paymentJSON = getPaymentJSON();
				var merchant = paymentJSON.merchant;
				var product = paymentJSON.product;
						
				var password = $("#qb_auth_password").val();
				var email = paymentJSON.userEmail
				
				if(password == '' || password == undefined || password == null) {
					YatraQBPaymentUI.showQBPassReqdMessage();
					return;
				}
				
				YatraPaymentUI.notifyPayopToLOB();
				
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
						YatraQBPaymentUI.processQuickBookResponse(response);
					}, 
					
					error: function (request, status, error) {
						alert("Error occurred while authenticating.. Please try again or use some other payment option..")
					}
				});	
			});
			
			$("#qb_auth_fb").on('click', function() {
				YatraPaymentUI.disableNewCreditCard();
				YatraPaymentUI.disableNewDebitCard();
				YatraFBPaymentUI.loginByFacebook();
			});
		
		}
}
