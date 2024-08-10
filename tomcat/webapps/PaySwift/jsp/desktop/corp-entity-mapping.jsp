<div id="product-div">
	<div id="productDropdown" class="product_dropdown">
	   <!--  <label for="" class="control-label product_label"><yatra:languageTag content="Select product(s) for which this card can be used" language="${language}"/></label>-->
	   <label class="control-label product_label">Select product(s) for which this card can be used</label>
	   <button type="button" id="product" class="multiselect_btn multiselect_product" title="Select Product">
	   <span>Select Product</span>
	   <span class="dropdown_arrow"></span>
	   </button>
	   <ul id="productlist" class="multiselect-container dropdown-menu-product">
		  <li>
			 <label class="checkbox">
			 <span class="custom-checkbox">
			 <input type="checkbox" value="DOM_FLIGHT" name="Domestic Flight">
			 <span class="box">
			 <b class="tick"></b>
			 </span>
			 </span>
			 Domestic Flight</label>
		  </li>
		  <li>
			 <label class="checkbox">
			 <span class="custom-checkbox">
			 <input type="checkbox" value="INT_FLIGHT" name="International Flight">
			 <span class="box">
			 <b class="tick"></b>
			 </span>
			 </span>
			 International Flight</label>
		  </li>
		  <li class="hideForPcc">
			 <label class="checkbox">
			 <span class="custom-checkbox">
			 <input type="checkbox" value="DOM_HOTEL" name="Domestic Hotel">
			 <span class="box">
			 <b class="tick"></b>
			 </span>
			 </span>
			 Domestic Hotel</label>
		  </li>
		  <li  class="hideForPcc">
			 <label class="checkbox">
			 <span class="custom-checkbox">
			 <input type="checkbox" value="INT_HOTEL" name="International Hotel">
			 <span class="box">
			 <b class="tick"></b>
			 </span>
			 </span>
			 International Hotel</label>
		  </li>
		  <li  class="hideForPcc">
			 <label class="checkbox">
			 <span class="custom-checkbox">
			 <input type="checkbox" value="OTHER" name="Others">
			 <span class="box">
			 <b class="tick"></b>
			 </span>
			 </span>
			 Others (Bus, Train, Visa, Insurance) </label>
		  </li>
	   </ul>
	</div>
	<div id="mappingDropdown" class="level_dropdown">
	   <!--  <label for="" class="control-label mapping_label"><yatra:languageTag content="Select user(s) level for card should be mapped" language="${language}"/></label>-->
	   <label class="control-label mapping_label">Select user(s) level allowed to use this card</label>
	   <button type="button" id="mappingLevel" class="multiselect_btn multiselect_mapping">
	   <span>Select Mapping Level</span>
	   </button>
	   <ul id="mappingLevel_List" class="multiselect-container dropdown-menu-mapping">
		  <li >
			 <label class="checkbox">
			 <span class="custom-checkbox">
			 <input type="checkbox" value="companyLevel" name="Company Level">
			 <span class="box">
			 <b class="tick"></b>
			 </span>
			 </span>
			 Company Level </label>
		  </li>
		  <li class="hideList">
			 <label class="checkbox">
			 <span class="custom-checkbox">
			 <input type="checkbox" value="entityGroup" name="Billing Entity Group">
			 <span class="box">
			 <b class="tick"></b>
			 </span>
			 </span>
			 Billing Entity Group </label>
		  </li>
		  <li class="hideList">
			 <label class="checkbox">
			 <span class="custom-checkbox">
			 <input type="checkbox" value="entity" name="Billing Entity">
			 <span class="box">
			 <b class="tick"></b>
			 </span>
			 </span>
			 Billing Entity </label>
		  </li>
		  <li>
			 <label class="checkbox">
			 <span class="custom-checkbox">
			 <input type="checkbox" value="user" name="Employee Level">
			 <span class="box">
			 <b class="tick"></b>
			 </span>
			 </span>
			 Employee Level </label>
		  </li>
	   </ul>
	</div>
	<div class="form-group" id="mapping-div">
	   <div id="corp_qb_bookingType_level_asuggest"  class="row companyLevel" style="display: none">
		  <div class="col-md-4 col-xs-12 entityGroup" style="display:none">
			 <div id="be-group-container" class="ui-front">
				<input type="text" id="input_be_group" class="form-control" autocomplete="off" placeholder="Select billing entity group"/>
			 </div>
			 <p id="no_begroup_id" style="display:none;">No Billing Entity Group Found</p>
			 <div id ="be-group-values-container" class="be-group-values-container" style="display:none">
				<p class="be-list-title"> Selected Entity Group</p>
				<ul id = "be-group-list-container" class="be-group-list-container">
				</ul>
			 </div>
		  </div>
		  <div class="col-md-4 col-xs-12 entity" style="display:none">
			 <div id="bentity-container" class="ui-front">
				<input type="text" id="input_be" class="form-control" autocomplete="off" placeholder="Select billing entity"/>
			 </div>
			 <p id="no_be_id" style="display:none;">No Billing Entity Found</p>
			 <div id="entity-values-container" class="be-group-values-container" style="display:none">
				<p class="be-list-title"> Selected Entity</p>
				<ul id ="be-list-container" class ="be-list-container">
				</ul>
			 </div>
		  </div>
		  <div class="col-md-4 col-xs-12 user" style="display:none">
			 <div id="user-container" class="ui-front">
				<input type="text" id="input_user" class="form-control" autocomplete="off" placeholder="Select Employee"/>
			 </div>
			 <p id="no_user_id" style="display:none;">No Employee Found</p>
			 <div id="user-values-container" class="be-group-values-container" style="display:none">
				<p class="be-list-title"> Selected Employee</p>
				<ul id ="user-list-container" class ="user-list-container">
				</ul>
			 </div>
		  </div>
	   </div>
	</div>
 </div>