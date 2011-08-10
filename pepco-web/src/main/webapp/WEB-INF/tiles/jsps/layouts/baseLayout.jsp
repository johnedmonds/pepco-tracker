<%@taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN"
   "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
<title><tiles:getAsString name="title" />
</title>
<link rel="stylesheet" href="<c:url value="/resources/css/main.css"/>" >
<link rel="stylesheet" href="<c:url value="/resources/css/menu.css"/>" >
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
