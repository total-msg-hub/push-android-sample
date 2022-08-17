package com.uracle.cloudpush;

import java.util.ArrayList;

import com.uracle.cloudpush.R;
import com.uracle.cloudpush.common.PushListInfo;
import com.uracle.cloudpush.db.DBUtils;
import com.uracle.cloudpush.helper.Define;

import m.client.push.library.PushManager;
import m.client.push.library.common.PushLog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class PushListAdapter extends BaseAdapter{

	private Context mContext;
	private ArrayList<PushListInfo> mList = new ArrayList<PushListInfo>();

	public PushListAdapter(Context context, ArrayList<PushListInfo> list) {
		mContext = context;
		mList = list;
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;

		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.moe_notify_list_row, null);

			viewHolder = new ViewHolder();
			viewHolder.checkbox = (ImageView)convertView.findViewById(R.id.deleteBtn);
			viewHolder.content_title = (TextView)convertView.findViewById(R.id.content_title);
			viewHolder.content = (TextView)convertView.findViewById(R.id.content);
			viewHolder.updateDate = (TextView)convertView.findViewById(R.id.updateDate);
			viewHolder.newicon = (TextView)convertView.findViewById(R.id.newicon);



			convertView.setTag(viewHolder);
			
		}else{
			viewHolder = (ViewHolder)convertView.getTag();
		}
		
		PushListInfo info = mList.get(position);

		
		viewHolder.content_title.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				goDetailPage(position, viewHolder);
			}
		});
		
		viewHolder.content.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				goDetailPage(position, viewHolder);
			}
		});
		
		viewHolder.updateDate.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				goDetailPage(position, viewHolder);
			}
		});
		
		viewHolder.checkbox.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				PushListInfo info = mList.get(position);
				if (info.getChecked()){
					viewHolder.checkbox.setImageResource(R.drawable.check_off);
				}else{
					viewHolder.checkbox.setImageResource(R.drawable.check_on);
				}
				info.setChecked(!info.getChecked());
//				mList.set(position, info);
			}
		});
		
		if (info.getChecked()){
			viewHolder.checkbox.setImageResource(R.drawable.check_on);
		}else{
			viewHolder.checkbox.setImageResource(R.drawable.check_off);
		}

		viewHolder.content_title.setText(info.getTitle());
		String sMessage = info.getMessage();
		sMessage = sMessage.replace("/r/n", " ");
		sMessage = sMessage.replace("/n"," ");
		viewHolder.content.setText(sMessage);
		viewHolder.updateDate.setText(info.getDate());

		if (info.getNew() == 1){
			viewHolder.newicon.setVisibility(View.VISIBLE);
		}else{
			viewHolder.newicon.setVisibility(View.INVISIBLE);	
		}
		viewHolder.info = info;
		
		return convertView;
	}

	public class ViewHolder
	{
		ImageView checkbox;
		TextView content_title;
		TextView content;
		TextView updateDate;
		TextView newicon;
		PushListInfo info;
	}
	
	public void goDetailPage(int position, ViewHolder vh){
		PushLog.d("PushListAdapter", "goDetailPage : " + position);
		PushListInfo info = vh.info;
		
		
		
		// New 아이콘 invisible
		info.setNew(0);
		vh.newicon.setVisibility(View.INVISIBLE);
		

		
		try{
			NotificationManager notifiyMgr = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
			int nKey = Integer.parseInt(info.getSeqNo());
			notifiyMgr.cancel(Define.KEY_TAG, nKey);
		}catch (Exception e){
			PushLog.d("PushListAdapter", e.getMessage());
		}
		
		String type = Integer.toString(info.getType());
		Intent intent = new Intent(mContext, DetailActivity.class);
		intent.putExtra("type", type);
		intent.putExtra("title", info.getTitle());
		intent.putExtra("message", info.getMessage());
		intent.putExtra("ext", info.getExt());
		PushLog.d("PushListAdapter", "type : " + type);
		PushLog.d("PushListAdapter", "title : " + info.getTitle());
		PushLog.d("PushListAdapter", "message : " + info.getMessage());
		PushLog.d("PushListAdapter", "ext : " + info.getExt());
		mContext.startActivity(intent);
		
		//read confirm 
		if(DBUtils.getIsAlreadyReadMessage(mContext, info.getSeqNo()) == false){
			// DBUpdate 필요
			DBUtils.getDbOpenHelper(mContext).updateNewPushMsg(info.getSeqNo());
			String msg = DBUtils.getMessage(mContext, info.getSeqNo());
			PushManager.getInstance().pushMessageReadConfirm(mContext, msg);
			int count = DBUtils.getUnReadCount(mContext);
			PushManager.getInstance().initBadgeNo(mContext, String.valueOf(count));
			PushManager.getInstance().setDeviceBadgeCount(mContext, String.valueOf(count));
		}
		
	}

	public void setList(ArrayList<PushListInfo> list){
		mList = list;
	}
}
