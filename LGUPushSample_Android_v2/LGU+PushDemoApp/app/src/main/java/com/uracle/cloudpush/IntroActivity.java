package com.uracle.cloudpush;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import com.uracle.cloudpush.common.CommonDef;
import com.uracle.cloudpush.common.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import m.client.push.library.PushManager;
import m.client.push.library.common.PushConstants;
import m.client.push.library.utils.PushUtils;

public class IntroActivity extends Activity {

	private static final String TAG  = "IntroActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.moe_intro);


		//notification data clear 
		NotificationManager mManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		mManager.cancelAll();

		goLoginProcess();
		
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
	}

	@Override
	protected void onPause() {
		super.onPause();

	}

	public void goLoginProcess(){
		if (Utils.getConfigString(IntroActivity.this, CommonDef.CONFIG_SAVE_LOGIN).equalsIgnoreCase("Y")){
			//goPushMessageBox();


			final String cuid = PushUtils.getStringFromStorage(CommonDef.CONFIG_USER_CUID, getApplicationContext());
			final String cname = PushUtils.getStringFromStorage(CommonDef.CONFIG_USER_NAME, getApplicationContext());


			if(TextUtils.isEmpty(cuid) || TextUtils.isEmpty(cname)){
				goLoginPage();
			}


			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					//	Utils.showProgressBar(IntroActivity.this, "로그인 중입니다.", "______");

					JSONObject params = new JSONObject();
					try {

						String url = PushUtils.getStringFromStorage(PushConstants.KEY_RECEIVER_SERVER_URL, getApplicationContext(), "");
						params.put(PushConstants.KEY_CUSTOM_RECEIVER_SERVER_URL, url);
						params.put(PushConstants.KEY_CUID, cuid);
						params.put(PushConstants.KEY_CNAME, cname);
					} 
					catch (JSONException e) {
						e.printStackTrace();
					}

					PushManager.getInstance().registerServiceAndUser(getApplicationContext(), params);
					startActivity(new Intent(IntroActivity.this, PushListActivity.class));
					finish();

				}
			}, 1000);


		}else{
			goLoginPage();
		}
	}


	private void goLoginPage(){
		new Handler().postDelayed(new Runnable(){
			public void run(){
				Intent pageIntent = new Intent(IntroActivity.this, LoginActivity.class);
				startActivity(pageIntent);
				finish();
			}
		},1000);

	}

	



}
