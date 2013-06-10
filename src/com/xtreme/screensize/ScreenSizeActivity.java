package com.xtreme.screensize;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

@SuppressWarnings("deprecation")
@SuppressLint("NewApi")
public class ScreenSizeActivity extends Activity {

	private static final String APPLICATION_JSON = "application/json";
	private static final String TAG = "ScreenSize";
	private static final String GET_RAW_HEIGHT = "getRawHeight";
	private static final String GET_RAW_WIDTH = "getRawWidth";
	private static final String ANDROID = "android";
	private static final String DIMEN = "dimen";
	private static final String STATUS_BAR_HEIGHT = "status_bar_height";
	private static final int TIME_BETWEEN_ATTEMPTS = 1000;

	private boolean mIsDestroyed;
	private boolean mHideTitle;
	private boolean mFullScreen;
	private DeviceInformation mDeviceInformation;
	private int mScreenOrientation;

	private static class Extras {
		public static final String HIDE_TITLE_KEY = "hideTitle";
		public static final String FULL_SCREEN_KEY = "fullScreen";
		public static final String DEVICE_INFORMATION_KEY = "deviceInformation";
		public static final String SCREEN_ORIENTATION_KEY = "screenOrientation";

	}

	public static void start(final Activity activity, final boolean hideTitle, final boolean fullScreen, final int screenOrientation, final DeviceInformation deviceInformation) {
		final Intent intent = new Intent(activity, ScreenSizeActivity.class);

		intent.putExtra(Extras.HIDE_TITLE_KEY, hideTitle);
		intent.putExtra(Extras.FULL_SCREEN_KEY, fullScreen);
		intent.putExtra(Extras.DEVICE_INFORMATION_KEY, deviceInformation);
		intent.putExtra(Extras.SCREEN_ORIENTATION_KEY, screenOrientation);

		activity.startActivity(intent);
		activity.finish();
	}

	private void parseIntent() {
		final Intent intent = getIntent();
		mHideTitle = intent.getBooleanExtra(Extras.HIDE_TITLE_KEY, true);
		mFullScreen = intent.getBooleanExtra(Extras.FULL_SCREEN_KEY, true);
		mScreenOrientation = intent.getIntExtra(Extras.SCREEN_ORIENTATION_KEY, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		mDeviceInformation = (DeviceInformation) intent.getSerializableExtra(Extras.DEVICE_INFORMATION_KEY);
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		parseIntent();

		setRequestedOrientation(mScreenOrientation);

		if (mHideTitle)
			this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		if (mFullScreen)
			this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_screen_size);

		if (mScreenOrientation == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
			final TextView textView = (TextView) findViewById(R.id.activity_screen_size_text_view);
			textView.setText(mDeviceInformation.toString());
			sendData();
		} else {

			final Handler handler = new Handler();
			handler.postDelayed(new Runnable() {

				@Override
				public void run() {
					if (mIsDestroyed)
						return;
					if (mDeviceInformation == null)
						mDeviceInformation = new DeviceInformation(getApplicationContext());

					getDeviceInformation();

					if (isPass3() && mScreenOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
						start(ScreenSizeActivity.this, false, false, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED, mDeviceInformation);
					else if (isPass1())
						start(ScreenSizeActivity.this, !mHideTitle, mFullScreen, mScreenOrientation, mDeviceInformation);
					else if (isPass2())
						start(ScreenSizeActivity.this, mHideTitle, !mFullScreen, mScreenOrientation, mDeviceInformation);
					else if (isPass3())
						start(ScreenSizeActivity.this, true, true, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, mDeviceInformation);

				}
			}, TIME_BETWEEN_ATTEMPTS);
		}

	}

	private void sendData() {
		new AsyncTask<String, String, String>() {

			@Override
			protected String doInBackground(String... params) {
				final HttpClient httpClient = new DefaultHttpClient();
				final HttpPost httpPost = new HttpPost("http://192.168.90.166:8888/ScreenSize/UploadScreenSize.php");

				try {

					final StringEntity stringEntity = new StringEntity(mDeviceInformation.toJson().toString());
					stringEntity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, APPLICATION_JSON));
					httpPost.setEntity(stringEntity);
					final HttpResponse httpResponse = httpClient.execute(httpPost);

					if (httpResponse.getStatusLine().getStatusCode() == 200) {
						final SharedPreferences sharedPreferences = getSharedPreferences("com.example.app", Context.MODE_PRIVATE);
					}
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				return null;
			}

		}.execute("");
	}

