<!--
 * Stallion v1.0.0 (http://stallion.io)
 * Copyright 2016-2018 Stallion Software LLC
 * Licensed under GPL (https://github.com/StallionCMS/stallion-core/blob/master/LICENSE)
-->



<template>
    <div class="internal-link-picker">
        <loading-div v-if="loading">Loading</loading-div>
        <div v-show="!loading">
            <label>Pick a page or blog post</label>
            <select name="pageSelector" class="form-control" v-model="link"></select>
        </div>
    </div>
</template>

<script>
 module.exports = {
     props: {
         callback: Function
     },
     data: function() {
         return {
             loading: true,
             link: ''
         }
     },
     mounted: function() {
         var self = this;
         stallion.request({
             url: '/st-publisher/content/all-live-contents',
             success: function(o) {
                 self.items = o.pager.items;
                 self.initSelect();
             }
         });
     },
     methods: {
         initSelect: function() {
             var self = this;
             var data = [{id: '', text: 'Pick a page'}];
             var labelByUrl = {};
             var $select = $(self.$el).find('select');
             self.items.forEach(function(item) {
                 labelByUrl[item.permalink] = item.title;
                 data.push({
                     id: item.permalink,
                     text: item.title
                 });
             });
             console.log('data', data, $select, $select.get(0));
             $select.select2({
                 data: data,
                 width: '100%',
                 placeholder: 'Select an option'
             }).on('select2:select', function (evt) {
                 self.link = $select.val();
                 self.callback({url: $select.val(), text: labelByUrl[self.link]});
             });
             self.loading = false;
         }
     }
 }
</script>
