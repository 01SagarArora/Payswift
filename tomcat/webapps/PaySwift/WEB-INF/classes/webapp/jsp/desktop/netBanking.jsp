<!--net Banking starts here*-->
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>
<div class="content cpmt_payop" id="tab_nb" style="display:none;">
<span class="head-tp">Pay with Net Banking</span>
<h3 class="gtm-class-text" style="color: rgb(0, 128, 0); margin: 8px; text-align: left;">Avail Easy EMI using HDFC Netbanking Option</h3>
	<input type="hidden" name="payop" value="nb"/>
	<input type="hidden" name="bankCode" value=""/>
	<input type="hidden" id="iframeEnabledFor-nb" value=""/>
	<input type="hidden" id="iframeEnabledFor-nb-bank" value=""/>
	
	<div class="">
	<c:forEach var="paymentBean" items="${paymentDisplayBean.paymentOptions}">
		<c:if test="${paymentBean.code == 'nb'}">
			<c:if test="${not empty payopMessage.nb}">
				<c:forEach var="messageObj" items="${payopMessage.nb}">
					<div class="pmt_msg_${messageObj.messageType} nb_payop_msg">
	         			<span><yatra:languageTag content="${messageObj.messageText}" language="${language}"/></span>
	      			</div>
	      		</c:forEach>
	      	</c:if>
	      	<div id='nb_bank_msg' class='pmt_msg_WARNING' style='display:none;'></div>
			<p class="cardName" style='margin-top: 10px;font-size: 16px;' ><yatra:languageTag content="" language="${language}"/></p>
			<div class="net-banking-desk">
			<article class="cpmt_net cpmt_lastInfo pd-tp-0">
				<ul class="noListStyle">
				
					<c:forEach var="bankBean" items="${paymentBean.preferredBanks}">
						<c:if test="${(bankBean.status == 'ENABLED')}">
							<li class="mb20">
								<label for='${bankBean.code}'>
									<span class="custom-Radiobox">
										<input type="radio" id="${bankBean.code}" iframeEnabled="${bankBean.iframeEnabled}" name="prBank" value='${bankBean.code}' class="cpmt_alignMid radioMid">
										<span class="box"><b class="tick"></b></span>
									</span>
									<label class="${bankBean.logoURL}" for="${bankBean.code}"></label>
								</label>
							</li>
						</c:if>
					</c:forEach>
					
				</ul>
			</article>
			<div class="wfull">
				<div class="cpmt_OtherBnkBlock">
						<label class="cpmt_CClabel" for="nprBank"><yatra:languageTag content="Other banks" language="${language}"/></label>
						<label class="custom-select">
						<select id="nprBank" name="nprBank" class="template">
							<option selected="selected" value=""><yatra:languageTag content="Select Your Bank" language="${language}"/></option>
							<c:forEach var="bankBean" items="${paymentBean.banks}">
								<c:if test="${(bankBean.preferred == 'false') and (bankBean.status == 'ENABLED')}">
									<option value='${bankBean.code}' iframeEnabled="${bankBean.iframeEnabled}"><yatra:languageTag content="${bankBean.displayText}" language="${language}"/></option>
								</c:if>
							</c:forEach>
						</select>
						</label>
					</div>
			</div>
			</div>
			<div class="wfull net-banking-wap">
			<label class="custom-select">
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
			</label>
			</div>
		</c:if>
	</c:forEach>
	</div>
</div>
<!--net Banking ends here-->