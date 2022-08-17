package com.uracle.cloudpush.receiver;

import org.json.JSONException;
import org.json.JSONObject;

import com.uracle.cloudpush.common.CommonDef;
import com.uracle.cloudpush.common.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import m.client.push.library.common.Logger;
import m.client.push.library.common.PushConstants;
import m.client.push.library.common.PushConstantsEx;
import m.client.push.library.utils.PushUtils;

public class RegisterBroadcastReceiver extends BroadcastReceiver{


	
		protected String result = "";
		protected String bundle = "";
		protected JSONObject result_obj = null;
		protected String resultCode = "";
		protected String resultMsg = "";

		@Override
		public void onReceive(Context context, Intent intent) {

			if(!PushUtils.checkValidationOfCompleted(intent, context)){
				return;
			}

			

			result = intent.getExtras().getString(PushConstants.KEY_RESULT);
			bundle = intent.getExtras().getString(PushConstantsEx.KEY_BUNDLE);

		
			try {
				result_obj = new JSONObject(result);
				resultCode = result_obj.getString(PushConstants.KEY_RESULT_CODE);
				resultMsg = result_obj.getString(PushConstants.KEY_RESULT_MSG);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		//dissMissProgressBar();

		//intent 정보가 정상적인지 판단 
		String result = intent.getExtras().getString(PushConstants.KEY_RESULT);
		String bundle = intent.getExtras().getString(PushConstantsEx.KEY_BUNDLE);

	
		//Action에 따라 분기 (이미 서비스 등록이 된 경우 다음 process 이동) 
		if(bundle.equals(PushConstantsEx.COMPLETE_BUNDLE.REG_USER)) { 
			if (resultCode.equals(PushConstants.SUCCESS_RESULT_CODE)) {
				Toast.makeText(context, "로그인 성공!", Toast.LENGTH_SHORT).show();
			}else {
				Toast.makeText(context, "푸시 등록 error code: " + resultCode + " msg: " + resultMsg, Toast.LENGTH_SHORT).show();
			}
		}else if (bundle.equals(PushConstantsEx.COMPLETE_BUNDLE.UNREG_PUSHSERVICE)) { 

			if (resultCode.equals(PushConstants.SUCCESS_RESULT_CODE)) {
				Toast.makeText(context, "해제 성공!", Toast.LENGTH_SHORT).show();
			}else {

				Toast.makeText(context, "[LoginActivity] error code: " + resultCode + " msg: " + resultMsg, Toast.LENGTH_SHORT).show();
			}
		}else if (bundle.equals(PushConstantsEx.COMPLETE_BUNDLE.REG_GROUP)) { 

			if (resultCode.equals(PushConstants.SUCCESS_RESULT_CODE)) {
				Toast.makeText(context, "그룹 등록 성공!", Toast.LENGTH_SHORT).show();
			}else {
				Toast.makeText(context, "[LoginActivity] error code: " + resultCode + " msg: " + resultMsg, Toast.LENGTH_SHORT).show();
			}
		}else if (bundle.equals(PushConstantsEx.COMPLETE_BUNDLE.UNREG_GROUP)) { 

			if (resultCode.equals(PushConstants.SUCCESS_RESULT_CODE)) {
				Toast.makeText(context, "그룹 해제 성공!", Toast.LENGTH_SHORT).show();
			}else {
				Toast.makeText(context, "[LoginActivity] error code: " + resultCode + " msg: " + resultMsg, Toast.LENGTH_SHORT).show();
			}
		}else if (bundle.equals(PushConstantsEx.COMPLETE_BUNDLE.REG_SERVICE_AND_USER)) { 

			if (resultCode.equals(PushConstants.SUCCESS_RESULT_CODE)) {
				Toast.makeText(context, "로그인 성공!", Toast.LENGTH_SHORT).show();
				// 자동로그인 설정
				Utils.setConfigString(context, CommonDef.CONFIG_SAVE_LOGIN, "Y");
				
			}else {
				Toast.makeText(context, "서비스 등록 오류 error code: " + resultCode + " msg: " + resultMsg, Toast.LENGTH_SHORT).show();
			}
		}else if (bundle.equals(PushConstantsEx.COMPLETE_BUNDLE.INITBADGENO)) { 

			if (resultCode.equals(PushConstants.SUCCESS_RESULT_CODE)) {
				Toast.makeText(context, "Badge Number 초기화 성공 !", Toast.LENGTH_SHORT).show();
				//PushManager.getInstance().setDeviceBadgeCount(context, "0");
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

}
