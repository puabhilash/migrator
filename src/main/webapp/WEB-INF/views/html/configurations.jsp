<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page isELIgnored="false" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
	<link rel="icon" type="image/x-icon" href="/migrator/res/images/favicon.ico">
	<link rel="stylesheet" type="text/css" href="/migrator/res/js/assets/skins/sam/datatable.css" />
	<link rel="stylesheet" type="text/css" href="/migrator/res/js/assets/skins/sam/menu.css" />
	<link rel="stylesheet" type="text/css" href="/migrator/res/js/assets/skins/sam/treeview.css" />
	<link rel="stylesheet" type="text/css" href="/migrator/res/js/assets/skins/sam/button.css" />
	<link rel="stylesheet" type="text/css" href="/migrator/res/js/button/assets/skins/sam/button.css" />
	<link rel="stylesheet" type="text/css" href="/migrator/res/js/button/assets/skins/sam/button-skin.css" />
	<link rel="stylesheet" type="text/css" href="/migrator/res/js/container/assets/skins/sam/container.css" />
	<link rel="stylesheet" type="text/css" href="/migrator/res/js/tabview/assets/skins/sam/tabview.css" />
	<link rel="stylesheet" type="text/css" href="/migrator/res/js/tabview/assets/skins/sam/tabview-skin.css" />
	<link rel="stylesheet" type="text/css" href="/migrator/res/css/configurations.css" />
		
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
	<script type="text/javascript" src="/migrator/res/js/tabview/tabview-min.js"></script>
		
		
	<script src="/migrator/res/configurations.js"></script>
