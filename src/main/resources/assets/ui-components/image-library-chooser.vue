<!--
 * Stallion v1.0.0 (http://stallion.io)
 * Copyright 2016-2018 Stallion Software LLC
 * Licensed under GPL (https://github.com/StallionCMS/stallion-core/blob/master/LICENSE)
-->




<style lang="scss">
 .image-library-chooser-vue {
     .image-thumbnail {
         max-width: 33px;
         max-height: 33px;
     }
 }
</style>

<template>
    <div class="image-library-chooser-vue">
        <st-data-table label="file" data-url="/st-user-uploads/library/image" browser-url-template="#" :columns="columns" :infinite-scroll="false"></st-data-table>
    </div>
</template>

<script>
 module.exports = {
     props: {

     },
     data: function() {
         var self = this;
         return {
             files: [],
             columns: [
                 {
                     component: {
                         template: '<a class="btn btn-primary" @click="pickFile">Pick</a>',
                         methods: {
                             pickFile: function() {
                                 self.$emit('chosen', this.item);
                             }
                         }
                     }
                 },
                 {
                     title: 'Name',
                     field: 'name'
                 },
                 {
                     title: 'Size',
                     render: function(item) {
                         if (item.height && item.width) {
                             return item.width + 'x' + item.height;
                         } else {
                             if (item.sizeBytes < 1048576) {
                                 return parseInt(item.sizeBytes / 1024, 10) + 'KB';
                             } else {
                                 return parseInt(item.sizeBytes / 1048576, 10) + 'MB';
                             }
                         }
                     }
                 },
                 {
                     component: {
                         template: '<img class="image-thumbnail" v-if="isImage" :src="item.thumbUrl"><span v-else class="material-icons">{{icon}}</span>',
                         data: function() {
                             var icon = 'description';
                             if (this.item.extension === 'pdf') {
                                 icon = 'picture_as_pdf'
                             } else if (this.item.extension === 'mobi' || this.item.extension === 'epub') {
                                 icon = 'book';
                             }
                             //picture_as_pdf
                             return {
                                 icon: icon,
                                 isImage: this.item.type === 'image'
                             };
                         }
                     }
                 },
                 {
                     component: {
                         template: '<a class="" target="new" :href="item.url" class="material-icons">file_download</a>'
                     }
                 }
             ]
         };
     },
     created: function() {

     },
     activated: function() {

     },
     methods: {
       
     }
 };
</script>
