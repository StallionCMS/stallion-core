/*
 * Stallion Core: A Modern Web Framework
 *
 * Copyright (C) 2015 - 2016 Stallion Software LLC.
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


/* Stallion common JS library */

if (window.$ && !window.jQuery) {
  window.jQuery = $;
}


(function() {

    var st = {};
    window.stallion = st;

    st.formToData = function(form) {
        var data = {};
        $.each(form.elements, function(i, ele) {
            console.log('an element', ele);
            var name = ele.getAttribute('name');
            console.log(name, ele);
            if (!name) {
                return;
            }
            if (ele.getAttribute("type") === 'checkbox') {
                data[name] = $(ele).is(":checked");
            } else {
                data[name] = ele.value;
            }
        });
        return data;
    };

    st.testUnicode = function() {
        console.log("This is a unicode snowman in stallion.js: ☃");
    };

    var tokenRe = /\{\s*([\w\.]+)\s*\}/g;
    st.renderTemplate = function(s, ctx) {
        s = s.replace(tokenRe, function(full, token, c, d) {
            var parts = token.split('.');
            var len = parts.length;
            var val = ctx;
            for (var i = 0; i <len; i++) {
                val = val[parts[i]];
            }
            return val;
        });
        return s;
    };
    
    st.request = function(req) {
        var spinner = null;
        if (req.form) {
            spinner = st.tryMakeSpinner(req.form);
            if (!spinner) {
                // Spinner is already running, abort
                console.log('Button .st-button-submit not found, or spinner already running, aborting stallion.request');
                return;
            }
            $(req.form).find('.st-success').hide();
            $(req.form).find('.st-error').hide();
        }
        

        if (!req.error) {
            req.error = st.defaultRequestErrorHandler;
        }
        

        var method = req.method || 'get';
        var dataString = req.data;

        if (dataString && typeof(dataString) !== typeof("")) {
            dataString = JSON.stringify(dataString);
        }
        var headers = {};
        var xsrfCookie = st.getCookie("XSRF-TOKEN");
        if (xsrfCookie) {
            headers["X-XSRF-TOKEN"] = xsrfCookie; 
        }
        return $.ajax({
            url: req.url,
            type: method.toLowerCase(),
            data: dataString,
            headers: headers,
            contentType: 'application/json',
            dataType: 'json'
        }).success(function(obj) {
            try {
                if (spinner) {
                    spinner.clear();
                }
                if (req.success) {
                    req.success(obj);
                }
            } finally {
                if (req.always) {
                    req.always(obj);
                }
            }
        }).error(function(xhr) {
            try {
                if (spinner) {
                    spinner.clear();
                }
                // Don't give an error message for cases where the error happens from navigating away from the page
                // before we have finished a request.
                if (xhr.readyState != 4) {
                    return;
                }
                var o;
                try {
                    o = JSON.parse(xhr.responseText);
                } catch(e) {
                    o = {message: xhr.responseText};
                }
                if (!o.message) {
                    o.message = 'Error processing request';
                }
                
                req.error(o, req.form, xhr);
                
            } finally {
                if (req.always) {
                    req.always(xhr);
                }
            }
        });
    };

    st.tryMakeSpinner = function(form) {
        var $form = $(form);
        var $btn = [];
        if ($form.get(0) && $form.get(0).tagName === 'BUTTON') {
            $btn = $form;
        }
        if (!$btn.length) {
            if ($form.hasClass('.btn') || $form.hasClass('.pure-button')) {
                $btn = $form;
            }
        }
        if (!$btn.length) {
            $btn = $form.find('.st-button-submit');
        }
        if (!$btn.length) {
            $btn = $form.find('.pure-button-primary[type="submit"]');
        }
        if (!$btn.length) {
            $btn = $form.find('[type="submit"]');
        }
        if (!$btn.length) {
            $btn = $form.find('.btn-primary');
        }
        
        if (!$btn.length) {
            return null;
        }
        if ($btn.hasClass('st-processing')) {
            return null;
        }
        $btn.addClass('st-processing').addClass('st-processing-wide');
        interval = setInterval(function() {
            if ($btn.hasClass('st-processing-wide')) {
                console.log('add narrow class');
                $btn.addClass('st-processing-narrow');
                $btn.removeClass('st-processing-wide');
            } else {
                console.log('make wide');
                $btn.addClass('st-processing-wide');
                $btn.removeClass('st-processing-narrow');
            }
        }, 600);
        return {
            clear: function() {
                $btn.removeClass('st-processing-narrow').removeClass('st-processing-wide').removeClass('st-processing');
                clearInterval(interval);
            }
        };
    };

    st.getCookie = function(name) {
        try {
            var value = "; " + document.cookie;
        } catch (e) {
            console.log("error in stallion.getCookie()", e);
            return "";
        }
        var parts = value.split("; " + name + "=");
        if (parts.length == 2) return parts.pop().split(";").shift();
    };

    st.queryParams = function() {
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
    };
    
    st.defaultRequestErrorHandler = function(o, form, xhr) {
        st.showError(o.message, form);
    };

    st.showError = function(msg, target, noFade) {
        msg = msg.replace(/</g, '&lt;');
        msg = msg.replace(/>/g, '&gt;');
        msg = msg.replace(/\n/g, '<br>');
        if (target) {
            var $target = $(target);
            var $errorWrap = $target.find('.st-error-wrap');
            if (!$errorWrap.length) {
                var $node = $('<div></div>');
                $node.addClass('st-error-wrap');
                $target.prepend($node);
                $errorWrap = $node;
            }
            $errorWrap.html("<div style='' class='alert alert-danger st-error pre-fade'><div class='alert-icon-wrapper'><span class='alert-icon-inner material-icons'></span></div><div class='alert-message'>" + msg + "</div></div>");
            setTimeout(function() {
                $('.st-error.pre-fade').removeClass('pre-fade');
            }, 30);       
            
        } else {
            var top = calcTopForMessageFixed();
            var $div = $('<div class="alert alert-danger st-error st-message-fixed pre-fade"></div>')
                .css({right: '20px', top: top + 'px', position: 'fixed'})
                .html("<div class='alert-icon-wrapper'><span class='alert-icon-inner material-icons'></span></div><div class='alert-message'>" + msg + "</div><span class='close-alert-wrap'><span class='material-icons'>close</span></span>");
            $(document.body).append($div);
            $div.find('.close-alert-wrap').click(function() {
                $div.remove();
            });
            setTimeout(function() {
                $('.st-error.pre-fade').removeClass('pre-fade');
            }, 30);
            if (!noFade) {
                setTimeout(function() { $div.fadeOut(2000); }, 4000);
                setTimeout(function() { $div.remove(); }, 6000);
            }
        }

    };

    function calcTopForMessageFixed() {
        var top = $(window).height();
        // If there is no HTML5 doctype, then window.height is the document height, and we do not want to use it, need to put
        // it in the top right corner instead
        if (($(document).height() - 100) < top && top > 700) {
            top = 100;
        }
        console.log('top ', top);
        $others = $('.st-message-fixed');
        if ($others.length) {
            var minTop = top;
            $others.each(function(i) {
                var eleTop = parseInt($(this).css('top'), 10);
                if (eleTop < minTop) {
                    minTop = eleTop;
                }
            });
            top = minTop;
        }
        top = top - 100;
        console.log('top again ', top);
        if (top < 0) {
            top = 0;
        }
        return top;
    }

    st.showSuccess = function(msg, target) {
        msg = msg.replace(/</g, '&lt;');
        msg = msg.replace(/>/g, '&gt;');
        msg = msg.replace(/\n/g, '<br>');
        
        var $target = null;
        if (target) {
            $target = $(target);
            var $successWrap = $target.find('.st-success-wrap');
            if (!$successWrap.length) {
                var $node = $('<div></div>');
                $node.addClass('st-success-wrap');
                $target.prepend($node);
                $successWrap = $node;
            }
            $successWrap.html("<div style='' class='alert alert-success st-success pre-fade'><div class='alert-message'>" + msg + "</div></div>");
            setTimeout(function() {
                $('.st-success.pre-fade').removeClass('pre-fade');
            }, 30);       
        } else {
            var top = calcTopForMessageFixed();            
            var $div = $('<div class="alert alert-success st-success st-message-fixed"></div>')
                .css({right: '20px', top: top + 'px', position: 'fixed'})
                .html("<div class='alert-icon-wrapper'><span class='alert-icon-inner material-icons'></span></div><div class='alert-message'>" + msg + "</div><span class='close-alert-wrap'><span class='material-icons'>close</span></span>");
            $(document.body).append($div);
            $div.find('.close-alert-wrap').click(function() {
                $div.remove();
            });
            if (!noFade) {
                setTimeout(function() { $div.fadeOut(2000); }, 4000);
                setTimeout(function() { $div.remove(); }, 6000);
            }
        }
    };

    st.debounce = function(func, wait, immediate) {
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
    };
    
    st.slugify = function(text) {
        return text.toString().toLowerCase()
            .replace(/\s+/g, '-')           // Replace spaces with -
            .replace(/[^\w\-]+/g, '')       // Remove all non-word chars
            .replace(/\-\-+/g, '-')         // Replace multiple - with single -
            .replace(/^-+/, '')             // Trim - from start of text
            .replace(/-+$/, '');            // Trim - from end of text
    };
    
    st.generateUUID = function() {
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
    };

    st.toTitleCase = function(str) {
        return str.replace(/\w\S*/g, function(txt){return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();});
    }
    
    
    
}());



