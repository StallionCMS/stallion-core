(function() {

    var UsersManagePlugin = {};

    function bootVue() {
        Vue.use(UsersManagePlugin);
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
            { path: '/user/:userId', component: 'users-edit' },
            { path: '/users', component: 'users-table' }
        ]

        const router = new VueRouter({
            routes // short for `routes: routes`
        })

        
        new Vue({
            el: '#app',
            router,  
            store,
            template: '<users-table></users-table>'
        })

        
    }

    
    UsersManagePlugin.install = function (Vue, options) {

        /*
        // 1. add global method or property
        Vue.myGlobalMethod = function () {
        // something logic ...
        }

        // 2. add a global asset
        Vue.directive('my-directive', {
        bind (el, binding, vnode, oldVnode) {
        // something logic ...
        }
        ...
        })

        // 3. inject some component options
        Vue.mixin({
        created: function () {
        // something logic ...
        }
        ...
        })

        // 4. add an instance method
        Vue.prototype.$myMethod = function (methodOptions) {
        // something logic ...
        }
        */
    }






    document.addEventListener("DOMContentLoaded", bootVue); 



    

}());
