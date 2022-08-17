package com.uracle.cloudpush;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.uracle.cloudpush.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;


public class ImageDetailsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.moe_image_details_activity);
		
        String imagePath = getIntent().getStringExtra("imagePath");
        
        if (imagePath.startsWith("http")) {
        	new imageUrlLodingTask(imagePath).execute();
        }
    }

	private void setUI(Bitmap bitmap) {
			ImageView zoomImageView = (ImageView) findViewById(R.id.zoom_image);
			zoomImageView.setImageBitmap(bitmap);
	}
    
    class imageUrlLodingTask extends AsyncTask<Void, Void, Bitmap> {
    	private String imagePath = "";
    	private ProgressDialog mDlg;
    	
    	public imageUrlLodingTask(String imagePath) {
    		this.imagePath = imagePath;
    	}
    	
		@Override
		protected void onPreExecute() {
			mDlg = ProgressDialog.show(ImageDetailsActivity.this, "", "", true, false);
			mDlg.setContentView(R.layout.moe_progressdialoglayout);
			mDlg.getWindow().setBackgroundDrawable(new ColorDrawable(0));
			mDlg.show();
			
			super.onPreExecute();
		}

		@Override
        protected Bitmap doInBackground(Void... params) {
            try {
            	imagePath = imagePath.replaceAll(" ", "%20");
                URL url = new URL(imagePath);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                BitmapFactory.Options option = new BitmapFactory.Options();
        		option.inSampleSize = 2;
                return BitmapFactory.decodeStream(input, null, option);
            } 
            catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
		
		@Override
		protected void onPostExecute(Bitmap bitmap) {
			mDlg.dismiss();
			if (bitmap != null) {
				setUI(bitmap);
			}
		}
    }
    
    public void onClickCloseBtn(View view){
    	finish();
    }

}
