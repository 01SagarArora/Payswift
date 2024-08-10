<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>
<div id="corp-qb-card-holder-address" style="display: none;">
	<p> <yatra:languageTag content="For cards issued outside India, card holder's billing address is mandatory" language="${language}"/>.</p>

	<div class="form-group">
		<label for="name" class="control-label"><yatra:languageTag content="Address" language="${language}"/></label>
		<div class="row">
			<div class="col-md-8">
				<input type="text" id = "qb_ba1_id" name = "ba1" class="form-control" placeholder="" data-validation = "required" data-msginfo = "* Please enter address"/>
			</div>
		</div>
	</div>

	<div class="form-group">
		<div class="row">
			<div class="col-md-6 col-xs-6">
				<label for="name" class="control-label"><yatra:languageTag content="City" language="${language}"/></label>
				<div class="row">
					<div class="col-md-12">
						<input type="text" id = "qb_bacy_id" name = "bacy" class="form-control" placeholder="" data-validation = "required|regType[onlyLetter]" data-msginfo = "* Please enter city|* alphabets only"/>
					</div>
				</div>
			</div>
			<div class="col-md-2 col-xs-6">
				<label for="name" class="control-label"><yatra:languageTag content="Zip Code" language="${language}"/></label>
				<div class="row">
					<div class="col-md-12 pd-left">
						<input type="text" id = "qb_bapi_id" name = "bapi" class="form-control" placeholder="" maxlength = "10" data-validation = "required" data-msginfo = "* Please enter zipcode" />
					</div>
				</div>
			</div>
		</div>
	</div>

	<div class="form-group">
		<label for="name" class="control-label"><yatra:languageTag content="State/Province" language="${language}"/></label>
		<div class="row">
			<div class="col-md-6 col-xs-9">
				<input type="text" id = "qb_bast_id" name = "bast" placeholder="" class="form-control" data-validation = "required|regType[onlyLetter]" data-msginfo = "* Please enter state/province|* alphabets only" />
			</div>
		</div>
	</div>

	<div class="form-group">
		<label for="name" class="control-label"><yatra:languageTag content="Country" language="${language}"/></label>
		<div class="row">
			<div class="col-md-6 col-xs-9">
				<select id="qb_bacu_id" name = "bacu" data-validation = "required" data-msginfo = "* Please select country">
		        	<%@ include file="countryList.jsp" %>
		        </select> 
			</div>
		</div>
	</div>
	
	<div class="form-group">
		<div class="row">
			<div class="col-md-3 col-xs-4">
				<label class="control-label" for="name"><yatra:languageTag content="ISD Code" language="${language}"/></label>
				<div class="row">
					<div class="col-md-12">
						<input type="text" data-validation="required|regType[number]" data-msginfo="* Please enter isd code|* numbers only" onkeypress='return event.charCode >= 48 && event.charCode <= 57' maxlength="6" placeholder="" class="form-control" name="baisd" id="qb_baisd_id" />
					</div>
				</div>
			</div>
			<div class="col-md-5 col-xs-6">
				<label class="control-label" for="name"><yatra:languageTag content="Mobile" language="${language}"/></label>
				<div class="row">
					<div class="col-md-12 pd-left">
						<input type="text" data-validation="required|regType[number]|internationalMobileRangeCheck" maxlength="17" data-msginfo="* Please enter mobile number|* Please enter a valid number|* Please enter a valid number" onkeypress='return event.charCode >= 48 && event.charCode <= 57' placeholder="" class="form-control" name="bamob" id="qb_bamob_id" />
					</div>
				</div>
			</div>
		</div>
	</div>
</div>