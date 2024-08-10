<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>
<div class="modal fade yt-modal" id="delete-popup-id" tabindex="-1" role="dialog" aria-labelledby="mySmallModalLabel" aria-hidden="true" style="display: none;">
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
				<h3 class="modal-title"><yatra:languageTag content="Are you sure you want to delete your card" language="${language}"/>?</h3>
			</div>
			<div class="modal-body delete_bts">
				<center>
					<button confirm="yes" class="yt-btn btn-orange"><yatra:languageTag content="Yes" language="${language}"/></button>
					<button confirm="no" class="yt-btn btn-blue"><yatra:languageTag content="No" language="${language}"/></button>
				</center>
			</div>
		</div>
	</div>
</div>