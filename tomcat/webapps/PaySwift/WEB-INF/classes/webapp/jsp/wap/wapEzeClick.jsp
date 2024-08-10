<!-- amexeze click starts here-->
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>
<div id="tab_ec" style="display:none;" class="content amex-eze  other_option_div">
	<input type="hidden" name="payop" value="ec"/>
	<input type="hidden" name="txntype" value="nm"/>
		<c:forEach var="paymentBean" items="${paymentDisplayBean.paymentOptions}">				
			<c:if test="${paymentBean.code == 'ec'}">
				<c:forEach var="messageBean" items="${paymentBean.messages}">
					<c:if test="${(messageBean.messageType == 'WARNING') and (messageBean.status == 'ENABLED')}">
						<div class="wflL pmt_warningMsgBlock">
							<span><font color="#FF0000">${messageBean.messageText}</font></span>
						</div>
					</c:if>
				</c:forEach>
				
				<input type="hidden" name="bankCode" value='${paymentBean.optionCode}'/>
				<div class="wfull"><span class="payment-icon amex-card-b"></span></div>
			<div class="eze-txt">
				<span><yatra:languageTag content="Use ezeClick for any booking on Yatra.com" language="${language}"/></span>
				<span><yatra:languageTag content="Please click 'Pay Now' to continue" language="${language}"/>.</span>
			</div>
				
			</c:if>
		</c:forEach>
	
</div>
<!-- amexeze click ends here-->