(function($)
{
    /**
     * Auto-growing textareas; technique ripped from Facebook
     *
     *
     * http://github.com/jaz303/jquery-grab-bag/tree/master/javascripts/jquery.autogrow-textarea.js
     */
    var autoGrow = function(options, query)
    {
        var query = query || this;
        return query.filter('textarea').each(function()
        {
            var self         = this;
            var $self        = $(self);
            var minHeight    = $self.height();
            var noFlickerPad = $self.hasClass('autogrow-short') ? 0 : parseInt($self.css('lineHeight')) || 0;
            var settings = $.extend({
                preGrowCallback: null,
                postGrowCallback: null
              }, options );

            var shadow = $('<div></div>').css({
                position:    'absolute',
                top:         -10000,
                left:        -10000,
                width:       $self.width(),
                fontSize:    $self.css('fontSize'),
                fontFamily:  $self.css('fontFamily'),
                fontWeight:  $self.css('fontWeight'),
                lineHeight:  $self.css('lineHeight'),
                resize:      'none',
    			'word-wrap': 'break-word'
            }).appendTo(document.body);

            var update = function(event)
            {
                var times = function(string, number)
                {
                    for (var i=0, r=''; i<number; i++) r += string;
                    return r;
                };

                var val = self.value.replace(/</g, '&lt;')
                                    .replace(/>/g, '&gt;')
                                    .replace(/&/g, '&amp;')
                                    .replace(/\n$/, '<br/>&nbsp;')
                                    .replace(/\n/g, '<br/>')
                                    .replace(/ {2,}/g, function(space){ return times('&nbsp;', space.length - 1) + ' ' });

				// Did enter get pressed?  Resize in this keydown event so that the flicker doesn't occur.
				if (event && event.data && event.data.event === 'keydown' && event.keyCode === 13) {
					val += '<br />';
				}

                shadow.css('width', $self.width());
                shadow.html(val + (noFlickerPad === 0 ? '...' : '')); // Append '...' to resize pre-emptively.

                var newHeight=Math.max(shadow.height() + noFlickerPad, minHeight);
                if(settings.preGrowCallback!=null){
                  newHeight=settings.preGrowCallback($self,shadow,newHeight,minHeight);
                }

                $self.height(newHeight);

                if(settings.postGrowCallback!=null){
                  settings.postGrowCallback($self);
                }
            }

            $self.change(update).keyup(update).keydown(update);
            $(window).resize(update);

            update();
        });
    };

    stallion.autoGrow = autoGrow;
    if ($.fn) {
        $.fn.autogrow = autoGrow;
    }

})(jQuery);






