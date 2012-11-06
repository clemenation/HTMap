package vn.edu.hut.htmap.activity;

import vn.edu.hut.htmap.view.PinAnnotationView;
import vn.edu.hut.htmap.view.PinAnnotationView.PinAnnotationViewDelegate;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

class PinOverlay extends ItemizedOverlay<OverlayItem> implements OnGestureListener, PinAnnotationViewDelegate
{
	/**
	 * 
	 */
	private final HTMapActivity htMapActivity;
	private OverlayItem pin = null;
	private GestureDetector gestureDetector;
	private Context context;
	private PinAnnotationView annotationView = null;

	public PinOverlay(HTMapActivity htMapActivity, Drawable marker, Context context) {			
		super(boundCenterBottom(marker));
		this.htMapActivity = htMapActivity;
		this.context = context;
		this.populate();

		this.gestureDetector = new GestureDetector(this.context, this);
		this.gestureDetector.setIsLongpressEnabled(true);
		
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

	@Override
	public boolean onTouchEvent(MotionEvent event, MapView mapView)
	{
		return this.gestureDetector.onTouchEvent(event);
	}

	public void setPin(OverlayItem overlay)
	{
		this.pin = overlay;
		this.populate();
	}

	public OverlayItem getPin()
	{
		return this.pin;
	}

	@Override
	protected boolean onTap(int i)
	{
		((MapView.LayoutParams)this.annotationView.getLayoutParams()).point = this.getPin().getPoint();
		this.annotationView.setVisibility(View.GONE);
		this.annotationView.setVisibility(View.VISIBLE);		
		
		return true;
	}



	// OnGestureListener methods

	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		return false;
	}

	public void onLongPress(MotionEvent e) {
		GeoPoint pinPoint = this.htMapActivity.mapView.getProjection().fromPixels((int)e.getX(), (int)e.getY());
		this.setPin(new OverlayItem(pinPoint, "Pin", "Pin"));

	}

	public boolean onScroll(MotionEvent e1, MotionEvent e2,
			float distanceX, float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	public boolean onSingleTapUp(MotionEvent e) {
		this.annotationView.setVisibility(View.GONE);
		
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