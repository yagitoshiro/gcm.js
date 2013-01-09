/*global require: true, console: true, process: true */

(function (messageId, callback) {
	var _GCM = require('gcm').GCM,
	GCM = new _GCM('API_KEY'); // API KEY at Google APIs Console

	var message = {
		registration_id: 'USER_REGISTRATION_ID',
		'data.title': 'shephard: what lies in the shadow of the statue?',
		'data.message': '4 8 15 16 23 42',
		'data.sound': 'blacksmoke.mp3',
		collapse_key: messageId
	};

	GCM.send(message, function (err, messageId) {
		if (err) {
			console.error('error!');
		}
		callback(0);
	});
})((new Date()).getTime() + '', process.exit);