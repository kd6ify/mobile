package com.futureconcepts.anonymous;


public class DataList {
	String ID;
	String Name;
	private boolean selected;
	//Constructor
	public DataList(String ID, String Name) {
		super();
		this.ID = ID;
		this.Name = Name;
	}
	@Override
	public String toString() {
		return Name;
	}
	public String getID() {
		return ID;
	}
	public boolean isSelected() {
        return selected;
    }
	
	public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
