<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>
<%@ page isELIgnored="false"%>
<meta charset="utf-8">

<!-- 				ECash hidden parameters -->
<input type="hidden" id="ew_payOption" name="payop" value="ew" />
<input type="hidden" id="totalECash" name="totalECash" value="" />
<input type="hidden" id="totalRedeemableECash" name="totalRedeemableECash" value="" />
<input type="hidden" id="eCashToBeRedeemed" name="eCashToBeRedeemed" value="" />
<input type="hidden" id="eCashRedeemed" name="eCashRedeemed" value="" />
<input type="hidden" id="nexpEcashForGVToRedeem" name="nexpEcashForGVToRedeem" value="" />
<input type="hidden" id="expEcashForGVToRedeem" name="expEcashForGVToRedeem" value="" />
<input type="hidden" id="previousPayOp" value="" />

<div id="ecash-div" style="display: none;">
	<div class="outer-redeem" >
		<!--redeem block starts here-->
		<div class="redeem-block" id="redeem-block-id">
			<!--div class="redeem-slider">
				<div class="slider-base">
					<div class="min-limit">
						<span class="RupeeSign">Rs.</span> 0
					</div>
					<div class="max-limit">
						<span class="RupeeSign">Rs.</span> <span id="total-redeemable-ecash"></span>
					</div>
				</div>

			</div--> 
				<div class="available-ecash-bal">
					<span>
						Available <span class="ecash-txt">eCash</span> :
					</span>
					<span class="amount-bal">
						<span class="RupeeSign">Rs.</span>
						<span id="totalECash-Bal"></span>

					</span>
				</div>
			<div class="redemable-ecash">
				<span id="redeemEcashText">Redeem eCash:</span>
				<span id="redeemableEcashText">Redeemable eCash:</span>
				<span class="mob-align">
					<span class="RupeeSign">Rs.</span>
					<span id="total-redeemable-ecash"></span>
				</span>
				<span class="text-mob">eCash applicable with this booking</span>
			</div>
			<div class="redm-btn">
				<input type="button" id="redeem-ecash-button" value="Redeem Now" name="" class="ytBtn ytBtnOrange"> 
			</div>
		</div>
		<!--redeem block ends here-->
		<!--applied redeem starts -->
		<div style="display: none;" class="redeem-applied" id="redeem-applied-id">
			<div class="available-ecash-bal">
					<span>
						Available <span class="ecash-txt">eCash</span> :
					</span>
					<span class="amount-bal">
						<span class="RupeeSign">Rs.</span>
						<span id="totalECash-Balj"></span>

					</span>
				</div>

				<div class="redemable-ecash">
				<span class="mob-align">
					<i class="PaymentSprite ewallet-success"></i>
					<yatra:languageTag content="Your eCash" language="${language}"/><span class="applied-ecash"> <span class="RupeeSign">Rs.</span> <span class="total-redeemed-ecash"></span> </span><yatra:languageTag content="redeemed successfully" language="${language}"/> 
					<i class="ico_info_cvv PaymentSprite"></i>
					<div class="info-tooltip cvvtoolTip">
										<div class="tooltip-content ">
										If your payment goes into pending state, the eCash that you have redeemed would be credited in your account within 30 mins.
									</div>
									</div>
				</span>
				<span class="remove-now-btn">
					<a href="javascript:void(0);" class="cancel-redemption-active" id="cancelRedemption"><!--i class="sprite-step3 cancel-icon"></i--><yatra:languageTag content="Remove" language="${language}"/></a>
				<span class="cancel-redemption-inactive" style="display:none;"><yatra:languageTag content="Remove" language="${language}"/></span> 
				</span>
			</div>
			<div class="redm-btn">
				 
			</div>
			<!--h3>
				<yatra:languageTag content="Redemption of" language="${language}"/><span class="applied-ecash"><span class="RupeeSign">Rs.</span> <span class="total-redeemed-ecash"></span></span><yatra:languageTag content="eCash successfully applied on
				this booking" language="${language}"/>  <a href="javascript:void(0);" class="cancel-redemption-active" id="cancelRedemption"><i class="sprite-step3 cancel-icon"></i><yatra:languageTag content="Cancel Redemption" language="${language}"/></a>
				<span class="cancel-redemption-inactive" style="display:none;"><yatra:languageTag content="Cancel Redemption" language="${language}"/></span>
			</h3-->
			<!--div class="ecash-info" id="ecash-tupple">
            <i class="sprite-step3 info-arrow"></i>
    <span class="info-lft"><yatra:languageTag content="If your payment goes into pending state, the eCash that you have redeemed would be credited in your account within 30 mins" language="${language}"/>. </span>
     <a class="info-rgt" id="okgotitlink" href="#"><yatra:languageTag content="Ok, Got it" language="${language}"/>!</a>         
  </div-->
			<!--h4>
				<span class="ecash_head"><yatra:languageTag content="Balance amount to be paid" language="${language}"/></span> <span class="total-used">(<span class="RupeeSign">Rs.</span> <span id="total-price"><span class="total-amount-due"></span></span>
					- <span class="RupeeSign">Rs. </span><span id="redm-used"><span class="total-redeemed-ecash"></span></span>)
				</span> = 
				<span class = "balance_amt"><span class="RupeeSign">Rs. </span><span id="available-bal"></span></span>
			</h4-->
		
		</div>
		<!--div style="font-size: smaller;"><span class="gst_text" id="gst_text_id" style="display:none;">* Please Note: Your taxes have been updated on redeeming ecash. Please review the final amount in Payment Details.</span></div-->
			<div class="eCash-loader">
				<div class="loader-blo">
					<span class="span-txt">Please Wait ...</span>
					<span class="loader-dot">
					<i>&nbsp;</i>
					<i>&nbsp;</i>
					<i>&nbsp;</i>
				</span>
				</div>
			</div>
		<div style="color:#E56811;display:none;font-size:14px;margin-top: 10px;" id="ecash-error-message"></div>
		<div class="ecash-warning-msg" style="display:none" id="ecash-warning-message">
			<i class="ico_info_ecash PaymentSprite"></i>
			<span>Only eCash credited as refund can be used with the applied Promo Code.</span>
		</div>
	
	</div>
</div>
<!--applied redeem ends-->