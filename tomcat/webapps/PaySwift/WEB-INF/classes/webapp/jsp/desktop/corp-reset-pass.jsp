<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>
<div class="corp-qb-box row" id="corp-reset-pass" style="display:none;">
	<div class="col-md-7 col-centered">
		<section class="col-md-12">
			<h3 class="mg-top mg-bottom hidden-xs"><yatra:languageTag content="Corporate QuickBook" language="${language}"/></h3>
			<div class="clearfix"></div>
			<div class="panel panel-default clearfix text-md my-book">

				<div class="panel-heading">
					<h3 class="panel-title"><yatra:languageTag content="Change Your Password" language="${language}"/></h3>
				</div>
				<div class="panel-body pd-bottom">

					<form name="corp_qb_reset_pass_form" id = "corp_qb_reset_pass_form_id" action="/PaySwift/update-password.htm" class="yt-form" role="form" autocomplete="off">

						<div class="form-group">
							<p><yatra:languageTag content="As an added security measure, we request you to change password" language="${language}"/>.</p>
							<hr class="dotted">
						</div>

						<div class="form-group">
							<label for="name" class="control-label"><yatra:languageTag content="Enter Current Password" language="${language}"/></label>
							<div class="row">
								<div class="col-md-6">
									<input type="password" name = "oldPassword" id = "qb_oldPassword_id" class="form-control" placholder="" data-validation = "required" data-msginfo = "* required">
								</div>
								<div class="col-md-6">
									<a href="https://secure.yatra.com/social/common/yatra/forgotpassword"><yatra:languageTag content="Forgot Password" language="${language}"/>?</a>
								</div>
							</div>
						</div>

						<div class="form-group">
							<label for="name" class="control-label"><yatra:languageTag content="Enter New Password" language="${language}"/></label>
							<div class="row">
								<div class="col-md-6">
									<input type="password" name = "newPassword" id = "qb_newPassword_id" class="form-control" placholder="" data-validation = "required|oldPassCheck|lengthCheck" data-msginfo = "* required|New and old passwords cannot be same|Your password must include a number, a lower case letter and at least 8 characters">
								</div>
							</div>
						</div>

						<div class="form-group">
							<label for="name" class="control-label"><yatra:languageTag content="Confirm New Password" language="${language}"/></label>
							<div class="row">
								<div class="col-md-6">
									<input type="password" name = "confrmPassword" id = "qb_confirmPassword_id" class="form-control" placholder="" data-validation = "required|resetPassCheck|lengthCheck" data-msginfo = "* required|New and confirm passwords do not match|Your password must include a number, a lower case letter and at least 8 characters">
								</div>
							</div>
						</div>

						<div class="form-group row">
							<div class="col-md-4">
								<button type="button" class="yt-btn btn-block text-bold" id = "qb_reset_pass_btn_id"><yatra:languageTag content="Change Password" language="${language}"/></button>
							</div>
						</div>

						<div id = "reset_pass_error_msg" class = "qb_error_msg" style ="margin-bottom:10px; color:red; display:none;"><yatra:languageTag content="Your password could not be reset. Please retry" language="${language}"/>.</div>
					</form>
				</div>
			</div>
		</section>
	</div>
</div>
