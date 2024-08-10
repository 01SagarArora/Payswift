<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>
<c:forEach var="paymentBean" items="${paymentDisplayBean.paymentOptions}">
<c:if test="${paymentBean.code == 'nb'}">
<div id="tab_nb" class="content detail-holder other_option_div" style="display: none;">
	<div class="net-banking-cont">
	<input type="hidden" name="payop" value="nb"/>
	<input type="hidden" name="bankCode" value=""/>
			
	<div class="wfull">
		<select name="netBankDropDowm" id="netBankDropDowm" class="other-banks" data-msginfo='* <yatra:languageTag content="Please select Option from the dropdown" language="${language}"/>' data-validation="required">
		<option value="" selected="selected"><yatra:languageTag content="Select your Bank" language="${language}"/></option>
		<c:forEach var="bankBean" items="${paymentBean.banks}">
			<c:if test="${bankBean.status == 'ENABLED' && not empty bankBean.logoURL}">
				<option value="${bankBean.code}"><yatra:languageTag content="${bankBean.displayText}" language="${language}"/></option>
			</c:if>
		</c:forEach>
		<c:forEach var="bankBean" items="${paymentBean.banks}">
			<c:if test="${bankBean.status == 'ENABLED' && empty bankBean.logoURL}">
				<option value="${bankBean.code}"><yatra:languageTag content="${bankBean.displayText}" language="${language}"/></option>
			</c:if>
		</c:forEach>
		</select>
			</div>
		</div>
</div>
</c:if>
</c:forEach>