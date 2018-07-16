<!--
 * Stallion v1.0.0 (http://stallion.io)
 * Copyright 2016-2018 Stallion Software LLC
 * Licensed under GPL (https://github.com/StallionCMS/stallion-core/blob/master/LICENSE)
-->


<style lang="scss">
 .checkboxes-vue {
     .checkboxes-choice label {
         font-weight: normal;
     }
     input[type="checkbox"] {
         vertical-align: 7%;
     }
 }
</style>

<template>
    <div class="checkboxes-vue">
        <div class="checkboxes-choice" v-for="choice in choicesComputed">
            <label><input type="checkbox" :checked="choice.checked" :value="choice.value" @change="updateValue"> {{ choice.label }}</label>
        </div>
    </div>
</template>

<script>
 module.exports = {
     props: {
         choices: {
             default: function() {
                 return [];
             }
         },
         value: null
     },
     data: function() {
         var self = this;
         return {
             choicesComputed: []
         };
     },
     created: function() {
         var self = this;
         this.setChoicesComputed();
     },
     watch: {
         choices: function() {
             var self = this;
             this.setChoicesComputed();
         },
         value: function(newValue) {
             var self = this;
             if (newValue === null || newValue === undefined) {
                 return 
             }
             this.choicesComputed.forEach(function(choice) {
                 choice.checked = newValue.indexOf(choice.value) !== -1;
             });
         }
     },
     methods: {
         setChoicesComputed: function() {
             var self = this;
             var value = this.value || [];
             var computed = [];
             this.choices.forEach(function(choice) {
                 var o = choice;
                 if (typeof(choice) === typeof('')) {
                     o = {
                         value: choice,
                         label: choice
                     }
                 }
                 o.checked = value.indexOf(o.value) > -1;
                 computed.push(o);
             });
             self.choicesComputed = computed;
         },
         updateValue: function() {
             var self = this;
             var newValue = [];
             $(this.$el).find('input[type="checkbox"]').each(function() {
                 var $el = $(this);
                 if ($el.is(':checked')) {
                     newValue.push($el.val());
                 }
             });
             this.$emit('input', newValue);
         }
     }
 };
</script>
