<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page isELIgnored="false" %>
<!DOCTYPE html>
<html>
<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>Alfresco Migrator</title>
		<link rel="icon" type="image/x-icon" href="/migrator/res/images/favicon.ico">
		
		<link rel="stylesheet" type="text/css" href="/migrator/res/js/assets/skins/sam/datatable.css" />
		<link rel="stylesheet" type="text/css" href="/migrator/res/js/assets/skins/sam/menu.css" />
		<link rel="stylesheet" type="text/css" href="/migrator/res/js/assets/skins/sam/treeview.css" />
		<link rel="stylesheet" type="text/css" href="/migrator/res/js/assets/skins/sam/button.css" />
		<link rel="stylesheet" type="text/css" href="/migrator/res/js/button/assets/skins/sam/button.css" />
		<link rel="stylesheet" type="text/css" href="/migrator/res/js/button/assets/skins/sam/button-skin.css" />
		<link rel="stylesheet" type="text/css" href="/migrator/res/js/container/assets/skins/sam/container.css" />
		<link rel="stylesheet" type="text/css" href="/migrator/res/css/templates.css" />
		
		<script type="text/javascript" src="/migrator/res/js/yahoo-dom-event/yahoo-dom-event.js"></script>
		<script type="text/javascript" src="/migrator/res/js/event/event-min.js"></script>
		<script type="text/javascript" src="/migrator/res/js/element/element-min.js"></script>
		<script type="text/javascript" src="/migrator/res/js/container/container-min.js"></script>
		<script type="text/javascript" src="/migrator/res/js/dragdrop/dragdrop-min.js"></script>
		<script type="text/javascript" src="/migrator/res/js/connection/connection-min.js"></script>
		<script type="text/javascript" src="/migrator/res/js/datatable/datatable-min.js"></script>
		<script type="text/javascript" src="/migrator/res/js/datasource/datasource-min.js"></script>
		<script type="text/javascript" src="/migrator/res/js/treeview/treeview-min.js"></script>
		<script type="text/javascript" src="/migrator/res/js/paginator/paginator-min.js"></script>
		<script type="text/javascript" src="/migrator/res/js/button/button-min.js"></script>
		<script type="text/javascript" src="/migrator/res/js/menu/menu-min.js"></script>
		
		
		<script src="/migrator/res/templates.js"></script>
		
	</head>
	<body class="yui-skin-sam">
		<input type="hidden" id='urlcontext' value="${urlcontext}"/>
		<input type="hidden" id='hasalfdetails' value=""/>
		<center>
			<div class="templatepanel">
			<table cellpadding="5">
				<tr>
					<td>Select Template</td>
					<td>:</td>
					<td>
						<select id="templateSelect" size="5">
							<options>
								<option value="">Select Template</option>
								[(${template})]
							</options>
						</select>
					</td>
				</tr>
				<tr>
					<td>Template Name</td>
					<td>:</td>
					<td>
						<span id="templateName"></span>
					</td>
				</tr>
				<tr>
					<td>Alfresco Type</td>
					<td>:</td>
					<td>
						<span id="alfType"></span>
					</td>
				</tr>
				<tr>
					<td>File Column Name</td>
					<td>:</td>
					<td>
						<span id="filecolumn"></span>
					</td>
				</tr>
			</table>
			<div id="templateDetailsTable"></div>
			</div>
		</center>
		<div id="loadingPanel"></div>
		<script type="text/javascript">//<![CDATA[
			new Migrator.component.templates();
		//]]></script>
	</body>
</html>