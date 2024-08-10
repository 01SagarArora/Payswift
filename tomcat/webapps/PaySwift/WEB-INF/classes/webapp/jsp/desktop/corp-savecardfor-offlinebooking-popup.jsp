<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>
<div class="modal fade yt-modal" id="save-card-offline-popup-id" tabindex="-1" role="dialog" aria-labelledby="mySmallModalLabel" aria-hidden="true" style="display: none;">
	<div class="modal-dialog modal-md">
		<div class="modal-content">
			<div class="modal-header text-center">
				<button type="button" class="close hidden-xs" data-dismiss="modal">
					<i class="fa fa-times"></i>
				</button>
				<!-- 
				<button class="yt-btn pull-left btn-sm visible-xs btn-back"	type="button" data-dismiss="modal">
					<i class="fa fa-chevron-left"></i>
				</button>
				-->
				<h3 class="modal-title" id="savecardforOfflinePopupTitle" ><yatra:languageTag  content="Are you sure you want to allow this card for offline bookings" language="${language}"/>?</h3>
				<p id="savecardforOfflinePopupSubtitle" ><yatra:languageTag content="This will overwrite the already allowed card for Offline Bookings." language="${language}"/></h3>
		
			</div>
			<div class="modal-body save_card_offline">
				<center>
					<button confirm="yes" class="yt-btn btn-orange"><yatra:languageTag content="Yes" language="${language}"/></button>
					<button confirm="no" data-dismiss="modal" class="yt-btn btn-blue"><yatra:languageTag content="No" language="${language}"/></button>
				</center>
			</div>
		</div>
	</div>
</div>