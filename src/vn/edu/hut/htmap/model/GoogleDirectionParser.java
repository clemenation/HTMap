package vn.edu.hut.htmap.model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.Html;
import android.util.Log;

import com.google.android.maps.GeoPoint;

public class GoogleDirectionParser extends XMLParser implements DirectionParser {
	/** Distance covered. **/
	private int distance;
	
	public GoogleDirectionParser(GeoPoint from, GeoPoint to)
	{
		super(GoogleDirectionParser.feedUrlFromPoints(from, to));
	}
	
	/**
	 * Use the defined points to create the direction URL
	 * @param from
	 * @param to
	 * @return a URL that uses Google Direction API
	 */
	public static String feedUrlFromPoints(GeoPoint from, GeoPoint to)
	{
		String jsonURL = "http://maps.google.com/maps/api/directions/json?";
		final StringBuffer sBuf = new StringBuffer(jsonURL);
		sBuf.append("origin=");
		sBuf.append(from.getLatitudeE6()/1E6);
		sBuf.append(',');
		sBuf.append(from.getLongitudeE6()/1E6);
		sBuf.append("&destination=");
		sBuf.append(to.getLatitudeE6()/1E6);
		sBuf.append(',');
		sBuf.append(to.getLongitudeE6()/1E6);
		sBuf.append("&sensor=true&mode=driving");
		
		return sBuf.toString();
	}

	/**
	 * Parses a url pointing to a Google JSON object to a Route object.
	 * @return a Route object based on the JSON object.
	 */

	public Route parse() {
		// turn the stream into a string
		final String result = convertStreamToString(this.getInputStream());
		//Create an empty route
		final Route route = new Route();
		//Create an empty segment
		final Segment segment = new Segment();
		try {
			//Tranform the string into a json object
			final JSONObject json = new JSONObject(result);
			//Get the route object
			final JSONObject jsonRoute = json.getJSONArray("routes").getJSONObject(0);
			
			//Get the bounds
			JSONObject bounds = jsonRoute.getJSONObject("bounds");
			JSONObject boundsNE = bounds.getJSONObject("northeast");
			route.setBoundsNE(new GeoPoint((int)(boundsNE.getDouble("lat")*1E6),
					(int)(boundsNE.getDouble("lng")*1E6)));
			JSONObject boundsSW = bounds.getJSONObject("southwest");
			route.setBoundsSW(new GeoPoint((int)(boundsSW.getDouble("lat")*1E6),
					(int)(boundsSW.getDouble("lng")*1E6)));
			
			//Get the leg, only one leg as we don't support waypoints
			final JSONObject leg = jsonRoute.getJSONArray("legs").getJSONObject(0);
			//Get the steps for this leg
			final JSONArray steps = leg.getJSONArray("steps");
			//Number of steps for use in for loop
			final int numSteps = steps.length();
			//Set the name of this route using the start & end addresses
			route.setName(leg.getString("start_address") + " to " + leg.getString("end_address"));
			//Get google's copyright notice (tos requirement)
			route.setCopyright(jsonRoute.getString("copyrights"));
			//Get the total length of the route.
			route.setLength(leg.getJSONObject("distance").getInt("value"));
			//Get any warnings provided (tos requirement)
			if (!jsonRoute.getJSONArray("warnings").isNull(0)) {
				route.setWarning(jsonRoute.getJSONArray("warnings").getString(0));
			}
			/* Loop through the steps, creating a segment for each one and
			 * decoding any polylines found as we go to add to the route object's
			 * map array. Using an explicit for loop because it is faster!
			 */
			for (int i = 0; i < numSteps; i++) {
				//Get the individual step
				final JSONObject step = steps.getJSONObject(i);
				
				//Get the start position for this step and set it on the segment
				final JSONObject start = step.getJSONObject("start_location");
				GeoPoint position = new GeoPoint((int) (start.getDouble("lat")*1E6), 
						(int) (start.getDouble("lng")*1E6));
				segment.setStartPoint(position);
				
				// Get the end position for this step and set it on the segment
				final JSONObject end = step.getJSONObject("end_location");
				position = new GeoPoint((int) (end.getDouble("lat")*1E6), 
						(int) (end.getDouble("lng")*1E6));
				segment.setEndPoint(position);
				
				//Set the length of this segment in metres
				final int length = step.getJSONObject("distance").getInt("value");
				distance += length;
				segment.setLength(length);
				segment.setDistance(distance/1000);
				//Strip html from google directions and set as turn instruction
				String instruction = Html.fromHtml(step.getString("html_instructions")).toString().replace("\n\n", "\n").trim().replace("\n", ". ");
				segment.setInstruction(instruction);
				//Retrieve & decode this segment's polyline and add it to the route.
				route.addPoints(decodePolyLine(step.getJSONObject("polyline").getString("points")));
				//Push a copy of the segment to the route
				route.addSegment(segment.copy());
			}
		} catch (JSONException e) {
			Log.e(e.getMessage(), "Google JSON Parser - " + feedUrl);
		}
		return route;
	}

	/**
	 * Decode a polyline string into a list of GeoPoints.
	 * @param poly polyline encoded string to decode.
	 * @return the list of GeoPoints represented by this polystring.
	 */

	private List<GeoPoint> decodePolyLine(final String poly) {
		int len = poly.length();
		int index = 0;
		List<GeoPoint> decoded = new ArrayList<GeoPoint>();
		int lat = 0;
		int lng = 0;

		while (index < len) {
			int b;
			int shift = 0;
			int result = 0;
			do {
				b = poly.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lat += dlat;

			shift = 0;
			result = 0;
			do {
				b = poly.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lng += dlng;

			decoded.add(new GeoPoint(
					(int) (lat*1E6 / 1E5), (int) (lng*1E6 / 1E5)));
		}

		return decoded;
	}
}