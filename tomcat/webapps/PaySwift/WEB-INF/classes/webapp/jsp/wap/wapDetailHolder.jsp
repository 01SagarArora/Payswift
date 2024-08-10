<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>
<div  id="tab_cc" class="detail-holder" data-paymentMethod="cc">
<input type="hidden" name="payop" value="cc" />
<input type="hidden" name="cexpm" value="" id="cc_expMonth_id" />
<input type="hidden" name="cexpy" value="" id="cc_expYear_id" />
<input type="hidden" name="ctype" value="" id="cc_ctype_id" />
<div id='cc_divErrMsgBlock' class='pmt_errMsgBlock' style='display:none;'></div>
<div id='cc_divWarningMsgBlock' class='pmt_warningMsgBlock' style='display:none;'></div>
<c:forEach var="paymentBean" items="${paymentDisplayBean.paymentOptions}">
<c:if test="${paymentBean.code == 'cc'}">
<div class="wfull card-number">
	<label class="field-label"><yatra:languageTag content="Card Number" language="${language}"/></label>
	<input id="cc_cno_id" name="cno"  placeholder='<yatra:languageTag content="Credit Card Number" language="${language}"/>' value="" class="cardVal process-card-no" data-payop="cc" data-validation="required|validateLuhn|cardRegType[VISA]|supportedCard[VISA,cc]|enabledCard[VISA,VISA]" data-msginfo='* <yatra:languageTag content="required" language="${language}"/>|<yatra:languageTag content="Enter valid card number" language="${language}"/>|<yatra:languageTag content="Enter valid card number" language="${language}"/>|<yatra:languageTag content="Enter a credit card" language="${language}"/>|<yatra:languageTag content="Card Type is temporarily not supported" language="${language}"/>' processCardNo="true" maxlength="${paymentBean.cardTypes[0].cardDetail.cardNoLength}" autocomplete="off"  pattern="[0-9]*"/>
		<script language="javascript">
			var cc_enabledCards = "";
			var amexCvvURL;
			var amexCvvLength;
			var amexCvvImageText;
		</script>
		<c:forEach var="cardTypeBean" items="${paymentBean.cardTypes}">
			<c:if test="${cardTypeBean.status == 'ENABLED'}">
				<i class="payment-icon ${cardTypeBean.logoURL}  logo_${cardTypeBean.code} card_logo" title=${cardTypeBean.code}></i>
				<script language="javascript">
					cc_enabledCards = cc_enabledCards + "/"	+ "${cardTypeBean.code}";
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
	</div>
<div class="wfull">
	<label class="field-label">Name on Card</label>
	<input id="cc_cardholder_name_id" placeholder='<yatra:languageTag content="Cardholder's Name" language="${language}"/>' name="cardholder_name" type="text" value="" data-validation="required|regType[alpha]" data-msginfo='* <yatra:languageTag content="required" language="${language}"/>|* <yatra:languageTag content="alphabets only" language="${language}"/>' />
</div>
<div class="wfull">
	<div class="expiry-date ">
		<label class="field-label">${paymentBean.cardTypes[0].cardDetail.expiryDateLabel}</label>
		<div class="drop-menu ml0">
		
			<select id="cc_expm_id" onChange="javascript:YatraPaymentUI.setCardExpData(this);" paramId="cc_expMonth_id" name="cexpm" data-validation="required"  data-msginfo='* <yatra:languageTag content="required" language="${language}"/>'>
				<option value="">Month</option>
				<jsp:include page="monthList.jsp"></jsp:include>
			</select>
			</div>
			<div class="drop-menu">
			<jsp:useBean id="date" class="java.util.Date" />
			<fmt:formatDate value="${date}" pattern="yyyy" var="currentYear" />
			<select id="cc_expy_id" name="cexpy" paramId="cc_expYear_id" onChange="javascript:YatraPaymentUI.setCardExpData(this);" data-validation="required"  data-msginfo='* <yatra:languageTag content="required" language="${language}"/>' >
				<option value="">Year</option>
				<c:forEach var="i" begin="${currentYear}" end="${currentYear+20}" step="1">
					<option value="${i}">${i}</option>
				</c:forEach>
			</select>
			</div>
	</div>
	<div class="card-cvv">
		<label class="field-label"><yatra:languageTag content="${paymentBean.cardTypes[0].cardDetail.cvvLabel}" language="${language}"/></label>
			<input  id="cc_cvv_id" name="ccsc" type="password" value="" data-validation="required|regType[number]|lenCheck[3]" data-msginfo='* <yatra:languageTag content="required" language="${language}"/>|* <yatra:languageTag content="number only" language="${language}"/>|* <yatra:languageTag content="invalid cvv length" language="${language}"/>' maxlength="${paymentBean.cardTypes[0].cardDetail.cvvLength}" pattern="[0-9]*" />
	</div>
</div>

