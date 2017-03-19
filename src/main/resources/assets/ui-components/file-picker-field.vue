
<style lang="scss">
 .file-picker-field-vue {

 }
</style>

<template>
    <div class="file-picker-field-vue">
        <button class="btn btn-default pure-button"  v-on:click="modalShown=true">{{ file && file.url ? labelSelected : label }}</button>
        <file-picker-modal v-if="modalShown" @close="modalShown=false" @chosen="onChosen"></file-picker-modal>
        <a v-if="file && file.url" :href="file.url" target="new">{{ file.name }}</span>
    </div>
</template>

<script>
 module.exports = {
     props: {
         label: {
             default: 'Choose File'
         },
         labelSelected: {
             default: 'Change File'
         },
         value: {
             default: function() {
                 return {};
             }
         }
     },
     data: function() {
         return {
             file: this.value || {},
             modalShown: false
         };
     },
     methods: {
         onChosen: function(file) {
             this.file = file;
             this.$emit('input', file);
             this.$emit('change', file);
         }
     },
     watch: {
         'value': function(file) {
             this.file = file;
         }
     }
 };
</script>
