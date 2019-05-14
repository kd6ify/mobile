package com.futureconcepts.ax.model.dataset;

import com.futureconcepts.ax.model.data.Address;
import com.futureconcepts.ax.model.data.Asset;
import com.futureconcepts.ax.model.data.AssetAttribute;
import com.futureconcepts.ax.model.data.Equipment;

public class EquipmentViewDataSet extends DataSet
{
	public EquipmentViewDataSet()
	{
		super();
		addItem(new IncidentDataSetTable("IncidentAssetAddress", Address.CONTENT_URI));
		addItem(new IncidentDataSetTable("IncidentAssetCheckInLocation", Address.CONTENT_URI));
		addItem(new IncidentDataSetTable("IncidentAssetAttribute", AssetAttribute.CONTENT_URI));
		addItem(new IncidentDataSetTable("IncidentEquipment", Equipment.CONTENT_URI));
		addItem(new IncidentDataSetTable("IncidentAsset", Asset.CONTENT_URI));
	}
}
