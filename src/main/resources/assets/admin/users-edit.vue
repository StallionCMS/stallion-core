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
            <el-breadcrumb separator="/">
                <el-breadcrumb-item :to="{ path: '/' }">All Users</el-breadcrumb-item>

                <el-breadcrumb-item v-if="user && user.id">{{ user.username }}</el-breadcrumb-item>
            </el-breadcrumb>
        </div>
        <div class="pure-g"  v-if="!loading" style="margin-top: 1em;">
            <div class="pure-u-2-3" style="padding-right: 30px;">
                <h2 class="p">Edit User <b>{{ user.displayName }}</b></h2>
                <el-form ref="form" :model="user" label-width="120px" label-position="top" @submit.native.prevent="submit" size="small">
                    <el-form-item label="Display Name">
                        <el-input v-model="user.displayName" name="displayName" type="text" required="true" ></el-input>
                    </el-form-item>
                    <el-form-item label="Given Name">
                        <el-input v-model="user.givenName" name="givenName" type="text" class="pure-input-1"></el-input>
                    </el-form-item>
                    <el-form-item label="Family Name">
                        <el-input v-model="user.familyName" name="familyName" type="text" class="pure-input-1"></el-input>
                    </el-form-item>
                    <el-form-item label="Email">
                        <el-input v-model="user.email" name="email" type="email" placeholder="Email" class="pure-input-1"></el-input>
                    </el-form-item>
                    <el-form-item label="Username">
                        <el-input v-model="user.username" name="username" type="text" class="pure-input-1"></el-input>
                    </el-form-item>
                    <el-form-item label="Role">
                        <el-select v-model="user.role" >
                            <el-option value="ANON">ANON</el-option>
                            <el-option value="CONTACT">CONTACT</el-option>
                            <el-option value="REGISTERED">REGISTERED</el-option>
                            <el-option value="MEMBER">MEMBER</el-option>
                            <el-option value="STAFF_LIMITED">STAFF_LIMITED</el-option>
                            <el-option value="STAFF">STAFF</el-option>
                            <el-option value="ADMIN">ADMIN</el-option>
                        </el-select>
                    </el-form-item>
                    <p>
                        <el-button type="primary" native-type="submit" v-stallion-locking="'saveUser.stay'" >Save changes</el-button>
                        <el-button v-stallion-locking="'saveUser.return'" @click="submitAndReturn">Save and Return</el-button>
                    </p>
                </el-form>        
            </div>
            <div class="pure-u-1-3 user-management-actions">
                <h4>Actions</h4>
                <div v-if="!resetSent && !user.deleted">
                    <el-button v-on:click="forcePasswordReset" title="Will null-out the users password and send them an email asking them to reset it." v-stallion-locking="'reset'" >Force password reset.</el-button>
                </div>
                <div v-if="resetSent && !user.deleted">
                    Password reset!
                </div>
                <div v-if="!user.disabled">
                    
                    <el-button v-on:click="disableUser" class="" v-stallion-locking="'disable'">Disable user</el-button>
                </div>
                <div v-if="user.disabled">
                    <el-button v-on:click="enableUser" class="" v-stallion-locking="'disable'">Enable user</el-button>
                </div>
                <div v-if="!user.approved">
                    <el-button v-on:click="approveUser" class="" v-stallion-locking="'approve'">Approve user</el-button>
                </div>
                <div v-if="user.approved">
                    <el-button v-on:click="unapproveUser" class="" v-stallion-locking="'approve'">Un-approve user</el-button>
                </div>
                <div v-if="!user.deleted">
                    <el-button v-on:click="deleteUser" class="" v-stallion-locking="'delete'">Delete user</el-button>
                </div>
                <div v-if="user.deleted">
                    <el-button v-on:click="restoreUser" class="" v-stallion-locking="'delete'">Restore user</el-button>
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



