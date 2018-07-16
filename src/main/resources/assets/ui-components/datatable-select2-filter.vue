<!--
 * Stallion v1.0.0 (http://stallion.io)
 * Copyright 2016-2018 Stallion Software LLC
 * Licensed under GPL (https://github.com/StallionCMS/stallion-core/blob/master/LICENSE)
-->

<style lang="scss">

</style>
<template>
    <div class="publisher-style-guide-vue presto-details-page">
        <select2-field :multiple="multiple" :choices="choices" :config="config"></select2-field>
    </div>
</template>
<script>
 module.exports = {
     props: {
         fieldName: {
             required: true,
             type: String
         },
         multiple: {
             default: false,
             type: Boolean
         },
         datatable: {
             required: true,
             type: Object
         },
         choices: Array,
         config: {},
         change: Function
     },
     data: function() {
         return {};
     },
     methods: {
         syncFromRoute: function() {
             
         }
     },
     watch: {
         value: function(val, old) {
             var self = this;
             if (typeof(val) === typeof([])) {
                 val = [val];
             }
             self.datatable.clearFilter(this.fieldName);
             if (val) {
                 self.datatable.clearFilter(this.fieldName);
                 self.datatable.addFilter(this.fieldName, val, 'any');
             }
             self.datatable.navigate({page: 1});
             if (this.change) {
                 this.change(val, old);
             }
         }
     }
 }
</script>
