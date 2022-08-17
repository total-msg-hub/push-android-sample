package com.uracle.cloudpush.helper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.uracle.cloudpush.IntroActivity;
import com.uracle.cloudpush.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import m.client.push.library.common.Logger;

public class PushNotifyHelper {

	/**
	 * showNotification
	 * 메세지를 분석하여 타입 별 노티피케이션 생성
	 *
	 * @param context
	 * @param jsonMsg
	 * @throws Exception
	 */
	public static void showNotification(final Context context, final JSONObject jsonMsg) throws Exception {
		if (isDnDModeEnabled(context)) {
			Toast.makeText(context, "방해 금지 모드 설정 중...", Toast.LENGTH_SHORT).show();
			return;
		}
		final JSONObject mpsObjet = jsonMsg.optJSONObject(Define.KEY_MPS);
		String extData = mpsObjet.optString(Define.KEY_EXT);
		// EXT가 존재할 경우 다음과 같은 형태로 전달됨
		// extData = "http:/211.241.199.158:8180/msg/totalInfo/0218115649_msp.html";
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

						createNotification(context, jsonMsg, message);
					}
				}
			};
			// EXT url 로부터 메세지 정보를 수신하기 위한 쓰레드
			new HttpGetStringThread(handler, extData).start();
		} else { // 일반 스크링의 경우
			createNotification(context, jsonMsg, extData);
		}
	}

	/**
	 * createNotification
	 * 노티피케이션 생성
	 *
	 * @param context
	 * @param jsonMsg
	 * @param message
	 */
	private static void createNotification(final Context context, final JSONObject jsonMsg, String message) {
		ArrayList<String> params = null;
		if (message != null) {
			String[] paramArray = message.split("\\|");
			params = new ArrayList<String>(Arrays.asList(paramArray));
		}

		Logger.d("params size:: " + params.size());
		// 메세지 타입에 따라 노티피케이션 생성 - 용도에 따라 커스터마이징하여 사용이 가능 (타입 번호/파싱 룰 등등..)
		if (params != null && params.size() > 0) {
			try {
				// URL을 실제 상세 메세지 데이터로 치환
				jsonMsg.remove(Define.KEY_EXT);
				jsonMsg.put(Define.KEY_EXT, message);

				// 일반 메세지의 경우
				if (params.get(0).equals("0")) {
					Logger.d(" defaultNotification: " + message);
					defaultNotification(context, jsonMsg, message);
				}
				// 이미지 메세지의 경우
				else if (params.size() > 2) {
					Logger.d(" showImageNotification: " + message);
					showImageNotification(context, jsonMsg, params.get(1), params.get(2), message);
				}
				// 기타의 경우 - 일반메세지로 처리했으나 용도에 맞게 변경
				else {
					Logger.d(" UNKNOUWN TYPE:: " + params.get(0));
					defaultNotification(context, jsonMsg, message);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static void defaultNotification(Context context, JSONObject jsonMsg, String ext) throws Exception {
		JSONObject mpsObj = jsonMsg.optJSONObject(Define.KEY_MPS);
		String alert = jsonMsg.optJSONObject(Define.KEY_APS).optString(Define.KEY_ALERT);
		String title = context.getString(R.string.app_name);
		String body = "";
		try {
			JSONObject object = new JSONObject(alert);
			title = object.optString(Define.KEY_TITLE, context.getString(R.string.app_name));
			body = object.optString(Define.KEY_BODY);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		int icon = R.drawable.ic_launcher;
		int seqno = Integer.parseInt(mpsObj.getString(Define.KEY_SEQNO));

		Intent intent = new Intent(context, IntroActivity.class);
		intent.putExtra(Define.KEY_PUSH_OBJECT, jsonMsg.toString());
		intent.putExtra(Define.KEY_EXT, ext);
		intent.putExtra(Define.KEY_IS_PUSH, true);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

		PendingIntent pIntent = PendingIntent.getActivity(context, seqno, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context.getApplicationContext(), getChannelId(notificationManager, "일반 푸시"))
				.setAutoCancel(true)
				.setContentIntent(pIntent)
				.setSmallIcon(icon)
				.setContentTitle(title)
				.setContentText(body)
				.setPriority(NotificationCompat.PRIORITY_HIGH)
				.setTicker(title)
				.setSound(soundUri);

		notificationManager.notify(Define.KEY_TAG, seqno, mBuilder.build());
	}

	private static void showImageNotification(Context context, final JSONObject jsonMsg, final String strMessage, String img, final String ext) {
		final int icon = R.drawable.ic_launcher;
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
				notificationWithBigPicture(ctx, jsonMsg, icon, loadedImage, ext);
			}
		});
	}

	private static void notificationWithBigPicture(Context context, JSONObject jsonMsg, int icon, Bitmap banner, String ext) {

		JSONObject mpsObject = jsonMsg.optJSONObject(Define.KEY_MPS);
		String alert = jsonMsg.optJSONObject(Define.KEY_APS).optString(Define.KEY_ALERT);

		String title = context.getString(R.string.app_name);
		String body = "";
		try {
			JSONObject object = new JSONObject(alert);
			title = object.optString(Define.KEY_TITLE, context.getString(R.string.app_name));
			body = object.optString(Define.KEY_BODY);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		Intent intent = new Intent(context, IntroActivity.class);
		intent.putExtra(Define.KEY_PUSH_OBJECT, jsonMsg.toString());
		intent.putExtra(Define.KEY_EXT, ext);
		intent.putExtra(Define.KEY_IS_PUSH, true);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		int seqno = 0;
		try {
			seqno = Integer.parseInt(mpsObject.getString(Define.KEY_SEQNO));
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

		NotificationCompat.BigPictureStyle style = new NotificationCompat.BigPictureStyle();
		style.setBigContentTitle(title);
		style.setSummaryText(body);
		style.bigPicture(banner);
		builder.setStyle(style);
		builder.setContentIntent(pendingIntent);
		builder.setDefaults(Notification.DEFAULT_VIBRATE);
		builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

		notificationManager.notify(Define.KEY_TAG, seqno, builder.build());
	}

	private static String getChannelId(NotificationManager notificationManager, String name) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
		if (Build.VERSION.SDK_INT < 23) {
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