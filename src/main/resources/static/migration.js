(function()
{
	var me;
	var Dom = YAHOO.util.Dom,
    Event = YAHOO.util.Event;
	Migrator.component.Migrate = function (){
		me=this;
		me.widgets={};
      	me.onComponentsLoaded();
      	return this;
   	};
   	Migrator.component.Migrate.prototype ={
		treeContentElId:"",
		totalRecords:0,
		filesSource:"local",
		hasincludecolumn:false,
		starttime:new Date(),
		endtime:new Date(),
		options:{
			urlcontext:""
		},
		setOptions: function(obj){
	        me.options = YAHOO.lang.merge(me.options, obj);
	        return this;
	    },
		onComponentsLoaded:function(){
			Event.onContentReady("htmlbody", me.onReady, this, true);
		},
		onReady :function(){
			var templateCtrl = Dom.get("metadatatemplate");
			var nativeTemplateCtrl = Dom.get("nativemetadatatemplate");
			var templViewIcon = Dom.get("metadatatemplate-img");
			var nativeTemplViewIcon = Dom.get("nativemetadatatemplate-img");
			var localLocationCtrl = Dom.get("local");
			var s3LocationCtrl = Dom.get("amazons3");
			var yesincludeCtrl = Dom.get("yesinclude");
			var noincludeCtrl = Dom.get("noinclude");
			me.widgets.progressbar = new YAHOO.widget.ProgressBar({height:'25px', width: 300, barColor:'green',backColor:'orange',border:'thin solid black'});
			var progressTextCtrl = Dom.get("percentage");
			me.widgets.progressbar.render('progressbar');
			me.widgets.pdfdestinationBtn = new YAHOO.widget.Button("destinationBtn",{
				label : "..."
			});
			me.widgets.nativedestinationBtn = new YAHOO.widget.Button("nativeDestinationBtn",{
				label : "..."
			});
			me.widgets.migrateBtn = new YAHOO.widget.Button("migrateBtn",{
				label : "Migrate"
			});
			me.widgets.resetBtn = new YAHOO.widget.Button("resetBtn",{
				label : "Reset"
			});
			me.widgets.resetBtn.set("disabled",true);
			me.widgets.resetBtn.on("click",function(){
				location.reload();
			});
			me.widgets.pdfdestinationBtn.on('click',function(){
				me.removeFocusClass(me.widgets.pdfdestinationBtn);
				var pdfDestinationEl = Dom.get("pdfdestination");
				me.destinationPanel(pdfDestinationEl);
			});
			me.widgets.nativedestinationBtn.on('click',function(){
				me.removeFocusClass(me.widgets.nativedestinationBtn);
				var nativedestinationEl = Dom.get("nativedestination");
				me.destinationPanel(nativedestinationEl);
			});
			me.widgets.migrateBtn.on('click',this.startMigration);
			var csvColumns = [{
				key:"status",
				label:"Status Code"
			},{
				key:"statustext",
				label:"Status Message",
				formatter:function(el,oRecord,oColumn,oData){
					var statusCode = oRecord.getData("status");
					if(200==statusCode || 201==statusCode){
						el.innerHTML+="Uploaded";
					}else{
						el.innerHTML+="Failed";
					}
				}
			},{
				key:"count",
				label:"count"
			}];
			me.widgets.csvDataSource = new YAHOO.util.DataSource("checkstatus?csvid=",{
				responseType : YAHOO.util.DataSource.TYPE_JSON,
				responseSchema : {
				resultsList : "list",
				fields : ["status", "count"]
				}
			}); 
			me.widgets.csvDataTable = new YAHOO.widget.DataTable("statustable", csvColumns, me.widgets.csvDataSource,{
				initialLoad: false,
        		MSG_EMPTY:"No records migrated"
        	});
			me.widgets.csvDataTable.subscribe("renderEvent",function(){
				var totalMigrated=0,remaining=0;
				var remainingCtrl = Dom.get("remaining");
				var messageTableCtrl = Dom.get("messageTable");
				var percentage = 0;
				var recordSet = me.widgets.csvDataTable.getRecordSet().getRecords();
				var loadingCtrl = Dom.get("loadingCtrl");
				for(var index=0;index<recordSet.length;index++){
					totalMigrated += parseInt(recordSet[index].getData("count"));
				}
				console.log("total migrated "+totalMigrated+" total records "+me.totalRecords);
				remaining= me.totalRecords-totalMigrated;
				remainingCtrl.innerHTML = remaining;
				percentage = parseInt((totalMigrated/me.totalRecords)*100,10);
				me.widgets.progressbar.set("value",percentage);
				progressTextCtrl.innerHTML = percentage+"%";
				if(totalMigrated>=me.totalRecords){
					var timeTakenCtrl = Dom.get("timetaken");
					me.endtime = new Date();
					timeTakenCtrl.innerHTML = me.timeDiff(me.starttime,me.endtime);
					Dom.removeClass(messageTableCtrl,"hidden");
					Dom.addClass(loadingCtrl,"hidden");
					remaining= me.totalRecords-totalMigrated;
					remainingCtrl.innerHTML = remaining;
					me.widgets.resetBtn.set("disabled",false);
					me.widgets.csvDataSource.clearAllIntervals();
				}
			});
			Event.addListener(templateCtrl,"change",me.toggleViewIcon);
			Event.addListener(nativeTemplateCtrl,"change",me.toggleViewIcon);
			Event.addListener(templViewIcon,"click",me.templateDetailPanelShow);
			Event.addListener(nativeTemplViewIcon,"click",me.templateDetailPanelShow);
			Event.addListener(localLocationCtrl,"click",me.filesLocation);
			Event.addListener(s3LocationCtrl,"click",me.filesLocation);
			Event.addListener(yesincludeCtrl,"click",me.includeColumn);
			Event.addListener(noincludeCtrl,"click",me.includeColumn);
		},
		timeDiff :function(starttime,endtime){
			var timeStart = starttime.getTime();
			var timeEnd = endtime.getTime();
			var hourDiff = timeEnd - timeStart; //in ms
			var secDiff = hourDiff / 1000; //in s
			var minDiff = hourDiff / 60 / 1000; //in minutes
			var hDiff = hourDiff / 3600 / 1000; //in hours
			var humanReadable = {};
			humanReadable.hours = Math.floor(hDiff);
			humanReadable.minutes = Math.round(minDiff - 60 * humanReadable.hours);
			humanReadable.seconds = Math.floor(secDiff%60);
			return humanReadable.hours+" Hr :"+humanReadable.minutes+" Min :"+humanReadable.seconds+" Sec"; 
		},
		includeColumn :function(e){
			var radioCtrl = e.target;
			var includeValue =radioCtrl.id;
			me.hasincludecolumn = (includeValue=="yesinclude")?true:false;
		},
		filesLocation :function(e){
			var radioCtrl = e.target;
			me.filesSource=radioCtrl.id;
		},
		templateDetailPanelShow : function(e){
			var selectCtrl = e.target;
			var templId = Dom.get(selectCtrl.id.replace("-img",""));
			var tempCtrl = Dom.get(templId);
			var tempValue = tempCtrl.options[tempCtrl.selectedIndex].value;
			me.templatePanel(tempValue);
		},
		toggleViewIcon : function(e){
			var selectCtrl = e.target;
			var tempValue = selectCtrl.options[selectCtrl.selectedIndex].value;
			var viewIcon = Dom.get(selectCtrl.id+"-img");
			if(tempValue==null||tempValue==undefined||tempValue==""){
				Dom.addClass(viewIcon,"hidden");
			}else{
				Dom.removeClass(viewIcon,"hidden");
			}
		},
		templatePanel : function(templid){
				var templatePanel = new YAHOO.widget.Panel("templatepanel", { visible:true,modal:false, draggable:true, close:false,zindex:4,keylisteners:[escapeListener]} );
				var escapeListener = new YAHOO.util.KeyListener(document,
               	{
                  	keys: YAHOO.util.KeyListener.KEY.ESCAPE
               	},
               	{
                  	fn: function(id, keyEvent)
                  	{
                     	this.destroy();
                  	},
                  	scope: templatePanel,
                  	correctScope: true
               	});
        	 	escapeListener.enable();
				templatePanel.setHeader("<center><b>Metadata Mapping</b><div id='close' class='close'></center>");
				templatePanel.setBody("<center><div style='min-width:750px;min-height:400px;'><table>"
				+"<tr><td><b>Template ID</b></td><td>:</td><td><span id='templateid'></span></td></tr>"
				+"<tr><td><b>Template Name</b></td><td>:</td><td><span id='templatename'></span></td></tr>"
				+"<tr><td><b>Mapped Alfresco Type</b></td><td>:</td><td><span id='alfname'></span></td></tr>"
				+"<tr><td><b>File Name Column</b></td><td>:</td><td><span id='filenamecolumn'></span></td></tr>"
				+"</table>"
				+"<br/><div id='detailsTable'></div></div>"
				+"</center>");
				templatePanel.setFooter("<center><table><tr><td><div id='save'></div></td></tr></table>");
				templatePanel.showEvent.subscribe(function(){
					var templateIdCtrl = Dom.get("templateid");
					var templateNameCtrl = Dom.get("templatename");
					var alfNameCtrl = Dom.get("alfname");
					var fileNameCtrl = Dom.get("filenamecolumn");
					var closeCtrl = Dom.get("close");
					YAHOO.util.Event.addListener(closeCtrl,"click",function(e){
						templatePanel.destroy();
					});
					var saveBtn = new YAHOO.widget.Button("save",{
						label:"Ok"
					}); 
					saveBtn.on("click",function(){
						templatePanel.destroy();
					});
					templateIdCtrl.innerHTML = templid;
					
					var templateSuccess = function(res){
						var resJson = JSON.parse(res.responseText);
						console.log(resJson);
						templateNameCtrl.innerHTML=resJson.templatename;
						alfNameCtrl.innerHTML = resJson.propertytitle+" ("+resJson.propertyqname+")";
						fileNameCtrl.innerHTML = resJson.columnname;
						var columns=[{
							key : "csvcolumnname",
							label : "CSV Column"
						},{
							key : "alfpropertytitle",
							label : "Alfresco Property"
						},{
							key : "alfrescopropertyqname",
							label : "Alfresco QName"
						},{
							key : "uniqueid",
							label : "Unique ID",
							hidden:true
						}];
						var datasource = new YAHOO.util.LocalDataSource(resJson.templatedetails); 
		        	 	var datatable =  new YAHOO.widget.ScrollingDataTable("detailsTable", columns, datasource,{
				       		MSG_EMPTY:"",
				       		height:"290px"
				       	});
					};
				var templateFailure = function(res){
					console.log(res);
				};
				var templateCallback ={
					success: templateSuccess,
					failure: templateFailure
				};
				var templateReq = YAHOO.util.Connect.asyncRequest('GET', "/migrator/gettemplate?templateId="+templid, templateCallback);
					
			});
			templatePanel.render(document.body);
			templatePanel.center();
		},
		removeFocusClass :function(el){
			Dom.removeClass(el,"yui-button-focus");
			Dom.removeClass(el,"yui-push-button-focus");
		},
		destinationPanel : function(el){
				var selectedFolder = "";
				var destinationPanel = new YAHOO.widget.Panel("destinationPanel", { visible:true,modal:false, draggable:true, close:false,zindex:4,keylisteners:[escapeListener]} );
				var escapeListener = new YAHOO.util.KeyListener(document,
               	{
                  	keys: YAHOO.util.KeyListener.KEY.ESCAPE
               	},
               	{
                  	fn: function(id, keyEvent)
                  	{
                     	this.destroy();
                  	},
                  	scope: destinationPanel,
                  	correctScope: true
               	});
        	 	escapeListener.enable();
				destinationPanel.setHeader("<center>Select Destination</center><div id='close' class='close'></div>");
				destinationPanel.setBody("<div class='destinationspace' id='destTree' ></div>");
				destinationPanel.setFooter("<center><table><tr><td><div id='save'></div></td><td><div id='cancel'></div></td></tr></table>");
				destinationPanel.showEvent.subscribe(function(){
					var closeCtrl = Dom.get("close");
					YAHOO.util.Event.addListener(closeCtrl,"click",function(e){
						destinationPanel.destroy();
					});
					var saveBtn = new YAHOO.widget.Button("save",{
						label:"Ok"
					}); 
					var cancelBtn = new YAHOO.widget.Button("cancel",{
						label:"Cancel"
					});
					saveBtn.on("click",function(){
						el.value=selectedFolder;
						destinationPanel.destroy();
					});
					cancelBtn.on("click",function(){
						el.value="";
						destinationPanel.destroy();
					});
					var tree = new YAHOO.widget.TreeView("destTree");
					var loadNodeData=function(node, fnLoadComplete){
						var child,nodes=[];
						if(node.label!=null&&node.label!=""){
							var success= function(res){
								console.log(res);
								var obj = eval('(' + res.responseText + ')');
								for(var i in obj.entries){
									node.data={};
									node.data.noderef=obj.entries[i].entry["id"];
									node.data.allowedoperations=obj.entries[i].entry["allowableOperations"];
									child =new YAHOO.widget.TextNode({
										label:obj.entries[i].entry["name"],
										title:obj.entries[i].entry["id"],
										html:"#"+obj.entries[i].entry["allowableOperations"].toString()+";"+obj.entries[i].entry["id"]
										}, node, false);
									//child.isLeaf = false;
									//child.isLoading =false;
									nodes.push(child);
								}
								fnLoadComplete();
								for(var i in nodes){
									
										
								}
							};
							var failure= function(res){
								console.log(res);
								fnLoadComplete();
							};
							var callback = {
								success:success,
								falilure:failure
							};
							try{
								YAHOO.util.Connect.asyncRequest('GET', "alfrescofolders?nodeid="+node.title, callback);
							}catch(message){
								alert(message);
							}
						}else{
							Migrator.utils.PopupManager.displayPrompt(document.body,"Alert","Failed to load node data");
						}
					};
					tree.setDynamicLoad(loadNodeData,1); 
					var root = tree.getRoot();
					var repositoryNode = new YAHOO.widget.TextNode({label:"Repository",title:"-root-",href:"#"}, root, true);
					//repositoryNode.isLoading =false;
					tree.draw();
					tree.subscribe("labelClick",function(node){
						var nodeLabel = node.title;
						selectedFolder=nodeLabel;
						if(!(me.treeContentElId==""||me.treeContentElId==null||me.treeContentElId==undefined)){
							Dom.removeClass(Dom.get(me.treeContentElId),"selected");
						}
						var currentItem = Dom.get(node.contentElId);
						Dom.addClass(currentItem,"selected");
						me.treeContentElId=node.contentElId;
					});
					tree.subscribe("clickEvent",function(){
						return false;
					});
					tree.subscribe("expandComplete",function(node){
						var childNodes = node.children;
						var appendPlusIcon = function(textNode){
							var nodeLavelSpan = Dom.get(textNode.labelElId);
							if(nodeLavelSpan.childNodes.length==1){
								console.log(textNode.data);
								var htmlStr = textNode.data.html.toString().split(";");
								//console.log(nodes[i].getNodeDefinition().data);
								if(!(nodeLavelSpan==null||nodeLavelSpan==undefined||nodeLavelSpan=="") && htmlStr[0].indexOf("create")>=0){
									var spanEl = document.createElement("span");
									spanEl.id=htmlStr[1];
									var imageEl = document.createElement("img");
									imageEl.src="/migrator/res/images/add.png";
									imageEl.style.width="15px";
									imageEl.id=htmlStr[1];
									imageEl.alt=textNode.index;
									Dom.addClass(spanEl,"addicon");
									//console.log(nodes[i]);
									console.log(nodeLavelSpan);
									spanEl.appendChild(imageEl);
									nodeLavelSpan.appendChild(spanEl);
									YAHOO.util.Event.addListener(imageEl,"click",function(e){
										var selectCtrl = e.target;
										var spanElParent=Dom.get("ygtvlabelel"+selectCtrl.alt);
										var targetFolderId = spanElParent.title;
										me.createFolderPanel(targetFolderId,selectCtrl.alt,tree,loadNodeData);
									});
								}
							}
							
						};
						for(var index=0;index<childNodes.length;index++){
							console.log(childNodes[index]);
							appendPlusIcon(childNodes[index]);
						}
						appendPlusIcon(node);
					});
					
				});
				destinationPanel.hideEvent.subscribe(function(e){
					console.log(YAHOO.util.Event);
					YAHOO.util.Event.preventDefault(destinationPanel);
					destinationPanel.destroy();
				});
				destinationPanel.render(document.body);
				destinationPanel.center();
			},
			createFolderPanel :function(target,index,tree,loadNodeData){
				var folderDialog = new YAHOO.widget.Panel("messagefailedpanel", { visible:true,modal:false, draggable:true, close:false,zindex:12} );
				folderDialog.setHeader("<center>Create Folder</center><div id='close' class='close'></div>");
				folderDialog.setBody("<center><div class='loading hidden' id='loadingImg' ></div>"
				+"<table><tr><td>Name</td><td>:</td><td><input type='text' id='folderName'/></td></tr></table></center>");
				folderDialog.setFooter("<center><table><tr><td><div id='save'></td><td></div><div id='cancel'></div></td></tr></table>");
				folderDialog.showEvent.subscribe(function(){
					var saveBtn = new YAHOO.widget.Button("save",{
						label:"Ok"
					}); 
					var cancelBtn = new YAHOO.widget.Button("cancel",{
						label:"Cancel"
					});
					var closeCtrl = Dom.get("close");
					YAHOO.util.Event.addListener(closeCtrl,"click",function(e){
						folderDialog.destroy();
					});
					saveBtn.on("click",function(){
						me.removeFocusClass(saveBtn);
						var folderNameCtrl = Dom.get("folderName");
						if(folderNameCtrl.value==null||folderNameCtrl.value==undefined||folderNameCtrl.value==""){
							Migrator.utils.PopupManager.displayMessage(document.body,{
								text:"Please provide folder name"
							});
							folderNameCtrl.style.border="1px solid red";
						}else{
							console.log("target "+target);
							folderNameCtrl.style.border=null;
							var sUrl = "createfolder";
							var handleSuccess = function(res){
								console.log(res);
								var responseJson = JSON.parse(res.responseText);
								Dom.addClass(loadingCtrl,"hidden");
								console.log(responseJson);
								var noderef = responseJson.entry.id;
								var name = responseJson.entry.name;
								var allowedOperations = responseJson.entry.allowableOperations.toString();
								var node =tree.getNodeByIndex(index);
								var treeNode = new YAHOO.widget.TextNode({
										label:name,
										title:noderef,
										html:"#"+allowedOperations
									}, node, false);
								treeNode.isLeaf = false;
								treeNode.isLoading =false;
								treeNode.setDynamicLoad(loadNodeData,1);
								node.collapse();
								node.expand();
								folderDialog.destroy();
							};
							var handleFailure = function(res){
								console.log(res);
								Dom.addClass(loadingCtrl,"hidden");
							};
							var callback ={
								success:handleSuccess,
								failure: handleFailure,
								argument: ['foo','bar']
								};
							var postData = "parentnode="+target+"&foldername="+folderNameCtrl.value;
							var request = YAHOO.util.Connect.asyncRequest('POST', sUrl, callback, postData);
						}
					});
					cancelBtn.on("click",function(){
						folderDialog.destroy();
					});
				});
				folderDialog.render(document.body);
				folderDialog.center();
			},
			startMigration : function(){
				me.removeFocusClass(me.widgets.migrateBtn);
				me.starttime = new Date();
				var progressTable = Dom.get("progresstable");
				var remainingCtrl = Dom.get("remaining");
				var failedMessageTableCtrl = Dom.get("messageFailedTable");
				var csvRecordCtrl = Dom.get("csvrecordid");
				var totalRecordsCtrl = Dom.get("totalRecords");
				var selectedFiles = Dom.get("csvFile");
				var selectedCsvFiles = selectedFiles.files;
				var pdfDestCtrl = Dom.get("pdfdestination");
				var nativeDestCtrl = Dom.get("nativedestination");
				var pdfLocationCtrl = Dom.get("localpdflocation");
				var nativeLocationCtrl = Dom.get("localnativelocation");
				var islatestCtrl = Dom.get("islatest");
				var nativemetadataTemplateCtrl = Dom.get("nativemetadatatemplate");
				var metadataTemplateCtrl = Dom.get("metadatatemplate");
				var metadataTemplateValue = metadataTemplateCtrl.options[metadataTemplateCtrl.selectedIndex].value;
				var nativemetadataTemplateValue = nativemetadataTemplateCtrl.options[nativemetadataTemplateCtrl.selectedIndex].value;
				var loadingCtrl = Dom.get("loadingCtrl");
				if(selectedCsvFiles.length==0){
					Migrator.utils.PopupManager.displayMessage(document.body,{
						text:"please select CSV files"
						});
					selectedFiles.style.border = "1px solid red";
				}else if(pdfDestCtrl.value==""||pdfDestCtrl.value==null||pdfDestCtrl.value==undefined){
					Migrator.utils.PopupManager.displayMessage(document.body,{
						text:"please select PDF files destination in Alfresco"
						});
					selectedFiles.style.border = null;
					pdfDestCtrl.style.border = "1px solid red";
				}else if(nativeDestCtrl.value==""||nativeDestCtrl.value==null||nativeDestCtrl.value==undefined){
					Migrator.utils.PopupManager.displayMessage(document.body,{
						text:"please select Native files destination in Alfresco"
						});
					pdfDestCtrl.style.border = null;
					nativeDestCtrl.style.border = "1px solid red";
				}else if(pdfLocationCtrl.value==""||pdfLocationCtrl.value==null||pdfLocationCtrl.value==undefined){
					Migrator.utils.PopupManager.displayMessage(document.body,{
						text:"please select local PDF files folder location"
						});
					nativeDestCtrl.style.border = null;
					pdfLocationCtrl.style.border = "1px solid red";
				}
				else if(nativeLocationCtrl.value==""||nativeLocationCtrl.value==null||nativeLocationCtrl.value==undefined){
					Migrator.utils.PopupManager.displayMessage(document.body,{
						text:"please select local native files folder location"
						});
					pdfLocationCtrl.style.border = null;
					nativeLocationCtrl.style.border = "1px solid red";
				}else if(metadataTemplateValue==""||metadataTemplateValue==null||metadataTemplateValue==undefined){
					Migrator.utils.PopupManager.displayMessage(document.body,{
						text:"please select metadata template"
						});
					nativeLocationCtrl.style.border = null;
					metadataTemplateCtrl.style.border = "1px solid red";
				}else if(nativemetadataTemplateValue==""||nativemetadataTemplateValue==null||nativemetadataTemplateValue==undefined){
					Migrator.utils.PopupManager.displayMessage(document.body,{
						text:"please select native metadata template"
						});
					metadataTemplateCtrl.style.border = null;
					nativemetadataTemplateCtrl.style.border = "1px solid red";
				}else if(me.filesSource==""||me.filesSource==null||me.filesSource==undefined){
					Migrator.utils.PopupManager.displayMessage(document.body,{
						text:"please select source location"
						});
				}else{
					nativemetadataTemplateCtrl.style.border = null;
					Dom.removeClass(loadingCtrl,"hidden");
					me.widgets.migrateBtn.set("disabled",true);
					var formData = new FormData();
					formData.append("file",selectedCsvFiles[0]);
					formData.append("pdfdestination",pdfDestCtrl.value);
					formData.append("nativedestination",nativeDestCtrl.value);
					formData.append("localpdflocation",pdfLocationCtrl.value);
					formData.append("localnativelocation",nativeLocationCtrl.value);
					formData.append("metadtatemplateid",metadataTemplateValue);
					formData.append("islatest",(islatestCtrl.checked==undefined||islatestCtrl.checked==null||islatestCtrl.checked=="")?false:true);
					formData.append("nativemetadtatemplateid",nativemetadataTemplateValue);
					formData.append("sourcelocation",me.filesSource);
					formData.append("hasinclude",me.hasincludecolumn);
					console.log(formData);
					var migrateReq = new XMLHttpRequest();
					migrateReq.upload.addEventListener("load", function(e){
					 	if( migrateReq.readyState!=4){
						 	migrateReq.onreadystatechange=function(e) {
								if (migrateReq.readyState == XMLHttpRequest.DONE && migrateReq.status==200) {
									var resJson = JSON.parse(migrateReq.response);
									console.log(resJson);
									Dom.removeClass(progressTable,"hidden");
									me.totalRecords= parseInt(resJson.hasOwnProperty("totalrecords")?resJson["totalrecords"]:0);
									totalRecordsCtrl.innerHTML = me.totalRecords;
									remainingCtrl.innerHTML = me.totalRecords;
									csvRecordCtrl.value = resJson.uniqueid;
									me.widgets.csvDataSource.setInterval(20000,"", function dataSourceRefesh() {
										//csvDataTable.showTableMessage("<img src='/migrator/images/ajaxloading.gif' class='ajaxloading'/>");
										me.widgets.csvDataTable.showTableMessage("<img src='/migrator/res/images/alfresco-logo-vector-1.svg' class='circle'/>");
										me.widgets.csvDataSource.sendRequest(csvRecordCtrl.value, {
											success: me.widgets.csvDataTable.onDataReturnReplaceRows,
											scope: me.widgets.csvDataTable
									});}, me.widgets.csvDataTable);
									var messageData = new FormData();
									messageData.append("file",selectedCsvFiles[0]);
									messageData.append("pdfdestination",pdfDestCtrl.value);
									messageData.append("nativedestination",nativeDestCtrl.value);
									messageData.append("islatest",(islatestCtrl.checked==undefined||islatestCtrl.checked==null||islatestCtrl.checked=="")?false:true);
									messageData.append("savedcsvobject",JSON.stringify(resJson));
									messageData.append("sourcelocation",me.filesSource);
									messageData.append("hasinclude",me.hasincludecolumn);
									var uploadReq = new XMLHttpRequest();
									uploadReq.upload.addEventListener("load", function(e){
									 	if( uploadReq.readyState!=4){
										 	uploadReq.onreadystatechange=function(e) {
												if (uploadReq.readyState == XMLHttpRequest.DONE && uploadReq.status==200) {
													var messageJson = JSON.parse(uploadReq.response);
												}else if (uploadReq.readyState == XMLHttpRequest.DONE && (uploadReq.status ==400 || uploadReq.status ==500)) {
												 	var messageJson = JSON.parse(uploadReq.response);
												 	Dom.removeClass(failedMessageTableCtrl,"hidden");
													Dom.addClass(loadingCtrl,"hidden");
													me.widgets.resetBtn.set("disabled",false);
													Migrator.utils.PopupManager.displayPrompt(document.body,messageJson.status,messageJson.message);
													me.widgets.migrateBtn.set("disabled",false);
				    						 	}
											}
										}
									});
									uploadReq.open("POST",  "senduplaodmessage",true);
									uploadReq.send(messageData);
									
							 	}else if (migrateReq.readyState == XMLHttpRequest.DONE && (migrateReq.status ==400 || migrateReq.status ==500)) {
								 	var messageJson = JSON.parse(migrateReq.response);
								 	Dom.removeClass(failedMessageTableCtrl,"hidden");
									Dom.addClass(loadingCtrl,"hidden");
									me.widgets.resetBtn.set("disabled",false);
									Migrator.utils.PopupManager.displayPrompt(document.body,messageJson.status,messageJson.message);
									me.widgets.migrateBtn.set("disabled",false);
    						 	}else if(migrateReq.readyState == XMLHttpRequest.DONE){
									console.log(migrateReq.readyState);
									me.widgets.migrateBtn.set("disabled",false);
								}
						 	}
					 	};
				 	}, false);
					migrateReq.open("POST",  "migrate",true);
					migrateReq.send(formData);
				}
			}
	};
})();