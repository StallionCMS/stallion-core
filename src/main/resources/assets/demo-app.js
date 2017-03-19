(function() {

    var demo = {};
    window.StallionDemoApp = demo;

    demo.init = function() {

       var routes = [
            {
                path: '/',
                name: 'demo-home',
                component: vueComponents['demo-home']
            },
            {
                path: '/tomes',
                name: 'tomes-table',
                component: vueComponents['tomes-table']
            },
            {
                path: '/form',
                name: 'form-demo',
                component: vueComponents['form-demo']
            },
            {
                path: '/file-fields',
                name: 'file-fields',
                component: vueComponents['file-fields-demo']
            },
            {
                path: '/markdown-textarea',
                name: 'markdown-textarea',
                component: vueComponents['simple-text-editor-demo']
            }           
        ];

        
        var router = new VueRouter({
            routes: routes
        });


        router.beforeEach(function(to, from, next) {
            next();
        });        


        var App = new Vue(
            {
                router: router
            }
        );
        var AppMounted = App.$mount('#stallion-demo-vue-app');

        
        demo.router = router;
        demo.vueApp = App;

        console.log('app mounted? ', App);
        

    };
    
    $(document).ready(demo.init);
    
}());



