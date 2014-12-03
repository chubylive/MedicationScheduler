package com.example.medicationscheduler;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		InfoDownloaderTask idt = new InfoDownloaderTask(MainActivity.this, true, getApplicationContext());
		String str = "00713-0633-37";
		idt.execute(str);
		
	}
}
