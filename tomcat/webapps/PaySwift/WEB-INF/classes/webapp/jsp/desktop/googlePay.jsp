<!--net Banking starts here*-->
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>
<div class="content cpmt_payop" id="tab_tez" style="display:none;">
<span class="head-tp">Pay with Google Pay</span>
	<input type="hidden" name="payop" value="tez"/>
	<input type="hidden" name="bankCode" value=""/>
	<input type="hidden" id="iframeEnabledFor-tez" value=""/>
	<input type="hidden" id="iframeEnabledFor-tez-bank" value=""/>
	<div class="">
		<label for="payerVAMobile" class="cpmt_cardNameLabel">Enter mobile number linked with Google Pay</label>
		<input type="text" autocomplete="off" data-msginfo="* required|Enter Valid Mobile Number" data-validation="required|checkVAMobile" name="payerVAMobile" id="payerVAMobile" class="w300 ie6Input">
		<div style="margin-top: 25px;">
		   	1. Enter your mobile number and click on pay now. <br>
			2. You will receive payment request from Yatra in your Google Pay app. <br>
			3. Enter mpin in your Google Pay app to authorize payment. <br>
		 </div>
	</div>
</div>
<!--net Banking ends here-->