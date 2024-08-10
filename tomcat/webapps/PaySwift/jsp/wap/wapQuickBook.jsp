<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>
<section class="Quick-book hide" id="tab_qb">
	<input type="hidden" name="payop" value="qb">
	<input type="hidden" id="qb_cvv" name="ccsc" value="">
	<input type="hidden" id="qb_ctype" name="ctype" value="">
	<input type="hidden" id="qb_card_id" name="cardid" value="">
	<input type="hidden" id="qb_saveQuickBookCard" name="saveQBCard" value="false">
	<input type="hidden" id="qb_isCardInternational" name="isCardInternational" value="false">
	<input type="hidden" id="qb_selected" value="false">
	<div id="qb_section">
		<p><span class="quick-text"><yatra:languageTag content="Your Saved Cards Through" language="${language}"/></span> <i class="payment-icon qb-logo"></i></p>
		<div id="qb_divErrMsgBlock" class="pmt_errMsgBlock" style='display:none;'></div>
		<ul>
			
		</ul>
	</div>
</section>