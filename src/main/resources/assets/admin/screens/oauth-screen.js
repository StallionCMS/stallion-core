(function() {

    var screen = {};
    window.stallion = window.stallion || {};
    window.stallion.loginScreen = screen;

    screen.init = function() {
        $('#st-login-form').submit(screen.submit);
    };

    screen.submit = function(event) {
        event.preventDefault();
        var formEle = this;
        var data = stallion.formToData(formEle);

        stallion.request({
            url: '/st-users/submit-login',
            method: 'POST',
            data: data,
            form: formEle,
            success: function(o) {
                window.location.href = stallion.queryParams().stReturnUrl;
            }
        });

        return false;
    };


    $(document).ready(screen.init);

}());

