<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>
<div id="my-booking-saved-cards" style="display: none;">
	<div class="col-md-10">
		<ol class="qb-breadcrumb hidden-xs">
			<li><a href="#" class="show-qb-home"><yatra:languageTag content="QuickBook" language="${language}"/></a></li>
			<li class="active"><yatra:languageTag content="Saved Cards" language="${language}"/></li>
		</ol>
	</div>
	<div id="saved-cards"></div>
</div>