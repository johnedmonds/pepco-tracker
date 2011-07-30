<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<ul id="menu">
	<li <tiles:getAsString name="home.selected"/>><a
		href="<c:url value="/"/>">Home</a></li>
	<li <tiles:getAsString name="about.selected"/>><a
		href="<c:url value="/about"/>">About</a></li>
	<li><a href="<c:url value="/contact"/>">Contact</a>
	</li>
</ul>