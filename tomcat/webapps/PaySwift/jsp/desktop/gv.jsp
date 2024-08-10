<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>
<div class="content cpmt_payop" id="tab_gv" style="display:none">
	<article id="gv_block">
		<input type="hidden" name="payop" value="gv"/>
		<p class="gvHeading">Make payment using Gift Vouchers</p>
		<div id="gv-promo-error" class="gv-promo-error"> Gift Vouchers cannot be clubbed with Promos </div>
			<div id="gv-promo-msg-completeBooking" class="gv-promo-error"> To use any other payment method, please remove voucher(s) </div>
		<ul id="addGVDiv" class="noListStyle cpmt_listItem">
			<li class="input-field">
				<label class="cpmt_cardLabel" for="dc_cno_id">Gift Card Number</label>
				<div class="">
				<input type="text"  class="cardNumber gv-number ie6Input" id="gvNo" name="gvNo" maxLength="20" autocomplete="off">
				</div>
			</li>
			<li class="input-field" id="gvPinBlock" style="display:none">
				<label class="cpmt_cardLabel" for="dc_cno_id">PIN</label>
				<div class="">
				<input type="password"  class="cardNumber gv-pin ie6Input" id="gvPin" name="gvPin" maxLength="6" autocomplete="off" style="width:100px">
				</div>
			</li>
			<li class="button-area">
				<input type="button" id="redeem-gv-button" value="Redeem Gift Card" name="" class="gv-btn gv-btn-disabled">
			</li>
		</ul>
		<p class="errorGV" id="errorGV"></p>
		<p class="applied-gift-vouc-txt" id="applied-gift-vouc-txt">Applied Gift Voucher</p>
		<ul class="noListStyle redeem-block" id="appliedGvBlock">
		
		</ul>
		<div id="gv_pmt_msg_WARNING" class="pmt_msg_WARNING" style=" display:none;"> </div>
	</article>
</div>