<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>
<div id="my-booking-add-card" style="display:none;">
	<div class="col-md-10">
		<ol class="qb-breadcrumb hidden-xs">
			<li><a href="#" class="show-qb-home"><yatra:languageTag content="QuickBook" language="${language}"/></a></li>
			<li class="active"><yatra:languageTag content="Add a Card" language="${language}"/></li>
		</ol>
	</div>

	<section class="col-md-7">
		<h3 class="mg-top mg-bottom hidden-xs"><yatra:languageTag content="QuickBook" language="${language}"/></h3>
		<p class="hidden-xs"><yatra:languageTag content="Start by adding credit/debit card. It's safe &amp; convenient!" language="${language}"/></p>

		<div class="clearfix"></div>
		<div class="panel panel-default clearfix text-md my-book">
			<div class="panel-heading">
				<h3 class="panel-title"><yatra:languageTag content="Enter Card Details" language="${language}"/></h3>
			</div>
			<div class="panel-body pd-bottom" id = "add_card_div_id">
				
				<div class="btn-group pull-left btn-group-xs table js-card-type">
                		<button class="yt-btn btn-tab btn-tab-small" id = "qb_cc_tab_id" card_type = "cc"><yatra:languageTag content="Credit Card" language="${language}"/></button>
                		<button class="yt-btn btn-tab btn-selected btn-tab-small" id = "qb_dc_tab_id" card_type = "dc"><yatra:languageTag content="Debit Card" language="${language}"/></button>
            	</div>
				
				<form name="qb_add_card_form" id = "qb_add_card_form_id" action="/PaySwift/save-qb-card.htm" class="yt-form" role="form">
					
					<input type="hidden" id = "qb_ctype_id" name="ctype" value=""/>
					<input type="hidden" id = "qb_cbrand_id" name="cbrand" value=""/>
					<input type="hidden" id = "qb_isCardInternational_id" name="isCardInternational" value = "false"/>
					<input type="hidden" name="merchant" value="yatra"/>
					<input type="hidden" name = "csrfToken" value="${csrfToken}"/>
					<input type="hidden" name="product" value="mybookings"/>
					
					<div class="form-group">
						</br>
						<label for="name" class="control-label"><yatra:languageTag content="Cardholder's Name" language="${language}"/></br><small> (<yatra:languageTag content="Please enter the same name which is written on your card" language="${language}"/>)</small>
						</label>
						<div class="row">
							<div class="col-md-5 col-xs-12">
								<input type="text" class="form-control" id = "qb_cardholder_name_id" autocomplete="off" name="cardholder_name" placeholder="" data-validation = "required|regType[alpha]" data-msginfo = "* Please enter cardholder's name|* alphabets only"/>
							</div>
						</div>
					</div>

					<div class="form-group">
						<label for="" class="control-label" id = "card_no_label_id"><yatra:languageTag content="Credit Card Number" language="${language}"/></label>
						<div class="row">
							<div class="col-md-5 col-xs-12">
								<input type="text" id= "qb_cno_id" name = "cno" placeholder="" class="form-control" autocomplete="off">
							</div>
							<div class="form-group col-xs-12 visible-xs"></div>
							<div class="col-md-7 col-xs-12 card_logos">
								<i class="ico-visa" id = "visa_logo_id"></i>
								<i class="ico-mastercard" id = "master_logo_id"></i>
								<i class="ico-maestro" id = "maest_logo_id"></i>
								<i class="ico-americanexpress" id = "amex_logo_id"></i>
								<i class="ico-dinnerclub" id = "diners_logo_id"></i>
							</div>
						</div>
					</div>						
					
					<div class="form-group">
						<div id = "maest_expiry_msg" class = "qb_error_msg" style ="margin-bottom:10px; color:red; display:none;"><yatra:languageTag content="Expiry date and CVV are optional if not present on your card" language="${language}"/>.</div>
						<label for="" class="control-label"><yatra:languageTag content="Expiry Date" language="${language}"/></label>
						<div class="row">
							<div class="col-md-3 col-xs-5">
								<select name = "cexpm" id = "qb_expm_id" maxlength = "2">
									<option value = "">Month</option>
									<option value = "01">01</option>
									<option value = "02">02</option>
									<option value = "03">03</option>
									<option value = "04">04</option>
									<option value = "05">05</option>
									<option value = "06">06</option>
									<option value = "07">07</option>
									<option value = "08">08</option>
									<option value = "09">09</option>
									<option value = "10">10</option>
									<option value = "11">11</option>
									<option value = "12">12</option>
								</select>
							</div>
							<div class="col-md-2 col-xs-4">
								<select name = "cexpy" id = "qb_expy_id" maxlength = "4"></select>
							</div>
						</div>
					</div>
					
					<div class="form-group">
						<label for="name" class="control-label"><yatra:languageTag content="Cardholder Nick Name" language="${language}"/> <small>(<yatra:languageTag content="optional" language="${language}"/>)</small>
						</label>
						<div class="row">
							<div class="col-md-6 col-xs-9">
								<input type="text" id = "qb_cname_id" name = "cname" placeholder="eg. My HDFC Gold Card" class="form-control">
							</div>
						</div>
					</div>
					
					<div class="form-group" id = "amex_issue_div">
						<div style ="margin-bottom:10px; display:block;"><yatra:languageTag content="Where was your Amex card issued" language="${language}"/>?</div>
						<div class="row">
							<div class="col-md-2 col-xs-3">
								<input type="radio" value="dom" name="address_check_rdo">
								<yatra:languageTag content="India" language="${language}"/>
							</div>
							<div class="col-md-4 col-xs-6">
								<input type="radio" value="int" name="address_check_rdo">
								<yatra:languageTag content="Outside India" language="${language}"/>
							</div>
						</div>
					</div>

					<jsp:include page="my-booking-address.jsp"></jsp:include>

					<div class="form-group row">
						<div class="col-md-3">
							<button type="button" class="yt-btn btn-block" id = "qb_add_card_btn_id"><yatra:languageTag content="Save Card" language="${language}"/></button>
						</div>
					</div>
					
					<div id = "add_card_error_msg" class = "qb_error_msg" style ="margin-bottom:10px; color:red; display:none;"><yatra:languageTag content="Your card could not be saved. Please retry" language="${language}"/>.</div>
					
					<div id = "duplicate_card_error_msg" class = "qb_error_msg" style ="margin-bottom:10px; color:red; display:none;"><yatra:languageTag content="Your card is already saved" language="${language}"/>.</div>

					<p class="text-sm"><yatra:languageTag content="Note" language="${language}"/>: <yatra:languageTag content="We store your credit card details in
						encrypted form that adhere toPCI DSS security standards. We do
						not store the CVV number or 3Dsecure details" language="${language}"/>.</p>
				</form>

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