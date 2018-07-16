<!--
 * Stallion v1.0.0 (http://stallion.io)
 * Copyright 2016-2018 Stallion Software LLC
 * Licensed under GPL (https://github.com/StallionCMS/stallion-core/blob/master/LICENSE)
-->

<style lang="scss">
 .insert-link-modal-vue {
     .tab-box {
         border-top: 1px solid #E9E9E9;
         border-left: 1px solid #E9E9E9;
         padding-top: 1em;
         margin-left: -16px;
         padding-left: 28px;
     }
     .tab-left {
         z-index: 3;
     }
     .tabs-row {

     }
 }
</style>

<template>
    <div class="insert-link-modal-vue">
        <modal-base ref="themodal" v-on:close="$emit('close')" title="Insert Link" :callback="modalClosed">
            <div slot="body">
                <div class="row">
                    <div class="col-sm-12">
                        <div class="form-group">
                            <label>Link text</label>
                            <input type="text" v-model="thisText" class="form-control">
                        </div>
                    </div>
                </div>
                <div class="row" style="tabs-row">
                    <div class="col-sm-4 tab-left">
                        <a v-if="!hideInternalPages" href="javascript:;" v-on:click="tab='internal-link'" v-bind:class="{'vertical-tab': true, 'active-tab': tab==='internal-link'}">
                            Internal Page
                        </a>
                        <a href="javascript:;" v-on:click="tab='external-link'" v-bind:class="{'vertical-tab': true, 'active-tab': tab==='external-link'}">
                            External Link
                        </a>
                        <a href="javascript:;" v-on:click="tab='document'" v-bind:class="{'vertical-tab': true, 'active-tab': tab==='document'}">
                            Document or File
                        </a>
                    </div>
                    <div class="col-sm-8 tab-box">
                        <div v-if='tab==="external-link"'>
                            <label>Enter the URL of the page here</label>
                            <input type="text" autofocus="autofocus" name="link" class="form-control" v-model="thisLink" placeholder="https://..." @keyup.enter="insert">            
                        </div>
                        <div v-if='tab==="internal-link"'>
                            <internal-link-picker :callback="linkPicked"></internal-link-picker>
                        </div>
                        <div v-if='tab==="document"'>
                            <file-library :ispicker="true" :callback="filePicked" ></file-library>
                        </div>
                    </div>
                </div><!-- /.row -->
            </div><!-- end .body -->
        </modal-base>
    </div>
</template>

<script>
 module.exports = {
     props: {
         hideInternalPages: false,
         text: '',
         link: '',
     },
     data: function() {
         var tab = 'internal-link';
         if (this.link  || this.hideInternalPages) {
             tab = 'external-link';
         }
         return {
             tab: tab,
             thisText: this.text,
             thisLink: this.link
         }
     },
     created: function() {
         
     },
     watch: {
         text: function(newText) {
             this.thisText = newText;
         },
         link: function(newLink) {
             this.thisLink = newLink;
         }
     },
     methods: {
         insert: function() {
             var self = this;
             this.$emit('input', this.thisLink, this.thisText);
             this.$refs.themodal.close();             
         },
         modalClosed: function() {
             var self = this;             
             this.$emit('input', this.thisLink, this.thisText);
         },
         linkPicked: function(o) {
             var self = this;
             this.thisLink = o.url;
             this.$emit('input', this.thisLink, (self.thisText || o.text));
             this.$refs.themodal.close();             
         },
         filePicked: function(file) {
             var self = this;             
             if (file.name && !this.thisText) {
                 this.thisText = file.name;
             }
             this.thisLink = file.url;
             this.$emit('input', this.thisLink, this.thisText);
             this.$refs.themodal.close();
         }
     }
     
     
 }
</script>
