package com.xtreme.screensize;

import java.io.Serializable;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;

import com.google.gson.JsonObject;

@SuppressLint("NewApi")
@SuppressWarnings("deprecation")
public class DeviceInformation implements Serializable, Jsonizable {

	private static final String UNKOWN_SCREEN = "Unkown screen";
	private static final String UNDEFINED_SCREEN = "Undefined screen";
	private static final String X_LARGE_SCREEN = "XLarge screen";
	private static final String LARGE_SCREEN = "Large screen";
	private static final String NORMAL_SCREEN = "Normal screen";
	private static final String SMALL_SCREEN = "Small screen";
	private static final String XXHDPI = "xxhdpi";
	private static final String TVDPI = "tvdpi";
	private static final String XHDPI = "xhdpi";
	private static final String HDPI = "hdpi";
	private static final String MDPI = "mdpi";
	private static final String LDPI = "ldpi";

	private static final long serialVersionUID = -1256137182982237492L;

	public static final String sVersionSdkString = android.os.Build.VERSION.SDK;
	public static final String sRadio = android.os.Build.RADIO;

	public static final String sDeviceName = android.os.Build.MODEL;
	public static final String sVersionCodeName = android.os.Build.VERSION.CODENAME;
	public static final String sVersionIncremental = android.os.Build.VERSION.INCREMENTAL;
	public static final String sVersionRelease = android.os.Build.VERSION.RELEASE;
	public static final int sVersionSdkInteger = android.os.Build.VERSION.SDK_INT;

	public static final String sBoard = android.os.Build.BOARD;
	public static final String sBootLoader = android.os.Build.BOOTLOADER;
	public static final String sBrand = android.os.Build.BRAND;
	public static final String sCpuAbi = android.os.Build.CPU_ABI;
	public static final String sCpuAbi2 = android.os.Build.CPU_ABI2;
	public static final String sDevice = android.os.Build.DEVICE;
	public static final String sDisplay = android.os.Build.DISPLAY;
	public static final String sFingerPrint = android.os.Build.FINGERPRINT;
	public static final String sHardwade = android.os.Build.HARDWARE;
	public static final String sHost = android.os.Build.HOST;
	public static final String sId = android.os.Build.ID;
	public static final String sManufacturer = android.os.Build.MANUFACTURER;
	public static final String sProduct = android.os.Build.PRODUCT;

	public static final String sTags = android.os.Build.TAGS;
	public static final long sTime = android.os.Build.TIME;
	public static final String sType = android.os.Build.TYPE;
	public static final String sUnkown = android.os.Build.UNKNOWN;
	public static final String sUser = android.os.Build.USER;

	public static final String sSerial;
	public static final String sRadioVersion;
	public final String mScreenSize;
	public final String mDensityName;
	public final float mDensity;
	public final float mXdpi;
	public final float mYdpi;
	public final ScreenDetails mPortraitScreenDetails = new ScreenDetails();
	public final ScreenDetails mLandscapeScreenDetails = new ScreenDetails();

