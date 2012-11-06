package vn.edu.hut.htmap.view;

import vn.edu.hut.htmap.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class RouteInstructionView extends RelativeLayout implements OnClickListener {

	private RouteInstructionViewDataSource dataSource = null;
	private RouteInstructionViewDelegate delegate = null;
	private int currentIndex=0;
	private Button prevButton;
	private Button nextButton;
	private TextView instructionText;
	private TextView lengthText;

	public RouteInstructionView(Context context, AttributeSet attributes)
	{
		super(context, attributes);

		LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutInflater.inflate(R.layout.route_instruction_view, this);

		this.prevButton = (Button)this.findViewById(R.id.route_instruction_view_prev_button);
		this.prevButton.setOnClickListener(this);
		this.nextButton = (Button)this.findViewById(R.id.route_instruction_view_next_button);
		this.nextButton.setOnClickListener(this);

		this.instructionText = (TextView)this.findViewById(R.id.route_instruction_view_instruction_text);
		this.lengthText = (TextView)this.findViewById(R.id.route_instruction_view_length_text);
	}

	public void setDelegate(RouteInstructionViewDelegate delegate)
	{
		this.delegate = delegate;
	}

	public void setDataSource(RouteInstructionViewDataSource dataSource)
	{
		this.dataSource = dataSource;
	}

	public void onPrevButtonClick(View view)
	{
	}

	public void onNextButtonClick(View view)
	{
	}

	public void onClick(View v) {
		if (v == this.prevButton)
		{
			// Prev button clicked
			this.setCurrentIndex(this.currentIndex-1);

		}
		else if (v == this.nextButton)
		{
			// Next button clicked
			this.setCurrentIndex(this.currentIndex+1);

		}
	}

	public void setCurrentIndex(int index)
	{
		int size = this.dataSource.numberOfSegment();

		// Only works if index in correct bounds
		if (size > 0)
		{
			if (index >= 0 && index < size)
			{
				this.currentIndex = index;

				// Display instruction & length
				this.instructionText.setText(this.dataSource.instructionAtIndex(index));
				this.lengthText.setText(this.dataSource.lengthAtIndex(index) + "m");

				// Notify changed index to its delegate
				this.delegate.onChangedIndex(index);

				// Enable and disable appropriate button
				// based on current index
				this.prevButton.setEnabled(!(this.currentIndex <= 0));
				this.nextButton.setEnabled(!(this.currentIndex >= (size-1)));
			}
		}
	}

	public interface RouteInstructionViewDataSource
	{
		String instructionAtIndex(int index);
		double lengthAtIndex(int index);
		int numberOfSegment();
	}

	public interface RouteInstructionViewDelegate
	{
		void onChangedIndex(int index);
	}
}
