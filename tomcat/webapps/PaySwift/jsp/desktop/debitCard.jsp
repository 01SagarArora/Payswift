
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>
<div class="content cpmt_payop" id="tab_dc" style="display:none;">
	
	<div id="tab_dc_qb">		
		<input type="hidden" name="payop" value="dc"/>
		<input type="hidden" id="dc_ctype_id" name="ctype" value=""/>
		<input type="hidden" id="dc_isCardInternational_id" name="isCardInternational" value="false"/>
		<input type="hidden" id="dc_saveQuickBookCard_id" name="saveQBCard" value="false"/>
		<input type="hidden" id="dc_cvv" name="ccsc" value=""/>
		<input type="hidden" id="dc_qbEnabled" value="false"/>
	
		<div id='dc_divErrMsgBlock' class='pmt_errMsgBlock' style='display:none;'></div>
		<div id='dc_divWarningMsgBlock' class='pmt_warningMsgBlock' style='display:none;'></div>
		
		<div class="cpmt_quickBook" id="tab_dcQB" style="display:block;">
		</div>			
	</div>
	
	<!--  <label id="qb_newDebitCard" for="dc_newCard_id" class="cpmt_newCardOPT" style="display:none">
		<span class="custom-Radiobox">
			<input type="radio" class="cpmt_alignMid" id="dc_newCard_id" name="dc_quickOrCard" checked="false">
			<span class="box"><b class="tick"></b></span>
		</span><yatra:languageTag content="Use new Debit Card" language="${language}"/>
	</label>-->
		
	<article id="dc_newCard" class="flL">
		<c:forEach var="paymentBean" items="${paymentDisplayBean.paymentOptions}">
			<c:if test="${paymentBean.code == 'dc'}">
				<c:if test="${paymentBean.nonMoto}">
					<input type="hidden" id="txntype" name="txntype" value="nm"/>
					<div class="cpmt_itzTxt">
							<yatra:languageTag content="Please click on Pay Now to continue. You will be required to enter card details on next page." language="${language}"/>
					</div>
				</c:if>
				<c:if test="${!paymentBean.nonMoto }">
				<c:if test="${not empty payopMessage.dc}">
				<c:forEach var="messageObj" items="${payopMessage.dc}">
					<div class="pmt_msg_${messageObj.messageType}">
	         			<span><yatra:languageTag content="${messageObj.messageText}" language="${language}"/></span>
	      			</div>
	      		</c:forEach>
	      	</c:if>
				<p class="heading-p head-tp">Pay with Debit Card</p>
				<p class="save_card" style="display:none">Enter New Card</p>		
				<ul class="noListStyle cpmt_listItem">
					
					<li>
						<div style="clear: both">
						<div class="card_num">
							<label class="cpmt_cardLabel" for="dc_cno_id"><yatra:languageTag content="${paymentBean.cardNoLabel}" language="${language}"/></label>
							<div class="card-wrapr card-wrap-num">
							<input type="text" class="cardNumber w240 ie6Input" id="dc_cno_id" name="cno" data-validation="required|validateLuhn|cardRegType[VISA]|supportedCard[VISA,dc]|enabledCard[VISA,VISA]" data-msginfo="* required|Enter valid card number|Enter valid card number|Enter a debit card|Card Type is temporarily not supported" processCardNo="true" maxlength=19 class="w120 pmt_inputTxtBox" autocomplete="off"/>						
						
							<script language="javascript">
								var dc_enabledCards = "";
							</script>
							<span cardAttr="pm_cards" class="card-logos"> 
							<i class="PaymentSprite dummy"></i>
								<c:forEach var="cardTypeBean" items="${paymentBean.cardTypes}">
									<c:if test="${cardTypeBean.status == 'ENABLED'}">
										<i class="PaymentSprite ${cardTypeBean.logoURL}" title=${cardTypeBean.code}></i>	
										<script language="javascript">
											dc_enabledCards = dc_enabledCards + "/" + "${cardTypeBean.code}";
										</script>	
									</c:if>
								</c:forEach>
							</span>
							<script language="javascript">
								dc_enabledCards = dc_enabledCards.substring(1);
							</script>
							<p class="cpmt_debitTxt" id="dc_cvvMsg_id" style="display:none"><yatra:languageTag content="Expiry date and CVV are optional if not present on your card" language="${language}"/>. </p>
							</div>
						</div>
						<div class="card_name">
						<label class="cpmt_cardNameLabel" for="dc_cardholder_name_id"><yatra:languageTag content="Name On Card" language="${language}"/><!--span class="txtDefault"> (<yatra:languageTag content="Please enter the same name which is written on your card" language="${language}"/>)</span--></label>
						<input type="text" class="w188 ie6Input" id="dc_cardholder_name_id" name="cardholder_name" data-validation="required|regType[alpha]" data-msginfo="* required|* alphabets only" autocomplete="off">
						</div>
						</div>
						</li>
						<li>
								
							<div class="blockEx cpmt_expDate-dd blocksize">
								<input type="hidden" id="dc_expMonth_id" name="cexpm" value=""/>
								<label class="cpmt_CClabel pb5" for="dc_expMonth_id"><yatra:languageTag content="Expiry Date" language="${language}"/></label>
								<label class="custom-select m_w100">
								<select id="dc_expm_id" paramId="dc_expMonth_id" data-validation="required" data-msginfo="* required" class="template" maxlength="2">
									<%@ include file="monthList.jsp" %>
								</select>
								</label>
							</div>
							
							<div class="blockEx cpmt_expDate-dd blocksize">
								<input type="hidden" id="dc_expYear_id" name="cexpy" value=""/>
								<label class="cpmt_CClabel pb5">&nbsp;</label>
								<div class="expdate_ml">
								<label class="custom-select m_w100">
									<select id="dc_expy_id" paramId="dc_expYear_id" data-validation="required|checkExpiryDate[cc]" data-msginfo="* required|Invalid Date" class="ml10 template" maxlength="4">
										<option value=""><yatra:languageTag content="Year" language="${language}"/></option>
									</select>
								</label>	
								</div>
							</div>
							<div class="blockEx ml40 cpmt_CvvNo blocksize">
								<label class="cpmt_cardLabel align-tp" for="dc_cvv_id"><yatra:languageTag content="${paymentBean.cardTypes[0].cardDetail.cvvLabel}" language="${language}"/><i class="ico_info_paySwift PaymentSprite" style="margin-left:10px;"></i><div class="info-tooltip cvvtoolTip">
										<div class="tooltip-content "><div class="cpmt_cvvBlock">
								<i class="PaymentSprite ${paymentBean.cardTypes[0].cardDetail.cvvImageURL}" id="cc_CvvUrl_id" defaultVal='PaymentSprite ${paymentBean.cardTypes[0].cardDetail.cvvImageURL}'></i>
								<div class="cpmt_CVVtxt" id="cc_cvvImageText_id" defaultVal='${paymentBean.cardTypes[0].cardDetail.cvvImageText}' >${paymentBean.cardTypes[0].cardDetail.cvvImageText}</div>
							</div></div>
									</div></label>
								<input type="password" class="w100 cvvInput flL ie6Input" id="dc_cvv_id" maxlength=${paymentBean.cardTypes[0].cardDetail.cvvLength} data-validation="required|regType[number]|lenCheck[3]" data-msginfo="* required|* number only|* invalid cvv length" autocomplete="off"/>
							</div>
							<!--div class="cpmt_cvvBlock">
								<i class="PaymentSprite ${paymentBean.cardTypes[0].cardDetail.cvvImageURL}" id="dc_CvvUrl_id" defaultVal='PaymentSprite ${paymentBean.cardTypes[0].cardDetail.cvvImageURL}'></i>
								<span class="cpmt_CVVtxt" id="dc_cvvImageText_id" defaultVal="${paymentBean.cardTypes[0].cardDetail.cvvImageText}">${paymentBean.cardTypes[0].cardDetail.cvvImageText}</span>
							</div-->
						</li>
				</ul>
				
				<div id="dc_process-pay-type" class="flL dc_multipay">
                						<%-- 	<jsp:include page="../common/multi-pay-option.jsp" /> --%>
                						<h3>Complete your payment using</h3>
                						<div class="mt5 wfull" id='dc_process_pay_type_div'></div>
                </div>

				
				<div class="cpmt_IntCardDetails" id="dc_pmt_addressId" style="display:none;">
					<label for="dc_ba1" class="block mb5"><yatra:languageTag content="${paymentBean.billingAddress.billingAddressLabel}" language="${language}"/></label>
					<input type="text" id="dc_ba1" name="ba1" class="ie6Input flL" data-validation="required" data-msginfo="* required" 
					placeholder="<yatra:languageTag content="${paymentBean.billingAddress.billingAddress1}" language="${language}"/>">
					<div class="cpmt_billing">
						<input type="text" id="dc_bcity" name="bacy" placeholder="<yatra:languageTag content="${paymentBean.billingAddress.billingCity}" language="${language}"/>"  data-validation="required|regType[onlyLetter]" data-msginfo="* required|* alphabets only" class="w204 flL ie6Input" >
						<input type="text" id="dc_bstate" name="bast" placeholder="<yatra:languageTag content="${paymentBean.billingAddress.billingState}" language="${language}"/>"  data-validation="required|regType[onlyLetter]" data-msginfo="* required|* alphabets only" class="w204 flL mb10 ie6Input" >
						<input type="text" id="dc_bpin" name="bapi" placeholder="<yatra:languageTag content="${paymentBean.billingAddress.billingPin}" language="${language}"/>" data-validation="required" data-msginfo="* required" maxlength="10" class="w204 flL ie6Input">
						<input type="hidden" id="dc_bcountry_id" name="bacu" value=""/>
						<input type="hidden" id="dc_bcountry_name" name="bcountry" value=""/>
					 	<label class="custom-select mt10">
					 	<select id="dc_bcountry" style="width:206px" paramId="dc_bcountry_id" data-validation="required" data-msginfo="* required" class="w204 template">
	                  		<%@ include file="countryList.jsp" %>
	                 	</select> 
	                 	</label>
	                 	<div class="wfull mt10">
	                 		<input type="text"  id="dc_baisd" name="baisd" placeholder="<yatra:languageTag content="${paymentBean.billingAddress.billingIsdCode}" language="${language}"/>" style="width:60px"
	                 		data-validation="required|regType[number]" data-msginfo="* required|* numbers only" maxlength="6" class="flL ie6Input">
							<input type="text" id="dc_bamob" name="bamob" placeholder="<yatra:languageTag content="${paymentBean.billingAddress.billingMobile}" language="${language}"/>" 
							 data-validation="required|regType[number]|internationalMobileRangeCheck[dc]" data-msginfo="* required|* Please enter a valid number|* Please enter a valid number"
							 maxlength="17"  class="w204 flL mb10 ie6Input" style="width:180px;">
							<span class="cpmt_sampleNo"><b>Eg: 91-9876543210</b></span>
						</div>
					</div>
				</div>
				<c:if test="${showSaveCardCorpcheckBox}">
				<div class="cpmt_quickBStrip" id="dc_SaveOptionDiv">
					<label for="dc_saveQBCard">
						<span class="custom-checkbox">
							<input type="checkbox" name="saveQB" value="" id="dc_saveQBCard" class="mt3 flL" />
							<span class="box"><b class="tick"></b></span>
						</span>

					<yatra:languageTag content="${saveCardMsg}" language="${language}"/> <i class="PaymentSprite logo_quickBook" style="display: none;"></i></label>
				</div>
				</c:if>
			</c:if>
			</c:if>
		</c:forEach>	
	</article>
</div>