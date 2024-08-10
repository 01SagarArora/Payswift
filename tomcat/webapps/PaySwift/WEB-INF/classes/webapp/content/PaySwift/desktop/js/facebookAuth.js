var YatraFBPaymentUI = {
		
		initialiseFB : false,
		
		postLogin : function(authResponse,fbResponse){
			
			var authURL = YatraFBPaymentUI.getQBAuthURLForFBUser();
			var paymentJSON = getPaymentJSON();
			var merchant = paymentJSON.merchant;
			var product = paymentJSON.product;
			var token = null
			
			if (fbResponse != null || fbResponse != undefined) {
		    	token = fbResponse.authResponse.accessToken;		
			}
			
			$.ajax({
				url: authURL,
				timeout: (5 * 1000),
				type: 'POST',
				data: { 
				        'merchant': merchant, 
				        'product': product,
				        'accessToken' : token,
				        'sourcepage' : window.location.href
				},
				cache: false,
				
				success : function(response){
					YatraQBPaymentUI.generateQBHtmlFromJson(response);
				}, 
				
				error: function (request, status, error) {
					alert("Error occurred while authenticating via facebook.. Please try again or use some other payment option..")
				}
			});	
		},

		getQBAuthURLForFBUser : function(){
			return "/PaySwift/authenticate-fb-user.htm";
		},
		
		executeFn : function(f, c, p) {
		    return function () {
		        f.apply(c, p);
		    };
		},
		
		initFB : function() {
			YatraFBPaymentUI.initialiseFB = true;
		    FB.init({
		        appId: '326464424135609',
		        cookie: true,
		        status: true,
		        xfbml: true,
		        oauth: true
		    });
		},
		
		getLoggedInFb : function(fn, arr, src) {
		    var resp = null;
		    if(!YatraFBPaymentUI.initialiseFB){
		    	YatraFBPaymentUI.initFB();
		    }
		    
		    FB.login(
				function (response) {
					if (response != undefined && response.status == "connected" && response.authResponse != null) {
						resp = response;
					}
					arr[arr.length] = resp;
					YatraFBPaymentUI.executeFn(fn, this, arr)();
				}, 
				{
					scope: 'email'
				}
			);
		},
		
		fbLogin : function(r, params,src) {
			if (r == null || r == "undefined") {
				YatraFBPaymentUI.getLoggedInFb(YatraFBPaymentUI.postLogin, params, src);
		    } 
		},
		
		loginByFacebook : function() {
			authResponse={status:"noResponse"};
			YatraFBPaymentUI.fbLogin(null,[authResponse],'signin');
		}
};