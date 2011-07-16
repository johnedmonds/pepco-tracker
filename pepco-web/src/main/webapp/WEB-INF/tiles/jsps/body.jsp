<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page isELIgnored="false"%>
<h1>Outage Summary</h1>
<dl>
	<dt>Total Outages</dt>
	<dd>
		<c:out value="${request}" />
		<c:out value="${totalOutages}" />
	</dd>
	<dt>Affected Customers</dt>
	<dd>
		<dl>
			<dt>DC</dt>
			<dd>
				<c:out value="${dcCust}" />
			</dd>
			<dt>Prince George</dt>
			<dd>
				<c:out value="${pgCust}" />
			</dd>
			<dt>Montgomery</dt>
			<dd>
				<c:out value="${pgCust}" />
			</dd>
			<dt>Total</dt>
			<dd>
				<c:out value="${dcCust+pgCust+montCust}" />
			</dd>
		</dl>
	</dd>
</dl>