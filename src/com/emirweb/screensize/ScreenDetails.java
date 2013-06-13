package com.emirweb.screensize;

import java.io.Serializable;

import com.google.gson.JsonObject;

public class ScreenDetails implements Serializable, Jsonizable {

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
		return details;
	}

	@Override
	public JsonObject toJson() {
		final JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("devicePixelHeight", mDevicePixelHeight);
		jsonObject.addProperty("devicePixelWidth", mDevicePixelWidth);
		jsonObject.addProperty("windowPixelHeight", mWindowPixelHeight);
		jsonObject.addProperty("windowPixelWidth", mWindowPixelWidth);
		jsonObject.addProperty("contentViewPixelHeight", mContentViewPixelHeight);
		jsonObject.addProperty("contentViewPixelWidth", mContentViewPixelWidth);
		jsonObject.addProperty("navBarHeight", mNavBarHeight);
		jsonObject.addProperty("navBarWidth", mNavBarWidth);
		jsonObject.addProperty("statusBarHeight", mStatusBarHeight);
		jsonObject.addProperty("titleBarHeight", mTitleBarHeight);
		return jsonObject;
	}
}