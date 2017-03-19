


<style>

</style>


<template>
    <div>
        <form v-bind:action="formAction"
              class="my-dropzone st-file-dropzone dropzone"
              id="my-awesome-dropzone">
            
        </form>        
    </div>
</template>

<script>
 module.exports = {
     props: {
         options: Object,
         formAction: {
             type: String,
             default: "/st-user-uploads/upload-file"
         },
         after: Function
     },
     mounted: function() {
         var self = this;
         var opts = this.options || {};
         if (!opts.dictDefaultMessage) {
             opts.dictDefaultMessage = "Drag one more more files here. Or click to open a file picker.";
         }
         opts.init = function() {
             this.on("uploadprogress", function(file, percent, c, d) {
                 if (percent === 100) {
                     $(file.previewTemplate).find(".dz-progress").html("Processing...");
                 }
             });
             this.on("success", function(info, file) {
                 this.removeFile(info);
                 if (self.after) {
                     self.after(file);
                 }
                 self.$emit('uploaded', file);
             });
         };
         this.dropzone = new Dropzone($(this.$el).find('.st-file-dropzone').get(0), opts);
     }
 };
</script>




