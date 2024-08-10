<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
	<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
		<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>
<html class="yt-ui-mobile ui-mobile">
<head>
<link rel="stylesheet" href="/${static_content_url}/PaySwift/common/css/popup-new.css?version=${staticResourceVersion}" type="text/css" />
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=0" name="viewport">
<title>Payment Page</title>
</head>
<script>
var detailsJson = ${bookingDetail};
var client = "${client}";
var messageForDisplay = "${message}";
<c:if test="${not empty surcharge}">
var surcharge = ${surcharge};		
</c:if>
var cardDiscountingStatus="false";
var b2bProducts = "${b2bProducts}";
var otherDiscountingStatus="false";
</script>
			<script type="text/javascript" src="/${static_content_url}/PaySwift/desktop/js/jquery.min.js"></script>
			<script type="text/javascript" src="/${static_content_url}/PaySwift/wap/js/tools-wap.js"></script>
			<script type="text/javascript" src="/${static_content_url}/PaySwift/desktop/js/helper.js?version=${staticResourceVersion}"></script>
			<script type="text/javascript" src="/${static_content_url}/PaySwift/desktop/js/payment-commons.js?version=${staticResourceVersion}"></script>
<div class="loading-bg wfull"></div>

<div data-role='page' id="payment_overlay" class=""> <!-- CONTENT SECTION START HERE -->	
	<!-- New srp header -->
	<div data-role="header" id="" class="yt-header" data-position="fixed"> 
				<a href="#mobileLogin" data-icon="bars" data-iconpos="notext" class="menu-open menu-with-logo ui-icon"></a>
		<!-- Breadcrumb -->
		<section class="yt-steps wfull allow-back-button">
			<ul id="breadCrumbUl" class="wfull steps">
				<li class="complete"><span class="counter wfull" title="1">&nbsp;</span>
					<p class="step-label wfull">
						<span>Review Trip</span>
					</p>
					<!-- tapable ring -->
					<a href="#review-page" class="tapable">&nbsp;</a>
				</li>
				<li class="complete"><span class="counter wfull" title="2">&nbsp;</span>
					<p class="step-label wfull">Passenger Details</p>
					<!-- tapable ring -->
					<a href="#paxDetail_overlay" class="tapable">&nbsp;</a>
				</li>
				<li class="active last"><span class="counter wfull" title="3">&nbsp;</span>
					<p class="step-label wfull">Make Payment</p>
					<!-- tapable ring -->
					<a href="#" class="tapable">&nbsp;</a>
				</li>
			</ul>
		</section>
		<!-- End -->
	</div> 
	<!-- /New srp header -->
	<section data-role="content" class="yt-ui-content">
	
	<section id="failed_payment_res" style="display:none">
			<div id="price_change_overlay_cont"> <!-- START FARE CHANGE MODAL --> 
			<div id="priceChangeMsg_overlay" style="width:100%;padding:1.2em" data-shadow="false" data-corners="false"> 
			<div class="fare-change-modal">
			<div class="change-info"> 
			<i class="ui-icon ui-icon-warn fl">&nbsp;</i> 
			<span id="failed_msg_container" class="text" style="width:80%;float:left;margin-left:10px"> 
			Oops, we seem to have temporary hiccup. We apologise for the inconvenience caused and request you to try again. </span> 
			</div> </div>  </div> <!-- eof FARE CHANGE MODAL --> </div>
	</section>
			
		<form id="paymentForm"  name="paxDet">		
			<section class="yt-pay-main">
				
				<!-- BOOKING SUMMARY START HERE -->
				<section id="itineraryDetailHeader" class="booking-summary wfull" data-role="collapsible"  data-collapsed-icon="right-arrow" data-expanded-icon="down-arrow" data-iconpos="right">
				<h3><div id="bookingSummary_sector"><span class="src-dest three-dot">Booking Summary</span></div></h3>
		
				<ul class="details-summary wfull list-unstyled bxs" id="itineraryHTMLContainer">		
				</ul>
				</section>
				<!-- BOOKING SUMMARY ENDS HERE -->
				<!-- Fare Details -->
				<section class="fare-details wfull hideUnderOverlay" data-role="collapsible"
					data-collapsed-icon="right-arrow" data-expanded-icon="down-arrow"
					data-iconpos="right">
					<h3>You Pay<span class="total-amt"><i class="rs rs-orange">Rs.</i> <span class="youPayPayment" id="totalAmountSpan"></span></span></h3>
					<ul class="details-summary wfull list-unstyled bxs fareDetails_cont" id="breakDownContainer">
					</ul>
				</section>
				<!-- End -->
				<input type="hidden" name="amount" value="" id="amount" />
				
				<div class="wfull">
				<jsp:include page="wapPaymentContainer.jsp" /> 
				</div>
				
					
					<div class="yt-tnc" style="margin-top:0;margin-bottom:2em;">
						<div class="bold-txt" style="padding:1em 0 0.5em 0; display:none" id='convFeeDiv'>
							A processing fee of <span class="rs">Rs.</span> <span id='convFeeText'></span>  is applicable on this booking. 							
						</div>
						By clicking on pay now, you are agreeing to Yatra's<a tabindex="" href="https://www.yatra.com/online/yatra-user-agreement.html" target="_blank"> Terms & Conditions.</a>
					</div>
					<div class="pay-now-btn" style="margin-bottom:3em;">
						<a href="javascript:void(0);" class="relative" >
							<input id="paynow_btn" data-role="button" onclick="ProcessPayment.payNow()" data-theme="orange large" type="button" class="payNowAction" value="Pay Now" />
						</a>
					</div>
			</section>
		</form>		
	</section>
