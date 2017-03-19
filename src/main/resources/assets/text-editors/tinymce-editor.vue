<style lang="scss">
 .tinymce-editor-vue {
     .tiny-target {
         width: 100%;
         height: 300px;
         border: 1px solid #DDD;
         visibility: hidden;
     }
     .loading-overlay {
         position: absolute;
         z-index: 100;
         font-size: 20px;
         padding-top: 20px;
         padding-left: 20px;
     }
 }
</style>
<template>
    <div class="tinymce-editor-vue">
        <div class="loading-overlay">Loading editor &hellip;</div>
        <textarea :name="name" style="" class="tiny-target" v-model="html" ></textarea>
        <widget-modal v-if="showWidgetModal" v-on:close="showWidgetModal=false" :widget-type="activeWidgetType" :widget-data="activeWidgetData" :callback="insertWidgetCallback"></widget-modal>
        <insert-link-modal v-if="showInsertLinkModal" v-on:close="showInsertLinkModal=false" v-on:input="insertLinkCallback" :hide-internal-pages="options.hideInternalPages" :link="activeLinkUrl" :text="activeLinkText"></insert-link-modal>
        <component v-if="customModalShown && customModalTag" ref="custommodal" :is="customModalTag" v-on:close="customModalShown = false" :callback="customModalCallback" :options="customModalOptions"></component>
    </div>
