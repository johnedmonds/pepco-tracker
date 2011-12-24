<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<h1>Outage History</h1>
<img src="<c:url value="http://maps.googleapis.com/maps/api/staticmap?size=100x100&zoom=14&sensor=false&markers=${outage.lat},${outage.lon}"/>"/>
<ol>
    <c:forEach items="${outage.revisions}" var="revision" varStatus="status">
        <li>
            <p>At <fmt:formatDate value="${revision.run.asof}" pattern="yyyy-MM-dd H:mm:ss a"/></p>
            <dl>
                <dt>Customers Affected:</dt><dd><c:choose><c:when test="${revision.numCustomersAffected eq 0}">1-5</c:when><c:otherwise>${outageRevision.numCustomersAffected}</c:otherwise></c:choose></dd>
                <dt>Estimated Restoration:</dt><dd><fmt:formatDate value="${revision.estimatedRestoration}" pattern="yyyy-MM-dd h:mm:ss a"/></dd>
                <dt>Cause:</dt><dd><c:out value="${revision.cause}"/></dd>
                <dt>Status:</dt><dd><c:out value="${revision.status}"/></dd>
            </dl>
        </li>
    </c:forEach>
</ol>