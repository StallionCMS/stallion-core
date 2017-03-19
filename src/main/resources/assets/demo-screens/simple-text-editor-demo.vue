
<style lang="scss">
 .simple-text-editor-demo-vue {

 }
</style>


<template>
    <div class="simple-text-editor-demo-vue">
        <div v-if="isLoading"><loading-div></loading-div></div>
        <div v-if="!isLoading">
            <h1>Simple Text Editor</h1>
            <markdown-textarea @input-debounced="onInputDebounced" @input="onInput" :original-content="theMarkdown"></markdown-textarea>
        </div>
    </div>
</template>

<script>
 module.exports = {
     data: function() {
         return {
             isLoading: true,
             theMarkdown: 'The *bold* truth!'
         };
     },
     created: function() {
         this.onRoute();
     },
     watch: {
         '$route': 'onRoute'
     },
     methods: {
         onRoute: function() {
             var self = this;
             self.isLoading = false;

             //this.fetchData();
         },
         onInput: function() {
             console.log('text editor demo onInput');
         },
         onInputDebounced: function() {
             console.log('text editor demo onInputDebounced');
         },
         fetchData: function() {
             var self = this;
             
         }
     }
 };
</script>
