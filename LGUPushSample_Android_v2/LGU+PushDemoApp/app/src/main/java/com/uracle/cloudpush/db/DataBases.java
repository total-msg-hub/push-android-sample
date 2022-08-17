package com.uracle.cloudpush.db;

import android.provider.BaseColumns;

// DataBase Table
public final class DataBases {
	
	public static final class CreateDB implements BaseColumns {
		
		public static final String PUSHMSG_TABLENAME = "push_msg";
		public static final String COL_MSGKEY = "msgkey";
		public static final String COL_MSGTYPE = "type"; // 0:text, 1:web, 2:동영상, 3:이미지
		public static final String COL_MSGTITLE = "title";
		public static final String COL_MSGCONTENT = "content";
		public static final String COL_MSGEXT = "ext";
		public static final String COL_NEW = "new"; // 0:old, 1:new
		public static final String COL_DATE = "date";
		public static final String COL_MESSAGE = "message";
		
				
		public static final String PUSHMSG_CREATE = 
				"CREATE TABLE "+PUSHMSG_TABLENAME+"(" 
						+ _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " 	
						+  COL_MSGKEY + " TEXT UNIQUE NOT NULL , "
						+  COL_MSGTYPE + " INTEGER NOT NULL , "
						+  COL_MSGTITLE + " TEXT NOT NULL , "
						+  COL_MSGCONTENT + " TEXT NOT NULL , "
						+  COL_MSGEXT + " TEXT , "
						+  COL_NEW + " INTEGER NOT NULL , "
						+  COL_DATE + " INTEGER NOT NULL , "
						+  COL_MESSAGE + " TEXT NOT NULL "+
						");";
	}
}
