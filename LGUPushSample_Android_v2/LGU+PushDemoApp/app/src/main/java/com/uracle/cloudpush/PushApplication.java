package com.uracle.cloudpush;

import android.app.Application;

public class PushApplication extends Application {

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		System.out.println("PushSampleApp:: oncreate()");
	}

	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub
		super.onTerminate();
		System.out.println("PushSampleApp:: onTerminate()");
	}

	@Override
	public void onLowMemory() {
		// TODO Auto-generated method stub
		super.onLowMemory();
		System.out.println("PushSampleApp:: onLowMemory()");
	}

}
