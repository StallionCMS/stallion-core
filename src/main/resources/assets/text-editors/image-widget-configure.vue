
<style lang="scss">
 .image-widget-configure-vue {
     form.my-dropzone {
         margin-top: 1em;
     }
     .st-data-table-vue {
         border: 1px solid #CCC;
         border-width: 0px 1px 1px 1px;
         .data-table-header {
             padding: 1em 0px 1em 0px;
             .table-title {
                 display: none;
             }
         }
         height: 70vh;
         overflow: auto;
     }
     
 }
</style>


<template>
    <div class="image-widget-configure-vue image-widget-configure">
        <div v-if="phase==='selection'">
            <div v-if="enableTabs">
                <ul class="nav nav-tabs" role="tablist">        
                    <li v-if="showLibrary" role="presentation" v-bind:class="{active: tab==='chooser'}" @click="tab='chooser'"><a class="" href="javascript:;" >Image Library</a></li>
                    <li role="presentation" @click="tab='uploader'" v-bind:class="{active: tab==='uploader'}"><a href="javascript:;"  >Upload</a></li>
                    <li v-if="allowExternal" role="presentation" @click="tab='external'" v-bind:class="{active: tab==='external'}"><a href="javascript:;" >Web Address (URL)</a></li>
                </ul>
            </div>
            <div class="p">
                <image-library-chooser  v-if="tab==='chooser'" @chosen="onChosen"></image-library-chooser>
                <image-upload-target v-if="tab==='uploader'" @uploaded="onChosen"></image-upload-target>
            </div>
        </div>
    </div>
</template>

<script>
 module.exports = {
     props: {
         widgetOriginal: Object,
         showLibrary: {
             default: true
         },
         allowExternal: {
             default: true
         },
         enableTabs: {
             default: true
         }
     },
     data: function() {
         var self = this;
         var widget = JSON.parse(JSON.stringify(this.widgetOriginal));
         var defaults = {
             altText: '',
             title: '',
             caption: '',
             linkUrl: '',
             alignment: 'center',
             image: {}
         };
         Object.keys(defaults).forEach(function(key) {
             widget.data[key] = defaults[key];
         });
         var phase = 'selection';
         var tab = 'chooser';
         return {
             widget: widget,
             tab: tab,
             phase: phase
         }
     },
     mounted: function() {
         
     },
     methods: {
         onChosen: function(image) {
             var self = this;
             this.widget.data.image = image;
             this.widget.previewHtml = '<img src="' + image.thumbUrl + '">';
             this.widget.content = this.buildHtml(image, this.widget.data);
             this.$emit('confirmed', this.widget);
         },
         getWidget: function() {
             return this.widget;
         },
         buildHtml: function(image, data) {
             var $wrap = $('<div></div>')
             $wrap.addClass('st-image-wrapper');
             $imgOuter = $('<div></div>').addClass('image-outer');
             var $img = $('<img>');
             $imgOuter.append($img);
             $wrap.append($imgOuter);
             $img.attr('src', image.mediumUrl).attr('alt', data.altText).attr('title', data.altText);
             
             $wrap.addClass('st-image-' + data.alignment);
             
             $img.css({'border-width': '1px', 'border-style': 'solid', 'border-color': '#F9F9F9'});

             if (data.title) {
                 var $title = $('<h5 class="st-image-title"></h5>').html(data.title);
                 $wrap.prepend($title);
             }
             
             if (data.caption) {
                 var $caption = $('<div class="st-image-caption"></div>').html(data.caption);
                 $wrap.append($caption);
             }
             
             $wrap.css({'display': 'block'});
             
             if (data.linkUrl) {
                 var $a = $('<a></a>');
                 $a.attr('href', data.linkUrl);
                 $img.wrap($a);
             }
             $wrap.append($("<div></div>").addClass("image-bottom"));
             return $wrap.get(0).outerHTML;
             
         }
     }
 }

</script>
