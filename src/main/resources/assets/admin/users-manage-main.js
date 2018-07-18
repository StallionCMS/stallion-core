(function() {

    window.StallionAdminUsersManageMain = {};

    StallionAdminUsersManageMain.boot = function() {
        
        Vue.use(StallionUtilsVuePlugin);
        Vue.use(Vuex);

        var allComponents = Vue.options.components;

        const store = new Vuex.Store({
            state: {
                count: 0
            },
            mutations: {
                increment (state) {
                    state.count++
                }
            }
        });

        const routes = [
            { path: '/user/:userId', component: allComponents['users-edit'] },
            { path: '/', component: allComponents['users-table'] }
        ]

        const router = new VueRouter({
            routes // short for `routes: routes`
        })

        
        new Vue({
            el: '#app',
            router,  
            store,
            template: '<stallion-admin-app></stallion-admin-app>'
        })

        
    }

    







    

}());
