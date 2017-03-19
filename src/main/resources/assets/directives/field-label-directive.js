
(function() {

    Vue.directive('field-label', {
        bind: function(el, binding) {
            console.log('bound field label');
            $(el).addClass('moving-field');
        },
        inserted: function(el, binding) {
            // binding.value
            var $el = $(el);
            var $label = $('<label class="moving-field-label"></label>');
            $label.html(binding.value);
            $label.insertBefore(el);
            
            function addClassesForValue() {
                if (!$el.val()) {
                    $el.addClass('empty');
                    $label.addClass('empty');
                    $el.removeClass('nonempty');
                    $label.removeClass('nonempty');
                } else {
                    $el.removeClass('empty');
                    $label.removeClass('empty');
                    $el.addClass('nonempty');
                    $label.addClass('nonempty');
                }
            }
            
            $el.blur(function() {
                $el.removeClass('focused');
                $label.removeClass('focused');
                $el.addClass('blurred');
                $label.addClass('blurred');
                addClassesForValue();
            });
            
            $el.focus(function() {
                $el.addClass('focused');
                $label.addClass('focused');
                $el.removeClass('blurred');
                $label.removeClass('blurred');
            });
            
            $el.change(addClassesForValue);
            addClassesForValue();
        }
    });
    
}());
