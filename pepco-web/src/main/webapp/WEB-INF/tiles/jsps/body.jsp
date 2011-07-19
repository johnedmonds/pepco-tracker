<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<h1>Outage Summary</h1>
<div id="outageSummary">
	<table>
		<tr>
			<td>Total Outages</td>
			<td><c:out value="${summary.totalOutages}" /></td>
		</tr>
		<tr>
			<td>Total Affected Customers</td>
			<td>${summary.dcAffectedCustomers+summary.pgAffectedCustomers+summary.montAffectedCustomers}/${summary.dcTotalCustomers+summary.pgTotalCustomers+summary.montTotalCustomers}</td>
		</tr>
		<tr>
			<td>DC Customers Affected Customers</td>
			<td>${summary.dcAffectedCustomers}/${summary.dcTotalCustomers}</td>
		</tr>
		<tr>
			<td>Prince George Affected Customers</td>
			<td>${summary.pgAffectedCustomers}/${summary.pgTotalCustomers}</td>
		</tr>
		<tr>
			<td>Montgomery Affected Customers</td>
			<td>${summary.montAffectedCustomers}/${summary.montTotalCustomers}</td>
		</tr>
	</table>
	<img alt="Pepco Reliability Statistics"
		src="<c:url value="/resources/img/generated/reliability.svg"/>">
</div>
