//
//   Copyright 2013 jordi domenech <http://iamyellow.net, jordi@iamyellow.net>
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//

package net.iamyellow.gcmjs;

import java.util.HashMap;
import java.util.List;

import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.util.TiRHelper;
import org.appcelerator.titanium.util.TiRHelper.ResourceNotFoundException;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import android.support.v4.app.NotificationCompat;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import org.appcelerator.kroll.KrollDict;

public class GCMIntentService extends IntentService {
	private static final String TAG = "GCMIntentService";

	public static final int NOTIFICATION_ID = 1;
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;

	public GCMIntentService() {
		super(GCMIntentService.class.getSimpleName());
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		String messageType = gcm.getMessageType(intent);

		if (!extras.isEmpty()) {
			if (messageType == null) {
				GcmjsModule.logd(TAG + ": messageType is null");
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
				GcmjsModule.logd(TAG + ": deleted");
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
				int appIconId = 0;
				try {
					appIconId = TiRHelper.getApplicationResource("drawable.appicon");
				} catch (ResourceNotFoundException e) {
					GcmjsModule.logd(TAG + ": ResourceNotFoundException: " + e.getMessage());
				}

				// フォアグラウンドの場合だけPush通知
				if (!isInForeground()) {
					TiApplication tiapp = TiApplication.getInstance();
					Intent launcherIntent = new Intent(tiapp, GcmjsService.class);
					for (String key : extras.keySet()) {
						String eventKey = key.startsWith("data.") ? key.substring(5) : key;
						String data = extras.getString(key);
						// GcmjsModule.logd(TAG + ": eventKey:" + eventKey + "
						// data:" + data);
						if (data != null && !"".equals(data)) {
							launcherIntent.putExtra(eventKey, data);
						}
					}
					tiapp.startService(launcherIntent);

				} else {
					KrollDict messageData = new KrollDict();
					for (String key : extras.keySet()) {
						String eventKey = key.startsWith("data.") ? key.substring(5) : key;
						String data = extras.getString(key);
						// GcmjsModule.logd(TAG + ": eventKey:" + eventKey + "
						// data:" + data);
						if (data != null && !"".equals(data)) {
							messageData.put(eventKey, data);
						}
					}
					fireMessage(messageData);
				}

			}
		}
		GCMBroadcastReceiver.completeWakefulIntent(intent);
	}

	public static void fireMessage(KrollDict messageData) {
		GcmjsModule module = GcmjsModule.getInstance();
		if (module != null) {
			module.fireMessage(messageData);
		} else {
			GcmjsModule.logd(TAG + ": fireMessage module instance not found.");
		}
	}

	public static boolean isInForeground() {
		Context context = TiApplication.getInstance().getApplicationContext();
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		String packageName = context.getPackageName();
		if (am.getRunningTasks(1).get(0).topActivity.getPackageName().equals(packageName)) {
			return true;
		}
		return false;
	}

}
