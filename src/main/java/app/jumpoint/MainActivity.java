package app.jumpoint;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.ValueCallback;
import android.widget.Toast;
import android.database.Cursor;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends Activity {
	
	// LogCat tag
	private static final String TAG = MainActivity.class.getSimpleName();

	public static String USER_ID;
	 // Camera activity request codes
	private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
	private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;
	
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;

	public static final String USER_AGENT = "Mozilla/5.0 (Linux; Android 4.1.1; Galaxy Nexus Build/JRO03C) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.166 Mobile Safari/535.19";

	private Uri fileUri; // file url to store image/video

	private static final int SELECT_AVATAR = 1;

	private ValueCallback<Uri> mCallback;
	private ValueCallback<Uri[]> mArrayCallback;
	 
	 //private Button btnCapturePicture, btnRecordVideo;
	private WebView web;

	@SuppressLint("JavascriptInterface")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		 MyLocationListener.SetUpLocationListener(this);
		setContentView(R.layout.activity_main);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		

		this.web = (WebView) this.findViewById(R.id.webIndex);
		web.getSettings().setJavaScriptEnabled(true);
		web.getSettings().setDomStorageEnabled(true);
		web.getSettings().setDefaultTextEncodingName("utf-8");
		web.getSettings().setLoadWithOverviewMode(true);
		web.getSettings().setAllowFileAccess(true);
		web.getSettings().setAllowContentAccess(true);
		web.getSettings().setUseWideViewPort(true);
		web.getSettings().setAppCacheMaxSize( 5 * 1024 * 1024 ); // 5MB
		web.getSettings().setAppCachePath( getApplicationContext().getCacheDir().getAbsolutePath() );
		web.getSettings().setAppCacheEnabled( true );
		web.getSettings().setUserAgentString(USER_AGENT);
		web.setWebViewClient(new WebViewClient() {
			public void onPageStarted(WebView web, String url) {
			}
			// Выполнение после загрузки страницы
			public void onPageFinished(WebView web, String url) {
				//web.loadUrl("javascript: file:///android_asset/script.js");
				web.loadUrl("javascript: " +
								"localStorage.device = 'mobile'; " +
								"localStorage.deviceOS = 'android'; " +
								"android.onDataUserID(localStorage.userId);"
				);
			}
			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				Config.PAGE_ERROR = 1;
				web.loadUrl("file:///android_asset/error.html");

			}
			// Проверка URL после клика
			// Открывает браузер, если это не приложение и не авторизация вк
			@Override
			public boolean shouldOverrideUrlLoading(WebView webView, String url) {
			// все ссылки, в которых содержится домен
			// будут открываться внутри приложения
				String[] allowUrls = {
						Config.SERVER_URL,
						Config.CLIENT_URL,
						"https://login.vk.com",
						"https://oauth.vk.com",
						"https://m.facebook.com/v3",
						"https://facebook.com/dialog",
						"https://m.facebook.com/dialog",
						"https://facebook.com/login.php",
						"https://m.facebook.com/login.php",
						"https://accounts.google.com",
						"https://accounts.youtube.com"
				};

				for (int i = 0; i < allowUrls.length; i++) {
					if(url.contains(allowUrls[i])){
						return false;
					}
				}
				// все остальные ссылки будут спрашивать какой браузер открывать
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				startActivity(intent);
				return true;
			}
		});

		web.setWebChromeClient(new WebChromeClient() {
			@Override
			public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> callback, WebChromeClient.FileChooserParams fileChooserParams) {
				runFileChooser(null, callback);
				return true;
			}

			@SuppressWarnings("unused")
			public void openFileChooser(ValueCallback<Uri> callback, String accept, String capture) {
				this.openFileChooser(callback);
			}

			@SuppressWarnings("unused")
			public void openFileChooser(ValueCallback<Uri> callback, String accept) {
				this.openFileChooser(callback);
			}

			public void openFileChooser(ValueCallback<Uri> callback) {
				runFileChooser(callback, null);
			}

			public void runFileChooser(ValueCallback<Uri> callback, ValueCallback<Uri[]> arrayCallback) {
				mCallback = callback;
				mArrayCallback = arrayCallback;
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_AVATAR);
			}

		});

		web.addJavascriptInterface(new JSInterface(), "android");
		web.loadUrl(Config.SERVER_ADDR);

		View decorView = getWindow().getDecorView();
		int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE;
		decorView.setSystemUiVisibility(uiOptions);
		ActionBar actionBar = getActionBar();
		actionBar.hide();

		  //btnCapturePicture = (Button) findViewById(R.id.btnCapturePicture);
		  //btnRecordVideo = (Button) findViewById(R.id.btnRecordVideo);


		
		  // Checking camera availability
		if (!isDeviceSupportCamera()) {
			Toast.makeText(getApplicationContext(),
				"Устройство не поддерживает камеру!",
				Toast.LENGTH_LONG).show();
				// will close the app if the device does't have camera
				//finish();
		}
	 }
		// Активация кнопки назад
		@Override
		public void onBackPressed() {
			if (web.canGoBack() && Config.PAGE_ERROR == 0) {
				web.goBack();
			} else {
				super.onBackPressed();
			}
		}

	 public class JSInterface {
		@JavascriptInterface
		public void loadCaptureImage() {
/*			Toast.makeText(getApplicationContext(),
					"+"+MyLocationListener.statusLoc,
					100).show();*/
			if(MyLocationListener.longitude == 0.0 || MyLocationListener.latitude == 0.0){
				Toast.makeText(getApplicationContext(),
					"Включите местоположение",
					100).show();
			} else {
				captureImage();
			}
		}
		@JavascriptInterface
		public void onDataUserID(String value) {
			USER_ID = value;
		}
	 }
	 /**
	  * Checking device has camera hardware or not
	  * */
	 private boolean isDeviceSupportCamera() {
		 // this device has a camera
// no camera on this device
		 return getApplicationContext().getPackageManager().hasSystemFeature(
				 PackageManager.FEATURE_CAMERA);
 }

		public String getPath(Uri uri) {
			if (uri == null) {
				return "";
			}
			String[] projection = {MediaStore.Images.Media.DATA};
			Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
			if (cursor != null) {
				int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				cursor.moveToFirst();
				String path = cursor.getString(column_index);
				cursor.close();
				return path;
			}
			return uri.getPath();
		}
 
	 /**
	  * Launching camera app to capture image
	  */
	 private void captureImage() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		
		fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
		
		intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
		
		  // start the image capture Intent
		startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
	 }

	 /**
	  * Launching camera app to record video
	  */
	 private void recordVideo() {
		Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

		fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);

		  // set video quality
		intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

		  intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file
																				// name

		  // start the video capture Intent
		  startActivityForResult(intent, CAMERA_CAPTURE_VIDEO_REQUEST_CODE);
	  }
	  
	 /**
	  * Здесь мы храним url-адрес файла, поскольку он будет нулевым после возвращения с камеры
	  */
	 @Override
	 protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		  // save file url in bundle as it will be null on screen orientation
		  // changes
		outState.putParcelable("file_uri", fileUri);
	 }
	 
	 @Override
	 protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		
		  // get the file url
		fileUri = savedInstanceState.getParcelable("file_uri");
	 }
	 
	 
	 
	 /**
	  * Receiving activity result method will be called after closing the camera
	  * */
	 @Override
	 protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		  // if the result is capturing Image
		if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				
					// successfully captured the image
					 // launching upload activity
				launchUploadActivity(true);
				
				
			} else if (resultCode == RESULT_CANCELED) {
				
					// user cancelled Image capture
				Toast.makeText(getApplicationContext(),
					"Режим сьемки отключен", Toast.LENGTH_SHORT)
				.show();
				
			} else {
					 // failed to capture image
				Toast.makeText(getApplicationContext(),
					"Не удалось захватить изображение!", Toast.LENGTH_SHORT)
				.show();
			}
			
		} else if (requestCode == CAMERA_CAPTURE_VIDEO_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {

					// video successfully recorded
					 // launching upload activity
				launchUploadActivity(false);

			} else if (resultCode == RESULT_CANCELED) {

					// user cancelled recording
				Toast.makeText(getApplicationContext(),
					"Режим видео отключен!", Toast.LENGTH_SHORT)
				.show();

			} else {
					 // failed to record video
				Toast.makeText(getApplicationContext(),
					"Не удалось записать видео!", Toast.LENGTH_SHORT)
				.show();
			}
		} else if (requestCode == SELECT_AVATAR) {
			if (resultCode == RESULT_OK) {
				Uri selectedImageUri = data.getData();
				String pixPath = getPath(selectedImageUri);
				if (mCallback != null) {
					mCallback.onReceiveValue(Uri.parse("file://" + pixPath));
				} else if (mArrayCallback != null) {
					mArrayCallback.onReceiveValue(new Uri[]{Uri.parse("file://" + pixPath)});
				}
			}
		}
	 }
	 
	 private void launchUploadActivity(boolean isImage){
		Intent i = new Intent(MainActivity.this, UploadActivity.class);
		i.putExtra("filePath", fileUri.getPath());
		i.putExtra("isImage", isImage);
		startActivity(i);
	 }

	 /**
	  * ------------ Helper Methods ---------------------- 
	  * */
	 
	 /**
	  * Creating file uri to store image/video
	  */
	 public Uri getOutputMediaFileUri(int type) {
		return Uri.fromFile(getOutputMediaFile(type));
	 }

	 /**
	  * returning image / video
	  */
	 private static File getOutputMediaFile(int type) {
		
		  // External sdcard location
		File mediaStorageDir = new File(
			Environment
			.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
			Config.IMAGE_DIRECTORY_NAME);
		
		  // Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d(TAG, "Oops! Failed create "
					+ Config.IMAGE_DIRECTORY_NAME + " directory");
				return null;
			}
		}

		  // Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
			Locale.getDefault()).format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
				+ "IMG_" + timeStamp + ".jpg");
		} else if (type == MEDIA_TYPE_VIDEO) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
				+ "VID_" + timeStamp + ".mp4");
		} else {
			return null;
		}
		
		return mediaFile;
	 }
 }
