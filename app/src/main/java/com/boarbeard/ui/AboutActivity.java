package com.boarbeard.ui;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

import com.boarbeard.R;

public class AboutActivity extends Activity {
	private WebView browser;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.activity_about);
				
		browser = (WebView) findViewById(R.id.webkit);
		browser.loadUrl("file:///android_asset/About.html");
	}
		
}
