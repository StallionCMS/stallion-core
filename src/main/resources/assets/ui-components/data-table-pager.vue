<!--
 * Stallion v1.0.0 (http://stallion.io)
 * Copyright 2016-2018 Stallion Software LLC
 * Licensed under GPL (https://github.com/StallionCMS/stallion-core/blob/master/LICENSE)
-->


<style lang="scss">
 .data-table-pager-vue {
     .pager-link {
         border: 1px solid #999;
         display: inline-block;
         margin-right: 2px;
         padding: 3px 7px 3px 7px;
     }
     .current-page {
         color: #999;
         cursor: default;
     }
 }
</style>

<template>
    <div class="data-table-pager-vue">
        <div v-if="pager && pager.pageCount > 1">
            <a v-bind:class="{'pager-link-text': true, 'pager-link': true, 'current-page': page==1}" :href="makeLink(1)">First</a>
            <a v-for="num in pager.surroundingPages" :href="makeLink(num)" v-bind:class="{'pager-link': true, 'current-page': num==page}">
                {{num}}
            </a>
            <a v-bind:class="{'pager-link-text': true, 'pager-link': true, 'current-page': page==pager.pageCount}" :href="makeLink(pager.pageCount)">Last</a>
        </div>
    </div>
</template>
<script>
 module.exports = {
     props: {
         page: 1,
         makeLink: {
             required: true,
             type: Function
         },
         basePath: {
             required: true,
             type: String
         },
         pager: Object
     },
     data: function() {
         return {
             
         };
     },
     methods: {
         query: function(params) {

         },
         makeUrl: function() {

         }
     }
     
 }
</script>
