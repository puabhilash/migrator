(function()
{
	var me;
	var Dom = YAHOO.util.Dom,
    Event = YAHOO.util.Event;
	Migrator.component.Home = function (){
		me=this;
		me.widgets={};
      	me.onComponentsLoaded();
      	return this;
   	};
   	Migrator.component.Home.prototype ={
		options:{
			urlcontext:"",
			hasalfrescodetails:""
		},
		setOptions: function(obj){
	        me.options = YAHOO.lang.merge(me.options, obj);
	        return this;
	    },
		onComponentsLoaded:function(){
			Event.onContentReady("validationpanel", me.onReady, this, true);
		},
		onReady :function(){
			this.loadAlfrescoControls();
			this.loadActiveMQControls();
		},
		loadAlfrescoControls :function(){
			me.widgets.alfrescoEditButton = new YAHOO.widget.Button("alfrescoedit",{
				label : "Edit"
			});
			me.widgets.alfrescoValidateButton = new YAHOO.widget.Button("alfrescovalidate",{
				label : "Validate"
			});
			me.widgets.alfrescoEditButton.on('click',this.editAlfrescoDetails);
			me.widgets.alfrescoValidateButton.on('click',this.validateAlfrescoConnection);
		},
		loadActiveMQControls :function(){
			me.widgets.activeMqEditButton = new YAHOO.widget.Button("activemqedit",{
				label : "Edit"
			});
			me.widgets.activeMqValidateButton = new YAHOO.widget.Button("activemqvalidate",{
				label : "Validate"
			});
			me.widgets.activeMqValidateButton.on('click',this.validateActiveMQ)
		},
		showTickSavedMark : function(){
			var tickSaveCtrl = Dom.get("tickSave");
			var successSaveCtrl = Dom.get("successSave");
			Dom.removeClass(successSaveCtrl,"hidden");
			Dom.addClass(tickSaveCtrl,"animated-check");
			Dom.removeClass(tickSaveCtrl,"hidden");
		},
		hideTickSavedMark : function(){
			var tickSaveCtrl = Dom.get("tickSave");
			var successSaveCtrl = Dom.get("successSave");
			Dom.addClass(successSaveCtrl,"hidden");
			Dom.removeClass(tickSaveCtrl,"animated-check");
			Dom.addClass(tickSaveCtrl,"hidden");
		},
		showTickMark : function(){
			var tickCtrl = Dom.get("tick");
			var successCtrl = Dom.get("successTable");
			Dom.removeClass(successCtrl,"hidden");
			Dom.addClass(tickCtrl,"animated-check");
			Dom.removeClass(tickCtrl,"hidden");
		},
		showCrossMark : function(){
			var crossCtrl = Dom.get("checsvg");
			var failMsgTable = Dom.get("failTable");
			var checkCircleCTrl = Dom.get("checkcircle");
			Dom.removeClass(failMsgTable,"hidden");
			Dom.addClass(crossCtrl,"checkmark");
			Dom.addClass(checkCircleCTrl,"checkmark_circle");
		},
		hideTickMark : function(){
			var tickCtrl = Dom.get("tick");
			var successCtrl = Dom.get("successTable");
			Dom.addClass(successCtrl,"hidden");
			Dom.removeClass(tickCtrl,"animated-check");
			Dom.addClass(tickCtrl,"hidden");
		},
		hideCrossMark : function(){
			var crossCtrl = Dom.get("checsvg");
			var failMsgTable = Dom.get("failTable");
			var checkCircleCTrl = Dom.get("checkcircle");
			Dom.addClass(failMsgTable,"hidden");
			Dom.removeClass(crossCtrl,"checkmark");
			Dom.removeClass(checkCircleCTrl,"checkmark_circle");
		},
		editAlfrescoDetails : function(){
			var editsuccessMsgCtrl = Dom.get("successMsg");
			var yuiDialog = new YAHOO.widget.Panel("loadingPanel", { width:"320px", visible:true,modal:false, draggable:true, close:false,zindex:4,keylisteners:[escapeListener]} );
			var escapeListener = new YAHOO.util.KeyListener(document,
            {
               	keys: YAHOO.util.KeyListener.KEY.ESCAPE
            },
            {
               	fn: function(id, keyEvent)
               	{
					Dom.removeClass(me.widgets.alfrescoEditButton,"yui-push-button-focus");
					Dom.removeClass(me.widgets.alfrescoEditButton,"yui-button-focus");
                    this.destroy();
                },
                scope: yuiDialog,
                correctScope: true
            });
            escapeListener.enable();
			yuiDialog.setHeader("<center>Edit Alfresco Details</center><div id='close' class='close'></div>");
			yuiDialog.setBody("<center><div class='hidden' id='loadingImg' style='background-image: url(/migrator/res/images/loading.gif);width: 301px;height: 21px;background-repeat: no-repeat;background-position: 0px;background-size: 110px;margin-left: 90px;margin-bottom: 5px;'></div><table>"
				+"<tr><td class='label'>Config ID</td><td>:</td><td><input id='configid' type='text' readonly='true' class='readonly'/></td></tr>"
				+"<tr><td class='label'>Protocol *</td><td>:</td><td><select id='alfprotocol'><options><option value=''>Select</option><option value='http'>HTTP</option><option value='https'>HTTPS</option></options></select></td></tr>"
				+"<tr><td class='label'>Host *</td><td>:</td><td><input type='text' id='alfhost'/></td></tr>"
				+"<tr><td class='label'>Port *</td><td>:</td><td><select id='alfport'><options><option value=''>Select</option><option value='80'>80</option><option value='443'>443</option><option value='8080'>8080</option><option value='8443'>8443</option></options></select></td></tr>"
				+"<tr><td class='label'>Username *</td><td>:</td><td><input type='text' id='alfusername'/></td></tr>"
				+"<tr><td class='label'>Password *</td><td>:</td><td><input type='password' id='alfpassword'/></td></tr>"
				+"</table></center>");
			yuiDialog.setFooter("<center><table><tr><td><div id='save'></div></td><td><div id='cancel'></div></td></td></table>");
			yuiDialog.showEvent.subscribe(function(){
				me.hideCrossMark();
				me.hideTickMark();
				var closeCtrl = Dom.get("close");
				YAHOO.util.Event.addListener(closeCtrl,"click",function(e){
					yuiDialog.destroy();
				});
				var loadingCtrl = Dom.get("loadingImg");
				var configIdCtrl = Dom.get("configid");
				var protocolCtrl = Dom.get("alfprotocol");
				var hostCtrl = Dom.get("alfhost");
				var portCtrl = Dom.get("alfport");
				var usernameCtrl = Dom.get("alfusername");
				var passwordCtrl = Dom.get("alfpassword");
				var alfDetailsURL = "retrievealfconf";
				var alfhandleSuccess = function(res){
					var resJson = JSON.parse(res.responseText);
					//hasalfdetailsCtrl.value = resJson.hasalfconfig;
					me.hasalfrescodetails=resJson.hasalfconfig;
					protocolCtrl.value=resJson.protocol;
					hostCtrl.value=resJson.host;
					portCtrl.value=resJson.port;
					usernameCtrl.value=resJson.username;
					passwordCtrl.value=resJson.password;
					configIdCtrl.value=resJson.configid;
				};
				var alfhandleFailure = function(res){
					console.log(res);
				};
				var alfDetailsCallback ={
					success:alfhandleSuccess,
					failure: alfhandleFailure
				};
				var alfDetailsReq = YAHOO.util.Connect.asyncRequest('GET', alfDetailsURL, alfDetailsCallback);
				var cancelBtn = new YAHOO.widget.Button("cancel",{
					label:"Cancel"
				}); 
				var saveBtn = new YAHOO.widget.Button("save",{
					label:"Save"
				}); 
				saveBtn.on("click",function(){
					var protocolValue = protocolCtrl.options[protocolCtrl.selectedIndex].value;
					var portValue = portCtrl.options[portCtrl.selectedIndex].value;
					var hostValue = hostCtrl.value;
					var usernameValue = usernameCtrl.value;
					var passwordValue = passwordCtrl.value;
					var configIdValue = configIdCtrl.value;
					if(protocolValue =="" || protocolValue==undefined || protocolValue==null){
						protocolCtrl.style.border="1px solid red";
						Migrator.utils.PopupManager.displayMessage(document.body,{
							text:"please select protocol"
						});
					}else if(hostValue =="" || hostValue==undefined || hostValue==null){
						protocolCtrl.style.border=null;
						Migrator.utils.PopupManager.displayMessage(document.body,{
							text:"please provide host value"
						});
						hostCtrl.style.border="1px solid red";
					}else if(portValue =="" || portValue==undefined || portValue==null){
						hostCtrl.style.border=null;
						portCtrl.style.border="1px solid red";
						Migrator.utils.PopupManager.displayMessage(document.body,{
							text:"please select port"
						});
					}else if(usernameValue =="" || usernameValue==undefined || usernameValue==null){
						portCtrl.style.border=null;
						Migrator.utils.PopupManager.displayMessage(document.body,{
							text:"please provide username value"
						});
						usernameCtrl.style.border="1px solid red";
					}else if(passwordValue =="" || passwordValue==undefined || passwordValue==null){
						usernameCtrl.style.border=null;
						passwordCtrl.style.border="1px solid red";
						Migrator.utils.PopupManager.displayMessage(document.body,{
							text:"please provide password value"
						});
					}else{
						var waitMsg = Migrator.utils.PopupManager.displayMessage(document.body,{
							text:"Please wait while saving Alfresco details",
							spanClass:"wait"
						});
						passwordCtrl.style.border=null;
						Dom.removeClass(loadingCtrl,"hidden");
						var sUrl = "savealfconf";
						var handleSuccess = function(res){
							console.log(res);
							var resJson = JSON.parse(res.responseText);
							yuiDialog.destroy();
							Dom.addClass(loadingCtrl,"hidden");
							me.showTickSavedMark();
							editsuccessMsgCtrl.innerHTML = resJson.message;
							waitMsg.hide();
						};
						var handleFailure = function(res){
							console.log(res);
							yuiDialog.destroy();
							Dom.addClass(loadingCtrl,"hidden");
							waitMsg.hide();
						};
						var callback ={
							success:handleSuccess,
							failure: handleFailure
						};
						var postData = "protocol="+protocolValue+"&port="+portValue+"&host="+hostValue+"&appname=alfresco&username="+usernameValue+"&password="+passwordValue+"&hasalfconfig="+me.hasalfrescodetails;
						if(!(configIdValue==null||configIdValue==undefined||configIdValue=="")){
							postData+="&configid="+configIdValue;
						}
						var request = YAHOO.util.Connect.asyncRequest('POST', sUrl, callback, postData);
						console.log(request);
					}
					});
					cancelBtn.on("click",function(){
						yuiDialog.destroy();
					});
				});
				yuiDialog.render(document.body);
				yuiDialog.center(); 
			},
			validateAlfrescoConnection : function(){
				var failMsgCtrl = Dom.get("failMsg");
				var tickCtrl = Dom.get("tick");
				var loadingCtrl = Dom.get("validatingDiv");
				var successMsgCtrl = Dom.get("successCtrl");
				Dom.removeClass(loadingCtrl,"hidden");
				Dom.removeClass(tickCtrl,"animated-check");
				me.hideTickSavedMark();
				var alfValhandleSuccess = function(res){
					var resJson = JSON.parse(res.responseText);
					Dom.addClass(loadingCtrl,"hidden");
					me.hideCrossMark();
					me.showTickMark();
					successMsgCtrl.innerHTML = resJson.message;
				};
				var alfValhandleFailure = function(res){
					Dom.addClass(loadingCtrl,"hidden");
					var resJson = JSON.parse(res.responseText);
					me.hideTickMark();
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
			validateActiveMQ :function(){
				var loadingCtrl = Dom.get("loadingImg");
				var successMsgCtrl = Dom.get("successCtrl");
				var failMsgCtrl = Dom.get("failMsg");
				Dom.removeClass(me.widgets.activeMqValidateButton,"yui-push-button-focus");
				Dom.removeClass(me.widgets.activeMqValidateButton,"yui-button-focus");
				var activeMQValhandleSuccess = function(res){
					var resJson = JSON.parse(res.responseText);
					Dom.addClass(loadingCtrl,"hidden");
					console.log(resJson);
					me.hideCrossMark();
					me.showTickMark();
					successMsgCtrl.innerHTML = resJson.message;
				};
				var activeMQValhandleFailure = function(res){
					Dom.addClass(loadingCtrl,"hidden");
					var resJson = JSON.parse(res.responseText);
					console.log(resJson);
					me.hideTickMark();
					me.showCrossMark();
					failMsgCtrl.innerHTML = resJson.message;
				};
				var activeMQValidationCallback ={
					success:activeMQValhandleSuccess,
					failure: activeMQValhandleFailure
				};
				var activeMQValidateURL = "validateactivemq?appname=activemq";
				var activeMQDetailsReq = YAHOO.util.Connect.asyncRequest('GET', activeMQValidateURL, activeMQValidationCallback);
			}
	};
})();