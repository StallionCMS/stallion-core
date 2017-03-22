<style lang="scss">
 .st-data-table-vue {
     .data-table-header {
         padding-right: 20px;
         .table-title {
             display: inline-block;
             margin: 0px 0px 0px 20px;
             font-weight: 200;
             vertical-align: -15%;
         }
         form {
             display: inline-block;
         }
         .search-input-group {
             width: 300px;
             margin-left: 20px;
             .form-control {
                 width: 250px;
                 height: 40px;
             }
         }
         .btn {
             height: 40px;
             padding-top: 5px;
             float: right;
             margin-right: 5px;
         }
         
         .btn.search-button {
             display: inline-block;
             margin-left: -1px;
             margin-right: 0px;
             float: left;
         }
         .cancel-search {
             position: relative;
             position: absolute;
             margin-left: -27px;
             z-index: 10;
             display: inline-block;
             height: 50px;
             padding-top: 7px;
             width: 30px;
             font-size: 20px;
             color: #aaa;
             cursor: pointer;
         }
         .cancel-search:hover {
             color: #555;
         }
         [slot="actions"], [slot="filters"] {
             display: inline-block;
             float: right;
             text-align: right;
             margin-left: 20px;
         }
     }
     .actions-slot, .filters-slot {
         display: inline-block;
         float: right;
         margin-left: 20px;
     }
     .cell-action-link {
         display: inline-block;
         margin-right: 5px;
     }
     .cell-editable {
         border-left: 1px solid transparent;
         border-right: 1px solid transparent;
     }
     .cell-editable:hover {
         cursor: pointer;
         border: 1px dashed #ccc;
     }
     thead {
         background-color: #f9f9f9;
         border-top: 1px solid #DDD;
     }
     tbody>tr:nth-of-type(even) {
         background-color: #f9f9f9;
     }         

     th.sortable:hover {
         background-color: #eee;
         cursor: pointer;
     }
     th.sorted {
         background-color: #ccc;
     }
     th.sorted.sorted-desc:after {
         content: " \25BE";
     }
     th.sorted.sorted-asc:after {
         content: " \25B4";
     }
              

 }
 .st-data-table-vue.fixed-headers {
     thead, .data-table-header {

     }
     .data-table-header {

     }
     table.st-data-table {
         margin: 0px;
     }
     .table-wrap {
         margin-top: 2em;
         background-color: #f9f9f9;
         border-top: 1px solid #ddd;
         border-bottom: 1px solid #ddd;
     }
     tbody {

     }
     tfoot {
         display: none;
     }

 }
 
