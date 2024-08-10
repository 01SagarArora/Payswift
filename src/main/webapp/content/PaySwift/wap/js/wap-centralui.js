var intl_productsArr = new Array("RAIL","mdomwap","mintwap","HOLIDAY_IN","HOLIDAYS_OB","HOLIDAY_WAP");
var partialPay_productsArr = new Array("HOLIDAY_IN","HOLIDAYS_OB","HOLIDAY_WAP","mintwap","dom2");
var payopArray = {}
payopArray['payPartial'] = new Array("cc", "dc", "nb", "cashCard","atm","mw","ec","qb");
payopArray['holdAndPay'] = new Array("cc", "dc", "nb", "cashCard");
var holdAndPay_productsArr = new Array("HOLIDAY_IN","HOLIDAYS_OB","HOLIDAY_WAP","dom2");
var gaEnabled_productsArr = new Array("RAIL", "HOLIDAY_IN", "HOLIDAYS_OB");
var paymentJSON = null;
var productMap = new Object();
var productMapGAPageView = new Object();

productMap["mdomwap"] = "WAP Common Payment Page - DOM FLIGHTS";
productMap["mintwap"] = "WAP Common Payment Page - OB FLIGHTS";
productMap["mdomhotelwap"] = "WAP Common Payment Page - HOTELS";
productMap["trainngetwap"] = "WAP Common Payment Page - TRAINS";
productMap["HOLIDAY_WAP"] = "WAP Common Payment Page - HOLIDAYS";

productMapGAPageView["mdomwap"] = "/vpvw/coreTrans/DOMFlights/payment";
productMapGAPageView["mintwap"] = "/vpvw/coreTrans/OBFlights/payment";
productMapGAPageView["mdomhotelwap"] = "/vpvw/coreTrans/DOMHotels/payment";
productMapGAPageView["trainngetwap"] = "/vpvw/coreTrans/Trains/payment";
productMapGAPageView["HOLIDAY_WAP"] = "/vpvw/coreTrans/Holidays/payment";

var cardTypeArr = new Array();
cardTypeArr [0] = {cardName: "VISA", lengths: "13,16"};
cardTypeArr [1] = {cardName: "MASTER", lengths: "16"};
cardTypeArr [2] = {cardName: "DINERS", lengths: "14"};
cardTypeArr [3] = {cardName: "AMEX", lengths: "15"};
cardTypeArr [4] = {cardName: "MAEST", lengths: "16,18,19"};

var continueClass;
var typing = false;
var timer;