</template>
<script>
 module.exports = {
     props: {
         html: String,
         widgets: {
             type: Array,
             default: function() {
                 return [];
             }
         },
         tinyOptions: Object,
         options: Object,
         name: String,
         editorId: String,
         changeCallback: Function,
         pluginLoaders: Array,
         extraPlugins: String,
         onKeyPress: Function,
         onSetup: Function,
         onInit: Function
     },
     data: function() {
         if (this.options.pluginLoaders) {
             this.pluginLoadersComputed = this.options.pluginLoaders;
         } else {
             this.pluginLoadersComputed = this.options.pluginLoaders;             
         }
         if (this.options.extraPlugins) {
             this.extraPluginsComputed = this.options.extraPlugins;
         } else {
             this.extraPluginsComputed = this.extraPlugins;
         }
         
         return {
             showWidgetModal: false,
             activeWidgetType: '',
             activeWidgetData: {},
             activeLinkText: '',
             activeLinkUrl: '',
             tinymce: null,
             editor: null,
             editorIdComputed: this.editorId,
             customModalTag: '',
             customModalShown: false,
             customModalOptions: {text: '', link: ''},
             customModalCallback: null,
             showInsertLinkModal: false,
             ticks: new Date().getTime()
         }
     },
     mounted: function() {
         var self = this;
         var id = self.editorId || 'tiny-' + stPublisher.generateUUID();
         self.editorIdComputed = id;
         $(this.$el).find('textarea').attr('id', id);

         if (window.tinymce && tinymce.EditorManager.get(this.editorIdComputed)) {
             tinymce.EditorManager.get(this.editorIdComputed).destroy();
         }

         
         var customOptions = this.tinyOptions || {};
         
         requirejs([
             'tinyMCE'
         ], function (tinymce) {
             console.log('initialize tinymce ', id);
             //var ele = $(this.$el).find('textarea').get(0);
             self.tinymce = tinymce;
             stPublisher.initStallionButtonsPlugin(tinymce);
             stPublisher.initHeadersPlugin(tinymce);
             var extraPlugins = self.extraPluginsComputed || '';
             if (self.pluginLoadersComputed) {
                 self.pluginLoadersComputed.forEach(function(loader) {
                     if (!loader.name) {
                         throw new Exception("Plugin loader needs field 'name'");
                     }
                     if (!loader.load) {
                         throw new Exception("Plugin loader needs a field 'load' which is a function that take tinmce as its first argument");
                     }
                     extraPlugins += ' ' + loader.name;
                     console.log('Initialize plugin ', loader.name);
                     loader.load(tinymce);
                 });
             }

             var options = {
                 selector: '#' + id,
                 statusbar: false,
                 plugins: 'autoresize textcolor colorpicker textpattern imagetools paste charmap example headers link ' + extraPlugins,
                 toolbar1: 'bold italic | styleselect | bullist numlist outdent indent blockquote removeformat | undo redo | stlink stimage stinsert ',
                 menubar: false,
                 content_css: stPublisherAdminContext.siteUrl + '/st-resource/publisher/tinymce/tinymce-content.css?ts=' + self.ticks + ',' + stPublisherAdminContext.siteUrl + '/st-resource/publisher/public/contacts-always.css?vstring=' + self.ticks,
                 init_instance_callback : function(editor) {
                     console.log('tiny init callback ', id);
                     self.editor = editor;
                     editor.vueTag = self;
                     $(self.$el).find('.loading-overlay').hide();
                     self.addWidgetToolbarToHtml();

                     if (self.onInit) {
                         self.onInit(editor, self);
                     }
                     
                 },
                 setup: function(editor) {
                     console.log('tinysetup ', id);
                     editor.on('change', function(e) {
                         self.$emit('input');
                         self.$emit('change');
                         if (self.changeCallback) {
                             self.changeCallback();
                         }
                     });

                     if (self.onKeyPress) {
                         self.$emit('input');
                         editor.on('keypress', function(e) {
                             if (self.onKeyPress) {
                                 self.onKeyPress(e);
                             }
                         });
                     }

                     if (self.onSetup) {
                         self.onSetup(editor, self);
                     }
                 }                 
             };
             Object.keys(customOptions).forEach(function(key) {
                 options[key] = customOptions[key];
             });
             console.log('call tiny init', id);
             console.log('tinytarget ', $('#' + id), $('#' + id).length);
             tinymce.init(options);

         });         
     },
     beforeDestroy: function() {
         console.log('before destroy tiny');
     },
     deactivated: function() {
         if (window.tinymce && this.editorId) {
             console.log('detach tiny', this.editorId);
             tinymce.execCommand('mceRemoveControl', true, this.editorId);
             tinymce.remove("#" + this.editorId);
             if (tinymce.EditorManager.get(this.editorId)) {
                 tinymce.EditorManager.get(this.editorId).destroy();
             }
         }
         console.log('deatched tiny');
     },
     methods: {
         getData: function() {
             var self = this;
             if (!this.editor) {
                 return {
                     html: this.html,
                     widgets: this.widgets
                 }
             }
             return {
                 widgets: this.getEditorCleanedWidgets(),
                 html: this.getEditorCleanHtml()
             };
         },
         getEditorCleanHtml: function() {
             var self = this;
             var html = this.editor.getContent({format : 'raw'});
             var $html = $('<div>' + html + '</div>');
             $html.find('.widget-toolbar').remove();
             this.widgets.forEach(function(w) {
                 var eleId = 'wrap-widget-' + w.guid;
                 var $existing = $(self.editor.getBody()).find('#' + eleId);
                 if ($existing.length < 1) {
                     return;
                 }
                 // Reset the HTML
                 $existing.find('.widget-html').html(w.html);
             });
             return $html.html();
         },
         getEditorCleanedWidgets: function() {
             var self = this;
             var cleanedWidgets = [];
             this.widgets.forEach(function(w) {
                 var eleId = 'wrap-widget-' + w.guid;
                 var $existing = $(self.editor.getBody()).find('#' + eleId);
                 if ($existing.length > 0) {
                     cleanedWidgets.push(w);
                 }
             });
             return cleanedWidgets;
         },
         addWidgetToolbarToHtml: function() {
             var self = this;
             this.widgets.forEach(function(widget) {
                 var eleId = 'wrap-widget-' + widget.guid;
                 var $wrapper = $(self.editor.getBody()).find('#' + eleId);
                 if ($wrapper.length < 1) {
                     return;
                 }
                 self.addToolbarForWidgetWrapper(widget, $wrapper);
             });
         },
         insertLinkCallback: function(link, text) {
             var self = this;
             if (!text) {
                 text = this.editor.selection.getContent();
             }
             if (!text) {
                 text = link;
             }
             console.log('insert link ', link, text);
             var $ele = $('<a></a>').attr('href', link).html(text);
             this.editor.insertContent($ele.get(0).outerHTML);
         },
         insertWidgetCallback: function(widget) {
             var self = this;
             console.log('insertWidgetCallback');
             var eleId = 'wrap-widget-' + widget.guid;
             var existing = $(this.editor.getBody()).find('#' + eleId);
             if (existing.length) {
                 self.editor.undoManager.transact(function() {
                     existing.find('.widget-html').html(widget.html);
                 });
                 this.widgets.forEach(function(w) {
                     if (w.guid === widget.guid) {
                         w.data = widget.data;
                         w.html = widget.html;
                         return false;
                     }
                 });
             } else {
                 self.editor.undoManager.transact(function() {
                     self.editor.insertContent('<div contenteditable="false" id="' + eleId + '" class="st-widget-wrapper st-widget-' + widget.type + '"  ><div class="widget-html">' + widget.html + '</div></div>');
                     self.widgets.push(widget);
                     var $wrapper = $(self.editor.getBody()).find('#' + eleId);
                     self.addToolbarForWidgetWrapper(widget, $wrapper);
                 });
             }
         },
         addToolbarForWidgetWrapper: function(widget, $wrapper) {
             var self = this;
             var eleId = $wrapper.attr('id');
             $wrapper.prepend($('<div class="widget-toolbar"><a class="widget-edit-link" href="javascript:;">Edit widget</a> | <a class="delete-widget-link" href="javascript:;">Delete</a></div>'));
             $wrapper.find('.widget-edit-link').bind('click', function() {
                 self.activeWidgetType = widget.type;
                 self.activeWidgetData = widget;
                 self.showWidgetModal = true;
             });
             $wrapper.find('.delete-widget-link').bind('click', function() {
                 //debugger;
                 self.editor.undoManager.transact(function() {
                     $wrapper.remove();
                 });
             });
             
         }
     }
 }
</script>
