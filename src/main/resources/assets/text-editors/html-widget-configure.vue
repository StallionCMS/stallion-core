

<template>
    <div class="html-widget-configure">
        <div class="form-group">
            <label>Enter any custom HTML you wish</label>
            <autogrow-textarea class="form-control" name="html" style="min-height: 360px;" v-model="html"></autogrow-textarea>
        </div>
    </div>
</template>

<script>
 
 module.exports = {
     props: {
         widgetData: Object,
         insertCallback: Function,
     },
     data: function() {
         var html = '';
         if (this.widgetData && this.widgetData.data && this.widgetData.data.html) {
             html = this.widgetData.data.html;
         }
         return {
             html: html
         }
     },
     methods: {
         getWidgetData: function() {
             var self = this;
             var data = {html: this.html};
             var html = this.html;
             var widgetData = {
                 isBlock: true,
                 type: 'html',
                 label: 'HTML',
                 previewHtml: '',
                 data: data,
                 html: this.html
             };
             return widgetData;
         }
     }
 }

</script>
