"use strict";
var dashboardappApp = angular.module('dashboardappApp', [ 'ui.router', 'ngAnimate', 'ui.load', 'ngSanitize', 'ngCookies', 'ui.bootstrap', 'ncy-angular-breadcrumb', 'ngRetina', 'toastr', 'NgSwitchery', 'textAngular', 'angularFileUpload']);

dashboardappApp
   
    .constant('baseUrl', /10|localhost/.test(location.hostname) ? 'http://localhost:8081' : '')

    /* Config Block */
    .config(
    [ '$provide', '$httpProvider', 'baseUrl',

        function ($provide, $httpProvider, baseUrl) {
          
          // Intercept http calls.
          $provide.factory('MyHttpInterceptor', [ '$cookies', '$q', function ($cookies, $q) {
            return {
              // On request success
              request: function (config) {
                if ($cookies.token !== undefined) {
                  // console.log("cookiesProvides.token is ", $cookies.token)
                  $httpProvider.defaults.headers.common['Authorization'] = 'Basic ' + $cookies.token
                } 
                // console.log(config); // Contains the data about the request before it is sent.
                if (/^\/v1\//.test(config.url)){
                  config.crossOrigin = true;
                  // config.xhrFields || (config.xhrFields = { withCredentials: false });
                  config.url = baseUrl + config.url;
                }
                // Return the config or wrap it in a promise if blank.
                return config || $q.when(config);
              },

              // On request failure
              requestError: function (rejection) {
                //console.log('request failure', rejection); // Contains the data about the error on the request.

                // Return the promise rejection.
                return $q.reject(rejection);
              },

              // On response success
              response: function (response) {
                if (/\/v1\//.test(response.config.url)){
                  //console.log(response.config);
                  return response || $q.when(response);
                }
                // Return the response or promise.
                return response || $q.when(response);
              },

              // On response failure
              responseError: function (rejection) {
                //console.log('response failure', rejection); // Contains the data about the error.

                // Return the promise rejection.
                return $q.reject(rejection);
              }
            }
          }])

          // Add the interceptor to the $httpProvider.
          $httpProvider.interceptors.push('MyHttpInterceptor')

    }])

    /* Breadcrumbs options */
    .config(function($breadcrumbProvider) {
        $breadcrumbProvider.setOptions({
            prefixStateName: 'auth.home',
            templateUrl: 'tmpls/partials/breadcrumbs.html'
        })
    })

    /* bootstrap-ui tooltips */
    .config(function($tooltipProvider ) {
        $tooltipProvider.options({
            appendToBody: true
        })
    })

    /* Run Block */
    .run(
    [ '$rootScope', '$state', '$stateParams', '$cookies', 
        function ($rootScope, $state, $stateParams, $cookies) {

            $rootScope.$state = $state;
            $rootScope.$stateParams = $stateParams;

            var is_logged_in = function(){
              return !!$cookies.token && !!$cookies.username;
            }

            $rootScope.$on('$stateChangeSuccess', function () {
                // scroll view to top
                $("html, body").animate({ scrollTop: 0 }, 200)
                // fastclick (eliminate the 300ms delay between a physical tap and the firing of a click event on mobile browsers)
                FastClick.attach(document.body)
            })

            $rootScope.$on('$stateChangeStart', function (event, toState, toParams, fromState, fromParams) {

                var requiresLogin = toState.data.requiresLogin,
                    requiresAdminPriviledge = toState.data.requiresAdminPriviledge,
                    requiresLogout = toState.data.requiresLogout;

                if (requiresLogin === true && !is_logged_in()) {
                    event.preventDefault()
                    $state.go('login')
                }
                
                else if (requiresLogout === true && is_logged_in()) {
                    event.preventDefault()
                    $state.go('auth.home')
                }

                // remove datatables fixedHeader from DOM
                if($(".FixedHeader_Cloned").length) {
                    $(".FixedHeader_Cloned").remove()
                }
                // remove daterangepicker element from DOM
                if($(".daterangepicker").length) {
                    $(".daterangepicker").remove()
                }
                // remove autosize element from DOM
                if($("#autosizejs").length) {
                    $("#autosizejs").remove()
                }
                // remove select2-hidden-accessible
                if($(".select2-hidden-accessible").length) {
                    $('.select2-hidden-accessible').remove()
                }

            })

            $rootScope.isTouchDevice = !!('ontouchstart' in window);
            $rootScope.isHighDensity = function () {
                return ((window.matchMedia && (window.matchMedia('only screen and (min-resolution: 124dpi), only screen and (min-resolution: 1.3dppx), only screen and (min-resolution: 48.8dpcm)').matches || window.matchMedia('only screen and (-webkit-min-device-pixel-ratio: 1.3), only screen and (-o-min-device-pixel-ratio: 2.6/2), only screen and (min--moz-device-pixel-ratio: 1.3), only screen and (min-device-pixel-ratio: 1.3)').matches)) || (window.devicePixelRatio && window.devicePixelRatio > 1.3))
            }

            $rootScope.appVer = 'v1.0';

            // Main menu
            $rootScope.sideMenuAct = false;
            $rootScope.topMenuAct = true;
            $rootScope.fixedLayout = true;
            
        }
    ])


    /* filters */
    // https://github.com/angular-ui/ui-utils
    .filter('unique', ['$parse', function ($parse) {
        return function (items, filterOn) {
            if (filterOn === false) {
                return items;
            }
            if ((filterOn || angular.isUndefined(filterOn)) && angular.isArray(items)) {
                var newItems = [],
                    get = angular.isString(filterOn) ? $parse(filterOn) : function (item) {
                        return item;
                    };
                var extractValueToCompare = function (item) {
                    return angular.isObject(item) ? get(item) : item;
                };
                angular.forEach(items, function (item) {
                    var isDuplicate = false;
                    for (var i = 0; i < newItems.length; i++) {
                        if (angular.equals(extractValueToCompare(newItems[i]), extractValueToCompare(item))) {
                            isDuplicate = true;
                            break;
                        }
                    }
                    if (!isDuplicate) {
                        newItems.push(item);
                    }
                });
                items = newItems;
            }
            return items;
        };
    }])