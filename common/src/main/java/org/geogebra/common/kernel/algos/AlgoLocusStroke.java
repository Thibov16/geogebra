/* 
GeoGebra - Dynamic Mathematics for Everyone
http://www.geogebra.org

This file is part of GeoGebra.

This program is free software; you can redistribute it and/or modify it 
under the terms of the GNU General Public License as published by 
the Free Software Foundation.

 */

package org.geogebra.common.kernel.algos;

import java.util.ArrayList;

import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.MyPoint;
import org.geogebra.common.kernel.SegmentType;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.commands.Commands;
import org.geogebra.common.kernel.geos.GeoBoolean;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoLocusStroke;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.kernelND.GeoPointND;

/**
 * Creates a PolyLine from a given list of points or point array.
 * 
 * @author Michael Borcherds
 */
public class AlgoLocusStroke extends AlgoElement
		implements AlgoStrokeInterface {

	protected GeoLocusStroke poly; // output

	/**
	 * @param cons
	 *            the construction
	 * @param label
	 *            name of the polyline
	 * @param points
	 *            vertices of the polygon
	 * @param geoList
	 *            list of vertices of the polygon (alternative to points)
	 */


	public AlgoLocusStroke(Construction cons, GeoPointND[] points) {
		super(cons);
		poly = new GeoLocusStroke(this.cons);
		updatePointArray(points);

		// Log.debug(penStroke);

		// poly = new GeoPolygon(cons, points);


		// updatePointArray already covered compute

		input = new GeoElement[points.length + 1];
		for (int i = 0; i < points.length; i++) {
			input[i] = (GeoElement) points[i];
		}

		input[points.length] = new GeoBoolean(cons, true); // dummy to
															// force
															// PolyLine[...,
															// true]

		setInputOutput(); // for AlgoElement

	}

	@Override
	public Commands getClassName() {
		return Commands.PolyLine;
	}

	@Override
	public int getRelatedModeID() {
		return EuclidianConstants.MODE_POLYLINE;
	}

	/**
	 * @return - true, if poly is pen stroke
	 */
	public boolean getIsPenStroke() {
		return true;
	}

	/**
	 * Update point array of polygon using the given array list
	 * 
	 * @param pointList
	 */
	private void updatePointArray(GeoPointND[] data) {
		// check if we have a point list

		// create new points array
		int size = data.length;
		poly.setDefined(true);
		poly.getPoints().clear();
		for (int i = 0; i < size; i++) {
			poly.getPoints().add(new MyPoint(data[i].getInhomX(),
					data[i].getInhomY(),
					i == 0 ? SegmentType.MOVE_TO : SegmentType.LINE_TO));
		}


	}

	// for AlgoElement
	@Override
	protected void setInputOutput() {





		// set dependencies
		for (int i = 0; i < input.length; i++) {
			input[i].addAlgorithm(this);
		}

		// set output
		setOutputLength(1);
		setOutput(0, poly);
		setDependencies();
	}

	@Override
	public void update() {
		// compute output from input
		compute();
		getOutput(0).update();
	}



	@Override
	public void compute() {

		poly.getPoints().clear();
		for (int i = 0; i < input.length - 1; i++) {
			GeoPoint pt = (GeoPoint) input[i];
			poly.getPoints().add(new MyPoint(pt.getInhomX(), pt.getInhomY(),
					i == 0 ? SegmentType.MOVE_TO : SegmentType.LINE_TO));
		}

	}


	@Override
	final public String toString(StringTemplate tpl) {

		return "";

	}

	public final MyPoint[] getPointsND() {

		return poly.getPointsND();
	}

	public int getPointsLength() {
		return poly.getPointLength();
	}

	public GeoPoint getPointCopy(int i) {
		return new GeoPoint(cons, poly.getPoints().get(i).getInhomX(),
				poly.getPoints().get(i).getInhomY(), 1);
	}

	public ArrayList<MyPoint> getPoints() {
		return poly.getPoints();
	}

	public void updateFrom(MyPoint[] data) {
		int size = data.length;
		poly.setDefined(true);
		poly.getPoints().clear();
		for (int i = 0; i < size; i++) {
			poly.getPoints().add(data[i]);
		}
		updateInput(data);
		getOutput(0).update();
	}

	public void updateInput(Object[] data) {

		input = new GeoElement[data.length + 1];
		for (int i = 0; i < data.length; i++) {
			input[i] = new GeoPoint(cons, ((MyPoint) data[i]).getInhomX(),
					((MyPoint) data[i]).getInhomY(), 1);
		}

		input[data.length] = new GeoBoolean(cons, true); // dummy to
															// force
															// PolyLine[...,
															// true]

		// set dependencies
		for (int i = 0; i < input.length; i++) {
			input[i].addAlgorithm(this);
		}
	}

}
