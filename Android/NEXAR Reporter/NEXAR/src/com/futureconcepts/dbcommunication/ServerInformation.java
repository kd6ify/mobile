package com.futureconcepts.dbcommunication;

public class ServerInformation {
public String Host;
public String IP;
public String Port;
public String GetData;
public String SaveKeys;
public String SaveData;
public String Protocol;
public String SaveMedia;

	public  ServerInformation() {
		super();
		this.Host="172.16.21.187";
		this.Protocol="https://";
		this.IP=this.Protocol+this.Host;
		this.Port="443";
		this.GetData=this.IP+"/NEXAR/getData.php";
		this.SaveKeys=this.IP+"/NEXAR/saveKeys.php";
		this.SaveData=this.IP+"/NEXAR/saveData.php";
		this.SaveMedia=this.IP+"/NEXAR/saveMedia.php";

	}
}
