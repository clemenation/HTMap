package vn.edu.hut.htmap.activity;

import vn.edu.hut.htmap.view.PinAnnotationView;
import vn.edu.hut.htmap.view.PinAnnotationView.PinAnnotationViewDelegate;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

class PinOverlay extends ItemizedOverlay<OverlayItem> implements PinAnnotationViewDelegate
{
	/**
	 * 
	 */
	private final HTMapActivity htMapActivity;
	private OverlayItem pin = null;
	private Context context;
	private PinAnnotationView annotationView = null;

	public PinOverlay(HTMapActivity htMapActivity, Drawable marker, Context context) {			
		super(boundCenterBottom(marker));
		this.htMapActivity = htMapActivity;
		this.context = context;
		this.populate();

		this.annotationView = new PinAnnotationView(this.context, null);
		this.annotationView.setVisibility(View.GONE);
		this.annotationView.setDelegate(this);

		MapView.LayoutParams mapParams = new MapView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 
				ViewGroup.LayoutParams.WRAP_CONTENT,
				null,
				0,
				5-marker.getIntrinsicHeight(),
				MapView.LayoutParams.BOTTOM_CENTER);

		this.htMapActivity.mapView.addView(this.annotationView, mapParams);
	}

	@Override
	protected OverlayItem createItem(int arg0) {
		return this.pin;
	}

	@Override
	public int size() {
		if (this.pin == null) return 0;
		return 1;
	}

	public void setPin(OverlayItem overlay)
	{
		this.pin = overlay;
		this.htMapActivity.mapView.getController().animateTo(this.pin.getPoint());
		this.populate();

		((MapView.LayoutParams)this.annotationView.getLayoutParams()).point = this.getPin().getPoint();
		this.annotationView.setVisibility(View.GONE);
		this.annotationView.setVisibility(View.VISIBLE);	
	}

	public OverlayItem getPin()
	{
		return this.pin;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event, MapView mapView)
	{
		return super.onTouchEvent(event, mapView);
	}

	@Override
	public boolean onTap(GeoPoint p, MapView mapView)
	{		
		this.setPin(new OverlayItem(p, "Pin", "Pin"));

		return true;
	}

	// PinAnnotationView delegate methods		
	public void onDirectionButtonClick(PinAnnotationView view) {
		this.htMapActivity.to = this.pin.getPoint();

		new Thread(this.htMapActivity.getPath).start();
	}

	public void onDetailButtonClick(PinAnnotationView view) {
		// TODO Open detail activity

		Log.i("Log", "Detail clicked");
	}

}