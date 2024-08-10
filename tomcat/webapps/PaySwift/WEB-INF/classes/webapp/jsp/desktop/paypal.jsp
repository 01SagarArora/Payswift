<!--Pay Pal starts here*-->
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>
<div id="payPalTemplate" style="display: none">
	<div class="paypal-block-sel">
		<label>
			<span class="custom-Radiobox">
				<input type="radio"  iframeenabled="false" name="paypal"  class="cpmt_alignMid radioMid">
				<span class="box"><b class="tick"></b></span>
			</span>
			<span class="currency-name"> </span>
			<div class="amount-dis">
			<span class="currency-sign">Rs.</span>
			<span class="amount-usd"></span>
			</div>
		</label>
	</div>
</div>

<div class="content cpmt_payop paypal-block" id="tab_paypal" style="display:none;">
				
	<input type="hidden" name="payop" value="paypal"/>
	<input type="hidden" name="bankCode" value=""/>
	<c:forEach var="paymentBean" items="${paymentDisplayBean.paymentOptions}">
		<c:if test="${paymentBean.code == 'paypal'}">
			<div class="heading-p head-tp mb0important">Pay with PayPal</div>
			<div class="mb10 flL paypalLogoSection mt15">
				<i class="paypal-ico">&nbsp;</i>
				<span class="pay-pal-txt"></span>
			</div>
			<div id="paypalContainer">
				
			</div>
				<div class="paypal_pmt_msg_INFO mw_payop_msg">
	         			<span>Please select the Currency</span>
	      			</div>
			
			<!-- <div class="paypal-block-sel">
					<label for="AUD">
						<span class="custom-Radiobox">
							<input type="radio" id="AUD" iframeenabled="false" name="paypal" value="AUD" class="cpmt_alignMid radioMid">
							<span class="box"><b class="tick"></b></span>
						</span>
						<span class="curreny-name">AUD <span class="cur-name">( Australian Dollar )</span></span>
						<div class="amout-dis">
						<span class="">&#x00024;</span>
							<span id="amount_usd">1,3200</span>
						</div>
					</label>
				</div> -->
			</c:if>
		</c:forEach>
	
</div>


<!--PayPal ends here-->