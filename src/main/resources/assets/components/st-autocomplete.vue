<!-- Copyright 2019 Stallion Software LLC Author: Patrick Fitzsimmons -->
<style lang="scss">
 .st-autocomplete-vue {
     position: relative;
     select.select-empty {
         color: #AAA;
     }
     .autocomplete-choices {
         position: absolute;
         background: white;
         border: 1px solid #CCC;
         z-index: 1000;
         height: 400px;
         max-height: calc(100vh - 50% - 100px);

         -webkit-box-shadow: 0px 4px 5px 2px rgba(163,163,163,0.71);
         -moz-box-shadow: 0px 4px 5px 2px rgba(163,163,163,0.71);
         box-shadow: 0px 4px 5px 2px rgba(163,163,163,0.71);
         
     }
     .autocomplete-choices.pop-upwards {
         -webkit-box-shadow: 0px -4px 7px 2px rgba(163,163,163,0.71);
         -moz-box-shadow: 0px -4px 7px 2px rgba(163,163,163,0.71);
         box-shadow: 0px -4px 7px 2px rgba(163,163,163,0.71);         
     }

     
     .autocomplete-item:hover {
         background: #EEE;
         cursor: pointer;
     }
     .autocomplete-items {
         overflow: scroll;
         max-height: calc(100% - 46px);
     }
     .autocomplete-item {
         padding: 4px 4px 4px 11px;
     }
     .autocomplete-input {
         padding: 8px 0px;
         text-align: center;
         input {
             width: 95%;
             border-radius: 4px;
             height: 30px;
             font-size: 18px;
         }
     }
     .autocomplete-highlight {
         background-color: #ddd;
     }
     ::-webkit-scrollbar {
         -webkit-appearance: none;
         width: 7px;
     }
     ::-webkit-scrollbar-thumb {
         border-radius: 4px;
         background-color: rgba(0,0,0,.5);
         -webkit-box-shadow: 0 0 1px rgba(255,255,255,.5);
     }     
 }
</style>

<template>
    <div class="st-autocomplete-vue control">
        <div :class="['select', expanded ? 'is-fullwidth' : '', value ? '' : 'is-empty', size]">
            <select :required="required" ref="theselect" @mousedown.prevent="noop" @click="openSelect" placeholder="placeholder" :class="['select', value ? '' : 'select-empty']" @keypress.enter.prevent="openSelect" :disabled="disabled" >
                <option v-if="placeholder" ref="placeholderoption" value=""  >{{ placeholder }}</option>
                <option ref="firstoption" value=""></option>
            </select>
        </div>
        <div v-show="autoCompleteShown" class="autocomplete-choices" ref="choicesdiv">
            <div class="autocomplete-input">
                <input @keypress.enter.prevent="onEnter" @keydown="onArrow" ref="theinput" autofocus="autofocus" @input="updateSearch">
            </div>
            <div class="autocomplete-items">
                <div v-if="!required && !search" class="autocomplete-item" @click="clearSelected"><a href="javascript:;" >Clear selected item</a></div>
                <div :class="['autocomplete-item', item.highlight ? 'autocomplete-highlight' : '']" v-for="item in dataFiltered" @click="chooseItem(item)">
                    {{ item.label }}
                </div>
            </div>
        </div>
    </div>
</template>

