<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>
<div id="my-booking-qb-home" style="display:none;">
	<div class="col-md-10">
		<ol class="qb-breadcrumb hidden-xs">
			<li class="active"></li>
		</ol>
	</div>

	<section class="col-md-7">
		<h3 class="mg-top mg-bottom hidden-xs"><yatra:languageTag content="QuickBook" language="${language}"/></h3>
		<p class="hidden-xs"><yatra:languageTag content="Start by adding credit/debit card. It's safe &amp; convenient!" language="${language}"/></p>

		<div class="clearfix"></div>

		<div class="panel panel-default clearfix text-md my-book">
			<div class="panel-heading visible-xs">
				<h3 class="panel-title"><yatra:languageTag content="QuickBook" language="${language}"/></h3>
			</div>
			<div class="panel-body pd-bottom">
				<p><i class="ico-quickbook"></i></p>
				<p class="col-md-offset-1">
					<yatra:languageTag content="Making bookings on Yatra just got easier with QuickBook" language="${language}"/>. <br/><yatra:languageTag content="Start by adding credit/debit card. It's safe & convenient!" language="${language}"/>
				</p>
				<p class="col-md-offset-1">
					<em><yatra:languageTag content="Try it and be a Happy Traveller" language="${language}"/>!</em>
				</p>

				<div class="col-md-offset-1 my-btn-group clearfix">
					<div class="col-md-4">
						<button class="yt-btn btn-blue btn-block" onclick="MyBookingQB.showAddCardDiv()">
							<i class="fa fa-fw fa-plus"></i><yatra:languageTag content="Add a Card" language="${language}"/> 
						</button>
					</div>
				
					<p class="visible-xs"></p>
				
					<div class="col-md-4">
						<button class="yt-btn btn-orange btn-block" id="view-card-btn" onclick="MyBookingQB.showSavedCardsDiv()"><yatra:languageTag content="View saved cards" language="${language}"/></button>
					</div>
				</div>

				<div class="row my-book-info">
					<div class="col-md-6">
						<i class="fa fa-lock fa-3x"></i>
						<h5><yatra:languageTag content="Guaranteed Safety" language="${language}"/>:</h5>
						<p class="text-sm"><yatra:languageTag content="We store your card details in encrypted form that adhere to PCI DSS security standards" language="${language}"/>.</p>
					</div>
				
					<div class="col-md-6">
						<i class="ico-bell"></i>
						<h5><yatra:languageTag content="Faster Booking" language="${language}"/>:</h5>
						<p class="text-sm"><yatra:languageTag content="Save your card once and avoid filling in	your card details repeatedly" language="${language}"/>.</p>
					</div>
				</div>
				
				<div class="row my-book-logo">
					<span class="text-sm"><yatra:languageTag content="Secured by" language="${language}"/>: </span> 
					<i class="ico-gray-verisign"></i>
					<i class="ico-gray-mastercard"></i>
					<i class="ico-gray-safekey"></i> 
					<i class="ico-gray-pcidss"></i> 
					<i class="ico-gray-visa"></i>
				</div>
			</div>
		</div>
	</section>
</div>
