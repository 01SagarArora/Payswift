<!--emi tab starts here-->
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>
<div class="content cpmt_payop" id="tab_emi" style="display:none;">
	<span class="head-tp">Pay with EMI</span>
	<input type="hidden" name="payop" value="emi"/>
	 <input type="hidden" id="emi_ctype_id" name="ctype" value=""/>
	 <input type="hidden" name="isEmiPayment" value="true"/>
	 <input type="hidden" id="emi_isCardInternational_id" name="isCardInternational" value="false"/>
	 <input type="hidden" id="emi_emiBank_id" name="emiBank" value=""/>
	 <input type="hidden" id="emi_emiTenure_id" name="emiTenure" value=""/>

	 <c:forEach var="paymentBean" items="${paymentDisplayBean.paymentOptions}">
		<c:if test="${paymentBean.code == 'emi'}">
			<c:if test="${not empty payopMessage.emi}">
				<c:forEach var="messageObj" items="${payopMessage.emi}">
					<div id="emi_msg" class="pmt_msg_${messageObj.messageType}">
	         			<span><yatra:languageTag content="${messageObj.messageText}" language="${language}"/></span>
	      			</div>
	      		</c:forEach>
	      	</c:if>
					
			<div class="wfull">

				
				<!-- <div>
					<font color="#FE2E2E"><b><yatra:languageTag content="PLEASE NOTE" language="${language}"/>:</b></font><br>
					<ul style="padding-left: 3%">
					<li><yatra:languageTag content="In case of any kind of refund in an EMI transaction, interest already billed in a particular transaction will not be refundable under any circumstances." language="${language}"/><br><br></li>
					<li><yatra:languageTag content="EMI option is available on CREDIT / EMI CARDS only ." language="${language}"/></li>
					</ul>
				</div> -->
				 <div class="cpmt_OtherBnkBlock cpmt_net">
					<ul class="noListStyle">
						<script type="text/javascript">
							banksArray = "[";
							$(function () {
								$(document).tooltip();
							});
						</script>
						<c:forEach var="emiBankBean" items="${paymentBean.banks}">
							<c:if test="${emiBankBean.status == 'ENABLED'}">
								<script type="text/javascript">
									<c:forEach var="emiCardTypeBean" items="${emiBankBean.cardTypes}">		
										<c:if test="${emiCardTypeBean.status == 'ENABLED'}">
											banksArray = banksArray  + "{\'bankName\' : \'" + "${emiBankBean.code}" + "\' , \'code\' : \'"+ "${emiCardTypeBean.code}" + "\' , \'logoURL\' : \'" + "${emiCardTypeBean.logoURL}" + "\' , \'cardNumberLength\' : \'" + "${emiCardTypeBean.cardDetail.cardNoLength}" + "\' , \'cvvLength\' : \'" + "${emiCardTypeBean.cardDetail.cvvLength}" + "\'},";
										</c:if>
									</c:forEach>
								</script>
							</c:if>
						</c:forEach>
						<li class="mb10" id="selcted-bank">
							<label class="custom-select w300 select-bank-dd ">
								<select id="emiBank_select" name="emiBank_select" class="template" data-validation="required" data-msginfo="* required">
									<option value=""><yatra:languageTag content="Select your Card Issuer" language="${language}"/></option>
									<c:forEach var="emiBankBean" items="${paymentBean.banks}">
									<c:if test="${emiBankBean.status == 'ENABLED'}">
									<option value="${emiBankBean.code}">${emiBankBean.displayText}</option>
									</c:if>
									</c:forEach>
								</select>
							</label>	
							<div class="get-logo">
							<c:forEach var="emiBankBean" items="${paymentBean.banks}">
							<label class="${emiBankBean.logoURL} hide" for="emiBank"></label>
							</c:forEach>
							</div>
						</li>
						<script type="text/javascript">
							banksArray = banksArray.substring(0,banksArray.length-1)  + "]";								
						</script>
					</ul>
				</div> 
				<div id="emi_pmt_msg_WARNING" class="pmt_msg_WARNING" style=" display:none; "></div>
				<article class="cpmt_payOptions">
					<div class="cpmt_emiOptn" style="display:none;">
						<table width="100%" id="emi-details-table">
							<tr class="tabel-header-footer">
								<th style="width:5%">&nbsp;</th>
								<th style="width:15%"><yatra:languageTag content="Tenure" language="${language}"/>(<yatra:languageTag content="months" language="${language}"/>)</th>
								<th style="width:20%"><yatra:languageTag content="Issuer Interest Rate" language="${language}"/>(%)</th>
								<th style="width:20%"><yatra:languageTag content="Monthly installments" language="${language}"/></th>
								<th style="width:20%" class="lastTd"><yatra:languageTag content="Interest paid to Issuer" language="${language}"/></th>
								<th style="width:20%" class="lastTd"><yatra:languageTag content="Total Amount Payable" language="${language}"/></th>
							</tr>
							
							<c:forEach var="emiBankBean" items="${paymentBean.banks}">
							<c:if test="${emiBankBean.status == 'ENABLED'}">
							<c:forEach var="emiType" items="${emiBankBean.emiTypes}">
							<tr class="${emiBankBean.code}-emi-details ${emiBankBean.code}-${emiType.emiTenure} hide">
								<td>
								<label for='${emiBankBean.code}-${emiType.emiTenure}'>
									<span class="custom-Radiobox" style="left: 10px;">
										<input type="radio" id="${emiBankBean.code}-${emiType.emiTenure}" name="${emiBankBean.code}" value="${emiType.emiTenure}" class="emi-tenure-radio">
										<span class="box"><b class="tick"></b></span>
									</span>
								</label>
								</td>
								<td class="emiTenure">${emiType.emiTenure}</td>
									<c:choose>
    							<c:when test="${emiType.interestRate == '0'}">
       							<td class="interestRate">Interest Free EMI</td>
    							</c:when>    
    							<c:otherwise>
      							 <td class="interestRate">${emiType.interestRate}</td>
   							     </c:otherwise>
								</c:choose>
							
								<td><span class="RupeeSign">Rs.</span><span class="monthlyEMI"></span></td>
								<td><span class="RupeeSign">Rs.</span><span class="InterestPaid"></span></td>
								<td><span class="RupeeSign">Rs.</span><span class="total-payable-amount"></span></td>
							</tr>
							</c:forEach>
							</c:if>
							</c:forEach>
							<tr class="tabel-header-footer">
							<td class="addtMsg" id="emiMsg" colspan="6">Over <span id="cpmt_tenure"></span>  <yatra:languageTag content="months" language="${language}"/>, <yatra:languageTag content="you pay" language="${language}"/> <b><span class="RupeeSign">Rs.</span> 
							<span id="cpmt_emiInterest"></span></b>&nbsp;<yatra:languageTag content="as total interest to the Issuer on a billed amount of <span class" language="${language}"/>="RupeeSign">Rs.</span> 
							<span id="cpmt_amount" ></span></td>
							
								<td class="addtMsg"  id="noCostEmiMsg" colspan="6" style="text-align: left;">
							
									</td>
                                                        </tr>
						</table>
						<span class="taxmsg">
						*<yatra:languageTag content="Note" language="${language}"/>: <yatra:languageTag content="GST will be applicable on the interest amount. Processing fee will be charged as per Bank's terms and conditions" language="${language}"/>.
						</span>
						<span class="taxmsg1">
						<yatra:languageTag content="Convenience Fee of Rs 199+GST is applicable for EMI transactions on HDFC Bank Cards" language="${language}"/>.
						</span>
						<%-- <a target="_blank" href="https://www.yatra.com/fresco/online/journal-article-emi-ax">
						<yatra:languageTag content="View More" language="${language}"/> >></a> --%>
					</div>
					
					<div>	
									 
