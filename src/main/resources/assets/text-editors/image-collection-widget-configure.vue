

<template>
    <div class="image-collection-widget-configure">
        <image-collection-configure ref="collection" :data="widgetData.data"></image-collection-configure>
    </div>
</template>

<script>
 module.exports = {
     props: {
         widgetData: Object,
         insertCallback: Function
     },
     data: function() {
         return {
         }
     },
     methods: {
         getWidgetData: function() {
             var self = this;
             var data = this.$refs.collection.getData();
             var widgetData = {
                 isBlock: true,
                 type: 'image-collection',
                 label: 'Image Collection',
                 previewHtml: '<img src="' + data.images[0].src + '">',
                 data: data,
                 html: data.html
             };
             delete data.html;
             return widgetData;
         }
     }
 }

</script>


<style>

</style>