</div>
	<div class="loading-message wfull">
		<h2 class="wfull">
			<i class="ui-icon icon-yatra-loading"></i>
			<span id="loader_title">Processing Payment</span></h2>
		<p class="wfull mr-n" id="loader_message">Please give us a moment</p>
		<i class="wfull loading-icon">&nbsp;</i>
	</div>
	<!-- eof custom yatra loader -->
	<div id="rowTemplate" style="display:none;">
		<li class="wfull">
			<span class="fare-brk-title fl">{label}</span>
			<span class="fare-brk-rs fr"><span class="rs">Rs.</span>{{fn:YatraHelper.formatPrice({amount})}}</span>
		</li>
	</div>
	<div id="rowTemplateString" style="display:none;">
		<li class="wfull">
			<span class="fare-brk-title fl">{label}</span>
			<span class="fare-brk-rs fr">{amount}</span>
		</li>
	</div>
						<div id="time-label" style="display: none;">Time left for booking <span id="time">00:00</span> minutes!</div>
						
	<div id="invalid_promo_code_discount_pop_up" style="display: none; float: left; position: fixed; top: 40%; z-index: 9999999; width: 50% ! important; left: 29%;">
			 <jsp:include page="../common/promo-popup.jsp" /> 
		</div>
	<%-- 	<div id="invalid_visa_discount_pop_up" style="display: none; float: left; position: fixed; top: 40%; z-index: 9999999; width: 40% ! important; left: 29%;">
		<jsp:include page="../common/visa-not-valid.jsp" /> 
		</div> --%>
		<!-- <div id="invalid_partial_visa_discount_pop_up" style="display: none; float: left; position: absolute; top: 614px; z-index: 9999999; width: 40% ! important; left: 26%;">
			<div>
				<h3 style="padding: 0px; font-size: 12px; color: red; font-weight: normal;">Unfortunately {cno} is not valid for Yatra-Visa discount !!</h3>
			</div>
		</div> -->
		<!-- <div id="payop_changed_visa_discount_pop_up" class="payment_popUp" style="display: none; float: left; position: absolute; top: 614px; z-index: 9999999; width: 100% ! important; left: 26%;">
			<div>
				<h3 style="padding: 0px; font-size: 12px; color: red; font-weight: normal;">Unfortunately Visa discount is only available if you select credit card or debit card!!</h3>
			</div>
		</div> -->
		<!-- <div id="visa_discount_applied_pop_up" class="payment_popUp" style="display: none; float: left; position: absolute; top: 614px; z-index: 9999999; width: 40% ! important; left: 26%;">
			<div>
				<h3 style="padding: 0px; font-size: 12px; color: green; font-weight: normal;">Congrats !! You are eligible for visa Discount if you use a valid VISA Card</h3>
			</div>
		</div> -->
		<!-- <div id="valid_partial_visa_discount_pop_up" style="display: none; float: left; position: absolute; top: 55%; z-index: 9999999; width: 50% ! important; left: 26%;">
			<div >
				<h3 style="padding: 0px; font-size: 12px; color: green; font-weight: normal;">Congratulations !! your Card is eligible for Yatra-Visa discount, Your Shopping cart is updated accordingly</h3>
			</div>
		</div> -->
		<div class="black_div_pop" style="background-color: rgb(51, 51, 51); position: absolute; top: 0px; min-height: 100%; height: 100%; z-index: 999999; width: 100%; opacity: 0.5;display:none;"></div>					
		<div id="editStep" style="display: none;"><a href="{url}" >[Edit]</a></div>
	</html>