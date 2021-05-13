package fr.univavignon.ceri.deskmap.model.overpass;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javafx.scene.shape.*;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.event.EventHandler;
import javafx.scene.*;
import javafx.scene.Node;



import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import fr.univavignon.ceri.deskmap.Launcher;
import fr.univavignon.ceri.deskmap.controllers.Clickable;
import fr.univavignon.ceri.deskmap.model.*;
import fr.univavignon.ceri.deskmap.model.angers.*;
import fr.univavignon.ceri.deskmap.model.itineraire.*;
import fr.univavignon.ceri.deskmap.model.map.*;
import fr.univavignon.ceri.deskmap.model.overpass.*;
public class OverPassWay implements Serializable, Clickable{
	public long id;
	public ArrayList<OverPassRelation> relations;
	public ArrayList<OverPassNode> noeuds;
	public ArrayList<VertexNode> connectedWays;
	public ArrayList<OverPassNode> inter = new ArrayList<OverPassNode>();
	public String name = "";
	public String highWay = "";
	public String railWay = "";
	public String natural = "";
	public String leisure = "";
	public String landuse = "";
	public static double pixelCenterX = 3000;
	public static double pixelCenterY = 3000;
	public double distanceTotal = 0;
	private double distanceTmp;
	public static double distMax = 0;
	public boolean oneWay = false;
	public int maxSpeed = 0;
	public boolean bikeFriendly = false;
	public boolean pedestrianFriendly = false;
	public boolean carAuthorized = false;
	public boolean busLigne = false;
	public static Image arrow;
	static{
		try {
			arrow = new Image(new FileInputStream(new File("assets/img/arrow.png")),50,50,true, true);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public OverPassWay(String name, boolean busLigne) {
		this.name = name;
		noeuds= new ArrayList<OverPassNode>();
		connectedWays = new ArrayList<VertexNode>();
		this.busLigne = busLigne;
	}
	public OverPassWay(Element elem) {
		connectedWays = new ArrayList<VertexNode>();
		relations = new ArrayList<OverPassRelation>();
		this.id = Long.parseLong(elem.getAttribute("ref"));
		this.noeuds = new ArrayList<OverPassNode>();
		NodeList noeudsElm = elem.getElementsByTagName("nd");
		for(int i=0;i<noeudsElm.getLength();i++) {
			Element noeudXml = (Element) noeudsElm.item(i);
			if(noeudXml.hasAttribute("lon")) {
				double lon =  Double.parseDouble(noeudXml.getAttribute("lon"));
				double lat =  Double.parseDouble(noeudXml.getAttribute("lat"));
				noeuds.add(new OverPassNode(lon,lat));
			}
		}	
	}
	public OverPassWay(Element elem, HashMap<String, OverPassNode> nodes) {
		connectedWays = new ArrayList<VertexNode>();
		relations = new ArrayList<OverPassRelation>();
		this.id = Long.parseLong(elem.getAttribute("id"));
		this.noeuds = new ArrayList<OverPassNode>();
		int nodeCount = 0;
		NodeList tags = elem.getElementsByTagName("tag");
		for(int i =0; i<tags.getLength();i++ ) {
			Element membre =(Element) tags.item(i);
			String k = membre.getAttribute("k");
			if(k.equals("name")) {
				name = membre.getAttribute("v");
			}
			if(k.equals("highway")) {
				highWay = membre.getAttribute("v");
			}
			if(k.equals("landuse")) {
				landuse = membre.getAttribute("v");
			}
			if(k.equals("leisure")) {
				leisure = membre.getAttribute("v");
			}
			if(k.equals("railWay")) {
				railWay = membre.getAttribute("v");
			}
			if(k.equals("natural")) {
				natural = membre.getAttribute("v");
			}
			if(k.equals("oneway")) {
				oneWay = membre.getAttribute("v").equals("yes");
			}
			if(k.equals("maxspeed")) {
				try {
				maxSpeed = Integer.parseInt(membre.getAttribute("v"));
				}
				catch(NumberFormatException e) {
					maxSpeed=0;
				}
			}
		}
		if(maxSpeed==0) {
			if(highWay.equals("primary")||highWay.equals("trunk")) maxSpeed = 70;
			if(highWay.equals("secondary") || highWay.equals("tertiary")) maxSpeed = 50;
			else maxSpeed = 40;
		}
		//if(highWay.equals("primary")||highWay.equals("trunk")) maxSpeed = 200;
		if(!highWay.equals("motorway")&&maxSpeed<=50&&!highWay.equals("")) {
			bikeFriendly = true;
			pedestrianFriendly = true;
			Map.getMapInstance().pietonBikeWay.add(this);
		}
		if(!highWay.equals("footway")&&!highWay.equals("pedestrian")&&!highWay.equals("")) {
			carAuthorized = true;
			Map.getMapInstance().carWay.add(this);
		}
		NodeList noeudsElm = elem.getElementsByTagName("nd");
		OverPassNode oldNode = null;
		for(int i=0;i<noeudsElm.getLength();i++) {
			Element noeudXml = (Element) noeudsElm.item(i);
			if(noeudXml.hasAttribute("lon")) {
				double lon =  Double.parseDouble(noeudXml.getAttribute("lon"));
				double lat =  Double.parseDouble(noeudXml.getAttribute("lat"));
				addNode(0, lon, lat, Map.mapInstance);
			}
			else{
				long nodeId = Long.parseLong(noeudXml.getAttribute("ref"));
				addNode(nodeId, 0, 0, Map.mapInstance);
			}
		}	
		//System.out.println("Added way: "+id+" Nodes count: "+nodeCount);
	}

	public double distance() {
		double distance = 0;
		int i=1;
		for(;i<noeuds.size();i++) {
			distance += noeuds.get(i).distance(noeuds.get(i-1));
		}
	//	System.out.println("hop "+this.noeuds.contains(node));
		return distance;
	}
	
	public ArrayList<Double> pointInRange(OverPassNode node1, OverPassNode node2){
		if(!noeuds.contains(node1)||!noeuds.contains(node2)) return null;
		boolean debut = false;
		ArrayList<Double> nodes = new ArrayList<Double>();
		for(OverPassNode node : noeuds) {
			if(node==node1) debut = true;
			if(debut) {
				double[] pos = node.getScreenPos();
				nodes.add(pos[0]);
				nodes.add(pos[1]);
			}
			if(node==node2) break;
		}
		if(nodes.size()==0) {
			debut = false;
			for(OverPassNode node: noeuds) {
				if(node==node2) debut = true;
				if(debut) {
					double[] pos = node.getScreenPos();
					nodes.add(pos[0]);
					nodes.add(pos[1]);
				}
				if(node==node1) break;
			}	
		}
		return nodes;
	}
	public void addNode(long ref, double lon, double lat, Graph graph) {
		if(graph == null) graph = Map.getMapInstance();
		OverPassNode node=null;
		if(ref==-1) {
			node = new OverPassNode(-1,lon,lat);
			if(!graph.roadNetword.containsKey(node.ref2))
				noeuds.add(node);
			else
				noeuds.add(graph.roadNetword.get(node.ref2));
			node.chemins.add(this.id);
		}
		else if(ref==0) {
			Map map = Map.getMapInstance();
			String refString = OverPassNode.generateRef(lat, lon);
			if(map.nodes.containsKey(refString)) {
				node = map.nodes.get(refString);
				node.chemins.add(this.id);
				noeuds.add(node);
				if(!highWay.equals("")) {
					if(noeuds.size()>=2) {
						distanceTotal += noeuds.get(noeuds.size()-1).distance(noeuds.get(noeuds.size()-2));
					}
					//attachNode(node);
				}
			}
			else {
				node = new OverPassNode(-1,lon,lat);
				node.chemins.add(this.id);
				noeuds.add(node);
				map.nodes.put(refString, node);
			}
		}
		int size = noeuds.size();
		if(size>1) {
			if(!graph.roadNetword.containsKey(node.ref2))
				graph.roadNetword.put(node.ref2, node);
			else
				node = graph.roadNetword.get(node.ref2);
			OverPassNode oldNode = noeuds.get(size-2);
			double distance = (int)node.distance(oldNode);
			double distance2 = (oneWay)?-distance:distance;
			node.connectedNode.add(new VertexNode(oldNode, distance2, this, graph));
			oldNode.connectedNode.add(new VertexNode(node, distance, this, graph));
			
		}
		else {
			if(!graph.roadNetword.containsKey(node.ref2))
				graph.roadNetword.put(node.ref2, node);
		}
	}
	public OverPassWay(long id, ArrayList<OverPassNode> noeuds) {
		super();
		this.id = id;
		this.noeuds = noeuds;
	}

	public void connectWay(Node nd) {
		
	}
	public static boolean serializeWay(HashMap<Long,OverPassWay> ways, String nom) {
		File fichier =  new File("cache/"+nom+"/ways.ser") ;

		ObjectOutputStream oos;
		try {
			fichier.getParentFile().mkdirs();
			fichier.createNewFile();
			 oos = new ObjectOutputStream(new FileOutputStream(fichier));
			 oos.writeObject(ways) ;
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		}
			
		return true;
	}

	public static boolean track = true;
	public void addArrow() {
		if(!oneWay) return;
		OverPassNode oldNode = null;
		for(OverPassNode node: noeuds) {
			if(oldNode==null) {
				oldNode = node;
				continue;
			}
			System.out.println(node.getScreenDistance(oldNode)>10);
			if(node!=oldNode && node.getScreenDistance(oldNode)>10) {
				float angle = 360-(float) Math.toDegrees(Math.atan2(oldNode.nord - node.nord, oldNode.est - node.est));
				ImageView arrowImageView = new ImageView(arrow);
				arrowImageView.setFitWidth(10);
				arrowImageView.setFitHeight(10);
				arrowImageView.setLayoutX(oldNode.getScreenPos()[0]-5);
				arrowImageView.setLayoutY(oldNode.getScreenPos()[1]-5);
				arrowImageView.setRotate(angle);
				Tooltip.install(arrowImageView, new Tooltip(this.name +" "+angle));
				Launcher.mapPane.getChildren().add(arrowImageView);
			}
			oldNode = node;
		}
	}
	ArrayList<Double> getWayPointArrayList(){
		
		
        ArrayList<Double> xy = new ArrayList<Double>();
		int i = 0;
		double[] lastPoint = null;
		double[] lastPointOnZone=null;
		for(OverPassNode noeud: noeuds) {
			double xyPos[] = noeud.getScreenPos();
			double x = xyPos[0];
			double y = xyPos[1]; 
			xy.add(x);xy.add(y);
			
		}
		return xy;
	}
	public void unFocus() {
		Polyline polyline = Launcher.ways.get(this);
		if(highWay.equals("primary") || highWay.equals("trunk") || highWay.equals("primary_link")) {
			(polyline).setStroke(Color.YELLOW);
		}
		else if (highWay.equals("secondary")){
			(polyline).setStroke(Color.WHITE);
		}
		else if (highWay.equals("tertiary")){
			(polyline).setStroke(Color.WHITE);
		}
		else if (highWay.equals("pedestrian") || highWay.equals("residential") || highWay.equals("living_street") || highWay.equals("unclassified")){
			(polyline).setStroke(Color.WHITE);
		}
	}
	public void focus() {
		Polyline polyline = Launcher.ways.get(this);
		(polyline).setStroke(Color.DODGERBLUE);
	}
	public Node drawArea() {
		Polygon pol = new Polygon();
		
		ArrayList<Double> xy = getWayPointArrayList();
		pol.getPoints().addAll(xy);
		if(natural.equals("water") || landuse.equals("basin") || landuse.equals("reservoir"))
			pol.setFill(Color.LIGHTSKYBLUE);
		else
			pol.setFill(Color.LAWNGREEN);
		return (Node)pol;
	}

	/*
	public void drawArea(GraphicsContext gc, double scale, OverPassNode center) { CANVAS
		double[][] xy = getWayPointArray(scale, center);

		if(natural.equals("water") || landuse.equals("basin") || landuse.equals("reservoir"))
			gc.setFill(Color.LIGHTSKYBLUE);
		else
			gc.setFill(Color.LAWNGREEN);
		
		gc.fillPolygon(xy[0], xy[1], xy[1].length);
	}*/
	public OverPassWay invert() {
		ArrayList<OverPassNode> invNoeuds = new ArrayList<OverPassNode>(noeuds.size());
		for(int i=noeuds.size()-1; i>=0;i--) {
			invNoeuds.add(noeuds.get(i));
		}
		return new OverPassWay(id, invNoeuds);
	}
	public OverPassNode getClosestNode(double x, double y) {
		double distance = -1;
		OverPassNode nd = null;
		for(int i=0;i<this.noeuds.size();i++) {
			double[] noeudXY = this.noeuds.get(i).getScreenPos();
			double tmpDistance = Math.sqrt((noeudXY[1] - y) * (noeudXY[1] - y) + (noeudXY[0] - x) * (noeudXY[0] - x)); 
			if(tmpDistance < distance || distance == -1) {
				distance = tmpDistance;
				nd = this.noeuds.get(i);
			}
		}
		double[] noeudXY = nd.getScreenPos();
		
		return nd;
	}
	public Node draw() {
		if(relations != null &&relations.size()!=0) return null;
		if(!leisure.equals("") || !landuse.equals("") || !natural.equals("")) {
			
			return drawArea();
		}
		Polyline poly = new Polyline();
		if(busLigne) {
			poly.setStrokeWidth(2);
			poly.setStroke(Color.rgb(161, 52, 235));
		}
		else if(highWay.equals("primary") || highWay.equals("trunk")) {
			
			poly.setStrokeWidth(4);
			poly.setStroke(Color.YELLOW);
		}
		else if(highWay.equals("motorway")) {
			poly.setStrokeWidth(5);
			poly.setStroke(Color.INDIANRED);
		}
		else if( highWay.equals("primary_link")) {
			poly.setStrokeWidth(3);
			poly.setStroke(Color.YELLOW);
		}
		else if (highWay.equals("secondary")){
			poly.setStrokeWidth(3);
			poly.setStroke(Color.WHITE);
		}
		else if (highWay.equals("tertiary")){
			poly.setStrokeWidth(2);
			poly.setStroke(Color.WHITE);
		}
		else if (highWay.equals("pedestrian") || highWay.equals("residential") || highWay.equals("living_street") || highWay.equals("unclassified")){
			poly.setStrokeWidth(1);
			poly.setStroke(Color.WHITE);
		}
		else if( highWay.equals("footway")) {
			poly.setStrokeWidth(0.5);
			poly.setStroke(Color.CORAL);
		}
		else if(railWay.equals("tram")) {
			System.out.println("Draw Tram");
			poly.setStrokeWidth(1);
			poly.setStroke(Color.BLACK);
		}
		else {
			poly.setStrokeWidth(1);
			poly.setStroke( Color.color(Math.random(), Math.random(), Math.random()));
		}

        poly.getPoints().addAll(getWayPointArrayList());
		return poly;
	}
	public Color getDefaultColor() {
		if(highWay.equals("primary") || highWay.equals("trunk") || highWay.equals("primary_link")) {
			return Color.YELLOW;
		}
		else if (highWay.equals("secondary")){
			return Color.WHITE;
		}
		else if (highWay.equals("tertiary")){
			return Color.WHITE;
		}
		else if (highWay.equals("pedestrian") || highWay.equals("residential") || highWay.equals("living_street") || highWay.equals("unclassified")){
			return Color.WHITE;
		}
		else if( highWay.equals("footway")) {
			return Color.CORAL;
		}
		else if(highWay.equals("motorway")) {
			return Color.INDIANRED;
		}
		else {
			return Color.color(Math.random(), Math.random(), Math.random());
		}
	}
	/*public void draw(GraphicsContext gc, double scale, OverPassNode center) { // CANVAS
		if(relations.size()!=0) return ;
		drawn = true;
		if(!leisure.equals("") || !landuse.equals("") || !natural.equals("")) {
			
			drawArea(gc,scale,center);
		}
		if(highWay.equals("primary") || highWay.equals("trunk")) {
			gc.setStroke(Color.YELLOW);
			gc.setLineWidth(8);
		}
		else if (highWay.equals("secondary")){
			gc.setStroke(Color.WHITE);
			gc.setLineWidth(6);
		}
		else if (highWay.equals("tertiary")){
			gc.setStroke(Color.WHITE);
			gc.setLineWidth(4);
		}
		else if (highWay.equals("pedestrian") || highWay.equals("residential") || highWay.equals("living_street") || highWay.equals("unclassified")){
			gc.setStroke(Color.WHITE);
			gc.setLineWidth(3);
			
		}
		else {
			gc.setStroke(Color.color(Math.random(), Math.random(), Math.random()));
			gc.setLineWidth(1);
		}
		double[][] xy = getWayPointArray(scale, center);
        gc.strokePolyline(xy[0], xy[1], xy[1].length);

	}*/
	public String toString() {
		return getNom();
	}
	@Override
	public String getNom() {
		if(name.equals(""))
			return "Nom Inconnu";
		return name+" "+id;
	}
	@Override
	public String getType() {
		if(!highWay.equals("")) return highWay;
		if(!natural.equals("")) return natural;
		if(!leisure.equals("")) return leisure;
		if(!landuse.equals("")) return landuse;
		return "";
	}
	
}
