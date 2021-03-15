
(function() {

    window.StallionPublicPages = {};
    var pp = window.StallionPublicPages;

    Vue.use(StallionUtilsVuePlugin);


    pp.loadLoginPage = function() {
        
        
        new Vue({
            el: '#layout',
            data: function() {
                return {
                    username: '',
                    password: '',
                    rememberMe: false
                }
            },
            created: function() {
                
            },
            methods: {
                onLoginSubmit: function() {
                    var that = this;
                    this.$stAjax({
                        url: '/st-users/submit-login',
                        method: 'POST',
                        lock: 'signin',
                        useDefaultCatch: true,
                        data: {
                            username: that.username,
                            password: that.password,
                            rememberMe: that.rememberMe
                        }
                    }).then(function(res) {
                        var o = res.data;
                        if (!o.approved && !o.emailVerified) {
                            window.location.href = "/st-users/verify-email";
                        } else if (!o.approved) {
                            that.$message({
                                message: "Your login was successful, but your account is pending administrative approval.",
                                type: 'warning'
                            });
                        } else if (GeneralHelpers.queryParams().stReturnUrl) {
                            window.location.href = GeneralHelpers.queryParams().stReturnUrl;
                        } else {
                            window.location.href = "/";
                        }
                    });
                }
            }
              
            
        });
    }

    pp.loadRegisterPage = function() {
        new Vue({
            el: '#layout',
            data: function() {
                return {
                    email: ''
                }
            },
            methods: {
                
            }
        });
    };

    pp.loadVerifyEmailPage = function(opts) {
        new Vue({
            el: '#layout',
            data: function() {
                return {
                    email: opts.email,
                    alreadySent: opts.alreadySent === true
                }
            },
            methods: {
                requestEmail: function() {
                    var that = this;
                    this.$stAjax({
                        url: '/st-users/send-verify-email',
                        method: 'POST',
                        data: {
                            email: that.email
                        },
                        useDefaultCatch: true,
                        lock: 'sendverify'
                    }).then(function(res) {
                        that.alreadySent = true;
                    });
                },
            }
        });
    };

    pp.loadPasswordResetPage = function(opts) {
        new Vue({
            el: '#layout',
            data: function() {
                return {
                    email: opts.email,
                    password: '',
                    passwordConfirm: '',
                    token: '',
                    emailSent: false    
                }
            },
            methods: {
                sendResetEmail: function() {
                    var that = this;
                    this.$stAjax({
                        url: '/st-users/send-reset-email',
                        method: 'POST',
                        data: {
                            email: that.email
                        },
                        lock: 'sendreset'
                    }).then(function(res) {
                        that.emailSent = true;
                        that.$message("Reset requested.");
                    });
                },
                submitNewPassword: function() {
                    var that = this;
                    this.$stAjax({
                        url: '/st-users/do-password-reset',
                        method: 'POST',
                        data: {
                            email: that.email,
                            password: that.password,
                            passwordConfirm: that.passwordConfirm,
                            resetToken: GeneralHelpers.queryParams().resetToken
                        },
                        lock: 'submitnewpassword'
                    }).then(function(res) {
                        var returnUrl = GeneralHelpers.queryParams().stReturnUrl || GeneralHelpers.queryParams().returnUrl || "/";
                        window.location.href = returnUrl;
                    });
                }
            }

        });
    };

}());
