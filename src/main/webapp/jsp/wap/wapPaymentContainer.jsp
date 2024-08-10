<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra"%>
<%@ page isELIgnored="false"%>

<link rel="stylesheet"	href="/${static_content_url}/PaySwift/wap/css/flight-review.css?version=${staticResourceVersion}"	type="text/css" />
<link rel="stylesheet"	href="/${static_content_url}/PaySwift/wap/css/header.css?version=${staticResourceVersion}"	type="text/css" />
<link rel="stylesheet"	href="/${static_content_url}/PaySwift/wap/css/jquery-mobile.css" type="text/css" />
<link rel="stylesheet"	href="/${static_content_url}/PaySwift/wap/css/yt-common.css?version=${staticResourceVersion}"	type="text/css" />
<script type="text/javascript" src="/${static_content_url}/PaySwift/wap/js/jquery.mobile.js"></script>


<link href="/${static_content_url}/PaySwift/wap/css/payment.css?version=${staticResourceVersion}" rel="stylesheet" />

<jsp:include page="errorMessage.jsp"></jsp:include>

<script language="javascript" src="/${static_content_url}/PaySwift/wap/js/wap-quickbookui.js?version=${staticResourceVersion}"></script>
<script language="javascript" src="/${static_content_url}/PaySwift/wap/js/wap-ewalletui.js?version=${staticResourceVersion}"></script>
<script type="text/javascript" src="//connect.facebook.net/en_US/all.js"></script>
<script language="javascript" src="/${static_content_url}/PaySwift/wap/js/wap-facebookAuth.js?version=${staticResourceVersion}"></script>
<script language="javascript" src="/${static_content_url}/PaySwift/wap/js/wap-corporateCardui.js?version=${staticResourceVersion}"></script>

<script type="text/javascript">
	var qbJson = ${qbJson};
	var enableEWallet = false;
	var enableQuickBook = false;
