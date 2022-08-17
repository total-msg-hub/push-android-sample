package com.uracle.cloudpush.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.uracle.cloudpush.IntroActivity;
import com.uracle.cloudpush.LoginActivity;
import com.uracle.cloudpush.PushListActivity;
import com.uracle.cloudpush.R;
import com.uracle.cloudpush.common.CommonDef;
import com.uracle.cloudpush.common.Utils;
import com.uracle.cloudpush.db.DBUtils;
import com.uracle.cloudpush.helper.ContentType;
import com.uracle.cloudpush.helper.Define;
import com.uracle.cloudpush.helper.PushInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

import m.client.push.library.PushManager;
import m.client.push.library.common.Logger;

public class UpnsNotifyHelper {

	public static void showNotification(final Context context, final JSONObject jsonMsg) throws Exception {

		if (isDnDModeEnabled(context)) {
			Toast.makeText(context, "방해 금지 모드 설정 중...", Toast.LENGTH_SHORT).show();
			return;
		}

		final JSONObject mpsObjet = jsonMsg.optJSONObject(Define.KEY_MPS);
		String extData = mpsObjet.optString(Define.KEY_EXT);

		if (extData.startsWith("http")) { // HTTP 로 시작하는 url 형태일 경우 - 메세지 구분을 위한 스트링 형태로 서버로부터 내려받는다.
			Handler handler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					if (msg.what == 0) {
						String message = (String) msg.obj;
						if (message != null) {
							message = message.replaceAll("\\\\", "/");

							try {
								// 다음과 같은 형태로 응답 값이 구성되었을 경우 치환을 위한 로직 수행
								// "TYPE":"R","VAR":"hoseok|2015-06-31|2015-07-30|104320|11"
								if (mpsObjet.has("TYPE") && mpsObjet.getString("TYPE").equals("R")) {
									String var = mpsObjet.getString("VAR");
									HashMap<String, String> varMap = new HashMap<String, String>();
									if (var != null) {
										String[] varArray = var.split("\\|");
										for (int i = 0; i < varArray.length; i++) {
											int idx = i + 1;
											varMap.put("%VAR" + idx + "%", varArray[i]);
											//Log.d("test", "%VAR" + idx + "%" + " " +  varArray[i]);
										}

										Iterator<?> keys = varMap.keySet().iterator();
										while (keys.hasNext()) {
											String key = (String) keys.next();
											message = message.replaceAll(key, varMap.get(key));
											//Log.d("test", "message: " + message);
										}
									}
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}

						try {
							createNotification(context, jsonMsg, new JSONObject(message));
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}
			};
			new HttpGetStringThread(handler, extData).start();
		} else {
			if(TextUtils.isEmpty(extData)){
				createNotification(context, jsonMsg, new JSONObject());
			}else {
				createNotification(context, jsonMsg, new JSONObject(extData));
			}
		}
	}

	private static void createNotification(final Context context, final JSONObject jsonMsg, JSONObject message) {


		JSONObject mpsObj = jsonMsg.optJSONObject(Define.KEY_MPS);
		JSONObject apsObj = jsonMsg.optJSONObject(Define.KEY_APS);
		String seqNo = mpsObj.optString(Define.KEY_SEQNO);
		String alert = apsObj.optString(Define.KEY_ALERT);
		String title = context.getString(R.string.app_name);
		String body = "";
		try {
			JSONObject object = new JSONObject(alert);
			title = object.optString(Define.KEY_TITLE, context.getString(R.string.app_name));
			body = object.optString(Define.KEY_BODY);
		} catch (JSONException e) {
			e.printStackTrace();
			body = alert;
		}

		try {
			Logger.i(message.toString(2));

			jsonMsg.remove("EXT");
			jsonMsg.put("EXT", message);

			int type = message.optInt("type", 0);


			if (type == ContentType.TYPE_IMAGE) {
				// 이미지
				String imgUrl = message.optString(PushInfo.KEY_IMG_URL);
				String notiImage = message.optString(PushInfo.KEY_NOTIFICATION_IMAGE);
				DBUtils.getDbOpenHelper(context).insertPushMSG(seqNo, ContentType.TYPE_IMAGE, title, body, imgUrl, 1, jsonMsg.toString());
				if(TextUtils.isEmpty(notiImage)){
					defaultNotification(context, jsonMsg);
				}else{
					showImageNotification(context, jsonMsg, body, notiImage);
				}

			}else if(type == ContentType.TYPE_IMAGE_HTML){
				String notiImage = message.optString(PushInfo.KEY_NOTIFICATION_IMAGE);

				DBUtils.getDbOpenHelper(context).insertPushMSG(seqNo, ContentType.TYPE_IMAGE_HTML, title, body, message.toString(), 1, jsonMsg.toString());
				if(TextUtils.isEmpty(notiImage)){
					defaultNotification(context, jsonMsg);
				}else {
					showImageNotification(context, jsonMsg, body, notiImage);
				}
			}else if(type == ContentType.TYPE_JSON_URL){
				String jsonUrl = message.optString(PushInfo.KEY_JSON_URL);

				Handler handler = new Handler() {
					@Override
					public void handleMessage(Message msg) {
						if (msg.what == 0) {
							String message = (String) msg.obj;
							if (message != null) {
								Logger.i(message);
							}

							try {
								createNotification(context, jsonMsg, new JSONObject(message));
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					}
				};
				new HttpGetStringThread(handler, jsonUrl).start();

			}
			else {
				// 그외
				DBUtils.getDbOpenHelper(context).insertPushMSG(seqNo, ContentType.TYPE_DEFAULT, title, body, "", 1, jsonMsg.toString());
				String notiImage = message.optString(PushInfo.KEY_NOTIFICATION_IMAGE);
				if(TextUtils.isEmpty(notiImage)) {
					defaultNotification(context, jsonMsg);
				}
				else{
					showImageNotification(context, jsonMsg, body, notiImage);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			// count 처리
			int count = DBUtils.getUnReadCount(context);
			String badgeNo = apsObj.getString(Define.KEY_BADGE_NO);
			PushManager.getInstance().setDeviceBadgeCount(context, badgeNo);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		if (Utils.IsRunningApp(context)) {
			// 앱이 실행중이고 포그라운드일때
			if (Utils.IsAppForeground(context)) {
				if (Utils.getConfigString(context, CommonDef.CONFIG_SAVE_LOGIN).equalsIgnoreCase("Y")) {
					Intent intent1 = new Intent(context, PushListActivity.class);
					intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
							| Intent.FLAG_ACTIVITY_CLEAR_TOP
							| Intent.FLAG_ACTIVITY_SINGLE_TOP);
					context.startActivity(intent1);
				} else {
					Intent intent1 = new Intent(context, LoginActivity.class);
					intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
							| Intent.FLAG_ACTIVITY_CLEAR_TOP
							| Intent.FLAG_ACTIVITY_SINGLE_TOP);
					context.startActivity(intent1);
				}
			}
		}

	}



	private static void defaultNotification(Context context, JSONObject jsonMsg) throws Exception {
		JSONObject mpsObj = jsonMsg.optJSONObject(Define.KEY_MPS);
		JSONObject apsObj = jsonMsg.optJSONObject(Define.KEY_APS);
		String alert = apsObj.optString(Define.KEY_ALERT);
		String title = context.getString(R.string.app_name);
		String body = "";
		try {
			JSONObject object = new JSONObject(alert);
			title = object.optString(Define.KEY_TITLE, context.getString(R.string.app_name));
			body = object.optString(Define.KEY_BODY);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		int icon = R.mipmap.ic_launcher;
		int seqno = 0;
		try {
			seqno = Integer.parseInt(apsObj.getString(Define.KEY_BADGE_NO));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}


		Intent intent = null;
		if (Utils.IsRunningApp(context)) {
			// 앱이 실행중이면
			if (Utils.getConfigString(context, CommonDef.CONFIG_SAVE_LOGIN).equalsIgnoreCase("Y")) {
				intent = new Intent(context, PushListActivity.class);
			} else {
				intent = new Intent(context, LoginActivity.class);
			}
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_CLEAR_TOP
					| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		} else {
			intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			intent.setComponent(new ComponentName(context, IntroActivity.class));
		}

		// 노티에서 개행문자는 스페이스 처리함.
		String number = apsObj.optString(Define.KEY_BADGE_NO);

		PendingIntent pIntent = PendingIntent.getActivity(context, seqno, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context.getApplicationContext(), getChannelId(notificationManager, "일반 푸시"))
				.setAutoCancel(true)
				.setContentIntent(pIntent)
				.setSmallIcon(icon)
				.setContentTitle(title)
				.setContentText(body)
				.setPriority(NotificationCompat.PRIORITY_HIGH)
				.setTicker(title)
				.setSound(soundUri);

		if (Integer.parseInt(number) > 1) {
			mBuilder.setSubText("+" + number + " More");
		}
		notificationManager.notify(Define.KEY_TAG, seqno, mBuilder.build());
	}

	private static void showImageNotification(Context context, final JSONObject jsonMsg, final String strMessage, String img) {
		final int icon = R.mipmap.ic_launcher;
		final Context ctx = context;

		if (img.contains("http")) {
			img = img.replaceAll("\\\\", "/");
		}
		Logger.i(img);
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context).build();

		ImageLoader.getInstance().init(config);
		ImageLoader.getInstance().loadImage(img, new SimpleImageLoadingListener() {

			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				notificationWithBigPicture(ctx, jsonMsg, icon, loadedImage);
			}
		});
	}

	private static void notificationWithBigPicture(Context context, JSONObject jsonMsg, int icon, Bitmap banner) {

		JSONObject mpsObj = jsonMsg.optJSONObject(Define.KEY_MPS);
		JSONObject apsObj = jsonMsg.optJSONObject(Define.KEY_APS);
		String alert = apsObj.optString(Define.KEY_ALERT);

		String title = context.getString(R.string.app_name);
		String body = "";
		try {
			JSONObject object = new JSONObject(alert);
			title = object.optString(Define.KEY_TITLE, context.getString(R.string.app_name));
			body = object.optString(Define.KEY_BODY);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		Intent intent;

		if (Utils.IsRunningApp(context)) {
			// 앱이 실행중이면
			if (Utils.getConfigString(context, CommonDef.CONFIG_SAVE_LOGIN).equalsIgnoreCase("Y")) {
				intent = new Intent(context, PushListActivity.class);
			} else {
				intent = new Intent(context, LoginActivity.class);
			}
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_CLEAR_TOP
					| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		} else {
			intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			intent.setComponent(new ComponentName(context, IntroActivity.class));
		}

		int seqno = 0;
		try {
			seqno = Integer.parseInt(apsObj.getString(Define.KEY_BADGE_NO));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		PendingIntent pendingIntent = PendingIntent.getActivity(context, seqno, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context, getChannelId(notificationManager, "이미지 푸시"))
				.setSmallIcon(icon)
				.setTicker(title)
				.setContentTitle(title)
				.setContentText(body)
				.setAutoCancel(true);
		//builder.setFullScreenIntent(pendingIntent, true);
		String number = apsObj.optString(Define.KEY_BADGE_NO);
		if (Integer.parseInt(number) > 1) {
			builder.setSubText("+" + number + " More");
		}

		NotificationCompat.BigPictureStyle style = new NotificationCompat.BigPictureStyle();
		style.setBigContentTitle(title);
		style.setSummaryText(body);
		if (banner != null) {
			style.bigPicture(banner);
			builder.setStyle(style);
		}
		builder.setContentIntent(pendingIntent);
		builder.setDefaults(Notification.DEFAULT_VIBRATE);
		builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

		notificationManager.notify(Define.KEY_TAG, seqno, builder.build());
	}

	private static String getChannelId(NotificationManager notificationManager, String name) {
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
			String CHANNEL_ID = name;
			// Create a channel.
			NotificationChannel notificationChannel =
					new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH);
			notificationManager.createNotificationChannel(notificationChannel);

			return CHANNEL_ID;
		}
		return null;
	}

	public static boolean isDnDModeEnabled(Context context) {
		if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) {
			return false;
		}

		try {
			NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			int filterValue = notificationManager.getCurrentInterruptionFilter();
			switch (filterValue) {
				case NotificationManager.INTERRUPTION_FILTER_ALL:
					Logger.i("Interruption filter all");
					break;
				case NotificationManager.INTERRUPTION_FILTER_ALARMS:
					Logger.i("Interruption filter alarms");
					break;
				case NotificationManager.INTERRUPTION_FILTER_PRIORITY:
					Logger.i("Interruption filter priority");
					break;
				case NotificationManager.INTERRUPTION_FILTER_UNKNOWN:
					Logger.i("Interruption filter unknown");
					break;
				case NotificationManager.INTERRUPTION_FILTER_NONE:
					Logger.i("Interruption filter none");
					break;

			}
			if (filterValue == NotificationManager.INTERRUPTION_FILTER_ALL) {
				return false;
			} else if (filterValue == NotificationManager.INTERRUPTION_FILTER_PRIORITY) {
				//Logic based on which apps are allowed as priority

				return true; //or false
			} else {
				return true;
			}
		} catch (Exception e) {
			return false;
		}
	}
}