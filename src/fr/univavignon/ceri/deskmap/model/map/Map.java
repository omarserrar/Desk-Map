package fr.univavignon.ceri.deskmap.model.map;
	
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Map.Entry;

import javax.naming.Context;
import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import fr.univavignon.ceri.deskmap.model.itineraire.*;
import fr.univavignon.ceri.deskmap.model.overpass.*;
import fr.univavignon.ceri.deskmap.Launcher;
import fr.univavignon.ceri.deskmap.controllers.ElementMenu;
import fr.univavignon.ceri.deskmap.model.*;
import fr.univavignon.ceri.deskmap.model.angers.*;
import javafx.concurrent.Task;

import org.w3c.dom.*;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.scene.*;
import javafx.scene.Node;
import javafx.scene.canvas.*;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/* TODO CORRECTION DES ROUTES 
 * TODO COLORATION DES RELATION
 * TODO	CORRECTION DES OCEAN
 * TODO	PROBLEME MER ALBORAN
 * TODO ENLEVER LE CANVAS 
 */
public class Map extends Graph implements Serializable {
	public OverPassRelation ville;
	public ArrayList<OverPassWay> highWays = new ArrayList<OverPassWay>(); 
	public ArrayList<Bus> bus = new ArrayList<Bus>();
	public static enum Deplacement { HAUT, BAS, GAUCHE, DROIT, ZOOM_IN, ZOOM_OUT, RESET };
	public double centerLat= 43.9492493, centerLon=4.8059012, scale = 4; // Avignon , 
//	public static double centerLat= 43.3327, centerLon=5.3577, scale = 0.0001; // Marseille ,, 
	//public static double centerLat= 43.2946, centerLon=5.3677, scale = 0.000009; , 
	//public static double centerLat=  43.4238, centerLon=4.5066, scale = 0.0007; 36.6258, -4.433
//	public static double centerLat= 35.4311, centerLon=-2.5792, scale = 0.005;
	public OverPassNode center;
	public OverPassNode limitTop;
	public OverPassNode limitBottom;
	public HashMap<String,OverPassNode> nodes;
	public ArrayList<OverPassWay> pietonBikeWay = new ArrayList<OverPassWay>();
	public ArrayList<OverPassWay> carWay = new ArrayList<OverPassWay>();
	public HashMap<Long,OverPassWay> ways;
	public List<BusStop> busStops;
	public List<OverPassNode> lastPath;
	//public  HashMap<Long,OverPassWay> roadNetword;
	public HashMap<Long,OverPassRelation> relations;
	public static double mapDimension = 3000;
	//private Canvas map;
	
	
	public ArrayList<VertexNode> vertex;
	public static Map mapInstance;
	
	private boolean mapLoaded = false;
	private double[] deltaDep = new double[2];
	
	public Map(OverPassRelation ville) {
		super();
		this.ville = ville;
		ville.getBBOX();
		Launcher.mapPane = new Pane();
		mapInstance = this;
		busStops = new ArrayList<BusStop>();
		this.ways = new HashMap<Long,OverPassWay>();
		this.nodes = new HashMap<String,OverPassNode>();
		this.relations = new HashMap<Long,OverPassRelation>();
		this.limitTop = ville.bbox[0];
		this.limitBottom = ville.bbox[1];
		this.center = ville.center;
		this.scale = (limitTop.est-limitBottom.est)/mapDimension;
	}

