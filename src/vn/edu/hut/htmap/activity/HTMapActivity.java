package vn.edu.hut.htmap.activity;

import vn.edu.hut.htmap.R;
import vn.edu.hut.htmap.model.GoogleParser;
import vn.edu.hut.htmap.model.Parser;
import vn.edu.hut.htmap.model.Route;
import vn.edu.hut.htmap.model.Segment;
import vn.edu.hut.htmap.view.RouteInstructionView;
import vn.edu.hut.htmap.view.RouteInstructionView.RouteInstructionViewDataSource;
import vn.edu.hut.htmap.view.RouteInstructionView.RouteInstructionViewDelegate;
import vn.edu.hut.htmap.view.RouteOverlay;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ZoomControls;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

public class HTMapActivity extends MapActivity implements RouteInstructionViewDataSource, RouteInstructionViewDelegate {
	MapView mapView;
	private PinOverlay pinOverlay;
	private GeoPoint from;
	GeoPoint to;
	private RouteOverlay routeOverlay;
	private MyLocationOverlay me = null;
	private Route route = null;
	private RouteInstructionView instructionView = null;


	@Override
	protected boolean isRouteDisplayed() { return false; }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_htmap);
		this.mapView = (MapView)this.findViewById(R.id.mapview);
		final MapController mapController = this.mapView.getController();
		mapController.setZoom(17);

		// Turn on built in zoom control & make it appear at right side of screen
		this.mapView.setBuiltInZoomControls(true);
		ZoomControls zoomControls = (ZoomControls)this.mapView.getZoomButtonsController().getZoomControls();
		((FrameLayout.LayoutParams)zoomControls.getLayoutParams()).gravity = Gravity.RIGHT;
		//Example data
		final double latitudeFrom = 21.034199;
		final double longitudeFrom =105.849813;

		GeoPoint defaultLocation = this.geoPoint(latitudeFrom, longitudeFrom);

		mapController.animateTo(defaultLocation);

		// Draw pin overlay
		this.pinOverlay = new 
				PinOverlay(this, this.getResources().getDrawable(R.drawable.pin_s),
						this);
		this.mapView.getOverlays().add(this.pinOverlay);

		this.me = new MyLocationOverlay(this, this.mapView);
		this.mapView.getOverlays().add(this.me);
		
		this.me.runOnFirstFix(new Runnable()
		{

			@Override
			public void run() {
				// TODO Auto-generated method stub
				mapController.animateTo(me.getMyLocation());
			}
			
		});

		// Get the instruction view and set its
		// data source & delegate
		this.instructionView = (RouteInstructionView)this.findViewById(R.id.route_instruction_view);
		this.instructionView.setDataSource(this);
		this.instructionView.setDelegate(this);
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		
		this.me.enableMyLocation();
	}
	
	@Override
	public void onPause()
	{
		this.me.disableMyLocation();
		
		super.onPause();
	}

	private GeoPoint geoPoint(double lat, double lon)
	{
		return new GeoPoint((int)(lat*1E6),(int)(lon*1E6));
	}

	Runnable getPath = new Runnable()
	{
		public void run() {
			final HTMapActivity outer = HTMapActivity.this;
			outer.from = outer.me.getMyLocation();
			
			if (outer.from != null)
			{
				outer.route = directions(outer.from, outer.to);

				runOnUiThread(new Runnable() {
					public void run()
					{

						// Display route overlay
						if (outer.mapView.getOverlays().contains(routeOverlay))
						{
							outer.mapView.getOverlays().remove(routeOverlay);
						}
						outer.routeOverlay = new RouteOverlay(route, Color.BLUE);			        
						outer.mapView.getOverlays().add(routeOverlay);

						outer.instructionView.setVisibility(View.VISIBLE);
						outer.instructionView.setCurrentIndex(0);					
					}
				});
			} 
			else
			{
				Log.e("Location error", "Cannot find user location");
			}
		}
	};

	/**
	 * Calls Google Maps API to get the route 
	 * @param start Start point
	 * @param dest Destination
	 * @return A route from start to destination
	 */
	private Route directions(final GeoPoint start, final GeoPoint dest) {
		Parser parser;
		String jsonURL = "http://maps.google.com/maps/api/directions/json?";
		final StringBuffer sBuf = new StringBuffer(jsonURL);
		sBuf.append("origin=");
		sBuf.append(start.getLatitudeE6()/1E6);
		sBuf.append(',');
		sBuf.append(start.getLongitudeE6()/1E6);
		sBuf.append("&destination=");
		sBuf.append(dest.getLatitudeE6()/1E6);
		sBuf.append(',');
		sBuf.append(dest.getLongitudeE6()/1E6);
		sBuf.append("&sensor=true&mode=driving");

		System.out.println("Route URL: " + sBuf.toString());

		parser = new GoogleParser(sBuf.toString());
		Route r =  parser.parse();
		return r;
	}

	// Instruction view delegate methods
	public void onChangedIndex(int index) {
		this.mapView.getController().animateTo(this.route.getSegments().get(index).startPoint());
	}

	// Instruction view data source methods
	public String instructionAtIndex(int index) {
		return ((Segment)this.route.getSegments().get(index)).getInstruction();
	}

	public double lengthAtIndex(int index) {
		return ((Segment)this.route.getSegments().get(index)).getLength();
	}

	public int numberOfSegment() {
		return (this.route.getSegments().size());
	}
}
