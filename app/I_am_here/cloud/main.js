// Use Parse.Cloud.define to define as many cloud functions as you want.
// For example:
/*
Parse.Cloud.define("hello", function(request, response) {
	response.success("Hello world!");
});
*/

// Importas
var _ = require('underscore');
var moment = require('moment');

// Constants
var tokenLifetimeSec = 30;

var tokenName = "token";
var baseToken = {udid: "", dieAfter: 0, createdAt: moment()};

var eventsChannelName = "events";
var publishKey = "pub-c-6271f363-519a-432d-9059-e65a7203ce0e",
    subscribeKey = "sub-c-a3d06db8-410b-11e5-8bf2-0619f8945a4f",
    httpRequestUrl = 'http://pubsub.pubnub.com/publish/' + publishKey + '/' + subscribeKey + '/0/' + eventsChannelName + '/0/';


// Utils
function Log(obj, tag)
{
	var prefix = "Cloud_code: ";
	var loggingString = prefix;
	if (tag != null)
		loggingString += "[" + tag + "] ";
	loggingString += obj + "\n";

	console.log(loggingString);
}


// Handlers
var baseHandlers = {
    success: function() {
        // Push was successfu
    },
    error: function(error) {
        Log(error.message, "error")
    }
};


// Supporting
function UnregisterDeadUsers()
{
    var query = new Parse.Query(tokenName);
    query.lessThanOrEqualTo(deadAtColName, getNow())
        .each(function(obj)
         {
            obj.destroy();
         },
         baseHandlers);

    // TODO
}

function SendEvent(event)
{
    Parse.Cloud.httpRequest({
        url: httpRequestUrl + event,

        success: function(httpResponse) {
            Log(httpResponse.text);
        },

        error: function(httpResponse) {
            Log('Request failed with response code ' + httpResponse.status);
        }
    });
}


// APIs
Parse.Cloud.define("GetUsers", function(request, response) {
	var query = new Parse.Query(tokenName);

	// TODO

	response.success(query);
});

Parse.Cloud.define("IsUserAlive", function(request, response) {
	var query = new Parse.Query(tokenName);

	// TODO

	response.success(query);
});

Parse.Cloud.define("Login", function(request, response) {
	//var query = new Parse.Query(tokenName);

	var token = _.clone(baseToken);
	token.createdAt = moment();
	token.dieAfter = moment().add({seconds:tokenLifetimeSec}) - token.createdAt;
	token.DATATEST = moment(30000);
	//token.udid = request.object.get("udid");

	// TODO: SAVE IT TO DB

	response.success( JSON.stringify(token) );
});

Parse.Cloud.define("Logout", function(request, response) {
	var query = new Parse.Query(tokenName);

	// TODO: DELETE TOKEN FROM DB

	response.success(query);
});


// Bindings
/*Parse.Cloud.afterSave(tokenName, function(request) {
    var token = _.clone(baseToken);
    var reqToken = request.object;
    Log(reqToken, "reqToken on afterSave");

    token.udid = reqToken.get("udid");
    token.deadAt = reqToken.get("deadAt");

	SendEvent(token);

	response.success();
});

Parse.Cloud.afterDelete(tokenName, function(request) {
    var token = _.clone(baseToken);
    var reqToken = request.object;
    Log(reqToken, "reqToken on afterDelete");

    token.udid = reqToken.get("udid");
    token.deadAt = reqToken.get("deadAt");
    token.dead = true;

	SendEvent(token);

	response.success();
});*/


/*Parse.Cloud.beforeSave(Parse.User, function(request, response) {
	var lifetimeSeconds = 13;

	var deadAt = new Date( getNow() );
	deadAt.setSeconds(deadAt.getSeconds() + lifetimeSeconds);

    var username = request.object.get("username");
	Log(deadAt, username + " - deletingAt");

	request.object.set(deadAtColName, deadAt);
	response.success();
});*/