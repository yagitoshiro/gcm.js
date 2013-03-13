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

import org.appcelerator.titanium.TiApplication;

import android.content.Context;
import android.content.Intent;

import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {

    public GCMIntentService () {
		super(TiApplication.getInstance().getAppProperties().getString(GcmjsModule.PROPERTY_SENDER_ID, ""));
    }
        
    @Override
    protected void onRegistered (Context context, String registrationId) {
    	GcmjsModule module = GcmjsModule.getInstance();
    	if (module != null) {
    		GcmjsModule.logd("onRegistered: got the module!");
    		module.fireSuccess(registrationId);
    	}
    	else {
    		GcmjsModule.logd("onRegistered: module instance not found.");
    	}
    }
    
    @Override
    protected void onUnregistered (Context context, String registrationId) {
    	GcmjsModule module = GcmjsModule.getInstance();
    	if (module != null) {
    		GcmjsModule.logd("onUnregistered: got the module!");
    		module.fireUnregister(registrationId);
    	}
    	else {
    		GcmjsModule.logd("onUnregistered: module instance not found.");
    	}
    }

    @Override
    protected void onMessage (Context context, Intent messageIntent) {
    	TiApplication tiapp = TiApplication.getInstance();
    	
    	GcmjsModule module = GcmjsModule.getInstance();
    	if (module != null) {
    		GcmjsModule.logd("onMessage: got the module!");
    		if (module.isInFg()) {
    			GcmjsModule.logd("onMessage: app is in foreground, no need for notifications.");

    			HashMap<String, Object> messageData = new HashMap<String, Object>();
    	    	for (String key : messageIntent.getExtras().keySet()) {
    	    		String eventKey = key.startsWith("data.") ? key.substring(5) : key;
    				messageData.put(eventKey, messageIntent.getExtras().getString(key));
    			}
    			module.fireMessage(messageData);
    			return;
    		}
    	}
    	else {
    		GcmjsModule.logd("onMessage: module instance not found.");
    	}
    	
		Intent intent = new Intent(tiapp, GcmjsService.class);
        for (String key : messageIntent.getExtras().keySet()) {
			String eventKey = key.startsWith("data.") ? key.substring(5) : key;
			intent.putExtra(eventKey, messageIntent.getExtras().getString(key));
		}
        tiapp.startService(intent);
    }

    @Override
    public void onError (Context context, String errorId) {
    	GcmjsModule module = GcmjsModule.getInstance();
    	if (module != null) {
    		GcmjsModule.logd("onError: got the module!");
    		module.fireError(errorId);
    	}
    	else {
    		GcmjsModule.logd("onError: module instance not found.");
    	}
    }

    @Override
    protected boolean onRecoverableError (Context context, String errorId) {
    	GcmjsModule module = GcmjsModule.getInstance();
    	if (module != null) {
    		GcmjsModule.logd("onRecoverableError: got the module!");
    		module.fireError(errorId);
    	}
    	else {
    		GcmjsModule.logd("onRecoverableError: module instance not found.");
    	}
        return super.onRecoverableError(context, errorId);
    }

    @Override
    protected void onDeletedMessages (Context context, int total) {
    }
}