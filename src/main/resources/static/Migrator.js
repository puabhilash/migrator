if (typeof Migrator == "undefined" || !Migrator)
{
   var Migrator = {};
}
Migrator.component = Migrator.component || {};
Migrator.utils = Migrator.utils || {};

Migrator.utils.PopupManager =function(){
	//YAHOO.Bubbling.on("folderrefselected", this.onFolderRefSelected, this);
	return ({
		displayTime: 2.5,
		dialogConfig :{
			modal: false,
		    visible: false,
		    close: false,
		    draggable: false,
		    effect:YAHOO.widget.ContainerEffect.FADE,
		    displayTime: this.displayTime,
		    zIndex: 15,
		    spanClass: "",
		    text:"no message"
		},
		displayMessage: function(parent,config){
			var el = (parent==null||parent==undefined||parent=="")?document.body:parent;
			var finalconfig = YAHOO.lang.merge(this.dialogConfig, config);
			var message = new YAHOO.widget.Dialog("migratordisplaymessage", finalconfig);
			var bd ="";
			if(finalconfig.spanClass=="wait"){
				bd =  "<table cellpadding='5' style='color:#FFFFFF'><tr><td><img class='circle' src='/migrator/res/images/alfresco-logo-vector-1.svg'/></td><td align='center'> <span>" + finalconfig.text+ "</span></td></tr></table>";
			}else{
			  	bd="<span>" + finalconfig.text + "</span>";
			}
			message.setBody(bd);
			message.render(el);
		    message.center();
		    if (this.displayTime > 0 && finalconfig.spanClass=="") {
            	message.subscribe("show", this._delayPopupHide,
                  {
                     popup: message,
                     displayTime: (this.displayTime * 1000)
                  }, true);
         	}
		    message.show();
		    return message;
	    },
	    _delayPopupHide: function PopupManager__delayPopupHide(){
         	YAHOO.lang.later(this.displayTime, this, function()
         	{
            	this.popup.destroy();
         	});
      	},
      	displayPrompt: function(parent,header,messageText){
			var buttons= [
            {
               text: "Ok",
               handler: function()
               {
                  this.destroy();
               },
               isDefault: true
            }]
			var el = (parent==null||parent==undefined||parent=="")?document.body:parent;
			var prompt = new YAHOO.widget.SimpleDialog("migratordisplayprompt",
               {
                  close: false,
                  constraintoviewport: true,
                  draggable: true,
                  modal: false,
                  visible: false,
                  zIndex: 5,
                  keylisteners:[escapeListener]
               });
            var escapeListener = new YAHOO.util.KeyListener(document,
               	{
                  	keys: YAHOO.util.KeyListener.KEY.ESCAPE
               	},
               	{
                  	fn: function(id, keyEvent)
                  	{
                     	this.destroy();
                  	},
                  	scope: prompt,
                  	correctScope: true
             });
            escapeListener.enable();
            prompt.setHeader("<center>"+header+"</center><div id='close' class='close'></div>");
            prompt.setBody("<center>"+messageText+"</center>");
            prompt.cfg.queueProperty("buttons", buttons);
            prompt.showEvent.subscribe(function(){
					var closeCtrl = Dom.get("close");
					YAHOO.util.Event.addListener(closeCtrl,"click",function(e){
						prompt.destroy();
					});
				});
            prompt.render(parent);
         	prompt.center();
         	prompt.show();
		},
		treeConfig:{
			treeContentElId:"",
			selectedFolder:"",
			nodeName:""
		},
		destinationPanel: function(nameCtrl,nodeRefCtrl) {
			var me = this;
			var destinationPanel = new YAHOO.widget.Panel("migratorDestinationPanel", { visible: false, modal: false, draggable: true, close: false, zindex: 4, keylisteners: [escapeListener] });
			var escapeListener = new YAHOO.util.KeyListener(document,
				{
					keys: YAHOO.util.KeyListener.KEY.ESCAPE
				},
				{
					fn: function(id, keyEvent) {
						console.log(keyEvent);
						console.log(this);
						this.destroy();
					},
					scope: destinationPanel,
					correctScope: true
				});
			escapeListener.enable();
			destinationPanel.setHeader("<center>Select Destination</center><div id='close' class='close'></div>");
			destinationPanel.setBody("<div class='destinationspace' id='destTree' ></div>");
			destinationPanel.setFooter("<center><table><tr><td><div id='save'></div></td><td><div id='cancel'></div></td></tr></table>");
			destinationPanel.showEvent.subscribe(function() {
				var closeCtrl = Dom.get("close");
				YAHOO.util.Event.addListener(closeCtrl, "click", function(e) {
					destinationPanel.destroy();
				});
				var saveBtn = new YAHOO.widget.Button("save", {
					label: "Ok"
				});
				var cancelBtn = new YAHOO.widget.Button("cancel", {
					label: "Cancel"
				});
				saveBtn.on("click", function() {
					nameCtrl.value = me.treeConfig.nodeName;
					nodeRefCtrl.value = me.treeConfig.selectedFolder;
					destinationPanel.destroy();
				});
				cancelBtn.on("click", function() {
					nameCtrl.value = "";
					nodeRefCtrl.value = "";
					destinationPanel.destroy();
				});
				var tree = new YAHOO.widget.TreeView("destTree");
				var loadNodeData = function(node, fnLoadComplete) {
					var child, nodes = [];
					if (node.label != null && node.label != "") {
						var success = function(res) {
							var obj = eval('(' + res.responseText + ')');
							for (var i in obj.entries) {
								node.data = {};
								node.data.noderef = obj.entries[i].entry["id"];
								node.data.allowedoperations = obj.entries[i].entry["allowableOperations"];
								child = new YAHOO.widget.TextNode({
									label: obj.entries[i].entry["name"],
									title: obj.entries[i].entry["id"],
									html: "#" + obj.entries[i].entry["allowableOperations"].toString() + ";" + obj.entries[i].entry["id"]
								}, node, false);
								nodes.push(child);
							}
							fnLoadComplete();
							for (var i in nodes) {


							}
						};
						var failure = function(res) {
							console.log(res);
							fnLoadComplete();
						};
						var callback = {
							success: success,
							falilure: failure
						};
						YAHOO.util.Connect.asyncRequest('GET', "alfrescofolders?nodeid=" + node.title, callback);
					} else {
						Migrator.utils.PopupManager.displayPrompt(document.body, "Alert", "Failed to load node data");
					}
				};
				tree.setDynamicLoad(loadNodeData, 1);
				var root = tree.getRoot();
				var repositoryNode = new YAHOO.widget.TextNode({ label: "Repository", title: "-root-", href: "#" }, root, true);
				tree.draw();
				tree.subscribe("labelClick", function(node) {
					var nodeLabel = node.title;
					me.treeConfig.selectedFolder = nodeLabel;
					me.treeConfig.nodeName = node.label;
					if (!(me.treeConfig.treeContentElId == "" || me.treeConfig.treeContentElId == null || me.treeConfig.treeContentElId == undefined)) {
						Dom.removeClass(Dom.get(me.treeConfig.treeContentElId), "selected");
					}
					var currentItem = Dom.get(node.contentElId);
					Dom.addClass(currentItem, "selected");
					me.treeConfig.treeContentElId = node.contentElId;
					console.log(node.contentElId);
				});
				tree.subscribe("clickEvent", function() {
					return false;
				});

			});
			/*destinationPanel.hideEvent.subscribe(function(e){
					YAHOO.Bubbling.fire("folderrefselected",
			         {
			           "folderref":me.treeConfig.selectedFolder
			         });
				});*/
			destinationPanel.render(document.body);
			destinationPanel.center();
			destinationPanel.show();
			return destinationPanel;
		}
    });
}();