var YatraPaymentUI = {
		d5errorMessage : d5errorMessageJSP,
		intlWarningMessage : intlWarningMessageJSP,
		intlerrorMessage : intlerrorMessageJSP,
		amexWarningMessage : amexWarningMessageJSP,
		emiIntlErrorMessage : emiIntlErrorMessageJSP,
		
	pushGAInfoGeneric : function(paymentMethod, action) {
		try{
		var paymentJSON = getPaymentJSON();
		var productCode = paymentJSON.product;
	
		var paymentPageText = productMap[productCode];
		_gaq.push([ '_trackEvent', paymentPageText, paymentMethod, action]);
			
		}catch(e){
			//console.log(e);
		}
	},
		
		
	pushGAInfo : function() {
		try{
		var productCode = getPaymentJSON().product;
		var paymentPageText = productMap[productCode];
		paymentMethod = $("#other_payop").parent().children('span').text();
		_gaq.push([ '_trackEvent', paymentPageText, 'Payment Method', paymentMethod ]);
		}
		catch(e){}
	},
	pushGAInfoPageView : function(){
		try{
				var paymentJSON = getPaymentJSON();
				if(paymentJSON.gavpv){
					_gaq.push(['_trackPageview', paymentJSON.gavpv]);
				}
				else if(productMapGAPageView[paymentJSON.product]){
					_gaq.push(['_trackPageview', productMapGAPageView[paymentJSON.product]]);
				}
			}catch(e){
				//console.log(e);
			}
	},

	enableFirstPaymentOption : function() {

		// check if there are quickbooks
		if($("#qb_selected") && $("#qb_selected").val()=="true"){
		}
		else{
		
			var firstPayOptionElem = $('#cpmt_tabmenu li')[1];
			var payOption = $(firstPayOptionElem).attr('paymentOption');
			var tabPayOption = "#tab_" + payOption;
			$(firstPayOptionElem).addClass('showcardInfo');
			//$("input:radio[name=radio-common]").checkboxradio();
			//$("input:radio[name=radio-common]").prop('checked', false).checkboxradio('refresh');
			//$("#cpmt_tabmenu input:radio[name=radio-common]:first").prop('checked', true)
			//$("input:radio[name=radio-common]").checkboxradio('refresh');
			
			// Push GA Info For First Payment Option
			YatraPaymentUI.pushGAInfo();
			
			// remove preset payment Details
			YatraPaymentUI.clearPaymentDetails(tabPayOption);
		}
	},

	clearValidationError : function() {
		$("body").find("div.pmt_errorlist").remove();
	},

	resetDropDown : function(div) {
		try{
		$(div)[0].selectedIndex = 0;
		$(div).selectmenu('refresh');
		//$("select").each(function(i, t) {console.log(typeof $(t).selectmenu)})
		$("select").selectmenu('refresh');
		}catch(e){}
	},

	initializeBanksPaymentUI : function() {
		$('#tab_nb input:radio[name="prBank"]:nth(0)').attr("checked", "checked");
		$('#tab_nb input[name="bankCode"]').val($('input[name="prBank"]:checked').val());

		$('#tab_atm input:radio[name="atmOptns"]:nth(0)').attr("checked", "checked");
		$('#tab_atm input[name="bankCode"]').val($('input[name="atmOptns"]:checked').val());

		$('#tab_emi input:radio[name="emiBank"]:nth(0)').attr("checked", "checked");

	},

	isPartialOrHnpActive:false,
	initializePartialPaymentUI : function() {
	try{
		$("#hnp-mode").hide();
		$("#pp-mode").hide();
		var index = 0;
		var paymentJson= getPaymentJSON();
		$("#payNowAmount").html(paymentJson.amount);
		$("input:radio[name=paymentmode]").checkboxradio();
		var holdAndPayJSON = paymentJson && paymentJson.holdAndPay ? paymentJson.holdAndPay : null;
		var partialPaymentJSON = paymentJson && paymentJson.partialpayment ?paymentJson.partialpayment : null;
		if ( partialPaymentJSON && YatraPaymentUI.isPartialPaySupportedOnProduct() && partialPaymentJSON.isEligibleForPartial == "true") {
			index = 1;
			this.generatePartialPayDiv(partialPaymentJSON);
			$("input:radio[name=paymentmode]").prop('checked', false).checkboxradio('refresh');
			$("input:radio[name=paymentmode]:first").prop('checked', true).checkboxradio('refresh');
		}
		if (holdAndPayJSON && YatraPaymentUI.isHoldAndPaySupportedOnProduct() && holdAndPayJSON.isEligibleForHold == "true") {
			index = 2;
			this.generateHoldAndPayDiv(holdAndPayJSON);
			$("input:radio[name=paymentmode]").prop('checked', false).checkboxradio('refresh');
			$("input:radio[value=holdAndPay]").prop('checked', true).checkboxradio('refresh');
		}
		if (index==0) {
			$("#payment-modes-text").hide();
			$("#payment-modes").hide();
		} else {
			$("#payment-modes-text").show();
			$("#payment-modes").show();
			this.isPartialOrHnpActive = true;
		} 
		
		this.userSelectedMode = $("input:radio[name=paymentmode]:checked").val();
	}catch(e){}
	},
	checkPaymentModes : function(paymentMode) {
		var currentPayop = YatraPaymentUI.getPaymentOption();
		if(!currentPayop || currentPayop.trim()=="") {
			this.userSelectedMode = $("input:radio[name=paymentmode]:checked").val();
			if(typeof paymentMode!= 'undefined')YatraPaymentUI.notifyPartial(paymentMode);
			return;
		}
		if(paymentMode && paymentMode=="payFull") this.userSelectedMode = paymentMode;
		paymentMode = (paymentMode) ? paymentMode : this.userSelectedMode;
		if(isPaymentModeValid(paymentMode,currentPayop)){
			this.userSelectedMode = paymentMode;
			$("input[name=paymentmode]:radio").prop('checked', false).checkboxradio('refresh');
			$("input:radio[value="+paymentMode+"]").prop('checked', true).checkboxradio('refresh');
			YatraPaymentUI.notifyPartial(paymentMode);
		}
		else{
			setTimeout(function() {selectPayNow()}, 250);
		}
		return false;
		function isPaymentModeValid(mode,payop){
			if(mode=='payFull') return false;
			var payopJson = payopArray[mode];
			for (j = 0; j < payopJson.length; j++) {
				if(payop==payopJson[j]){
					return true;
				}
			}
			return false;
		}
		function selectPayNow(){
		$("input[name=paymentmode]:radio").prop('checked', false).checkboxradio('refresh');
		$("input:radio[name=paymentmode]:first").prop('checked', true).checkboxradio('refresh');
		YatraPaymentUI.notifyPartial('payFull');
		}
	},

	resetBillingDiv : function(payop) {
		var payopDiv = '#' + payop + '_';
		$(payopDiv + 'ba1').val("");
		IE_Placeholder.callBackPlaceDefaultText($(payopDiv + 'ba1'));
		$(payopDiv + 'bcity').val("");
		IE_Placeholder.callBackPlaceDefaultText($(payopDiv + 'bcity'));
		$(payopDiv + 'bstate').val("");
		IE_Placeholder.callBackPlaceDefaultText($(payopDiv + 'bstate'));
		$(payopDiv + 'bpin').val("");
		IE_Placeholder.callBackPlaceDefaultText($(payopDiv + 'bpin'));
		$(payopDiv + 'bcountry_id').val("");
		IE_Placeholder.callBackPlaceDefaultText($(payopDiv + 'bcountry_id'));
		YatraPaymentUI.resetDropDown(payopDiv + 'bcountry');
		$(payopDiv + 'baisd').val("");
		IE_Placeholder.callBackPlaceDefaultText($(payopDiv + 'baisd'));
		$(payopDiv + 'bamob').val("");
		IE_Placeholder.callBackPlaceDefaultText($(payopDiv + 'bamob'));
	},

	resetCreditCardCvv : function() {
		var cvvClass = $("#cc_CvvUrl_id").attr('class');
		$('#cc_CvvUrl_id').removeClass(cvvClass);
		$('#cc_CvvUrl_id').addClass($("#cc_CvvUrl_id").attr("defaultVal"));
		$("#cc_cvvImageText_id").text($("#cc_cvvImageText_id").attr("defaultVal"));
		$('#cc_cvv_id').attr("maxlength", 3);
		$('#cc_cvv_id').attr("data-validation", 'required|regType[number]|lenCheck[3]');
	},

	resetCreditCard : function() {

		//Remove preset cvv Image and ImageText
		YatraPaymentUI.resetCreditCardCvv();

		//Reset Biling Address Div
		$("#cc_AddressShowHide").hide();
		$("#cc_pmt_addressId").hide();

		//Reset Expiry Date dropdown 
		YatraPaymentUI.resetDropDown('#cc_expm_id');
		YatraPaymentUI.resetDropDown('#cc_expy_id');
		$('#cc_expm_id').prop('selectedIndex', 0);
		$('#cc_expMonth_id').val("");
		$('#cc_expYear_id').val("");

		//Reset QuickBook Save card checkbox
		if ($('#cc_qbEnabled').val() == 'true') {
			$('#cc_saveQBCard').attr('checked', true);
			$('#cc_saveQuickBookCard_id').val('true');
		}

		//Reset Billing Country
		YatraPaymentUI.resetDropDown('#cc_bcountry');
		$('#cc_bcountry_id').val("");

		// Set Error Block to Display None
		$('#cc_divErrMsgBlock').css('display', 'none');
	},

	resetDebitCard : function() {

		//Reset Biling Address Div
		$("#dc_pmt_addressId").hide();

		//Reset Expiry Date dropdown
		YatraPaymentUI.resetDropDown('#dc_expm_id');
		YatraPaymentUI.resetDropDown('#dc_expy_id');
		$('#dc_expMonth_id').val("");
		$('#dc_expYear_id').val("");

		//Reset QuickBook Save card checkbox
		if ($('#dc_qbEnabled').val() == 'true') {
			$('#dc_saveQBCard').attr('checked', true);
			$('#dc_saveQuickBookCard_id').val('true');
		}

		//Reset Billing Country
		YatraPaymentUI.resetDropDown('#dc_bcountry');
		$('#dc_bcountry_id').val();

		//Reset ExpiryDate Message
		YatraPaymentUI.setMaestroUI('VISA');

		// Set Error Block to Display None
		$('#dc_divErrMsgBlock').css('display', 'none');
	},

	resetEMI : function() {
		//Reset Expiry Date dropdown
		YatraPaymentUI.resetDropDown('#emi_expm_id');
		YatraPaymentUI.resetDropDown('#emi_expy_id');
		$('#emi_expMonth_id').val("");
		$('#emi_expYear_id').val("");

		// Reset CardNumber and CVV
		$("#emi_cno_id").val("");
		$("#emi_cvv_id").val("");
		YatraPaymentUI.generateEmiTable();
	},

	resetRewardPoint : function() {
		YatraPaymentUI.resetDropDown('#rw_expm_id');
		YatraPaymentUI.resetDropDown('#rw_expy_id');
		$('#rw_expMonth_id').val("");
		$('#rw_expYear_id').val("");
	},

	resetQuickBookCards : function(payemntDiv) {
		if (payemntDiv != "#tab_quickBook") {
			var qb_list = $('#tab_qb input:radio[class="qb_radio"]');
			$(qb_list).each(function() {
				YatraQBPaymentUI.disableQBCVVValidation($(this));
				YatraQBPaymentUI.deselectQBCard($(this));
			});
			$('#qb_divErrMsgBlock').css('display', 'none');
		}
	},

	clearPaymentDetails : function(paymentOptiondiv) {

		// Resetting default values
		$(paymentOptiondiv + " input:text").each(function() {
			$(this).val("");
			IE_Placeholder.callBackPlaceDefaultText($(this));
			
		});

		$(paymentOptiondiv + " input:password").each(function() {
			$(this).val("");
			IE_Placeholder.callBackPlaceDefaultText($(this));
		});

		// Logic for removing fading
		$(paymentOptiondiv+" .card_logo").hide();
		//var defaultCard = $(paymentOptiondiv +" .payment-icon")[0];
		//$(defaultCard).show();

		// Reset Credit Card Details
		YatraPaymentUI.resetCreditCard();

		// Reset Debit Cards
		YatraPaymentUI.resetDebitCard();

		// Reset Banking options
		YatraPaymentUI.initializeBanksPaymentUI();

		// Reset NetBanking dropdown
		YatraPaymentUI.resetDropDown('#nprBank');

		//Reset EMI Card details
		YatraPaymentUI.resetEMI();

		// Reset Reward Points
		YatraPaymentUI.resetRewardPoint();

		//Reset Quickbook cards
		YatraPaymentUI.resetQuickBookCards(paymentOptiondiv);

		//Reset Partial Payment Div
		//YatraPaymentUI.initializePartialPaymentUI();

	},
	displayEmiTable : function(){
		$('#emiDropdown').change(function() {
			var optionSelected = $("#emiDropdown option:selected").val();
			$('#emi_emiBank_id').val(optionSelected);
			if(optionSelected != ''){
				finalName = optionSelected + '-emi';
				$('.emi-plans').show();
				$('.emi-plan-box').find('li').not('.tabel-header-footer').addClass('hide');
				$('.tabel-header-footer').removeClass('hide');
				$('.addtMsg').addClass('hide');
				$('.emi-plan-box').find('.'+optionSelected+'-emi-details').removeClass('hide');
				//$('.emi-plan-box').find('.'+optionSelected+'-emi-details').removeClass('hide');
				var radio = $('input:radio[name="' + optionSelected + '"]:nth(0)');
				radio.attr("checked", "checked");
				YatraPaymentUI.setEMITableFooterDetails(radio);
			}
			else{
				$('.emi-plans').hide();
			}
		});
	},
	contentSwitcher : function(settings) {
		var settings = {
			contentClass : '.cpmt_Paytabbox > .content', navigationId : '#cpmt_tabContainer'
		};
		$(settings.contentClass).not(':first').hide();
		$(settings.navigationId).find('li:first').addClass('active');
		YatraPaymentUI.notifyPayopToLOB();

		$(settings.navigationId).find('a').click(function(e) {
			e.preventDefault();
			var paymentOption = $(this).attr('paymentOption');
			var contentToShowDiv = '#tab_' + paymentOption;
			$(settings.navigationId).find('li').removeClass('active').addClass('dactive');
			$(this).parent('li').addClass('active').removeClass('dactive');
			$(settings.contentClass).hide().removeClass('show');
			$('#tab_' + paymentOption).show().addClass('show');

			// Enable Pay Now by default
			YatraPaymentUI.enablePayNow(paymentOption);
			
			if(showAuthDiv && (paymentOption == 'cc' || paymentOption == 'dc')) {
		       	YatraPaymentUI.disablePayNow();
		    }

			// remove preset payment Details
			YatraPaymentUI.clearPaymentDetails(contentToShowDiv);

			YatraPaymentUI.clearValidationError();
			
			YatraPaymentUI.notifyPayopToLOB();

		});
	},

	onPaymentOptionSwitch : function (selectedDiv){
		YatraPaymentUI.clearPaymentDetails(selectedDiv);
		YatraPaymentUI.setDefaultOptions(selectedDiv);
		YatraPaymentUI.clearValidationError();
		YatraPaymentUI.notifyPayopToLOB();
		//YatraPaymentUI.addDropDownValidation();
		if(YatraPaymentUI.isPartialOrHnpActive)
		YatraPaymentUI.checkPaymentModes();
	},
	addDropDownValidation : function (){
	if($("#qb_selected") && $("#qb_selected").val() == 'false'){
		var firstPayOptionElem = $('#cpmt_tabmenu li')[1];
		var payop = $(firstPayOptionElem).attr('paymentOption');
			$('#other_payop').attr("data-validation","required");	
		} else {
			$('#other_payop').attr("data-validation","");		
		}
	},
	setDefaultOptions: function (selectedDiv){
		var selectedBank = "";
		if(selectedDiv=='#tab_nb'){
			/*
			$("input:radio[name=netBankRadio]").checkboxradio();
			$("input:radio[name=netBankRadio]").prop('checked', false).checkboxradio('refresh');
			$("input:radio[name=netBankRadio]:first").prop('checked', true).checkboxradio('refresh');
			*/
			selectedBank = $("#netBankDropDowm").val();
			$('#tab_nb input[name="bankCode"]').val(selectedBank);
		}else if(selectedDiv=='#tab_atm'){
			//$("input:radio[name=atmOptns]").checkboxradio();
			//$("input:radio[name=atmOptns]").prop('checked', false).checkboxradio('refresh');
			//$("input:radio[name=atmOptns]:first").prop('checked', true).checkboxradio('refresh');
			selectedBank = $("#atmDropdown").val();
			$('#tab_atm input[name="bankCode"]').val(selectedBank);
		}else if(selectedDiv=='#tab_cashCard'){
			$("#itz-radio").checkboxradio();
			$("#itz-radio").prop('checked', true).checkboxradio('refresh');
		}else if(selectedDiv=='#tab_mw'){
			selectedBank = $("#mobileWalletDropDown").val();
			$('#tab_mw input[name="bankCode"]').val(selectedBank);
		}else if(selectedDiv=='#tab_emi'){
			//$("input:radio[name=emiBank]").checkboxradio();
			//$("input:radio[name=emiBank]").prop('checked', false).checkboxradio('refresh');
			//$("input:radio[name=emiBank]:first").prop('checked', true).checkboxradio('refresh');
			selectedBank = $("#emiDropdown").val();
			$('#tab_emi input[name="bankCode"]').val(selectedBank);
		}
		
	},
	getPaymentOption : function() {
		var payop = $('#other_payop').val();
		if(payop == "" && $("#qb_section").is(":visible") && $('#tab_qb input:radio[class="qb_radio"]:checked').length > 0){
			payop = "qb";
		}
		return payop;
	},
	
	notifyPayopToLOB : function () {
		var paymentOption = YatraPayment.getPayOp();
		if(paymentOption != undefined && typeof processPayOp != 'undefined') {
			processPayOp(paymentOption);
		}
	},

	checkNaN : function(field) {
		var inputVal = $(field).val();
		if (isNaN(inputVal)) {
			var newInputVal = parseInt(inputVal)
			if (isNaN(newInputVal)) {
				newInputVal = '';
			}
			$(field).val(newInputVal);
		}
	},

	disableSpace : function(field) {
		//Not allow spaces.
		//$(field).val($(field).val().replace(/\s/g, ''));
		$(field).val($(field).val().replace(/ /g, ''));
	},

	getCardType : function(cardNumber) {
		var cno = (cardNumber + '').replace(/\s/g, ''); //remove space
		if ((/^(4)/).test(cno)) {
			return 'VISA'; //VISA begins with 4, and length is 13 or 16.
		} else if ((/^(51|52|53|54|55|222)/).test(cno) ) {
			return 'MASTER'; //MasterCard beigins with 51-55, and length is 16.
		} else if ((/^(34|37)/).test(cno)) {
			return 'AMEX'; //AMEX begins with 34 or 37, and length is 15.	
		} else if ((/^(50|56|57|58|6)/).test(cno)) {
			return 'MAEST'; //Maestro begins with 50 or 56 or 57 or 58 or 6, and length is 16 or 18 or 19.	
		} else if ((/^(300|301|302|303|304|305|36|38|3095)/).test(cno)) {
			return 'DINERS'; //Diners Club begins with 300-305 or 36 or 38, and length is 14.
		}
		return null; //unknown type
	},

	setMaestroUI : function(cardType) {
		if (cardType == 'MAEST') {
			$('#dc_cvvMsg_id').show();

			$('#dc_cvv_id').attr('data-validation', 'checkCvv');
			$('#dc_cvv_id').attr('data-msginfo', 'Enter a valid cvv');

			$('#dc_expm_id').attr('data-validation', '');
			$('#dc_expm_id').attr('data-msginfo', '');

			$('#dc_expy_id').attr('data-validation', 'checkExpiryDate[dc]');
			$('#dc_expy_id').attr('data-msginfo', 'Invalid Date');
		} else {
			$('#dc_cvvMsg_id').hide();

			$('#dc_cvv_id').attr('data-validation', 'required|regType[number]|lenCheck[3]');
			$('#dc_cvv_id').attr('data-msginfo', '* required|* number only|* invalid cvv length');

			$('#dc_expm_id').attr('data-validation', 'required');
			$('#dc_expm_id').attr('data-msginfo', '* required');

			$('#dc_expy_id').attr('data-validation', 'required|checkExpiryDate[dc]');
			$('#dc_expy_id').attr('data-msginfo', '* required|Invalid Date');
		}
	},

	getEnabledCards : function(paymentOption) {

		if (paymentOption == 'cc')
			return cc_enabledCards;
		else if (paymentOption == 'dc') {
			return dc_enabledCards;
		} else if (paymentOption == 'rewards') {
			return rw_enabledCards;
		} else if (paymentOption == 'emi') {

			//var emiBank = $('input:radio[name="emiBank"]').val();
			var emiBank = $("#emiDropdown").val();
			var emi_enabledCards = "";
			var bankList = "";
			if (banksArray == "")
				return;
			bankList = eval(banksArray);
			for ( var i = 0; i < bankList.length; i++) {
				if (emiBank == bankList[i].bankName) {
					emi_enabledCards = emi_enabledCards + "/" + bankList[i].code;
				}
			}
			emi_enabledCards = emi_enabledCards.substring(1);

			return emi_enabledCards;
		}

	},

	setCardValidation : function(field, ctype, payOption, enabledCards) {
		$(field).attr('data-validation',
				'required|validateLuhn|cardRegType[VISA]|supportedCard[VISA,' + payOption + ']|enabledCard[VISA,\'' + enabledCards + '\']')
		if (ctype != null) {
			var validationCriteria = 'required|validateLuhn|cardRegType[' + ctype + ']|supportedCard[' + ctype + ',' + payOption + ']|enabledCard[' + ctype
					+ ',' + enabledCards + ']';
			if (ctype == "MAEST"){
				validationCriteria = 'required|cardRegType[' + ctype + ']|supportedCard[' + ctype + ',' + payOption + ']|enabledCard[' + ctype	+ ',' + enabledCards + ']';
			}
			$(field).attr('data-validation', validationCriteria);
		}
	},

	fadeCardLogo : function(paymentOption, ctype) {
		$("#tab_"+paymentOption+" .card_logo").hide();
		if(typeof ctype == 'undefined' || ctype==null){
			//var defaultCard = $("#tab_"+paymentOption+" .payment-icon")[0];
			//$(defaultCard).show();
		}
		else
			$("#tab_"+paymentOption+" .logo_"+ctype).show();
		
	},

	setCardTypeParameter : function(ctype, payOption) {
		$("#"+payOption+"_ctype_id").val(ctype);
	},

	setCvvLogo : function(ctype) {

		if (ctype == 'AMEX') {
			var cvvClass = $("#cc_CvvUrl_id").attr('class');
			$('#cc_CvvUrl_id').removeClass(cvvClass);
			$('#cc_CvvUrl_id').addClass("PaymentSprite " + amexCvvURL);
			$('#cc_cvv_id').attr("maxlength", amexCvvLength);
			$('#cc_cvv_id').attr("data-validation", 'required|regType[number]|lenCheck[4]');
			$("#cc_cvvImageText_id").text(amexCvvImageText);
		} else {
			YatraPaymentUI.resetCreditCardCvv();
		}
	},

	isInternationalSupportedOnProduct : function() {

		var productCode = getPaymentJSON().product;
		for (j = 0; j < intl_productsArr.length; j++) {
			if (productCode == intl_productsArr[j])
				return true;
		}
		return false;
	},

	isPartialPaySupportedOnProduct : function() {

		var productCode = getPaymentJSON().product;
		for (j = 0; j < partialPay_productsArr.length; j++) {
			if (productCode == partialPay_productsArr[j])
				return true;
		}
		return false;
	},

	isHoldAndPaySupportedOnProduct : function() {

		var productCode = getPaymentJSON().product;
		for (j = 0; j < holdAndPay_productsArr.length; j++) {
			if (productCode == holdAndPay_productsArr[j])
				return true;
		}
		return false;
	},

	showErrorMessage : function(divId, errorMsg) {
		if(errorMsg && errorMsg!="" && errorMsg!=null){
			$(divId + 'divWarningMsgBlock').css('display', 'none');
			$(divId + 'divErrMsgBlock').html(errorMsg);
			$(divId + 'divErrMsgBlock').css('display', '');
		}
	},

	showInternationalWarning : function(payOption) {
		var divId = "#" + payOption + "_";

		$(divId + 'divErrMsgBlock').css('display', 'none');
		$(divId + 'divWarningMsgBlock').html(YatraPaymentUI.amexWarningMessage);
		$(divId + 'divWarningMsgBlock').css('display', '');
	},

	setDefaultClassForContinue : function() {
		try{
			var continueDivId = '#' + getContinueButtonId();
			continueClass = $(continueDivId).attr('class');
		}catch(e){e.message};
	},

	enablePayNow : function(payOption) {

		var divId = "#" + payOption + "_";
		$(divId + 'divErrMsgBlock').css('display', 'none');
		$(divId + 'divWarningMsgBlock').css('display', 'none');

		var continueDivId = '#' + getContinueButtonId();
		$(continueDivId).parent().removeClass('ui-state-disabled');
		$(continueDivId).prop("disabled", false);
	},

	disablePayNow : function(payOption, errorMsg) {

		var divId = "#" + payOption + "_";
		YatraPaymentUI.showErrorMessage(divId, errorMsg);

		var continueDivId = '#' + getContinueButtonId();

		$(continueDivId).prop("disabled", true);
		$(continueDivId).parent().addClass("ui-state-disabled");
	},
	
	isLengthValid : function(cardType, cardNo){
		
		var cardIndex = -1;
		for (var j=0; j< cardTypeArr.length; j++) {
			if (cardType.toLowerCase() == cardTypeArr[j].cardName.toLowerCase()) {
				cardIndex = j;
				break;
			}
		}

		lengths = cardTypeArr[cardIndex].lengths.split(",");
		for (var j=0; j<lengths.length; j++) {
			if (cardNo.length == lengths[j]) 
				return true;
		}
		
		return false;
	},

	showBillingAddressDiv : function(payOption, ctype, cardNo) {
		
			var divId = '#' + payOption + '_';
			var isCardInternational_Id = divId + 'isCardInternational_id';
			var addressDiv = divId + 'pmt_addressId';
	
			if (ctype != null && YatraPaymentUI.isLengthValid(ctype, cardNo)) {
	
				// Set PayNow Enabled By Default
				YatraPaymentUI.enablePayNow(payOption);
	
				//Add Product Check
				var isProdInternational = YatraPaymentUI.isInternationalSupportedOnProduct();
	
				// Amex Card Type
				if (ctype == 'AMEX') {
					YatraPaymentUI.enablePayNow(payOption);
					if (isProdInternational == false) {
						YatraPaymentUI.showInternationalWarning(payOption);
						$(isCardInternational_Id).val('false');
						return;
					}
	
					if (!$('#cc_AddressShowHide').is(':visible')) {
						$('#cc_AddressShowHide input:radio[name="address_check_rdo"]:nth(0)').prop('checked', true).checkboxradio('refresh');
						$('#cc_AddressShowHide input:radio[name="address_check_rdo"]').checkboxradio('refresh');
					}
	
					$("#cc_AddressShowHide").show();
				} else {
	
					//Non Amex Card Type			
					$(divId + "AddressShowHide").hide();
	
					if (ctype == 'VISA' || ctype == 'MASTER') {
	
						var bin = parseInt(cardNo.substring(0, 6));
						var cardTypeURL = "/PaySwift/isCardInternational.htm";
						$.ajax({
							url : cardTypeURL,
							type : 'POST',
							data : "bin=" + bin,
							cache : false,
							async: false, 
							success : function(resp) {
	
								var res = $.parseJSON(resp);
								var serverDate = res.serverDate;
								if ("true" == res.isCardInternational) {
	
									// If product does not support international payment
									if (isProdInternational == false) {
	
										var errorMsg = YatraPaymentUI.intlerrorMessage;
										YatraPaymentUI.disablePayNow(payOption, errorMsg);
										$(isCardInternational_Id).val('false');
										return;
									}
									
									if (payOption == 'emi') {
										YatraPaymentUI.disablePayNow(payOption, YatraPaymentUI.emiIntlErrorMessage);
										$(isCardInternational_Id).val('false');
										return;
									} 
	
									// check for D-5 travelDate
									var travelCheck = YatraPaymentUI.checkForTravelDate(payOption, serverDate);
									if (travelCheck == "true") {
										$(addressDiv).show();
										$(isCardInternational_Id).val('true');
									} else {
										$(addressDiv).hide();
										$(isCardInternational_Id).val('false');
									}
	
								} else if ("false" == res.isCardInternational) {
									$(addressDiv).hide();
									$(isCardInternational_Id).val('false');
									YatraPaymentUI.enablePayNow(payOption);
								} else if ("error" == res.isCardInternational) {
									$(addressDiv).hide();
									$(isCardInternational_Id).val('false');
									YatraPaymentUI.enablePayNow(payOption);
								}
							}, error : function(request, status, error) {
								$(addressDiv).hide();
								$(isCardInternational_Id).val('false');
								YatraPaymentUI.enablePayNow(payOption);
							}
						});
					} else {
						$(isCardInternational_Id).val('false');
					}
	
				}//else ends here
			} else {
	
				$("#cc_AddressShowHide").hide();
				$(addressDiv).hide();
				$(isCardInternational_Id).val('false');
				YatraPaymentUI.enablePayNow(payOption);
			}
	
	},

	checkForTravelDate : function(payOption, serverDate) {
		//Format : MM/dd/yyyy hh:mm:ss aa
		var departDateVal = getPaymentJSON().international.travelDate;
		var departDate = '';

		if (departDateVal == null || departDateVal == '') {
			YatraPaymentUI.disablePayNow(payOption, YatraPaymentUI.d5errorMessage);
			return "false";
		} else {
			departDate = new Date(departDateVal);
		}

		var paymentDate;
		if (serverDate == null || serverDate == '')
			paymentDate = new Date();
		else
			paymentDate = new Date(serverDate);

		if (((departDate.getTime() - paymentDate.getTime()) / (24 * 3600 * 1000)) <= 5) {
			YatraPaymentUI.disablePayNow(payOption, YatraPaymentUI.d5errorMessage);
			return "false";
		} else {
			return "true";
		}

		return "true";
	},

	interateRewardcardList : function(cardNum, len, binList) {
		for (i = 0; i < len; i++) {
			var exp = new RegExp("^" + binList[i]);
			if (exp.test(cardNum))
				rwprefixValid = true;
		}
		return rwprefixValid;
	},

	validateRewardCardNumber : function(cardNo) {
		var rewardBinData = eval(rewardCardsBinList);
		rwprefixValid = false;

		for ( var i = 0; i < rewardBinData.length; i++) {

			var binList = rewardBinData[i][1];
			rwprefixValid = YatraPaymentUI.interateRewardcardList(cardNo, binList.length, binList);
			if (rwprefixValid) {
				rwConversion = rewardBinData[i][2];
				break;
			}
		}

		// not a valid reward card number
		if (!rwprefixValid) {
			$('#error-rwCnoValid').show();
			$('.card_logo').addClass('logo-pos-changed');
		}
	},

	calRewardAmount : function(discountAMT) {
		var rewardsAmt = Math.floor(discountAMT * parseInt($("#rw_reward_points_id").val()) / 100);
		if (!isNaN(rewardsAmt)) {
			$("#rw_reward_amount_id").val(rewardsAmt);
		}
	},

	getParamValue : function(key) {
		var payOp = YatraPaymentUI.getPaymentOption()
		return $("#tab_"+payOp+" input[name=" + key + "]").val();
	},

	generateEMILogos : function(emiBank) {

		var bankList = "";
		if (banksArray == "")
			return;

		bankList = eval(banksArray);
		$("#emiCardLogos").empty();
		for ( var i = 0; i < bankList.length; i++) {
			if (emiBank == bankList[i].bankName) {
				$("#emiCardLogos").append("<i class='payment-icon " + bankList[i].logoURL + " logo_" + bankList[i].code + " card_logo' title='" + bankList[i].code + "' style='display: none;' ></i>");
			}
		}
	},

	disableNewCreditCard : function() {
		$('#cc_newCard_id').prop('checked', false);
		$('#cc_newCard_id + .box > .tick').parents('label').addClass('selChecked');
		$("#cc_newCard").hide();
		$('#tab_cc_qb > input[name="payop"]').val('qb');
	},

	disableNewDebitCard : function() {

		$('#dc_newCard_id').prop('checked', false);
		$('#dc_newCard_id + .box > .tick').parents('label').addClass('selChecked');
		$("#dc_newCard").hide();
		$('#tab_dc_qb > input[name="payop"]').val('qb');
	},

	disableQBCVVForCreditCard : function(qbElem) {
		var qbCVVElem = $(qbElem).parent().parent().parent().find('.qb_ccCVV');
		$(qbCVVElem).val('');
		$(qbCVVElem).prop("disabled", true);
		$(qbCVVElem).attr('data-validation', '');
		$(qbCVVElem).attr('data-msginfo', '');
	},

	disableQBCVVForDebitCard : function(qbElem) {
		var qbCVVElem = $(qbElem).parent().parent().parent().find('.qb_dcCVV');
		$(qbCVVElem).prop("disabled", true);
		$(qbCVVElem).attr('data-validation', '');
		$(qbCVVElem).attr('data-msginfo', '');
	},

	enableQBCVVForCheckedCreditCard : function(qbCardElem) {

		var qbCardCVVElem = $(qbCardElem).parent().parent().parent().find('.qb_ccCVV');
		$(qbCardCVVElem).prop("disabled", false);
		var length = $(qbCardCVVElem).attr('maxlength');
		$(qbCardCVVElem).parents('li').find('.selChecked').removeClass('selChecked');
		$(qbCardCVVElem).attr('data-validation', 'required|regType[number]|lenCheck[' + length + ']');
		$(qbCardCVVElem).attr('data-msginfo', '* required|* number only|* invalid cvv length');
	},

	enableQBCVVForCheckedDebitCard : function(qbCardElem) {

		var cardType = $(qbCardElem).attr("cardType");

		var qbCardCVVElem = $(qbCardElem).parent().parent().parent().find('.qb_dcCVV');
		$(qbCardCVVElem).prop("disabled", false);
		var length = $(qbCardCVVElem).attr('maxlength');
		$(qbCardCVVElem).parents('li').find('.selChecked').removeClass('selChecked');

		if (cardType == 'MAESTRO') {
			$(qbCardCVVElem).attr('data-validation', 'checkCvv');
			$(qbCardCVVElem).attr('data-msginfo', 'Enter a valid cvv');
		} else {
			$(qbCardCVVElem).attr('data-validation', 'required|regType[number]|lenCheck[' + length + ']');
			$(qbCardCVVElem).attr('data-msginfo', '* required|* number only|* invalid cvv length');
		}
	},

	setElementInnerHtml : function(elemId, elemValue) {
		var elem = document.getElementById(elemId);
		if (elem != null) {
			$(elem).innerHTML = elemValue;
		}
	},

	setElementText : function(elemId, elemValue) {
		var elem = document.getElementById(elemId);
		if (elem != null) {
			$(elem).text(elemValue);
		}
	},
	generateHoldAndPayDiv : function(modeJson) {
		$("#hnp-mode").show();
		$("#holdAndPayAmount").html(modeJson.holdAndPayNowAmount);
	},
	generatePartialPayDiv : function(modeJson) {
		$("#pp-mode").show();
		$("#partialPayNowAmount").html(modeJson.partialPayNowAmount);
		$("#partialLaterAmount").html(modeJson.partialPayLaterAmount);
	},

	getMonthlyInterest : function(amount, emiTenure,emiInterestRate) {
		var monthlyInterest = emiInterestRate / 12;
		var R = monthlyInterest / 100;
		var C = 1 / Math.pow(1 + R, emiTenure);
		var B = 1 - C;
		var RR = R / B;
		var EMI = Math.ceil(amount * RR);

		return EMI;
	},

	getBankInterest : function(emiInstallment, amount, emiTenure) {

		return (emiInstallment * emiTenure) - amount;
	},

	generateEmiTable : function() {
		var amount = $('input[name="amount"]').val();
		if(amount == undefined || amount == null || amount == '' || amount <= 0) {
			var paymentJSON = getPaymentJSON();
			amount = paymentJSON.amount;
		}
		$(".emi-plan-box li").each(function() {
		    var tenure = $(this).find('.emi-tenure-radio').val();
		    if(tenure != null && tenure != '' && tenure != undefined) {
		    	var interestRate = $(this).find(".interestRate").html();
				var emiInstallment = YatraPaymentUI.getMonthlyInterest(amount, tenure, interestRate);
		    	$(this).find(".monthlyEMI").html(YatraPayment.formatAmount(emiInstallment));
		    	var bankInterest = YatraPaymentUI.getBankInterest(emiInstallment, amount, tenure);
		    	$(this).find(".InterestPaid").html(YatraPayment.formatAmount(bankInterest));
		    	var totalAmount = emiInstallment * tenure;
		    	totalAmount = Math.round(totalAmount * 100) / 100;
		    	$(this).find(".total-payable-amount").html(YatraPayment.formatAmount(totalAmount));
		    }
		});
		var radioName = $("#emiDropdown option:selected").val();
		if(radioName != null && radioName != '' && radioName != undefined) {
			$('#'+radioName+'-3').trigger('click');
			$('.'+radioName+'-3'+' label').addClass('ui-radio-on'); 
		}
		
	},
	setEMITableFooterDetails : function (selectedRadio) {
		var tenure = selectedRadio.val();
		var bankCode = selectedRadio.attr("name");
		$("#emi_emiTenure_id").val(tenure);
		var interestPaid = $('.' + bankCode + '-' + tenure).find(".InterestPaid").html();
		$('#cpmt_emiInterest').html(interestPaid);
		
		var amount = $('input[name="amount"]').val();
		if(amount == undefined || amount == null || amount == '' || amount <= 0) {
			var paymentJSON = getPaymentJSON();
			amount = paymentJSON.amount;
		}
		
		$('#cpmt_tenure').html(YatraPayment.formatAmount(tenure));
		$('#cpmt_amount').html(YatraPayment.formatAmount(amount));
	},

	notifyPartial : function(partialPayOption) {
		if(typeof processPartialPayment != "undefined"){
		processPartialPayment(partialPayOption);
		}
		
	},

	renderOthers : function() {
		return false; // abhi to be removed
		if (!enableOthers) {
			$('#cpmt_others').hide();
		}
	},
	
    setCardExpData: function(target){
		$('#'+$(target).attr('paramId')).val($(target).val());
	},
	
	onAmountChange : function() {
		YatraPaymentUI.generateEmiTable();
		YatraEWalletPaymentUI.setEWalletUI();
	},
	
	onLogin : function() {
		YatraQBPaymentUI.updateQBDiv();
		YatraEWalletPaymentUI.updateEWalletHTML();
	},
	
	onLogout : function() {
		YatraQBPaymentUI.updateQBDiv();
		YatraEWalletPaymentUI.updateEWalletHTML();
	},
	
	beforeLogout : function() {
		YatraEWalletPaymentUI.reverseAuth();
	},
	
	handleAMEXQBCard : function(qbCardElem, isProdInternational,payOp){
		
		var saveQBCardAddress = '#qb_saveQuickBookCard';
		var isCardInternational_Id = '#qb_isCardInternational';
		
		//1. Enable Pay Now for QuickBook
		YatraPaymentUI.enablePayNow(payOp);
		
		//2. International cards not eligible for product
		if (isProdInternational == false) {
			YatraPaymentUI.showInternationalWarning(payOp);
			$(isCardInternational_Id).val('false');
			$(saveQBCardAddress).val('false');
			return;
		}
		
		//3. Show Amex Address Div
		var amexAddressDiv = $(qbCardElem).parent().parent().find('#qb_AddressShowHide');
		if (!$(amexAddressDiv).is(':visible')) {
			$(amexAddressDiv).find('input:radio[name="qb_address_check_rdo"]:nth(0)').prop('checked', true);
			$(amexAddressDiv).find('input:radio[name="qb_address_check_rdo"]').checkboxradio('refresh');
			$(isCardInternational_Id).val('false');
			$(saveQBCardAddress).val('false');
		}			
		$(amexAddressDiv).show();
	},
	
	showQuickBookBillingDiv : function(addressDiv, isCardInternational_Id, payOp, saveQBCardAddress){
		$(addressDiv).show();//1. show Billing address div
		$(isCardInternational_Id).val('true');//2. set isCardInternational
		YatraPaymentUI.enablePayNow(payOp);//3. enablePayNow
		$(saveQBCardAddress).val('true');//4. set saveAddressCard flag
	},
	
	hideQuickBookBillingDiv : function(addressDiv, isCardInternational_Id,saveQBCardAddress){
		$(addressDiv).hide(); //1. hide Billing address div
		$(isCardInternational_Id).val('false'); //2. set isCardInternational
		$(saveQBCardAddress).val('false'); //3. set saveAddressCard flag
	},
	
	handleDomesticQBCard : function(addressDiv, isCardInternational_Id, payOp, saveQBCardAddress){
		$(addressDiv).hide();//1. hide Billing address div
		$(isCardInternational_Id).val('false');	//2. set isCardInternational false
		YatraPaymentUI.enablePayNow(payOp);//3. enable PayNow
		$(saveQBCardAddress).val('false');//4. set saveAddressCard flag as false
	},
	
	handleInternationalQBCard : function(qbCardElem){

		var cardAccessType = $(qbCardElem).attr("cardAccessType");
		var isAddressSaved = $(qbCardElem).attr("isAddressSaved");
		var cardNumber = $(qbCardElem).attr("cardNumber");
		var cardType = $(qbCardElem).attr("cardbrand");
		var addressDiv = $(qbCardElem).parent().parent().find('#qb_pmt_addressId');
		var saveQBCardAddress = '#qb_saveQuickBookCard';
		var isCardInternational_Id = '#qb_isCardInternational';
		var payOp = 'qb';
		var isProdInternational = YatraPaymentUI.isInternationalSupportedOnProduct();
		
		if(cardAccessType == null || cardAccessType=="null" || cardAccessType == "" || cardAccessType == "undefined"){
			//Old cases where cardAccessType not available
			
			if(cardType == "AMEX"){
				
				YatraPaymentUI.handleAMEXQBCard(qbCardElem,isProdInternational,payOp);
				
			}else if (cardType == 'VISA' || cardType == 'MASTER') {	
				
				var bin = parseInt(cardNumber.substring(0, 6));
				var cardTypeURL = "/PaySwift/isCardInternational.htm";
				$.ajax({
					url : cardTypeURL, type : 'POST', data : "bin=" + bin, cache : false, success : function(resp) {

						var res = $.parseJSON(resp);
						var serverDate = res.serverDate;
						if ("true" == res.isCardInternational) {

							// If product does not support international payment
							if (isProdInternational == false) {

								var errorMsg = YatraPaymentUI.intlerrorMessage;
								YatraPaymentUI.disablePayNow(payOp, errorMsg);
								$(isCardInternational_Id).val('false');
								return;
							}

							// check for D-5 travelDate
							var travelCheck = YatraPaymentUI.checkForTravelDate(payOp , serverDate);
							if (travelCheck == "true") {
								
								YatraPaymentUI.showQuickBookBillingDiv(addressDiv, isCardInternational_Id, payOp, saveQBCardAddress);
								
							} else {
								
								YatraPaymentUI.hideQuickBookBillingDiv(addressDiv, isCardInternational_Id, saveQBCardAddress);
							}

						} else if ("false" == res.isCardInternational || "error" == res.isCardInternational) {
							YatraPaymentUI.handleDomesticQBCard(addressDiv, isCardInternational_Id, payOp, saveQBCardAddress);
						} 
					}, error : function(request, status, error) {
						YatraPaymentUI.handleDomesticQBCard(addressDiv, isCardInternational_Id, payOp, saveQBCardAddress);
					}
				});
				
			}else {
				
				// Card is International or not, cannot be determined. Treat as Domestic Card
				YatraPaymentUI.handleDomesticQBCard(addressDiv, isCardInternational_Id, payOp, saveQBCardAddress);
			}
			
		}else{
			
			if("INT" == cardAccessType){
				
				if (cardType == 'VISA' || cardType == 'MASTER' || cardType == 'AMEX') {	
					
					// check for D-5 travelDate
					var travelCheck = YatraPaymentUI.checkForTravelDate(payOp, null);
					
					if (travelCheck == "true") {
						
						if(isAddressSaved == "false"){
							YatraPaymentUI.showQuickBookBillingDiv(addressDiv, isCardInternational_Id, payOp, saveQBCardAddress);
						}
						else {
							YatraPaymentUI.hideQuickBookBillingDiv(addressDiv, isCardInternational_Id, saveQBCardAddress);
						}

					} else {
						YatraPaymentUI.hideQuickBookBillingDiv(addressDiv, isCardInternational_Id, saveQBCardAddress);
					}
				}
			}else if("DOM" == cardAccessType){
				YatraPaymentUI.handleDomesticQBCard(addressDiv, isCardInternational_Id, payOp, saveQBCardAddress);
			}
			
		}
	},
	
	clearAddressDiv : function(qb_li){
		$(qb_li).find('#qb_AddressShowHide').hide();
		$(qb_li).find('#qb_pmt_addressId').hide();
	},
	resetPayOpsDropdown : function(obj){
		var length = obj.length;
		for(var i=0; i<length; i++){
			$("#"+obj[i]+" option:eq(0)").prop("selected", true);
			$('#'+obj[i]).selectmenu();
			$("#"+obj[i]).selectmenu('refresh', true);
		}
		$('.other_option_div').hide();
		$(".detail-emi").hide();
	},
	initialise : function() {
		$('.emi-tenure-radio').on('click',function(){
			YatraPaymentUI.setEMITableFooterDetails($(this));
			$(".addtMsg").removeClass('hide');
		});

		YatraPayment.initialisePage();
		
		// Logic to hide Others in case of basic PayOptions
		YatraPaymentUI.renderOthers();

		// Set default State for NetBanking/ATM/EMI
		YatraPaymentUI.initializeBanksPaymentUI();

		// Set default State for Partial Payment
		YatraPaymentUI.initializePartialPaymentUI();

		// Logic to generate EMI Interest Table
		YatraPaymentUI.generateEmiTable();
		
		// Login to generate Ewallet Div
		YatraEWalletPaymentUI.init();
		
		// Logic to get QuickBook cards
		YatraQBPaymentUI.updateQBDiv();
		
		YatraPaymentUI.pushGAInfoPageView();
		// Enable First Payment Option
		YatraPaymentUI.enableFirstPaymentOption();
		YatraPaymentUI.displayEmiTable();
		//Disable Custom RadioBox and CheckBox on IE version lesser than 9
		/*if ($.browser.msie && parseInt($.browser.version) < 9) {
			$("input").parent().removeClass("custom-checkbox");
			$("input").parent().removeClass("custom-Radiobox");
		}*/

		// Logic for Payment Tab switching
		//abhi YatraPaymentUI.contentSwitcher();
		
		$('#cc_cno_id, #dc_cno_id, #emi_cno_id, #rw_cno_id').bind("contextmenu", function(e) {
			e.preventDefault();
		});

		// Logic for click event on 'Others' Payment Option
		$('#cpmt_others').click(function() {
			$(this).hide();
			$(this).next('li').show();
			var nextPayOp = $('#cpmt_otherPayOp').find('ul').children('li:first').children('a').attr('paymentOption');
			$('#tab_' + nextPayOp).show();
			$('#cpmt_otherPayOp').find('ul').find('li:first').addClass('active');
			YatraPaymentUI.notifyPayopToLOB();
		});
		
		
		
		$('#other_payop').click(function() {
			var payOption = YatraPaymentUI.getPaymentOption();
			YatraPaymentUI.enablePayNow(payOption);
			YatraPaymentUI.notifyPayopToLOB();
		});
	
		// Processing required on Card Number input
		
		$("input[processCardNo=true]").on('blur',function(){
			var cardNumber = $(this).val();
		});
		$('input[processcardno=true]').each(function() {
			
			$(this).on('input', function() {
				
				if(typing) {
					   clearInterval(timer);
					 }
				$this = $(this);
					
				timer = setTimeout(function(){
				
						//1. Not allow spaces.
						YatraPaymentUI.disableSpace($this);
			
						//2. Only digits allowed on cardNumber input
						YatraPaymentUI.checkNaN($this);
			
						//3. Get CardNumber value
						var cardNo = "";
						cardNo = $($this).val();
			
						//4. Identify CardType
						var ctype = YatraPaymentUI.getCardType(cardNo);
			
						//6. Get Payment Option
						var paymentOption = $($this).data('payop');
								
						//5. setMaestroUI
						if(paymentOption == 'dc')
							YatraPaymentUI.setMaestroUI(ctype);
			
						//7. Get Enabled Cards for Payment Option
						var enabledCards = YatraPaymentUI.getEnabledCards(paymentOption);
			
						//8. Update Card Validations as per Card Type
						YatraPaymentUI.setCardValidation($this, ctype, paymentOption, enabledCards);
			
						//9. Logic for fading
						YatraPaymentUI.fadeCardLogo(paymentOption, ctype);
			
						//10. Set 'ctype' parameter
						YatraPaymentUI.setCardTypeParameter(ctype, paymentOption);
			
						//11. Update cvvImage in case of Amex
						if (paymentOption == 'cc')
							YatraPaymentUI.setCvvLogo(ctype);
			
						//12. Logic for displaying Billing Address
						YatraPaymentUI.showBillingAddressDiv(paymentOption, ctype, cardNo);
						
						typing = false;
				
				}, 500);
				
				typing=true;
	
			});
		});
		

		$('#cc_cvv_id').on('keyup', function() {
			YatraPaymentUI.disableSpace($(this));
			YatraPaymentUI.checkNaN($(this));
			$('#cc_cvv').val($('#cc_cvv_id').val());
		});

		$('#dc_cvv_id').on('keyup', function() {
			YatraPaymentUI.disableSpace($(this));
			YatraPaymentUI.checkNaN($(this));
		});

		$('#emi_cvv_id').on('keyup', function() {

			YatraPaymentUI.disableSpace($(this));
			YatraPaymentUI.checkNaN($(this));
		});

		$('input[name=baisd]').on('keyup', function() {
			YatraPaymentUI.checkNaN($(this));
		});

		$('input[name=bamob]').on('keyup', function() {
			YatraPaymentUI.checkNaN($(this));
		});

		// Logic for Amex Issue Check
		$('input:radio[name="address_check_rdo"]').on('change', function() {

			YatraPaymentUI.resetBillingDiv('cc');
			var amexCardType = $("input[name='address_check_rdo']:checked").val();
			var addressDiv = "#cc_pmt_addressId";
			var isCardInternational_Id = "#cc_isCardInternational_id";
			if (amexCardType == 'Domestic') {
				$(addressDiv).hide();
				$(isCardInternational_Id).val('false');
				YatraPaymentUI.enablePayNow('cc');
			} else if (amexCardType == 'International') {

				// check for D-5 travelDate
				var travelCheck = YatraPaymentUI.checkForTravelDate('cc', $('#paymentDate').val());

				if (travelCheck == "true") {
					$(addressDiv).show();
					$(isCardInternational_Id).val('true');
					YatraPaymentUI.enablePayNow('cc');
				} else {
					$(addressDiv).hide();
					$(isCardInternational_Id).val('false');
				}

			}
		});		
		$('#netBankDropDowm').change(function(e) {
			//$('input:radio[name="netBankRadio"]').removeAttr('checked').checkboxradio('refresh');
			var ddVal= $(this).val()
			if (ddVal == 'Select Your Bank' || ddVal=='') {
				$('#tab_nb input:radio[name="netBankRadio"]:first').attr("checked", "checked");
				$('#tab_nb input[name="bankCode"]').val('');
			} else {
				$('#tab_nb input[name="bankCode"]').val(ddVal);
			}
		});
		
		$('#mobileWalletDropDown').change(function(e) {

			var ddVal= $(this).val()
			if (ddVal == 'Please select Option from the dropdown' || ddVal=='') {
				$('#tab_mw input[name="bankCode"]').val('');
			} else {
				$('#tab_mw input[name="bankCode"]').val(ddVal);
			}
		});
		
		
		$('#atmDropdown').change(function() {
			$('#tab_atm input[name="bankCode"]').val($(this).val());
		});
		$('#emiDropdown').change(function() {
			if($("#emiDropdown option:eq(0)").prop("selected")){
				$(".detail-emi").hide();
			} else {
				$(".detail-emi").show();
			}
			$('#tab_emi input[name="ctype"]').val($(this).val());
			YatraPaymentUI.resetEMI();
			YatraPaymentUI.generateEMILogos($(this).val());
			var payOption = YatraPaymentUI.getPaymentOption();
			YatraPaymentUI.enablePayNow(payOption);
		});


		$('select').change(function() {
			if ($(this).attr("paramId") != undefined) {
				$('#' + $(this).attr("paramId")).val($(this).val());
			}
		});

		$('#rw_cno_id').on('focus', function(e) {
			$('#error-rwCnoRequired').hide();
		});

		$('#rw_cno_id').on('input', function(e) {

			var cno = $('#rw_cno_id').val();
			if (cno.length == 8) {
				YatraPaymentUI.validateRewardCardNumber(cno);
			} else if (cno.length < 8) {
				$('.card_logo').removeClass('logo-pos-changed');
				$('#error-rwCnoValid').hide();
				rwConversion = "";
			}

		});

		$('#rw_reward_points_id').on('input', function(e) {

			YatraPaymentUI.checkNaN($(this));
			if ($('#rw_cno_id').val() == '' || $('#rw_cno_id').val().length < 8) {
				$('#error-rwCnoRequired').show();
			} else {
				YatraPaymentUI.calRewardAmount(rwConversion);
			}
		});
		
	/*	$("#continue_invalid_visa").on("click",function(){
			$("#invalid_visa_discount_pop_up").hide();
			PaymentCommon.hideBlackDiv();
			ProcessPayment.payNow();
		});

		$("#undo_invalid_visa").on("click",function(){
			$("#invalid_visa_discount_pop_up").hide();
			PaymentCommon.hideBlackDiv();
			var paymentOption = YatraPaymentUI.getPaymentOption();
			var paymentOptionDiv = '#tab_' + paymentOption;
			YatraPaymentUI.clearPaymentDetails(paymentOptionDiv);
			YatraPaymentUI.clearValidationError();
			PaymentCommon.onAmountChange();
		});*/
		
		$("#ChangePromo").on("click",function(){
			PaymentCommon.hideBlackDiv();
			$("#invalid_promo_code_discount_pop_up").hide();
			var changePromoUrl = resp.changePromoUrl;
				var paymentOption = YatraPaymentUI.getPaymentOption();
				var paymentOptionDiv = '#tab_' + paymentOption;
				YatraPaymentUI.clearPaymentDetails(paymentOptionDiv);
				YatraPaymentUI.clearValidationError();
				PaymentCommon.onAmountChange();
				window.location = resp.promo.changePromoUrl;
		});
		$("#ChangePayOpt").on("click",function(){
			PaymentCommon.hideBlackDiv();
			$("#invalid_promo_code_discount_pop_up").css({"display":"block"});
			var paymentOption = YatraPaymentUI.getPaymentOption();
			var paymentOptionDiv = '#tab_' + paymentOption;
			YatraPaymentUI.clearPaymentDetails(paymentOptionDiv);
			YatraPaymentUI.clearValidationError();
			PaymentCommon.onAmountChange();
		});
		$("#ContinueWithoutPromo").on("click",function(){
			PaymentCommon.hideBlackDiv();
			ShoppingCart.updatePromocode(0);
			$("#invalid_promo_code_discount_pop_up").hide();
			ProcessPayment.payNow();
		});
		
		$('#cc_newCard_id').click(function() {
			$('#tab_ccQB > input:radio[name=cardid]').prop('checked', false);
			$('#tab_ccQB .custom-Radiobox > input:checked + .box > .tick').parents('label').addClass('selChecked');
			$("#cc_newCard").show();
			$('#tab_cc_qb > input[name="payop"]').val('cc');
			$('#qb_newCreditCard').removeClass('selChecked');
			YatraPaymentUI.disableQBCVVValidation($('input:radio[processCardId=cc]:checked'));
			$('#qb_password_cc').val("");
			YatraPaymentUI.enablePayNow();
			YatraPaymentUI.notifyPayopToLOB();
		});

		$('#dc_newCard_id').click(function() {
			$('#tab_dcQB > input:radio[name=cardid]').prop('checked', false);
			$('#tab_dcQB .custom-Radiobox > input:checked + .box > .tick').parents('label').addClass('selChecked');
			$("#dc_newCard").show();
			$('#tab_dc_qb > input[name="payop"]').val('dc');
			$('#qb_newDebitCard').removeClass('selChecked');
			YatraPaymentUI.disableQBCVVValidation($('input:radio[processCardId=dc]:checked'));
			$('#qb_password_dc').val("");
			YatraPaymentUI.enablePayNow();
			YatraPaymentUI.notifyPayopToLOB();
		});

		$('input:radio[processCardId=cc]').on('click', function() {
			YatraPaymentUI.disableNewCreditCard();
			YatraPaymentUI.disableQBCVVValidation($('input:radio[processCardId=cc]'));
			YatraPaymentUI.enableQBCVVValidation($('input:radio[processCardId=cc]:checked'));
			YatraPaymentUI.clearValidationError();
			YatraPaymentUI.notifyPayopToLOB();
		});

		$('input:radio[processCardId=dc]').on('click', function() {
			YatraPaymentUI.disableNewDebitCard();
			YatraPaymentUI.disableQBCVVValidation($('input:radio[processCardId=dc]'));
			YatraPaymentUI.enableQBCVVValidation($('input:radio[processCardId=dc]:checked'));
			YatraPaymentUI.clearValidationError();
			YatraPaymentUI.notifyPayopToLOB();
		});

		$('input[processCVV=ccCVV]').on('keyup', function() {
			YatraPaymentUI.checkNaN($(this));
			$('#cc_cvv').val($(this).val());
		});

		$('input[processCVV=dcCVV]').on('keyup', function() {
			YatraPaymentUI.checkNaN($(this));
			$('#dc_cvv').val($(this).val());
		});

		$(".pay-mode-div").on('click', function() {
			$(".payment-mode").is(":visible") ? $(".payment-mode").hide() : $(".payment-mode").show();
		});
		$("#other_payop").bind( "change", function(event, ui) {
			//var selected = $("input:radio[name=radio-common]:checked").val();
			var selected = $(this).val();			
			$('#tab_qb input.qb_radio').prop('checked', false);
			$("#qb_selected").val('false');
			YatraPaymentUI.onPaymentOptionSwitch('#tab_'+selected);	
			YatraPaymentUI.pushGAInfo();
		});
		$("input[name=paymentmode]:radio").on('click',handleRadioChange);
	}
};
var handleRadioChange = function () {
	var valueSelected = $("input:radio[name=paymentmode]:checked").val();
	YatraPaymentUI.checkPaymentModes(valueSelected);
	return false;
};
function copyPPFormDataAndSubmit(relativeURL) {

	var payOption = YatraPaymentUI.getPaymentOption();
	$("#paymentOPT").empty();
	$("#tab_" + payOption).clone().appendTo($("#paymentOPT"));

	var creditcardAction = relativeURL + 'creditcard.htm';
	var debitcardAction = relativeURL + 'debitcard.htm';
	var ibankingAction = relativeURL + 'ibanking.htm';
	var quickbookAction = relativeURL + 'quickbook.htm';
	var rewardsAction = relativeURL + 'rewards.htm';
	var mobileWalletAction = relativeURL + 'mobileWallet.htm';
	var itzAction = relativeURL + 'itz.htm';
	var atmAction = relativeURL + 'atm.htm';
	var emiAction = relativeURL + 'creditcard.htm';
	var ezeClickAction = relativeURL + 'creditcard.htm';
	if (payOption == 'cc') {
		var payop = YatraPaymentUI.getParamValue("payop");
		if (payop == 'qb') {
			$("#paymentForm").attr("action", quickbookAction);
			$("#paymentOPT").empty();
			$("#tab_cc_qb").clone().appendTo($("#paymentOPT"));
		} else
			$("#paymentForm").attr("action", creditcardAction);
	} else if (payOption == 'dc') {
		var payop = YatraPaymentUI.getParamValue("payop");
		if (payop == 'qb') {
			$("#paymentForm").attr("action", quickbookAction);
			$("#paymentOPT").empty();
			$("#tab_dc_qb").clone().appendTo($("#paymentOPT"));
		} else
			$("#paymentForm").attr("action", debitcardAction);
	} else if (payOption == 'nb') {
		$("#paymentForm").attr("action", ibankingAction);
	} else if (payOption == 'qb') {
		$("#paymentForm").attr("action", quickbookAction);
	} else if (payOption == 'mw') {
		$("#paymentForm").attr("action", mobileWalletAction);
	} else if (payOption == 'cashCard') {
		$("#paymentForm").attr("action", itzAction);
	} else if (payOption == 'atm') {
		$("#paymentForm").attr("action", atmAction);
	} else if (payOption == 'emi') {
		$("#paymentForm").attr("action", emiAction);
	} else if (payOption == 'ec') {
		$("#paymentForm").attr("action", ezeClickAction);
	}

	if (YatraPayment.validatePaymentDiv("paymentForm"))
		$("#paymentForm").submit();
}


