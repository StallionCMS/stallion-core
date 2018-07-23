<style>
 
</style>

<template>
    <div class="users-table-vue">
        <div>
            <nav class="breadcrumb" aria-label="breadcrumbs">
                <ul>
                    <li class="is-active"><a href="#" aria-current="page">All Users</a></li>
                </ul>
            </nav>            
        </div>
        <div class="p">
            <b-table :data="users" hoverable is-striped striped @click="onRowClick">
                <template slot-scope="props">
                    <b-table-column field="username" label="Username" >
                        {{ props.row.username }}
                    </b-table-column>
                    <b-table-column field="displayName" label="Display Name">
                        {{ props.row.displayName }}
                    </b-table-column>
                    <b-table-column field="email" label="Email">
                        {{ props.row.email }}
                    </b-table-column>
                    <b-table-column field="role" label="Role">
                        {{ props.row.role }}
                    </b-table-column>                    
                    <b-table-column field="createdAt" label="Created At">
                        {{ formatCreatedAt(props.row.createdAt) }}
                    </b-table-column>                    
                </template>
            </b-table>
<!--
<el-table
    :data="users"
    stripe
    border
    style="width: 100%"
    @row-click="onRowClick"
    >
    <el-table-column
      prop="username"
      label="Username"
      width="180">
    </el-table-column>
    <el-table-column
      prop="displayName"
      label="Display Name"
      width="180">
    </el-table-column>
    <el-table-column
      prop="email"
      label="Email">
    </el-table-column>
    <el-table-column
      prop="role"
      label="Role">
    </el-table-column>
    <el-table-column
      prop="createdAt"
      label="Created"
      :formatter="formatCreatedAt"
      >
    </el-table-column>    
    
</el-table>
 -->            
        </div>
    </div>
</template>

<script>
module.exports = {
    data: function() {

        return {
            tableData: [],
            users: [],
            columns: [
                
            ],
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
            var that = this;
            console.log('fetchData');
            that.$stAjax({
                url: '/st-users/users-table?page=' + that.page + '&withDeleted=' + that.withDeleted,
                useDefaultCatch: true,
            }).then(function(res) {
                that.users = res.data.items;
                that.pager = res.data.pager;
                that.loading = false;
                
            });
        },
        formatCreatedAt: function(mils) {
            if (mils === 0) {
                return '';
            }
            return dateFns.format(mils, "MMM D, YYYY");
        },
        onRowClick: function(user, b, c, d) {
            window.location.hash = "#/user/" + user.id;
        },
        rowClick: function(userId) {
            window.location.hash = "#/user/" + userId;
        },
        toggleIncludeDeleted: function(evt, a, b, c) {
            var that = this;
            that.withDeleted = true;
            console.log('toggleIncludeDeleted', evt, a, b, c);
            that.fetchData();
        }
    }
}
</script>



