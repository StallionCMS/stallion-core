/*
 * Stallion Core: A Modern Web Framework
 *
 * Copyright (C) 2015 - 2019 Stallion Software LLC.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 2 of
 * the License, or (at your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public
 * License for more details. You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/gpl-2.0.html>.
 *
 *
 *
 */



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

        function initAutocompleteFloatingLabels(el, binding, vnode, autocompleteEl) {
            var selectEle = autocompleteEl.querySelector('select.select');
            var fieldEle = autocompleteEl.closest('.field');
            // floating-active
            if (fieldEle === null) {
                fieldEle = autocompleteEl.parentElement;
            }
            function onChange(evt) {
                var that = this;
                if (selectEle.value) {
                    if (!fieldEle.classList.contains('floating-label-active')) {
                        fieldEle.classList.add("floating-label-active");
                    }
                    fieldEle.classList.remove("floating-label-empty");
                } else {
                    if (!fieldEle.classList.contains('floating-label-empty')) {
                        fieldEle.classList.add("floating-label-empty");
                    }
                    fieldEle.classList.remove("floating-label-active");
                }
            }
            onChange();
            selectEle.addEventListener('change', function(evt) {
                onChange();
            });            

        }


        function initTaginputFloatingLabels(el, binding, vnode, taginputEle) {
            var fieldEle = taginputEle.closest('.field');
            var inputEle = taginputEle.querySelector('input');
            // floating-active
            if (fieldEle === null) {
                fieldEle = taginputEle.parentElement;
            }
            function onChange(evt) {
                var that = this;
                
                if (Number(inputEle.getAttribute('data-count')) > 0) {
                    if (!fieldEle.classList.contains('floating-label-active')) {
                        fieldEle.classList.add("floating-label-active");
                    }
                    fieldEle.classList.remove("floating-label-empty");
                } else {
                    if (!fieldEle.classList.contains('floating-label-empty')) {
                        fieldEle.classList.add("floating-label-empty");
                    }
                    fieldEle.classList.remove("floating-label-active");
                }
            }
            onChange();
            inputEle.addEventListener('change', function(evt) {
                onChange();
            });            

        }
        
        
        Vue.directive('stallion-floating-labels', {
             // directive definition
            inserted: function (el, binding, vnode) {
               
                 el.classList.add('floating-labels-form');
                el.querySelectorAll("input").forEach(function(input) {
                     var autocomplete = input.closest('.st-autocomplete-vue');
                     if (autocomplete) {
                         initAutocompleteFloatingLabels(el, binding, vnode, autocomplete);
                         return;
                     }
                    var taginput = input.closest('.st-taginput-vue');
                    if (taginput) {
                        initTaginputFloatingLabels(el, binding, vnode, taginput);
                        return;
                    }
                    
                     var labelEle = null;
                     var fieldEle = input.closest('.field');
                     // floating-active
                     if (fieldEle === null) {
                         fieldEle = input.parentElement;
                     }
                     
                     
                     labelEle = fieldEle.querySelector('label.label');
                     var tagInputContainer = input.closest('.taginput-container');
                     
                     if (input.value || (tagInputContainer && tagInputContainer.querySelectorAll('.tag').length > 0)) {
                         fieldEle.classList.add('floating-label-active');
                         input.setAttribute('placeholder', '');
                     } else if (labelEle) {
                         input.setAttribute('placeholder', labelEle.innerText);
                         fieldEle.classList.add('floating-label-empty');
                     }

                     function onValueChanged(evt) {
                         console.log('onValueChanged');
                         if (input.value || (tagInputContainer && tagInputContainer.querySelectorAll('.tag').length > 0)) {
                             fieldEle.classList.add("floating-label-active");
                             fieldEle.classList.remove("floating-label-empty");
                             input.setAttribute('placeholder', '');
                         } else {
                             fieldEle.classList.remove("floating-label-active");
                             fieldEle.classList.add("floating-label-empty");
                         }
                     };
                     
                     input.addEventListener('input', onValueChanged);
                     console.log('input ', input);
                     input.addEventListener('change', onValueChanged);
                     
                     input.addEventListener('blur', onValueChanged);

                     input.addEventListener('blur', function(evt) {
                         if (labelEle) {
                             if (!input.value && !(tagInputContainer && tagInputContainer.querySelectorAll('.tag').length > 0)) {
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
                    var lockFound = false;
                    if (elLock.indexOf(lockPrefix + ".") === 0) {
                        el.classList.add('stallion-locking-disabled');
                        el.setAttribute('disabled', true);
                        lockFound = true;
                    }
                    if (elLock === opts.lock) {
                        el.classList.add('stallion-locking-processing');
                        el.setAttribute('disabled', true);
                        lockFound = true;
                    }
                    if (lockFound) {
                        lockedElements.push(el);
                    }

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
    
