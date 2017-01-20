<style>
 
</style>

<template>
    <div class="users-table-vue">
        <h3>All Users</h3>
        <table class="pure-table users-table">
            <thead>
                <tr>
                    <th>
                        Username
                    </th>
                    <th>
                        Display name
                    </th>
                    <th>
                        Email
                    </th>
                    <th>
                        Role
                    </th>
                    <th>
                        Created
                    </th>
                    <th>
                        Status
                    </th>
                </tr>
            </thead>
            <tbody v-if="loading">
                <tr>
                    <td colspan="6">Loading users…</td>
                </tr>
            </tbody>
            <tbody v-if="!loading && users.length === 0"">
                <tr>
                    <td colspan="6">No users found</td>
                </tr>
            </tbody>
            <tbody v-if="!loading && users.length > 0">
                <tr v-for="user in users""  :class="['clickable-row', 'user-row', 'user-row-' + user.id]" v-on:click="rowClick(user.id)" :data-user-id="user.id">
                    <td>{{user.username}}</td>
                    <td>{{user.displayName}}</td>
                    <td>{{user.email}}</td>
                    <td>{{user.role}}</td>
                    <td>{{formatCreatedAt(user.createdAt)}}</td>
                    <td class="user-status">
                        <span v-if="!user.deleted && !user.disabled && user.approved && !user.predefined">normal</span>
                        <span v-if="user.deleted">deleted</span>
                        <span v-if="user.predefined">built-in user</span>
                        <span v-if="user.disabled">disabled</span>
                        <span v-if="!user.approved">pending approval</span>
                    </td>
                </tr>
            </tbody>
            <tfoot v-if="pager"">
                <tr>
                    <td colspan="6" v-if="pager.pageCount > 0">
                        <a v-bind:class="{'pager-link-text': true, 'pager-link': true, 'current-page': page==1}" href="#/1">First</a>
                        <a v-for="num in pager.surroundingPages" :href="'#/' + num" v-bind:class="{'pager-link': true, 'current-page': num==page}">
                            {{num}}
                        </a>
                        <a v-bind:class="{'pager-link-text': true, 'pager-link': true, 'current-page': page==pager.pageCount}" :href="'#/' +pager.pageCount">Last</a>
                    </td>
                </tr>
                <tr>
                    <td colspan="6">
                        <label><input type="checkbox" id="include-deleted" @click="toggleIncludeDeleted"> Show deleted users?</label>
                    </td>
                </tr>
            </tfoot>
        </table>
    </div>
</template>

<script>
module.exports = {
    data: function() {

        return {
            users: [],
            pager: null,
            page: 1,
            withDeleted: false,
            loading: true
        };
    },
    watch: {
        '$route': function(to, from) {
            this.updateFromRoute();
        }
    },
    created: function() {
        console.log('created', this.$route);
        this.updateFromRoute();
    },
    methods: {
        updateFromRoute: function() {
            this.loading = true;
            this.page = this.$route.params.page || 1;
            this.user = [];
            console.log('route changed!!!');
            this.fetchData();
        },
        fetchData: function() {
            var self = this;
            console.log('fetchData');
            stallion.request({
                url: '/st-users/users-table?page=' + self.page + '&withDeleted=' + self.withDeleted,
                success: function (o) {
                    self.users = o.items;
                    self.pager = o;
                    self.loading = false;
                }
            });
        },
        formatCreatedAt: function(mils, format) {
            var format = format || "mmm d, yyyy";
            if (mils === 0) {
                return '';
            }
            return dateFormat(new Date(mils), format);
        },
        rowClick: function(userId) {
            window.location.hash = "#/edit-user/" + userId;
        },
        toggleIncludeDeleted: function(evt, a, b, c) {
            var self = this;
            self.withDeleted = true;
            console.log('toggleIncludeDeleted', evt, a, b, c);
            self.fetchData();
        }
    }
}
</script>



