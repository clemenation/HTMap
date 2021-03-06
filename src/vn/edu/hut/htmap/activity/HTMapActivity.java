package vn.edu.hut.htmap.activity;

import vn.edu.hut.htmap.R;
import vn.edu.hut.htmap.model.DirectionParser;
import vn.edu.hut.htmap.model.GoogleDirectionParser;
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
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

import de.android1.overlaymanager.OverlayManager;

public class HTMapActivity extends MapActivity implements RouteInstructionViewDataSource, RouteInstructionViewDelegate, PinAnnotationViewDelegate {
	public final static String LOCATION_COORDINATE_EXTRA = "vn.edu.hut.htmap.LocationCoordinate";
	
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

			public void run() {
				mapController.animateTo(me.getMyLocation());
			}

		});

		// Get the instruction view and set its
		// data source & delegate
		this.instructionView = (RouteInstructionView)this.findViewById(R.id.route_instruction_view);
		this.instructionView.setDataSource(this);
		this.instructionView.setDelegate(this);

		// Disable action bar title
		this.getActionBar().setDisplayShowTitleEnabled(false);
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
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = this.getMenuInflater();
		inflater.inflate(R.menu.htmap_options, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{		
		switch (item.getItemId()) {
		case android.R.id.home:
			this.setDirectionMode(false);
			return true;
		case R.id.menu_zoom_in:
			this.mapView.getController().zoomIn();
			return true;
		case R.id.menu_zoom_out:
			this.mapView.getController().zoomOut();
			return true;
		case R.id.menu_locate_user:
			try
			{
				this.mapView.getController().animateTo(this.me.getMyLocation());
				this.mapView.getController().setZoom(17);
			}
			catch (NullPointerException e)
			{
				this.userLocationNotFoundAlert();
			}
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
					public void run() {
						outer.progressDialog = ProgressDialog.show(outer, 
								"Direction", "Loading your direction", true, true);
						outer.progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener()
						{

							public void onCancel(DialogInterface dialog) {
								// TODO Auto-generated method stub
								outer.to = null;
							}

						});
					}
				});

				final GeoPoint from = outer.from;
				final GeoPoint to = outer.to;				
				final Route route = directions(from, to);

				runOnUiThread(new Runnable() {
					public void run()
					{
						outer.progressDialog.dismiss();

						if ((from == outer.from) && (to == outer.to))
						{
							// if the from & to has not changed after getting direction							
							outer.route = route;
							outer.setDirectionMode(true);
						}
						else
						{
							Log.e("HTMapActivity", "From & to changed");
						}
					}
				});
			} 
			else
			{
				runOnUiThread(new Runnable()
				{
					public void run()
					{
						outer.userLocationNotFoundAlert();
					}
				});

				Log.e("Location error", "Cannot find user location");
			}
		}
	};	

	public void userLocationNotFoundAlert()
	{
		// Display alert dialog indicates user location not found
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Cannot find user location")
		.setTitle("Error")
		.setNeutralButton("Back", null);
		AlertDialog dialog = builder.create();	
		dialog.show();
	}

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
			GeoPoint boundsNE = this.route.getBoundsNE();
			GeoPoint boundsSW = this.route.getBoundsSW();
			this.mapView.getController().zoomToSpan(Math.abs(boundsNE.getLatitudeE6() - boundsSW.getLatitudeE6()), 
					Math.abs(boundsNE.getLongitudeE6() - boundsSW.getLongitudeE6()));
			this.mapView.getController().animateTo(this.midPoint(boundsNE, boundsSW));

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
	 * @param from Start point
	 * @param to Destination
	 * @return A route from start to destination
	 */
	public static Route directions(final GeoPoint from, final GeoPoint to) {
		DirectionParser parser;
		parser = new GoogleDirectionParser(from, to);
		Route r =  parser.parse();
		return r;
	}

	// Instruction view delegate methods
	public void onChangedIndex(int index) {
		this.mapView.getController().animateTo(this.route.getSegments().get(index).getStartPoint());
		this.mapView.getController().setZoom(17);

		this.routeNodeOverlayManager.setSelectedNode(index);
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
	public void onDirectionButtonClick(PinAnnotationView view) {
		this.to = view.getPoint();

		new Thread(this.getPath).start();

		this.pinOverlayManager.removePin();
	}

	public void onDetailButtonClick(PinAnnotationView view) {
		this.showLocationDetail(view.getPoint());
	}
	
	public void showLocationDetail(GeoPoint location)
	{
		Intent locationDetailIntent = new Intent(this, HTLocationDetailActivity.class);
		int[] locationCoordinate = {location.getLatitudeE6(), location.getLongitudeE6()};
		locationDetailIntent.putExtra(HTMapActivity.LOCATION_COORDINATE_EXTRA, locationCoordinate);
		this.startActivity(locationDetailIntent);
	}
}
