/************** query properties & some core methods *************************/
/******************/
//author: suleman
//version: 1.2
var browserData = '';

var autoHideDiv = { //hide element on body click
	currentDiv:null,
	timer:null,
	scrolling:false,
	init: function(div) {
		autoHideDiv.currentDiv = div;
		//clear old one
		$(document).unbind("mouseup", autoHideDiv.unbindAutoHide);
		if(autoHideDiv.timer) {
			clearTimeout(autoHideDiv.timer);
		}
		autoHideDiv.timer = setTimeout(function() {
			$(document).bind("mouseup", autoHideDiv.unbindAutoHide);
		}, 200);
	},
	unbindAutoHide: function(e) {
		if(e.pageX > $(window).width())
		{
			return;
		}
		if (e && !$(e.target).is(autoHideDiv.currentDiv +", "+autoHideDiv.currentDiv + " *") && autoHideDiv.scrolling==false) {
			$(autoHideDiv.currentDiv).hide();
			if(autoHideDiv.currentDiv == '#priceCalendarPopup') {
				$("#datePopup, #returnDatePopup").hide();
			}
			$(document).unbind("mouseup", autoHideDiv.unbindAutoHide);
			autoHideDiv.currentDiv = null;
		}
	},
	clear: function() {
		if(autoHideDiv.currentDiv) {
			$(autoHideDiv.currentDiv).hide();
			$(document).unbind("mouseup", autoHideDiv.unbindAutoHide);
			autoHideDiv.currentDiv = null;
		}
	},
	initScroll: function() {
		var timer;
		function maintainVisible() {
			autoHideDiv.scrolling = true;
			if(timer) {
				clearTimeout(timer);
			}
			timer = setTimeout(function() {autoHideDiv.scrolling=false;},100);
		}
		$(window).bind("scroll", maintainVisible);
	}
};
autoHideDiv.initScroll();

//get element and maintain cache
function getElement(id) {
	if(!getElement["elem"]) {
		getElement["elem"] = {};
	}
	if(!getElement["elem"][id]) {
		getElement["elem"][id] = document.getElementById(id);
	}
	return getElement["elem"][id];
}

/************** end query properties *********************/


//author: suleman
//date: 20 March, 2013
//Desc: Load Ajax file and maintain js cache
//version: 1.2
var CustomLoader = {
	cache: {},
	serialize:function(obj) {
		var data = [];
		for(var i in obj) {
			data.push(i+"="+obj[i]);
		}
		return (data.length>0) ? data.join("&") : "";
	},
	useCache:true,
	ajax:function(url, cb, errorcb) {
		var obj = {
				url:url,
				data:null,
				success:null,
				error:null,
				type:"post"
		};
		if(typeof url === "object") {
			$.extend(obj, url);
		} else {
			obj.url = url;
			obj.success = cb;
			obj.error = errorcb || null;
		}

		var filename = obj.url;
		var callback = obj.success;
		var error_callback = obj.error;
		var key = filename;
		key = (typeof obj.data === undefined) ? key+"" : key+(this.serialize(obj.data));
		key = key.replace(/\/|\.|\:|\-|\=|\&/g, "");
		if(this.cache[key]!==undefined && this.useCache) {
			if(callback) {
				eval(callback)(this.cache[key]);
			}
		} else {
			$.ajax({
				url:filename,
				type:obj.type,
				data:obj.data,
				success: function(res) {
					CustomLoader.cache[key] = res;
					if(callback) {
						eval(callback)(CustomLoader.cache[key]);
					}
				},
				error: function() {
					//alert("Sorry! There is some error.");
					if(error_callback) {
						eval(error_callback)();
					}
				}
			});
		}
	}
};

