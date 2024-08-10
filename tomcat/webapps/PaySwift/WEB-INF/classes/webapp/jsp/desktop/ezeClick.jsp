<!-- amexeze click starts here-->
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>
<div class="content cpmt_payop" id="tab_ec" style="display:none;">
<span class="head-tp">Pay with ezeClick</span>
	<input type="hidden" name="payop" value="ec"/>
	<input type="hidden" name="txntype" value="nm"/>
	
	<ul>
		<c:forEach var="paymentBean" items="${paymentDisplayBean.paymentOptions}">				
			<c:if test="${paymentBean.code == 'ec'}">
				<c:forEach var="messageBean" items="${paymentBean.messages}">
					<c:if test="${(messageBean.messageType == 'WARNING') and (messageBean.status == 'ENABLED')}">
						<div class="wflL pmt_warningMsgBlock">
							<span><font color="#FF0000"><yatra:languageTag content="${messageBean.messageText}" language="${language}"/></font></span>
						</div>
					</c:if>
				</c:forEach>
				
				<input type="hidden" name="bankCode" value='${paymentBean.optionCode}'/>
				<div class="cpmt_displayTxt">
					<i class="PaymentSprite ${paymentBean.logoURL}"></i>
					<yatra:languageTag content="Use" language="${language}"/> <yatra:languageTag content="${paymentBean.displayText}" language="${language}"/> <yatra:languageTag content="for any booking on Yatra.com" language="${language}"/> <yatra:languageTag content="Please click 'Pay now' for continue" language="${language}"/>.</div>
			  <article class="cpmt_amexEzeTxt">
					<yatra:languageTag content="${paymentBean.displayText}" language="${language}"/> <yatra:languageTag content="provides American Express Cardmembers with an easy and fast method to complete their online purchase" language="${language}"/>. <yatra:languageTag content="All you need to do is create a unique ID" language="${language}"/> - <yatra:languageTag content="a one-time process" language="${language}"/>. <yatra:languageTag content="You will not have to refer to your card for any future online transactions" language="${language}"/>. <yatra:languageTag content="You need to simply enter the ezeClick User ID" language="${language}"/>. <yatra:languageTag content="The transaction process is fast, safe and secure" language="${language}"/>. <yatra:languageTag content="Also, you get to Sync multiple American Express Cards to this account" language="${language}"/>.
			  </article>
				
			</c:if>
		</c:forEach>
	
</div>
<!-- amexeze click ends here-->
