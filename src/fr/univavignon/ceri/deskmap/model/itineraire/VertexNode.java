package fr.univavignon.ceri.deskmap.model.itineraire;

import java.io.Serializable;

import fr.univavignon.ceri.deskmap.model.map.*;
import fr.univavignon.ceri.deskmap.model.overpass.*;
import fr.univavignon.ceri.deskmap.model.itineraire.*;
public class VertexNode implements Comparable<VertexNode>,Serializable {
	
	private String nodeRef;
	private long wayId;
	private Graph graph;
	private OverPassWay way;
	private OverPassNode node;
	private Double distance;
	
	public VertexNode(OverPassNode node, Double distance, OverPassWay way, Graph graph) {
		if(graph!=null) this.graph = graph;
		if(node!=null)
			this.nodeRef = node.ref2;
		this.distance = distance;
		if(way!=null)
			this.wayId = way.id;
		if(graph==null)
			this.graph = Map.getMapInstance();
		this.graph = graph;
	}
	public VertexNode(OverPassNode node, Double distance, OverPassWay way) {
		super();
		if(node!=null)
			this.nodeRef = node.ref2;
		this.distance = distance;
		if(way!=null)
			this.wayId = way.id;
		this.graph = Map.getMapInstance();
	}
	public OverPassWay getWay() {
		if(way!=null) return way;
		return Map.getMapInstance().ways.get(wayId);
	}

	public void setWay(OverPassWay way) {
		this.wayId = way.id;
	}
	public OverPassNode getNode() {
		//System.out.println("Ref "+nodeRef+ " "+graph.roadNetword.containsKey(nodeRef)+" "+(graph instanceof AngersBusLigne));
		return graph.roadNetword.get(nodeRef);
	}

	public Double getDistance() {
		return distance;
	}

	public void setNode(OverPassNode node) {
		this.nodeRef = node.ref2;
	}

	public void setDistance(Double distance) {
		this.distance = distance;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((distance == null) ? 0 : distance.hashCode());
		result = prime * result + ((getNode() == null) ? 0 : getNode().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		return false;
	}

	@Override
	public String toString() {
		return "Vertex [id=distance=" + distance + "]";
	}

	@Override
	public int compareTo(VertexNode o) {
//		System.out.println(o.distance+" "+distance+ " "+o.node.ref+" "+node.ref);
		if (this.distance < o.distance)
			return -1;
		else if (this.distance > o.distance)
			return 1;
		else
			return 0;
	}
	
}