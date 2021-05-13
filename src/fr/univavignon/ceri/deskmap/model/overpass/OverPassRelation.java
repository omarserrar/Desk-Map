package fr.univavignon.ceri.deskmap.model.overpass;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.*;

import javafx.scene.Group;
import javafx.scene.canvas.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import fr.univavignon.ceri.deskmap.controllers.Clickable;
import fr.univavignon.ceri.deskmap.model.*;
import fr.univavignon.ceri.deskmap.model.angers.*;
import fr.univavignon.ceri.deskmap.model.itineraire.*;
import fr.univavignon.ceri.deskmap.model.map.*;
import fr.univavignon.ceri.deskmap.model.overpass.*;

public class OverPassRelation implements Serializable, Clickable{
	public long id;
	public String name = "";
	public String natural = "";
	public String type = "";
	public String place = "";
	public ArrayList<OverPassNode> noeuds;
	public ArrayList<OverPassWay> chemins;
	public ArrayList<OverPassWay> outer;
	public ArrayList< ArrayList<OverPassWay>> rings = new ArrayList< ArrayList<OverPassWay>>();
	public OverPassNode[] bbox;
	public OverPassNode center;
	
	public OverPassRelation(String name, OverPassNode[] bbox, OverPassNode center, long id) {
		this.name = name;
		this.bbox = bbox;
		this.center = center;
		this.id = id;
	}
	public ArrayList<Polyline> drawBorder(Pane root, double scale ,OverPassNode center) {
		ArrayList<Polyline> polylines = new ArrayList<Polyline>();
		for(ArrayList<OverPassWay> ring: rings) {
			double[] x = {},y = {};
	    	Polyline pol = new Polyline();
	    	for(OverPassWay chemin: ring) {	
	    		ArrayList<Double> xy = chemin.getWayPointArrayList();
	    		pol.getPoints().addAll(xy);
	        }
	        if(natural.equals("water") || natural.equals("bay") || place.equals("sea")) {
	        	
	        	Color c = Color.LIGHTBLUE;
	        	pol.setFill(c);
	        	//pol.setStroke(Color.color(Math.random(), Math.random(), Math.random()));
	        	pol.setStroke(Color.rgb(56, 154, 186));
	        	pol.setStrokeWidth(2);
	        	polylines.add(pol);
	        }
		}
		System.out.println("Polylines "+polylines.size());
		return polylines;
	}
	public void getBBOX() {
		bbox = OverPassQuery.getBBOX(this.id);
//		center = new OverPassNode((bbox[0].lon+bbox[1].lon)/2, (bbox[0].lat+bbox[1].lat)/2);
		center = OverPassNode.nodeFromMeter((bbox[0].est+bbox[1].est)/2, (bbox[0].nord+bbox[1].nord)/2);
		fixBBOX();
	}
	public void fixBBOX() {
		double xDistance = bbox[0].est - bbox[1].est;
		double yDistance = bbox[0].nord - bbox[1].nord;
		System.out.println("Distances "+xDistance+" "+yDistance);
		if(xDistance>yDistance) {
			bbox[0].add(0, xDistance-yDistance);
		}
		else if(yDistance>xDistance) {
			bbox[0].add(yDistance-xDistance,0);
		}
		xDistance = bbox[0].est - bbox[1].est;
		yDistance = bbox[0].nord - bbox[1].nord;
		System.out.println("Nouvelle distance "+xDistance+" "+yDistance);
	}
	public static boolean serializeWay(HashMap<Long,OverPassRelation> relations, String nom) {
		File fichier =  new File("cache/"+nom+"/relations.ser") ;

		ObjectOutputStream oos;
		try {
			fichier.getParentFile().mkdirs();
			fichier.createNewFile();
			 oos = new ObjectOutputStream(new FileOutputStream(fichier));
			 oos.writeObject(relations) ;
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		}
			
		return true;
	}
	public OverPassRelation(Element elements, HashMap<Long,OverPassWay> ways, HashMap<String, OverPassNode> nodes) {
		System.out.println("lzaejlezrj");
		//RECUPERER LES TAGS DE LA RELATION
		NodeList tags = elements.getElementsByTagName("tag");
		this.chemins = new ArrayList<OverPassWay>();
		this.outer = new ArrayList<OverPassWay>();
		this.id = Long.parseLong(elements.getAttribute("id"));
		for(int i =0; i<tags.getLength();i++ ) {
			Element membre =(Element) tags.item(i);
			String k = membre.getAttribute("k");
			if(k.equals("name")) {
				name = membre.getAttribute("v");
			}
			if(k.equals("natural")) {
				natural = membre.getAttribute("v");
				System.out.println("Added "+natural);
			}
			if(k.equals("type")) {
				type = membre.getAttribute("v");
			}
			if(k.equals("place")) {
				place = membre.getAttribute("v");
			}
		}
			Element bb = (Element) elements.getElementsByTagName("bounds").item(0);
			
			if(bb != null) {
				System.out.println("bbox");
				double minlat = Double.parseDouble(bb.getAttribute("minlat"));
				double maxlat = Double.parseDouble(bb.getAttribute("maxlat"));
				double minlon = Double.parseDouble(bb.getAttribute("minlon"));
				double maxlon = Double.parseDouble(bb.getAttribute("maxlon"));
				bbox = new OverPassNode[2];
				bbox[0] = new OverPassNode(minlon, minlat);
				bbox[1] = new OverPassNode(maxlon, maxlat);
			}
			NodeList membres = elements.getElementsByTagName("member");
			for(int i =0; i<membres.getLength();i++ ) {
				Element membre =(Element) membres.item(i);
				String type = membre.getAttribute("type");
				String role = membre.getAttribute("role");
				long ref = Long.parseLong(membre.getAttribute("ref"));
				//System.out.println("MEMBRE type: "+type+" ref "+ ref);
				// SI LE MEMBRE EST UN CHEMIN ( WAY )
				if(type.equals("way")) {
					if(membre.hasChildNodes()) {
							long id = Long.parseLong(membre.getAttribute("ref"));
							if(!ways.containsKey(id)) {
								OverPassWay way = new OverPassWay(membre);
								ways.put(id,way);
								if(role!=null && role.equals("outer")) {
									way.relations.add(this);
									outer.add(way);
								}
								else {
									way.relations.add(this);
									chemins.add(way);
								}
							}
							else if(ways.containsKey(ref)) {
								if(role!=null && role.equals("outer")) {
									OverPassWay outerWay = ways.get(ref);
									outerWay.relations.add(this);
									outer.add(outerWay);
									if(ref==431066928)System.out.println("hohoohohofhkfbenrbzdjkcbdfekl;rklef");
								}
								else {
									OverPassWay outerWay = ways.get(ref);
									outerWay.relations.add(this);
									chemins.add(outerWay);
								}
							}
						}
				}
				// SI LE MEMBRE EST UN POINT ( NODE )
				if(type.equals("node")) {
					if(nodes.containsKey(ref))
						noeuds.add(nodes.get(ref));
				}
			
			}
			getPolygons();
		}
	
