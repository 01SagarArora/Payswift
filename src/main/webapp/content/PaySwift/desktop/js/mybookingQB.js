var cardsSaved = false;
var noOfCards = 0;
var internationalCardBrandsArr = new Array("VISA", "MASTER");
var isUserAuthenticated = true; // login has not expired (older than 1 hr)
var currentCardId;
var currentCardUL;
var alreadyCustomized = false;

var MyBookingQB = {
		
		initialize : function() {
			var ssoToken = MyBookingQB.getSSOToken();
			if(MyBookingQB.isValid(ssoToken)) {
				MyBookingQB.isPasswordCompromised();
				MyBookingQB.registerEvents();
			} else {
				MyBookingQB.showNotLoggedInDiv();
			}
		},
		
		isPasswordCompromised : function() {
			var passCompromisedURL = MyBookingQB.getPassCompromisedURL();
			
			$.ajax({
				
				url: passCompromisedURL,
				timeout: (5 * 1000),
				type: 'POST',
				cache: false,

				success : function(response) {
					var responseJSON = MyBookingQB.convertToJsonObject(response);
					
					// Password is NOT compromised when status (pass compromised status) is FAILURE and there is no error code.
					if(MyBookingQB.isValid(responseJSON) && responseJSON.status == "FAILURE" && MyBookingQB.isInvalid(responseJSON.error_code)) {
						MyBookingQB.getSavedCards();
			        	MyBookingQB.showQBHomeDiv();
			        	
			        }  else {
			        	MyBookingQB.showResetPassDiv();
			        }
				}, 
				
				error: function (request, status, error) {
					alert("Error while determining if user is eligible for Quick Book.");
		        	MyBookingQB.showResetPassDiv();
				}
			});
		},
		
		getPassCompromisedURL : function(){
			return "/PaySwift/is-pass-compromised.htm";
		},
		
		registerEvents : function() {
			
			$('.show-qb-home').on('click', function() {
				MyBookingQB.showQBHomeDiv();
			});
			
			$('.js-card-type').on('click', 'button', function(e) {
				e.preventDefault();
				$(this).parent().find('.btn-selected').removeClass('btn-selected');
				$(this).addClass('btn-selected');
				MyBookingQB.resetAddCardDiv(false);
			});
			
			$('input:radio[name="address_check_rdo"]').on('change', function() {
				var cardIssueType = $(this).val();
				if(cardIssueType == "dom") {
					MyBookingQB.hideBillingAddDiv();
				} else {
					MyBookingQB.showBillingAddDiv();
				}
			});
			
			$('.delete_bts').on('click', 'button', function(e) {
				$("#delete-popup-id").modal('hide');
				var confirm = $(this).attr("confirm");
				if(confirm == "yes") {
					$(currentCardUL).remove();
					if(noOfCards == 1) {
						cardsSaved = false;
						MyBookingQB.getNoCardsDiv();
					} 
					MyBookingQB.deleteQBCard(currentCardId, "yatra", "mybookings");
					noOfCards--;
					MyBookingQB.setViewCardBtnLabel();
					currentCardId = '';
					currentCardUL = '';
				}
			});
			
			$("#qb_cno_id").on('keyup', function(e) {

				// Not allow spaces.
				MyBookingQB.disableSpace($(this));

				// Only digits allowed on cardNumber input.
				MyBookingQB.checkNaN($(this));

				// Get CardNumber value.
				var cardNo = "";
				cardNo = $(this).val();

				// Identify CardBrand.
				var cardBrand = MyBookingQB.getCardBrand(cardNo);
				MyBookingQB.setCardBrand(cardBrand);
				
				// Logic for fading.
				MyBookingQB.fadeCardLogo(cardBrand);
				
				// Show Address div for amex cards
				MyBookingQB.showAmexIssueDiv(cardBrand);
				
				// Show Expiry Msg for Maestro cards.
				MyBookingQB.showMaestExpiryMsg(cardBrand);
				
				// Update validations for the cardType and cardBrand.
				MyBookingQB.updateValidations(cardBrand);			

				// Logic for displaying Billing Address
				MyBookingQB.processInternationalCard(cardBrand, cardNo);

			});
			
			$("#qb_add_card_btn_id").on('click', function() {
				var isValid = PaymentValidation.validate('#qb_add_card_form_id');
				if(isValid) {
					$("#qb_add_card_form_id").submit();
				}
			});
			
			$("#qb_add_card_form_id").submit(function(e) {
				
			    var postData = $(this).serializeArray();
			    var addCardURL = $(this).attr("action");
			    
			    $.ajax({
			        
			    	url : addCardURL,
			        type : 'POST',
			        data : postData,
			        cache : false,
			        
			        success:function(response) {
			        	var responseJSON = MyBookingQB.convertToJsonObject(response);
				        if(MyBookingQB.isValid(responseJSON) && responseJSON.status == "SUCCESS") {
				           	MyBookingQB.getSavedCards();
				           	MyBookingQB.showSavedCardsDiv();
							if($('#quickBookErrSucc').length > 0){
								$('#quickBookErrSucc').remove();
							}
							$('#saved-cards>section>.row').append('<div class="ajaxErrorSuccess col-xs-12" id="quickBookErrSucc" style="background-color: #fff3bd;padding: 10px 15px;margin-bottom: 20px;color: #dc3201;"><i class="fa fa-tick-r"></i>Your card has been successfully added.</div>')
							window.scrollTo($('#quickBookErrSucc').offset().left,$('#quickBookErrSucc').offset().top);
        	
				        } else if(MyBookingQB.isValid(responseJSON) && responseJSON.status == "FAILURE" && MyBookingQB.isValid(responseJSON.error_code) && responseJSON.error_code == "AUTH_REQUIRED") {
				        	MyBookingQB.showAuthPage();
				        	
				        } else if(MyBookingQB.isValid(responseJSON) && responseJSON.status == "FAILURE" && MyBookingQB.isValid(responseJSON.error_code) && responseJSON.error_code == "108") {
				        	MyBookingQB.showDuplicateCardErrorMsg();
				        	
				        } else {
				        	MyBookingQB.showAddCardErrorMsg();
				        }
			        },
			        error: function(request, status, error) {
			            alert("Error occurred while adding card.");
			            MyBookingQB.showAddCardErrorMsg();
			        }
			    });
			    e.preventDefault(); // to stop default action.
			});
			
			$("#qb_reset_pass_btn_id").on('click', function() {
				var isValid = PaymentValidation.validate('#qb_reset_pass_form_id');
				if(isValid) {
					$("#qb_reset_pass_form_id").submit();
				} 
			});
			
			$("#qb_reset_pass_form_id").submit(function(e) {
				
			    var postData = $(this).serializeArray();
			    var resetPassURL = $(this).attr("action");
			    
			    $.ajax({
			        
			    	url : resetPassURL,
			        type : 'POST',
			        data : postData,
			        cache : false,
			        
			        success:function(response) {
			        	var responseJSON = MyBookingQB.convertToJsonObject(response);
				        if(MyBookingQB.isValid(responseJSON) && responseJSON.status == "SUCCESS") {
				        	MyBookingQB.getNoCardsDiv();
				        	MyBookingQB.showQBHomeDiv();
				        	
				        } else if(MyBookingQB.isValid(responseJSON) && responseJSON.status == "FAILURE" && MyBookingQB.isValid(responseJSON.error_code) && responseJSON.error_code == "AUTH_REQUIRED") {
				        	MyBookingQB.showAuthPage();
				        	
				        } else {
				        	MyBookingQB.showResetPassErrorMsg();
				        }
			        },
			        error: function(request, status, error) {
			            alert("Error occurred while resetting password.");
			            MyBookingQB.showResetPassErrorMsg();
			        }
			    });
			    e.preventDefault(); // to stop default action.
			});
		},
		
		getSavedCards : function() {
			var merchant = 'yatra';
			var product = 'mybookings';
			var saveCardURL = MyBookingQB.getSavedCardsURL();
			
			$.ajax({
				
				url: saveCardURL,
				timeout: (5 * 1000),
				type: 'POST',
				data: { 
				        'merchant': merchant, 
				        'product': product
				},
				cache: false,

				success : function(response) {
					MyBookingQB.processGetCardsResponse(response);
				}, 
				
				error: function (request, status, error) {
					alert("Error occurred while getting saved cards.");
					MyBookingQB.getErrorCardsDiv();
				}
			});
		},
		
		getSavedCardsURL : function(){
			return "/PaySwift/qb.htm";
		},
		
		processGetCardsResponse : function(qbJson) {
			
			// Initialize no of cards to 0.
			noOfCards = 0;	
			
			// set isUserAuthenticated to true.
			isUserAuthenticated = true;
			
			// Convert JSON to JSONObject.
			var jsonObject = MyBookingQB.convertToJsonObject(qbJson);
			
			// If JSON Object is null, show error msg.
			if(MyBookingQB.isInvalid(jsonObject)) {
				MyBookingQB.getErrorCardsDiv();
				return;
			}
			
			if(jsonObject.authenticationRequired) {
				isUserAuthenticated = false;
				return;
	        }
			
			// Get saved cards and show them.
			MyBookingQB.getSavedCardsDiv(jsonObject);
			if(cardsSaved) {
				MyBookingQB.registerDeleteCardEvent();
				return;
			}
			
			// If there are no cards saved, show no cards div.
			MyBookingQB.getNoCardsDiv();
		},
		
		getSavedCardsDiv : function(qbJsonObject) {
			var savedCardsArray = qbJsonObject.quickBookCards;
			if(savedCardsArray != null && savedCardsArray != undefined && savedCardsArray.length > 0) {
				cardsSaved = true;
				var savedCards = '<section class="col-md-7"><div class="row">';
				savedCards += '<div class="pull-left col-md-9 hidden-xs"><h3 class="mg-top mg-bottom">QuickBook</h3></div>';
				savedCards += '<div class="pull-right col-md-3 col-xs-12"><button class="yt-btn btn-block" id="savedAddCardBtn" type="button">';
				savedCards += '+  Add a card</button></div></div><div class="clearfix"></div>';
				savedCards += '<div class="panel panel-default clearfix my-travellers text-md">';
				savedCards += '<div class="panel-heading"><h3 class="panel-title">Your Saved Cards</h3></div><div class="panel-body">';
				
				for (var i = 0; i < savedCardsArray.length; i++) {
					savedCards += '<ul class="yt-list row"><li class="col-md-10 pd-left pd-right col-xs-9">';
					savedCards += '<div class="col-md-2 pd-left pd-right col-xs-3">';
					savedCards += '<i class="' + MyBookingQB.getCardLogo(savedCardsArray[i].cardBrand) + '"></i></div>';
					savedCards += '<div class="col-md-10 pd-right col-xs-9 card-id" card-id="'+savedCardsArray[i].cardId+'">';
					savedCards += '<strong>' + savedCardsArray[i].cardNumber.replace(/-/g, " ") + '</strong><br/>';
					savedCards += savedCardsArray[i].cardName + ', ' + savedCardsArray[i].cardHolderFirstName + ' ' + savedCardsArray[i].cardHolderLastName;
					savedCards += '</div></li><li class="col-md-1 pd-right col-xs-1 text-right"><a href="#" title="Delete">';
					savedCards += '<i class="fa fa-times-circle-o delete-card"></i></a></li></ul><hr class="row hr-dotted"/>';
					noOfCards++;
				}
				savedCards += '</div></div></section>';
				$('#saved-cards').html(savedCards);
				MyBookingQB.setViewCardBtnLabel();
				$('#savedAddCardBtn').on('click',function(){
					MyBookingQB.showAddCardDiv();
				});
			} 
			if(window.attachEvent && !window.addEventListener){
				$('.col-xs-12').removeClass('col-xs-12');
				$('.col-xs-5').removeClass('col-xs-5');
			}
		},
		
		getCardLogo : function(cardBrand) {
			switch(cardBrand) {
				case "VISA": return 'ico-visa';
				case "MASTER": return 'ico-mastercard';
				case "MAEST": return 'ico-maestro';
				case "MAESTRO": return 'ico-maestro';
				case "AMEX": return 'ico-americanexpress';
				case "DINNERS": return 'ico-dinnerclub';
				case "DINERS": return 'ico-dinnerclub';
				default: return 'ico-visa';
			}
		},
		
		getNoCardsDiv : function() {
			var savedCards = '<section class="col-md-7"><div class="row">';
			savedCards += '<div class="pull-left col-md-9 hidden-xs"><h3 class="mg-top mg-bottom">QuickBook</h3></div>';
			savedCards += '<div class="pull-right col-md-3 col-xs-12"><button class="yt-btn btn-block" onclick="MyBookingQB.showAddCardDiv()" type="button">';
			savedCards += '+  Add a card</button></div></div><div class="clearfix"></div>';
			savedCards += MyBookingQB.getNoCardsMsg();
			savedCards += '</section>';
			$('#saved-cards').html(savedCards);
			MyBookingQB.setViewCardBtnLabel();
		},
		
		getErrorCardsDiv : function() {
			var savedCards = '<section class="col-md-7"><div class="row">';
			savedCards += '<div class="pull-left col-md-9 hidden-xs"><h3 class="mg-top mg-bottom">QuickBook</h3></div>';
			savedCards += '<div class="pull-right col-md-3 col-xs-12"><button class="yt-btn btn-block" onclick="MyBookingQB.showAddCardDiv()" type="button">';
			savedCards += '+  Add a card</button></div></div><div class="clearfix"></div>';
			savedCards += MyBookingQB.getErrorCardsMsg();
			savedCards += '</section>';
			$('#saved-cards').html(savedCards);
			MyBookingQB.setViewCardBtnLabel();
		},
		
		getErrorCardsMsg : function() {
			return 'Error occurred while fetching your cards. Please try after some time.';
		},
		
		getNoCardsMsg : function() {
			return 'You do not have any cards saved.';
		},
		
		registerDeleteCardEvent : function() {
			$('.panel-body .delete-card').each(function(index) {
				$(this).on('click', function() {
					currentCardId = $(this).parents("ul").find("li .card-id").attr("card-id");
					currentCardUL = $(this).parents("ul");
					$("#delete-popup-id").modal('show');
				})
			});
		},
		
		deleteQBCard : function(cardId, merchant, product) {
			var qbDeleteCardURL = MyBookingQB.getQBDeleteCardURL();
			
			$.ajax({
				
				url: qbDeleteCardURL,
				type: 'POST',
				data: { 
				        'merchant': merchant, 
				        'product': product,
				        'cardid' : cardId,
				        'csrfToken':csrfToken
				},
				cache: false,

				success : function(response) {
					var responseJSON = MyBookingQB.convertToJsonObject(response);
					if(responseJSON.status == "SUCCESS"){
						if($('#quickBookErrSucc').length > 0){
							$('#quickBookErrSucc').remove();
						}
						$('#saved-cards>section>.row').append('<div class="ajaxErrorSuccess col-xs-12" id="quickBookErrSucc" style="background-color: #fff3bd;padding: 10px 15px;margin-bottom: 20px;color: #dc3201;"><i class="fa fa-tick-r"></i>Your card has been successfully removed.</div>')
						window.scrollTo($('#quickBookErrSucc').offset().left,$('#quickBookErrSucc').offset().top);
					}
			        if(MyBookingQB.isValid(responseJSON) && responseJSON.status == "FAILURE" && MyBookingQB.isValid(responseJSON.error_code) && responseJSON.error_code == "AUTH_REQUIRED") {
			        	MyBookingQB.showAuthPage();
			        } 
				}, 
				
				error: function (request, status, error) {
					alert("Error while deleting quickbook card.");
				}
			});
		},
		
		getQBDeleteCardURL : function () {
			return "/PaySwift/delete-qb-card.htm"
		},
		
		setCardType : function() {
			var cardType = $('.js-card-type .btn-selected').attr("card_type");
			if(MyBookingQB.isValid(cardType)) {
				$("#qb_ctype_id").val(cardType);
			} else {
				$("#qb_ctype_id").val("");
			}
		},
		
		getCardType : function() {
			return $("#qb_ctype_id").val();
		},
		
		setCardBrand : function(cardBrand) {
			if(MyBookingQB.isValid(cardBrand)) {
				$("#qb_cbrand_id").val(cardBrand);
			} else {
				$("#qb_cbrand_id").val("");
			}
		},
		
		getCardBrand : function(cardNumber) {
			var cno = (cardNumber + '').replace(/\s/g, ''); //remove space
			if ((/^(4)/).test(cno)) {
				return 'VISA'; 
			} else if ((/^(51|52|53|54|55|222)/).test(cno)) {
				return 'MASTER'; 
			} else if ((/^(34|37)/).test(cno)) {
				return 'AMEX'; 
			} else if ((/^(50|56|57|58|6)/).test(cno)) {
				return 'MAEST';
			} else if ((/^(300|301|302|303|304|305|36|38|3095)/).test(cno)) {
				return 'DINERS'; 
			}
			return null; //unknown type
		},
		
		getIsCardInternational : function() {
			return $("#qb_isCardInternational_id").val();
		},
		
		setIsCardInternational : function(isCardInt) {
			$("#qb_isCardInternational_id").val(isCardInt);
		},
		
		fadeCardLogo : function(cardBrand) {
			var cardBrand = $("#qb_cbrand_id").val();
			$.each($(".card_logos > i"), function() {
				var cardLogoId = $(this).attr("id");
				var cardLogo = cardLogoId.substring(0, cardLogoId.indexOf("_")).toUpperCase();
				if(MyBookingQB.isInvalid(cardBrand) || cardLogo == cardBrand) {
					$(this).removeClass("ico-o-5");
				} else {
					$(this).addClass("ico-o-5");
				}
			});
		},
		
		showMaestExpiryMsg : function(cardBrand) {
			var cardType = MyBookingQB.getCardType();
			if(MyBookingQB.isValid(cardBrand) && cardBrand == "MAEST" && cardType == "dc") {
				$("#maest_expiry_msg").show();
			} else {
				$("#maest_expiry_msg").hide();
			}
		},
		
		updateValidations : function(cardBrand) {
			
			if(MyBookingQB.isValid(cardBrand)) {
				
				// Get Card Type (cc/dc)
				var cardType = MyBookingQB.getCardType();
				
				// Card number.
				var validationRule = MyBookingQB.getCardNoValidationRule(cardType, cardBrand);
				var validationMsg = MyBookingQB.getCardNoValidationMsg(cardType, cardBrand);
				
				$('#qb_cno_id').attr('data-validation', validationRule);
				$('#qb_cno_id').attr('data-msginfo', validationMsg);
				
				// MAEST validations.
				if(cardType == 'dc' && cardBrand == 'MAEST') {

					// Expiry Month
					$('#qb_expm_id').attr('data-validation', '');
					$('#qb_expm_id').attr('data-msginfo', '');
					
					// Expiry Year
					$('#qb_expy_id').attr('data-validation', 'checkExpiryDate[qb]');
					$('#qb_expy_id').attr('data-msginfo', '* Invalid Date');
				} 
			} else {
				MyBookingQB.setDefaultValidations();
			}
		},
		
		getCardNoValidationRule : function(cardType, cardBrand) {
		
			// Validation Rule for CC
			var validationRule = 'required|validateLuhn|cardRegType[' + cardBrand + ']|supportedCard[' + cardBrand + ',' + cardType + ']';
			
			// Validation Rule for MAEST DC
			if(cardType == 'dc' && cardBrand == 'MAEST') {
				validationRule = 'required|cardRegType[' + cardBrand + ']|supportedCard[' + cardBrand + ',' + cardType + ']';
			} 
			return validationRule;
		},
		
		getCardNoValidationMsg : function(cardType, cardBrand) {
			
			// Validation Msg for CC
			var validationMsg = '* required|Enter valid card number|Enter valid card number|Enter valid credit card number';
			
			// Validation Msg for DC other than MAEST
			if(cardType == 'dc' && cardBrand != 'MAEST') {
				validationMsg = '* required|Enter valid card number|Enter valid card number|Enter valid debit card number';
			}
			
			// Validation Msg for MAEST DC
			if(cardType == 'dc' && cardBrand == 'MAEST') {
				validationMsg = '* required|Enter valid card number|Enter a debit card';
			}
			return validationMsg;
		},

		processInternationalCard : function(cardBrand, cardNo) {
		
			if(!MyBookingQB.isCardBrandInternational(cardBrand)) {
				MyBookingQB.hideBillingAddDiv();
				return;
			}
			
			// Check if card is international (only for VISA and MASTER). If international, show address div.
			if(MyBookingQB.isCardLengthValid(cardBrand, cardNo)) {
				
				var bin = parseInt(cardNo.substring(0, 6));
				var isCardIntURL = MyBookingQB.getIsCardInternationalURL();
			
				$.ajax({
					
					url: isCardIntURL,
					type: 'POST',
					data: { 
							'bin': bin 
					},
					cache: false,

					success : function(response) {
						var responseJSON = MyBookingQB.convertToJsonObject(response);
						if (MyBookingQB.isValid(responseJSON) && responseJSON.isCardInternational == "true") {
							MyBookingQB.showBillingAddDiv();
						} else {
							MyBookingQB.hideBillingAddDiv();
						}
					}, 
					
					error: function (request, status, error) {
						alert("Error while determining if card is international or not.");
					}
				});
			} else {
				MyBookingQB.hideBillingAddDiv();
			}
		},
		
		isCardBrandInternational : function(cardBrand) {
			if(MyBookingQB.isInvalid(cardBrand))
				return false;
			return MyBookingQB.ifArrayContains(internationalCardBrandsArr, cardBrand)
		},
		
		getIsCardInternationalURL : function() {
			return '/PaySwift/isCardInternational.htm';
		},
		
		isCardLengthValid : function(cardBrand, cardNo) {
			switch(cardBrand) {
				case "VISA": return (cardNo.length == 13 || cardNo.length == 16);
				case "MASTER": return (cardNo.length == 16);
				case "MAEST": return (cardNo.length == 16 || cardNo.length == 18 || cardNo.length == 19);
				case "AMEX": return (cardNo.length == 15);
				case "DINERS": return (cardNo.length == 14);
				default: return false;
			}
		},
			
		resetAddCardDiv : function(enableDefaultTab) {
			if(enableDefaultTab) {
				MyBookingQB.enableCCTab();				
			}
			
			// Hiding details / div.
			MyBookingQB.clearDetails();
			MyBookingQB.hideErrorMsgs();
			MyBookingQB.hideBillingAddDiv();
			MyBookingQB.hideAmexIssueDiv();
			MyBookingQB.resetDropDown();
			MyBookingQB.setCardBrand("");
			
			// Reseting depending upon cardType (cc/dc)
			MyBookingQB.setCardType();
			MyBookingQB.showCardLogos();
			MyBookingQB.setCardNoLabel();
			MyBookingQB.setDefaultValidations();
		},
		
		enableCCTab : function() {
			$("#qb_dc_tab_id").removeClass('btn-selected');
			$("#qb_cc_tab_id").addClass('btn-selected');
		},
		
		clearDetails : function() {
			$("#add_card_div_id input:text").each(function() {
				$(this).val("");
				IE_Placeholder.callBackPlaceDefaultText($(this));
			});
		},
		
		hideErrorMsgs : function() {
			$(".qb_error_msg").hide();
		},
		
		resetDropDown : function() {
			$('#qb_expm_id :nth(0)').attr("selected", "selected");
			$('#qb_bacu_id :nth(0)').attr("selected", "selected");
			MyBookingQB.populateExpYrDropDown();
		},
		
		populateExpYrDropDown : function() {
			$("#qb_expy_id").empty();
			$("#qb_expy_id").append($('<option></option>').val("").html("Year"));
			var currYear = new Date().getFullYear();
			for ( var i = 0; i <= 20; i++) {
				$("#qb_expy_id").append($('<option></option>').val(currYear + i).html(currYear + i));
			}
			var expM = $('#qb_expm_id');
			var expY = $('#qb_expy_id');
			var country = $('#qb_bacu_id');
			if(alreadyCustomized && $(window).width()>768){
				$('#qb_expm_id').parents('div.select').replaceWith(expM);				
				$('#qb_expy_id').parents('div.select').replaceWith(expY);
				$('#qb_bacu_id').parents('div.select').replaceWith(country);	
				addMyCustomController();
			}
			if(!alreadyCustomized)
			{
				addMyCustomController();
				alreadyCustomized = true;
			}
		},
		
		showCardLogos : function() {
			
			$.each($(".card_logos > i"), function() {
				$(this).show();
				$(this).removeClass("ico-o-5");
			});
			
			var cardType = MyBookingQB.getCardType();
			switch(cardType) {
				case "cc": $("#maest_logo_id").hide(); break;
				case "dc": $("#amex_logo_id").hide(); break;
			}
		},
		
		setCardNoLabel : function() {
			var cardType = MyBookingQB.getCardType();
			switch(cardType) {
				case "cc": $("#card_no_label_id").html("Credit Card Number"); break;
				case "dc": $("#card_no_label_id").html("Debit Card Number"); break;
			}
		},
		
		setDefaultValidations : function() {
			
			// Card Number	
			var cardType = MyBookingQB.getCardType();
			if(cardType == 'cc') {
				$('#qb_cno_id').attr('data-validation', 'required|validateLuhn|cardRegType[VISA]|supportedCard[VISA,cc]');
				$('#qb_cno_id').attr('data-msginfo', '* required|Enter valid card number|Enter valid card number|Enter valid credit card number');
				$("#qb_cno_id").attr("maxlength","16");
			} else {
				$('#qb_cno_id').attr('data-validation', 'required|validateLuhn|cardRegType[VISA]|supportedCard[VISA,dc]');
				$('#qb_cno_id').attr('data-msginfo', '* required|Enter valid card number|Enter valid card number|Enter valid debit card number');
				$("#qb_cno_id").attr("maxlength","19");
			}
			
			// Expiry Month
			$('#qb_expm_id').attr('data-validation', 'required');
			$('#qb_expm_id').attr('data-msginfo', '* required');
			
			// Expiry Year
			$('#qb_expy_id').attr('data-validation', 'required|checkExpiryDate[qb]');
			$('#qb_expy_id').attr('data-msginfo', '* required|Invalid Date');
			
		},
		
		showAddCardErrorMsg : function() {
			$("#add_card_error_msg").show();
			$('#add_card_error_msg').delay(5000).fadeOut();
		},
		
		showDuplicateCardErrorMsg : function() {
			$("#duplicate_card_error_msg").show();
			$('#duplicate_card_error_msg').delay(5000).fadeOut();
		},
		
		showResetPassErrorMsg : function() {
			$("#reset_pass_error_msg").show();
			$('#reset_pass_error_msg').delay(5000).fadeOut();
		},
		
		showAmexIssueDiv : function(cardBrand) {
			var cardType = MyBookingQB.getCardType();
			if(MyBookingQB.isValid(cardBrand) && cardBrand == 'AMEX' && cardType == 'cc') {
				$('input:radio[name="address_check_rdo"]:nth(0)').attr("checked", "checked");
				$("#amex_issue_div").show();
			} else {
				MyBookingQB.hideAmexIssueDiv();
			}
		},
		
		hideAmexIssueDiv : function() {
			$("#amex_issue_div").hide();
		},
		
		showAddCardDiv : function() {
			// Hide other divs.
			MyBookingQB.hideResetPassDiv();
			MyBookingQB.hideSavedCardsDiv();
			MyBookingQB.hideQBHomeDiv();
			MyBookingQB.hideNotLoggedInDiv();
			
			$('#my-booking-add-card').show();
			MyBookingQB.resetAddCardDiv(true);
			if($('#quickBookErrSucc').length > 0){
				$('#quickBookErrSucc').remove();
			}
		},
		
		hideAddCardDiv : function() {
			$('#my-booking-add-card').hide();
		},

		showSavedCardsDiv : function() {
			if(isUserAuthenticated) {
				// Hide other divs.
				MyBookingQB.hideResetPassDiv();
				MyBookingQB.hideQBHomeDiv();
				MyBookingQB.hideAddCardDiv();
				MyBookingQB.hideNotLoggedInDiv();
				
				$('#my-booking-saved-cards').show();
				if(noOfCards == 0) {
					MyBookingQB.getSavedCards();
				}
				if($('#quickBookErrSucc').length > 0){
					$('#quickBookErrSucc').remove();
				}
			} else {
				MyBookingQB.showAuthPage();
			}
		},
		
		hideSavedCardsDiv : function() {
			$('#my-booking-saved-cards').hide();
		},
		
		showQBHomeDiv : function() {
			// Hide other divs.
			MyBookingQB.hideResetPassDiv();
			MyBookingQB.hideAddCardDiv();
			MyBookingQB.hideSavedCardsDiv();
			MyBookingQB.hideNotLoggedInDiv();
			
			$('#my-booking-qb-home').show();
		},
		
		hideQBHomeDiv : function() {
			$('#my-booking-qb-home').hide();
		},
		
		showResetPassDiv : function() {
			// Hide other divs.
			MyBookingQB.hideQBHomeDiv();
			MyBookingQB.hideAddCardDiv();
			MyBookingQB.hideSavedCardsDiv();
			MyBookingQB.hideNotLoggedInDiv();
			
			$('#my-booking-reset-pass').show();
		},
		
		hideResetPassDiv : function() {
			$('#my-booking-reset-pass').hide();
		},
		
		showNotLoggedInDiv : function() {
			// Hide other divs.			
			MyBookingQB.hideQBHomeDiv();
			MyBookingQB.hideAddCardDiv();
			MyBookingQB.hideSavedCardsDiv();
			MyBookingQB.hideResetPassDiv();
			
			$('#my-booking-not-logged-in').show();
		},
		
		hideNotLoggedInDiv : function() {
			$('#my-booking-not-logged-in').hide();
		},
		
		showBillingAddDiv : function() {
			MyBookingQB.setIsCardInternational(true);
			$('#qb-card-holder-address').show();
		},
		
		hideBillingAddDiv : function() {
			MyBookingQB.setIsCardInternational(false);
			$('#qb-card-holder-address').hide();
		},
		
		setViewCardBtnLabel : function() {
			$("#view-card-btn").html("View saved cards (" + noOfCards + ")");
		},
		
		showAuthPage : function() {
			window.location.replace(MyBookingQB.getAuthURL());
		},
		
		getAuthURL : function() {
			return "https://secure.yatra.com/social/common/yatra/signin.htm";
		},
		
		// Utility functions.
		
		isValid : function(value) {
			return (value != null && typeof(value) != "undefined" && value != "" && value != "null");
		},
		
		isInvalid : function(value) {
			return (value == null || typeof(value) == "undefined" || value == "" || value == "null");
		},
		
		convertToJsonObject : function(input) {
			var jsonObject = null;
			if(MyBookingQB.isValid(input)) {
				jsonObject = $.parseJSON(input);
			}
			return jsonObject;
		},
		
		ifArrayContains : function(array, string) {
			for (var j = 0; j < array.length; j++) {
				if (string == array[j])
					return true;
			}
			return false;
		},
		
		checkNaN : function(field) {
			var inputVal = $(field).val();
			if (isNaN(inputVal)) {
				var newInputVal = parseInt(inputVal);
				if (isNaN(newInputVal)) {
					newInputVal = '';
				}
				$(field).val(newInputVal);
			}
		},
		
		disableSpace : function(field) {
			$(field).val($(field).val().replace(/\s/g, ''));
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
		}
};

$(document).ready(function() {
	MyBookingQB.initialize();
	$('#qbLinkMobile').on('click',function(){MyBookingQB.showQBHomeDiv()});
});

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
        }
};