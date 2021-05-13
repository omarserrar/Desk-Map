package fr.univavignon.ceri.deskmap.model.angers;

import java.util.ArrayList;

public class AngersBusParcour {
	private int numParcour;
	private String nomParcour;
	private ArrayList<AngersBusDesserte> dessertes;
	private AngersBusLigne ligne;
	
	public AngersBusParcour(int numParcour, String nomParcour) {
		super();
		this.numParcour = numParcour;
		this.nomParcour = nomParcour;
	}
	public AngersBusLigne getLigne() {
		return ligne;
	}
	public void setLigne(AngersBusLigne ligne) {
		this.ligne = ligne;
	}
	public int getNumParcour() {
		return numParcour;
	}
	public void setNumParcour(int numParcour) {
		this.numParcour = numParcour;
	}
	public String getNomParcour() {
		return nomParcour;
	}
	public void setNomParcour(String nomParcour) {
		this.nomParcour = nomParcour;
	}
	public ArrayList<AngersBusDesserte> getDessertes() {
		return dessertes;
	}
	public void setDessertes(ArrayList<AngersBusDesserte> dessertes) {
		this.dessertes = dessertes;
	}
	
}
