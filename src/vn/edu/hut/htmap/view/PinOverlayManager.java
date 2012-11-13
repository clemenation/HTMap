package vn.edu.hut.htmap.view;

import vn.edu.hut.htmap.view.PinAnnotationView.PinAnnotationViewDelegate;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

import de.android1.overlaymanager.ManagedOverlay;
import de.android1.overlaymanager.ManagedOverlayGestureDetector.OnOverlayGestureListener;
import de.android1.overlaymanager.ManagedOverlayItem;
import de.android1.overlaymanager.OverlayManager;
import de.android1.overlaymanager.ZoomEvent;

public class PinOverlayManager
{
	private PinAnnotationView annotationView = null;
	private Context context = null;
	private MapView mapView = null;
	private ManagedOverlayItem pin = null;
	private ManagedOverlay pinOverlay = null;
	private OverlayManager manager = null;
	private Drawable defaultMarker = null;

	public PinOverlayManager(OverlayManager manager, Drawable defaultMarker)
	{
		this.manager = manager;
		this.defaultMarker = defaultMarker;

		this.context = this.manager.getContext();
		this.mapView = this.manager.getMapView();

		this.annotationView = new PinAnnotationView(this.context, null);
		this.annotationView.setVisibility(View.GONE);

		MapView.LayoutParams mapParams = new MapView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 
				ViewGroup.LayoutParams.WRAP_CONTENT,
				null,
				0,
				5-defaultMarker.getIntrinsicHeight(),
				MapView.LayoutParams.BOTTOM_CENTER);

		this.mapView.addView(this.annotationView, mapParams);

		this.pinOverlay = this.manager.createOverlay("PinOverlay", this.defaultMarker);
		this.pinOverlay.setOnOverlayGestureListener(this.gestureListener);

		this.manager.populate();
	}
	
	public void setDelegate(PinAnnotationViewDelegate delegate)
	{
		this.annotationView.setDelegate(delegate);
	}

	public void setPinPoint(GeoPoint pinPoint)
	{
		if (this.pin != null)
		{
			this.pinOverlay.remove(this.pin);
		}
		this.pin = this.pinOverlay.createItem(pinPoint);

		this.annotationView.setPoint(this.getPin().getPoint());
		this.annotationView.setVisibility(View.VISIBLE);
		this.mapView.invalidate();

		this.mapView.getController().animateTo(this.pin.getPoint());
	}

	public ManagedOverlayItem getPin()
	{
		return this.pin;
	}

	private OnOverlayGestureListener gestureListener = new OnOverlayGestureListener()
	{

		@Override
		public boolean onSingleTap(MotionEvent arg0, ManagedOverlay arg1,
				GeoPoint arg2, ManagedOverlayItem arg3) {
			
			PinOverlayManager outer = PinOverlayManager.this;

			if (arg3 != null)
			{
				// if tapped a pin
				if (outer.annotationView.getVisibility() == View.VISIBLE)
				{
					outer.annotationView.setVisibility(View.GONE);
				}
				else
				{
					outer.annotationView.setVisibility(View.VISIBLE);
				}
				
				outer.mapView.invalidate();
				
				return true;
			}
			else
			{
				// hide annotationView if tap a blank space
				outer.annotationView.setVisibility(View.GONE);
				outer.mapView.invalidate();
				
				return true;
			}
		}

		@Override
		public void onLongPress(MotionEvent arg0, ManagedOverlay arg1) {
			GeoPoint pinPoint = PinOverlayManager.this.mapView.getProjection().fromPixels((int)arg0.getX(), (int)arg0.getY());
			PinOverlayManager.this.setPinPoint(pinPoint);
		}

		@Override
		public void onLongPressFinished(MotionEvent arg0, ManagedOverlay arg1,
				GeoPoint arg2, ManagedOverlayItem arg3) {
		}

		@Override
		public boolean onScrolled(MotionEvent arg0, MotionEvent arg1,
				float arg2, float arg3, ManagedOverlay arg4) {			
			return false;
		}

		@Override
		public boolean onZoom(ZoomEvent arg0, ManagedOverlay arg1) {
			return false;
		}

		@Override
		public boolean onDoubleTap(MotionEvent arg0, ManagedOverlay arg1,
				GeoPoint arg2, ManagedOverlayItem arg3) {
			return false;
		}

	};

}
