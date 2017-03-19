
<style>
 .vue-date-picker input, .vue-datetime-picker select {
     display: inline-block;
 }

 .vue-date-picker .date-input {
     width: 100px;
 }
 
</style>

<template>
    <div class="vue-date-picker">
        <input class="form-control date-input" type="text" v-model="date" name="datepicker">
    </div>
</template>


<script>
 module.exports = {
     props: {
         value: 0
     },
     mounted: function() {
         var self = this;
         console.log('init pikaday');
         self.pikaday = new Pikaday({
             field: $(self.$el).find('[name="datepicker"]').get(0),
             onSelect: function(date) {
                 console.log('new date picked', self.pikaday.toString());
                 self.date = self.pikaday.toString();
             }             
         });

     },
     data: function() {
         var m = moment(this.value);
         return {
             date: m.format('YYYY-MM-DD')
         };
     },
     methods: {
         changed: function(a, b) {
             console.log('date changed ', a, b);
             var m = moment(this.date, 'YYYY-MM-DD');
             console.log(m.format(), m, m.valueOf());
             this.$emit('input', m.valueOf());
         }
     },
     watch: {
         'date': 'changed'
     }
     
 };

</script>

