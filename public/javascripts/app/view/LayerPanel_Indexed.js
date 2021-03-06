
//------------------------------------------------------------------------------
Ext.define('MyApp.view.LayerPanel_Indexed', {
    extend: 'MyApp.view.LayerPanel_Common',
    alias: 'widget.layer_indexed',

    requires: [
        'MyApp.view.LegendElement',
        'MyApp.view.LegendTitle'
    ],

    width: 400,
    bodyPadding: '0 0 5 0', // just really need to pad bottom to maintain spacing there
    
    //--------------------------------------------------------------------------
    initComponent: function() {
    	
        var me = this;
        me.DSS_RecievedColorKey = false;

        Ext.applyIf(me, {
            items: [{
				xtype: 'container',
				itemId: 'legendcontainer',
				x: 40,
				y: 4,
				padding: '0 0 5 0',
//				style: {
//					border: '1px solid #f0f0f0'
//				},
				width: 346,
				layout: {
					type: 'column'
				}
			},{
            	xtype: 'button',
            	x: 390,
            	y: 4,
            	width: 23,
            	// if no WMS layer bound, just hide this control
            	hidden: true,//(typeof me.DSS_Layer === 'undefined'), 
            	icon: 'app/images/go_icon_small.png',
            	handler: function(self) {
            		me.showColorChips();
            		me.createOpacityPopup(self, me.hideColorChips, me);
            	},
            	tooltip: {
            		text: 'Viewable Layer Overlay'
            	}
			},{
            	xtype: 'button',
            	x: 390,
            	y: 30,
            	width: 23,
            	hidden: true,
            	icon: 'app/images/eye_icon.png',
            	handler: function(self) {
            		alert('Query for this layer would be run here...');
            	},
            	tooltip: {
            		text: 'Preview only this criteria selection'
            	}
			}]
        });

        me.callParent(arguments);
        
        // TODO: Code path should be deprecated...
        if (me.DSS_LegendElements) {
			var cont = me.getComponent('legendcontainer');
			for (var i = 0; i < me.DSS_LegendElements.length; i++) {
				// add index for every other colouring
				me.DSS_LegendElements[i].DSS_LegendElementIndex = i-1;
				var element = Ext.create('MyApp.view.LegendElement', 
					me.DSS_LegendElements[i]);
				cont.insert(i, element);
			}
		}
        this.DSS_RequestTryCount = 0;
        this.requestLayerRange(this);
    },

	//--------------------------------------------------------------------------
    requestLayerRange: function(container) {

    	var me = this;
    	
		var queryLayerRequest = { 
			name: container.DSS_QueryTable,
			type: 'colorKey',
		};
    	
		var obj = Ext.Ajax.request({
			url: location.href + 'layerParmRequest',
			jsonData: queryLayerRequest,
			timeout: 10000,
			scope: container,
			
			success: function(response, opts) {

				// TODO: keep this check? Or should the server be sending a fail message?		
				if (response.responseText != '') {				
					// Note: old versions of IE may not support Json.parse...
					var obj = JSON.parse(response.responseText);
					
					if (obj.length == 0) {
						console.log("layer request object return was null?");
						return;
					}
					
					// adding multiple elements causes a layout calc each time...
					//	disable that for performance
					Ext.suspendLayouts();
					var cont = this.getComponent('legendcontainer');
					for (var i = 0; i < obj.length; i++) {
						// add index for every other colouring
						obj[i].DSS_LegendElementIndex = i-1;
						var element = Ext.create('MyApp.view.LegendElement', 
							obj[i]);
						cont.insert(i, element);
					}
					me.DSS_RecievedColorKey = true;

					// Based on weird timing issues, the selection information can
					//	get set before a color key comes in...So crutching that up
					if (me.DSS_SavedSetSelectionCriteria) {
						me.setSelectionCriteria(me.DSS_SavedSetSelectionCriteria);
					}
					/*if (me.header.getComponent('DSS_ShouldQuery').pressed && DSS_DoExpandQueried) {
						me.expand();
					}*/

					// Layouts were disabled...must turn them back on!!
					Ext.resumeLayouts(true);
				}
			},
			
			failure: function(response, opts) {
				console.log('layer request failed');
				if (this.DSS_RequestTryCount < 5) {
					console.log('trying again');
					this.DSS_RequestTryCount++;
					this.requestLayerRange(this);
				}
				else {
					console.log('giving up');
				}
			}
		});
	},

	
    //--------------------------------------------------------------------------
	clearChecks: function() {
		
        var cont = this.getComponent('legendcontainer');
        for (var i = 0; i < cont.items.length; i++) {
        	var item = cont.items.items[i];
        	item.setChecked(false);
        }
    },

    //--------------------------------------------------------------------------
	showColorChips: function() {
		
        var cont = this.getComponent('legendcontainer');
        for (var i = 0; i < cont.items.length; i++) {
        	var item = cont.items.items[i];
        	item.showColorChip();
        }
    },
	
    //--------------------------------------------------------------------------
	hideColorChips: function() {
		
        var cont = this.getComponent('legendcontainer');
        for (var i = 0; i < cont.items.length; i++) {
        	var item = cont.items.items[i];
        	item.hideColorChip();
        }
    },
    
    //--------------------------------------------------------------------------
    getSelectionCriteria: function() {
    	
		var queryLayer = { 
			name: this.DSS_QueryTable,
			type: 'indexed',
			matchValues: []
		};
		
		var addedElement = false;
        var cont = this.getComponent('legendcontainer');
        for (var i = 0; i < cont.items.length; i++) {
        	var item = cont.items.items[i];
        	if (item.elementIsChecked()) {
        		addedElement = true;
        		var queryIdx = item.getElementQueryIndex();
        		queryLayer.matchValues.push(queryIdx);
        	}
        }
        
      //  if (!addedElement) {
       // 	return;
       // }
        return queryLayer;
    },

    //--------------------------------------------------------------------------
    setSelectionCriteria: function(jsonQuery) {

    	if (!jsonQuery || !jsonQuery.queryLayers) {
			this.header.getComponent('DSS_ShouldQuery').toggle(false);
			this.hide();
			this.clearChecks();
    		return;
    	}

    	if (!this.DSS_RecievedColorKey) {
    		this.DSS_SavedSetSelectionCriteria = jsonQuery;
    		return;
    	}
    	
		for (var i = 0; i < jsonQuery.queryLayers.length; i++) {
		
			var queryElement = jsonQuery.queryLayers[i];
			
			// in query?
			if (queryElement && queryElement.name == this.DSS_QueryTable) {
				// yup
				this.show();
				this.header.getComponent('DSS_ShouldQuery').toggle(true);
				
				// start with a clean slate, then check only the ones that need it
				this.clearChecks();
				var cont = this.getComponent('legendcontainer');
				// get each match value in the query...
				for (var j = 0; j < queryElement.matchValues.length; j++) {
					// hopefully find the right checkbox that corresponds to it...
					for (var k = 0; k < cont.items.items.length; k++) {
						var item = cont.items.items[k];
						// and make that checkbox checked...
						if (item.getElementQueryIndex() == queryElement.matchValues[j]) {
							item.setChecked(true);
							// don't check any more items for this match value
							break;
						}
					}
				}
				return;
        	}
        }
				
		// Nope, mark as not queried
    	this.hide();
		this.header.getComponent('DSS_ShouldQuery').toggle(false);
    },

    //--------------------------------------------------------------------------    
	resetLayer: function() {
    	
		this.header.getComponent('DSS_ShouldQuery').toggle(false);
		this.clearChecks();
    }
    
});

