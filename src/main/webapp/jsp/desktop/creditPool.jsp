<!--Credit Pool starts here-->
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra"%>
<div class="content cpmt_payop" id="tab_cp" style="display: none;">
			<p class="heading-p head-tp">Pay with Credit Pool</p>
		<c:forEach var="paymentBean" items="${paymentDisplayBean.paymentOptions}">
			<c:if test="${paymentBean.code == 'cp'}">

				<input type="hidden" name="payop" value="cp"/>
				<input type="hidden" id="cp_poolType_id" name="poolType" value=""/>
			
				<div id='cp_divErrMsgBlock' class='pmt_errMsgBlock' style='display:none;'></div>
				<div id='cp_divWarningMsgBlock' class='pmt_warningMsgBlock' style='display:none;'></div>

				<div class="cpmt_cptxt">
				<c:if test="${(paymentBean.showBalance == '1')}">
					<div id="CREDITPoolDiv" style="display:none;" amount='${paymentBean.creditAmount}'>
						<yatra:languageTag content="Your credit Pool Balance is <span> ${paymentBean.creditAmount} </span> INR" language="${language}"/>
					</div>

					<div id="CASHPoolDiv" style="display: none;" amount='${paymentBean.cashAmount}'>
						<yatra:languageTag content="Your cash Balance is <span>${paymentBean.cashAmount}</span> INR" language="${language}"/>.
					</div>
					</c:if>	
					
					<c:if test="${(paymentBean.showBalance == '0')}">
					<div id="CREDITPoolDiv" style="display:none;" amount='${paymentBean.creditAmount}'>
						<yatra:languageTag content="This booking will be charged on your corporate Account" language="${language}"/>
					</div>
					<div id="CASHPoolDiv" style="display: none;" amount='${paymentBean.cashAmount}'>
						<yatra:languageTag content="This booking will be charged on your corporate Account"  language="${language}"/>.
					</div>
					</c:if>				
				</div>

			</c:if>	
		</c:forEach>			
		
</div>
<!--Credit Pool ends here-->