<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>
<div class="content cpmt_payop" id="tab_dcpin" style="display:none;">

    <input type="hidden" name="payop" value="dcpin"/>
    <input type="hidden" name="bankCode" value=""/>
    <input type="hidden" id="dcpin_isCardInternational_id" name="isCardInternational" value="false"/>
    <input type="hidden" id="dcpin_ctype_id" name="ctype" value=""/>
    <input type="hidden" id="dcpin_cvv" name="ccsc" value=""/>
    <input type="hidden" id="dcpin_bank_name" name="dcPinBankName" value=""/>

    <div id='dcpin_divErrMsgBlock' class='pmt_errMsgBlock' style='display:none;'></div>
    <div id='dcpin_divWarningMsgBlock' class='pmt_warningMsgBlock' style='display:none;'></div>

    <div class="">
        <c:forEach var="paymentBean" items="${paymentDisplayBean.paymentOptions}">
            <c:if test="${paymentBean.code == 'dcpin'}">
                <div class="">
                    <script language="javascript">
                        var dcPinBankCodeVsBankName = new Object();
                    </script>
                    <label class="custom-select">
                        <select id="dcpinBank" name="dcPinBank" class="template" style="width:240px;" data-validation="validBankSelectedCheck" data-msginfo="* Select Your Bank">
                            <option selected="selected" value=""><yatra:languageTag content="Select Your Bank" language="${language}"/></option>
                                <c:forEach var="bankBean" items="${paymentBean.banks}">
                                    <c:if test="${(bankBean.preferred == 'false') and (bankBean.status == 'ENABLED')}">
                                        <option value='${bankBean.code}'><yatra:languageTag content="${bankBean.displayText}" language="${language}"/></option>
                                        <script language="javascript">
                                            dcPinBankCodeVsBankName["${bankBean.code}"] = "${bankBean.displayText}";
                                        </script>
                                    </c:if>
                                </c:forEach>
                        </select>
                    </label>
                </div>

                <div class="cpmt_itzTxt" id="dcpinpaylaterlabel" style="display:none;margin-top:20px;">
					<yatra:languageTag content="Please click on Pay Now to continue. You will be required to enter card details on next page." language="${language}"/>
				</div>

                <ul class="noListStyle cpmt_listItem" id = "dcpincardul" style="margin-top:20px;display:none;">
                    <li>
                        <label class="cpmt_cardLabel"><yatra:languageTag content="${paymentBean.cardNoLabel}" language="${language}"/></label>
                        <div class="card-wrapr">
                        	<input type="text" class="cardNumber w240 ie6Input" id="dcpin_cno_id" name="cno" data-validation="required|validateLuhn|cardRegType[VISA]|supportedCard[VISA,dc]|enabledCard[VISA,VISA]" data-msginfo="* required|Enter valid card number|Enter valid card number|Enter a debit card|Card Type is temporarily not supported" processCardNo="true" maxlength=19 class="w120 pmt_inputTxtBox" autocomplete="off"/>

                        	<script language="javascript">
                        		var dcpin_enabledCards = "";
                        		var dcPinCardLogoVsCode = new Object();
                        	</script>
                        	<span cardAttr="pm_cards" id = "dcpincardlogos" class="card-logos">
                        		<c:forEach var="cardTypeBean" items="${paymentBean.cardTypes}">
                        			<c:if test="${cardTypeBean.status == 'ENABLED'}">
                        				<i class="PaymentSprite ${cardTypeBean.logoURL}" title=${cardTypeBean.code}></i>
                        				<script language="javascript">
                        					dcpin_enabledCards = dcpin_enabledCards + "/" + "${cardTypeBean.code}";
                        					dcPinCardLogoVsCode["${cardTypeBean.logoURL}"] = "${cardTypeBean.code}";
                        				</script>
                        			</c:if>
                        		</c:forEach>
                        	</span>
                        	<script language="javascript">
                        		dcpin_enabledCards = dcpin_enabledCards.substring(1);
                        	</script>

                        </div>
                    </li>
                    <li>
                    	<label class="cpmt_cardNameLabel"><yatra:languageTag content="Cardholder's Name" language="${language}"/><span class="txtDefault"> (<yatra:languageTag content="Please enter the same name which is written on your card" language="${language}"/>)</span></label>
                    	<input type="text" class="w300 ie6Input" id="dcpin_cardholder_name_id" name="cardholder_name" data-validation="required|regType[alpha]" data-msginfo="* required|* alphabets only" autocomplete="off">
                    </li>
                    <li>

                    	<div class="blockEx cpmt_expDate-dd">
                    		<input type="hidden" id="dcpin_expMonth_id" name="cexpm" value=""/>
                    		<label class="cpmt_CClabel pb5" ><yatra:languageTag content="${paymentBean.cardTypes[0].cardDetail.expiryDateLabel}" language="${language}"/></label>
                    		<label class="custom-select m_w90">
                    		<select id="dcpin_expm_id" paramId="dcpin_expMonth_id" data-validation="required" data-msginfo="* required" class="template" maxlength="2">
                    			<%@ include file="monthList.jsp" %>
                    		</select>
                    		</label>
                    	</div>

                    	<div class="blockEx cpmt_expDate-dd">
                    		<input type="hidden" id="dcpin_expYear_id" name="cexpy" value=""/>
                    		<label class="cpmt_CClabel pb5">&nbsp;</label>
                    		<div class="ml10">
                    		<label class="custom-select m_w90">
                    			<select id="dcpin_expy_id" paramId="dcpin_expYear_id" data-validation="required|checkExpiryDate[cc]" data-msginfo="* required|Invalid Date" class="ml10 template" maxlength="4">
                    				<option value=""><yatra:languageTag content="Year" language="${language}"/></option>
                    			</select>
                    		</label>
                    		</div>
                    	</div>

                    	<div class="blockEx ml15 cpmt_CvvNo">
                        	<label class="cpmt_cardLabel"><yatra:languageTag content="${paymentBean.cardTypes[0].cardDetail.cvvLabel}" language="${language}"/></label>
                        	<input type="password" class="w45 cvvInput flL ie6Input" id="dcpin_cvv_id" maxlength=${paymentBean.cardTypes[0].cardDetail.cvvLength} data-validation="required|regType[number]|lenCheck[3]" data-msginfo="* required|* number only|* invalid cvv length" autocomplete="off"/>
                        </div>
                        <!--div class="cpmt_cvvBlock">
                        	<i class="PaymentSprite ${paymentBean.cardTypes[0].cardDetail.cvvImageURL}" defaultVal='PaymentSprite ${paymentBean.cardTypes[0].cardDetail.cvvImageURL}'></i>
                        	<span class="cpmt_CVVtxt" defaultVal="${paymentBean.cardTypes[0].cardDetail.cvvImageText}">${paymentBean.cardTypes[0].cardDetail.cvvImageText}</span>
                        </div-->

                    </li>
                </ul>


            </c:if>
        </c:forEach>
    </div>

</div>