</script>
	<section data-role="content" class="yt-ui-content">
		<section class="yt-pay-main">
			<c:forEach var="paymentBean" items="${paymentDisplayBean.paymentOptions}">
				<c:if test="${(paymentBean.status == 'ENABLED') && (paymentBean.code == 'ew')}">
					<script type="text/javascript">
						enableEWallet = true;
					</script>
					<jsp:include page="wapEWalletContainer.jsp"></jsp:include>
				</c:if>
				
			</c:forEach>
				
			 <section id="cpmt_payment">
				<c:forEach var="paymentBean" items="${paymentDisplayBean.paymentOptions}">
					
					<c:if test="${(paymentBean.status == 'ENABLED') && (paymentBean.code == 'qb')}">
						<script type="text/javascript">
							enableQuickBook = true;
						</script>
						<jsp:include page="wapQuickBook.jsp"></jsp:include>
					</c:if>
				</c:forEach>
				
				<section class="other-cards">
					<ul id="cpmt_tabmenu">
				<li class="section-heading" id="payment-modes-text" style="display:none"><yatra:languageTag content="Payment Mode" language="${language}"/></li>
				<li class="payment_opyions_li" id="payment-modes" style="display:none">
					<div class="detail-holder">
						<div class="other-payment">
							<div class="ui-select pay-mode-div">
								<div
									class="ui-btn ui-icon-carat-d ui-btn-icon-right ui-corner-all ui-shadow">
									<span><yatra:languageTag content="Please select your payment mode" language="${language}"/></span>
								</div>
							</div>
							<ul id="" style="" class="content payment-mode">
								<li id="paynow-mode">
								<input type="radio" id="paynowradio" value="payFull" name="paymentmode" checked="true" />
									<label for="paynowradio"> <span class="label-align lato-bold"><yatra:languageTag content="Pay Now" language="${language}"/></span>
										<p class="cash-txt">
											<yatra:languageTag content="Amount" language="${language}"/> <span class="rs">Rs.</span><span id="payNowAmount"></span> 
										</p>
									</label>
								</li>
								<li id="pp-mode" style="display:none">
								<input type="radio" id="dfg" value="payPartial" name="paymentmode" />
									<label for="dfg"> <span class="label-align lato-bold"><yatra:languageTag content="Partial Payment" language="${language}"/></span>
										<p class="cash-txt">
											<yatra:languageTag content="Pay now" language="${language}"/> :<span class="rs">Rs.</span> <span id="partialPayNowAmount"></span>
											<yatra:languageTag content="Pay later" language="${language}"/> :<span class="rs">Rs.</span> <span id="partialLaterAmount"></span>
										</p>
									</label>
								</li>
								<li id="hnp-mode" style="display:none">
								<input type="radio" id="sdfg" value="holdAndPay" name="paymentmode"/>
									<label for="sdfg"> <span class="label-align lato-bold"><yatra:languageTag content="Pay and Hold" language="${language}"/></span>
										<p class="cash-txt">
											<yatra:languageTag content="Pay" language="${language}"/> <span class="rs">Rs.</span><span id="holdAndPayAmount"></span> <yatra:languageTag content="& reserve your holiday" language="${language}"/>
										</p>
									</label>
								</li>
							</ul>
						</div>
					</div>
				</li>
								<c:if test="${(paymentDisplayBean.productCode == 'mdomsbi') || (paymentDisplayBean.productCode == 'mintsbi') || (paymentDisplayBean.productCode == 'mdomhotelsbi') || (paymentDisplayBean.productCode == 'mdombussbi')}">
						<li class="section-heading" style="visibility: hidden;"><yatra:languageTag content="Pay Using SBI Buddy" language="${language}"/></li>
						<!-- below li will be removed but now is here as chirag needs it -->
						<li paymentOption='other' class="payment_opyions_li">
							<div class="detail-holder" style="visibility: hidden;">
								<div class="other-payment">
								<select name="other_payop" id="other_payop" data-validation="required" data-msginfo='* <yatra:languageTag content="Please select a Payment Option from the dropdown" language="${language}"/>' style="visibility: hidden;">
										<c:forEach var="paymentBean" items="${paymentDisplayBean.paymentOptions}">	
										<%--	<c:if test="${(paymentBean.status == 'ENABLED') && (paymentBean.code != 'qb') && (paymentBean.code != 'ew')}"> --%>
										<c:if test="${(paymentBean.code != 'qb') && (paymentBean.code != 'ew')}">
												<option value="${paymentBean.code}" data-payop="${paymentBean.code}"><yatra:languageTag content="${paymentBean.displayText}" language="${language}"/></option>
										</c:if>
										</c:forEach>
									</select>
									<jsp:include page="wapCreditPool.jsp"></jsp:include>
									<jsp:include page="wapCorporateCard.jsp"></jsp:include>
									<jsp:include page="wapCreditCard.jsp"></jsp:include>
									<jsp:include page="wapDebitCard.jsp"></jsp:include>
									<jsp:include page="wapNetBanking.jsp"></jsp:include>
									<jsp:include page="wapMobileWallet.jsp"></jsp:include>
									<jsp:include page="wapRewardPoint.jsp"></jsp:include>
									<jsp:include page="wapEzeClick.jsp"></jsp:include>
									<jsp:include page="wapEmiOption.jsp"></jsp:include>
									<jsp:include page="wapAtmCard.jsp"></jsp:include>
									<jsp:include page="wapCashCard.jsp"></jsp:include>
									<jsp:include page="wapPayu.jsp"></jsp:include>
									<jsp:include page="wapSBIBuddy.jsp"></jsp:include>
								</div>
						</div></li>
						</c:if>
						<c:if test="${(paymentDisplayBean.productCode != 'mdomsbi') && (paymentDisplayBean.productCode != 'mintsbi') && (paymentDisplayBean.productCode != 'mdomhotelsbi') && (paymentDisplayBean.productCode != 'mdombussbi')}">
						<li class="section-heading"><yatra:languageTag content="Payment Options" language="${language}"/></li>
						<!-- below li will be removed but now is here as chirag needs it -->
						<li paymentOption='other' class="payment_opyions_li">
							<div class="detail-holder">
								<div class="other-payment">
									<select name="other_payop" id="other_payop" data-validation="required" data-msginfo='* <yatra:languageTag content="Please select a Payment Option from the dropdown" language="${language}"/>'>
										<option value=""><yatra:languageTag content="Please select your payment option" language="${language}"/>.</option>
										<c:forEach var="paymentBean" items="${paymentDisplayBean.paymentOptions}">			   
											<c:if test="${(paymentBean.status == 'ENABLED') && (paymentBean.code != 'qb') && (paymentBean.code != 'ew')}">
												<option value="${paymentBean.code}" data-payop="${paymentBean.code}"><yatra:languageTag content="${paymentBean.displayText}" language="${language}"/></option>
											</c:if>	
										</c:forEach>
									</select>

									<jsp:include page="wapCreditPool.jsp"></jsp:include>
									<jsp:include page="wapCorporateCard.jsp"></jsp:include>
									<jsp:include page="wapCreditCard.jsp"></jsp:include>
									<jsp:include page="wapDebitCard.jsp"></jsp:include>
									<jsp:include page="wapNetBanking.jsp"></jsp:include>
									<jsp:include page="wapMobileWallet.jsp"></jsp:include>
									<jsp:include page="wapRewardPoint.jsp"></jsp:include>
									<jsp:include page="wapEzeClick.jsp"></jsp:include>
									<jsp:include page="wapEmiOption.jsp"></jsp:include>
									<jsp:include page="wapAtmCard.jsp"></jsp:include>
									<jsp:include page="wapCashCard.jsp"></jsp:include>
									<jsp:include page="wapPayu.jsp"></jsp:include>
									<jsp:include page="wapSBIBuddy.jsp"></jsp:include>
								</div>
						</div></li>
						</c:if>
					</ul>
				</section>
			</section>
		</section>
	</section>
	<script type="text/javascript" src="/${static_content_url}/PaySwift/wap/js/wap-centralPayment.js?version=${staticResourceVersion}"></script>
	<script type="text/javascript" src="/${static_content_url}/PaySwift/wap/js/wap-centralui.js?version=${staticResourceVersion}"></script>
	<script type="text/javascript" src="/${static_content_url}/PaySwift/wap/js/payment.js?version=${staticResourceVersion}"></script>
	<script type="text/javascript" src="/${static_content_url}/PaySwift/wap/js/wap-centralui-validation.js?version=${staticResourceVersion}"></script>