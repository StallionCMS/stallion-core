(function() {

    var UsersManagePlugin = {};

    function bootVue() {
        Vue.use(StallionUtilsVuePlugin);
        Vue.use(Vuex);

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
            { path: '/user/:userId', component: vueComponents['users-edit'] },
            { path: '/', component: vueComponents['users-table'] }
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

    



    document.addEventListener("DOMContentLoaded", bootVue); 



    

}());
