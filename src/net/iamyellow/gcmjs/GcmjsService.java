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

import ti.modules.titanium.android.TiJSService;
import org.appcelerator.titanium.TiC;

import android.app.Service;
import android.content.Intent;

public final class GcmjsService extends TiJSService {
	public GcmjsService() {
		super("gcm.js");
	}

	private void finalizeUrl(Intent intent) {
		if (url == null) {
			if (intent != null && intent.getDataString() != null) {
				url = intent.getDataString();
			} else {
				throw new IllegalStateException("Service url required.");
			}
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		finalizeUrl(intent);
		GcmjsServiceProxy proxy = createProxy(intent);
		GcmjsModule.getInstance().setCurrentService(proxy);

		start(proxy);

		return intent.getIntExtra(TiC.INTENT_PROPERTY_START_MODE,
				Service.START_REDELIVER_INTENT);
	}

	@Override
	protected GcmjsServiceProxy createProxy(Intent intent) {
		GcmjsServiceProxy proxy = new GcmjsServiceProxy(this, intent,
				proxyCounter.incrementAndGet());
		return proxy;
	}
}
