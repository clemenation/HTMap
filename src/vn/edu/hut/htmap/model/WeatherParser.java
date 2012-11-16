package vn.edu.hut.htmap.model;

import com.google.android.maps.GeoPoint;

public interface WeatherParser {
	public Weather parse();
	public GeoPoint getRequestLocation();
}
