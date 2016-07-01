

console.log("users namage v2");


var goTo = function(hash) {
    
    window.location.hash = hash;
};

// The router needs a root component to render.
// For demo purposes, we will just use an empty one
// because we are using the HTML as the app template.
// !! Note that the App is not a Vue instance.
var App = Vue.extend({})

// Create a router instance.
// You can pass in additional options here, but let's
// keep it simple for now.
var router = new VueRouter({
    transitionOnLoad: true
});

// Define some routes.
// Each route should map to a component. The "component" can
// either be an actual component constructor created via
// Vue.extend(), or just a component options object.
// We'll talk about nested routes later.
router.map({
    '': {
        component: vueComponents['users-table']
    },
    '/users/:page': {
        component: vueComponents['users-table']
    },
    '/edit-user/:userId': {
        component: vueComponents['users-edit']
    }
})

// Now we can start the app!
// The router will create an instance of App and mount to
// the element matching the selector #app.
router.start(App, '#app')
