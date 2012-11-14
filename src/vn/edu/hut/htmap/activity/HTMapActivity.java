package vn.edu.hut.htmap.activity;

import vn.edu.hut.htmap.R;
import vn.edu.hut.htmap.model.GoogleParser;
import vn.edu.hut.htmap.model.Parser;
import vn.edu.hut.htmap.model.Route;
import vn.edu.hut.htmap.model.Segment;
import vn.edu.hut.htmap.view.PinAnnotationView;
import vn.edu.hut.htmap.view.PinAnnotationView.PinAnnotationViewDelegate;
import vn.edu.hut.htmap.view.PinOverlayManager;
import vn.edu.hut.htmap.view.RouteInstructionView;
import vn.edu.hut.htmap.view.RouteInstructionView.RouteInstructionViewDataSource;
import vn.edu.hut.htmap.view.RouteInstructionView.RouteInstructionViewDelegate;
import vn.edu.hut.htmap.view.RouteNodeOverlayManager;
import vn.edu.hut.htmap.view.RouteOverlay;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ZoomControls;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

import de.android1.overlaymanager.OverlayManager;

public class HTMapActivity extends MapActivity implements RouteInstructionViewDataSource, RouteInstructionViewDelegate, PinAnnotationViewDelegate {
	private MapView mapView;
	private PinOverlayManager pinOverlayManager;
	private GeoPoint from;
	private GeoPoint to;
	private RouteOverlay routeOverlay;
	private MyLocationOverlay me = null;
	private Route route = null;
	private RouteInstructionView instructionView = null;
	private OverlayManager overlayManager = null;
	private RouteNodeOverlayManager routeNodeOverlayManager = null;
	private boolean directionMode = false;
	private ProgressDialog progressDialog = null; 

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
		final double longitudeFrom = 105.849813;

		GeoPoint defaultLocation = this.geoPoint(latitudeFrom, longitudeFrom);

		mapController.animateTo(defaultLocation);

		// Create overlayManager
		this.overlayManager = new OverlayManager(this, this.mapView);

		// Draw pin overlay
		this.pinOverlayManager = new PinOverlayManager(this.overlayManager, this.getResources().getDrawable(R.drawable.pin_s));
		this.pinOverlayManager.setDelegate(this);

		// Create route node overlay
		this.routeNodeOverlayManager = new RouteNodeOverlayManager(this.overlayManager);

		// My location overlay
		this.me = new MyLocationOverlay(this, this.mapView);
		this.mapView.getOverlays().add(this.me);
		this.me.runOnFirstFix(new Runnable()
		{

			@Override
			public void run() {
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

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId()) {
		case android.R.id.home:
			this.setDirectionMode(false);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onBackPressed()
	{
		if (this.directionMode == true)
		{
			this.setDirectionMode(false);
			return;
		}

		super.onBackPressed();
	}

	private GeoPoint geoPoint(double lat, double lon)
	{
		return new GeoPoint((int)(lat*1E6),(int)(lon*1E6));
	}

	private GeoPoint midPoint(GeoPoint point1, GeoPoint point2)
	{
		return new GeoPoint((point1.getLatitudeE6() + point2.getLatitudeE6())/2,
				(point1.getLongitudeE6() + point2.getLongitudeE6())/2);
	}

	Runnable getPath = new Runnable()
	{
		public void run() {
			final HTMapActivity outer = HTMapActivity.this;
			outer.from = outer.me.getMyLocation();

			if (outer.from != null)
			{
				runOnUiThread(new Runnable()
				{
					@Override
					public void run() {
						outer.progressDialog = ProgressDialog.show(outer, "Direction", "Loading your direction", true, false);
					}
				});

				outer.route = directions(outer.from, outer.to);

				runOnUiThread(new Runnable() {
					public void run()
					{
						outer.progressDialog.dismiss();
						outer.setDirectionMode(true);
					}
				});
			} 
			else
			{
				runOnUiThread(new Runnable()
				{
					public void run()
					{
						// Display alert dialog indicates user location not found
						AlertDialog.Builder builder = new AlertDialog.Builder(outer);
						builder.setMessage("Cannot find user location")
						.setTitle("Error")
						.setNeutralButton("Back", null);
						AlertDialog dialog = builder.create();	
						dialog.show();
					}
				});

				Log.e("Location error", "Cannot find user location");
			}
		}
	};	

	public void setDirectionMode(boolean directionMode)
	{
		this.directionMode = directionMode;

		// Remove route overlay
		if (this.mapView.getOverlays().contains(this.routeOverlay))
		{
			this.mapView.getOverlays().remove(this.routeOverlay);
		}

		if (this.directionMode == true)
		{
			// Display route overlay
			this.routeOverlay = new RouteOverlay(this.route, Color.BLUE);			        
			this.mapView.getOverlays().add(this.routeOverlay);

			// Display route node overlay
			this.routeNodeOverlayManager.setRoute(this.route);

			// Display the instruction view
			this.instructionView.setVisibility(View.VISIBLE);
			this.instructionView.setCurrentIndex(0);

			// Zoom to route
			GeoPoint from = this.route.getFromPoint();
			GeoPoint to = this.route.getToPoint();
			this.mapView.getController().zoomToSpan(Math.abs(from.getLatitudeE6() - to.getLatitudeE6()), 
					Math.abs(from.getLongitudeE6() - to.getLongitudeE6()));
			this.mapView.getController().animateTo(this.midPoint(from, to));

			// Enable up button
			this.getActionBar().setDisplayHomeAsUpEnabled(true);
		}
		else
		{
			// Remove route node overlay
			this.routeNodeOverlayManager.setRoute(null);

			// Hide the instruction view
			this.instructionView.setVisibility(View.GONE);

			// Disable up button
			this.getActionBar().setDisplayHomeAsUpEnabled(false);
		}
	}

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
		this.mapView.getController().setZoom(17);
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


	// PinAnnotationView delegate methods	
	@Override
	public void onDirectionButtonClick(PinAnnotationView view) {
		this.to = view.getPoint();

		new Thread(this.getPath).start();

		this.pinOverlayManager.removePin();
	}

	@Override
	public void onDetailButtonClick(PinAnnotationView view) {
		// TODO Auto-generated method stub

	}
}
