package com.uracle.cloudpush;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.uracle.cloudpush.common.CommonDef;
import com.uracle.cloudpush.helper.ContentType;
import com.uracle.cloudpush.helper.PushInfo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import m.client.push.library.common.PushLog;
import m.client.push.library.utils.PushUtils;

public class DetailActivity extends Activity{

	WebView mWebview;
	private String mImagePath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.moe_detailpage);
		

		
		InitUI();
	}
	
	OnClickListener mZoomClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			
			TextView tv_Text = (TextView)findViewById(R.id.detail_text);
			if(v.getTag().toString().equals("PLUS")){
				tv_Text.setTextSize(TypedValue.COMPLEX_UNIT_PX, tv_Text.getTextSize() + 5.0f);
			}else{
				tv_Text.setTextSize(TypedValue.COMPLEX_UNIT_PX, tv_Text.getTextSize() - 5.0f);
			}
			
			PushUtils.setIntToStorage(CommonDef.CONFIG_FONT_SIZE, (int)tv_Text.getTextSize(), getApplicationContext());
		}
	};
	
	public void InitUI(){
		TextView tv_Title = (TextView)findViewById(R.id.detail_title);
		mWebview = (WebView)findViewById(R.id.detail_webview);
		mWebview.setBackgroundColor(Color.RED);
		TextView tv_Text = (TextView)findViewById(R.id.detail_text);
		ScrollView scv = (ScrollView)findViewById(R.id.scrollview);
	
		
		int message_type = Integer.parseInt(this.getIntent().getStringExtra("type"));
		
		String title = this.getIntent().getStringExtra("title");
		String message = this.getIntent().getStringExtra("message");
		// 메시지
		message = message.replaceAll("/n", "\n");
		//message = message.replaceAll("/r", "");
		
		//String str = "(?i)\\b((?:https?://|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:\'\".,<>?«»“”‘’]))";
		//Pattern patt = Pattern.compile(str);
		//Matcher matcher = patt.matcher(message);
		//message = matcher.replaceAll("<a href=\"$1\">$1</a>");
		
		
/*		TextView tv_plus = (TextView)findViewById(R.id.plus);
		tv_plus.setTag("PLUS");
		TextView tv_minus = (TextView)findViewById(R.id.minus);
		tv_minus.setTag("MINUS");
		tv_plus.setOnClickListener(mZoomClickListener);
		tv_minus.setOnClickListener(mZoomClickListener);*/
		
		float fontSize = (float)PushUtils.getIntFromStorage(CommonDef.CONFIG_FONT_SIZE, getApplicationContext());
		if(fontSize <= 0){
			fontSize = tv_Text.getTextSize();
		}

		tv_Text.setTextSize(TypedValue.COMPLEX_UNIT_PX,fontSize);
		
		
		configureWebSetting();
		
		// 제목
		//tv_Title.setText(title);
			
		if (message_type == ContentType.TYPE_IMAGE_HTML) { //동영상
			// 이미지의 경우 메시지가 없으므로 메시지를 넣어줌.
			String exts = this.getIntent().getStringExtra("ext"); // "imgURL|webURL"

			JSONObject object = null;
			try {
				object = new JSONObject(exts);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			String imageUrl = object.optString(PushInfo.KEY_IMG_URL);
			String htmlUrl = object.optString(PushInfo.KEY_HTML_URL);





			if(!TextUtils.isEmpty(imageUrl)){
				mWebview.setVisibility(View.VISIBLE);
				mWebview.loadUrl(imageUrl);
			}
			if(TextUtils.isEmpty(message) == false){
				String source = "<b> " + title +
						" </b><br><br>";


				tv_Text.setText(Html.fromHtml(source+txtToHtml(message)));
				tv_Text.setVisibility(View.VISIBLE);
				scv.setVisibility(View.VISIBLE);
			}

			if(!TextUtils.isEmpty(htmlUrl)){
				final String link = htmlUrl;
				Button gotoVideo = findViewById(R.id.goto_video);
				gotoVideo.setVisibility(View.VISIBLE);
				gotoVideo.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						mWebview.loadUrl(link);
						mWebview.setVisibility(View.VISIBLE);
					}
				});
			}
		}else if (message_type == ContentType.TYPE_IMAGE){
			// 이미지의 경우 메시지가 없으므로 메시지를 넣어줌.
			String sUrl = this.getIntent().getStringExtra("ext");
			PushLog.d("JOY", "InitUI url : " + sUrl);
			if(!TextUtils.isEmpty(sUrl)){
				mWebview.setVisibility(View.VISIBLE);
				mWebview.loadUrl(sUrl);
			}
			if(TextUtils.isEmpty(message) == false){
				String source = "<b> &lt;" + title +
			            "&gt; </b><br><br>";


				tv_Text.setText(Html.fromHtml(source+txtToHtml(message)));
				tv_Text.setVisibility(View.VISIBLE);
				scv.setVisibility(View.VISIBLE);
			}
			
		}else{
			tv_Title.setText("상세보기");
			// 메시지
			String source = "<b> " + title +
		            "</b><br><br>";

			tv_Text.setText(Html.fromHtml(source+txtToHtml(message)));
			tv_Text.setVisibility(View.VISIBLE);


			scv.setVisibility(View.VISIBLE);
		}
	}


	
	private static String txtToHtml(String s) {
        StringBuilder builder = new StringBuilder();
        boolean previousWasASpace = false;
        for (char c : s.toCharArray()) {
            if (c == ' ') {
                if (previousWasASpace) {
                    builder.append("&nbsp;");
                    previousWasASpace = false;
                    continue;
                }
                previousWasASpace = true;
            } else {
                previousWasASpace = false;
            }
            switch (c) {
                case '<':
                    builder.append("&lt;");
                    break;
                case '>':
                    builder.append("&gt;");
                    break;
                case '&':
                    builder.append("&amp;");
                    break;
                case '"':
                    builder.append("&quot;");
                    break;
                case '\r':
                    builder.append("");
                case '\n':
                    builder.append("<br>");
                    break;
                // We need Tab support here, because we print StackTraces as HTML
                case '\t':
                    builder.append("&nbsp; &nbsp; &nbsp;");
                    break;
                default:
                    builder.append(c);

            }
        }
        String converted = builder.toString();
        String str = "(?i)\\b((?:https?://|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:\'\".,<>?«»“”‘’]))";
        Pattern patt = Pattern.compile(str);
        Matcher matcher = patt.matcher(converted);
        converted = matcher.replaceAll("<a href=\"$1\">$1</a>");
        return converted;
    }
	
	public void onClickCloseBtn(View v){
		onBackPressed();
	}
	
	public void configureWebSetting(){
		
		WebSettings settings = mWebview.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setJavaScriptCanOpenWindowsAutomatically(true);
		settings.setUseWideViewPort(true);
		settings.setLoadWithOverviewMode(true);
		settings.setDomStorageEnabled(true);
		//settings.setAppCacheEnabled(true);
		settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		
//		mWebview.setWebViewClient(new MyWebViewClient());
	}
	
//	private class MyWebViewClient extends WebViewClient {
//	    @Override
//	    public boolean shouldOverrideUrlLoading(WebView view, String url) {
//	    	PushLog.d("JOY", "shouldOverrideUrlLoading url : " + url);
//	        if (Uri.parse(url).getHost().contains("http://receiver.morpheus.kr")) {
//	            // This is my web site, so do not override; let my WebView load the page
//	            return true;
//	        }
//	        // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
//	        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//	        startActivity(intent);
//	        return true;
//	    }
//	    
//	}
	
	
	public void onShowDetailImage(View view){
		Intent imgDetailView = new Intent(DetailActivity.this, ImageDetailsActivity.class);
		imgDetailView.putExtra("imagePath", mImagePath);
		startActivity(imgDetailView);
		
	}
	
	
	
}
