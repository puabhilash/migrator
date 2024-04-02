(function()
{
	var me;
	var Dom = YAHOO.util.Dom,
    Event = YAHOO.util.Event;
	Migrator.component.templates = function (){
		me=this;
		me.widgets={};
      	me.onComponentsLoaded();
      	return this;
   	};
   	Migrator.component.templates.prototype ={
		onComponentsLoaded:function(){
			console.log(Migrator.utils.PopupManager);
			var templateCtrl = Dom.get("templateSelect");
			var templateNameCtrl = Dom.get("templateName");
			var typeNameCtrl = Dom.get("alfType");
			var columnNameCtrl = Dom.get("filecolumn");
			this.setupDatatable();
			Event.addListener(templateCtrl,"change",function(e){
				var selectCtrl = e.target;
				var templateValue = selectCtrl.options[selectCtrl.selectedIndex].value;
				if(templateValue==""||templateValue==null||templateValue==undefined){
					Migrator.utils.PopupManager.displayMessage(document.body,{
						text:"Please select template to render details"
						});
					//popupPanel("Alert","Please select template to render details");
				}else{
					var templatehandleSuccess = function(res){
						me.widgets.templateDetailsTable.showTableMessage("<img src='/migrator/res/images/alfresco-logo-vector-1.svg' class='circle'/>");
						var resJson = JSON.parse(res.responseText);
						templateNameCtrl.innerHTML = resJson.templatename;
						typeNameCtrl.innerHTML = resJson.propertytitle;
						console.log(resJson);
						columnNameCtrl.innerHTML = resJson.columnname;
						me.widgets.templateDetailsTable.getDataSource().sendRequest(templateValue,
						{
							success : me.widgets.templateDetailsTable.onDataReturnReplaceRows,
							scope : me.widgets.templateDetailsTable
	
						});
					};
					var templatehandleFailure = function(res){
						console.log(res);
					};
					var templateCallback ={
						success: templatehandleSuccess,
						failure: templatehandleFailure
					};
					var templateURL = "gettemplate?templateId="+templateValue;
					var templateReq = YAHOO.util.Connect.asyncRequest('GET', templateURL, templateCallback);
				}
			});
		},
		setupDatatable:function(){
			var templateDetailsColumns = [{
				key:"propertytitle",
				label:"Property",
				width:300
			},{
				key:"propertyqname",
				label:"property QName",
				hidden:true
			},{
				key:"uniqueid",
				label:"Unique ID",
				hidden:true
			},{
				key:"columnname",
				label:"CSV Column"
			}];
			var templateDetailsDataSource = new YAHOO.util.DataSource("/migrator/gettemplatedetails?templateId=",{
				responseType : YAHOO.util.DataSource.TYPE_JSON,
				responseSchema : {
				resultsList : "templatedetails",
				fields : ["propertytitle", "propertyqname","columnname","uniqueid"]
				}
				}); 
			this.widgets.templateDetailsTable= new YAHOO.widget.DataTable("templateDetailsTable", templateDetailsColumns, templateDetailsDataSource,{
				initialLoad: false,
        		MSG_EMPTY:"No Template Selected",
        		width:"470px", 
        		height:"300px"
        	});
		}
	};
})();