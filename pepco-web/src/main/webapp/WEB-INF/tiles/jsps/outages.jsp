<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<h1>Active Outages</h1>
<p>As of <c:out value="${asof}"/></p>
<ul id="outages">
    <c:forEach items="${outages}" var="outageRevision">
        <li>
            <img src="<c:url value="http://maps.googleapis.com/maps/api/staticmap?size=100x100&zoom=14&sensor=false&markers=${outageRevision.outage.lat},${outageRevision.outage.lon}"/>"/>
            
        </li>
    </c:forEach>
</ul>