	static {
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD)
			sSerial = android.os.Build.SERIAL;
		else
			sSerial = null;

		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			sRadioVersion = android.os.Build.getRadioVersion();
		else
			sRadioVersion = null;
	}

	public DeviceInformation(final Context context) {
		if (context == null) {
			mScreenSize = null;
			mDensityName = null;
			mDensity = 0;
			mXdpi = 0;
			mYdpi = 0;
			return;
		}

		final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		switch (displayMetrics.densityDpi) {
		case DisplayMetrics.DENSITY_LOW:
			mDensityName = LDPI;
			break;
		case DisplayMetrics.DENSITY_MEDIUM:
			mDensityName = MDPI;
			break;
		case DisplayMetrics.DENSITY_HIGH:
			mDensityName = HDPI;
			break;
		case DisplayMetrics.DENSITY_XHIGH:
			mDensityName = XHDPI;
			break;
		case DisplayMetrics.DENSITY_TV:
			mDensityName = TVDPI;
			break;
		case DisplayMetrics.DENSITY_XXHIGH:
			mDensityName = XXHDPI;
			break;
		default:
			mDensityName = null;
		}

		mDensity = displayMetrics.density;
		mXdpi = displayMetrics.xdpi;
		mYdpi = displayMetrics.ydpi;

		final int screeLayout = (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK);
		if (screeLayout == Configuration.SCREENLAYOUT_SIZE_SMALL) {
			mScreenSize = SMALL_SCREEN;
		} else if (screeLayout == Configuration.SCREENLAYOUT_SIZE_NORMAL) {
			mScreenSize = NORMAL_SCREEN;
		} else if (screeLayout == Configuration.SCREENLAYOUT_SIZE_LARGE) {
			mScreenSize = LARGE_SCREEN;
		} else if (screeLayout == Configuration.SCREENLAYOUT_SIZE_XLARGE) {
			mScreenSize = X_LARGE_SCREEN;
		} else if (screeLayout == Configuration.SCREENLAYOUT_SIZE_UNDEFINED) {
			mScreenSize = UNDEFINED_SCREEN;
		} else {
			mScreenSize = UNKOWN_SCREEN;
		}

	}

	public String toString() {
		String details = sDeviceName + "\n";
		details += "mPortraitScreenDetails: " + mPortraitScreenDetails + "\n";
		details += "mLandscapeScreenDetails: " + mLandscapeScreenDetails + "\n";
		details += "sVersionCodeName: " + sVersionCodeName + "\n";
		details += "sVersionIncremental: " + sVersionIncremental + "\n";
		details += "sVersionRelease: " + sVersionRelease + "\n";
		details += "sVersionSdkString: " + sVersionSdkString + "\n";
		details += "sVersionSdkInteger: " + sVersionSdkInteger + "\n";
		details += "sBoard: " + sBoard + "\n";
		details += "sBootLoader: " + sBootLoader + "\n";
		details += "sBrand: " + sBrand + "\n";
		details += "sCpuAbi: " + sCpuAbi + "\n";
		details += "sCpuAbi2: " + sCpuAbi2 + "\n";
		details += "sDevice: " + sDevice + "\n";
		details += "sDisplay: " + sDisplay + "\n";
		details += "sFingerPrint: " + sFingerPrint + "\n";
		details += "sHardwade: " + sHardwade + "\n";
		details += "sHost: " + sHost + "\n";
		details += "sId: " + sId + "\n";
		details += "sManufacturer: " + sManufacturer + "\n";
		details += "sProduct: " + sProduct + "\n";
		details += "sRadio: " + sRadio + "\n";
		details += "sTags: " + sTags + "\n";
		details += "sTime: " + sTime + "\n";
		details += "sType: " + sType + "\n";
		details += "sUnkown: " + sUnkown + "\n";
		details += "sUser: " + sUser + "\n";
		details += "mDensity: " + mDensityName + "\n";
		details += "mScreenSize: " + mScreenSize + "\n";
		details += "mDensity: " + mDensity + "\n";
		details += "mXdpi: " + mXdpi + "\n";
		details += "mYdpi: " + mYdpi + "\n";

		return details;
	}

	public JsonObject toJson() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.add("portraitScreenDetails", mPortraitScreenDetails.toJson());
		jsonObject.add("landscapeScreenDetails", mLandscapeScreenDetails.toJson());
		jsonObject.addProperty("deviceName", sDeviceName);
		jsonObject.addProperty("versionCodeName", sVersionCodeName);
		jsonObject.addProperty("versionIncremental", sVersionIncremental);
		jsonObject.addProperty("versionRelease", sVersionRelease);
		jsonObject.addProperty("versionSdkString", sVersionSdkString);
		jsonObject.addProperty("versionSdkInteger", sVersionSdkInteger);
		jsonObject.addProperty("board", sBoard);
		jsonObject.addProperty("bootLoader", sBootLoader);
		jsonObject.addProperty("brand", sBrand);
		jsonObject.addProperty("cpuAbi", sCpuAbi);
		jsonObject.addProperty("cpuAbi2", sCpuAbi2);
		jsonObject.addProperty("device", sDevice);
		jsonObject.addProperty("display", sDisplay);
		jsonObject.addProperty("fingerPrint", sFingerPrint);
		jsonObject.addProperty("hardwade", sHardwade);
		jsonObject.addProperty("host", sHost);
		jsonObject.addProperty("deviceId", sId);
		jsonObject.addProperty("manufacturer", sManufacturer);
		jsonObject.addProperty("product", sProduct);
		jsonObject.addProperty("radio", sRadio);
		jsonObject.addProperty("tags", sTags);
		jsonObject.addProperty("time", sTime);
		jsonObject.addProperty("type", sType);
		jsonObject.addProperty("unkown", sUnkown);
		jsonObject.addProperty("user", sUser);
		jsonObject.addProperty("densityName", mDensityName);
		jsonObject.addProperty("screenSize", mScreenSize);
		jsonObject.addProperty("density", mDensity);
		jsonObject.addProperty("xdpi", mXdpi);
		jsonObject.addProperty("ydpi", mYdpi);
		return jsonObject;
	}

}
