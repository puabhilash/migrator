<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page isELIgnored="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<html>
<head>
<meta charset="ISO-8859-1">
	<link rel="icon" type="image/x-icon" href="<c:url value="/res/images/favicon.ico"/>" />
	<title>Alfresco Migrator <tiles:insertAttribute name="title" /></title>
</head>
<body>
	<tiles:insertAttribute name="header" />
	<tiles:insertAttribute name="body" />
    <%-- <tiles:insertAttribute name="footer" /> --%>
</body>
</html>