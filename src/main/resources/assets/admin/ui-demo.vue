
<style lang="scss">
 #ui-demo-form {

 }
</style>


<template>
    <div class="ui-demo-vue" id="ui-demo-form">
        <div>
            <a href=""></a>
        </div>
        <form  method="post" @submit.prevent="onSubmit" enctype="multipart/form-data" v-stallion-floating-labels style="max-width: 700px;">
            <div class="p">
                <h4>Upload Files</h4>
                <input type="file" name="file" multiple>
                <input type="submit" value="Upload File" name="submit">
            </div>
            <div class="p">
                <b-field label="Associated Article">
                    <st-autocomplete placeholder="Choose associated article..." required="true" label-field="title" value-field="id" v-model="book.articleId" :data="articles"></st-autocomplete>
                </b-field>                
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
                <b-field label="Category">
                    <st-autocomplete :data="categories" placeholder="Category"  v-model="book.category" ></st-autocomplete>
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
             categories: [
                 'War',
                 'Peace',
                 'Trade',
                 'Education',
                 'Healthcare',
                 'Policy',
                 'Immigration',
                 'Emmigration',
                 'Economy'
             ],
             articles: [
                 {
                     id: 100,
                     title: 'Do you want volatile characters on your team?'
                 },
                 {
                     id: 101,
                     title: 'Best Instagram Japanese Vacations'
                 },
                 {
                     id: 102,
                     title: 'Is the downside worth the upside?'
                 },
                 {
                     id: 103,
                     title: 'What impact on the locker room in your team?'
                 },
                 {
                     id: 104,
                     title: "It's a complicated situation"
                 },
                 {
                     id: 105,
                     title: "Ten dollars off your next purchase"
                 },
                 {
                     id: 106,
                     title: "Kings have $30 million in cap room"
                 },
                 {
                     id: 107,
                     title: "I renounce! I renounce him!"
                 },
                 {
                     id: 108,
                     title: "The Agent Summit is where the Rules are Made"
                 }                                  
                 
             ],
             book: {
                 title: 'A Tale of a Team of Beards',
                 author: 'Mike Napoli',
                 publisher: '',
                 city: '',
                 articleId: 106,
                 category: 'Healthcare'
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
