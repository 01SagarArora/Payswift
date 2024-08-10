<!--<div style="width:500px;">-->
		<div class="pop-up-payswift overlay" >
			<h3 class="pop-up-heading-payswift">Pricing Validation Failed</h3>
			<div id="pricing-failure-error-message" class="plr10">
				
			</div>
			<ul class="payswift-btn-area">
				<li>
					<input type="button" class="button grey-btn sleek-btn promo-btn" name="ChangeFlight" id="ChangeFlight" value="Change Flight">
				</li>
				<li>
					<input type="button" class="button primary rounded wid-162 cont-btn" name="ContinueWithNewPrice" id="ContinueWithNewPrice" value="Continue with Updated Price">
				</li>
			</ul>
		</div>
		<div class="msg-popup-payswift id " id="price-change-warning-message-template" style="display: none;">
				{pricingErrorMessages}
					<p>Your final payment amount has therefore changed to {currencySymbol} {newAmount}</p>
			</div>