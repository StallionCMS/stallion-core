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
            if (ele.getAttribute("type") === 'checkbox') {
                data[name] = $(ele).is(":checked");
            } else {
                data[name] = ele.value;
            }
        });
        return data;
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
        var xsrfCookie = st.getCookie("XSF-TOKEN");
        if (xsrfCookie) {
            header["X-XSRF-TOKEN"] = xsrfCookie;
        }
        $.ajax({
            url: req.url,
            type: method.toLowerCase(),
            data: dataString,
            headers: headers,
            contentType: 'application/json',
            dataType: 'json'
        }).success(function(obj) {
            if (spinner) {
                spinner.clear();
            }
            if (req.success) {
                req.success(obj);
            }
        }).error(function(xhr) {
            if (spinner) {
                spinner.clear();
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
            req.error(o);

        });

        
    };

    st.tryMakeSpinner = function(form) {

        var $btn = $('#' + form.getAttribute('id') + ' .st-button-submit');
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
        var value = "; " + document.cookie;
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
    
    st.defaultRequestErrorHandler = function(o) {
        if (o.form) {
            var $errorWrap = $('#' + o.form.getAttribute('id') + ' .st-error-wrap');
            if (!$errorWrap.length) {
                var $form = $('#' + o.form.getAttribute('id'));
                var $node = $('<div></div>');
                $node.addClass('st-error-wrap');
                $form.appendChild($node);
                $errorWrap = $node;
            }
            $errorWrap.html("<div style='' class='alert alert-danger st-error pre-fade'>" + o.message + "</div>");
            setTimeout(function() {
                $('.st-error.pre-fade').removeClass('pre-fade');
            }, 30);       
            
        } else {
            alert(o.message);
        }
    };
    
    
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
