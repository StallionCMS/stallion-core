<!-- Copyright 2019 Stallion Software LLC Author: Patrick Fitzsimmons -->
<style lang="scss">
 .st-taginput-vue {
     position: relative;
     .taginput-container {
         min-height: 2.25em;
     }
     .tag {
         margin-right: 3px;
     }
     .placeholder {
         color: #AAA;
         padding-top: 2px;
     }
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
         -webkit-box-shadow: 0px 0px 7px -2px rgba(0,0,0,0.75);
         -moz-box-shadow: 0px 0px 7px -2px rgba(0,0,0,0.75);
         box-shadow: 0px 0px 7px -2px rgba(0,0,0,0.75);
     }
     .autocomplete-item:hover {
         background: #EEE;
         cursor: pointer;
     }
     .autocomplete-items {
         overflow: scroll;
         max-height: 100%;
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
     .min-max {
         font-size: 12px;
         color: #777;
         align-self: flex-end;
         margin-top: -4px;
         display: inline-block;
     }
 }
</style>

<template>
    <div class="st-taginput-vue control taginput">
        <div :class="['taginput-container', 'is-focusable', expanded ? 'is-fullwidth' : '', value ? '' : 'is-empty', size]" @click="clickOpenSelect" ref="taginputcontainer">
            <input ref="hiddeninput" type="text" style="left: 0px; top:0px;height:100%; width: 100%; border-width: 0px; position:absolute; color: transparent; background-color: transparent;" @keypress.prevent="keyPressOpenSelect" required="required" :minlength="minCount" :maxlength="maxCount" @invalid="onHiddenInputInvalid">
            <div style=" z-index: 10px; background-color: white; display:flex; justify-content: space-between; width: calc(100% - 20px); margin-bottom: 3px; ">
                <div style="flex: grow;">
                    <div v-if="!value || !value.length && placeholder" class="placeholder">
                        {{ placeholder }}
                    </div>
                    <div class="tags-wrapper">
                        <b-tag v-for="tag in tagInfos" closable @close="removeItem(tag)" :key="tag.id">{{ tag.label }}</b-tag>
                    </div>
                </div>
                <div class="min-max"  v-if="min || maxCount">
                    <span v-if="min">Min: {{ min }}</span>
                    <span v-if="maxCount">Max: {{ maxCount }}</span>
                </div>
            </div>
        </div>
        <div v-show="autoCompleteShown" class="autocomplete-choices" ref="choicesdiv">
            <div class="autocomplete-input">
                <input @keypress.enter.prevent="onEnter" @keydown="onArrow" ref="theinput" autofocus="autofocus" @input="updateSearch">
            </div>
            <div class="autocomplete-items">
                <div v-if="allowNew" :class="['autocomplete-item', highlightedIndex === -1 ? 'autocomplete-highlight' : '']"><button class="button" type="button" @click="addNewFromInput" :disabled="!search">Add as new item</button></div>
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
         allowNew: false,
         data: '',
         expanded: {
             type: Boolean
         },
         labelField: '',
         loading: false,
         keepOpen: false,
         numericValues: null,
         placeholder: '',
         required: {
             type: Boolean
         },
         minCount: 0,
         maxCount: null,
         size: '',
         valueField: '',
         value: {
             default: function() {
                 return [];
             }
         }
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
         var labelById = {};
         this.data.forEach(function(d) {
             var id = d;
             var label = d;
             if (that.valueField) {
                 id = d[that.valueField];
             }
             if (that.labelField) {
                 label = d[that.labelField];
             }
             labelById[id] = label;
         });
        
         var sizeClass 
         return {
             labelById: labelById,
             isNumeric: isNumeric,
             autoCompleteShown: false,
             search: '',
             dataFiltered: this.data,
             highlightedIndex: 0,
             tagInfos: []
         };
     },
     watch: {
         highlightedIndex: function(cur, old) {
             if (old > -1 && this.dataFiltered.length > old) {
                 this.dataFiltered[old].highlight = false;
             }             
             if (cur > -1 && this.dataFiltered.length > cur) {
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
     computed: {
         min: function() {
             if (this.minCount === 0 && this.required) {
                 return 1;
             } else {
                 return this.minCount;
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
         onHiddenInputInvalid: function(evt) {
             if (this.value.length < this.minCount) {
                 var itemStr = 'items';
                 if (this.minCount === 1) {
                     itemStr = 'item';
                 }
                 evt.target.setCustomValidity("Must select at least " + this.minCount + " " + itemStr);
             } else if (this.value.length > this.maxCount) {
                 var itemStr = 'items';
                 if (this.minCount === 1) {
                     itemStr = 'item';
                 }
                 evt.target.setCustomValidity("Cannot select more than " + this.maxCount + " " + itemStr);

             } else if (this.required && !this.value.length) {

             }
         },
         onOutsideClick: function(evt) {
             if (evt.target.closest('.autocomplete-choices')) {
                 
             } else {
                 this.autoCompleteShown = false;
             }
         },
         onValueUpdated: function() {
             var that = this;
             var tagInfos = [];
             that.value.forEach(function(val) {
                 label = that.labelById[val] || val;
                 tagInfos.push({value: val, label: label});
             });
             that.tagInfos = tagInfos;
             if (that.tagInfos.length >= this.minCount && that.tagInfos.length <= this.maxCount) {
                 that.$refs.hiddeninput.value = '1';
             } else {
                 that.$refs.hiddeninput.value = '';
             }
             that.$refs.hiddeninput.setAttribute('data-count', that.value.length);
             var event = new Event('change');
             that.$refs.hiddeninput.dispatchEvent(event);             
             
         },
         keyPressOpenSelect: function(evt) {

             this.openSelect(evt.key);
         },
         clickOpenSelect: function(evt) {
             if (evt.target.closest('.delete')) {
                 return;
             }
             this.openSelect('')
         },
         openSelect: function(initial) {
             var that = this;
             if (this.value && this.value.length >= this.maxCount) {
                 return;
             }
             that.search = initial;
             that.$refs.theinput.value = initial;
             that.processItemsForSearch();
             console.log('open select');
             that.autoCompleteShown = !that.autoCompleteShown;

             var rect = that.$refs.taginputcontainer.getBoundingClientRect();
             var selectPos = rect.top;
             var h = Math.max(document.documentElement.clientHeight, window.innerHeight || 0);
             if (selectPos > (h/2)) {
                 that.$refs.choicesdiv.style.bottom = (rect.height) + 'px'; 
                 that.$refs.choicesdiv.style.top = '';
             } else {
                 that.$refs.choicesdiv.style.bottom = '';
                 that.$refs.choicesdiv.style.top = rect.height + 'px';
             }
             if (that.$refs.choicesdiv.getBoundingClientRect().width < rect.width) {
                 that.$refs.choicesdiv.style.width = rect.width + 'px';
             }

                          
             
             /*
             var rect = that.$refs.theselect.getBoundingClientRect();
             var selectPos = rect.top;
             var h = Math.max(document.documentElement.clientHeight, window.innerHeight || 0);
             if (selectPos > (h/2)) {
                 that.$refs.choicesdiv.style.bottom = (rect.height) + 'px'; 
                 that.$refs.choicesdiv.style.top = '';
             } else {
                 that.$refs.choicesdiv.style.bottom = '';
                 that.$refs.choicesdiv.style.top = rect.height + 'px';
             }
             */
             
             Vue.nextTick(function() {
                 that.$refs.theinput.focus();
             });
             return false;
         },
         onArrow: function(evt) {
             var that = this;
             if (evt.key === 'ArrowUp') {
                 if (that.highlightedIndex > 0 || (that.allowNew && that.highlightedIndex > -1)) {
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
             var that = this;
             if (this.highlightedIndex === -1) {
                 this.addNewFromInput();
             } else {
                 this.chooseItem(this.dataFiltered[this.highlightedIndex]);
             }
         },
         addNewFromInput: function() {
             var that = this;
             if (!that.search) {
                 return;
             }
             var item = that.search;
             if (that.labelField && that.valueField) {
                 item = {}
                 item[that.labelField] = that.search;
                 item[that.valueField] = that.search;
             }
             this.data.push(item);
             that.chooseValue(that.search);
             that.search = '';
         },
         removeItem: function(tag, event) {
             var that = this;
             var newValue = [];
             this.value.forEach(function(val) {
                 if (val == tag.value) {
                     return;
                 }
                 newValue.push(val);
             });
             that.$emit('input', newValue);
         },
         chooseItem: function(item) {
             var that = this;
             var itemValue = null;
             if (that.isNumeric && item.value === '')  {
                 return;
             } else if (that.isNumeric && !isNaN(+item.value)) {
                 itemValue = Number(item.value);
             } else {
                 itemValue = item.value;
             }
             that.chooseValue(itemValue);
             
         },
         chooseValue: function(itemValue) {
             var that = this;
             var newValue = null;
             if (this.value === null || this.value === undefined) {
                 newValue = [];
             } else {
                 newValue = this.value.slice();
             }
             newValue.push(itemValue);

             if (!that.keepOpen || newValue.length >= that.maxCount) {
                 that.autoCompleteShown = false;
             } else {
                 that.$refs.theinput.value = '';
                 that.search = '';
                 Vue.nextTick(function() {
                     that.processItemsForSearch();
                 });
             }

             that.$emit('input', newValue);
             

         },
         updateSearch: function(evt) {
             var that = this;
             that.search = evt.target.value.toLowerCase();
             that.processItemsForSearch();
         },
         processItemsForSearch: function() {
             var that = this;
             var newData = [];
             var searchLower = that.search ? that.search.toLowerCase() : '';
             that.data.forEach(function(d) {
                 var label = d;
                 var value = d;
                 if (that.labelField) {
                     label = d[that.labelField];
                 }
                 if (that.labelField) {
                     value = d[that.valueField];
                 }
                 if (that.value && that.value.indexOf(value) > -1) {
                     return;
                 }
                 if (!that.search || (label && label.toLowerCase().indexOf(searchLower) > -1)) {
                     var highlight = newData.length === 0 ? true : false;
                     newData.push({
                         value: value,
                         label: label,
                         highlight: highlight
                     });
                 }
                 
             });
             that.highlightedIndex = 0;
             if (newData.length === 0 && that.allowNew) {
                 that.highlightedIndex = -1;
             }
             that.dataFiltered = newData;

         }
     }
 };
</script>
