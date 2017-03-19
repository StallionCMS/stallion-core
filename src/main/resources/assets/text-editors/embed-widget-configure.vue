

<template>
    <div class="embed-widget-configure">
        <div>
            <ul class="nav nav-tabs" role="tablist">        
                <li role="presentation" v-bind:class="{'active': tab==='embedLink'}"><a class="" href="javascript:;" @click="tab='embedLink'">Embed from a URL</a></li>
                <li role="presentation" v-bind:class="{'active': tab==='embedHtml'}"><a href="javascript:;"  @click="tab='embedHtml'">Embed HTML</a></li>
            </ul>
        </div>
        <div v-if="tab==='embedLink'">
            <div class="form-group">
                <label>Paste in a link to the content you want to embed. Can be a Youtube video, Twitter message, Facebook post, or anything else, and we'll try to guess the embed code. If you have an exact embed code, click "Embed HTML" and paste it in there.</label>
                <input  class="form-control" autofocus="autofocus" name="embedLink" v-model="widget.data.embedLink">
            </div>
        </div>
        <div v-if="tab==='embedHtml'">
            <div class="form-group">
                <label>The HTML Embed code</label>
                <autogrow-textarea class="form-control" autofocus="autofocus" name="embedCode" v-model="widget.data.embedCode" ></autogrow-textarea>
            </div>
        </div>
    </div>
</template>

<script>
 module.exports = {
     props: {
         widgetOriginal: {
             type: Object,
             required: true
         }
     },
     data: function() {
         var widget = JSON.parse(JSON.stringify(this.widgetOriginal));
         widget.data = widget.data || {};
         widget.data.embedLink = widget.data.embedLink || '';
         widget.data.embedCode = widget.data.embedCode || '';
         return {
             widget: widget,
             tab: 'embedLink'
         }
     },
     mounted: function() {
     },
     methods: {
         getWidget: function() {
             var wd = this.widget;
             wd.label = 'Embed code';
             wd.previewHtml = '<span style="font-size: 11px; margin-left: 5px;display:inline-block;">' + (this.widget.data.embedLink || this.widget.data.embedCode.substr(0, 120).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;')) + '...</span>';
             wd.content = wd.data.embedCode;
             return wd;
         }
     },
     watch: {
         'widget.data.embedLink': function(link) {
             var self = this;
             var $ele = $('<div></div>');
             //self.link = self.embedLink.value;
             $ele.oembed(link, {
                 afterEmbed: function(o) {
                     if (typeof(o.code) === 'string') {
                         self.embedCode = o.code;
                     } else {
                         self.embedCode = o.code[0].outerHTML;
                     }
                     //debugger;
                     //debugger;
                     //self.parent.saveWidget();
                 }
             });
         }
     }
 }
</script>