/*
 * Date Format 1.2.3
 * (c) 2007-2009 Steven Levithan <stevenlevithan.com>
 * MIT license
 *
 * Includes enhancements by Scott Trenda <scott.trenda.net>
 * and Kris Kowal <cixar.com/~kris.kowal/>
 *
 * Accepts a date, a mask, or a date and a mask.
 * Returns a formatted version of the given date.
 * The date defaults to the current date/time.
 * The mask defaults to dateFormat.masks.default.
 */

var dateFormat = function () {
    var token = /d{1,4}|m{1,4}|yy(?:yy)?|([HhMsTt])\1?|[LloSZ]|"[^"]*"|'[^']*'/g,
        timezone = /\b(?:[PMCEA][SDP]T|(?:Pacific|Mountain|Central|Eastern|Atlantic) (?:Standard|Daylight|Prevailing) Time|(?:GMT|UTC)(?:[-+]\d{4})?)\b/g,
        timezoneClip = /[^-+\dA-Z]/g,
        pad = function (val, len) {
            val = String(val);
            len = len || 2;
            while (val.length < len) val = "0" + val;
            return val;
        };

    // Regexes and supporting functions are cached through closure
    return function (date, mask, utc) {
        var dF = dateFormat;

        // You can't provide utc if you skip other args (use the "UTC:" mask prefix)
        if (arguments.length == 1 && Object.prototype.toString.call(date) == "[object String]" && !/\d/.test(date)) {
            mask = date;
            date = undefined;
        }

        // Passing date through Date applies Date.parse, if necessary
        date = date ? new Date(date) : new Date;
        if (isNaN(date)) throw SyntaxError("invalid date");

        mask = String(dF.masks[mask] || mask || dF.masks["default"]);

        // Allow setting the utc argument via the mask
        if (mask.slice(0, 4) == "UTC:") {
            mask = mask.slice(4);
            utc = true;
        }

        var _ = utc ? "getUTC" : "get",
            d = date[_ + "Date"](),
            D = date[_ + "Day"](),
            m = date[_ + "Month"](),
            y = date[_ + "FullYear"](),
            H = date[_ + "Hours"](),
            M = date[_ + "Minutes"](),
            s = date[_ + "Seconds"](),
            L = date[_ + "Milliseconds"](),
            o = utc ? 0 : date.getTimezoneOffset(),
            flags = {
                d:    d,
                dd:   pad(d),
                ddd:  dF.i18n.dayNames[D],
                dddd: dF.i18n.dayNames[D + 7],
                m:    m + 1,
                mm:   pad(m + 1),
                mmm:  dF.i18n.monthNames[m],
                mmmm: dF.i18n.monthNames[m + 12],
                yy:   String(y).slice(2),
                yyyy: y,
                h:    H % 12 || 12,
                hh:   pad(H % 12 || 12),
                H:    H,
                HH:   pad(H),
                M:    M,
                MM:   pad(M),
                s:    s,
                ss:   pad(s),
                l:    pad(L, 3),
                L:    pad(L > 99 ? Math.round(L / 10) : L),
                t:    H < 12 ? "a"  : "p",
                tt:   H < 12 ? "am" : "pm",
                T:    H < 12 ? "A"  : "P",
                TT:   H < 12 ? "AM" : "PM",
                Z:    utc ? "UTC" : (String(date).match(timezone) || [""]).pop().replace(timezoneClip, ""),
                o:    (o > 0 ? "-" : "+") + pad(Math.floor(Math.abs(o) / 60) * 100 + Math.abs(o) % 60, 4),
                S:    ["th", "st", "nd", "rd"][d % 10 > 3 ? 0 : (d % 100 - d % 10 != 10) * d % 10]
            };

        return mask.replace(token, function ($0) {
            return $0 in flags ? flags[$0] : $0.slice(1, $0.length - 1);
        });
    };
}();