	public void getPolygons() {
		if(!type.equals("multipolygon")) { 
			rings.add(outer);
			System.out.println("Polygon Added (No Multi)"+outer.size());
		}
		else {
			if(outer.size()==0) return;
			System.out.println(outer.get(0).id);
			HashMap<Long,Boolean> usedOuter = new HashMap<Long,Boolean>();
			for(int i = 0; i<outer.size();i++) {
				
				OverPassWay chemin = outer.get(i);
				System.out.println("way("+chemin.id+");");
				if(usedOuter.containsKey(chemin.id)) continue;
				ArrayList<OverPassWay> ring = new ArrayList<OverPassWay>();
				if(chemin.noeuds.get(0).equals(chemin.noeuds.get(chemin.noeuds.size()-1))) {
					ring.add(chemin);
					usedOuter.put(chemin.id, true);
					System.out.println("One Way Ring");
				}
				else {
					ring.add(chemin);
					if(chemin.id == 38904011) System.out.println(38904011);
					if(chemin.id == 667816738) System.out.println(667816738);
					usedOuter.put(chemin.id, true);
					for(int j = i+1; j<outer.size();j++) {
						OverPassWay ch = outer.get(j);
						if(usedOuter.containsKey(ch.id)) {
							System.out.println("way("+ch.id+"); //USED");
							continue;
						}
						//System.out.println("La");
						OverPassWay lastWay = ring.get(ring.size()-1);
					//	if(lastWay.id == 72457470) System.out.println("\nHAHAHAHA "+chemin.id+"\n");
						if(ch.noeuds.get(0).equals( lastWay.noeuds.get(lastWay.noeuds.size()-1)) ) {
							System.out.println("way("+ch.id+");");
							usedOuter.put(ch.id, true);
							if(ch.id == 667816738) System.out.println("ss "+667816738);
							if(ch.id == 38904011) System.out.println("ss "+38904011);
							ring.add(ch);
							if(ch.noeuds.get(ch.noeuds.size()-1).equals(ring.get(0).noeuds.get(0))) {
								System.out.println("Fin du polygon 0 "+ring.get(0).id+" "+ch.id);
								System.out.println(ch.noeuds.get(ch.noeuds.size()-1)+ "  "+ring.get(0).noeuds.get(0));
								
								break;
							}
						}
						else if(ch.noeuds.get(0).equals(lastWay.noeuds.get(0)) ) {
							System.out.println("way("+ch.id+");");
							ring.set(ring.size()-1, lastWay.invert());
						//	System.out.println("INVERT "+ch.id);
							usedOuter.put(ch.id, true);
							ring.add(ch);
							if(ch.noeuds.get(ch.noeuds.size()-1).equals(ring.get(0).noeuds.get(0))) {
								System.out.println("Fin du polygon 1 "+ring.get(0).id+" "+ch.id);
								break;
							}
						}
						
						else if(ch.noeuds.get(ch.noeuds.size()-1).equals(lastWay.noeuds.get(0)) ) {
							System.out.println("way("+ch.id+");");
							ring.set(ring.size()-1, lastWay.invert());
						//	System.out.println("INVERT "+ch.id);
							usedOuter.put(ch.id, true);
							ring.add(ch.invert());
							if(ch.noeuds.get(0).equals(ring.get(0).noeuds.get(0))) {
								System.out.println("Fin du polygon 2 "+ring.get(0).id+" "+ch.id);
								break;
							}
						}
						else if(ch.noeuds.get(ch.noeuds.size()-1).equals(lastWay.noeuds.get(lastWay.noeuds.size()-1)) ) {
							System.out.println("way("+ch.id+");");
							//	System.out.println("INVERT "+ch.id);
							usedOuter.put(ch.id, true);
							ring.add(ch.invert());
							if(ch.noeuds.get(0).equals(ring.get(0).noeuds.get(0))) {
								System.out.println("Fin du polygon 3 "+ring.get(0).id+" "+ch.id);
								break;
							}
						}
						else {
							if(outer.size()!=j+1) {
								ring.add(outer.get(j+1));
								usedOuter.put(outer.get(j+1).id, true);
								System.out.println("Incolable "+lastWay.id+ " "+ ch.id+" "+id);
							}
						}
						
					}
				}
				rings.add(ring);
				System.out.println("Polygon Added "+ring.size());
			}
		}
		System.out.println("Added "+rings.size()+" Polygons");
	}

	public String toString() {
		return getNom();
	}
	@Override
	public String getNom() {
		if(name.equals(""))
			return "relation: "+id;
		return name;
	}





	@Override
	public String getType() {
		return type;
	}
}
