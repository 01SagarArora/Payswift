<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>
<div class="corp-qb-box row" id="corp-saved-cards" style="display: none;">
	<div class="col-md-7 col-centered">
		<div class="col-md-12">
	
			<ol class="qb-breadcrumb hidden-xs">
				<li><a href="#" class="show-corp-qb-home"><yatra:languageTag content="Corporate QuickBook" language="${language}"/></a></li>
				<li class="active"><yatra:languageTag content="Saved Cards" language="${language}"/></li>
			</ol>
		</div>
			<div id="saved-cards-error"></div>
		<div id="saved-cards" class="col-md-12"></div>
	</div>
</div>