</head>
<body class="yui-skin-sam" style="min-height: calc(100vh - 50px);">
		<input type="hidden" id='urlcontext' value="${urlcontext}"/>
		<input type="hidden" id='alfportValue' value="${port}"/>
		<input type="hidden" id='alfprotocolValue' value="${protocol}"/>
		<input type="hidden" id='hasalfdetails' value="${hasalfconfig}"/>
		<br/>
			<div id="configtabview" class="yui-navset">
	    		<ul class="yui-nav">
	        		<li ><a href="#alfresco"><em>Alfresco</em></a></li>
	        		<li><a href="#activemq"><em>Active MQ</em></a></li>
	        		<li class="selected"><a href="#metadata"><em>CSV Metadata Mapping</em></a></li>
	    		</ul>            
	    		<div class="yui-content">
	        		<div id="alfresco">
	        			<p>
	        				<div class="alfrescopanel">
	        				<table cellpadding="5" >
	        					<tr>
	        						<td>Config ID</td>
	        						<td>:</td>
	        						<td>
	        							<input type="text" id="alfconfigid" readonly="readonly" class="readonly" value="${configid}"/>
	        						</td>
	        					</tr>
	        					<tr>
	        						<td>Protocol *</td>
	        						<td>:</td>
	        						<td>
	        							<select id="alfprotocol" value="<c:out value='${protocol}' />">
	        								<option value="" >Select Protocol</option>
	        								<option value="http" >HTTP</option>
	        								<option value="https">HTTPS</option>
	        							</select>
	        						</td>
	        					</tr>
	        					<tr>
	        						<td>Host *</td>
	        						<td>:</td>
	        						<td>
	        							<input type="text" id="alfhost" value="${host}"/>
	        						</td>
	        					</tr>
	        					<tr>
	        						<td>Port *</td>
	        						<td>:</td>
	        						<td>
	        							<select id="alfport" value="${port}">
	        								<option value="" >Select Port</option>
	        								<option value="80" >80</option>
	        								<option value="8080">8080</option>
	        								<option value="443">443</option>
	        								<option value="8443">8443</option>
	        							</select>
	        						</td>
	        					</tr>
	        					<tr>
	        						<td>Username</td>
	        						<td>:</td>
	        						<td>
	        							<input type="text" id="alfusername" value="${username}"/>
	        						</td>
	        					</tr>
	        					<tr>
	        						<td>Password</td>
	        						<td>:</td>
	        						<td>
	        							<input type="password" id="alfpassword" value="${password}"/>
	        						</td>
	        					</tr>
	        					<tr  align="center">
	        						<td colspan="3">
	        							<div id="saveBtn"></div>
	        							<div id="validateBtn"></div>
	        						</td>
	        					</tr>
	        					<tr  align="center">
	        						<td colspan="3">
	        							<div id="savingDiv" class="hidden"><img class="circle" src="/migrator/res/images/alfresco-logo-vector-1.svg"/>&nbsp;Saving Please Wait...</div>
	        						</td>
	        					</tr>
	        					<tr  align="center">
					      			<td colspan="3">
					      				<div id="validatingDiv" class="hidden"><img class="circle" src="/migrator/res/images/alfresco-logo-vector-1.svg"/>&nbsp;validating Please Wait...</div>
					      			</td>
					      		</tr>
	        					<!-- <tr  align="center">
	        						<td colspan="3">
	        							<div id="validatingDiv" class="hidden"><img class="circle" src="/migrator/res/images/alfresco-logo-vector-1.svg"/>&nbsp;validating Please Wait...</div>
	        						</td>
	        					</tr> -->
	        				</table>
	        				</div>
	        				<center>
	        				<table id="successSave" class="hidden" style="margin-left: 65px">
								<tr>
									<td>
										<div class="wrapper"> <svg id="tickSave" class="" viewBox="0 0 24 24">
											<path d="M4.1 12.7L9 17.6 20.3 6.3" fill="none" /> </svg> 
										</div>
									</td>
									<td><span>Details Saved Successfully</span></td>
								</tr>
							</table>
							<table id="successTable" class="hidden" style="margin-left: 65px">
								<tr>
									<td>
										<div class="wrapper"> <svg id="tick" class="" viewBox="0 0 24 24">
											<path d="M4.1 12.7L9 17.6 20.3 6.3" fill="none" /> </svg> 
										</div>
									</td>
									<td><span>Connection Success</span></td>
								</tr>
							</table>
							<table id="failTable" class="hidden" style="margin-left: 65px">
								<tr>
									<td>
										<div class="wrapper">
											<svg id="checsvg" class="" viewBox="0 0 52 52"><circle id="checkcircle" class="" cx="26" cy="26" r="25" fill="none"/><path class="checkmark_check" fill="none" d="M14.1 14.1l23.8 23.8 m0,-23.8 l-23.8,23.8"/></svg>
										</div>
									</td>
									<td>&nbsp;<span id="failMsg"></span></td>
								</tr>
							</table>
	        				</center>
	        				
						</p>
					</div>
	        		<div id="activemq">
	        			<p>
	        				<div class="alfrescopanel">
	        				<table cellpadding="5" >
	        					<tr>
	        						<td>Protocol *</td>
	        						<td>:</td>
	        						<td><input type="text" id="amqhost" value="${activemqprotocol}" readonly="readonly" class="readonly"/></td>
	        					</tr>
	        					<tr>
	        						<td>Host *</td>
	        						<td>:</td>
	        						<td>
	        							<input type="text" id="amqhost" value="${activemqhost}" readonly="readonly" class="readonly"/>
	        						</td>
	        					</tr>
	        					<tr>
	        						<td>Port *</td>
	        						<td>:</td>
	        						<td><input type="text" id="amqport" value="${activemqport}" readonly="readonly" class="readonly"/></td>
	        					</tr>
	        					<tr>
	        						<td>Username</td>
	        						<td>:</td>
	        						<td>
	        							<input type="text" id="amqusername" value="${activemqusername}" readonly="readonly" class="readonly"/>
	        						</td>
	        					</tr>
	        					<tr>
	        						<td>Password</td>
	        						<td>:</td>
	        						<td>
	        							<input type="password" id="amqpassword" value="${activemqpassword}" readonly="readonly" class="readonly"/>
	        						</td>
	        					</tr>
	        					<tr  align="center">
	        						<td colspan="3">
	        							<div id="amqalidateBtn"></div>
	        						</td>
	        					</tr>
	        					<tr  align="center">
					      			<td colspan="3">
					      				<div id="validatingDivAmq" class="hidden"><img class="circle" src="/migrator/res/images/alfresco-logo-vector-1.svg"/>&nbsp;validating Please Wait...</div>
					      			</td>
					      		</tr>
	        				</table>
	        				<table id="successTableAmq" class="hidden" style="margin-left: 65px">
								<tr>
									<td>
										<div class="wrapper"> <svg id="amqtick" class="" viewBox="0 0 24 24">
											<path d="M4.1 12.7L9 17.6 20.3 6.3" fill="none" /> </svg> 
										</div>
									</td>
									<td><span>Connection Success</span></td>
								</tr>
							</table>
							<table id="failTableAmq" class="hidden" style="margin-left: 65px">
								<tr>
									<td>
										<div class="wrapper">
											<svg id="checsvgamq" class="" viewBox="0 0 52 52"><circle id="amqcheckcircle" class="" cx="26" cy="26" r="25" fill="none"/><path class="checkmark_check" fill="none" d="M14.1 14.1l23.8 23.8 m0,-23.8 l-23.8,23.8"/></svg>
										</div>
									</td>
									<td>&nbsp;<span id="amqfailMsg"></span></td>
								</tr>
							</table>
	        				</div>
	        			</p>
	        		</div>
	        		<div id="metadata">
	        			<p>
	        				<table cellspacing="5">
	        					<tr align="center">
	        						<td>Template Name</td>
	        						<td><div class="vl"></div></td>
	        						<td>Select Type</td>
	        						<td><div class="vl"></div></td>
	        						<td>Select Aspect</td>
	        						<td><div class="vl"></div></td>
	        						<td>Select File Template &nbsp;<span ><img class="helpicon" src="/migrator/res/images/info.png" title=".csv,.XLX,.XLSX are supproted"/></span></td>
	        					</tr>
	        					<tr>
	        						<td><input type="text" id="templateName"/></td>
	        						<td><div class="vl"></div></td>
	        						<td>
	        							<select id="alfrescotype" style="width:300px">
	        									<option value="">Select</option>
	        									[(${filetypedef})]
	        							</select>
	        						</td>
	        						<td><div class="vl"></div></td>
	        						<td>
	        							<select id="alfrescoaspect" style="width:300px">
	        								<options>
	        									<option value="">Select</option>
	        									[(${aspectdef})]
	        								</options>
	        							</select>
	        						</td>
	        						<td><div class="vl"></div></td>
	        						<td><input type="file" id="csvTemplate" accept=".csv,.xls,.xlsx"/></td>
	        					</tr>
	        					<tr align="center">
	        						<td></td>
	        						<td><div></div></td>
	        						<td><div id="locTypeBtn"></div></td>
	        						<td><div></div></td>
	        						<td></td>
	        						<td><div></div></td>
	        						<td></td>
	        					</tr>
	        				</table>
	        				<center>
	        					<div class="filetypepanel">
	        						<table>
	        							<tr>
	        								<td align="right"><span class="label">Selected Filetype</span></td>
	        								<td>:</td>
	        								<td><span id="typevalueSelected"></span></td>
	        								<td><div ><img id="deleteFileType" src="/migrator/res/images/delete.png" class="delete hidden" title="Delete"/></div></td>
	        							</tr>
	        							<tr>
	        								<td><span class="label">Physical File Column</span></td>
	        								<td>:</td>
	        								<td><span id="physicalFile"></span></td>
	        								<td><div ><img id="deleteColumn" src="/migrator/res/images/delete.png" class="delete hidden" title="Delete"/></div></td>
	        							</tr>
	        							<tr >
	        								<td colspan="4" align="center"><div id="resetBtn"></div></td>
	        							</tr>
	        						</table>
	        					</div>
	        				</center>
	        				<br>
	        				<table>
	        					<tr>
	        						<td>
	        							<div class="tablelabels">
	        								<span >CSV Columns
	        								</span>
	        							</div>
	        						</td>
	        						<td>
	        							<div class="tablelabels">
	        								<span>Alfresco Metadata
	        								</span>
	        							</div>
	        						</td>
	        						<td>
	        						</td>
	        						<td>
	        							<div class="tablelabels">
	        								<span>Mapped Metadata
	        								</span>
	        							</div>
	        						</td>
	        					</tr>
	        					<tr>
	        						<td>
	        							<div class="tablesize">
	        								<div id="csvcolumnTable" class="typeproptable">
	        								</div>
	        							</div>
	        						</td>
	        						<td>
	        							<div class="tablesize">
	        								<div id="typePropertiesTable" class="typeproptable">
	        								</div>
	        							</div>
	        						</td>
	        						<td>
	        							<div id="mapBtn"></div>
	        						</td>
	        						<td>
	        							<div class="tablesize">
	        								<div id="mapTable"></div>
	        							</div>
	        						</td>
	        					</tr>
	        					<tr>
	        						<td>
	        						</td>
	        						<td>
	        						</td>
	        						<td>
	        						</td>
	        						<td align="center">
	        							<div id="saveMapBtn"></div>
	        						</td>
	        					</tr>
	        				</table>
	        			</p>
	        		</div>
	    		</div>
			</div>
		<div id="loadingPanel"></div>
		<script type="text/javascript">//<![CDATA[
			new Migrator.component.configurations();
		//]]></script>
	</body>
</html>