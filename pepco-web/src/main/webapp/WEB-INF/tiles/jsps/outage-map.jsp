<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<h1>Outage Map</h1>
<script type="text/javascript" src="<c:url value="/resources/js/jquery.js"/>"></script>
<script type="text/javascript" src="<c:url value="/resources/js/date.js"/>"></script>
<script type="text/javascript" src="http://maps.googleapis.com/maps/api/js?sensor=true"></script>
<script src="<c:url value="/resources/js/outage-map.js"/>"></script>
<div id="outage-map" style="width:100%;height:100%;"></div>