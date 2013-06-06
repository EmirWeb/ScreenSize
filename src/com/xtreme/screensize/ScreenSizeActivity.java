package com.xtreme.screensize;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

@SuppressWarnings("deprecation")
@SuppressLint("NewApi")
public class ScreenSizeActivity extends Activity {

	private static final String TAG = "ScreenSize";
	private static final String GET_RAW_HEIGHT = "getRawHeight";
	private static final String GET_RAW_WIDTH = "getRawWidth";
	private static final String ANDROID = "android";
	private static final String DIMEN = "dimen";
	private static final String STATUS_BAR_HEIGHT = "status_bar_height";
	private static final int MAX_ATTEMPTS = 10;
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
		} else {

			final Handler handler = new Handler();
			handler.postDelayed(new Runnable() {

				@Override
				public void run() {
					if (mIsDestroyed)
						return;
					if (mDeviceInformation == null)
						mDeviceInformation = new DeviceInformation();

					getDeviceInformation();

					// if (mDeviceInformation.isInvalid)
					// mHandler.postDelayed(this, TIME_BETWEEN_ATTEMPTS);

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

	@Override
	protected void onDestroy() {
		mIsDestroyed = true;
		super.onDestroy();
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void hideNavBar() {
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
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
			getDensity(screenDetails);
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

		// if (!(mHideTitle || mFullScreen)) {
		// getConentViewPixels(deviceInformation, display);
		// getStatusBarHeight(deviceInformation);
		// getTitleBarHeight(deviceInformation);
		// getDensity(deviceInformation);
		// } else {
		//
		// }

	}

	private void getDensity(final ScreenDetails screenDetails) {
		final DisplayMetrics metrics = getResources().getDisplayMetrics();
		screenDetails.mDensity = metrics.density;
		screenDetails.mXdpi = metrics.xdpi;
		screenDetails.mYdpi = metrics.ydpi;
	}

	private void getWindowPixels(final ScreenDetails screenDetails, final Display display) {
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			final Point size = new Point();
			display.getSize(size);
			screenDetails.mWindowPixelHeight = size.y;
			screenDetails.mWindowPixelWidth = size.x;
		} else {
			// screenDetails.isInvalid = true;
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
			// deviceInformation.isInvalid = false;
		}
	}

	private void getConentViewPixels(final ScreenDetails screenDetails, final Display display) {

		// screenDetails.isInvalid = true;
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
		// deviceInformation.isInvalid = false;
		// }
	}

	private void getDevicePixels(final ScreenDetails screenDetails, final Display display) {
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
			final Point size = new Point();
			display.getRealSize(size);
			screenDetails.mDevicePixelHeight = size.y;
			screenDetails.mDevicePixelWidth = size.x;
		} else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB_MR2) {
			try {
				// deviceInformation.isInvalid = true;
				Method mGetRawW = Display.class.getMethod(GET_RAW_WIDTH);
				Method mGetRawH = Display.class.getMethod(GET_RAW_HEIGHT);
				screenDetails.mDevicePixelHeight = (Integer) mGetRawH.invoke(display);
				screenDetails.mDevicePixelWidth = (Integer) mGetRawW.invoke(display);
				// deviceInformation.isInvalid = false;
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
		// if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
		// final Point size = new Point();
		// display.getSize(size);
		//
		// } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
		// final int resourceId = getResources().getIdentifier(STATUS_BAR_HEIGHT, DIMEN, ANDROID);
		//
		// int statusBarHeight = 0;
		// if (resourceId > 0)
		// statusBarHeight = getResources().getDimensionPixelSize(resourceId);
		//
		// deviceInformation.mNavBarHeight = statusBarHeight;
		// } else
		// deviceInformation.mNavBarHeight = 0;
		screenDetails.mNavBarHeight = screenDetails.mDevicePixelHeight - screenDetails.mWindowPixelHeight;
		screenDetails.mNavBarWidth = screenDetails.mDevicePixelWidth - screenDetails.mWindowPixelWidth;
	}

	private void getTitleBarHeight(final ScreenDetails screenDetails) {

		final int statusBarHeight = screenDetails.mStatusBarHeight;

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

		final int contentViewTop = view.getTop();
		// final int statusBarHeight = rectgle.top;
		final int titleBarHeight = contentViewTop - statusBarHeight;
		if (titleBarHeight != 0)
			screenDetails.mTitleBarHeight = titleBarHeight;
	}
}