</style>
<template>
    <div class="st-data-table-vue" v-bind:class="{'fixed-headers': this.infiniteScroll}">
        <div class="data-table-header">
            <slot name="header">
                <slot name="title">
                    <h2 class="table-title">{{ titleComputed }}</h2>
                </slot>
                <slot name="search">
                    <form @submit.prevent="doSearch" class="table-search form-inline">
                        <div class="input-group search-input-group">
                            <input type="text" class="form-control search-field" :placeholder="'Search for ' + label" v-model="searchTerm">
                            <span v-show="searchTerm && searchTerm.length >= 3" @click="navigate({page: 1, searchTerm: ''})" class="cancel-search">✕</span>
                            <button type="submit" class="btn btn-primary input-group-addon search-button"><i class="material-icons">search</i></button>
                        </div>
                    </form>
                </slot>
                <slot name="actions">
                    
                </slot>
                <slot name="filters">
                    
                </slot>                
            </slot>
        </div>
        <div class="table-wrap">
        <table :class="'st-data-table table ' + tableClass">
            <colgroup>
                <col v-for="col in columnsComputed" :style="col.style"></col>
            </colgroup>
            <thead>
                <tr>
                    <th v-bind:class="[sortFieldComputed===col.field ? 'sorted' : '', sortDirectionComputed==='desc' ? 'sorted-desc': '', col.sortable ? 'sortable' : '', sortDirectionComputed==='asc' ? 'sorted-asc': '', 'header-' + col.className, 'st-header-cell']" v-for="col in columnsComputed" @click="sortColumn(col)" v-if="!col.hidden">{{ col.title }}</th>
                </tr>
            </thead>
            <tbody v-if="loading">
                <tr>
                    <td  :colspan="columnsComputed.length">
                        Loading data &hellip;
                    </td>
                </tr>
            </tbody>
            <tbody v-if="!loading && !items.length">
                <tr>
                    <td :colspan="columnsComputed.length">
                        <div v-if="searchTerm">No {{ labelPluralComputed }} found matching search term "{{ searchTerm }}".</div>
                        <div v-else>No {{ labelPluralComputed }} found.</div>
                    </td>
                </tr>
            </tbody>
            <tbody v-if="!loading && items.length">
                <tr v-for="(item, rowNumber) in items" v-if="!item.$hidden" v-bind:key="item.id" :class="'data-table-row-index-' + rowNumber + ' data-table-row-id-' + item.id">
                    <td v-for="(col, colNumber) in columnsComputed" @click="onCellClicked(item, col, rowNumber, colNumber, $event)" @dblclick="onCellDoubleClicked(item, col, rowNumber, colNumber, $event)" v-bind:class="[col.editableComponent? 'cell-editable': '', col.className]" v-if="!col.hidden" :style="col.$widthWithPadding ? 'width: ' + (col.$widthWithPadding) + 'px;': ''">
                        <template v-if="item.$isEditing === colNumber">
                            <component :is="col.editableComponent" :item="item" :col="col" :row-number="rowNumber" :col-number="colNumber" :callback="onEditCallback" :refresh="refresh" :cancel="cancelEdit" v-on:cell-value-updated="onEditCallback"></component>
                        </template>
                        <template v-if="item.$isEditing !== colNumber">
                            <template v-if="col.component">
                                <component :is="col.component" :item="item" :col="col" :row-number="rowNumber" :col-number="colNumber"></component>
                            </template>
                            <template v-if="col.getLink">
                                <a v-bind:href="col.getLink(item)">{{ col.getLabel(item) }}</a>
                            </template>
                            <template v-if="col.actions">
                                <a v-for="action in col.actions" :class="'cell-action-link ' + action.className" v-show="action.shown ? action.shown(item) : true " v-bind:href="action.getLink ? action.getLink(item) : 'javascript:;' " @click="action.click ? action.click(item, col, rowNumber, colNumber) : triggerItemEvent(action.event, item, col, rowNumber, colNumber)">{{ action.getLabel ? action.getLabel(item) : action.label }}</a>
                            </template>
                            <template v-if="!col.component && !col.actions && !col.getLink">
                                <div v-if="col.allowHtml" v-html="renderCell(item, col, rowNumber, colNumber)">
                                </div>
                                <template v-else>
                                    {{ renderCell(item, col, rowNumber, colNumber) }}
                                </template>
                            </template>
                        </template>
                    </td>
                </tr>
                <tr v-if="!hasMore">
                    <td :colspan="columnsComputed.length">
                        All matching {{ labelPluralComputed }} have been loaded.
                    </td>
                </tr>
                <tr v-if="isScrolling">
                    <td :colspan="columnsComputed.length">
                        Fetching additional {{ labelPluralComputed }} &hellip;
                    </td>
                </tr>
            </tbody>
            <tfoot>
                <tr>
                    <td :colspan="columnsComputed.length">
                        <!-- <data-table-pager :make-link="makePageLink" v-if="pager!==null && !infiniteScroll" :page="page" :pager="pager" :base-path="'!/posts'" ></data-table-pager>-->
                    </td>
                </tr>
            </tfoot>
        </table>
        </div><!-- /table-wrap -->
    </div>
