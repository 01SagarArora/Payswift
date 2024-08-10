	<!--<div style="width:500px;">-->
		<div class="pop-up-payswift overlay" >
			<h3 class="pop-up-heading-payswift">Promocode Validation Failed</h3>
			<div id="promo-code-failure-message" class="plr10">
				
			</div>
			<ul class="payswift-btn-area">
				<li>
					<input type="button" class="button grey-btn sleek-btn promo-btn" name="ChangePromo" id="ChangePromo" value="Change Promocode">
				</li>
				<li>
					<input type="button" class="button grey-btn sleek-btn promo-btn" name="ChangePayOpt" id="ChangePayOpt" value="Change Payment Option">
				</li>
				<li>
					<input type="button" class="button primary rounded wid-162 cont-btn" name="leavePromo" id="ContinueWithoutPromo" value="Continue without Promocode">
				</li>
			</ul>
		</div>
		<div class="msg-popup-payswift id " id="promo-code-failure-message-template" style="display: none;">
				{promoMessages}
				<p>You can also select continue without promocode to make a payment of {currencySymbol} {fullAmount}(the promo discount of {currencySymbol} {promoAmount} will be reverted {gstMessage})</p>
			</div>
		