package com.uracle.cloudpush.receiver;

import m.client.push.library.PushManager;
import m.client.push.library.common.Logger;
import m.client.push.library.common.PushConstants;

import com.uracle.cloudpush.notification.UpnsNotifyHelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

import org.json.JSONObject;

public class MessageArrivedReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(final Context context, Intent intent) {
		if (intent.getAction().equals(context.getPackageName() + PushConstants.ACTION_GCM_MESSAGE_ARRIVED)) {
			try {

				// 수신된 payload data 는 아래 3가지 방식으로 획득 할 수 있다.
				String data = intent.getExtras().getString(PushConstants.KEY_JSON);
				final String rawData = intent.getExtras().getString(PushConstants.KEY_ORIGINAL_PAYLOAD_STRING);
				byte[] rawDataBytes = intent.getExtras().getByteArray(PushConstants.KEY_ORIGINAL_PAYLOAD_BYTES);



				Logger.i(new JSONObject(data).toString(2));
				Logger.i("received raw data : " + rawData);
				Logger.i("received bytes data : " + new String(rawDataBytes, "utf-8"));
				new Handler(context.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(context, rawData, Toast.LENGTH_LONG).show();
					}
				});
				// 노티피케이션 생성
				UpnsNotifyHelper.showNotification(context, new JSONObject(data));

				//푸시 메시지 수신 ack
				PushManager.getInstance().pushMessageReceiveConfirm(context, data);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
