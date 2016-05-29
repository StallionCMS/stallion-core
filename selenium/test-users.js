
var driver = SeleniumContext.driver;
var helper = SeleniumContext.helper;
var By = org.openqa.selenium.By;

var Unirest = com.mashape.unirest.http.Unirest;

runner.addSuite('user-management', function() {
    var suite = {counter: 0};

    /*
    suite.testAddUser = function() {
        suite.addUser();
    };

    suite.addUser = function() {

    };
    */
    
    suite.testUserManagement = function() {
        suite.login(helper.baseUrl + '/st-admin/users/manage', '.user-row');

        // TODO -- add the user first
        
        //helper.waitExists('.user-row');
        var userRow = driver.findElement(By.cssSelector('.user-row'));
        var id = userRow.getAttribute('data-user-id');
        userRow.click();
        helper.waitExists('#st-update-user-form .st-button-submit');

        
        var newName = 'new-name-' + new Date().getTime() * 100 + suite.counter + '';
        driver.executeScript('jQuery(\'input[name="displayName"]\').val("")');

        driver.findElement(By.name('displayName')).sendKeys(newName);
        driver.findElement(By.cssSelector('.st-submit-and-return')).click();

        var row = driver.findElement(By.cssSelector('tr.user-row-' + id));
        helper.assertHasText(row, newName);
        
    };

    suite.login = function(url, selector) {
        var email = 'selenium@stallion.io';
        var password = 'Qp7c0sBMul3A';
        
        driver.get(url);
        helper.waitExists('#st-login-form');
        driver.findElement(By.name('username')).sendKeys(email);
        driver.findElement(By.name('password')).sendKeys(password);
        driver.findElement(By.cssSelector('.st-button-submit')).click();
        helper.waitExists(selector);
        

    };
    
    return suite;

}());

runner.addSuite('user-public-pages', function() {

    var suite = {counter: 0};
    
    suite.testLogin = function() {
        var email = 'selenium@stallion.io';
        var password = 'Qp7c0sBMul3A';
        suite.login(email, password, 'admin');
    };

    suite.login = function(email, password, role) {
        driver.get(helper.baseUrl + "/st-admin/users/login?stReturnUrl=/st-admin/users/login");
        driver.findElement(By.name('username')).sendKeys(email);
        driver.findElement(By.name('password')).sendKeys(password);
        driver.findElement(By.cssSelector('.st-button-submit')).click();
        // Thanks to stReturnUrl, we redirect right back to the login page where the following text should appear.
        helper.waitTextExists('#st-login-form', email);
        helper.waitTextExists('#st-login-form', role);
        helper.waitTextExists('#st-login-form', "You are currently logged in as " + email + " with role level " + role);
    };

    suite.testRegister = function() {
        suite.register();        
    };

    suite.testPasswordReset = function() {
        // Create a user
        var mils = new Date().getTime() * 100 + suite.counter;
        var email = 'selenium+resettest+' + mils + '@stallion.io';
        var user = suite.register(email);

        // Trigger a password reset email
        driver.get(helper.baseUrl + "/st-admin/users/reset-password");
        driver.findElement(By.name('email')).sendKeys(user.email);
        driver.findElement(By.cssSelector('.st-button-submit')).click();
        helper.waitExists('.st-success');
        helper.waitTextExists('.st-success', 'Password reset email sent');
        
        // Get the password reset token
        var res = Unirest.get(helper.baseUrl + "/_stx/selenium/get-reset-token")
            .queryString("email", user.email)
            .queryString("secret", "miuLa90ljgM5")
            .asJson();
        var token = res.getBody().getObject().get('resetToken');


        // Actually reset the password, we chould get sent back to the login page
        var url = helper.baseUrl + "/st-admin/users/reset-password?resetToken=" + token + "&email=" + user.email.replace(/\+/g, "%2B") + "&returnUrl=/st-admin/users/login";
        driver.get(url);
        var newPassword = 'newPassword' + mils;
        driver.findElement(By.name('password')).sendKeys(newPassword);        
        driver.findElement(By.name('passwordConfirm')).sendKeys(newPassword);
        driver.findElement(By.cssSelector('.st-button-submit')).click();
        helper.waitTextExists('#st-login-form', user.email);
        helper.waitTextExists('#st-login-form', 'member');
        
        print('logoff');
        // Log off
        driver.get(helper.baseUrl + '/st-admin/users/logoff?t=' + mils);
        helper.waitExists('#st-login-form');
        helper.waitNotExists('.logged-in-as-box');

        print('login with new password');

        // Login with the new password
        suite.login(user.email, newPassword, 'member');
        

        
    };

    suite.testVerifyEmail = function() {
        var user = suite.register();
        driver.get(helper.baseUrl + "/st-admin/users/verify-email");
        driver.findElement(By.cssSelector('.st-button-submit')).click();
        helper.waitExists('.st-success');
        helper.waitTextExists('.st-success', user.email);
        helper.waitTextExists('.st-success', 'verification email has been sent');
        
    };

    suite.register = function(email) {
        driver.get(helper.baseUrl + "/st-admin/users/register?stReturnUrl=/st-admin/users/login");
        suite.counter++;
        var mils = new Date().getTime() * 100 + suite.counter;
        email = email || 'selenium+' + mils + '@stallion.io';
        var user = {
            email: email,
            displayName: 'Selenium ' + mils,
            password: 'Password1-' + mils
        };
        
        driver.findElement(By.name('username')).sendKeys(user.email);
        driver.findElement(By.name('displayName')).sendKeys(user.displayName);
        driver.findElement(By.name('password')).sendKeys(user.password);        
        driver.findElement(By.name('passwordConfirm')).sendKeys(user.password);
        driver.findElement(By.cssSelector('.st-button-submit')).click();
        // Thanks to stReturnUrl, we redirect right back to the login page where the following text should appear.
        helper.waitTextExists('#st-login-form', user.email);
        helper.waitTextExists('#st-login-form', "member");
        helper.waitTextExists('#st-login-form', "You are currently logged in as " + user.email + " with role level member");
        
        
        return user;
        
    };

    return suite;
    
}());





runner.run();
