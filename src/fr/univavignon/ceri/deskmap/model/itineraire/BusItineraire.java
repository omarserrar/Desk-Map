package fr.univavignon.ceri.deskmap.model.itineraire;

import java.util.ArrayList;
import java.util.List;

import fr.univavignon.ceri.deskmap.model.*;
import fr.univavignon.ceri.deskmap.model.angers.*;
import fr.univavignon.ceri.deskmap.model.itineraire.*;
import fr.univavignon.ceri.deskmap.model.map.*;
import fr.univavignon.ceri.deskmap.model.overpass.*;

import fr.univavignon.ceri.deskmap.controllers.LigneInfoController;
import javafx.application.Platform;
import javafx.scene.Node;

public class BusItineraire {
	AngersBusArret arretDepart;
	AngersBusArret arretArrivee;
	AngersBusLigne ligne;
	ArrayList<OverPassWay> ways;
	String dest;
	public BusItineraire(AngersBusArret arretDepart, AngersBusArret arretArrivee, AngersBusLigne ligne) {
		super();
		this.arretDepart = arretDepart;
		this.arretArrivee = arretArrivee;
		this.ligne = ligne;
		ways = new ArrayList<OverPassWay>();
	}
	
	public ArrayList<OverPassWay> getWays() {
		return ways;
	}

