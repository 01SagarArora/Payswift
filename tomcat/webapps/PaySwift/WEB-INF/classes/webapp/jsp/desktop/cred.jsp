<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>
<div class="content cpmt_payop" id="tab_cred" style="display:none;">
	<div class="cred-icon"></div>
	<input type="hidden" name="payop" value="cred"/>
	<input type="hidden" name="bankCode" value=""/>
	<input type="hidden" id="iframeEnabledFor-tez" value=""/>
	<input type="hidden" id="iframeEnabledFor-tez-bank" value=""/>
	<div class="">
		<label for="credMobile" class="cpmt_cardNameLabel">Enter mobile number linked with CRED</label>
		<input type="text" autocomplete="off" data-msginfo="* required|Enter Valid Mobile Number" data-validation="required|checkVAMobile" name="credMobile" id="credMobile" class="w300 ie6Input">
		<div style="margin-top: 25px;">
		   	1. Enter your mobile number and click on pay now. <br>
			2. You will receive payment request from Yatra in your CRED app. <br>
			3. Complete your payment in CRED app. <br>
		 </div>
	</div>
</div>