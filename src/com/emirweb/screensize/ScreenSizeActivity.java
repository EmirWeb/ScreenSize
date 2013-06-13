package com.emirweb.screensize;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
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
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

@SuppressWarnings("deprecation")
@SuppressLint("NewApi")
public class ScreenSizeActivity extends Activity {

	private static final String DEVICE_INFORMATION = "deviceInformation";
	private static final String VERSION = "1";
	private static final String DP = "dp";
	private static final String PX = "px";
	private static final String UPLOAD_URL = "http://emirweb.com/ScreenSize/UploadScreenSize.php";
	private static final String LINK_URL = "http://emirweb.com/ScreenDeviceStatistics.php";
	private static final String SCREEN_SIZE = "ScreenSize";
	private static final String SUBMITTED = "Submitted";
	private static final String APPLICATION_JSON = "application/json";
	private static final String GET_RAW_HEIGHT = "getRawHeight";
	private static final String GET_RAW_WIDTH = "getRawWidth";
	private static final int TIME_BETWEEN_ATTEMPTS = 1500;

	private boolean mIsDestroyed;
	private boolean mHideTitle;
	private boolean mFullScreen;
	private DeviceInformation mDeviceInformation;
	private int mScreenOrientation;
	private boolean mCalculationAttempted;
	private boolean mCalculationsComplete;

	private static class Extras {
		public static final String HIDE_TITLE_KEY = "hideTitle";
		public static final String FULL_SCREEN_KEY = "fullScreen";
		public static final String DEVICE_INFORMATION_KEY = DEVICE_INFORMATION;
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

		getWindow().setBackgroundDrawable(null);

		boolean deviceInformationSaved = false;
		if (isPass1() && mScreenOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
			final DeviceInformation deviceInformation = getSavedDeviceInformation();
			deviceInformationSaved = deviceInformation != null;
			if (deviceInformationSaved)
				mDeviceInformation = deviceInformation;
		}

		if (deviceInformationSaved && !hasPreviouslySubmitted())
			sendData(false);

		mCalculationsComplete = deviceInformationSaved || mScreenOrientation == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
		if (mCalculationsComplete)
			handleCompletedData();
		else
			setContentView(R.layout.activity_screen_size_calculating);

	}

