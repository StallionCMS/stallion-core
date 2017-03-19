

<template>
    <div class="markdown-editor" :id="editorId">
        <textarea class="form-control"></textarea>
        <widget-modal v-if="showWidgetModal" :shown.sync="showWidgetModal" :widget-type="activeWidgetType" :widget-data="activeWidgetData" :callback="insertWidgetCallback"></widget-modal>
        <insert-link-modal v-if="showInsertLinkModal" v-on:close="showInsertLinkModal=false" :callback="insertLinkCallback"></insert-link-modal>
        <paste-html-modal v-if="showPasteHtmlModal" v-on:close="showPasteHtmlModal=false" :callback="pasteRichContentCallback"></paste-html-modal>
    </div>
</template>

<script>
 module.exports = {
     props: {
         markdown: '',
         widgets: [],
         changeCallback: Function,
         editorId: {
             required: true
         }
     },
     data: function() {
         var originalWidgets = this.widgets || [];
         var originalMarkdown = this.markdown || '';
         // Make a deep-copy of widgets         
         this.widgets = JSON.parse(JSON.stringify(originalWidgets));
         this.markdown = this.parseOutWidgetHtmlFromContent(originalMarkdown);
         return {
             cm: null,
             simplemde: null,
             lineWidgets: [],
             lineWidgetByGuid: {},
             editorToolbarTop: 140,
             editorToolbarWidth: 500,
             originalWidgets: originalWidgets,
             originalMarkdown: originalMarkdown,
             showWidgetModal: false,
             showPasteHtmlModal: false,
             showInsertLinkModal: false,
             activeWidgetData: {},
             activeWidgetType: 'dzf',
             forceAllowChange: false
         };
     },
     mounted: function() {
         var self = this;
         console.log('markdown ', this.markdown);
         this.simplemde = new SimpleMDE({
             element: $(this.$el).find('textarea').get(0),
             autoDownloadFontAwesome: false,
             toolbar: this.makeToolbar(),
             initialValue: this.markdown
         });
         this.cm = this.simplemde.codemirror;
         this.attachCodeMirrorEventHandlers();
         this.syncAllWidgetsToCodeMirror();
         this.setupScrollHandling();
         setTimeout(function() {
             self.cm.refresh();
         }, 1);
     },
     methods: {
         /****************************
         *  Getting and setting data functions
         ************************/
         getData: function() {
             this._updateWidgetsArrayFromCodeMirror();
             return {
                 markdown: this._getContentWithWidgetHtml(),
                 widgets: JSON.parse(JSON.stringify(this.widgets))
             }
         },
         setData: function(markdown, widgets) {
             console.log('set data');
             markdown = this.parseOutWidgetHtmlFromContent(markdown);
             this.cm.doc.setValue(markdown);
             this.originalMarkdown = markdown;
             this.originalWidgets = widgets;
             this.widgets = JSON.parse(JSON.stringify(widgets));
             this.markdown = markdown;

             this.syncAllWidgetsToCodeMirror();
         },
         // Syncs the data structure with the reality of codemirror:
         // Removes all widgets that have been deleted, syncs line numbers for each widget
         _updateWidgetsArrayFromCodeMirror: function() {
             var self = this;
             var cm = self.cm;
             var newWidgets = []
             var widgetByGuid = {};
             self.widgets.forEach(function(widget) {
                 widgetByGuid[widget.guid] = widget;
             });
             cm.getAllMarks().forEach(function(mark) {
                 var widget = widgetByGuid[mark.widgetGuid];
                 if (!widget) {
                     return;
                 }
                 if (!mark.lines) {
                     return;
                 }
                 widget.line = cm.doc.getLineNumber(mark.lines[0]);
                 newWidgets.push(widget);
             });
             self.widgets = newWidgets;
         },
         _getContentWithWidgetHtml: function() {
             var self = this;
             var content = self.simplemde.value();
             var lines = content.split('\n');
             var widgetByLine = {};
             self.widgets.forEach(function(widgetData) {
                 if (widgetByLine[widgetData.line] === undefined) {
                     widgetByLine[widgetData.line] = [];
                 }
                 widgetByLine[widgetData.line].push(widgetData);
             });
             var newLines = [];
             for(var i = 0; i<lines.length; i++) {
                 var line = lines[i];
                 var widgets = widgetByLine[i];
                 if (!widgets) {
                     newLines.push(line);
                     continue;
                 }
                 widgets.forEach(function(widgetData) {
                     var html = '<rawHtml><!--widget:' + widgetData.guid + '-->' + widgetData.html + '<!--end-widget:' + widgetData.guid + '--></rawHtml>';
                     if (widgetData.isBlock) {
                         html = "" + html + "";
                     }
                     line += html;
                 });
                 newLines.push(line);
             };
             var content = newLines.join('\n');
             return content;
         },
         parseOutWidgetHtmlFromContent: function(content) {
             return content.replace(/(\s\s\n|)<rawHtml><!\-\-widget:[\w\-]*\-\->[\s\S]*?<!\-\-end-widget:[\w\-]*\-\-><\/rawHtml>/g, '')
         },
         syncAllWidgetsToCodeMirror: function() {
             var self = this;
             self.lineWidgets.forEach(function(lineWidget) {
                 lineWidget.clear();
             });
             self.lineWidgets = [];
             self.widgets.forEach(function(widgetData) {
                 self._addWidgetToCodeMirror(widgetData, false);
             });
         },
         /****************************
         /* Inserting Widgets
         *****************************/
         insertWidget: function(type) {
             this.activeWidgetType = type || '';
             this.activeWidgetData = {};
             this.showWidgetModal = true;
         },
         insertWidgetCallback: function(widgetData) {
             var self = this;
             if (this.activeWidgetData && this.activeWidgetData.guid) {
                 // Is existing widget
                 self.widgets.forEach(function(existing) {
                     if (existing.guid === widgetData.guid) {
                         existing.data = widgetData.data;
                         existing.html = widgetData.html;
                         existing.isBlock = widgetData.isBlock;
                         existing.previewHtml = widgetData.previewHtml;
                         return false;
                     }
                 });
                 $(self.$el).find('.line-widget-' + widgetData.guid + ' .widget-preview').html(widgetData.previewHtml);
                 self.triggerChange();
             } else {
                 // Is new widget
                 var line = this._addWidgetToCodeMirror(widgetData, true);
                 widgetData.line = line;
                 this.widgets.push(widgetData);
                 self.triggerChange();
             }
             console.log('insert the widget', widgetData, widgetData.type, widgetData.html);
         },
         insertLink: function() {
             this.showInsertLinkModal = true;
         },
         insertLinkCallback: function(url, text) {
             var cm = this.cm;
             var state = this.simplemde.getState();
             var selection = cm.getSelection();
             if (selection) {
                 text = '';
             }
             //var options = this.simplemde.options;
             var startEnd = ["[", text + "](" + url + ")"]
	     this._replaceSelection(cm, state.link, startEnd, '');
         },
         insertImage: function() {
             this.insertWidget('image');
         },
         triggerChange: function() {
             console.log('trigger change');
             if (this.changeCallback) {
                 this.changeCallback();
             }
             
         },
         pasteRichContent: function() {
             console.log('pasteRichContent modal');
             this.showPasteHtmlModal = true;
         },
         pasteRichContentCallback: function(content) {
             var cursor = this.cm.doc.getCursor();
             console.log('insert content ' , content,  ' at ', cursor);
             this.cm.replaceRange(content, {line: cursor.line, ch: cursor.ch});
         },
         _addWidgetToCodeMirror: function(widgetData, atCursor) {
             var self = this;
             var cm = this.cm;
             var $node = $('<span class="line-widget line-widget-' + widgetData.type + ' line-widget-' + widgetData.guid +  '"><span class="widget-label">' + widgetData.label + '</span> <span class="line-widget-edit btn btn-default btn-xs">Edit widget</span> <span class="line-widget-delete btn btn-xs btn-default">remove &#xd7;</span><span class="widget-preview">' + (widgetData.previewHtml || '') + '</span></span>').addClass('line-widget');
             //var widget = self.simplemde.codemirror.addLineWidget(line, $node.get(0), {});
             var cursor = cm.doc.getCursor();
             var line = widgetData.line;
             if (line === undefined || atCursor) {
                 line = cursor.line;
             }
             var thisLine = cm.getRange({line: line, ch:0}, {line: line+1, ch: 0}).trim('\n');
             if (thisLine.length > 0) {
                 if (cursor.ch > 0) {
                     cm.replaceRange("\n", {line:line, ch:999999});
                     line = line + 1;
                     cm.execCommand('goLineDown');
                 } else {
                     cm.replaceRange("\n", {line:line, ch:0});
                     cm.execCommand('goLineUp');
                 }
             }
             cm.replaceRange("", {line:line, ch:0});
             cm.doc.addLineClass(line, 'wrap', 'editor-line-with-widget');
             var insertLeft = false;
             if (line < 1 || line <= (cm.doc.lineCount() / 2)) {
                 insertLeft = true;
             }
             
             var marker = cm.markText(
                 {line: line, ch:0}, {line: line, ch:0},
                 {
                     replacedWith: $node.get(0),
                     clearWhenEmpty: false,
                     clearOnEnter: false,
                     inclusiveRight: false,
                     inclusiveLeft: false,
                     insertLeft: insertLeft,
                     addToHistory: false,
                     widgetGuid: widgetData.guid
                 });
             marker.widgetGuid = widgetData.guid;
             cm.execCommand('goLineDown');
             marker.deleted = false;
             self.lineWidgets.push(marker);
             self.lineWidgetByGuid[widgetData.guid] = marker;
             $node.find('.line-widget-delete').click(function() {
                 cm.doc.removeLineClass(line, 'wrap', 'editor-line-with-widget');
                 var newWidgets = [];
                 self.widgets.forEach(function(existing) {
                     if (existing.guid && existing.guid !== widgetData.guid) {
                         newWidgets.push(existing);
                     }
                 });
                 self.widgets = newWidgets;
                 marker.deleted = true;
                 marker.clear();
                 widgetData.deleted = true;
                 self.triggerChange();
             });
             $node.find('.line-widget-edit').click(function() {
                 self.activeWidgetType = widgetData.type;
                 self.activeWidgetData = widgetData;
                 console.log('edit widget ', widgetData);
                 self.showWidgetModal = true;
             });
             
             return line;
         },
         _replaceSelection: function(cm, active, startEnd, url) {
	     if(/editor-preview-active/.test(cm.getWrapperElement().lastChild.className))
	         return;
             
	     var text;
	     var start = startEnd[0];
	     var end = startEnd[1];
	     var startPoint = cm.getCursor("start");
	     var endPoint = cm.getCursor("end");
	     if(url) {
	         end = end.replace("#url#", url);
	     }
	     if(active) {
	         text = cm.getLine(startPoint.line);
	         start = text.slice(0, startPoint.ch);
	         end = text.slice(startPoint.ch);
	         cm.replaceRange(start + end, {
		     line: startPoint.line,
		     ch: 0
	         });
	     } else {
	         text = cm.getSelection();
	         cm.replaceSelection(start + text + end);
                 
	         startPoint.ch += start.length;
	         if(startPoint !== endPoint) {
		     endPoint.ch += start.length;
	         }
	     }
	     cm.setSelection(startPoint, endPoint);
	     cm.focus();
         },
         /***************************
         * Code Mirror event handlers
         ****************************/
         attachCodeMirrorEventHandlers: function() {
             var self = this;
             var cm = self.cm;

             cm.on('beforeChange', function(cm, change) {
                 if (self.forceAllowChange) {
                     self.forceAllowChange = false;
                     return;
                 }
                 var marks = cm.doc.findMarks({line: change.from.line - 1, ch: 99999}, {line: change.to.line + 1, ch: 0});
                 var filteredMarks = [];
                 if (marks.length > 0) {

                 }
                 marks.forEach(function(mark) {
                     if (mark.className === 'CodeMirror-selectedtext' || !mark.widgetGuid) {
                         return;
                     }
                     filteredMarks.push(mark);
                 });

                 if (filteredMarks && filteredMarks.length > 0) {
                     change.cancel();
                     if (change.origin === '+input' && change.text.length == 2 && change.text[0] === '' && change.text[1] === '') {
                         if (change.to.line === 0) {
                             self._insertLine(cm, 0);
                         } else {
                             self._insertLine(cm, change.to.line + 1);
                         }
                     }
                 }
                 
             });

             cm.on('change', function(evt) {
                 //if (!self.loaded) {
                 //    return;
                 //}
                 console.log('codemirror changed ' + self.editorId);
                 if (self.changeCallback) {
                     self.changeCallback(evt);
                 }
             });

             cm.on('cursorActivity', function() {

             });
         },
         setupScrollHandling: function() {
             var self = this;
             var cm = self.cm;
             var toolbarSelector = '#' + self.editorId + ' .editor-toolbar';
             self.calculatedFrameHeight = $(window).height() - 150;
             setTimeout(function() {
                 self.editorToolbarTop = $(toolbarSelector).offset().top;
                 self.editorToolbarWidth = $(toolbarSelector).width() + 22;
             }, 100);
             function onScrollOrFocus () {
                 if ($(window).scrollTop() > self.editorToolbarTop && cm.hasFocus()) {
                     $(toolbarSelector).css({'background-color': 'white', 'opacity': 1, 'border-bottom': '1px solid #999', 'position': 'fixed', 'z-index': 1000, 'top': '0px', 'width': self.editorToolbarWidth + 'px'});
                 } else {
                     $(toolbarSelector).css({'position': 'static', 'width': '100%'});
                 }
                 
             }
             cm.on('blur', onScrollOrFocus);
             cm.on('focus', onScrollOrFocus);         
             $(window).scroll(onScrollOrFocus);
             
         },
         _insertLine: function(cm, index) {
             var self = this;
             var lines = cm.doc.getValue().split('\n');
             if (index >= lines.length) {
                 lines.push('');
                 index = lines.length -1;
             } else {
                 lines.splice(index, 0, '');
             }
             
             var newValue = lines.join('\n');
             self.forceAllowChange = true;
             
             self.widgets.forEach(function(widgetData) {
                 if (widgetData.line >= index) {
                     widgetData.line++;
                 }
             });
             
             cm.doc.setValue(newValue);
             self.syncAllWidgetsToCodeMirror();
             //var cursor = cm.doc.getCursor();
             cm.doc.setCursor({line: index, ch: 0});
         },
         makeToolbar: function() {
             var self = this;
             var toolbar = [
	         {
		     name: "bold",
		     className: "icon icon-bold",
		     title: "Bold",
                     action: SimpleMDE.toggleBold,
		     default: true
	         },
	         {
		     name: "italic",
		     action: SimpleMDE.toggleItalic,
		     className: "icon icon-italic",
		     title: "Italic",
		     default: true
	         },
	         {
		     name: "strikethrough",
		     action: SimpleMDE.toggleStrikethrough,
		     className: "icon icon-strikethrough",
		     title: "Strikethrough"
	         },
	         {
		     name: "heading",
		     action: SimpleMDE.toggleHeadingSmaller,
		     className: "icon icon-font-size",
		     title: "Heading",
		     default: true
	         },
                 {
		     name: "code",
		     action: SimpleMDE.toggleCodeBlock,
		     className: "icon icon-embed2",
		     title: "Code"
	         },
	         {
		     name: "quote",
		     action: SimpleMDE.toggleBlockquote,
		     className: "icon icon-quotes-left",
		     title: "Quote",
		     default: true
	         },
	         {
		     name: "unordered-list",
		     action: SimpleMDE.toggleUnorderedList,
		     className: "icon icon-list2",
		     title: "Generic List",
		     default: true
	         },
	         {
		     name: "ordered-list",
		     action: SimpleMDE.toggleOrderedList,
		     className: "icon icon-list-numbered",
		     title: "Numbered List",
		     default: true
	         },
                 {
		     name: "link",
		     action: this.insertLink,
		     className: "icon icon-link",
		     title: "Create Link",
		     default: true
	         },
	         {
		     name: "image",
		     action: this.insertImage,
		     className: "icon icon-image2",
		     title: "Insert Image",
		     default: true
	         },
	         {
		     name: "pasteRich",
		     action: this.pasteRichContent,
		     className: "icon icon-paste",
		     title: "Paste Rich Content",
		     default: true
	         },
                 
                 {
		     name: "fullscreen",
		     action: SimpleMDE.toggleFullScreen,
		     className: "icon icon-enlarge2 no-disable no-mobile",
		     title: "Toggle Fullscreen",
		     default: true
                 },
	         {
		     name: "undo",
		     action: SimpleMDE.undo,
		     className: "icon icon-undo no-disable",
		     title: "Undo"
	         },
	         {
		     name: "redo",
		     action: SimpleMDE.redo,
		     className: "icon icon-redo no-disable",
		     title: "Redo"
	         },             
                 {
                     name: "insertWidget",
                     action: function(editor) {
                         // Add your own code
                         self.insertWidget();
                     },
                     className: "editor-insert-button",
                     title: "Insert Widget",
                 },
                 "|", // Separator
             ];
             return toolbar;
             
         }
     }
 }
</script>

<style>
.markdown-editor .line-widget {
    background-color: #F9F9F9;
    border-radius: 2px;
    padding: 6px;
    margin-top: 5px;
    margin-bottom: 5px;
    border: 1px dotted #ccc;
    height: 40px;
    display: block;
}
.markdown-editor .line-widget .line-widget-delete.btn.btn-default {
    color: #944;
}
.markdown-editor .line-widget .widget-label {
    font-weight: 600;
}

.markdown-editor .line-widget.line-widget-image, .markdown-editor .line-widget.line-widget-image-collection {
    height: 50px;
    
}
.markdown-editor .line-widget-image .widget-preview, .markdown-editor .line-widget-image-collection .widget-preview {
    text-align: right;
    float: right;
}
.markdown-editor .line-widget-image-collection .widget-preview img, .markdown-editor .line-widget-image .widget-preview img {
    max-height: 38px;
    max-width: 120px;
    border: 1px solid #999;
}
 
</style>
