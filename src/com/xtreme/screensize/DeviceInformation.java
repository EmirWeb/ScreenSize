package com.xtreme.screensize;

import java.io.Serializable;

public class DeviceInformation implements Serializable {

	private static final long serialVersionUID = -1256137182982237492L;

	@SuppressWarnings("deprecation")
	public final String sVersionSdkString = android.os.Build.VERSION.SDK;

	public final String sDeviceName = android.os.Build.MODEL;
	public final String sVersionCodeName = android.os.Build.VERSION.CODENAME;
	public final String sVersionIncremental = android.os.Build.VERSION.INCREMENTAL;
	public final String sVersionRelease = android.os.Build.VERSION.RELEASE;
	public final int sVersionSdkInteger = android.os.Build.VERSION.SDK_INT;

	public final String sBoard = android.os.Build.BOARD;
	public final String sBootLoader = android.os.Build.BOOTLOADER;
	public final String sBrand = android.os.Build.BRAND;
	public final String sCpuAbi = android.os.Build.CPU_ABI;
	public final String sCpuAbi2 = android.os.Build.CPU_ABI2;
	public final String sDevice = android.os.Build.DEVICE;
	public final String sDisplay = android.os.Build.DISPLAY;
	public final String sFingerPrint = android.os.Build.FINGERPRINT;
	public final String sHardwade = android.os.Build.HARDWARE;
	public final String sHost = android.os.Build.HOST;
	public final String sId = android.os.Build.ID;
	public final String sManufacturer = android.os.Build.MANUFACTURER;
	public final String sProduct = android.os.Build.PRODUCT;
	public final String sRadio = android.os.Build.RADIO;

	public final String sTags = android.os.Build.TAGS;
	public final long sTime = android.os.Build.TIME;
	public final String sType = android.os.Build.TYPE;
	public final String sUnkown = android.os.Build.UNKNOWN;
	public final String sUser = android.os.Build.USER;

	// public final String sSerial = android.os.Build.SERIAL;
	// public final String sRadioVersion = android.os.Build.getRadioVersion();
	
	public boolean isInvalid;
	
	public ScreenDetails mPortraitScreenDetails = new ScreenDetails();
	public ScreenDetails mLandscapeScreenDetails = new ScreenDetails();

	public String toString() {
		if (isInvalid)
			return "Unable to process data";

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

		return details;
	}
}
