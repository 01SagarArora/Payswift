<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra"%>
<div class="row corp-qb-box" id="corp-qb-home" style="display: none;">
	<div class="col-md-7 col-centered">
		<div class="col-md-12">
			<ol class="qb-breadcrumb hidden-xs">
				<li class="active"></li>
			</ol>
		</div>

		<section class="col-md-12">
			<h3 class="mg-top mg-bottom hidden-xs">
				<yatra:languageTag content="Corporate QuickBook"
					language="${language}" />
			</h3>
			<p class="hidden-xs">
				<yatra:languageTag
					content="Start by adding credit/debit card. It's safe &amp; convenient!"
					language="${language}" />
			</p>

			<div class="clearfix"></div>

			<div class="panel panel-default clearfix text-md my-book">
				<div class="panel-heading visible-xs">
					<h3 class="panel-title">
						<yatra:languageTag content="QuickBook" language="${language}" />
					</h3>
				</div>
				<div class="panel-body pd-bottom">
					<div id="ea-user-container" style="display: none;">
					    <p>
                            <i class="ico-quickbook" style="display: inline-block;"></i>
                            <div class="col-md-4 col-xs-12 ea-user" style="float:right;margin-top:-40px">
                                 <div id="user-container" class="ui-front">
                                     <input type="text" id="input_user" class="form-control" autocomplete="off" placeholder="Select Employee"/>
                                 </div>
                                 <p id="no_user_id" style="display:none;">No Employee Found</p>
                                 <div id="ea-user-values-container" class="be-group-values-container" style="display:none">
                                     <p class="be-list-title">Selected Employee</p>
                                     <ul id="ea-user-list-container" class="ea-user-list-container"></ul>
                                 </div>
                            </div>
                        </p>

					</div>

					<p class="col-md-offset-1">
						<yatra:languageTag
							content="Making bookings on Yatra just got easier with QuickBook Test"
							language="${language}" />
						. <br />
						<yatra:languageTag
							content="Start by adding credit/debit card. It's safe & convenient!"
							language="${language}" />
					</p>
					<p class="col-md-offset-1">
						<em><yatra:languageTag
								content="Try it and be a Happy Traveller" language="${language}" />!</em>
					</p>

					<div class="col-md-offset-1 my-btn-group clearfix">
						<div class="col-md-4">
							<button class="yt-btn btn-blue btn-block"
								onclick="CorporateQB.showAddCardDiv()">
								<i class="fa fa-fw fa-plus"></i>
								<yatra:languageTag content="Add a Card" language="${language}" />
							</button>
						</div>

						<p class="visible-xs"></p>

						<div class="col-md-4">
							<button class="yt-btn btn-orange btn-block" id="view-card-btn"
								onclick="CorporateQB.showSavedCardsDiv()">
								<yatra:languageTag content="View saved cards"
									language="${language}" />
							</button>
						</div>
					</div>

					<div class="row my-book-info">
						<div class="col-md-6">
							<i class="fa fa-lock fa-3x"></i>
							<h5>
								<yatra:languageTag content="Guaranteed Safety"
									language="${language}" />
								:
							</h5>
							<p class="text-sm">
								<yatra:languageTag
									content="We store your card details in encrypted form that adhere to PCI DSS security standards"
									language="${language}" />
								.
							</p>
						</div>

						<div class="col-md-6">
							<i class="ico-bell"></i>
							<h5>
								<yatra:languageTag content="Faster Booking"
									language="${language}" />
								:
							</h5>
							<p class="text-sm">
								<yatra:languageTag
									content="Save your card once and avoid filling in	your card details repeatedly"
									language="${language}" />
								.
							</p>
						</div>
					</div>

					<div class="row my-book-logo">
						<span class="text-sm"><yatra:languageTag
								content="Secured by" language="${language}" />: </span> <i
							class="ico-gray-verisign"></i> <i class="ico-gray-mastercard"></i>
						<i class="ico-gray-safekey"></i> <i class="ico-gray-pcidss"></i> <i
							class="ico-gray-visa"></i>
					</div>
				</div>
			</div>
		</section>
	</div>
</div>
