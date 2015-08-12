// Use Parse.Cloud.define to define as many cloud functions as you want.
// For example:
/*
Parse.Cloud.define("hello", function(request, response) {
	response.success("Hello world!");
});
*/

// Utils
function Log(obj, tag)
{
	var prefix = "Cloud_code: ";
	var loggingString = prefix;
	if (tag != null)
		loggingString += "[" + tag + "] ";
	loggingString += obj;

	console.log(loggingString);
}


// Constants
var deadAtColName = "deadAt",
    eventsChannelName = "UserEvent";
var baseHandlers = {
                      success: function() {
                          // Push was successfu
                      },
                      error: function(error) {
                          Log(error.message, "error")
                      }
                   };

function getNow()
{
    return new Date();
}


// Supporting
function DeleteDeadUsers()
{
    var query = new Parse.Query(Parse.User);
    query.lessThanOrEqualTo(deadAtColName, getNow())
        .each(function(obj)
         {
            obj.destroy();
         },
         baseHandlers);
}



// Bindings
Parse.Cloud.beforeSave(Parse.User, function(request, response) {
	var lifetimeSeconds = 13;

	var deadAt = new Date( getNow() );
	deadAt.setSeconds(deadAt.getSeconds() + lifetimeSeconds);

    var username = request.object.get("username");
	Log(deadAt, username + " - deletingAt");
	
	request.object.set(deadAtColName, deadAt);
	response.success();
});

Parse.Cloud.afterSave(Parse.User, function(request) {
    sendUserSigninNotification(request.object);
});

Parse.Cloud.beforeDelete(Parse.User, function(request, response) {
    sendUserLogoutNotification(request.object);
    response.success();
});


// Notifications
function sendUserSigninNotification(user)
{
    DeleteDeadUsers();
    var notification = {
                          channels: [ eventsChannelName ],
                          data: {
                              username: user.get("username"),
                              deadAt: user.get(deadAtColName),
                              isDead: false
                          }
                       }
    Parse.Push.send(notification, baseHandlers);
}

function sendUserLogoutNotification(user)
{
    DeleteDeadUsers();
    var notification = {
                          channels: [ eventsChannelName ],
                          data: {
                              username: user.get("username"),
                              isDead: true
                          }
                       }
    Parse.Push.send(notification, baseHandlers);
}


// APIs
Parse.Cloud.define("getUsersOnline", function(request, response) {
	var query = new Parse.Query(Parse.User);
	response.success(query);
});