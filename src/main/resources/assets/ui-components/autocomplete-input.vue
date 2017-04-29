
<style lang="scss">
 .autocomplete-input-vue {

 }
</style>

<template>
    <input @input="onInput" :value="value" @change="onChange" :class="'form-control autocomplete-input-vue ' + cssClass" :placeholder="placeholder"" :required="required">
</template>

<script>
 module.exports = {
     props: {
         config: {
             default: function() {
                 return {};
             }
         },
         placeholder: {
             default: ''
         },
         required: undefined,
         cssClass: '',
         choices: null,
         value: null
     },
     data: function() {
         return {
         };
     },
     mounted: function() {
         var config = $.extend({}, this.config);

         if (config.source === undefined || config.source === null) {
             var choices = this.choices || [];
              config.source = function(term, suggest){
                  term = term.toLowerCase();
                  var matches = [];
                  for (i=0; i<choices.length; i++)
                      if (~choices[i].toLowerCase().indexOf(term)) matches.push(choices[i]);
                  suggest(matches);
              }
         }
         config.selector = this.$el;
         new autoComplete(config);
     },
     methods: {
         onInput: function(evt) {
             this.$emit('input', evt.target.value);
         },
         onChange: function(evt) {
             this.$emit('input', evt.target.value);
             this.$emit('change', evt);
         }
     }
 };
</script>
