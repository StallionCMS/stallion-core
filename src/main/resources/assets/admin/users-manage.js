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

(function (window, document) {
var menu = document.getElementById('menu'),
    WINDOW_CHANGE_EVENT = ('onorientationchange' in window) ? 'orientationchange':'resize';

function toggleHorizontal() {
    [].forEach.call(
        document.getElementById('menu').querySelectorAll('.custom-can-transform'),
        function(el){
            el.classList.toggle('pure-menu-horizontal');
        }
    );
};

function toggleMenu() {
    // set timeout so that the panel has a chance to roll up
    // before the menu switches states
    if (menu.classList.contains('open')) {
        setTimeout(toggleHorizontal, 500);
    }
    else {
        toggleHorizontal();
    }
    menu.classList.toggle('open');
    document.getElementById('toggle').classList.toggle('x');
};

function closeMenu() {
    if (menu.classList.contains('open')) {
        toggleMenu();
    }
}

document.getElementById('toggle').addEventListener('click', function (e) {
    toggleMenu();
});

window.addEventListener(WINDOW_CHANGE_EVENT, closeMenu);
})(this, this.document);



(function() {
    var admin = {};
    var mountedTag = null;

    admin.init = function() {
        riot.compile(function() {
            riot.route.start(true);

        });
    };

    

    riot.route('/view-user/*', function(userId) {
        if (mountedTag) {
            mountedTag.unmount(true);
        }
        mountedTag = riot.mount('user-view', {title: 'View User', userId: userId})[0];
    });
    
    riot.route('/*', function(page) {
        if (mountedTag) {
            mountedTag.unmount(true);
        }
        console.log('page', page);
        mountedTag = riot.mount('users-table', {title: 'Manage Users', page: page})[0];
    });

    riot.route('/', function(page) {
        if (mountedTag) {
            mountedTag.unmount(true);
        }
        console.log('page', page);
        mountedTag = riot.mount('users-table', {title: 'Manage Users', page: 1})[0];
    });
    
    

    

    window.stUsersAdmin = admin;
})();

$(document).ready(stUsersAdmin.init);
