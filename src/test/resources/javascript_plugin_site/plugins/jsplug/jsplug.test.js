(function() {

    var eq_ = stallion.assertEqual;
    var ok_ = stallion.assertTrue;
    var Log = stallion.Log;

    var onSetupValue = "";

    var suite = stallion.newTestSuite("jsplug.suite", {
        setUp: function(self) {
            onSetupValue = "I was set up";
        }
    });

    suite.add("endpointTest", function(self) {
        var foo = 'fighters';
        var r = suite.client.post('/js-plugin-test/test-tester/freedom', {'foo': foo});
        eq_(200, r.status);
        var o = r.asMap();
        eq_('freedom', o.yourThing);
        print("o.yourThing ", o.yourThing);
        eq_(foo, o.yourFoo);
        eq_("I was set up", onSetupValue);
    });

    suite.add("secondTest", function(self) {
        eq_("yes", "yes");
    });

    suite.run();

})();
