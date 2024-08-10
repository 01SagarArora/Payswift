<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>
<!--oxigen b2b starts here-->
<div id="tab_oxigen_b2b" style="display: none;" class="content cash-card  other_option_div">
	<input type="hidden" name="payop" value="oxigen_b2b"/>
	<ul>
		<c:forEach var="paymentBean" items="${paymentDisplayBean.paymentOptions}">				
			<c:if test="${paymentBean.code == 'oxigen_b2b'}">
				<c:forEach var="messageBean" items="${paymentBean.messages}">
					<c:if test="${(messageBean.messageType == 'WARNING') and (messageBean.status == 'ENABLED')}">
						<div class="wflL pmt_warningMsgBlock">
							<span><font color="#FF0000"><yatra:languageTag content="${messageBean.messageText}" language="${language}"/></font></span>
						</div>
					</c:if>
				</c:forEach>
				
				<div class="cpmt_itzTxt">
					<yatra:languageTag content="Dear Customer" language="${language}"/>,<br/></br>		
			  	<yatra:languageTag content="Please click 'Pay now' to continue. You will be required to login on next page." language="${language}"/></div>				
			</c:if>
		</c:forEach>
	</ul>
</div>
<!--oxigen b2b ends here-->