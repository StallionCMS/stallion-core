

<template>
    <div class="html-form-widget-configure">
        <div class="form-group">
            <label>The HTML for your form</label>
            <autogrow-textarea class="form-control" name="html" style="min-height: 360px;" v-model="html"></autogrow-textarea>
        </div>
    </div>
</template>

<script>
     var FORM_HTML = "<form id=\"stallion-contact-form\" class=\"pure-form pure-form-stacked st-contacts-form\"><div class=\"st-form-success\" style=\"display:none;\">\n                    Thank you for reaching out, we will get back to you promptly!\n                </div>\n                <label for=\"name\">Your name</label>\n                <input class=\"pure-input-1\" name=\"name\" type=\"text\" placeholder=\"Your name\" required=\"required\">\n\n                <label for=\"email\">Email</label>\n                <input class=\"pure-input-1\" name=\"email\" type=\"email\" placeholder=\"Email\" required=\"required\">\n                <label>Your message</label>\n                <textarea class=\"pure-input-1\" name=\"message\" required=\"required\" style=\"height: 74px;\"></textarea>\n                <p>\n                    <button type=\"submit\" class=\"pure-button pure-button-primary\">Get in touch</button>\n                </p>\n            </fieldset>\n</form>";

 
 module.exports = {
     props: {
         insertCallback: Function
     },
     data: function() {
         return {
             html: FORM_HTML
         }
     },
     mounted: function() {

     },
     methods: {
         getWidgetData: function() {
             var self = this;
             var data = {};
             var html = this.html;
             var widgetData = {
                 isBlock: true,
                 type: 'html',
                 label: 'HTML',
                 previewHtml: '<form...>',
                 data: data,
                 html: this.html
             };
             return widgetData;
         }
     }
 }

</script>
