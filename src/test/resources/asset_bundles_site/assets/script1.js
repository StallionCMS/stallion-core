console.log('script 1');


var sampleApp = angular.module('partialApp', []);
  
sampleApp.config(
    ['$routeProvider',
     function($routeProvider) {
         $routeProvider.
             when('/hello-js', {
                 templateUrl: 'partial1.html',
                 controller: 'partial1Controller'
             }).
             otherwise({
                 redirectTo: '/hello-js'
             });
     }
    ]
);

sampleApp.controller('partial1Controller', function($scope) {
    $scope.person = 'Jon Stark';
    console.log('partial1');
});

