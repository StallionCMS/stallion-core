/***************** DEBUGGING HELPER ***********************
/** Copy and paste this to eval in local scope

while(true) { try { var input = javaToJsHelpers.getInput('> ');if (input==='!c') break;if (input==='!source') {try { throw new Error(); } catch(e) { print(e.fileName, e.lineNumber); };break;}if (input==='!stack') {try { throw new Error(); } catch(e) { print(e.lineNumber); };break;}var result = eval(input); smartPrint(result); } catch(e) { print(e)} };

***************************************************/

var Long = java.lang.Long;
var Boolean = java.lang.Boolean;
var Integer = java.lang.Integer;



print('loading stallion shared.js');


function smartPrint(result) {
    if (typeof(result) === 'string') {
        print(result);
    } else if (typeof(result) === 'number') {
        print(result);
    } else if (typeof(result) === 'boolean') {
        print(result);
    } else if (typeof(result) === 'function') {
        print(result);
    } else if (result === null) {
        print('null');
    } else if (typeof(result) === 'undefined') {
        print('undefined');
    } else if (!result instanceof Object) {
        print(result);
    } else {
        try {
            print(javaToJsHelpers.inspect(result));
        } catch (e) {
            print(result);
        }
    }
}

function jsrepl(scope) {
    scope = scope || this;
    print('---------------------');
    print('Entering debugger. Type "q" to quit.');
    var err = new Error();
    print('Current stack location: \n', err.stack);
    print('---------------------');
    scope.err = err;
    while (true) {
        var input = stallion.getInput('> ');
        if (input === 'q' || input === 'quit') {
            break;
        }
        try {
            var result = scope.eval(input);
            smartPrint(result);
        } catch (e) {
            err.printStackTrace();
        }
    }
}

if (!('contains' in String.prototype)) {
  String.prototype.contains = function(str, startIndex) {
    return ''.indexOf.call(this, str, startIndex) !== -1;
  };
}

Array.prototype.contains = function(obj) {
    var i = this.length;
    while (i--) {
        if (this[i] == obj) {
            return true;
        }
    }
    return false;
};

Array.prototype.add = function(obj) {
    this.push(obj);
};


var stallion = stallion || {};

function contains(list, o) {
    if (list === null || list === undefined) {
        return false;
    }
    var len = list.length;
    for (var i = 0; i<len; i++) {
        if (list[i] == o) {
            return true;
        }
    }
    return false;
}

originalLoad = load;

var stallionFileSourceMap = {};

var stallionGetSurroundingLines = function(file, lineno) {
    var source = stallionFileSourceMap[file];
    if (!source) {
        return;
    }
    var lines = source.split('\n');
    var begin = 0;
    if (lineno > 20) {
        begin = lineno - 20;
    }
    var end = begin + 40;
    if (end > lines.length) {
        end = lines.length;
    }
    var i = begin;
    var newLines = [];
    lines.slice(begin, end).forEach(function(line) {
        if ((lineno-1) === i) {
            newLines.push("\t\t----> YOU ARE HERE <-----\t\t");
        } else {
            newLines.push(i + "\t" + line);
        }
        i++;
    });
    return newLines.join('\n');
};

(function() {
var JsFileReader = Packages.io.stallion.plugins.javascript.JsFileReader;
var pluginFolder = myContext.pluginFolder;
loadjs = function(a, b, c) {
    var source = '';
    var fileName = '';
    if (typeof(a) === typeof("")) {
        source = JsFileReader.readToString(a, pluginFolder);
        fileName = a;
    } else {
        source = a.script;
        fileName = a.name;
    }
    source = source.replace(/[\t ]*debugger[;\t ]*\n/g, "print('Entering inspection mode. Type anything and enter to eval. Type @stack to see the call stack, @source to get the file and line number, @c to continue.');while(true) { try { var input = javaToJsHelpers.getInput('> ');if (input==='@c') break;if (input==='@source') {print(__FILE__, __LINE__);print(stallionGetSurroundingLines(__FILE__, __LINE__));break;}if (input==='@stack') {try { throw new Error(); } catch(e) { print(e.stack); };break;}var result = eval(input); smartPrint(result); } catch(e) { print(e)} };\n");
    stallionFileSourceMap[fileName] = source;
    originalLoad({script: source, name: fileName});
};

load = loadjs;

})();

