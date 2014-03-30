package com.example.savemytrip;

import java.io.File;

import android.os.Environment;

public class Factory {

	public static final String columnSeparator = ";";

	public static final String localDirectoryName = "SaveMyTrip";

	public static final String saveFileName = "SaveMyTrip.csv";

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