/* IE PlaceHolder text */
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
 
            /*var $this = this;
 
            if(jElem.val() == "" || jElem.val() == jElem.attr('placeholder'))
            {
                jElem.addClass($this.config.placeholder_color_class);
            }
            else
            {
                jElem.removeClass($this.config.normaltext_color_class);
            }*/
        }
};
window.onload = function() {
	jQuery.support.cors = true;
	setTimeout(function() {YatraPaymentUI.initialise();}, 200);
	var elements = ['other_payop', 'emiDropdown', 'netBankDropDowm', 'atmDropdown'];
	YatraPaymentUI.resetPayOpsDropdown(elements);
	if($('#tab_qb input:radio[class="qb_radio"]:nth(0)').length > 0) {
		var firstQBRadio = $('#tab_qb input:radio[class="qb_radio"]:nth(0)');
						YatraQBPaymentUI.selectQBCard(firstQBRadio);
						YatraQBPaymentUI.enableQBCVVValidation(firstQBRadio);
	}
};
Popup = {
		loaderTitle: "loader_title",
		loaderMessage : "loader_message",
		loaded: false,
		showPopup :function(message, headerTitle){
			$("#"+this.loaderTitle).html(headerTitle);
			$("#"+this.loaderMessage).html(message);
			$('body').addClass('show-loading');
		 },
		 showSessionExpire: function(){
			 Popup.showPopup("Your session has been expired. Please wait while we reconfirm your itinerary","Session Expire")
		 },
		 processingPayment: function(){
			 Popup.showPopup("Please give us some time","Processing Payment")
		 },
		 hidePopup : function (){
			 $('body').removeClass('show-loading');
		 }
}