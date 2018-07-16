<!--
 * Stallion v1.0.0 (http://stallion.io)
 * Copyright 2016-2018 Stallion Software LLC
 * Licensed under GPL (https://github.com/StallionCMS/stallion-core/blob/master/LICENSE)
-->

<template>
    <textarea class="form-control" ></textarea>
</template>

<script>
 module.exports = {
     props: {
         value: ''
     },
     mounted: function() {
         var self = this;
         //stallion.autoGrow({}, $(this.$el));
         this.simplemde = new SimpleMDE({
             element: this.$el,
             toolbar: this.makeToolbar(),
             status: false,
             forceSync: true,
             initialValue: self.value
         });
         this.simplemde.codemirror.on("change", function(){
             //console.log(self.simplemde.value());
             self.$emit('input', self.simplemde.value());
         });         
     },
     methods: {
         makeToolbar: function() {
             var toolbar = [
	         {
		     name: "bold",
		     className: "icon icon-bold",
		     title: "Bold",
                     action: SimpleMDE.toggleBold,
		     default: true
	         },
	         {
		     name: "italic",
		     action: SimpleMDE.toggleItalic,
		     className: "icon icon-italic",
		     title: "Italic",
		     default: true
	         },
	         {
		     name: "strikethrough",
		     action: SimpleMDE.toggleStrikethrough,
		     className: "icon icon-strikethrough",
		     title: "Strikethrough"
	         },
	         {
		     name: "heading",
		     action: SimpleMDE.toggleHeadingSmaller,
		     className: "icon icon-font-size",
		     title: "Heading",
		     default: true
	         },
                 {
		     name: "code",
		     action: SimpleMDE.toggleCodeBlock,
		     className: "icon icon-embed2",
		     title: "Code"
	         },
	         {
		     name: "quote",
		     action: SimpleMDE.toggleBlockquote,
		     className: "icon icon-quotes-left",
		     title: "Quote",
		     default: true
	         },
	         {
		     name: "unordered-list",
		     action: SimpleMDE.toggleUnorderedList,
		     className: "icon icon-list2",
		     title: "Generic List",
		     default: true
	         },
	         {
		     name: "ordered-list",
		     action: SimpleMDE.toggleOrderedList,
		     className: "icon icon-list-numbered",
		     title: "Numbered List",
		     default: true
	         },
                 {
		     name: "link",
		     action: SimpleMDE.insertLink,
		     className: "icon icon-link",
		     title: "Create Link",
		     default: true
	         },
                 {
		     name: "fullscreen",
		     action: SimpleMDE.toggleFullScreen,
		     className: "icon icon-enlarge2 no-disable no-mobile",
		     title: "Toggle Fullscreen",
		     default: true
                 },
	         {
		     name: "undo",
		     action: SimpleMDE.undo,
		     className: "icon icon-undo no-disable",
		     title: "Undo"
	         },
	         {
		     name: "redo",
		     action: SimpleMDE.redo,
		     className: "icon icon-redo no-disable",
		     title: "Redo"
	         }
         ];
         return toolbar;

         }
     }
     
 };
</script>
