<!-- payu click starts here-->
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>
<div class="content cpmt_payop" id="tab_payu" style="display:none;">
	
	<ul>
		<c:forEach var="paymentBean" items="${paymentDisplayBean.paymentOptions}">				
			<c:if test="${paymentBean.code == 'payu'}">
				<c:forEach var="messageBean" items="${paymentBean.messages}">
					<c:if test="${(messageBean.messageType == 'WARNING') and (messageBean.status == 'ENABLED')}">
						<div class="wflL pmt_warningMsgBlock">
							<span><font color="#FF0000"><yatra:languageTag content="${messageBean.messageText}" language="${language}"/></font></span>
						</div>
					</c:if>
				</c:forEach>
				
				<div class="cpmt_itzTxt">
					<yatra:languageTag content="Dear Customer" language="${language}"/>,<br/>		
			  	<i class="PaymentSprite masterSEc"></i>Logo to be inserted here instead of Master Card
			  	<br/><br/><yatra:languageTag content="Please click 'Pay now' for continue" language="${language}"/>.</div>
			  <div class="cpmt_itzTxt mt20">
					<yatra:languageTag content="Pay via PayU Money to get 80% cashback" language="${language}"/>
			 </div>
				
			</c:if>
		</c:forEach>
	</ul>
</div>
<!-- payu click ends here-->
