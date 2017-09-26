<template>
    <select :class="'select2-field-vue select2-target ' + className" :multiple="multiple" >
        <option v-for="opt in selectOptions" :selected="opt.selected" :value="opt.value">{{ opt.label }}</option>
    </select>
</template>

<script>
 module.exports = {
     props: {
         multiple: {
             default: false
         },
         choices: {
             type: Array,
             default: function() {
                 return [];
             }
         },
         config: {
             type: Object,
             default: function() {
                 return {};
             }
         },
         defaultValue: {
             default: function() {
                 return null;
             }
         },
         //selectOptions: Object,
         //selectValues: Array,
         className: '',
         value: {
             default: function() {
                 return '';
             }
         }
     },
     data: function() {
         var self = this;
         var selectOptions = [];
         var choices = JSON.parse(JSON.stringify(this.choices));
         (this.choices || []).forEach(function(opt) {
             var selected = false;
             var value = opt;
             var label = opt;
             if (typeof(opt) !== 'string') {
                 value = opt.value;
                 label = opt.label;
                 if (opt.selected !== undefined && opt.selected !== null) {
                     selected = opt.selected;
                 }
             }
             if (self.multiple && self.value && self.value.length != undefined) {
                 if ($.inArray(value, self.value) > -1) {
                     console.log('inArray(value, self.value) ', value, self.value);
                     selected = true;
                 }
             } else if (self.value == value) {
                 console.log('self.value == value ', self.value, value);
                 selected = true;
             }
             selectOptions.push({
                 value: value,
                 label: label,
                 selected: selected
             });
         });
         this.selectOptions = selectOptions;
         //this.selectValues = selectValues;
         //this.options = this.options || {};
         //this.selectOptions = this.selectOptions || {};
         return {};
     },
     mounted: function() {
         var self = this;
         console.log('config for select2 ', this.config);
         $(this.$el)
               .select2(this.config)
               .on('change', function(evt, msg) {
                   // Not sure why this doesn't automatically update
                   var defaultVal = this.defaultValue;
                   if (this.defaultValue === null && self.multiple) {
                       defaultVal = [];
                   }
                   console.log('select changed, new val is ', $(self.$el).val());
                   if (msg === 'dontTriggerInput') {
                       // Skip
                   } else {
                       self.$emit('input', $(self.$el).val() || defaultVal);
                   }
                   
               })
             ;
     },
     watch: {
         'value': function(newValue) {
             $(this.$el).val(newValue).trigger('change', 'dontTriggerInput');
         },
         'selectOptions': function(newValues, oldValues) {
             var self = this;
             var $select = $(this.$el);
             newValues.forEach(function(val) {
                 var value = val;
                 var label = val;
                 if (val.value) {
                     value = val.value;
                     label = val.label;
                 }
                 var $opt = $select.find('option[value="' + value + '"]');
                 if ($opt.length) {
                     return;
                 } else {
                     $select.append($('<option></option>').attr('value', value).html(label));
                 }
             });
             $select.trigger('change');
         }
     },
     methods: {
         addOption: function(option) {
             var hasOption = false;
             this.selectOptions.forEach(function(existing) {
                 if (existing === option || (existing.value === option) || (existing == option.value) || (existing.value == option.value)) {
                     hasOption = true;
                     return false;
                 }
             });
             if (!option.value) {
                 option = {value: option.value, label: option.label};
             }
             if (!hasOption) {
                 this.selectOptions.push(option);
             }
         },
         
         onEnter: function(evt) {
             evt.stopPropagation();
             evt.preventDefault();
             console.log('enter clicked');
             // do nothing, eat the enter key
             return false;
         }
     }
 };
</script>
