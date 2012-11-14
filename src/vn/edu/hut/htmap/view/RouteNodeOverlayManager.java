package vn.edu.hut.htmap.view;

import java.util.ArrayList;
import java.util.List;

import vn.edu.hut.htmap.R;
import vn.edu.hut.htmap.model.Route;
import vn.edu.hut.htmap.model.Segment;
import android.content.Context;
import android.graphics.drawable.Drawable;
import de.android1.overlaymanager.ManagedOverlay;
import de.android1.overlaymanager.ManagedOverlayItem;
import de.android1.overlaymanager.OverlayManager;

public class RouteNodeOverlayManager {

	private Route route = null;
	private OverlayManager manager = null;
	private ManagedOverlay routeNodeOverlay = null;
	private Context context = null;
	private int selectedNode = 0;
	private Drawable nodeMarker = null;
	private Drawable selectedMarker = null;

	public RouteNodeOverlayManager(OverlayManager manager)
	{
		this.manager = manager;
		this.context = this.manager.getContext();
		
		this.nodeMarker = this.context.getResources().getDrawable(R.drawable.node);
		ManagedOverlay.boundToCenter(this.nodeMarker);
		
		this.selectedMarker = this.context.getResources().getDrawable(R.drawable.node_selected);
		ManagedOverlay.boundToCenter(this.selectedMarker);
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
			this.routeNodeOverlay = this.manager.createOverlay("RouteNodeOverlay", this.nodeMarker);

			this.manager.populate();		

			// Draw points
			List<ManagedOverlayItem> routeNodeItem = new ArrayList<ManagedOverlayItem>();

			// Draw the from point		
			routeNodeItem.add(new ManagedOverlayItem(this.route.getFromPoint(), "from", null));

			// Draw the in between points
			List<Segment> segments = this.route.getSegments();		
			for (int i=1; i<segments.size(); i++)
			{
				routeNodeItem.add(new ManagedOverlayItem(segments.get(i).startPoint(), "node" + i, null));
			}
			
			// Draw the to point
			routeNodeItem.add(new ManagedOverlayItem(this.route.getToPoint(), "to", null));

			this.routeNodeOverlay.addAll(routeNodeItem);
		}
	}
	
	public void setSelectedNode(int index)
	{
		this.routeNodeOverlay.getItem(this.selectedNode).setMarker(this.nodeMarker);
		
		this.selectedNode = index;
		
		this.routeNodeOverlay.getItem(this.selectedNode).setMarker(this.selectedMarker);
	}
}
