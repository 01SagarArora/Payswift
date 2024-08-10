<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>
<style>
   .autocomplete-render{
   background-color : white;
   box-shadow: 1px 1px  1px 1px rgba(0,0,0,0.4);
   list-style: none;
   width:100%;
   }
</style>
<div class="corp-qb-box row" id="corp-add-card" style="display:none;">
   <div class="col-md-7 col-centered">
      <div class="col-md-12">
         <ol class="qb-breadcrumb hidden-xs">
            <li>
               <a href="#" class="show-corp-qb-home">
                  <yatra:languageTag content="Corporate QuickBook" language="${language}"/>
               </a>
            </li>
            <li class="active">
               <yatra:languageTag content="Add a Card" language="${language}"/>
            </li>
         </ol>
      </div>
      <section class="col-md-12">
         <h3 class="mg-top mg-bottom hidden-xs heading-bold">
            <yatra:languageTag content="QuickBook" language="${language}"/>
         </h3>
         <p class="hidden-xs">
            <yatra:languageTag content="Start by adding credit/debit card. It's safe &amp; convenient!" language="${language}"/>
         </p>
                  <p class="hidden-xs" id="selected-ea-user-label"></p>

         <div class="clearfix"></div>
         <div class="panel panel-default clearfix text-md my-book">
            <div class="panel-heading">
               <h3 class="panel-title heading-bold">
                  <yatra:languageTag content="Enter Card Details" language="${language}"/>
               </h3>
            </div>
             <div class="panel-heading" id="not-authorized" style="display:none;">
                   <h4 class="panel-title heading-bold">
                      <yatra:languageTag content="You are not authorized to add cards." language="${language}"/>
                   </h4>
                </div>
            <div class="panel-body pd-bottom" id = "add_card_div_id">
               <div class="btn-group pull-left btn-group-xs table js-card-type">
                  <script type="text/javascript">
                     var showCorpCardsTab = ${showCorpCardsTab};
                  </script>
                  <button class="yt-btn btn-tab btn-tab-small heading-bold" id = "qb_corporate_tab_id" card_type = "CORPORATE" qb-type = "CORPORATE">
                     <yatra:languageTag content="Corporate Card" language="${language}"/>
                  </button>
                  <button class="yt-btn btn-tab btn-tab-small heading-bold" id = "qb_bta_tab_id" card_type = "BTA" qb-type = "CORPORATE">
                     <yatra:languageTag content="BTA Card" language="${language}"/>
                  </button>
                  <button class="yt-btn btn-tab btn-tab-small heading-bold" id = "qb_cta_tab_id" card_type = "CTA" qb-type = "CORPORATE">
                     <yatra:languageTag content="CTA Card" language="${language}"/>
                  </button>
                  <button class="yt-btn btn-tab btn-tab-small heading-bold" id = "qb_pcc_tab_id" card_type = "PCC" qb-type = "CORPORATE">
                     <yatra:languageTag content="Personal Corporate Card" language="${language}"/>
                  </button>
                  <button  class="yt-btn btn-tab btn-tab-small heading-bold" id = "qb_personal_tab_id" card_type = "CreditCard" qb-type = "PERSONAL">
                     <yatra:languageTag content="PCC" language="${language}"/>
                  </button>
               </div>
               <form name="corp_qb_add_card_form" id = "corp_qb_add_card_form_id" action="/PaySwift/corporate-quickbook/save-card.htm" class="yt-form" role="form">
                  <input type="hidden" id = "corp_qb_ctype_id" name="ctype" value=""/>
                  <input type="hidden" id = "corp_qb_qbtype_id" name="qbType" value=""/>
                  <input type="hidden" id = "corp_qb_cbrand_id" name="cbrand" value=""/>
                  <input type="hidden" id = "corp_qb_isCardInternational_id" name="isCardInternational" value = "false"/>
                  <input type="hidden" name="merchant" value="yatra"/>
                  <input type="hidden" name = "csrfToken" value="${csrfToken}"/>
                  <input type="hidden" name="product" value="mybookings"/>
                  <input type="hidden" id="add_card_random_uuid" name="uuid"  value =""/>

                <div id="pccOfficialBookingMsg" style="display:none;">
                    <span>This Card will only be Saved for official bookings.</span>
                </div>
				 <!-- <div id="cardIssuanceTypeDiv">
                    <span class="icLabel">Card Issuance Type:</span>
                    <span class="icGroup">
                        <span class="custom-Radiobox">
                            <input type="radio" name="cardIssuanceType" value="virtual"><span class="box"><b class="tick"></b></span>
                        </span>&nbsp;<span class="corp-qb-radio-text-edit">Virtual</span>

                        <span class="custom-Radiobox">
                            <input type="radio" name="cardIssuanceType" value="physical"><span class="box"><b class="tick"></b></span>
                        </span>&nbsp;<span class="corp-qb-radio-text-edit">Physical</span>
                    </span>
                 </div> -->
                  <div class="form-group" id = "corp_qb_bookingType" style="display: none;margin-bottom:40px">
                     </br>
                     <label for="" class="control-label">
                        <yatra:languageTag content="Save this Card for:" language="${language}"/>
                     </label>
                     </br>
                     <div>
                        <span class="custom-Radiobox"><input type="radio" name="bookingType"   value="OFFICIAL"><span class="box"><b class="tick"></b></span></span> <span class="corp-qb-radio-text-edit"> Official Bookings </span>
                        <span class="custom-Radiobox"><input type="radio" name="bookingType"  value="PERSONAL" ><span class="box"><b class="tick"></b></span></span> <span class="corp-qb-radio-text-edit" > Personal Bookings </span>
                        <span class="custom-Radiobox"><input type="radio" name="bookingType"  id="bookingType_both" value="ALL" data-validation = "required" data-msginfo = "* Please choose what type of bookings (personal / official) can this card be used for"><span class="box"><b class="tick"></b></span></span><span class="corp-qb-radio-text-edit"> Both </span>
                     </div>
                  </div>
                  <jsp:include page="corp-entity-mapping.jsp"></jsp:include>
                  <div class="form-group">
                     <label for="name" class="control-label">
                        <yatra:languageTag content="Card Holder's Name" language="${language}"/>
                        </br>
                        <small>
                           (
                           <yatra:languageTag content="Please enter the same name which is written on your card" language="${language}"/>
                           )
                        </small>
                     </label>
                     <div class="row">
                        <div class="col-md-5 col-xs-12">
                           <input type="text" class="form-control w270" id = "corp_qb_cardholder_name_id" autocomplete="off" name="cardholder_name" placeholder="" data-validation = "required|regType[alpha]" data-msginfo = "* Please enter cardholder's name|* Card name should have only alphabetic charcters"/>
                        </div>
                     </div>
                  </div>
                  <div class="form-group">
                     <label for="" class="control-label" id = "card_no_label_id">
                        <yatra:languageTag content="Credit Card Number" language="${language}"/>
                     </label>
                     <div class="row">
                        <div class="col-md-5 col-xs-12">
                           <input type="text" id= "qb_cno_id" name = "cno" placeholder="" class="form-control w270" autocomplete="off">
                        </div>
                        <div class="form-group col-xs-12 visible-xs"></div>
                        <div class="col-md-7 col-xs-12 card_logos">
                           <i class="ico-visa" id = "visa_logo_id"></i>
                           <i class="ico-mastercard" id = "master_logo_id"></i>
                           <i class="ico-americanexpress" id = "amex_logo_id"></i>
                           <i class="ico-dinnerclub" id = "diners_logo_id"></i>
                        </div>
                     </div>
                  </div>
                  <div class="form-group">
                     <div id = "maest_expiry_msg" class = "qb_error_msg" style ="margin-bottom:10px; color:red; display:none;">
                        <yatra:languageTag content="Expiry date and CVV are optional if not present on your card" language="${language}"/>
                        .
                     </div>
                     <label for="" class="control-label">
                        <yatra:languageTag content="Expiry Date" language="${language}"/>
                     </label>
                     <div class="row">
                        <div class="col-md-3 col-xs-4">
                           <select  name = "cexpm" id = "qb_expm_id" maxlength = "2">
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
                        <div class="col-md-3 col-xs-4">
                           <select  name = "cexpy" id = "qb_expy_id" maxlength = "4"></select>
                        </div>
                     </div>
                  </div>
                  <!-- Changes for CVV box -->
                  <div class="form-group  ccsc-label-box">
                     <label for="name" class="control-label">
                        <yatra:languageTag content="CVV" language="${language}"/>
                     </label>
                     <div class="row">
                        <div class="col-md-3 col-xs-4">
							<input type="password" id = "qb_ccsc" name = "ccsc" placeholder="CVV" class="form-control" maxlength = "4" autocomplete="off" data-validation="required" data-msginfo="* required|Enter valid cvv" />
                        </div>
                     </div>
                  </div>
                  <div class="form-group">
                     <label for="name" class="control-label">
                        <yatra:languageTag content="Name this Card" language="${language}"/>
                        <small>
                           (
                           <yatra:languageTag content="optional" language="${language}"/>
                           )
                        </small>
                     </label>
                     <div class="row">
                        <div class="col-md-6 col-xs-9">
                           <input type="text" id = "qb_cname_id" name = "cname" placeholder="eg. My HDFC Gold Card" class="form-control" autocomplete="off" />
                        </div>
                     </div>
                  </div>
                  <div class="form-group" id="allowOfflineCheckBoxContainer" style="display:none">
                     <div class="row">
                        <div class="col-md-6 col-xs-9">
                           <input type="checkbox" name="saveCardToGDS" id="saveCardToGDS"><label for="saveCardToGDS"  style="position: relative;top: -2px;left: 3px;"> Allow this card for offline booking</label>
                        </div>
                     </div>
                  </div>
                  <div class="form-group" id="btaInPersonalCheckBoxContainer" style="display:none">
                     <div class="row">
                        <div class="col-md-6 col-xs-9">
                           <input type="checkbox" name="saveAsCorpCard" id="saveAsCorpCard"><label for="saveAsCorpCard"  style="position: relative;top: -2px;left: 3px;"> Allow this card for BTA</label>
                        </div>
                     </div>
                  </div>
                  <div class="form-group" id = "amex_issue_div">
                     <div style ="margin-bottom:10px; display:block;">
                        <yatra:languageTag content="Where was your Amex card issued" language="${language}"/>
                        ?
                     </div>
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
                  <jsp:include page="corp-address.jsp"></jsp:include>

				  <div class="row" id="saveTokenConsentDiv">
					<div class="col-md-6 col-xs-9">
					   <span id="corpUserConsent"></span><label for="saveTokenConsent">&nbsp;&nbsp;You consent to save this card as per RBI guidelines</label>
					</div>
				 </div>

                  <div class="form-group row">
                     <div class="col-md-3">
                        <button type="button" class="yt-btn btn-block" id = "qb_add_card_btn_id">
                           <yatra:languageTag content="Tokenize Card" language="${language}"/>
                        </button>
                     </div>
                  </div>
                  <div id = "add_card_error_msg" class = "qb_error_msg" style ="margin-bottom:10px; color:red; display:none;">
                     <yatra:languageTag content="Your card could not be saved. Please retry" language="${language}"/>
                     .
                  </div>
                  <div id = "duplicate_card_error_msg" class = "qb_error_msg" style ="margin-bottom:10px; color:red; display:none;">
                     <yatra:languageTag content="Your card is already saved" language="${language}"/>
                     .
                  </div>
                  <p class="text-sm">
                     <yatra:languageTag content="Note" language="${language}"/>
                     :
                     <yatra:languageTag content="We tokenize your credit card details as per the latest RBI Guidelines." language="${language}"/>
                     .
                  </p>
               </form>
               <div class="row my-book-logo">
                  <span class="text-sm">
                     <yatra:languageTag content="Secured by" language="${language}"/>
                     :
                  </span>
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
</div>