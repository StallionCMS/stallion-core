
<style lang="scss">
 #ui-demo-form {

 }
</style>


<template>
    <div class="ui-demo-vue" id="ui-demo-form">
        <div>
            <a href="#/ui-demo-floating-form">Floating Form</a>
            <a href="#/ui-demo-normal-form">Normal Form</a>
        </div>
        <form  method="post" @submit.prevent="onSubmit" enctype="multipart/form-data" style="max-width: 700px;">
            <div class="p">
                <h4>Upload Files</h4>
                <input type="file" name="file" multiple>
                <input type="submit" value="Upload File" name="submit">
            </div>
            <div class="p">
                <b-field label="Associated Article">
                    <st-autocomplete placeholder="Choose associated article..." required expanded label-field="title" value-field="id"  v-model="book.articleId" :data="articles" ></st-autocomplete>
                </b-field>
                <b-field label="Tags">
                    <st-taginput expanded :data="tags" placeholder="Tags" :allow-new="true"  v-model="book.tags"  :keep-open="true" :required="true" :min-count="2" :max-count="3"></st-taginput>
                </b-field>                
                <b-field label="Title" >
                    <b-input v-model="book.title" ></b-input>
                </b-field>
                <b-field label="Author">
                    <b-input v-model="book.author" ></b-input>
                </b-field>
                <b-field label="Publisher">
                    <b-input v-model="book.publisher" :required="true"></b-input>
                </b-field>
                <b-field label="City">
                    <b-input v-model="book.city"  ></b-input>
                </b-field>
                <b-field label="Genre">
                    <b-select expanded>
                        <option>Non-Fiction</option>
                        <option>Fiction</option>
                    </b-select>
                </b-field>
                    
                <b-field label="Category">
                    <st-autocomplete expanded :data="categories" placeholder="Category"  v-model="book.category" ></st-autocomplete>
                </b-field>
                <b-field label="Available in these stores">
                    <st-taginput expanded :data="stores" label-field="name" value-field="id" placeholder="Available in these stores..."  v-model="book.stores" ></st-taginput>
                </b-field>                
                
            </div>
            <div class="p">
                <button class="button" type="submit">Submit</button>
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
        <p>&nbsp;</p>
        <p>&nbsp;</p>
        <p>&nbsp;</p>
        <p>&nbsp;</p>
        <p>&nbsp;</p>
        <p>&nbsp;</p>
        <p>&nbsp;</p>
        <p>&nbsp;</p>
        <p>&nbsp;</p>
        <p>&nbsp;</p>
        <p>&nbsp;</p>
        <p>&nbsp;</p>
        <p>&nbsp;</p>
        <p>&nbsp;</p>
        <p>&nbsp;</p>
        <p>&nbsp;</p>
        <p>&nbsp;</p>
        <p>&nbsp;</p>
        <p>&nbsp;</p>
        <p>&nbsp;</p>
        
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
             tags: [
                 'funny',
                 'sad',
                 'happy',
                 'page turner',
                 'trash',
                 'pulp fiction',
                 'the baby escapes her cage',
                 'the cow jumped over the moon',
                 'drug & apothecary shops',
                 'retrofitting & renovation services'
             ],
             stores: [
                 {
                     id: 200,
                     name: 'Amazon',
                 },
                 {
                     id: 201,
                     name: 'Borders',
                 },
                 {
                     id: 202,
                     name: 'Barnes & Noble',
                 }                 
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
                 category: 'Healthcare',
                 stores: [200]
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
