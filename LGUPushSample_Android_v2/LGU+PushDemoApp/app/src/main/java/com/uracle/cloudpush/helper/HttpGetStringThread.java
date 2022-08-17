package com.uracle.cloudpush.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import android.os.Handler;
import android.os.Message;

import m.client.push.library.common.Logger;

/**
 * HttpGetStringThread
 * 메세지json 데이터 내의 EXT가 존재할 경우, 
 * EXT url로부터 상세 정보를 수신하기 위한 통신 쓰레드
 */
public class HttpGetStringThread extends Thread {
	
	private Handler mHandler;
	private String mHttpUrl;

	public HttpGetStringThread(Handler handler, String url) {
		mHandler = handler;
		mHttpUrl = url;
	}
	
	@Override
	public void run() {
		StringBuffer sb = new StringBuffer();
		
		try {
			if (mHttpUrl.contains("http")) {
				mHttpUrl = mHttpUrl.replaceAll("\\\\", "/");
			}
			else {
				//mHttpUrl = mHttpUrl.replaceAll("//", "/");
				Logger.d( "HttpGetStringThread mHttpUrl: " + mHttpUrl);
			}
			
			//mHttpUrl = "http://211.241.199.158:8180/msg/totalInfo/0218115649_msp.html";
			URL url = new URL(mHttpUrl);
			HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
			
			if (urlConn != null) {
				Logger.d( "HttpGetStringThread HttpURLConnection is not null");
				urlConn.setDoOutput(true);
				urlConn.setConnectTimeout(10000);

				int retCode = urlConn.getResponseCode();
				if (retCode == HttpURLConnection.HTTP_OK) {
					BufferedReader br = new BufferedReader(new InputStreamReader(urlConn.getInputStream(), "UTF-8"));
					
					String line = "";
					while ((line = br.readLine()) != null) {
						//line = new String(line.getBytes("ISO-8859-1"), "UTF-8"); // 한글???
						line = URLDecoder.decode(line, "UTF-8");
						//System.out.println(line);
						sb.append(line);
					}
					br.close();
				}
				else {
					Logger.e( "HttpGetStringThread Response Code = " + retCode);
				}
				
				urlConn.disconnect();
				
				Message msg = mHandler.obtainMessage(0, sb.toString());
				mHandler.sendMessage(msg);	
			}
			else {
				Logger.e( "HttpGetStringThread HttpURLConnection is null");
			}
			
		} 
		catch (MalformedURLException e) {
			Logger.e( "HttpGetStringThread INVALID URL:: " + e.getLocalizedMessage());
		} 
		catch (IOException e) {
			Logger.e( "HttpGetStringThread NETWORK ERROR:: " + e.getLocalizedMessage());
		}
	}
}