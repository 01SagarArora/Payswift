<!--Credit Pool starts here-->
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra"%>
<div class="content cpmt_payop" id="tab_cashPool" style="display: none;">
			<p class="heading-p head-tp">Pay with Cash Pool</p>
		<c:forEach var="paymentBean" items="${paymentDisplayBean.paymentOptions}">
			<c:if test="${paymentBean.code == 'cashPool'}">

				<input type="hidden" name="payop" value="cashPool"/>
			
				<div id='cashPool_divErrMsgBlock' class='pmt_errMsgBlock' style='display:none;'></div>
				<div id='cashPool_divWarningMsgBlock' class='pmt_warningMsgBlock' style='display:none;'></div>

				<div class="cpmt_cptxt">
					<div id="CashPoolAmountDiv" style="display:none;" amount='${paymentBean.userBalance}'>
						<yatra:languageTag content="Your cash Pool Balance is <span> ${paymentBean.userBalance} </span> INR" language="${language}"/>
					</div>
								
				</div>

			</c:if>	
		</c:forEach>			
		
</div>
<!--Credit Pool ends here-->