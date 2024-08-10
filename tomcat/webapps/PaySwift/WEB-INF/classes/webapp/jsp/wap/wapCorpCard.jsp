<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>

<ul>
	<c:forEach var="corpCard" items="${corpCardList}">
			<li>
				<label for='${corpCard.cardId}'>
				<span>
					<input type="radio" name="cardId" id='${corpCard.cardId}' value='${corpCard.cardId}' cardType ='${corpCard.cardType}' cardBrand='${corpCard.cardBrand}' processCardId="corpCard">
						<i class="payment-icon ${corpCard.cardBrand}-quick"></i>
						<span class="cardNumber">${corpCard.cardNumber1}-${corpCard.cardNumber2}-${corpCard.cardNumber3}-${corpCard.cardNumber4}</span>
				</span>
					<input type="password" value="" id="corpCard_ccCVV" placeholder="CVV No." processCVV="ccCVV" maxlength=3 />
				<br/>
				</label>		
				
			</li>	
	</c:forEach>

</ul>