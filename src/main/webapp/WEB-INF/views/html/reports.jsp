<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ page isELIgnored="false"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Alfresco Migrator</title>
<link rel="icon" type="image/x-icon" href="/migrator/images/favicon.ico">


<link rel="stylesheet" type="text/css"
	href="/migrator/res/css/reports.css" />

<script src="/migrator/res/reports.js"></script>
<script src="/migrator/res/google/loader.js"></script>
<script type="text/javascript" src="/migrator/res/js/charts/charts.js"></script>

<script type="text/javascript">
	google.charts.load('current', {
		'packages' : [ 'corechart' ]
	});
</script>
</head>
<body class="yui-skin-sam">
	<input type="hidden" id='urlcontext' value="${urlcontext}" />
	<input type="hidden" id='hasalfdetails' value="" />
	<br />
	<div id="reportbody">
		<div id="configtabview" class="yui-navset">
			<ul class="yui-nav">
				<li class="selected"><a href="#table"><em>CSV Table
							Format</em></a></li>
				<li ><a href="#alfresco"><em>Alfresco</em></a></li>
			</ul>
			<div class="yui-content">
				<center>
					<div id="table">
						<table>
							<tr>
								<td>Select CSV</td>
								<td>:</td>
								<td><select id="csvctrl" size="5" style="width: 250px;">
										<option value="">Select CSV</option> 
										${csvoptions}
								</select></td>
							</tr>
						</table>
						<div class="filter">
							<table>
								<tr>
									<td><input type="radio" name="status" id="success"
										value="success">Success</input></td>
									<td><span id="successCount"></span></td>
									<td><input type="radio" name="status" id="failed"
										value="failed">Failed</input></td>
									<td><span id="failedCount"></span></td>
									<td><input type="button" name="status" id="clear" /></td>
								</tr>
							</table>
							<table>
								<tr>
									<td><img id="piechartctrl"
										src="/migrator/res/images/piechart.png"
										class="diagrams hidden" /></td>
									<td>
										<img id='deleteall' src='/migrator/res/images/trash.png' class="diagrams hidden" title='Delete All'/>
									</td>
									<td>
										<img id='deletecsv' src='/migrator/res/images/trash.png' class="diagrams hidden" title='Delete CSV'/>
									</td>
									<td>
										<img id='calculatetime' src='/migrator/res/images/calculate_time.png' class="diagrams hidden" title='Calculate Time Taken'/>
									</td>
									<td>
										<a id="downloadcsv" href="#" class="diagrams hidden"><img id='exportcsv' src='/migrator/res/images/csvexport.webp' class="diagrams hidden" title='Export CSV'/></a>
									</td>
									<td>
										<img id='reuploadfailed' src='/migrator/res/images/upload.jpg' class="diagrams hidden" title='Reupload Failed'/>
									</td>
								</tr>
							</table>
						</div>
						<br />
						<div id="reporttablepaginationone"></div>
						<br />
						<div id="reporttable"></div>
						<br />
						<div id="reporttablepaginationtwo"></div>
						<br/>
						<div id="reuploadtable" class="hidden"></div>
					</div>

				</center>
				<div id="alfresco">
					<center>
						<table cellpadding="5">
							<tr>
								<td>Select Folder</td>
								<td>:</td>
								<td><input type="text" readonly="readonly" id="folderName" /></td>
								<td>
									<div id="folderButton"></div>
								</td>
							</tr>
							<tr>
								<td></td>
								<td></td>
								<td><input type="hidden" readonly="readonly" id="folderRef" /></td>
								<td></td>
							</tr>
							<tr>
								<td colspan="3" align="center"><div id="loadDetails"></div></td>
								<td></td>
							</tr>
						</table>
						<div id="reconsileTable"></div>
					</center>
				</div>
			</div>
		</div>
		<div id="loadingPanel"></div>
	</div>
	<script type="text/javascript">
		//<![CDATA[
		new Migrator.component.reports().setOptions({
			"urlcontext" : "${urlcontext}"
		});
		//]]>
	</script>
</body>
</html>