	public static Map getMapInstance() {
		return mapInstance;
	}
	public void busPath(OverPassWay depart, OverPassWay arrivee,OverPassNode departNode, OverPassNode arriveeNode, boolean corresp) {
		Launcher.searchThread = new Thread() {
			public void run() {
			ArrayList<BusItineraire> itins = getBusItineraire(departNode, arriveeNode, corresp);
			if(itins==null || itins.size()==0) {
				Launcher.stopSearch("Aucun résultats", "");
				return;
			}
			
			Launcher.busLines = new ArrayList<Polyline>();
			String itineraire = "";
			String xml = "";
			if(itins.size()>1) {
				itineraire += "Nombre de correspondance: "+(itins.size()-1)+"\n";
			}
			for(BusItineraire itin: itins) {
				itineraire += "Marchez jusqu'a l'arret "+itin.getArretDepart().name+" ("+itin.getWays().get(0).distance()+"m)\n"+
							 "Prenez le bus N "+itin.getLigne().getNumLigne()+" \n"+
							 "Descendez à l'arret "+itin.getArretArrivee().name+"\n";
				for(OverPassWay way : itin.getWays()) {
					xml+="<chemin bus='"+way.busLigne+"' name='"+way.name+"'>";
					Polyline pol = (Polyline)way.draw();
					int line = 0;
					for(OverPassNode nd: way.noeuds) {
						xml +="<noeud lon='"+nd.lon+"' lat='"+nd.lat+"'/>";
						if(line++%10==0) xml+="\n";
					}
					xml+="</chemin>\n";
					Launcher.busLines.add(pol);
					Platform.runLater(()->{
						Launcher.mapPane.getChildren().add(pol);
					});
					
				}
			}
			itineraire += "Marchez jusqu'a la destination ("+itins.get(itins.size()-1).getWays().get(1).distance()+"m)";
			
			xml +="<info>"+itineraire+"</info>";
			String dataXML = "<itineraire ville='"+ville.id+"' moyen_transport='BUS'>"+xml+"</itineraire>";
			Launcher.stopSearch(itineraire, dataXML);
		};
		};
        Launcher.searchThread.start();
	}
	public void path(OverPassWay depart, OverPassWay arrivee,OverPassNode departNode, OverPassNode arriveeNode, MoyenDeTransport moyenTransport, boolean sens, RechercheType searchMode) {
		Launcher.searchThread = new Thread() {

            // runnable for that thread
			 public void run() {
	        		OverPassNode dNode =  (departNode != null)? departNode:depart.noeuds.get(0);
	        		OverPassNode aNode =  (arriveeNode!=null)? arriveeNode:arrivee.noeuds.get(0);
	        		lastPath = new ArrayList<OverPassNode>();
	        		lastPath.add(dNode);
	        		List<OverPassNode> path = getShortestPath(dNode, aNode,
	        				moyenTransport,
	        				sens,
	        				searchMode);
	        		if(path==null) {
	        			Launcher.stopSearch("Aucun chemin",null);
	        			return;
	        		}
	        		else {
	        			lastPath.addAll(path);
	        		}
	        		String result = "";
	        		OverPassWay oldWay = null;
	        		OverPassNode oldNode = null;
	        		ArrayList<Double> nodes = new ArrayList<Double>();
	        		Launcher.points = new ArrayList<Circle>();
	        		double distance=0;
	        		double temp = 0;
	        		double vitesseMoy = 0;
	        		double wayDistance = 0;
	        		String waysXML = "";
	        		String nodesXML = "";
	        		
	        		for(int i=lastPath.size()-1;i>=1;i--) {
	        			OverPassNode node = lastPath.get(i);
	        			nodesXML += "<noeud id='"+node.ref2+"'/>";
	        			if(oldNode!=null) {
	        				VertexNode vertex = oldNode.getConnectionVertex(node);
	        				if(oldWay!=null&& (!vertex.getWay().name.equals(oldWay.name))) {
	        					
	        					result += oldWay.getNom()+" ( "+wayDistance+"m )\n";
	        					waysXML +="<chemin id='"+oldWay.id+
	        							"' nom='"+oldWay.getNom()+"' distance='"+wayDistance+
	        							"' distance_cumule='"+distance+"' />";
	        					wayDistance = 0;
	        				}
	        				double vitesse = 0;
	        				if(moyenTransport == MoyenDeTransport.VOITURE) vitesse = vertex.getWay().maxSpeed;
	        				 else if(moyenTransport == MoyenDeTransport.VELO) vitesse = (20<vertex.getWay().maxSpeed)? 20:vertex.getWay().maxSpeed;
	        				 else if(moyenTransport == MoyenDeTransport.PIED) vitesse = 5;
	        				vitesseMoy += vitesse;
	        				
	        				temp += Math.abs(vertex.getDistance()/(vitesse*1000));
	        				distance += Math.abs(vertex.getDistance());
	        				wayDistance+= Math.abs(vertex.getDistance());
	        				nodes.addAll(vertex.getWay().pointInRange(oldNode, node));
	        				oldWay = vertex.getWay();
	        			}
	        			if(oldWay == null) oldWay = depart;
	        			Circle circle = new Circle(3);
	        			double[] pos = node.getScreenPos();
	        			circle.setLayoutX(pos[0]);
	        			circle.setLayoutY(pos[1]);
	        			
	        			Platform.runLater(new Runnable() {

	    	            public void run() {
	    	            	Launcher.mapPane.getChildren().add(circle);
	            			Launcher.points.add(circle);
	    	                }
	    	            });
	        			oldNode = node;
	        			
	        		}
	        		result += oldWay.getNom()+" ( "+wayDistance+"m )\n";
	        		waysXML +="<chemin id='"+oldWay.id+
							"' nom='"+oldWay.getNom()+"' distance='"+wayDistance+
							"' distance_cumule='"+distance+"' />";
	        		result+="Distance Totale: "+distance+"m";
	        		int tmpHeure = (temp>=1)?(int)temp:0;
	        		int tmpMin = (temp*60>=1)?(int)(temp*60)-(tmpHeure*60):0;
	        		int tmpSec = ((temp*3600)>=1)?(int)((temp*3600)-(tmpMin*60)-(tmpHeure*3600)):0;
	        		String time = tmpHeure+"h "+tmpMin+"min "+tmpSec+"s";
	        		result+="\nTemp estimé: "+time;
	        		String vitesseMoyenne = String.format("%.2f", (vitesseMoy/lastPath.size()));
	        		result+="\nVitesse moyenne estimé: "+vitesseMoyenne+" km/h";
	        		String dataXML = "<itineraire ville='"+ville.id+"' distance_total='"+distance+"' vitesse_moyenne='"+vitesseMoyenne+"' temp_estime='"+time+"' moyen_transport='"+moyenTransport.toString()+"' sens='"+sens+"'>"+waysXML+"\n"+nodesXML+"</itineraire>";
	        		Launcher.pathPolyline = new Polyline();
	        		Launcher.pathPolyline.getPoints().addAll(nodes);
	        		Launcher.pathPolyline.setStrokeWidth(2);
	        		Launcher.pathPolyline.setStroke(Color.BLUE);
	        		
	        		Platform.runLater(new Runnable() {

	    	            public void run() {
	    	            	Launcher.mapPane.getChildren().add(Launcher.pathPolyline);
	    	                }
	    	            });
	        		Launcher.stopSearch(result, dataXML);
	                }
	        };
	        Launcher.searchThread.start();
			
		}
	public void removeFocus() {
		for(OverPassWay way: focusedWay) {
			Polyline line = Launcher.ways.get(way);
			line.setStroke(way.getDefaultColor());
		}
		focusedWay = new ArrayList<OverPassWay>();
	}
	private ArrayList<OverPassWay> focusedWay = new ArrayList<OverPassWay>(); //Les chemins affiche en fonction du moyen de transport F22 
	public void addFocus(MoyenDeTransport moyenDeTransport) {
		removeFocus();
		if(moyenDeTransport == MoyenDeTransport.VOITURE) {
			for(OverPassWay way: carWay) {
				Polyline line = Launcher.ways.get(way);
				line.setStroke(Color.CADETBLUE);
				focusedWay.add(way);
			}
		}
		else if(moyenDeTransport == MoyenDeTransport.PIED || moyenDeTransport == MoyenDeTransport.VELO) {
			for(OverPassWay way: pietonBikeWay) {
				Polyline line = Launcher.ways.get(way);
				line.setStroke(Color.CADETBLUE);
				focusedWay.add(way);
			}
		}
	}
	public void stopRecherche() {
		if(Launcher.pathPolyline!=null)
			Launcher.mapPane.getChildren().remove(Launcher.pathPolyline);
		if(Launcher.points!=null)
			Launcher.mapPane.getChildren().removeAll(Launcher.points);
	}
	public Pane getMapPane() {
		if(mapLoaded) {

			
			Launcher.mapPane = new Pane();
			Launcher.mapPane.setPrefSize(mapDimension, mapDimension);
			Platform.runLater(new Runnable() {	
				@Override
				public void run() {
					drawMap();
				}
			});
			return Launcher.mapPane;
		}
		try {
		

			Polyline pol = new Polyline(0,500,1000,500);
			pol.setStrokeWidth(5);
			Launcher.mapPane.setPrefSize(mapDimension, mapDimension);
			getMap();
			Platform.runLater(new Runnable() {	
				@Override
				public void run() {
				drawMap();
				}
			});
			return Launcher.mapPane;

		} catch(Exception e) {
			e.printStackTrace();
			return null;

		}
	}
	