	public static ArrayList<AngersBusArret> arretsLesPlusProche(OverPassNode node, int distanceMax){
		
		ArrayList<AngersBusArret> arrets = new ArrayList<AngersBusArret>();
		for(AngersBusArret arret: AngersData.arrets.values()) {
			if(arret.distance(node)<distanceMax) {
				arrets.add(arret);
			}
		}
		return arrets;
	}
	private static ArrayList<AngersBusLigne> ligneVisite = null;
	private static ArrayList<AngersBusLigne> ligneVisiteCorr = null;
	private static ArrayList<AngersBusArret> arretsVisites = null;
	private static ArrayList<AngersBusArret> arretsVisitesCorr = null;
	public static ArrayList<BusItineraire> rechercheItitneraireBus(OverPassNode node1, OverPassNode node2, int correspondance)
	{
		ligneVisite = new ArrayList<AngersBusLigne>();
		arretsVisites = new ArrayList<AngersBusArret>();
		arretsVisitesCorr = new ArrayList<AngersBusArret>();
		ligneVisiteCorr = new ArrayList<AngersBusLigne>();
		ArrayList<BusItineraire> resultats = trajetEntre2Point( node1,  node2,  correspondance);
		System.out.println("Taille "+resultats.size());
		ligneVisite = null;
		arretsVisites = null;
		ligneVisiteCorr=null;
		arretsVisitesCorr = null;
		return resultats;
	}
	public static ArrayList<BusItineraire> trajetEntre2Point(OverPassNode node1, OverPassNode node2, int correspondance){
		ArrayList<AngersBusArret> arretProche1 = arretsLesPlusProche(node1, 300);
		ArrayList<AngersBusArret> arretProche2 = arretsLesPlusProche(node2, 300);
		ArrayList<ArrayList<BusItineraire>> itinsTrouve = new ArrayList<ArrayList<BusItineraire>>();
		ArrayList<BusItineraire> arretTrouve = new ArrayList<BusItineraire>();
		double distanceDeMarcheMin = Double.POSITIVE_INFINITY;
		for(AngersBusArret arretDepart: arretProche1) {
			if(arretsVisites.contains(arretDepart)) continue;
			arretsVisites.add(arretDepart);
			for(AngersBusArret arretArrive: arretProche2) {
				if(arretDepart == arretArrive) continue;
				for(AngersBusLigne ligne: arretDepart.getDessertes().keySet()) {
					if(ligneVisite.contains(ligne)) continue;
					if(arretArrive.getDessertes().containsKey(ligne)) {
						System.out.println(arretDepart.name+" > "+arretArrive.name);
						ArrayList<BusItineraire> itin = new ArrayList<BusItineraire>();
						BusItineraire busItineraire = new BusItineraire(arretDepart, arretArrive, ligne);
						itin.add(busItineraire);
						OverPassWay wayToDepartTemp = new OverPassWay("",false);
						OverPassWay wayToArriveeTemp = new OverPassWay("",false);
						OverPassWay busWay = busItineraire.getLigne().trajetEntre2Arret(busItineraire.arretDepart, busItineraire.arretArrivee);
						if(busWay==null) {
							busWay = new OverPassWay(busItineraire.ligne.getNumLigne(),true);
							busWay.noeuds.add(busItineraire.getArretArrivee());
							busWay.noeuds.add(busItineraire.getArretDepart());
						}
						wayToDepartTemp.noeuds.add(node1);
						wayToDepartTemp.noeuds.add(busItineraire.getArretDepart().findClosestNode());
						wayToArriveeTemp.noeuds.add(busItineraire.getArretArrivee().findClosestNode());
						wayToArriveeTemp.noeuds.add(node2);
					
						busItineraire.getWays().add(wayToDepartTemp);
						busItineraire.getWays().add(wayToArriveeTemp);
						busItineraire.getWays().add(busWay);
						System.out.println(arretDepart.name+" > "+arretArrive.name+" "+busItineraire.getWays().size());
						itinsTrouve.add(itin);
						return itin;
					}
					else {
						ligneVisite.add(ligne);
					}
				}
			}
		}
		if(itinsTrouve.size()==0&&correspondance!=0) {
			
			for(AngersBusArret arretDepart: arretProche1) {
				if(arretsVisitesCorr.contains(arretDepart)) continue;
				arretsVisitesCorr.add(arretDepart);
				for(AngersBusLigne ligne: arretDepart.getDessertes().keySet()) {
					if(ligneVisiteCorr.contains(ligne)) continue;
					for(AngersBusDesserte des: ligne.getDessertes()) {
						if(arretsVisites.contains(des.getArret())) continue;
						ArrayList<BusItineraire> itin = trajetEntre2Point(des.getArret(), node2, correspondance-1);
						if(itin.size()>0) {
							BusItineraire busItineraire = new BusItineraire(arretDepart , des.getArret(), ligne);
							System.out.println("here "+arretDepart.name+" > "+des.getArret().name);
							OverPassWay wayToDepartTemp = new OverPassWay("",false);
							OverPassWay wayToArriveeTemp = new OverPassWay("",false);
							
							OverPassWay busWay = busItineraire.getLigne().trajetEntre2Arret(busItineraire.arretDepart, busItineraire.arretArrivee);
							if(busWay==null) {
								busWay = new OverPassWay(busItineraire.ligne.getNumLigne(),true);
								busWay.noeuds.add(busItineraire.getArretArrivee());
								busWay.noeuds.add(busItineraire.getArretDepart());
							}
							wayToDepartTemp.noeuds.add(node1);
							wayToDepartTemp.noeuds.add(arretDepart);

							wayToArriveeTemp.noeuds.add(des.getArret());
							wayToArriveeTemp.noeuds.add(itin.get(0).getArretDepart());

							busItineraire.getWays().add(wayToDepartTemp);
							busItineraire.getWays().add(wayToArriveeTemp);
							busItineraire.getWays().add(busWay);
							itin.add(0, busItineraire);
							System.out.println("itin "+itin.size());
							itinsTrouve.add(itin);
							return itin;
						}
						else {
							ligneVisiteCorr.add(ligne);
						}
					}
				}
			}
		}
		
		if(itinsTrouve.size()>0) {
			double distance = Double.MAX_VALUE;
			for(ArrayList<BusItineraire> busItineraires: itinsTrouve) {
				double d = 0;
				for(BusItineraire busItineraire : busItineraires) {
					for(OverPassWay way: busItineraire.ways) {
						d+= way.distance();
					}
				}
				if(d<distance) {
					distance = d;
					arretTrouve = busItineraires;
				}
				System.out.println("itinss "+arretTrouve.size()+" cor "+correspondance);
			}
		}
		return arretTrouve;
	}
	public AngersBusArret getArretDepart() {
		return arretDepart;
	}
	public AngersBusArret getArretArrivee() {
		return arretArrivee;
	}
	public AngersBusLigne getLigne() {
		return ligne;
	}
	public String getDest() {
		return dest;
	}
}
