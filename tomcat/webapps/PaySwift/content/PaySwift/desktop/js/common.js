var appProp = appProp || {};
//CommonActions.enableCarousel();
var PaymentCommonActions = {
	ids: {
		module_tab_class: "jsModules"
	},
	constants: {
		module_ajaxURL: (appProp && appProp.frescoServiceUrl) ? appProp.frescoServiceUrl : ""
	},
	init: function() {		
		this.initAction();
		this.customSelect();
	},
	getCookie: function(name) {
		var i,x,y;
		var ARRcookies=document.cookie.split(";");
		for (i=0;i<ARRcookies.length;i++) {
		  x=ARRcookies[i].substr(0,ARRcookies[i].indexOf("="));
		  y=ARRcookies[i].substr(ARRcookies[i].indexOf("=")+1);
		  x=x.replace(/^\s+|\s+$/g,"");
		  if (x==name) {
			return unescape(y);
		  }
		}
	},
	getDeviceName: function() {
		var width = $(window).width();
		var device = "desktop";
		if(width>480 && width<=920) {
			device = "tab";
		}
		return device;
	},
	getCarouselProp: function() {
		var deviceName = this.getDeviceName();
		var carsouselProp = {desktop:{width:538, height:167, displayItems:3}, 
								 tab:{width:325, height:167, displayItems:2}};
		return carsouselProp[deviceName];
	},
	customSelect: function(){
		/************start Custom select***********/
		try {
			$('select:not(".template")').msDropdown({enableAutoFilter:true}).bind('change',function(evt){
				if(typeof(BE_Hotels) != "undefined" && typeof($(this).attr(BE_Hotels.ids.agegroup_attribute)) != "undefined"){
					BE_Hotels_Action.onChangeSelectValue(this);
				}else if(typeof(BE_FlightsHotels) != "undefined" && typeof($(this).attr(BE_FlightsHotels.ids.agegroup_attribute)) != "undefined"){
					BE_FlightsHotels_Action.onChangeSelectValue(this);
				}else if(typeof(BE_Flights) != "undefined" && typeof($(this).attr(BE_Flights.ids.agegroup_select_attribute)) != "undefined"){
					BE_Flights_Action.onChangeSelectValue(this);
				}else if(typeof(gMap) != "undefined" && typeof($(this).attr(gMap.ids.city_select_attribute)) != "undefined"){
					gMapAction.onChangeSelectCity(this);
				}
			});
			
		} catch(e) {
			//console.log(e);	
		}
		/************eof Custom select***********/
	},
	oTab:null,
	enableTouch: function(){
		//touch scroll
		var oTabs = this.oTabs;
		$("#touchScroll").msTouch(
			{
			onLeft:function() {
				$(".box").hide();
				$(".box:first").show(1000);
				oTabs.switchTabByCounter(2);
			},
			onRight:function() {
				$(".box").hide();
				$(".box:last").show(1000);
				oTabs.switchTabByCounter(0);
			}
		});
	},
	enableCarouselTouch: function() {		
		if($.fn.msTouch) {
			var carsouselProp = this.getCarouselProp();
			$(".mscarousel").msTouch({
				onLeft: function(res) {
					//$(this).find(".msCarouselNext, .next").trigger("click");
					var data = $(this).data();
					var carousel = data["msCarousel"];
					var items = carousel.items();
					var current = carousel.getCurrentId();
					var visibleItem = data["visibleitem"] || carsouselProp.displayItems;
					visibleItem = parseInt(visibleItem);
					var nextItem = (current+visibleItem);					
					//carousel.goto(nextItem);
				},
				onRight: function() {
					var data = $(this).data();
					var carousel = data["msCarousel"];
					var items = carousel.items();
					var current = carousel.getCurrentId();
					var visibleItem = data["visibleitem"] || 3;
					visibleItem = parseInt(visibleItem);					
					var nextItem = (current-visibleItem);
					nextItem = (nextItem < 0) ? 0 : nextItem;
					//carousel.goto(nextItem);
					//$(this).find(".msCarouselPrevious, .prev").trigger("click");
				}
			})
		}
	},
	enableCustomScroller: function() {
	// custom vertical scroller
		if($.fn.customScrollbar) {
			$(".scrollContainer").customScrollbar({
			  skin: "default-skin", 
			  hScroll: false,
			  updateOnWindowResize: true
			});
		};
	},
	fixUI: function() {
		this.enableCustomScroller();
		this.customSelect();
	},
	enableCarousel: function(div) {
			//Carousel
		try {
			var carsouselProp = this.getCarouselProp();
			$(".carouseldiv" || div).each(function() {
				$(this).msCarousel({
					boxClass:'div.singleOffer, div.comments, li.matrixTopCarousel',
					width:$(this).data("carouselwidth") || carsouselProp.width,
					height:$(this).data("carouselheight") || carsouselProp.height,
					scrollSpeed:$(this).data("carouselspeed") || 500,
					callback:function(arg) {
						var element_id = arg.prop.element_id;
						if(arg.getCurrentId()==0) {
							$("#"+element_id + " .prev").addClass("prevDisabled");
							$("#"+element_id + " .iconSpekPre").addClass("prevDisabled");
						} else {
							$("#"+element_id + " .prev").removeClass("prevDisabled");
							$("#"+element_id + " .iconSpekPre").removeClass("prevDisabled");
						};
					}
					
				});				
				//check if this has less item
				if($(this).find("li.matrixTopCarousel").length<=carsouselProp.displayItems) {
					$(this).find(".next, .prev").hide();
				};
			})			
			
			if(!this.enableCarousel.isInit) {
				this.enableCarousel.isInit = true;
				$(".carouseldiv").parent().find(".next, .iconSpekNxt").on("click", function() {
					var carousel = $(this).parent().find(".carouseldiv").data("msCarousel");
					var totalItem = carousel.item().length;
					var currentItem = carousel.getCurrentId();
					var dispalyedItem = $(this).data("carouseldisplayitem") || carsouselProp.displayItems;
					//console.log("dispalyedItem "+dispalyedItem)
					if(dispalyedItem!=1) {
					if(currentItem != totalItem-dispalyedItem) {
						carousel.next();
					};
					if(currentItem == totalItem-dispalyedItem) {
						//carousel.goto(0);
					};					
					} else {
						carousel.next();
					};
				})
				
				$(".carouseldiv").parent().find(".prev, .iconSpekNxt").on("click", function() {
					var carousel = $(this).parent().find(".carouseldiv").data("msCarousel");
					carousel.previous();
				})			
				this.enableCarouselTouch();
			}
			
		} catch(e) {
			//alert(e.message);
		}		
	},
	initAction: function(){		
		var $this = this;		
		//module loader
		$('.'+this.ids.module_tab_class).on("click",function(){
			var active_class = $(this).data('active');
			if(!$(this).hasClass(active_class)){
				MS_Loader.showLoader(this, "Please wait...", false);
				var parent_nav = $(this).data('parent');
				$(this).parents(parent_nav).find('a.'+$this.ids.module_tab_class).removeClass(active_class);
				$(this).addClass(active_class);
				var module_name = $(this).data('module');
				var module_ajaxURL = $this.constants.module_ajaxURL;
				var module_placer = $(this).data('place');
				var module_ajaxURL_parameter = {};
				module_ajaxURL_parameter.name = module_name;
				module_ajaxURL = module_ajaxURL + "?" + common_func.objToUrl(module_ajaxURL_parameter);
				CustomLoader.ajax({
					url:module_ajaxURL,
					noCache:false,
					type:"POST",
					dataType: "html",
					success:function(res) {
						MS_Loader.hideLoader();
						$('#'+module_placer).html(res);
						$this.fixUI();
					},
					error: function() {
						MS_Loader.hideLoader();
						console.log('module: '+module_name+'is not coming');						 
					}
				});
			}
		});
		
		//Home page big image carousal
		if($.fn.msTabs) {
		this.oTabs = $(".tabholder").msTabs({tabs:'li', effects:'none', speed:'fast', selected:'active', event:'mouseover'}).data("msTabs");
		}
		
		this.enableCarousel();
		//Header selected navs
		$('.nav-tabs li').on("click", function(){
			$('.nav li').removeClass('active');
		  $(this).addClass('active');
		});
		
		$('.jsWinopen').on("click", function(){
			window.location = $(this).data("url");
		});
		
		// custom vertical scroller
		this.fixUI();		
		
		//footer country dropdown
		$('.dropdown').hover(function(){
			$(this).addClass("active");
			$(this).find('ul:first').show();
		}, function() {
			$(this).removeClass("active");
			$(this).find('ul:first').hide();
		});
	}
};

