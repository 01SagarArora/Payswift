var YatraCorpCardUI = {
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