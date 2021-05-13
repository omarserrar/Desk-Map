package fr.univavignon.ceri.deskmap.model.overpass;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import fr.univavignon.ceri.deskmap.model.itineraire.*;
import fr.univavignon.ceri.deskmap.model.map.*;

public class OverPassNode implements Serializable {
	public long ref;
	public String ref2;
	public ArrayList<Long> chemins;
	public double lon, lat, nord, est;
	public ArrayList<VertexNode> connectedNode = new ArrayList<VertexNode>();
	public static double pixelCenterX = 2999;
	public static double pixelCenterY = 2999;
	public static final double CIRC = 40075, LAT_TO_KM = 111;
	private OverPassNode() {
		ref=0;
		lon=0;
		lat=0;
	}
	
	public ArrayList<OverPassWay> getChemins() {
		ArrayList<OverPassWay> ways = new ArrayList<OverPassWay>();
		for(Long cheminId : chemins) {
			if(Map.getMapInstance().ways.containsKey(cheminId))
				ways.add(Map.getMapInstance().ways.get(cheminId));
		}
		return ways;
	}
	public OverPassNode(int ref, double lon, double lat) {
		super();
		this.ref = ref;
		this.chemins = new ArrayList<Long>();
		this.lon = lon;
		this.lat = lat;
		toMeter();
		generateRef();
	}
	public OverPassNode(double lon, double lat) {
		this(-1,lon,lat);
	}
	public OverPassNode(long ref) {
		this.ref = ref;
		Element nodeElem = OverPassQuery.node(ref);
		lat = Double.parseDouble(nodeElem.getAttribute("lat"));
		lon = Double.parseDouble(nodeElem.getAttribute("lon"));
		toMeter();generateRef();
		//System.out.println("\t\t lon: "+lon+" lat: "+lat);
	}
	public OverPassNode(Element noeudXml) {
		super();
		ref = Long.parseLong(noeudXml.getAttribute("id"));
		lon = Double.parseDouble(noeudXml.getAttribute("lon"));
		lat = Double.parseDouble(noeudXml.getAttribute("lat"));
		toMeter();generateRef();
		//System.out.println("\t\t lon: "+lon+" lat: "+lat);
	}
	/*public static OverPassNode xyToNode(double x, double y) {
		/*double centerX = Map.mapInstance.limitTop.lon;
        double centerY = Map.mapInstance.limitTop.lat;
        OverPassNode node = new OverPassNode(centerX, centerY);
        System.out.println("MeterE "+((-Map.mapDimension/2 + x)*Map.mapInstance.scale)+" MeterN "+(Map.mapDimension/2 - y)*Map.mapInstance.scale);
        System.out.println("XXMeterE "+node.est+" MeterN "+node.nord);
        node.add((-Map.mapDimension/2 + x)*Map.mapInstance.scale, (Map.mapDimension/2 - y)*Map.mapInstance.scale);
        return node;
	}*/
	public VertexNode getConnectionVertex(OverPassNode node) {
		for(VertexNode vertex: connectedNode) {
			if(vertex.getNode()==node) {
				return vertex;
			}
		}
		return null;
	}
	public double[] getScreenPos() {
		OverPassNode limitTop = Map.getMapInstance().limitTop;
		double scale = Map.getMapInstance().scale;
		double centerX = limitTop.est - Map.mapDimension*scale;
        double centerY = limitTop.nord;
      /*System.out.println("scale est "+(limitTop.est - limitBottom.est));
        System.out.println("scale nord "+(limitTop.nord - limitBottom.nord));*/
        double x = ((est - centerX)/scale);
		double y = (-1*(nord - centerY)/scale);
		double[] xy = {x,y};
		return xy;
	}
	public double getScreenDistance(OverPassNode node) {
		double[] xy = getScreenPos();
		double[] xy2 = node.getScreenPos();
		return Math.sqrt((xy[1] - xy2[1]) * (xy[1] - xy2[1]) + (xy[0] - xy2[0]) * (xy[0] - xy2[0]));
	}
//	public double[] getScreenPos(double scale, OverPassNode center) {
//		double centerX = center.est - pixelCenterX*scale;
//        double centerY = center.nord + pixelCenterY*scale;
//        double x = ((est - centerX)/scale);
//		double y = (-1*(nord - centerY)/scale);
//		double[] xy = {x,y};
//		return xy;
//	}
	public static String point(double lat, double lon) {
		
		String lonString = new Double(lon).toString();
		String latString = new Double(lat).toString();
		String ref2 = lonString + latString;
		return ref2;
	}
	public static String generateRef(double lat, double lon) {
			
			String lonString = new Double(lon).toString();
			String latString = new Double(lat).toString();
			String ref2 = lonString + latString;
			return ref2;
	}
	public void generateRef() {
		
		String lonString = new Double(lon).toString();
		String latString = new Double(lat).toString();
		ref2 = lonString + latString;
	}
	/*public void getIntersection(OverPassNode nodeInLine, OverPassNode limitTop, OverPassNode limitBottom) {
		
	}
	public Zone getZone(OverPassNode limitTop, OverPassLimit limitBottom) {
		if(limitTop.lat<lat && limitTop.lon<lon )return Zone.NE; // NE
		if(limitBottom.lat>lat && limitBottom.lon>lon )return Zone.SW; // SW
		if(limitTop.lon<lon && limitTop.lat<lat )return Zone.NW; // SW
		if(limitBottom.lat && limitTop.lon<lon )return Zone.SE; // SW
		if(limitTop.lat<lat ) return Zone.N; // Nord
		if(limitBottom.lat>lat) return Zone.S; // Sud
		if(limitBottom.lon>lon) return Zone.W; // West
		if(limitTop.lon<lon ) return Zone.E; // Est
		return 0;
	}*/
	public static boolean serializeWay(HashMap<String, OverPassNode> nodes, String nom) {
		File fichier =  new File("cache/"+nom+"/nodes.ser") ;

		ObjectOutputStream oos;
		try {
			fichier.getParentFile().mkdirs();
			fichier.createNewFile();
			 oos = new ObjectOutputStream(new FileOutputStream(fichier));
			 oos.writeObject(nodes) ;
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		}
			
		return true;
	}
	public void add(double meterE, double meterN) {
		nord += meterN;
		est += meterE;
		lat = nord/(LAT_TO_KM*1000);
		lon = (est*360)/(Math.cos(Math.toRadians(lat))*CIRC*1000);
	}
	public static OverPassNode nodeFromMeter(double est, double nord) {
		double lat = nord/(LAT_TO_KM*1000);
		double lon = est/((Math.cos(Math.toRadians(lat))*CIRC)*1000/360);
		return new OverPassNode(lon,lat);
	}
	public void toMeter() {
		nord = lat * LAT_TO_KM*1000;
		est = lon*(Math.cos(Math.toRadians(lat))*CIRC)*1000/360;
	}
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		return ((OverPassNode) obj).lat == lat && ((OverPassNode) obj).lon == lon;
	}
	public double distance(OverPassNode node) {
		return Math.sqrt((node.nord - nord) * (node.nord - nord) + (node.est - est) * (node.est - est));
	}
	public String toString() {
		return "lat= "+lat+" ,lon= "+lon;
		
	}
	
	public OverPassNode interSection4Point(OverPassNode ab, OverPassNode ab2, OverPassNode cd1, OverPassNode cd2) {
		double a1 = ab2.nord - ab.nord; 
	   	double b1 = ab.est - ab2.est; 
        double c1 = a1*(ab.est) + b1*(ab.nord); 
        double a2 = cd2.nord - cd1.nord; 
        double b2 = cd1.est - cd2.est; 
        double c2 = a2*(cd1.est)+ b2*(cd1.nord); 
       
        double determinant = a1*b2 - a2*b1; 
       
        if (determinant == 0) 
        { 
        	
            return null; 
        } 
        else
        { 
            double est = (b2*c1 - b1*c2)/determinant; 
            double nord = (a1*c2 - a2*c1)/determinant; 
            return OverPassNode.nodeFromMeter(est, nord);
        } 
	}
}
