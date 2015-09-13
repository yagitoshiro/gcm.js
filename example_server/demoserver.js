/*global require: true, console: true, process: true */

(function(messageId, callback) {
	var _GCM = require('gcm').GCM;

	var GCM = new _GCM('SET YOU API KEY HERE');

	var registration_id = 'SET YOUR DEVICES REGISTRATIONID HERE';

	var message = {
		collapse_key: messageId,
		registration_id: registration_id,
		'data.title': 'Push From demoserver.js',
		'data.sound': 'blacksmoke.mp3',
		'data.message': 'Message Description',
		'data.appdata': JSON.stringify({
			avatar: 'hoge.png',
			badges: 1,
			notification_id: 100,
			tipo_id: 10,
			from: 302421605303,
			link: 'http://www.google.com/'
		})
	};

	GCM.send(message, function(err, messageId) {
		console.log('send messageId:' + messageId);
		if (err) {
			console.error('error!');
		}
		callback(0);
	});
})((new Date()).getTime() + '', process.exit);
