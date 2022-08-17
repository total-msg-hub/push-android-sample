package com.uracle.cloudpush;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.uracle.cloudpush.common.CommonDef;
import com.uracle.cloudpush.common.PushListInfo;
import com.uracle.cloudpush.common.Utils;
import com.uracle.cloudpush.db.DBUtils;
import com.uracle.cloudpush.db.DataBases;
import com.uracle.cloudpush.helper.ContentType;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import m.client.push.library.PushManager;
import m.client.push.library.common.Logger;
import m.client.push.library.common.PushConstants;
import m.client.push.library.common.PushConstantsEx;
import m.client.push.library.common.PushDefine;
import m.client.push.library.common.PushLog;
import m.client.push.library.utils.PushUtils;

public class PushListActivity extends Activity {
	
	private static final String TAG  = "PushListActivity";
	
	private ArrayList<PushListInfo> mPushList = new ArrayList<PushListInfo>();
	
	private Context mContext = (Context)this;
	private BroadcastReceiver mUnregBroadcastReceiver;
	
	ProgressDialog mProgressDialog = null;
	
	// 전체삭제 체크박스
	private boolean m_bAllDeleteChecked = false;
	
	// 전체 삭제 버튼
	ImageView allDeleteBtn;
	
	// push list Adapter
	public PushListAdapter mPushListAdapter;

