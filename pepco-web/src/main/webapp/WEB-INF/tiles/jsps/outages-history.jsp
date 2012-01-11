<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<h1>Outage History</h1>
<img alt="Mini-map of outage at ${outage.lat},${outage.lon}" src="<c:url value="http://maps.googleapis.com/maps/api/staticmap?size=100x100&amp;zoom=14&amp;sensor=false&amp;markers=${outage.lat},${outage.lon}"/>"/>

<c:choose>
    <c:when test="${not (outage.observedEnd eq null)}"><p id="outageResolutionStatus" class="resolved">Outage was resolved by <fmt:formatDate pattern="yyyy-MM-dd hh:mm:ss a" value="${outage.observedEnd}"/>.</p></c:when>
    <c:otherwise><p id="outageResolutionStatus" class="unresolved">Outage is still unresolved.</p></c:otherwise>
</c:choose>
<table id="outageRevisions">
    <thead><tr><th>As Of</th><th>Customers Affected</th><th>Estimated Restoration</th><th>Cause</th><th>Status</th></tr></thead>
    <tbody>
        <c:forEach items="${outageRevisions}" var="revision" varStatus="status">
            <tr>
                <td>${revision.run.asof}</td>
                <c:choose>
                    <c:when test="${status.first}">
                        <td>${revision.numCustomersAffected==0 ? '1-5' : revision.numCustomersAffected}</td>
                        <td>${revision.estimatedRestoration}</td>
                        <td>${revision.cause}</td>
                        <td>${revision.status}</td>
                    </c:when>
                    <c:otherwise>
                        <td ${outageRevisions[status.index-1].numCustomersAffected!=revision.numCustomersAffected ? 'class="changed"' : ''}>${revision.numCustomersAffected==0 ? '1-5' : revision.numCustomersAffected}</td>
                        <td ${not (outageRevisions[status.index - 1].estimatedRestoration eq revision.estimatedRestoration) ? 'class="changed"' : ''}>${revision.estimatedRestoration}</td>
                        <td ${not (outageRevisions[status.index - 1].cause eq revision.cause) ? 'class="changed"' : ''}>${revision.cause}</td>
                        <td ${not (outageRevisions[status.index - 1].status eq revision.status) ? 'class="changed"' : ''}>${revision.status}</td>
                    </c:otherwise>
                </c:choose>
            </tr>
        </c:forEach>
    </tbody>
</table>