<!-- 						<span style="color:#348AA3" id="citi_info_message">	</span> -->
							<span id="citi_info_message">	</span>
					</div>
					<div id="bajajWarningMsgDiv">
					<span id="bajajWarningMsg" style="text-align: left;display: inline;margin-top: 0;margin-bottom: 10px;background-color: #FFFCC7;font-size: 13px;float: left;border-radius: 3px;width: 100%;color: #333333;padding: 10px;" >	</span>		
					</div>
					
					<div class="cpmt_tnCstrip">
						<label for="TncAgree">
							<span class="custom-checkbox" >
								<input type="checkbox" name="TncAgree" value="" id="TncAgree" class="mt3" data-validation="required" data-msginfo="* required">
								<span class="box"><b class="tick"></b></span>
							</span>
							<yatra:languageTag content="Yes" language="${language}"/>, <yatra:languageTag content="I have gone through the details and accept the <a target='_blank' id='tncEMI' href='https://www.yatra.com/fresco/online/journal-article-emi-ax' title='Terms and Conditions'>terms and conditions </a> for payment by EMI option" language="${language}"/>.
						</label>
					</div>
				</article> 
				
				 <div id='emi_divErrMsgBlock' class='pmt_errMsgBlock' style='display:none;'></div>
				 <div id='emi_divWarningMsgBlock' class='pmt_warningMsgBlock' style='display:none;'></div>
				<ul class="noListStyle cpmt_listItem mt25">
					
					<li>
						<label class="cpmt_cardLabel emi_card_label" for="emi_cno_id"><yatra:languageTag content="${paymentBean.cardNoLabel}" language="${language}"/></label>
						<div class="card-wrapr">
						<input type="text" class="cardNumber w300 ie6Input" id="emi_cno_id" name="cno" value="" data-validation="required|validateLuhn|cardRegType[VISA]|supportedCard[VISA,emi]|enabledCard[VISA,VISA]|emiCardBankValidaton" data-msginfo="* required|Enter valid card number|Enter valid card number|Card Type is not supported|Card Type is temporarily not supported|Card not issued by selected bank. Please select correct EMI bank" processCardNo="true" maxlength=${paymentBean.banks[0].cardTypes[0].cardDetail.cardNoLength} autocomplete="off">
						<script language="javascript">
							var emi_enabledCards = "";
						</script>
						
						<span cardAttr="pm_cards" id="emiCardLogos" class="card-logos"> 
							<c:forEach var="cardTypeBean" items="${paymentBean.banks[0].cardTypes}">
								<c:if test="${cardTypeBean.status == 'ENABLED'}">
									<i class="PaymentSprite ${cardTypeBean.logoURL}" title=${cardTypeBean.code}></i>	
									<script language="javascript">
										emi_enabledCards = emi_enabledCards + "/" + "${cardTypeBean.code}";
									</script>	
								</c:if>
							</c:forEach>
							<script language="javascript">
								emi_enabledCards = emi_enabledCards.substring(1);
							</script>
						</span>
						</div>
					</li>
					<li>
						<label class="cpmt_cardNameLabel" for="emi_cardholder_name_id"><yatra:languageTag content="Cardholder's Name" language="${language}"/><span class="txtDefault">(<yatra:languageTag content="Please enter the same name which is written on your card" language="${language}"/>)</span></label>
						<input type="text" class="w300 ie6Input" id="emi_cardholder_name_id" name="cardholder_name" data-validation="required|regType[alpha]" data-msginfo="* required|* alphabets only" autocomplete="off">
					
					</li>
				
					<li>
						<div class="blockEx cpmt_expDate-dd" id="emiExpMonth">
							<input type="hidden" id="emi_expMonth_id" name="cexpm" value=""/>
							<label class="cpmt_CClabel pb5" for="emi_expMonth_id"><yatra:languageTag content="${paymentBean.banks[0].cardTypes[0].cardDetail.expiryDateLabel}" language="${language}"/></label>
							<label class="custom-select m_w90" id="expr_month">
							<select id="emi_expm_id" paramId="emi_expMonth_id" data-validation="required" data-msginfo="* required" class="template" maxlength="2">
								<%@ include file="monthList.jsp" %>
							</select> 
							</label>
						</div>
						
						<div class="blockEx cpmt_expDate-dd" id="emiExpYear">
							<input type="hidden" id="emi_expYear_id" name="cexpy" value=""/>
							<label class="cpmt_CClabel pb5">&nbsp;</label>
							<div class="ml10">
							<label class="custom-select m_w90" id="expr_year">
								<select id="emi_expy_id" paramId="emi_expYear_id" data-validation="required|checkExpiryDate[emi]" data-msginfo="* required|Invalid Date" msginfo="* required" class="ml10 template" maxlength="4">
									<option value=""><yatra:languageTag content="Year" language="${language}"/></option>
								</select>
								</label>
							</div>
						</div>
						<div class="blockEx ml15 cpmt_CvvNo" id="emiCvv">
							<label class="cpmt_cardLabel" for="emi_cvv_id"><yatra:languageTag content="${paymentBean.banks[0].cardTypes[0].cardDetail.cvvLabel}" language="${language}"/><i class="ico_info_paySwift PaymentSprite" style="margin-left:10px;"></i><div class="info-tooltip cvvtoolTip">
										<div class="tooltip-content "><div class="cpmt_cvvBlock">
								<i class="PaymentSprite ${paymentBean.banks[1].cardTypes[0].cardDetail.cvvImageURL}" id="emi_CvvUrl_id" defaultVal='PaymentSprite ${paymentBean.banks[1].cardTypes[0].cardDetail.cvvImageURL}'></i>
								<div class="cpmt_CVVtxt" id="emi_cvvImageText_id" defaultVal='${paymentBean.banks[1].cardTypes[0].cardDetail.cvvImageText}' >${paymentBean.banks[1].cardTypes[0].cardDetail.cvvImageText}</div>
							</div></div>
									</div></label>
							<input type="password" style="margin-top:2px" class="w94 flL ie6Input cvvInput" id="emi_cvv_id" name="ccsc"  maxlength=${paymentBean.banks[1].cardTypes[0].cardDetail.cvvLength} data-validation="required|regType[number]|lenCheck[3]" data-msginfo="* required|* number only|* invalid cvv length" autocomplete="off"/>
						</div>
						<!--div class="cpmt_cvvBlock">
							<i class="PaymentSprite ${paymentBean.banks[0].cardTypes[0].cardDetail.cvvImageURL}" id="emi_CvvUrl_id" defaultVal='PaymentSprite ${paymentBean.banks[0].cardTypes[0].cardDetail.cvvImageURL}'></i>
							<span class="cpmt_CVVtxt" id="emi_cvvImageText_id" defaultVal="${paymentBean.banks[0].cardTypes[0].cardDetail.cvvImageText}"><yatra:languageTag content="${paymentBean.banks[0].cardTypes[0].cardDetail.cvvImageText}" language="${language}"/></span>
						</div-->
					</li>
					<li class="emi_moble_row">
						<c:forEach var="emiBankBean" items="${paymentBean.banks}">
							<c:if test="${emiBankBean.displayMobile == 'true'}">
								<div class="emi_mobile_${emiBankBean.code} hide">
									<label class="cpmt_mobileNumberLabel" for="emi_mobile_number_id_${emiBankBean.code}"><yatra:languageTag content="Cardholder's Mobile Number" language="${language}"/></label>
									<input type="text" class="w300 ie6Input" id="emi_mobile_number_id_${emiBankBean.code}" name="emiMobile" value="" data-validation="required|checkVAMobile" data-msginfo="* required|Enter Valid Mobile Number" autocomplete="off">           
								</div>
							</c:if>
						</c:forEach>	
                    </li>
				</ul>
			</div>
		</c:if>
	</c:forEach>
</div>
<!--emi tab ends here-->