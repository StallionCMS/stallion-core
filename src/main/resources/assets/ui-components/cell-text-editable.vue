<style lang="scss">
 .cell-text-editable-vue {
     input {
         border: 1px solid #999;
         padding: 0px 2px 0px 2px;
         width: 100%;
     }
     .saving-span {
         position: absolute;
         z-index: 100;
         background-color: #888;
         color: #F9F9F9;
         border-radius: 4px;
         width: 100px;
     }
 }
</style>
<template>
    <form @submit.prevent="submit" class="form-inline cell-text-editable-vue">
        <span v-if="saving" class="saving-span">Saving...</span>
        <input type="text" @blur="cancel(item, col)"  v-model="value" autofocus="autofocus">
    </form>
</template>
<script>
 module.exports = {
     props: {
         item: Object,
         col: Object,
         cancel: Function
     },
     data: function() {
         return {
             value: this.item[this.col.field],
             saving: false
         };
     },
     mounted: function() {
         var self = this;
         setTimeout(function() {
             $(self.$el).find('input').focus();
         }, 20);
     },
     methods: {
         submit: function() {
             var self = this;
             var onSave = this.col.onSave;
             if (!onSave) {
                 onSave = function(func) { self.saving = false; func() }; 
             }
             this.saving = true;
             onSave(function() {
                 self.saving = false;
                 self.$emit('cell-value-updated', self.item, self.col.field, self.value, self.col);
             });             
         }
     }
 };
</script>
