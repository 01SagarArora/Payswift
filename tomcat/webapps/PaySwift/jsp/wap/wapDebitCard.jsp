<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<div  id="tab_dc" class="content detail-holder other_option_div" data-paymentMethod="dc" style="display: none;">
<input type="hidden" name="payop" value="dc" />
<input type="hidden" name="cexpm" value="" id="dc_expMonth_id" />
<input type="hidden" name="cexpy" value="" id="dc_expYear_id" />
<input type="hidden" name="ctype" value="" id="dc_ctype_id" />
<input type="hidden" name="saveQBCard" value="false" id="dc_saveQuickBookCard_id" />
<div id='dc_divErrMsgBlock' class='pmt_errMsgBlock' style='display:none;'></div>
<div id='dc_divWarningMsgBlock' class='pmt_warningMsgBlock' style='display:none;'></div>
<c:forEach var="paymentBean" items="${paymentDisplayBean.paymentOptions}">
<c:if test="${paymentBean.code == 'dc'}">
<div class="wfull card-number">
	<label class="field-label"><yatra:languageTag content="Card Number" language="${language}"/></label>
	<input id="dc_cno_id" name="cno"  autocomplete="off" placeholder="<yatra:languageTag content="Debit Card Number" language="${language}"/>" value="" class="cardVal process-card-no"  data-payop="dc" data-validation="required|validateLuhn|cardRegType[VISA]|supportedCard[VISA,cc]|enabledCard[VISA,VISA]" data-msginfo='* <yatra:languageTag content="required" language="${language}"/>|<yatra:languageTag content="Please enter a valid number" language="${language}"/>|<yatra:languageTag content="Please enter a valid number" language="${language}"/>|<yatra:languageTag content="Enter a debit card" language="${language}"/>|<yatra:languageTag content="Card Type is temporarily not supported" language="${language}"/>' processCardNo="true" maxlength="19" autocomplete="off" pattern="[0-9]*"/>
		<script language="javascript">
			var dc_enabledCards = "";
		</script>
		<c:forEach var="cardTypeBean" items="${paymentBean.cardTypes}">
			<c:if test="${cardTypeBean.status == 'ENABLED'}">
				<i class="payment-icon ${cardTypeBean.logoURL}  logo_${cardTypeBean.code} card_logo" title=${cardTypeBean.code}></i>
				<script language="javascript">
					dc_enabledCards = dc_enabledCards + "/"	+ "${cardTypeBean.code}";
				</script>
			</c:if>
		</c:forEach>
		<script language="javascript">
			dc_enabledCards = dc_enabledCards.substring(1);
		</script>
	</div>
<div class="wfull" style="margin:0">
	<label class="field-label"><yatra:languageTag content="Name on Card" language="${language}"/></label>
	<input id="dc_cardholder_name_id" placeholder="<yatra:languageTag content="Cardholder's Name" language="${language}"/>" name="cardholder_name" type="text" value="" data-validation="required|regType[alpha]" data-msginfo='* <yatra:languageTag content="required" language="${language}"/>|* <yatra:languageTag content="alphabets only" language="${language}"/>' />
</div>
<div class="wfull">
	<div class="expiry-date ">
		<label class="field-label"><yatra:languageTag content="${paymentBean.cardTypes[0].cardDetail.expiryDateLabel}" language="${language}"/></label>
		<div class="drop-menu ml0">
		
			<select id="dc_expm_id" onChange="javascript:YatraPaymentUI.setCardExpData(this);" paramId="dc_expMonth_id" name="cexpm" data-validation="required"  data-msginfo='* <yatra:languageTag content="required" language="${language}"/>' >
				<option value=""><yatra:languageTag content="Month" language="${language}"/></option>
				<jsp:include page="monthList.jsp"></jsp:include>
			</select>
			</div>
			<div class="drop-menu">
			<jsp:useBean id="date" class="java.util.Date" />
			<fmt:formatDate value="${date}" pattern="yyyy" var="currentYear" />
			<select id="dc_expy_id" name="cexpy" paramId="dc_expYear_id" onChange="javascript:YatraPaymentUI.setCardExpData(this);" data-validation="required"  data-msginfo='* <yatra:languageTag content="required" language="${language}"/>' >
				<option value=""><yatra:languageTag content="Year" language="${language}"/></option>
				<c:forEach var="i" begin="${currentYear}" end="${currentYear+20}" step="1">
					<option value="${i}">${i}</option>
				</c:forEach>
			</select>
			</div>
	</div>
	<div class="card-cvv">
		<label class="field-label"><yatra:languageTag content="${paymentBean.cardTypes[0].cardDetail.cvvLabel}" language="${language}"/></label>
			<input  id="dc_cvv_id" autocomplete="off"  name="ccsc" type="password" value="" data-validation="required|regType[number]|lenCheck[3]" data-msginfo='* <yatra:languageTag content="required" language="${language}"/>|* <yatra:languageTag content="number only" language="${language}"/>|* <yatra:languageTag content="invalid cvv length" language="${language}"/>' maxlength="${paymentBean.cardTypes[0].cardDetail.cvvLength}" pattern="[0-9]*"/>
	</div>
