<!--mobile wallet starts here-->
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>
<div class="content cpmt_payop" id="tab_mw" style="display:none;">
	<span class="head-tp">Pay with Mobile Wallet</span>
<!--pay later partial payment starts here-->
	<input type="hidden" name="payop" value="mw"/>
	<input type="hidden" name="bankCode" value=""/>
	<div id='nb_pmt_msg_WARNING' class='pmt_msg_WARNING' style='display:none;'></div>
	<c:forEach var="paymentBean" items="${paymentDisplayBean.paymentOptions}">
		<c:if test="${paymentBean.code == 'mw'}">
		<c:if test="${not empty payopMessage.mw && not empty paymentBean.suboptionList}">
				<c:forEach var="messageObj" items="${payopMessage.mw}">
					<div class="pmt_msg_${messageObj.messageType} mw_payop_msg">
	         			<span><yatra:languageTag content="${messageObj.messageText}" language="${language}"/></span>
	      			</div>
	      		</c:forEach>
	      	</c:if>
	      	<div id='mw_bank_msg' class='pmt_msg_WARNING' style='display:none;'></div>
	<!-- new design starts--> 
	<article class="cpmt_net cpmt_lastInfo pd-tp-0">
		<ul class="noListStyle">
					<c:forEach var="suboptionList" items="${paymentBean.suboptionList}">
							<c:if test="${suboptionList.status == 'ENABLED'}">
								<li class="mt20"><label for='${suboptionList.subOption}'> <span
									class="custom-Radiobox"> <input type="radio" id="${suboptionList.subOption}"
										name="walletService" value="${suboptionList.subOption}"
										class="cpmt_alignMid radioMid"> <span class="box"><b
											class="tick"></b></span>
									</span> <label class="${suboptionList.imageURL}" for="${suboptionList.subOption}"></label>
								</label></li>
							</c:if>
							</c:forEach>
						<c:if test="${empty paymentBean.suboptionList }">
						<div class="cpmt_itzTxt">
							<yatra:languageTag content="Dear Customer" language="${language}"/>,<br/>
							<yatra:languageTag content="Please click the 'Pay Now' button to make a payment" language="${language}"/>.
							<input type="hidden" id="txntype" name="txntype" value="nm"/>
							<!-- <script>
  								 document.getElementById('warningMessageDiv').style.visibility = 'hidden';
  								document.getElementById('warningMessageDiv').style.display = 'none';
							</script> -->
						</div>
						</c:if>
		</ul>

	</article>
	</c:if>
	</c:forEach>
	<!-- new design ends-->

</div>

<!--mobile wallet ends here-->
