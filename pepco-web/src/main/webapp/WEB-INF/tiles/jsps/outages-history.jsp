<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<h1>Outage History</h1>
<img src="<c:url value="http://maps.googleapis.com/maps/api/staticmap?size=100x100&zoom=14&sensor=false&markers=${outage.lat},${outage.lon}"/>"/>