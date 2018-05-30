
<style lang="scss">
 .modal-base-vue.modal .modal-footer {
     button {
         float: left;
     }
     a {
         float: right;
         vertical-align: -60%;
     }
 }
</style>

<template>
    <div :class="'modal-base-vue modal ' + cssClass + ' ' + classProp" role="dialog" aria-labelledby="myLargeModalLabel">
        <div :class="'modal-dialog ' + modalClass" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <slot name="header">
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                            <span aria-hidden="true">&times;</span>
                        </button>
                        <h4 v-if="title!==null" class="modal-title">{{ title }}</h4>
                    </slot>
                </div><!-- end .modal-header -->
                <div class="modal-body">
                    <slot name="body">
                        <component ref="bodyComponent" :is="tag" :callback="modalCallback">
                        </component>
                    </slot>
                </div>
                <div class="modal-footer">
                    <slot name="footer">
                        <button class="btn btn-primary btn-large st-button-submit" v-on:click="saveChanges">{{ confirmLabel }}</button>
                        <a href="javascript:;" class="modal-cancel-link" v-on:click="close" style="float:right;">{{ cancelLabel }}</a>
                    </slot>
                </div>
            </div><!-- end .modal-content -->
        </div><!-- end .modal-dialog -->
    </div><!-- end .modal -->
</template>


<script>
 module.exports = {
     props: {
         shown: {
             default: true
         },
         title: null,
         tag: '',
         large: false,
         small: false,
         cancelLabel: {
             type: String,
             default: 'Cancel'
         },
         confirmLabel: {
             type: String,
             default: 'Save Changes'
         },
         backdrop: {
             default: true
         },
         options: {
             
         },
         'class': '',
         cssClass: '',
         callback: Function,
         saveLabel: 'Save'
     },
     data: function() {
         return {
             modal: null,
             modalClass: ''
         }
     },
     computed: {
         classProp: function() {
             return this['class'] || '';
         }
     },
     mounted: function() {
         console.log('mounted modal');
         var self = this;
         console.log('tag ', this.tag);
         var $ele = $(self.$el);
         this.modalClass = 'modal-md';
         if (this.large) {
             this.modalClass = 'modal-lg';
         } else if (this.small) {
             this.modalClass = 'modal-sm';
         }
         $ele.on("dragover", function(e) { e.preventDefault();  e.stopPropagation(); });
         $ele.on("drop", function(e) { e.preventDefault(); e.stopPropagation(); });
         $ele.modal({
             show: true,
             backdrop: self.backdrop,
             width: 800
         });
         $ele.on('hidden.bs.modal', function(e) {
             console.log('hide modal!!');
             self.$emit('close')
         });
         //console.log('attached modal ', this.shown);
         //if (this.shown) {
         //    $(this.$el).modal('show');
         //}
     },
     methods: {
         open: function() {
             
         },
         modalCallback: function() {
             console.log('modal close callback');
             this.callback.apply(this, arguments);
             this.close();
         },
         saveChanges: function() {
             console.log('modal save changes clicked!');
             var funcResult = null;
             if (this.$refs.bodycomponent && this.$refs.bodycomponent.saveChanges) {
                 var result = this.$refs.bodycomponent.saveChanges();
                 funcResult = this.callback(result);
             } else if (this.callback) {
                 funcResult = this.callback();
             }
             if (funcResult === false) {

             } else {
                 this.close();
             }
         },
         close: function() {
             console.log('methods.close');
             $(this.$el).modal('hide');
         }
     },
     watch: {
         shown: function(newVal, old) {
             console.log('shown changed!');
             if (newVal) {
                 $(this.$el).modal('show');
             } else {
                 $(this.$el).modal('hide');
             }
         }
     }
 }
     
</script>
