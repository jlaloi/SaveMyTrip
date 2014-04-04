package com.example.savemytrip;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.graphics.Color;
import android.os.Environment;

public class Factory {

	public static final String columnSeparator = ";";

	public static final String localDirectoryName = "SaveMyTrip";

	public static final String saveFileName = "SMT_";

	public static final String saveFileExt = ".csv";

	public static final int cycleTime = 19;

	public static final int gpsQueryNumber = 5;

	public static final String saveFileDateFormat = "yyyyMMdd";

	public static SimpleDateFormat saveFileSimpleDateFormat;

	public static int markerLineColor = Color.parseColor("#D358F7");

	private static File localDirecory;

	static {
		localDirecory = new File(Environment.getExternalStorageDirectory() + File.separator + localDirectoryName + File.separator);
		localDirecory.mkdir();
		saveFileSimpleDateFormat = new SimpleDateFormat(saveFileDateFormat, Locale.getDefault());
	}

	public static File getSaveFile() {
		String date = saveFileSimpleDateFormat.format(Calendar.getInstance().getTime());
		File saveFile = new File(localDirecory.getAbsolutePath() + File.separator + saveFileName + date + saveFileExt);
		return saveFile;
	}

	public static File getLocalDirecory() {
		return localDirecory;
	}

}
