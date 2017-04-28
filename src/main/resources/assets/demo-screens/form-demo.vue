<style lang="scss">
 .moving-field-label {
     position:absolute;
     
 }
 .moving-field {
     border: 1px solid transparent;
     outline: none;
     -moz-box-shadow:    0px 0px 0px 0px #FFF;
     -webkit-box-shadow: 0px 0px 0px 0px #FFF;
     box-shadow:         0px 0px 0px 0px #FFF;
     padding: 0px 0px 0px 0px;
     margin-top: 2em;
 }
 .moving-field:active, .moving-field:focus {
     border: 1px solid transparent;
     outline: none;
     -moz-box-shadow:    0px 0px 0px 0px #FFF;
     -webkit-box-shadow: 0px 0px 0px 0px #FFF;
     box-shadow:         0px 0px 0px 0px #FFF;
 }
 .moving-field.active-field {

 }
 .moving-field.nonempty, .moving-field.focused, .moving-field:active, .moving-field:focus {
     padding-top: 2em;
     margin-top: 0em;
 }
 .moving-field.empty, .moving-field {

 } 
</style>

<template>
    <div class="ui-demo-vue">
        <form @submit.prevent="onSave">
            <div class="form-group">
                <label>Title (autogrow-text)</label>
                <autogrow-text v-model="book.title"></autogrow-text>
            </div>
            <div class="form-group">
                <label>Description (autogrow-textarea)</label>
                <autogrow-textarea v-model="book.description"></autogrow-textarea>
            </div>
            <div class="form-group">
                <label>Author (select2-field single)</label>
                <div>
                    <select2-field v-model="book.author" :choices="authors" :config="{width: 500}">
                        <option value="Charles Dickens">Charles Dickens</option>
                        <option value="Edward Gibbon">Edward Gibbon</option>
                        <option value="Tom Clancy">Tom Clancy</option>
                    </select2-field>
                    <button type="input"  @click="book.author='Tom Clancy'">Set to Tom</button>
                </div>
            </div>
            <div class="form-group">
                <label>Published City</label>
                <div>
                    <autocomplete-input v-model="book.city" :choices="cities" :config="{minChars: 1}"></autocomplete-input>
                </div>
            </div>
            <div class="form-group">
                <label>Published At (date-picker)</label>
                <div>
                    <date-picker v-model="book.publishedAt"></date-picker>
                </div>
            </div>
            <div class="form-group">
                <label>Special Features</label>
                <checkboxes v-model="book.features" :choices="['Big Print', 'Reference', 'No Checkout', 'Audiobook']"></checkboxes>
            </div>
            <div class="form-group">
                <label>Expires At (datetime-picker)</label>
                <div>
                    <datetime-picker v-model="book.expiresAt"></datetime-picker>
                </div>
            </div>
            <div class="form-group">
                <label>Categories (select2-field[multiple=true]</label>
                <div>
                    <select2-field v-model="book.categories" :choices="categories"  :config="{width: 500}" :multiple="true">
                        <option value="history">History</option>
                        <option value="geography">Geography</option>
                        <option value="science">Science</option>
                        <option value="fiction">Fiction</option>
                    </select2-field>
                    <button type="input" @click="book.categories=['science', 'fiction']">Set to Science & Fiction</button>
                </div>
            </div>
            <div class="form-group">
                <button type="submit" class="btn btn-primary btn-xl">Save</button>
            </div>
        </form>
    </div>
</template>

<script>
 module.exports = {
     data: function() {
         var book = {
             author: 'Edward Gibbon',
             title: 'A Tale of Two Cities',
             city: 'Boston',
             description: 'One of the finest novels of all time',
             features: ['Big Print', 'Audiobook'],
             publishedAt: (new Date().getTime() / 1000) - (2 * 86400),
             expiresAt: (new Date().getTime() / 1000) + (2 * 86400),
             categories: ['science'],
             
         };

         if (localStorage.demoBook) {
             book = JSON.parse(localStorage.demoBook);
         }
         if (!book.publishedAt) {
             book.publishedAt = (new Date().getTime() / 1000) - (2 * 86400);
         }
         if (!book.expiresAt) {
             book.expiresAt = (new Date().getTime() / 1000) - (2 * 86400);
         }
         if (book.city === undefined) {
             book.city = 'Boston';
         }
         console.log('initial book categories ', book.categories);
         
         return {
             authors: [
                 '',
                 'Charles Dickens',
                 'Edward Gibbon',
                 'Tom Clancy'
             ],
             cities: [
                 'Chicago',
                 'Boston',
                 'Detroit',
                 'New York',
                 'Los Angeles',
                 'York',
                 'Newark',
                 'Portland (Oregon)',
                 'Portland (Maine)',
                 'San Francisco',
                 'San Diego',
                 'Miami',
             ],
             categories: [
                 'history',
                 'geography',
                 'science',
                 'fiction'
             ],
             book: book
         };
     },
     watch: {
         '$route': 'loadBook',
         'book.categories': 'onSave',
         'book.publishedAt': function(newVal) {
             console.log('new published at ', newVal);
         },
         'book.expiresAt': function(newVal) {
             console.log('new expired at ', newVal);
         }         
     },
     methods: {
         loadBook: function() {
             if (localStorage.demoBook) {
                 this.book = JSON.parse(localStorage.demoBook);
             }
         },
         onSave: function() {
             console.log(
                 'book ',
                 this.book,
                 this.book.title,
                 this.book.categories,
                 this.book.author,
                 this.book.description,
                 this.book.publishedAt,
                 this.book.expiresAt
             );
             localStorage.demoBook = JSON.stringify(this.book);
             
         }
     }
 }
</script>