</div>
<div   class="detail-holder amex-form" id="dc_pmt_addressId" style="display:none;">
		<div class="wfull card-number">
			<label class="field-label"><yatra:languageTag content="${paymentBean.billingAddress.billingAddressLabel}" language="${language}"/></label>
			<input  type="text" id="dc_ba1" name="ba1" class="cardVal" data-validation="required" data-msginfo='* <yatra:languageTag content="required" language="${language}"/>' placeholder="<yatra:languageTag content="${paymentBean.billingAddress.billingAddress1}" language="${language}"/>"/>
		</div>
		<div class="extra-indo-amex" >
			<ul>
				<li>
					<label class="field-label"><yatra:languageTag content="${paymentBean.billingAddress.billingCity}" language="${language}"/></label>
					<input  type="text" id="dc_bcity" name="bacy" placeholder="<yatra:languageTag content="${paymentBean.billingAddress.billingCity}" language="${language}"/>" data-validation="required|regType[onlyLetter]" data-msginfo='* <yatra:languageTag content="required" language="${language}"/>|* <yatra:languageTag content="alphabets only" language="${language}"/>' class="cardVal"/>
				</li>
				<li>
					<label class="field-label"><yatra:languageTag content="${paymentBean.billingAddress.billingState}" language="${language}"/></label>
					<input type="text" id="dc_bstate" name="bast" placeholder="<yatra:languageTag content="${paymentBean.billingAddress.billingState}" language="${language}"/>" data-validation="required|regType[onlyLetter]" data-msginfo='* <yatra:languageTag content="required" language="${language}"/>|* <yatra:languageTag content="alphabets only" language="${language}"/>' class="cardVal"/>
				</li>
			</ul>
			<ul>
				<li>
					<label class="field-label"><yatra:languageTag content="${paymentBean.billingAddress.billingPin}" language="${language}"/></label>
					<input type="text" id="dc_bpin" name="bapi" placeholder="<yatra:languageTag content="${paymentBean.billingAddress.billingPin}" language="${language}"/>" data-validation="required" data-msginfo='* <yatra:languageTag content="required" language="${language}"/>' maxlength="10" class="cardVal"/>
				</li>
				<li>
					<label class="field-label"><yatra:languageTag content="Country" language="${language}"/></label>
				<div class="drop-menu ml0">
					<input type="hidden" id="dc_bcountry_id" name="bacu" value=""/>
					<select id="dc_bcountry" paramId="dc_bcountry_id" data-validation="required" data-msginfo='* <yatra:languageTag content="select an option" language="${language}"/>'>
						<%@ include file="countryList.jsp" %>
					</select>
					</div>
				</li>
			</ul>
			<ul>
				<li>
					<label class="field-label"><yatra:languageTag content="${paymentBean.billingAddress.billingIsdCode}" language="${language}"/></label>
					<input  type="text" id="dc_baisd" name="baisd" placeholder="<yatra:languageTag content="${paymentBean.billingAddress.billingIsdCode}" language="${language}"/>" data-validation="required|regType[number]|lenCheck[3]" data-msginfo='* <yatra:languageTag content="required" language="${language}"/>|* <yatra:languageTag content="numbers only" language="${language}"/>|<yatra:languageTag content="Invalid ISD length" language="${language}"/>' maxlength="3" class="cardVal"/>
				</li>
				<li>
					<label class="field-label"><yatra:languageTag content="${paymentBean.billingAddress.billingMobile}" language="${language}"/></label>
					<input type="text" id="dc_bamob" name="bamob" placeholder="<yatra:languageTag content="${paymentBean.billingAddress.billingMobile}" language="${language}"/>" data-validation="required|regType[number]|lenCheck[10]" data-msginfo='* <yatra:languageTag content="required" language="${language}"/>|* <yatra:languageTag content="Please enter a valid number" language="${language}"/>|* <yatra:languageTag content="Please enter a valid number" language="${language}"/>' maxlength="10" class="cardVal"/>
				</li>
			</ul>
		</div>
	</div>
	<!--quickbook register -->
	<div class="qb-register hide" id="dc_SaveOptionDiv">
		<input type="checkbox"  id="dc_saveQBCard" class="custom" />
		<label for="dc_saveQBCard">
		<span class="qb-text-cont">
		<i class="payment-icon qb-logo-small"></i><span class="qb-text"><yatra:languageTag content="Save this card for faster payment next time" language="${language}"/>.</span>
		</span>
		</label>
	</div>
</c:if>
</c:forEach>

</div>