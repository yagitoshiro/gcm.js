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
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiApplication;

import android.content.Context;

import com.google.android.gcm.GCMRegistrar;

@Kroll.module(name="Gcmjs", id="net.iamyellow.gcmjs")
public class GcmjsModule extends KrollModule {

	// *************************************************************
	// constants 

	private static final String PROPERTY_ON_SUCCESS =  "success";
	private static final String PROPERTY_ON_ERROR =  "error";
	private static final String PROPERTY_ON_MESSAGE =  "callback";
	private static final String PROPERTY_ON_UNREGISTER =  "unregister";

	private static final String EVENT_PROPERTY_DEVICE_TOKEN = "deviceToken";
	private static final String EVENT_PROPERTY_ERROR = "error";
	
	public static String PROPERTY_SENDER_ID = "GCM_sender_id";
	public static String MODULE_NAME = "gcmjs";
	public static final boolean DBG = org.appcelerator.kroll.common.TiConfig.LOGD;
	
	// *************************************************************
	// state 

	public static AppStateListener appStateListener = null;
	
	// *************************************************************
	// logging

	public static final String LCAT = "gcmjs";	
	public static void logd (String msg) {
		if (DBG) {
			Log.d(LCAT, msg);
		}
	}
	public static void logw (String msg) {
		Log.e(LCAT, msg);
	}

	// *************************************************************
	// callbacks 

	private KrollFunction onSuccessCallback;
	private KrollFunction onErrorCallback;
	private KrollFunction onMessageCallback;
	private KrollFunction onUnregisterCallback;

	// *************************************************************
	// constructor 

	public GcmjsModule () {		
		super();
		
		onSuccessCallback = null;
		onErrorCallback = null;
		onMessageCallback = null;
		onUnregisterCallback = null;
		
		if (TiApplication.getInstance().getModuleByName(MODULE_NAME) == null) {
			logd("No module instance, registering this.");
			TiApplication.getInstance().registerModuleInstance(MODULE_NAME, this);
		}
		else {
			logd("A module instance has been found.");
		}
	}

	// *************************************************************
	// related to activities lifecycle 
	
	@Kroll.onAppCreate
	public static void onAppCreate (TiApplication app) {
		if (appStateListener == null) {
			appStateListener = new AppStateListener();
			TiApplication.addActivityTransitionListener(appStateListener);
		}
	}
		
	// *************************************************************
	// registration 
	
	@Kroll.method
	public void registerForPushNotifications (Object arg) {
		// collect parameters
		@SuppressWarnings("unchecked")
		HashMap<String, Object> kd = (HashMap<String, Object>)arg;
		Object onSuccessCallback = kd.get(PROPERTY_ON_SUCCESS);
		Object onErrorCallback = kd.get(PROPERTY_ON_ERROR);
		Object onMessageCallback = kd.get(PROPERTY_ON_MESSAGE);
		Object onUnregisterCallback = kd.get(PROPERTY_ON_UNREGISTER);
        
		if (onSuccessCallback instanceof KrollFunction) {
			this.onSuccessCallback = (KrollFunction)onSuccessCallback;
        }
        if (onErrorCallback instanceof KrollFunction) {
			this.onErrorCallback = (KrollFunction)onErrorCallback;
		}
        if (onMessageCallback instanceof KrollFunction) {
			this.onMessageCallback = (KrollFunction)onMessageCallback;
		}
        if (onUnregisterCallback instanceof KrollFunction) {
			this.onUnregisterCallback = (KrollFunction)onUnregisterCallback;
		}
        
        // if we're executing this, we're **SHOULD BE** in fg
        appStateListener.oneActivityIsResumed = true;
        
		// do the registration
        Context context = TiApplication.getInstance().getApplicationContext();
		GCMRegistrar.checkDevice(context);
		GCMRegistrar.checkManifest(context);
		String registrationId = GCMRegistrar.getRegistrationId(context);
		if (registrationId.equals("")) {
			logd("Registering for GCM notifications.");
	    	
			GCMRegistrar.register(context, TiApplication.getInstance().getAppProperties().getString(GcmjsModule.PROPERTY_SENDER_ID, ""));
		}
		else {			
			logd("Previously registered for GCM notifications, firing success event.");
			
			fireSuccess(registrationId);
		}
	}
	
	// *************************************************************
	// unregister 

	@Kroll.method
	public void uregister () {
        Context context = TiApplication.getInstance().getApplicationContext();
		GCMRegistrar.checkDevice(context);
		GCMRegistrar.checkManifest(context);
		String registrationId = GCMRegistrar.getRegistrationId(context);
		if (registrationId.equals("")) {
			logw("Trying to unregister but user was not registered.");
		}
		else {
			GCMRegistrar.unregister(context);
		}
	}
	
	// *************************************************************
	// events
	
	public void fireSuccess (String registrationId) {
		if (onSuccessCallback != null) {
	    	HashMap<String, String> result = new HashMap<String, String>();
	    	result.put(EVENT_PROPERTY_DEVICE_TOKEN, registrationId);
	    	onSuccessCallback.callAsync(getKrollObject(), result);		
		}
	}

	public void fireError (String error) {
		if (onErrorCallback != null) {
	    	HashMap<String, String> result = new HashMap<String, String>();
	    	result.put(EVENT_PROPERTY_ERROR, error);
	    	onErrorCallback.callAsync(getKrollObject(), result);
		}
	}
	
	public void fireUnregister (String registrationId) {
		if (onUnregisterCallback != null) {
	    	HashMap<String, String> result = new HashMap<String, String>();
	    	result.put(EVENT_PROPERTY_DEVICE_TOKEN, registrationId);
	    	onUnregisterCallback.callAsync(getKrollObject(), result);
		}
	}

	public void fireMessage (HashMap<String, Object>messageData) {
		if (onMessageCallback != null) {
	    	onMessageCallback.callAsync(getKrollObject(), messageData);
		}
	}
}