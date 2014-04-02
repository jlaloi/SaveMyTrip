package com.example.savemytrip;

import java.io.File;

import android.graphics.Color;
import android.os.Environment;

public class Factory {

	public static final String columnSeparator = ";";

	public static final String localDirectoryName = "SaveMyTrip";

	public static final String saveFileName = "SaveMyTrip.csv";

	public static final int cycleTime = 19;

	public static final int gpsQueryNumber = 5;
	
	public static int markerLineColor = Color.parseColor("#D358F7");

	private static File localDirecory, saveFile;

	static {
		localDirecory = new File(Environment.getExternalStorageDirectory() + File.separator + localDirectoryName + File.separator);
		localDirecory.mkdir();
		saveFile = new File(localDirecory.getAbsolutePath() + File.separator + saveFileName);
	}

	public static File getSaveFile() {
		return saveFile;
	}

	public static File getLocalDirecory() {
		return localDirecory;
	}

}
