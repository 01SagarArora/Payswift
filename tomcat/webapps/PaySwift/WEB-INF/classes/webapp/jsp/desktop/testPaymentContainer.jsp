<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>
<%@ taglib uri="/WEB-INF/tld/yatraweb.tld" prefix="y" %>

<y:purl var="js_prefix" key="js_prefix" def="https://secure.yatra.com" />

<script type="text/javascript" src="${js_prefix}/${static_content_url}/PaySwift/checkout/desktop/20.84/js/jquery-1.8.3.min.js"></script>
<script type="text/javascript">
<y:purl var="cssversion" key="jsversion" def="1"/>


function getPaymentJSON(){
var a= {"amount":3376,"cartAmount":3376,"userEmail":"","superPNR":"010915F60438","merchant_code":"yatra","product_code":"dom2","convenience_fee":200,"wallet":{"maxRedeemableECash":169},"international":{"travelDate":"09/06/2015 6:30:00 am","sid":"2a457fcf47be8dc5f3e4c9d36486c3a","origin":"DEL","destination":"BOM","tripType":"O"},"partialpayment":{"isEligibleForPartial":"true","payNowAmount":"3","partialPayNowAmount":"2","partialPayLaterAmount":"1"},"holdAndPay":{ "isEligibleForHold":"true","payNowAmount":"3","holdAndPayNowAmount":"1","holdAndPayLaterAmount":"2"},"agentPoolType":"credit"};
return a;
}

function processPayOp(){
	return "payNow";
}

function processCardPayment(card){
	return "payNow";
};

function getContinueButtonId(){
	return "payNow";
}
function processPartialPayment(partialPayOption){console.log("notifying pay mode as : "+ partialPayOption)}
</script>

<c:set var="clientType" value="${requestScope.Error_Message}" />
<c:choose>
  <c:when test="${client=='WAP'}">
		<jsp:include page="../wap/wapPaymentContainer.jsp"/>
  </c:when>
  <c:otherwise>
   		<jsp:include page="paymentContainer.jsp"/>
  </c:otherwise>
</c:choose>
		<div class="panel_contPay">
		
		<div class="wfull mt10" id="panel-payNow-button">
			<div>
				<input type="button" class="ytBtn ytBtnOrange" style="width:100px !important;font-size:14px;" id="payNowId" value="Pay Now">
			</div>
		</div>
		<div class="wfull mt10" style="display:none;" id="panel-continue-button">
			<div>
				<input type="button"  id="completeBooking" value="Complete Booking" name="" class="ytBtn ytBtnOrange" style="width:187px;font-size:14px;">
			</div>
		</div>
		<div id="panel_TnC_div" style="font-size: 13px; margin-top: 50px;">
				By Clicking on pay now, you are agreeing to Yatra's <a target="_blank" href="https://www.yatra.com/c/journal_articles/view_article_content?groupId=23306&articleId=25400&version=1.0">Terms and Conditions</a>
		</div>
		<div id="panel_TnC_div_c" style="font-size: 13px; margin-top: 50px;display:none;">
				By Clicking on Complete booking, you are agreeing to Yatra's <a target="_blank" onclick="return tc_popitup('https://www.yatra.com/c/journal_articles/view_article_content?groupId=23306&articleId=25400&version=1.0', 700, 500);" href="javascript:void(0);">Terms and Conditions</a>
		</div>
	</div> 

