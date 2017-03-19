
<style lang="scss">
 .file-fields-demo.vue-vue {

 }
</style>


<template>
    <div class="file-fields-demo-vue">
        <div v-if="isLoading"><loading-div></loading-div></div>
        <div v-if="!isLoading">
            <form class="form" @submit.prevent="onSave">
                <h3>File Fields</h3>
                <div class="p">
                    <label>Upload your resume</label>
                    <file-picker-field v-model="jobApp.resume" label="Upload Resume (.pdf, .doc, .docx)" label-selected="Re-upload Resume"></file-picker-field>
                </div>
                <div class="p">
                    <label>Upload headshot</label>
                    <image-picker-field v-model="jobApp.headshot" label="Upload Photo" label-selected="Change Photo"></image-picker-field>
                </div>
                <div class="p">
                    <button type="submit" class="btn btn-primary">Save this</button>
                </div>
            </form>
        </div>
    </div>
</template>

<script>
 module.exports = {
     data: function() {
         return {
             isLoading: true,
             jobApp: {}
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
             this.fetchData();
         },
         onSave: function() {
             console.log(this.jobApp);
         },
         fetchData: function() {
             var self = this;
             self.isLoading = false;
         }
     }
 };
</script>
