package vn.edu.hut.htmap.activity;

import vn.edu.hut.htmap.R;
import vn.edu.hut.htmap.model.Address;
import vn.edu.hut.htmap.model.AddressParser;
import vn.edu.hut.htmap.model.GoogleAddressParser;
import vn.edu.hut.htmap.model.Weather;
import vn.edu.hut.htmap.model.WeatherParser;
import vn.edu.hut.htmap.model.WundergroundWeatherParser;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

public class HTLocationDetailActivity extends Activity {
	
	private final static String DEGREE_SYMBOL = "¡";

	private GeoPoint location;
	private TextView addressText;
	private TextView locationNameText;
	private TextView temperatureText;
	private ImageView weatherIcon;
	private TextView weatherDetailText;
	private TextView lowHighTemperatureText;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.ht_location_detail_activity);
		
		// Get the views from layout
		this.addressText = (TextView)this.findViewById(R.id.ht_location_detail_address);
		this.locationNameText = (TextView)this.findViewById(R.id.ht_location_detail_location_name);
		this.temperatureText = (TextView)this.findViewById(R.id.ht_location_detail_temperature);
		this.weatherDetailText = (TextView)this.findViewById(R.id.ht_location_detail_weather_detail);
		this.lowHighTemperatureText = (TextView)this.findViewById(R.id.ht_location_detail_low_high_temperature);
		this.weatherIcon = (ImageView)this.findViewById(R.id.ht_location_detail_weather_icon);

		// Get the location from intent extra
		int[] locationCoordinate = this.getIntent().getIntArrayExtra(HTMapActivity.LOCATION_COORDINATE_EXTRA);
		this.setLocation(new GeoPoint(locationCoordinate[0], locationCoordinate[1]));
		
		// Enable the up button in action bar
		this.getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{		
		switch (item.getItemId()) {
		case android.R.id.home:
			this.onBackPressed();
			return true;		
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void setLocation(GeoPoint location)
	{
		this.location = location;
		
		this.clearDetailViews();

		new GetWeather().execute();
		new GetAddress().execute();
	}
	
	private void clearDetailViews()
	{
		this.addressText.setText("");
		this.locationNameText.setText("");
		this.temperatureText.setText("--" + DEGREE_SYMBOL);
		this.weatherDetailText.setText("");
		this.lowHighTemperatureText.setText("--" + DEGREE_SYMBOL + " --" + DEGREE_SYMBOL);
		this.weatherIcon.setImageDrawable(null);
		this.setTitle("Location Detail");
	}

	private class GetWeather extends AsyncTask<Void, Void, Void>
	{
		Weather weather;
		WeatherParser parser;
		HTLocationDetailActivity outer = HTLocationDetailActivity.this;

		@Override
		protected Void doInBackground(Void... params) {
			parser = new WundergroundWeatherParser(outer.location);
			this.weather = parser.parse();

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// check if point changed during getting weather detail
			if (outer.location == parser.getRequestLocation())
			{
				outer.locationNameText.setText(this.weather.getLocationName());
				outer.temperatureText.setText(this.weather.getTemperatureC() + DEGREE_SYMBOL);
				outer.lowHighTemperatureText.setText(this.weather.getLowTempC() + DEGREE_SYMBOL 
						+ " " + this.weather.getHighTempC() + DEGREE_SYMBOL);
				outer.weatherDetailText.setText(this.weather.getWeatherCondition() + "\n"
						+ "Humidity = " + this.weather.getRelativeHumidity() + "\n"
						+ "Wind: " + this.weather.getWindKPH() + "kph");
				outer.setTitle(this.weather.getLocationName());
				UrlImageViewHelper.setUrlDrawable(outer.weatherIcon, this.weather.getIconURLString());
			}
			else
			{
				Log.e("HTLocationDetailActivity", "Location changed");
			}
		}
	}
	
	private class GetAddress extends AsyncTask<Void, Void, Void>
	{
		Address address;
		AddressParser parser;
		HTLocationDetailActivity outer = HTLocationDetailActivity.this;

		@Override
		protected Void doInBackground(Void... params) {
			parser = new GoogleAddressParser(outer.location);
			this.address = parser.parse();

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// check if point changed during getting weather detail
			if (outer.location == parser.getRequestLocation())
			{
				outer.addressText.setText(this.address.getFormattedAddress());
			}
			else
			{
				Log.e("HTLocationDetailActivity", "Location changed");
			}
		}
	}
}
