package com.example.savemytrip;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.LatLngBounds.Builder;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class MainActivity extends Activity {

	public static final String LOG = "MainActivity";
	public static File selectedFile;

	private GoogleMap googleMap;
	private List<File> files;
	private static boolean showLine = true;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_main);
		MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
		googleMap = mapFragment.getMap();
		googleMap.setMyLocationEnabled(true);
		files = new ArrayList<File>();
	}

	protected void onResume() {
		super.onResume();
		if (selectedFile != null) {
			load(selectedFile);
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			if (selectedFile != null) {
				showLine = !showLine;
				load(selectedFile);
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private void load(File file) {
		googleMap.clear();
		Builder boundsBuilder = new LatLngBounds.Builder();
		try {
			LatLng lastLatLng = null;
			for (String line : Utils.getFileLines(file)) {
				String cols[] = line.split(Factory.columnSeparator);
				if (cols.length > 2) {
					LatLng latLng = new LatLng(Double.valueOf(cols[1]), Double.valueOf(cols[2]));
					MarkerOptions marker = new MarkerOptions().position(latLng).title(cols[0]);
					googleMap.addMarker(marker);
					boundsBuilder.include(latLng);
					if (showLine) {
						if (lastLatLng != null) {
							PolylineOptions polylineOptions = new PolylineOptions().add(lastLatLng, latLng).width(5).color(Factory.markerLineColor);
							googleMap.addPolyline(polylineOptions);
						}
						lastLatLng = latLng;
					}
				}
			}
		} catch (Exception e) {
			Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
			Log.e(LOG, "load", e);
		}
		LatLngBounds bounds = boundsBuilder.build();
		googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, this.getResources().getDisplayMetrics().widthPixels, this.getResources().getDisplayMetrics().heightPixels, 100));
	}

	public void getFiles() {
		files.clear();
		files.addAll(Utils.listFiles(Factory.getLocalDirecory()));
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		getFiles();
		int i = 0;
		for (File file : files) {
			menu.add(Menu.NONE, Menu.FIRST + i++, 0, file.getName());
		}
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		selectedFile = files.get(item.getItemId() - 1);
		Toast.makeText(this, getResources().getString(R.string.loading, selectedFile.getAbsolutePath()), Toast.LENGTH_LONG).show();
		load(selectedFile);
		return super.onOptionsItemSelected(item);
	}
}
