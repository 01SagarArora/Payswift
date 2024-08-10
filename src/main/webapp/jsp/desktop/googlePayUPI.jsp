<!--net Banking starts here*-->
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>
<div class="content cpmt_payop" id="tab_googlePayUPI" style="display:none;">
<span class="head-tp">Pay with Google Pay</span>
	<input type="hidden" name="payop" value="upi"/>
	<input type="hidden" name="sub_payop" value="tez"/>
	<input type="hidden" name="bankCode" value=""/>
	<input type="hidden" id="iframeEnabledFor-tez" value=""/>
	<input type="hidden" id="iframeEnabledFor-tez-bank" value=""/>
	<div class="">
		<label for="gpayerVA" class="cpmt_cardNameLabel">Enter your Google Pay ID linked with Google Pay</label>
		<input type="text" autocomplete="off" data-msginfo="* required|Enter Valid Google Pay UPI ID" data-validation="required|checkVA" name="payerVA" id="gpayerVA" class="w300 ie6Input">
		<div style="margin-top: 25px;">
		   	1. Enter your Google Pay UPI ID and click on pay now. <br>
			2. You will receive payment request from Yatra in your Google Pay app. <br>
			3. Enter mpin in your Google Pay app to authorize payment. <br>
		 </div>
	</div>
</div>
<!--net Banking ends here-->