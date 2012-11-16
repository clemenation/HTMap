package vn.edu.hut.htmap.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.google.android.maps.GeoPoint;

public class GoogleAddressParser extends XMLParser implements AddressParser {
	
	private GeoPoint requestLocation;

	public GoogleAddressParser(GeoPoint location)
	{
		super(GoogleAddressParser.feedURLFromLocation(location));
		
		this.requestLocation = location;
	}
	
	public GeoPoint getRequestLocation()
	{
		return this.requestLocation;
	}

	public static String feedURLFromLocation(GeoPoint location)
	{
		String jsonURL = "http://maps.googleapis.com/maps/api/geocode/json?latlng=";
		final StringBuffer sBuf = new StringBuffer(jsonURL);
		sBuf.append(location.getLatitudeE6()/1E6);
		sBuf.append(',');
		sBuf.append(location.getLongitudeE6()/1E6);
		sBuf.append("&sensor=true");

		return sBuf.toString();
	}

	@Override
	public Address parse() {
		// Turn the stream into a string
		final String result = convertStreamToString(this.getInputStream());
		// Create an empty address
		final Address address = new Address();

		try
		{
			final JSONObject json = new JSONObject(result);
			
			final JSONObject locationResult = json.getJSONArray("results").getJSONObject(0);
			
			// Get the formatted address
			address.setFormattedAddress(locationResult.getString("formatted_address"));
			
			// Get the coordinate location
			final JSONObject location = locationResult.getJSONObject("geometry").getJSONObject("location");			
			address.setLocation(new GeoPoint((int)(location.getDouble("lat") * 1E6),
					(int)(location.getDouble("lng") * 1E6)));
		} catch (JSONException e) {
			Log.e(e.getMessage(), "Google JSON Parser - " + feedUrl);
		}
		
		return address;
	}

}
