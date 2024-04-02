(function()
{
	var me;
	var Dom = YAHOO.util.Dom,
    Event = YAHOO.util.Event;
	Migrator.component.configurations = function (){
		me=this;
		me.widgets={};
      	me.onComponentsLoaded();
      	return this;
   	};
   	Migrator.component.configurations.prototype ={
		columnSelected:{},
		propertySelected:"",
		propertyQnameSelected:"",
		onComponentsLoaded:function(){
			var tabView = new YAHOO.widget.TabView('configtabview');
			this.loadAlfrescoControls();
			this.loadActiveMqControls();
			this.loadCSVConfigurationControls();
		},
		loadAlfrescoControls:function(){
			var alfProtocolCtrl = Dom.get("alfprotocol");
			var alfProtocolVlaueCtrl = Dom.get("alfprotocolValue");
			alfProtocolCtrl.value=alfProtocolVlaueCtrl.value;
			var alfPortCtrl = Dom.get("alfport");
			var alfortVlaueCtrl = Dom.get("alfportValue");
			alfPortCtrl.value=alfortVlaueCtrl.value;
			me.widgets.alfrescoSaveBtn = new YAHOO.widget.Button("saveBtn",{
				label : "Save"
			});
			me.widgets.alfrescovalidateBtn = new YAHOO.widget.Button("validateBtn",{
				label : "Validate"
			});
			me.widgets.alfrescoSaveBtn.on('click',this.saveAlfrescoDetails);
			me.widgets.alfrescovalidateBtn.on('click',this.validateAlfrescoDetails);
		},
		loadActiveMqControls : function(){
			me.widgets.amqvalidateBtn = new YAHOO.widget.Button("amqalidateBtn",{
				label : "Validate"
			});
			me.widgets.amqvalidateBtn.on('click',this.validateActiveMQDetails);
		},
		loadCSVConfigurationControls :function(){
			var fileTypeCtrl = Dom.get("alfrescotype");
			var aspectCtrl = Dom.get("alfrescoaspect");
			var physicalFileColumn = Dom.get("physicalFile");
			var deleteIcon = Dom.get("deleteColumn");
			var deleteFileType = Dom.get("deleteFileType");
			var csvFileCtrl = Dom.get("csvTemplate");
			me.widgets.lockTypeBtn = new YAHOO.widget.Button("locTypeBtn",{
				label : "Lock Type"
			});
			me.widgets.saveMapBtn = new YAHOO.widget.Button("saveMapBtn",{
				label : "Save Map"
			});
			me.widgets.saveMapBtn.set("disabled",true);
			me.widgets.mapBtn = new YAHOO.widget.Button("mapBtn",{
				label : "Map Column"
			});
			me.widgets.mapBtn.set("disabled",true);
			me.widgets.resetBtn = new YAHOO.widget.Button("resetBtn",{
				label : "Reset"
			});
			me.widgets.resetBtn.on("click",function(){
				location.reload();
			});
			me.widgets.lockTypeBtn.on('click',this.lockTypeButtonFn);
			me.widgets.mapBtn.on('click',this.mapBtnClick);
			me.widgets.saveMapBtn.on('click',this.saveTemplate);
			var csvColumns = [{
				key:"checkbox",
				label:"",
				formatter:function(el,oRecord,oColumn,oData){
					var checkboxEl = document.createElement("input");
					checkboxEl.type="checkbox";
					checkboxEl.name="csvcolumn";
					checkboxEl.id=oRecord.getId()+"-checkbox";
					el.append(checkboxEl);
					YAHOO.util.Event.addListener(checkboxEl,"change",function(e){
						var checkBoxCtrl = e.target;
						console.log(checkBoxCtrl);
						if(checkBoxCtrl.checked){
							me.columnSelected[oRecord.getId()] = oRecord;
						}else{
							if(me.columnSelected.hasOwnProperty(oRecord.getId())){
								delete me.columnSelected[oRecord.getId()];
							}
						}
						console.log(me.columnSelected);
					});
				}	
			},{
				key:"columnname",
				label:"Column Name",
				width:185
			},{
				key:"actions",
				label:"Actions",
				formatter:function(el,oRecord,oColumn,oData){
					el.innerHTML = "<div class='ZoomIt'><ul>"
					+"<li><a href='#declarefile' ><img class='actions zit' src='/migrator/res/images/File.webp' title='File Column'/></a></li>"
					+"</ul></div>";
				}
			}];
			var csvDataSource = new YAHOO.util.LocalDataSource([{
        			 "columnname":""
        		 }]); 
			me.widgets.csvDataTable = new YAHOO.widget.ScrollingDataTable("csvcolumnTable", csvColumns, csvDataSource,{
				initialLoad: false,
        		MSG_EMPTY:"No CSV added",
        		width:"300px", 
        		height:"400px"
        	});
        	me.widgets.csvDataTable.subscribe("rowMouseoverEvent", me.widgets.csvDataTable.onEventHighlightRow);
        	me.widgets.csvDataTable.subscribe("rowMouseoutEvent", me.widgets.csvDataTable.onEventUnhighlightRow);
        	var typeColumns = [{
				key:"checkbox",
				label:"",
				formatter:function(el,oRecord,oColumn,oData){
					var checkboxEl = document.createElement("input");
					checkboxEl.type="radio";
					checkboxEl.name="typegroup";
					checkboxEl.id=oRecord.getId()+"-checkbox";
					el.append(checkboxEl);
				}	
			},{
				key:"propertylabel",
				label:"Property",
				width:300,
				formatter:function(el,oRecord,oColumn,oData){
					el.innerHTML = oRecord.getData("propertytitle")+" ("+oRecord.getData("propertyqname")+")"
				}
			},{
				key:"propertytitle",
				label:"Property",
				width:300,
				hidden:true
			},{
				key:"propertyqname",
				label:"property QName",
				hidden:true
			}];
			var typeDataSource = new YAHOO.util.DataSource("alfrescoclassproperties?type=",{
				responseType : YAHOO.util.DataSource.TYPE_JSON,
				responseSchema : {
				resultsList : "properties",
				fields : ["propertytitle", "propertyqname"]
				}
				}); 
			me.widgets.typeDataTable = new YAHOO.widget.ScrollingDataTable("typePropertiesTable", typeColumns, typeDataSource,{
				initialLoad: false,
        		MSG_EMPTY:"No Type Selected",
        		width:"300px", 
        		height:"400px"
        	});
        	me.widgets.typeDataTable.subscribe("rowMouseoverEvent", me.widgets.typeDataTable.onEventHighlightRow);
        	me.widgets.typeDataTable.subscribe("rowMouseoutEvent", me.widgets.typeDataTable.onEventUnhighlightRow);
        	var mappedColumns = [{
				key:"columnname",
				label:"Column Name"
			},{
				key:"alfrescoproperty",
				label:"Property Name"
			},{
				key:"propertyqname",
				label:"QName",
				hidden:true
			},{
				key:"recordidcolumn",
				label:"Column ID",
				hidden:true
			},{
				key:"recordidproperty",
				label:"Property ID",
				hidden:true
			},{
				key:"ismultivalued",
				label:"Is Multivalued?"
			},{
				key:"actions",
				label:"",
				formatter:function(el,oRecord,oColumn,oData){
					el.innerHTML = "<a href='#delete' ><img class='actions' src='/migrator/res/images/delete.png'/></a>";
				}
			}];
			var mapDataSource = new YAHOO.util.LocalDataSource([{
        			 "columnname":"",
        			 "alfrescoproperty":"",
        			 "recordidcolumn":"",
        			 "recordidproperty":"",
        			 "propertyqname":""
        		 }]);
        	me.widgets.mapDataTable = new YAHOO.widget.ScrollingDataTable("mapTable", mappedColumns, mapDataSource,{
				initialLoad: false,
        		MSG_EMPTY:"No Mapped Metadata",
        		width:"300px", 
        		height:"400px"
        	});
        	me.widgets.mapDataTable.subscribe("linkClickEvent",function(e){
				var operation = e.target.hash.substr(1).toLowerCase();
				var tableRecord = me.widgets.mapDataTable.getRecord(e.target);
				if(operation=="delete"){
					var csvColumnIds = tableRecord.getData("recordidcolumn");
					var csvColumnIdsSplit = csvColumnIds.split(',');
					for(var index=0;index<csvColumnIdsSplit.length;index++){
						var radioCtrl = Dom.get(csvColumnIdsSplit[index]+"-checkbox");
						radioCtrl.disabled=false;
					}
					var checkBoxCtrl = Dom.get(tableRecord.getData("recordidproperty")+"-checkbox");
					checkBoxCtrl.disabled=false;
					me.widgets.mapDataTable.deleteRow(tableRecord);
				}
			});
        	YAHOO.util.Event.addListener(fileTypeCtrl,"change",function(e){
				me.widgets.typeDataTable.showTableMessage("<img src='/migrator/res/images/alfresco-logo-vector-1.svg' class='circle'/>");
				var selectCtrl = e.target;
				var fileTypeValue = selectCtrl.options[selectCtrl.selectedIndex].value;
				me.widgets.typeDataTable.getDataSource().sendRequest(fileTypeValue.replace(":","_"),
					{
						success : me.widgets.typeDataTable.onDataReturnReplaceRows,
						scope : me.widgets.typeDataTable

					});
			});
			YAHOO.util.Event.addListener(aspectCtrl,"change",function(e){
				me.widgets.typeDataTable.showTableMessage("<img src='/migrator/res/images/alfresco-logo-vector-1.svg' class='circle'/>");
				var selectCtrl = e.target;
				var aspectValue = selectCtrl.options[selectCtrl.selectedIndex].value;
				me.widgets.typeDataTable.getDataSource().sendRequest(aspectValue.replace(":","_"),
					{
						success : me.widgets.typeDataTable.onDataReturnAppendRows,
						scope : me.widgets.typeDataTable

					});
			});
			YAHOO.util.Event.addListener(deleteIcon,"click",function(e){
				physicalFileColumn.innerHTML = "";
				Dom.addClass(deleteIcon,"hidden");
			});
			YAHOO.util.Event.addListener(deleteFileType,"click",function(e){
				Dom.get("typevalueSelected").innerHTML = "";
				Dom.addClass(deleteFileType,"hidden");
				fileTypeCtrl.disabled=false;
				me.widgets.lockTypeBtn.set("disabled",false);
				var records = me.widgets.typeDataTable.getRecordSet().getRecords();
				if(records.length>0){
					me.widgets.typeDataTable.deleteRows(0,records.length);
				}
				me.widgets.mapBtn.set("disabled",true);
				me.widgets.saveMapBtn.set("disabled",true);
				var maprecords = me.widgets.mapDataTable.getRecordSet().getRecords();
				me.widgets.mapDataTable.deleteRows(0,maprecords.length);
				var csvrecords = me.widgets.csvDataTable.getRecordSet().getRecords();
				for(var index=0;index<csvrecords.length;index++){
					var recId = csvrecords[index].getId();
					var checkCtrl = Dom.get(recId+"-checkbox");
					checkCtrl.disabled=false;
					checkCtrl.checked=false;
				}
				fileTypeCtrl.value="";
			});
			
			YAHOO.util.Event.addListener(csvFileCtrl,"change",function(e){
				me.widgets.csvDataTable.showTableMessage("<center><img src='/migrator/res/images/alfresco-logo-vector-1.svg' class='circle'/></center>");
				var fileCtrl = e.target;
				console.log(fileCtrl.files);
				var formData = new FormData();
				formData.append("filedata",fileCtrl.files[0]);
				var uploadReq = new XMLHttpRequest();
				uploadReq.upload.addEventListener("load", function(e){
					 if( uploadReq.readyState!=4){
						 uploadReq.onreadystatechange=function(e) {
							if (uploadReq.readyState == XMLHttpRequest.DONE && uploadReq.status==200) {
								console.log(uploadReq);
								var resJson = JSON.parse(uploadReq.response);
								console.log(resJson);
								var columnsArray = resJson.columns;
								var records =me.widgets.csvDataTable.getRecordSet().getRecords();
								if(records.length>0){
									me.widgets.csvDataTable.deleteRows(0,records.length);
								}
								for(var index in columnsArray){
									var dataTableRecord = {
        								 columnname:columnsArray[index].columnname
        						 	};
        						 	me.widgets.csvDataTable.addRow(dataTableRecord);
								}
							 }else if (uploadReq.status ==400) {
								 console.log(uploadReq);
    						 }
						 }
					 };
				 }, false);
				uploadReq.open("POST",  "retrievefileheader",true);
				uploadReq.send(formData);
				console.log(formData);
				fileCtrl.value="";
			});
			/*me.widgets.csvDataTable.subscribe("checkboxClickEvent",function(e){
				var target = e.target;
				var record = me.widgets.csvDataTable.getRecord(target);
				var recordId = record.getId();
				var checkBoxCtrl = Dom.get(recordId+"-checkbox");
				if(checkBoxCtrl.checked){
					me.columnSelected[recordId] = record;
				}else{
					checkBoxCtrl.checked=false;
					if(me.columnSelected.hasOwnProperty(recordId)){
						delete me.columnSelected[recordId];
					}
				}
				console.log(checkBoxCtrl.checked);
			});*/
			me.widgets.csvDataTable.subscribe("linkClickEvent",function(e){
				var operation = e.target.hash.substr(1).toLowerCase();
				var tableRecord = me.widgets.csvDataTable.getRecord(e.target);
				if(operation=="declarefile"){
					var physicalFileValue = physicalFileColumn.innerHTML;
					if(physicalFileValue==null || physicalFileValue==undefined || physicalFileValue=="" || physicalFileValue.length==0){
						physicalFileColumn.innerHTML = tableRecord.getData("columnname");
						Dom.removeClass(deleteIcon,"hidden");
					}else{
						//popupPanel("Alert","Physical File column already defined");
						Migrator.utils.PopupManager.displayMessage(document.body,{
							text:"Physical File column already defined"
						});
					}
				}
			});
			me.widgets.typeDataTable.subscribe("rowClickEvent",function(e){
				var target = e.target;
				var record = me.widgets.typeDataTable.getRecord(target);
				var recordId = record.getId();
				var checkBoxCtrl = Dom.get(recordId+"-checkbox");
				if(checkBoxCtrl.disabled==false){
					checkBoxCtrl.checked=true;
					me.propertyQnameSelected=record;
					me.propertySelected=record;
				}else{
					//popupPanel("Alert","property "+record.getData("propertytitle")+" already mapped");
					Migrator.utils.PopupManager.displayMessage(document.body,{
							text:"property "+record.getData("propertytitle")+" already mapped"
						});
				}
			});
			me.widgets.csvDataTable.subscribe("rowClickEvent",function(e){
				var target = e.target;
				var record = me.widgets.csvDataTable.getRecord(target);
				var recordId = record.getId();
				var checkBoxCtrl = Dom.get(recordId+"-checkbox");
				if(checkBoxCtrl.disabled==false && checkBoxCtrl.checked==false){
					checkBoxCtrl.checked=true;
					checkBoxCtrl.click();
				}else if(checkBoxCtrl.disabled==false && checkBoxCtrl.checked==true){
					checkBoxCtrl.checked=false;
					checkBoxCtrl.click();
				}else{
					//popupPanel("Alert","property "+record.getData("propertytitle")+" already mapped");
					Migrator.utils.PopupManager.displayMessage(document.body,{
							text:"property "+record.getData("columnname")+" already mapped"
						});
				}
			});
		},
		saveTemplate :function(){
			me.removeFocusClass(me.widgets.saveMapBtn);
			var physicalFileColumn = Dom.get("physicalFile");
			var records = me.widgets.mapDataTable.getRecordSet().getRecords();
			var templateNameCtrl = Dom.get("templateName");
			var fileTypeSelected = Dom.get("alfrescotype");
			var fileTypeValue = fileTypeSelected.options[fileTypeSelected.selectedIndex].value;
			var physicalFileValue = physicalFileColumn.innerHTML;
			if(templateNameCtrl.value==""||templateNameCtrl.value==undefined||templateNameCtrl.value==null){
				//popupPanel("Alert","Please define template name");
				Migrator.utils.PopupManager.displayMessage(document.body,{
						text:"Please define template name"
						});
				templateNameCtrl.style.border="1px solid red";
			}else if(fileTypeValue==""||fileTypeValue==undefined||fileTypeValue==null){
				//popupPanel("Alert","Please select filetype");
				Migrator.utils.PopupManager.displayMessage(document.body,{
						text:"Please select filetype"
						});
				fileTypeSelected.style.border="1px solid red";
				templateNameCtrl.style.border=null;
			}else if(records.length==0){
				//popupPanel("Alert","Please define metadata mapping");
				Migrator.utils.PopupManager.displayMessage(document.body,{
						text:"Please define metadata mapping"
						});
				fileTypeSelected.style.border=null;
			}else if(physicalFileValue==""||physicalFileValue==undefined||physicalFileValue==null || physicalFileValue.length==0){
				//popupPanel("Alert","Please select which column defines physical file location");
				Migrator.utils.PopupManager.displayMessage(document.body,{
						text:"Please select which column defines physical file location"
						});
				fileTypeSelected.style.border=null;
			}else{
				var mappedQNameCtrl = Dom.get("typevalueSelected");
				//var dropDownCtrl = Dom.get("alfrescotype");
				if(mappedQNameCtrl.innerHTML==""||mappedQNameCtrl.innerHTML==null||mappedQNameCtrl.innerHTML==undefined){
					//popupPanel("Alert","Please lock FileType before saving");
					Migrator.utils.PopupManager.displayMessage(document.body,{
						text:"Please lock FileType before saving"
						});
				}else{
					var waitMsg = Migrator.utils.PopupManager.displayMessage(document.body,{
						text:"Please wait while saving template",
						spanClass:"wait"
						});
					var templateObj = {};
					templateObj["template"]={};
					templateObj["templatedetails"]=[];
					templateObj["template"]["templatename"] = templateNameCtrl.value;
					templateObj["template"]["propertyqname"] = mappedQNameCtrl.innerHTML;
					templateObj["template"]["propertytitle"] = fileTypeSelected.options[fileTypeSelected.selectedIndex].text;
					for(var index=0;index<records.length;index++){
						var tRecord = records[index];
						var detail = {
							"propertytitle":tRecord.getData("alfrescoproperty"),
							"propertyqname":tRecord.getData("propertyqname"),
							"columnname":tRecord.getData("columnname")
						};
						templateObj["templatedetails"].push(detail);
					}
					console.log(templateObj);
					var sUrl = "savetemplate";
					var formData = new FormData();
					formData.append("templateDefinition",JSON.stringify(templateObj));
					formData.append("columnname",physicalFileValue);
					var templateReq = new XMLHttpRequest();
					templateReq.upload.addEventListener("load", function(e){
						 if( templateReq.readyState!=4){
							 templateReq.onreadystatechange=function(e) {
								if (templateReq.readyState == XMLHttpRequest.DONE && templateReq.status==200) {
									console.log(templateReq);
									var resJson = JSON.parse(templateReq.response);
									console.log(resJson);
									//popupPanel(resJson.status,resJson.message);
									Migrator.utils.PopupManager.displayMessage(document.body,{
										text:resJson.message
										});
									me.metadataMappingReset();
								 }else if (templateReq.readyState == XMLHttpRequest.DONE && (templateReq.status ==400 || templateReq.status ==500)) {
									 console.log(templateReq);
									 var resJson = JSON.parse(templateReq.responseText);
									 //popupPanel(resJson.status,resJson.error);
									 waitMsg.hide();
									 Migrator.utils.PopupManager.displayPrompt(document.body,resJson.status,resJson.error);
		    					 }
							 }
						 };
					 }, false);
					templateReq.open("POST",  sUrl,true);
					templateReq.send(formData);
				}
					
			}
		},
		metadataMappingReset : function(){
			var templateNameCtrl = Dom.get("templateName");
			var fileTypeSelected = Dom.get("alfrescotype");
			var mappedQNameCtrl = Dom.get("typevalueSelected");
			var aspectCtrl = Dom.get("alfrescoaspect");
			var physicalFileColumn = Dom.get("physicalFile");
			var deleteIcon = Dom.get("deleteColumn");
			var deleteFileType = Dom.get("deleteFileType");
			var csvrecords =me.widgets.csvDataTable.getRecordSet().getRecords();
			if(csvrecords.length>0){
				me.widgets.csvDataTable.deleteRows(0,csvrecords.length);
			}
			var records = me.widgets.mapDataTable.getRecordSet().getRecords();
			me.widgets.mapDataTable.deleteRows(0,records.length);
			var filetypeRecords =me.widgets.typeDataTable.getRecordSet().getRecords();
			if(filetypeRecords.length>0){
				me.widgets.typeDataTable.deleteRows(0,filetypeRecords.length);
			}
			var selectCtrl = Dom.get("alfrescotype");
			selectCtrl.disabled=false;
			selectCtrl.value="";
			me.widgets.mapBtn.set("disabled",true);
			me.widgets.saveMapBtn.set("disabled",true);
			templateNameCtrl.value="";
			aspectCtrl.value="";
			mappedQNameCtrl.innerHTML="";
			fileTypeSelected.value="";
			physicalFileColumn.value="";
			Dom.addClass(deleteFileType,"hidden");
			Dom.addClass(deleteIcon,"hidden");
		},
		mapBtnClick :function(){
			var columnsString = JSON.stringify(me.columnSelected);
			if(me.columnSelected=="" || me.columnSelected==null || me.columnSelected==undefined || columnsString.length==2){
				Migrator.utils.PopupManager.displayMessage(document.body,{
					text:"Select CSV column(s)"
					});
			}else if(me.propertySelected=="" || me.propertySelected==null || me.propertySelected==undefined){
				Migrator.utils.PopupManager.displayMessage(document.body,{
					text:"Select Alfresco property"
					});
			}else{
				console.log(me.columnSelected);
				console.log(me.propertySelected);
				console.log(columnsString.length);
				me.ismultiValuedPanel(me.columnSelected,me.propertySelected);
			}
		},
		ismultiValuedPanel :function(csvrecord,propertyrecord){
			var multivalueDialog = new YAHOO.widget.Panel("messagefailedpanel", { width:"320px", visible:true, draggable:true, close:false,zindex:4,keylisteners:[escapeListener]} );
			var escapeListener = new YAHOO.util.KeyListener(document,
            {
               	keys: YAHOO.util.KeyListener.KEY.ESCAPE
            },
            {
               	fn: function(id, keyEvent)
               	{
                   	this.destroy();
                },
                scope: multivalueDialog,
                correctScope: true
            });
            escapeListener.enable();
			multivalueDialog.setHeader("<center>Is Property Multivalued?</center><div id='close' class='close'></div>");
			multivalueDialog.setBody("<center><input type='checkbox' id='multivalued'>is multivalued?</input></center>");
			multivalueDialog.setFooter("<center><table><tr><td><div id='save'></div></td></tr></table>");
			multivalueDialog.showEvent.subscribe(function(){
				var saveBtn = new YAHOO.widget.Button("save",{
					label:"Ok"
				}); 
				var closeCtrl = Dom.get("close");
				YAHOO.util.Event.addListener(closeCtrl,"click",function(e){
					multivalueDialog.destroy();
				});
				saveBtn.on("click",function(){
					var multiValuedCheckbox = Dom.get("multivalued");
					var ischecked = false;
						
					if(multiValuedCheckbox.checked==null||multiValuedCheckbox.checked==undefined||multiValuedCheckbox.checked==""){
						ischecked=false
					}else{
						ischecked = true;
					}
					var csvColumns = "";
					var recordIds = "";
					for(var key in csvrecord){
						csvColumns+=csvrecord[key].getData("columnname")+"+";
						recordIds+=key+",";
					}
					var dataTableRecord = {
		        	 	"columnname":csvColumns.replace(/(^\+)|(\+$)/g, ''),
		        	 	"alfrescoproperty":propertyrecord.getData("propertytitle"),
		        	 	"recordidcolumn":recordIds.replace(/(^,)|(,$)/g, ''),
		        	 	"recordidproperty":propertyrecord.getId(),
		        	 	"propertyqname":propertyrecord.getData("propertyqname"),
		        	 	"ismultivalued":ischecked
		        	};
		        	me.widgets.mapDataTable.addRow(dataTableRecord);
		        	for(var key in csvrecord){
						var checkbox = Dom.get(key+"-checkbox");
						checkbox.checked=false;
						checkbox.disabled=true;
					}
					Dom.get(propertyrecord.getId()+"-checkbox").checked=false;
					Dom.get(propertyrecord.getId()+"-checkbox").disabled=true;
					me.columnSelected={};
					me.propertySelected="";
					multivalueDialog.destroy();
				});
			});
			multivalueDialog.render(document.body);
			multivalueDialog.center();
		},
		lockTypeButtonFn :function(){
			me.removeFocusClass(me.widgets.lockTypeBtn);
			var selectCtrl = Dom.get("alfrescotype");
			var fileTypeValue = selectCtrl.options[selectCtrl.selectedIndex].value;
			if(fileTypeValue==""||fileTypeValue==null||fileTypeValue==undefined){
				Migrator.utils.PopupManager.displayMessage(document.body,{
					text:"select filetype to lock"
					});
			}else{
				var spanCtrl = Dom.get("typevalueSelected");
				spanCtrl.innerHTML = fileTypeValue;
				selectCtrl.disabled=true;
				me.widgets.mapBtn.set("disabled",false);
				me.widgets.saveMapBtn.set("disabled",false);
				Dom.removeClass(deleteFileType,"hidden");
				me.widgets.lockTypeBtn.set("disabled",true);
			}
		},
		validateActiveMQDetails :function(){
			me.removeFocusClass(me.widgets.amqvalidateBtn);
			var loading = Dom.get("validatingDivAmq");
			Dom.removeClass(loading,"hidden");
			//var loadingCtrl = Dom.get("validatingDiv");
			//Dom.removeClass(loadingCtrl,"hidden");
			var amqValhandleSuccess = function(res){
				var resJson = JSON.parse(res.responseText);
				console.log(resJson);
				Dom.addClass(loading,"hidden");
				me.showAMQTickMark();
			};
			var amqValhandleFailure = function(res){
				var resJson = JSON.parse(res.responseText);
				console.log(resJson);
				Dom.addClass(loading,"hidden");
				me.showAMQCrossMark();
				failMsgCtrl.innerHTML = resJson.message;
			};
			var amqValidationCallback ={
				success: amqValhandleSuccess,
				failure: amqValhandleFailure
			};
			var activeMQValidateURL = "validateactivemq?appname=activemq";
			var amqDetailsReq = YAHOO.util.Connect.asyncRequest('GET', activeMQValidateURL, amqValidationCallback);
		},
		saveAlfrescoDetails : function(){
			me.removeFocusClass(me.widgets.alfrescoSaveBtn);
			me.hideCrossMark();
			me.hideTickMark();
			var configIdCtrl = Dom.get("alfconfigid");
			var protocolCtrl = Dom.get("alfprotocol");
			var hostCtrl = Dom.get("alfhost");
			var portCtrl = Dom.get("alfport");
			var userNameCtrl = Dom.get("alfusername");
			var passwordCtrl = Dom.get("alfpassword");
			var selectedProtocol = protocolCtrl.options[protocolCtrl.selectedIndex].value;
			var loading = Dom.get("savingDiv");
			var selectedPort = portCtrl.options[portCtrl.selectedIndex].value;
			var configIdValue = configIdCtrl.value;
			var hasalfdetailsCtrl = Dom.get("hasalfdetails");
			if(selectedProtocol==""||selectedProtocol==null||selectedProtocol==undefined){
				Dom.addClass(protocolCtrl,"error");
				Migrator.utils.PopupManager.displayMessage(document.body,{
					text:"Please select protocol"
					});
			}else if(hostCtrl.value==""||hostCtrl.value==null||hostCtrl.value==undefined){
				Dom.removeClass(protocolCtrl,"error");
				Dom.addClass(hostCtrl,"error");
				Migrator.utils.PopupManager.displayMessage(document.body,{
					text:"Please provide host"
					});
			}else if(selectedPort==""||selectedPort==null||selectedPort==undefined){
				Dom.removeClass(hostCtrl,"error");
				Dom.addClass(portCtrl,"error");
				Migrator.utils.PopupManager.displayMessage(document.body,{
					text:"Please select port"
					});
			}else if(userNameCtrl.value==""||userNameCtrl.value==null||userNameCtrl.value==undefined){
				Dom.removeClass(portCtrl,"error");
				Dom.addClass(userNameCtrl,"error");
				Migrator.utils.PopupManager.displayMessage(document.body,{
					text:"Please provide username"
					});
			}else if(passwordCtrl.value==""||passwordCtrl.value==null||passwordCtrl.value==undefined){
				Dom.removeClass(userNameCtrl,"error");
				Dom.addClass(passwordCtrl,"error");
				Migrator.utils.PopupManager.displayMessage(document.body,{
					text:"Please provide password"
					});
			}else{
				Dom.removeClass(passwordCtrl,"error");
				Dom.removeClass(hostCtrl,"error");
				Dom.removeClass(protocolCtrl,"error");
				Dom.removeClass(loading,"hidden");
				Dom.removeClass(portCtrl,"error");
				var sUrl = "savealfconf";
				var handleSuccess = function(res){
					var resJson = JSON.parse(res.responseText);
					console.log(resJson);
					Dom.addClass(loading,"hidden");
					me.showTickSavedMark();
				};
				var handleFailure = function(res){
					console.log(res);
					Dom.addClass(loading,"hidden");
					Migrator.utils.PopupManager.displayPrompt(document.body,resJson.status,resJson.message);
				};
				var callback ={
					success:handleSuccess,
					failure: handleFailure
				};
				console.log(hasalfdetailsCtrl);
				var postData = "protocol="+selectedProtocol+"&port="+selectedPort+"&host="+hostCtrl.value+"&appname=alfresco&username="+userNameCtrl.value+"&password="+passwordCtrl.value+"&hasalfconfig="+hasalfdetailsCtrl.value;
				if(!(configIdValue==null||configIdValue==undefined||configIdValue=="")){
					postData+="&configid="+configIdValue;
				}
				var request = YAHOO.util.Connect.asyncRequest('POST', sUrl, callback, postData);
			}
		},
		validateAlfrescoDetails: function(){
			var failMsgCtrl = Dom.get("failMsg");
			me.hideCrossMark();
			me.hideTickMark();
			me.hideTickSavedMark();
			var loading = Dom.get("validatingDiv");
			Dom.removeClass(loading,"hidden");
			var loadingCtrl = Dom.get("validatingDiv");
			Dom.removeClass(loadingCtrl,"hidden");
			me.removeFocusClass(me.widgets.alfrescovalidateBtn);
			var alfValhandleSuccess = function(res){
				var resJson = JSON.parse(res.responseText);
				console.log(resJson);
				Dom.addClass(loading,"hidden");
				me.showTickMark();
			};
			var alfValhandleFailure = function(res){
				var resJson = JSON.parse(res.responseText);
				console.log(resJson);
				Dom.addClass(loading,"hidden");
				me.showCrossMark();
				failMsgCtrl.innerHTML = resJson.message;
			};
			var alfValidationCallback ={
				success:alfValhandleSuccess,
				failure: alfValhandleFailure
			};
			var alfValidateURL = "validatealf?appname=alfresco";
			var alfDetailsReq = YAHOO.util.Connect.asyncRequest('GET', alfValidateURL, alfValidationCallback);
		},
		removeFocusClass :function(el){
			Dom.removeClass(el,"yui-button-focus");
			Dom.removeClass(el,"yui-push-button-focus");
		},
		showAMQTickSavedMark : function(){
			var amqcheckCircleCTrl = Dom.get("amqcheckcircle");
			var amqtickSaveCtrl = Dom.get("tickSave");
			var amqsuccessSaveCtrl = Dom.get("successSave");
			Dom.removeClass(amqsuccessSaveCtrl,"hidden");
			Dom.addClass(amqtickSaveCtrl,"animated-check");
			Dom.removeClass(amqcheckCircleCTrl,"hidden");
		},
		showTickSavedMark : function(){
			var successSaveCtrl = Dom.get("successSave");
			var tickSaveCtrl = Dom.get("tickSave");
			Dom.removeClass(successSaveCtrl,"hidden");
			Dom.addClass(tickSaveCtrl,"animated-check");
			Dom.removeClass(tickSaveCtrl,"hidden");
		},
		hideTickSavedMark : function(){
			var successSaveCtrl = Dom.get("successSave");
			var tickSaveCtrl = Dom.get("tickSave");
			Dom.addClass(successSaveCtrl,"hidden");
			Dom.removeClass(tickSaveCtrl,"animated-check");
			Dom.addClass(tickSaveCtrl,"hidden");
		},
		hideAMQTickSavedMark : function(){
			var amqsuccessSaveCtrl = Dom.get("successSave");
			var amqtickSaveCtrl = Dom.get("tickSave");
			Dom.addClass(amqsuccessSaveCtrl,"hidden");
			Dom.removeClass(amqtickSaveCtrl,"animated-check");
			Dom.addClass(amqtickSaveCtrl,"hidden");
		},
		showTickMark : function(){
			var successCtrl = Dom.get("successTable");
			var tickCtrl = Dom.get("tick");
			Dom.removeClass(successCtrl,"hidden");
			Dom.addClass(tickCtrl,"animated-check");
			Dom.removeClass(tickCtrl,"hidden");
		},
		showAMQTickMark : function(){
			var amqsuccessCtrl = Dom.get("successTableAmq");
			var amqtickCtrl = Dom.get("amqtick");
			Dom.removeClass(amqsuccessCtrl,"hidden");
			Dom.addClass(amqtickCtrl,"animated-check");
			Dom.removeClass(amqtickCtrl,"hidden");
		},
		showCrossMark : function(){
			var failMsgTable = Dom.get("failTable");
			var crossCtrl = Dom.get("checsvg");
			var checkCircleCTrl = Dom.get("checkcircle");
			Dom.removeClass(failMsgTable,"hidden");
			Dom.addClass(crossCtrl,"checkmark");
			Dom.addClass(checkCircleCTrl,"checkmark_circle");
		},
		showAMQCrossMark : function(){
			var amqcheckCircleCTrl = Dom.get("amqcheckcircle");
			var amqcrossCtrl = Dom.get("checsvgamq");
			var amqfailMsgTable = Dom.get("failTableAmq");
			Dom.removeClass(amqfailMsgTable,"hidden");
			Dom.addClass(amqcrossCtrl,"checkmark");
			Dom.addClass(amqcheckCircleCTrl,"checkmark_circle");
		},
		hideTickMark : function(){
			var tickCtrl = Dom.get("tick");
			var successCtrl = Dom.get("successTable");
			Dom.addClass(successCtrl,"hidden");
			Dom.removeClass(tickCtrl,"animated-check");
			Dom.addClass(tickCtrl,"hidden");
		},
		hideAMQTickMark : function(){
			var amqtickCtrl = Dom.get("amqtick");
			var amqsuccessCtrl = Dom.get("successTableAmq");
			Dom.addClass(amqsuccessCtrl,"hidden");
			Dom.removeClass(amqtickCtrl,"animated-check");
			Dom.addClass(amqtickCtrl,"hidden");
		},
		hideCrossMark : function(){
			var crossCtrl = Dom.get("checsvg");
			var failMsgTable = Dom.get("failTable");
			var checkCircleCTrl = Dom.get("checkcircle");
			Dom.addClass(failMsgTable,"hidden");
			Dom.removeClass(crossCtrl,"checkmark");
			Dom.removeClass(checkCircleCTrl,"checkmark_circle");
		},
		hideAMQCrossMark : function(){
			var amqcheckCircleCTrl = Dom.get("amqcheckcircle");
			var amqcrossCtrl = Dom.get("checsvgamq");
			var amqfailMsgTable = Dom.get("failTableAmq");
			Dom.addClass(amqfailMsgTable,"hidden");
			Dom.removeClass(amqcrossCtrl,"checkmark");
			Dom.removeClass(amqcheckCircleCTrl,"checkmark_circle");
		}
	};
})();
