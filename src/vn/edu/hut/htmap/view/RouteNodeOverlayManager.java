package vn.edu.hut.htmap.view;

import java.util.ArrayList;
import java.util.List;

import vn.edu.hut.htmap.R;
import vn.edu.hut.htmap.model.Route;
import vn.edu.hut.htmap.model.Segment;
import android.content.Context;
import de.android1.overlaymanager.ManagedOverlay;
import de.android1.overlaymanager.ManagedOverlayItem;
import de.android1.overlaymanager.OverlayManager;

public class RouteNodeOverlayManager {

	private Route route = null;
	private OverlayManager manager = null;
	private ManagedOverlay routeNodeOverlay = null;
	private Context context = null;

	public RouteNodeOverlayManager(OverlayManager manager)
	{
		this.manager = manager;
		this.context = this.manager.getContext();
	}

	public void setRoute(Route route)
	{
		this.route = route;

		this.reloadRoute();
	}

	public void reloadRoute()
	{
		if (this.routeNodeOverlay != null)
		{
			this.manager.removeOverlay(this.routeNodeOverlay);
			this.manager.populate();
		}

		if (this.route != null)
		{
			this.routeNodeOverlay = this.manager.createOverlay("RouteNodeOverlay", 
					this.context.getResources().getDrawable(R.drawable.pin_s));

			this.manager.populate();		

			// Draw points
			List<ManagedOverlayItem> routeNodeItem = new ArrayList<ManagedOverlayItem>();

			// Draw the from and to points
			routeNodeItem.add(new ManagedOverlayItem(this.route.getFromPoint(), null, null));
			routeNodeItem.add(new ManagedOverlayItem(this.route.getToPoint(), null, null));

			// Draw the in between points
			List<Segment> segments = this.route.getSegments();		
			for (int i=1; i<segments.size(); i++)
			{
				routeNodeItem.add(new ManagedOverlayItem(segments.get(i).startPoint(), null, null));
			}

			this.routeNodeOverlay.addAll(routeNodeItem);
		}
	}
}
