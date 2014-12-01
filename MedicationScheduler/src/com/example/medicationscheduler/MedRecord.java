package com.example.medicationscheduler;
import java.util.TreeSet;


public class MedRecord {
	private String Name;
	private String OtherName;
	private String NDC_CODE;
	private TreeSet<Interaction> interactables;

	public MedRecord(String Name, String NDC_CODE){
		this.setName(Name);
		this.setNDC_CODE(NDC_CODE);
		this.setInteractables(new TreeSet<Interaction>());
		
	}

	public String getName() {
		return Name; 
	}

	public void setName(String name) {
		Name = name;
	}

	public String getNDC_CODE() {
		return NDC_CODE;
	}

	public void setNDC_CODE(String nDC_CODE) {
		NDC_CODE = nDC_CODE;
	}

	public TreeSet<Interaction> getInteractables() {
		return interactables;
	}

	public void setInteractables(TreeSet<Interaction> interactables) {
		this.interactables = interactables;
	}

	public String getOtherName() {
		return OtherName;
	}

	public void setOtherName(String otherName) {
		OtherName = otherName;
	}

}
