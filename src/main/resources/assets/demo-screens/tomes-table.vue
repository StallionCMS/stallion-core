<style lang="scss">
 .tomes-table-vue {
     .authors-filter {
         height: 50px;
     }
     .price-filter.empty-value {
         color: #999;
     }
     td, th {
         white-space: nowrap;
         text-overflow: ellipsis;
         overflow: hidden;
     }
     td.title-cell {
         max-width: 300px;
         min-width: 150px;
     }
     td.status-cell {
         width: 70px;
         text-overflow: ellipsis;
         overflow: hidden;
     }
 }
</style>



<template>
    <div class="tomes-table-vue">
        <h3 v-if="isLoading">Loading &hellip;</h3>
        <div v-if="!isLoading">
            <st-data-table ref="tometable" :infinite-scroll="true" :columns="columns" label="tome" :browser-url-template="'/tomes'" :data-url="'/st-ui-demo/testing-tomes/tomes'" :route="$route" table-class="table table-striped" :single-click-editing="true" :extra-data-fields="['checkedoutBooks', 'maxCheckouts']"  >
                <div class="filters-slot form-inline" slot="filters">
                    <select v-model="filterPrice" class="form-control price-filter" v-bind:class="{'empty-value': !filterPrice}" placeholder="Filter by price">
                        <option value=""> - Filter by price - </option>
                        <option value="<12">less than $12</option>
                        <option value=">12<20">$12 to $20</option>
                        <option value=">20">greater than A$20</option>
                    </select>
                    <select2-field :class-name="'authors-filter form-control'" v-model="filterAuthors" :choices="authors" :config="{'placeholder': 'Filter by author', 'width': 320}" :multiple="true">
                    </select2-field>
                </div>
                <div class="actions-slot" slot="actions">
                    <span>Checked out: {{ $refs.tometable ? $refs.tometable.checkedoutBooks : 'NA' }} / {{ $refs.tometable ? $refs.tometable.maxCheckouts : 'NA' }}</span>
                    <button class="btn btn-primary">Add Tome</button>
                </div>
            </st-data-table>
        </div>
    </div> 
</template>

<script>
 var ToggleRow = Vue.extend({
     props: {
         item: Object
     },
     template: '<input @click="toggleRow" type="checkbox" checked data-toggle="toggle">',
     methods: {
         toggleRow: function() {
             alert('toggle ' + this.item.title);
         }
     }
});
 
 module.exports = {
     data: function() {
         console.log('normal get data');
         return {
             isLoading: false,
             filterAuthors: [],
             filterPrice: '',
             route: {},
             authors: [
                 "Mark Twain", "Charles Dickens", "Ernest Hemingway", "Louisa May Alcott", "Jane Austen", "Jonathan Swift", "Daniel Defoe",
                 "Clive Cussler", "Francis Scott Fitzgerald", "JD Salinger", "Aesop", "Homer"
             ],
             columns: [
                 {
                     actions: [{
                         className: 'btn btn-default btn-xs',
                         label: 'Edit',
                         event: 'edit-tome'
                     }]
                 },
                 {
                     field: 'author'
                 },
                 {
                     title: 'Status',
                     field: 'status',
                     className: 'status-cell',
                     editableComponent: 'cell-select-editable',
                     render: function(item) {
                         return item.status.replace(/_/g, ' ').toLowerCase();
                     },
                     selectableValues: [
                         {
                             label: 'Out of Print',
                             value: 'OUT_OF_PRINT'
                         },
                         {
                             label: 'Public Domain',
                             value: 'PUBLIC_DOMAIN'
                         }                         
                     ]
                         
                 },
                 {
                     title: 'Snowman',
                     className: 'the-circle',
                     allowHtml: true,
                     render: function(item) {
                         return '<a class="btn btn-default" href="javascript:;">\u2603</a>'
                     }
                 },
                 {
                     title: 'Title',
                     field: 'title',
                     className: 'title-cell',
                     editableComponent: 'cell-text-editable',                     
                     style: 'max-width: 300px; '
                 },
                 {
                     title: 'Published',
                     format: 'epochSeconds:MMM Do YYYY',
                     field: 'publishedAt'
                 },
                 {
                     title: 'Unit Price',
                     field: 'price',
                     editableComponent: 'cell-text-editable',
                     format: 'dollars',
                     sortable: true
                 },
                 {
                     title: 'Bulk Price',
                     field: 'bulkPrice',
                     format: 'dollars'                     
                 },
                 {
                     title: 'Number Sold',
                     field: 'numberSold',
                     format: 'number'
                 },
                 
                 {
                     title: 'Updated At',
                     render: function(item) {
                         return moment(item.updatedAt * 1000).fromNow();
                     }
                 },
                 {
                     component: ToggleRow
                 }
             ]
         };
     },
     created: function() {
         this.onRoute();
         var self = this;
         $(this.$el).find('.authors-filter').select2({
             width: 300,
             placeholder: 'Filter by author'
         }).on('change', function() {
             console.log('filter authors select changed val is: ', $(this).val());
             var val = $(this).val();
         });
     },
     methods: {
         onRoute: function() {

         }
     },
     watch: {
         '$route': function() {
             this.onRoute();
         },
         filterAuthors: function(val, old) {
             var self = this;
             console.log('authors changed ', val);
             if (!val || !val.length) {
                 self.$refs.tometable.clearFilter('author');
             } else {
                 self.$refs.tometable.clearFilter('author');
                 self.$refs.tometable.addFilter('author', val, 'any');
             }
             self.$refs.tometable.navigate({page: 1});             
         },
         filterPrice: function(cur, old) {
             var self = this;
             this.$refs.tometable.clearFilter('price');
             switch (cur) {
                 case '<12':
                     self.$refs.tometable.addFilter('price', 12.0, '<');
                     break;
                 case '>12<20':
                     self.$refs.tometable.addFilter('price', 12.0, '>=');
                     self.$refs.tometable.addFilter('price', 20.0, '<');
                     break;
                 case '>20':
                     self.$refs.tometable.addFilter('price', 20.0, '>=');
                     break;
                 default:
                     break;
             }
             self.$refs.tometable.navigate({page: 1});
         }
     },
     events: {
         'edit-tome': function(item) {
             alert('Edit ' + item.title);
         },
         'cell-value-updated': function(item, field, value) {
             console.log('save new item value ', item, field, value);
         }
     }
 }
</script>
