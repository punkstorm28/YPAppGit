package com.artifex.mupdfdemo;

import android.net.Uri;

public abstract class FilePicker {
	public interface FilePickerSupport {

	}

	public abstract void onPick(Uri uri);
}
