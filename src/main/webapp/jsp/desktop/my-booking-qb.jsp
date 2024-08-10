<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="/WEB-INF/tld/yatraweb.tld" prefix="y" %>
<%@ page isELIgnored="false"%>

<y:purl var="js_prefix" key="js_prefix" def="https://secure.yatra.com" />
<y:purl var="css_prefix" key="css_prefix" def="https://secure.yatra.com" />

<script language="javascript" src="${js_prefix}/${static_content_url}/PaySwift/desktop/js/jquery.min.js"></script>
<script language="javascript" src="${js_prefix}/${static_content_url}/PaySwift/desktop/js/tools-responsive.js"></script>
<script type="text/javascript" src="${js_prefix}/${static_content_url}/PaySwift/desktop/js/lib/modal.js"></script>

<script type="text/javascript" src="${js_prefix}/${static_content_url}/PaySwift/desktop/js/mybookingQB.js?version=${staticResourceVersion}"></script>
<script type="text/javascript" src="${js_prefix}/${static_content_url}/PaySwift/desktop/js/mybooking-validation.js?version=${staticResourceVersion}"></script>

<link rel="stylesheet" href="${css_prefix}/${static_content_url}/PaySwift/desktop/css/bootstrap.css?version=${staticResourceVersion}">
<link rel="stylesheet" href="${css_prefix}/${static_content_url}/PaySwift/desktop/css/yt-my-booking.css?version=${staticResourceVersion}">
<link rel="stylesheet" href="${css_prefix}/${static_content_url}/PaySwift/desktop/css/bootstrap-ie7.css">

<script type="text/javascript">
	var csrfToken = "${csrfToken}";
	function addMyCustomController(){
		var script = document.createElement('script');
		script.src = "/content/responsive/resources/js/lib/jquery.mycustom.js";
		document.getElementsByTagName('head')[0].appendChild(script);
	}
</script>
<%-- <html>
	<head>
	</head>
	<body>
<div  class='quickbook-header' id = "frescoHeader">${frescoHeader}</div>
<div class='quickbook-main'> --%>
<jsp:include page="my-booking-qb-home.jsp"></jsp:include>
<jsp:include page="my-booking-add-card.jsp"></jsp:include>
<jsp:include page="my-booking-saved-cards.jsp"></jsp:include>
<jsp:include page="my-booking-reset-pass.jsp"></jsp:include>
<jsp:include page="my-booking-not-logged-in.jsp"></jsp:include>
<jsp:include page="my-booking-delete-card-popup.jsp"></jsp:include>
<%--</div>
<div id="frescoFooter" >${frescoFooter}</div>
</body>
</html> --%>
<style type="text/css">
.container .row{
	padding: 0;
}
.select ul.options{
	height: 250px;
    overflow-y: scroll;
}
</style>