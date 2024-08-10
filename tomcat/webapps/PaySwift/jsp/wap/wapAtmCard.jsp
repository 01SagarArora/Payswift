<!--ATM Banking starts here*-->
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>
<div id="tab_atm" style="display: none;" class="content atm-card  other_option_div">
	<input type="hidden" name="payop" value="atm" /> 
	<input type="hidden" name="bankCode" value="" />

	<c:forEach var="paymentBean" items="${paymentDisplayBean.paymentOptions}">
		<c:if test="${paymentBean.code == 'atm'}">
			<c:forEach var="messageBean" items="${paymentBean.messages}">
				<c:if
					test="${(messageBean.messageType == 'WARNING') and (messageBean.status == 'ENABLED')}">
					<div class="wflL pmt_warningMsgBlock">
						<span><font color="#FF0000">${messageBean.messageText}</font></span>
					</div>
				</c:if>
			</c:forEach>
			<div class="atm-optn-cont">
				<div class="wfull">
					<label class="field-label"><yatra:languageTag content="Choose your Bank" language="${language}"/></label>
					<select id="atmDropdown" name="atmDropdown" data-msginfo='* <yatra:languageTag content="Please select Option from the dropdown" language="${language}"/>' data-validation="required">
						<option value="" selected="selected"><yatra:languageTag content="Popular Banks" language="${language}"/></option>
						<c:forEach var="bankBean" items="${paymentBean.banks}">
							<c:if test="${bankBean.status == 'ENABLED'}">
								<option name="atmOptns"	id="${bankBean.code}" value="${bankBean.code}" >
									<yatra:languageTag content="${bankBean.displayText}" language="${language}"/>
								</option>
							</c:if>

						</c:forEach>
					</select>
				</div>
			</div>
		</c:if>
	</c:forEach>

</div>
<!--ATM Banking ends here-->
