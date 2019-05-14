package com.futureconcepts.ax.sync.tablevalidators;

public class TableData {
	
	private String queryName;
	private long syncVersion;
	
	public TableData(){};
	
	public TableData(String tableName, long syncVersion){
		this.queryName = tableName;
		this.syncVersion = syncVersion;
	}

	public String getQueryName() {
		return queryName;
	}

	public void setQueryName(String tableName) {
		this.queryName = tableName;
	}

	public long getSyncVersion() {
		return syncVersion;
	}

	public void setSyncVersion(long syncVersion) {
		this.syncVersion = syncVersion;
	}
	

}