	private DeviceInformation getSavedDeviceInformation() {
		FileInputStream fileInputStream = null;
		ObjectInputStream objectInputStream = null;
		try {
			fileInputStream = openFileInput(DEVICE_INFORMATION + VERSION);
			objectInputStream = new ObjectInputStream(fileInputStream);
			final DeviceInformation deviceInformation = (DeviceInformation) objectInputStream.readObject();
			return deviceInformation;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (OptionalDataException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (objectInputStream != null) {
				try {
					objectInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;

	}

	@Override
	protected void onStart() {
		super.onStart();
		if (!mCalculationsComplete)
			handleCalculations();
	}

	private void handleCalculations() {
		if (mCalculationAttempted)
			return;
		mCalculationAttempted = true;
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

	private void handleCompletedData() {
		setContentView(R.layout.activity_screen_size);
		final TextView deviceNameTextView = (TextView) findViewById(R.id.activity_screen_size_device_name_text_view);
		final TextView deviceDpResolutionTextView = (TextView) findViewById(R.id.activity_screen_size_device_dp_resolution_text_view);
		final TextView devicePixelResolutionTextView = (TextView) findViewById(R.id.activity_screen_size_device_pixel_resolution_text_view);
		final TextView screenBucketTextView = (TextView) findViewById(R.id.activity_screen_size_screen_bucket_text_view);
		final TextView screenSizeTextView = (TextView) findViewById(R.id.activity_screen_size_screen_size_text_view);
		final TextView widthTextView = (TextView) findViewById(R.id.activity_screen_size_width_text_view);
		final TextView heightTextView = (TextView) findViewById(R.id.activity_screen_size_height_text_view);
		final TextView navigationBarHeightTextView = (TextView) findViewById(R.id.activity_screen_size_navigation_bar_height_title_text_view);
		final TextView navigationBarWidthTextView = (TextView) findViewById(R.id.activity_screen_size_navigation_bar_width_title_text_view);
		final TextView navigationBarValueTextView = (TextView) findViewById(R.id.activity_screen_size_navigation_bar_value_text_view);
		final TextView titleBarWidthTextView = (TextView) findViewById(R.id.activity_screen_size_title_bar_height_text_view);
		final TextView statusBarWidthTextView = (TextView) findViewById(R.id.activity_screen_size_status_bar_height_text_view);

		deviceNameTextView.setText(DeviceInformation.sDeviceName);
		screenSizeTextView.setText(mDeviceInformation.mScreenSize);
		screenBucketTextView.setText(mDeviceInformation.mDensityName);

		ScreenDetails screenDetails = null;
		final float density = mDeviceInformation.mDensity;
		if (isPortrait())
			screenDetails = mDeviceInformation.mPortraitScreenDetails;
		else if (isLandscape())
			screenDetails = mDeviceInformation.mLandscapeScreenDetails;

		final String deviceDpResolution = getDpResolutionString(screenDetails.mDevicePixelHeight, screenDetails.mDevicePixelWidth, density);
		final String devicePixelResolution = getPixelString(screenDetails.mDevicePixelHeight, screenDetails.mDevicePixelWidth);

		deviceDpResolutionTextView.setText(deviceDpResolution);
		devicePixelResolutionTextView.setText(devicePixelResolution);

		widthTextView.setText(getMeasurement(screenDetails.mContentViewPixelWidth, density, false));
		heightTextView.setText(getMeasurement(screenDetails.mContentViewPixelHeight, density, true));

		if (screenDetails.mNavBarHeight != 0) {
			navigationBarHeightTextView.setVisibility(View.VISIBLE);
			navigationBarValueTextView.setText(getMeasurement(screenDetails.mNavBarHeight, density, false));
		} else if (screenDetails.mNavBarWidth != 0) {
			navigationBarWidthTextView.setVisibility(View.VISIBLE);
			navigationBarValueTextView.setText(getMeasurement(screenDetails.mNavBarWidth, density, false));
		} else
			navigationBarValueTextView.setText("Navigation bar not present");

		if (screenDetails.mTitleBarHeight != 0)
			titleBarWidthTextView.setText(getMeasurement(screenDetails.mTitleBarHeight, density, false));
		else
			titleBarWidthTextView.setText("Title bar not present");

		if (screenDetails.mStatusBarHeight != 0)
			statusBarWidthTextView.setText(getMeasurement(screenDetails.mStatusBarHeight, density, false));
		else
			statusBarWidthTextView.setText("Title bar not present");

		sendData(true);
	}

	private String getMeasurement(final int pixel, final float density, final boolean hasLineBreak) {
		String lineBreak = " ";
		if (hasLineBreak)
			lineBreak = "\n";
		return getPixelMeasurement(pixel) + lineBreak + getDpMeasurement(pixel, density);
	}

	private String getPixelMeasurement(final int pixel) {
		return Integer.toString(pixel) + PX;
	}

	private String getDpMeasurement(final int pixel, final float density) {
		return "(" + Integer.toString((int) (pixel / density)) + DP + ")";
	}

	private String getPixelString(final int devicePixelHeight, final int devicePixelWidth) {
		return devicePixelHeight + " x " + devicePixelWidth + PX;
	}

	private String getDpResolutionString(final int devicepixelHeight, final int devicePixelWidth, final float density) {
		return "(" + (int) (devicepixelHeight / density) + " x " + (int) (devicePixelWidth / density) + DP + ")";
	}

	private void sendData(final boolean saveDeviceInformation) {
		final boolean hasPreviouslySubmitted = hasPreviouslySubmitted();
		if (hasPreviouslySubmitted)
			return;
		new AsyncTask<String, String, String>() {

			@Override
			protected String doInBackground(String... params) {
				
				final HttpClient httpClient = new DefaultHttpClient();
				final HttpPost httpPost = new HttpPost(UPLOAD_URL);

				try {

					final StringEntity stringEntity = new StringEntity(mDeviceInformation.toJson().toString());
					stringEntity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, APPLICATION_JSON));
					httpPost.setEntity(stringEntity);
					final HttpResponse httpResponse = httpClient.execute(httpPost);
					
					if (httpResponse.getStatusLine().getStatusCode() == 200)
						setHasPreviouslySubmitted();
					
					if (saveDeviceInformation)
						saveDeviceInformation(mDeviceInformation);

				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				return null;
			}

		}.execute("");
	}

	private void setHasPreviouslySubmitted() {
		final SharedPreferences sharedPreferences = getSharedPreferences(SCREEN_SIZE, Context.MODE_PRIVATE);
		final Editor editor = sharedPreferences.edit();
		editor.putBoolean(SUBMITTED, true);
		editor.commit();
	}

	private void saveDeviceInformation(final DeviceInformation deviceInformation) {
		FileOutputStream fileOutputStream = null;
		ObjectOutputStream objectOutputStream = null;
		try {
			fileOutputStream = openFileOutput(DEVICE_INFORMATION + VERSION, Context.MODE_PRIVATE);
			objectOutputStream = new ObjectOutputStream(fileOutputStream);
			objectOutputStream.writeObject(deviceInformation);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (objectOutputStream != null) {
				try {
					objectOutputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fileOutputStream != null) {
				try {
					fileOutputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private boolean hasPreviouslySubmitted() {
		final SharedPreferences sharedPreferences = getSharedPreferences(SCREEN_SIZE, Context.MODE_PRIVATE);
		final boolean submitted = sharedPreferences.getBoolean(SUBMITTED, false);
		return submitted;
	}

	@Override
	protected void onDestroy() {
		mIsDestroyed = true;
		super.onDestroy();
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
			screenDetails.mTitleBarHeight = screenDetails.mWindowPixelHeight - screenDetails.mContentViewPixelHeight;
		} else if (isPass3()) {
			getConentViewPixels(screenDetails, display);
			screenDetails.mStatusBarHeight = screenDetails.mWindowPixelHeight - screenDetails.mContentViewPixelHeight - screenDetails.mTitleBarHeight;
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
				final Method getRawWidthMethod = Display.class.getMethod(GET_RAW_WIDTH);
				final Method getRawHeightMethod = Display.class.getMethod(GET_RAW_HEIGHT);
				screenDetails.mDevicePixelHeight = (Integer) getRawHeightMethod.invoke(display);
				screenDetails.mDevicePixelWidth = (Integer) getRawWidthMethod.invoke(display);
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

	private boolean isPortrait() {
		final Display display = getWindowManager().getDefaultDisplay();
		final int rotation = display.getRotation();
		return rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180;
	}

	private boolean isLandscape() {
		final Display display = getWindowManager().getDefaultDisplay();
		final int rotation = display.getRotation();
		return rotation == Surface.ROTATION_270 || rotation == Surface.ROTATION_90;
	}

	public void onVisitUrlButtonClicked(final View view) {
		final Uri uri = Uri.parse(LINK_URL);
		final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		startActivity(intent);
	}

}