<script>
 module.exports = {
     props: {
         data: '',
         disabled: null,
         expanded: {
             type: Boolean
         },
         labelField: '',
         loading: false,
         numericValues: null,
         placeholder: '',
         required: {
             type: Boolean
         },
         size: '',
         valueField: '',
         value: null
     },
     data: function() {
         var that = this;
         // Try to guess whether the value should be a number object rather than a string
         var isNumeric = false;
         if (that.numericValues !== null) {
             isNumeric = that.numericValues;
         } else {
             if (that.value !== null) {
                 isNumeric = !isNaN(+that.value);
             } else if (that.data && that.data.length ) {
                 var d = that.data[0];
                 var val = d;
                 if (that.valueField) {
                     val = d[that.valueField];
                 }
                 isNumeric = !isNaN(+val);
             }
         }
         var sizes = {
             
         }
        
         var sizeClass 
         return {
             isNumeric: isNumeric,
             autoCompleteShown: false,
             search: '',
             dataFiltered: this.data,
             highlightedIndex: 0
         };
     },
     watch: {
         highlightedIndex: function(cur, old) {
             if (this.dataFiltered.length > old) {
                 this.dataFiltered[old].highlight = false;
             }             
             if (this.dataFiltered.length > cur) {
                 console.log('cur ', cur);
                 this.dataFiltered[cur].highlight = true;
                 this.$set(this.dataFiltered, cur, this.dataFiltered[cur]);
             }

         },
         value: function(cur, old) {
             this.onValueUpdated();
         },
         autoCompleteShown: function(shown) {
             if (shown === true) {
                 document.addEventListener("click", this.onOutsideClick);
             } else {
                 document.removeEventListener("click", this.onOutsideClick);
             }
         }
     },
     mounted: function() {
         this.onValueUpdated();
     },
     unmounted: function() {
         document.removeEventListener("click", this.onOutsideClick);
     },
     methods: {
         noop: function() {

         },
         onOutsideClick: function(evt) {
             if (evt.target.closest('.autocomplete-choices')) {
                 
             } else {
                 this.autoCompleteShown = false;
             }
         },
         onValueUpdated: function() {
             var that = this;
             if (!that.value) {
                 that.$refs.placeholderoption.selected = true;
                 var event = new Event('change');
                 that.$refs.theselect.dispatchEvent(event);             
                 return;
             }

             
             var label = that.value;
             this.data.forEach(function(d) {
                 var itemValue = d;
                 var itemLabel = d;
                 if (that.valueField) {
                     itemValue = d[that.valueField];
                 }
                 if (that.labelField) {
                     itemLabel = d[that.labelField];
                 }
                 if (itemValue === that.value) {
                     label = itemLabel;
                     return false;
                 }
             });
             var opt = that.$refs.firstoption;
             that.$refs.firstoption.value = that.value === null ? '': that.value;
             that.$refs.firstoption.innerHTML = label;
             that.$refs.firstoption.selected = true;
             var event = new Event('change');
             that.$refs.theselect.dispatchEvent(event);             
         },
         openSelect: function(evt) {
             var that = this;
             that.search = '';
             that.$refs.theinput.value = '';
             that.processItemsForSearch();
             console.log('open select');
             that.autoCompleteShown = !that.autoCompleteShown;

             var rect = that.$refs.theselect.getBoundingClientRect();
             var selectPos = rect.top;
             var h = Math.max(document.documentElement.clientHeight, window.innerHeight || 0);

             if (selectPos > (h/2)) {
                 that.$refs.choicesdiv.style.bottom = (rect.height) + 'px';
                 that.$refs.choicesdiv.style.top = '';
                 that.$refs.choicesdiv.classList.add('pop-upwards');
                 that.$refs.choicesdiv.classList.remove('pop-downwards');
                 that.$refs.choicesdiv.style.maxHeight = (rect.top - 50) + 'px'
             } else {
                 that.$refs.choicesdiv.style.bottom = '';
                 that.$refs.choicesdiv.style.top = rect.height + 'px';
                 that.$refs.choicesdiv.classList.remove('pop-upwards');
                 that.$refs.choicesdiv.classList.add('pop-downwards');
                 that.$refs.choicesdiv.style.maxHeight = (h - rect.top - 50) + 'px'    
             }
             if (that.$refs.choicesdiv.getBoundingClientRect().width < rect.width) {
                 that.$refs.choicesdiv.style.width = rect.width + 'px';
             }             
             
             Vue.nextTick(function() {
                 that.$refs.theinput.focus();
             });
             return false;
         },
         onArrow: function(evt) {
             var that = this;
             if (evt.key === 'ArrowUp') {
                 if (that.highlightedIndex > 0) {
                     that.highlightedIndex--;
                 }
             } else if (evt.key === 'ArrowDown') {
                 if (that.highlightedIndex < (that.dataFiltered.length - 1)) {
                     that.highlightedIndex++;
                 }
             }
             console.log('highlighted ', that.highlightedIndex);
         },
         onKeyPress: function(evt) {
             if (evt.key === 'Enter') {
                 evt.preventDefault();
                 that.onEnter();
                 return;
             } 
         },
         onEnter: function() {
             this.chooseItem(this.dataFiltered[this.highlightedIndex]);
         },
         clearSelected: function() {
             var that = this;
             that.autoCompleteShown = false;
             that.$emit('input', null, '');
         },
         chooseItem: function(item) {
             var that = this;
             that.autoCompleteShown = false;
             if (that.isNumeric && item.value === '')  {
                 that.$emit('input', null, item.label);
             } else if (that.isNumeric && !isNaN(+item.value)) {
                 that.$emit('input', Number(item.value), item.label);
             } else {
                 that.$emit('input', item.value, item.label);
             }
             
         },
         updateSearch: function(evt) {
             var that = this;
             that.search = evt.target.value.toLowerCase();
             that.processItemsForSearch();
         },
         processItemsForSearch: function() {
             var that = this;
             var newData = [];
             that.data.forEach(function(d) {
                 var label = d;
                 var value = d;
                 if (that.labelField) {
                     label = d[that.labelField];
                 }
                 if (that.labelField) {
                     value = d[that.valueField];
                 }
                 if (!that.search || (label && label.toLowerCase().indexOf(that.search) > -1)) {
                     var highlight = newData.length === 0 ? true : false;
                     newData.push({
                         value: value,
                         label: label,
                         highlight: highlight
                     });
                 }
                 
             });
             that.highlightedIndex = 0;
             that.dataFiltered = newData;

         }
     }
 };
</script>
