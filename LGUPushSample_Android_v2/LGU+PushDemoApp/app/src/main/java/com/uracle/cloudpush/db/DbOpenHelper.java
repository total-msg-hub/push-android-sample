package com.uracle.cloudpush.db;

import java.io.File;

import m.client.push.library.common.Logger;
import m.client.push.library.common.PushLog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

public class DbOpenHelper {

	private static final String DATABASE_NAME = "/morpheus_cloud_push.db";
	public static String DATABASE_PATH = Environment.getDataDirectory().getAbsolutePath() + File.separator + DATABASE_NAME;
	private static final int DATABASE_VERSION = 1;
	public static SQLiteDatabase mDB;
	private DatabaseHelper mDBHelper;
	private Context mContext;
	protected String mDBPath; 
	
	private class DatabaseHelper extends SQLiteOpenHelper{

		public DatabaseHelper(Context context, String name, CursorFactory factory, int version) {
			super(context, name, factory, version);
			try {
				
				
				DATABASE_PATH  = context.getFilesDir().getCanonicalPath().toString()+ DATABASE_NAME;
				Logger.e(DATABASE_PATH);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DataBases.CreateDB.PUSHMSG_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS "+DataBases.CreateDB.PUSHMSG_TABLENAME);
			onCreate(db);
		}
	}

	public DbOpenHelper(Context context){
		this.mContext = context;
		try {
			DATABASE_PATH  = context.getFilesDir().getCanonicalPath().toString()+ DATABASE_NAME;
		} 
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	public DbOpenHelper open() {
		try {
			mDBHelper = new DatabaseHelper(mContext, DATABASE_PATH /*DATABASE_NAME*/, null, DATABASE_VERSION);
			mDB = mDBHelper.getWritableDatabase();
		} 
		catch(Exception e) {
			e.printStackTrace();
		}
		return this;
	}

	public void close(){
		mDB.close();
	}

	public boolean databaseDrop() {
		Logger.e(DATABASE_PATH);
		return this.mContext.deleteDatabase(DATABASE_PATH);
	}

	public Cursor getPushAppMassageALL() {
		Cursor c = mDB.query(DataBases.CreateDB.PUSHMSG_TABLENAME, null, null, null, null, null, null);
		if (c != null && c.getCount() != 0) {
			c.moveToFirst();
		}	
		
		return c;		
	}		
	
	public Cursor getPushAppMassage(String messageKey) {
		Cursor c = mDB.query(DataBases.CreateDB.PUSHMSG_TABLENAME, null, 
				DataBases.CreateDB.COL_MSGKEY + " = '"+ messageKey +"'", null, null, null, null);
		if (c != null && c.getCount() != 0) {
			c.moveToFirst();
		}	
		
		return c;		
	}		

	public long insertPushMSG(String msgkey, int nType, String title, String content, String ext, int nNew, String msg) {
		long returnVal = -1;

		try {
			ContentValues values = new ContentValues();
			values.put(DataBases.CreateDB.COL_MSGKEY, msgkey);
			values.put(DataBases.CreateDB.COL_MSGTYPE, nType);
			values.put(DataBases.CreateDB.COL_MSGTITLE, title);
			values.put(DataBases.CreateDB.COL_MSGCONTENT, content);
			values.put(DataBases.CreateDB.COL_MSGEXT, ext);
			values.put(DataBases.CreateDB.COL_NEW, nNew);
//			values.put(DataBases.CreateDB.COL_DATE, DBUtils.getStrNowDate());
			long date = System.currentTimeMillis();
			values.put(DataBases.CreateDB.COL_DATE, date);
			values.put(DataBases.CreateDB.COL_MESSAGE, msg);
			
			PushLog.d("DatabaseHelper", "insertPushMSG COL_MSGKEY : " + msgkey);
			PushLog.d("DatabaseHelper", "insertPushMSG COL_MSGTYPE : " + nType);
			PushLog.d("DatabaseHelper", "insertPushMSG COL_MSGTITLE : " + title);
			PushLog.d("DatabaseHelper", "insertPushMSG COL_MSGCONTENT : " + content);
			PushLog.d("DatabaseHelper", "insertPushMSG COL_MSGEXT : " + ext);
			PushLog.d("DatabaseHelper", "insertPushMSG COL_NEW : " + nNew);
			PushLog.d("DatabaseHelper", "insertPushMSG COL_DATE : " + date);
			
			returnVal = mDB.insert(DataBases.CreateDB.PUSHMSG_TABLENAME, null, values);
		} 
		catch(Exception e) {
			returnVal = - 99;
		}
		return returnVal;
	}
	
	public int deletePushMSG(String msgkey){
		int returnVal = -1;
		try {
			returnVal = mDB.delete(DataBases.CreateDB.PUSHMSG_TABLENAME, 
								   	DataBases.CreateDB.COL_MSGKEY + " = '"+ msgkey +"'", null);
		} catch(SQLException e) {
			returnVal = - 99;
		}
		return returnVal;
	}
	
//	public int deletePushMSG(Date date){
//		int returnVal = -1;
//		try {
//			returnVal = mDB.delete(DataBases.CreateDB.PUSHMSG_TABLENAME, 
//								   	DataBases.CreateDB.COL_MSGKEY + " = '"+ date.getDate() +"'", null);
//		} catch(SQLException e) {
//		}
//		return returnVal;
//	}
	
	public int updateNewPushMsg(String msgkey){
		int returnVal = -1;
		try {
			ContentValues values = new ContentValues();
			values.put(DataBases.CreateDB.COL_NEW, 0);
			returnVal = mDB.update(DataBases.CreateDB.PUSHMSG_TABLENAME, values,
									DataBases.CreateDB.COL_NEW + " = '"+ 1 +"'" + " and " + DataBases.CreateDB.COL_MSGKEY + " = '"+ msgkey +"'", null);
		} catch(SQLException e) {
			returnVal = - 99;
		}
		return returnVal;
	}
	
}






