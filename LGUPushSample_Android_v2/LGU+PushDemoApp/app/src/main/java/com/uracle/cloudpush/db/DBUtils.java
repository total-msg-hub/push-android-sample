package com.uracle.cloudpush.db;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import m.client.push.library.common.PushLog;
import android.content.Context;
import android.database.Cursor;


public class DBUtils {
		
	
	private static DbOpenHelper mDbOpenHelper;
	/**
	 * 데이타베이스를 체크해서 존재하지 않으면 OBJECT를 생성한다.
	 * CBS 데이타베이스 관련처리는 공통으로 이용한다.
	 * @param context Context 
	 * @return 데이타OBJECT를 반환한다  DbOpenHelper
	 */		
	public static DbOpenHelper getDbOpenHelper(Context context) {
		if (mDbOpenHelper == null) {
			mDbOpenHelper = new DbOpenHelper(context);
			mDbOpenHelper.open(); 
		}
		return mDbOpenHelper;
	}	
	
	/**
	 * 시분초를 HH, MM, SS 문자열 배열로 반환한다.
	 * @return
	 */
	public static String[] getStrArrNowTime() {
		Calendar oCalendar = Calendar.getInstance();

		int iHour = oCalendar.get(Calendar.HOUR_OF_DAY);
		String hour = (iHour < 10) ? "0" + iHour : "" + iHour;

		int iMinute = oCalendar.get(Calendar.MINUTE);
		String minute = (iMinute < 10) ? "0" + iMinute : "" + iMinute;

		int iSecond = oCalendar.get(Calendar.SECOND);
		String second = (iSecond < 10) ? "0" + iSecond : "" + iSecond;

		String[] strNowTime = { hour, minute, second };
		return strNowTime;
	}
	
	/**
	 * 현재 날짜를 YYYYMMDD로 반환한다.
	 * @return
	 */
	public static String getStrNowDate() {
		Calendar oCalendar = Calendar.getInstance();
		String year = "" + oCalendar.get(Calendar.YEAR);
		String month = "";
		String date = "";
		
		int m = oCalendar.get(Calendar.MONTH) + 1;
		int d = oCalendar.get(Calendar.DAY_OF_MONTH);

		month = (m < 10) ? "0" + m : "" + m;
		date = (d < 10) ? "0" + d : "" + d;
		String[] time = getStrArrNowTime();
		return year +"-"+ month +"-"+ date+"\n"+time[0]+":"+time[1]+":"+time[2];
	}

	public static String convertToDate(long time) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd\nHH:mm:ss"); 
		String str = null;
		Date date = null;
		try {
			date = new Date(time);
			str = df.format(date);
			PushLog.d("DBUtils", "convertToDate str : " + str);
		} catch (IllegalArgumentException ie){
			PushLog.d("DBUtils", "convertToDate IllegalArgumentException occured : " + ie.getMessage());
			str = "";
		}
		return str;
	}
	
	public static long getBeforeOneMonth() {
		Date cuurDate = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(cuurDate);
		
		cal.add(Calendar.MONTH, -1);
		cal.set(Calendar.HOUR, -12);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd, HH:mm:ss"); 
		PushLog.d("DBUtils", "getBeforeOneMonth str : " + df.format(cal.getTime()));
		
		return cal.getTimeInMillis();
	}

	
	  // 푸시 리시트를 초기화 한다.
    public static int getUnReadCount(Context context){
    
    	int count = 0;
    	Cursor cursor = DBUtils.getDbOpenHelper(context).getPushAppMassageALL();
		if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
			do {
				try {
					
						if(cursor.getInt(cursor.getColumnIndex(DataBases.CreateDB.COL_NEW)) == 1){
							count ++;
						};
	
				}catch (Exception e){
					
				}
			} while(cursor.moveToNext());
		}
		return count;
    }
    
    
	  // 푸시 리스트에 데이터 가져오기 
    public static String getMessage(Context context, String messageKey){
    
    	Cursor cursor = DBUtils.getDbOpenHelper(context).getPushAppMassage(messageKey);
		if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
			return cursor.getString(cursor.getColumnIndex(DataBases.CreateDB.COL_MESSAGE));
		}
		return null;
    }
	
	  // 푸시 리스트에 데이터 가져오기 
    
    public static boolean getIsAlreadyReadMessage(Context context, String messageKey){
    
    	Cursor cursor = DBUtils.getDbOpenHelper(context).getPushAppMassage(messageKey);
		if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
			if(cursor.getInt(cursor.getColumnIndex(DataBases.CreateDB.COL_NEW)) == 1){
				return false;
			};
		}
		return true;
    }
}








