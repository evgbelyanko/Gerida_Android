package app.gerida;

import java.nio.charset.Charset;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;

import app.gerida.AndroidMultiPartEntity.ProgressListener;

public class UploadActivity extends Activity {
	// LogCat tag
	private static final String TAG = MainActivity.class.getSimpleName();

	private ProgressBar progressBar;
	private String filePath = null;
	private TextView txtPercentage;
	private ImageView imgPreview;
	private VideoView vidPreview;
	private String photo_desc = null;
    private String photo_title = null;
	private Button btnOK;
	long totalSize = 0;

	@SuppressLint("ResourceType")
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_upload);
		txtPercentage = (TextView) findViewById(R.id.txtPercentage);
		final Button btnUpload = (Button) findViewById(R.id.btnUpload);
		Button btnNext = (Button) findViewById(R.id.btnNext);
        btnOK = (Button) findViewById(R.id.btnOK);
		final LinearLayout layoutPhoto = (LinearLayout) findViewById(R.id.layoutPhoto);
		final LinearLayout layoutDesc = (LinearLayout) findViewById(R.id.layoutDesc);
		final LinearLayout layoutPercent = (LinearLayout) findViewById(R.id.layoutPercent);
        final EditText editText_title = (EditText) findViewById(R.id.editText_title);
		final EditText editText_desc = (EditText) findViewById(R.id.editText_desc);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		imgPreview = (ImageView) findViewById(R.id.imgPreview);
		vidPreview = (VideoView) findViewById(R.id.videoPreview);

		ActionBar actionBar = getActionBar();
		//MyLocationListener.imHere

		// Changing action bar background color
		actionBar.setBackgroundDrawable(
				new ColorDrawable(Color.parseColor(getResources().getString(
						R.color.action_bar))));

		// Receiving the data from previous activity
		Intent i = getIntent();

		// image or video path that is captured in previous activity
		filePath = i.getStringExtra("filePath");

		// boolean flag to identify the media type, image or video
		boolean isImage = i.getBooleanExtra("isImage", true);

		if (filePath != null) {
			// Displaying the image or video on the screen
			previewMedia(isImage);
		} else {
			Toast.makeText(getApplicationContext(),
					"Sorry, file path is missing!", Toast.LENGTH_LONG).show();
		}

        btnOK.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });
		btnNext.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				layoutDesc.setVisibility(View.VISIBLE);
				layoutPhoto.setVisibility(View.GONE);
			}
		});
		btnUpload.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				layoutPercent.setVisibility(View.VISIBLE);
				layoutDesc.setVisibility(View.GONE);
                photo_title = editText_title.getText().toString().trim();
				photo_desc = editText_desc.getText().toString().trim();

				// uploading the file to server
				new UploadFileToServer().execute();
			}
		});

	}

	/**
	 * Displaying captured image/video on the screen
	 * */
	private void previewMedia(boolean isImage) {
		// Checking whether captured media is image or video
		if (isImage) {
			imgPreview.setVisibility(View.VISIBLE);
			vidPreview.setVisibility(View.GONE);
			// bimatp factory
			BitmapFactory.Options options = new BitmapFactory.Options();

			// down sizing image as it throws OutOfMemory Exception for larger
			// images
			options.inSampleSize = 8;

			final Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);

			imgPreview.setImageBitmap(bitmap);
		} else {
			imgPreview.setVisibility(View.GONE);
			vidPreview.setVisibility(View.VISIBLE);
			vidPreview.setVideoPath(filePath);
			// start playing
			vidPreview.start();
		}
	}

	/**
	 * Uploading the file to server
	 * */
	private class UploadFileToServer extends AsyncTask<Void, Integer, String> {
		@Override
		protected void onPreExecute() {
			// setting progress bar to zero
			progressBar.setProgress(0);
			super.onPreExecute();
		}

		@SuppressLint("SetTextI18n")
        @Override
		protected void onProgressUpdate(Integer... progress) {
			// Making progress bar visible
			progressBar.setVisibility(View.VISIBLE);

			// updating progress bar value
			progressBar.setProgress(progress[0]);

			// updating percentage value
			txtPercentage.setText(String.valueOf(progress[0]) + "%");
		}

		@Override
		protected String doInBackground(Void... params) {
			return uploadFile();
		}

		@SuppressWarnings("deprecation")
		private String uploadFile() {
			String responseString = null;

			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(Config.FILE_UPLOAD_URL);

			try {
				AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
						new ProgressListener() {

							@Override
							public void transferred(long num) {
								publishProgress((int) ((num / (float) totalSize) * 100));
							}
						});

				File sourceFile = new File(filePath);

				// Adding file data to http body
				entity.addPart("image", new FileBody(sourceFile));

				// Extra parameters if you want to pass to server
				Charset charSet = Charset.forName("UTF-8");
				entity.addPart("photo_title", new StringBody( photo_title, charSet) );
				entity.addPart("photo_desc", new StringBody( photo_desc, charSet) );
				entity.addPart("latitude", new StringBody( Double.toString(MyLocationListener.latitude)) );
				entity.addPart("longitude", new StringBody( Double.toString(MyLocationListener.longitude)) );
				entity.addPart("user_id", new StringBody( MainActivity.USER_ID) );

				totalSize = entity.getContentLength();
				httppost.setEntity(entity);

				// Making server call
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity r_entity = response.getEntity();

				int statusCode = response.getStatusLine().getStatusCode();
				if (statusCode == 200) {
					// Server response
					//responseString = EntityUtils.toString(r_entity,"UTF-8");
					responseString = EntityUtils.toString(r_entity);
				} else {
					responseString = "Error occurred! Http Status Code: "
							+ statusCode;
				}

			} catch (ClientProtocolException e) {
				responseString = e.toString();
			} catch (IOException e) {
				responseString = e.toString();
			}

			return responseString;

		}

		@Override
		protected void onPostExecute(String result) {
			Log.e(TAG, "Response from server: " + result);

			// showing the server response in an alert dialog
			showAlert(result);

			btnOK.setVisibility(View.VISIBLE);

			super.onPostExecute(result);
		}

	}

	/**
	 * Method to show alert dialog
	 * */
	private void showAlert(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message).setTitle("Response from Servers")
				.setCancelable(false)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// do nothing
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

}