	private long backPressedTime = 0;
	private int mTotalCount = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.moe_pushmessagebox);
		initPushList();

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				TextView upmc = findViewById(R.id.tv_upmc_url);
				Logger.e(PushUtils.getStringFromStorage(PushConstants.KEY_RECEIVER_SERVER_URL, getApplicationContext()));
				upmc.setText(PushUtils.getStringFromStorage(PushConstants.KEY_RECEIVER_SERVER_URL, getApplicationContext()));
			}
		});
	}
    

	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Logger.i("");
		
		//background >> foreground시 에만 적
		initPushList();
		registerReceiver();
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		unregisterReceiver();
	}
	
    @Override
    protected void onNewIntent(Intent intent) {
    	PushLog.d(TAG, "Enter PushListActivity onNewIntent");
    	
    	initPushList();
    	
    	super.onNewIntent(intent);
    	
    	registerReceiver();
    }
	
    @Override
    public void onBackPressed() {
		long tempTime        = System.currentTimeMillis();
		long intervalTime    = tempTime - backPressedTime;
		if ( 0 <= intervalTime && CommonDef.FINSH_INTERVAL_TIME >= intervalTime ) {
			super.onBackPressed();
			finishAffinity();
		} 
		else { 
			backPressedTime = tempTime; 
			Toast.makeText(getApplicationContext(),"'뒤로'버튼을 한번 더 누르시면 종료됩니다.",Toast.LENGTH_SHORT).show(); 
		} 
    }
    
    /////// getter/setter //////
	public boolean get_bAllDeleteChecked() {
		return m_bAllDeleteChecked;
	}

	public void set_bAllDeleteChecked(boolean m_bAllDeleteChecked) {
		this.m_bAllDeleteChecked = m_bAllDeleteChecked;
	}
    
    /////// 버튼이벤트 ///////
	
	// 전체 삭제 버튼 이벤트
    public void onClickAllDelete(View v){
    	PushLog.d(TAG, "Enter onClickAllDelete");
    	
    	setAllDeleteCheck(!get_bAllDeleteChecked());
    	
    	// 리스트 박스내 체크 UI 처리
    	setDeleateCheck();
    }
    
    // 로그아웃 버튼
    public void onClickLogout(View v){
    	PushLog.d(TAG, "Enter onClickLogout");
    	
    	Handler handler = new Handler(){
    		@Override
    		public void handleMessage(Message msg) {
    			switch(msg.what){
         			case -1: // 확인
         				showProgressBar();
         		    	// 자동로그인 설정
         				PushManager.getInstance().unregisterPushService(getApplicationContext());
         				break;
         			case -2: // 취소
         				break;
         			default:
         				break;
    			}
    		}
    	};
		
		Utils.createAlertDialog(mContext, getString(R.string.moe_alert_title), getString(R.string.moe_alert_logout), handler).show();

    }
    
    // 삭제버튼
    public void onClickDelete(View v){
		PushLog.d(TAG, "Enter onClickDelete");
		
    	if (!checked()){
    		Utils.createConfirmDialog(mContext, getString(R.string.moe_alert_title), getString(R.string.moe_alert_delete_noselect), null).show(); 
    		return;
    	}else{
    		Handler handler = new Handler(){
        		@Override
        		public void handleMessage(Message msg) {
        			switch(msg.what){
             			case -1: // 확인
             	    		int nTotalCount = mPushList.size();
             	    		for(int i=nTotalCount-1; i>=0; i--){
             	    			PushLog.d(TAG, "Enter onClickDelete i : " + i);
             	    			if (mPushList.get(i).getChecked()){
             	    				PushLog.d(TAG, "Enter onClickDeletem PushList.get(i).getChecked() : " + mPushList.get(i).getChecked());
             	    				// db 삭제
             	    				DBUtils.getDbOpenHelper(PushListActivity.this).deletePushMSG(mPushList.get(i).getSeqNo());
             	    				
             	    				;
             	    				
             	    				// 리스트 삭제
             	    				mPushList.remove(i);
             	    			}
             	    		}
             	    		
             	    		int count = DBUtils.getUnReadCount(mContext);
     	    				PushManager.getInstance().initBadgeNo(mContext, String.valueOf(count));
     	    				PushManager.getInstance().setDeviceBadgeCount(mContext, String.valueOf(count));
     	    				
             	    		// 전체삭제 해제
             	    		setAllDeleteCheck(false);
             	    		mPushListAdapter.notifyDataSetChanged();
             				break;
             			case -2: // 취소
             				break;
             			default:
             				break;
        			}
        		}
        	};
    		
    		Utils.createAlertDialog(mContext, getString(R.string.moe_alert_title), getString(R.string.moe_alert_delete), handler).show();

    	}
    }

    public void onClickChange(View v){
	    startActivity(new Intent(PushListActivity.this, LoginActivity.class));
	    finish();
    }
    
    ////// 기타 메서드 //////
    
    // 푸시 리시트를 초기화 한다.
    public void initPushList(){
    	mPushList.clear();
    	
    	Cursor cursor = DBUtils.getDbOpenHelper(this).getPushAppMassageALL();
		if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
			do {
				try {
					
					String msgkey = cursor.getString(cursor.getColumnIndex(DataBases.CreateDB.COL_MSGKEY));
					int nType = cursor.getInt(cursor.getColumnIndex(DataBases.CreateDB.COL_MSGTYPE));
					String title = cursor.getString(cursor.getColumnIndex(DataBases.CreateDB.COL_MSGTITLE));
					String message = cursor.getString(cursor.getColumnIndex(DataBases.CreateDB.COL_MSGCONTENT));
					String ext = cursor.getString(cursor.getColumnIndex(DataBases.CreateDB.COL_MSGEXT));
					int nNew = cursor.getInt(cursor.getColumnIndex(DataBases.CreateDB.COL_NEW));
					String date = DBUtils.convertToDate(cursor.getLong(cursor.getColumnIndex(DataBases.CreateDB.COL_DATE)));
					
					Logger.d( "msgkey:" + msgkey + ", nType:" + Integer.toString(nType) + ", title:" + title + ", message:" + message + ", ext:" + ext + ", date:" + date);

					// 일반 텍스트
    				if (nType == ContentType.TYPE_DEFAULT) {
    					mPushList.add(0, new PushListInfo(msgkey, nType, title, message, "", date, false, nNew));
    				}else {
    					mPushList.add(0, new PushListInfo(msgkey, nType, title, message, ext, date, false, nNew));
    				}
    				
				}catch (Exception e){
					
				}
			} while(cursor.moveToNext());
		}
		
		ListView listView = (ListView)this.findViewById(R.id.noticeListView);
		if (mPushListAdapter == null){
			mPushListAdapter = new PushListAdapter(this, mPushList);
			listView.setAdapter(mPushListAdapter);
		}else{
			listView.setAdapter(mPushListAdapter);
		}
		//notification data clear 
		NotificationManager mManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		mManager.cancelAll();
		
		
    }
    
    // 푸시 리스트 체크박스 UI 처리
    public void setDeleateCheck(){
    	for(int i=0; i<mPushList.size(); i++){
    		mPushList.get(i).setChecked(get_bAllDeleteChecked());
    	}
    	
    	mPushListAdapter.notifyDataSetChanged();
    }
    
    // 전체 삭제 체크박스 UI 처리
    public void setAllDeleteCheck(Boolean bCheck){
    	set_bAllDeleteChecked(bCheck);
    	if (bCheck){
    		ImageView iv = (ImageView)findViewById(R.id.deleteBtn);
    		iv.setImageResource(R.drawable.check_on);
    	}else{
    		ImageView iv = (ImageView)findViewById(R.id.deleteBtn);
    		iv.setImageResource(R.drawable.check_off);
    	}
    }
    
    // 체크박스가 선택된 것이 있는지 확인
    public boolean checked(){
    	int nTotalCount = mPushList.size();
 		for(int i=0; i<nTotalCount; i++){
 			if (mPushList.get(i).getChecked()){
 				return true;
 			}
 		}
 		return false;
    }
    
    public void registerReceiver() {
		if (mUnregBroadcastReceiver != null) {
			return;
		}

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(getPackageName()  + PushConstantsEx.ACTION_COMPLETED);
		intentFilter.addAction(getPackageName() + PushConstants.ACTION_UPNS_MESSAGE_ARRIVED);

		mUnregBroadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {

				Logger.e(intent.toString());
				if(intent.getAction().toString().equals(getPackageName()+PushConstants.ACTION_UPNS_MESSAGE_ARRIVED)){
					new Handler().postDelayed(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							initPushList();
							mPushListAdapter.notifyDataSetChanged();
						}
					}, 1000);

					return;
				};
				
				
				if(!PushUtils.checkValidationOfCompleted(intent, context)){
					return;
				}

				dissMissProgressBar();
				
				//intent 정보가 정상적인지 판단 
				String result = intent.getExtras().getString(PushConstants.KEY_RESULT);
				String bundle = intent.getExtras().getString(PushConstantsEx.KEY_BUNDLE);

				JSONObject result_obj = null;
				String resultCode = "";
				String resultMsg = "";
				try {
					result_obj = new JSONObject(result);
					resultCode = result_obj.getString(PushConstants.KEY_RESULT_CODE);
					resultMsg = result_obj.getString(PushConstants.KEY_RESULT_MSG);
				} catch (JSONException e) {
					e.printStackTrace();
				}

				//Action에 따라 분기 (이미 서비스 등록이 된 경우 다음 process 이동) 
				if (bundle.equals(PushConstantsEx.COMPLETE_BUNDLE.UNREG_PUSHSERVICE)) { 
					if (resultCode.equals(PushConstants.SUCCESS_RESULT_CODE)) {
						Toast.makeText(context, "해제 성공!", Toast.LENGTH_SHORT).show();
         				Utils.setConfigString(mContext, CommonDef.CONFIG_SAVE_LOGIN, "N");
         				startActivity(new Intent(PushListActivity.this, LoginActivity.class));
         				finish();
					}else {
						Toast.makeText(context, "[LoginActivity] error code: " + resultCode + " msg: " + resultMsg, Toast.LENGTH_SHORT).show();
					}
				}else if(bundle.equals(PushConstantsEx.COMPLETE_BUNDLE.IS_REGISTERED_SERVICE)){
					String isRegister = "";
					try {
						isRegister = result_obj.getString(PushConstants.KEY_ISREGISTER);
					} catch (JSONException e) {
						e.printStackTrace();
					}
					if(isRegister.equals("C")){
						Toast.makeText(context, "CHECK ON [ 사용자 재등록 필요 !! ]", Toast.LENGTH_LONG ).show();
					}else if(isRegister.equals("N")){
						Toast.makeText(context, "CHECK ON [ 서비스 재등록 필요 !! ]", Toast.LENGTH_LONG).show();
					}else{
						Logger.i("서비스 정상 등록 상태 ");
					}
				}
			}
		};

		LocalBroadcastManager.getInstance(this).registerReceiver(mUnregBroadcastReceiver, intentFilter);
	}

	public void unregisterReceiver() {
		if (mUnregBroadcastReceiver != null) {
			LocalBroadcastManager.getInstance(this).unregisterReceiver(mUnregBroadcastReceiver);
			mUnregBroadcastReceiver = null;
		}
	}
    
	public void showProgressBar(){
		mProgressDialog = ProgressDialog.show(this, "", "", true, false);
		mProgressDialog.setContentView(R.layout.moe_progressdialoglayout);
		mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
		mProgressDialog.show();
	}
	
	public void dissMissProgressBar(){
		if (mProgressDialog != null){
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}
	
	@Override
	 protected void onRestoreInstanceState(Bundle savedInstanceState){
		Logger.i("");
		initPushList();
	 }
}