	public void drawAllBusStop() {
		new Thread() {

            // runnable for that thread
            public void run() {
	            	ArrayList<ImageView> busStopsTmp = new ArrayList<ImageView>();
	            	for(BusStop stop: busStops) {
	            		
	            		ImageView busStopIcon = stop.getIcon();
	            		busStopIcon.setFitHeight(10);
	            		busStopIcon.setFitWidth(10);
	            		Launcher.arrets.put(stop, busStopIcon);
	            		Tooltip.install(busStopIcon, new Tooltip(stop.name));
	            		//busStopsTmp.add(busStopIcon);
	            		//System.out.println("added bus "+stop.getIcon().getLayoutX()+ " "+stop.getIcon().getLayoutY());
	            		Platform.runLater(new Runnable() {

	                        public void run() {
	                        	Launcher.mapPane.getChildren().addAll(busStopIcon);
	                        }
	                    });
	            		if(stop instanceof AngersBusArret) {
	            			busStopIcon.setOnMouseClicked(new EventHandler<MouseEvent>() {

								@Override
								public void handle(MouseEvent arg0) {
									if(arg0.getButton() == MouseButton.SECONDARY)
										new ArretBusInfo((AngersBusArret)stop).show();
									
								}
	            				
							});
	            		}
	            	}
                    /*Platform.runLater(new Runnable() {

                        public void run() {
                        	Launcher.mapPane.getChildren().addAll(busStopsTmp);
                        }
                    });*/
                }
        }.start();
		
	}
	public void getAllBusData() {
		if(ville.id != Launcher.ANGERS) return;
		
		new Thread() {
            public void run() {
            	AngersData.getAllBusInfo();
            	drawAllBusStop();
                while(true) {
                    try {
                        // imitating work
                        Thread.sleep(new Random().nextInt(10000));
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    bus = AngersData.getAllBus();
            		
                    Platform.runLater(new Runnable() {

                        public void run() {
                        	int i=0;
                        	for(ImageView image: Launcher.bus.values()) {
                        		Launcher.mapPane.getChildren().remove(image);
                        	}
                        	Launcher.bus = new HashMap<Bus, ImageView>();
                        	for(Bus aBus: bus) {
                        		ImageView image = aBus.getIcon();
                        		Launcher.bus.put(aBus, image);
                    			Launcher.mapPane.getChildren().add(image);
                    			//System.out.println("added live bus "+aBus.getIcon().getLayoutX()+ " "+aBus.getIcon().getLayoutY());
                    			i++;
                    		}
                        }
                    });
                }
            }
        }.start();
		
	}
	public void drawNatural(OverPassNode limitTop,OverPassNode limitBottom) {
		String query = "";
		query += OverPassQuery.createQuery("natural","scrub");
		System.out.println("Loading Natural...");
		query += OverPassQuery.createQuery("natural","grassland");
		query += OverPassQuery.createQuery("natural","wood");
		query += OverPassQuery.createQuery("natural","heath");
		query += OverPassQuery.createQuery("natural","bay");
		query += OverPassQuery.createQuery("natural","water");
		OverPassQuery.execQuery(query, 25, "geom");
	}
	public void drawLanduse(OverPassNode limitTop,OverPassNode limitBottom) {
		String query = "";
		query += OverPassQuery.createQuery("landuse","forest");
		System.out.println("Loading Landuse...");
		query += OverPassQuery.createQuery("landuse","grass");
		query += OverPassQuery.createQuery("landuse","reservoir");
		query += OverPassQuery.createQuery("landuse","basin");
		OverPassQuery.execQuery(query, 25, "geom");
	}
	public void drawRoad(OverPassNode limitTop,OverPassNode limitBottom) {
		String query = "";
		query += OverPassQuery.createQuery("highway", "residential");
		System.out.println("Loading Road...");
		query += OverPassQuery.createQuery("highway", "pedestrian");
		query += OverPassQuery.createQuery("highway", "living_street");
		query += OverPassQuery.createQuery("highway", "unclassified");
		query += OverPassQuery.createQuery("highway", "tertiary");
		query += OverPassQuery.createQuery("highway", "secondary");
		
		query += OverPassQuery.createQuery("highway", "trunk");
		query += OverPassQuery.createQuery("highway", "primary_link");
		query += OverPassQuery.createQuery("highway", "footway");
		query += OverPassQuery.createQuery("highway", "motorway");
		if(ville.id != Launcher.ANGERS)
			query += OverPassQuery.findBusStop();
		query += OverPassQuery.createQuery("highway", "primary");
		query += OverPassQuery.createQuery("railway", "tram");
		OverPassQuery.execQuery(query, 25, "geom");
	}
	public void getMap() {
		if(mapLoaded) return;
		drawRoad(this.limitTop,this.limitBottom);
		drawLanduse(this.limitTop, this.limitBottom);
		drawNatural(this.limitTop, this.limitBottom);
		
		mapLoaded = true;
	//	OverPassQuery.getPlace("sea",limitTop,limitBottom,nodes,ways,relations);
		serializeMap();
	}
	public void drawMap() {
		
		// Parcourt tous les chemins
		
			// Recuperer le Node (Polyline pour les chemins / Polygon pour les chemins fermé (parc, foret...)
		
		Platform.runLater(new Runnable() {

            public void run() {
            	for(Entry<Long, OverPassRelation> relation: relations.entrySet()) {
        			ArrayList<Polyline> waters = relation.getValue().drawBorder(Launcher.mapPane, scale, center);
        			if(waters!=null) {
        				Launcher.mapPane.getChildren().addAll(waters);
        				//System.out.println("Water added");
        			}
        		}
            	for(Entry<Long, OverPassWay> way: ways.entrySet()) {
	            	Node nd = way.getValue().draw();
	            	
	    			if(nd != null) {
	    				// Ajouter le node dans la Pane princpale pour l'afficher
	    				Launcher.mapPane.getChildren().add(nd);
	    				if(!way.getValue().highWay.equals("")) {
	    					Launcher.ways.put(way.getValue(), (Polyline)nd);
	    					// Ajouter un Tooltip avec le nom de du chemin ( Si highway n'est pas vide )
	    					Tooltip.install(nd, new Tooltip(way.getValue().getNom()));
	    					// Afficher un menu deroulant lors d'un clique droit sur le chemin ( Si highway n'est pas vide )
	    					nd.setOnContextMenuRequested(event -> {
	    						
	    						new ElementMenu(way.getValue(), event.getX(), event.getY()).show(nd, event.getSceneX(),event.getSceneY());
	    					});
	    				}
	    			}
	    			if(way.getValue().oneWay) {
	    				way.getValue().addArrow();
	    			}
    		}
            }
        });
		getAllBusData();
		if(ville.id != Launcher.ANGERS)
			drawAllBusStop();
	}
	public void loadBusItineraire(Element itineraire) {
		String text = itineraire.getElementsByTagName("info").item(0).getTextContent();
		NodeList cheminsXML = itineraire.getElementsByTagName("chemin");
		for(int i=0;i<cheminsXML.getLength();i++) {
			Element el =(Element) cheminsXML.item(i);
			NodeList nodes = el.getElementsByTagName("noeud");
			if(nodes.getLength()>0) {
				String name = el.getAttribute("name");
				boolean busLigne = el.getAttribute("bus").equals("true");
				OverPassWay way = new OverPassWay(name, busLigne);
				for(int j=0;j<nodes.getLength();j++) {
					Element node = (Element) nodes.item(j);
					double lon = Double.parseDouble(node.getAttribute("lon"));
					double lat = Double.parseDouble(node.getAttribute("lat"));
					OverPassNode nd = new OverPassNode(lon,lat);
					way.noeuds.add(nd);
				}
				Polyline pol = (Polyline)way.draw();
				Launcher.busLines.add(pol);
				Platform.runLater(()->{
					Launcher.mapPane.getChildren().add(pol);
				});
				
			}
		}
		Launcher.stopSearch(text, itineraire.toString());
		
	}
	public void loadItineraireFromXml(Document xml) {
		Launcher.searchThread = new Thread() {

            // runnable for that thread
			 public void run() {
	            	
	        		try {
	        		Element itineraire = (Element) xml.getElementsByTagName("itineraire").item(0);
	        		if(itineraire == null) throw new ItineraireParseErreur("Le fichier n'est pas un itineraire");
	        		String villeId = itineraire.getAttribute("ville");
	        		if(Integer.parseInt(villeId)!=ville.id) throw new ItineraireParseErreur("L'itineraire ne se trouve pas dans cette ville");
	        		if(itineraire.getAttribute("moyen_transport").equals("BUS")) {
	        			loadBusItineraire(itineraire);
	        			return;
	        		}
	        		double distanceTotal = Double.parseDouble(itineraire.getAttribute("distance_total"));
	        		String vitesseMoyenne = itineraire.getAttribute("vitesse_moyenne");
	        		String tempEstime = itineraire.getAttribute("temp_estime");
	        		String data = "";
	        		NodeList waysXML = itineraire.getElementsByTagName("chemin");
	        		String cheminData = "";
	        		for(int i =0; i<waysXML.getLength();i++) {
	        			Element wayXML = (Element) waysXML.item(i);
	        			
	        			cheminData += wayXML.getAttribute("nom")+" ( "+wayXML.getAttribute("distance")+"m )\n";
	        		}
	        		data += cheminData;
	        		data+="Distance Totale: "+distanceTotal+"m";
	        		data+="\nTemp estimé: "+tempEstime;
	        		data+="\nVitesse moyenne estimé: "+vitesseMoyenne+" km/h";
	        		NodeList nodesXML = itineraire.getElementsByTagName("noeud");
	        		ArrayList<Double> noeuds = new ArrayList<Double>();
	        		Launcher.points = new ArrayList<Circle>();
	        		for(int i =0; i<nodesXML.getLength();i++) {
	        			Element nodeXML = (Element) nodesXML.item(i);
	        			String nodeId = nodeXML.getAttribute("id");
	        			OverPassNode node = nodes.get(nodeId);
	        			if(node==null) throw new ItineraireParseErreur("Une erreur s'est produite: point inconnu.");
	        			double[] screenPos = node.getScreenPos();
	        			noeuds.add(screenPos[0]);
	        			noeuds.add(screenPos[1]);
	        			Circle circle = new Circle(3);
	        			circle.setLayoutX(screenPos[0]);
	        			circle.setLayoutY(screenPos[1]);
	        			
	        			Platform.runLater(new Runnable() {

	    	            public void run() {
	    	            	Launcher.mapPane.getChildren().add(circle);
	            			Launcher.points.add(circle);
	    	                }
	    	            });
	        		}
	        		Launcher.pathPolyline = new Polyline();
	        		Launcher.pathPolyline.getPoints().addAll(noeuds);
	        		Launcher.pathPolyline.setStrokeWidth(2);
	        		Launcher.pathPolyline.setStroke(Color.BLUE);
	        		Platform.runLater(new Runnable() {

	    	            public void run() {
	    	            	Launcher.mapPane.getChildren().add(Launcher.pathPolyline);
	    	                }
	    	            });
	        		Launcher.stopSearch(data, xml.getTextContent());
	        		}
	        		catch(ItineraireParseErreur e) {
	        			Launcher.textAreaEtat.setText(e.getMessage());
	        		}
	        		catch(NumberFormatException e) {
	        			Launcher.textAreaEtat.setText("Fichier XML endomagé.");
	        		}
	                }
	        		
	        };
	        Launcher.searchThread.start();
	}

	public void serializeMap() {
		new Thread() {

            // runnable for that thread
            public void run() {
            	if(ville==null) return;
        		File fichier =  new File("cache/"+ville.id+"/data.ser") ;
        		ObjectOutputStream oos;
        		try {
        			fichier.getParentFile().mkdirs();
        			fichier.createNewFile();
        			 oos = new ObjectOutputStream(new FileOutputStream(fichier));
        			 oos.writeObject(Map.getMapInstance());
        			 System.out.println("Serialized");
        		} catch (IOException e1) {
        			e1.printStackTrace();
        		}
        			
        	}
        }.start();
        return ;
	}

}
	
class ItineraireParseErreur extends Exception{
	public ItineraireParseErreur(String message) {
		super(message);
	}
}
