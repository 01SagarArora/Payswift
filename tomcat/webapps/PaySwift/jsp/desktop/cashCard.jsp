<!--ITZ card starts here-->
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>
<div class="content cpmt_payop" id="tab_cashCard" style="display:none;">
<span class="head-tp">Pay with Cash Card</span>
	<ul class="noListStyle">
		<c:forEach var="paymentBean" items="${paymentDisplayBean.paymentOptions}">				
			<c:if test="${paymentBean.code == 'cashCard'}">
				<c:forEach var="messageBean" items="${paymentBean.messages}">
					<c:if test="${(messageBean.messageType == 'WARNING') and (messageBean.status == 'ENABLED')}">
						<div class="wflL pmt_warningMsgBlock">
							<span><font color="#FF0000"><yatra:languageTag content="${messageBean.messageText}" language="${language}"/></font></span>
						</div>
					</c:if>
				</c:forEach>
					
				<c:forEach var="cashCardBean" items="${paymentBean.cashCards}">
					<c:if test="${(cashCardBean.status == 'ENABLED')}">
						<input type="hidden" name="payop" value="${cashCardBean.code}"/>
						
						<div class="cpmt_itzTxt">
							<yatra:languageTag content="Dear Customer" language="${language}"/>,<br/>
							<yatra:languageTag content="You can use a registered ITZ Cash Card for any booking on Yatra.com" language="${language}"/>.
						</div>
						<div class="cpmt_itzTxt mt20">
							<yatra:languageTag content="If your card is registered with ITZ" language="${language}"/>, <yatra:languageTag content="please click the 'Pay Now' button to make a payment" language="${language}"/>. <yatra:languageTag content="If your card is not registered with ITZ" language="${language}"/>, <yatra:languageTag content="then please select any other payment option to proceed" language="${language}"/>.
						</div>
					</c:if>
				</c:forEach>
			</c:if>
		</c:forEach>
	</ul>
	
	
	
	
</div>
<!--ITZ card ends here-->