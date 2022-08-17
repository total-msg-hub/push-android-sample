package com.uracle.cloudpush.common;

import java.util.List;

import com.uracle.cloudpush.R;

import m.client.push.library.utils.PushUtils;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

public class Utils {

	private static boolean mPhoneCheck = false;
	
	private static boolean mIsRefresh = false;

	public static void setConfigString(Context ac, String key, String value) {
		SharedPreferences preferences = ac.getSharedPreferences(CommonDef.CONFIG_PREFERENCE_NAME, Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(key, value);
		editor.commit();
	}

	public static String getConfigString(Context ac, String key) {
		SharedPreferences preferences = ac.getSharedPreferences(CommonDef.CONFIG_PREFERENCE_NAME, Activity.MODE_PRIVATE);
		return preferences.getString(key, "");
	}

	public static boolean IsRunningApp(Context context){
		ActivityManager am = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
		String packageName;
		if(android.os.Build.VERSION.SDK_INT > 20){
			packageName = am.getRunningAppProcesses().get(0).processName;
		}
		else{
			packageName = am.getRunningTasks(1).get(0).topActivity.getPackageName();
		}

		if (TextUtils.equals(packageName, context.getPackageName())){
			return true;
		}else{
			return false;
		}
	}

	public static boolean IsAppForeground(Context context) {
		Log.d("NotificationManager","NotificationManager IsApplicationActive");

		final ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		try {
			// 롤리팝인 경우
			if (android.os.Build.VERSION.SDK_INT >= 21){

				// Get a list of running tasks, we are only interested in the last one,
				// the top most so we give a 1 as parameter so we only get the topmost.
				final List<ActivityManager.RunningAppProcessInfo> task = manager.getRunningAppProcesses();

				// Get the info we need for comparison.
				//ComponentName componentInfo = task.get(0).importanceReasonComponent;

				//	        	    for (int i=0; i<task.size(); i++){
				//	        	    	Log.d("NotificationManager","NotificationManager RunningAppProcessInfo importance : " + task.get(i).importance);
				//	        	    	Log.d("NotificationManager","NotificationManager RunningAppProcessInfo pkg : " + task.get(i).pkgList[0].toString());
				//	        	    }

				// Check if it matches our package name.
				if(task.get(0).pkgList[0].toString().equals(context.getPackageName()) && task.get(0).importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
					Log.d("NotificationManager","NotificationManager 0 IsApplicationActive true");
					return true;
				}
				// If not then our app is not on the foreground.
				Log.d("NotificationManager","NotificationManager 0 IsApplicationActive false");
				return false;
			}else{
				if (PushUtils.isRunningPushApps(context)){
					return true;
				}else{
					return false;
				}
			}

		} catch (NullPointerException e) {
			Log.d("NotificationManager","NotificationManager IsApplicationActive NullPointerException : " + e.getMessage());
		}
		Log.d("NotificationManager","NotificationManager IsApplicationActive false 1");
		return false;

	}

	public static AlertDialog createAlertDialog(final Context context, final String title, final String message, final Handler handler) {
		return new AlertDialog.Builder(context)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(title)
				.setMessage(message)
				.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (handler != null) {
							Message msg = handler.obtainMessage(Dialog.BUTTON_NEGATIVE, 0, 0, 0);
							handler.sendMessage(msg);

							dialog.dismiss();
						}    
					}
				})
				.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (handler != null) {
							Message msg = handler.obtainMessage(Dialog.BUTTON_POSITIVE, 0, 0, 0);
							handler.sendMessage(msg);

							dialog.cancel();
							dialog.dismiss();
						}   
					}
				})
				.create();
	}

	public static AlertDialog createConfirmDialog(final Context context, final String title, final String message, final Handler handler) {
		return new AlertDialog.Builder(context)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(title)
				.setMessage(message)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (handler != null) {
							Message msg = handler.obtainMessage(Dialog.BUTTON_POSITIVE, 0, 0, 0);
							handler.sendMessage(msg);

							dialog.cancel();
							dialog.dismiss();
						}else{
							dialog.cancel();
							dialog.dismiss();
						}
					}
				})
				.create();
	}


	static ProgressDialog mProgressDialog;

	public static void showProgressBar(Context context){
		showProgressBar(context, "", "");
	}

	public static void showProgressBar(Context context, String title, String messgae){
		mProgressDialog = ProgressDialog.show(context, title, messgae, true, false);
		mProgressDialog.setContentView(R.layout.moe_progressdialoglayout);
		mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
		mProgressDialog.show();
	}

	public static  void dissMissProgressBar(){
		if (mProgressDialog != null){
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}


/*	public static  String getPhoneNumber(Context context){
		TelephonyManager telManager = null;
		try {
			telManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);

		} catch (Exception e) {
			// TODO: handle exception
		}
		try {
			String phoneNumber = telManager.getLine1Number();
			if(phoneNumber != null && phoneNumber.startsWith("+82")){
				phoneNumber = phoneNumber.replace("+82", "0");

			}
			//phoneNumber = "+821086369183";
			if(!TextUtils.isEmpty(phoneNumber)){
				return PhoneNumberUtils.formatNumber( phoneNumber, Locale.getDefault().getCountry()).replaceAll("-", "");
			}

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		if(getCheckPhoneNumber() == false){
			return "01012345678";
		}
		return null;
	}*/

	public static void setCheckPhoneNumber(boolean check){
		mPhoneCheck = check;
	}

	public static boolean getCheckPhoneNumber(){
		return mPhoneCheck;
	}

}
