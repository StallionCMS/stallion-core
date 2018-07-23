<style lang="scss">
 .user-management-actions {
     
     
     button {
         display: block;
         width: 95%;
         max-width: 300px;
         margin-bottom: 1em;
         
     }
 }
</style>

<template>
    <div>
        <div v-if="loading">Loading user …</div>
        <div>
            <nav class="breadcrumb" aria-label="breadcrumbs">
                <ul>
                    <li><a href="/st-users/manage" aria-current="page">All Users</a></li>
                    <li v-if="user && user.id" class="is-active"><a href="'#/users/' + User.id">{{ user.username }}</a></li>
                </ul>
            </nav>            
        </div>
        <div class="pure-g columns"  v-if="!loading" style="margin-top: 1em;">
            <div class="pure-u-2-3 column is-two-thirds" style="padding-right: 30px;">
                <h2 class="p">Edit User <b>{{ user.displayName }}</b></h2>
                <form @submit.prevent="submit">
                    <b-field label="Display Name">
                        <b-input v-model="user.displayName"></b-input>
                    </b-field>                    


                    <b-field label="Given Name">
                        <b-input v-model="user.givenName"></b-input>
                    </b-field>                    

                    <b-field label="Family Name">
                        <b-input v-model="user.familyName"></b-input>
                    </b-field>                    

                    
                    <b-field label="Email"
                             type="email"
                             >
                        <b-input type="email"
                                 v-model="user.email"
                                 maxlength="60">
                        </b-input>
                    </b-field>

                    <b-field label="Username">
                        <b-input type="text"
                                 v-model="user.username"
                                 maxlength="60">
                        </b-input>
                    </b-field>
                    
                    <b-field label="Role">
                        <b-select placeholder="Role">
                            <option value="ANON">ANON</option>
                            <option value="CONTACT">CONTACT</option>
                            <option value="REGISTERED">REGISTERED</option>
                            <option value="MEMBER">MEMBER</option>
                            <option value="STAFF_LIMITED">STAFF_LIMITED</option>
                            <option value="STAFF">STAFF</option>
                            <option value="ADMIN">ADMIN</option>
                        </b-select>
                    </b-field>
                    
                    <p>
                        <button class="button "type="primary" native-type="submit" v-stallion-locking="'saveUser.stay'" >Save changes</button>
                        <button class="button" v-stallion-locking="'saveUser.return'" @click="submitAndReturn">Save and Return</button>
                    </p>
                </form>        
            </div>
            <div class="pure-u-1-3 user-management-actions column">
                <h4>Actions</h4>
                <div v-if="!resetSent && !user.deleted">
                    <button class="button" v-on:click="forcePasswordReset" title="Will null-out the users password and send them an email asking them to reset it." v-stallion-locking="'reset'" >Force password reset.</button>
                </div>
                <div v-if="resetSent && !user.deleted">
                    Password reset!
                </div>
                <div v-if="!user.disabled">
                    
                    <button v-on:click="disableUser" class="button" v-stallion-locking="'disable'">Disable user</button>
                </div>
                <div v-if="user.disabled">
                    <button v-on:click="enableUser" class="button" v-stallion-locking="'disable'">Enable user</button>
                </div>
                <div v-if="!user.approved">
                    <button v-on:click="approveUser" class="button" v-stallion-locking="'approve'">Approve user</button>
                </div>
                <div v-if="user.approved">
                    <button v-on:click="unapproveUser" class="button" v-stallion-locking="'approve'">Un-approve user</button>
                </div>
                <div v-if="!user.deleted">
                    <button v-on:click="deleteUser" class="button" v-stallion-locking="'delete'">Delete user</button>
                </div>
                <div v-if="user.deleted">
                    <button v-on:click="restoreUser" class="button" v-stallion-locking="'delete'">Restore user</button>
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
            var that = this;
            this.loading = true;
            this.page = this.$route.params.page || 1;
            this.user = [];
            console.log('route changed!!!');
            that.$stAjax({
                url: '/st-users/view-user/' + that.$route.params.userId,
                useDefaultCatch: true
            }).then(function(res) {
                    that.user = res.data;
                    that.loading = false;
            });
        },
        submit: function(evt, lock, callback) {
            var that = this;
            lock = lock || 'saveUser.stay'; 
            var fields = ['displayName', 'givenName', 'familyName', 'email', 'username', 'role'];
            var data = {};
            fields.forEach(function(field) {
                data[field] = that.$data.user[field];
            });
            that.$stAjax({
                url: '/st-users/update-user/' + that.$data.user.id,
                method: 'POST',
                data: data,
                lock: lock,
                useDefaultCatch: true
            }).then(function(res) {
                if (callback) {
                    callback();
                }                
            });
        },
        submitAndReturn: function() {
            console.log('submitAndReturn');
            this.submit(null, 'saveUser.return', function() {
                window.location.hash = "/";
            });
            //debugger;
        },
        forcePasswordReset: function() {
            var that = this;
            console.log('forcePasswordReset');
            this.$stAjax({
                url: '/st-users/force-password-reset/' + that.user.id,
                method: 'POST',
                useDefaultCatch: true,
                lock: 'reset'
            }).then(function(res) {
                that.resetSent = true;
            });
            
        },
        approveUser: function() {
            var that = this;
            console.log('approveUser');
            this.$stAjax({
                url: '/st-users/toggle-user-approved/' + that.user.id,
                method: 'POST',
                data: {approved: true},
                useDefaultCatch: true,
                lock: 'approve'
            }).then(function(res) {
                that.user.approved = true;
            });
            
        },
        unapproveUser: function() {
            var that = this;
            console.log('unapproveUser');
            this.$stAjax({
                url: '/st-users/toggle-user-approved/' + that.user.id,
                method: 'POST',
                data: {approved: false },
                useDefaultCatch: true,
                lock: 'approve'
            }).then(function(res) {
                that.user.approved = false;
            });
            
        },
        disableUser: function() {
            var that = this;
            console.log('disableUser');
            this.$stAjax({
                url: '/st-users/toggle-user-disabled/' + that.user.id,
                method: 'POST',
                data: {disabled: true},
                useDefaultCatch: true,
                lock: 'disable'
            }).then(function(res) {
                that.user.disabled = true;
            });
            
        },
        enableUser: function() {
            var that = this;
            console.log('enableUser');
            this.$stAjax({
                url: '/st-users/toggle-user-disabled/' + that.user.id,
                method: 'POST',
                data: {disabled: false },
                useDefaultCatch: true,
                lock: 'disable'
            }).then(function(res) {
                that.user.disabled = false;
            });
        },
        deleteUser: function() {
            var that = this;
            console.log('deleteUser');
            this.$stAjax({
                url: '/st-users/toggle-user-deleted/' + that.user.id,
                method: 'POST',
                data: {deleted: true },
                useDefaultCatch: true,
                lock: 'delete'
            }).then(function(res) {
                that.user.deleted = true;
            });
            
        },
        restoreUser: function() {
            var that = this;
            console.log('restoreUser');
            this.$stAjax({
                url: '/st-users/toggle-user-deleted/' + that.user.id,
                method: 'POST',
                data: {deleted: false },
                useDefaultCatch: true,
                lock: 'delete'
            }).then(function(res) {
                that.user.deleted = false;
            });
            
        }
    }
}
</script>



