// ********************************* /
// FileName: tools.js
// Author: Marghoob Suelman
// Desc: This script needs jquery UI, will be responsible for common effects and utilities
// Created: 17 May, 2011
// ********************************* /
//get element and maintain cache
function getElement(id) {
	if(!getElement["elem"]) {
		getElement["elem"] = {};
	};
	if(!getElement["elem"][id]) {
		getElement["elem"][id] = document.getElementById(id);
	};
	return getElement["elem"][id];
}

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
		}
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
			})
		}
	}
}

var Utils = {};
Utils = {
	objGet:{},
	scrollMe: function(div, speed) {
		var position = $("#"+div).position();
		var windowPos = $(window).scrollTop();
		if(windowPos<45) {
			windowPos = 45;
		}
		//console.debug("windowPos "+windowPos);
		var scrollSpeed = (typeof(speed)=="undefined") ? 500 : speed;
		$("#"+div).animate({top:(windowPos)}, {queue:false, duration:scrollSpeed});
	},
	scrollWin: function(where, duration, cb) {
		var pos;
		if(typeof(where)=="number") {
			pos = where;
		} else {
			pos = $(where).offset();
			pos = pos.top;
			//alert(pos);
		}
		var callback = cb;		
		var d = (typeof(duration)=="undefined") ? 500 : duration;
		$("html, body").animate({scrollTop:(pos)}, {queue:false, duration:d}, function() {
			if(typeof(callback)!="undefined") {
				 callback.apply(this, arguments);
		   }
		});
	},
	queryString: function (ji, custom) {		
		var query = (typeof(custom)=="undefined") ? window.location.search.substring(1) : custom;
		var hu = query;
		var gy = hu.split("&");
		for (i=0;i<gy.length;i++) {
			var ft = gy[i].split("=");
			if (ft[0] == ji) {
				return ft[1];
			}
		}
		return "";
	},	
	queryStringForPaging: function (allParams) {
		var hu = window.location.search.substring(1);
		var gy = hu.split("&");
		var str = '';
		var allparam = (typeof(allParams)=="undefined") ? false : true; //not using
		for (i=0;i<gy.length;i++) {
			var current = gy[i].split("=");
			var key = current[0];
			var value = current[1];
			if(key!="offset" && typeof(value)!='undefined') {
				str += key+"="+value+"&";
			}
		}
		return str;
	},
	toTitleCase: function (str)
	{
		return str.replace(/\w\S*/g, function(txt){return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();});
	},
	setPos: function(s, t, w) {
		//more will improve later
		if(typeof(s)=="undefined" || typeof(t)=="undefined") return false;
		var source_target = s;
		var to_target = t;
		var where = (typeof(w)=='undefined') ? 'right' : w;
		var pos = $(to_target).position();
		var top, left, right;
		switch(where) {
			case 'right':
				left = pos.left + $(to_target).width();
				top = pos.top;
			break;
			case 'left':
				left = pos.left -  $(source_target).width();
				top = pos.top;				
			break;
			case 'bottomLeft':
				left = pos.left;
				top = pos.top+$(to_target).height();
			break;
			case 'topLeft':
				left = pos.left;
				top = pos.top-$(source_target).height();				
			break;
			case 'bottomRight':
				left = pos.left+$(to_target).width();
				top = pos.top+$(to_target).height();
			break;
			case 'topRight':
				left = pos.left + $(to_target).width();
				top = pos.top-$(source_target).height();				
			break;
			case 'center':
				left = (pos.left + $(to_target).width()/2) - ($(source_target).width()/2);
				top = (pos.top + $(to_target).height()/2) - ($(source_target).height()/2);
			break;
			default:
				left = (pos.left + $(to_target).width()/2) - ($(source_target).width()/2);
				top = (pos.top + $(to_target).height()/2) - ($(source_target).height()/2);
			break;
		}
		//console.debug("top "+top+ " left "+left);
		$(source_target).css({position:'absolute', top:top+'px', left:left+'px'});
	},
	addToParam: function(params, key, val) {
		if(typeof(key)!== "undefined" && typeof(val)!== "undefined") {
			var pm = (params.length==0) ? "" : "&";			
			if(params.indexOf(key)!=-1) {
				params = params.replace(key+"="+this.queryString(key, params), key+"="+val);
			} else {
				//var pm = "&";
				params = params+pm+key+"="+val;
			}
		}	
		return params;
	},
	getGet: function(w) {
		return (typeof(this.objGet[w])=="undefined") ? "" : this.objGet[w];
	},
	formatNumber: function(number) {	
		if (number!=undefined) {
			var regexp = new RegExp(/(\d)(?=(\d\d\d)+(?!\d))/g)
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
	}
}
//Effects
FX = {
	come: function(div, cb) {
		var target = div;
		var callback = cb;
		$(target).fadeIn("fast", function() {
										  if(typeof(callback)!="undefined") {
											  callback.apply(this, arguments);
										  }
										  })
	},
	out: function(div, cb) {
		var target = div;
		var callback = cb;
		$(target).fadeOut("fast", function() {
										  if(typeof(callback)!="undefined") {
											  callback.apply(this, arguments);
										  }
										  })		
	},
	highlight: function(div) {
		/*
		var callback = cb;
		var bgcolor = $(div).css("backgroundColor");
		$(div).animate({
			backgroundColor: "#FFFF99"
		  }, 1500, function() {
				$(div).animate({
					backgroundColor: bgcolor
				  }, 1500, function() {
					   if(typeof(callback)!="undefined") {
							 callback.apply(this, arguments);
					   }
				  });			
		  });

		*/
		$(div).effect("highlight", 3000);
	},
	toggleSlide: function(div, cb) {
		var callback = cb;
		$(div).toggle('slide', function() {
										if(typeof(callback)!="undefined") {
											callback.apply(this, arguments);
										}										
										});
	},
	scrollWinTo: function(w) {
		var div = w;
		if($(div).length>0) {
			$('html, body').animate({scrollTop:$(div).position().top}, 1000);
		}
	}
}

/*************** Toast by marghoob suleman ***************/
//version: 1.1
var Toaster = function(msg, w, timeout, callbackAfter, mobiletemplate) {
		var target_id = (typeof(w)=="string") ? w  : $(w).attr("id");
		var isMsDd = $('#' + target_id).data('dd');
		var target = isMsDd ? target_id + '_msdd' : w;
		var where = (typeof(target)=="undefined") ? "window" : target;
		var sMsg = msg;
		var intervalid = 0;
		var iTimeout = (typeof(timeout)=="undefined") ? 1000 : timeout;
		if(typeof(sMsg)=="undefined") {
			return false;
		}
		var id = "toater_"+Toast.counter++;
		target = (typeof(target)=="string") ? document.getElementById(target)  : target;
		if(typeof(mobiletemplate) === "undefined"){
			mobiletemplate = false;
		}
		if($("#"+id).length==0) {
			//create new one
			if(mobiletemplate){
				if(target.type == "select-one" && $(target).parents('.select').length > 0){
					$(target).parents('.select').after("<span id='"+id+"' class='ui-icon-label ui-icon-error input-error mobileToasterHolder' style='margin-top: 32px;'></span>");
					window.scrollTo($(target).offset().left,$(target).offset().top-10)
				}
				else{
					$(target).after("<span id='"+id+"' class='ui-icon-label ui-icon-error input-error mobileToasterHolder'></span>");
				}
			}else{
				if(target.type == "select-one" && $(target).parents('.select').length > 0){
					$('body').append("<div id='"+id+"' class='toasterHolder' style='margin-top:-7px;margin-left:4px;'></div>");
					window.scrollTo($(target).offset().left,$(target).offset().top-10)
				}
				else{
				$('body').append("<div id='"+id+"' class='toasterHolder'></div>");
				}
			}
		}
		$("#"+id).html(sMsg).hide();
		var top, left, right = 0;
		var pos;
		left = ($(window).width()/2)-($("#"+id).width()/2);
		top = ($(window).height()/2 - $("#"+id).height()/2)+$(window).scrollTop();
		switch(where) {
			case 'window':
				
			break;
			default:
				//align to target's next to right
				if(target["left"]) {
					left = target["left"];
				};
				if(target["top"]) {
					top = target["top"];
				};
				if(!target["left"] && !target["top"]) {
					pos = $(target).offset();
					if(mobiletemplate){
						left = (pos.left);
						top = (pos.top+$(target).height()+10);//+$(window).scrollTop();
					}else{
						left = (pos.left+$(target).width()+20);
						top = (pos.top);//+$(window).scrollTop();	
					}
				}
			break;
		}
//		console.debug(" left "+left + " top "+top);
		if(!mobiletemplate){
			
			left = left - 45;
			$("#"+id).css({position:'absolute', left:left+'px', top:top+'px', zIndex:9999});
		}
		clearInterval(intervalid);
		FX.come("#"+id, function() {
									intervalid = setInterval(function(){
																	  FX.out("#"+id, function() {
																					  $("#"+id).remove();
																					  })
																	  clearInterval(intervalid);
																	  if(typeof(callbackAfter)!=="undefined") {
																		  callbackAfter.apply(this, arguments);
																	  }
																	  }, iTimeout);
									});
		this.killAll = function() {
			clearInterval(intervalid);
			if(mobiletemplate){
				$(".mobileToasterHolder").remove();
			}else{
				$(".toasterHolder").remove();
			}
		}
		if(!Toaster.isInit) {
			Toaster.isInit = true;
			$("body").append("<style>.toasterHolder{background:#fa2d2d; padding:10px; text-align:center; float:left;color:#fff; font-size:11px; letter-spacing:1px; -moz-border-radius:5px; border-radius:5px;}.toasterHolder .toasterContent{text-align:left; color:#fff;}</style>");
		}
};

Toast = {
	id:'toaster',
	counter:20,
	timeout:3000,
	intervalid:0,
	show: function(msg, w, timeout, callbackAfter, mobiletemplate) {
		if(typeof(mobiletemplate) === "undefined"){
			mobiletemplate = false;
		}
		var iTimeout = (typeof(timeout)=="undefined") ? Toast.timeout : timeout;
		this.killAll(mobiletemplate);
		new Toaster(msg, w, iTimeout, callbackAfter, mobiletemplate);
	},
	showMultiple: function(msg, w, timeout, callbackAfter) {
		var iTimeout = (typeof(timeout)=="undefined") ? Toast.timeout : timeout;
		new Toaster(msg, w, iTimeout, callbackAfter);
	},
	killAll: function(mobiletemplate) {
		if(mobiletemplate){
			$(".mobileToasterHolder").remove();
		}else{
			$(".toasterHolder").remove();
		}
	},
	alignToast: function(onId, where, mobiletemplate) {
		if(typeof(mobiletemplate) !== "undefined" && mobiletemplate){
			var toasterid = $(".mobileToasterHolder").attr("id");
		}else{
			var toasterid = $(".toasterHolder").attr("id");
		}
		Utils.setPos("#"+toasterid, "#"+onId, where); //setPos: function(s, t, w)		
	}
}
/*************************/

//loader 
var MS_Loader = {
		isInit: false,
		loaderImg:'https://secure.yatra.com/fresco/resources/default/images/ajax-loader-gray-white.gif',
		showLoader: function(arg, t_text, isModal) {
			var target = arg;
			var where = (typeof(target)=="undefined") ? "window" : target;
			var loadingText = (typeof(t_text)=="undefined") ? "Loading. Please wait..." : t_text;		
			var left, top = 0;
			var pos;
			switch(where) {
				case 'window':
					//align to windows
					left = ($(window).width()/2)-($("#wizardLoader").width()/2);
					top = ($(window).height()/2 - $("#wizardLoader").height()/2) +$(window).scrollTop();
				break;
				default:
					target = (typeof(target)=="string") ? document.getElementById(target) : target;
					if(target["left"]) {
						left = target["left"];
					};
					if(target["top"]) {
						top = target["top"];
					};
					if(!target["left"] && !target["top"]) {
						pos = $(target).offset();
						left = (pos.left+$(target).width()+20);
						top = (pos.top);	
					};	
				break;
			}
			$("#wizardLoader .txt").html(loadingText);
			//24 scrollbar width
			if(($("#wizardLoader").width() + left + 24) >= $(window).width()) {
				left = left - $("#wizardLoader").width();
			}
			$("#wizardLoader").css({zIndex:1012, display:'block',position:'absolute', left:left+'px', top:top+'px'});
			if(typeof(isModal)!="undefined" && isModal==true) {
				var h = $(document).height();
				var w = $(document).width();
				if($(".msModelBg").length == 0) {
					$("#wizardLoader").before("<div class='msModelBg'></div>");
				}
				$(".msModelBg").css({zIndex:1011, background:'#000000',height:h+'px', width:w+'px', position:'absolute', top:'0px', left:'0px', display:'block', opacity:0.5});
			}
		},
		hideLoader: function() {
			$("#wizardLoader").hide();
			$(".msModelBg").hide()
		},
		init: function() {
			if(this.isInit == false) {
				this.isInit = true;
				var loaderImg = this.loaderImg;
				var html = '<span id="wizardLoader" class="loaderHolder shadowNew" style="float:left;display:none"><span class="wizLoader"><img src="" class="loaderTop" /> <img src="'+loaderImg+'" alt="Loading. Please wait..." class="loaderImg" /></span><span class="txt">Loading. Please wait...</span></span>';
				var style = '<style>.loaderHolder{position:absolute; top:-150px; left:40px;border:2px solid #999; padding:10px; background:#333; letter-spacing:1px; -moz-border-radius:5px; border-radius:5px; z-index:999999} .wizLoader{text-align:center; width:32px;height:32px; background:#fff; float:left} .loaderHolder .txt{position:relative; padding:9px 10px 5px 10px; text-align:center; float:left; color:#fff} .loaderTop{position:relative; top:-6px; text-align:center; display:none} .loaderImg{position:relative; text-align:center} .laoderBg{background:#000; height:100%; width:100%; position:absolute; top:0; left:0;}</style>';
				$("body").append(html);
				$("body").append(style);
			}
		}
}

var Modal = {
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


//version: 1.4.1
var FrescoHelper = {	
	fileCache:{},
	longMonths:["Januray", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"],
	months:["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"],
	days:["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"],
	shortDays:["S", "M", "T", "W", "T", "F", "S"],
	enableDock : true,
	enableSidebarFix: false,
	sidebarFixTop: 0,
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
	// get city info
	getCityInfo: function(cityCode) {
		var $this = this;
		
		if($this.isEmpty(cityCode))
		{
			return null;
		}
		
		return airportCodeJson[cityCode];
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
			};
		})
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
			var regexp = new RegExp(/(\d)(?=(\d\d\d)+(?!\d))/g)
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

			var dF = FrescoHelper;
			// Passing date through Date applies Date.parse, if necessary
				
			date = (date && date != "null") ? new Date(date) : new Date();
			if (isNaN(date))
			{
				throw new SyntaxError("invalid date");
			}
			
			
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
		};
		return false;
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
		};
		if (number!=undefined && number!="") {
			number=parseFloat(number);
			//number =(as===undefined) ?  Math.round(parseInt(number)) : parseFloat(number); //fixed for multiple commas sometime
			if(number<=999) {
				return number;
			};
			var regexp = new RegExp(/\B(?=(\d{2})*(\d{3})$)/g);
			var fNumber= number.toString().replace(regexp, ",");			
			return fNumber;
		};
		return 0;
	},
	
	getFormattedCurrency : function(val, withSymbol, convert, custTenantScope){
		
		/*if(typeof(custTenantScope) != "undefined" && custTenantScope != null)
		{
			var tenantScope = custTenantScope;
		}
		
		val = FrescoHelper.getPriceFromFormattedPrice(val);
		if(tenantScope =='us'){
			return FrescoHelper.formattedCurrencyForUS(val, withSymbol);
		}
		//var selectedCurrency = Rules.getCurrencyId();
		if(tenantScope == 'dom')
		{
			var selectedCurrency = "INR";
		}
		else
		{
			var selectedCurrency = "USD";
		}*/
		
		
		val = FrescoHelper.getPriceFromFormattedPrice(val);
		var selectedCurrency = "INR";
		
		var xRate = selectedCurrency=="INR" ? 1 : 0.0159; // Hard coded for USD
		var curSymbol = selectedCurrency=="INR" ? '<span class="rs-symbol">Rs.</span>' : '<span class="symbol">$</span>'; // Hard coded for USD
		
		var price = convert === false ? Math.ceil(parseFloat(val)) : Math.ceil((parseFloat(val)*xRate));
		if(isNaN(price)) return 0;
		else if(!withSymbol)
		return price
		else
		return curSymbol+" "+ FrescoHelper.formatPrice(price);
	},
	formattedCurrencyForUS: function(val, withSymbol) {
		var curSymbol ='<span class="symbol">$</span>';
		var price = Math.ceil(parseFloat(val));
		if(isNaN(price)) return 0;
		else if(!withSymbol)
		return price
		else
		return curSymbol+" "+ FrescoHelper.formatPrice(price);
	},
	getPriceFromFormattedPrice: function(price) {
		if(typeof(price) == 'string')
		price=price.replace(/,/g,"");
		return parseInt(price);
	},
		customRadio:function(block,radiobtnID){
		/*$("#"+radiobtnID).on("click",function(i,radichecked){
			if(FrescoHelper.checkIEVersion()){
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
		if(h){return parseInt(h[0]);};
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
	getFirstDateOfMonth: function(date, type) {
		var $this = this;
		
		date = $this.date.strToDate(date);
		
		if(type == 'obj')
		{
			return new Date(date.getFullYear(), date.getMonth(), 1);
		}
		else
		{
			return $this.strToDate(new Date(date.getFullYear(), date.getMonth(), 1), 'dd/mm/yyyy');
		}
		
		
	},
	getLastDateOfMonth: function (date, type) {
		var $this = this;
		
		if(type == 'obj')
		{
			return new Date(date.getFullYear(), date.getMonth() + 1, 0);
		}
		else
		{
			return $this.strToDate(new Date(date.getFullYear(), date.getMonth() + 1, 0), 'dd/mm/yyyy');
		}
	},
	getDateAfterMonths: function(date, months) {
		var $this = this,
			newDate = new Date(date);
		
		newDate.setMonth(parseInt(newDate.getMonth()) + parseInt(months));
		
		return newDate;
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
		var formatStr = {'year':inYears, 'month':inMonths, 'day':inDays, 'hour':inHours, 'minute':inMinutes, 'second':inSecs, 'displayDiff':'Now'}

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
			}else if(date_str == null && dt != "" && dt != "null"){
				date = new Date(dt);
			} else if(date_str != null) {
				// for date AS string
				date = new Date(date_str + time);// if a normal time stamp
			}  	// E.g Date('January 1, 2008 2:54:16 am');
			else
			{
				date = null;
			}
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
			promoCarousel.init();
			//$("#result-sidebar").height($("#results").height());
		}else{
				$("body").removeClass("aside-push")
		}
	
	},
	
	toggleExpand:function(target,options){
		var linkedElement = $(target).data("linkedelement");
		var cb = $(target).data("callback");
		if(options.onclass){
			if($("#"+linkedElement+ ":visible").length==0) {
			//console.log(options.spanclass);
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
			monthFullName: ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"],
			formatDate:function(dt, format) {
				return FrescoHelper.formatDate(dt, format);
			},
			strToDate: function(dt, format) {
				return FrescoHelper.strToDate(dt, format);
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
			    	daterray[i]=FrescoHelper.strToDate(currentDate, 'yyyy/mm/d');
			        currentDate = FrescoHelper.date.getDaysAhead(currentDate, 1);
			        i++;
			      }
			      
			      return daterray;
			},
			numberOfDays: function(m, y) {
				return /8|3|5|10/.test(--m)?30:m==1?(!(y%4)&&y%100)||!(y%400)?29:28:31;
			},
			getDaysBehind: function(dt, days, type) {
				var date = this.strToDate(dt);
				date.setDate(date.getDate() - days);
				
				if(typeof type != "undefined" && type == 'obj')
				{
					return date;
				}
				else
				{
					return this.formatDate(date, "dd/mm/yyyy");
				}
				
			},
			isDateInBetween: function(startDate, endDate, dateToCheck) {
				var startDateObj = FrescoHelper.strToDate(startDate);
				var endDateObj = FrescoHelper.strToDate(endDate);
				var dateToCheckObj = FrescoHelper.strToDate(dateToCheck);
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
		if (($(window).scrollTop()+$(window).height())>$(selector).height()) {			
			if(FrescoHelper.sidebarFixTop == 0 || typeof(topPosition) != 'undefined') {
				FrescoHelper.sidebarFixTop = $(window).scrollTop();
				$(selector).addClass('sidebarFix');
				var top = FrescoHelper.sidebarFixTop;
				if(typeof(topPosition) != 'undefined') {
					top = topPosition;
				}
				var setTop = (top-45);		        
		        var visibleContent = $(selector).height() - setTop;
		        if($(window).height() - visibleContent > 0) {
		        	var diff = $(window).height() - visibleContent;
		        	$(selector).css('top', -(setTop-diff));
		        } else {
		        	$(selector).css('top', -setTop);
		        }
			}      
	     }
	     else {
	    	FrescoHelper.sidebarFixTop = 0;
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
			if(FrescoHelper.enableDock) {
				scrollPannelCont();
			};
		});
		
		function scrollPannelCont() {			
			if(scrollPannel.is(':visible') && Filters.vCount > 4) {				
				var top = $(window).scrollTop();//document.documentElement.scrollTop || document.body.scrollTop;
				if ($this.dockablePannelRefTop <= top) {
					scrollPannel.css({'position':'fixed','z-index':1,'top':0});
					$('#selectionSummary_div').css({"width":"730px"});
					$('#dockFilterPannel').css({marginLeft : "220px"});
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
			elmObject.css({'position':'fixed','z-index':1,'top':0});
		} else {
			//$("html, body").animate({ scrollTop: 0 }, "fast");
			elmObject.css({'position': 'static','z-index':1,'top': 0});
			$('#dockFilterPannel').css({"height":"auto"});
		}
	},
	getMonthLabel: function(container, date) {
		var $this = this;

		var tempDate = new Date(date);

		if($(container).find('.graphDate').length > 0)
		{
			$(container).find('.graphDate').html($this.date.monthFullName[tempDate.getMonth()] + "," + tempDate.getFullYear());
		}
		else
		{
			$("<DIV CLASS='graphDate'>" + $this.date.monthFullName[tempDate.getMonth()] + ", " + tempDate.getFullYear() + "</DIV>").appendTo($(container));
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
			if(np != op) {
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
				document.getElementById(holder).innerHTML = FrescoHelper.formatPrice(price);
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
		}
		
		return ieVersion;
	},
	winReloadWithMsg:function(msgToDisplay) {
		window.location.reload();
		setTimeout(function(){Modal.show({content:"<div><h2>"+msgToDisplay+"</h2></div>", title:"Please wait.", manual:true});},10)
	}
};

$(document).ready(function(e) {
    MS_Loader.init();
});