(function() {





    // Exported classes

    // Helpers
    stallion.JSON = Java.type("io.stallion.utils.json.JSON");
    stallion.DateUtils = Java.type("io.stallion.utils.DateUtils");
    stallion.Sanitize = Java.type("io.stallion.utils.Sanitize");
    stallion.Markdown = Java.type("io.stallion.utils.Markdown");
    stallion.Log = Java.type("io.stallion.services.Log");
    var Log = stallion.Log;
    stallion.Encrypter = Java.type("io.stallion.utils.Encrypter");
    stallion.SimpleTemplate = Java.type("io.stallion.utils.SimpleTemplate");
    stallion.GeneralUtils = Java.type("io.stallion.utils.GeneralUtils");
    stallion.SafeMerger = Java.type('io.stallion.requests.validators.SafeMerger');
    stallion.SafeViewer = Java.type('io.stallion.requests.validators.SafeViewer');
    stallion.Literals = Java.type('io.stallion.utils.Literals');
    stallion.Set = stallion.Literals.set;
    
    // Base Classes
    stallion.Job = Java.type("io.stallion.jobs.Job");
    stallion.JobDefinition = Java.type("io.stallion.jobs.JobDefinition");
    stallion.JobSchedule = Java.type("io.stallion.jobs.Schedule");
    stallion.AsyncTaskHandler = Java.type('io.stallion.plugins.javascript.JsAsyncTaskHandler');

    // Enums
    stallion.Role = Packages.io.stallion.users.Role;

    // Exported Functions
    stallion.renderTemplate = function(template, ctx) {
        return myContext.renderTemplate(template, ctx);
    };
    stallion.raiseRedirect = function(msg, status) {
        javaToJsHelpers.raiseRedirect(msg, status || 302);
    };
    stallion.raiseClientException = function(msg, status) {
        javaToJsHelpers.raiseClientException(msg, status || 400);
    };
    stallion.raiseNotFound = function(msg) {
        javaToJsHelpers.raiseNotFound(msg);
    };
    stallion.raiseServerException = function(msg, status) {
        javaToJsHelpers.raiseServerException(msg, status || 500);
    };
    stallion.raiseAssertionError = function(msg) {
        javaToJsHelpers.raiseAssertionError(msg);
    };


    //


    var Long = java.lang.Long;
    var ArrayList = java.util.ArrayList;

    var DateTimeFormatter = Java.type("java.time.format.DateTimeFormatter");

    var DalRegistration = Java.type('io.stallion.dal.base.DalRegistration');
    var DalRegistry = Java.type('io.stallion.dal.DalRegistry');
    var DynamicModelDefinition = Java.type('io.stallion.dal.base.DynamicModelDefinition');
    var Col = Java.type("io.stallion.dal.db.Col");                                                                         var DbColumnConverter = Java.type("io.stallion.dal.db.DbColumnConverter");

    var StallionJSON = Java.type('io.stallion.utils.json.JSON');
    var StallionUtils = Java.type('io.stallion.utils.GeneralUtils');
    var EndpointsRegistry = Java.type('io.stallion.restfulEndpoints.EndpointsRegistry').instance();
    var RequestArg = Java.type("io.stallion.restfulEndpoints.RequestArg");
    var IJSHandler = Java.type('io.stallion.restfulEndpoints.JavascriptRequestHandler');

    var JsEndpoint = Java.type('io.stallion.plugins.javascript.JsEndpoint');
    var BaseModel = Java.type("io.stallion.plugins.javascript.BaseJavascriptModel");
    var BaseColumn = Java.type("io.stallion.plugins.javascript.BaseJavascriptColumn");
    var JsPojoController = Java.type("io.stallion.plugins.javascript.JsPojoController");
    var BaseDynamicColumn = Java.type("io.stallion.plugins.javascript.BaseDynamicColumn");

    var AsyncCoordinator = Java.type('io.stallion.asyncTasks.AsyncCoordinator');
    var JobCoordinator = Java.type('io.stallion.jobs.JobCoordinator');

    var Role = Java.type("io.stallion.users.Role");

    // Testing
    var JsTestSuite = Java.type('io.stallion.plugins.javascript.JsTestSuite');

    stallion.newTestSuite = function(name, extensions) {
        if (typeof(name) !== typeof("")) {
            throw "First argument to newTestSuite() must be the name of the suite.";
        }
        var extensions = extensions || {};
        var cls = Java.extend(JsTestSuite, extensions);
        var suite = new cls();
        suite.setName(name);
        suite.setFile(jsSuitesHolder.getFile());
        jsSuitesHolder.add(suite);
        return suite;
    }

    stallion.assertEqual = function(a, b, msg) {
        var msg = msg || '';
        if (a !== b) {
            stallion.raiseAssertionError("AssertionError: '" + a + "'!=='" + b + "'. The classes of a and b are: '" + typeof(a) + "' and '" + typeof(b) + "'. Additional info: " + msg);
        }
    }

    stallion.assertTrue = function(a, msg) {
        if (!a) {
            stallion.raiseAssertionError("Is not truthy: " + a + " message:" + msg);
        }
    }


    var JExtend = Java.extend;

    /***************************
    * Columns available for model properties
    /**************/

    /**
     * Common initializer for all columns
     */
    var initCol = function(colObj, type, opts) {
        if (colObj.defaultValue === undefined) {
            colObj.defaultValue = null;
        }
        colObj.insertable = null;
        colObj.updateable = null;
        colObj.type = type;
        colObj.json_include = true;
        opts = opts || {};
        if (opts.defaultValue !== null && opts.defaultValue !== undefined) {
            colObj.defaultValue = opts.defaultValue;
        }
        if (opts.uniqueKey === true) {
            colObj.uniqueKey = true;
        }
        if (opts.alternativeKey === true) {
            colObj.alternativeKey = true;
        }
        if (opts.length !== null && opts.length !== undefined) {
            colObj.length = opts.length;
        }
        if (opts.nullable !== null && opts.nullable !== undefined) {
            colObj.nullable = opts.nullable;
        }
        
        Object.keys(opts).forEach(function(key) {
            this[key] = opts[key];
        });
    };

    stallion.StringCol = function(opts) {
        initCol(this, 'string', opts);
    };

    stallion.LongCol = function(opts) {
        this.defaultValue = 0;
        initCol(this, 'long', opts);
    };

    stallion.IntegerCol = function(opts) {
        this.defaultValue = 0;
        initCol(this, 'integer', opts);
    };

    stallion.BooleanCol = function(opts) {
        this.defaultValue = false;
        initCol(this, 'boolean', opts);
    };

    stallion.EnumCol = function(opts) {
        this.defaultValue = false;
        initCol(this, 'enum', opts);
    };
    

    /**
     *
     */
    stallion.ListCol = function(opts) {
        var self = this;
        this.defaultValue = [];
        initCol(this, 'list', opts);
        this.fromDb = function(obj, val, name) {
            if (val === null || val === '') {
                return new ArrayList();
            } else {
                val = StallionJSON.parseList(val);
                if (!val) {
                    return new ArrayList();
                }
                return val;
            }
        };
        this.toDb = function(obj, val, name) {
            if (val === null) {
                val = [];
                obj.put(name, val);
            }
            return StallionJSON.stringify(val);
        };
    };

    /**
     * A column that stores an arbitrary map/dictionary of data.
     *
     * Stored in the database as a JSON string
     */
    stallion.MapCol = function(opts) {
        var self = this;
        this.defaultValue = [];
        initCol(this, 'map', opts);
        this.fromDb = function(obj, val, name) {
            if (val === null || val === '') {
                return new HashMap();
            } else {
                val = StallionJSON.parseMap(val);
                if (!val) {
                    return new HashMap();
                }
                return val;
            }
        };
        this.toDb = function(obj, val, name) {
            if (val === null) {
                val = new HashMap();
                obj.put(name, val);
            }
            return StallionJSON.stringify(val);
        };
    };


    /**
     * A column that is stored as a UTC, ZonedDateTime object
     * Stored in the database as a datetime column
     */
    stallion.DateTimeCol = function(opts) {
        opts = opts || {};
        initCol(this, 'datetime', opts);

        this.toDb = function(obj, val, name) {
            if (val=== null && opts.nowOnCreate) {
                val = StallionUtils.utcNow();
                obj.put(name, val);
            } else if (opts.nowOnUpdate) {
                val = StallionUtils.utcNow();
                obj.put(name, val);
            }
            if (val === null) {
                return val;
            }
            return val.format(StallionUtils.SQL_FORMAT);
        };
    };

    /**
     * Function column does not store anything in the database or file system.
     * Rather it accepts a function, and the function is called every time the property
     * is accessed.
     */
    stallion.FunctionCol = function(func, opts) {
        this.type = 'dynamic';
        this.func = func;
    };


    stallion.modelRegistration = function() {
        var o = {};
        var builder = {
            modelClass: function(cls) {
                o.modelClass = cls;
                return this;
            },
            controllerBase: function(cls) {
                o.controllerClass = cls;
                return this;
            },
            columns: function(columns) {
                o.columns = columns;
                return this;
            },
            controllerExtensions: function(extensions) {
                o.controllerExtensions = extensions;
                return this;
            },
            persister: function(cls) {
                o.persister = cls;
                return this;
            },
            bucket: function(bucket) {
                o.bucket = bucket;
                return this;
            },
            tableName: function(tableName) {
                o.tableName = tableName;
                return this;
            },
            path: function(path) {
                o.path = path;
                return this;
            },
            useDataDirectory: function(useDataDirectory) {
                o.useDataFolder = useDataFolder;
                return this;
            },
            register: function() {
                if (!o.bucket) {
                    if (o.path) {
                        o.bucket = o.path;
                    } else {
                        o.bucket = o.tableName;
                    }
                }
                if (!o.tableName) {
                    o.tableName = o.bucket;
                }
                if (!o.bucket) {
                    throw "Every model registration must set a path, tablename, or bucket!";
                }

                // Don't re-register a model that has already been registered
                if (DalRegistry.instance().containsKey(o.bucket)) {
                    return DalRegistry.instance().get(o.bucket);
                }

                javaToJsHelpers.isClass(o.persister);
                javaToJsHelpers.isStatic(o.persister);
                if (javaToJsHelpers.isStatic(o.persister)) {
                    o.persister = o.persister.class;
                }



                var registration = new DalRegistration()
                     .setPersisterClass(o.persister || null)
                     .setBucket(o.bucket || '')
                     .setPath(o.path || '')
                     .setTableName(o.tableName || '');
                if (o.useDataFolder !== undefined) {
                     o.setUseDataFolder(o.useDataFolder);
                }

                // Create the model class, and process any  defined database columns
                var ModelClassStatic;
                if (o.columns !== undefined) {
                    var info = columnsToModelInfo(o.tableName, o.bucket, o.modelClass, o.columns);
                    ModelClassStatic = info.modelClass;
                    registration
                        .setModelClass(info.modelClass.class)
                        .setDynamicModelDefinition(info.dynamicDefinition)
                    ;
                } else {
                    ModelClassStatic = makeModelClass(o.modelClass, o.bucket);
                    registration.setModelClass(ModelClassStatic.class);
                }

                // Create the controller class

                registration.setControllerClass(
                    makeControllerClass(
                        registration.getBucket(),
                        ModelClassStatic,
                        o.controllerClass,
                        o.controllerExtensions
                        ));

                DalRegistry.instance().registerDal(registration);
                var controller = DalRegistry.instance().get(registration.getBucket());
                return controller;
            }
        };
        return builder;
    };

    var makeControllerClass = function(bucket, ModelClassStatic, base, extensions) {
        var ModelClass = ModelClassStatic || BaseModel;
        var base = base || JsPojoController;
        var extensions = extensions || {};

        extensions.getBucketName = function() {
            return bucket;
        };

        extensions.newModel = function(o) {
            var m = new ModelClass();
            if (o !== null && o !== undefined) {
                Object.keys(o).forEach(function(name) {
                    m.put(name, o[name]);
                });
            }
            return m;
        };

        

        return JExtend(base, extensions).class;
    };

    var makeModelClass = function(baseModel, bucket) {
        var baseModel = baseModel || BaseModel;

        var cls = JExtend(baseModel, {
            getBucketName: function() {
                return bucket;
            }
        });
        return cls;
    };


    var columnsToModelInfo = function(tableName, bucket, baseModel, properties) {
        var typeMap = {};
        var jsonIgnoreColumns = [];
        var dynamicColumns = {};

        var baseModel = baseModel || BaseModel;

        var modelDef = new DynamicModelDefinition();
        modelDef.setTable(tableName);
        modelDef.columns = [];

        Object.keys(properties).forEach(function(name) {
            var prop = properties[name];
            if (prop.type == 'dynamic') {
                var DynClass = Java.extend(BaseDynamicColumn, {
                    func: prop.func
                });
                dynamicColumns[name] = new DynClass();
                return;
            }
            if (!prop.json_include) {
                jsonIgnoreColumns.push(name);
            }

            if (prop.insertable !== true && prop.insertable !== false) {
                prop.insertable = true;
            }
            if (prop.updateable !== true && prop.updateable !== false ) {
                prop.updateable = true;
            }
            typeMap[name] = prop.type;

            var converter = null;
            if (prop.fromDb || prop.toDb) {
                var methods = {};
                if (prop.fromDb) {
                    methods.fromDb = prop.fromDb;
                }
                if (prop.toDb) {
                    methods.toDb = prop.toDb;
                }
                var converterCls = JExtend(DbColumnConverter, methods);
                converter = new converterCls();
            }


            var col = new Col()
                .setName(prop.columnName || name)
                .setPropertyName(name)
                .setAlternativeKey(prop.alternativeKey === true)
                .setUniqueKey(prop.uniqueKey === true)
                .setUpdateable(prop.updateable)
                .setInsertable(prop.insertable)
                .setConverter(converter)
                .setDefaultValue(prop.defaultValue);
            if (prop.length) {
                col.setLength(prop.length);
            }
            if (prop.nullable === false) {
                col.setNullable(false);
            }
            modelDef.columns.push(col);
        });


        var baseModel = baseModel || BaseModel;

        var ModelClass = JExtend(BaseModel, {
            getBucketName: function() {
                return bucket;
            },
            getTypeMap: function() {
                return typeMap;
            },
            getJsonIgnoredColumns: function() {
                return jsonIgnoreColumns;
            },
            getDynamicProperties: function() {
                return dynamicColumns;
            }
        });
        modelDef.setModelClass(ModelClass.class);
        return {
            modelClass: ModelClass,
            dynamicDefinition: modelDef
        };
    };


    stallion.registerEndpoints = function(endpoints) {

        var meta = endpoints.meta || {};

        Object.keys(endpoints).forEach(function(key) {
            if (key === 'meta') {
                return;
            }
            var endpoint = endpoints[key];
            var handler = JExtend(IJSHandler, {
                handler: endpoint.handler
            });
            var route = endpoint.route;
            if (meta.baseRoute) {
                route = meta.baseRoute + route;
            }
            var jsEndpoint = new JsEndpoint(route, endpoint.method || 'GET');
            if (endpoint.produces) {
                jsEndpoint.setProduces(endpoint.produces);
            } else if (meta.defaultProduces) {
                jsEndpoint.setProduces(meta.defaultProduces);
            }
            jsEndpoint.setScope(endpoint.scope || "");
            var role = endpoint.role || meta.defaultRole || 'STAFF';


            jsEndpoint.setRole(Role.valueOf(role.toUpperCase()));
            if (endpoint.route.indexOf(':') > -1 && !endpoint.params) {
                throw "Your route " + endpoint.route + "has a param in it, but the endpoint has no params: [pathParam('...')] propeerty.";
            }
            if (endpoint.checkXSRF !== null && endpoint.checkXSRF !== undefined) {
                jsEndpoint.setCheckXSRF(endpoint.checkXSRF);
            }
            if (endpoint.params) {
                endpoint.params.forEach(function(param) {
                    Log.info("param: name={0} cls={1}", param.name, param.cls);
                    jsEndpoint.getArgs().add(
                        new RequestArg()
                            .setName(param.name)
                            .setType(param.type)
                            .setTargetClass(param.cls || null)
                    );
                });
            }
            jsEndpoint.setHandler(wrapHandler(endpoint.handler));
            EndpointsRegistry.addEndpoints(jsEndpoint);
        });
    };

    var wrapHandler = function(handler) {
        return function() {
            
            var result = handler.apply(this, arguments);
            //return result;
            result = javaToJsHelpers.toJava(result);
            //while(true) { try { var evalInput = javaToJsHelpers.getInput('> ');if (evalInput==='c') {break};var evalTheResult = eval(evalInput); smartPrint(evalTheResult); } catch(e) { print(e)} };                        
            return result;
        };
    };
    
    var AsyncCoordinator = Java.type('io.stallion.asyncTasks.AsyncCoordinator');
    stallion.registerAsyncHandler = function(cls) {
        if (javaToJsHelpers.isStatic(cls)) {
            cls = cls.class;
        }
        AsyncCoordinator.instance().registerHandler(cls);
    };

    stallion.submitAsync = function(o) {
        AsyncCoordinator.instance().enqueue(o);
    };

    var JobCoordinator = Java.type('io.stallion.jobs.JobCoordinator');
    stallion.registerJob = function(definition) {
        JobCoordinator.instance().registerJob(definition);
    };

    var HookRegistry = Java.type("io.stallion.hooks.HookRegistry");
    stallion.registerHook = function(parent, methods) {
        if (!javaToJsHelpers.isStatic(parent)) {
            stallion.raiseServerException("You must pass in the static class to registerHook ( registerHook(MyClass) not registerHook(MyClass.class)");
        }
        var cls = JExtend(parent, methods);
        HookRegistry.instance().register(new cls());
    }



    stallion.pathParam = function(name, cls) {
        return {
            type: 'PathParam',
            name: name,
            cls: cls
        };
    };

    stallion.stringParam = function(name) {
        return {
            type: 'PathParam',
            name: name
        };
    };

    stallion.intParam = function(name) {
        return {
            type: 'PathParam',
            name: name,
            cls: Long.class
        };
    };

    stallion.longParam = function(name) {
        return {
            type: 'PathParam',
            name: name,
            cls: Long.class
        };
    };


    stallion.bodyParam = function(name, cls) {
        return {
            type: 'BodyParam',
            name: name,
            cls: cls
        };
    };

    stallion.queryParam = function(name, cls) {
        return {
            type: 'QueryParam',
            name: name,
            cls: cls
        };
    };

    stallion.objectParam = function(name, cls) {
        return {
            type: 'ObjectParam',
            name: name,
            cls: cls
        };
    };

    stallion.mapParam = function() {
        return {
            type: 'MapParam',
            name: 'data',
            cls: null
        };
    };





}());
