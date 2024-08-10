<!--emi tab starts here-->
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>
<div id="tab_emi" style="display:none;" class="content emi-optn other_option_div">
	<input type="hidden" name="payop" value="emi"/>
	<input type="hidden" id="emi_ctype_id" name="ctype" value=""/>
	<input type="hidden" name="isEmiPayment" value="true"/>
	<input type="hidden" id="emi_emiBank_id" name="emiBank" value=""/>
	<input type="hidden" id="emi_emiTenure_id" name="emiTenure" value=""/>

	 <div id='emi_divErrMsgBlock' class='pmt_errMsgBlock' style='display:none;'></div>
	<div id='emi_divWarningMsgBlock' class='pmt_warningMsgBlock' style='display:none;'></div>
			 
	<c:forEach var="paymentBean" items="${paymentDisplayBean.paymentOptions}">
		<c:if test="${paymentBean.code == 'emi'}">				
			<div class="net-banking-cont">
				<c:forEach var="messageBean" items="${paymentBean.messages}">
					<c:if test="${(messageBean.messageType == 'WARNING') and (messageBean.status == 'ENABLED')}">
						<div class="wflL pmt_warningMsgBlock">
							<span><font color="#FF0000">${messageBean.messageText}</font></span>
						</div>
					</c:if>
				</c:forEach>
				<div class="wfull">
					<label class="field-label"><yatra:languageTag content="Select your Bank" language="${language}"/></label>
					<select id="emiDropdown" name="emiDropdown" data-msginfo='* <yatra:languageTag content="Please select Option from the dropdown" language="${language}"/>' data-validation="required">
						<option value="" selected="selected"><yatra:languageTag content="Popular Banks" language="${language}"/></option>
						<script language="javascript">
							banksArray = "[";
						</script>
						<c:forEach var="emiBankBean" items="${paymentBean.banks}">
							<c:if test="${emiBankBean.status == 'ENABLED'}">
								<option id="${emiBankBean.code}" name="emiBank" value='${emiBankBean.code}'>
									<yatra:languageTag content="${emiBankBean.displayText}" language="${language}"/>
								</option>
								<script language="javascript">
									<c:forEach var="emiCardTypeBean" items="${emiBankBean.cardTypes}">		
										<c:if test="${emiCardTypeBean.status == 'ENABLED'}">
											banksArray = banksArray  + "{\'bankName\' : \'" + "${emiBankBean.code}" + "\' , \'code\' : \'"+ "${emiCardTypeBean.code}" + "\' , \'logoURL\' : \'" + "${emiCardTypeBean.logoURL}" + "\'},";
										</c:if>
									</c:forEach>
								</script>
							</c:if>
						</c:forEach>
						<script language="javascript">
							banksArray = banksArray.substring(0,banksArray.length-1)  + "]";								
						</script>
					</select>
				</div>
				<div class="detail-emi">
					<div class="emi-dets">
						<div class="emi-plans" style="display:none;">
							<div class="wfull" style="margin:0">
								<label class="field-label"><yatra:languageTag content="Choose an EMI Plan" language="${language}"/></label>
							</div>
							<div class="emi-head">
								<span>Tenure</span>
								<span class="fr"><yatra:languageTag content="EMI Amount" language="${language}"/></span>
							</div>
							<ul class="clearfix emi-plan-box">
								<c:forEach var="emiBankBean" items="${paymentBean.banks}">
								<c:if test="${emiBankBean.status == 'ENABLED'}">
								<c:forEach var="emiType" items="${emiBankBean.emiTypes}">
								<li class="${emiBankBean.code}-emi-details ${emiBankBean.code}-${emiType.emiTenure}">
									<input type="radio" id="${emiBankBean.code}-${emiType.emiTenure}" name="${emiBankBean.code}" value="${emiType.emiTenure}" class="emi-tenure-radio">
									<label for='${emiBankBean.code}-${emiType.emiTenure}'>
										<table width="100%" class="emi-tenure" id="">
											<tr>
												<td>
													<span id="cpmt_emiTenure">${emiType.emiTenure}</span> Months
												</td>
												<td colspan="2">
													<span class="fr"><span class="rs">Rs.</span><span id="cpmt_monthInstallment" class="monthlyEMI"></span></span>
												</td>
											</tr>
											<tr class="emi-intrest">
												<td><yatra:languageTag content="Interest Rate" language="${language}"/> : <span id="cpmt_emiInterestRate" class="interestRate">${emiType.interestRate}</span> %</td>
												<td><yatra:languageTag content="Interest Paid" language="${language}"/> : <span class="rs">Rs.</span><span id="cpmt_bankInterest" class="InterestPaid"></span></td>
												<td><yatra:languageTag content="Total" language="${language}"/> : <span class="rs">Rs.</span><span class="total-payable-amount"></span></td>
											</tr>
											
										</table>
									</label>
								</li>
								</c:forEach>
								</c:if>
								</c:forEach>
								<li class="tabel-header-footer">
									<div class="addtMsg" colspan="6">Over <span id="cpmt_tenure"></span>  months, you pay <b><span class="rs">Rs.</span>
										<span id="cpmt_emiInterest"></span></b>&nbsp;as total interest to the Issuer on a billed amount of <span class="rs">Rs.</span>
										<span id="cpmt_amount" ></span>
									</div>
									<div>*Note:<yatra:languageTag content="Service tax will be applicable on the interest amount." language="${language}"/><br><a target="_blank" href="https://www.yatra.com/fresco/online/journal-article-emi-ax" class="more-link">Know More</a><br>**Note:<yatra:languageTag content="EMI option is available on CREDIT CARDS only ." language="${language}"/></div><br>
									<br>
								</li>
								
							</ul>
						</div>
					</div>
					<div class="detail-holder">
						<div class="wfull card-number">
							<label class="field-label"><yatra:languageTag content="Card Number" language="${language}"/></label>
							<input type="text" id="emi_cno_id" autocomplete="off"  name="cno" class="process-card-no" value="" data-payop="emi" data-validation="required|validateLuhn|cardRegType[VISA]|supportedCard[VISA,emi]|enabledCard[VISA,VISA]" data-msginfo='* <yatra:languageTag content="required" language="${language}"/>|<yatra:languageTag content="Enter valid card number" language="${language}"/>|<yatra:languageTag content="Enter valid card number" language="${language}"/>|<yatra:languageTag content="Enter a credit card" language="${language}"/>|<yatra:languageTag content="Card Type is not supported by bank" language="${language}"/>' processCardNo="true" maxlength=${paymentBean.banks[0].cardTypes[0].cardDetail.cardNoLength} autocomplete="off" pattern="[0-9]*">
							<script language="javascript">
							var emi_enabledCards = "";
							</script>
							<span cardAttr="pm_cards" id="emiCardLogos"> 
							<c:forEach var="cardTypeBean" items="${paymentBean.banks[0].cardTypes}">
							<c:if test="${cardTypeBean.status == 'ENABLED'}">
							<i class="payment-icon ${cardTypeBean.logoURL} logo_${cardTypeBean.code} card_logo" title=${cardTypeBean.code}></i>
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
						<div class="wfull">
							<label class="field-label"><yatra:languageTag content="Name on Card" language="${language}"/></label>
							<input type="text" placeholder="Cardholder's Name" id="emi_cardholder_name_id" name="cardholder_name" data-validation="required|regType[alpha]" data-msginfo='* <yatra:languageTag content="required" language="${language}"/>|* <yatra:languageTag content="alphabets only" language="${language}"/>' autocomplete="off">
						</div>
						<div class="wfull">
							<div class="expiry-date">
								<label class="field-label"><yatra:languageTag content="Expiry Date" language="${language}"/></label>
								<div class="drop-menu ml0">
									<input type="hidden" id="emi_expMonth_id" name="cexpm" value=""/>
									<select id="emi_expm_id" paramId="emi_expMonth_id" data-validation="required" data-msginfo='* <yatra:languageTag content="required" language="${language}"/>' maxlength="2">
										<option value=""><yatra:languageTag content="Month" language="${language}"/></option>
										<jsp:include page="monthList.jsp"></jsp:include>
									</select>
								</div>
								<div class="drop-menu">
									<input type="hidden" id="emi_expYear_id" name="cexpy" value=""/>
									<jsp:useBean id="date" class="java.util.Date" />
									<fmt:formatDate value="${date}" pattern="yyyy" var="currentYear" />
									<select id="emi_expy_id" paramId="emi_expYear_id" data-validation="required|checkExpiryDate[emi]" data-msginfo='* <yatra:languageTag content="required" language="${language}"/>|<yatra:languageTag content="Invalid Date" language="${language}"/>' msginfo="* required" maxlength="4">
										<option value="">Year</option>
										<c:forEach var="i" begin="${currentYear}" end="${currentYear+20}" step="1">
											<option value="${i}">${i}</option>
										</c:forEach>
									</select>
								</div>
							</div>
							<div class="card-cvv">
								<label class="field-label">${paymentBean.banks[0].cardTypes[0].cardDetail.cvvLabel}</label>
								<input type="password"  autocomplete="off" id="emi_cvv_id" name="ccsc"  maxlength=${paymentBean.banks[0].cardTypes[0].cardDetail.cvvLength} data-validation="required|regType[number]|lenCheck[3]" data-msginfo='* <yatra:languageTag content="required" language="${language}"/>|* <yatra:languageTag content="number only" language="${language}"/>|* <yatra:languageTag content="invalid cvv length" language="${language}"/>' autocomplete="off" pattern="[0-9]*"/>
							</div>
						</div>
					</div>
				</div>
			</div>
		</c:if>
	</c:forEach>
</div>
<!--emi card ends here-->