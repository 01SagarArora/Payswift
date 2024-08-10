<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>
<!--jcash starts here-->
<div id="tab_upiNonMoto" style="display: none;" class="content cash-card  other_option_div">
	<input type="hidden" name="payop" value="cc"/>
	<input type="hidden" id="txntype" name="txntype" value="nm"/>
	<ul>
		<c:forEach var="paymentBean" items="${paymentDisplayBean.paymentOptions}">				
			<c:if test="${paymentBean.code == 'upiNonMoto'}">
				<c:forEach var="messageBean" items="${paymentBean.messages}">
					<c:if test="${(messageBean.messageType == 'WARNING') and (messageBean.status == 'ENABLED')}">
						<div class="wflL pmt_warningMsgBlock">
							<span><font color="#FF0000"><yatra:languageTag content="${messageBean.messageText}" language="${language}"/></font></span>
						</div>
					</c:if>
				</c:forEach>
				
				<div class="cpmt_itzTxt">
					<yatra:languageTag content="Dear Customer" language="${language}"/>,<br/><br/>		
			  	<yatra:languageTag content="Please click 'Pay now' to continue. You will be required to enter VPA or Scan QR Code on next page." language="${language}"/></div>				
			</c:if>
		</c:forEach>
	</ul>
</div>
<!--jcash ends here-->