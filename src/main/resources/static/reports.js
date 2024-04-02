(function()
{
	var me;
	var Dom = YAHOO.util.Dom,
    Event = YAHOO.util.Event;
	Migrator.component.reports = function (){
		me=this;
		me.widgets={};
      	me.onComponentsLoaded();
      	//YAHOO.Bubbling.on("folderrefselected", me.onFolderRefSelected, this);
      	return this;
   	};
   	Migrator.component.reports.prototype ={
		options:{
			urlcontext:""
		},
		setOptions: function(obj){
	        me.options = YAHOO.lang.merge(me.options, obj);
	        return this;
	    },
		max:100,
		page:0,
		statusFilter:"",
		selectedrecords:[],
		reupload:false,
		exporttype:"all",
		onComponentsLoaded:function(){
			Event.onContentReady("reportbody", me.onReady, this, true);
		},
		onReady :function(){
			this.setupdatatable();
			this.handleCsvCtrl();
			this.setupAlfrescoTabCtrls();
			this.setupStatusTable();
			YAHOO.util.Event.addListener("success","click",this.radioButtonEvent);
			YAHOO.util.Event.addListener("failed","click",this.radioButtonEvent);
		},
		setupAlfrescoTabCtrls :function(){
			me.widgets.folderButton = new YAHOO.widget.Button("folderButton",{
				label : "..."
			});
			me.widgets.loadDetails = new YAHOO.widget.Button("loadDetails",{
				label : "Fetch"
			});
			me.widgets.folderButton.on('click',this.loadAlfrescoTree);
			me.widgets.loadDetails.on('click',this.fetchDetailsAlfresco);
			var reconsileColumns = [{
				key:"name",
				label:"Name"
			},{
				key:"count",
				label:"Alfresco Count"
			}];
			me.widgets.reconsileDataSource = new YAHOO.util.DataSource("fetchcsvcount?nodeid=",{
				responseType : YAHOO.util.DataSource.TYPE_JSON,
				responseSchema : {
				resultsList : "details",
				fields : ["name", "count"]
				}
			}); 
			me.widgets.reconsileDataTable = new YAHOO.widget.DataTable("reconsileTable", reconsileColumns, me.widgets.reconsileDataSource,{
				initialLoad: false,
        		MSG_EMPTY:"No records migrated"
        	});
		},
		loadAlfrescoTree(){
			var destNameCtrl = Dom.get("folderName");
			var folderRefCtrl = Dom.get("folderRef");
			var destinationPanel = Migrator.utils.PopupManager.destinationPanel(destNameCtrl,folderRefCtrl);
		},
		fetchDetailsAlfresco :function(){
			var folderRefCtrl = Dom.get("folderRef");
			if(folderRefCtrl.value==""||folderRefCtrl.value==undefined||folderRefCtrl.value==null){
				Migrator.utils.PopupManager.displayMessage(document.body,{
					text:"Please Select folder to display count"
				});
			}else{
				me.widgets.reconsileDataTable.showTableMessage("<img src='/migrator/res/images/alfresco-logo-vector-1.svg' class='circle'/>");
				me.widgets.reconsileDataTable.getDataSource().sendRequest(folderRefCtrl.value,
				{
					success : me.widgets.reconsileDataTable.onDataReturnReplaceRows,
					scope : me.widgets.reconsileDataTable
				});
			}
		},
		radioButtonEvent :function(e){
			var statusValue = e.target.value;
			me.page=0;
			me.max=100;
			me.statusFilter=statusValue;
			var URL = me.getFilterURL(statusValue,me.page,me.max);
			me.widgets.clearBtn.set("disabled",false);
			me.widgets.recordDataTable.showTableMessage("<img src='/migrator/res/images/alfresco-logo-vector-1.svg' class='circle'/>");
			me.widgets.recordDataTable.getDataSource().sendRequest(URL,
			{
				success : me.widgets.recordDataTable.onDataReturnReplaceRows,
				scope : me.widgets.recordDataTable
			});
		},
		getFilterURL :function(statusValue,page,max){
			var selectCtrl = Dom.get("csvctrl");
			var csvValue = selectCtrl.options[selectCtrl.selectedIndex].value;
			var URL = csvValue+"&skip="+page+"&max="+max+"&status=200&issuccess=";
			if(statusValue=="success"){
				URL+="true";
			}else{
				URL+="false";
			}
			return URL;
		},
		removeFocusClass :function(el){
			Dom.removeClass(el,"yui-button-focus");
			Dom.removeClass(el,"yui-push-button-focus");
		},
		handleCsvCtrl :function(){
			var piechartCtrl = Dom.get("piechartctrl");
			var calculateTimeCtrl = Dom.get("calculatetime");
			var exportCsvCtrl = Dom.get("exportcsv");
			var reuploadFailedCtrl = Dom.get("reuploadfailed");
			YAHOO.util.Event.addListener(piechartCtrl,"click",function(){
				me.displayPieChart();
			});
			me.widgets.clearBtn = new YAHOO.widget.Button("clear",{
				label : "Clear Filter"
			});
			me.widgets.clearBtn.set("disabled",true);
			me.widgets.clearBtn.on('click',this.clearBtnFn);
			var tabView = new YAHOO.widget.TabView('configtabview');
			var successRadio = Dom.get("success");
			var failedRadio = Dom.get("failed");
			var csvCtrl = Dom.get("csvctrl");
			var deleteallCtrl = Dom.get("deleteall");
			var csvDeleteCtrl = Dom.get("deletecsv");
			Event.addListener(csvCtrl,"change",function(e){
				var selectCtrl = e.target;
				var csvValue = selectCtrl.options[selectCtrl.selectedIndex].value;
				var selectallCtrl = Dom.get("selectall");
				me.max=100;
				me.page=0;
				var records = me.widgets.recordDataTable.getRecordSet().getRecords();
				if(csvValue==null||csvValue==undefined||csvValue==""){
					Migrator.utils.PopupManager.displayMessage(document.body,{
						text:"Please Select CSV to list migrated documents"
						});
					successRadio.disabled=true;
					failedRadio.disabled=true;
					me.widgets.clearBtn.set("disabled",true);
					//me.widgets.recordDataTable.deleteRows(0,records.length);
					me.widgets.recordDataTable.getDataSource().sendRequest("&skip="+me.page+"&max="+me.max,
						{
							success : me.widgets.recordDataTable.onDataReturnReplaceRows,
							scope : me.widgets.recordDataTable
	
						});
					me.widgets.recordDataTable.showTableMessage("Please Select CSV to list migrated documents");
					Dom.addClass(piechartCtrl,"hidden");
					Dom.addClass(deleteallCtrl,"hidden");
					selectallCtrl.checked=false;
					selectallCtrl.disabled=true;
				}else{
					successRadio.disabled=false;
					failedRadio.disabled=false;
					successRadio.checked=false;
					failedRadio.checked=false;
					me.widgets.clearBtn.set("disabled",true);
					me.page=0;
					me.max=100;
					me.widgets.epaginator.setStartIndex(me.page,true);
					me.widgets.recordDataTable.showTableMessage("<img src='/migrator/res/images/alfresco-logo-vector-1.svg' class='circle'/>");
					//recordDataTable.showTableMessage("<img src='/migrator/res/images/ajaxloading.gif' class='ajaxloading'/>");
					me.widgets.recordDataTable.getDataSource().sendRequest(csvValue+"&skip="+me.page+"&max="+me.max,
						{
							success : me.widgets.recordDataTable.onDataReturnReplaceRows,
							scope : me.widgets.recordDataTable
	
						});
					me.fetchCounts(csvValue);
					pieURL="/migrator/getpiechartdata?csvid="+csvValue;
					Dom.removeClass(piechartCtrl,"hidden");
					selectallCtrl.disabled=false;
				}
			});
			Event.addListener(deleteallCtrl,"click",function(e){
				var selectAll = Dom.get("selectall");
				var deleteallCtrl = Dom.get("deleteall");
				var csvDeleteCtrl = Dom.get("deletecsv");
				if(me.selectedrecords.length>0){
					me.widgets.recordDataTable.showTableMessage("<img src='/migrator/res/images/alfresco-logo-vector-1.svg' class='circle'/> Please wait while deleting documents...");
					var request = YAHOO.util.Connect;
					var deleteURL = "/migrator/deleterecords?records="+encodeURI(JSON.stringify(me.selectedrecords));
					var handleSuccess = function(res){
						var resJson = JSON.parse(res.responseText);
						console.log(res);
						me.reloadTable();
						selectAll.checked=false;
						me.selectedrecords=[];
						Migrator.utils.PopupManager.displayMessage(document.body,{
							text:resJson.message
						});
					};
					var handleFailure = function(res){
						var resJson = JSON.parse(res.responseText);
						console.log(res);
						me.reloadTable();
						selectAll.checked=false;
						me.selectedrecords=[];
						Migrator.utils.PopupManager.displayMessage(document.body,{
							title:resJson.status+" "+resJson.message
						});
					};
					var callback ={
						success:handleSuccess,
						failure: handleFailure
					};
					request.asyncRequest('DELETE', deleteURL, callback);
				}else{
					Migrator.utils.PopupManager.displayMessage(document.body,{
						text:"No Records Selected to delete"
					});
					selectAll.checked=false;
					me.selectedrecords=[];
				}
			});
			Event.addListener(csvDeleteCtrl,"click",function(e){
				var selectallCtrl = Dom.get("selectall");
				var deleteallCtrl = Dom.get("deleteall");
				var selectCtrl = Dom.get("csvctrl");
				var csvValue = selectCtrl.options[selectCtrl.selectedIndex].value;
				var request = YAHOO.util.Connect;
				var deleteURL = "/migrator/deletecsv?csvid="+csvValue;
				var handleSuccess = function(res){
					var resJson = JSON.parse(res.responseText);
					console.log(res);
					Migrator.utils.PopupManager.displayMessage(document.body,{
						text:resJson.message
					});
					selectallCtrl.checked=false;
					me.selectedrecords=[];
					selectCtrl.remove(selectCtrl.selectedIndex);
					Dom.addClass(csvDeleteCtrl,"hidden");
					Dom.addClass(deleteallCtrl,"hidden");
				};
				var handleFailure = function(res){
					var resJson = JSON.parse(res.responseText);
					console.log(res);
					Migrator.utils.PopupManager.displayMessage(document.body,{
						title:resJson.status+" "+resJson.message
					});
					selectallCtrl.checked=false;
					me.selectedrecords=[];
				};
				var callback ={
					success:handleSuccess,
					failure: handleFailure
				};
				request.asyncRequest('DELETE', deleteURL, callback);
			});
			Event.addListener(calculateTimeCtrl,"click",function(e){
				var request = YAHOO.util.Connect;
				var selectCtrl = Dom.get("csvctrl");
				var csvValue = selectCtrl.options[selectCtrl.selectedIndex].value;
				var calculateUrl = "/migrator/calculatetimetaken?csvid="+csvValue;
				var waitMsg = Migrator.utils.PopupManager.displayMessage(document.body,{
						text:"Please wait while calculating time...",
						spanClass:"wait"
					});
				var handleSuccess = function(res){
					var resJson = JSON.parse(res.responseText);
					console.log(resJson);
					waitMsg.hide();
					Migrator.utils.PopupManager.displayPrompt(document.body,"Migraton Time",resJson.timetaken);
				};
				var handleFailure = function(res){
					var resJson = JSON.parse(res.responseText);
					console.log(resJson);
					Migrator.utils.PopupManager.displayPrompt(document.body,"Migraton Time","Error occured while calculating time taken contact administrator!");
				};
				var callback ={
					success:handleSuccess,
					failure: handleFailure
				};
				request.asyncRequest('GET', calculateUrl, callback);
			});
			Event.addListener(reuploadFailedCtrl,"click",function(e){
				var request = YAHOO.util.Connect;
				var selectCtrl = Dom.get("csvctrl");
				var csvValue = selectCtrl.options[selectCtrl.selectedIndex].value;
				if(csvValue==null||csvValue==undefined||csvValue==""){
					
				}else{
					var waitMsg = Migrator.utils.PopupManager.displayMessage(document.body,{
						text:"Please wait while reuploading failed documents.....",
						spanClass:"wait"
					});
					var reuploadURL = "/migrator/reuploadfailed?csvid="+csvValue;
					var handleSuccess = function(res){
						var resJson = JSON.parse(res.responseText);
						console.log(resJson);
						me.widgets.statusDatasource.setInterval(5000,"", function dataSourceRefesh() {
							//csvDataTable.showTableMessage("<img src='/migrator/images/ajaxloading.gif' class='ajaxloading'/>");
							me.widgets.statusDataTable.showTableMessage("<img src='/migrator/res/images/alfresco-logo-vector-1.svg' class='circle'/>");
							me.widgets.statusDatasource.sendRequest("", {
							success: me.widgets.statusDataTable.onDataReturnReplaceRows,
							scope: me.widgets.statusDataTable
						});}, me.widgets.statusDataTable);
						waitMsg.hide();
						Migrator.utils.PopupManager.displayMessage(document.body,{
							text:"Success 0 Failed 0",
							spanClass:"wait"
						});
						me.reupload=true;
					};
					var handleFailure = function(res){
						var resJson = JSON.parse(res.responseText);
						console.log(resJson);
						waitMsg.hide();
						Migrator.utils.PopupManager.displayPrompt(document.body,"Re Upload status","Error occured while reuploading failed documents contact administrator..!");
					};
					var callback ={
						success:handleSuccess,
						failure: handleFailure
					};
					request.asyncRequest('GET', reuploadURL, callback);
				}
			});
			
			Event.addListener(exportCsvCtrl,"click",function(e){
				var yuiDialog = new YAHOO.widget.Panel("exportpanel", { width:"400px",visible:true,modal:false, draggable:true, close:false,zindex:4,keylisteners:[escapeListener]} );
				var escapeListener = new YAHOO.util.KeyListener(document,
	            {
	                 keys: YAHOO.util.KeyListener.KEY.ESCAPE
	             },
	             {
	               	fn: function(id, keyEvent)
	               	{
	                  	this.destroy();
	               	},
	               	scope: yuiDialog,
	               	correctScope: true
	           	});
	        	escapeListener.enable();
				yuiDialog.setHeader("<center><b>Export CSV</b></center><div id='close' class='close'></div>");
				yuiDialog.setBody("<center><input type='radio' name='exportcsv' value='all' checked id='allcsv'>All</input>&nbsp;<input type='radio' name='exportcsv' value='success' id='successcsv'>Success</input>&nbsp;<input type='radio' name='exportcsv' value='failed' id='failedcsv'>Failed</input></center>");
				yuiDialog.setFooter("<center><table><tr><td><div id='save'></div></td></tr></table>");
				yuiDialog.showEvent.subscribe(function(){
					var saveBtn = new YAHOO.widget.Button("save",{
						label:"Ok"
					}); 
					
					var closeCtrl = Dom.get("close");
					YAHOO.util.Event.addListener(closeCtrl,"click",function(e){
						yuiDialog.destroy();
					});
					var allRadio = Dom.get("allcsv");
					var successRadio = Dom.get("successcsv");
					var failedRadio = Dom.get("failedcsv");
					var checkedRadioEvent = function(e){
						me.exporttype=e.target.value;
					};
					YAHOO.util.Event.addListener(allRadio,"click",checkedRadioEvent);
					YAHOO.util.Event.addListener(successRadio,"click",checkedRadioEvent);
					YAHOO.util.Event.addListener(failedRadio,"click",checkedRadioEvent);
					saveBtn.on("click",function(){
						var selectCtrl = Dom.get("csvctrl");
						var csvValue = selectCtrl.options[selectCtrl.selectedIndex].value;
						var downloadURL = "/migrator/downloadcsv?csvid="+csvValue+"&exporttype="+me.exporttype;
						var downloadAnchor = document.createElement("a");
						downloadAnchor.href=downloadURL;
						downloadAnchor.click();
						console.log(downloadAnchor);
						yuiDialog.destroy();
					});
				});
				yuiDialog.beforeShowEvent.subscribe(function(){
					me.exporttype="all";
				});
				yuiDialog.render(document.body);
				yuiDialog.center();
			});
		},
		reloadTable :function(){
			me.page=0;
			me.max=100;
			var selectCtrl = Dom.get("csvctrl");
			var csvValue = selectCtrl.options[selectCtrl.selectedIndex].value;
			me.widgets.recordDataTable.getDataSource().sendRequest(csvValue+"&skip="+me.page+"&max="+me.max,
			{
				success : me.widgets.recordDataTable.onDataReturnReplaceRows,
				scope : me.widgets.recordDataTable
			});
			me.fetchCounts(csvValue);
		},
		clearBtnFn :function(){
			var successRadio = Dom.get("success");
			var failedRadio = Dom.get("failed");
			me.page=0;
			me.max=100;
			successRadio.checked=false;
			failedRadio.checked=false;
			var selectCtrl = Dom.get("csvctrl");
			var csvValue = selectCtrl.options[selectCtrl.selectedIndex].value;
			me.widgets.recordDataTable.getDataSource().sendRequest(csvValue+"&skip="+me.page+"&max="+me.max,
					{
						success : me.widgets.recordDataTable.onDataReturnReplaceRows,
						scope : me.widgets.recordDataTable
					});
			me.widgets.clearBtn.set("disabled",true);
			me.statusFilter="";
		},
		setupStatusTable : function(){
			var statusColumns = [{
				key:"totalrecords",
				label:"Total"
			},
			{
				key:"success",
				label:"Success"
			},
			{
				key:"failed",
				label:"Failed"
			}];
			me.widgets.statusDatasource = new YAHOO.util.DataSource("reuploadstatus",{
				responseType : YAHOO.util.DataSource.TYPE_JSON,
				responseSchema : {
				resultsList : "list",
				fields : ["success","failed","totalrecords"]
				}
			});
			me.widgets.statusDataTable = new YAHOO.widget.DataTable("reuploadtable", statusColumns, me.widgets.statusDatasource,{
        		MSG_EMPTY:"No Reupload started",
        	});
        	me.widgets.statusDataTable.subscribe("renderEvent",function(e){
				var records = me.widgets.statusDataTable.getRecordSet().getRecords();
				var total = records[0].getData("totalrecords");
				var failed = records[0].getData("failed");
				var success = records[0].getData("success");
				var migrated = failed+success;
				if(me.reupload==true){
					var waitMsg = Migrator.utils.PopupManager.displayMessage(document.body,{
						text:"Success "+success+" failed "+failed,
						spanClass:"wait"
					});
					if(migrated>=total ){
						var selectCtrl = Dom.get("csvctrl");
						var csvValue = selectCtrl.options[selectCtrl.selectedIndex].value;
						me.widgets.statusDatasource.clearAllIntervals();
						waitMsg.hide();
						Migrator.utils.PopupManager.displayPrompt(document.body,"Re Upload status","Reupload finished. Success "+success+" Failed "+failed);
						me.resetCounter();
						/*me.widgets.statusDatasource.sendRequest("", {
							success: me.widgets.statusDataTable.onDataReturnReplaceRows,
							scope: me.widgets.statusDataTable
						});*/
						var URL = csvValue+"&skip="+me.page+"&max="+me.max;
						if(!(me.statusFilter=="" || me.statusFilter==undefined || me.statusFilter==null)){
							URL=me.getFilterURL(me.statusFilter,me.page,me.max);
						}
						me.widgets.recordDataTable.getDataSource().sendRequest(URL,
						{
							success : me.widgets.recordDataTable.onDataReturnReplaceRows,
							scope : me.widgets.recordDataTable
						});
						me.fetchCounts(csvValue);
					}
				}
			});
        	
			
		},
		resetCounter :function(){
			var request = YAHOO.util.Connect;
			var resetURL = "/migrator/resetcounter";
			var handleSuccess = function(res){
				var resJson = JSON.parse(res.responseText);
				console.log(resJson);
				me.reupload=false;
				me.widgets.statusDatasource.sendRequest("", {
					success: me.widgets.statusDataTable.onDataReturnReplaceRows,
					scope: me.widgets.statusDataTable
				});
			};
			var handleFailure = function(res){
				var resJson = JSON.parse(res.responseText);
				console.log(resJson);
				Migrator.utils.PopupManager.displayPrompt(document.body,"Re Upload status","Error occured while resetting counter contact administrator..!");
			};
			var callback ={
				success:handleSuccess,
				failure: handleFailure
			};
			request.asyncRequest('GET', resetURL, callback);
		},
		setupdatatable:function(){
			var recordcolumns = [{
				key:"checkbox",
				label:"<a href='#selectall'><input type='checkbox' disabled='true' id='selectall'/></a>",
				formatter:function(el,oRecord,oColumn,oData){
					el.innerHTML = "<input type='checkbox' id='"+oRecord.getId()+"-delete'/>";
				}
			},
			{
				key:"slno",
				label:"SLNO"
			},{
				key:"csvrecordnumber",
				label:"Record No",
				hidden:true
			},{
				key:"csvfileid",
				label:"CSV File Id",
				hidden:true
			},{
				key:"properties",
				label:"Metadata",
				hidden:true
			},{
				key:"noderef",
				label:"Node Ref",
				hidden:true
			},{
				key:"status",
				label:"Status",
				hidden:true
			},{
				key:"tick",
				label:"Uploaded?",
				formatter:function(el,oRecord,oColumn,oData){
					var statusCode = oRecord.getData("status");
					if(200==statusCode || 201==statusCode){
						el.innerHTML+="<img src='/migrator/res/images/tickright.png' class='statusicon' title='"+oRecord.getData("message")+"'/>";
					}else{
						el.innerHTML+="<img src='/migrator/res/images/error.png' class='statusicon' title='"+oRecord.getData("message")+"'/> ("+statusCode+")";
					}
				}
			},{
				key:"metadatatick",
				label:"Metadata Updated?",
				formatter:function(el,oRecord,oColumn,oData){
					var statusCode = oRecord.getData("propertiesstatus");
					if(200==statusCode || 201==statusCode){
						el.innerHTML+="<img src='/migrator/res/images/tickright.png' class='statusicon' title='"+oRecord.getData("propertiesmessage")+"'/>";
					}else{
						el.innerHTML+="<img src='/migrator/res/images/error.png' class='statusicon' title='"+oRecord.getData("propertiesmessage")+"'/>";
					}
				}
			},{
				key:"message",
				label:"Upload Status",
				hidden:true
			},{
				key:"isnative",
				label:"Is Native?"
			},{
				key:"islatest",
				label:"Is Latest?",
				hidden:true
			},{
				key:"localfilelocation",
				label:"PDF Location",
				hidden:true
			},{
				key:"propertiesstatus",
				label:"Metadata Updated?",
				hidden:true
			},{
				key:"propertiesmessage",
				label:"Metadata message",
				hidden:true
			},{
				key:"csvuniqueid",
				label:"Unique ID",
				hidden:true
			},{
				key:"filename",
				label:"File Name",
				formatter:function(el,oRecord,oColumn,oData){
					el.innerHTML = oData;
					Dom.setStyle(el,"max-width","350px");
				}
			},{
				key:"templateid",
				label:"PDF Template ID",
				hidden:true
			},{
				key:"nativetemplateid",
				label:"Native Template ID",
				hidden:true
			},{
				key:"fileparentid",
				label:"Parent ID",
				hidden:true
			},{
				key:"nativeparentid",
				label:"Native Parent ID",
				hidden:true
			},{
				key:"filetype",
				label:"File Type",
				hidden:true
			},{
				key:"sourcelocation",
				label:"Source Location",
				hidden:true
			},{
				key:"startdate",
				label:"Start Date",
				hidden:true
			},{
				key:"enddate",
				label:"End Date",
				hidden:true
			},{
				key : "filesize",
				label:"File Size",
				formatter:function(el,oRecord,oColumn,oData){
					el.innerHTML = me.calculateSize(oRecord.getData("filesize"));
				}
			},{
				key:"timetaken",
				label:"Time Took",
				formatter:function(el,oRecord,oColumn,oData){
					var startTime = oRecord.getData("startdate");
					var endTime = oRecord.getData("enddate");
					if(!(startTime==undefined || startTime ==null || startTime=="") && !(endTime==undefined || endTime ==null || endTime=="")){
						var startDate = new Date(startTime);
						var endDate = new Date(endTime);
						var calculatedTime = me.timeDiff(startDate,endDate);
						el.innerHTML = calculatedTime;
					}
				}
			},{
				key:"actions",
				label:"Actions",
				formatter:function(el,oRecord,oColumn,oData){
					var status = oRecord.getData("status");
					el.innerHTML+="<div class='ZoomIt'><ul>"
					+"<li><a href='#viewdetails'><img src='/migrator/res/images/details.png' class='zit' title='View Details'/></a></li>"
					+"<li><a href='#viewtemplatedetails'><img src='/migrator/res/images/template.png' class='zit' title='View Metadata Template'/></a></li>"
					+"<li><a href='#viewalfresco'><img id='"+oRecord.getId()+"-reupload' src='/migrator/res/images/metadata.webp' class='zit "+((parseInt(status)==200 || parseInt(status)==201)?"":"hidden")+"' title='Load From Alfresco'/></a></li>"
					+"<li><a href='#reupload'><img id='"+oRecord.getId()+"-reupload' src='/migrator/res/images/upload.jpg' class='zit "+((parseInt(status)==200 || parseInt(status)==201)?"hidden":"")+"' title='Re-Upload'/></a></li>"
					+"<li><a href='#loading'><img id='"+oRecord.getId()+"-loading' src='/migrator/res/images/alfresco-logo-vector-1.svg' class='circle zit hidden' title='Uploading.....'/></a></li>"
					+"</ul></div>";
				}
			}];
			var recordDataSource = new YAHOO.util.DataSource("retrievecsvmigrateddata?csvid=",{
				responseType : YAHOO.util.DataSource.TYPE_JSON,
				responseSchema : {
				resultsList : "list",
				metaFields: {
					totalRecords: "totalrecords",
					page:"page"
				},
				fields : ["filesize","startdate","enddate","sourcelocation","filetype","slno","csvfileid", "properties","noderef","status","message","isnative","islatest","localfilelocation","csvrecordnumber","propertiesstatus","csvuniqueid","filename","propertiesmessage","templateid","nativetemplateid","fileparentid","nativeparentid"]
				}
			});
			var template ='<span class="pg-nav">' +
					            '&nbsp; {FirstPageLink}  &nbsp;{PreviousPageLink} ' +
					            '&nbsp;<span>{CurrentPageReport}</span>' +
					            '&nbsp; {NextPageLink} &nbsp; {LastPageLink}' +
					        '</span> ' +
					        '&nbsp; <label>Page size: {RowsPerPageDropdown}</label>' ;
			me.widgets.epaginator = new YAHOO.widget.Paginator({ 
					containers : ["reporttablepaginationone","reporttablepaginationtwo"],
					rowsPerPage:me.max,
					//template : '{CurrentPageReport} {FirstPageLink} {PreviousPageLink} {PageLinks} {NextPageLink} {LastPageLink}'
					template:template,
					pageReportTemplate : "{startRecord} - {endRecord} of {totalRecords}",
					rowsPerPageOptions : [
						{ value : 10, text : 10 },
						{ value : 25, text : 25 }, 
				        { value : 50, text : 50}, 
				        { value : 100, text : 100 },
				        { value : 1000, text : 1000 } 
					    ]
					});
	    	var egenerateRequest = function(oState, oSelf){
				var  csvCtrl = Dom.get("csvctrl");
				me.widgets.recordDataTable.showTableMessage("<img src='/migrator/res/images/alfresco-logo-vector-1.svg' class='circle'/>");
	    		var csvValue = csvCtrl.options[csvCtrl.selectedIndex].value;
				oState = oState || { pagination: null, sortedBy: null };
				me.page = (oState.pagination) ? (oState.pagination.page-1) : 0;
			    me.max = (oState.pagination) ? oState.pagination.rowsPerPage : me.max;
	    		var URL =csvValue+"&skip="+me.page+"&max="+me.max; 
	    		if(!(me.statusFilter=="" || me.statusFilter==undefined || me.statusFilter==null)){
					URL=me.getFilterURL(me.statusFilter,me.page,me.max);
				}
	    		return URL;
	    	};
			me.widgets.recordDataTable = new YAHOO.widget.DataTable("reporttable", recordcolumns, recordDataSource,{
				generateRequest: egenerateRequest,
				initialLoad: false,
        		MSG_EMPTY:"No records migrated",
        		sortedBy:{key:"csvrecordnumber", dir:"asc"},
        		dynamicData: true,
				paginator:  me.widgets.epaginator
				
        	});
        	me.widgets.recordDataTable.handleDataReturnPayload = function(oRequest, oResponse, oPayload) {
				//recordDataTable.showTableMessage("<img src='/migrator/res/images/ajaxloading.gif' class='ajaxloading'/>");
				me.widgets.recordDataTable.showTableMessage("<img src='/migrator/res/images/alfresco-logo-vector-1.svg' class='circle'/>");
				if(!(oPayload==null || oPayload==undefined ||oPayload=="")){
					return oPayload; 
				}else{
					//page=oResponse.meta.page;
					return oResponse.meta;
				}
				
			};
			
			me.widgets.recordDataTable.subscribe("postRenderEvent",function(){
				var records = me.widgets.recordDataTable.getRecordSet().getRecords();
				var deleteallCtrl = Dom.get("deleteall");
				var csvDeleteCtrl = Dom.get("deletecsv");
				var calculateTimeCtrl = Dom.get("calculatetime");
				var exportCsvCtrl = Dom.get("exportcsv");
				var downloadCsvCtrl = Dom.get("downloadcsv");
				var selectCtrl = Dom.get("csvctrl");
				var csvValue = selectCtrl.options[selectCtrl.selectedIndex].value;
				if(null == records || records.length==0){
					Dom.removeClass(csvDeleteCtrl,"hidden");
					Dom.addClass(deleteallCtrl,"hidden");
					Dom.addClass(calculateTimeCtrl,"hidden");
					Dom.addClass(exportCsvCtrl,"hidden");
					Dom.addClass(downloadCsvCtrl,"hidden");
				}else{
					Dom.addClass(csvDeleteCtrl,"hidden");
					Dom.removeClass(deleteallCtrl,"hidden");
					Dom.removeClass(calculateTimeCtrl,"hidden");
					Dom.removeClass(exportCsvCtrl,"hidden");
					Dom.removeClass(downloadCsvCtrl,"hidden");
					//downloadCsvCtrl.href="/migrator/downloadcsv?csvid="+csvValue;
				}
			});
			me.widgets.recordDataTable.subscribe("theadLinkClickEvent",function(e){
				var operation = e.target.hash.substr(1).toLowerCase();
				var deleteallCtrl = Dom.get("deleteall");
				var selectAllCtrl = Dom.get("selectall");
				if("selectall"==operation){
					var records = me.widgets.recordDataTable.getRecordSet().getRecords();
					if(records.length>0){
						Dom.removeClass(deleteallCtrl,"hidden");
						for(var index=0;index<records.length;index++){
							var checkBoxCtrl = Dom.get(records[index].getId()+"-delete");
							checkBoxCtrl.checked=selectAllCtrl.checked;
							var toDelete = {
								csvuniqueid:records[index].getData("csvuniqueid"),
								noderef:records[index].getData("noderef"),
								recordid:records[index].getData("csvfileid")
							};
							me.selectedrecords.push(toDelete);
						}
						if(!selectAllCtrl.checked){
							me.selectedrecords=[];
						}
					}else{
						Dom.addClass(deleteallCtrl,"hidden");
					}
				}
			});
			me.widgets.recordDataTable.subscribe("linkClickEvent",function(e){
				var operation = e.target.hash.substr(1).toLowerCase();
				var tableRecord = me.widgets.recordDataTable.getRecord(e.target);
				var selectCtrl = Dom.get("csvctrl");
				var csvValue = selectCtrl.options[selectCtrl.selectedIndex].value;
				
				if(operation=="reupload"){
					var waitMsg = Migrator.utils.PopupManager.displayMessage(document.body,{
						text:"Please wait while uploading to Alfresco",
						spanClass:"wait"
					});
					var loadingImage = Dom.get(tableRecord.getId()+"-loading");
					var reuploadCtrl = Dom.get(tableRecord.getId()+"-reupload");
					Dom.removeClass(loadingImage,"hidden");
					var target = "";
					var filepath=tableRecord.getData("localfilelocation")+"/"+tableRecord.getData("filename");;
					if(tableRecord.getData("isnative")){
						target = tableRecord.getData("nativeparentid");
					}else{
						target = tableRecord.getData("fileparentid");
					}
					var postDataTemplate = YAHOO.lang.substitute("target={target}&properties={properties}&filelocation={filelocation}&filetype={filetype}&isnative={isnative}&migratedrecordid={migratedrecordid}", {
						target : target,
						properties : encodeURI(tableRecord.getData("properties")),
						filelocation : filepath,
						filetype : tableRecord.getData("filetype"),
						isnative :tableRecord.getData("isnative")+"",
						migratedrecordid :tableRecord.getData("csvfileid")
					});
					var formData = new FormData();
					formData.append("target",target);
					formData.append("properties",tableRecord.getData("properties"));
					formData.append("filelocation",filepath);
					formData.append("filetype",tableRecord.getData("filetype"));
					formData.append("isnative",tableRecord.getData("isnative"));
					formData.append("migratedrecordid",tableRecord.getData("csvfileid"));
					formData.append("sourcelocation",tableRecord.getData("sourcelocation"));
					formData.append("localfilelocation",filepath);
					var reuploadreq= new XMLHttpRequest();
					
					reuploadreq.upload.addEventListener("load", function(e){
					 	if( reuploadreq.readyState!=4){
						 	reuploadreq.onreadystatechange=function(e) {
								if (reuploadreq.readyState == XMLHttpRequest.DONE && reuploadreq.status==200) {
									var resJson = JSON.parse(reuploadreq.response);
									Dom.addClass(loadingImage,"hidden");
									Dom.addClass(reuploadCtrl,"hidden");
									var URL = csvValue+"&skip="+me.page+"&max="+me.max;
									if(!(me.statusFilter=="" || me.statusFilter==undefined || me.statusFilter==null)){
										URL=me.getFilterURL(me.statusFilter,me.page,me.max);
									}
									me.widgets.recordDataTable.getDataSource().sendRequest(URL,
									{
										success : me.widgets.recordDataTable.onDataReturnReplaceRows,
										scope : me.widgets.recordDataTable
				
									});
									waitMsg.hide();
									me.fetchCounts(csvValue);
							 	}else if (reuploadreq.readyState == XMLHttpRequest.DONE && (reuploadreq.status ==400 || reuploadreq.status ==500 || reuploadreq.status ==404 || reuploadreq.status ==408 || reuploadreq.status ==401)) {
								 	console.log(reuploadreq);
								 	var resJson = JSON.parse(reuploadreq.responseText);
								 	console.log(resJson);
								 	Migrator.utils.PopupManager.displayPrompt(document.body,resJson.statuscode,resJson.message);
									Dom.addClass(loadingImage,"hidden");
									var URL = csvValue+"&skip="+me.page+"&max="+me.max;
									console.log("BEFORE URL : "+URL);
									if(!(me.statusFilter=="" || me.statusFilter==undefined || me.statusFilter==null)){
										URL=me.getFilterURL(me.statusFilter,me.page,me.max);
									}
									console.log("AFTER URL : "+URL);
									me.widgets.recordDataTable.getDataSource().sendRequest(URL,
									{
										success : me.widgets.recordDataTable.onDataReturnReplaceRows,
										scope : me.widgets.recordDataTable
				
									});
									waitMsg.hide();
									me.fetchCounts(csvValue);
    						 	}else if(reuploadreq.readyState == XMLHttpRequest.DONE){
									waitMsg.hide();
									me.fetchCounts(csvValue);
									Dom.addClass(loadingImage,"hidden");
									Dom.addClass(reuploadCtrl,"hidden");
								}
								
						 	}
					 	};
				 	}, false);
					
					reuploadreq.open("POST",  "/migrator/reupload",true);
					reuploadreq.send(formData);
				}else if(operation=="viewdetails"){
					me.detailsPanel(tableRecord);
					//viewPanel(tableRecord);
				}else if(operation=="viewtemplatedetails"){
					me.templatePanel(tableRecord);
				}
				else if(operation=="viewalfresco"){
					var waitMsg = Migrator.utils.PopupManager.displayMessage(document.body,{
							text:"Please wait while fetching details from Alfresco",
							spanClass:"wait"
							});
					var nodeId = tableRecord.getData("noderef").replace("workspace://SpacesStore/","");
					var templateId="";
					console.log(tableRecord.getData("isnative")==false);
					if(tableRecord.getData("isnative")==false){
						templateId=tableRecord.getData("templateid");
					}else{
						templateId=tableRecord.getData("nativetemplateid");
					}
					var handleSuccess = function(res){
						var resJson = JSON.parse(res.responseText);
						waitMsg.hide();
						me.alfrescopanel(resJson);
					};
					var handleFailure = function(res){
						var resJson = JSON.parse(res.responseText);
						console.log(resJson);
						//popupPanel(resJson.status,resJson.details.errorKey);
						Migrator.utils.PopupManager.displayMessage(document.body,{
							title:resJson.status+" "+resJson.details.errorKey
							});
					};
					var callback ={
						success:handleSuccess,
						failure: handleFailure
					};
					var request = YAHOO.util.Connect.asyncRequest('GET', "/migrator/getnodedetails?nodeid="+nodeId+"&templateid="+templateId, callback);
				}
			});
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
		fetchCounts :function(csvid){
			var successCountCtrl = Dom.get("successCount");
			var failedCountCtrl = Dom.get("failedCount");
			var reuploadCtrl = Dom.get("reuploadfailed");
			var countReqSuccess = function(res){
				var resJson = JSON.parse(res.responseText);
				var countList = resJson.list;
				var successCount = 0;
				var failedCount = 0;
				for(var index in countList){
					if(countList[index].status==200 || countList[index].status==201){
						successCount+=parseInt(countList[index].count);
					}else{
						failedCount+=parseInt(countList[index].count);
					}
				}
				if(failedCount>0){
					Dom.removeClass(reuploadCtrl,"hidden");
				}else{
					Dom.addClass(reuploadCtrl,"hidden");
				}
				successCountCtrl.innerHTML = "("+successCount+")";
				failedCountCtrl.innerHTML = "("+failedCount+")";
			};
			var countReqFailure = function(res){
				var resJson = JSON.parse(res.responseText)
				console.log(resJson);
				if(resJson.status=404){
					Migrator.utils.PopupManager.displayMessage(document.body,resJson.status+" "+resJson.message);
					//popupPanel(resJson.status,resJson.message);
				}
			};
			var countReqCallback ={
				success: countReqSuccess,
				failure: countReqFailure
			};
			var countReq = YAHOO.util.Connect.asyncRequest('GET', "/migrator/checkstatus?csvid="+csvid, countReqCallback);
		},
		displayPieChart :function(){
			var yuiDialog = new YAHOO.widget.Panel("chartpanel", { width:"600px",visible:true,modal:false, draggable:true, close:false,zindex:4,keylisteners:[escapeListener]} );
			var escapeListener = new YAHOO.util.KeyListener(document,
            {
                 keys: YAHOO.util.KeyListener.KEY.ESCAPE
             },
             {
               	fn: function(id, keyEvent)
               	{
                  	this.destroy();
               	},
               	scope: yuiDialog,
               	correctScope: true
           	});
        	escapeListener.enable();
			yuiDialog.setHeader("<center><b>Pie Chart</b></center><div id='close' class='close'></div>");
			yuiDialog.setBody("<div id='gpiechartDiv' style='maxwidth:900px;min-height:400px'></div>");
			yuiDialog.setFooter("<center><table><tr><td><div id='save'></div></td></tr></table>");
			yuiDialog.showEvent.subscribe(function(){
			var saveBtn = new YAHOO.widget.Button("save",{
				label:"Ok"
			}); 
			saveBtn.on("click",function(){
				yuiDialog.destroy();
			});
			var closeCtrl = Dom.get("close");
			YAHOO.util.Event.addListener(closeCtrl,"click",function(e){
				yuiDialog.destroy();
			});
			var handleSuccess = function(res){
			var resJson = JSON.parse(res.responseText);
			var data =google.visualization.arrayToDataTable(resJson.gvalues);
			var formatter = new google.visualization.PatternFormat("{0} ({1})");
			formatter.format(data, [0,1]);
			var options = {
				title: '',
	          	is3D: true,
		        pieSliceTextStyle: {
	            	color: 'black',
				},
				legend:{"position":'bottom'},
			    backgroundColor:'transparent',
			    pieSliceText: 'value',
			    colors:['#00b33c','#e62e00','#ffb84d','#4da6ff','#e6ac00','#e6ac00','#e69900']
			 };
				
			 var chart = new google.visualization.PieChart(document.getElementById('gpiechartDiv'));
   			 chart.draw(data, options);
			};
			var handleFailure = function(res){
				var resJson = JSON.parse(res.responseText);
				Migrator.utils.PopupManager.displayPrompt(document.body,resJson.status,resJson.details.errorKey);
			};
			var callback ={
				success:handleSuccess,
				failure: handleFailure
			};
			var request = YAHOO.util.Connect.asyncRequest('GET', pieURL, callback);
					
			});
			yuiDialog.render(document.body);
			yuiDialog.center();
		},
		detailsPanel :function(record){
			var sourcelocation = record.getData("sourcelocation");
			var tablerow = "";
			if(sourcelocation=="amazons3"){
				tablerow = "<tr><td><b>s3 Location</b></td><td>:</td><td>s3://"+record.getData("localfilelocation")+"</td></tr>";
			}else{
				tablerow = "<tr><td><b>Local File Location</b></td><td>:</td><td>"+record.getData("localfilelocation")+"</td></tr>";
			}
			var yuiDialog = new YAHOO.widget.Panel("viewpanel", { visible:true, draggable:true, close:false,zindex:4,keylisteners:[escapeListener]} );
				var escapeListener = new YAHOO.util.KeyListener(document,
               	{
                  	keys: YAHOO.util.KeyListener.KEY.ESCAPE
               	},
               	{
                  	fn: function(id, keyEvent)
                  	{
                     	this.destroy();
                  	},
                  	scope: yuiDialog,
                  	correctScope: true
               	});
               	escapeListener.enable();
				yuiDialog.setHeader("<center><b>Details</b></center><div id='close' class='close'></div>");
				yuiDialog.setBody("<center><div style='min-width:750px;min-height:400px;'>"
				+"<table>"
				+"<tr><td><b>Node Ref</b></td><td>:</td><td>"+record.getData("noderef")+"</td></tr>"
				+tablerow
				+"<tr><td><b>File Name</b></td><td>:</td><td>"+record.getData("filename")+"</td></tr>"
				+"<tr><td><b>Status</b></td><td>:</td><td>"+record.getData("status")+"</td></tr>"
				+"<tr><td><b>Upload Message</b></td><td>:</td><td>"+record.getData("message")+"</td></tr>"
				+"<tr><td><b>Metadata Updated?</b></td><td>:</td><td>"+record.getData("propertiesstatus")+"</td></tr>"
				+"<tr><td><b>Metadata Message</b></td><td>:</td><td>"+record.getData("propertiesmessage")+"</td></tr>"
				+"<tr><td><b>CSV Record No</b></td><td>:</td><td>"+record.getData("csvrecordnumber")+"</td></tr>"
				+"<tr><td><b>CSV File ID</b></td><td>:</td><td>"+record.getData("csvuniqueid")+"</td></tr>"
				+"<tr><td><b>Native Template ID</b></td><td>:</td><td>"+record.getData("nativetemplateid")+"</td></tr>"
				+"<tr><td><b>File Template ID</b></td><td>:</td><td>"+record.getData("templateid")+"</td></tr>"
				+"<tr><td><b>File Parent Ref</b></td><td>:</td><td>"+record.getData("fileparentid")+"</td></tr>"
				+"<tr><td><b>Native Parent Ref</b></td><td>:</td><td>"+record.getData("nativeparentid")+"</td></tr>"
				+"</table>"
				+"<div id='metadatatable'></div>"
				+"</div></center>");
				yuiDialog.setFooter("<center><table><tr><td><div id='save'></div></td></tr></table>");
				yuiDialog.showEvent.subscribe(function(){
					var saveBtn = new YAHOO.widget.Button("save",{
						label:"Ok"
					}); 
					saveBtn.on("click",function(){
						yuiDialog.destroy();
					});
					var closeCtrl = Dom.get("close");
					YAHOO.util.Event.addListener(closeCtrl,"click",function(e){
						yuiDialog.destroy();
					});
					var columns=[{
						key : "property",
						label : "Property"
					},{
						key : "value",
						label : "Value"/*,
						formatter: function(el,oRecord,oColumn,oData){
							el.innerHTML = '<span style="width:300px;word-break: break-word;">'+oData+'</span>';
							Dom.addClass(el,"breakword");
						}*/
					}];
					var datasource = new YAHOO.util.LocalDataSource([{
        			 	"property":"",
        			 	"value":""
        		 	}]); 
        		 	var datatable =  new YAHOO.widget.ScrollingDataTable("metadatatable", columns, datasource,{
						initialLoad: false,
		        		MSG_EMPTY:"",
		        		height:"110px"
		        	});
		        	var propertiesJson = JSON.parse(record.getData("properties"));
		        	for(var index in propertiesJson){
						var data ={
							"property":index,
							"value":propertiesJson[index]
						};
						datatable.addRow(data);
					}
				});
				yuiDialog.hideEvent.subscribe(function(e,args,data){
					yuiDialog.hide();
				});
				yuiDialog.render(document.body);
				yuiDialog.center();
		},
		templatePanel :function(record){
			var templatePanel = new YAHOO.widget.Panel("templatepanel", { visible:true, draggable:true, close:false,zindex:4,keylisteners:[escapeListener]} );
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
				templatePanel.setHeader("<center><b>Metadata Mapping</b></center><div id='close' class='close'></div>");
				templatePanel.setBody("<center><div style='min-width:750px;min-height:400px;'><table>"
				+"<tr><td><b>Template ID</b></td><td>:</td><td><span id='templateid'></span></td></tr>"
				+"<tr><td><b>Template Name</b></td><td>:</td><td><span id='templatename'></span></td></tr>"
				+"<tr><td><b>Mapped Alfresco Type</b></td><td>:</td><td><span id='alfname'></span></td></tr>"
				+"<tr><td><b>File Column Name</b></td><td>:</td><td><span id='columnName'></span></td></tr>"
				+"</table>"
				+"<br/><div id='detailsTable'></div></div>"
				+"</center>");
				templatePanel.setFooter("<center><table><tr><td><div id='save'></div></td></tr></table>");
				templatePanel.showEvent.subscribe(function(){
					var templateIdCtrl = Dom.get("templateid");
					var templateNameCtrl = Dom.get("templatename");
					var columnCtrl = Dom.get("columnName");
					var closeCtrl = Dom.get("close");
					YAHOO.util.Event.addListener(closeCtrl,"click",function(e){
						templatePanel.destroy();
					});
					var alfNameCtrl = Dom.get("alfname");
					var saveBtn = new YAHOO.widget.Button("save",{
						label:"Ok"
					}); 
					saveBtn.on("click",function(){
						templatePanel.destroy();
					});
					var templateId = "";
					if(record.getData("isnative")){
						templateId = record.getData("nativetemplateid");
						templateIdCtrl.innerHTML = record.getData("nativetemplateid");
					}else{
						templateId = record.getData("templateid");
					}
					templateIdCtrl.innerHTML = templateId;
					
					var templateSuccess = function(res){
						var resJson = JSON.parse(res.responseText);
						console.log(resJson);
						templateNameCtrl.innerHTML=resJson.templatename;
						columnCtrl.innerHTML = resJson.columnname;
						alfNameCtrl.innerHTML = resJson.propertytitle+" ("+resJson.propertyqname+")";
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
			        		height:"310px"
			        	});
					};
					var templateFailure = function(res){
						console.log(res);
					};
					var templateCallback ={
						success: templateSuccess,
						failure: templateFailure
					};
					var templateReq = YAHOO.util.Connect.asyncRequest('GET', "/migrator/gettemplate?templateId="+templateId, templateCallback);
					
				});
				templatePanel.render(document.body);
				templatePanel.center();
		},
		calculateSize :function formatBytes(bytes, decimals = 2) {
		    if (!+bytes) return '0 Bytes';
		    var k = 1024;
		    var dm = decimals < 0 ? 0 : decimals;
		    var sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
		    var i = Math.floor(Math.log(bytes) / Math.log(k));
		    return `${parseFloat((bytes / Math.pow(k, i)).toFixed(dm))} ${sizes[i]}`;
		},
		alfrescopanel : function(resJson){
			var yuiDialog = new YAHOO.widget.Panel("alfrescopanel", {width:"1100px", visible:true,modal:false, draggable:true, close:false,zindex:4,keylisteners:[escapeListener]} );
			var escapeListener = new YAHOO.util.KeyListener(document,
           	{
               	keys: YAHOO.util.KeyListener.KEY.ESCAPE
           	},
           	{
               	fn: function(id, keyEvent)
               	{
                  	this.destroy();
               	},
               	scope: yuiDialog,
               	correctScope: true
           	});
           	escapeListener.enable();
			yuiDialog.setHeader("<center><b>Details</b></center><div id='close' class='close'></div>");
			yuiDialog.setBody("<center>"
				+"<img src='/migrator/res/images/alfresco-logo-vector-1.svg' class='circle' id='loading'/>"
				+"<table>"
				+"<tr><td align='right'><b>Name</b></td><td>:</td><td>"+resJson.details.name+"</td></tr>"
				+"<tr><td align='right'><b>File Type</b></td><td>:</td><td>"+resJson.details.filetype.title+"&nbsp;("+resJson.details.filetype.qname+")"+"</td></tr>"
				+"<tr><td align='right'><b>Size</b></td><td>:</td><td>"+me.calculateSize(resJson.details.content.sizeInBytes)+"</td></tr>"
				+"<tr><td align='right'><b>Path</b></td><td>:</td><td>"+resJson.details.path.name+"</td></tr>"
				+"<tr><td><b></b></td><td></td><td></td></tr>"
				+"</table>"
				+"<div id='propertiesTable'></div>"
				
				+"</center>");
			yuiDialog.setFooter("<center><table><tr><td><div id='save'></div></td></tr></table>");
			yuiDialog.showEvent.subscribe(function(){
				var loadingImg = Dom.get("loading");
				var saveBtn = new YAHOO.widget.Button("save",{
					label:"Ok"
				}); 
				saveBtn.on("click",function(){
					yuiDialog.destroy();
				});
				var closeCtrl = Dom.get("close");
				YAHOO.util.Event.addListener(closeCtrl,"click",function(e){
					yuiDialog.destroy();
				});
				var columns=[{
					key : "csvcolumnname",
					label : "CSV Column"
				},{
					key : "alfrescopropertyqname",
					label : "QName"
				},{
					key : "alfpropertytitle",
					label : "Title"
				},{
					key : "value",
					label : "Value",
					width:300
				}];
				var datasource = new YAHOO.util.LocalDataSource({"csvcolumnname":"","alfrescopropetyqname":"","alfrescopropertytitle":"","value":""}); 
       		 	var datatable =  new YAHOO.widget.ScrollingDataTable("propertiesTable", columns, datasource,{
					initialLoad: false,
	        		MSG_EMPTY:"",
	        		height:"170px"
	        	});
	        	for(var index in resJson.details.properties){
					datatable.addRow(resJson.details.properties[index]);
				}
				Dom.addClass(loadingImg,"hidden");
			});
			yuiDialog.render(document.body);
			yuiDialog.center();
		}
	}
})();