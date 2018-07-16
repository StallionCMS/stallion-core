<!--
 * Stallion v1.0.0 (http://stallion.io)
 * Copyright 2016-2018 Stallion Software LLC
 * Licensed under GPL (https://github.com/StallionCMS/stallion-core/blob/master/LICENSE)
-->

<template>
    <div class="image-picker">
        <button class="btn btn-default" @click="modalShown=true">{{ image && image.thumbUrl ? labelSelected : label }}</button>
        <span v-if="image.thumbUrl">
            <a target="_blank" :href="image.url"><img style="max-width: 40px; max-height: 40px;" v-bind:src="image.thumbUrl"></a>
        </span>
        <span v-if="!image.thumbUrl">
            
        </span>
        <image-picker-modal v-if="modalShown" @close="modalShown=false" title="Pick an image" @chosen="onChosen"></image-picker-modal>
    </div>
</template>
    
<script>
 module.exports = {
     props: {
         value: {
             default: function() {
                 return {};
             }
         },
         label: {
             default: 'Choose Image'
         },
         labelSelected: {
             default: 'Change Image'
         }
     },
     data: function() {
         return {
             modalShown: false,
             image: this.value || {}
         }
     },
     methods: {
         onChosen: function(imageInfo) {
             console.log('handleSelected ', imageInfo);
             //this.value = imageInfo;
             this.$emit('input', imageInfo);
             this.$emit('change', imageInfo);
             this.image = imageInfo;
         }
     },
     watch: {
         'value': function(image) {
             this.image = image;
         }
     }
 };
</script>
