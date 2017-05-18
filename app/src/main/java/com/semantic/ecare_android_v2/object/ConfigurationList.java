package com.semantic.ecare_android_v2.object;

import java.util.ArrayList;



public class ConfigurationList{

	private static final long serialVersionUID = -7904050296144520007L;

	private String ipServeur;
	private int portHl7;
	private int portWrite;
	private ArrayList<Patient> patientListe;
	private String idPatientOrService; // tout depend du si c'est pour le domicile ou à l'hopital
	private int type; //domicile (2) ou hopital (1), non definie (0)
	private String blocInfo; //information comme numero de telephone pour lentretient
	private int active;
	private int initialise; // si cette tablette a deja ete initialisee
	private String nomOuEtablissement;
	private String prenomOuService;
	private int gender;
	private int canUpdate;
	
	public int getCanUpdate() {
		return canUpdate;
	}

	public void setCanUpdate(int canUpdate) {
		this.canUpdate = canUpdate;
	}

	public int getGender() 
	{
		return gender;
	}

	public void setGender(int gender) 
	{
		this.gender = gender;
	}

	public String getNomOuEtablissement() 
	{
		return nomOuEtablissement;
	}

	public void setNomOuEtablissement(String nomOuEtablissement) 
	{
		this.nomOuEtablissement = nomOuEtablissement;
	}

	public String getPrenomOuService() 
	{
		return prenomOuService;
	}

	public void setPrenomOuService(String prenomOuService)
	{
		this.prenomOuService = prenomOuService;
	}
	
	public ConfigurationList()
	{
	}
	
	public String getIdPatientOrService() 
	{
		return idPatientOrService;
	}

	public int getPortWrite() 
	{
		return portWrite;
	}

	public void setPortWrite(int portWrite) 
	{
		this.portWrite = portWrite;
	}

	public void setIdPatientOrService(String idPatientOrService) 
	{
		this.idPatientOrService = idPatientOrService;
	}

	public int getType()
	{
		return type;
	}

	public void setType(int type) 
	{
		this.type = type;
	}

	public int getActive() 
	{
		return active;
	}

	public void setActive(int active) 
	{
		this.active = active;
	}

	public String getIpServeur() 
	{
		return ipServeur;
	}
	
	public void setIpServeur(String ipServeur) 
	{
		this.ipServeur = ipServeur;
	}
	
	public int getPortHl7()
	{
		return portHl7;
	}
	
	public void setPortHl7(int portHl7) 
	{
		this.portHl7 = portHl7;
	}
	
	public ArrayList<Patient> getPatientListe() 
	{
		return patientListe;
	}
	
	public void setPatientListe(ArrayList<Patient> patientListe) 
	{
		this.patientListe = patientListe;
	}
	
	public static long getSerialversionuid() 
	{
		return serialVersionUID;
	}

	public int getInitialise() 
	{
		return initialise;
	}

	public void setInitialise(int initialise) 
	{
		this.initialise = initialise;
	}


	public String getBlocInfo() 
	{
		return blocInfo;
	}

	public void setBlocInfo(String blocInfo) 
	{
		this.blocInfo = blocInfo;
	}
}
