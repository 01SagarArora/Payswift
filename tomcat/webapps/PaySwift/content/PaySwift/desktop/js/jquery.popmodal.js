// inspired by bootstrap popup
// author: Marghoob Suleman
// version: 1.3.2.1
// dependency: CustomLoader, jQuery
//@added "$(popover).addClass(contentcss);", this edit required to handle new UI of Model
 //fixed ajax loader issue in ajax content for 1.8.3 jQuery
(function($, window) {
	//make compatible with old jquery
	if ($.fn.prop === undefined) {$.fn.prop = $.fn.attr;}
	if ($.fn.on === undefined) {$.fn.on = $.fn.bind;$.fn.off = $.fn.unbind;}
	//start class - Static + dynamic
	function PopModal(ele, options) {
		var  settings = $.extend({
							templatePopover:'<div id="popoverWindowOverlay" style="background:#000000" class="popoverOverlay hide"></div><a id="msStaticPopover" href="#" style="display:none"></a><div id="popoverWindow" class="popover"><div class="arrow poparrow"></div><h3 class="popover-title clearfix"><span class="popover-titlecontent modalTitle"></span><span class="PaymentSprite ico_close modal-dismiss hide yticonClose srpSprite" title="Close" style="display:none">&nbsp;</span></h3><div class="popover-content modalBody"></div><div class="modal-footer hide"></div></div>',
							width:250,
							loadingLabel:"Please wait...",
							fixedFooter:true
							}, options);
		var $this = this;
		var element = $(ele);
		var popupOverlay = "#popoverWindowOverlay";
		var popover = "#popoverWindow";
		PopModal.asModal = false;
		PopModal.isVisible = false;
		var hidemanual = element.data("hidemanual") || "false";
		hidemanual = hidemanual.toString();
		var delay = parseInt(element.data("delay")) || 0;
		var trigger = element.data("trigger") || "click";
		trigger = (trigger == "hover" || trigger=="mouseenter" || trigger=="mouseover") ? "mouseenter" : trigger;
		var oldTitle = "";
		if(trigger=="manual") {
			element.data("manual", true);
			trigger = "click";
		};
		//event binding
		element.on(trigger, function(evt) {
			evt.preventDefault();
			$this.show();
			$.popmodal.current = this;
		});
		//console.log("trigger "+trigger)
		if(trigger == "mouseenter") {
			element.on("mouseout", function() {
				$this.hide();
			}); //hide on mouseout

		};
		var updateTitle = function(force) {
			var data = element.data();
			var title = data["originalTitle"] || element.prop("title") || "";			
			title = (force!==undefined) ? force : title;
			$(popover + " .modalTitle").html(title);
			oldTitle = title;
			element.prop("title", "");
		};		
		var updateContent = function(content, status) {
			var contentmanupluate = element.data("contentmanupluate");
			if(contentmanupluate) {
				try {
					//content = '[[{"ac":"G8","an":"Go Air","fbc":"XXXXX","ft":"Refundable","c":"","rt":"Refundable","cp":"Rs. 1000 will be charged as the cancellation fee. Tickets booked under special return fare , partial cancellation is not allowed. Both onwards and return tickets will be cancelled together at charge Rs 1000 per sector per person. For No show refund, please contact us at the earliest - Any refund claim post 15 days of travel date will not be processed by the airline","chp":"Rs. 1000 plus fare difference will be charged if change is done up to 4 hours prior to flight departure. No changes will be done within 4 hours of flight departure.","ri":"Rs. 1000 plus fare difference will be charged if change is done up to 4 hours prior to flight departure. No changes will be done within 4 hours of flight departure.","yf":"300","o":"Rs 300 per person per sector","cm":"A","camt":"1000","yof":"500"}]]';
					content = eval(contentmanupluate).call(this, $.trim(content), element.data("returnparam"));
				} catch(e) {
					content = settings.loadingLabel;//e.message + " line numer "+e.lineNumber + " filename "+ e.fileName;
					
				};
			};
			var fixedFooter = (element.data("fixedfooter")==false) ? false : settings.fixedFooter;
			var hasFooter = false;
			if(fixedFooter) {
				if(content!=settings.loadingLabel) {
					var modalFooter = $(content).find('.modal-footer');					
					hasFooter = (modalFooter.length>0) ? true : false;					
					if(hasFooter) {
						$(popover + " .modal-footer").html($(modalFooter[0]).html()).show();
					} else {
						$(popover + " .modal-footer").html("");					
					};
				};				
			};
			$(popover + " .modalBody").html(content);
			if(hasFooter) {
				$(popover + " .modalBody .modal-footer").remove();
			};
			//set height
			$(popover + " .modalBody").removeAttr("style");
			if($(popover + " .modalBody").height() > $(window).height()) {
				setInternalWidthHeight(undefined, $(window).height()-150, "auto");
			};
			$this.alignBox();
		};
		var setInternalWidthHeight =  function(w, h, o) {
			$(popover + " .modalBody").removeAttr("style");
			var css = {};
			if(w) {css["width"] = w};
			if(h) {css["height"] = h};
			if(o) {css["overflow"] = o};
			$(popover + " .modalBody").css(css);
		};
		var set_width = function(w, h) {			
			var width = (w) ? w : element.data('width') || settings.width;
			var height = (h) ? h : element.data('height') || "auto";		
			
			$(popover).css({width:width+"px"});
			if(height) {
				$(popover).css({height:height});
			};
		};
		this.setWidth = function(w) {
			set_width(w);
			$this.alignBox();
		};
		this.alignBox = function() {
			var pos = element.offset();
			var $popover = $(popover);
			var popoverWidth = $popover.width();
			var popoverHeight = $popover.height();
			var placement = element.data('placement') || "bottom";
			var arrowOffset = "-11px";
			var tp = {};
			$(popover+" .poparrow").removeAttr("style");//remove old
			switch (placement) {
				case 'bottom':
					arrowOffset = ((popoverWidth/2) - 10);
					$(popover+" .poparrow").css({marginLeft:(arrowOffset)+"px"});
					tp = {top: pos.top + element.height(), left: pos.left + element.width() / 2 - popoverWidth / 2};
				break;
				case 'bottomleft':
					arrowOffset = "-"+((popoverWidth/2) - 10);
					$(popover+" .poparrow").css({marginLeft:(arrowOffset)+"px"});
					tp = {top: pos.top + element.height(), left: pos.left - 10};
					placement = 'bottom'; //for css
				break;
				case 'bottomright':
					arrowOffset = ((popoverWidth/2) - 30);
					$(popover+" .poparrow").css({marginLeft:(arrowOffset)+"px"});
					tp = {top: pos.top + element.height(), left: (pos.left + element.width()) - popoverWidth };
					placement = 'bottom'; //for css
				break;
				case 'top':
					tp = {top: pos.top - popoverHeight, left: pos.left + element.width() / 2 - popoverWidth / 2};
				break;
				case 'left':
					tp = {top: pos.top + element.height() / 2 - popoverHeight / 2, left: pos.left - popoverWidth};
				break;
				case 'right':
					tp = {top: pos.top + element.height() / 2 - popoverHeight / 2, left: pos.left + element.width()};
				break;
				case 'window':
					tp = {top:$(window).scrollTop()+ $(window).height() / 2 - popoverHeight / 2, left: $(window).width()/2 - popoverWidth/2};
				break;
				default:
					try {
						tp = eval(placement);
					} catch(e) {
					}
				break;
			};
			$(popover).removeClass("fade in top right bottom left");
			$(popover).addClass("fade in "+placement);
			$(popover).show();
			$(popover).css({top:tp.top+'px', left:tp.left+'px'});
			if(placement=="window" || PopModal.asModal==true) {
				$(popupOverlay).css({height:$(document).height(), width:$(window).width(), position:"fixed", top:0, left:0, opacity:.5, zIndex:9999});
				$(popupOverlay).show();
				if(placement=="window") {
					$(popover+" .arrow").hide();
				};
			};
		};
		this.show = function() {
			initEvents();						
			var data = element.data();
			$(popupOverlay).hide();
			$(popover+" .arrow").show();
			$(popover + " .modal-footer").hide();
			/**********  static *****************/
			PopModal.asModal = data["modalwindow"] || false;
			PopModal.manual = data["manual"] || false;
			PopModal.onshow = data["onshow"] || null;
			PopModal.onhide = data["onhide"] || null;
			PopModal.onsuccess = data["onsuccess"] || null;
			PopModal.onerror = data["onerror"] || null;
			window.sendPopUpCallback.onsuccess = PopModal.onsuccess;
			window.sendPopUpCallback.onerror = PopModal.onerror;
			/**********  /static *****************/			
			var doif =  data["doif"]; 
			if(doif) {
				doif = eval(doif);
				var doelse =  data["doelse"];
				if(doif===0 && doelse) {
					eval(doelse);
				};
			} else {
				doif = 1; //do if
			};
			if(doif===1) {
				PopModal.isVisible = true;
				var remote = data["remote"] || false;
				var ajax = data["ajax"];
				var url = data["ajaxurl"] || data["url"] || element.prop("href");
				var contentcss = data["contentcss"];
				$(popover+" .modalBody").removeClass().addClass("popover-content modalBody");//remove old and add new all
				$(popover).removeClass().addClass("popover");				
				if(contentcss) {
					$(popover+" .modalBody").addClass(contentcss);
					$(popover).addClass(contentcss);
				};				
				set_width(); //set default
				//update title
				updateTitle();
				if(!remote) {
					var linkelement = data["linkelement"] || data["linkedelement"]; //get html from this div
					var template = data["template"]; //for futer use					
					//content and dynamic content
					var content = data["content"]; //inline content
					var contentDynamic = data["contentdynamic"]; //get content from a method
					if(contentDynamic && contentDynamic.toString() == "true") {
						content = eval(content);
					};
					//if using content from local div;
					if(linkelement) { //inline content			  			  
					  content = $(linkelement).html();
					  $this.lastElement = {html:content, item:linkelement, isVisible:$(linkelement).is(":visible")}; //to maintain 
					  $(linkelement).html("");
					  $(linkelement).hide();
					};
					//update conetnt
					if(content) {
						updateContent(content, 1);
					};
					if(ajax && ajax.toString()=="true") {
						  var loadingLabel = data["loadinglabel"] || settings.loadingLabel;
						  var noCache = data["nocache"] || false;
						  var method  = data["method"] || "POST";
						  updateContent(loadingLabel);
						  //use common ajax						  
						  CustomLoader.ajax({
							  url:url,
							  noCache:noCache,
							  type:method,
							  success:function(res) {
								updateContent(res, 1);
								sendSuccessCallback();
							  },
							  error: function() {
								 // CustomLoader.showErrorBox(url);
								 $("#brokenlink").val(url || "");
								 updateContent($("#errorAlert").html(), 0);
								 $this.setWidth($("#errorAlert").outerWidth());
								 if(PopModal.onerror != null) {
									eval(PopModal.onerror);
									PopModal.onerror = null; //remove
								};							 
							  }
						  });
						};
				} else if(remote == true) {
					var width, height;
					width = (data["width"]) || ($(window).width() - 150); // 100 pixel both side
					height = (data["height"]) || ($(window).height() - 150); // 100 pixel both side
					setInternalWidthHeight(width, height, "hidden");
					$(popover + " .modalBody").removeAttr("style");
					set_width(width, height);
					$(popover + " .modalBody").html("<iframe id='popoverWindow_iframe' onload=\"window.sendPopUpCallback(this)\" frameborder='0' src='"+url+"' height='"+(height-50)+"' width='"+(width-27)+"'></ifram>");
					$this.alignBox();
				};
				if(PopModal.onshow != null) {
					eval(PopModal.onshow);
					PopModal.onshow = null;
				};
				$this.alignBox();
			};
		};
		var sendSuccessCallback = function() {
			if(PopModal.onsuccess != null) {
				eval(PopModal.onsuccess);
				PopModal.onsuccess = null; //remove
			};
		}
		this.hide = function() {
			if(oldTitle!="") {
				element.prop("title", oldTitle);
			};
			$.popmodal.autoHide.timer = null;
			PopModal.isVisible = false;
			$(popupOverlay).hide();
			$(popover).hide();
			$(popover + " .modalBody").html("");
			$(popover + " .modalTitle").html("");
			if($this.lastElement) {
				$($this.lastElement.item).html($this.lastElement.html);
				if($this.lastElement.isVisible) {
					$($this.lastElement.item).show();	
				} else {
					$($this.lastElement.item).hide();
				};
				$this.lastElement = null;
			};
			if(PopModal.onhide != null) {
				eval(PopModal.onhide);
				PopModal.onhide = null;
			};
			removeEvents();
			document.documentElement.style.overflow = ""; // Pls. don't remove this code. Applied for browser scroller - SOM
		};
		
		var manageScroll = function() {
			if(PopModal.isVisible) {
				$($.popmodal.current).popmodal().data("popmodal").alignBox();
			};
			PopModal.scrolling = true;
			if(PopModal.intervalId) {
				clearTimeout(PopModal.intervalId);
			};
			PopModal.intervalId = setTimeout(function() {
				PopModal.scrolling = false;
			}, 20);
		};
		var manageResize = function() {
			if(PopModal.isVisible) {
				$($.popmodal.current).popmodal().data("popmodal").alignBox();
			};
		};		
		var initEvents = function() {
			$(document).on("mouseup", initDocumentMouseUp); //init events
			$(window).on("scroll", manageScroll);
			$(window).on("resize", manageResize);
		};
		var removeEvents = function() {
			$(document).off("mouseup", initDocumentMouseUp); //init events
			$(window).off("scroll", manageScroll);
			$(window).off("resize", manageResize);
		};
		var initDocumentMouseUp = function(e) {
			e.stopPropagation();
			e.preventDefault();
			PopModal.scrolling = (!PopModal.scrolling) ? false : PopModal.scrolling;
			var hideThis = false;
			var elemPrevent = false;
			// ignore click on calendar - this will go in a method
			if(PopModal.exception_div && PopModal.exception_div.length>0) {
				var totalDivs = PopModal.exception_div.length;
				for(var i=0;i<totalDivs;i++) {
					var current = PopModal.exception_div[i];
					if($(e.target).parents(current).length>0 || ($(e.target).closest(current).length==1)) {
						elemPrevent = true;
						break;
					};
				};
			};
			if(PopModal.asModal==false && elemPrevent==false) {
				var popover_e = $(popover);
				var offest = popover_e.offset();
				var width = popover_e.width();
				var height = popover_e.height();
				var clickedX = e.pageX;
				var clickedY = e.pageY;
				//hack for dropdown
				if(e.target.nodeName == "SELECT" || e.target.nodeName == "OPTION") {
					clickedX = offest.left;
					clickedY = offest.top;
				};
				if((clickedX < offest.left) || (clickedX > (offest.left + width)) || (clickedY < offest.top) || (clickedY > (offest.top + height))) {
					hideThis = true;					
				};
			};
			if (PopModal.scrolling==false && hideThis===true) { 
				$.popmodal.hide();
			};
			
		};
		var initTemplate = function() {
				if(!PopModal.isInit) {
					$("body").append(settings.templatePopover);
					$(".modal-dismiss").on("click", function(evt) {						
						$.popmodal.hide();
					});	
					$(popupOverlay).on("click", function(evt) {
						if(PopModal.asModal==false) {
							$.popmodal.hide();
						};
					});	
					
					$(popover).on("mouseover", function() {	
						if(PopModal.manual==false) {
							$(popover+" .ico_close").show();
						};
					});
					$(popover).on("mouseout", function(evt) {
						evt.preventDefault();
						$(popover+" .ico_close").hide();
					});	
					PopModal.isInit = true;	
				};
				window.sendPopUpCallback = function(ifrmae) {
					if(window.sendPopUpCallback.onsuccess) {
						eval(window.sendPopUpCallback.onsuccess);
						window.sendPopUpCallback.onsuccess = null;
					};
				}
		};
		if(!PopModal.isInit) {
			initTemplate();	
		};
	};
	/*********** exception for hiding *************/
	//prevent some element from closing;
	PopModal.preventHideOnClick = function(div) {
		if(!PopModal.exception_div) {PopModal.exception_div = []};
		var isArray = $.isArray(div);
		if(isArray) {
			PopModal.exception_div = PopModal.exception_div.concat(div);
		} else {
			PopModal.exception_div.push(div);
		};
	};	
	/*********** Make Jquery Plugin *************/
	$.fn.extend({
		popmodal: function(opt) {
			return this.each(function() {
			  if (!$(this).data('popmodal')){
					var obj = new PopModal(this, opt);
					$(this).data('popmodal', obj);
			  };
			});			
		}
	});
	window.PopModal = PopModal;
	//static
	$.popmodal = function(settings) { //static
		var settings = $.extend({
			url:"#",
			title:null,			
			content:null,
			width:250,
			nocache:false,
			success:null,
			error:null,
			placement:'window',
			modalwindow:false,
			manual: false,			
			contentmanupluate:null,
			remote:false,
			fixedfooter:true,
			hidemanual:false,
			delay:0
		}, settings);
		var element = $("#msStaticPopover");
		if(!element.data("popmodal")) {
			$("#msStaticPopover").popmodal();
		};
		element.prop("href",settings.url);
		element.prop("title", settings.title || "&nbsp;");
		element.data("linkelement", settings.linkelement);
		element.data("ajax", (settings.url==="#") ? false : true);
		element.data("nocache", settings.nocache);
		element.data("content", settings.content);
		element.data("width", settings.width);
		element.data("contentdynamic", settings.contentdynamic);
		element.data("placement", settings.placement);
		element.data("onsuccess", settings.success);
		element.data("onerror", settings.error);
		element.data("modalwindow", settings.modalwindow);
		element.data("manual", settings.manual);
		element.data("onshow", settings.onshow);
		element.data("onhide", settings.onhide);
		element.data("contentmanupluate", settings.contentmanupluate);
		element.data("remote", settings.remote);
		element.data("fixedfooter", settings.fixedfooter);
		element.data("hidemanual", settings.hidemanual.toString());
		element.data("delay", settings.delay);	
		element.data("contentcss", settings.contentcss);	
		//open it
		element.trigger("click");
	};
	$.popmodal.hide = function() {
		if($.popmodal.current) {
			$($.popmodal.current).data("popmodal").hide();
		};
	};
	$.popmodal.autoHide = function(time) {
		if($.popmodal.autoHide.timer) {
			clearTimeout($.popmodal.autoHide.timer);
		};
		if($.popmodal.current) {
			time = time || 3000;			
			$.popmodal.autoHide.timer = setTimeout(function() {
				if($.popmodal.current) { //check again
					$($.popmodal.current).data("popmodal").hide();
				};
			}, time);
		};
	};	
	$.popmodal.reInit = function() {
		$(".popmodal").popmodal();	
	};
})(jQuery, window);
jQuery(document).ready(function(e) {
	//$(".popmodal").popmodal();
	
	//known divs
	window.PopModal.preventHideOnClick(["#ytCalWrap", ".ui-autocomplete"]);
});
/*
example:
<a class="popmodal" title="Subscribe Me" 
	data-ajaxurl="http://loveincafe.localhost/subscribe" 
	data-contentdynamic="true" data-content="dynmicContent" 
	data-placement="bottom"
	href="javascript:void(0)" 
	data-original-title="Subscribe Me"
	data-linkelement
	data-ajax
	data-nocache
	data-content
	data-width
	data-contentdynamic
	data-onsuccess
	data-onerror
	data-modalwindow
	data-manual
	data-onshow
	data-onhide
	data-fixedfooter
	>Subscribe Me</a>
	*/