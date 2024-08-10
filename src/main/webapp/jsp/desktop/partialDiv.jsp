<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>
	<!--pay later partial payment starts here-->
	<article id="partialPaymentDiv" class="partialPay cpmt_payOptions mb20" style="display:none;">
		<div class="cpmt_paymentBox cpmt_ie6_ml" id="full-payment-div">
			<label class="cpmt_payBlock" for="payNowOption" >
				<span class="custom-Radiobox">
					<input type="radio" name="payOption" value="payFull" id="payNowOption" class="cpmt_alignMid"/>
					<span class="box"><b class="tick"></b></span>
				</span>
				
				<h4><yatra:languageTag content="Pay Now" language="${language}"/></h4>
				<div class="cpmt_paytxt" style="display: none;"><yatra:languageTag content="Pay Now" language="${language}"/>: <span style="float:right;text-align:left"><span class="RupeeSign">Rs. </span><span id="payNowAmount"></span></span></div>
				<div class="cpmt_paytxt" style="display: none;"><yatra:languageTag content="Pay Later" language="${language}"/>: <span style="float:right;text-align:left"><span class="RupeeSign">Rs. </span><span>0</span></span></div>
			</label>
		</div>
		<div class="cpmt_paymentBox" id="partial_payment_div" style="display:none;">
			<label class="cpmt_payBlock" for="partPayOption" >
				<span class="custom-Radiobox">
					<input type="radio" name="payOption" value="payPartial" id="partPayOption" class="cpmt_alignMid">
					<span class="box"><b class="tick"></b></span>
				</span>
				<h4><yatra:languageTag content="Partial Payment" language="${language}"/></h4>
				<div class="cpmt_paytxt" style="display: none;"><yatra:languageTag content="Pay Now" language="${language}"/>: <span style="float:right;text-align:left"><span class="RupeeSign">Rs. </span><span id="partialPayNowAmount"></span></span></div>
				<div class="cpmt_paytxt"  style="display: none;"><yatra:languageTag content="Pay Later" language="${language}"/>: <span style="float:right;text-align:left"><span class="RupeeSign">Rs. </span><span id="partialPayLaterAmount"></span></span></div>
			</label>
		</div>
		
		<div class="cpmt_paymentBox" id="hold_pay_div" style="display:none;">
			<label class="cpmt_payBlock" for="holdAndPayOption">
				<span class="custom-Radiobox">
					<input type="radio" name="payOption" value="holdAndPay" id="holdAndPayOption" class="cpmt_alignMid">
					<span class="box"><b class="tick"></b></span>
				</span>
				<h4><yatra:languageTag content="Pay and Hold" language="${language}"/></h4>
				<!-- <div class="cpmt_paytxt">Pay <span class="RupeeSign">Rs. </span><span id="holdAndPayNowAmount"></span> <yatra:languageTag content="to book your holiday" language="${language}"/></div>
			 -->
				
				</label>
		</div>
	</article>
	<!--pay later partial payment and hold booking ends here-->