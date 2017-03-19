

<template>
    <div class="paste-html-modal">
        <modal-base ref="themodal" @close="$emit('close')" title="Paste Rich Content or HTML" :callback="saveCallback">
            <div slot="body">
                <div class="row">
                    <div class="col-sm-6">
                        <label>Paste rich text, HTML, or from Google Docs or Word into this text box.</label>
                        <div contenteditable="true" v-on:paste="htmlUpdated" class="paste-target" name="pasteTarget"></div>
                    </div>
                    <div class="col-sm-6">
                        <label>Preview the resulting markdown here:</label>
                        <textarea class="markdown-preview" class="form-control" name="markdownPreview" v-model="markdown"></textarea>
                    </div>
                </div>
            </div>
        </modal-base>
    </div>
</template>

<script>
 module.exports = {
     props: {
         callback: Function
     },
     data: function() {
         console.log('pasteHtmlModal data');         
         return {
             markdown: ''
         }
     },
     mounted: function() {
         console.log('pasteHtmlModal created');
     },
     methods: {
         htmlUpdated: function(evt) {
             var self = this;
             setTimeout(function() {
                 var $target = $(self.$el).find('.paste-target');
                 var markdown = toMarkdown($target.html());
                 markdown = markdown.replace(/<[^>]+>/g, '');
                 self.markdown = markdown;
             }, 20);
         },
         saveCallback: function() {
             console.log('save callback');
             this.callback(this.markdown);
         }
     }
     
     
 }

</script>
