<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ page isELIgnored="false" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>Alfresco Migrator</title>
		<link rel="icon" type="image/x-icon" href="/migrator/res/images/favicon.ico">
		
		<link rel="stylesheet" type="text/css" href="/migrator/res/js/assets/skins/sam/datatable.css" />
		<link rel="stylesheet" type="text/css" href="/migrator/res/js/assets/skins/sam/menu.css" />
		<link rel="stylesheet" type="text/css" href="/migrator/res/js/assets/skins/sam/treeview.css" />
		<link rel="stylesheet" type="text/css" href="/migrator/res/js/treeview/assets/skins/sam/treeview.css" />
		<link rel="stylesheet" type="text/css" href="/migrator/res/js/assets/skins/sam/button.css" />
		<link rel="stylesheet" type="text/css" href="/migrator/res/js/button/assets/skins/sam/button.css" />
		<link rel="stylesheet" type="text/css" href="/migrator/res/js/button/assets/skins/sam/button-skin.css" />
		<link rel="stylesheet" type="text/css" href="/migrator/res/js/container/assets/skins/sam/container.css" />
		<link rel="stylesheet" type="text/css" href="/migrator/res/css/migration.css" />
		
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
		
		
		<script src="/migrator/res/migration.js"></script>
	</head>
	<body class="yui-skin-sam claro">
		<input type="hidden" id='urlcontext' value="${urlcontext}"/>
		<input type="hidden" id='hasalfdetails' value=""/>
		<input type="hidden" id='csvrecordid' value=""/>
		<div id="htmlbody">
		<div id="configtabview" class="yui-navset">
	    		<ul class="yui-nav">
	        		<li class="selected"><a href="#table"><em>CSV Migration</em></a></li>
	    		</ul> 
	    		<div class="yui-content">
	    			<center>
						<table cellspacing="6">
							<tr>
								<td>Has Include Column?</td>
								<td>:</td>
								<td>
									<input type="radio" id="yesinclude" name="hasinclude" >Yes</input><input type="radio" id="noinclude" name="hasinclude" checked="checked"/>No</input>
								</td>
								<td></td>
							</tr>
							<tr>
								<td>Select CSV</td>
								<td>:</td>
								<td>
									<input type="file" id="csvFile" accept=".csv,.xls,.xlsx"/>
								</td>
								<td></td>
							</tr>
							<tr>
								<td>Select Destination</td>
								<td>:</td>
								<td>
									<input type="text" id="pdfdestination" readonly="readonly" class="readonly textbox"/>
								</td>
								<td><div id="destinationBtn"></div></td>
							</tr>
							<tr>
								<td>Select Native Files Destination</td>
								<td>:</td>
								<td><input type="text" id="nativedestination" readonly="readonly" class="readonly textbox"/></td>
								<td>
									<div id="nativeDestinationBtn"></div>
								</td>
							</tr>
							<tr>
								<td>Source Location</td>
								<td>:</td>
								<td><input type="radio" id="local" name="locationSpecify" checked="checked">Local</input><input type="radio" id="amazons3" name="locationSpecify" />Amazon s3</input></td>
								<td></td>
							</tr>
							<tr>
								<td>Local PDF Files Location</td>
								<td>:</td>
								<td><input type="text" id="localpdflocation" class="textbox"/></td>
								<td></td>
							</tr>
							<tr>
								<td>Local Native Files Location</td>
								<td>:</td>
								<td><input type="text" id="localnativelocation" class="textbox"/></td>
								<td></td>
							</tr>
							<tr>
								<td>Is Latest?</td>
								<td>:</td>
								<td align="left"><input type="checkbox" id="islatest" checked/></td>
								<td></td>
							</tr>
							<tr>
								<td>Metadata Template</td>
								<td>:</td>
								<td align="left">
									<select id="metadatatemplate">
										<option value="">Select Template</option>
										${template}
									</select>
									&nbsp;&nbsp; <img class="hidden viewicon" id="metadatatemplate-img" src="/migrator/res/images/details.png" style="width:20px;height:15px;" title="View Template"/>
								</td>
								<td></td>
							</tr>
							<tr>
								<td>Native Metadata Template</td>
								<td>:</td>
								<td align="left">
									<select id="nativemetadatatemplate"> 
										<option value="">Select Native Template</option>
										${template}
									</select>
									&nbsp;&nbsp; <img class="hidden viewicon" id="nativemetadatatemplate-img" src="/migrator/res/images/details.png" style="width:20px;height:15px;" title="View Template"/>
								</td>
								<td></td>
							</tr>
							<tr>
								<td colspan="2" align="right"><div id="migrateBtn" style="outline:none !important;"></div></td>
								<td colspan="2" align="left"><div id="resetBtn"></div>&nbsp;</td>
							</tr>
							
						</table> 
						<table id="loadingCtrl" class="hidden">
							<!-- <tr align="center"><td><div class="loading" ></div></td></tr>-->
							<tr align="center"><td><img src="/migrator/res/images/alfresco-logo-vector-1.svg" class="circle"/></td></tr>
							<tr align="center"><td>Please wait while migrating documents. Do not refresh page until migration finished</td></tr>
						</table>
						<table id="messageTable" class="hidden"><tr><td><div class="statusmessage"></div></td><td><div class="messagelabel">Migration Finished</div><td></td></tr></table>
						<table id="messageFailedTable" class="hidden"><tr><td><div class="failedmessage"></div></td><td><div class="messagelabel">Migration Failed</div><td></td></tr></table>
						<table id="progresstable" class="hidden">
							<tr>
								<td>0</td>
								<td>
									<div id="progressbar" ></div>
								</td>
								<td>100</td>
							</tr>
							<tr align="center"><td colspan="3"><span id="percentage">0%</span></td></tr>
						</table>
						<table>
							<tr>
								<td colspan="3">
									
								</td>
							</tr>
							<tr>
								<td><b>Total Records</b></td>
								<td>:</td>
								<td><span id="totalRecords"></span></td>
							</tr>
							<tr>
								<td><b>Remaining</b></td>
								<td>:</td>
								<td><span id="remaining"></span></td>
							</tr>
							<tr>
								<td><b>Time Taken</b></td>
								<td>:</td>
								<td><span id="timetaken"></span></td>
							</tr>
						</table>
						<div id="statustable"></div>
						</center>
	    		</div> 
	    </div>
		</div>
		
		<div id="loadingPanel"></div>
		<script type="text/javascript">//<![CDATA[
			new Migrator.component.Migrate().setOptions({
				"urlcontext":"${urlcontext}"
			});
		//]]></script>
	</body>
</html>