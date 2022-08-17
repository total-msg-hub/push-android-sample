package com.uracle.cloudpush.notification;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;

import m.client.push.library.common.Logger;

public class PushNotificationManager {

	public static void createNotification(final Context context, final Intent intent, final String pushType) {
		try {
			createUpnsNotification(context, intent);
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
//	public static boolean isRestrictedScreen(final Context context) {
//		KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
//		return km.inKeyguardRestrictedInputMode(); 
//	}
	
	public static void createUpnsNotification(final Context context, final Intent intent) throws Exception {
        String jsonData = intent.getExtras().getString("JSON");
        String encryptData = intent.getExtras().getString("message_encrypt");
        if (jsonData != null) {
        	jsonData = jsonData.replaceAll("https", "http");
        	jsonData = jsonData.replaceAll("\\\\", "/");
        	jsonData = jsonData.replaceAll("//", "/");
        }
        
        try {
        	//Log.d("PushNotificationManager", "[PushNotificationManager] createUpnsNotification: " + jsonData);
	        JSONObject jsonMsg = new JSONObject(jsonData);
	        Logger.i(jsonMsg.toString(2));
	        String psid = intent.getExtras().getString("PSID");
	        //PushWakeLock.acquireCpuWakeLock(context);
	        
	       // UpnsNotifyHelper.showNotification(context, jsonMsg, psid, encryptData);
	        //PushWakeLock.releaseCpuLock();
        } 
        catch(Exception e) {
        	e.printStackTrace();
        }	
	}
	
}