// Some common format strings
dateFormat.masks = {
    "default":      "ddd mmm dd yyyy HH:MM:ss",
    shortDate:      "m/d/yy",
    mediumDate:     "mmm d, yyyy",
    longDate:       "mmmm d, yyyy",
    fullDate:       "dddd, mmmm d, yyyy",
    shortTime:      "h:MM TT",
    mediumTime:     "h:MM:ss TT",
    longTime:       "h:MM:ss TT Z",
    isoDate:        "yyyy-mm-dd",
    isoTime:        "HH:MM:ss",
    isoDateTime:    "yyyy-mm-dd'T'HH:MM:ss",
    isoUtcDateTime: "UTC:yyyy-mm-dd'T'HH:MM:ss'Z'"
};

// Internationalization strings
dateFormat.i18n = {
    dayNames: [
        "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat",
        "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
    ],
    monthNames: [
        "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
        "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"
    ]
};

// For convenience...
Date.prototype.format = function (mask, utc) {
    return dateFormat(this, mask, utc);
};

/******************
* End dateFormat 1.2.3   *
*******************/


/****
* Riot databind mixin
*****/

 var RiotDataBindMixin = {
     init: function() {
         this.riotDataBindMounted = false;

         this.on('before-mount', function() {
             if (!this.formData) {
                 this.formData = this.opts.formData || {};
             }

         });

         this.on('mount', function() {
             this.syncToDom();
             this.riotDataBindMounted = true;
         });

         this.syncToDom = function(triggerChange) {
             var self = this;
             if (self.formData === undefined) {
                  self.formData = self.opts.formData || {};
             }

             var seenKeys = [];
             Object.keys(self).forEach(function(key) {
                 if (key === 'scheduled') {

                 }
                 if (seenKeys.indexOf(key) > -1) {
                     return;
                 }
                 seenKeys.push(key);
                 var ele = self[key];
                 if (!ele) {
                     return;
                 }
                 var val = self.formData[key];
                 if (self.tags[key] && self.tags[key].setFormData) {
                     self.tags[key].setFormData(val);
                     return;
                 }
                 //if (ele.length) {
                 //    debugger;
                 //}
                 if ($.isArray(ele) && ele.length > 0 && ele[0].tagName) {
                     ele.forEach(function(radio) {
                         if (radio.value === val) {
                             $(radio).prop('checked', true);
                         } else if (radio.value === 'false' && val === false) {
                             $(radio).prop('checked', true);
                         } else if (radio.value === 'true' && val === true) {
                             $(radio).prop('checked', true);
                         }
                                 
                     });
                     return;
                 }
                 var tag = ele.tagName;
                 if (tag === undefined || tag === null) {
                     return;
                 }
                 tag = tag.toUpperCase();
                 if (tag !== 'INPUT' && tag !== 'TEXTAREA' && tag !== 'SELECT') {
                     return;
                 }
                 if (val === undefined) {
                     return;
                 }
                 var type = ele.getAttribute('type');
                 if (type === 'checkbox' || type === 'radio') {
                     if (val === true || val === ele.value) {
                         ele.setAttribute('checked', 'checked');
                     } else {
                         ele.removeAttribute('checked');
                     }
                 } else if (tag === 'SELECT') {
                     var $ele = $(ele);
                     $option = $ele.find('option[value="' + val + '"]');
                     $option.attr('selected', 'selected');
                     $(ele).val(val);
                 } else {
                     $(ele).val(val);
                 }
                 if (triggerChange) {
                     $(ele).change();
                 }

             });
         };



     },
     processForm: function() {
         this.formData = this.getFormData();
         return this.formData;
     },
     getFormData: function() {
         var self = this;
         var data = {};
         Object.keys(self).forEach(function(key) {
             var ele = self[key];
             if (!ele) {
                 return;
             }
             if (self.tags[key] && self.tags[key].getFormData) {
                 data[key] = self.tags[key].getFormData();
                 return;
             }

             
             if ($.isArray(ele) && ele.length > 0 && ele[0].tagName) {
                 ele.forEach(function(radio) {
                     if ($(radio).is(':checked')) {
                         data[key] = radio.value;
                         return false;
                     }
                 });
                 return;
             }
             var tag = ele.tagName;
             if (tag === undefined || tag === null) {
                 return;
             }
             tag = tag.toUpperCase();
             if (tag !== 'INPUT' && tag !== 'TEXTAREA' && tag !== 'SELECT') {
                 return;
             }
             var type = ele.getAttribute('type');
             var $ele = $(ele);
             if (type === 'checkbox' || type === 'radio') {
                 var checked = $ele.is(':checked');
                 var val = $ele.val();
                 if (checked) {
                     if (val && val != 'true') {
                         data[key] = val;
                     } else {
                         data[key] = true;
                     }
                 } else {
                     data[key] = false;
                 }
             } else {
                 data[key] = $(ele).val();
             }

         });
         return data;
     },
     setFormData: function(formData) {
         if (this.riotDataBindMounted) {
             this.updateData(formData);
         } else {
             this.formData = formData;
         }
     },
     updateData: function(formData) {
         this.update({formData: formData});
         this.syncToDom();
     },
     updateFormData: function(formData) {
         this.updateData(formData);
     }

 };

 if (window.riot) {
     riot.mixin('databind', RiotDataBindMixin);
     riot.mixin('bound-form', RiotDataBindMixin);
 }




