<template>
    <div class="image-uploader">
        <h3 class="title">Upload Image</h3>
        <form :action="formAction"
              class="image-dropzone dropzone"
              id="my-image-dropzone">
        </form>        
    </div>
</template>

<script>
 module.exports = {
     props: {
         callback: Function,
         formAction: {
             type: String,
             default: "/st-user-uploads/upload-file"
         },         
     },
     mounted: function() {
         var self = this;
         console.log('form-components/image-uploader.mounted');
         self.dropzone = new Dropzone($(self.$el).find('.image-dropzone').get(0), {
             dictDefaultMessage: "Drag one more more files here. Or click to open a file picker.",
             uploadMultiple: false,
             //             parallelUploads: true,
             maxFiles: 1,
             acceptedFiles: 'image/*,.jpg,.png,.svg,.gif',
             init: function() {
                 this.on("uploadprogress", function(file, percent, c, d) {
                     if (percent === 100) {
                         $(file.previewTemplate).find(".dz-progress").html("Processing...");
                     }
                 });
                 this.on("success", function(file, response) { 
                     //var o = JSON.parse(response);
                     this.removeFile(file);                     
                     if (self.callback) {
                         self.callback(response);
                     }
                     self.$emit('uploaded', file);

                 });
             },
         });

     }
 };
</script>
