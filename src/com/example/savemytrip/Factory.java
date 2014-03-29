package com.example.savemytrip;

import android.util.Log;

public class Factory {

	private static boolean running = true;

	public static boolean isRunning() {
		return running;
	}

	public static void setRunning(boolean running) {
		Log.e("Factory", "Running: " + running);
		Factory.running = running;
	}

}
