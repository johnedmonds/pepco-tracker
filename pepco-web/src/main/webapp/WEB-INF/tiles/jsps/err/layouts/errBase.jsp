<%@taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
<title><tiles:getAsString name="title" /></title>
<link rel="stylesheet" href="<c:url value="/resources/css/err.css"/>" />
</head>
<body>
	<div id="header">
		<tiles:insertAttribute name="header" />
	</div>
	<tiles:insertAttribute name="menu" />
	<div id="body">
		<tiles:insertAttribute name="body" />
	</div>
	<div id="footer">
		<tiles:insertAttribute name="footer" />
	</div>
</body>
</html>