	private String getStringResponse(final InputStream inputStream) {
		if (inputStream == null)
			return null;

		final char[] chars = new char[1024];

		InputStreamReader inputStreamReader = null;
		final StringBuffer stringBuffer = new StringBuffer();
		try {
			inputStreamReader = new InputStreamReader(inputStream);
			int charsRead = 0;
			while (((charsRead = inputStreamReader.read(chars))) > 0)
				stringBuffer.append(chars, 0, charsRead);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (inputStreamReader != null) {
				try {
					inputStreamReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try {
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return stringBuffer.toString();
	}

	@Override
	protected void onDestroy() {
		mIsDestroyed = true;
		super.onDestroy();
	}

	public void getStatusBarHeight(final ScreenDetails screenDetails) {
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB && android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.HONEYCOMB_MR2) {
			screenDetails.mStatusBarHeight = 0;
			return;
		}

		final int resourceId = getResources().getIdentifier(STATUS_BAR_HEIGHT, DIMEN, ANDROID);

		int statusBarHeight = 0;
		if (resourceId > 0)
			statusBarHeight = getResources().getDimensionPixelSize(resourceId);

		screenDetails.mStatusBarHeight = statusBarHeight;
	}

	private boolean isPass1() {
		return mFullScreen && mHideTitle;
	}

	private boolean isPass2() {
		return mFullScreen && !mHideTitle;
	}

	private boolean isPass3() {
		return !mFullScreen && !mHideTitle;
	}

	private void getDeviceInformation() {
		final Display display = getWindowManager().getDefaultDisplay();

		ScreenDetails screenDetails = null;

		if (mScreenOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
			screenDetails = mDeviceInformation.mPortraitScreenDetails;
		else if (mScreenOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
			screenDetails = mDeviceInformation.mLandscapeScreenDetails;

		if (isPass1()) {
			getDevicePixels(screenDetails, display);
			getWindowPixels(screenDetails, display);
			getNavigationBarHeight(screenDetails, display);
		} else if (isPass2()) {
			getConentViewPixels(screenDetails, display);
			screenDetails.mStatusBarHeight = screenDetails.mWindowPixelHeight - screenDetails.mContentViewPixelHeight;
		} else if (isPass3()) {
			getConentViewPixels(screenDetails, display);
			screenDetails.mTitleBarHeight = screenDetails.mWindowPixelHeight - screenDetails.mContentViewPixelHeight - screenDetails.mStatusBarHeight;
		}
	}

	private void getWindowPixels(final ScreenDetails screenDetails, final Display display) {
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			final Point size = new Point();
			display.getSize(size);
			screenDetails.mWindowPixelHeight = size.y;
			screenDetails.mWindowPixelWidth = size.x;
		} else {
			final Window window = getWindow();
			if (window == null)
				return;

			final View decorView = window.getDecorView();
			if (decorView == null)
				return;

			final Rect rectgle = new Rect();
			decorView.getWindowVisibleDisplayFrame(rectgle);

			final View view = window.findViewById(Window.ID_ANDROID_CONTENT);
			if (view == null)
				return;

			screenDetails.mWindowPixelHeight = view.getMeasuredHeight();
			screenDetails.mWindowPixelWidth = view.getMeasuredWidth();
		}
	}

	private void getConentViewPixels(final ScreenDetails screenDetails, final Display display) {

		final Window window = getWindow();
		if (window == null)
			return;

		final View decorView = window.getDecorView();
		if (decorView == null)
			return;

		final Rect rectgle = new Rect();
		decorView.getWindowVisibleDisplayFrame(rectgle);

		final View contentView = window.findViewById(Window.ID_ANDROID_CONTENT);
		if (contentView == null)
			return;

		screenDetails.mContentViewPixelHeight = contentView.getMeasuredHeight();
		screenDetails.mContentViewPixelWidth = contentView.getMeasuredWidth();
	}

	private void getDevicePixels(final ScreenDetails screenDetails, final Display display) {
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
			final Point size = new Point();
			display.getRealSize(size);
			screenDetails.mDevicePixelHeight = size.y;
			screenDetails.mDevicePixelWidth = size.x;
		} else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB_MR2) {
			try {
				Method mGetRawW = Display.class.getMethod(GET_RAW_WIDTH);
				Method mGetRawH = Display.class.getMethod(GET_RAW_HEIGHT);
				screenDetails.mDevicePixelHeight = (Integer) mGetRawH.invoke(display);
				screenDetails.mDevicePixelWidth = (Integer) mGetRawW.invoke(display);
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		} else {
			screenDetails.mDevicePixelHeight = display.getHeight();
			screenDetails.mDevicePixelWidth = display.getWidth();
		}
	}

	private void getNavigationBarHeight(final ScreenDetails screenDetails, final Display display) {
		screenDetails.mNavBarHeight = screenDetails.mDevicePixelHeight - screenDetails.mWindowPixelHeight;
		screenDetails.mNavBarWidth = screenDetails.mDevicePixelWidth - screenDetails.mWindowPixelWidth;
	}

}
