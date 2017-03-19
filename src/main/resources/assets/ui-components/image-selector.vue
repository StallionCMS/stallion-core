<template>
    <div class="image-selector">
        <div>
            <ul class="nav nav-tabs" role="tablist">        
                <li role="presentation" v-bind:class="{active: tab==='library'}" v-on:click="showTab('library')"><a class="" href="javascript:;" >Image Library</a></li>
                <li role="presentation" v-on:click="showTab('upload')" v-bind:class="{active: tab==='upload'}"><a href="javascript:;"  >Upload</a></li>
                <li v-if="allowExternalImages" show={!opts.hideurltab} role="presentation" v-on:click="showTab('url')" v-bind:class="{active: tab==='url'}"><a href="javascript:;" >Web Address (URL)</a></li>
            </ul>
        </div>
        <div v-if="tab==='library'">
            <image-library :callback="selectImage"></image-library>
        </div>
        <div v-if="tab==='upload'">
            <image-uploader :callback="selectImage"></image-uploader>
        </div>
        <div v-if="tab==='url'">
            <div class="form-group"></div>
            <div class="form-group">
                <label>Insert the URL of the image here:</label>
                <input type="text" class="form-control" name="src" v-model="externalSrc" >
            </div>
        </div>
    </div>
</template>
    
<script>
 module.exports = {
     props: {
         callback: Function,
         allowExternalImages: {
             type: Boolean,
             default: true
         }
     },
     data: function() {
         return {
             tab: 'library',
             externalSrc: '',
             image: {}
         }
     },
     methods: {
         showTab: function(tab) {
             this.tab = tab;
         },
         urlChange: function() {
             
         },
         saveChanges: function() {
             console.log('save changes', this.image);
             if (this.callback) {
                 this.callback(this.image);
             }
             return this.image;
         },
         selectImage: function(imageInfo) {
             console.log('select image', imageInfo);
             this.image = imageInfo;
             if (this.callback) {
                 console.log('call callback');
                 this.callback(imageInfo);
             }
         }
     },
     watch: {
         externalSrc: function(src) {
             this.image = {
                 url: src,
                 thumbUrl: src,
                 mediumUrl: src,
                 smallurl: src,
                 name: src
             };
         }
     }
 };
</script>
