/**
 * 
 */
//size method defination
jQuery.fn.size = function(){
      return this.length;
  }
var cardsSaved = false;
var noOfCards = 0;
var internationalCardBrandsArr = new Array("VISA", "MASTER", "DINERS", "DINNERS", "RUPAY");
var isUserAuthenticated = true; // login has not expired (older than 1 hr)
var currentCardId;
var currentQBType;
var currentCardUL;
var currentisGDSCard;
var alreadyCustomized = false;
var canSaveCardForOffline = false;
var lvCardSelectedOfflineId;
var selectedLevelData;
var selectedLevelValue;
var saveInfo ={
		'target':'',
		'cardId':'',
		'formId':'',
		'qbType':''
}
var updateDataOnDuplicateAdd={};



var yatraCards = {
		
		initialize : function() {
						//yatraCardsgetSavedCards();
						yatraCards.showSavedCardsDiv()
						yatraCards.registerEvents();
					
		},
		
		postForm :  function(saveInfo, isUpdatingDuplicate){
			
			var addCardURL = '';
			var message;
			if(saveInfo['target'] === "ADD"){
				message = "Your card has been successfully added.";
			}
			else{
				message = "Your card has been successfully updated.";
			}
			if(saveInfo && !isUpdatingDuplicate){
				var formId = saveInfo['formId'];
				var postData = $('#'+saveInfo.formId).serializeArray();
			    addCardURL = $('#'+saveInfo.formId).attr("action");
			    
			    postData.push({
			    	"name" : "target",
			    	"value": saveInfo.target});
			    postData.push({
			    	"name" : "cardId",
			    	"value": saveInfo.cardId});
			 
			   
				    	postData.push({
					    	"name":"productGroup",
					    	"value" : JSON.stringify(["ALL"])
					    });
				    
					    postData.push({
					    	"name":"savedAtCorpLevel",
					    	"value" : true
					    })
				   
			   
			}else if(isUpdatingDuplicate){
				postData = yatraCards.getUpdateDataOnDuplicateAdd();
				addCardURL = "/PaySwift/corporate-quickbook/save-card.htm";
			}
			 if( saveInfo.target == "UPDATE"){
			    	postData.push({
				    	"name" : "qbType",
				    	"value": saveInfo.qbType});

			  }
			console.log(JSON.stringify(postData));
			$("#loading-popup-id").modal('show');
		    $.ajax({
		        
		    	url : addCardURL,
		        type : 'POST',
		        data : postData,
		        cache : false,
		        
		        success:function(response) {
		        	$("#loading-popup-id").modal('hide');
		        	var responseJSON = yatraCards.convertToJsonObject(response);
			        if(yatraCards.isValid(responseJSON) && responseJSON.status == "SUCCESS") {
				           	yatraCards.getSavedCards();
				           	yatraCards.showSavedCardsDiv();
							if($('#quickBookErrSucc').length > 0){
								$('#quickBookErrSucc').remove();
							}
							if(responseJSON.error_code == "109"){
								$('#saved-cards-error').append('<div class="ajaxErrorSuccess col-xs-12" id="quickBookErrSucc" style="background-color: #fff3bd;padding: 10px 15px;margin-bottom: 20px;color: #dc3201;font-family: Lato Regular, Helvetica Neue, Helvetica, Arial, sans-serif;"><i class="fa fa-tick-r"></i>Your card has been successfully added but Sorry, we could not add your card for processing offline bookings at this moment, please try again later.</div>')
								window.scrollTo($('#quickBookErrSucc').offset().left,$('#quickBookErrSucc').offset().top);
	        	
							}else{
								$('#saved-cards-error').append('<div class="ajaxErrorSuccess col-xs-12" id="quickBookErrSucc" style="background-color: #fff3bd;padding: 10px 15px;margin-bottom: 20px;color: #dc3201;font-family: Lato Regular, Helvetica Neue, Helvetica, Arial, sans-serif;"><i class="fa fa-tick-r"></i>' + message + '</div>')
								window.scrollTo($('#quickBookErrSucc').offset().left,$('#quickBookErrSucc').offset().top);
	        	
							}
						
			        } else if(yatraCards.isValid(responseJSON) && responseJSON.status == "FAILURE" && yatraCards.isValid(responseJSON.error_code) && responseJSON.error_code == "AUTH_REQUIRED") {
			        	yatraCards.showAuthPage();
			        	
			        } else if(yatraCards.isValid(responseJSON) && responseJSON.status == "FAILURE" && yatraCards.isValid(responseJSON.error_code) && responseJSON.error_code == "108") {
			        	yatraCards.showDuplicateCardErrorMsg();
			        	
			        } else {
			        	//yatraCards.showAddCardErrorMsg();
			        	$('#delete-conform-popup-id .content p').empty();
			        	var msg = (responseJSON.error_msg)?responseJSON.error_msg:"Error while adding quickbook card";
						$('#delete-conform-popup-id .content p').text(msg);
						$('#delete-conform-popup-id').modal('show');
			        }
		        },
		        error: function(request, status, error) {
		        	$("#loading-popup-id").modal('hide');
		        	$('#delete-conform-popup-id .content p').empty();
		        	var msg = "Error while adding quickbook card";
					$('#delete-conform-popup-id .content p').text(msg);
					$('#delete-conform-popup-id').modal('show');
		        }
		    });
		    //e.preventDefault(); // to stop default action.
		},

		
		
		registerEvents : function() {
			$('.show-corp-qb-home').on('click', function() {
				yatraCards.showSavedCardsDiv();
			});
			
			$('.js-card-type').on('click', 'button', function(e) {
				e.preventDefault();
				$(this).parent().find('.btn-selected').removeClass('btn-selected');
				$(this).addClass('btn-selected');
				yatraCards.resetAddCardDiv(false);
			});
			
			$('input:radio[name="address_check_rdo"]').on('change', function() {
				var cardIssueType = $(this).val();
				if(cardIssueType == "dom") {
					yatraCards.hideBillingAddDiv();
				} else {
					yatraCards.showBillingAddDiv();
				}
			});
			
					
			$('.delete_bts').on('click', 'button', function(e) {
				$("#delete-yatra-card-popup-id").modal('hide');
				var confirm = $(this).attr("confirm");
				if(confirm == "yes") {
					$("#loading-popup-id").modal('show');
					$.when(yatraCards.deleteQBCard(currentCardId, currentQBType, "yatra", "mybookings", currentisGDSCard)).then(function(res){
						$("#loading-popup-id").modal('hide'); 
						if(res=="success"){
							
							$(currentCardUL).remove();
							if(noOfCards == 1) {
								cardsSaved = false;
								yatraCards.getNoCardsDiv();
							}
							noOfCards--;
							
							currentisGDSCard = '';
							$('#delete-conform-popup-id .content p').empty();
							$('#delete-conform-popup-id .content p').html("The card <span style='color:red'>"+ currentCardId+" </span> has been deleted successfully. Please remove any related Yatra Cards configuration where this card has been used.");
							$('#delete-conform-popup-id').modal('show');
							currentCardId = '';
							currentQBType = '';
							currentCardUL = '';
						}
						if(res =="failure"){
							$('#delete-conform-popup-id .content p').empty();
							$('#delete-conform-popup-id .content p').text("Error while deleting quickbook card");
							$('#delete-conform-popup-id').modal('show');
							currentCardId = '';
							currentQBType = '';
							currentCardUL = '';
						}
					});
					
				}
			});
			
			$("#qb_cno_id").on('keyup', function(e) {

				// Not allow spaces.
				yatraCards.disableSpace($(this));

				// Only digits allowed on cardNumber input.
				yatraCards.checkNaN($(this));

				// Get CardNumber value.
				var cardNo = "";
				cardNo = $(this).val();

				// Identify CardBrand.
				var cardBrand = yatraCards.getCardBrand(cardNo);
				yatraCards.setCardBrand(cardBrand);
				
				// Logic for fading.
				yatraCards.fadeCardLogo(cardBrand);
				
				// Show Address div for amex cards
				yatraCards.showAmexIssueDiv(cardBrand);
				
				// Show Expiry Msg for Maestro cards.
				yatraCards.showMaestExpiryMsg(cardBrand);
				
				// Update validations for the cardType and cardBrand.
				yatraCards.updateValidations(cardBrand);			

				// Logic for displaying Billing Address
				yatraCards.processInternationalCard(cardBrand, cardNo);

			});
			
			
			$("#qb_add_card_btn_id").on('click', function() {
				var isValid = PaymentValidation.validate('#corp_qb_add_card_form_id');
				saveInfo['formId'] = 'corp_qb_add_card_form_id';	
				//var isValid_entity=yatraCards.validateSelectedLevelData("corp_qb_add_card_form_id");
				$("#corp_qb_ctype_id").val("CORPORATE");
				var qbookingType = $('#corp_qb_qbtype_id').attr('value');
				var isValid_mapping = false;
				var isValid_product  = false;
				if(isValid){
					saveInfo['qbType'] = qbookingType;
					saveInfo['cardId'] = '';
					saveInfo['target'] = 'ADD';				
					yatraCards.postForm(saveInfo);
				}
				
			});
			
					
		},
		getUpdateDataOnDuplicateAdd : function(){
			return updateDataOnDuplicateAdd;
		},
		setUpdateDataOnDuplicateAdd : function(data){
			updateDataOnDuplicateAdd = data;
		},
		getSavedCards : function() {
			var merchant = 'yatra';
			var product = 'mybookings';
			var saveCardURL = yatraCards.getSavedCardsURL();
			$.ajax({
				
				url: saveCardURL,
				type: 'POST',
				data: { 
				        'merchant': merchant, 
				        'product': product
				},
				cache: false,

				success : function(response) {
	//				response = '{"status":"SUCCESS","CORPORATE":[{"cardId":"48046bde-31bd-4160-bdbc-917f272f1e76","corporateId":null,"title":"Mr.","firstName":"lkjlsd","lastName":"","cardName":"nameSample","cardType":"CORPORATE","cardBrand":"VISA","cardNumber1":"4214","cardNumber2":"24xx","cardNumber3":"xxxx","cardNumber4":"2911","hashedCard":"299e16e5f6c013c72743c42d5468f891e2e444356f0e67902604ed12b13a871734ae17700e83dcf53092e1c3f391b129ab5a515f0764b1e30fa595af13031501","expiryMonth":null,"expiryYear":null,"cardAccessType":"DOM","cardProperties":{"isEnabled":"true"},"productGroup":null,"levelGroup":null},{"cardId":"0824bf2a-3796-4ac5-9120-2df22647d316","corporateId":null,"title":"Mr.","firstName":"ldakjdl","lastName":"","cardName":"dkjadhk","cardType":"CreditCard","cardBrand":"DINERS","cardNumber1":"4214","cardNumber2":"24xx","cardNumber3":"xxxx","cardNumber4":"2911","hashedCard":"299e16e5f6c013c72743c42d5468f891e2e444356f0e67902604ed12b13a871734ae17700e83dcf53092e1c3f391b129ab5a515f0764b1e30fa595af13031501","expiryMonth":null,"expiryYear":null,"cardAccessType":"DOM","cardProperties":{},"productGroup":null,"levelGroup":null},{"cardId":"0f6f9557-2e6e-400c-90bb-c89fd414f57e","corporateId":null,"title":"Mr.","firstName":"kldja","lastName":"","cardName":"daskdjh","cardType":"CORPORATE","cardBrand":"DINERS","cardNumber1":"4111","cardNumber2":"11xx","cardNumber3":"xxxx","cardNumber4":"1111","hashedCard":"71dd8d47ccae5f67daa09c11e6e360609c6e55ff513b98d52e823dce089e27ca20f630ec88f0211e3b8f8c92e8e964c16e7b60ec70d6fca2f7417eee40b348d2","expiryMonth":null,"expiryYear":null,"cardAccessType":"DOM","cardProperties":{},"productGroup":null,"levelGroup":null},{"cardId":"340c8166-dd86-4cbb-bc2a-a12b6fa5e0a9","corporateId":null,"title":"Mr.","firstName":"sadsadas","lastName":"","cardName":"asdas","cardType":"CORPORATE","cardBrand":"DINERS","cardNumber1":"3401","cardNumber2":"75xxxx","cardNumber3":"x3757","cardNumber4":"","hashedCard":"2f3ac19c479e3327a2a80ed426ad57eac2a4516bc8a4139e2c039093c921d6347f36961ae0e2e95e05e2b384995e5bfc35f09059e3d03722ebc2667432fbf7f8","expiryMonth":null,"expiryYear":null,"cardAccessType":"DOM","cardProperties":{},"productGroup":null,"levelGroup":null},{"cardId":"eed37e40-7d35-48f5-881b-6dc0f7e070cf","corporateId":null,"title":"Mr.","firstName":"lkjl","lastName":"","cardName":"123","cardType":"CORPORATE","cardBrand":"VISA","cardNumber1":"4214","cardNumber2":"24xx","cardNumber3":"xxxx","cardNumber4":"2911","hashedCard":"299e16e5f6c013c72743c42d5468f891e2e444356f0e67902604ed12b13a871734ae17700e83dcf53092e1c3f391b129ab5a515f0764b1e30fa595af13031501","expiryMonth":null,"expiryYear":null,"cardAccessType":"DOM","cardProperties":{"isEnabled":"true"},"productGroup":null,"levelGroup":null},{"cardId":"a375c103-2933-47a7-93cb-96845ca99fae","corporateId":null,"title":"Mr.","firstName":"wdio","lastName":"","cardName":"qiowue","cardType":"CreditCard","cardBrand":"DINERS","cardNumber1":"3485","cardNumber2":"03xxxx","cardNumber3":"x6320","cardNumber4":"","hashedCard":"ad3027c637380d032b31b9a48800636986f6723622e9dc01ca4d3eca01db349c435cf18783eea7e01bdecc4b60698135a8b6f215b2c11a55b1e63166015054f7","expiryMonth":null,"expiryYear":null,"cardAccessType":"DOM","cardProperties":{},"productGroup":null,"levelGroup":null},{"cardId":"bcb199bd-24a5-4638-bd5a-f839d10118e6","corporateId":null,"title":"Mr.","firstName":"lofty","lastName":"","cardName":"adshfhd","cardType":"CreditCard","cardBrand":"DINERS","cardNumber1":"3735","cardNumber2":"82xxxx","cardNumber3":"x5744","cardNumber4":"","hashedCard":"87403fd3bb1bbd2cb6217eb7ee48d30e532cb16b62d7d9f258e01599d74b6a00cb802cd1188554c4ec2adfe0986390aad3a0d6e1f2af6cc6e78ba723401be9d3","expiryMonth":null,"expiryYear":null,"cardAccessType":"DOM","cardProperties":{},"productGroup":null,"levelGroup":null}],"PERSONAL":[],"allowOfflineBookings":true,"users":null,"entities":null}';
					yatraCards.processGetCardsResponse(response);
				}, 
				
				error: function (request, status, error) {
					//alert("Error occurred while getting saved cards.");
					yatraCards.getErrorCardsDiv();
				}
			});
			},
		
		getSavedCardsURL : function(){
			return "/PaySwift/yatra-quickbook/get-yatra-cards.htm";
		},
		
		processGetCardsResponse : function(qbJson) {
			
			// Initialize no of cards to 0.
			noOfCards = 0;	
			
			// set isUserAuthenticated to true.
			isUserAuthenticated = true;
			
			// Convert JSON to JSONObject.
			var jsonObject = yatraCards.convertToJsonObject(qbJson);
			
			if(jsonObject.error_type == "AUTH_FAILED" || yatraCards.isInvalid(jsonObject)) {
				yatraCards.getAuthErrorDiv();
				return;
					}
			
			// If JSON Object is null, show error msg.
			if(yatraCards.isInvalid(jsonObject) || 'SUCCESS' != jsonObject.status) {
				yatraCards.getErrorCardsDiv();
				return;
			}
		
				
		
			
			if(yatraCards.isEmptyCardsList(jsonObject)){
				// If there are no cards saved, show no cards div.
				yatraCards.getNoCardsDiv();
			}
			else{
				// Get saved cards and show them.
				yatraCards.getSavedCardsDiv(jsonObject);
				$(".card-type").each(function(){
					if($(this).text() === "undefined"){
						$(this).hide();
					}
				});
				if(cardsSaved) {
					yatraCards.registerDeleteCardEvent();
					return;
				}
			}
			
		},
		
		isEmptyCardsList : function (jsonObject) {
			var isEmpty = true;
			for (var qbType in jsonObject) {
				if( $.isArray(jsonObject[qbType]) && jsonObject[qbType].length ) {
					isEmpty = false;
					break;
				}
			}
			return isEmpty;
		},
		
	
		
		getSavedCardsDiv : function(qbJsonObject) {
			savedCardsJson = qbJsonObject;
			mappingLevelJson = qbJsonObject;
			if(savedCardsJson != null && savedCardsJson != undefined) {
				cardsSaved = true;
				var savedCards = '<section><div class="row" style="margin-bottom:10px">';
				savedCards += '<div class="pull-left col-md-9 hidden-xs"><h3 class="mg-top mg-bottom heading-bold"></h3></div>';
				savedCards += '<div class="pull-right col-md-3 col-xs-12"><button class="yt-btn btn-block" id="savedAddCardBtn" type="button">';
				savedCards += '+  Add a card</button></div></div><div class="clearfix"></div>';
				savedCards += '<div class="container-saved-card">';
				savedCards += ' <div class="header-container"><p>Saved Cards</p></div><div class="content-saved-card">';
				
				for (var qbType in savedCardsJson) {
					if( $.isArray(savedCardsJson[qbType]) && savedCardsJson[qbType].length ) {
						savedCards += yatraCards.getSavedCardsListingHtml(savedCardsJson[qbType], qbType);
					}
				}
				savedCards += '</div></div></section>';
				$('#saved-cards').html(savedCards);
				$('#savedAddCardBtn').on('click',function(){
					yatraCards.showAddCardDiv();
				});
				
				$('input:checkbox[name="cardForOfflineBooking"]').on('click', function(e) {
					if($('#quickBookErrSucc').length > 0){
						$('#quickBookErrSucc').remove();
					}
					currentCardId = $(this).parents(".card-details-container").find(".card-number").attr("card-id");
					currentQBType = $(this).parents(".card-details-container").find(".card-number").attr("qb-type");
					currentisGDSCard = $(this).parents(".card-details-container").find(".card-number").attr("isGDSCard")
					currentCardUL = $(this).parents(".card-row");
					lvCardSelectedOfflineId = this.id
					if (this.checked) {
						$("#lv_savecardforOfflinePopupTitle").html("Are you sure you want to allow this card for processing offline bookings?");
						if(jQuery('input:checkbox[name="cardForOfflineBooking"]:checked').length) {
							$("#lv_savecardforOfflinePopupSubtitle").css("visibility", "visible");
						} else {
							$("#lv_savecardforOfflinePopupSubtitle").css("visibility", "hidden");
						}
					
						$("#lv_save-card-offline-popup-id").modal('show');
				    } else {
				    	
						$("#lv_savecardforOfflinePopupTitle").html("Are you sure you want to remove this card for processing offline bookings?");
						$("#lv_savecardforOfflinePopupSubtitle").css("visibility", "hidden");
						$("#lv_save-card-offline-popup-id").modal('show');
				    }
					e.preventDefault();
					
				});
				$('.lv_save_card_offline').off('click');
				$('.lv_save_card_offline').on('click', 'button', function(e) {
					$("#lv_save-card-offline-popup-id").modal('hide');
					var confirm = $(this).attr("confirm");
					if(confirm == "yes" && $('#'+lvCardSelectedOfflineId).is(":checked")) {
						// remove the offline card
						

						yatraCards.deleteGDSCard(currentCardId, currentQBType, "yatra", "mybookings");
						return false;
					}else if(confirm == "yes" && !$('#'+lvCardSelectedOfflineId).is(":checked")){
						// add the card 
						
						
						// $('input:checkbox[name="cardForOfflineBooking"]').attr('checked',false);
						// $('#'+lvCardSelectedOfflineId).prop('checked', true);
						// $('#'+lvCardSelectedOfflineId).attr('value', $('#'+lvCardSelectedOfflineId).prop('checked'));
						yatraCards.saveGDSCard(currentCardId, currentQBType, "yatra", "mybookings");
						return false;
					} else {
						
					}
				});
			} 
			if(window.attachEvent && !window.addEventListener){
				$('.col-xs-12').removeClass('col-xs-12');
				$('.col-xs-5').removeClass('col-xs-5');
			}
		},
		
		getSavedCardsListingHtml : function(savedCardsArray, qbType){
			var savedCards = '';
			for (var i = 0; i < savedCardsArray.length; i++) {
				var cardNumber = yatraCards.getCardNumber(savedCardsArray[i], qbType);
				var gdsFlag =  savedCardsArray[i].isGDSCard?'true':'false';
				var checkedFlag = savedCardsArray[i].isGDSCard?'checked':'';
				var showCheckboxFlag = savedCardsArray[i].bookingType && savedCardsArray[i].bookingType  == 'official'?true:false;
				if(qbType != "PERSONAL"){
					var productFlag= savedCardsArray[i].productGroup ?true:false;
					var mappingFlag=savedCardsArray[i].productGroup ?true:false;
				}
				savedCards += '<div class="card-row"><div class="card-picture">';
				savedCards += '<i class="' + yatraCards.getCardLogo(savedCardsArray[i].cardBrand) + '"></i></div>';
				savedCards += ' <div class="card-details" style="padding-left: 15px;"><div class="card-details-container" style="margin-bottom:0px"><div class="card-type-container" style="margin-bottom:0px"><div class="card-type-number-box"><div class="card-number-name">';
				savedCards += ' <div class="card-number-tag">< card-id="'+savedCardsArray[i].cardId+'" qb-type="'+qbType+ '" isGDSCard="'  + gdsFlag +'"><p class="heading-bold" style="display:inline-block">'+ cardNumber.toString().replace(/-/g, " ") +'</p><div  class="card-type">'+ yatraCards.getCardTypeName(savedCardsArray[i].cardType) + '</div></div>';
				savedCards += '<p  class="card-name">'+ yatraCards.getCardName(savedCardsArray[i], qbType) +'</p>';
				savedCards += '<p  class="card-name"> Card Id : '+ savedCardsArray[i].cardId +'</p>';
				
				//savedCards += '<div  class="card-type">'+ qbType +'</div>';
				savedCards += '</div></div></div>';
				savedCards += '</div>';
				savedCards += '<div data-brackets-id="1440" class="card-actions-block"><ul data-brackets-id="1441" class="card-actions">';
				savedCards += '<li data-brackets-id="1447"><p data-brackets-id="1448" class="delete-action"><a data-brackets-id="1449" href="#">Delete</a></p></li>';  
                savedCards += '</ul></div></div></div></div>';
				noOfCards++;
			}
			return savedCards;
		},
		
		
		
	getCardNumber : function(cardJson, qbType) {
			if('CORPORATE' == qbType){
				return cardJson.cardNumber1 + cardJson.cardNumber2 + cardJson.cardNumber3 + cardJson.cardNumber4;
			}	else {
				return cardJson.cardNumber;
			}
		},
		
		getCardName : function(cardJson, qbType) {
			if('CORPORATE' == qbType){
				return ((cardJson.cardName && cardJson.cardName.toLowerCase()!="null")?(cardJson.cardName + ', '):"") + ((cardJson.firstName && cardJson.firstName.toLowerCase()!="null")?(cardJson.firstName + ' '):"") + ((cardJson.lastName && cardJson.lastName.toLowerCase()!="null")?(cardJson.lastName):"");
			}	else {
				return ((cardJson.cardName && cardJson.cardName.toLowerCase()!="null")?(cardJson.cardName + ', '):"") + ((cardJson.cardHolderFirstName && cardJson.cardHolderFirstName.toLowerCase()!="null")?(cardJson.cardHolderFirstName + ' '):"") + ((cardJson.cardHolderLastName && cardJson.cardHolderLastName.toLowerCase()!="null")?(cardJson.cardHolderLastName):"");
			}
		},
	
	
		getCardTypeName : function(name){
			if(name && name.toLowerCase() != "null"){
				switch(name){
					case "CORPORATE" : return "Corporate Card";
					case "BTA" : return "BTA Card";
					case "CTA" : return "CTA Card";
					case "BTA" : return "BTA Card";
					case "PCARD" : return "P-Card";
					default : return name;
				}
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
			var savedCards = '<section class="col-md-12" style="font-family: Lato Regular, Helvetica Neue, Helvetica, Arial, sans-serif;"><div class="row">';
			savedCards += '<div class="pull-left col-md-8 hidden-xs"><h3 class="mg-bottom" style="margin-top:10px">Yatra Card</h3></div>';
			savedCards += '<div class="pull-right col-md-4 col-xs-12"><button class="yt-btn btn-block" onclick="yatraCards.showAddCardDiv()" type="button">';
			savedCards += '+  Add a card</button></div></div><div class="clearfix"></div>';
			savedCards += '<p style="font-size:14px; margin-top:12px">' + yatraCards.getNoCardsMsg()+'</p>';
			savedCards += '</section>';
			$('#saved-cards').html(savedCards);
		},
		
		getErrorCardsDiv : function() {
			var savedCards = '<section class="col-md-12" style="font-family: Lato Regular, Helvetica Neue, Helvetica, Arial, sans-serif;"><div class="row">';
			savedCards += '<div class="pull-left col-md-8 hidden-xs"><h3 class="mg-bottom" style="margin-top:10px">Yatra Card</h3></div>';
			savedCards += '<div class="pull-right col-md-4 col-xs-12"><button class="yt-btn btn-block" onclick="yatraCards.showAddCardDiv()" type="button">';
			savedCards += '+  Add a card</button></div></div><div class="clearfix"></div>';
			savedCards += '<p style="font-size:14px; margin-top:12px">' + yatraCards.getErrorCardsMsg() + '</p>';
			savedCards += '</section>';
			$('#saved-cards').html(savedCards);
			},
			
			getAuthErrorDiv : function() {
				var savedCards = '<section class="col-md-12" style="font-family: Lato Regular, Helvetica Neue, Helvetica, Arial, sans-serif;"><div class="row">';
				savedCards += '<div class="pull-left col-md-12 hidden-xs" style="margin-bottom:10px"><h3 class="mg-bottom" style="margin-top:10px">Yatra Card</h3></div>';
				savedCards += '<p style="font-size:14px; margin-top:12px;padding-left: 12px; padding-top:10px">' + yatraCards.getAuthErrorMsg() + '</p>';
				savedCards += '</section>';
				$('#saved-cards').html(savedCards);
				},
		
		getErrorCardsMsg : function() {
			return 'Error occurred while fetching your cards. Please try after some time.';
		},
		
		getNoCardsMsg : function() {
			return 'There are no Yatra/ATB pass-through cards saved currently. Start adding cards by clicking on "Add a Card" button.';
		},
		getAuthErrorMsg : function(){
			return 'You are not authorized to access Yatra/ATB pass-through cards.';
		}, 
		
		
		registerDeleteCardEvent : function() {
			$('.delete-action').each(function(index) {
				$(this).on('click', function() {
					currentCardId = $(this).parents(".card-details-container").find(".card-number").attr("card-id");
					currentQBType = $(this).parents(".card-details-container").find(".card-number").attr("qb-type");
					currentisGDSCard = $(this).parents(".card-details-container").find(".card-number").attr("isGDSCard")
					currentCardUL = $(this).parents(".card-row");
					$("#delete-yatra-card-popup-id").modal('show');
				})
			});
		},
		
		deleteQBCard : function(cardId, qbType, merchant, product, isGDSCard ) {
			
			var qbDeleteCardURL = yatraCards.getQBDeleteCardURL();
			var delDefer = $.Deferred();
			
			$.ajax({
				
				url: qbDeleteCardURL,
				type: 'POST',
				data: { 
				        'merchant': merchant, 
				        'product': product,
				        'cardid' : cardId,
				        'qbType' : qbType,
				        'isGDSCard' : isGDSCard
				},
				cache: false,

				success : function(response) {
					var responseJSON = yatraCards.convertToJsonObject(response);
					if( responseJSON.status && responseJSON.status == "SUCCESS"){
						if($('#quickBookErrSucc').length > 0){
							$('#quickBookErrSucc').remove();
						}
						
						delDefer.resolve('success');
//						$('#saved-cards-error').append('<div class="ajaxErrorSuccess col-xs-12" id="quickBookErrSucc" style="background-color: #fff3bd;padding: 10px 15px;margin-bottom: 20px;color: #dc3201;"><i class="fa fa-tick-r"></i>Your card has been successfully removed.</div>')
//						window.scrollTo($('#quickBookErrSucc').offset().left,$('#quickBookErrSucc').offset().top);
					}
					  if(yatraCards.isValid(responseJSON) && responseJSON.status == "FAILURE") {
						  $("#loading-popup-id").modal('hide'); 
						  $('#saved-cards-error').append('<div class="ajaxErrorSuccess col-xs-12" id="quickBookErrSucc" style="border: 1px solid #fda5ab;padding: 10px 15px;margin-bottom: 20px;color: #ea2330;font-family:lato regular;border-radius: 4px;background: rgba(194, 35, 48, 0.1);">'+responseJSON.error_msg+'</div>')
				        		//delDefer.resolve('failure');
					  }
				}, 
				
				error: function (request, status, error) {
					
					delDefer.resolve('failure');
				}
			});
			return delDefer.promise();
		},
		
		getQBDeleteCardURL : function () {
			return "/PaySwift/yatra-quickbook/delete-yatra-card.htm"
		},
		
		setCardType : function() {
			var cardType = $('.js-card-type .btn-selected').attr("card_type");
			if(yatraCards.isValid(cardType)) {
				$("#corp_qb_ctype_id").val(cardType);
			} else {
				$("#corp_qb_ctype_id").val("");
			}
		},
		
		setQBType : function() {
			var cardType = $('.js-card-type .btn-selected').attr("qb-type");
			if(yatraCards.isValid(cardType)) {
				$("#corp_qb_qbtype_id").val(cardType);
			} else {
				$("#corp_qb_qbtype_id").val("");
			}
		},
		
		getCardType : function() {
			return $("#corp_qb_ctype_id").val();
		},
		
		setCardBrand : function(cardBrand) {
			if(yatraCards.isValid(cardBrand)) {
				$("#corp_qb_cbrand_id").val(cardBrand);
			} else {
				$("#corp_qb_cbrand_id").val("");
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
			return $("#corp_qb_isCardInternational_id").val();
		},
		
		setIsCardInternational : function(isCardInt) {
			$("#corp_qb_isCardInternational_id").val(isCardInt);
		},
		
		fadeCardLogo : function(cardBrand) {
			var cardBrand = $("#corp_qb_cbrand_id").val();
			$.each($(".card_logos > i"), function() {
				var cardLogoId = $(this).attr("id");
				var cardLogo = cardLogoId.substring(0, cardLogoId.indexOf("_")).toUpperCase();
				if(yatraCards.isInvalid(cardBrand) || cardLogo == cardBrand) {
					$(this).removeClass("ico-o-5");
				} else {
					$(this).addClass("ico-o-5");
				}
			});
		},
		
		showMaestExpiryMsg : function(cardBrand) {
			/*var cardType = yatraCards.getCardType();*/
			if(yatraCards.isValid(cardBrand) && cardBrand == "MAEST" /*&& cardType == "dc"*/) {
				$("#maest_expiry_msg").show();
			} else {
				$("#maest_expiry_msg").hide();
			}
		},
		
		updateValidations : function(cardBrand) {
			
			if(yatraCards.isValid(cardBrand)) {
				
				// Get Card Type (cc/dc)
				var cardType = yatraCards.getCardType();
				
				// Card number.
				var validationRule = yatraCards.getCardNoValidationRule(cardType, cardBrand);
				var validationMsg = yatraCards.getCardNoValidationMsg(cardType, cardBrand);
				
				$('#qb_cno_id').attr('data-validation', validationRule);
				$('#qb_cno_id').attr('data-msginfo', validationMsg);
				
			} else {
				yatraCards.setDefaultValidations();
			}
		},
		
		getCardNoValidationRule : function(cardType, cardBrand) {
		
			// Validation Rule for CC
			var validationRule = 'required|validateLuhn|cardRegType[' + cardBrand + ']';
			
			return validationRule;
		},
		
		getCardNoValidationMsg : function(cardType, cardBrand) {
			
			// Validation Msg for CC
			var validationMsg = '* required|Enter valid card number|Enter valid card number|Enter valid credit card number';
			
			return validationMsg;
		},

		processInternationalCard : function(cardBrand, cardNo) {
		
			if(!yatraCards.isCardBrandInternational(cardBrand)) {
				yatraCards.hideBillingAddDiv();
				return;
			}
			
			// Check if card is international (only for VISA and MASTER). If international, show address div.
			if(yatraCards.isCardLengthValid(cardBrand, cardNo)) {
				
				var bin = parseInt(cardNo.substring(0, 6));
				var isCardIntURL = yatraCards.getIsCardInternationalURL();
			
				$.ajax({
					
					url: isCardIntURL,
					type: 'POST',
					data: { 
							'bin': bin 
					},
					cache: false,

					success : function(response) {
						var responseJSON = yatraCards.convertToJsonObject(response);
						if (yatraCards.isValid(responseJSON) && responseJSON.isCardInternational == "true") {
							yatraCards.showBillingAddDiv();
						} else {
							yatraCards.hideBillingAddDiv();
						}
					}, 
					
					error: function (request, status, error) {
						alert("Error while determining if card is international or not.");
					}
				});
			} else {
				yatraCards.hideBillingAddDiv();
			}
		},
		
		isCardBrandInternational : function(cardBrand) {
			if(yatraCards.isInvalid(cardBrand))
				return false;
			return yatraCards.ifArrayContains(internationalCardBrandsArr, cardBrand)
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
			/*if(enableDefaultTab) {
				if(showCorpCardsTab)
					yatraCards.enableCorporateTab();
				else {
					yatraCards.enablePersonalTab();
					yatraCards.hideCorporateTabs();
				}
			} */
			
			// Hiding details / div.
			yatraCards.clearDetails();
			yatraCards.hideErrorMsgs();
			yatraCards.hideBillingAddDiv();
			yatraCards.hideAmexIssueDiv();
			yatraCards.resetDropDown();
			yatraCards.setCardBrand("");
			
			// Reseting depending upon cardType (cc/dc)
			yatraCards.setCardType();
			yatraCards.setQBType();
			yatraCards.showCardLogos();
			yatraCards.setCardNoLabel();
			yatraCards.setDefaultValidations();
		},
		
	clearDetails : function() {
			$("#add_card_div_id input:text").each(function() {
				$(this).val("");
				IE_Placeholder.callBackPlaceDefaultText($(this));
			});
			
			//clearing multiselect dropdown - product, mapping level and autosuggest fields
			var textDisplayProduct = ["Domestic Flight","Domestic Hotel","International Hotel","International Flight","Others"];
			$("#productlist input[type='checkbox']").attr("checked",true);
			$("#product-div #product").text(textDisplayProduct);
			$("#mappingLevel_List input[type='checkbox']").attr("checked",false);
			$("#mappingLevel_List input[value='companyLevel']").attr("checked",true);
			$("#product-div #mappingLevel").text("Company Level");
			$(".companyLevel,.entityGroup,.entity,.user,.dropdown-menu-product,.dropdown-menu-mapping").hide();
			$('#be-group-values-container').hide();
			$('#entity-values-container').hide();
			$('#user-values-container').hide();
			// clearing the offline checkbox also
			$('#saveCardToGDS').attr('checked', false);
			$('#saveCardToGDS').attr('value', false);
			yatraCards.resetYourCardRadio()
			
		},
		
		hideErrorMsgs : function() {
			$(".qb_error_msg").hide();
		},
		
		resetDropDown : function() {
			$('#qb_expm_id :nth(0)').attr("selected", "selected");
			$('#qb_bacu_id :nth(0)').attr("selected", "selected");
			yatraCards.populateExpYrDropDown();
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
			
			var cardType = yatraCards.getCardType();
			/*switch(cardType) {
				case "cc": */$("#maest_logo_id").hide(); /*break;*/
				/*case "dc": $("#amex_logo_id").hide(); break;
			}*/
		},
		
		setCardNoLabel : function() {
			 $("#card_no_label_id").html("Credit Card Number");
		},
		
		setDefaultValidations : function() {
			
			// Card Number	
			/*var cardType = yatraCards.getCardType();
			if(cardType == 'cc') {
				$('#qb_cno_id').attr('data-validation', 'required|validateLuhn|cardRegType[VISA]|supportedCard[VISA,cc]');
				$('#qb_cno_id').attr('data-msginfo', '* required|Enter valid card number|Enter valid card number|Enter valid credit card number');
				$("#qb_cno_id").attr("maxlength","16");
			} else {
				$('#qb_cno_id').attr('data-validation', 'required|validateLuhn|cardRegType[VISA]|supportedCard[VISA,dc]');
				$('#qb_cno_id').attr('data-msginfo', '* required|Enter valid card number|Enter valid card number|Enter valid debit card number');
				$("#qb_cno_id").attr("maxlength","19");
			}*/
			
			$('#qb_cno_id').attr('data-validation', 'required|validateLuhn|cardRegType[VISA]');
			$('#qb_cno_id').attr('data-msginfo', '* required|Enter valid card number|Enter valid card number|Enter valid credit card number');
			$("#qb_cno_id").attr("maxlength","16");
				
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
			/*var cardType = yatraCards.getCardType();*/
			if(yatraCards.isValid(cardBrand) && cardBrand == 'AMEX' /*&& cardType == 'cc'*/) {
				$('input:radio[name="address_check_rdo"]:nth(0)').attr("checked", "checked");
				$("#amex_issue_div").show();
			} else {
				yatraCards.hideAmexIssueDiv();
			}
		},
		
		hideAmexIssueDiv : function() {
			$("#amex_issue_div").hide();
		},
		
		showAddCardDiv : function() {
			// Hide other divs.
			yatraCards.hideSavedCardsDiv();
			yatraCards.resetYourCardRadio();
			$('#corp-add-card').show();
			yatraCards.resetAddCardDiv(true);
			if($('#quickBookErrSucc').length > 0){
				$('#quickBookErrSucc').remove();
			}
		},
		
		resetYourCardRadio : function(){
			$('input:radio[name="bookingType"]').attr('checked', false)
			$("#allowOfflineCheckBoxContainer").hide();
		},
		hideAddCardDiv : function() {
			$('#corp-add-card').hide();
		},

		showSavedCardsDiv : function() {
			
				// Hide other divs.
				yatraCards.hideAddCardDiv();
				$('#corp-saved-cards').show();
				yatraCards.getSavedCards();
				
			
		},
		
		hideSavedCardsDiv : function() {
			$('#corp-saved-cards').hide();
		},
		
		showBillingAddDiv : function() {
			yatraCards.setIsCardInternational(true);
			$('#corp-qb-card-holder-address').show();
		},
		
		hideBillingAddDiv : function() {
			yatraCards.setIsCardInternational(false);
			$('#corp-qb-card-holder-address').hide();
		},
		
		
		showAuthPage : function() {
			yatraCards.hideAddCardDiv();
			$('#corp-saved-cards').show();
			yatraCards.getAuthErrorDiv();
		},
		
		
		isValid : function(value) {
			return (value != null && typeof(value) != "undefined" && value != "" && value != "null");
		},
		
		isInvalid : function(value) {
			return (value == null || typeof(value) == "undefined" || value == "" || value == "null");
		},
		
		convertToJsonObject : function(input) {
			var jsonObject;
			if(yatraCards.isValid(input)) {
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
	yatraCards.initialize();
	$(".custom-Radiobox").click(function(event){
		$(".custom-Radiobox").each(function(){
			if(this == event.target || this.contains(event.target)){
			$(this).find("input[type='radio']").attr("checked","checked");
		}
		});
							
	});
	$("#qb_expm_id_div").keyup(function(event){
		if (event.keyCode == 9) {
			$("#qb_expm_id_div .select .options").show();
		  }
	});
	$("#qb_expm_id_div").change(function(){
		$("#qb_expm_id_div .select .options").hide();
	});
	$("#qb_expm_id_div").focusout(function(){
		$("#qb_expm_id_div .select .options").hide();
	});
	$("#qb_expy_id_div").keyup(function(event){
		if (event.keyCode == 9) {
			$("#qb_expy_id_div .select .options").show();
		  }
	});
	$("#qb_expy_id_div").change(function(){
		$("#qb_expy_id_div .select .options").hide();
	});
	$("#qb_expy_id_div").focusout(function(){
		$("#qb_expy_id_div .select .options").hide();
	});
	
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
