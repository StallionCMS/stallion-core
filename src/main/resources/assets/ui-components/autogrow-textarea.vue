<!--
 * Stallion v1.0.0 (http://stallion.io)
 * Copyright 2016-2018 Stallion Software LLC
 * Licensed under GPL (https://github.com/StallionCMS/stallion-core/blob/master/LICENSE)
-->

<template>
    <textarea class="autogrow-textarea form-control" @input="onInput" :required="required"></textarea>
</template>

<script>
 module.exports = {
     props: {
         value: '',
         required: {
             default: undefined
         }
     },
     mounted: function() {
         this.$el.value = this.value;
         stallion.autoGrow({}, $(this.$el));
     },
     methods: {
         onInput: function(a, b) {
             console.log('onInput', a, b);
             this.$emit('input', this.$el.value);
         }
     },
     watch: {
         value: function(newVal) {
             this.$el.value = newVal;
         }
     }     
 };
</script>
