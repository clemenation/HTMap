package vn.edu.hut.htmap.model;

import com.google.android.maps.GeoPoint;

public class Segment {
	/** Points in this segment. **/
	private GeoPoint start;
	private GeoPoint end;
	/** Turn instruction to reach next segment. **/
	private String instruction;
	/** Length of segment. **/
	private int length;
	/** Distance covered. **/
	private double distance;

	/**
	 * Create an empty segment.
	 */

	public Segment() {
	}


	/**
	 * Set the turn instruction.
	 * @param turn Turn instruction string.
	 */

	public void setInstruction(final String turn) {
		this.instruction = turn;
	}

	/**
	 * Get the turn instruction to reach next segment.
	 * @return a String of the turn instruction.
	 */

	public String getInstruction() {
		return instruction;
	}

	/**
	 * Add a point to this segment.
	 * @param point GeoPoint to add.
	 */

	public void setStartPoint(final GeoPoint point) {
		start = point;
	}

	public void setEndPoint(final GeoPoint point)
	{
		this.end = point;
	}

	/** Get the starting point of this 
	 * segment.
	 * @return a GeoPoint
	 */

	public GeoPoint getStartPoint() {
		return start;
	}
	
	public GeoPoint getEndPoint()
	{
		return this.end;
	}

	/** Creates a segment which is a copy of this one.
	 * @return a Segment that is a copy of this one.
	 */

	public Segment copy() {
		final Segment copy = new Segment();
		copy.start = start;
		copy.instruction = instruction;
		copy.length = length;
		copy.distance = distance;
		return copy;
	}

	/**
	 * @param length the length to set
	 */
	public void setLength(final int length) {
		this.length = length;
	}

	/**
	 * @return the length
	 */
	public int getLength() {
		return length;
	}

	/**
	 * @param distance the distance to set
	 */
	public void setDistance(double distance) {
		this.distance = distance;
	}

	/**
	 * @return the distance
	 */
	public double getDistance() {
		return distance;
	}

}