<div id="cc_AddressShowHide" style="display:none;" class="detail-holder amex-form">
	<div id="cc_amex_qn_div">
		<h3><yatra:languageTag content="Where was your Amex card issued?" language="${language}"/></h3>
		<ul>
			<li>
				<input type="radio" checked="checked" class="custom" value="Domestic" id="domestic_address_rdo" name="address_check_rdo">
				<label for="India"><yatra:languageTag content="India" language="${language}"/></label>
			</li>
			<li>
				<input type="radio" class="custom" value="International" id="international_address_rdo" name="address_check_rdo">
				<label for="outside-india"><yatra:languageTag content="Outside India" language="${language}"/></label>
			</li>
		</ul>
	</div>
	<div class="extra-indo-amex" id="cc_pmt_addressId" style="display:none;">
		<ul>
			<li>
				<label class="field-label"><yatra:languageTag content="${paymentBean.billingAddress.billingAddressLabel}" language="${language}"/></label>
				<input  type="text" id="cc_ba1" name="ba1" class="cardVal" data-validation="required" data-msginfo='* <yatra:languageTag content="required" language="${language}"/>' placeholder='<yatra:languageTag content="${paymentBean.billingAddress.billingAddress1}" language="${language}"/>'/>
			</li>
			<li>
				<label class="field-label"><yatra:languageTag content="${paymentBean.billingAddress.billingCity}" language="${language}"/></label>
				<input  type="text" id="cc_bcity" name="bacy" placeholder='<yatra:languageTag content="${paymentBean.billingAddress.billingCity}" language="${language}"/>' data-validation="required|regType[onlyLetter]" data-msginfo='* <yatra:languageTag content="required" language="${language}"/>|* <yatra:languageTag content="alphabets only" language="${language}"/>' class="cardVal"/>
			</li>
			<li>
				<label class="field-label"><yatra:languageTag content="${paymentBean.billingAddress.billingState}" language="${language}"/></label>
				<input type="text" id="cc_bstate" name="bast" placeholder='<yatra:languageTag content="${paymentBean.billingAddress.billingState}" language="${language}"/>' data-validation="required|regType[onlyLetter]" data-msginfo='* <yatra:languageTag content="required" language="${language}"/>|* <yatra:languageTag content="alphabets only" language="${language}"/>' class="cardVal"/>
			</li>
		</ul>
				<ul>
			<li>
				<label class="field-label"><yatra:languageTag content="${paymentBean.billingAddress.billingPin}" language="${language}"/></label>
				<input type="text" id="cc_bpin" name="bapi" placeholder='<yatra:languageTag content="${paymentBean.billingAddress.billingPin}" language="${language}"/>' data-validation="required" data-msginfo='* <yatra:languageTag content="required" language="${language}"/>' maxlength="10" class="cardVal"/>
			</li>
			<li>
				<label class="field-label"><yatra:languageTag content="Country" language="${language}"/></label>
			<div class="drop-menu ml0">
				<select id="cc_bcountry" paramId="cc_bcountry_id" data-validation="required" data-msginfo='* <yatra:languageTag content="select an option" language="${language}"/>'>
					<option value="">Country</option>
					<option value="India">India</option>
					<option value="USA">USA</option>
					<option value="UK">UK</option>
				</select>
				</div>
			</li>
			<li>
				<label class="field-label"><yatra:languageTag content="${paymentBean.billingAddress.billingIsdCode}" language="${language}"/></label>
				<input  type="text" id="cc_baisd" name="baisd" pattern="[0-9]*" placeholder='<yatra:languageTag content="${paymentBean.billingAddress.billingIsdCode}" language="${language}"/>' data-validation="required|regType[number]|lenCheck[4]" data-msginfo='* <yatra:languageTag content="required" language="${language}"/>|* <yatra:languageTag content="numbers only" language="${language}"/>|<yatra:languageTag content="Invalid ISD length" language="${language}"/>' maxlength="4" class="cardVal"/>
			</li>
			<li>
				<label class="field-label"><yatra:languageTag content="${paymentBean.billingAddress.billingMobile}" language="${language}"/></label>
				<input type="text" id="cc_bamob" name="bamob" placeholder='<yatra:languageTag content="${paymentBean.billingAddress.billingMobile}" language="${language}"/>' data-validation="required|regType[number]|lenCheck[10]" data-msginfo='* <yatra:languageTag content="required" language="${language}"/>|* <yatra:languageTag content="Please enter a valid number" language="${language}"/>|* <yatra:languageTag content="Please enter a valid number" language="${language}"/>' maxlength="10" class="cardVal"/>
			</li>
		</ul>
	</div>
</div>

</c:if>
</c:forEach>
<!--quickbook register -->
	<div class="qb-register hide">
		<input type="checkbox" name="saveQB" id="cc_saveQBCard" class="custom" />
		<label for="checkbox-1">
		<span class="qb-text-cont">
		<i class="payment-icon qb-logo-small"></i><span class="qb-text"><yatra:languageTag content="Save this card for faster payment next time" language="${language}"/>.</span>
		</span>
		</label>
	</div>
</div>