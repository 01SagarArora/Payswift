<!--Corporate Card starts here-->
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<div id="tab_corpCard" class="content cpmt_payop"  style="display:none;">

	<!-- input fields to be send to the lob-->

	<input type="hidden" name="payop" value="corpCard"/>
	<input type="hidden" id="corpCard_cardType_id" name="cardType" value=""/>
	<input type="hidden" id="corpCard_cardBrand_id" name="cardBrand" value=""/>
	<input type="hidden" id="corpCard_cno_id" name="cno" value=""/>
	<input type="hidden" id="corpCard_email_id" name="email" value=""/>
	<input type="hidden" id="corpCard_mob_id" name="mob" value=""/>
	<input type="hidden" id="corpCard_cvv_id" name="ccsc" value=""/>
	
	<div id='corpCard_divErrMsgBlock' class='pmt_errMsgBlock' style='display:none;'></div>
	<div id='corpCard_divWarningMsgBlock' class='pmt_warningMsgBlock' style='display:none;'></div>

	<p id="corpCardAuthRequire" style="display:none;"><span>For security reasons please log in to proceed.</span></p><br>
    <div class="new_cc_ui" id="corpLogin" style="display:none;">
    	<div class="qbLogin">
    		<div class="mt5">
    			<input type="text" id="corp_user_email" class="w240 ie6Input" disabled="disabled"/>
    		</div>
    		<div class="mt10">
    			<input type="password" placeholder="Enter your password" class="w240 ie6Input" id="corp_user_password"/>
    			<input type="button" id="corpLoginContinueBtn" class="cpmt_Btn cpmt_BtnOrange latoBold" value="Continue" >
    		</div>
    	</div>
        <div class="clear">&nbsp;</div>
    </div>
    <div id="corpAuthFailMsg" style="font-size: 13px; color : red; display:none;">Your login failed. Please try again or proceed with some other payment option.</div>

	<article id="corpCard" class="cpmt_quickBook corpCardArticleTab" style="display:block;">
		<div id="waitCorpCards" style="display:none;">
		     <center>
             	<strong style="font-size:1.5em;">Please wait while we are getting cards for you...</strong><Br/><Br/>
             </center>
		</div>
	<p class="heading-p head-tp">Pay with Corporate Card</p>
		<c:forEach var="paymentBean" items="${paymentDisplayBean.paymentOptions}">
			<c:if test="${paymentBean.code == 'corpCard'}">
			<ul class="noListStyle">
				<c:forEach var="corpCard" items="${paymentBean.corporateCards}"  varStatus="count">

			<!-- Changes to display starting 6 digit with xxxxxxx in corporate Cards -->
                				        <c:set var="card1" value="${corpCard.cardNumber1}" />
                                        <c:set var="card1_new" value='${fn:replace(card1,card1, "xxxx")}' />

                                         <c:set var="card2" value="${corpCard.cardNumber2}" />
                                         <c:set var="card2_new" value='${fn:replace(card2,card2, "xxxx")}' />
                            <li li-card-id='${corpCard.cardId}' >
							<label  style="width:82%" for='${corpCard.cardId}'>
								
								<span class="custom-Radiobox" style="visibility:hidden;margin-left:-10px;">
									<input type="radio" name="cardId" id='${corpCard.cardId}' value='${corpCard.cardId}' cardType ='${corpCard.cardType}' cardBrand='${corpCard.cardBrand}' cardNumber ='${corpCard.cardNumber1}${corpCard.cardNumber2}${corpCard.cardNumber3}${corpCard.cardNumber4}' processCardId="corpCard">
									
									<span class="box"><b class="tick"></b></span>
								</span>
								<i class="PaymentSprite ${corpCard.cardBrand}_cc"></i>
								<span class="cpmt_qbCardType">${corpCard.cardType}</span>
								<span class="cpmt_qbCardNo">${card1_new}-${card2_new}-${corpCard.cardNumber3}-${corpCard.cardNumber4}</span>
							
							</label>		
							
							<c:if test="${corpCard.cardType == 'CORPORATE'}">
							
									<c:choose>
    									<c:when test="${corpCard.cardBrand == 'AMEX'}">
    									<span class="corporateCvv">
											<label for="corpCard_ccCVV ie6">
     										  <span class="cpmt_CVVno" style="display:none">CVV<small class="cpmt_cvvLength">4-digit Number</small></span>
									<input type="password" value="" id="corporate_cvv_id${count.count}"  placeholder="CVV" name="corpCard_ccCVV" processCVV="corpCardCVV" class="w45 cvvInput cpmt_alignMid ie6Input" maxlength=4  data-validation="required|regType[number]|lenCheck[4]" data-msginfo="* required|* number only|* invalid cvv length" />
										</label>
											</span>
   										 </c:when>    
    									<c:otherwise>
    									<span class="corporateCvv">
											<label for="corpCard_ccCVV ie6">
        							<span class="cpmt_CVVno" style="display:none">CVV<small class="cpmt_cvvLength">3-digit Number</small></span>
									<input type="password" value="" id="corporate_cvv_id${count.count}"  placeholder="CVV" name="corpCard_ccCVV" processCVV="corpCardCVV" class="w45 cvvInput cpmt_alignMid ie6Input" maxlength=3  data-validation="required|regType[number]|lenCheck[3]" data-msginfo="* required|* number only|* invalid cvv length" />
								 <span class="corporateCvv">
											<label for="corpCard_ccCVV ie6">
								  </c:otherwise>
										</c:choose>
									
							</c:if>

							<p card-id='${corpCard.cardId}' style="display:none;" class="token-label">CARD TOKENIZED</p>
							
							<div style="display: block; width: 50%; margin-left: 170px; "> ${corpCard.firstName} ${corpCard.lastName}</div>
						</li>	
				</c:forEach>
			</ul>			

			</c:if>
		</c:forEach>
		
	</article>
	
</div>	
<!--Corporate Card ends here-->

<style>
.token-label {
   background: #F0EEEA;
   padding: 4px 12px;
   border-radius: 7px;
   font-size: .7em;
   text-transform: uppercase!important;
   text-shadow: 0px 0px 0px, 0px 0px, 0px 0px;
   letter-spacing: .1em;
   line-height: 20px;
   white-space: nowrap;
   float: left;
   margin: 5px 0 0 2%;
   color: #666;
   font-family: Rubik,sans-serif;
   font-weight: 600;
   border: 1px solid #ddd;
   box-shadow: 0 1px 8px rgb(0 0 0 / 10%);
}
</style>