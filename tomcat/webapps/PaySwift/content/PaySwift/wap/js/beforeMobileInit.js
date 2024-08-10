/*	
	start:
	stop page ajax enabled, while we using multipage template,
	this should be called before jquery.mobile.js and after jquery.js
*/
$(document).on('mobileinit', function(){
	$.extend( $.mobile, {
        ajaxEnabled: false
	});
});
/*	ends */