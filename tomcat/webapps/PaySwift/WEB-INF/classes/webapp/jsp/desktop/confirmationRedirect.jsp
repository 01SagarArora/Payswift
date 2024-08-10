<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<!DOCTYPE html>
<html>
    <head>
    <title>Payment Page</title>
    </head>

    <body onLoad="document.redirectForm.submit()">
        <form name="redirectForm" action="<c:out value="${confirmationUrl}" />" method="post" >
            <table align="center" border="0" cellpadding='0' cellspacing='0'>
                <c:forEach items="${fieldMap}" var="mapEntry">
                    <tr>
                        <td><input type="hidden" name="${mapEntry.key}" value="<c:out value="${mapEntry.value}" />"></td>
                    </tr>
        	    </c:forEach>
            </table>
        </form>
    </body>
</html>