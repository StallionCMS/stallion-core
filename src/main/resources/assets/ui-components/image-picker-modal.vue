<!--
 * Stallion v1.0.0 (http://stallion.io)
 * Copyright 2016-2018 Stallion Software LLC
 * Licensed under GPL (https://github.com/StallionCMS/stallion-core/blob/master/LICENSE)
-->


<style lang="scss">
 .image-picker-modal-vue {
     form.my-dropzone {
         margin-top: 1em;
     }
     .st-data-table-vue {
         border: 1px solid #CCC;
         border-width: 0px 1px 1px 1px;
         .data-table-header {
             padding: 1em 0px 1em 0px;
             .table-title {
                 display: none;
             }
         }
         height: 70vh;
         overflow: auto;
     }
     
 }
</style>

<template>
    <div class="image-picker-modal-vue">
        <modal-base ref="themodal" v-on:close="$emit('close')" :large="true" title="Select an Image">
            <div slot="body">
                <div v-if="enableTabs">
                    <ul class="nav nav-tabs" role="tablist">        
                        <li v-if="showLibrary" role="presentation" v-bind:class="{active: tab==='chooser'}" @click="tab='chooser'"><a class="" href="javascript:;" >Image Library</a></li>
                        <li role="presentation" @click="tab='uploader'" v-bind:class="{active: tab==='uploader'}"><a href="javascript:;"  >Upload</a></li>
                        <li v-if="allowExternal" role="presentation" @click="tab='external'" v-bind:class="{active: tab==='external'}"><a href="javascript:;" >Web Address (URL)</a></li>
                    </ul>
                </div>
                <div class="p">
                    <image-library-chooser  v-if="tab==='chooser'" @chosen="onChosen"></image-library-chooser>
                    <image-upload-target v-if="tab==='uploader'" @uploaded="onChosen"></image-upload-target>
                </div>
            </div>
            <div slot="footer">
                <a @click="cancel" class="btn btn-default" href="javascript:;">Cancel</a>
            </div>
        </modal-base>
    </div>
</template>

<script>
 module.exports = {
     props: {
         initialTab: {
             default: 'uploader'
         },
         allowExternal: {
             default: false
         },
         enableTabs: {
             default: true
         },
         showLibrary: {
             default: true
         }
     },
     data: function() {
         return {
             tab: this.initialTab
         };
     },
     methods: {
         onChosen: function(file) {
             console.log('file-picker-modal onChosen ', file);
             this.$emit('chosen', file);
             this.$refs.themodal.close();
         },
         cancel: function() {
             this.$refs.themodal.close();
         }
     }
 };
</script>
