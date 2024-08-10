<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>

<section id="tab_corpCard" data-paymentMethod="corpCard" class="content detail-holder other_option_div" style="display: none;">
	
	<!-- input fields to be send to the lob-->

	<input type="hidden" name="payop" value="corpCard"/>
	<input type="hidden" id="corpCard_cardType_id" name="cardType" value=""/>
	<input type="hidden" id="corpCard_cardBrand_id" name="cardBrand" value=""/>
	<input type="hidden" id="corpCard_cvv" name="ccsc" value=""/>
	<div id='corpCard_divErrMsgBlock' class='pmt_errMsgBlock' style='display:none;'></div>
	<div id='corpCard_divWarningMsgBlock' class='pmt_warningMsgBlock' style='display:none;'></div>
	
	<div id="corpCard_section">
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
	</div>

</section>
