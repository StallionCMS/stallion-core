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
            url: '/st-users/do-register',
            method: 'POST',
            data: data,
            form: formEle,
            success: function(o) {
                if (o.requireValidEmail) {
                    window.location.href = "/st-users/verify-email?alreadySent=true&stReturnlUrl=" + encodeURIComponent(stallion.queryParams().stReturnUrl || "");
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
            url: '/st-users/submit-login',
            method: 'POST',
            data: data,
            form: formEle,
            success: function(o) {
                if (!o.approved && !o.emailVerified) {
                    window.location.href = "/st-users/verify-email";
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
            url: '/st-users/send-verify-email',
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
            url: '/st-users/send-reset-email',
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
            url: '/st-users/do-password-reset',
            method: 'POST',
            data: data,
            form: formEle,
            success: function(o) {
                var returnUrl = stallion.queryParams().stReturnUrl || stallion.queryParams().returnUrl || "/";
                window.location.href = returnUrl;
            }
        });

        return false;

    }

    $(document).ready(screen.init);
    
}());

