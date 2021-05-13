package fr.univavignon.ceri.deskmap.model.itineraire;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import fr.univavignon.ceri.deskmap.model.overpass.*;
import fr.univavignon.ceri.deskmap.model.*;
import fr.univavignon.ceri.deskmap.model.angers.*;
import fr.univavignon.ceri.deskmap.model.itineraire.*;
import fr.univavignon.ceri.deskmap.model.map.*;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Graph implements Serializable{
	public enum RechercheType {DISTANCE,TEMP};
	public Graph() {
		roadNetword = new HashMap<String, OverPassNode>();
	}

	public  HashMap<String,OverPassNode> roadNetword;
	public ArrayList<BusItineraire> getBusItineraire(OverPassNode start, OverPassNode finish, boolean correspondance){
			ArrayList<BusItineraire> trajet = BusItineraire.rechercheItitneraireBus(start, finish,(correspondance)?1:0);
			return trajet;
	}
	public List<OverPassNode> getShortestPath(OverPassNode start, OverPassNode finish, MoyenDeTransport moyenTransport, boolean oneWayOnly, RechercheType rechercheType) {
		if(start==finish) {
			ArrayList<OverPassNode> nd = new ArrayList<OverPassNode>();
			nd.add(start);
			return nd;
		}
		boolean trouve = false;
	//	System.out.println(roadNetword.size());

		PriorityQueue<VertexNode> noeuds = new PriorityQueue<VertexNode>();
		final Map<OverPassNode, Double> dist = new HashMap<OverPassNode, Double>();
		final Map<OverPassNode, VertexNode> precedent = new HashMap<OverPassNode, VertexNode>();
		for(Map.Entry<String, OverPassNode> v : roadNetword.entrySet()) {
			OverPassNode vertex = v.getValue();
//			System.out.println("1--------");
			if (vertex == start) {
				dist.put(vertex, 0d);
//				System.out.println("2--------");
				noeuds.add(new VertexNode(vertex, 0d,null));
//				System.out.println("3--------");
			} else {
				dist.put(vertex, Double.MAX_VALUE);
//				System.out.println("4--------");
				noeuds.add(new VertexNode(vertex, Double.MAX_VALUE,null));
//				System.out.println("5--------");
			}
//			System.out.println("6--------");
			precedent.put(vertex, null);
//			System.out.println("7--------");
		}
		int t = 0;
	//System.out.println("Node size "+noeuds.size());
		while (!noeuds.isEmpty()) {
			t++;
		//	System.out.println(t);
			VertexNode smallest = noeuds.poll();
			if (smallest.getNode() == finish) {
				//System.out.println("trouve");
				final List<OverPassNode> path = new ArrayList<OverPassNode>();
				while (precedent.get(smallest.getNode()) != null) {
					path.add(smallest.getNode());
					smallest = precedent.get(smallest.getNode());
				}
				return (trouve)?path:null;
			}

			if (dist.get(smallest.getNode()) == Integer.MAX_VALUE) {
				break;
			}
			int tmp = noeuds.size();	
			for (VertexNode neighbor : smallest.getNode().connectedNode) {
				if(moyenTransport == MoyenDeTransport.VOITURE && !neighbor.getWay().carAuthorized) continue;
				if(moyenTransport == MoyenDeTransport.VELO && !neighbor.getWay().bikeFriendly) continue;
				if(moyenTransport == MoyenDeTransport.PIED && !neighbor.getWay().pedestrianFriendly) continue;
				if(neighbor.getDistance()<0&&oneWayOnly&&moyenTransport==MoyenDeTransport.VOITURE) continue;
				if(neighbor.getNode()==finish) {
					trouve = true;
					//System.out.println("Trouve");
				}
				double distance = Math.abs(neighbor.getDistance());
				
				if(rechercheType == RechercheType.TEMP) {
					int vitesse = 0;
					 if(moyenTransport == MoyenDeTransport.VOITURE) vitesse = neighbor.getWay().maxSpeed;
					 else if(moyenTransport == MoyenDeTransport.VELO) vitesse = (20<neighbor.getWay().maxSpeed)? 20:neighbor.getWay().maxSpeed;
					 else if(moyenTransport == MoyenDeTransport.PIED) vitesse = 5;
					 distance = distance/vitesse;
					// System.out.println(distance);
				}
				
				Double alt = dist.get(smallest.getNode()) + distance;
				if (alt < dist.get(neighbor.getNode())) {
					dist.put(neighbor.getNode(), alt);
					precedent.put(neighbor.getNode(), smallest);
					
					forloop:
					for(VertexNode n : noeuds) {
						if (n.getNode() == neighbor.getNode()) {
							noeuds.remove(n);
							n.setDistance(alt);
							noeuds.add(n);
							break forloop;
						}
					}
				}
			}
		//	System.out.println(tmp+" > "+noeuds.size());
		}
		if(trouve)
			return new ArrayList<OverPassNode>(dist.keySet());
		return null;
	}
	
}