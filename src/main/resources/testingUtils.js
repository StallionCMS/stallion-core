var StallionTestSuite = function() {
        var self = this;
        self.tests = [];
        self.add = function(testObject) {
            for (var name in testObject) {
                if (testObject.hasOwnProperty(name)) {
                    self.tests.push(testObject[name]);
                }
            }
        };

        self.run = function() {
            var results = {
                passed: 0,
                failed: 0,
                ran: 0
            };

            self.tests.forEach(function(func) {
                //try {
                    results.ran++;
                    func();
                    results.passed++;
                //} catch(e) {
                 //   print(e);
                 //   results.failed++;
                //}
            });
            return results;
        };

        self.printResults = function(results) {
            print('Ran: ' + results.ran);
            print('Passed: ' + results.passed);
            print('Failed: ' + results.failed);
        };

        self.runAndReport = function() {
            var results = self.run();
            self.printResults(results);
            if (results.failed > 0) {
                throw "JS tests failed: " + results.failed;
            }
        };
    };

var eq_ = function(a, b) {
        if (a !== b) {
            throw "NotEqual: \n" + a + "\n" + b + "\nEndNotEqual";
        }
    };

var ok_ = function(a, msg) {
        msg = msg || "NotTruthy!";
        if (!a) {
            throw msg;
        }
    };

