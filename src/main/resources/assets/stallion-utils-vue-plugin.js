
(function() {

    window.StallionUtilsVuePlugin = {};
    var plugin = window.StallionUtilsVuePlugin;

    plugin.install = function(Vue, options) {

        axios.defaults.headers.post['X-Requested-By'] = 'XMLHttpRequest';
        

        /*
        // 1. add global method or property
        Vue.myGlobalMethod = function () {
        // something logic ...
        }
        */


        // 2. add a global asset

        Vue.directive('stallion-locking', {
            bind (el, binding, vnode, oldVnode) {
                el.classList.add('stallion-locking');
                el.setAttribute('data-stallion-locking', binding.value);
                //
                // something logic ...
            }
        })


        // 3. inject some component options
        Vue.mixin({
            data: function () {
                return {
                  //  stallionProcessingLocks: {}
                }
            }
        })
        

        // 4. add an instance method
        Vue.prototype.$stAjax = function (opts) {
            var that = this;
            var lockedElements = [];
            if (opts.lock) {
                that.stallionProcessingLocks = that.stallionProcessingLocks || {};
                if (that.stallionProcessingLocks[opts.lock]) {
                    console.warn("Lock " + opts.lock + " is already active, canceling $stajax request");
                    return;
                }
                that.stallionProcessingLocks[opts.lock] = true;
                var lockPrefix = opts.lock.split('.')[0];

                that.$el.querySelectorAll(".stallion-locking").forEach(function(el) {
                    var elLock = el.getAttribute('data-stallion-locking');
                    if (elLock.indexOf(lockPrefix + ".") === 0) {
                        el.classList.add('stallion-locking-disabled');
                        el.setAttribute('disabled', true);
                    }
                    if (elLock === opts.lock) {
                        el.classList.add('stallion-locking-processing');
                    }
                    lockedElements.push(el);
                });
                // Clear existing errors
                
            }



            
            if (!opts.method) {
                opts.method = 'GET';
            }
            var promise = axios(opts);
            var originalCatch = promise.catch;
            var catchOverridden = false;

            if (opts.useDefaultCatch !== false) {
                promise.catch(function(e, b, c) {
                    console.log('$stajax catch ', e, b, c, 'catchOverridden: ' + catchOverridden);
                    var message = e.message;
                    var debugMessage = '';
                    if (typeof(e.response.data) === 'object') {
                        if (e.response.data.message) {
                            message = e.response.data.message;
                        }
                        if (e.response.data.debugMessage) {
                            debugMessage = e.response.data.debugMessage;
                        }                        
                    }
                    console.error(e, message, debugMessage);
                    that.$toast.open({
                        duration: 5000,
                        message: message,
                        type: 'is-danger'
                    });
                });
            }

            if (opts.success) {
                alert("Option 'success' is deprecated. Use promise '.then()' method.");
                return;
                /*promise.then(function(res) {
                    opts.success(res.data, res);
                });*/
            }
            if (opts.error) {
                alert("Option 'error' is deprecated. Use promise '.then()' method.");
                return;
            }
            
            promise.finally(function(a, b, c) {
                if (opts.lock) {
                    delete that.stallionProcessingLocks[opts.lock];
                }
                lockedElements.forEach(function(el) {
                    el.classList.remove('stallion-locking-disabled');
                    el.classList.remove('stallion-locking-processing');
                    el.removeAttribute("disabled")
                });
            });
            return promise;
        };
        
    }

    


    

}());
    
