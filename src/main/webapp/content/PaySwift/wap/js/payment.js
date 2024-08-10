  //reset type=date inputs to text
  $( document ).bind( "mobileinit", function(){
    $.mobile.page.prototype.options.degradeInputs.date = true;
  });	
  //show hide password input box
	var otherCradsOptn = {
		cardHolder:{
			radioChek:$('.other-cards').find('li input[type="radio"]'),
			showcardInfo:'cardform-open', 
			paymentoptn:'#other_payop', 
			promoBtn:$('#promoBtn'), 
			promoValue:$('#promo-holder'),
			promoCont:$('.promo-container'),
			promoClose:$('.promo-close'),
			cardVal:$('.cardVal'),
			btnHolder:$('.btn-holder'),
			cardLogo:$('.card-number').find('i')
		},
		bind:function(){
			$(otherCradsOptn.cardHolder.radioChek).off('click');
			otherCradsOptn.cardHolder.radioChek.removeAttr("checked");
			$(otherCradsOptn.cardHolder.radioChek).on('click', function(){
				var _this = $(this);
				_this.parents('.other-cards').find('.showcardInfo').removeClass('showcardInfo');
				_this.parents('li').addClass('showcardInfo');
			});
		},
		cardoptn:function(){
			$(otherCradsOptn.cardHolder.paymentoptn).change(function(){
				var optnSel = $(this).val()
				 if( optnSel !=''){
					 $(".other_option_div").hide(); 
					 $('#tab_'+optnSel).show();
					 YatraPaymentUI.onPaymentOptionSwitch('#tab_'+optnSel);
				 }
				 else {
					 $('.other_option_div').hide();
				 }
			});
		},
		promoCheck:function(){
			otherCradsOptn.cardHolder.promoBtn.on('click',function(){
				if(otherCradsOptn.cardHolder.promoValue.val() !== 'YATRA'){
					otherCradsOptn.cardHolder.promoCont.fadeOut();
				}
				else{
					otherCradsOptn.cardHolder.promoCont.fadeIn();
					otherCradsOptn.cardHolder.btnHolder.fadeOut();
				}
			})
		},
		promoClose:function(){
			otherCradsOptn.cardHolder.promoClose.on('click',function(){
				otherCradsOptn.cardHolder.promoCont.fadeOut();
				otherCradsOptn.cardHolder.btnHolder.fadeIn();
			})
		},
		showCards:function(){},
		
		init:function(){
			otherCradsOptn.bind();
			otherCradsOptn.cardoptn();
			otherCradsOptn.promoCheck();
			otherCradsOptn.promoClose();
			//otherCradsOptn.showCards();
		}
	}
	otherCradsOptn.init();