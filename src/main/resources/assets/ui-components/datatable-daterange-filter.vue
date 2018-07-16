<!--
 * Stallion v1.0.0 (http://stallion.io)
 * Copyright 2016-2018 Stallion Software LLC
 * Licensed under GPL (https://github.com/StallionCMS/stallion-core/blob/master/LICENSE)
-->

<style lang="scss">
 .datatable-daterange-filter-vue {
     
 }
</style>
<template>
    <div class="datatable-daterange-filter-vue">
        <date-picker :value="value.rangeStart" @input="onInput" ></date-picker> to <date-picker :value="value.rangeEnd" @input="onInput"></date-picker>
    </div>
</template>
<script>
 module.exports = {
     props: {
         fieldName: {
             required: true,
             type: String
         },
         value: {
             default: function() {
                 return {
                     rangeStart: (new Date().getTime() / 10000) - 86400,
                     rangeEnd: (new Date().getTime() / 10000) - 86400
                 }
             }
         },
         syncOnChange: {
             default: true,
             type: Boolean
         },
         datatable: {
             required: true,
             type: Object
         }
     },
     data: function() {
         return {
             rangeStartLocal: (this.value && this.value.rangeStart) ? this.value.rangeStart : defaultValue,
             rangeStartEnd: (this.value && this.value.rangeEnd) ? this.value.rangeEnd : defaultValue
         };
     },
     methods: {
         toDatatable: function() {
             dt.clearFilter(this.fieldName);
             dt.addFilter(this.fieldName, moment(this.rangeStartLocal).format('YYYY-MM-DD') + ' 00:00:00', '>');
             dt.addFilter(this.fieldName, moment(this.rangeEndLocal).format('YYYY-MM-DD') + ' 23:59:59', '<=');
             self.datatable.navigate({page: 1});
             return;
         },
         onStartInput: function(newVal) {
             this.rangeStartLocal = newVal;
             this.$emit({rangeStart: this.rangeStartLocal, rangeEnd: this.rangeEndLocal});
             if (this.syncOnChange) {
                 this.toDatatable();
             }
         },
         onEndInput: function(newVal) {
             this.rangeEndLocal = newVal;
             this.$emit({rangeStart: this.rangeStartLocal, rangeEnd: this.rangeEndLocal});
             if (this.syncOnChange) {
                 this.toDatatable();
             }
         },
         syncFromRoute: function() {
             
         }
     },
     watch: {
         'value.rangeEnd': function(val, old) {
             
         },
         'value.rangeStart': function(val, old) {
             
         }
     }
 }
</script>
