<!--
 * Stallion v1.0.0 (http://stallion.io)
 * Copyright 2016-2018 Stallion Software LLC
 * Licensed under GPL (https://github.com/StallionCMS/stallion-core/blob/master/LICENSE)
-->

<style lang="scss">
 .text-editor-widget-modal-vue {
     .widget-option > a {
         display: block;
         padding: 1em 20px 1em 0px;
         border-bottom: 1px solid #CCC;
         border-top: 1px solid #CCC;
         margin-top: -1px;
         .material-icons {
             vertical-align: -30%;
         }
     }
 }
</style>

<template>
    <div class="widget-modal text-editor-widget-modal-vue">
        <modal-base ref="themodal" v-on:close="$emit('close')" :large="true" :title="modalTitle" >
            <div slot="body">
                <div v-if="!activeWidget.type">
                    <div class="widget-option" v-for="wt in widgetTypes">
                        <a href="javascript:;" @click="selectWidget(wt)">
                            <span class="material-icons">{{ wt.materialIcon }}</span> {{ wt.description }}
                        </a>
                    </div>
                </div>
                <div v-if="activeWidget.type">
                    <component :is="widgetConfigureTag" ref="active" :widget-original="activeWidget" @confirmed="onWidgetConfirmed" @insertable="onInsertableStateChange"></component>
                </div>
            </div>
            <div slot="footer">
                <button id="widget-modal-insert-button" v-show="!hideInsert && activeWidget" class="btn btn-primary btn-md" :disabled="disableInsert" @click="insertWidgetClicked">{{ insertLabel }}</button>
                <a @click="cancel" href="javascript:;">Cancel</a>
            </div>
        </modal-base>
    </div>
</template>

<script>
 module.exports = {
     props: {
         widgetType: '',
         widget: null,
         widgetName: null
     },
     data: function() {
         return {
             activeWidget: null,
             activeWidgetDefinition: null,
             insertLabelOverride: null,
             isNew: false,
             widgetConfigureTag: '',
             widgetTypes: this.loadWidgetTypes(),
             disableInsert: true,
             hideInsert: true,
             insertCallback: null
         }
         
     },
     computed: {
         insertLabel: function() {
             var self = this;
             if (self.insertLabelOverride) {
                 return self.insertLabelOverride;
             } else if (self.activeWidgetDefinition && self.activeWidgetDefinition.insertLabel) {
                 return self.activeWidgetDefinition.insertLabel;
             } else {
                 return "Insert Widget";
             }
         },
         modalTitle: function() {
             var self = this;
             if (self.$refs && self.$refs.active && self.$refs.active.getModalTitle) {
                 return self.$refs.active.getModalTitle(self.isNew);
             } else if (self.isNew && self.activeWidgetDefinition && self.activeWidgetDefinition.insertModalTitle) {
                 return self.activeWidgetDefinition.insertModalTitle;
             } else if (!self.isNew && self.activeWidgetDefinition) {
                 return self.activeWidgetDefinition.editModalTitle;
             } else if (self.activeWidget.type && self.isNew) {
                 return "Insert " + stallion.toTitleCase(self.activeWidget.type || 'Widget');
             } else if (self.activeWidgetType && !self.isNew) {
                 return "Edit " + stallion.toTitleCase(self.activeWidget.type || 'Widget');
             } else {
                 return "Insert Widget";
             }
         }
     },
     created: function() {
         this.onActivate();
     },
     activated: function() {
         this.onActivate();
     },
     methods: {
         onInsertableStateChange: function(state) {
             console.log('onInsertableStateChange ', state);
             if (state.hidden !== undefined) {
                 this.hideInsert = state.hidden;
             }
             if (state.enabled !== undefined) {
                 this.disableInsert = !state.enabled;
                 if (state.enabled) {
                     this.hideInsert = false;
                 }
             }
             if (state.label) {
                 this.insertLabelOverride = state.label;
             }
             if (state.callback) {
                 this.insertCallback = state.callback;
             } else {
                 this.insertCallback = null;
             }
         },
         onActivate: function() {
             var self = this;
             var activeWidget = null;
             var isNew = true;
             if (self.widget && self.widget.type && self.widget.guid) {
                 isNew = false;
                 activeWidget = JSON.parse(JSON.stringify(self.widget));
                 if (!activeWidget.data) {
                     activeWidget.data = {};
                 }
                 if (!activeWidget.widgets) {
                     activeWidget.widgets = [];
                 }
             } else {
                 var id = stallion.generateUUID();
                 activeWidget = {
                     type: self.widgetType || '',
                     guid: id,
                     name: self.widgetName || id,
                     data: {

                     },
                     content: '',
                     originalContent: '',
                     widgets: []
                 }
             }
             self.isNew = isNew;
             self.activeWidget = activeWidget;
             if (self.activeWidget) {
                 self.widgetTypes.forEach(function(wt) {
                     if (wt.type === self.activeWidget.type) {
                         self.activeWidgetDefinition = wt;
                     }
                 });
             }
             if (self.activeWidgetDefinition && self.activeWidgetDefinition.defaultInsertButtonShown) {
                 self.showInsert = true;
             } else {
                 self.showInsert = false;
             }
             self.widgetConfigureTag = this.widgetTypeToTag(activeWidget.type);
             
         },
         loadWidgetTypes: function() {
             if (this.$store && this.$store.state.widgetTypes) {
                 return this.$store.state.widgetTypes;
             }
             return [
                 {
                     type: 'image',
                     description: 'Image',
                     buttonLabel: 'Insert Image',
                     modalTitle: 'Choose Image to Insert',
                     materialIcon: 'image',
                     defaultInsertButtonShown: true
                 },
                 {
                     type: 'embed',
                     materialIcon: 'code',
                     description: 'HTML Embed (Youtube Video, Slideshare, etc.)',
                     defaultInsertButtonShown: true
                 }
             ];   
         },         
         widgetTypeToTag: function(widgetType) {
             return widgetType + '-widget-configure';
         },
         cancel: function() {
             this.$refs.themodal.close();
         },
         onWidgetConfirmed: function(widget) {
             this.$emit('confirmed', JSON.parse(JSON.stringify(widget)));
             this.$refs.themodal.close();
         },
         insertWidgetClicked: function() {
             if (this.insertCallback) {
                 this.insertCallback();
                 return;
             }
             if (this.$refs.active.insertClicked) {
                 this.$refs.active.insertClicked();
                 return;
             }
             var widget = this.activeWidget;
             widget = JSON.parse(JSON.stringify(this.$refs.active.getWidget()));
             this.$emit('confirmed', widget);
             this.$refs.themodal.close();
         },
         selectWidget: function(widgetTypeDefinition) {
             var self = this;
             self.activeWidget.type = widgetTypeDefinition.type;
             self.widgetConfigureTag = self.widgetTypeToTag(self.activeWidget.type);
             self.activeWidgetDefinition = widgetTypeDefinition;
             if (self.activeWidgetDefinition.defaultInsertButtonShown) {
                 self.showInsert = true;
             } else {
                 self.showInsert = false;
             }
         }
     }
     
     
 }

</script>
