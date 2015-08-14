/*global Parse:false, $:false, jQuery:false */

// Importas
var _ = require('underscore'); // jshint ignore:line
var moment = require('moment'); // jshint ignore:line

// Constants
var sessionObjName = "IMH_Session";
var sessionLifetimeSec = 61;

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
    loggingString += JSON.stringify(obj) + "\n";

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

function GetSessionQuery()
{
    "use strict";
    var objConstructor = Parse.Object.extend(sessionObjName); // jshint ignore:line
    return new Parse.Query(objConstructor);
}

function NewParseObject(session) {
    "use strict";

    var objConstructor = Parse.Object.extend(sessionObjName); // jshint ignore:line
    var obj = new objConstructor();

    obj.set({
        udid: session.udid,
        loginedAt: session.loginedAt.toDate(),
        aliveTo: session.aliveTo.toDate()
        }
    );
    Log(obj, "func- NewParseObject");

    return obj;
}

function SendEvent(udid) {
    "use strict";

    Parse.Cloud.httpRequest({ // jshint ignore:line
        url: httpRequestUrl + JSON.stringify({udid: udid}),

        success: function(httpResponse) {
            //Log(httpResponse.text);
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
    var query = GetSessionQuery();

    response.success( query.toJSON() );
};

/*var API_IsUserOnline = function(request, response) {
    "use strict";

    var userUdid = request.params.udid;
    var query = GetSessionQuery()
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
};*/

var API_GetUserAliveTo = function(request, response) {
    "use strict";

    var userUdid = request.params.udid;
    var query = GetSessionQuery();
    var promise = query.first("udid", userUdid);
    promise.then(function(result) {
        response.success(result);
    });

    response.error();
};

var API_Login = function(request, response) {
    "use strict";

    var userUdid = request.params.udid;
    var session = NewSession(userUdid);
    var parseObject = NewParseObject(session);

    parseObject.save(null, {
        success: function(obj){Log(obj); response.success( JSON.stringify(parseObject) );},
        error: function(error) {errorHandler(error); response.error();}
    });

    // TODO: make UDID unique (+ can't login if already loginned)
    // TODO: not correct. Need another method. 3 types of sessions exist: NULL | DEAD | ALIVE
};

var API_Logout = function(request, response) {
    "use strict";

    var userUdid = request.params.udid;

    var query = GetSessionQuery()
        .equalTo("udid", userUdid);

    query.each( function(obj) {
        Log(obj, "Logout:destroy");
       obj.destroy();
    }).done( function() {response.success();} );
};


// Bindings
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

Parse.Cloud.define("GetOnlineUsers", API_GetOnlineUsers); // jshint ignore:line
Parse.Cloud.define("GetUserAliveTo", API_GetUserAliveTo); // jshint ignore:line

Parse.Cloud.define("Login", API_Login); // jshint ignore:line
Parse.Cloud.define("Logout", API_Logout); // jshint ignore:line