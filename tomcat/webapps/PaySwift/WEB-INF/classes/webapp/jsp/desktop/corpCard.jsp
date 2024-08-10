<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>

<!-- Corporate card jsp for the ajax request -->

	
	<ul class="noListStyle">
		<c:forEach var="corpCard" items="${corpCardList}">
				<li>
					<label for='${corpCard.cardId}'>
						
						<span class="custom-Radiobox">
							<input type="radio" name="cardId" id='${corpCard.cardId}' value='${corpCard.cardId}' cardType ='${corpCard.cardType}' cardBrand='${corpCard.cardBrand}' processCardId="corpCard">
							
							<span class="box"><b class="tick"></b></span>
						</span>
						<i class="PaymentSprite ${corpCard.cardBrand}_cc"></i>
						<span class="cpmt_qbCardNo">${corpCard.cardNumber1}-${corpCard.cardNumber2}-${corpCard.cardNumber3}-${corpCard.cardNumber4}</span>
					
					</label>		
					<c:if test="${corpCard.cardType == 'CORPORATE'}">
						<label for="qb_CVV ie6">
							<c:choose>
    									<c:when test="${corpCard.cardBrand == 'AMEX'}">
     									<span class="cpmt_CVVno">CVV<small class="cpmt_cvvLength">4-digit Number</small></span>
							<input type="password" value="" id="corpCard_ccCVV" processCVV="ccCVV" class="w45 cpmt_alignMid ie6Input cvvInput" maxlength=4/>
					  			 </c:when>    
    									<c:otherwise>
        								<span class="cpmt_CVVno">CVV<small class="cpmt_cvvLength">3-digit Number</small></span>
							<input type="password" value="" id="corpCard_ccCVV" processCVV="ccCVV" class="w45 cpmt_alignMid ie6Input cvvInput" maxlength=3/>
					  </c:otherwise>
										</c:choose>
								</label>
					</c:if>	
				</li>	

		</c:forEach>
	</ul>			

