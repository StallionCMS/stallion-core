
<style lang="scss">
 #ui-demo-form {

 }
</style>


<template>
    <div class="ui-demo-vue" id="ui-demo-form">
        <form  method="post" @submit.prevent="onSubmit" enctype="multipart/form-data" v-stallion-floating-labels>
            <div class="p">
                <h4>Upload Files</h4>
                <input type="file" name="file" multiple>
                <input type="submit" value="Upload File" name="submit">
            </div>
            <div class="p">
                <b-field label="Title" >
                    <b-input v-model="book.title" ></b-input>
                </b-field>
                <b-field label="Author">
                    <b-input v-model="book.author" ></b-input>
                </b-field>
                <b-field label="Publisher">
                    <b-input v-model="book.publisher" ></b-input>
                </b-field>
                <b-field label="City">
                    <b-input v-model="book.city"  ></b-input>
                </b-field>
                
            </div>
        </form>
        <hr>
        <div>
            <table>
                <tr v-for="thing in things">
                    <td>{{ thing.name }}</td>
                    <td>{{ thing.createdAt }}</td>
                </tr>
            </table>
        </div>
    </div>
</template>

<script>
 module.exports = {
     data: function() {
         return {
             isLoading: true,
             book: {
                 title: 'A Tale of a Team of Beards',
                 author: 'Mike Napoli',
                 publisher: '',
                 city: ''
             },
             things: []
         };
     },
     created: function() {
         this.onRoute();
     },
     watch: {
         '$route': 'onRoute'
     },
     methods: {
         onSubmit: function() {
             var url = '/st-user-uploads/upload-file';
             var files = document.querySelector('[type=file]').files;
             var formData = new FormData();
             // Append files to files array
             for (let i = 0; i < files.length; i++) {
                 let file = files[i];
                 
                 formData.append('file', file);
             }
             fetch(url, {
                 headers: {
                     'X-Requested-By': 'XMLHttpRequest'
                 },
                 method: 'POST',
                 body: formData
             }).then(response => {
                 debugger;
                 console.log(response);
             });
             
         },
         onRoute: function() {
             var self = this;
             this.fetchData();
         },
         fetchData: function() {
             var self = this;
             this.$stAjax({
                 url: '/st-ui-demo/demo-data'
             }).then(function(o) {
                 self.things = o.data.demoThings;
             })
         }
     }
 };
</script>
