package vn.edu.hut.htmap.view;

import vn.edu.hut.htmap.R;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;

public class PinAnnotationView extends LinearLayout implements OnClickListener {

	private Button directionButton = null;
	private TextView detailText = null;
	private PinAnnotationViewDelegate delegate = null;
	private GeoPoint point = null;
	private LinearLayout detailLayout = null;

	public PinAnnotationView(Context context, AttributeSet attrs) {
		super(context, attrs);

		LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutInflater.inflate(R.layout.pin_annotation_view, this);

		this.directionButton = (Button)this.findViewById(R.id.pin_annotation_view_direction_button);
		this.directionButton.setOnClickListener(this);
		
		this.detailText = (TextView)this.findViewById(R.id.pin_annotation_view_detail_text);
			
		this.detailLayout = (LinearLayout)this.findViewById(R.id.pin_annotation_view_detail_layout);
		this.detailLayout.setOnClickListener(this);
	}

	public void setDelegate(PinAnnotationViewDelegate delegate)
	{
		this.delegate = delegate;
	}

	public void setPoint(GeoPoint point)
	{
		this.point = point;
		
		// TODO get detail of point and display in
		// its detailText
	}
	
	public void onClick(View v) {

		if (v == this.directionButton)
		{
			try
			{
				this.delegate.onDirectionButtonClick(this);
			}
			catch (NullPointerException e)
			{
				Log.e("PinAnnotationView", "No delegate");
			}
		}
		
		if (v == this.detailLayout)
		{
			try
			{
				this.delegate.onDetailButtonClick(this);
			}
			catch (NullPointerException e)
			{
				Log.e("PinAnnotationView", "No delegate");
			}
		}
	}

	public interface PinAnnotationViewDelegate
	{
		void onDirectionButtonClick(PinAnnotationView view);	
		void onDetailButtonClick(PinAnnotationView view);
	}

}
