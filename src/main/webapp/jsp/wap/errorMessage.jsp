<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="/WEB-INF/tld/yatraTagLib.tld" prefix="yatra" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
</head>
<body>
<script language="javascript">
	//wap-centralui.js	
	var d5errorMessageJSP = '<yatra:languageTag content="The date of travel should be at least 5 days from the date of booking in case of international credit cards. Please use some other mode of payment." language="${language}"/>';
	var intlWarningMessageJSP = '<yatra:languageTag content="International cards are not supported for this transaction. Please use a card issued in India, or a different mode of payment e.g. Net banking" language="${language}"/>';
	var intlerrorMessageJSP = '<yatra:languageTag content="This transaction cannot be completed using an international credit card. Please use a card issued in India, or a different mode of payment e.g. Net banking" language="${language}"/>';
	var amexWarningMessageJSP = '<yatra:languageTag content="We accept AMEX cards issued in India. If you have a AMEX card issued outside India, please use a different mode of payment e.g. Net Banking." language="${language}"/>';
	var emiIntlErrorMessageJSP = '<yatra:languageTag content="International credit cards are not supported in case of EMI payment option" language="${language}"/>';
	
	//wap-quickbook.js
	var qbAuthRequiredMessageJSP = '<yatra:languageTag content="For security reasons please signin again to proceed with" language="${language}"/>';
	var qbAuthFailedMessageJSP = '<yatra:languageTag content="Your Yatra login/authentication failed. Please re-try login or proceed with some other payment option." language="${language}"/>';
</script> 
</body>
</html>