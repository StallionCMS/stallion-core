
print("Begin load of plugin.js");

print('stallion', stallion);

(function() {
    var jsPlugin = {};


    load(myContext.pluginFolder + '/included.js');

    var pathParam = stallion.pathParam;
    var queryParam = stallion.queryParam;
    var bodyParam = stallion.bodyParam;
    var longParam = stallion.longParam;
    var objectParam = stallion.objectParam;
    var mapParam = stallion.mapParam;
    

    var assertEqual = stallion.assertEqual;
    var assertTrue = stallion.assertTrue;



    var LongCol = stallion.LongCol;
    var StringCol = stallion.StringCol;
    var BooleanCol = stallion.BooleanCol;    
    var ListCol = stallion.ListCol;
    var MapCol = stallion.MapCol;
    var DateTimeCol = stallion.DateTimeCol;
    var FunctionCol = stallion.FunctionCol;

    var Log = stallion.Log;


var endpoints = {
    meta: {
        baseRoute: '/js-plugin-test',
        defaultRole: 'anon',
        defaultProduces: 'application/json'
    },
    helpers: {
        route: '/helpers',
        handler: function() {
            assertTrue(212, stallion.JSON.parseMap(stallion.JSON.stringify({foo: 212})).foo);
            assertTrue(stallion.DateUtils.mils() > 1000);
            assertEqual("me", stallion.Sanitize.stripAll("<div>me</div>"));
            assertTrue("<h1>hey</h1>", stallion.Markdown.instance().process("# hey"));
            stallion.Log.finest("I am logging");
            assertEqual('123abc', stallion.Encrypter.decryptString("mykey", stallion.Encrypter.encryptString('mykey', '123abc')));
            assertEqual('something', new stallion.SimpleTemplate("{ foo.bar }", {foo: {bar: 'something'}}).render());
            assertEqual('my-slug', stallion.GeneralUtils.slugify('My Slug'));

        }
    },
    user: {
        route: '/my-context',
        handler: function() {
            //javaToJsHelpers.inspect(myContext);
            assertEqual('', myContext.user.email);
            assertEqual('someSettingValue', myContext.pluginSettings.someSettingName);
            myContext.response.setStatus(200);
            assertEqual(200, myContext.response.status);
            assertEqual('/js-plugin-test/my-context', myContext.request.path);
        }
    },
    returnJson: {
        route: '/return-json/:something',
        method: 'POST',
        params: [pathParam('something')],
        handler: function(something) {
            return {
                'fooList': ['alpha', 'beta'],
                'fooMap': {
                    'subItem': 'subValue'
                },
                'something': something,
                'five-hundred': 500
            };
        }
    },
    params: {
         route: '/exercise-params/:pathFirst/:pathSecond',
         method: 'POST',
         params: [
             longParam('pathSecond'),
             pathParam('pathFirst'),
             queryParam('query'),
             queryParam('limit', Long.class),
             bodyParam('fooList')
             ],
         handler: function(pathSecond, pathFirst, query, limit, fooList) {
             var o = {};
             assertEqual(1234, pathSecond);
             assertEqual('the-first-thing', pathFirst);
             assertEqual('searchterm', query);
             assertEqual(50, limit);
             assertEqual('bar', fooList[0]);
             assertEqual('jabber', fooList[1]);
         }
    },
    templating: {
        route: '/render-template/:thing',
        params: [pathParam('thing')],
        produces: 'text/html',
        handler: function(thing) {
            return stallion.renderTemplate('js-plugin-template.jinja', {'foo': thing});
        }
    },
    testTester: {
        route: '/test-tester/:thing',
        method: 'POST',
        params: [pathParam('thing'), bodyParam('foo')],
        handler: function(thing, foo) {
            return {yourThing: thing, yourFoo: foo};
        }
    }

};

stallion.registerEndpoints(endpoints);

var TomlItem = Java.type('io.stallion.dal.file.TomlItem');
var TomlItemController = Java.type('io.stallion.dal.file.TomlItemController');
var TomlPersister = Java.type('io.stallion.dal.file.TomlPersister');
var JsonFilePersister = Java.type('io.stallion.dal.file.JsonFilePersister');

var tomlThings = stallion
    .modelRegistration()
    .modelClass(TomlItem)
    .controllerBase(TomlItemController)
    .controllerExtensions({
        onPreSavePrepare: function(o) {
            o.touchedAt = stallion.DateUtils.mils();
            o.marking = 'foobar';
        }
    })
    .persister(TomlPersister.class)
    .bucket('js-toml-things')
    .register();

var item1 = new TomlItem();
item1.name = "jonas";
item1.id = 2000;
tomlThings.save(item1);

var rowThings = stallion
    .modelRegistration()
    .columns({
        title: new StringCol(),
        description: new StringCol(),
        listed: new BooleanCol(),
        owner_ids: new ListCol()
    })
    .persister(JsonFilePersister)
    .bucket('js-row-things')
    .register();




(function registerAsync() {

    var MyAsyncHandler = Java.extend(Java.type('io.stallion.plugins.javascript.JsAsyncTaskHandler'), {
        getHandlerClassName: function() {
            return 'myAsyncHandler';
        },
        processData: function(data) {
            Log.info("This: {0}", data);
            Log.info("Async task with value:" + data.value);
            assertEqual('someValue', data.value);
        }
    });

    stallion.registerAsyncHandler(MyAsyncHandler.class);
    var async = new MyAsyncHandler();
    async.value = 'someValue';
    stallion.submitAsync(async);
})();


(function registerJobs() {
    var JobRunner = Java.extend(Java.type('io.stallion.jobs.Job'), {
        execute: function() {
            stallion.Log.info("Job is running!");
        }
    });


    var JobDefinition = Java.type('io.stallion.jobs.JobDefinition');
    var Schedule = Java.type('io.stallion.jobs.Schedule');



    var jobDefinition = new JobDefinition()
        .setName("js-job")
        .setSchedule(Schedule.hourly())
        .setJobClass(JobRunner.class);

    stallion.registerJob(jobDefinition);

})();

(function registerHooks() {

    var PostRequestHandler = Java.type('io.stallion.requests.PostRequestHookHandler');

    stallion.registerHook(PostRequestHandler, {
        handleRequest: function(request, response) {
            response.addHeader('x-js-plugin-header', 'my-header');
        }
    });

})();


print("completed load of test plugin.js");



})();
