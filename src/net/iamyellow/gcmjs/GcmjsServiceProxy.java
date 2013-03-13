package net.iamyellow.gcmjs;

import org.appcelerator.titanium.proxy.IntentProxy;
import org.appcelerator.titanium.proxy.ServiceProxy;
import org.appcelerator.kroll.annotations.Kroll;

import android.app.Service;
import android.content.Intent;

@Kroll.proxy
public class GcmjsServiceProxy extends ServiceProxy {
	
	private Service service;
	private int serviceInstanceId;
	private IntentProxy intentProxy;
	
	public GcmjsServiceProxy () {
	}

	public GcmjsServiceProxy (IntentProxy intentProxy) {
		setIntent(intentProxy);
	}

	public GcmjsServiceProxy (Service service, Intent intent, int serviceInstanceId) {
		this.service = service;
		setIntent(intent);
		this.serviceInstanceId = serviceInstanceId;
	}

	@Kroll.getProperty @Kroll.method
	public int getServiceInstanceId () {
		return serviceInstanceId;
	}

	@Kroll.getProperty @Kroll.method
	public IntentProxy getIntent () {
		return intentProxy;
	}

	public void setIntent (Intent intent) {
		setIntent(new IntentProxy(intent));
	}

	public void setIntent (IntentProxy intentProxy) {
		this.intentProxy = intentProxy;
	}

	@Kroll.method
	public void start () {
	}

	@Kroll.method
	public void stop() {
		GcmjsModule.logd("Stopping gcm.js service.");
		service.stopSelf();
		GcmjsModule.getInstance().setCurrentService(null);
	}

	@Override
	public void release() {
		super.release();
		this.service = null;
	}
}