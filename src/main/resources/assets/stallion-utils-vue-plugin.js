
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


        Vue.directive('stallion-floating-labels', {
             // directive definition
             inserted: function (el, binding, vnode) {
                 el.classList.add('floating-labels-form');
                 el.querySelectorAll("input").forEach(function(input) {
                     var labelEle = null;
                     var fieldEle = null;
                     // floating-active
                     if (input.parentElement && input.parentElement.classList.contains("field")) {
                         fieldEle = input.parentElement;
                     } else if (input.parentElement && input.parentElement.parentElement && input.parentElement.parentElement.classList.contains("field")) {
                         fieldEle = input.parentElement.parentElement;
                     }
                     if (fieldEle) {
                         labelEle = fieldEle.querySelector('label.label');
                         if (input.value) {
                             fieldEle.classList.add('floating-label-active');
                             input.setAttribute('placeholder', '');
                         } else if (labelEle) {
                             input.setAttribute('placeholder', labelEle.innerText);
                         }
                     }
                     
                     input.addEventListener('input', function onInputChanged(evt) {
                         var ele = evt.target;
                         if (ele.value) {
                             ele.parentElement.parentElement.classList.add("floating-label-active");
                             input.setAttribute('placeholder', '');
                         }
                     });
                     
                     input.addEventListener('blur', function onBlur(evt) {
                         var ele = evt.target;
                         if (ele.value) {
                             ele.parentElement.parentElement.classList.add("floating-label-active");
                         } else {
                             ele.parentElement.parentElement.classList.remove("floating-label-active");
                             if (labelEle) {
                                 input.setAttribute('placeholder', labelEle.innerText);
                             }
                         }

                     });
                 });                 
                 
             },
             componentUpdated: function(el, binding, vnode, oldNode) {
                 var that = this;
                 
                 
             },
             onInputChanged: function(evt) {
                 debugger;
             },
             onBlur: function(evt) {
                 debugger;
             }
        });          


        // 3. inject some component options
        Vue.mixin({
            data: function () {
                return {
                  //  stallionProcessingLocks: {}
                }
            }
        })
        

        // 4. add instance methods

        Vue.prototype.$stallionUtils = {
            debounce: function(func, wait, immediate) {
	        var timeout;
	        return function() {
	            var context = this, args = arguments;
	            var later = function() {
		        timeout = null;
		        if (!immediate) func.apply(context, args);
	            };
	            var callNow = immediate && !timeout;
	            clearTimeout(timeout);
	            timeout = setTimeout(later, wait);
	            if (callNow) func.apply(context, args);
	        };
            },
            slugify: function(text) {
                return text.toString().toLowerCase()
                    .replace(/\s+/g, '-')           // Replace spaces with -
                    .replace(/[^\w\-]+/g, '')       // Remove all non-word chars
                    .replace(/\-\-+/g, '-')         // Replace multiple - with single -
                    .replace(/^-+/, '')             // Trim - from start of text
                    .replace(/-+$/, '');            // Trim - from end of text
            },
            generateUUID: function() {
                var d = new Date().getTime();
                if(window.performance && typeof window.performance.now === "function"){
                    d += performance.now(); //use high-precision timer if available
                }
                var uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
                    var r = (d + Math.random()*16)%16 | 0;
                    d = Math.floor(d/16);
                    return (c=='x' ? r : (r&0x3|0x8)).toString(16);
                });
                return uuid;
            },
            toTitleCase: function(str) {
                return str.replace(/\w\S*/g, function(txt){return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();});
            }
        };


        
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
                        el.setAttribute('disabled', true);
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
    
