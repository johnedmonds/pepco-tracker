<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<h1>Active Outages</h1>
<p>
<form method="get" action="<c:url value="/outages"/>">
    <label for="outagesAsof">As of</label> <input type="text" id="outagesAsof" name="asof" value="<fmt:formatDate value="${asof}" pattern="yyyy-MM-dd HH:mm:ss"/>"/>
    <input type="submit" name="submit" value="Update"/>
</form>
</p>
<ul id="outages">
    <c:forEach items="${outages}" var="outageRevision">
        <li>
            <img alt="Mini-map of outage at ${outageRevision.outage.lat},${outageRevision.outage.lon}" src="<c:url value="http://maps.googleapis.com/maps/api/staticmap?size=100x100&zoom=14&sensor=false&markers=${outageRevision.outage.lat},${outageRevision.outage.lon}"/>"/>
            <dl>
                <dt>Customers Affected:</dt><dd><c:choose><c:when test="${outageRevision.numCustomersAffected eq 0}">1-5</c:when><c:otherwise>${outageRevision.numCustomersAffected}</c:otherwise></c:choose></dd>
                <dt>Estimated Restoration:</dt><dd><fmt:formatDate value="${outageRevision.estimatedRestoration}" pattern="yyyy-MM-dd h:mm:ss a"/></dd>
                <dt>Cause:</dt><dd><c:out value="${outageRevision.cause}"/></dd>
                <dt>Status:</dt><dd><c:out value="${outageRevision.status}"/></dd>
            </dl>
            <a href="<c:url value="/outages/${outageRevision.outage.id}"/>">History</a>
        </li>
    </c:forEach>
</ul>