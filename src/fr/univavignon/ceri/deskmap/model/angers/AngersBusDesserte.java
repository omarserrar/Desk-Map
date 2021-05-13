package fr.univavignon.ceri.deskmap.model.angers;

import fr.univavignon.ceri.deskmap.model.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class AngersBusDesserte implements Serializable{
	private AngersBusArret arret;
	private AngersBusLigne ligne;
	private int numDesserte;
	private String dest;
	private String parcour;
	private ArrayList<Horaire> horaires;
	public AngersBusDesserte(AngersBusArret arret, int numDesserte, AngersBusLigne ligne, String dest, String parcour) {
		super();
		this.arret = arret;
		this.numDesserte = numDesserte;
		this.dest = dest;
		this.ligne = ligne;
		this.parcour = parcour;
		this.horaires = new ArrayList<Horaire>();
	}
	public ArrayList<Horaire> getHoraires() {
		return horaires;
	}
	public ArrayList<Horaire> getNextNHoraire(int n){
		boolean present = false;
		ArrayList<Horaire> nextNHoraire = new ArrayList<Horaire>();
		int count = 0;
		Date now = new Date();
		for(Horaire horaire: horaires) {
			if(!present && horaire.getDateArrivee().getTime()-now.getTime()>=0) {
				present = true;
			}
			if(present && (count<n || n == 0)) {
				nextNHoraire.add(horaire);
				count++;
			}
			else if(present && count>=n) {
				return nextNHoraire;
			}
		}
		return nextNHoraire;
	}
	public String getParcour() {
		return parcour;
	}

	public AngersBusLigne getLigne() {
		return ligne;
	}
	public String getDest() {
		return dest;
	}
	public AngersBusArret getArret() {
		return arret;
	}
	public void setArret(AngersBusArret arret) {
		this.arret = arret;
	}
	public int getNumDesserte() {
		return numDesserte;
	}
	public void setNumDesserte(int numDesserte) {
		this.numDesserte = numDesserte;
	}
	
	
}
