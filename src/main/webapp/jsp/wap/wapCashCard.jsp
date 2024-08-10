<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>
<!--cash card starts here-->
<div id="tab_cashCard" style="display: none;" class="content cash-card  other_option_div">
	<input type="hidden" name="payop" value="itz" />
	<c:forEach var="paymentBean"
		items="${paymentDisplayBean.paymentOptions}">
		<c:if test="${paymentBean.code == 'cashCard'}">
			<c:forEach var="cashCardBean" items="${paymentBean.cashCards}">
				<c:if test="${(cashCardBean.status == 'ENABLED')}">
					<input type="radio" id="itz-radio" value='${cashCardBean.code}'
						name="cashPg" checked="checked" />
					<label for="itz-radio"><span class="label-align">${cashCardBean.displayText}</span></label>
					<p class="cash-txt"><yatra:languageTag content="If your card is registered with ITZ" language="${language}"/>, <yatra:languageTag content="please click the 'Pay Now' button to make a payment" language="${language}"/>. <yatra:languageTag content="If your card is not registered with ITZ" language="${language}"/>, <yatra:languageTag content="then please select any other payment option to proceed" language="${language}"/>.</p>
				</c:if>
			</c:forEach>
		</c:if>
	</c:forEach>
</div>
<!--cash card ends here-->