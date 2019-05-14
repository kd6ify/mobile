package com.futureconcepts.ax.trinity.osm;

public class DrawingStyle {
	
	String penColor = "255000000000";//Default Line Color
	int penThickness = 3;//default line thickness
	
	String brushColor = "150255000000"; //Default fill color.
	String brushStyle="Solid";
	
	public DrawingStyle(){	
	}

	public String  getPenColor() {
		return penColor;
	}

	public void setPenColor(String string) {
		this.penColor = string;
	}

	public int getPenThickness() {
		return penThickness;
	}

	public void setPenThickness(int penThickness) {
		this.penThickness = penThickness;
	}

	public String getBrushColor() {
		return brushColor;
	}

	public void setBrushColor(String string) {
		this.brushColor = string;
	}

	public String getBrushStyle() {
		return brushStyle;
	}

	public void setBrushStyle(String style) {
		brushStyle = style;
	}
	
}
