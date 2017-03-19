

<template>
    <div class="image-widget-configure">
        <image-selector v-if="tab==='selector'" :callback="selectImageCallback"></image-selector>
        <div v-if="tab==='formatting'">
            <div style="border-bottom: 1px solid #F4F4F4; padding-bottom: 1em; margin-bottom: 1em;">
                <img v-bind:src="image.thumbUrl"" style="max-width: 50px; max-width: 50px; display:inline-block; margin-right: 20px; border: 1px solid #CCC;">
                <a target="_blank" v-bind:href="image.url">{{image.url}}</a>
                <a class="btn btn-default btn-sm" href="javascript:;" @click="tab='selector'">Change Image</a>
            </div>
            <image-full-formatting ref="formatting" name="formatting" :existing-formatting="formatting"></image-full-formatting>
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
             formatting: JSON.parse(JSON.stringify(this.widgetData.data)),
             tab: tab,
             image: image
         }
     },
     mounted: function() {
     },
     methods: {
         getWidgetData: function() {
             var wd = this.widgetData;
             wd.type = 'image';
             wd.label = 'Image';
             wd.data = this.$refs.formatting.getData();
             wd.data.image = this.image;
             wd.data.isBlock = true;
             wd.previewHtml = '<img src="' + this.image.thumbUrl + '">'
             //wd.data.embedLink = this.embedLink;
             //wd.data.embedCode = this.embedCode;
             console.log('image ', wd.data.image, ' data ', wd.data);
             wd.html = this.buildHtml(wd.data.image, wd.data);
             return wd;
         },
         selectImageCallback: function(image) {
             this.image = image;
             this.tab = 'formatting';
             //this.okToInsert = true;
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
