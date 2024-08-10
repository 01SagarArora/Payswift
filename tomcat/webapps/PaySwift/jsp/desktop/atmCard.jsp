<!--ATM Banking starts here*-->
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>
<div class="content cpmt_payop" id="tab_atm" style="display:none;">
				<span class="head-tp head-tp-25">Pay with ATM Card</span>
	<input type="hidden" name="payop" value="atm"/>
	<input type="hidden" name="bankCode" value=""/>
	
	<c:forEach var="paymentBean" items="${paymentDisplayBean.paymentOptions}">
		<c:if test="${paymentBean.code == 'atm'}">
			<c:forEach var="messageBean" items="${paymentBean.messages}">
				<c:if test="${(messageBean.messageType == 'WARNING') and (messageBean.status == 'ENABLED')}">
					<div class="wflL pmt_warningMsgBlock">
						<span><font color="#FF0000"><yatra:languageTag content="${messageBean.messageText}" language="${language}"/></font></span>
					</div>
				</c:if>
			</c:forEach>
					
			
				<article class="cpmt_net cpmt_lastInfo pd-tp-0">
					<ul class="noListStyle">
						<c:forEach var="bankBean" items="${paymentBean.banks}">
							<c:if test="${bankBean.status == 'ENABLED'}">
								<li class="mt20">
									<label for='${bankBean.code}'>
										<span class="custom-Radiobox">
											<input type="radio" id="${bankBean.code}" name="atmBank" value='${bankBean.code}' class="cpmt_alignMid radioMid">
											<span class="box"><b class="tick"></b></span>
										</span>
										<label class="${bankBean.logoURL}" for="${bankBean.code}"></label>
									</label>
								</li>
							</c:if>
						</c:forEach>
					</ul>
				</article>
			</c:if>
		</c:forEach>
	
</div>
<!--ATM Banking ends here-->