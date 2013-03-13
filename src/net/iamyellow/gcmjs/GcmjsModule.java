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

import org.appcelerator.kroll.common.Log;
import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollRuntime;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiApplication;

import android.content.Context;

import com.google.android.gcm.GCMRegistrar;

@Kroll.module(name = "Gcmjs", id = "net.iamyellow.gcmjs")
public class GcmjsModule extends KrollModule {

	// *************************************************************
	// constants

	private static final String PROPERTY_ON_SUCCESS = "success";
	private static final String PROPERTY_ON_ERROR = "error";
	private static final String PROPERTY_ON_MESSAGE = "callback";
	private static final String PROPERTY_ON_UNREGISTER = "unregister";
	private static final String PROPERTY_ON_DATA = "data";

	private static final String EVENT_PROPERTY_DEVICE_TOKEN = "deviceToken";
	private static final String EVENT_PROPERTY_ERROR = "error";

	public static String PROPERTY_SENDER_ID = "GCM_sender_id";
	public static final boolean DBG = org.appcelerator.kroll.common.TiConfig.LOGD;

	// *************************************************************
	// logging

	private static final String LCAT = "gcmjs";

	public static void logd(String msg) {
		// if (DBG) {
		if (true) {
			Log.d(LCAT, msg);
		}
	}

	public static void logw(String msg) {
		Log.e(LCAT, msg);
	}

	// *************************************************************
	// callbacks

	private static KrollFunction onSuccessCallback;
	private static KrollFunction onErrorCallback;
	private static KrollFunction onMessageCallback;
	private static KrollFunction onUnregisterCallback;
	private static KrollFunction onDataCallback;

	// *************************************************************
	// singleton

	private static GcmjsModule instance = null;

	public static GcmjsModule getInstance() {
		return instance;
	}

	// *************************************************************
	// constructor

	private static AppStateListener appStateListener = null;

	public GcmjsModule() {
		super();

		onSuccessCallback = null;
		onErrorCallback = null;
		onMessageCallback = null;
		onUnregisterCallback = null;
		onDataCallback = null;

		instance = this;
		if (appStateListener == null) {
			appStateListener = new AppStateListener();
			TiApplication.addActivityTransitionListener(appStateListener);
		}
	}

	// *************************************************************
	// related to activities lifecycle

	public boolean isInFg() {
		if (!KrollRuntime.isInitialized()) {
			return false;
		}

		if (AppStateListener.oneActivityIsResumed) {
			return true;
		}

		return false;
	}

	@Kroll.getProperty
	@Kroll.method
	public boolean getIsLauncherActivity() {
		return AppStateListener.appWasNotRunning;
	}

	// *************************************************************
	// registration

	@Kroll.method
	public void registerForPushNotifications(Object arg) {

		// collect parameters
		@SuppressWarnings("unchecked")
		HashMap<String, Object> kd = (HashMap<String, Object>) arg;
		Object pOnSuccessCallback = kd.get(PROPERTY_ON_SUCCESS);
		Object pOnErrorCallback = kd.get(PROPERTY_ON_ERROR);
		Object pOnMessageCallback = kd.get(PROPERTY_ON_MESSAGE);
		Object pOnUnregisterCallback = kd.get(PROPERTY_ON_UNREGISTER);
		Object pOnDataCallback = kd.get(PROPERTY_ON_DATA);

		if (pOnSuccessCallback instanceof KrollFunction) {
			logd("Setting onSuccessCallback.");
			onSuccessCallback = (KrollFunction) pOnSuccessCallback;
		}
		if (pOnErrorCallback instanceof KrollFunction) {
			logd("Setting onErrorCallback.");
			onErrorCallback = (KrollFunction) pOnErrorCallback;
		}
		if (pOnMessageCallback instanceof KrollFunction) {
			logd("Setting onMessageCallback.");
			onMessageCallback = (KrollFunction) pOnMessageCallback;
		}
		if (pOnUnregisterCallback instanceof KrollFunction) {
			logd("Setting onUnregisterCallback.");
			onUnregisterCallback = (KrollFunction) pOnUnregisterCallback;
		}
		if (pOnDataCallback instanceof KrollFunction) {
			logd("Setting onDataCallback.");
			onDataCallback = (KrollFunction) pOnDataCallback;
		}

		// if we're executing this, we **SHOULD BE** in fg
		AppStateListener.oneActivityIsResumed = true;

		// do the registration
		Context context = TiApplication.getInstance().getApplicationContext();
		GCMRegistrar.checkDevice(context);
		GCMRegistrar.checkManifest(context);
		String registrationId = GCMRegistrar.getRegistrationId(context);
		if (registrationId.equals("")) {
			logd("Registering for GCM notifications.");

			GCMRegistrar.register(
					context,
					TiApplication.getInstance().getAppProperties()
							.getString(GcmjsModule.PROPERTY_SENDER_ID, ""));
		} else {
			logd("Previously registered for GCM notifications, firing success event.");

			fireSuccess(registrationId);
		}
	}

