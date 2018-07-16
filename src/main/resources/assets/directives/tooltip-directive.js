/*
 * Stallion v1.0.0 (http://stallion.io)
 * Copyright 2016-2018 Stallion Software LLC
 * Licensed under GPL (https://github.com/StallionCMS/stallion-core/blob/master/LICENSE)
*/


(function() {

    Vue.directive('st-tooltip', {
        bind: function () {
            // do preparation work
            // e.g. add event listeners or expensive stuff
            // that needs to be run only once
            //debugger;
            $(this.el).tooltip({});
        },
        update: function (newValue, oldValue) {
            // do something based on the updated value
            // this will also be called for the initial value
            
        },
        unbind: function () {
            // do clean up work
            // e.g. remove event listeners added in bind()
        }
    });
    
}());
