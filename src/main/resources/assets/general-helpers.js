
(function() {

    window.GeneralHelpers = {
        queryParams: function() {
            var qs = (function(a) {
                if (a === "") return {};
                var b = {};
                for (var i = 0; i < a.length; ++i)
                {
                    var p=a[i].split('=', 2);
                    if (p.length == 1)
                        b[p[0]] = "";
                    else
                        b[p[0]] = decodeURIComponent(p[1].replace(/\+/g, " "));
                }
                return b;
            })(window.location.search.substr(1).split('&'));
            return qs;
        },
        getCookie: function(name) {
            try {
                var value = "; " + document.cookie;
            } catch (e) {
                console.log("error in stallion.getCookie()", e);
                return "";
            }
            var parts = value.split("; " + name + "=");
            if (parts.length == 2) return parts.pop().split(";").shift();
        },
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


    

}());
    
