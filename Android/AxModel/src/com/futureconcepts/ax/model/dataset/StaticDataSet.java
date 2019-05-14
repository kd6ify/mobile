package com.futureconcepts.ax.model.dataset;

import com.futureconcepts.ax.model.data.AddressType;
import com.futureconcepts.ax.model.data.Agency;
import com.futureconcepts.ax.model.data.AgencyType;
import com.futureconcepts.ax.model.data.AssetStatus;
import com.futureconcepts.ax.model.data.AssetType;
import com.futureconcepts.ax.model.data.CollectionType;
import com.futureconcepts.ax.model.data.EquipmentType;
import com.futureconcepts.ax.model.data.Gender;
import com.futureconcepts.ax.model.data.INCITS38200x;
import com.futureconcepts.ax.model.data.Icon;
import com.futureconcepts.ax.model.data.Incident;
import com.futureconcepts.ax.model.data.IncidentType;
import com.futureconcepts.ax.model.data.JournalStatus;
import com.futureconcepts.ax.model.data.JournalType;
import com.futureconcepts.ax.model.data.OperationalPeriod;
import com.futureconcepts.ax.model.data.SourceType;
import com.futureconcepts.ax.model.data.TacticPriority;
import com.futureconcepts.ax.model.data.TacticStatus;
import com.futureconcepts.ax.model.data.TacticType;
import com.futureconcepts.ax.model.data.TriageColor;
import com.futureconcepts.ax.model.data.TriageStatus;
import com.futureconcepts.ax.model.data.UserRankType;
import com.futureconcepts.ax.model.data.UserType;

public class StaticDataSet extends DataSet
{
	public StaticDataSet()
	{
		super();
		
		// General
		
//		addItem(new DataSetTable(EyeColor.CONTENT_URI));
		addItem(new DataSetTable(Gender.CONTENT_URI));
//		addItem(new DataSetTable(HairColor.CONTENT_URI));
//		addItem(new DataSetItem(Ethnicity.CONTENT_URI));
//		addItem(new DataSetItem(Religion.CONTENT_URI));
//		addItem(new DataSetTable(BloodType.CONTENT_URI));
		addItem(new DataSetTable(AgencyType.CONTENT_URI));
		addItem(new DataSetTable(AddressType.CONTENT_URI));
		addItem(new DataSetTable(Agency.CONTENT_URI));
		addItem(new DataSetTable(Icon.CONTENT_URI));
		addItem(new DataSetTable(SourceType.CONTENT_URI));
//		addItem(new DataSetItem(ISO3166.CONTENT_URI));
		addItem(new DataSetTable(INCITS38200x.CONTENT_URI));
		
		// IncidentType
		
		addItem(new DataSetTable(IncidentType.CONTENT_URI));
		
		// Asset related

		addItem(new DataSetTable(AssetType.CONTENT_URI));
		addItem(new DataSetTable(AssetStatus.CONTENT_URI));
		addItem(new DataSetTable(EquipmentType.CONTENT_URI));
		addItem(new DataSetTable(UserType.CONTENT_URI));
		addItem(new DataSetTable(UserRankType.CONTENT_URI));

		// Journal related
		
		addItem(new DataSetTable(JournalType.CONTENT_URI));
		addItem(new DataSetTable(JournalStatus.CONTENT_URI));

		// Tactic related
		
		addItem(new DataSetTable(TacticType.CONTENT_URI));
		addItem(new DataSetTable(TacticStatus.CONTENT_URI));
		addItem(new DataSetTable(TacticPriority.CONTENT_URI));
		
		// Triage related
		
		addItem(new DataSetTable(TriageStatus.CONTENT_URI));
		addItem(new DataSetTable(TriageColor.CONTENT_URI));
		
		// Collectives related
		
		addItem(new DataSetTable(CollectionType.CONTENT_URI));
		
		// Incident and OperationalPeriod
		
		addItem(new DataSetTable(OperationalPeriod.CONTENT_URI));
		addItem(new DataSetTable(Incident.CONTENT_URI));
		
		// Incident tables
		
		addItem(new IncidentDataSet());
	}
}
