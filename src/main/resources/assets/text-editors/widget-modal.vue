

<template>
    <div class="widget-modal">
        <modal-base ref="themodal" v-on:close="$emit('close')" :large="true" :title="title" >
            <div slot="body">
                <div v-if="!widgetType">
                    <div class="widget-option">
                        <a href="javascript:;" @click="selectWidget('embed', 'Configure HTML Embed')">
                            <span class="icon icon-embed"></span> HTML Embed (Youtube Video, Slideshare, etc.)
                        </a>
                    </div>
                    <div class="widget-option">
                        <a href="javascript:;" @click="selectWidget('image', 'Choose Image to Insert', 'Insert Image')">
                            <span class="icon icon-image2"></span> Image
                        </a>
                    </div>
                    <div class="widget-option">
                        <a href="javascript:;" @click="selectWidget('image-simple', 'Choose Image to Insert', 'Insert Image')">
                            <span class="icon icon-image2"></span> Sourced Image 
                        </a>
                    </div>
                    <div class="widget-option">
                        <a href="javascript:;" @click="selectWidget('image-collection', 'Configure Image Collection')">
                            <span class="icon icon-images3"></span> Image Collection/Gallery
                        </a>
                    </div>
                    <div class="widget-option">
                        <a href="javascript:;" @click="selectWidget('html-form', 'Insert HTML Form')">
                            <span class="icon icon-insert-template"></span> HTML Form
                        </a>
                    </div>
                    <div class="widget-option">
                        <a href="javascript:;" @click="selectWidget('html', 'Insert Custom HTML Code')">
                            <span class="icon icon-file-xml"></span> HTML Code
                        </a>
                    </div>
                </div>
                <div v-if="widgetType">
                    <component :is="widgetConfigureTag" ref="active" :widget-data="activeWidgetData" :insert-callback="doInsertCallback" ></component>
                </div>
            </div>
            <div slot="footer">
                <button class="btn btn-primary btn-md" :disabled="!okToInsert" @click="insertWidget">{{ insertLabel }}</button>
                <a @click="cancel" href="javascript:;">Cancel</a>
            </div>
        </modal-base>
    </div>
</template>

<script>
 module.exports = {
     props: {
         callback: Function,
         insertLabel: {
             type: String,
             default: 'Insert'
         },
         widgetType: '',
         widgetData: null
     },
     data: function() {

         
         var activeWidgetData = {data:{type: this.widgetType}};
         if (this.widgetData && this.widgetData.type) {
             activeWidgetData = JSON.parse(JSON.stringify(this.widgetData));
             this.widgetType = activeWidgetData.type;
         }
         return {
             title: "Insert Widget",
             activeWidgetData: activeWidgetData,
             okToInsert: true,
             widgetConfigureTag: this.widgetTypeToTag(this.widgetType)
         }
     },
     created: function() {

     },
     methods: {
         widgetTypeToTag: function(widgetType) {
             return widgetType + '-widget-configure';
         },
         doInsertCallback: function() {

         },
         cancel: function() {
             this.$refs.themodal.close();
         },
         insertWidget: function() {
             var previousGuid = this.widgetData.guid || '';
             var widgetData = this.$refs['active'].getWidgetData();
             if (previousGuid) {
                 widgetData.guid = previousGuid;
             } else {
                 widgetData.guid = stPublisher.generateUUID();
             }
             this.callback(widgetData);
             this.$refs.themodal.close();
         },
         selectWidget: function(type, title, insertLabel) {
             if (this.widgetData && this.widgetData.type) {
                 this.activeWidgetData = this.widgetData;
             } else {
                 this.activeWidgetData = {
                     type: type + '',
                     data: {}
                 }
             }
             this.widgetType = type;
             this.insertLabel = insertLabel || this.insertLabel;
             this.title = title || this.title;
         }
     }
     
     
 }

</script>
