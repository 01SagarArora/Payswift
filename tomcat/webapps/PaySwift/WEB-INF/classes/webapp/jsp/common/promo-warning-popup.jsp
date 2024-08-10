<!--<div style="width:500px;">-->
		<div class="pop-up-payswift overlay" >
			<h3 class="pop-up-heading-payswift">Promocode Validation Failed</h3>
			<div id="promo-code-warning-message" class="plr10">
				
			</div>
			<ul class="payswift-btn-area">
				<li>
					<input type="button" class="button grey-btn sleek-btn promo-btn" name="ChangePromo" id="ChangePartialPromo" value="Change Promocode">
				</li>
				<!-- <li>
					<input type="button" class="button grey-btn sleek-btn promo-btn" name="ChangePayOpt" id="ChangePayOptWithPartialPromo" value="Change Payment Option">
				</li> -->
				<li>
					<input type="button" class="button primary rounded wid-162 cont-btn" name="leavePromo" id="ContinueWithPartialPromo" value="Continue with Updated Price">
				</li>
			</ul>
		</div>
		<div class="msg-popup-payswift id " id="promo-code-warning-message-template" style="display: none;">
				{promoWarn}
					<p>Your final payment amount has therefore changed to {currencySymbol} {discountedAmount} (inclusive of promo discount of {currencySymbol} {promoAmount} {gstMessage})</p>
			</div>