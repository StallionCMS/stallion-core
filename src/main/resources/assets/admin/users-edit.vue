<style>
    
</style>

<template>
    <div>
        <div v-if="loading">Loading user …</div>
        <div>
            <a href="#/">&#171; return to all users</a>
        </div>
        <div class="pure-g"  v-if="!loading">
            <div class="pure-u-2-3">
                <h3>Edit user: {{user.email}} - {{user.displayName}}</h3>
                <form id="st-update-user-form" name="updateUserForm" class="pure-form pure-form-stacked" v-on:submit.prevent="submit">
                    <fieldset>
                        <div class="st-bottom-space">
                            <label for="displayName">Display Name</label>
                            <input v-model="user.displayName" name="displayName" type="text" class="pure-input-1">
                        </div>
                        <div class="st-bottom-space">
                            <label for="givenName">Given Name</label>
                            <input v-model="user.givenName" name="givenName" type="text" class="pure-input-1">
                        </div class="st-bottom-space">
                        <div class="st-bottom-space">
                            <label for="familyName">Family Name</label>
                            <input v-model="user.familyName" name="familyName" type="text" class="pure-input-1">
                        </div>
                        <div class="st-bottom-space">
                            <label for="email">Email</label>
                            <input v-model="user.email" name="email" type="email" placeholder="Email" class="pure-input-1">
                        </div>
                        <div class="st-bottom-space">
                            <label for="email">Username</label>
                            <input v-model="user.username" name="username" type="text" class="pure-input-1">
                        </div>
                        <div class="st-bottom-space">
                            <label for="state">Role</label>
                            <select v-model="user.role" name="role" class="pure-input-1">
                                <option value="ANON">ANON</option>
                                <option value="CONTACT">CONTACT</option>
                                <option value="REGISTERED">REGISTERED</option>
                                <option value="MEMBER">MEMBER</option>
                                <option value="STAFF_LIMITED">STAFF_LIMITED</option>
                                <option value="STAFF">STAFF</option>
                                <option value="ADMIN">ADMIN</option>
                            </select>
                        </div>
                        <p>
                            <button type="submit" class="st-button-submit  pure-button pure-button-primary">Save changes</button>
                            <button v-on:click="submitAndReturn" value="save-and-return" type="submit" class="st-button-submit st-submit-and-return  pure-button pure-button-primary">Save and return</button>
                        </p>
                    </fieldset>
                </form>        
            </div>
            <div class="pure-u-1-3 user-management-actions">
                <h4>Actions</h4>
                <div v-if="!resetSent && !user.deleted">
                    <button v-on:click="forcePasswordReset" title="Will null-out the users password and send them an email asking them to reset it." class="pure-button">Force password reset.</button>
                </div>
                <div v-if="resetSent && !user.deleted">
                    Password reset!
                </div>
                <div v-if="!user.disabled">
                    <button v-on:click="disableUser" class="pure-button">Disable user</button>
                </div>
                <div v-if="user.disabled">
                    <button v-on:click="enableUser" class="pure-button">Enable user</button>
                </div>
                <div v-if="!user.approved">
                    <button v-on:click="approveUser" class="pure-button">Approve user</button>
                </div>
                <div v-if="user.approved">
                    <button v-on:click="unapproveUser" class="pure-button">Un-approve user</button>
                </div>
                <div v-if="!user.deleted">
                    <button v-on:click="deleteUser" class="pure-button">Delete user</button>
                </div>
                <div v-if="user.deleted">
                    <button v-on:click="restoreUser" class="pure-button">Restore user</button>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
module.exports = {
    data: function() {
        console.log('dafadsf');
        return {
            loading: false,
            user: {},
            resetSent: false
        }
    },
    created: function() {
        console.log('users edit created');
        this.updateFromRoute();
    },
    watch: {
        '$route': function(to, from) {
            this.updateFromRoute();
        }
    },
    methods: {
        updateFromRoute: function() {
            var self = this;
            this.loading = true;
            this.page = this.$route.params.page || 1;
            this.user = [];
            console.log('route changed!!!');
            stallion.request({
                url: '/st-users/view-user/' + self.$route.params.userId,
                success: function(user) {
                    self.user = user;
                    self.loading = false;
                }
            });
        },
        submit: function(evt, callback) {
            var self = this;
            var fields = ['displayName', 'givenName', 'familyName', 'email', 'username', 'role'];
            var data = {};
            fields.forEach(function(field) {
                data[field] = self.$data.user[field];
            });
            stallion.request({
                url: '/st-users/update-user/' + self.$data.user.id,
                method: 'POST',
                data: data,
                //form: self.updateUserForm,
                success: function(user) {
                    if (callback) {
                        callback();
                    }
                }
            });
        },
        submitAndReturn: function() {
            console.log('submitAndReturn');
            this.submit(null, function() {
                window.location.hash = "/";
            });
            //debugger;
        },
        forcePasswordReset: function() {
            var self = this;
            console.log('forcePasswordReset');
            stallion.request({
                url: '/st-users/force-password-reset/' + self.user.id,
                method: 'POST',
                success: function(o) {
                    self.resetSent = true;
                }
            });
            
        },
        approveUser: function() {
            var self = this;
            console.log('approveUser');
            stallion.request({
                url: '/st-users/toggle-user-approved/' + self.user.id,
                method: 'POST',
                data: {approved: true},
                success: function(o) {
                    self.user.approved = true;
                }
            });
            
        },
        unapproveUser: function() {
            var self = this;
            console.log('unapproveUser');
            stallion.request({
                url: '/st-users/toggle-user-approved/' + self.user.id,
                method: 'POST',
                data: {approved: false },
                success: function(o) {
                    self.user.approved = false;
                }
            });
            
        },
        disableUser: function() {
            var self = this;
            console.log('disableUser');
            stallion.request({
                url: '/st-users/toggle-user-disabled/' + self.user.id,
                method: 'POST',
                data: {disabled: true},
                success: function(o) {
                    self.user.disabled = true;
                }
            });
            
        },
        enableUser: function() {
            var self = this;
            console.log('enableUser');
            stallion.request({
                url: '/st-users/toggle-user-disabled/' + self.user.id,
                method: 'POST',
                data: {disabled: false },
                success: function(o) {
                    self.user.disabled = false;
                }
            });
        },
        deleteUser: function() {
            var self = this;
            console.log('deleteUser');
            stallion.request({
                url: '/st-users/toggle-user-deleted/' + self.user.id,
                method: 'POST',
                data: {deleted: true },
                success: function(o) {
                    self.user.deleted = true;
                }
            });
            
        },
        restoreUser: function() {
            var self = this;
            console.log('restoreUser');
            stallion.request({
                url: '/st-users/toggle-user-deleted/' + self.user.id,
                method: 'POST',
                data: {deleted: false },
                success: function(o) {
                    self.user.deleted = false;
                }
            });
            
        }
    }
}
</script>



