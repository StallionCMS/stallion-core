




var goTo = function(hash) {
    
    window.location.hash = hash;
};


var routes = [
    {
        path: '',
        component: vueComponents['users-table']
    },
    {
        path: '/users/:page',
        component: vueComponents['users-table']
    },
    {
        path: '/edit-user/:userId',
        component: vueComponents['users-edit']
    }
];

// Create a router instance.
// You can pass in additional options here, but let's
// keep it simple for now.
var router = new VueRouter({
    routes: routes,
    transitionOnLoad: true
});


// Now we can start the app!
// The router will create an instance of App and mount to
// the element matching the selector #app.
//router.start(App, '#app');



// The router needs a root component to render.
// For demo purposes, we will just use an empty one
// because we are using the HTML as the app template.
// !! Note that the App is not a Vue instance.
var App = new Vue(
    {
        router: router
    }
).$mount('#app');
//Vue.extend({})
