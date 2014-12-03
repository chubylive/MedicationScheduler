package com.example.medicationscheduler;

public class Interaction implements Comparable<Interaction> {
	private String drug;
	private String description;
	
	public Interaction(String drug, String description){
		this.setDrug(drug);
		this.setDescription(description);
	}

	public Interaction() {
		// TODO Auto-generated constructor stub
	}

	public String getDrug() {
		return drug;
	}

	public void setDrug(String drug) {
		this.drug = drug;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public int compareTo(Interaction another) {
		
		return drug.compareTo(another.drug);
	}
}
