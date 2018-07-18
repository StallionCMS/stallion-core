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