var flashingDiv = {
		containerShowInterval: '',
		containerHideInterval: '',
		tooltipShowInterval: '',
		tooltipShowInterval: '',
		mainConatinerTimeout: '',
		flashCount: 2,
		onLoadShowCount:2, // for cookie count
		msgDuration: 1000, // 1 sec
		flashingBorderShowInterval: 500, // this val shud be greater than flashingBorderHideTimeout
		flashingBorderHideTimeout: 200,
		flashingpopupShowTimeout:800,
		flashingpopupHideTimeout:7000,
		positionBlocks: function(blockId, msgDuration, srchBar, ignoreQBDiv, fixedTop, isScroll) {
			var divTop = $('#'+blockId).offset().top;
			var divLeft = $('#'+blockId).offset().left+4;
			var divWidth = $('#'+blockId).outerWidth()+2;

			// ignore setting width on scroll
			if(isScroll == true) {
				$('.flashing-container').css({'top': divTop, left:divLeft});
			} else {
				$('.flashing-container').css({'top': divTop, left:divLeft, width: divWidth});
			}

			var bookingformTop = $(window).scrollTop(),
			bookingStrip = $('#'+srchBar);
			if (!($.browser.msie && parseInt($.browser.version) < 8)) {
				if( bookingformTop >= fixedTop){
					$('.flashing-container').css({'top': bookingStrip.height()});
					flashingDiv.handleIgnoreQBDiv(ignoreQBDiv);
				} else 	{
					$('.flashing-container').css({'top':(bookingStrip.offset().top - $(window).scrollTop()) +bookingStrip.height()});
					flashingDiv.handleIgnoreQBDiv(ignoreQBDiv);
				}
				$('.flashingTooltips').css({'top': (parseInt($('.flashing-container').css('top'))+100), left:parseInt($('.flashing-container').css('left'))+$('.flashing-container').width()});
			} else {
				if( bookingformTop >= fixedTop){
					$('.flashing-container').css({'top': 0});
					flashingDiv.handleIgnoreQBDiv(ignoreQBDiv);
				} else 	{
					$('.flashing-container').css({'top':(bookingStrip.offset().top - $(window).scrollTop()) +bookingStrip.height()});
					flashingDiv.handleIgnoreQBDiv(ignoreQBDiv);
				}
				$('.flashingTooltips').css({'top': (parseInt($('.flashing-container').css('top'))+100), left:parseInt($('.flashing-container').css('left'))+$('.flashing-container').width()+4});
			}
		},
		handleClickEvent: function() {
			$('.flashingTooltips .close, #flip-aside').on('click', function() {
				clearInterval(flashingDiv.containerShowInterval);
				clearTimeout(flashingDiv.containerHideInterval );
				clearTimeout(flashingDiv.tooltipShowInterval);
				clearTimeout(flashingDiv.tooltipHideInterval);
				clearTimeout(flashingDiv.mainConatinerTimeout);
				$('.flashing-container').hide();
				$('.flashingTooltips').hide();
			});
		},
		handleCookieInitialization: function() {
			var flashingCookie = YatraHelper.getCookie('flashingFilter');
			if(typeof(flashingCookie) == 'undefined' || flashingCookie == 'undefined') {
				var now = new Date();
				var cookieValue = 1;
				var expireTime = now.getTime() + (1000*60* 60*24)*30; // 30 days
				now.setTime(expireTime);
				var cookieName = 'flashingFilter';
				document.cookie = cookieName + '=' + cookieValue + ';expires=' + now.toGMTString() + ';path=/';
			} else {
				if(flashingCookie >= flashingDiv.onLoadShowCount) {
					return false;
				}
				var now = new Date();
				var cookieValue = parseInt(flashingCookie)+1;
				var expireTime = now.getTime() + (1000*60* 60*24)*30; // 30 days
				now.setTime(expireTime);
				var cookieName = 'flashingFilter';
				document.cookie = cookieName + '=' + cookieValue + ';expires=' + now.toGMTString() + ';path=/';
			}

			return true;
		},
		init: function(blockId, srchBar, ignoreQBDiv) {
			try {
				var msgDuration = this.msgDuration;
				var res = this.handleCookieInitialization();
				if(res === false) {
					return false;
				}
				var div = $('body').append('<div class="flashing-container"></div>');
				var fixedTop = 70;
				this.positionBlocks(blockId, msgDuration, srchBar, ignoreQBDiv, fixedTop, false);
				$(window).scroll(function(){
					flashingDiv.positionBlocks(blockId, msgDuration, srchBar, ignoreQBDiv, fixedTop, true);
				});
				this.handleflashingDivShowHide(ignoreQBDiv);
				this.handleClickEvent();
			} catch(e){
				// for safe side hide flashing div in case of error as it comes at top of filter
				$('.flashing-container').hide();
			}
		},
		handleflashingDivShowHide: function(ignoreQBDiv) {
			var intervalDisplayCount = 0;
			flashingDiv.mainConatinerTimeout = setTimeout(function(){
				flashingDiv.containerShowInterval=setInterval(function(){
					intervalDisplayCount++;
					$('.flashing-container').show();
					flashingDiv.handleIgnoreQBDiv(ignoreQBDiv);
					flashingDiv.containerHideTimeout = setTimeout(function(){
						$('.flashing-container').hide();
					}, flashingDiv.flashingBorderHideTimeout);
					if(intervalDisplayCount >= flashingDiv.flashCount) {
						clearInterval(flashingDiv.containerShowInterval);
					}
				},flashingDiv.flashingBorderShowInterval);

				flashingDiv.tooltipShowInterval=setTimeout(function(){
					$('.flashingTooltips').fadeIn();
				}, flashingDiv.flashingpopupShowTimeout);

				flashingDiv.tooltipHideInterval=setTimeout(function(){
					$('.flashingTooltips').animate({
						opacity: 0
						}, 1000, function() {
							$('.flashingTooltips').hide();
							$('.flashing-container').hide();
					});
				}, flashingDiv.flashingpopupHideTimeout);
			}, 4000);

		},
		handleIgnoreQBDiv: function(ignoreQBDiv) {
			var divHeight = ($(window).height()) -($('.flashing-container').offset().top-$(window).scrollTop());
			if(ignoreQBDiv != false && $('#QBstrip').is(":visible") == true) {
				divHeight -= $('#QBstrip').height();
			}

			$('.flashing-container').css({'height': (divHeight-6)});
		}

};
//version: 1.4.1
var YatraHelper = {
	fileCache:{},
	longMonths:["Januray", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"],
	months:["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"],
	days:["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"],
	shortDays:["S", "M", "T", "W", "T", "F", "S"],
	enableDock : true,
	fixTabIndex: function(foc) {
		var tabindex = 1;
		$('body').find('input,select').each(function() {
			if (this.type != "hidden" && this.style.display!="none") {
				var $input = $(this);
				$input.attr("tabindex", tabindex);
				tabindex++;
			}
		});
		if(foc) {
			$(foc).focus();
		}
	},
	showTinyScrollBar: function() {
	var viewHeight = $('.popover .tinyScrollViewport:visible').height();
	var overviewHeight = $('.popover .tinyScrollOverview:visible').height();
		$('.popover .tinyScrollBarDiv').tinyscrollbar();

		if( overviewHeight > viewHeight){
		//alert("height is bigger")
		$('.popover .tinyScrolbBar').show();
		}
		else{
		//alert("height is small")
		$('.popover .tinyScrolbBar').hide();
		}		//$('.tinyScrollBarDiv').hover(function(){$('.tinyScrolTrack').show('slow');},function(){$('.tinyScrolTrack').show('slow');});
	},

	//Get cookie
	getCookie: function(c_name) {
		var i,x,y,ARRcookies=document.cookie.split(";");
		for (i=0;i<ARRcookies.length;i++) {
		  x=ARRcookies[i].substr(0,ARRcookies[i].indexOf("="));
		  y=ARRcookies[i].substr(ARRcookies[i].indexOf("=")+1);
		  x=x.replace(/^\s+|\s+$/g,"");
		  if (x==c_name) {
			return unescape(y);
		  }
		}
	},
	//Delete Cookie
	delCookie: function(name) {
		document.cookie = name + "=;expires=Thu,01 Jan 1970 00:00:01 UTC;path=\/;domain=.yatra.com" ;
	},
	getParamFromUrl: function(param, url) {
		  var regex = new RegExp(param + "=");
		  return $.grep(url.split("?")[1].split("&"), function(a) {
		    return a.match(regex);
		    })[0].split("=")[1];
		},

	scrollWin: function(w, cb, t) {
		var time = t || 1000;
		var top = (typeof w == "number") ? w : $(w).position().top;
		$("html,body").animate({scrollTop:top}, time, function() {
			if(cb) {
				cb.apply(this, arguments);
			}
		});
	},
	// Some common format strings
	pattern:  {
		"default":      "ddd mmm dd yyyy HH:MM:ss",
		shortDate:      "m/d/yy",
		mediumDate:     "mmm d, yyyy",
		longDate:       "mmmm d, yyyy",
		fullDate:       "dddd, mmmm d, yyyy",
		shortTime:      "h:MM TT",
		mediumTime:     "h:MM:ss TT",
		longTime:       "h:MM:ss TT Z",
		isoDate:        "yyyy-mm-dd",
		isoTime:        "HH:MM:ss",
		isoDateTime:    "yyyy-mm-dd'T'HH:MM:ss",
		isoUtcDateTime: "UTC:yyyy-mm-dd'T'HH:MM:ss'Z'",
		simpleDateTime: "dd/mm/yyyy"
	},

	names: {
		dayNames: [
			"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat",
			"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
		],
		monthNames: [
			"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
			"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"
		]
	},
	formatNumber: function(number) {
		if (number!=undefined) {
			var regexp = new RegExp(/(\d)(?=(\d\d\d)+(?!\d))/g);
			if (typeof number != 'undefined') {
				var fNumber= number.toString().replace(regexp, "$1,");
			}
			if(fNumber.length>6){
				return fNumber.substring(0,1)+","+fNumber.substring(1,fNumber.length);
			}else{
				return	number.toString().replace(regexp, "$1,");
			}

		}
	    return false;
	},
	// pass complete time value return by date func and format
	formatDate: function (date, pattern) {
		date = String(this.strToDate(date, false));
		var utc= false;
		var	token = /d{1,4}|m{1,4}|yy(?:yy)?|([HhMsTt])\1?|[LloSZ]|"[^"]*"|'[^']*'/g,
			timezone = /\b(?:[PMCEA][SDP]T|(?:Pacific|Mountain|Central|Eastern|Atlantic) (?:Standard|Daylight|Prevailing) Time|(?:GMT|UTC)(?:[-+]\d{4})?)\b/g,
			timezoneClip = /[^-+\dA-Z]/g,
			pad = function (val, len){
				val = String(val);
				len = len || 2;
				while (val.length < len) val = "0" + val;
				return val;
			};

			var dF = YatraHelper;
			// Passing date through Date applies Date.parse, if necessary
			date = date ? new Date(date) : new Date();
			if (isNaN(date)) throw new SyntaxError("invalid date");
			pattern = String(dF.pattern[pattern] || pattern || dF.pattern["default"]);

			var	_ = utc ? "getUTC" : "get",
				d = date[_ + "Date"](),
				D = date[_ + "Day"](),
				m = date[_ + "Month"](),
				y = date[_ + "FullYear"](),
				H = date[_ + "Hours"](),
				M = date[_ + "Minutes"](),
				s = date[_ + "Seconds"](),
				L = date[_ + "Milliseconds"](),
				o = utc ? 0 : date.getTimezoneOffset(),
				flags = {
					d:    d,
					dd:   pad(d),
					ddd:  dF.names.dayNames[D],
					dddd: dF.names.dayNames[D + 7],
					m:    m + 1,
					mm:   pad(m + 1),
					mmm:  dF.names.monthNames[m],
					mmmm: dF.names.monthNames[m + 12],
					yy:   String(y).slice(2),
					yyyy: y,
					h:    H % 12 || 12,
					hh:   pad(H % 12 || 12),
					H:    H,
					HH:   pad(H),
					M:    M,
					MM:   pad(M),
					s:    s,
					ss:   pad(s),
					l:    pad(L, 3),
					L:    pad(L > 99 ? Math.round(L / 10) : L),
					t:    H < 12 ? "a"  : "p",
					tt:   H < 12 ? "am" : "pm",
					T:    H < 12 ? "A"  : "P",
					TT:   H < 12 ? "AM" : "PM",
					Z:    utc ? "UTC" : (String(date).match(timezone) || [""]).pop().replace(timezoneClip, ""),
					o:    (o > 0 ? "-" : "+") + pad(Math.floor(Math.abs(o) / 60) * 100 + Math.abs(o) % 60, 4),
					S:    ["th", "st", "nd", "rd"][d % 10 > 3 ? 0 : (d % 100 - d % 10 != 10) * d % 10]
				};

			return pattern.replace(token, function ($0){
				return $0 in flags ? flags[$0] : $0.slice(1, $0.length - 1);
			});
		},
	isToday: function(dt) {
		var today = new Date();
		today.setHours(0,0,0,0);
		var target = dt;
		target.setHours(0,0,0,0);
		if(today.getTime()==target.getTime()) {
			return true;
		}
		return false;
	},

	formatPaymentDate : function(date){
		try {
			var dateArr =  date.split("T");
			var formatted_date = this.formatDate(dateArr[0],"mm/dd/yyyy")+" "+this.getFormattedTime(dateArr[1]);
			return formatted_date;
		}catch(e){}
		return date;
	},

	getFormattedTime: function(time){
		var time_arr = time.split(":");
		var hh = parseInt(time_arr[0],10);
		var mm = parseInt(time_arr[1],10);
		var ss = parseInt(time_arr[2],10);
		var hours = (hh == 0) ? "12" : (hh > 12 ? hh - 12 : hh);
	    var minutes = (mm < 10 ? "0" : "") + mm;
	    var seconds = (ss < 10 ? "0" : "") + ss;
	    var ampm = hh < 12 ? "am" : "pm";
	    var formattedTime = hours + ":" + minutes + ":" + seconds + " " + ampm;
	    return formattedTime;
	},
	daysdiffFromToday: function(dt) {
		var today = new Date();
		today.setHours(0,0,0,0);
		var target = dt;
		target.setHours(0,0,0,0);
		return (target.getTime() - today.getTime())/86400000;
	},
	daysdiff: function(dt1,dt2) {
		var first_date = dt1;
		first_date.setHours(0,0,0,0);
		var second_date = dt2;
		second_date.setHours(0,0,0,0);
		return (second_date.getTime() - first_date.getTime())/86400000;
	},
	formatPrice: function(number,isNumber,as) {
		if(number=="0"){ return 0; }
		if(typeof(number)=="undefined" || number==""){
			return
		}
		if(isNumber){
			return (as===undefined) ? parseInt(number.toString().replace(/,/g,"")) : parseFloat(number.toString().replace(/,/g, ""));
		}
		if (number) {
			x=parseFloat(number).toFixed(2);
			var afterPoint = '';
			if(x.indexOf('.') > 0)
			   afterPoint = x.substring(x.indexOf('.')+1,x.length);
			var regexp = new RegExp(/\B(?=(\d{2})*(\d{3})$)/g);
			var negative = x<0 ;
			x = Math.floor(Math.abs(x));
			x= x>999? x.toString().replace(regexp, ","):x;
			var afterPoint = parseInt(afterPoint)>0 ? "."+afterPoint : ""; 
			var res = x + afterPoint;
			if(negative) res = "-"+res; 
			return res;
		}
		return 0;
	},
	getRoundedAmount:function(amount){
		return (typeof(amount) == 'undefined' || amount == '' || parseFloat(amount) == 'NaN') ? 0 : Math.round(amount);
	},
	getFloatAmount:function(amount){
		return (typeof(amount) == 'undefined' || amount == '' || parseFloat(amount) == 'NaN') ? 0 : parseFloat(amount);
	},
	getPriceFromFormattedPrice: function(price) {
		if(typeof(price) == 'string')
		price=price.replace(/,/g,"");
		return parseInt(price);
	},
		customRadio:function(block,radiobtnID){
		/*$("#"+radiobtnID).on("click",function(i,radichecked){
			if(YatraHelper.checkIEVersion()){
			alert("hi")
				$("#"+radiobtnID).addClass('checked');
			}
			else
			{
				$("#"+radiobtnID).removeClass('checked');
			}
		});*/
	},
	getTimeFromDate: function(str) {
		return str;
		var date = this.strToDate(str);
		var time = date.getHours()+":"+date.getMinutes();
		return time;
	},
	getHours:function(dt){
		var h=dt.split(":");
		if(h){return parseInt(h[0]);}
		return dt;
	},
	setHTML:function(id,html){
		getElement(id).innerHTML=html;
	},
	formatMinutes:function(time){
		var hr = parseInt(parseInt(time) / 60);
		var min = parseInt(time) % 60;
		hr=(hr<10) ? "0"+hr : hr;
		min=(min<10) ? "0"+min : min;
		return hr + ":" + min + " hrs";
	},
	getMinutes:function(time){
				var time=time.split(":");
				var hr=Math.ceil(time[0]);
				var min=Math.ceil(time[1]);
				return (hr*60) + min;
	},
	handleDisplayDigit: function(value) {
		return value<10? '0'+value : value;
	},
	getDuration: function(startTime, endTime) {
		startTime = this.strToDate(startTime, false);
		endTime = this.strToDate(endTime, false);

		var startTimeObj = new Date(startTime);
		var endTimeObj = new Date(endTime);

		var timeDiff = endTimeObj.getTime() - startTimeObj.getTime();
		if(timeDiff < 0)
		{
			// add one day to end time
			timeDiff = (endTimeObj.getTime()+86400000) - startTimeObj.getTime();
		}

		var diffDate = new Date(timeDiff);

		var inDays = this.handleDisplayDigit(diffDate.getDate()-1);
		var inMonths = this.handleDisplayDigit(diffDate.getMonth());
		var inYears = this.handleDisplayDigit(diffDate.getFullYear()-1970);

		var inHours = this.handleDisplayDigit(diffDate.getUTCHours());
		var inMinutes = this.handleDisplayDigit(diffDate.getUTCMinutes());
		var inSecs = this.handleDisplayDigit(diffDate.getUTCSeconds());
		var formatStr = {'year':inYears, 'month':inMonths, 'day':inDays, 'hour':inHours, 'minute':inMinutes, 'second':inSecs, 'displayDiff':'Now'};

		if(inYears > 0)
		{
			formatStr['displayDiff'] = inYears+'yr '+inMonths+'mo ';
		}
		else if(inMonths > 0)
		{
			formatStr['displayDiff'] = inMonths+'mo '+inDays+'d';
		}
		else if(inDays > 0)
		{
			formatStr['displayDiff'] = inDays+'d '+inHours+'h';
		}
		else if(inHours > 0)
		{
			formatStr['displayDiff'] = inHours+'h '+inMinutes+'m';
		}
		else if(inMinutes > 0)
		{
			formatStr['displayDiff'] = inMinutes+'m ';
		}
		formatStr['duration'] = timeDiff/1000;

		return formatStr;
    },
	//params: 01-12-2003, 'dd/mm/yyyy'. To avoid formatting pass format false
	strToDate: function(dt, format) {
		var isDate = (Object.prototype.toString.call(dt) == "[object Date]") ? true : false;
		format = format || false;
		var date;
		if(isDate==true) {
			date = dt;
		} else {
			dt = String(dt);
			var time = " 00:00:00";
			var date_str;
			if(dt.indexOf(":")==-1) {
				 date_str = forumulateDateStr(dt);
			} else {
				//check again if date is missing;
				if(dt.indexOf("-")==-1 && dt.indexOf("/")==-1 && dt.indexOf("_")) {
					var nd = new Date();
					dt = nd.getDate()+"/"+(nd.getMonth()+1)+"/"+nd.getFullYear()+" "+dt;
				};
				var dts_arr = dt.split(" ");
				date_str = forumulateDateStr(dts_arr[0]);
				time = " "+dts_arr[1];
			};
			if(isNaN(parseInt(dt)) == false && date_str == null){
			// for timestamp
				date = new Date(parseInt(dt)*1000);
			}else if(date_str == null){
				date = new Date(dt);
			} else {
				// for date AS string
				date = new Date(date_str + time);// if a normal time stamp
			}  	// E.g Date('January 1, 2008 2:54:16 am');
		};
		if(format != false)
		{
			date = this.formatDate(date, format);
		}
		return date;

		//date formulate
		function forumulateDateStr(dts) {
			if(dts.indexOf("-")>0) {
				dt_arr = dts.split("-")
			} else if (dts.indexOf("/")>0) {
				dt_arr = dts.split("/")
			} else if (dts.indexOf("_")>0) {
				dt_arr = dts.split("_")
			} else {
				dt_arr = {};
			}

			var date_str = null;
			 if(dt_arr[2] && dt_arr[2].length == 4) { //date/month/year
				date_str = dt_arr[2]+"/"+dt_arr[1]+"/"+dt_arr[0];
			} else if(dt_arr[0] && dt_arr[0].length == 4) { //year/month/date
				date_str = dt_arr[0]+"/"+dt_arr[1]+"/"+dt_arr[2];
			};
			return date_str;
		}
	},

		collapsExpand:function(target){
		if(!$("#bodyjs").hasClass("aside-push")){
			$("#bodyjs").addClass("aside-push");
			//setTimeout(function(){
			//	promoCarousel.init(),1000
			//});
			//$("#result-sidebar").height($("#results").height());
		}else{
				$("body").removeClass("aside-push")
		}

	},

	toggleExpand:function(target,options){
	try{
		var linkedElement = $(target).data("linkedelement");
		var cb = $(target).data("callback");
		if(options.onclass){
			if($("#"+linkedElement+ ":visible").length==0) {
			////console.log(options.spanclass);
				if(options.spanclass){
					$(target).find("span").removeClass(options.onclass).addClass(options.offclass);
				}else{
					$(target).removeClass(options.onclass).addClass(options.offclass);
				}
				if(options.offtext!="undefined"){$(target).find(".showmoreflt").text(options.offtext)};
			} else {
				if(options.spanclass){
					$(target).find("span").removeClass(options.offclass).addClass(options.onclass);
				}else{
				$(target).removeClass(options.offclass).addClass(options.onclass);
				}
				if(options.ontext!="undefined"){$(target).find(".showmoreflt").text(options.ontext)};
			};
		}

		$("#"+linkedElement).slideToggle("slow", function() {
			if(cb) {
				eval(cb);
			}
		});
	}catch(e){
		//alert(e.message)
	}
	},
	customRadio:function(target){

			var dataset=$(target).data();
			var id=dataset["linked"];
			var bid=dataset["bid"];
			$("#"+bid+" span.box b").removeClass("showimp");
			$("#"+id).addClass("showimp");

	},
	date:
		{
			days:["Sun","Mon","Tue","Wed","Thu","Fri","Sat"],
			months:["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"],
			formatDate:function(dt, format) {
				return YatraHelper.formatDate(dt, format);
			},
			strToDate: function(dt, format) {
				return YatraHelper.strToDate(dt, format);
			},
			displayDate: function(dt, year) {
				var date = this.strToDate(dt);
				var dDate = this.days[date.getDay()] + ", " + date.getDate() + " " + this.months[date.getMonth()];
				if (year) {
					dDate = dDate + " " + date.getFullYear();
				};
				return dDate;
			},
			getDayFromDate: function(dt) {
				var date = this.strToDate(dt);
				return this.days[date.getDay()] ;
			},
			getMonthFromDate: function(dt) {
				var date = this.strToDate(dt);
				return this.months[date.getMonth()]
			},
			displayDatefromDate: function(dt) {
				var date = this.strToDate(dt);
				return date.getDate();
			},
			isPastDate: function(dt) {
				var src_date = this.strToDate(dt);
				src_date = new Date(src_date.getFullYear(), src_date.getMonth(), src_date.getDate(), 0, 0, 0);
				var target_date = new Date();
				target_date.setHours(0,0,0);
				if((src_date.getFullYear() == target_date.getFullYear()) && (src_date.getMonth() == target_date.getMonth()) && (src_date.getDate() == target_date.getDate())) {
					return false;
				};
				if(src_date.getTime() > target_date.getTime()) {
					return false;
				};
				return true;
			},
			getDaysAhead: function(dt, days, as) {
				var date = this.strToDate(dt);
				date.setDate((date.getDate()) + days);
				as = as || "string";
				return (as == "string") ? this.formatDate(date, "dd/mm/yyyy") : date;
			},
			getDates: function(startDate, stopDate) {
			      var daterray = new Array();
			      var currentDate = startDate;
			      var i=0;
			      while (currentDate <= stopDate) {
			    	daterray[i]=YatraHelper.strToDate(currentDate, 'yyyy/mm/d');
			        currentDate = YatraHelper.date.getDaysAhead(currentDate, 1);
			        i++;
			      }

			      return daterray;
			},
			numberOfDays: function(m, y) {
				return /8|3|5|10/.test(--m)?30:m==1?(!(y%4)&&y%100)||!(y%400)?29:28:31;
			},
			getDaysBehind: function(dt, days) {
				var date = this.strToDate(dt);
				date.setDate(date.getDate() - days);
				return this.formatDate(date, "dd/mm/yyyy");
			},
			isDateInBetween: function(startDate, endDate, dateToCheck) {
				var startDateObj = YatraHelper.strToDate(startDate);
				var endDateObj = YatraHelper.strToDate(endDate);
				var dateToCheckObj = YatraHelper.strToDate(dateToCheck);
				if(dateToCheckObj >= startDateObj && dateToCheckObj <= endDateObj) {
					return true;
				}

				return false;
			},
			isDateGreater: function(oDate1, oDate2) {
				if((oDate1.getDate() > oDate2.getDate()) || (oDate1.getMonth() > oDate2.getMonth()) || (oDate1.getFullYear() > oDate2.getFullYear())) {
					return true;
				};
				return false;
			}
		},
	addHtmlTag:function(htmlTag,html){
		if(htmlTag){
			return "<"+htmlTag+">"+html+"</"+htmlTag+">"
		}
		return html;
	},
	utils: {
		removeString: function(source_src, remove_str) {
			return source_src.replace(remove_str, "", "g").replace(/^\s+|\s+$/g, "");
		},
		removeElement: function(domElement) {
			domElement.parentNode.removeChild(domElement);
		},
		isArray: function(obj) {
			return (Object.prototype.toString.call(obj) == "[object Array]");
		},
		serialize:function(obj) {
			var data = [];
			for(var i in obj) {
				data.push(i+"="+obj[i]);
			}
			return (data.length>0) ? data.join("&") : "";
		},
		getUnique: function() {
			return Math.round(new Date().getTime()*Math.random(1,10));
		}
	},
		dockFrmBottom: function(selector, topPosition) {
			if($(window).scrollTop() > ($(selector).height() -$(window).height())) {
				$(selector).addClass('sidebarFix');
				$(selector).css('top', -(($(selector).height() -$(window).height())));
			} else {
				$(selector).removeClass('sidebarFix').css('top', '');
			}



	},
	setDockPos: function(pos) {
		if(!pos) {
			//work for matrix
			//$("#topMatrix:visible").length==0
			this.dockablePannelRefTop = $("#resultheader").offset().top;
		} else {
			this.dockablePannelRefTop = pos;
		}
	},
	dockablePannelRefTop: 411,
	dockablePannel:function(elmID) {
		var $this = this;
		var scrollPannel = $(elmID);
		if(!scrollPannel.data("originX")) {
			var position = scrollPannel.offset();
			scrollPannel.data("originalX", position.left);
			scrollPannel.data("originalY", position.top);
			scrollPannel.data("originalWidth", scrollPannel.width());
		}
		var ref = scrollPannel.offset().top;
		$(window).scroll(function () {
			if(YatraHelper.enableDock) {
				scrollPannelCont();
			};
		});

		function scrollPannelCont() {
			if(scrollPannel.is(':visible') && Filters.vCount > 4) {
				var top = $(window).scrollTop();//document.documentElement.scrollTop || document.body.scrollTop;
				if ($this.dockablePannelRefTop <= top) {
					scrollPannel.css({'position':'fixed','z-index':1,'top':0});
					$('#selectionSummary_div').css({"width":"730px"});
					//$('#dockFilterPannel').css({marginLeft : "220px"});
					$('.roundTrip > #resultheader').addClass("rtResultHdr");
					$('.oneWay > #resultheader').addClass("owResultHdr");
					$('.resultHead ').css({"padding-top":"12px"});
				} else {
					$('.roundTrip > #resultheader').removeClass("rtResultHdr");
					$('.oneWay > #resultheader').removeClass("owResultHdr");
					scrollPannel.css({'position': 'static','z-index':1,'top': 0});
					$('.resultHead').css({"padding-top":"5px"});
					$('#resultheader').css({"box-shadow":"none"});
					$('#selectionSummary_div').css({"width":"100%"});
				}
			}
		}
	},
	reSetdockablePannel:function(elmID,flag) {
		elmObject = $(elmID);
		if(flag) {
			elmObject.css({'position':'fixed','z-index':3,'top':0});
			$('#resultheader').css({"z-index":"1"});
		} else {
			//$("html, body").animate({ scrollTop: 0 }, "fast");

			//*** Changed by Aditya - made position static to relative
			elmObject.css({'position': 'relative','z-index':4,'top': 0});
			$('#resultheader').css({"z-index":"1"});
			$('#dockFilterPannel').css({"height":"auto"});
		}
	},
	PriceUpdater : {
		holder:"totalFare",
		speed:50,
		myLoop: function(start, end, fn, delay) {
			for(var i=start;i<end;i++) (function(i) {
				setTimeout(function() {
					fn.call(this, arguments);
				}, i*delay);
			})(i);
		},
		update: function(np, op, fl, where) {
			var np = parseFloat(np);
			var op = parseFloat(op);
			var holder = where || this.holder;
			var usefloat = fl || false;
			if(np != op && !($.browser.msie)) {
				var inc = (np > op) ? true : false;
				var step = 10;
				var cnt = (Math.max(np,op)-Math.min(np,op))/step;
				this.myLoop(0,9, function() {
				  var p;
				  if(inc===true) {
					op = op+cnt;
					p = (usefloat) ? (op+cnt).toFixed(2) : Math.round(op+cnt);
				  } else {
					op = op-cnt;
					p = (usefloat) ? (op-cnt).toFixed(2) : Math.round(op-cnt);
				  }
				  showPrice(p, holder);
				}, this.speed);
			} else {
				showPrice(np, holder);
			};
			function showPrice(price, holder) {
				document.getElementById(holder).innerHTML = YatraHelper.formatPrice(price);
			}
		}
	},
	hoverShow:function(elmID) {
		elmObject = $(elmID);
		elmObject.show();
	},
	hoverHide:function(elmID) {
		elmObject = $(elmID);
		elmObject.hide();
	},


	checkIEVersion:function() {
	var ieVersion = -1; // NOT IE browser
	if (navigator.appName == 'Microsoft Internet Explorer') {
		var ua = navigator.userAgent;
		var re  = new RegExp("MSIE ([0-9]{1,}[\.0-9]{0,})");
		if (re.exec(ua) != null)
		 ieVersion = parseFloat( RegExp.$1 );
		}  else if (navigator.appName == 'Netscape') {
		    var ua = navigator.userAgent;
		    var re  = new RegExp("Trident/.*rv:([0-9]{1,}[\.0-9]{0,})");
		    if (re.exec(ua) != null)
		    	ieVersion = parseFloat( RegExp.$1 );
		 }

		return ieVersion;
	},
	winReloadWithMsg:function(msgToDisplay) {
		window.location.reload();
		setTimeout(function(){Modal.show({content:"<div><h2>"+msgToDisplay+"</h2></div>", title:"Please wait.", manual:true});},10)
	},
	winRedirectWithMsg:function(url,msgToDisplay) {
		window.location.href = url;
		setTimeout(function(){Modal.show({content:"<div><h2>"+msgToDisplay+"</h2></div>", title:"Please wait.", manual:true});},10)
	},
	addRecentSearchLog: function() {

		/********************* RECENT SEARCH ***********************/
		try{
			var _logData_ = {};
			_logData_ = queryPropBuilder.build(); //query from url
			_logData_.vertical = "flights";
			_logData_.tenant = "dom";
			if(_logData_["type"] == "O") {
				_logData_.price = Math.min.apply(Math, Filters.price_arr) || 0; //lowest price
			} else {
				_logData_.price = AirlineMatrix.splData[0].l  || 0;
			};
			_logData_.page = "review";

			_logData_["class_"] = _logData_["class"]; //hack

			var reviewParams = PriceUpdater.reviewInfoParam;
			//on review page - these are required along with search params
			if(reviewParams["flightIdCSV"]) {
				_logData_.scId = reviewParams["searchId"] || ""; //search id
				if(_logData_["type"] == "O") {
					////////console.log('reviewParams["flightIdCSV"] '+reviewParams["flightIdCSV"])
					var flight_info_data = DataMaker.getFlightSchedultById(reviewParams["flightIdCSV"]);
					////////console.log(flight_info_data);
					flight_info_data = flight_info_data.OD[0].FS[0];
					_logData_.flight_info = [{"ac":flight_info_data.ac, "fl":flight_info_data.fl, "dd":flight_info_data.dd, "ad":flight_info_data.ad}];
				} else {
					_logData_.flight_info = SelectionSummary.details.itineraries;
				}
				_logData_.flightIdCSV = reviewParams["flightIdCSV"];
				_logData_.price = reviewParams["flightPrice"]; //override
			}

			//push now
			_rs_yatra.push(["setData", _logData_]);
		} catch(e) {
			CommonUtility.log(e);
		};
		/********************* END RECENT SEARCH ***********************/
	},	
	getObjlength: function(obj){
		var i = 0;
		for(var key in obj){ 
			if(obj.hasOwnProperty(key) && typeof(obj[key]) != 'undefined'){
				i++;
			}
		}
		return i;
	},
	getFirstWordUc: function(str){
		try {
			if(str != ''){
				var pieces = str.toLowerCase().split(" ");
			    for(var i = 0; i < pieces.length; i++ ) {
			        var j = pieces[i].charAt(0).toUpperCase();
			        pieces[i] = j + pieces[i].substr(1);
			    }
			    return pieces.join(" ");
			}
			return str;
		} catch(e){
			YatraHelper.log_error(e);
		}
	},
	log_error: function(data, isGaError) {	
		try { 
			var pageName = '';
			if(typeof(reviewMain) != 'undefined'){ pageName = 'Review Page';}
			if(typeof(ConfirmMain) != 'undefined'){ pageName = 'Confirmation Page';}
			
			var pricingIdVal = (typeof(jsonData) != 'undefined' && typeof(jsonData.pricingId) != 'undefined') ? jsonData.pricingId : '';
			var tenantIdVal = (typeof(tenantId) != 'undefined') ? tenantId : '';
			var errorStr = data.stack;		
			//console.log(errorStr);
			var sessionVxVal = YatraHelper.getCookie('SessionVX');	
			if(browserData == '') {
				browserData = YatraHelper.getBrowserDetails()
			}
			var userAgentInfo = JSON.stringify(browserData);
			var gaError = (isGaError == 'ga')?  true: false;
			var errorJson = {
				page: pageName,
				pageId: pricingIdVal,
				tenantId: tenantIdVal,
				userAgentInfo: userAgentInfo,
				exceptionData: errorStr,
				sessionVx: sessionVxVal,
				gaError: gaError
			};
			if(typeof(yt_log_error) == 'function') {
				yt_log_error(errorJson);
			}				
		} catch(e) {
			if(typeof(console) != 'undefined' && typeof(console.log) == 'function'){
				//console.log(e);
			}
		}
	},
	getBrowserDetails: function(){
		var nVer = navigator.appVersion;
		var nAgt = navigator.userAgent;
		var browserName  = navigator.appName;
		var fullVersion  = ''+parseFloat(navigator.appVersion); 
		var majorVersion = parseInt(navigator.appVersion,10);
		var nameOffset,verOffset,ix;

		if(YatraHelper.checkIEVersion() >= 6){
			browserName = "Microsoft Internet Explorer";
		 	fullVersion = YatraHelper.checkIEVersion();
		 	majorVersion = fullVersion;
		} else {		
			try {
				// In Opera 15+, the true version is after "OPR/" 
				if ((verOffset=nAgt.indexOf("OPR/"))!=-1) {
					browserName = "Opera";
					fullVersion = nAgt.substring(verOffset+4);
				} else if ((verOffset=nAgt.indexOf("Opera"))!=-1) {
					// In older Opera, the true version is after "Opera" or after "Version"
					browserName = "Opera";
					fullVersion = nAgt.substring(verOffset+6);
					if ((verOffset=nAgt.indexOf("Version"))!=-1)  {
						fullVersion = nAgt.substring(verOffset+8);
					}
				} else if ((verOffset=nAgt.indexOf("Chrome"))!=-1) {
					// In Chrome, the true version is after "Chrome" 
					browserName = "Chrome";
					fullVersion = nAgt.substring(verOffset+7);
				} else if ((verOffset=nAgt.indexOf("Safari"))!=-1) {
					// In Safari, the true version is after "Safari" or after "Version" 
					browserName = "Safari";
					fullVersion = nAgt.substring(verOffset+7);
					if ((verOffset=nAgt.indexOf("Version"))!=-1){
						fullVersion = nAgt.substring(verOffset+8);
					}
				} else if ((verOffset=nAgt.indexOf("Firefox"))!=-1) {
					// In Firefox, the true version is after "Firefox" 
					browserName = "Firefox";
					fullVersion = nAgt.substring(verOffset+8);
				}		
				// trim the fullVersion string at semicolon/space if present
				if ((ix=fullVersion.indexOf(";"))!=-1) {
				   fullVersion=fullVersion.substring(0,ix);
				}
				if ((ix=fullVersion.indexOf(" "))!=-1) {
				   fullVersion=fullVersion.substring(0,ix);
				}

				majorVersion = parseInt(''+fullVersion,10);
				if (isNaN(majorVersion)) {
					fullVersion  = ''+parseFloat(navigator.appVersion); 
					majorVersion = parseInt(navigator.appVersion,10);
				}	
			} catch(e){}	
		}
		browserObj = {
			browserName: browserName,
			fullVersion: fullVersion,
			majorVersion: majorVersion,
			appName: navigator.appName,
			userAgent: navigator.userAgent
		};
		return browserObj;
	}

};