</template>
<script>
 module.exports = {
     props: {
         label: {
             type: String,
             required: true
         },
         tableClass: {
             type: String,
             default: 'table'
         },
         dataUrl: {
             required: false,
             type: String             
         },
         loadData: {
             type: Function
         },
         browserUrlTemplate: {
             required: true,
             type: String
         },
         columns: {
             type: Array,
             required: true
         },
         autoSearch: {
             default: false
         },
         singleClickEditing: {
             default: false
         },
         extraDataFields: {
             type: Array,
             default: function() {
                 return [];
             }
         },
         
         labelPlural: '',
         sortDirection: String,
         sortField: String,
         route: Object,
         title: String,
         infiniteScroll: true
     },
     data: function() {
         var data = {
             rawHtml: '<b>BOLD</b>',
             columnsComputed: [],
             labelPluralComputed: '',
             titleComputed: '',
             customFilter: '',
             filters: [],
             filterBys: [],
             page: 1,
             searchTerm: '',
             initialDataLoaded: false,
             isAttached: false,
             sortFieldComputed: '',
             sortDirectionComputed: '',
             items: [],
             loading: true,
             hasMore: false,
             isScrolling: false,
             scrollPage: 1,
             pager: {}             
         };

         if (this.extraDataFields) {
             this.extraDataFields.forEach(function(field) {
                 data[field] = '';
             });
         }

         data.filtersByField = {};
         if (this.filters) {
             this.filters.forEach(function(filter) {
                 data.filtersByField[filter.name] = filter.value;
             });
         }
         return data;
     },
     created: function() {
         this.initFromProps();
         if (!this.route) {
             this.loadInitialData();
         }
     },
     mounted: function() {
         var self = this;
         this.isAttached = true;
         if (this.initialDataLoaded) {
             this.afterAttachedAndLoaded();
         }
     },
     methods: {
         initFromProps: function() {
             var self = this;
             if (!this.loadData && !this.dataUrl) {
                 console.log("either load-data callback function is required or 'data-url' property must be passed in.");
                 return;
             }
             // Set defaults

             
             var columnsNew = [];
             var index = 0;
             this.columns.forEach(function(col) {
                 var col = $.extend({}, col);
                 col.$index = index;
                 index++;
                 if (col.component && col.component.template) {
                     if (!col.component.props) {
                         col.component.props = {item: Object};
                     }
                     col.component = Vue.extend(col.component);
                 }
                 if (col === '__row_selector') {
                     columnsNew.push({
                         isRowSelector: true
                     });
                 } else if (typeof(col) === 'string') {
                     columnsNew.push({
                         field: col,
                         title: self.toTitleCase(col)
                     });
                 } else {
                     if (!col.title && col.field) {
                         col.title = self.toTitleCase(col.field);
                     }
                     columnsNew.push(col);
                 }
             });
             this.columnsComputed = columnsNew;
             this.labelPluralComputed = this.labelPlural || this.label + 's';
             this.titleComputed = this.title || this.toTitleCase(this.labelPluralComputed);
             this.sortFieldComputed = this.sortField || '';
             this.sortDirectionComputed = this.sortDirection || '';
             this.searchTerm = this.searchTerm || '';
             this.filters = this.filters || [];
             this.page = this.page || 1;

             // If route exists, parse from route
             if (this.route) {
                 this.updateFromRoute();
             }


         },
         noop: function() {

         },
         doSearch: function() {
             //this.page = 1;
             this.navigate({page: 1});
         },
         afterAttachedAndLoaded: function() {
             var self = this;
             setTimeout(function() {
                 var $table = $(self.$el).find('table');
                 if (self.infiniteScroll) {
                     self.scrolify($table, $(window).height() - (40 + 28 + 38 + 20));
                 }
             }, 50);
         },
         onEditCallback: function(item, field, value, col, d) {
             item.$isEditing = false;
             if (field) {
                 item[field] = value;
             }
             Vue.set(this.items, item.$index, item);
             return true;
         },
         onCellDoubleClicked: function(item, col, rowNumber, colNumber, $event) {
             if (col.editableComponent && !this.singleClickEditing) {
                 item.$isEditing = colNumber;
                 Vue.set(this.items, rowNumber, item)
             }
         },
         onCellClicked: function(item, col, rowNumber, colNumber, $event) {
             if (col.editableComponent && this.singleClickEditing) {
                 item.$isEditing = colNumber;
                 Vue.set(this.items, rowNumber, item);
             }
         },
         cancelEdit: function(item, col) {
             item.$isEditing = false;
             Vue.set(this.items, item.$index, item)
         },
         sortColumn: function(col) {
             if (!col.sortable) {
                 return;
             }
             if (col.field === this.sortFieldComputed) {
                 if (this.sortDirectionComputed === 'asc') {
                     this.sortDirectionComputed = 'desc';
                 } else {
                     this.sortDirectionComputed = 'asc';
                 }
             } else {
                 this.sortFieldComputed = col.field;
                 this.sortDirectionComputed = 'asc';
             }
             this.page = 0;
             this.navigate({page: 1});
         },
         renderCell2: function(item, col, index) {
             debugger;
             return this.renderCell(item, col, index);
         },
         renderCell: function(item, col, index) {

             if (typeof(col) === 'string') {
                 return item[col];
             } else if (col.render) {
                 return col.render(item, col, index);
             } else {
                 var format = col.format || '';
                 if (!col.field) {
                     console.log('column does not have field attribute ', col.field);
                     return '!error!';
                 }
                 var value = item[col.field];
                 var index = col.field.indexOf('.');
                 if (value === undefined && index > -1) {
                     value = item[col.field.substr(0, index)][col.field.substr(index + 1)];
                 }
                 if (!format) {
                     return value;
                 }
                 if (format === 'number') {
                     if (value.toLocaleString) {
                         return value.toLocaleString();
                     } else {
                         return value;
                     }
                 } else if (format === 'dollars') {
                     try {
                         var fmt = new Intl.NumberFormat(
                             "en-US",
                             { style: "currency", currency: "USD",
                               minimumFractionDigits: 2 });
                         return fmt.format(value);
                     } catch(e) {
                         console.log(e);
                     }
                     return '$' + value;
                 } else if (format.indexOf('moment:') === 0 || format.indexOf('epochSeconds:') === 0) {
                     // See if value is in seconds since the epoch instead of milliseconds
                     if (!value) {
                         return '';
                     }
                     if (format.indexOf('epochSeconds:') === 0) {
                         value = value * 1000;
                     }
                     var timeZone = moment.tz.guess();
                     if (this.$store && this.$store.state.user && this.$store.state.uesr.timeZoneId) {
                         timeZone = this.$store.state.uesr.timeZoneId;
                     }
                     return moment.tz(value, timeZone).format(format.substr(format.indexOf(':') + 1));
                 } else if(format) {
                     return sprintf(format, value);
                 }
                 return value;
             }
         },
         clearFilter: function(field) {
             var filters = [];
             this.filters.forEach(function(filter) {
                 if (filter.name !== field) {
                     filters.push(filter);
                 }
             });
             this.filters = filters;
         },
         clearAllFilters: function() {
             this.filters = [];
             this.filterBys = '';
         },
         clearFiltersAndRefresh: function() {
             this.clearAllFilters();
             this.refresh();
         },
         addFilter: function(field, value, op) {
             op = op || '=';
             this.filters.push({name: field, value: value, op: op});
         },
         toTitleCase: function(str) {
             return str.replace(/\w\S*/g, function(txt){return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();});
         },
         triggerItemEvent: function(event, item, col, rowNumber, colNumber) {
             if (!event) {
                 return;
             }
             this.$emit(event, item, col, rowNumber, colNumber);
         },
         refresh: function() {
             this.loading = true;
             this.loadInitialData();
         },
         loadInitialData: function() {
             var self = this;
             this.loading = true;
             function callback(pager) {
                 var index = 0;
                 pager.items.forEach(function(item) {
                     item.$index = index;
                     item._index = index;
                     index++;
                 });
                 self.pager = pager;
                 self.items = pager.items;
                 
                 self.loading = false;
                 self.hasMore = pager.hasNextPage;
                 if (!self.initialDataLoaded) {
                     self.initialDataLoaded = true;
                     if (self.isAttached) {
                         self.afterAttachedAndLoaded();
                     }
                 }
             };
             self.doFetchData(callback);
         },
         appendData: function() {
             var self = this;
             if (this.isScrolling || !this.hasMore) {
                 return;
             }
             this.isScrolling = true;
             function callback(pager) {
                 var index = self.items.length;
                 pager.items.forEach(function(item) {
                     item.$index = index;
                     item._index = index;
                     index++;
                 });
                 self.items = self.items.concat(pager.items);
                 //self.items = [{}];
                 //console.log('found new items ', pager.items.length, pager.items[pager.items.length-1].title);
                 //self.pager = pager;
                 //self.$set('items', pager.ite);
                 self.hasMore = pager.hasNextPage;
                 self.fetchingMore = false;
                 self.isScrolling = false;
                 
             };
             this.scrollPage++;
             self.doFetchData(callback, this.scrollPage);
         },
         doFetchData: function(callback, page) {
             var self = this;
             
             page = page || this.page;
             if (this.loadData) {
                 this.loadData(this, page, callback);
             } else {
                 var url = this.dataUrl;
                 if (!url.indexOf('?') > -1) {
                     url += '?';
                 }
                 url += '&page=' + page;
                 if (this.searchTerm.length > 2) {
                     url += '&search=' + encodeURIComponent(this.searchTerm);
                 }
                 if (this.sortFieldComputed) {
                     var sort = this.sortFieldComputed;
                     if (this.sortDirection.toLowerCase() == 'desc') {
                         sort = '-' + sort;
                     }
                     url += '&sort=' + sort;
                 }
                 if (this.filters) {
                     url += '&filters=' + encodeURIComponent(JSON.stringify(this.filters));
                 }
                 if (this.customFilter) {
                     url += '&customFilter=' + encodeURIComponent(this.customFilter);
                 }
                 if (this.filterBys) {
                     this.filterBys.forEach(function(f) {
                         url += '&filter_by=' + f;
                     });
                 }
                 stallion.request({
                     url: url,
                     success: function(o) {
                         o.pager.items.forEach(function(item) {
                             item.$isEditing = false;
                             item.$hidden = false;
                         });
                         self.extraDataFields.forEach(function(field) {
                             if (o[field] !== undefined) {
                                 self[field] = o[field];
                             }
                         });
                         callback(o.pager);
                     }
                 });
             }
         },
         makePageLinkInfo: function(page) {
             return this.makeUrlInfo(page, this.searchTerm, this.filters, this.sortFieldComputed, this.sortDirection);
         },
         hardRefresh: function() {
             var info = this.makePageLinkInfo(this.page);
             info.query.t = Date().getTime();
             StallionApplicationRouter.go(info);
         },
         navigate: function(newOpts) {
             var self = this;
             newOpts = newOpts || {};
             Object.keys(newOpts).forEach(function(name) {
                 self[name] = newOpts[name];
             });
             var info = this.makePageLinkInfo(this.page);
             //if (url.indexOf('#') === 0) {
             //    url = url.substr(2);
             //}
             //var parts = url.split('?');
             //console.log('path ', parts[0], 'query', parts[1]);
             self.$router.push(info);
         }, 
         makeUrlInfo: function(page, searchTerm, filters, sortField, sortDirection) {
             var self = this;
             var sort = '';
             if (sortField) {
                 sort = sortField;
                 if (sortDirection.toLowerCase() === 'desc') {
                     sort = '-' + sort;
                 }
             }
             var url = this.browserUrlTemplate;
             if (url.indexOf('#/') === 0) {
                 url = url.substr(1);
             }
             var params = {
                 page: page,
                 search: searchTerm,
                 filters: encodeURIComponent(JSON.stringify(filters)),
                 sort: sort,
                 customFilter: self.customFilter
             };
             //Object.keys(params).forEach(function(key) {
             //    url = url.replace(new RegExp('{{\\s*' + key + '\\s*}}', 'g'), params[key] || '');
             //});
             return {
                 path: url,
                 query: params,
             }
         },
         
         scrolify: function(tblAsJQueryObject, height){
             var self = this;
             var oTbl = tblAsJQueryObject;

             // for very large tables you can remove the four lines below
             // and wrap the table with <div> in the mark-up and assign
             // height and overflow property  
             var oTblDiv = $("<div/>").addClass('scrollable-wrapper');
             oTblDiv.css('height', height);
             oTblDiv.css('overflow-y','scroll');               
             oTbl.wrap(oTblDiv);

             // save original width
             oTbl.attr("data-item-original-width", oTbl.width());
             
             oTbl.find('thead tr th').each(function(){
                 $(this).attr("data-item-original-width", $(this).width());

             });
             var i = 0;
             oTbl.find('tbody tr:eq(0) td').each(function(){
                 $(this).attr("data-item-original-width",$(this).width());
                 $(this).attr("data-original-width-with-padding",$(this).outerWidth());
                 self.columnsComputed[i].$widthWithPadding = $(this).outerWidth();
                 i++;                 
             });                 


             // clone the original table
             var newTbl = oTbl.clone();

             // remove table header from original table
             oTbl.find('thead tr').remove();                 
             // remove table body from new table
             newTbl.find('tbody tr').remove();   

             oTbl.parent().parent().prepend(newTbl);
             newTbl.wrap("<div/>");

             // replace ORIGINAL COLUMN width                
             newTbl.width(newTbl.attr('data-item-original-width'));
             newTbl.find('thead tr th').each(function(){
                 $(this).width($(this).attr("data-item-original-width"));
             });     
             oTbl.width(oTbl.attr('data-item-original-width'));
             i = 0;
             oTbl.find('tbody tr:eq(0) td').each(function(){
                 $(this).width($(this).attr("data-item-original-width"));
                // $(this).width(self.columns[i].$width);
                 //$(this).css({'width': self.columns[i].$width + 'px !important'});
                 i++;
             });        
             $(this.$el).find(".scrollable-wrapper").on('scroll', function() {
                 if(($(this).scrollTop() + $(this).innerHeight() + 20) >= $(this)[0].scrollHeight) {
                     self.appendData();
                 }
             });
         },
         updateFromRoute: function() {
             this.page = parseInt(this.route.params.page || this.route.query.page || 1, 10) || 1;
             this.searchTerm = this.route.params.search || this.route.query.search || '',
             this.sortFieldComputed = this.route.params.sort || this.route.query.sort || '';
             if (this.sortFieldComputed.indexOf('-') === 0) {
                 this.sortDirectionComputed = 'desc';
                 this.sortFieldComputed = this.sortDirectionComputed.substr(1);
             } else {
                 this.sortDirectionComputed = 'asc';
             }
             this.customFilter = this.route.query.customFilter || '';
             this.filters = [];
             if (this.route.query.filters) { 
                 try {
                     this.filters = JSON.parse(decodeURIComponent(this.route.query.filters));
                 } catch(e) {
                     stallion.showError('Invalid "filters" in query');
                 }
             }
             if (this.route.query.filter_by) {
                 this.filterBys = [this.route.query.filter_by];
             }
             this.refresh();
             window.scrollTo(0, 0);
         }
     },
     watch: {
         columns: function() {
             this.initFromProps();
         },
         browserUrlTemplate: function() {
             this.initFromProps();
         },
         dataUrl: function() {
             this.initFromProps();
         },
         route: function(cur, prev) {
             this.updateFromRoute();
         },
         label: function() {
             this.labelPluralComputed = this.labelPlural || this.label + 's';
             this.titleComputed = this.title || this.toTitleCase(this.labelPluralComputed);
             
         },
         title: function(newTitle) {
             this.titleComputed = newTitle;
         },
         searchTerm: function(cur, prev) {
             if (this.loading) {
                 return;
             }
             if (cur !== prev) {
                 if (cur === '' || cur.length >= 3) {
                     this.page = 1;
                     this.loadInitialData();
                 }
             }
         } 
     },
     events: {
         'cell-value-updated': function(item, field, value, col) {
             item.$isEditing = false;
             if (field) {
                 item[field] = value;
             }
             Vue.set(this.items, item.$index, item);             
             return true;
             
         },
         'clear-filters': function() {
             this.clearFiltersAndRefresh();
         }
         
     }

 };
</script>
