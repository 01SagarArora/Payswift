<!--redeem points starts here-->
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>
<div class="content cpmt_payop other_option_div" id="tab_rewards" style="display: none">

	<input type="hidden" name="payop" value="rewards" /> 
	<input type="hidden" id="rw_ctype_id" name="ctype" value="" />
	<input type="hidden" id="new_reward" name="new_reward" value="true"/>
	<div id='rw_divErrMsgBlock' class='pmt_errMsgBlock'	style='display: none;'></div>
	<div id='rw_divWarningMsgBlock' class='pmt_warningMsgBlock'	style='display: none;'></div>

	<c:forEach var="paymentBean" items="${paymentDisplayBean.paymentOptions}">
		<c:if test="${paymentBean.code == 'rewards'}">
			<c:forEach var="messageBean" items="${paymentBean.messages}">
				<c:if test="${(messageBean.messageType == 'WARNING') and (messageBean.status == 'ENABLED')}">
					<div class="wflL pmt_warningMsgBlock">
						<span><font color="#FF0000">${messageBean.messageText}</font></span>
					</div>
				</c:if>
			</c:forEach>

			<div class="wfull" style="margin-bottom:0">
				<span class="net-banking citi-net"></span>
			</div>
			<div class="detail-holder">
				<div class="wfull card-number">
					<label class="field-label"><yatra:languageTag content="Card Number" language="${language}"/></label> 
						<div id="error-rwCnoRequired" class="pmt_defErrorlist" style='display:none;'><yatra:languageTag content="Please enter card details first" language="${language}"/>.</div>
						<div id="error-rwCnoValid" class="pmt_defErrorlist" style='display:none;'><yatra:languageTag content="Please Enter valid reward card number" language="${language}"/></div>
						<input type="text" class="cardVal process-card-no" autocomplete="off"  id="rw_cno_id" name="cno" data-payop="rewards" data-validation="required|validateLuhn|cardRegType[VISA]|supportedCard[VISA,rewards]|enabledCard[VISA,VISA]" data-msginfo='* <yatra:languageTag content="required" language="${language}"/>|<yatra:languageTag content="Enter valid card number" language="${language}"/>|<yatra:languageTag content="Enter valid card number" language="${language}"/>|<yatra:languageTag content="Enter a credit card" language="${language}"/>|<yatra:languageTag content="Card Type is temporarily not supported" language="${language}"/>' processCardNo="true" maxlength=${paymentBean.cardTypes[0].cardDetail.cardNoLength} autocomplete="off" pattern="[0-9]*"/>
						<script language="javascript">
							var rw_enabledCards = "";
						</script>
							<c:forEach var="cardTypeBean" items="${paymentBean.cardTypes}">
								<c:if test="${cardTypeBean.status == 'ENABLED'}">
									<i class="payment-icon ${cardTypeBean.logoURL} logo_${cardTypeBean.code} card_logo" title=${cardTypeBean.code}></i>	
									<script language="javascript">
										rw_enabledCards = rw_enabledCards + "/" + "${cardTypeBean.code}";
									</script>	
								</c:if>
							</c:forEach>
							<script language="javascript">
								rw_enabledCards = rw_enabledCards.substring(1);
							</script>
				</div>
				<div class="wfull">
					<label class="field-label"><yatra:languageTag content="Name on Card" language="${language}"/></label> 
					<input  type="text" placeholder='<yatra:languageTag content="Cardholder's Name" language="${language}"/>' id="rw_cardholder_name_id" name="cardholder_name" data-validation="required|regType[alpha]" data-msginfo='* <yatra:languageTag content="required" language="${language}"/>|* <yatra:languageTag content="alphabets only" language="${language}"/>' autocomplete="off"  class="cardVal" /> 
				</div>
				<div class="wfull" style="margin-bottom:0">
					<div class="expiry-date">
						<label class="field-label"><yatra:languageTag content="Expiry Date" language="${language}"/></label>
						<div class="drop-menu ml0">
							<input type="hidden" id="rw_expMonth_id" name="cexpm" value=""/>
							<select id="rw_expm_id" paramId="rw_expMonth_id" data-validation="required" data-msginfo='* <yatra:languageTag content="required" language="${language}"/>' maxlength="2">
								<option value=""><yatra:languageTag content="Month" language="${language}"/></option>
								<jsp:include page="monthList.jsp"></jsp:include>
							</select>
						</div>
						<div class="drop-menu">
						<input type="hidden" id="rw_expYear_id" name="cexpy" value=""/>
						<jsp:useBean id="date" class="java.util.Date" />
						<fmt:formatDate value="${date}" pattern="yyyy" var="currentYear" />
							<select id="rw_expy_id" paramId="rw_expYear_id" data-validation="required|checkExpiryDate[rw]" data-msginfo='* <yatra:languageTag content="required" language="${language}"/>|<yatra:languageTag content="Invalid Date" language="${language}"/>'  maxlength="4">
							<option value=""><yatra:languageTag content="Year" language="${language}"/></option>
							<c:forEach var="i" begin="${currentYear}" end="${currentYear+20}" step="1">
								<option value="${i}">${i}</option>
							</c:forEach>
							</select>
						</div>
					</div>
					<div class="card-cvv">
						<label class="field-label"><yatra:languageTag content="CVV No." language="${language}"/></label> 
						<input type="password" autocomplete="off"  id="rw_cvv_id" name="ccsc"  maxlength=${paymentBean.cardTypes[0].cardDetail.cvvLength} data-validation="required|regType[number]|lenCheck[3]" data-msginfo='* <yatra:languageTag content="required" language="${language}"/>|* <yatra:languageTag content="number only" language="${language}"/>|* <yatra:languageTag content="invalid cvv length" language="${language}"/>' autocomplete="off" pattern="[0-9]*"/>
					</div>
				</div>
			</div>
			<div class="wfull" style="margin-top:0">
				<div class="reards-cont">
					<label for="redm-rewards"><yatra:languageTag content="${paymentBean.rewardPointLabel}" language="${language}"/></label> 
					<input type="text" id="rw_reward_points_id" name="rewPoint" data-validation="required|regType[number]" data-msginfo="* required|* number only" pattern="[0-9]*"/>
				</div>
				<div class="reards-cont">
					<label style="margin-top: 10px;" for="redm-value"><yatra:languageTag content="Points Value" language="${language}"/> <span class="rs">Rs.</span></label>
					<input type="text" id="rw_reward_amount_id" name="rewamount" data-validation="checkReward" data-msginfo='<yatra:languageTag content="Reward amount should be equal/less than total payable amount." language="${language}"/>' readonly="readonly"/>
				</div>
			</div>

		</c:if>
	</c:forEach>
</div>
<script>
	var rewardCardsBinList = "${rewardCardsBinList}";
</script>
<!--redeem points ends here-->