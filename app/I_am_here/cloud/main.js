/*global Parse:false, $:false, jQuery:false */

// Importas
var _ = require('underscore'); // jshint ignore:line
var moment = require('moment'); // jshint ignore:line

// Constants
var sessionObjName = "Session";
var sessionLifetimeSec = 30;

var channelName = "events";
var publishKey = "pub-c-6271f363-519a-432d-9059-e65a7203ce0e",
    subscribeKey = "sub-c-a3d06db8-410b-11e5-8bf2-0619f8945a4f",
    httpRequestUrl = 'http://pubsub.pubnub.com/publish/' + publishKey + '/' + subscribeKey + '/0/' + channelName + '/0/';


// Utils
function Log(obj, tag) {
    "use strict";

    var loggingString = "Cloud_code: ";
    if (tag != null) { // jshint ignore:line
        loggingString += "[" + tag + "] ";
    }
    loggingString += obj + "\n";

    console.log(loggingString); // jshint ignore:line
}

function GetNow() {
    "use strict";
    return moment.utc();
}

// Supporting
var baseSession = {udid: "", loginedAt: GetNow(), aliveTo: GetNow()};

var errorHandler = function(error) {
    "use strict";
    Log(error.message, "error");
};
var baseHandler = {
    success: function(){},
    error: errorHandler
};

function DeleteDeadSessions() {
    "use strict";

    var query = new Parse.Query(sessionObjName); // jshint ignore:line
    query.lessThanOrEqualTo("aliveTo", GetNow())
        .each(function(obj)
         {
             Log(obj, "Deleting dead session");
             obj.destroy();
         },
        baseHandler
    );
}

function NewSession(udid) {
    "use strict";

    var session = _.clone(baseSession);
    session.udid = udid;
    session.loginedAt = GetNow();
    session.aliveTo = GetNow().add({seconds: sessionLifetimeSec});

    return session;
}

function NewParseObject(session) {
    "use strict";

    var obj = Parse.Object.extend(sessionObjName); // jshint ignore:line
    Log(obj);
    obj.set({
        udid: session.udid,
        loginedAt: session.loginedAt,
        aliveTo: session.aliveTo
        },
        {
            error: errorHandler
        }
    );

    return obj;
}

function SendEvent(udid) {
    "use strict";

    Parse.Cloud.httpRequest({ // jshint ignore:line
        url: httpRequestUrl + JSON.stringify({udid: udid}),

        success: function(httpResponse) {
            Log(httpResponse.text);
        },

        error: function(httpResponse) {
            Log('Request failed with response code ' + httpResponse.status);
        }
    });
}

// API functions
var API_GetNow = function(request, response) {
    "use strict";

    response.success( GetNow() );
};

var API_GetOnlineUsers = function(request, response) {
    "use strict";

    DeleteDeadSessions();
    var query = new Parse.Query(sessionObjName); // jshint ignore:line

    response.success( query.toJSON() );
};

var API_IsUserOnline = function(request, response) {
    "use strict";

    var userUdid = request.object.data.udid;
    var query = new Parse.Query(sessionObjName) // jshint ignore:line
        .equalTo("udid", userUdid)
        .lessThanOrEqualTo("aliveTo", GetNow());


    var userOnline = true;
    query.count( {
        success: function(number) {
            if (number === 0) {
                userOnline = false;
            }
        },
        error: errorHandler
    });

    response.success(userOnline);
};

var API_GetUserSession = function(request, response) {
    "use strict";

    var userUdid = request.object.get("data").udid;
    var query = new Parse.Query(sessionObjName); // jshint ignore:line
    var promise = query.first("udid", userUdid);
    promise.then(function(result) {
        response.success(result);
    });

    response.error();
};

var API_Login = function(request, response) {
    "use strict";

    var userUdid = request.params.data.udid;
    var session = NewSession(userUdid);
    var parseObject = NewParseObject(session);

    parseObject.save().then(
        function(success){},
        function(error) {

        }
    );
    Log(parseObject, "API_Login - parseObject");

    // TODO: make UDID unique (+ can't login if already loginned)

    response.success( JSON.stringify(parseObject) );
};

var API_Logout = function(request, response) {
    "use strict";

    var userUdid = request.object.get("udid");
    var query = new Parse.Query(sessionObjName) // jshint ignore:line
        .equalTo("udid", userUdid);

    query.each( function(obj) {
       obj.destroy();
    });

    response.success();
};


// Bindings
Parse.Cloud.beforeSave(sessionObjName, function(request, response) { // jshint ignore:line
    "use strict";


    // TODO: not correct. Need another method. 3 types of sessions exist: NULL | DEAD | ALIVE
    var userUdid = request.object.get("udid");
    Parse.Cloud.run("IsUserOnline", {data:{udid:userUdid}}, { // jshint ignore:line
        success: response.success(),
        error: response.error()
    });
});

Parse.Cloud.afterSave(sessionObjName, function(request) { // jshint ignore:line
    "use strict";

	SendEvent(request.object.get("udid"));
});

Parse.Cloud.afterDelete(sessionObjName, function(request) { // jshint ignore:line
    "use strict";

    SendEvent(request.object.get("udid"));
});


// API definitions
Parse.Cloud.define("GetNow", API_GetNow); // jshint ignore:line

Parse.Cloud.define("IsUserOnline", API_IsUserOnline); // jshint ignore:line
Parse.Cloud.define("GetOnlineUsers", API_GetOnlineUsers); // jshint ignore:line
Parse.Cloud.define("GetUserSession", API_GetUserSession); // jshint ignore:line

Parse.Cloud.define("Login", API_Login); // jshint ignore:line
Parse.Cloud.define("Logout", API_Logout); // jshint ignore:line