package com.example.savemytrip;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.location.Location;
import android.util.Log;

public class Utils {

	public static final String LOG = "Utils";
	public static final String PREFS_NAME = "MyPrefsFile";

	public static SimpleDateFormat dateFormat, dateSaveFormat;
	public static NumberFormat formatter;

	public static enum Configuration {
		running
	};

	static {
		dateFormat = new SimpleDateFormat("HH:mm (dd/MM)", Locale.getDefault());
		dateSaveFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault());
		formatter = new DecimalFormat("0.00000");
	}

	public static void addToFile(File file, String text, boolean append) {
		try {
			OutputStream bos = new BufferedOutputStream(new FileOutputStream(file, append));
			OutputStreamWriter out = new OutputStreamWriter(bos);
			out.write(text);
			out.flush();
			out.close();
			Log.i(LOG, "Saved to " + file.getAbsolutePath());
		} catch (Exception e) {
			Log.e(LOG, "addToFile", e);
		}
	}

	public static void addToFile(File file, Location location) {
		String toSave = dateSaveFormat.format(new Date(location.getTime())) + Factory.columnSeparator + location.getLatitude() + Factory.columnSeparator + location.getLongitude() + Factory.columnSeparator + location.getAccuracy() + Factory.columnSeparator + location.getAltitude();
		addToFile(file, toSave + "\n", true);
	}

	public static String formatDate(Date date) {
		return dateFormat.format(date);
	}

	public static String formatDate(Location location) {
		Date date = new Date(location.getTime());
		return dateFormat.format(date);
	}

	public static String formatNumber(Double number) {
		return formatter.format(number);
	}

	public static String formatLocation(Location location) {
		return formatNumber(location.getLatitude()) + ", " + formatNumber(location.getLongitude());
	}

	public static List<File> listFiles(File path) {
		List<File> result = new ArrayList<File>();
		for (File file : path.listFiles()) {
			if (file.isFile()) {
				result.add(file);
			}
		}
		return result;
	}

	public static List<String> getFileLines(File file) {
		List<String> result = new ArrayList<String>();
		try {
			FileInputStream fstream = new FileInputStream(file);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line;
			while ((line = br.readLine()) != null) {
				result.add(line);
			}
			in.close();
		} catch (Exception e) {
			Log.e(LOG, "getFileLines", e);
		}
		return result;
	}
}
