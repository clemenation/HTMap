package vn.edu.hut.htmap.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.google.android.maps.GeoPoint;

public class WundergroundWeatherParser extends XMLParser implements WeatherParser 
{
	private GeoPoint requestLocation;

	public WundergroundWeatherParser(GeoPoint location) {
		super(WundergroundWeatherParser.feedURLFromLocation(location));
		
		this.requestLocation = location;
	}

	public GeoPoint getRequestLocation()
	{
		return this.requestLocation;
	}

	public static String feedURLFromLocation(GeoPoint location)
	{
		String jsonURL = "http://api.wunderground.com/api/ece4fd8090a2ecba/conditions/forecast/q/";
		final StringBuffer sBuf = new StringBuffer(jsonURL);
		sBuf.append(location.getLatitudeE6()/1E6);
		sBuf.append(',');
		sBuf.append(location.getLongitudeE6()/1E6);
		sBuf.append(".json");

		return sBuf.toString();
	}

	@Override
	public Weather parse() {
		// Turn the stream into a string
		final String result = convertStreamToString(this.getInputStream());
		
		// Create an empty weather
		final Weather weather = new Weather();

		try
		{
			final JSONObject json = new JSONObject(result);

			final JSONObject currentObservation = json.getJSONObject("current_observation");

			// Get the full location name
			weather.setLocationName(currentObservation.getJSONObject("display_location").getString("full"));

			// Get the weather condition
			weather.setWeatherCondition(currentObservation.getString("weather"));
			
			// Get the current temperature
			weather.setTemperatureC(currentObservation.getInt("temp_c"));
			
			// Get the current humidity
			weather.setRelativeHumidity(currentObservation.getString("relative_humidity"));
			
			// Get the current wind speed
			weather.setWindKPH(currentObservation.getInt("wind_kph"));
			
			// Get the weather icon
			weather.setIconURLString(currentObservation.getString("icon_url"));
			
			// Get weather forecast for today
			final JSONObject todayForecast = json.getJSONObject("forecast")
					.getJSONObject("simpleforecast")
					.getJSONArray("forecastday")
					.getJSONObject(0);
			
			// Get low and high temperature
			weather.setHighTempC(todayForecast.getJSONObject("high").getInt("celsius"));
			weather.setLowTempC(todayForecast.getJSONObject("low").getInt("celsius"));
			
			
		} catch (JSONException e) {
			Log.e(e.getMessage(), "Wunderground JSON Parser - " + feedUrl);
		}

		return weather;
	}

}
