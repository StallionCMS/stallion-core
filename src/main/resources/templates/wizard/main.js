print("Running main.js");

/**
* HTML screens
*/
(function() {

    var screens = {
        home: {
            route: '/',
            produces: 'text/html',
            role: 'anon',
            params: [stallion.queryParam("name")],
            handler: function(name) {
                var ctx = {
                    name: name || "world"
                };
                return stallion.renderTemplate("app.jinja", ctx);
            }
        }
    };

    stallion.registerEndpoints(screens);

}());