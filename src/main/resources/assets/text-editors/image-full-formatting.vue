<style>
 .image-wrap-icon {

 }
 .image-wrapping img {
     max-height: 40px;
     margin-bottom: 1em;
 }
 .image-wrapping label {
     text-align: center;
     display: inline-block;
     margin-right: 2px;
     vertical-align: top;
     width: 130px;
     height: 130px;
     padding: 6px;
     border: 1px solid #CCC;
     cursor: pointer;
 }
 .image-wrapping label:hover {
     background-color: #F0F0F0;
 }
 .image-wrapping label.active {
     background-color: #FBFBFB;
 }
 .image-wrapping label .label-text {
     display: inline-block;
     width: 100%;
     height: 40px;
 }
 
</style>

<template>
    <div class="image-full-formatting">
        <div class="row">
            <div class="col-sm-12">
                <div><em><big>Image alignment and wrap {{ formatting.alignment }} {{formatting.altText }}</big></em></div>
                <div class="form-group image-wrapping">
                    <label v-bind:class="{active: formatting.alignment==='center'}"><span class="label-text">Left</span>
                        <br><img src="/st-resource/publisher/images/images-align-left.png">
                        <br><input type="radio"  name="alignment" value="left" v-model="formatting.alignment">
                    </label>
                    <label v-bind:class="{active: formatting.alignment==='center'}"><span class="label-text">Center</span>
                        <br><img src="/st-resource/publisher/images/images-center.png">
                        <br><input type="radio" name="alignment" value="center" v-model="formatting.alignment">
                        
                    </label>    
                    <label v-bind:class="{active: formatting.alignment==='center'}"><span class="label-text">Right</span>
                        <br><img src="/st-resource/publisher/images/images-align-right.png">
                        <br><input type="radio" name="alignment" value="right" v-model="formatting.alignment">
                    </label>
                    
                    <label v-bind:class="{active: formatting.alignment==='center'}">
                        <span class="label-text">Image Left, Caption Right</span>
                        <br><img src="/st-resource/publisher/images/images-caption-right.png">
                        <br><input type="radio" name="alignment" value="caption-right" v-model="formatting.alignment">
                    </label>
                </div>
            </div>
            <div class="col-sm-6">
            </div>
        </div>
        <div class="row">
            <div class="col-sm-6">
                <div class="form-group">
                    <label>Link URL (optional) -- link to follow when image is clicked on</label>
                    <input type="text" class="form-control" name="linkUrl" v-model="formatting.linkUrl"></textarea>
                </div>
                <div class="form-group">
                    <label>Caption (optional, appears below image)</label>
                    <textarea class="form-control" style="height: 2.5em;" name="caption" v-model="formatting.caption"></textarea>
                </div>
            </div>
            <div class="col-sm-6">
                <div class="form-group">
                    <label>Alternative text/Hover text (optional, appears for screen readers and mouse hover)</label>
                    <input type="text" class="form-control" v-model="formatting.altText"></textarea>
                </div>
                <div class="form-group">
                    <label>Title/Header (optional, appears above image)</label>
                    <textarea class="form-control" style="height: 2.5em;" name="title" v-model="formatting.title"></textarea>
                </div>
                
            </div>
        </div>
    </div>
</template>

<script>
 module.exports = {
     props: {
         existingFormatting: Object,
     },
     data: function() {
         var formatting = {};
         if (this.existingFormatting) {
             formatting = JSON.parse(JSON.stringify(this.existingFormatting));
         }
         formatting.altText = formatting.altText || '';
         formatting.title = formatting.title || '';
         formatting.alignment = formatting.alignment || 'center';
         formatting.linkUrl = formatting.linkUrl || '';
         formatting.caption = formatting.caption || '';                  
         return {
             formatting: formatting
         };
     },
     methods: {
         getData: function() {
             return this.formatting;
         }
     },
     watch : {

     }
 }

</script>
