package com.uracle.cloudpush.common;

public class PushListInfo {
	private String seqNo;
	private int type; // 0:text, 1:web, 2:동영상, 3:이미지
	private String title;
	private String message;
	private String ext;
	private String date;
	private boolean bChecked;
	private int nNew;

	public PushListInfo(String seqNo, int type, String title, String message, String ext, String date, boolean checked, int isNew) {
		this.seqNo = seqNo;
		this.type = type;
		this.title = title;
		this.message = message;
		this.ext = ext;
		this.date = date;
		this.bChecked = checked;
		this.nNew = isNew;
	}

	public String getSeqNo() {
		return seqNo;
	}
	public int getType() {
		return type;
	}
	public String getTitle() {
		return title;
	}
	public String getMessage() {
		return message;
	}
	public String getExt() {
		return ext;
	}
	public String getDate() {
		return date;
	}
	public boolean getChecked() {
		return bChecked;
	}
	public void setChecked(boolean checked) {
		this.bChecked = checked;
	}
	public int getNew() {
		return nNew;
	}
	public void setNew(int isNew) {
		this.nNew = isNew;
	}
}
