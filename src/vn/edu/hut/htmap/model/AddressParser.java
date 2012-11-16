package vn.edu.hut.htmap.model;

import com.google.android.maps.GeoPoint;

public interface AddressParser {
	public Address parse();
	public GeoPoint getRequestLocation();
}
