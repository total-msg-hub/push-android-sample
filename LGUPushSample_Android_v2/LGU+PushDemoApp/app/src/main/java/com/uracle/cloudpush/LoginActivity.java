package com.uracle.cloudpush;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;


import com.reginald.editspinner.EditSpinner;
import com.uracle.cloudpush.common.CommonDef;
import com.uracle.cloudpush.common.Utils;
import com.uracle.cloudpush.db.DBUtils;
import com.uracle.cloudpush.db.DbOpenHelper;
import com.uracle.cloudpush.receiver.RegisterBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import m.client.push.library.PushManager;
import m.client.push.library.common.Logger;
import m.client.push.library.common.PushConstants;
import m.client.push.library.common.PushConstantsEx;
import m.client.push.library.utils.PushUtils;

public class LoginActivity extends Activity {

	private static final String TAG  = "LoginActivity";

	private BroadcastReceiver mLoginBroadcastReceiver;

	private TextView mLoginPhoneText;
	private EditText mUserNameEdit;
	private Button mLoginButton;
	ProgressDialog mProgressDialog = null;
	
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    InitUI();
		

	}

	EditSpinner mEditSpinner;
	private void InitUI(){
		setContentView(R.layout.moe_login);

		mEditSpinner = (EditSpinner) findViewById(R.id.edit_spinner);
		ListAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,
				getResources().getStringArray(R.array.upmc));
		mEditSpinner.setAdapter(adapter);
		mEditSpinner.setEditable(false);

		String customUrl = PushUtils.getStringFromStorage(PushConstants.KEY_RECEIVER_SERVER_URL, getApplicationContext());
		if(!TextUtils.isEmpty(customUrl)){
			mEditSpinner.setText(customUrl);
		}

		// triggered when dropdown popup window dismissed
		mEditSpinner.setOnDismissListener(new PopupWindow.OnDismissListener() {
			@Override
			public void onDismiss() {

			}
		});

		// triggered when dropdown popup window shown
		mEditSpinner.setOnShowListener(new EditSpinner.OnShowListener() {
			@Override
			public void onShow() {
			}
		});

		// triggered when one item in the list is clicked
		mEditSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			}
		});


		//mEditSpinner.setDropDownDrawable(getResources().getDrawable(R.drawable.picker), 60, 60);
		// set the spacing bewteen Edited area and DropDown click area
		//mEditSpinner.setDropDownDrawableSpacing(50);
		//mEditSpinner.setDropDownWidth(500);
		// set DropDown popup window background
		//mEditSpinner.setDropDownBackgroundResource(R.drawable.your_custom_dropdown_bkg);


		// cuid
		mLoginPhoneText = (TextView) LoginActivity.this.findViewById(R.id.id);

		//mLoginPhoneText.setText(getString(R.string.demo_id));

		// cname
		mUserNameEdit = (EditText) LoginActivity.this.findViewById(R.id.pwd);

		//mUserNameEdit.setText(getString(R.string.demo_name));
		//mUserNameEdit.setFocusable(false);

		mLoginButton = (Button)findViewById(R.id.login);
		mLoginButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				onLogin(view);
			}
		});

	
		
	//	PushManager.getInstance().initPushServer(getApplicationContext());
		
	}
	
	
	

	public void onLogin(View view){
		if (mLoginPhoneText == null || mEditSpinner.getText().toString().length() == 0) {
			Toast.makeText(LoginActivity.this,"UPMC 정보를 선택하세요", Toast.LENGTH_SHORT).show();
			return;
		}

		if (mLoginPhoneText == null || mLoginPhoneText.getText().toString().length() == 0) {
			Toast.makeText(LoginActivity.this, getString(R.string.moe_not_support_phone), Toast.LENGTH_SHORT).show();
			return;
		}
		if (mUserNameEdit == null || mUserNameEdit.getText().toString().length() == 0) {
			Toast.makeText(LoginActivity.this, getString(R.string.moe_login_enter_pw), Toast.LENGTH_SHORT).show();
			return;
		}

		// FIX_IT
		JSONObject params = new JSONObject();
		try {
			params.put(PushConstants.KEY_CUSTOM_RECEIVER_SERVER_URL, mEditSpinner.getText().toString());
			params.put(PushConstants.KEY_CUID, mLoginPhoneText.getText().toString());
			params.put(PushConstants.KEY_CNAME, mUserNameEdit.getText().toString());

		} 
		catch (JSONException e) {
			e.printStackTrace();
		}
		Utils.showProgressBar(LoginActivity.this);
		
		PushManager.getInstance().registerServiceAndUser(getApplicationContext(), params);

		//Toast.makeText(getApplicationContext(), mEditSpinner.getText().toString(), Toast.LENGTH_LONG).show();
		//PushUtils.setStringToStorage(PushConstants.KEY_CUSTOM_RECEIVER_SERVER_URL, mEditSpinner.getText().toString(), getApplicationContext());

		DBUtils.getDbOpenHelper(this).databaseDrop();
		DBUtils.getDbOpenHelper(this).open();
	}


	@Override
	protected void onResume() {
		super.onResume();
		
		registerReceiver();
		Logger.i("onResume");

	}

	@Override
	protected void onPause() {
		super.onPause();

		unregisterReceiver();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();

		System.exit(0);
	}

	public void registerReceiver() {
		if (mLoginBroadcastReceiver != null) {
			return;
		}

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(LoginActivity.this.getPackageName()  + PushConstantsEx.ACTION_COMPLETED);

		mLoginBroadcastReceiver = new RegisterBroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				super.onReceive(context, intent);

				if(!PushUtils.checkValidationOfCompleted(intent, context)){
					return;
				}

				Utils.dissMissProgressBar();

				if (bundle.equals(PushConstantsEx.COMPLETE_BUNDLE.REG_SERVICE_AND_USER)) { 

					if (resultCode.equals(PushConstants.SUCCESS_RESULT_CODE)) {
						Toast.makeText(context, "로그인 성공!", Toast.LENGTH_SHORT).show();
						// 자동로그인 설정
						Utils.setConfigString(context, CommonDef.CONFIG_SAVE_LOGIN, "Y");
						
						Logger.e("CUID : " + mLoginPhoneText.getText().toString());
						Logger.e("CNAME : " + mUserNameEdit.getText().toString());
						mLoginPhoneText.getText().toString();
						PushUtils.setStringToStorage(CommonDef.CONFIG_USER_CUID, mLoginPhoneText.getText().toString(), getApplicationContext());
						PushUtils.setStringToStorage(CommonDef.CONFIG_USER_NAME,mUserNameEdit.getText().toString(), getApplicationContext());
						goPushMessageBox();
					}else {
						Toast.makeText(context, "서비스 등록 오류 error code: " + resultCode + " msg: " + resultMsg, Toast.LENGTH_SHORT).show();
					}
				}
			}
		};

		LocalBroadcastManager.getInstance(this).registerReceiver(mLoginBroadcastReceiver, intentFilter);
	}

	public void unregisterReceiver() {
		if (mLoginBroadcastReceiver != null) {
			LocalBroadcastManager.getInstance(this).unregisterReceiver(mLoginBroadcastReceiver);
			mLoginBroadcastReceiver = null;
		}
	}

	private void goPushMessageBox(){
		startActivity(new Intent(LoginActivity.this, PushListActivity.class));
		finish();
	}



	public void onExit(View v){
		onBackPressed();
	}

	



}