	// *************************************************************
	// unregister

	@Kroll.method
	public void unregisterForPushNotifications() {
		Context context = TiApplication.getInstance().getApplicationContext();
		GCMRegistrar.checkDevice(context);
		GCMRegistrar.checkManifest(context);
		String registrationId = GCMRegistrar.getRegistrationId(context);
		if (registrationId.equals("")) {
			logw("Trying to unregister but user was not registered.");
		} else {
			GCMRegistrar.unregister(context);
		}
	}

	// *************************************************************
	// main activity class name helper

	@Kroll.getProperty
	@Kroll.method
	public String getMainActivityClassName() {
		return TiApplication
				.getInstance()
				.getPackageManager()
				.getLaunchIntentForPackage(
						TiApplication.getInstance().getPackageName())
				.getComponent().getClassName();
	}

	// *************************************************************
	// current service stuff

	private static GcmjsServiceProxy currentService;

	public void setCurrentService(GcmjsServiceProxy currentService) {
		GcmjsModule.currentService = currentService;
	}

	@Kroll.getProperty
	@Kroll.method
	public GcmjsServiceProxy getCurrentService() {
		return currentService;
	}

	// *************************************************************
	// data

	private static HashMap<String, Object> data = null;
	private static boolean pendingData = false;

	@SuppressWarnings("rawtypes")
	@Kroll.getProperty
	@Kroll.method
	public HashMap getData() {
		logd("Getting data.");

		return data;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Kroll.setProperty
	@Kroll.method
	public void setData(HashMap pData) {
		logd("Setting data.");

		data = pData;

		if (AppStateListener.appWasNotRunning) {
			logd("Setting data while we're in bg.");
			return;
		}

		if (data != null) {
			logd("Mark pending data.");
			pendingData = true;
		} else {
			logd("No pending data to mark.");
		}
	}

	public void executeActionsWhileIfForeground() {
		logd("Checking for foreground pending actions.");
		if (pendingData) {
			logd("Found pending data.");
			pendingData = false;
			fireData();
		} else {
			logd("No pending data found.");
		}
	}

	// *************************************************************
	// events

	public void fireSuccess(String registrationId) {
		logd("Start firing success.");
		if (onSuccessCallback != null) {
			HashMap<String, String> result = new HashMap<String, String>();
			result.put(EVENT_PROPERTY_DEVICE_TOKEN, registrationId);
			onSuccessCallback.call(getKrollObject(), result);

			logd("Success event should have been fired.");
		}
	}

	public void fireError(String error) {
		logd("Start firing error.");
		if (onErrorCallback != null) {
			HashMap<String, String> result = new HashMap<String, String>();
			result.put(EVENT_PROPERTY_ERROR, error);
			onErrorCallback.call(getKrollObject(), result);

			logd("Error event should have been fired.");
		}
	}

	public void fireUnregister(String registrationId) {
		logd("Start firing unregister.");
		if (onUnregisterCallback != null) {
			logd("Should fire unregister.");
			HashMap<String, String> result = new HashMap<String, String>();
			result.put(EVENT_PROPERTY_DEVICE_TOKEN, registrationId);
			onUnregisterCallback.call(getKrollObject(), result);
		}
	}

	public void fireMessage(HashMap<String, Object> messageData) {
		logd("Start firing callback.");
		if (onMessageCallback != null) {
			onMessageCallback.call(getKrollObject(), messageData);

			logd("Callback event should have been fired.");
		}
	}

	public void fireData() {
		logd("Start firing data.");
		if (onDataCallback != null) {
			onDataCallback.call(getKrollObject(), data);

			logd("Data event should have been fired.");
		}
	}
}