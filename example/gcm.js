/*global Ti: true */

(function (service) {
	var serviceIntent = service.getIntent(),
	title = serviceIntent.hasExtra('title') ? serviceIntent.getStringExtra('title') : '',
	statusBarMessage = serviceIntent.hasExtra('title') ? serviceIntent.getStringExtra('title') : '',
	message = serviceIntent.hasExtra('message') ? serviceIntent.getStringExtra('message') : '',
	isAppStarted = Ti.Android.currentActivity && Ti.Android.currentActivity !== null,
	notificationId = (function () {
		// android notifications ids ara int32
		// java int32 max value is 2.147.483.647, so we cannot use javascript millis timpestamp
		// let's make a valid timed based id:

		// - we're going to use hhmmssDYLX where (DYL=DaysYearLeft, and X=0-9 rounded millis)
		// - hh always from 00 to 11
		// - DYL * 2 when hour is pm
		// - after all, its max value is 1.159.597.289

		var str = '',
		now = new Date();

		var hours = now.getHours(),
		minutes = now.getMinutes(),
		seconds = now.getSeconds();
		str += (hours > 11 ? hours - 12 : hours) + '';
		str += minutes + '';
		str += seconds + '';

		var start = new Date(now.getFullYear(), 0, 0),
		diff = now - start,
		oneDay = 1000 * 60 * 60 * 24,
		day = Math.floor(diff / oneDay); // day has remaining days til end of the year
		str += day * (hours > 11 ? 2 : 1);

		var ml = (now.getMilliseconds() / 100) | 0;
		str += ml;

		return str | 0;
	})();

	var intent = Ti.Android.createIntent({
		// NOTICE
		// replace '.NameOfYourAppActivity' with your app name!
		// check build/android/AndroidManifest.xml and search for android:name in the applcation tag, sure there it is!
		className: isAppStarted ? 'org.appcelerator.titanium.TiActivity' : Ti.App.id + '.NameOfYourAppActivity',
		packageName: Ti.App.id,
		flags: Ti.Android.FLAG_ACTIVITY_NEW_TASK | Ti.Android.FLAG_ACTIVITY_SINGLE_TOP
	});
	intent.addCategory(Ti.Android.CATEGORY_LAUNCHER);

	var pintent = Ti.Android.createPendingIntent({
		intent: intent
	}),
	notification = Ti.Android.createNotification({
		contentIntent: pintent,
		contentTitle: title,
		contentText: message,
		tickerText: statusBarMessage,
		defaults: Ti.Android.DEFAULT_LIGHTS | Ti.Android.DEFAULT_VIBRATE,
		flags: Ti.Android.FLAG_AUTO_CANCEL | Ti.Android.FLAG_SHOW_LIGHTS
	});
	Ti.Android.NotificationManager.notify(notificationId, notification);

	service.stop();
})(Ti.Android.currentService);