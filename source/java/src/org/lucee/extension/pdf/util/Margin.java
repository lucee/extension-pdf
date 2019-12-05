package org.lucee.extension.pdf.util;

import org.lucee.extension.pdf.PDFDocument;

import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.util.Cast;

public class Margin {

	public static final int UNIT_CM = 1;
	public static final int UNIT_IN = 2;
	public static final int UNIT_PT = 3;
	public static final int UNIT_PX = 4;

	public static final double UNIT_FACTOR_CM = 85d / 3d;// =28.333333333333333333333333333333333333333333;
	public static final double UNIT_FACTOR_IN = UNIT_FACTOR_CM * 2.54;
	public static final double UNIT_FACTOR_PT = 1;
	public static final double UNIT_FACTOR_PX = 1d / 12d / 16d;

	private int unitTop;
	private double unitFactorTop;
	private int unitBottom;
	private double unitFactorBottom;
	private int unitLeft;
	private double unitFactorLeft;
	private int unitRight;
	private double unitFactorRight;

	private final double top;
	private final double bottom;
	private final double left;
	private final double right;
	private Cast cast;

	public Margin(PDFDocument doc, double unitFactor, double top, double bottom, double left, double right) {
		this.cast = CFMLEngineFactory.getInstance().getCastUtil();

		this.unitFactorLeft = unitFactor;
		this.unitLeft = Margin.toUnit(unitFactor);
		this.unitFactorRight = unitFactor;
		this.unitRight = Margin.toUnit(unitFactor);

		// top
		if (top < 0) {
			this.top = doc.getHeader() != null ? PDFDocument.MARGIN_WITH_HF : PDFDocument.MARGIN_INIT;
			this.unitFactorTop = UNIT_FACTOR_PT;
			this.unitTop = UNIT_PT;
		}
		else {
			this.top = top;
			this.unitFactorTop = unitFactor;
			this.unitTop = Margin.toUnit(unitFactor);
		}

		// bottom
		if (bottom < 0) {
			this.bottom = doc.getFooter() != null ? PDFDocument.MARGIN_WITH_HF : PDFDocument.MARGIN_INIT;
			this.unitFactorBottom = UNIT_FACTOR_PT;
			this.unitBottom = UNIT_PT;
		}
		else {
			this.bottom = bottom;
			this.unitFactorBottom = unitFactor;
			this.unitBottom = Margin.toUnit(unitFactor);
		}

		// left
		if (left < 0) {
			this.left = 36;
			this.unitFactorLeft = UNIT_FACTOR_PT;
			this.unitLeft = UNIT_PT;
		}
		else {
			this.left = left;
			this.unitFactorLeft = unitFactor;
			this.unitLeft = Margin.toUnit(unitFactor);
		}

		// right
		if (right < 0) {
			this.right = 36;
			this.unitFactorRight = UNIT_FACTOR_PT;
			this.unitRight = UNIT_PT;
		}
		else {
			this.right = right;
			this.unitFactorRight = unitFactor;
			this.unitRight = Margin.toUnit(unitFactor);
		}
	}

	public double getTopAsPoint() {
		return toPoint(top, unitFactorTop);
	}

	public double getBottomAsPoint() {
		return toPoint(bottom, unitFactorBottom);
	}

	public double getLeftAsPoint() {
		return toPoint(left, unitFactorLeft);
	}

	public double getRightAsPoint() {
		return toPoint(right, unitFactorRight);
	}

	public String getTop() {
		return get(top, unitTop, unitFactorTop);
	}

	public String getBottom() {
		return get(bottom, unitBottom, unitFactorBottom);
	}

	public String getLeft() {
		return get(left, unitLeft, unitFactorLeft);
	}

	public String getRight() {
		return get(right, unitRight, unitFactorRight);
	}

	private String get(double nbr, int unit, double unitFactor) {

		if (unit == UNIT_PT || unit == UNIT_PX || isInteger(nbr)) {
			return cast.toString(nbr) + getUnitAsString(unit);
		}
		return cast.toString(toPoint(nbr, unitFactor)) + "pt";
	}

	private String getUnitAsString(int unit) {
		if (unit == UNIT_CM) return "cm";
		if (unit == UNIT_IN) return "in";
		if (unit == UNIT_PT) return "pt";
		return "px";
	}

	private boolean isInteger(double d) {
		return cast.toString(d).indexOf('.') == -1;
	}

	public final static int toPoint(double value, double unitFactor) {
		return (int) Math.round(value * unitFactor);
	}

	public static int toUnit(double unitFactor) {
		if (unitFactor == UNIT_FACTOR_CM) return UNIT_CM;
		if (unitFactor == UNIT_FACTOR_IN) return UNIT_IN;
		if (unitFactor == UNIT_FACTOR_PT) return UNIT_PT;
		return UNIT_PX;
	}
}