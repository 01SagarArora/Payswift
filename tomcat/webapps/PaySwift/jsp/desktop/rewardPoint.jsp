<!--redeem points starts here-->
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>
<%@ taglib uri="/WEB-INF/tld/yatraweb.tld" prefix="y" %>

<y:purl var="img_prefix" key="img_prefix" def="https://secure.yatra.com" />

<div class="content cpmt_payop" id="tab_rewards" style="display:none">
<span class="head-tp">Pay with Reward Point</span>
	<input type="hidden" name="payop" value="rewards"/>
	<input type="hidden" id="rw_ctype_id" name="ctype" value=""/>
	<input type="hidden" id="rewards_isCardInternational_id" name="isCardInternational" value="false"/>
	<input type="hidden" id="new_reward" name="new_reward" value="true"/>
	 
  	<div id='rewards_divErrMsgBlock' class='pmt_errMsgBlock' style='display:none;'> </div>
	<div id='rewards_divWarningMsgBlock' class='pmt_warningMsgBlock' style='display:none;'> </div>
	
	<div class="popover window fade in" id="popoverWindow-rewards" style="height: auto; top: 150px; display: none;">
		<div id="rewards-confirm" class="reward-modal">
		<h3><yatra:languageTag content="Please fill your reward points, or else the complete amount will be charged to your credit card" language="${language}"/>.
		<img src="${img_prefix}/${static_content_url}/PaySwift/desktop/images/error-img.gif"/></h3>
			<div class="btn-area">
				<div class="btns"><input type="button" name="" id="rewards_pay_now" class="rw_ytBtn rw_ytBtnBlue rw_payBtn" value="Pay Now"/>
				<input type="button" name="" id="rewards_enter_points" class="rw_ytBtn rw_ytBtnBlue rw_reddemBtn" value="Enter reward Points" style="margin-left:10px;"/></div>
			</div>
		</div>
	</div>
	
	<c:forEach var="paymentBean" items="${paymentDisplayBean.paymentOptions}">
		<c:if test="${paymentBean.code == 'rewards'}">	
				<c:forEach var="messageBean" items="${paymentBean.messages}">
					<c:if test="${(messageBean.messageType == 'WARNING') and (messageBean.status == 'ENABLED')}">
						<div class="wflL pmt_warningMsgBlock">
							<span><font color="#FF0000">${messageBean.messageText}</font></span>
						</div>
					</c:if>
				</c:forEach>
					
				<article class="cpmt_redeemBank">
					<i class="PaymentSprite ${paymentBean.bankLogoURL}"></i>
					<span class="cpmt_redeemTxt"><yatra:languageTag content="Enter your Citibank credit / debit card details to redeem your reward points" language="${language}"/></span>
				</article>
				
				<ul class="noListStyle cpmt_listItem">
					
					<li>
						<label class="cpmt_cardLabel" for="rw_cno_id"><yatra:languageTag content="${paymentBean.cardNoLabel}" language="${language}"/></label>
						<div id="error-rwCnoRequired" class="pmt_defErrorlist" style='display:none;'><yatra:languageTag content="Please enter card details first" language="${language}"/>.</div>
						<div id="error-rwCnoValid" class="pmt_defErrorlist" style='display:none;'><yatra:languageTag content="Please Enter valid reward card number" language="${language}"/></div>
						<div class="card-wrapr">
						<input type="text"  class="cardNumber w300 ie6Input" id="rw_cno_id" name="cno" data-validation="required|validateLuhn|cardRegType[VISA]|supportedCard[VISA,rewards]|enabledCard[VISA,VISA]" data-msginfo="* required|Enter valid card number|Enter valid card number|Enter a credit card|Card Type is temporarily not supported" processCardNo="true" maxlength=${paymentBean.cardTypes[0].cardDetail.cardNoLength} autocomplete="off"/>
						
						<script language="javascript">
							var rw_enabledCards = "";
						</script>
						<span cardAttr="pm_cards" class="card-logos"> 
							<c:forEach var="cardTypeBean" items="${paymentBean.cardTypes}">
								<c:if test="${cardTypeBean.status == 'ENABLED'}">
									<i class="PaymentSprite ${cardTypeBean.logoURL}" title=${cardTypeBean.code}></i>	
									<script language="javascript">
										rw_enabledCards = rw_enabledCards + "/" + "${cardTypeBean.code}";
									</script>	
								</c:if>
							</c:forEach>
							<script language="javascript">
								rw_enabledCards = rw_enabledCards.substring(1);
							</script>
						</span>
						</div>
					</li>
					<li>
						<label class="cpmt_cardNameLabel" for="rw_cardholder_name_id"><yatra:languageTag content="Cardholder's Name" language="${language}"/><span class="txtDefault">(<yatra:languageTag content="Please enter the same name which is written on your card" language="${language}"/>)</span></label>
						<input type="text" class="w300 ie6Input" id="rw_cardholder_name_id" name="cardholder_name" data-validation="required|regType[alpha]" data-msginfo="* required|* alphabets only" autocomplete="off">
					</li>
					
					<li>
						<div class="blockEx cpmt_expDate-dd">
							<input type="hidden" id="rw_expMonth_id" name="cexpm" value=""/>
							<label class="cpmt_CClabel pb5" for="rw_expMonth_id"><yatra:languageTag content="${paymentBean.cardTypes[0].cardDetail.expiryDateLabel}" language="${language}"/></label>
							<label class="custom-select m_w90">
							<select id="rw_expm_id" paramId="rw_expMonth_id" class="template" data-validation="required" data-msginfo="* required" maxlength="2">
								<%@ include file="monthList.jsp" %>
							</select>
							</label>
						</div>
						
						<div class="blockEx cpmt_expDate-dd">
							<input type="hidden" id="rw_expYear_id" name="cexpy" value=""/>
							<label class="cpmt_CClabel pb5">&nbsp;</label>
							<div class="ml10">
							<label class="custom-select m_w90">
								<select id="rw_expy_id" paramId="rw_expYear_id" data-validation="required|checkExpiryDate[rw]" data-msginfo="* required|Invalid Date" class="ml10 template" maxlength="4">
									<option value=""><yatra:languageTag content="Year" language="${language}"/></option>
								</select>
							</label>
							</div>
						</div>
						<div class="blockEx ml15 cpmt_CvvNo">
							<label class="cpmt_cardLabel" for="rw_cvv_id"><yatra:languageTag content="CVV" language="${language}"/><i class="ico_info_paySwift PaymentSprite" style="margin-left:10px;"></i><div class="info-tooltip cvvtoolTip">
										<div class="tooltip-content "><div class="cpmt_cvvBlock">
								<i class="PaymentSprite ${paymentBean.cardTypes[0].cardDetail.cvvImageURL}" id="cc_CvvUrl_id" defaultVal='PaymentSprite ${paymentBean.cardTypes[0].cardDetail.cvvImageURL}'></i>
								<div class="cpmt_CVVtxt" id="cc_cvvImageText_id" defaultVal='${paymentBean.cardTypes[0].cardDetail.cvvImageText}' >${paymentBean.cardTypes[0].cardDetail.cvvImageText}</div>
							</div></div>
									</div></label>
							<input type="password" class="w94 flL ie6Input cvvInput" id="rw_cvv_id" name="ccsc"  maxlength=${paymentBean.cardTypes[0].cardDetail.cvvLength} data-validation="required|regType[number]|lenCheck[3]" data-msginfo="* required|* number only|* invalid cvv length" autocomplete="off"/>
							
						</div>
						<!--div class="cpmt_cvvBlock">
							<i class="PaymentSprite ${paymentBean.cardTypes[0].cardDetail.cvvImageURL}" id="rw_CvvUrl_id" defaultVal='PaymentSprite ${paymentBean.cardTypes[0].cardDetail.cvvImageURL}'></i>
							<span class="cpmt_CVVtxt" id="rw_cvvImageText_id" defaultVal="${paymentBean.cardTypes[0].cardDetail.cvvImageText}">${paymentBean.cardTypes[0].cardDetail.cvvImageText}</span>
						</div-->
					</li>
					
				</ul>
				<div class="wfull mt10">
					<section class="cpmt_redeemBlock">
						<label for="rw_reward_points_id"><yatra:languageTag content="${paymentBean.rewardPointLabel}" language="${language}"/>
						<a data-url="/${static_content_url}/PaySwift/desktop/html/rewards-chart.html" title="Reward points rate" data-togglxe="popover" data-ajax="true" data-trigger="hover" 
						data-contentdynamic="true" data-placement="bottom" data-contentcss="res-rewards" data-width="428" class="PaymentSprite infoI popmodal" id="reward-point-tooltip"></a>
						
						</label>
						<input type="text" id="rw_reward_points_id" name="rewPoint" data-validation="required|regType[number]" data-msginfo="* required|* number only" class="ie6Input"/>
					</section>
					<section class="cpmt_redeemBlock">
						<label for="rw_reward_amount_id"><yatra:languageTag content="Points Value" language="${language}"/> <span class="RupeeSign">Rs.</span></label>
						<input type="text" id="rw_reward_amount_id" name="rewamount" data-validation="checkReward" data-msginfo="Reward amount should be equal/less than total payable amount." class="ie6Input" readonly="readonly"/>
					</section>
					<section class="cpmt_redeemPointLeftMessage">
						<yatra:languageTag content="Residual amount would be charged to your credit card" language="${language}"/>
					</section>
				</div>
				<p class="cpmt_redeemPointTxt">
					<yatra:languageTag content="To check your Reward Points balance with the Citibank" language="${language}"/><br/>
<yatra:languageTag content='SMS "Rewards XXXX" to 52484 from your registered mobile number' language="${language}"/> (<yatra:languageTag content="XXXX is last 4 digits of your card" language="${language}"/>)
				</p>
		</c:if>
	</c:forEach>
</div>
<script>
	var rewardCardsBinList = "${rewardCardsBinList}";
</script>
<!--redeem points ends here-->