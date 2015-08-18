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
    var promise = query.lessThanOrEqualTo("aliveTo", GetNow().toDate())
        .each(function(obj)
         {
             Log(obj, "Delete dead session");
             obj.destroy();
         }
    );
	return promise;
}

function NewSession(udid) {
    "use strict";

    var session = _.clone(baseSession);
    session.udid = udid;
    session.loginedAt = GetNow();
    session.aliveTo = GetNow().add({seconds: sessionLifetimeSec});

    return session;
}

function GetSessionQuery() {
    "use strict";
    var objConstructor = Parse.Object.extend(sessionObjName); // jshint ignore:line
	var query = new Parse.Query(objConstructor);
    //query.select("udid", "loginedAt", "aliveTo"); //not work for some reason
	return query;
}

function IsUserOnline(udid, onUserOnlineHanlder, onUserOfflineHanlder, onError) {
	"use strict";

	var userAlive = false;
	var query = GetSessionQuery();
	query.equalTo("udid", udid).greaterThanOrEqualTo("aliveTo", GetNow().toDate());
	query.find({
		success: function(result)
		{
			if (result.length == 0) {
				onUserOfflineHanlder();
			}
			else {
				onUserOnlineHanlder(result);
			}
		},
		error: onError
	});
}

function NewParseSession(session) {
    "use strict";

    var objConstructor = Parse.Object.extend(sessionObjName); // jshint ignore:line
    var obj = new objConstructor();

    obj.set({
        udid: session.udid,
        loginedAt: session.loginedAt.toDate(),
        aliveTo: session.aliveTo.toDate()
        }
    );

    return obj;
}

function SendEvent(session) {
    "use strict";

    Parse.Cloud.httpRequest({ // jshint ignore:line
        url: httpRequestUrl + JSON.stringify(session),

        success: function(httpResponse) {},
        error: function(httpResponse) {
            Log('Request failed with response code ' + httpResponse.status);
        }
    });
}

// API functions
var API_GetNow = function(request, response) {
    "use strict";

	var onUserOnline = function(result) {
		response.success( GetNow().toDate() );
	};

	var onUserOffline = function(error) {
		response.error(error);
	};

	var onError = function(error) {
		response.error(error);
	};

	IsUserOnline(request.params.udid, onUserOnline, onUserOffline, onError);
};

var API_GetOnlineUsers = function(request, response) {
    "use strict";

	var onUserOnline = function(result) {
		var query = GetSessionQuery()
		    .addDescending("aliveTo");
		query.find({
			success: function(result)
			{
				response.success( JSON.stringify(result) );
			},
			error: errorHandler
		});
	};

	var onUserOffline = function(error) {
		response.error(error);
	};

	var onError = function(error) {
		response.error(error);
	};

    DeleteDeadSessions().always( function() {
	    IsUserOnline(request.params.udid, onUserOnline, onUserOffline, onError);
    });
};

var API_Login = function(request, response) {
    "use strict";

    var userUdid = request.params.udid;
    var session = NewSession(userUdid);
    var parseObject = NewParseSession(session);

	Parse.Cloud.run("Logout", {udid: userUdid}).always( function() {
		parseObject.save(null, {
			success: function(obj) {
				Log(obj, "Login:save");
				response.success( JSON.stringify(parseObject) );
			},
			error: function(error) {
				errorHandler(error);
				response.error(error);
			}
		});
	});
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

	SendEvent(request.object);
});

Parse.Cloud.afterDelete(sessionObjName, function(request) { // jshint ignore:line
    "use strict";

    request.object.set("aliveTo", request.object.get("loginedAt"));
    SendEvent(request.object);
});


// API definitions
Parse.Cloud.define("GetNow", API_GetNow); // jshint ignore:line

Parse.Cloud.define("GetOnlineUsers", API_GetOnlineUsers); // jshint ignore:line

Parse.Cloud.define("Login", API_Login); // jshint ignore:line
Parse.Cloud.define("Logout", API_Logout); // jshint ignore:line