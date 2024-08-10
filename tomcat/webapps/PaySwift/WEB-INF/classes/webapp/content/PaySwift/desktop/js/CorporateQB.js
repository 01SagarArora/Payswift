/**
 *
 */
//size method defination
jQuery.fn.size = function() {
    return this.length;
}
var ctaCardData = null;
var cardsSaved = false;
var noOfCards = 0;
var internationalCardBrandsArr = new Array("VISA", "MASTER", "DINERS", "DINNERS", "RUPAY");
var isUserAuthenticated = true; // login has not expired (older than 1 hr)
var currentCardId;
var currentQBType;
var currentCardUL;
var currentisGDSCard;
var currentisSaveAsCorpCard;
var alreadyCustomized = false;
var canSaveCardForOffline = false;
var btaInPersonalEnabled = false;
var btaInPersonalArray = [];
var lvCardSelectedOfflineId;
var selectedLevelData;
var selectedLevelValue;
var selectedEAUser
var saveInfo = {
    'target': '',
    'cardId': '',
    'formId': '',
    'qbType': ''
}
var updateDataOnDuplicateAdd = {};
var isPCC = false;

var CorporateQB = {
	tokenInfoTitle: "Tokenization Information",
	tokenInfoContent: "Please wait and kindly complete the process of tokenization.",
	activeTokenCardLabel: "Card Tokenized",
	inactiveTokenCardLabel: "Card not Tokenized",
	inactiveTokenCardLabelDesc: "Due to some issues at your bank. Your card could not be tokenized. <br />Please reach out to your bank.",
    tokenCardListUrl:"/PaySwift/corporate-quickbook/corporateCardsTokenizationStatus?corporateId",
    initialize: function() {
    	if(eval(showPccTab) != true){
    			$("#qb_pcc_tab_id").hide();
    		}

    if(corpUserRole == "EA"){
        $("#ea-user-container").show();
    }else{
        $("#ea-user-container").hide();
    }


        if (CorporateQB.isValid(isAuthorized)) {
            if (isAuthorized == "true") {
                CorporateQB.initializeSelectedLevelData();
                CorporateQB.getSavedCards();
                CorporateQB.showQBHomeDiv();
                CorporateQB.registerEvents();
            } else
                CorporateQB.showNotAuthorizedDiv();

        } else CorporateQB.showNotAuthorizedDiv();

    },

    productValidation: function(divId) {
        var selected_product = [];
        var element = $("#" + divId + " #product");
        $("#" + divId + " #productlist input[type='checkbox']").each(function() {
            if ($(this).is(":checked")) {
                selected_product.push($(this).attr('value'));
            }
        });
        if (selected_product.length == 0 && !CorporateQB.isSbt()) {
            var isMobile = $(window).width() < 768 ? true : false;
            Toast.show("Select atleast one product", element, undefined, function() {}, isMobile);
            $(window).scrollTop(element.offset().top);
            return false;
        } else {
            return true;
        }
    },
    mappingValidation: function(divId) {
        var selected_mapping = [];
		var cType = $('#corp_qb_ctype_id').attr('value');
        var element = $("#" + divId + " #mappingLevel");
        $("#" + divId + " #mappingLevel_List input[type='checkbox']").each(function() {
            if ($(this).is(":checked")) {
                selected_mapping.push($(this).attr('value'));
            }
        });
        if (selected_mapping.length == 0 && showCorpCardsTab) {
            var isMobile = $(window).width() < 768 ? true : false;
            Toast.show("Select atleast one mapping level", element, undefined, function() {}, isMobile);
            return false;
        } else {
            return true;
        }
    },

    postForm: function(saveInfo, isUpdatingDuplicate) {
        var addCardURL = '';
        var message;
        $('#add_card_random_uuid').val(uuidv4());
        if (saveInfo['target'] === "ADD") {
            message = "Your card has been successfully tokenized.";
        } else {
            message = "Your card has been successfully updated.";
        }
        if (saveInfo && !isUpdatingDuplicate) {
            var formId = saveInfo['formId'];
            var postData = $('#' + saveInfo.formId).serializeArray();
            addCardURL = $('#' + saveInfo.formId).attr("action");

            postData.push({
                "name": "target",
                "value": saveInfo.target
            });
            postData.push({
                "name": "cardId",
                "value": saveInfo.cardId
            });

            if (saveInfo.qbType.toUpperCase() == 'CORPORATE') {
                postData.push({
                    "name": "productGroup",
                    "value": JSON.stringify(CorporateQB.getProductList(saveInfo.formId))
                });
                postData.push({
                    "name": "levelGroup",
                    "value": JSON.stringify(CorporateQB.getSelectedLevelData(saveInfo.formId))
                });
                postData.push({
                    "name": "savedAtCorpLevel",
                    "value": CorporateQB.checkCompanyLevel(saveInfo.formId)
                })
            }
        } else if (isUpdatingDuplicate) {
            postData = CorporateQB.getUpdateDataOnDuplicateAdd();
            addCardURL = "/PaySwift/corporate-quickbook/save-card.htm";
        }
        if (saveInfo.target == "UPDATE") {
            postData.push({
                "name": "qbType",
                "value": saveInfo.qbType
            });
        }
        if (selectedEAUser && Object.keys(selectedEAUser).length > 0) {
            postData.push({
                 "name": "targetUserId",
                 "value": selectedEAUser.value
            });
        };
		if(CorporateQB.isSbt()){
			postData.push({
				"name": "bookingType",
				"value": "OFFICIAL"
			});
		}

        $("#loading-popup-id").modal('show');
        $.ajax({
            url: addCardURL,
            type: 'POST',
            data: postData,
            cache: false,
            success: function(response) {
				this.ctaCardData = postData;
                $("#loading-popup-id").modal('hide');
                var responseJSON = CorporateQB.convertToJsonObject(response);
                if (CorporateQB.isValid(responseJSON) && responseJSON.status == "SUCCESS") {
					if (saveInfo.target != "UPDATE") {
						var doTokenization = (eval($('#qb_cta_tab_id').hasClass('btn-selected')) || eval($('#qb_pcc_tab_id').hasClass('btn-selected')));
						if (doTokenization) {
							this.ctaCardData.push({
								"name": "genCardId",
								"value": responseJSON.uniqueCID
							});

							$("#update-popup-id").find(".content").html("<p><h2>" + CorporateQB.tokenInfoTitle + "</h2></p><p>" + CorporateQB.tokenInfoContent + "</p>")
							$("#update-popup-id").modal('show');
							CorporateQB.ctaTokenFormSubmit(this.ctaCardData);
						}
					}
					if (selectedEAUser && Object.keys(selectedEAUser).length > 0) {
                        CorporateQB.getSavedCardsByUserId(selectedEAUser.value)
                    }else{
                        CorporateQB.getSavedCards();
                    }

                    CorporateQB.showSavedCardsDiv(false);
                    if ($('#quickBookErrSucc').length > 0) {
                        $('#quickBookErrSucc').remove();
                    }
                    if (responseJSON.error_code == "109") {
                        $('#saved-cards-error').append('<div class="ajaxErrorSuccess col-xs-12" id="quickBookErrSucc" style="background-color: #fff3bd;padding: 10px 15px;margin-bottom: 20px;color: #dc3201;"><i class="fa fa-tick-r"></i>Your card has been successfully added but Sorry, we could not add your card for processing offline bookings at this moment, please try again later.</div>')
                        window.scrollTo($('#quickBookErrSucc').offset().left, $('#quickBookErrSucc').offset().top);

                    } else {
                        $('#saved-cards-error').append('<div class="ajaxErrorSuccess col-xs-12" id="quickBookErrSucc" style="background-color: #fff3bd;padding: 10px 15px;margin-bottom: 20px;color: #dc3201;"><i class="fa fa-tick-r"></i>' + message + '</div>')
                        window.scrollTo($('#quickBookErrSucc').offset().left, $('#quickBookErrSucc').offset().top);

                    }

                } else if (CorporateQB.isValid(responseJSON) && responseJSON.status == "FAILURE" && CorporateQB.isValid(responseJSON.error_code) && responseJSON.error_code == "AUTH_REQUIRED") {
                    CorporateQB.showAuthPage();

                } else if (CorporateQB.isValid(responseJSON) && responseJSON.status == "FAILURE" && CorporateQB.isValid(responseJSON.error_code) && responseJSON.error_code == "108") {
                    if (saveInfo.qbType.toUpperCase() == 'CORPORATE') {
                        $("#non-personal-update-message").show();
                    } else {
                        $("#non-personal-update-message").hide();
                    }

                    $("#update-popup-id").modal('show');
                } else {
                    $('#delete-conform-popup-id .content p').empty();
                    var msg = (responseJSON.error_msg) ? responseJSON.error_msg : "Error while adding quickbook card";
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

    isPasswordCompromised: function() {
        var passCompromisedURL = CorporateQB.getPassCompromisedURL();

        $.ajax({

            url: passCompromisedURL,
            timeout: (5 * 1000),
            type: 'POST',
            cache: false,

            success: function(response) {
                var responseJSON = CorporateQB.convertToJsonObject(response);

                // Password is NOT compromised when status (pass compromised status) is FAILURE and there is no error code.
                if (CorporateQB.isValid(responseJSON) && responseJSON.status == "FAILURE" && CorporateQB.isInvalid(responseJSON.error_code)) {
                    CorporateQB.getSavedCards();
                    CorporateQB.showQBHomeDiv();

                } else {
                    CorporateQB.showResetPassDiv();
                }
            },

            error: function(request, status, error) {
                alert("Error while determining if user is eligible for Quick Book.");
                CorporateQB.showResetPassDiv();
            }
        });
    },

    mappingLevelFunctionality: function(divId) {
        $(document).click(function(e) {
            e.stopPropagation();
            if (divId === "edit-cards") {
                var product_dropdown = document.getElementsByClassName("dropdown-menu-product")[1];
            } else {
                var product_dropdown = document.getElementsByClassName("dropdown-menu-product")[0];
            }
            if (e.target == product_dropdown || product_dropdown.contains(e.target) || e.target.id == "product") {
                return;
            }
            $("#" + divId + " #productlist").hide();
            if (divId === "edit-cards") {
                var mapping_dropdown = document.getElementsByClassName("dropdown-menu-mapping")[1];
            } else {
                var mapping_dropdown = document.getElementsByClassName("dropdown-menu-mapping")[0];
            }
            if (e.target == mapping_dropdown || mapping_dropdown.contains(e.target) || e.target.id == "mappingLevel") {
                return;
            }
            $("#" + divId + " #mappingLevel_List").hide();
        });
        $("#" + divId + " #product").click(function() {
            $("#" + divId + " #productlist").toggle();
            $("#" + divId + " #mappingLevel_List").hide();
        });
        $(".dropdown-menu-product input[type='checkbox']").prop("checked", "checked");
        $(".dropdown-menu-product .checkbox").click(function() {
            var textDisplay = [];
            $(this).closest("ul.dropdown-menu-product").find("input[type='checkbox']").each(function() {
                if ($(this).is(":checked")) {
                    textDisplay.push($(this).attr('name'));
                }

            });
            $(".multiselect_product").text(textDisplay);
            if (textDisplay.length == 0) {
                $(".multiselect_product").text("Select Product");
            }
        });
        $("#" + divId + " .multiselect_mapping").click(function() {
            $("#" + divId + " #mappingLevel_List").toggle();
            $("#" + divId + " #productlist").hide();
        });
        $("#" + divId + " .dropdown-menu-mapping input[value='companyLevel']").prop("checked", "checked");
        $("#" + divId + " .dropdown-menu-mapping input[type='checkbox']").click(function() {
            var mappingLevel = $(this).attr('value');
            $("#" + divId + " ." + mappingLevel).toggle();
            $("#no_begroup_id,#no_be_id,#no_user_id").hide();
            $("#input_be_group,#input_be,#input_user").val("");

        });
        $(".dropdown-menu-mapping .checkbox").click(function() {
            var textDisplay = [];
            $(this).closest("ul.dropdown-menu-mapping").find("input[type='checkbox']").each(function() {
                if ($(this).is(":checked")) {
                    textDisplay.push($(this).attr('name'));
                }

            });
            $(".multiselect_mapping").text(textDisplay);
            if (textDisplay.length == 0) {
                $(".multiselect_mapping").text("Select Mapping Level");
            }
        });
    },

     setSelectedLevelEAUserData: function(data) {
       selectedEAUser = data;
     },

    corporateAutosuggestEAUser: function() {

     const divId = 'corp-qb-home';

     function appendToUl(parentId, val) {
         $("." + parentId).append('<li id=' + "li_" + val.value + '>' + val.label + '<span data-value=' + val.value + ' class="close-be-list-item" id=' + "span_" + val.value + '>&times</span></li>');
     }

     function removeFromUl(parentId) {
         $('#' + divId + ' #ea-user-values-container').hide();
         $('.' + parentId).empty();
         selectedEAUser = null
     }

        var eleUser = $('#' + divId + ' #input_user');
        var tempSource = [];

        eleUser.autocomplete({
                   appendTo: $('#' + divId + ' #user-container'),
                   minLength: 1,
                   messages: {
                       noResults: '',
                       results: function() {}
                   },
                   source: function(request, response) {
                       var url = "/falcon/autosuggest/api?searchPhrase=" + request.term + "&component=USER";
                       var searchData = '';
                       var type = "GET";
                       $.when(CorporateQB.getAutosuggestData(searchData, url, type)).then(function(resp) {
                           if (resp.status && resp.status == 200 && resp.responseText) {
                               var restext = JSON.parse(resp.responseText);
                               if (restext.responseStatus && restext.responseStatus.code == 200 && restext.searchResult) {
                                   var showRes = $.map(restext.searchResult, function(item) {
                                       var name = "";
                                       var title = (item.title) ? item.title + ". " : "";
                                       var firstname = (item.firstName) ? item.firstName + " " : "";
                                       var middlename = (item.middleName) ? item.middleName + " " : "";
                                       var lastname = (item.lastName) ? item.lastName + " " : "";
                                       name = title + firstname + middlename + lastname;
                                       return {
                                           label: name,
                                           value: item.userId
                                       }
                                   });
                                   if (showRes.length) {
                                       $("#" + divId + " #no_user_id").hide();
                                   } else {
                                       $("#" + divId + " #no_user_id").show();
                                   }
                                   response(showRes);

                               } else {
                                   $("#" + divId + " #no_user_id").show();
                               }
                           }

                       }, function(resp) {
                           if (resp.status && resp.status == 200 && resp.responseText) {
                               var restext = JSON.parse(resp.responseText);
                               if (restext.responseStatus && restext.responseStatus.code == 200 && restext.searchResult) {
                                   var showRes = $.map(restext.searchResult, function(item) {
                                       var name = "";
                                       var title = (item.title) ? item.title + ". " : "";
                                       var firstname = (item.firstName) ? item.firstName + " " : "";
                                       var middlename = (item.middleName) ? item.middleName + " " : "";
                                       var lastname = (item.lastName) ? item.lastName + " " : "";
                                       name = title + firstname + middlename + lastname;
                                       return {
                                           label: name,
                                           value: item.userId
                                       }
                                   });
                                   if (showRes.length) {
                                       $("#" + divId + " #no_user_id").hide();
                                   } else {
                                       $("#" + divId + " #no_user_id").show();
                                   }
                                   response(showRes);


                               } else {
                                   $("#" + divId + " #no_user_id").show();
                               }
                           }

                       });
                   },
                   select: function(event, ui) {
                      event.preventDefault();
                      removeFromUl('ea-user-list-container');
                      $('#' + divId + ' #ea-user-values-container').show();
                      appendToUl('ea-user-list-container', ui.item);
                      CorporateQB.getSavedCardsByUserId(ui.item.value);
                      eleUser.val("");
                      CorporateQB.setSelectedLevelEAUserData(ui.item);
                   },
                   focus: function(e, ui) {
                       e.preventDefault();
                   }
               }).data('ui-autocomplete')._renderItem = function(ul, item) {
            ul.addClass("autosuggest-dropdown-container");
            return $("<li>")
                .attr("data-value", item.label)
                .append($("<a>").text(item.label))
                .appendTo(ul);
            }
            $('#' + divId + ' #ea-user-list-container').on('click', function (event) {
                 var targetTag = event.target.tagName.toLowerCase();

                 if (targetTag == "span") {
                     CorporateQB.getSavedCards();
                    removeFromUl('ea-user-list-container');
                 }
            });
    },

    corporateAutosuggest: function(divId) {
        function appendToUl(parentId, val) {
            $("." + parentId).append('<li id=' + "li_" + val.value + '>' + val.label + '<span data-value=' + val.value + ' class="close-be-list-item" id=' + "span_" + val.value + '>&times</span></li>');
        }

        function removeFromUl(parentId, val) {

            if (parentId && val)
                $('.' + parentId + '> #li_' + val).remove();
            else if (parent) {
                $('.' + parentId).empty();
            }

        }
         var eleBEGroup = $('#' + divId + ' #input_be_group');
         var eleBEntity = $('#' + divId + ' #input_be');
         var eleUser = $('#' + divId + ' #input_user');
         var tempSource = [];

        eleBEGroup.autocomplete({
            appendTo: $('#' + divId + ' #be-group-container'),
            minLength: 0,
            messages: {
                noResults: '',
                results: function() {}
            },

            source: function(request, callback) {
                var url = "/PaySwift/corporate-quickbook/get-entity-groups.htm";
                var searchData = '';
                var type = "GET";
                if (tempSource.length) {
                    var showRes = $.map(tempSource, function(item) {

                        if (item.toLowerCase().indexOf(request.term.toLowerCase()) > -1)
                            return item;
                    });
                    if (showRes.length) {
                        $("#" + divId + " #no_begroup_id").hide();
                    } else {
                        $("#" + divId + " #no_begroup_id").show();
                    }
                    callback(showRes);

                } else {
                    $.when(CorporateQB.getAutosuggestData(searchData, url, type)).then(function(resp) {
                        if (resp.status && resp.status == 200 && resp.responseText) {
                            var restext = JSON.parse(resp.responseText);
                            if (restext.status && restext.status == "SUCCESS" && restext.groupCodes) {
                                tempSource = Array.from(restext.groupCodes);
                                var showRes = $.map(tempSource, function(item) {

                                    if (item.toLowerCase().indexOf(request.term.toLowerCase()) > -1)
                                        return item;
                                });
                                if (showRes.length) {
                                    $("#" + divId + " #no_begroup_id").hide();
                                } else {
                                    $("#" + divId + " #no_begroup_id").show();
                                }
                                callback(showRes);

                            }
                        }
                    }, function(resp) {
                        if (resp.status && resp.status == 200 && resp.responseText) {
                            var restext = JSON.parse(resp.responseText);
                            if (restext.status && restext.status == "SUCCESS" && restext.groupCodes) {
                                tempSource = Array.from(restext.groupCodes);
                                var showRes = $.map(tempSource, function(item) {

                                    if (item.toLowerCase().indexOf(request.term.toLowerCase()) > -1)
                                        return item;
                                });
                                if (showRes.length) {
                                    $("#" + divId + " #no_begroup_id").hide();
                                } else {
                                    $("#" + divId + " #no_begroup_id").show();
                                }
                                callback(showRes);

                            }
                        }
                    });
                }

            }

        }).data('ui-autocomplete')._renderItem = function(ul, item) {
            ul.addClass("autosuggest-dropdown-container");
            var checked = CorporateQB.existsSelectedLevelData('entityGroup', item.value) ? "checked" : "";
            var item = $("<li>")
                .append('<div class="checkbox-custom"><input class=' + "be_group" + item.value + ' type="checkbox"' + checked + '><label data-value=' + JSON.stringify(item) + ' for=' + item.value + '> </label></div>')
                .append($("<a>").text(item.label).attr("data-value", JSON.stringify(item)));
            return item.appendTo(ul)
        };
        eleBEGroup.autocomplete().data("uiAutocomplete").close = function(e) {
            if (divId == 'edit-cards') {
                var ele = document.getElementsByClassName('ui-autocomplete')[4];
            } else {
                var ele = document.getElementsByClassName('ui-autocomplete')[0];
            }
            eleBEGroup.val("");
            if (e.target !== ele && !ele.contains(e.target))
                clearTimeout(this.closing), this.menu.element.is(":visible") && (this.menu.element.hide(), this._trigger("close", e));
            else
                return false;
        };
        eleBEGroup.bind('focus', function() {
            $(this).data("uiAutocomplete").search("");
        });
        $('#' + divId + ' #be-group-container > ul').on('click', function(event) {
            var targetTag = event.target.tagName.toLowerCase();
            if (targetTag == "a" || targetTag == "label") {
                var val = JSON.parse($(event.target).attr('data-value'));
                if ($('.be_group' + val.value + ':checked').length > 0) {
                    //remove from level data
                    removeFromUl('be-group-list-container', val.value);
                    CorporateQB.removeSelectedLevelData('entityGroup', val.value);
                    $('.be_group' + val.value).prop('checked', false);
                    if (CorporateQB.checkEmptySelectedLevelData('entityGroup')) {
                        $('#' + divId + ' #be-group-values-container').hide();
                    }
                } else {
                    //push into level data
                    appendToUl('be-group-list-container', val);
                    CorporateQB.setSelectedLevelData('entityGroup', val);
                    if (!CorporateQB.checkEmptySelectedLevelData('entityGroup')) {
                        $('#' + divId + ' #be-group-values-container').show();
                    }
                    $('.be_group' + val.label).prop('checked', true);
                }
            }
        });

        eleBEntity.autocomplete({
            appendTo: $('#' + divId + ' #bentity-container'),
            minLength: 1,
            messages: {
                noResults: '',
                results: function() {}
            },
            source: function(req, callback) {
                var url = '/falcon/autosuggest/corporate';
                var type = "POST";
                var searchData = '{"query":{"searchPhrase":' + JSON.stringify(req.term) + ',"component":"billingEntity","count":50},"filters":[]}';
                $.when(CorporateQB.getAutosuggestData(searchData, url, type)).then(function(resp) {
                    if (resp.status && resp.status == 200 && resp.responseText) {
                        var restext = JSON.parse(resp.responseText);
                        if (restext.responseStatus && restext.responseStatus.code == 200 && restext.values) {
                            var showRes = $.map(restext.values, function(item) {
                                var labelDetail = item.paramValue + ((item.stateName) ? "\n" + item.stateName.toUpperCase() : "");
                                return {
                                    label: labelDetail,
                                    value: item.id
                                }
                            });
                            if (showRes.length) {
                                $("#" + divId + " #no_be_id").hide();
                            } else {
                                $("#" + divId + " #no_be_id").show();
                            }
                            callback(showRes);
                        } else {
                            $("#" + divId + " #no_be_id").show();
                        }
                    }
                }, function(resp) {

                    if (resp.status && resp.status == 200 && resp.responseText) {
                        var restext = JSON.parse(resp.responseText);
                        if (restext.responseStatus && restext.responseStatus.code == 200 && restext.values) {
                            var showRes = $.map(restext.values, function(item) {
                                var labelDetail = item.paramValue + ((item.stateName) ? "\n" + item.stateName.toUpperCase() : "");
                                return {
                                    label: labelDetail,
                                    value: item.id
                                }
                            });
                            if (showRes.length) {
                                $("#" + divId + " #no_be_id").hide();
                            } else {
                                $("#" + divId + " #no_be_id").show();
                            }
                            callback(showRes);
                        } else {
                            $("#" + divId + " #no_be_id").show();
                        }
                    }

                });
            },
            select: function(event, ui) {
                event.preventDefault();
                if (!CorporateQB.existsSelectedLevelData('entity', ui.item.value)) {
                    CorporateQB.setSelectedLevelData('entity', ui.item);
                    if (!CorporateQB.checkEmptySelectedLevelData('entity')) {
                        $('#' + divId + ' #entity-values-container').show();
                    }
                    appendToUl('be-list-container', ui.item);
                }
                eleBEntity.val("");
            },
            focus: function(e, ui) {
                e.preventDefault();
            }
        }).data('ui-autocomplete')._renderItem = function(ul, item) {
            ul.addClass("autosuggest-dropdown-container");
            return $("<li>")
                .attr("data-value", item.value)
                .append($("<a>").text(item.label))
                .appendTo(ul);
        };

        eleUser.autocomplete({
            appendTo: $('#' + divId + ' #user-container'),
            minLength: 1,
            messages: {
                noResults: '',
                results: function() {}
            },
            source: function(request, response) {
                var url = "/falcon/autosuggest/api?searchPhrase=" + request.term + "&component=USER";
                var searchData = '';
                var type = "GET";
                $.when(CorporateQB.getAutosuggestData(searchData, url, type)).then(function(resp) {
                    if (resp.status && resp.status == 200 && resp.responseText) {
                        var restext = JSON.parse(resp.responseText);
                        if (restext.responseStatus && restext.responseStatus.code == 200 && restext.searchResult) {
                            var showRes = $.map(restext.searchResult, function(item) {
                                var name = "";
                                var title = (item.title) ? item.title + ". " : "";
                                var firstname = (item.firstName) ? item.firstName + " " : "";
                                var middlename = (item.middleName) ? item.middleName + " " : "";
                                var lastname = (item.lastName) ? item.lastName + " " : "";
                                name = title + firstname + middlename + lastname;
                                return {
                                    label: name,
                                    value: item.userId
                                }
                            });
                            if (showRes.length) {
                                $("#" + divId + " #no_user_id").hide();
                            } else {
                                $("#" + divId + " #no_user_id").show();
                            }
                            response(showRes);

                        } else {
                            $("#" + divId + " #no_user_id").show();
                        }
                    }

                }, function(resp) {
                    if (resp.status && resp.status == 200 && resp.responseText) {
                        var restext = JSON.parse(resp.responseText);
                        if (restext.responseStatus && restext.responseStatus.code == 200 && restext.searchResult) {
                            var showRes = $.map(restext.searchResult, function(item) {
                                var name = "";
                                var title = (item.title) ? item.title + ". " : "";
                                var firstname = (item.firstName) ? item.firstName + " " : "";
                                var middlename = (item.middleName) ? item.middleName + " " : "";
                                var lastname = (item.lastName) ? item.lastName + " " : "";
                                name = title + firstname + middlename + lastname;
                                return {
                                    label: name,
                                    value: item.userId
                                }
                            });
                            if (showRes.length) {
                                $("#" + divId + " #no_user_id").hide();
                            } else {
                                $("#" + divId + " #no_user_id").show();
                            }
                            response(showRes);


                        } else {
                            $("#" + divId + " #no_user_id").show();
                        }
                    }

                });
            },
            select: function(event, ui) {
                event.preventDefault();
                if (!CorporateQB.existsSelectedLevelData('user', ui.item.value)) {
                    CorporateQB.setSelectedLevelData('user', ui.item);
                    if (!CorporateQB.checkEmptySelectedLevelData('user')) {
                        $('#' + divId + ' #user-values-container').show();
                    }
                    appendToUl('user-list-container', ui.item);
                }
                eleUser.val("");
            },
            focus: function(e, ui) {
                e.preventDefault();
            }
        }).data('ui-autocomplete')._renderItem = function(ul, item) {
            ul.addClass("autosuggest-dropdown-container");
            return $("<li>")
                .attr("data-value", item.label)
                .append($("<a>").text(item.label))
                .appendTo(ul);
        };


        $('#' + divId + ' #be-group-list-container').on('click', function(event) {
            var targetTag = event.target.tagName.toLowerCase();
            if (targetTag == "span") {
                var val = $(event.target).attr('data-value');
                removeFromUl('be-group-list-container', val);
                CorporateQB.removeSelectedLevelData('entityGroup', val);
                $('.be_group' + val).prop('checked', false);
                if (CorporateQB.checkEmptySelectedLevelData('entityGroup')) {
                    $('#' + divId + ' #be-group-values-container').hide();
                }
            }
        });
        $('#' + divId + ' #be-list-container').on('click', function(event) {
            var targetTag = event.target.tagName.toLowerCase();
            if (targetTag == "span") {
                var val = $(event.target).attr('data-value');
                removeFromUl('be-list-container', val);
                CorporateQB.removeSelectedLevelData('entity', val);
                if (CorporateQB.checkEmptySelectedLevelData('entity')) {
                    $('#' + divId + ' #entity-values-container').hide();
                }
            }
        });
        $('#' + divId + ' #user-list-container').on('click', function(event) {
            var targetTag = event.target.tagName.toLowerCase();
            if (targetTag == "span") {
                var val = $(event.target).attr('data-value');
                removeFromUl('user-list-container', val);
                CorporateQB.removeSelectedLevelData('user', val);
                if (CorporateQB.checkEmptySelectedLevelData('user')) {
                    $('#' + divId + ' #user-values-container').hide();
                }
            }
        });
    },

    getPassCompromisedURL: function() {
        return "/PaySwift/is-pass-compromised.htm";
    },

    registerEvents: function() {
        $('.show-corp-qb-home').on('click', function() {
            CorporateQB.showQBHomeDiv();
        });

        $('.js-card-type').on('click', 'button', function(e) {
            if (e.target.id == "qb_pcc_tab_id"){
                   isPCC=true;
                    $(".hideForPcc").hide();
			$("#pccOfficialBookingMsg").show();
				if(!showCorpCardsTab) {
					$("#mappingDropdown").hide();
				}
			 }else{
			   isPCC=false;
                if (selectedEAUser && Object.keys(selectedEAUser).length > 0) {
                   $("#mappingDropdown").hide();
                } else {
                   $("#mappingDropdown, .hideForPcc").show();
                }
				$("#pccOfficialBookingMsg").hide();
			 }
			 if (e.target.id == "qb_cta_tab_id" || e.target.id == "qb_pcc_tab_id") {
				 $("#corpUserConsent").html("<input type='checkbox' name='saveTokenConsent' id='saveTokenConsent' onclick='return false;' onkeydown='return false;' checked value='true' />")
				 $('.ccsc-label-box, #saveTokenConsentDiv').show();
			 } else {
				 $("#corpUserConsent").html("");
				 $('.ccsc-label-box, #saveTokenConsentDiv').hide();
			 }

			 $(this).parent().find('.btn-selected').removeClass('btn-selected');
			 $(this).addClass('btn-selected');
			 CorporateQB.resetAddCardDiv(false);

			 if(e.target.id == "qb_pcc_tab_id"){
				 $(".hideList,#corp_qb_bookingType").hide();
				 $(".multiselect_mapping").text("");
				 $(".multiselect_mapping").text("Select Mapping Level");
				 $("#mappingLevel_List input[value='companyLevel']").prop("checked", false);
				 $("#corp_qb_bookingType_level_asuggest,#productDropdown").show();
			 }else{
				 $(".hideList").show();
				 $(".multiselect_mapping").text("");
				 $(".multiselect_mapping").text("Company level");
				 $("#mappingLevel_List input[value='companyLevel']").prop("checked", true);
			 }

			 if (e.target.id == "qb_personal_tab_id") {
				 // if this flag is true then only show checkbox for personal cards
				 if (btaInPersonalEnabled)
					 $("#btaInPersonalCheckBoxContainer").hide();
				 else
					 $("#btaInPersonalCheckBoxContainer").hide();
			 } else
				 $("#btaInPersonalCheckBoxContainer").hide();
        });

        $('input:radio[name="address_check_rdo"]').on('change', function() {
            var cardIssueType = $(this).val();
            if (cardIssueType == "dom") {
                CorporateQB.hideBillingAddDiv();
            } else {
                CorporateQB.showBillingAddDiv();
            }
        });

        $('.custom-Radiobox').on('click', function() {
            var radioInput = $(this).find('input[type="radio"]');
            var isChecked = radioInput.prop('checked');
            radioInput.prop('checked', !isChecked);
            var bookingType = radioInput.val();
            if ((bookingType == "OFFICIAL" || bookingType == "ALL") && canSaveCardForOffline) {
                $("#allowOfflineCheckBoxContainer").show();
            } else {
                $("#allowOfflineCheckBoxContainer").hide();
            }
        });

        $('input:radio[name="bookingType"]').on('change', function() {
            var bookingType = $(this).val();
            if ((bookingType == "OFFICIAL" || bookingType == "ALL") && canSaveCardForOffline) {
                $("#allowOfflineCheckBoxContainer").show();
            } else {
                $("#allowOfflineCheckBoxContainer").hide();
            }
        });

        $('input:checkbox[name="saveAsCorpCard"]').on('click', function(e) {
            $('input:checkbox[name="saveAsCorpCard"]').attr('value', $('input:checkbox[name="saveAsCorpCard"]').prop('checked'));

        });

        $('input:checkbox[name="saveCardToGDS"]').on('click', function(e) {
            e.preventDefault();
            if (this.checked) {
                $("#savecardforOfflinePopupTitle").html("Are you sure you want to allow this card for offline bookings");
                $("#savecardforOfflinePopupSubtitle").css("visibility", "visible");
                $("#save-card-offline-popup-id").modal('show');
            } else {

                $("#savecardforOfflinePopupTitle").html("Are you sure you want to remove this card for offline bookings");
                $("#savecardforOfflinePopupSubtitle").css("visibility", "hidden");
                $("#save-card-offline-popup-id").modal('show');
            }

        });

        $('.save_card_offline').on('click', 'button', function(e) {
            $("#save-card-offline-popup-id").modal('hide');
            var confirm = $(this).attr("confirm");
            if (confirm == "yes") {
                $('input:checkbox[name="saveCardToGDS"]').prop('checked', !$('input:checkbox[name="saveCardToGDS"]').prop('checked'));
                $('input:checkbox[name="saveCardToGDS"]').attr('value', $('input:checkbox[name="saveCardToGDS"]').prop('checked'));
            } else {
                //untick the checkbox
            }
        });

        $('.update_bts').on('click', 'button', function(e) {
            $("#update-popup-id").modal('hide');
        });

        $('.delete_bts').on('click', 'button', function(e) {
            $("#delete-popup-id").modal('hide');
            var confirm = $(this).attr("confirm");
            if (confirm == "yes") {
                $("#loading-popup-id").modal('show');
                $.when(CorporateQB.deleteQBCard(currentCardId, currentQBType, "yatra", "mybookings", currentisGDSCard)).then(function(res) {
                    $("#loading-popup-id").modal('hide');
                    if (res == "success") {

                        $(currentCardUL).remove();
                        if (noOfCards == 1) {
                            cardsSaved = false;
                            CorporateQB.getNoCardsDiv();
                        }
                        noOfCards--;
                        CorporateQB.setViewCardBtnLabel();
                        currentCardId = '';
                        currentQBType = '';
                        currentCardUL = '';
                        currentisGDSCard = '';
                        currentisSaveAsCorpCard = '';

                        $('#delete-conform-popup-id .content p').empty();
                        $('#delete-conform-popup-id .content p').text("We have deleted this card from profile");
                        $('#delete-conform-popup-id').modal('show');
                    }
                    if (res == "failure") {
                        $('#delete-conform-popup-id .content p').empty();
                        $('#delete-conform-popup-id .content p').text("Error while deleting quickbook card");
                        $('#delete-conform-popup-id').modal('show');
                    }
                }, function(res) {
                    $("#loading-popup-id").modal('hide');
                    if (res == "success") {

                        $(currentCardUL).remove();
                        if (noOfCards == 1) {
                            cardsSaved = false;
                            CorporateQB.getNoCardsDiv();
                        }
                        noOfCards--;
                        CorporateQB.setViewCardBtnLabel();
                        currentCardId = '';
                        currentQBType = '';
                        currentCardUL = '';
                        currentisGDSCard = '';
                        currentisSaveAsCorpCard = '';
                        $('#delete-conform-popup-id .content p').empty();
                        $('#delete-conform-popup-id .content p').text("We have deleted this card from profile");
                        $('#delete-conform-popup-id').modal('show');
                    }
                    if (res == "failure") {
                        $('#delete-conform-popup-id .content p').empty();
                        $('#delete-conform-popup-id .content p').text("Error while deleting quickbook card");
                        $('#delete-conform-popup-id').modal('show');
                    }
                });

            }
        });

        $("#qb_cno_id").on('keyup', function(e) {

            // Not allow spaces.
            CorporateQB.disableSpace($(this));

            // Only digits allowed on cardNumber input.
            CorporateQB.checkNaN($(this));

            // Get CardNumber value.
            var cardNo = "";
            cardNo = $(this).val();

            // Identify CardBrand.
            var cardBrand = CorporateQB.getCardBrand(cardNo);
            CorporateQB.setCardBrand(cardBrand);

            // Logic for fading.
            CorporateQB.fadeCardLogo(cardBrand);

            // Show Address div for amex cards
            CorporateQB.showAmexIssueDiv(cardBrand);

            // Show Expiry Msg for Maestro cards.
            CorporateQB.showMaestExpiryMsg(cardBrand);

            // Update validations for the cardType and cardBrand.
            CorporateQB.updateValidations(cardBrand);

            // Logic for displaying Billing Address
            CorporateQB.processInternationalCard(cardBrand, cardNo);

        });


        $("#qb_add_card_btn_id").on('click', function() {
            var isValid = PaymentValidation.validate('#corp_qb_add_card_form_id');
            saveInfo['formId'] = 'corp_qb_add_card_form_id';
            if (selectedEAUser && Object.keys(selectedEAUser).length > 0) {
                saveInfo['targetUserId'] = selectedEAUser.value;
            };
            var qbookingType = $('#corp_qb_qbtype_id').attr('value');
            var isValid_mapping = false;
            var isValid_product = false;
            if(CorporateQB.isSbt()){
				saveInfo['qbType'] = qbookingType;
				saveInfo['cardId'] = '';
				saveInfo['target'] = 'ADD';
				var data = {
							label: 'user',
							value: corpUserId
						};
				CorporateQB.setSelectedLevelData('user', data);
				CorporateQB.postForm(saveInfo);
			} else {
				if (qbookingType && qbookingType.toUpperCase() == 'CORPORATE') {
					var mappingLevel = CorporateQB.getMappingLevelList(saveInfo.formId);
					var checkMappingLevel = ['entityGroup', 'user', 'entity'];
					for (var k = 0; k < checkMappingLevel.length; k++) {
						if (mappingLevel.indexOf(checkMappingLevel[k]) < 0) {
							CorporateQB.removeSelectedLevelData(checkMappingLevel[k]);
						}
					}
					if (!CorporateQB.checkCompanyLevel(saveInfo.formId) && qbookingType.toUpperCase() == 'CORPORATE') {
						if (!CorporateQB.validateSelectedLevelData(saveInfo.formId)) {
							return false;
						}
					}
					isValid_mapping = CorporateQB.mappingValidation("product-div");
					isValid_product = CorporateQB.productValidation("product-div");
				}
				if ((isValid && (qbookingType.toUpperCase() == 'PERSONAL') || (isValid && (qbookingType.toUpperCase() == 'CORPORATE') && isValid_product && isValid_mapping))) {
					saveInfo['qbType'] = qbookingType;
					saveInfo['cardId'] = '';
					saveInfo['target'] = 'ADD';
					CorporateQB.postForm(saveInfo);
				}
			}
        });

        $("#qb_personal_tab_id").on('click', function() {
            CorporateQB.initializeSelectedLevelData();
            $('.be-group-list-container').empty();
            $('.be-list-container').empty();
            $('.user-list-container').empty();
            $('#corp_qb_bookingType').show();
            $("#product-div #productDropdown").hide();
            $("#product-div #mappingDropdown").hide();

        });

        $("#qb_corporate_tab_id, #qb_bta_tab_id, #qb_cta_tab_id, #qb_pcard_tab_id").on('click', function() {
            CorporateQB.initializeSelectedLevelData();
            $('.be-group-list-container').empty();
            $('.be-list-container').empty();
            $('.user-list-container').empty();
            $('#corp_qb_bookingType').hide();
            $("#product-div #productDropdown").show();
            $("#product-div #mappingDropdown").show();
        });

        $("#qb_reset_pass_btn_id").on('click', function() {
            var isValid = PaymentValidation.validate('#corp_qb_reset_pass_form_id');
            if (isValid) {
                $("#corp_qb_reset_pass_form_id").submit();
            }
        });

        $("#corp_qb_reset_pass_form_id").submit(function(e) {

            var postData = $(this).serializeArray();
            var resetPassURL = $(this).attr("action");

            $.ajax({

                url: resetPassURL,
                type: 'POST',
                data: postData,
                cache: false,

                success: function(response) {
                    var responseJSON = CorporateQB.convertToJsonObject(response);
                    if (CorporateQB.isValid(responseJSON) && responseJSON.status == "SUCCESS") {
                        CorporateQB.getNoCardsDiv();
                        CorporateQB.showQBHomeDiv();

                    } else if (CorporateQB.isValid(responseJSON) && responseJSON.status == "FAILURE" && CorporateQB.isValid(responseJSON.error_code) && responseJSON.error_code == "AUTH_REQUIRED") {
                        CorporateQB.showAuthPage();

                    } else {
                        CorporateQB.showResetPassErrorMsg();
                    }
                },
                error: function(request, status, error) {
                    alert("Error occurred while resetting password.");
                    CorporateQB.showResetPassErrorMsg();
                }
            });
            e.preventDefault(); // to stop default action.
        });

        //autocomplete code for corporate card level
        CorporateQB.corporateAutosuggestEAUser();

        CorporateQB.corporateAutosuggest("product-div");
    },
    initializeSelectedLevelData: function() {
        selectedLevelData = new Object();
        selectedLevelValue = new Object();
        selectedLevelData.entityGroup = [];
        selectedLevelData.entity = [];
        selectedLevelData.user = [];
        selectedLevelData.eaUser = {};
        selectedLevelValue.entityGroup = {};
        selectedLevelValue.entity = {};
        selectedLevelValue.user = {};
//        selectedLevelValue.eaUser = {};

    },
    getUpdateDataOnDuplicateAdd: function() {
        return updateDataOnDuplicateAdd;
    },
    setUpdateDataOnDuplicateAdd: function(data) {
        updateDataOnDuplicateAdd = data;
    },
    removeSelectedLevelData: function(level, data) {
        if (!level && !data) {
            CorporateQB.initializeSelectedLevelData();
        }
        if (level && !data) {
            selectedLevelData[level] = [];
            selectedLevelValue[level] = {};
        }
        if (level && data) {
            var index = selectedLevelData[level].indexOf(data);
            if (index > -1) {
                selectedLevelData[level].splice(index, 1);
                delete selectedLevelValue[data];
            }
        }
    },
    setSelectedLevelData: function(level, data) {
        if (!level || !data) {
            return;
        }
        selectedLevelData[level].push(data.value);
        selectedLevelValue[level][data.value] = data.label;
    },
    getSelectedLevelData: function(formId) {


        return selectedLevelData;

    },

    existsSelectedLevelData: function(level, data) {
        if (level) {
            return selectedLevelData[level].indexOf(data) > -1;
        }
    },
    checkEmptySelectedLevelData: function(level) {
        return selectedLevelData[level].length < 1;
    },
    validateSelectedLevelData: function(formId) {
        var mplevel = this.getMappingLevelList(formId);
        if (this.checkEmptySelectedLevelData('entityGroup') && (mplevel.indexOf('entityGroup') > -1)) {
            Toast.show("Please provide entity group", $("#" + formId + " #input_be_group"), undefined, function() {
                //$("#" + formId +" #input_be_group").focus();
                //$(window).scrollTo("#" + formId +" #input_be_group");

            }, false);
            $(window).scrollTop($("#" + formId + " #input_be_group").offset().top);
            return false;
        }
        if (this.checkEmptySelectedLevelData('entity') && (mplevel.indexOf('entity') > -1)) {
            Toast.show("Please provide  billing entity", $("#" + formId + " #input_be"), undefined, function() {

            }, false);
            $(window).scrollTop($("#" + formId + " #input_be").offset().top);
            return false;
        }
        if (this.checkEmptySelectedLevelData('user') && (mplevel.indexOf('user') > -1)) {
            Toast.show("Please provide user id", $("#" + formId + " #input_user"), undefined, function() {

            }, false);
            $(window).scrollTop($("#" + formId + " #input_user").offset().top);
            return false;
        }
        return true;
    },
    createProperPostDataInUpdate: function(postData) {
        if (!postData)
            return false;
        var paramsUp = ['ctype', 'qbType', 'cbrand', 'isCardInternational', 'merchant', 'csrfToken', 'product', 'cardholder_name', 'cno', 'cexpm', 'cexpy', 'cname', 'productGroup', 'ba1', 'bacy', 'bapi', 'bast', 'bacu', 'baisd', 'bamob'];
        var keys = [];
        for (var j = 0; j < postData.length; j++) {
            var key = postData[j]["name"];
            keys.push(key);
        }
        for (var i = 0; i < paramsUp.length; i++) {
            if (keys.indexOf(paramsUp[i]) < 0) {
                postData.push({
                    "name": paramsUp[i],
                    "value": ""
                });
            }
        }

        return postData;
    },
    getAutosuggestData: function(data, url, type) {
        var defer = $.Deferred();
        $.ajax({
            url: url,
            type: type,
            contentType: "application/json",
            data: data,
            dataType: "application/json",
            success: function(res) {
                defer.resolve(res);
            },
            error: function(res) {
                defer.resolve(res);
            }
        });
        return defer.promise();
    },
    getSavedCards: function() {
		$("#loading-popup-id").modal('show');
        var merchant = 'yatra';
        var product = 'mybookings';
        var saveCardURL = CorporateQB.getSavedCardsURL();
        $.ajax({

            url: saveCardURL,
            type: 'POST',
            data: {
                'merchant': merchant,
                'product': product
            },
            cache: false,
            success: function(response) {
                CorporateQB.processGetCardsResponse(response);
            },
            error: function(request, status, error) {
                alert("Error occurred while getting saved cards.");
                CorporateQB.getErrorCardsDiv();
				$("#loading-popup-id").modal('hide');
            }
        });
    },
    getSavedCardsByUserId: function(userId) {
    		$("#loading-popup-id").modal('show');
    		var merchant = 'yatra';
            var product = 'mybookings';
            var saveCardURL = CorporateQB.getSavedCardsURL();
            $.ajax({
                url: saveCardURL,
                type: 'POST',
                data: {
                    'targetUserId': userId,
                    'merchant': merchant,
                    'product': product
                },
                cache: false,
                success: function(response) {
                    CorporateQB.processGetCardsResponse(response);
                },
                error: function(request, status, error) {
                    alert("Error occurred while getting saved cards.");
                    CorporateQB.getErrorCardsDiv();
    				$("#loading-popup-id").modal('hide');
                }
            });
    },
	getCardList: function() {
		var data = null;
		var getCardList = CorporateQB.tokenCardListUrl;
        $.ajax({
			url: getCardList+"="+clientId,
			type: 'GET',
			cache: false,
			async : false,
			success: function(res) {
                if(res.corpTokenizedCards && res.corpTokenizedCards.length>0) {
                    data =  res.corpTokenizedCards;
                }
			},
			error: function(request, status, error) {
				alert("Error occurred while getting saved cards.");
				CorporateQB.getErrorCardsDiv();
			}
		});
		return data;
	},
    getSavedCardsURL: function() {
    //url changed for testing
        return "/PaySwift/corporate-quickbook/get-cards.htm";
    },
    processGetCardsResponse: function(qbJson) {
		var tokenCardList = CorporateQB.getCardList();

        // Initialize no of cards to 0.
        noOfCards = 0;

        // set isUserAuthenticated to true.
        isUserAuthenticated = true;

        // Convert JSON to JSONObject.
        var jsonObject = CorporateQB.convertToJsonObject(qbJson);

		for (var j=0;j<jsonObject.CORPORATE.length;j++) {
if( typeof tokenCardList !=="undefined" && tokenCardList ){
    for (var i=0;i<tokenCardList.length;i++) {
            if(tokenCardList[i].cardId === jsonObject.CORPORATE[j].cardId) {
                if(tokenCardList[i].tokenStatus === "INAC"){
                    jsonObject.CORPORATE[j].isTokenized = false;
                }else{
                    jsonObject.CORPORATE[j].isTokenized = true;
                }
            }
        }
}
		}
		$("#loading-popup-id").modal('hide');
        // If JSON Object is null, show error msg.
        if (CorporateQB.isInvalid(jsonObject) || 'SUCCESS' != jsonObject.status) {
            CorporateQB.getErrorCardsDiv();
            return;
        }

        if (jsonObject.authenticationRequired) {
            isUserAuthenticated = false;
            return;
        }

        // check if it is allowed to save card for offline
        if (jsonObject.allowOfflineBookings) {
            canSaveCardForOffline = true;
        }

        // check if it is allowed to save card for offline
        if (jsonObject.BTAINPERSONAL && jsonObject.BTAINPERSONAL.length >= 0) {
            btaInPersonalArray = jsonObject.BTAINPERSONAL;
            btaInPersonalEnabled = true;
        }


        if (CorporateQB.isEmptyCardsList(jsonObject)) {
            // If there are no cards saved, show no cards div.
            CorporateQB.getNoCardsDiv();
        } else {
            // Get saved cards and show them.
            CorporateQB.getSavedCardsDiv(jsonObject);
            if (cardsSaved) {
                CorporateQB.registerDeleteCardEvent();
                return;
            }
        }

    },

    isEmptyCardsList: function(jsonObject) {
        var isEmpty = true;
        for (var qbType in jsonObject) {
            if ($.isArray(jsonObject[qbType]) && jsonObject[qbType].length) {
                isEmpty = false;
                break;
            }
        }
        return isEmpty;
    },

    getEditCardsDiv: function(i, qbType) {
        var editCardsJson = savedCardsJson[qbType];
        if (savedCardsJson != null && savedCardsJson != undefined) {
            cardsSaved = true;
            var cardId = editCardsJson[i].cardId;
            var cardNumber = CorporateQB.getCardNumber(editCardsJson[i], qbType);
            var gdsFlag = editCardsJson[i].isGDSCard ? 'true' : 'false';
            var checkedFlag = editCardsJson[i].isGDSCard ? 'checked' : '';
            var cardHolderName;
            if (qbType != "PERSONAL") {
                cardHolderName = editCardsJson[i].firstName;
            } else {
                cardHolderName = editCardsJson[i].cardHolderFirstName;
            }

            var strCardReplace = cardNumber;
            strCardReplace = strCardReplace.replace(/^.{7}/g, 'xxxxxxx');
            var displayCardNumber = strCardReplace;

            var showCheckboxFlag = editCardsJson[i].bookingType && editCardsJson[i].bookingType == 'official' ? true : false;
            var savedCards = '<section class="col-md-12"><div class="row">';
            savedCards += '<div class="pull-left col-md-9 hidden-xs" style="margin-bottom:5px"><h3 class="mg-top mg-bottom heading-bold">QuickBook</h3><p class="hidden-xs">Start by adding credit/debit card. Its safe &amp; convenient!</p></div>';
            savedCards += '<div class="clearfix"></div>';
            savedCards += '<div class="container-saved-card">';
            savedCards += ' <div class="header-container"><p class="heading-bold">Update Card Mapping/Product Access</p></div><div class="content-saved-card">';
            //savedCards += '<p>'+ CorporateQB.getCardNumber(editCardsJson[i], qbType) +'</p>';
            savedCards += '<div class="card-row" style="margin-bottom:10px;border:0"><div class="card-picture">';
            savedCards += '<i class="' + CorporateQB.getCardLogo(editCardsJson[i].cardBrand) + '"></i></div>';
            savedCards += ' <div class="card-details"><div class="card-details-container"><div class="card-type-container"><div class="card-type-number-box"><div class="card-number-name">';
            savedCards += ' <div class="card-number-tag"><div class="card-number"><p class=" style="display:inline-block">' + displayCardNumber.toString().replace(/-/g, " ") + '</p></div>';
            savedCards += '<p  class="card-name">' + CorporateQB.getCardName(editCardsJson[i], qbType) + '</p>';
            savedCards += '</div></div></div></div></div></div>';
            savedCards += '<form name="corp_qb_update_card_form" id = "corp_qb_update_card_form_id" action="/PaySwift/corporate-quickbook/save-card.htm" class="yt-form" role="form">';
            if (qbType != "PERSONAL") {
                savedCards += '<div class="form-group" id="edit-cards">' + $('#product-div').html() + '</div>';
            } else {
                savedCards += '<div class="form-group" style="margin-bottom:55px" id="corp-personal-bookingType"></br><label>Save this card for</label> </br>';
                savedCards += '<div><span class="custom-Radiobox"><input type="radio" name="bookingType"   value="OFFICIAL"><span class="box"><b class="tick"></b></span></span> <span class="corp-qb-radio-text-edit"> Official Bookings </span>';
                savedCards += '<span class="custom-Radiobox"><input type="radio" name="bookingType"  value="PERSONAL" ><span class="box"><b class="tick"></b></span></span> <span class="corp-qb-radio-text-edit" > Personal Bookings </span>';
                savedCards += '<span class="custom-Radiobox"><input type="radio" name="bookingType"  id="bookingType_both" value="ALL" data-validation = "required" data-msginfo = "* Please choose what type of bookings (personal / official) can this card be used for"><span class="box"><b class="tick"></b></span></span><span class="corp-qb-radio-text-edit"> Both </span></div></div>';
            }
            savedCards += '<input type="hidden" name = "csrfToken" value=' + csrfToken + ' />';
            savedCards += '<input type="hidden" name = "cno" value=' + cardNumber + ' />';
            savedCards += '<input type="hidden" name = "isCardInternational" value="false" />';
            savedCards += '<input type="hidden" name = "cardholder_name" value=' + cardHolderName + ' />';
            savedCards += '<input type="hidden" name = "cexpm" value=' + editCardsJson[i].expiryMonth + ' />';
            savedCards += '<input type="hidden" name = "cexpy" value=' + editCardsJson[i].expiryYear + ' />';
            savedCards += '<input type="hidden" name = "cname" value=' + editCardsJson[i].cardName + ' />';
            savedCards += '<input type="hidden" name = "cbrand" value=' + editCardsJson[i].cardBrand + ' />';
            savedCards += '<input type="hidden" name = "ctype" value=' + editCardsJson[i].cardType + ' />';
            savedCards += '<input type="hidden" name="merchant" value="yatra"/>';
            savedCards += '<input type="hidden" name="product" value="mybookings"/>';
            if (selectedEAUser && Object.keys(selectedEAUser).length > 0) {
                savedCards += '<input type="hidden" name="targetUserId" value='+ selectedEAUser.value +'/>';
            };
            savedCards += '</form>';
            savedCards += '<div class="form-group row"><div class="col-md-2"><button type="button" class="update-btn" id = "qb_update_card_btn_id">Update</button></div>';
            savedCards += '<div class="col-md-2"><button type="button" class="cancel-btn" id = "qb_cancel_card_btn_id">Cancel</button></div></div>';
            savedCards += '</div><p class="text-note">Note: We store your card as per the latest RBI guidelines.</p></div>';
            savedCards += '<div class="footer-section"><div class="footer-logo"><span class="text-sm">Secured by: </span>';
            savedCards += '<i class="ico-gray-verisign"></i><i class="ico-gray-mastercard"></i><i class="ico-gray-safekey"></i><i class="ico-gray-pcidss"></i><i class="ico-gray-visa"></i>';
            savedCards += '</div></div>';
            savedCards += '</div></div></section>';
            $('#saved-edit-cards').html(savedCards);
            if (qbType === "PERSONAL") {
                var bookingtype = CorporateQB.getBookingType(editCardsJson[i].bookingType);
                $("#corp-personal-bookingType input[value=" + bookingtype + "]").prop("checked", "checked");
            }
            if (qbType != "PERSONAL") {
                CorporateQB.mappingLevelFunctionality("edit-cards");
                CorporateQB.corporateAutosuggest("edit-cards");
                CorporateQB.clearDetails();
                //code of multiselect dropdown in update
                $("#edit-cards .dropdown-menu-product input[type='checkbox']").prop("checked", false);
                var productList = editCardsJson[i].productGroup;
                var productNames = [];
                var mappingNames = [];
                if (productList[0] === "ALL") {
                    $("#edit-cards .dropdown-menu-product input[type='checkbox']").prop("checked", "checked");
                    productNames = ["Domestic Flight", "Domestic Hotel", "International Hotel", "International Flight", "Others"];
                             if(editCardsJson[i].cardType == "PCC") {
                                            isPCC = true;
                                            $(".hideForPcc").hide();
                                            productNames = ["Domestic Flight", "International Flight"];
                                        }else{
                                            isPCC = false;
                                            $(".hideForPcc").show();
                                        }

                } else {
                    if(editCardsJson[i].cardType == "PCC"){
                        isPCC = true;
                        $(".hideForPcc").hide();
                        productList= ["DOM_FLIGHT", "INT_FLIGHT"];
                    }
                    else{
                        isPCC = false;
                        $(".hideForPcc").show();
                        }
                    for (j = 0; j < productList.length; j++) {
                        productNames[j] = CorporateQB.getProductName(productList[j]);
                        $("#edit-cards .dropdown-menu-product input[value=" + productList[j] + "]").prop("checked", "checked");
                    }
                }
                $("#edit-cards .multiselect_product").text(productNames);
                $("#edit-cards .dropdown-menu-mapping input[type='checkbox']").prop("checked", false);
                var index = 0;
                for (obj in editCardsJson[i].levelGroup) {
                    if (editCardsJson[i].levelGroup[obj] === true) {
                        mappingNames = ["Company Level"];
                        $("#edit-cards .dropdown-menu-mapping input[value='companyLevel']").prop("checked", "checked");
                        $("#edit-cards .companyLevel").hide();
                    } else {
                        if (editCardsJson[i].levelGroup[obj].length > 0) {
                            mappingNames[index++] = CorporateQB.getMappingLevel(obj);
                            $("#edit-cards .dropdown-menu-mapping input[value=" + obj + "]").prop("checked", "checked");
                            $("#edit-cards ." + obj).show();
                            $("#edit-cards .companyLevel").show();
                            $("#no_begroup_id,#no_be_id,#no_user_id").hide();
                        }
                    }
                    if (obj === "entityGroup") {
                        var entityGroupArray = editCardsJson[i].levelGroup.entityGroup;
                    }
                    if (obj === "entity") {
                        var entityArray = editCardsJson[i].levelGroup.entity;
                    }
                    if (obj === "user") {
                        var userArray = editCardsJson[i].levelGroup.user;
                    }

                }
                $("#edit-cards .multiselect_mapping").text(mappingNames);

                function appendToUl(parentId, val) {
                    $("." + parentId).append('<li id=' + "li_" + val.value + '>' + val.label + '<span data-value=' + val.value + ' class="close-be-list-item" id=' + "span_" + val.value + '>&times</span></li>');
                }

                function removeFromUl(parentId, val) {
                    $('.' + parentId + '> #li_' + val).remove();
                }
                for (clearIndex = 0; clearIndex < selectedLevelData.entityGroup.length; clearIndex++) {
                    removeFromUl('be-group-list-container', selectedLevelData.entityGroup[clearIndex]);
                }
                for (clearIndex = 0; clearIndex < selectedLevelData.entity.length; clearIndex++) {
                    removeFromUl('be-list-container', selectedLevelData.entity[clearIndex]);
                }
                for (clearIndex = 0; clearIndex < selectedLevelData.user.length; clearIndex++) {
                    removeFromUl('user-list-container', selectedLevelData.user[clearIndex]);
                }
                CorporateQB.initializeSelectedLevelData();

                /*$('#edit-cards #be-group-values-container').hide();
                $('#edit-cards #entity-values-container').hide();
                $('#edit-cards #user-values-container').hide();*/

                if (entityGroupArray && entityGroupArray.length > 0) {
                    for (entityGroupIndex = 0; entityGroupIndex < entityGroupArray.length; entityGroupIndex++) {
                        var val = entityGroupArray[entityGroupIndex];

                        var data = {
                            label: val,
                            value: val
                        }
                        appendToUl('be-group-list-container', data);
                        CorporateQB.setSelectedLevelData('entityGroup', data);
                    }
                    if (!CorporateQB.checkEmptySelectedLevelData('entityGroup')) {
                        $('#edit-cards #be-group-values-container').show();
                    }
                }
                if (entityArray && entityArray.length > 0 && mappingLevelJson.entities) {
                    for (entityIndex = 0; entityIndex < entityArray.length; entityIndex++) {
                        var entity_id = entityArray[entityIndex];
                        var val = mappingLevelJson.entities[entity_id].name + ((mappingLevelJson.entities[entity_id].stateName) ? "\n" + mappingLevelJson.entities[entity_id].stateName.toUpperCase() : "");
                        var data = {
                            label: val,
                            value: entity_id
                        }
                        appendToUl('be-list-container', data);
                        CorporateQB.setSelectedLevelData('entity', data);

                    }
                    if (!CorporateQB.checkEmptySelectedLevelData('entity')) {
                        $('#edit-cards #entity-values-container').show();
                    }
                }

                function getUserName(userItem) {
                    var name = "";
                    var title = (userItem.title) ? userItem.title + "." : "";
                    var firstName = userItem.firstName ? userItem.firstName + " " : "";
                    var middleName = userItem.middleName ? userItem.middleName + " " : "";
                    var lastName = userItem.lastName ? userItem.lastName + " " : "";
                    name = title + firstName + middleName + lastName;
                    return name;
                }
                if (userArray && userArray.length > 0 && mappingLevelJson.users) {
                    for (userIndex = 0; userIndex < userArray.length; userIndex++) {
                        var user_id = userArray[userIndex];
                        var user_item = mappingLevelJson.users[user_id];
                        var val = getUserName(user_item);
                        var data = {
                            label: val,
                            value: user_id
                        }
                        appendToUl('user-list-container', data);
                        CorporateQB.setSelectedLevelData('user', data);
                    }
                    if (!CorporateQB.checkEmptySelectedLevelData('user')) {
                        $('#edit-cards #user-values-container').show();
                    }
                }


            }
            // code for radio btn

            $(".custom-Radiobox").click(function(event) {
                $(".custom-Radiobox").each(function() {
                    if (this == event.target || this.contains(event.target)) {
                        $(this).find("input[type='radio']").prop("checked", "checked");
                    }
                });

            });
            // code for autosuggest in update

            $("#qb_update_card_btn_id").on("click", function() {
                var isValid = PaymentValidation.validate('#corp_qb_update_card_form_id');
                var isValid_mapping = false;
                var isValid_product = false;
                if (qbType != 'PERSONAL') {
                    if (!CorporateQB.checkCompanyLevel("corp_qb_update_card_form_id")) {
                        if (!CorporateQB.validateSelectedLevelData("corp_qb_update_card_form_id")) {
                            return false;
                        }
                    }
                    var isValid_mapping = CorporateQB.mappingValidation("edit-cards");
                    var isValid_product = CorporateQB.productValidation("edit-cards");
                }
                if ((isValid && qbType === "PERSONAL") || (isValid && isValid_product && isValid_mapping && qbType === "CORPORATE")) {
                    saveInfo['qbType'] = qbType;
                    saveInfo['cardId'] = cardId;
                    saveInfo['target'] = 'UPDATE';
                    saveInfo['formId'] = 'corp_qb_update_card_form_id';
                    CorporateQB.postForm(saveInfo);
                }
            });

            $("#qb_cancel_card_btn_id").on("click", function() {

                $("#corp-saved-cards").show();
                $("#corp-edit-cards").hide();
            });
        }
        if (window.attachEvent && !window.addEventListener) {
            $('.col-xs-12').removeClass('col-xs-12');
            $('.col-xs-5').removeClass('col-xs-5');
        }
    },



    getSavedCardsDiv: function(qbJsonObject) {
        savedCardsJson = qbJsonObject;
        mappingLevelJson = qbJsonObject;
        if (savedCardsJson != null && savedCardsJson != undefined) {
            cardsSaved = true;
            var savedCards = '<section class="col-md-12"><div class="row" style="margin-bottom:10px">';
            savedCards += '<div class="pull-left col-md-9 hidden-xs"><h3 class="mg-top mg-bottom heading-bold">QuickBook</h3></div>';
            savedCards += '<div class="pull-right col-md-3 col-xs-12"><button class="yt-btn btn-block" id="savedAddCardBtn" type="button">';
            savedCards += '+  Add a card</button></div></div><div class="clearfix"></div>';
            savedCards += '<div class="container-saved-card">';
            savedCards += ' <div class="header-container"><p>your saved cards</p></div><div class="content-saved-card">';

            for (var qbType in savedCardsJson) {
                if ($.isArray(savedCardsJson[qbType]) && savedCardsJson[qbType].length && qbType != "BTAINPERSONAL") {
                    savedCards += CorporateQB.getSavedCardsListingHtml(savedCardsJson[qbType], qbType);
                }
            }
            savedCards += '</div></div></section>';
            $('#saved-cards').html(savedCards);
            CorporateQB.setViewCardBtnLabel();
            $('#savedAddCardBtn').on('click', function() {
                CorporateQB.showAddCardDiv();
            });

            $('input:checkbox[name="cardForOfflineBooking"]').on('click', function(e) {
                if ($('#quickBookErrSucc').length > 0) {
                    $('#quickBookErrSucc').remove();
                }
                currentCardId = $(this).parents(".card-details-container").find(".card-number").attr("card-id");
                currentQBType = $(this).parents(".card-details-container").find(".card-number").attr("qb-type");
                currentisGDSCard = $(this).parents(".card-details-container").find(".card-number").attr("isGDSCard")
                currentCardUL = $(this).parents(".card-row");
                lvCardSelectedOfflineId = this.id
                if (this.checked) {
                    $("#lv_savecardforOfflinePopupTitle").html("Are you sure you want to allow this card for processing offline bookings?");
                    if (jQuery('input:checkbox[name="cardForOfflineBooking"]:checked').length) {
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
                if (confirm == "yes" && $('#' + lvCardSelectedOfflineId).is(":checked")) {
                    // remove the offline card


                    CorporateQB.deleteGDSCard(currentCardId, currentQBType, "yatra", "mybookings");
                    return false;
                } else if (confirm == "yes" && !$('#' + lvCardSelectedOfflineId).is(":checked")) {
                    // add the card


                    // $('input:checkbox[name="cardForOfflineBooking"]').attr('checked',false);
                    // $('#'+lvCardSelectedOfflineId).prop('checked', true);
                    // $('#'+lvCardSelectedOfflineId).attr('value', $('#'+lvCardSelectedOfflineId).prop('checked'));
                    CorporateQB.saveGDSCard(currentCardId, currentQBType, "yatra", "mybookings");
                    return false;
                } else {

                }
            });

            // for bta in personal card
            $('input:checkbox[name="btaInPersonalSavedCard"]').on('click', function(e) {
                if ($('#quickBookErrSucc').length > 0) {
                    $('#quickBookErrSucc').remove();
                }
                currentCardId = $(this).parents(".card-details-container").find(".card-number").attr("card-id");
                currentQBType = $(this).parents(".card-details-container").find(".card-number").attr("qb-type");
                currentisGDSCard = $(this).parents(".card-details-container").find(".card-number").attr("isGDSCard")
                currentCardUL = $(this).parents(".card-row");
                currentisSaveAsCorpCard = $(this).parents(".card-details-container").find(".card-number").attr("isBTAInPersonal")
                lvCardSelectedOfflineId = this.id
                if (this.checked) {
                    $("#lv_btaInPersonalPopupTitle").html("Are you sure you want to allow this card for BTA booking?");

                    $("#lv_bta-in-personal-id").modal('show');
                } else {

                    $("#lv_btaInPersonalPopupTitle").html("Are you sure you want to remove this card BTA Booking?");
                    $("#lv_bta-in-personal-id").modal('show');
                }
                e.preventDefault();

            });

            $('.lv_bta_in_personal').off('click');
            $('.lv_bta_in_personal').on('click', 'button', function(e) {
                $("#lv_bta-in-personal-id").modal('hide');
                var confirm = $(this).attr("confirm");
                if (confirm == "yes" && $('#' + lvCardSelectedOfflineId).is(":checked")) {
                    // remove the offline card
                    CorporateQB.deleteBtaInPersonalCard(currentCardId, currentQBType, "yatra", "mybookings");
                    return false;
                } else if (confirm == "yes" && !$('#' + lvCardSelectedOfflineId).is(":checked")) {
                    CorporateQB.saveBtaInPersonalCard(currentCardId, currentQBType, "yatra", "mybookings");
                    return false;
                } else {

                }
            });
        }
        if (window.attachEvent && !window.addEventListener) {
            $('.col-xs-12').removeClass('col-xs-12');
            $('.col-xs-5').removeClass('col-xs-5');
        }
    },

    getSavedCardsListingHtml: function(savedCardsArray, qbType) {
        var savedCards = '';
        for (var i = 0; i < savedCardsArray.length; i++) {
            var cardNumber = CorporateQB.getCardNumber(savedCardsArray[i], qbType);
            var strCardReplace = cardNumber;
            strCardReplace = strCardReplace.replace(/^.{7}/g, 'xxxxxxx');
            cardNumber = strCardReplace;
            var gdsFlag = savedCardsArray[i].isGDSCard ? 'true' : 'false';
            var checkedFlag = savedCardsArray[i].isGDSCard ? 'checked' : '';
            var showCheckboxFlag = savedCardsArray[i].bookingType && savedCardsArray[i].bookingType == 'official' ? true : false;
            if (qbType != "PERSONAL") {
                var productFlag = savedCardsArray[i].productGroup ? true : false;
                var mappingFlag = savedCardsArray[i].productGroup ? true : false;
            }
            var showBtaInPersonalFlag = '';
            if (qbType == "PERSONAL" && btaInPersonalArray.length) {
                for (var j = 0; j < btaInPersonalArray.length; j++) {
                    if (btaInPersonalArray[j] == savedCardsArray[i].cardId)
                        showBtaInPersonalFlag = 'checked';

                }
            }
            var btaInPersonalFlag = showBtaInPersonalFlag == 'checked' ? 'true' : 'false';


			// if(savedCardsArray[i].isTokenized!=undefined && savedCardsArray[i].isTokenized==false && showCorpCardsTab) {
            //     savedCards += '<div class="card-row showTokenizedRow"><div class="card-picture">';
            // }else{
            //     savedCards += '<div class="card-row"><div class="card-picture">';
            // }
            savedCards += '<div class="card-row"><div class="card-picture">';
            savedCards += '<i class="' + CorporateQB.getCardLogo(savedCardsArray[i].cardBrand) + '"></i></div>';
            savedCards += ' <div class="card-details"><div class="card-details-container"><div class="card-type-container"><div class="card-type-number-box"><div class="card-number-name">';
            savedCards += ' <div class="card-number-tag"><div class="card-number" card-id="' + savedCardsArray[i].cardId + '" qb-type="' + qbType + '" isGDSCard="' + gdsFlag + '" isBTAInPersonal= "' + btaInPersonalFlag + '" ><p class="heading-bold" style="display:inline-block">' + cardNumber.toString().replace(/-/g, " ") + '</p><div  class="card-type">' + CorporateQB.getCardTypeName(savedCardsArray[i].cardType) + '</div></div>';
            if (showCheckboxFlag) {
                savedCards += '<div style="float:right"><input type="checkbox" name="cardForOfflineBooking" id="cardForOfflineBooking' + i + '" style="width: 14px;height: 14px;"' + checkedFlag + '><label for="cardForOfflineBooking' + i + '" style="position: relative;top: -2px;left: 3px;">Offline Booking</label></div>';
            }
            if (btaInPersonalEnabled && qbType == "PERSONAL" && false) {
                savedCards += '<div style="float:right; margin-right: 7px"><input type="checkbox" name="btaInPersonalSavedCard" id="btaInPersonalSavedCard' + i + '" style="width: 14px;height: 14px;"' + showBtaInPersonalFlag + '><label for="btaInPersonalSavedCard' + i + '" style="position: relative;top: -2px;left: 3px;">Map as Individual BTA</label></div>';
            }
            savedCards += '<p  class="card-name">' + CorporateQB.getCardName(savedCardsArray[i], qbType) + '</p>';
            //savedCards += '<div  class="card-type">'+ qbType +'</div>';
            savedCards += '</div></div></div>';
            if (qbType != "PERSONAL") {
                savedCards += '<div class="card-allowed"> <div class="card-allowed-details"><div class="row-main-right">';
                if (productFlag) {
                    savedCards += '<div class="row-inner"><div class="card-allowed-title"><p>Product allowed to book - </p> </div>';
                    savedCards += '<div class="right-billing">';
                    for (j = 0; j < savedCardsArray[i].productGroup.length; j++) {
                        savedCards += '<div class="right-billing-row">';
                        savedCards += '<div class="card-allowed-lob">' + CorporateQB.getProductName(savedCardsArray[i].productGroup[j]) + '</div>';
                        savedCards += '</div>';
                        //savedCards += '<li>dfdfdf</li>';
                    }
                    savedCards += '</div>';
                }
                if (mappingFlag) {
                    savedCards += '</div><div class="row-inner"><div class="card-allowed-title"><p>User who can use this card - </p></div>';
                    savedCards += '<div class="right-billing">';
                    for (obj in savedCardsArray[i].levelGroup) {
                        if (savedCardsArray[i].levelGroup[obj] === true) {
                            savedCards += '<div class="right-billing-row">';
                            savedCards += '<div class="card-allowed-lob"><p>All employees</p></div>';
                            savedCards += '</div>';
                        } else {
                            if (savedCardsArray[i].levelGroup[obj].length > 0) {
                                savedCards += '<div class="right-billing-row">';
                                savedCards += '<div class="card-allowed-lob"><p>' + CorporateQB.getMappingLevel(obj) + '</p></div>';
                                savedCards += '<div class="count-entity">' + savedCardsArray[i].levelGroup[obj].length + '</div>';
                                savedCards += '</div>';
                            }
                        }
                    }
                    savedCards += '</div></div>';
                }
				if(savedCardsArray[i].isTokenized!=undefined && savedCardsArray[i].isTokenized==true) {
                    savedCards += '<div class="active-token-label">'+CorporateQB.activeTokenCardLabel+'</div>';
                }

				if(savedCardsArray[i].isTokenized!=undefined && savedCardsArray[i].isTokenized==false) {
                    savedCards += '<div class="inactive-token-label">'+CorporateQB.inactiveTokenCardLabel+'</div>';
					savedCards += '<div class="inactive-token-label-desc">'+CorporateQB.inactiveTokenCardLabelDesc+'</div>';
                }

                savedCards += '</div></div></div>';
            }
            savedCards += '</div>';
            savedCards += '<div data-brackets-id="1440" class="card-actions-block"><ul data-brackets-id="1441" class="card-actions">';
            if (qbType != "PERSONAL" && (corpUserRole != undefined && corpUserRole == 'ADMIN')) {
                savedCards += '<li data-brackets-id="1442"><p data-brackets-id="1443"><a data-brackets-id="1444" class="edit-action" onclick="CorporateQB.showSavedCardsDivEdit(\'' + i + '\',\'' + qbType + '\',\'' + savedCardsArray[i].cardType+ '\')">View/Edit</a></p></li>';
            }

            if( qbType == 'PERSONAL' || (corpUserRole != undefined && corpUserRole == 'ADMIN') ){
            savedCards += '<li data-brackets-id="1447"><p data-brackets-id="1448" class="delete-action"><a data-brackets-id="1449" href="#">Delete</a></p></li>';
            }

            savedCards += '</ul></div></div></div></div>';
            noOfCards++;
        }

        return savedCards;
    },

    getProductList: function(formId) {
        var selected = [];
        $("#" + formId + " #productlist input[type='checkbox']").each(function() {
            if ($(this).is(":checked")) {
                selected.push($(this).attr('value'));
            }

        });
        if(isPCC && selected.length == 5){
        selected.pop("DOM_HOTEL");
        selected.pop("INT_HOTEL");
        selected.pop("OTHER");
        }
        if (selected.length == 5) {
            selected = ["ALL"];
        }
        return selected;
    },

    getMappingLevelList: function(formId) {
        var selected = [];
        $("#" + formId + " #mappingLevel_List input[type='checkbox']").each(function() {
            if ($(this).is(":checked")) {
                selected.push($(this).attr('value'));
            }

        });
        return selected;
    },

    checkCompanyLevel: function(formId) {
        if ($("#" + formId + " #mappingLevel_List input[value='companyLevel']").prop("checked") == true) {
            return true;
        } else {
            return false;
        }
    },
	ctaTokenFormSubmit: function(postData) {
		var f = document.getElementById("tokenForm");
		if (f !== null) {
			f.parentNode.removeChild(f);
		}
		var form = document.createElement("form");
		form.setAttribute("id", "tokenForm");
		form.setAttribute("method", "POST");
		form.setAttribute("action", tokenizationUrl);
		$.each(postData, function(propName, propVal) {
			var hiddenField = document.createElement("input");
			hiddenField.setAttribute("type", "hidden");
			hiddenField.setAttribute("name", propVal.name);
			hiddenField.setAttribute("value", propVal.value);
			form.appendChild(hiddenField);
		});
		document.body.appendChild(form);
		document.getElementById('tokenForm').submit();
	},
    getCardNumber: function(cardJson, qbType) {
        if ('CORPORATE' == qbType) {
            return cardJson.cardNumber1 + cardJson.cardNumber2 + cardJson.cardNumber3 + cardJson.cardNumber4;
        } else {
            return cardJson.cardNumber;
        }
    },

    getCardName: function(cardJson, qbType) {
        if ('CORPORATE' == qbType) {
            return cardJson.cardName + ', ' + cardJson.firstName + ' ' + cardJson.lastName;
        } else {
            return cardJson.cardName + ', ' + cardJson.cardHolderFirstName + ' ' + cardJson.cardHolderLastName;
        }
    },

    getProductName: function(productName) {
        switch (productName) {
            case "DOM_FLIGHT":
                return 'Domestic Flight';
            case "DOM_HOTEL":
                return 'Domestic Hotel';
            case "INT_HOTEL":
                return 'International Hotel';
            case "INT_FLIGHT":
                return 'International Flight';
            case "OTHER":
                return 'Others(Bus,Train,Visa,Insurance)';
            case "ALL":
                return 'ALL';
        }
    },

    getMappingLevel: function(mappingLevel) {
        switch (mappingLevel) {
            case "entity":
                return "Billing Entity";
            case "entityGroup":
                return "Billing Entity Group";
            case "user":
                return "Employee";
            case "companyLevel":
                return "All employees";
        }
    },

    getBookingType: function(bookingType) {
        switch (bookingType) {
            case "official":
                return "OFFICIAL";
            case "personal":
                return "PERSONAL";
            case "all":
                return "ALL";
        }
    },

    getCardTypeName: function(name) {
        switch (name) {
            case "CORPORATE":
                return "Corporate Card";
            case "BTA":
                return "BTA Card";
            case "CTA":
                return "CTA Card";
            case "BTA":
                return "BTA Card";
            case "PCARD":
                return "P-Card";
            default:
                return name;
        }
    },

    getCardLogo: function(cardBrand) {
        switch (cardBrand) {
            case "VISA":
                return 'ico-visa';
            case "MASTER":
                return 'ico-mastercard';
            case "MAEST":
                return 'ico-maestro';
            case "MAESTRO":
                return 'ico-maestro';
            case "AMEX":
                return 'ico-americanexpress';
            case "DINNERS":
                return 'ico-dinnerclub';
            case "DINERS":
                return 'ico-dinnerclub';
            default:
                return 'ico-visa';
        }
    },

    getNoCardsDiv: function() {
        var savedCards = '<section class="col-md-7"><div class="row">';
        savedCards += '<div class="pull-left col-md-9 hidden-xs"><h3 class="mg-top mg-bottom">QuickBook</h3></div>';
        savedCards += '<div class="pull-right col-md-3 col-xs-12"><button class="yt-btn btn-block" onclick="CorporateQB.showAddCardDiv()" type="button">';
        savedCards += '+  Add a card</button></div></div><div class="clearfix"></div>';
        savedCards += CorporateQB.getNoCardsMsg();
        savedCards += '</section>';
        $('#saved-cards').html(savedCards);
        CorporateQB.setViewCardBtnLabel();
    },

    getErrorCardsDiv: function() {
        var savedCards = '<section class="col-md-12"><div class="row">';
        savedCards += '<div class="pull-left col-md-9 hidden-xs"><h3 class="mg-top mg-bottom">QuickBook</h3></div>';
        savedCards += '<div class="pull-right col-md-3 col-xs-12"><button class="yt-btn btn-block" onclick="CorporateQB.showAddCardDiv()" type="button">';
        savedCards += '+  Add a card</button></div></div><div class="clearfix"></div>';
        savedCards += CorporateQB.getErrorCardsMsg();
        savedCards += '</section>';
        $('#saved-cards').html(savedCards);
        CorporateQB.setViewCardBtnLabel();
    },

    getErrorCardsMsg: function() {
        return 'Error occurred while fetching your cards. Please try after some time.';
    },

    getNoCardsMsg: function() {
        return 'You do not have any cards saved.';
    },

    registerDeleteCardEvent: function() {
        $('.delete-action').each(function(index) {
            $(this).on('click', function() {
                currentCardId = $(this).parents(".card-details-container").find(".card-number").attr("card-id");
                currentQBType = $(this).parents(".card-details-container").find(".card-number").attr("qb-type");
                currentisGDSCard = $(this).parents(".card-details-container").find(".card-number").attr("isGDSCard")
                currentCardUL = $(this).parents(".card-row");
                currentisSaveAsCorpCard = $(this).parents(".card-details-container").find(".card-number").attr("isBTAInPersonal")
                $("#delete-popup-id").modal('show');
            })
        });
    },

    deleteQBCard: function(cardId, qbType, merchant, product, isGDSCard) {
        var qbDeleteCardURL = CorporateQB.getQBDeleteCardURL();
        var delDefer = $.Deferred();

        let postData = {
             'merchant': merchant,
             'product': product,
             'cardid': cardId,
             'qbType': qbType,
             'csrfToken': csrfToken,
             'isGDSCard': isGDSCard,
             'saveAsCorpCard': currentisSaveAsCorpCard == "true" ? currentisSaveAsCorpCard : undefined
        }

        if (selectedEAUser && Object.keys(selectedEAUser).length > 0) {
             postData['targetUserId'] = selectedEAUser.value;
        }

        $.ajax({
            url: qbDeleteCardURL,
            type: 'POST',
            data: postData,
            cache: false,

            success: function(response) {
                var responseJSON = CorporateQB.convertToJsonObject(response);
                if (responseJSON.status && responseJSON.status == "SUCCESS") {
                    if ($('#quickBookErrSucc').length > 0) {
                        $('#quickBookErrSucc').remove();
                    }
                    delDefer.resolve('success');
                }
                if (CorporateQB.isValid(responseJSON) && responseJSON.status == "FAILURE") {
                    if (CorporateQB.isValid(responseJSON.error_code) && (responseJSON.error_code == "AUTH_REQUIRED" || responseJSON.error_code == "103"))
                        CorporateQB.showAuthPage();
                }
            },

            error: function(request, status, error) {

                delDefer.resolve('failure');
            }
        });
        return delDefer.promise();
    },

    getQBDeleteCardURL: function() {
        return "/PaySwift/corporate-quickbook/delete-card.htm"
    },

    deleteGDSCard: function(cardId, qbType, merchant, product, isGDSCard) {
        $('input:checkbox[name="cardForOfflineBooking"]').attr('disabled', true);
        var qbDeleteGDSCardURL = CorporateQB.getQBDeleteGDSCardURL();

        $.ajax({

            url: qbDeleteGDSCardURL,
            type: 'POST',
            data: {
                'merchant': merchant,
                'product': product,
                'cardid': cardId,
                'qbType': qbType,
                'csrfToken': csrfToken,
            },
            cache: false,

            success: function(response) {
                $('input:checkbox[name="cardForOfflineBooking"]').removeAttr("disabled");
                var responseJSON = CorporateQB.convertToJsonObject(response);
                if (responseJSON.status && responseJSON.status == "SUCCESS") {

                    $('input:checkbox[name="cardForOfflineBooking"]').attr('checked', false);
                    $('#' + lvCardSelectedOfflineId).attr('value', $('#' + lvCardSelectedOfflineId).prop('checked'));

                    if ($('#quickBookErrSucc').length > 0) {
                        $('#quickBookErrSucc').remove();
                    }

                    $('#saved-cards-error').append('<div class="ajaxErrorSuccess col-xs-12" id="quickBookErrSucc" style="background-color: #fff3bd;padding: 10px 15px;margin-bottom: 20px;color: #dc3201;"><i class="fa fa-tick-r"></i>Your card has been successfully removed from offline bookings.</div>')
                    window.scrollTo($('#quickBookErrSucc').offset().left, $('#quickBookErrSucc').offset().top);
                } else if (CorporateQB.isValid(responseJSON) && responseJSON.status == "FAILURE" && CorporateQB.isValid(responseJSON.error_code) && responseJSON.error_code == "AUTH_REQUIRED") {
                    CorporateQB.showAuthPage();
                } else {
                    if ($('#quickBookErrSucc').length > 0) {
                        $('#quickBookErrSucc').remove();
                    }

                    $('#saved-cards-error').append('<div class="ajaxErrorSuccess col-xs-12" id="quickBookErrSucc" style="background-color: #fff3bd;padding: 10px 15px;margin-bottom: 20px;color: #dc3201;"><i class="fa fa-times"></i>Error while removing your card for offline bookings, Please Retry</div>')
                    window.scrollTo($('#quickBookErrSucc').offset().left, $('#quickBookErrSucc').offset().top);
                }
            },

            error: function(request, status, error) {
                $('input:checkbox[name="cardForOfflineBooking"]').removeAttr("disabled");
                if ($('#quickBookErrSucc').length > 0) {
                    $('#quickBookErrSucc').remove();
                }
                $('#saved-cards-error').append('<div class="ajaxErrorSuccess col-xs-12" id="quickBookErrSucc" style="background-color: #fff3bd;padding: 10px 15px;margin-bottom: 20px;color: #dc3201;"><i class="fa fa-times"></i>Error while removing your card for offline bookings, Please Retry</div>')
                window.scrollTo($('#quickBookErrSucc').offset().left, $('#quickBookErrSucc').offset().top);
            }
        });
    },

    getQBDeleteGDSCardURL: function() {
        return "/PaySwift/corporate-quickbook/delete-gds-card.htm"
    },

    saveGDSCard: function(cardId, qbType, merchant, product, isGDSCard) {
        $('input:checkbox[name="cardForOfflineBooking"]').attr('disabled', true);
        var qbSaveGDSCardURL = CorporateQB.getQBSaveGDSCardURL();

        $.ajax({
            url: qbSaveGDSCardURL,
            type: 'POST',
            data: {
                'merchant': merchant,
                'product': product,
                'cardid': cardId,
                'qbType': qbType,
                'csrfToken': csrfToken,
                'isGDSCard': isGDSCard
            },
            cache: false,

            success: function(response) {
                $('input:checkbox[name="cardForOfflineBooking"]').removeAttr("disabled");
                var responseJSON = CorporateQB.convertToJsonObject(response);
                if (responseJSON.status && responseJSON.status == "SUCCESS") {
                    if ($('#quickBookErrSucc').length > 0) {
                        $('#quickBookErrSucc').remove();
                    }

                    //CorporateQB.getSavedCards();
                    ///CorporateQB.showSavedCardsDiv();
                    $('input:checkbox[name="cardForOfflineBooking"]').attr('checked', false);
                    $('#' + lvCardSelectedOfflineId).prop('checked', true);
                    $('#' + lvCardSelectedOfflineId).attr('value', $('#' + lvCardSelectedOfflineId).prop('checked'));


                    $('#saved-cards-error').append('<div class="ajaxErrorSuccess col-xs-12" id="quickBookErrSucc" style="background-color: #fff3bd;padding: 10px 15px;margin-bottom: 20px;color: #dc3201;"><i class="fa fa-tick-r"></i>Your card has been successfully added card for offline bookings.</div>')
                    window.scrollTo($('#quickBookErrSucc').offset().left, $('#quickBookErrSucc').offset().top);
                } else if (CorporateQB.isValid(responseJSON) && responseJSON.status == "FAILURE" && CorporateQB.isValid(responseJSON.error_code) && responseJSON.error_code == "AUTH_REQUIRED") {
                    CorporateQB.showAuthPage();
                } else {

                    if ($('#quickBookErrSucc').length > 0) {
                        $('#quickBookErrSucc').remove();
                    }

                    $('#saved-cards-error').append('<div class="ajaxErrorSuccess col-xs-12" id="quickBookErrSucc" style="background-color: #fff3bd;padding: 10px 15px;margin-bottom: 20px;color: #dc3201;"><i class="fa fa-times"></i>Error while adding your card for offline bookings, Please Retry</div>')
                    window.scrollTo($('#quickBookErrSucc').offset().left, $('#quickBookErrSucc').offset().top);


                }
            },

            error: function(request, status, error) {
                $('input:checkbox[name="cardForOfflineBooking"]').removeAttr("disabled");
                if ($('#quickBookErrSucc').length > 0) {
                    $('#quickBookErrSucc').remove();
                }

                $('#saved-cards-error').append('<div class="ajaxErrorSuccess col-xs-12" id="quickBookErrSucc" style="background-color: #fff3bd;padding: 10px 15px;margin-bottom: 20px;color: #dc3201;"><i class="fa fa-times"></i>Error while adding your card for offline bookings, Please Retry</div>')
                window.scrollTo($('#quickBookErrSucc').offset().left, $('#quickBookErrSucc').offset().top);

            }
        });
    },
    getQBSaveGDSCardURL: function() {
        return "/PaySwift/corporate-quickbook/save-gds-card.htm"
    },
    saveBtaInPersonalCard: function(cardId, qbType, merchant, product, isGDSCard) {
        $('input:checkbox[name="btaInPersonalSavedCard"]').attr('disabled', true);
        var qbSaveBtaInPersonalCardURL = "/PaySwift/corporate-quickbook/save-corp-card-via-personal-flow";

        $.ajax({

            url: qbSaveBtaInPersonalCardURL,
            type: 'POST',
            data: {
                'merchant': merchant,
                'product': product,
                'cardid': cardId,
                'qbType': qbType,
                'csrfToken': csrfToken,
                'isGDSCard': isGDSCard
            },
            cache: false,

            success: function(response) {
                $('input:checkbox[name="btaInPersonalSavedCard"]').removeAttr("disabled");
                var responseJSON = CorporateQB.convertToJsonObject(response);
                if (responseJSON.status && responseJSON.status == "SUCCESS") {
                    if ($('#quickBookErrSucc').length > 0) {
                        $('#quickBookErrSucc').remove();
                    }
                    if (selectedEAUser && Object.keys(selectedEAUser).length > 0) {
                        CorporateQB.getSavedCardsByUserId(selectedEAUser.value)
                    }else{
                        CorporateQB.getSavedCards();
                    }

                    CorporateQB.showSavedCardsDiv(false);
                    $('#' + lvCardSelectedOfflineId).prop('checked', true);
                    $('#' + lvCardSelectedOfflineId).attr('value', $('#' + lvCardSelectedOfflineId).prop('checked'));


                    $('#saved-cards-error').append('<div class="ajaxErrorSuccess col-xs-12" id="quickBookErrSucc" style="background-color: #fff3bd;padding: 10px 15px;margin-bottom: 20px;color: #dc3201;"><i class="fa fa-tick-r"></i>Your card has been successfully added card for BTA.</div>')
                    window.scrollTo($('#quickBookErrSucc').offset().left, $('#quickBookErrSucc').offset().top);
                } else if (CorporateQB.isValid(responseJSON) && responseJSON.status == "FAILURE" && CorporateQB.isValid(responseJSON.error_code) && responseJSON.error_code == "AUTH_REQUIRED") {
                    CorporateQB.showAuthPage();
                } else {

                    if ($('#quickBookErrSucc').length > 0) {
                        $('#quickBookErrSucc').remove();
                    }

                    let errorMessage = responseJSON.error_msg ? responseJSON.error_msg : "Error while adding your card for BTA, Please Retry";
                    $('#saved-cards-error').append('<div class="ajaxErrorSuccess col-xs-12" id="quickBookErrSucc" style="background-color: #fff3bd;padding: 10px 15px;margin-bottom: 20px;color: #dc3201;"><i class="fa fa-times"></i>' + errorMessage + '</div>')
                    window.scrollTo($('#quickBookErrSucc').offset().left, $('#quickBookErrSucc').offset().top);

                    if (selectedEAUser && Object.keys(selectedEAUser).length > 0) {
                        CorporateQB.getSavedCardsByUserId(selectedEAUser.value)
                    }else{
                        CorporateQB.getSavedCards();
                    }
                    CorporateQB.showSavedCardsDiv(false);
                }
            },

            error: function(request, status, error) {
                $('input:checkbox[name="btaInPersonalSavedCard"]').removeAttr("disabled");
                if ($('#quickBookErrSucc').length > 0) {
                    $('#quickBookErrSucc').remove();
                }

                $('#saved-cards-error').append('<div class="ajaxErrorSuccess col-xs-12" id="quickBookErrSucc" style="background-color: #fff3bd;padding: 10px 15px;margin-bottom: 20px;color: #dc3201;"><i class="fa fa-times"></i>Error while adding your card for BTA, Please Retry</div>')
                window.scrollTo($('#quickBookErrSucc').offset().left, $('#quickBookErrSucc').offset().top);

                if (selectedEAUser && Object.keys(selectedEAUser).length > 0) {
                    CorporateQB.getSavedCardsByUserId(selectedEAUser.value)
                }else{
                    CorporateQB.getSavedCards();
                }
                CorporateQB.showSavedCardsDiv(false);
            }
        });
    },
    deleteBtaInPersonalCard: function(cardId, qbType, merchant, product, isGDSCard) {
        $('input:checkbox[name="btaInPersonalSavedCard"]').attr('disabled', true);
        var qbDeleteBtaInPersonalCardURL = "/PaySwift/corporate-quickbook/del-bta-card";

        $.ajax({

            url: qbDeleteBtaInPersonalCardURL,
            type: 'POST',
            data: {
                'merchant': merchant,
                'product': product,
                'cardid': cardId,
                'qbType': qbType,
                'csrfToken': csrfToken,
            },
            cache: false,

            success: function(response) {
                $('input:checkbox[name="btaInPersonalSavedCard"]').removeAttr("disabled");
                var responseJSON = CorporateQB.convertToJsonObject(response);
                if (responseJSON.status && responseJSON.status == "SUCCESS") {

                    $('#' + lvCardSelectedOfflineId).prop('checked', !$('#' + lvCardSelectedOfflineId).prop('checked'));
                    $('#' + lvCardSelectedOfflineId).attr('value', $('#' + lvCardSelectedOfflineId).prop('checked'));

                    if (selectedEAUser && Object.keys(selectedEAUser).length > 0) {
                        CorporateQB.getSavedCardsByUserId(selectedEAUser.value)
                    }else{
                        CorporateQB.getSavedCards();
                    }
                    CorporateQB.showSavedCardsDiv(false);

                    if ($('#quickBookErrSucc').length > 0) {
                        $('#quickBookErrSucc').remove();
                    }


                    $('#saved-cards-error').append('<div class="ajaxErrorSuccess col-xs-12" id="quickBookErrSucc" style="background-color: #fff3bd;padding: 10px 15px;margin-bottom: 20px;color: #dc3201;"><i class="fa fa-tick-r"></i>Your card has been successfully removed from BTA.</div>')
                    window.scrollTo($('#quickBookErrSucc').offset().left, $('#quickBookErrSucc').offset().top);
                } else if (CorporateQB.isValid(responseJSON) && responseJSON.status == "FAILURE" && CorporateQB.isValid(responseJSON.error_code) && responseJSON.error_code == "AUTH_REQUIRED") {
                    CorporateQB.showAuthPage();
                } else {
                    if ($('#quickBookErrSucc').length > 0) {
                        $('#quickBookErrSucc').remove();
                    }

                    // $('#'+lvCardSelectedOfflineId).attr('checked', !$('#'+lvCardSelectedOfflineId).is('checked'));
                    let errorMessage = responseJSON.error_msg ? responseJSON.error_msg : "Error while removing your card from BTA, Please Retry";
                    $('#saved-cards-error').append('<div class="ajaxErrorSuccess col-xs-12" id="quickBookErrSucc" style="background-color: #fff3bd;padding: 10px 15px;margin-bottom: 20px;color: #dc3201;"><i class="fa fa-times"></i>' + errorMessage + '</div>')

                    window.scrollTo($('#quickBookErrSucc').offset().left, $('#quickBookErrSucc').offset().top);
                    if (selectedEAUser && Object.keys(selectedEAUser).length > 0) {
                        CorporateQB.getSavedCardsByUserId(selectedEAUser.value)
                    }else{
                        CorporateQB.getSavedCards();
                    }
                    CorporateQB.showSavedCardsDiv(false);
                }
            },

            error: function(request, status, error) {
                $('input:checkbox[name="btaInPersonalSavedCard"]').removeAttr("disabled");
                if ($('#quickBookErrSucc').length > 0) {
                    $('#quickBookErrSucc').remove();
                }

                // $('#'+lvCardSelectedOfflineId).attr('checked', !$('#'+lvCardSelectedOfflineId).is('checked'));
                $('#saved-cards-error').append('<div class="ajaxErrorSuccess col-xs-12" id="quickBookErrSucc" style="background-color: #fff3bd;padding: 10px 15px;margin-bottom: 20px;color: #dc3201;"><i class="fa fa-times"></i>Error while removing your card from BTA, Please Retry</div>')
                window.scrollTo($('#quickBookErrSucc').offset().left, $('#quickBookErrSucc').offset().top);

                if (selectedEAUser && Object.keys(selectedEAUser).length > 0) {
                    CorporateQB.getSavedCardsByUserId(selectedEAUser.value)
                }else{
                    CorporateQB.getSavedCards();
                }
                CorporateQB.showSavedCardsDiv(false);

                //$('#'+lvCardSelectedOfflineId).attr('value', !$('#'+lvCardSelectedOfflineId).is('checked'));
            }
        });
    },



    setCardType: function() {
        var cardType = $('.js-card-type .btn-selected').attr("card_type");
        if (CorporateQB.isValid(cardType)) {
            $("#corp_qb_ctype_id").val(cardType);
        } else {
            $("#corp_qb_ctype_id").val("");
        }
    },

    setQBType: function() {
        var cardType = $('.js-card-type .btn-selected').attr("qb-type");
        if (CorporateQB.isValid(cardType)) {
            $("#corp_qb_qbtype_id").val(cardType);
        } else {
            $("#corp_qb_qbtype_id").val("");
        }
    },

    getCardType: function() {
        return $("#corp_qb_ctype_id").val();
    },

    setCardBrand: function(cardBrand) {
        if (CorporateQB.isValid(cardBrand)) {
            $("#corp_qb_cbrand_id").val(cardBrand);
        } else {
            $("#corp_qb_cbrand_id").val("");
        }
    },

    getCardBrand: function(cardNumber) {
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

    getIsCardInternational: function() {
        return $("#corp_qb_isCardInternational_id").val();
    },

    setIsCardInternational: function(isCardInt) {
        $("#corp_qb_isCardInternational_id").val(isCardInt);
    },

    fadeCardLogo: function(cardBrand) {
        var cardBrand = $("#corp_qb_cbrand_id").val();
        $.each($(".card_logos > i"), function() {
            var cardLogoId = $(this).attr("id");
            var cardLogo = cardLogoId.substring(0, cardLogoId.indexOf("_")).toUpperCase();
            if (CorporateQB.isInvalid(cardBrand) || cardLogo == cardBrand) {
                $(this).removeClass("ico-o-5");
            } else {
                $(this).addClass("ico-o-5");
            }
        });
    },

    showMaestExpiryMsg: function(cardBrand) {
        /*var cardType = CorporateQB.getCardType();*/
        if (CorporateQB.isValid(cardBrand) && cardBrand == "MAEST" /*&& cardType == "dc"*/ ) {
            $("#maest_expiry_msg").show();
        } else {
            $("#maest_expiry_msg").hide();
        }
    },

    updateValidations: function(cardBrand) {

        if (CorporateQB.isValid(cardBrand)) {

            // Get Card Type (cc/dc)
            var cardType = CorporateQB.getCardType();

            // Card number.
            var validationRule = CorporateQB.getCardNoValidationRule(cardType, cardBrand);
            var validationMsg = CorporateQB.getCardNoValidationMsg(cardType, cardBrand);

            $('#qb_cno_id').attr('data-validation', validationRule);
            $('#qb_cno_id').attr('data-msginfo', validationMsg);

        } else {
            CorporateQB.setDefaultValidations();
        }
    },

    getCardNoValidationRule: function(cardType, cardBrand) {

        // Validation Rule for CC
        var validationRule = 'required|validateLuhn|cardRegType[' + cardBrand + ']';

        return validationRule;
    },

    getCardNoValidationMsg: function(cardType, cardBrand) {

        // Validation Msg for CC
        var validationMsg = '* required|Enter valid card number|Enter valid card number|Enter valid credit card number';

        return validationMsg;
    },

    processInternationalCard: function(cardBrand, cardNo) {

        if (!CorporateQB.isCardBrandInternational(cardBrand)) {
            CorporateQB.hideBillingAddDiv();
            return;
        }

        // Check if card is international (only for VISA and MASTER). If international, show address div.
        if (CorporateQB.isCardLengthValid(cardBrand, cardNo)) {
            var bin = parseInt(cardNo.substring(0, 9));
            var isCardIntURL = CorporateQB.getIsCardInternationalURL();

            $.ajax({
                url: isCardIntURL,
                type: 'POST',
                data: {
                    'bin': bin
                },
                cache: false,
                success: function(response) {
                    var responseJSON = CorporateQB.convertToJsonObject(response);
                    if (CorporateQB.isValid(responseJSON) && responseJSON.isCardInternational == "true") {
                        CorporateQB.showBillingAddDiv();
                    } else {
                        CorporateQB.hideBillingAddDiv();
                    }
                },

                error: function(request, status, error) {
                    alert("Error while determining if card is international or not.");
                }
            });
        } else {
            CorporateQB.hideBillingAddDiv();
        }
    },

    isCardBrandInternational: function(cardBrand) {
        if (CorporateQB.isInvalid(cardBrand))
            return false;
        return CorporateQB.ifArrayContains(internationalCardBrandsArr, cardBrand)
    },

    getIsCardInternationalURL: function() {
        return '/PaySwift/isCardInternational.htm';
    },

    isCardLengthValid: function(cardBrand, cardNo) {
        switch (cardBrand) {
            case "VISA":
                return (cardNo.length == 13 || cardNo.length == 16);
            case "MASTER":
                return (cardNo.length == 16);
            case "MAEST":
                return (cardNo.length == 16 || cardNo.length == 18 || cardNo.length == 19);
            case "AMEX":
                return (cardNo.length == 15);
            case "DINERS":
                return (cardNo.length == 14);
            default:
                return false;
        }
    },

    resetAddCardDiv: function(enableDefaultTab) {
        if (enableDefaultTab) {
            if (showCorpCardsTab)
                CorporateQB.enableCorporateTab();
            else {
                CorporateQB.enablePersonalTab();
                CorporateQB.hideCorporateTabs();
            }
        }

        // Hiding details / div.
        CorporateQB.clearDetails();
        CorporateQB.hideErrorMsgs();
        CorporateQB.hideBillingAddDiv();
        CorporateQB.hideAmexIssueDiv();
        CorporateQB.resetDropDown();
        CorporateQB.setCardBrand("");

        // Reseting depending upon cardType (cc/dc)
        CorporateQB.setCardType();
        CorporateQB.setQBType();
        CorporateQB.showCardLogos();
        CorporateQB.setCardNoLabel();
        CorporateQB.setDefaultValidations();
    },

    enableCorporateTab: function() {
        $("#qb_corporate_tab_id").trigger('click');
    },

    enablePersonalTab: function() {
        $("#qb_personal_tab_id").trigger('click');
    },

    hideCorporateTabs: function() {
        $("#qb_corporate_tab_id").hide();
        $("#qb_bta_tab_id").hide();
        $("#qb_cta_tab_id").hide();
        $("#qb_pcard_tab_id").hide();
    },

    clearDetails: function() {
        $("#add_card_div_id input:text").each(function() {
            $(this).val("");
            IE_Placeholder.callBackPlaceDefaultText($(this));
        });

        //clearing multiselect dropdown - product, mapping level and autosuggest fields
        var textDisplayProduct = ["Domestic Flight", "Domestic Hotel", "International Hotel", "International Flight", "Others"];
          if(isPCC) {
                        textDisplayProduct = ["Domestic Flight", "International Flight"];
                    }

        $("#productlist input[type='checkbox']").prop("checked", true);
        $("#product-div #product").text(textDisplayProduct);
        $("#mappingLevel_List input[type='checkbox']").prop("checked", false);
        $("#mappingLevel_List input[value='companyLevel']").prop("checked", true);
        $("#product-div #mappingLevel").text("Company Level");
        $(".companyLevel,.entityGroup,.entity,.user,.dropdown-menu-product,.dropdown-menu-mapping").hide();
        $('#be-group-values-container').hide();
        $('#entity-values-container').hide();
        $('#user-values-container').hide();
        // clearing the offline checkbox also
        $('#saveCardToGDS').prop('checked', false);
        $('#saveCardToGDS').attr('value', false);

        $('#saveAsCorpCard').prop('checked', false);
        $('#saveAsCorpCard').attr('value', false);
        CorporateQB.resetYourCardRadio()

    },

    hideErrorMsgs: function() {
        $(".qb_error_msg").hide();
    },

    resetDropDown: function() {
        $('#qb_expm_id :nth(0)').attr("selected", "selected");
        $('#qb_bacu_id :nth(0)').attr("selected", "selected");
        CorporateQB.populateExpYrDropDown();
    },

    populateExpYrDropDown: function() {
        $("#qb_expy_id").empty();
        $("#qb_expy_id").append($('<option></option>').val("").html("Year"));
        var currYear = new Date().getFullYear();
        for (var i = 0; i <= 20; i++) {
            $("#qb_expy_id").append($('<option></option>').val(currYear + i).html(currYear + i));
        }
        var expM = $('#qb_expm_id');
        var expY = $('#qb_expy_id');
        var country = $('#qb_bacu_id');
        if (alreadyCustomized && $(window).width() > 768) {
            $('#qb_expm_id').parents('div.select').replaceWith(expM);
            $('#qb_expy_id').parents('div.select').replaceWith(expY);
            $('#qb_bacu_id').parents('div.select').replaceWith(country);
            addMyCustomController();
        }
        if (!alreadyCustomized) {
            addMyCustomController();
            alreadyCustomized = true;
        }
    },

    showCardLogos: function() {

        $.each($(".card_logos > i"), function() {
            $(this).show();
            $(this).removeClass("ico-o-5");
        });

        var cardType = CorporateQB.getCardType();
        /*switch(cardType) {
			  case "cc": */
        $("#maest_logo_id").hide(); /*break;*/
        /*case "dc": $("#amex_logo_id").hide(); break;
		  }*/
    },

    setCardNoLabel: function() {
        $("#card_no_label_id").html("Credit Card Number");
    },

    setDefaultValidations: function() {
        $('#qb_cno_id').attr('data-validation', 'required|validateLuhn|cardRegType[VISA]');
        $('#qb_cno_id').attr('data-msginfo', '* required|Enter valid card number|Enter valid card number|Enter valid credit card number');
        $("#qb_cno_id").attr("maxlength", "16");

        // Expiry Month
        $('#qb_expm_id').attr('data-validation', 'required');
        $('#qb_expm_id').attr('data-msginfo', '* required');

        // Expiry Year
        $('#qb_expy_id').attr('data-validation', 'required|checkExpiryDate[qb]');
        $('#qb_expy_id').attr('data-msginfo', '* required|Invalid Date');

    },

    showAddCardErrorMsg: function() {
        $("#add_card_error_msg").show();
        $('#add_card_error_msg').delay(5000).fadeOut();
    },

    showDuplicateCardErrorMsg: function() {
        $("#duplicate_card_error_msg").show();
        $('#duplicate_card_error_msg').delay(5000).fadeOut();
    },

    showResetPassErrorMsg: function() {
        $("#reset_pass_error_msg").show();
        $('#reset_pass_error_msg').delay(5000).fadeOut();
    },

    showAmexIssueDiv: function(cardBrand) {
        /*var cardType = CorporateQB.getCardType();*/
        if (CorporateQB.isValid(cardBrand) && cardBrand == 'AMEX' /*&& cardType == 'cc'*/ ) {
            $('input:radio[name="address_check_rdo"]:nth(0)').prop("checked", "checked");
            $("#amex_issue_div").show();
        } else {
            CorporateQB.hideAmexIssueDiv();
        }
    },

    hideAmexIssueDiv: function() {
        $("#amex_issue_div").hide();
    },

    showAddCardDiv: function() {
        // Hide other divs.
        CorporateQB.hideResetPassDiv();
        CorporateQB.hideSavedCardsDiv();
        CorporateQB.hideQBHomeDiv();
        CorporateQB.hideNotLoggedInDiv();
        CorporateQB.resetYourCardRadio();
        $('#corp-add-card').show();
        CorporateQB.resetAddCardDiv(true);
        if ($('#quickBookErrSucc').length > 0) {
            $('#quickBookErrSucc').remove();
        }

        if(corpUserRole == 'ADMIN'){
            $('#qb_corporate_tab_id').show();
            $('#qb_bta_tab_id').show();
            $('#qb_cta_tab_id').show();
            $('#qb_pcc_tab_id').show();
        } else {
            $('#qb_corporate_tab_id').hide();
            $('#qb_bta_tab_id').hide();
            $('#qb_cta_tab_id').hide();
            $('#qb_pcc_tab_id').hide();
            CorporateQB.enablePersonalTab();
        }

        if(eval(showPccTab) != true){
           $("#qb_pcc_tab_id").hide();
        }else{
            if (selectedEAUser && Object.keys(selectedEAUser).length > 0) {
                $("selected-ea-user-label").show();
                $("selected-ea-user-label").text("Selected EA User: "+selectedEAUser.label);
                $("#qb_pcc_tab_id").hide();
            }else{
                $("selected-ea-user-label").hide();
                $("selected-ea-user-label").text("");
                $("#qb_pcc_tab_id").show();
            }
        }
    },

    resetYourCardRadio: function() {
        $('input:radio[name="bookingType"]').attr('checked', false)
        $("#allowOfflineCheckBoxContainer").hide();
    },
    hideAddCardDiv: function() {
        $('#corp-add-card').hide();
    },

    showSavedCardsDiv: function(status = true) {
        if (isUserAuthenticated) {
            // Hide other divs.
            CorporateQB.hideResetPassDiv();
            CorporateQB.hideQBHomeDiv();
            CorporateQB.hideAddCardDiv();
            CorporateQB.hideNotLoggedInDiv();
            CorporateQB.hideEditCardsDiv();
            $('#corp-saved-cards').show();
            if (noOfCards == 0 && status) {
                CorporateQB.getSavedCards();
            }
        } else {
            CorporateQB.showAuthPage();
        }
    },

    showSavedCardsDivEdit: function(i, qbType , qbCardType) {
        if (isUserAuthenticated) {
            // Hide other divs.
            CorporateQB.hideResetPassDiv();
            CorporateQB.hideQBHomeDiv();
            CorporateQB.hideAddCardDiv();
            CorporateQB.hideNotLoggedInDiv();
            CorporateQB.hideSavedCardsDiv();
            CorporateQB.getEditCardsDiv(i, qbType);
            $('#corp-edit-cards').show();
            $("#edit-cards #productDropdown").show();
            $("#edit-cards #mappingDropdown").show();
            // enable below conditions to disable mapping dropdown for non admin users
            /*if( corpUserRole != undefined && corpUserRole != 'ADMIN' && qbCardType != undefined && qbCardType == 'PCC'){
            $("#edit-cards #mappingDropdown").hide();
            $("#edit-cards #mapping-div").hide();
            }*/

            $(window).scrollTop(0);
        } else {
            CorporateQB.showAuthPage();
        }
    },

    hideSavedCardsDiv: function() {
        $('#corp-saved-cards').hide();
    },


    hideEditCardsDiv: function() {
        $('#corp-edit-cards').hide();
    },

    showQBHomeDiv: function() {
        // Hide other divs.
        CorporateQB.hideResetPassDiv();
        CorporateQB.hideAddCardDiv();
        CorporateQB.hideSavedCardsDiv();
        CorporateQB.hideEditCardsDiv();
        CorporateQB.hideNotLoggedInDiv();

        $('#corp-qb-home').show();
    },

    hideQBHomeDiv: function() {
        $('#corp-qb-home').hide();
    },

    showResetPassDiv: function() {
        // Hide other divs.
        CorporateQB.hideQBHomeDiv();
        CorporateQB.hideAddCardDiv();
        CorporateQB.hideSavedCardsDiv();
        CorporateQB.hideNotLoggedInDiv();

        $('#corp-reset-pass').show();
    },

    hideResetPassDiv: function() {
        $('#corp-reset-pass').hide();
    },

    showNotLoggedInDiv: function() {
        // Hide other divs.
        CorporateQB.hideQBHomeDiv();
        CorporateQB.hideAddCardDiv();
        CorporateQB.hideSavedCardsDiv();
        CorporateQB.hideResetPassDiv();
        CorporateQB.hideNotAuthorizedDiv();

        $('#corp-not-logged-in').show();
    },

    showNotAuthorizedDiv: function() {
        // Hide other divs.
        CorporateQB.hideQBHomeDiv();
        CorporateQB.hideAddCardDiv();
        CorporateQB.hideSavedCardsDiv();
        CorporateQB.hideResetPassDiv();
        CorporateQB.hideNotLoggedInDiv();

        $('#corp-not-authorized').show();
    },

    hideNotLoggedInDiv: function() {
        $('#corp-not-logged-in').hide();
    },

    hideNotAuthorizedDiv: function() {
        $('#corp-not-authorized').hide();
    },

    showBillingAddDiv: function() {
        CorporateQB.setIsCardInternational(true);
        $('#corp-qb-card-holder-address').show();
    },

    hideBillingAddDiv: function() {
        CorporateQB.setIsCardInternational(false);
        $('#corp-qb-card-holder-address').hide();
    },

    setViewCardBtnLabel: function() {
        $("#view-card-btn").html("View saved cards (" + noOfCards + ")");
    },

    showAuthPage: function() {
        window.location.replace(CorporateQB.getAuthURL());
    },

    getAuthURL: function() {
        return "https://secure.yatra.com/social/common/b2b_agent/signin.htm";
    },

    // Utility functions.

    isValid: function(value) {
        return (value != null && typeof(value) != "undefined" && value != "" && value != "null");
    },

    isInvalid: function(value) {
        return (value == null || typeof(value) == "undefined" || value == "" || value == "null");
    },

    convertToJsonObject: function(input) {
        var jsonObject;
        if (CorporateQB.isValid(input)) {
            jsonObject = $.parseJSON(input);
        }
        return jsonObject;
    },

    ifArrayContains: function(array, string) {
        for (var j = 0; j < array.length; j++) {
            if (string == array[j])
                return true;
        }
        return false;
    },

    checkNaN: function(field) {
        var inputVal = $(field).val();
        if (isNaN(inputVal)) {
            var newInputVal = parseInt(inputVal);
            if (isNaN(newInputVal)) {
                newInputVal = '';
            }
            $(field).val(newInputVal);
        }
    },

    disableSpace: function(field) {
        $(field).val($(field).val().replace(/\s/g, ''));
    },

    getSSOToken: function() {
        var i, x, y;
        var ARRcookies = document.cookie.split(";");
        for (i = 0; i < ARRcookies.length; i++) {
            x = ARRcookies[i].substr(0, ARRcookies[i].indexOf("="));
            y = ARRcookies[i].substr(ARRcookies[i].indexOf("=") + 1);
            x = x.replace(/^\s+|\s+$/g, "");
            if (x == "userName") {
                return unescape(y);
            }
        }
    },
	isSbt: function() {
		var isAdmin = showCorpCardsTab;
		var cType = $('#corp_qb_ctype_id').attr('value');
		var isPCC_CardType = cType == 'PCC'? true : false;
		if(!isAdmin && isPCC_CardType) {
			return true;
		}
		return false;
	}
};

$(document).ready(function() {

    CorporateQB.initialize();
    CorporateQB.mappingLevelFunctionality("product-div");

    $('#qbLinkMobile').on('click', function() {
        CorporateQB.showQBHomeDiv()
    });
    $(".custom-Radiobox").click(function(event) {
        $(".custom-Radiobox").each(function() {
            if (this == event.target || this.contains(event.target)) {
                $(this).find("input[type='radio']").prop("checked", "checked");
            }
        });

    });

});

var IE_Placeholder = IE_Placeholder || {
    config: {
        placeholder_color_class: 'placeholderColor',
        normaltext_color_class: 'defaultColor',
        isIE: 0
    },
    init: function(elemStr) {

        var $this = this,
            ua = window.navigator.userAgent;

        $this.config.isIE = ua.match(/msie/i);

        if ($this.config.isIE) {
            // set default values for each input element

            if (elemStr == "" || elemStr == undefined || elemStr == null) {
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
    removeDefaultText: function(jElem) {

        var $this = this,
            placeholder = jElem.attr('placeholder');

        if (jElem.val() == placeholder) {
            jElem.val("");
            jElem.addClass($this.config.normaltext_color_class);
            jElem.removeClass($this.config.placeholder_color_class);
        }
    },
    placeDefaultText: function(jElem) {

        var $this = this,
            placeholder = jElem.attr('placeholder');

        if (jElem.val() == "" || jElem.val() == null || jElem.val() == undefined || (jElem.val() == jElem.attr('placeholder') && $this.config.isIE == 'MSIE')) {
            jElem.removeClass($this.config.normaltext_color_class);
            jElem.addClass($this.config.placeholder_color_class);
            jElem.val(placeholder);
        } else {
            jElem.removeClass($this.config.placeholder_color_class);
            jElem.addClass($this.config.normaltext_color_class);
        }
    },
    callBackPlaceDefaultText: function(jElem) {

        var $this = this;

        if ($this.config.isIE) {
            $this.placeDefaultText(jElem);
        };
    },
    validateDefaultText: function(elem) {
        var jElem = $(elem);
        var $this = this;
        if (jElem.val() == 0 || jElem.val() == "" || jElem.val() == undefined || (jElem.val() == jElem.attr('placeholder') && $this.config.isIE == 'MSIE')) {
            return 0;
        }

        return 1;
    },
    manageValueChange: function(jElem) {}
};