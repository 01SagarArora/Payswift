<!--Credit Pool starts here-->
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<div id="tab_cp" class="content detail-holder other_option_div" data-paymentMethod="cp" style="display: none;">

	<c:forEach var="paymentBean" items="${paymentDisplayBean.paymentOptions}">

		<c:if test="${paymentBean.code == 'cp'}">

			<div class="cpmt_cptxt">
				<yatra:languageTag content="Dear Agent" language="${language}" />,<br />
				<yatra:languageTag content="Your credit Pool Balance is ${paymentBean.creditAmount} INR" language="${language}" />
				<yatra:languageTag	content="Your cash Balance is ${paymentBean.cashAmount} INR" language="${language}" />.
			</div>

		</c:if>	

	</c:forEach>			
					
</div>
