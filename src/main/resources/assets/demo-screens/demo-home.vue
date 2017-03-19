
<style lang="scss">
 .demo-home-vue {

 }
</style>


<template>
    <div class="demo-home-vue">
        <div v-if="isLoading"><loading-div></loading-div></div>
        <div v-if="!isLoading">
            <h1>The Demo App</h1>
            <div class="p">
                <a href="#/tomes">Tomes Datatable</a>
            </div>
            <div class="p">
                <a href="#/form">Form UI</a>
            </div>
            <div class="p">
                <a href="#/markdown-textarea">Simple Text Editor</a>
            </div>
            <div class="p">
                <a href="#/file-fields">File Fields</a>
            </div>                        
        </div>
    </div>
</template>

<script>
 module.exports = {
     data: function() {
         return {
             isLoading: true,
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
         fetchData: function() {
         }
     }
 };
</script>
