<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>
<div class="content cpmt_payop" id="tab_cc" style="display:none;">
	
				
	<div id="tab_cc_qb">			
		<input type="hidden" name="payop" value="cc"/>
		<input type="hidden" id="cc_ctype_id" name="ctype" value=""/>
		<input type="hidden" id="cc_isCardInternational_id" name="isCardInternational" value="false"/>
		<input type="hidden" id="cc_saveQuickBookCard_id" name="saveQBCard" value="false"/>
		<input type="hidden" id="cc_cvv" name="ccsc" value=""/>
		<input type="hidden" id="cc_qbEnabled" value="false"/>
		
		<div id='cc_divErrMsgBlock' class='pmt_errMsgBlock' style='display:none;'></div>
		<div id='cc_divErrMsgBlock_hotelGDS' class='pmt_errMsgBlock' style='display:none;'></div>
		<div id='cc_divWarningMsgBlock' class='pmt_warningMsgBlock' style='display:none;'></div>
		
		<div class="cpmt_quickBook" id="tab_ccQB" style="display:block;">
		</div>			
	</div>
	
	<!-- <label id="qb_newCreditCard" for="cc_newCard_id" class="cpmt_newCardOPT" style="display:none;">
		<span class="custom-Radiobox" style="margin-top:2px;">
			<input type="radio" class="cpmt_alignMid" id="cc_newCard_id" name="cc_quickOrCard" checked="false">
			<span class="box"><b class="tick"></b></span>
		</span><yatra:languageTag content="Use new Credit Card" language="${language}"/>
	</label>-->

	<article id="cc_newCard" class="flL">
		<c:forEach var="paymentBean" items="${paymentDisplayBean.paymentOptions}">
			<c:if test="${paymentBean.code == 'cc'}">
				<c:if test="${paymentBean.nonMoto}">
					<input type="hidden" id="txntype" name="txntype" value="nm"/>
					<div class="cpmt_itzTxt">
							<yatra:languageTag content="Please click on Pay Now to continue. You will be required to enter card details on next page." language="${language}"/>
					</div>
				</c:if>	
				<c:if test="${!paymentBean.nonMoto }">
				<c:if test="${not empty payopMessage.cc}">
				<c:forEach var="messageObj" items="${payopMessage.cc}">
					<div class="pmt_msg_${messageObj.messageType}">
	         			<span><yatra:languageTag content="${messageObj.messageText}" language="${language}"/></span>
	      			</div>
	      		</c:forEach>
	      	</c:if>
			<p class="heading-p head-tp">Pay with Credit Card</p>
			<p class="save_card" style="display:none">Enter New Card</p>
					<ul class="noListStyle cpmt_listItem">
					
						
						<li>
						<div style="clear: both">
						<div class="card_num">
							<label class="cpmt_cardLabel" for="cc_cno_id"><yatra:languageTag content="${paymentBean.cardNoLabel}" language="${language}"/></label>
							<div class="card-wrapr card-wrap-num">
							<input type="text" value="" class="cardNumber w240 ie6Input" id="cc_cno_id" name="cno" data-validation="required|validateLuhn|cardRegType[VISA]|supportedCard[VISA,cc]|enabledCard[VISA,VISA]" data-msginfo="* required|Enter valid card number|Enter valid card number|Enter a credit card|Card Type is temporarily not supported" processCardNo="true" maxlength="${paymentBean.cardTypes[0].cardDetail.cardNoLength}" autocomplete="off">							
							
							<script language="javascript">
								var cc_enabledCards = "";
								var amexCvvURL;
								var amexCvvLength;
								var amexCvvImageText;
							</script>
							<span cardAttr="pm_cards" class="card-logos"> 
							<i class="PaymentSprite dummy"></i>
								<c:forEach var="cardTypeBean" items="${paymentBean.cardTypes}">
									<c:if test="${cardTypeBean.status == 'ENABLED'}">
										<i class="PaymentSprite ${cardTypeBean.logoURL}" title=${cardTypeBean.code}></i>	
										<script language="javascript">
											cc_enabledCards = cc_enabledCards + "/" + "${cardTypeBean.code}";
										</script>	
										<c:if test="${cardTypeBean.code == 'AMEX'}">
											<script language="javascript">
											 	amexCvvURL = "${cardTypeBean.cardDetail.cvvImageURL}";
											 	amexCvvLength = "${cardTypeBean.cardDetail.cvvLength}";
											 	amexCvvImageText = "${cardTypeBean.cardDetail.cvvImageText}";
											</script>	
										</c:if>		
									</c:if>
								</c:forEach>
								<script language="javascript">
									cc_enabledCards = cc_enabledCards.substring(1);
								</script>
							</span>
							</div>
						</div>
						
						<div class="card_name">
							<label class="cpmt_cardNameLabel" for="cc_cardholder_name_id"><yatra:languageTag content="Name On Card" language="${language}"/><!--span class="txtDefault"> (<yatra:languageTag content="Please enter the same name which is written on your card" language="${language}"/>)</span--></label>
							<input type="text" class="w188 ie6Input" id="cc_cardholder_name_id" name="cardholder_name" data-validation="required|regType[alpha]" data-msginfo="* required|* alphabets only" autocomplete="off">
						</div>
						</div>
						</li>
						
						<li>
							<div class="blockEx cpmt_expDate-dd blocksize">
								<input type="hidden" id="cc_expMonth_id" name="cexpm" value=""/>
								<label class="cpmt_CClabel pb5" for="cc_expMonth_id"><yatra:languageTag content="Expiry Date" language="${language}"/></label>
								<label class="custom-select m_w100">
								<select id="cc_expm_id" paramId="cc_expMonth_id" data-validation="required" data-msginfo="* required" class="template" maxlength="2">
									<%@ include file="monthList.jsp" %>
								</select> 
								</label>
							</div>
							
							<div class="blockEx cpmt_expDate-dd blocksize">
								<input type="hidden" id="cc_expYear_id" name="cexpy" value=""/>
								<label class="cpmt_CClabel pb5">&nbsp;</label>
								<div class="expdate_ml">
								<label class="custom-select m_w100">
									<select id="cc_expy_id" paramId="cc_expYear_id" data-validation="required|checkExpiryDate[cc]" data-msginfo="* required|Invalid Date" class="ml10 template" maxlength="4">
										<option value="">Year</option>
									</select>
								</label>
								</div>
							</div>
							<div class="blockEx ml40 cpmt_CvvNo blocksize">
								<label class="cpmt_cardLabel align-tp" for="cc_cvv_id"><yatra:languageTag content="${paymentBean.cardTypes[0].cardDetail.cvvLabel}" language="${language}"/><i class="ico_info_paySwift PaymentSprite" style="margin-left:10px;"></i><div class="info-tooltip cvvtoolTip">
										<div class="tooltip-content "><div class="cpmt_cvvBlock">
								<i class="PaymentSprite ${paymentBean.cardTypes[0].cardDetail.cvvImageURL}" id="cc_CvvUrl_id" defaultVal='PaymentSprite ${paymentBean.cardTypes[0].cardDetail.cvvImageURL}'></i>
								<div class="cpmt_CVVtxt" id="cc_cvvImageText_id" defaultVal='${paymentBean.cardTypes[0].cardDetail.cvvImageText}' >${paymentBean.cardTypes[0].cardDetail.cvvImageText}</div>
							</div></div>
									</div></label>
								<input type="password" class="w100 cvvInput flL ie6Input" id="cc_cvv_id" maxlength=${paymentBean.cardTypes[0].cardDetail.cvvLength} data-validation="required|regType[number]|lenCheck[3]" data-msginfo="* required|* number only|* invalid cvv length" autocomplete="off"/>

							</div>
							<!--div class="cpmt_cvvBlock">
								<i class="PaymentSprite ${paymentBean.cardTypes[0].cardDetail.cvvImageURL}" id="cc_CvvUrl_id" defaultVal='PaymentSprite ${paymentBean.cardTypes[0].cardDetail.cvvImageURL}'></i>
								<span class="cpmt_CVVtxt" id="cc_cvvImageText_id" defaultVal='${paymentBean.cardTypes[0].cardDetail.cvvImageText}' >${paymentBean.cardTypes[0].cardDetail.cvvImageText}</span>
							</div-->
						</li>
					</ul>
					
					<div id="cc_AddressShowHide" class="flL" style="display:none;">
						<article class="cpmt_amexCreditC" >
							<p class="block"><yatra:languageTag content="Where was your Amex card issued" language="${language}"/>?</p>
							 <div class="mt10">
								<div class="cpmt_countryBox">
									<label for='domestic_address_rdo'>
										<span class="custom-Radiobox">
											<input type="radio" checked="checked" class="cpmt_alignMid radioMid" value="Domestic" id="domestic_address_rdo" name="address_check_rdo">
											<span class="box"><b class="tick"></b></span>
										</span><yatra:languageTag content="India" language="${language}"/>
									</label>
								</div>
								<div class="cpmt_countryBox cpmt_international">
									<label for='international_address_rdo'>
										<span class="custom-Radiobox">
											<input type="radio" class="cpmt_alignMid radioMid" value="International" id="international_address_rdo" name="address_check_rdo">
											<span class="box"><b class="tick"></b></span>
										</span><yatra:languageTag content="Outside India" language="${language}"/>
									</label>
								</div>
							</div> 
						</article>
					</div>
					

								<div id="cc_process-pay-type" class="flL">
                	<%--   <jsp:include page="../common/multi-pay-option.jsp" /> --%>
                					<h3>Complete your payment using</h3>
                					<div class="mt5 wfull" id='cc_process_pay_type_div'></div>

                				</div>

					<div class="cpmt_IntCardDetails" id="cc_pmt_addressId" style="display:none;">
						<label for="cc_ba1" class="block mb5"><yatra:languageTag content="${paymentBean.billingAddress.billingAddressLabel}" language="${language}"/></label>
						<input type="text" id="cc_ba1" name="ba1" class="ie6Input flL w300" data-validation="required" data-msginfo="* required" placeholder='<yatra:languageTag content="${paymentBean.billingAddress.billingAddress1}" language="${language}"/>'>
						<div class="cpmt_billing">
							<input type="text" id="cc_bcity" name="bacy" placeholder='<yatra:languageTag content="${paymentBean.billingAddress.billingCity}" language="${language}"/>' data-validation="required|regType[onlyLetter]" data-msginfo="* required|* alphabets only" class="w145 flL ie6Input" >
							<input type="text" id="cc_bstate" name="bast" placeholder='<yatra:languageTag content="${paymentBean.billingAddress.billingState}" language="${language}"/>' data-validation="required|regType[onlyLetter]" data-msginfo="* required|* alphabets only" class="w145 flL ie6Input" >
							<input type="text" id="cc_bpin" name="bapi" placeholder='<yatra:languageTag content="${paymentBean.billingAddress.billingPin}" language="${language}"/>' data-validation="required" data-msginfo="* required" maxlength="10" class="w145 flL ie6Input" style="clear: left;">
							<input type="hidden" id="cc_bcountry_id" name="bacu" value=""/>
							<input type="hidden" id="cc_bcountry_name" name="bcountry" value=""/>
							<label class="custom-select mt10" style="float: left;">
						 	<select id="cc_bcountry" style="width:145px" paramId="cc_bcountry_id" data-validation="required" data-msginfo="* required" class="w204 template">
		                  		<%@ include file="countryList.jsp" %>
		                 	</select> 
		                 	</label>
		                 	<div class="wfull mt10">
		                 		<input type="text" id="cc_baisd" name="baisd" placeholder='<yatra:languageTag content="${paymentBean.billingAddress.billingIsdCode}" language="${language}"/>' style="width:60px"
		                 		data-validation="required|regType[number]" data-msginfo="* required|* numbers only" maxlength="6" class="flL ie6Input">
								<input type="text" id="cc_bamob" name="bamob" placeholder='<yatra:languageTag content="${paymentBean.billingAddress.billingMobile}" language="${language}"/>' 
								data-validation="required|regType[number]|internationalMobileRangeCheck[cc]" maxlength="17"  class="flL mb10 ie6Input" style="width:125px;"
								data-msginfo="* required|* Please enter a valid number|* Please enter a valid number">
								<span class="cpmt_sampleNo"><b style="font-size: 11px;margin-top: 11px;float: left;">Eg: 91-9876543210</b></span>
							</div>							
						</div>
					</div>
					<c:if test="${showSaveCardCorpcheckBox}">
					<div class="cpmt_quickBStrip" id="cc_SaveOptionDiv">
						<label for="cc_saveQBCard">
							<span class="custom-checkbox" >
								<input type="checkbox" name="saveQB" value="" id="cc_saveQBCard" class="mt3 flL" >
								<span class="box"><b class="tick"></b></span>
							</span>
						<yatra:languageTag content="${saveCardMsg}" language="${language}"/></label>
					</div>
					</c:if>
					<div id="cc_pmt_addressId_template" class="template" style ="display:none">
						<label for="temp_ba1" class="block mb5"><yatra:languageTag content="${paymentBean.billingAddress.billingAddressLabel}" language="${language}"/></label>
						<input type="text" id="temp_ba1" name="ba1" class="ie6Input flL w300" data-validation="required" data-msginfo="* required" placeholder='<yatra:languageTag content="${paymentBean.billingAddress.billingAddress1}" language="${language}"/>'>
						<div class="cpmt_billing flL mt10">
							<input type="text" id="temp_bcity" name="bacy" placeholder='<yatra:languageTag content="${paymentBean.billingAddress.billingCity}" language="${language}"/>' data-validation="required|regType[onlyLetter]" data-msginfo="* required|* alphabets only" class="w145 flL ie6Input" >
							<input type="text" id="temp_bstate" name="bast" placeholder='<yatra:languageTag content="${paymentBean.billingAddress.billingState}" language="${language}"/>' data-validation="required|regType[onlyLetter]" data-msginfo="* required|* alphabets only" class="w145 flL mb10 ie6Input" >
							<input type="text" id="temp_bpin" name="bapi" placeholder='<yatra:languageTag content="${paymentBean.billingAddress.billingPin}" language="${language}"/>' data-validation="required" data-msginfo="* required" maxlength="10" class="w145 flL ie6Input" style="clear:left">
							<input type="hidden" id="temp_bcountry_id" name="bacu" value="" style="clear:left" />
							<div class="custom-select" style="width: 145px;float: left;margin-top: 3px;">
						 	<select id="temp_bcountry" paramId="temp_bcountry_id" data-validation="required" data-msginfo="* required" class="w204 template">
		                  		<%@ include file="countryList.jsp" %>
		                 	</select> 
		                 	</div>
		                 	<div class="wfull mt10">
		                 		<input type="text" id="temp_baisd" name="baisd" placeholder='<yatra:languageTag content="${paymentBean.billingAddress.billingIsdCode}" language="${language}"/>' style="width:60px"
		                 		data-validation="required|regType[number]" data-msginfo="* required|* numbers only" maxlength="6" class="flL ie6Input">
								<input type="text" id="temp_bamob" name="bamob" placeholder='<yatra:languageTag content="${paymentBean.billingAddress.billingMobile}" language="${language}"/>' 
								data-validation="required|regType[number]|internationalMobileRangeCheck[qbcc]" maxlength="17"  class="flL mb10 ie6Input" style="width:125px;"
								data-msginfo="* required|* Please enter a valid number|* Please enter a valid number">
								<span class="cpmt_sampleNo"><b style="font-size: 11px;margin-top: 25px;float: left;">Eg: 91-9876543210</b></span>
							</div>							
						</div>
					</div>
					
			</c:if>
			</c:if>
		</c:forEach>
	</article>
</div>