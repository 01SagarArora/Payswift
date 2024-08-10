<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page isELIgnored="false"%>
<meta charset="utf-8">

<!-- 				ECash hidden parameters -->
<input type="hidden" id="totalECash" name="totalECash" value="" />
<input type="hidden" id="totalRedeemableECash" name="totalRedeemableECash" value="" />
<input type="hidden" id="eCashRedeemed" name="eCashRedeemed" value="" />

<section class="redeem-block" id="ewallet_div" style="display:none;">
	<div class="total-due">
		<span class="total-txt"><yatra:languageTag content="Total Due" language="${language}"/>:</span>
		<span class="total-bal">
			<span class="rs">Rs.</span>
			<span class="total-amount-due"></span>
		</span>
	</div>
	<label for="slider-1b"><i class="payment-icon eCash-logo"></i><yatra:languageTag content="Redeem your eCash" language="${language}"/></label>
	<!--redeem slider starts here-->
	<div id="redeem-slider">
		<input type="range"  id="eCashToBeRedeemed" value="0" min="0" max="100" data-highlight="true" data-popup-enabled="true" class="ui-hidden-accessible">
		<span class="min">
			<span class="rs">Rs.</span> 0
		</span>
    	<span class="max">
    		<span class="rs">Rs.</span> 
    		<span id="total-redeemable-ecash"></span>
    	</span>
		<div class="redeem-btn">
			<input type="button" data-theme="orange medium" icon-pos="center" value='<yatra:languageTag content="Redeem eCash" language="${language}"/>' id="redeem-ecash-button"/>
		</div>
		<div style="color:#FF0000; font-size: 14px; margin-top: 10px; display: none;" id="ecash-error-message"></div>
	</div>
	<!--redeem slider ends here-->
	<!--redeem sucess starts here-->
	<div class="success-block" id="redeem-success-block" style="display:none;">
		<span class="success-msg">
			<yatra:languageTag content="Redemption of" language="${language}"/> <span class="redem-point-used"><span class="rs">Rs.</span><span class="total-redeemed-ecash"></span></span> <yatra:languageTag content="eCash successfully applied on this booking" language="${language}"/>.
		</span>
		<div class="cancel-redem" id="cancel-redemption-button"><div class="ui-btn-grey"><input type="button" id="redeem-ecash-button-cancel" value='<yatra:languageTag content="Cancel Redemption" language="${language}"/>' icon-pos="center" ></div></div>
		
		<div class="bal-amnt">
			<span class="bal-txt"><yatra:languageTag content="Balance amount to be paid" language="${language}"/></span>
			<span class="bal-cal">(<span><span class="rs">Rs.</span><span class="total-amount-due"></span></span> - <span><span class="rs">Rs.</span><span class="total-redeemed-ecash"></span></span>) = <span class="total-bal"><span class="rs">Rs.</span> <span id="amount-balance"></span></span></span>
		</div>
	</div>
	<!--redeem sucess ends here-->
</section>
<!--applied redeem ends-->