Modal = {
	show: function(obj) {
		var modalProp = {};
		if(arguments.length>=2) { //it has title and content
			modalProp.title = arguments[0]; //title
			modalProp.content = arguments[1]; //content
			modalProp.manual = (arguments[2]===undefined) ? false : arguments[2];
		} else { //it should be object type
			modalProp = obj;
			modalProp.manual = (modalProp["manual"]===undefined) ? false : modalProp["manual"];
		};
		modalProp.modalwindow = (modalProp.manual===true) ? true : false; //always modal
		$.popmodal(modalProp);
	},
	hide: function(cb) {
		$.popmodal.hide(cb);
	},
	autoHide: function(time) {
		$.popmodal.autoHide(time);
	}
}



function Element(nm, attr, html) {
	var tag = document.createElement(nm);
	if (attr) {
		for (var i in attr) {
			switch (i) {
			case "style":
				tag.style.cssText = attr[i];
				break;
			case "class":
				tag.className = attr[i];
				break;
			default:
				tag[i] = attr[i];
				break;
			}
		}
	}
	if (html) {
		tag.innerHTML = html;
	}
	return tag;
}

function Template() {
	this.Class = this;
	this.configData = {};
	this.resultData = {};
	this.before = null;
	this.onInit = null;
	this.init();
};
Template.prototype = {
	Constructor:Template,
	defaultKey:"template",
	init: function() {
		if(this.onInit) {
			this.onInit.apply(this.Class, arguments);
		};
	},
	setConfig: function(key, value, isResult) { //set key value or set all
		var obj = (isResult===true) ? this.resultData : this.configData;
		if(!value) {
			obj = key;
		} else if(key && value) {
			obj[key] = value;//data is key now
		};
	},
	getConfig: function(key, isResult) {
		var obj = (isResult===true) ? this.resultData : this.configData;
		if(key) {
			return obj[key];
		};
		return obj;
	},
	set: function(key, from) {
		//key is a source element if second argument (from) is undefined;  otherwise key can be use as a key- it can be use as multiple template in single call
		var k, el;
		if(arguments.length==1) { //not in the mood the set the mulitple key
			el = key;
			k = this.defaultKey; //default key
		} else if(arguments.length>=2) {
			el = from;
			k = key;
		};
		if(document.getElementById(el)==null) {
			throw new Error(el + " is missing for "+k+"...");
		};
		this.setConfig(k, document.getElementById(el).innerHTML);
	},
	get: function(key) {
		var template = this.getConfig(key || this.defaultKey);
		return template;
	},
	render: function(obj, templateKey, as) {
		var isArray = (Object.prototype.toString.call(obj) == "[object Array]") ? true : false;
		var as = (as===undefined) ? "string" : as;
		//object can be array too
		var template = this.get(templateKey); //it will use default template if template key is not passed;
		if(!template) {
			//send error
			throw new Error("Template is empty or net yet set");
		};
		var result = template; //template

		var replaced_str;
		if(isArray) {
			var res = [];
			var total = obj.length;
			for(var i=0;i<total;i++) {
				replaced_str = replaceThis(obj[i], result);
				replaced_str = replaced_str.replace("{#number}", i, "g");
				res.push(replaced_str);
			}
			result = res.join("");
		} else {
			replaced_str = replaceThis(obj,result);
			replaced_str = replaced_str.replace("{#number}", 0, "g");
			result = replaced_str;
		}
		//replace
		function replaceThis(obj, src_str) {
			for(var i in obj) {
				//replace here

				var str = (typeof obj[i]=="function") ? obj[i]() : obj[i];
				var regExp = new RegExp("{" + i + "}", "g");
				src_str = src_str.replace(regExp,str);
			};
			return src_str;
		};
		//if there is any function inside
		var reg = /(\{\{fn\:.*?\}\})/gmi;
		var methods = result.match(reg);
		if(methods!=null && methods.length>0) { //has function
			updateFunctions(methods);
		};
		function updateFunctions(methods) {
			var total = methods.length;
			for(var i=0;i<total;i++) {
				var regExp = new RegExp("\{\{fn\:|\}\}", 'g');
				var fn = methods[i].replace(regExp, ""); //extract methods
				var value = eval(fn);
				//call - not doing cache - it can be incremental
			  //regExp = new RegExp("(\{\{fn\:.*?\}\})", 'g');
			   var reg = /(\{\{fn\:.*?\}\})/; //not global
			   result = result.replace(reg, value); //replacing match by match
			   };
		};
		if(as=="html") {
			var div = document.createElement("div");
			div.innerHTML = result;
			result = div.firstChild.parentNode;
		};

		this.setResult(templateKey, result);
		return result;
	},
	setResult: function(key, value) {
		key = key || this.defaultKey; //default key
		this.setConfig(key, value, true); //setting result
	},
	getResult: function(key) {
		key = key || this.defaultKey; //default key
		return this.getConfig(key, true); //setting result
	},
	cleanEmpty: function(val, templateKey) {
		var regExp = new RegExp("(\{)(.*?)(\})", "g");
		var html = this.getResult(templateKey, true);
		html = html.replace(regExp, val || "");
		return html;
	},
	renderOn: function(ele, obj, templateKey, clean) {
		ele = (typeof ele == "string") ? document.getElementById(ele) : ele;
		var html = this.render(obj, templateKey);
		if(clean) {
			clean = (typeof clean == "boolean") ? "" : clean; //it can be use as a replace string with empty nodes
			html = this.cleanEmpty(clean, templateKey);
		};
		ele.innerHTML = html;
		return this;
	}
};

