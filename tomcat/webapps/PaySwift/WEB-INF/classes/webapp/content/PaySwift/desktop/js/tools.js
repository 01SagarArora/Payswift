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
			pos = $(where).position();
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
var Toaster = function(msg, w, timeout, callbackAfter) {
		var target = w;
		var where = (typeof(target)=="undefined") ? "window" : target;
		var sMsg = msg;
		var intervalid = 0;
		var iTimeout = (typeof(timeout)=="undefined") ? 1000 : timeout;
		if(typeof(sMsg)=="undefined") {
			return false;
		}
		var id = "toater_"+Toast.counter++;
		if($("#"+id).length==0) {
			//create new one
			$('body').append("<div id='"+id+"' class='toasterHolder'></div>");
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
				target = (typeof(target)=="string") ? document.getElementById(target)  : target;
				if(target["left"]) {
					left = target["left"];
				};
				if(target["top"]) {
					top = target["top"];
				};
				if(!target["left"] && !target["top"]) {
					pos = $(target).offset();
					left = (pos.left+$(target).width()+20);
					top = (pos.top);//+$(window).scrollTop();	
				}
			break;
		}
//		console.debug(" left "+left + " top "+top);
		$("#"+id).css({position:'absolute', left:left+'px', top:top+'px', zIndex:9999});
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
			$(".toasterHolder").remove();
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
	show: function(msg, w, timeout, callbackAfter) {
		var iTimeout = (typeof(timeout)=="undefined") ? Toast.timeout : timeout;
		this.killAll();
		new Toaster(msg, w, iTimeout, callbackAfter);
	},
	showMultiple: function(msg, w, timeout, callbackAfter) {
		var iTimeout = (typeof(timeout)=="undefined") ? Toast.timeout : timeout;
		new Toaster(msg, w, iTimeout, callbackAfter);
	},
	killAll: function() {
		$(".toasterHolder").remove();
	},
	alignToast: function(onId, where) {
		var toasterid = $(".toasterHolder").attr("id");
		Utils.setPos("#"+toasterid, "#"+onId, where); //setPos: function(s, t, w)		
	}
}
/*************************/


//loader 
var MS_Loader = {
		isInit: false,
		loaderImg:'resources/default/images/ajax-loader-gray-white.gif',
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

$(document).ready(function(e) {
    MS_Loader.init();
});
