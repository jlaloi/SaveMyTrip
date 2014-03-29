package com.example.savemytrip;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.location.Location;
import android.util.Log;

public class Utils {

	private final static String LOG = "Utils";
	public static SimpleDateFormat dateFormat, dateSaveFormat;
	public static NumberFormat formatter;
	public static final String colSep = ";";
	public static final String lineSep = "\n";

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
		String toSave = dateSaveFormat.format(new Date(location.getTime())) + colSep + location.getLatitude() + colSep + location.getLongitude() + colSep + location.getAccuracy() + colSep + location.getAltitude();
		addToFile(file, toSave + lineSep, true);
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

}
