<!--mobile wallet starts here-->
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>
<c:forEach var="paymentBean" items="${paymentDisplayBean.paymentOptions}">
<c:if test="${paymentBean.code == 'mw'}">
<div id="tab_mw" style="display:none;" class="content airtel-money detail-holder other_option_div">
    <div class="mobile-wallet-cont">
        <input type="hidden" name="payop" value="mw"/>
	    <input type="hidden" name="bankCode" value=""/>
	
        <div class="wfull">
            <select name="mobileWalletDropDown" id="mobileWalletDropDown" class="other-banks" data-msginfo='* <yatra:languageTag content="Please select Option from the dropdown" language="${language}"/>' data-validation="required">
                <option value="" selected="selected"><yatra:languageTag content="Select Wallet" language="${language}"/></option>
                <c:forEach var="suboptionList" items="${paymentBean.suboptionList}">
                   <c:if test="${suboptionList.status == 'ENABLED'}">
                     <option  id="${suboptionList.subOption}" value="${suboptionList.subOption}" ><yatra:languageTag content="${suboptionList.displayName}" language="${language}"/></option>
				  </c:if>
				</c:forEach>
				
            </select>
        </div>
    
    </div>
</div>

</c:if>
</c:forEach>

<!--mobile wallet ends here-->