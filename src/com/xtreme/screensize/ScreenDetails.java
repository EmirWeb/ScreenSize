package com.xtreme.screensize;

import java.io.Serializable;

public class ScreenDetails implements Serializable {

	private static final long serialVersionUID = -5807828181957312856L;

	public int mDevicePixelHeight;
	public int mDevicePixelWidth;
	public int mWindowPixelHeight;
	public int mWindowPixelWidth;
	public int mContentViewPixelHeight;
	public int mContentViewPixelWidth;
	public int mNavBarHeight;
	public int mNavBarWidth;
	public int mStatusBarHeight;
	public int mTitleBarHeight;
	public float mDensity;
	public float mXdpi;
	public float mYdpi;

	@Override
	public String toString() {
		String details = "mDevicePixelHeight: " + mDevicePixelHeight + "\n";
		details += "mDevicePixelWidth: " + mDevicePixelWidth + "\n";
		details += "mWindowPixelHeight: " + mWindowPixelHeight + "\n";
		details += "mWindowPixelWidth: " + mWindowPixelWidth + "\n";
		details += "mContentViewPixelHeight: " + mContentViewPixelHeight + "\n";
		details += "mContentViewPixelWidth: " + mContentViewPixelWidth + "\n";
		details += "mNavBarHeight: " + mNavBarHeight + "\n";
		details += "mNavBarWidth: " + mNavBarWidth + "\n";
		details += "mStatusBarHeight: " + mStatusBarHeight + "\n";
		details += "mTitleBarHeight: " + mTitleBarHeight + "\n";
		details += "mDensity: " + mDensity + "\n";
		details += "mXdpi: " + mXdpi + "\n";
		details += "mYdpi: " + mYdpi + "\n";
		return details;
	}
}