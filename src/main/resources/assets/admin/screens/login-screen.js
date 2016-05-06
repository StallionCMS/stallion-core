(function() {

    var screen = {};
    window.stallion = window.stallion || {};
    window.stallion.loginScreen = screen;

    screen.init = function() {
        $('#st-login-form').submit(screen.loginSubmit);
        $('#st-register-form').submit(screen.registerSubmit);
        $('#st-email-verify-form').submit(screen.verifyEmailAddressSubmit);
        $('#st-reset-password-form').submit(screen.passwordResetSubmit);
        $('#st-choose-new-password-form').submit(screen.chooseNewPasswordSubmit);
    };

    screen.registerSubmit = function(event) {
        event.preventDefault();
        var formEle = this;
        var data = stallion.formToData(formEle);
        if (stallion.queryParams().stReturnUrl) {
            data.returnUrl = stallion.queryParams().stReturnUrl;
        }
        stallion.request({
            url: '/st-admin/users/do-register',
            method: 'POST',
            data: data,
            form: formEle,
            success: function(o) {
                if (o.requireValidEmail) {
                    window.location.href = "/st-admin/users/verify-email?alreadySent=true&stReturnlUrl=" + encodeURIComponent(stallion.queryParams().stReturnUrl || "");
                } else if (stallion.queryParams().stReturnUrl) {
                    window.location.href = stallion.queryParams().stReturnUrl;
                } else {
                    $('#st-register-page').html("<div class='st-success-message'><h3>New user created.</h3><h4><a href='/'>Return to home page</a></h4></div>");
                }

                //window.location.href = stallion.queryParams().stReturnUrl || "/";
            }
        });

        return false;

    };
    
    screen.loginSubmit = function(event) {
        event.preventDefault();
        var formEle = this;
        var data = stallion.formToData(formEle);

        stallion.request({
            url: '/st-admin/users/submit-login',
            method: 'POST',
            data: data,
            form: formEle,
            success: function(o) {
                if (!o.approved && !o.emailVerified) {
                    window.location.href = "/st-admin/users/verify-email";
                } else if (!o.approved) {
                    stallion.defaultRequestErrorHandler({message: "Your login was successful, but your account is pending administrative approval."}, formEle);
                } else if (stallion.queryParams().stReturnUrl) {
                    window.location.href = stallion.queryParams().stReturnUrl;
                } else {
                    window.location.href = "/";
                }
            }
        });
        
        return false;
    };

    screen.verifyEmailAddressSubmit = function(event) {
        event.preventDefault();
        var formEle = this;
        var data = stallion.formToData(formEle);
        data.returnUrl = stallion.queryParams().stReturnUrl || "";
        stallion.request({
            url: '/st-admin/users/send-verify-email',
            method: 'POST',
            data: data,
            form: formEle,
            success: function(o) {
                $('#verify-email-sent-box').show();
                console.log("sent!")
            }
        });

        return false;

    }



    screen.passwordResetSubmit = function(event) {
        event.preventDefault();
        var formEle = this;
        var data = stallion.formToData(formEle);
        stallion.request({
            url: '/st-admin/users/send-reset-email',
            method: 'POST',
            data: data,
            form: formEle,
            success: function(o) {
                $('#reset-sent-box').show();
            }
        });

        return false;

    }



    screen.chooseNewPasswordSubmit = function(event) {
        event.preventDefault();
        var formEle = this;
        var data = stallion.formToData(formEle);
        data.resetToken = stallion.queryParams().resetToken;
        stallion.request({
            url: '/st-admin/users/do-password-reset',
            method: 'POST',
            data: data,
            form: formEle,
            success: function(o) {
                window.location.href = stallion.queryParams().stReturnUrl;
            }
        });

        return false;

    }

    $(document).ready(screen.init);
    
}());

