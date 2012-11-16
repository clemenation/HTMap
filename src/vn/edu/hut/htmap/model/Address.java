package vn.edu.hut.htmap.model;

import com.google.android.maps.GeoPoint;


public class Address {
	private String formattedAddress;
	private GeoPoint location;
	
	public void setFormattedAddress(String address)
	{
		this.formattedAddress = address;
	}
	
	public String getFormattedAddress()
	{
		return this.formattedAddress;
	}
	
	public void setLocation(GeoPoint location)
	{
		this.location = location;
	}
	
	public GeoPoint getLocation()
	{
		return this.location;
	}
}
