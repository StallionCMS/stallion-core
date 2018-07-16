<!--
 * Stallion v1.0.0 (http://stallion.io)
 * Copyright 2016-2018 Stallion Software LLC
 * Licensed under GPL (https://github.com/StallionCMS/stallion-core/blob/master/LICENSE)
-->

<template>
    <select class="form-control" name="authorSelect">
        <option>Choose an author</option>
    </select>
</template>


<script>
 module.exports = {
     props: {
         value: {
             default: function() {
                 return '';
             }
         }
     },
     data: function() {
         return {

         }
     },
     mounted: function() {
         var self = this;
         
         stallion.request({
             url: '/st-publisher/content/active-authors',
             success: function(o) {
                 var data = [];
                 data.push({
                     id: 10,
                     text: 'John doe',
                     selected: false
                 });                 
                 o.authors.forEach(function(au) {
                     var isSelected = parseInt(this.value, 10) === au.author.id;
                     data.push({
                         id: au.author.id,
                         text: au.user.displayName,
                         selected: isSelected
                     });
                     
                 });
                 $(self.$el).select2({
                     width: '100%',
                     data: data
                 }).change(function() {
                     console.log('select2 change!');
                     self.$emit('input', Number($(this).val()) || null);
                 });
             }
         });
     },
     watch: {
         'value': function(newValue, b) {
             $(self.$el).val(newValue).change();
         }
     }
 };

</script>
