<template>
    <div class="image-collection-field">
        <button class="btn btn-default"  v-on:click="showModal">Edit Image Collection</button>
        <span v-if="data.images">
            {{ data.images.length }} image(s) in collection.
        </span>
        <span v-if="!data.images">
            No images in collection.
        </span>
        <modal-base v-if="modalShown" :close="modalShown=false" title="Image Collection" :callback="finishConfigure">
            <div slot="body">
                <image-collection-configure ref="collection" :data="data"></image-collection-configure>
            </div>
        </modal-base>
    </div>
</template>
    
<script>
 module.exports = {
     props: {
         value: null,
     },
     data: function() {
         return {
             data: this.value || {},
             modalShown: false
         }
     },
     watch: {
         'value': function(newData) {
             this.data = newData;
         }
     },
     methods: {
         showModal: function() {
             this.modalShown = true;
         },
         finishConfigure: function(imageInfo) {
             this.$emit('input', this.$refs.collection.getData());
         }
     }
 };
</script>
