<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>

			
			<!--start Partial Payment -->
			<div data-role="tabs" id="tabs" class="tabs-version-two">
	<div data-role="navbar">
		<ul>
			<li><a href="#pay-now" data-ajax="false"><yatra:languageTag content="Pay Now" language="${language}"/></a></li>
			<li><a href="#partial-payment" data-ajax="false"><yatra:languageTag content="Partial Payment" language="${language}"/></a></li>
		</ul>
	</div>
	<div id="pay-now" class="ui-content">
		<div class="you-pay">
			<span class="yt-txt"><yatra:languageTag content="You Pay" language="${language}"/>:</span>
			<span class="rs-sec"><i class="rs">Rs.</i> 2,00,000</span>
		</div>
	</div>
	<div id="partial-payment" class="ui-content">
		<ul class="pp-content">
			<li>
				<div class="pp-head"><yatra:languageTag content="Pay Now" language="${language}"/></div>
				<div class="pp-cont"><sup class="rs">Rs.</sup>100</div>
			</li>
			<li>
				<div class="pp-head"><yatra:languageTag content="Pay Later" language="${language}"/></div>
				<div class="pp-cont"><sup class="rs">Rs.</sup>2,000</div>
			</li>
			<li>
				<div class="pp-head"><yatra:languageTag content="Pay By" language="${language}"/></div>
				<div class="pp-cont">15 Dec</div>
			</li>
			
		</ul>
	</div>
</div>