<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<h1>Outage History</h1>
<img alt="Mini-map of outage at ${outage.lat},${outage.lon}" src="<c:url value="http://maps.googleapis.com/maps/api/staticmap?size=100x100&amp;zoom=14&amp;sensor=false&amp;markers=${outage.lat},${outage.lon}"/>"/>
<table id="outageRevisions">
    <thead><tr><th>As Of</th><th>Customers Affected</th><th>Estimated Restoration</th><th>Cause</th><th>Status</th></tr></thead>
    <tbody>
        <c:forEach items="${outage.revisions}" var="revision">
            <tr>
                <td>${revision.run.asof}</td>
                <td>${revision.numCustomersAffected}</td>
                <td>${revision.estimatedRestoration}</td>
                <td>${revision.cause}</td>
                <td>${revision.status}</td>
            </tr>
        </c:forEach>
    </tbody>
</table>
