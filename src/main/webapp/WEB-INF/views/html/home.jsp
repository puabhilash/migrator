<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ page isELIgnored="false" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<title>Home</title>
	<link rel="icon" type="image/x-icon" href="<c:url value="/res/images/favicon.ico"/>" />
	<link rel="stylesheet" type="text/css" href="<c:url value="/res/js/assets/skins/sam/menu.css"/>"/>
	<link rel="stylesheet" type="text/css" href="<c:url value="/res/js/assets/skins/sam/treeview.css"/>" />
	<link rel="stylesheet" type="text/css" href="<c:url value="/res/js/assets/skins/sam/button.css"/>" />
	<link rel="stylesheet" type="text/css" href="<c:url value="/res/js/button/assets/skins/sam/button.css"/>" />
	<link rel="stylesheet" type="text/css" href="<c:url value="/res/js/button/assets/skins/sam/button-skin.css"/>" />
	<link rel="stylesheet" type="text/css" href="<c:url value="/res/js/container/assets/skins/sam/container.css"/>" />
	<link rel="stylesheet" type="text/css" href="<c:url value="/res/css/home.css"/>" />
	
	<script type="text/javascript" src="<c:url value="/res/js/yahoo-dom-event/yahoo-dom-event.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/res/js/event/event-min.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/res/js/element/element-min.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/res/js/container/container-min.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/res/js/dragdrop/dragdrop-min.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/res/js/connection/connection-min.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/res/js/datatable/datatable-min.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/res/js/datasource/datasource-min.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/res/js/treeview/treeview-min.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/res/js/paginator/paginator-min.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/res/js/button/button-min.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/res/js/menu/menu-min.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/res/home.js"/>"></script>
</head>
<body class="yui-skin-sam" style="min-height: calc(100vh - 50px);">
		<input type="hidden" id='urlcontext' value="${urlcontext}"/>
		<input type="hidden" id='hasalfdetails' value=""/>
		<center>
			<div id="validationpanel" class="validationpanel">
			<table cellspacing="5">
				<tr>
					<td class='label'>Alfresco</td>
					<td>:</td>
					<td>
						<div id="alfrescoedit"></div>
					</td>
					<td>
						<div id="alfrescovalidate"></div>
					</td>
				</tr>
				<tr>
					<td class='label'>Active MQ</td>
					<td>:</td>
					<td class="hidden">
						<div id="activemqedit" ></div>
					</td>
					<td>
						<div id="activemqvalidate"></div>
					</td>
				</tr>
			</table>
			<table>
				<tr  align="center">
	        		<td colspan="3">
	      				<div id="savingDiv" class="hidden"><img class="circle" src="<c:url value="/res/images/alfresco-logo-vector-1.svg"/>"/>&nbsp;Saving Please Wait...</div>
	      			</td>
	      		</tr>
	      		<tr  align="center">
	      			<td colspan="3">
	      				<div id="validatingDiv" class="hidden"><img class="circle" src="<c:url value="/res/images/alfresco-logo-vector-1.svg"/>"/>&nbsp;validating Please Wait...</div>
	      			</td>
	      		</tr>
			</table>
			<table id="successSave" class="hidden">
				<tr>
					<td>
						<div class="wrapper"> <svg id="tickSave" class="" viewBox="0 0 24 24">
							<path d="M4.1 12.7L9 17.6 20.3 6.3" fill="none" /> </svg> 
						</div>
					</td>
					<td><span id="successMsg"></span></td>
				</tr>
			</table>
			<table id="successTable" class="hidden">
				<tr>
					<td>
						<div class="wrapper"> <svg id="tick" class="" viewBox="0 0 24 24">
							<path d="M4.1 12.7L9 17.6 20.3 6.3" fill="none" /> </svg> 
						</div>
					</td>
					<td><span id="successCtrl"></span></td>
				</tr>
			</table>
			<table id="failTable" class="hidden">
				<tr>
					<td>
						<div class="wrapper">
							<svg id="checsvg" class="" viewBox="0 0 52 52"><circle id="checkcircle" class="" cx="26" cy="26" r="25" fill="none"/><path class="checkmark_check" fill="none" d="M14.1 14.1l23.8 23.8 m0,-23.8 l-23.8,23.8"/></svg>
						</div>
					</td>
					<td>&nbsp;<span id="failMsg">Connection Success</span></td>
				</tr>
			</table>
			</div>
		</center>
		<div id="loadingPanel"></div>
		<script type="text/javascript">//<![CDATA[
			new Migrator.component.Home().setOptions({
				"urlcontext":"${urlcontext}",
				"hasalfrescodetails":""
			});
		//]]></script>
	</body>
</html>