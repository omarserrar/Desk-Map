package fr.univavignon.ceri.deskmap.model.angers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import fr.univavignon.ceri.deskmap.model.*;
import fr.univavignon.ceri.deskmap.model.angers.*;
import fr.univavignon.ceri.deskmap.model.itineraire.*;
import fr.univavignon.ceri.deskmap.model.map.*;
import fr.univavignon.ceri.deskmap.model.overpass.*;

public class AngersBusLigne extends Graph {
	private String nomLigne; 
	private String numLigne;
	private ArrayList<OverPassWay> ways = new ArrayList<OverPassWay>();
	private ArrayList<AngersBusDesserte> dessertes;
	public AngersBusLigne(String nomLigne, String numLigne) {
		super();
		this.nomLigne = nomLigne;
		this.numLigne = numLigne;
		this.dessertes = new ArrayList<AngersBusDesserte>();
	}
	public ArrayList<AngersBusDesserte> getDessertes() {
		return dessertes;
	}
	public String getNomLigne() {
		return nomLigne;
	}
	public String getNumLigne() {
		return numLigne;
	}
	public String toString() {
		return numLigne+" "+nomLigne;
	}
	public ArrayList<OverPassWay> getWays() {
		return ways;
	}
	public OverPassWay trajetEntre2Arret(AngersBusArret depart, AngersBusArret arrivee) {
		if(ways.size()==0) return null;
		OverPassNode departPoint= null, arriveePoint = null;
		OverPassWay departWay = null, arriveeWay = null;
		OverPassWay path = new OverPassWay(this.numLigne, true);
		double minDistDepart = Double.MAX_VALUE;
		double minDistArrivee = Double.MAX_VALUE;
		for(OverPassWay way: ways) {
			for(OverPassNode node: way.noeuds) {
				double distance = node.distance(depart);
				double distance2 = node.distance(arrivee);
				if(distance<minDistDepart) {
					minDistDepart= distance;
					departPoint = node;
					departWay = way;
				}
				if(distance2<minDistArrivee) {
					minDistArrivee = distance2;
					arriveePoint = node;
					arriveeWay = way;
				}
			}
		}
		//System.out.println("arr "+roadNetword.containsKey(arriveePoint.ref2));
		//System.out.println("dep "+roadNetword.containsKey(departPoint.ref2));
		//System.out.println(departPoint+" dep ar: "+arriveePoint);
		List<OverPassNode> pathNodes = getShortestPath(departPoint, arriveePoint);
		//System.out.println("cher "+chercher+" "+pathNodes.size());
		if(pathNodes==null || pathNodes.size()<2)return null;
		for(OverPassNode node: pathNodes) {
			path.noeuds.add(node);
		}
		return path;
		
	}
	private boolean chercher = false;
	public List<OverPassNode> getShortestPath(OverPassNode start, OverPassNode finish) {
		
		if(start==finish) {
			ArrayList<OverPassNode> nd = new ArrayList<OverPassNode>();
			nd.add(start);
			return nd;
		}
		boolean trouve = false;
		//System.out.println(roadNetword.size());

		PriorityQueue<VertexNode> noeuds = new PriorityQueue<VertexNode>();
		final Map<OverPassNode, Double> dist = new HashMap<OverPassNode, Double>();
		final Map<OverPassNode, VertexNode> precedent = new HashMap<OverPassNode, VertexNode>();
		for(Map.Entry<String, OverPassNode> v : roadNetword.entrySet()) {
			OverPassNode vertex = v.getValue();
//			System.out.println("1--------");
			if (vertex == start) {
				dist.put(vertex, 0d);
//				System.out.println("2--------");
				noeuds.add(new VertexNode(vertex, 0d,null,this));
//				System.out.println("3--------");
			} else {
				dist.put(vertex, Double.MAX_VALUE);
//				System.out.println("4--------");
				noeuds.add(new VertexNode(vertex, Double.MAX_VALUE,null,this));
//				System.out.println("5--------");
			}
//			System.out.println("6--------");
			precedent.put(vertex, null);
//			System.out.println("7--------");
		}
		int t = 0;
	//	System.out.println("Node size "+noeuds.size());
		while (!noeuds.isEmpty()) {
			
			t++;
	//		System.out.println(t);
			VertexNode smallest = noeuds.poll();
			if (dist.get(smallest.getNode()) > 10000000) {
				System.out.println("BIG DISTANCE");
				continue;
			}
			if (smallest.getNode() == finish ) {
				final List<OverPassNode> path = new ArrayList<OverPassNode>();
				while (precedent.get(smallest.getNode()) != null) {
					path.add(smallest.getNode());
					smallest = precedent.get(smallest.getNode());
				}
				return (trouve)?path:null;
			}

			
			int tmp = noeuds.size();	
			for (VertexNode neighbor : smallest.getNode().connectedNode) {
				if(neighbor.getNode()==finish) {
					trouve = true;
	//				System.out.println("Trouve");
				}
				double distance = Math.abs(neighbor.getDistance());
				
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
