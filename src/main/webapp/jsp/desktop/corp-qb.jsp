<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib uri="/WEB-INF/tld/yatraweb.tld" prefix="y" %>
<%@ page isELIgnored="false"%>

<y:purl var="js_prefix" key="js_prefix" def="https://secure.yatra.com" />
<y:purl var="css_prefix" key="css_prefix" def="https://secure.yatra.com" />

<!DOCTYPE html>
<html>
<head>
<script language="javascript"
	src="${js_prefix}/${static_content_url}/PaySwift/desktop/js/jquery.min.js"></script>
<script language="javascript"
	src="${js_prefix}/${static_content_url}/PaySwift/desktop/js/jquery-ui.js?ver=1"></script>

<script language="javascript"
	src="${js_prefix}/${static_content_url}/PaySwift/desktop/js/tools-responsive.js"></script>
<script type="text/javascript"
	src="${js_prefix}/${static_content_url}/PaySwift/desktop/js/lib/modal.js"></script>

<script type="text/javascript"
	src="${js_prefix}/${static_content_url}/PaySwift/desktop/js/CorporateQB.js?version=${staticResourceVersion}"></script>
<script type="text/javascript"
	src="${js_prefix}/${static_content_url}/PaySwift/desktop/js/mybooking-validation.js?version=${staticResourceVersion}"></script>

<link rel="stylesheet"
	href="${css_prefix}/${static_content_url}/PaySwift/desktop/css/bootstrap.css?version=${staticResourceVersion}">
<link rel="stylesheet"
	href="${css_prefix}/${static_content_url}/PaySwift/desktop/css/yt-my-booking.css?version=${staticResourceVersion}">
<!--<link rel="stylesheet"
	href="${css_prefix}/${static_content_url}/PaySwift/desktop/css/jquery-ui.css?version=${staticResourceVersion}"> -->
<link rel="stylesheet"
	href="${css_prefix}/${static_content_url}/PaySwift/desktop/css/bootstrap-ie7.css">
<style type="text/css">
.container .row {
	padding: 0;
}
.ui-helper-hidden-accessible{
	position: absolute;
	left : -999em;
}
.select ul.options {
	height: 250px;
	overflow-y: scroll;
}
</style>
<script type="text/javascript">
	var csrfToken = "${csrfToken}";
	var isAuthorized = "${isAuthorized}";
	var crypto = window.crypto || window.msCrypto;
	function addMyCustomController() {
		var script = document.createElement('script');
		script.src = "/content/responsive/resources/js/lib/jquery.mycustom.js";
		document.getElementsByTagName('head')[0].appendChild(script);
	}

    function uuidv4() {
      return ([1e7] + -1e3 + -4e3 + -8e3 + -1e11).replace(/[018]/g, function (c) {
        return (c ^ crypto.getRandomValues(new Uint8Array(1))[0] & 15 >> c / 4).toString(16);
      });
    }
    var tokenizationUrl = "${corpTokenizationUrl}";
    var showPccTab = "${showPccTab}";
    var clientId = "${clientId}";
    var corpUserId = "${corpUserId}";
    var corpUserRole = "${corpUserRole}";
    var userRole = "${userRole}";

    if(userRole == "EA"){
        $("#ea-user-container").show()
    }else{
        $("#ea-user-container").hide()
    }
</script>
</head>
<body>
	<div class='quickbook-header'>${frescoHeader}</div>
	<div class='quickbook-main'>
		<jsp:include page="corp-qb-home.jsp"></jsp:include>
		<jsp:include page="corp-add-card.jsp"></jsp:include>
		<jsp:include page="corp-saved-cards.jsp"></jsp:include>
		<jsp:include page="corp-edit-cards.jsp"></jsp:include>
		<jsp:include page="corp-reset-pass.jsp"></jsp:include>
		<jsp:include page="corp-not-logged-in.jsp"></jsp:include>
		<jsp:include page="corp-not-authorized.jsp"></jsp:include>
		<jsp:include page="corp-delete-card-popup.jsp"></jsp:include>
		<jsp:include page="corp-delete-card-confirm-popup.jsp"></jsp:include>
		<jsp:include page="corp-loading-popup.jsp"></jsp:include>
		<jsp:include page="corp-update-card-popup.jsp"></jsp:include>
		<jsp:include page="corp-savecardfor-offlinebooking-popup.jsp"></jsp:include>
		<jsp:include page="corp-listview-offlinebooking-popup.jsp"></jsp:include>
		<jsp:include page="corp-btaInPersonal-popup.jsp"></jsp:include>
	</div>
	<div>${frescoFooter}</div>
</body>
</html>