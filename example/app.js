/*global Ti: true, alert: true, require: true */

(function () {	
	var window = Ti.UI.createWindow({
		navBarHidden: true,
		backgroundColor: 'yellow',
		exitOnClose: true	
	}),
	btn = Ti.UI.createButton({
		width: Ti.UI.FILL, height: 40,
		left: 10, bottom: 10, right: 10,
		title: 'do something useless'
	}),
	window2 = Ti.UI.createWindow({
		backgroundColor: 'blue',
		fullscreen: true
	});
	btn.addEventListener('click', function () {
		window2.open();
	});
	window.add(btn);

	require('net.iamyellow.gcmjs').registerForPushNotifications({
		success: function (ev) {
			// on successful registration
			Ti.API.info('******* success, ' + ev.deviceToken);
		},
		error: function (ev) {
			// when an error occurs
			Ti.API.info('******* error, ' + ev.error);
		},
		callback: function () {
			// when a gcm notification is received WHEN the app IS IN FOREGROUND
			alert('hellow yellow!');
		},
		unregister: function (ev) {
			// on unregister 
			Ti.API.info('******* unregister, ' + ev.deviceToken);
		}
	});
	
	// in order to unregister:
	// require('net.iamyellow.gcmjs').unregister();

	window.open();	
})();