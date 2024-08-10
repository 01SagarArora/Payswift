<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>
<!--sbiBuddy starts here-->
<div id="tab_twid" style="display: none;" class="content cash-card  other_option_div">
	<input type="hidden" name="payop" value="twid"/>
	<input type="hidden" name="bankCode" value="twid"/>
	<ul>
		<c:forEach var="paymentBean" items="${paymentDisplayBean.paymentOptions}">				
			<c:if test="${paymentBean.code == 'twid'}">
				<c:forEach var="messageBean" items="${paymentBean.messages}">
					<c:if test="${(messageBean.messageType == 'WARNING') and (messageBean.status == 'ENABLED')}">
						<div class="wflL pmt_warningMsgBlock">
							<span><font color="#FF0000"><yatra:languageTag content="${messageBean.messageText}" language="${language}"/></font></span>
						</div>
					</c:if>
				</c:forEach>
				<div class="pay-with-rewards-icon"></div>	
				<div class="cpmt_itzTxt">
					<yatra:languageTag content="Dear Customer" language="${language}"/>,<br/><br/>		
					<yatra:languageTag content="Please click on pay now and choose your reward program on next page." language="${language}"/><br/>
					<c:if test="${paymentBean.balance > 0}">
						<yatra:languageTag content="Your redeemable points:" language="${language}"/><yatra:languageTag content="${paymentBean.balance}" language="${language}"/>
					</c:if>
				</div>
				<div class="pay-with-rewards-issuers-logos"></div>
								
			</c:if>
		</c:forEach>
	</ul>
</div>
