<style lang="scss">
 .image-simple-configure-vue {

 }
</style>

<template>
    <div class="image-simple-configure-vue">
        <image-selector v-if="tab==='selector'" :allow-external-images="false" :callback="selectImageCallback"></image-selector>
        <div v-if="tab==='formatting'">
            <div style="border-bottom: 1px solid #F4F4F4; padding-bottom: 1em; margin-bottom: 1em;">
                <img v-bind:src="image.thumbUrl"" style="max-width: 50px; max-width: 50px; display:inline-block; margin-right: 20px; border: 1px solid #CCC;">
                <a target="_blank" v-bind:href="image.url">{{image.url}}</a>
                <a class="btn btn-default btn-sm" href="javascript:;" @click="tab='selector'">Change Image</a>
            </div>
            <div class="form-group">
                <label>Image source name?</label>
                <input type="text" class="form-control" v-model="sourceName">
            </div>
            <div class="form-group">
                <label>Image source URL</label>
                <input type="text" class="form-control" v-model="sourceUrl">
            </div>
        </div>
    </div>
</template>

<script>
 module.exports = {
     props: {
         widgetData: Object,
         insertCallback: Function
     },
     data: function() {
         var tab = 'selector';
         var image = null;
         if (this.widgetData.data.image) {
             image = this.widgetData.data.image;
             tab = 'formatting';
         }
         return {
             sourceName: this.widgetData.data.sourceName || '',
             sourceUrl: this.widgetData.data.sourceUrl || '',
             tab: tab,
             image: image
         }
     },
     mounted: function() {
         if (this.image) {

         }
     },
     methods: {
         getWidgetData: function() {
             var wd = this.widgetData;
             wd.type = 'image-simple';
             wd.label = 'Image';
             wd.data = {};
             wd.data.sourceName = this.sourceName;
             wd.data.sourceUrl = this.sourceUrl;
             wd.data.image = this.image;
             wd.data.isBlock = true;
             wd.previewHtml = '<img src="' + this.image.thumbUrl + '">'
             wd.html = this.buildHtml(wd.data.image, wd.data);
             return wd;
         },
         selectImageCallback: function(image) {
             this.image = image;
             this.tab = 'formatting';
         },
         buildHtml: function(image, data) {
             var $wrap = $('<div></div>')
             $wrap.addClass('st-image-wrapper');
             $imgOuter = $('<div></div>').addClass('image-outer');
             var $img = $('<img>');
             $imgOuter.append($img);
             $wrap.append($imgOuter);
             $img.attr('src', image.mediumUrl);
             
             $wrap.addClass('st-image-center');
             
             if (data.sourceName) {
                 var $caption;
                 if (data.sourceUrl) {
                     $caption = $('<a></a>');
                     $caption.attr('href', data.sourceUrl).html(data.sourceName);
                 } else {
                     $caption = $('<span></span>').html(data.sourceName);
                 }
                 $caption = $('<div class="st-image-caption"></div>').append($caption);
                 $wrap.append($caption);
             }
             
             $wrap.css({'display': 'block'});
             $wrap.append($("<div></div>").addClass("image-bottom"));
             return $wrap.get(0).outerHTML;
             
         }
     }
 }

</script>
