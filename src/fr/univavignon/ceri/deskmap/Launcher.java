package fr.univavignon.ceri.deskmap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;


import fr.univavignon.ceri.deskmap.*;
import fr.univavignon.ceri.deskmap.controllers.*;
import fr.univavignon.ceri.deskmap.view.*;
import fr.univavignon.ceri.deskmap.model.*;
import fr.univavignon.ceri.deskmap.model.angers.*;
import fr.univavignon.ceri.deskmap.model.itineraire.*;
import fr.univavignon.ceri.deskmap.model.overpass.*;
import fr.univavignon.ceri.deskmap.model.map.*;
import fr.univavignon.ceri.deskmap.model.map.Map.Deplacement;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.ListCell;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * This class is used to launch the game.
 * 
 * @author FRESU Ilona
 * @author HARBOUCH Hamza
 * @author KHENIFRA Manel
 * @author SERRAR Omar
 */
public class Launcher extends Application implements  EventHandler<MouseEvent> 
{	
	public static OverPassWay departWay;
	public static OverPassWay arriveeWay;
	public static OverPassNode departNode;
	public static OverPassNode arriveeNode;
	public static OverPassRelation selectedRelation;
	public static Circle departPoint;
	public static Circle arriveePoint;
	public static ArrayList<Polyline> busLines = new ArrayList<Polyline>();
	public static boolean rechercheActive = false;
	public static TextField numVoieAText;
	public static TextField numVoieDText;
	public static ComboBox<OverPassWay> nomVoieA;
	public static Pane mapPane;
	public static Pane mapZone;
	public static ComboBox<OverPassWay> nomVoieD;
	private static Button depDroite;
	private static Button depGauche;
	public static HashMap<AngersBusLigne, ListCell<BusLigneCheck>> listeDesBus = new HashMap<AngersBusLigne, ListCell<BusLigneCheck>>();
	private static Button reset;
	private static Button depBas;
	private static String xmlData = "<empty/>";
	private static Button depHaut;
	private static Button zoomOut;
	private static Button zoomIn;
	public static ArrayList<Circle> points;
	public static Polyline pathPolyline;
	public static ArrayList<OverPassWay> busLigneWays = new ArrayList<OverPassWay>();
	public static HashMap<BusStop, ImageView> arrets = new HashMap<BusStop, ImageView>();
	public static HashMap<Bus, ImageView> bus = new HashMap<Bus, ImageView>();
	private static double[] deltaDep = new double[2];
	public static final int AVIGNON = 102478;
	public static final int ANGERS = 178351;
	public static HashMap<OverPassWay,Polyline> ways = new HashMap<OverPassWay, Polyline>();
	public static void addMap(OverPassRelation ville) {
		if(mapPane!=null) {
			Platform.runLater(new Runnable() {

                public void run() {
                	
                	mapZone.getChildren().remove(mapPane);
                }
            });
			
		}
		if(ville==null) ville = new OverPassRelation("",null,null,ANGERS);
			Map carte = MapCreator.createMap(ville);
			mapPane = carte.getMapPane();
			nomVoieD.getItems().setAll(Map.getMapInstance().highWays);
			nomVoieA.getItems().setAll(Map.getMapInstance().highWays);
			Platform.runLater(new Runnable() {

                public void run() {
                	
                	mapZone.getChildren().add(mapPane);
                }
            });
			
			
			mapPane.setBackground(new Background(new BackgroundFill(Color.gray(0.85), CornerRadii.EMPTY, Insets.EMPTY)));

			mapPane.setOnScroll(event-> {
				if(event.getDeltaY()<0)
					deplacer(Deplacement.ZOOM_OUT,1.1);
				else if(event.getDeltaY()>0)
					deplacer(Deplacement.ZOOM_IN,1.1);
			});
			mapPane.setOnMousePressed(new EventHandler<MouseEvent>() {
			  @Override public void handle(MouseEvent mouseEvent) {
			    // record a delta distance for the drag and drop operation.
				 deltaDep[0] = mapPane.getLayoutX() - mouseEvent.getSceneX();
				 deltaDep[1] = mapPane.getLayoutY() - mouseEvent.getSceneY();
				 mapPane.setCursor(Cursor.MOVE);
			  }
			});
			mapPane.setOnMouseReleased(new EventHandler<MouseEvent>() {
			  @Override public void handle(MouseEvent mouseEvent) {
				  mapPane.setCursor(Cursor.HAND);
			  }
			});
			mapPane.setOnMouseDragged(new EventHandler<MouseEvent>() {
			  @Override public void handle(MouseEvent mouseEvent) {
				  double newPosX = mouseEvent.getSceneX() + deltaDep[0];
				  double newPosY = (mouseEvent.getSceneY() + deltaDep[1]);
				  //System.out.println(newPosX*mapPane.getScaleX());
					  mapPane.setLayoutX(newPosX);
					  mapPane.setLayoutY(newPosY);
				 
				  
			  }
		});
		mapPane.setOnMouseEntered(new EventHandler<MouseEvent>() {
		  @Override public void handle(MouseEvent mouseEvent) {
			  mapPane.setCursor(Cursor.HAND);
		  }
		});		
	}
	public static void main(String[] args){
		try {
			launch(args);
		} catch (Exception e) {

		    // Answer:
		    e.getCause().printStackTrace();
		}
		
	}
	public static TextArea textAreaEtat;
	private static Button importer;
	public static Button afficherLigneBus;
	public static Button cacherLigneBus;
	public static ComboBox<BusLigneCheck> cb;


	
	
	@Override
	public void start(Stage primaryStage) {
		try {
			primaryStage.setTitle("DeskMap");
			BorderPane root = new BorderPane();
			Scene scene = new Scene(root, 1200, 900, Color.CADETBLUE);
			scene.getStylesheets();
			scene.getStylesheets().add(getClass().getResource("launcher.css").toExternalForm());
			primaryStage.setScene(scene);
			
			
			// Interface graphique
			VBox vBox1 = new VBox();
	        vBox1.setId("vBox1");
	        
	        
	        HBox ville = new HBox();
	        ville.setPadding(new Insets(10));
	        ville.setSpacing(10);
	        ComboBox<OverPassRelation> champTexteVille = new ComboBox<OverPassRelation>();
	        champTexteVille.setEditable(true);
	        champTexteVille.setPrefWidth(200); 
	        AutoComplete<OverPassRelation> villeAutoComplete = new AutoComplete<OverPassRelation>(champTexteVille, true);
	        Button rechercher = new Button ("OK");
	        ville.getChildren().addAll(champTexteVille, rechercher);

	       
	        HBox depart = new HBox();
	        depart.setPadding(new Insets(0, 10, 10, 10));
	        depart.setSpacing(10);
	        Label labelD = new Label("Depart");
	        labelD.setPadding(new Insets(10));
	        numVoieDText = new TextField();  
	        numVoieDText.setPrefWidth(100);
	        nomVoieD = new ComboBox<OverPassWay>();
	        nomVoieD.setPrefWidth(300); 
	        nomVoieD.setEditable(true);
	        new AutoComplete<OverPassWay>(nomVoieD,false);
	        depart.getChildren().addAll(numVoieDText, nomVoieD);
	        

	        HBox arrivee = new HBox();
	        arrivee.setPadding(new Insets(0, 10, 10, 10));
	        arrivee.setSpacing(10);
	        Label labelA = new Label("Arrivée");
	        labelA.setPadding(new Insets(10));
	        numVoieAText = new TextField();
	        numVoieAText.setPrefWidth(100);
	        nomVoieA = new ComboBox<OverPassWay>();
	        nomVoieA.setEditable(true);
	        nomVoieA.setPrefWidth(300); 
	        new AutoComplete<OverPassWay>(nomVoieA,false);
	        arrivee.getChildren().addAll(numVoieAText, nomVoieA);
	        
	        HBox mode = new HBox();
	        mode.setPadding(new Insets(10));
	        mode.setSpacing(10);
	        ComboBox<String> modeCalcul = new ComboBox<String>();
	        modeCalcul.setPromptText("Calcul");
	        modeCalcul.setPrefWidth(85);
	        modeCalcul.getItems().add("Temps");
	        modeCalcul.getItems().add("Distance");
	        modeCalcul.getSelectionModel().select(0);
	        ComboBox<String> modeTransport = new ComboBox<String>();
	        modeTransport.setPromptText("Transport");
	        modeTransport.setPrefWidth(115);
	        modeTransport.getItems().add("A pied");
	        modeTransport.getItems().add("Voiture");
	        modeTransport.getItems().add("Vélo");
	        modeTransport.getItems().add("Bus et Tram");
	        
	        modeTransport.getSelectionModel().select(0);
	        CheckBox corresp = new CheckBox("Correspondance");
	        CheckBox sens = new CheckBox("Sens");
	        mode.getChildren().addAll(modeCalcul, modeTransport, corresp, sens);
	        
	        HBox lancerR = new HBox();
	        lancerR.setPadding(new Insets(20, 20, 50, 60));
	        lancerR.setSpacing(20);     
	        recherche = new Button ("Rechercher");
	        recherche.setDisable(true);
	        recherche.setPrefWidth(150);
	        arret = new Button ("Arreter");
	        arret.setDisable(true);
	        arret.setPrefWidth(150);
	        lancerR.getChildren().addAll(recherche, arret);
	        
	        HBox bus = new HBox();
	        bus.setPadding(new Insets(10));
	        bus.setSpacing(10);
	        cb = new ComboBox<>();
	        cb.setCellFactory( c -> {
	        ListCell<BusLigneCheck> ligneDeBusElement = new ListCell<BusLigneCheck>(){
                @Override
                protected void updateItem(BusLigneCheck item, boolean empty) {
                    super.updateItem(item, empty);
                    if (!empty) {
                        final CheckBox cb = new CheckBox(item.toString());
                        cb.selectedProperty().bind(item.checkProperty());
                        setGraphic(cb);
                        listeDesBus.put(item.getLigne(), this);
                    }
                }
            };
            
            
            ligneDeBusElement.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
            	AngersData.afficherCacherLigne(ligneDeBusElement);
            });
            return ligneDeBusElement;
	        });
	        //ligneDeBus.setItems(FXCollections.observableArrayList(new String[] {"Voiture", "Velo", "Pietons"}));

	        afficherLigneBus = new Button ("Afficher");
	        afficherLigneBus.setDisable(false);
	        afficherLigneBus.setPrefWidth(100);
	        cacherLigneBus = new Button ("Cacher");
	        cacherLigneBus.setDisable(true);
	        cacherLigneBus.setPrefWidth(100);
	        bus.getChildren().addAll(cb);
	        
	        HBox itineraire = new HBox();
	        itineraire.setPadding(new Insets(0, 10, 10, 10));
	        Label labelI = new Label("Itinéraire");
	        labelI.setPadding(new Insets(10));
	        textAreaIti = new TextArea();
	        textAreaIti.setEditable(false);
	        textAreaIti.setMaxWidth(425);
	        textAreaIti.setMaxHeight(300);
	        itineraire.getChildren().add(textAreaIti);
	        
	        HBox boutonI = new HBox();
	        boutonI.setPadding(new Insets(10, 10, 10, 60));
	        boutonI.setSpacing(10);
	        exporter = new Button ("Exporter l'itinéraire");
	        exporter.setDisable(true);
	        importer = new Button ("Importer l'itinéraire");
	        importer.setDisable(false);
	        boutonI.getChildren().addAll(exporter, importer);
	        
	        HBox barre_etat = new HBox();
	        textAreaEtat = new TextArea();
	        textAreaEtat.setEditable(false);
	        textAreaEtat.setMinSize(1200, 50);
	        barre_etat.getChildren().add(textAreaEtat);
	        
	        
	        // Zone carte
	        Pane map = new Pane();
	        map.setMaxSize(825, 650);
	        vBox1.getChildren().addAll(ville, labelD, depart, labelA, arrivee, mode, lancerR, bus, labelI, itineraire, boutonI);
	        vBox1.setMaxSize(475, 650);
	        SplitPane split = new SplitPane(vBox1, map);
	        split.setPrefHeight(650);
	        
	        
	        // Barre de séparation
	        Platform.runLater(() -> {
	        	StackPane pane = (StackPane) split.lookup(".split-pane-divider");
		        
		        DoubleExpression scale = pane.widthProperty().multiply(0.50);
		        
		        Polygon leftArrow = new Polygon(0, 1, 1, 0, 1, 2);
		        leftArrow.setCursor(Cursor.DEFAULT);
		        leftArrow.scaleXProperty().bind(scale);
		        leftArrow.scaleYProperty().bind(scale);
		        leftArrow.translateYProperty().bind(scale.multiply(2));
		        leftArrow.translateXProperty().bind(scale);
		        
		        leftArrow.setOnMouseClicked(e -> {
		    		split.setDividerPosition(0, -1);
		        });
		        
		        Polygon rightArrow = new Polygon(1, 1, 0, 0, 0, 2);
		        rightArrow.setCursor(Cursor.DEFAULT);
		        rightArrow.scaleXProperty().bind(scale);
		        rightArrow.scaleYProperty().bind(scale);
		        rightArrow.translateYProperty().bind(scale.multiply(5));
		        rightArrow.translateXProperty().bind(scale);
		        
		        rightArrow.setOnMouseClicked(e -> {
		        	map.setMinSize(0, 845);
		    		split.setDividerPosition(0, 1);
		        });
		        
		        pane.getChildren().add(leftArrow);
		        pane.getChildren().add(rightArrow);
	        });
	        
	        //Choix affichage transport
	        VBox transport = new VBox();
	        ComboBox<String> choixTransport = new ComboBox<String>();
	        transport.getChildren().add(choixTransport);
	        choixTransport.setPromptText("Affichage des voies");
	        choixTransport.getItems().add("Voiture");
	        choixTransport.getItems().add("Pietons");
	        choixTransport.getItems().add("Velo");
	        choixTransport.getItems().add("Aucun");
	        transport.setLayoutX(scene.getWidth()-690);
	        transport.setLayoutY(scene.getHeight()-300);
	        choixTransport.setBackground(new Background(new BackgroundFill(Color.rgb(255, 255, 255, 0.5), null, null)));
	        // Bouton déplacement
	        VBox deplacement = new VBox();
	        HBox haut = new HBox(200);
	        HBox bas = new HBox(20);
	        HBox centre = new HBox(20);
	        haut.setAlignment(Pos.CENTER);
	        bas.setAlignment(Pos.CENTER);
	        
	        
	        depHaut = new Button("H");
	        depBas = new Button("B");
	        depGauche = new Button("<");
	        depDroite = new Button(">");
	        reset  = new Button("Reset");
	        
	        haut.getChildren().add(depHaut);
	        bas.getChildren().add(depBas);
	        centre.getChildren().addAll(depGauche, reset, depDroite);
	        deplacement.getChildren().addAll(haut,centre,bas);
	        deplacement.setLayoutX(scene.getWidth()-650);
	        deplacement.setLayoutY(10);
	        deplacement.setSpacing(5);
	        
	        
	        // Bouton Zoom
	        VBox zoom = new VBox(10);
	        zoomIn = new Button("+");
	        zoomOut = new Button("-");
	        zoomIn.getStyleClass().add("zoom-button");
	        zoomOut.getStyleClass().add("zoom-button");
	        zoom.getChildren().add(zoomIn);
	        zoom.getChildren().add(zoomOut);
	        zoom.setLayoutX(scene.getWidth()-570);
	        zoom.setLayoutY(scene.getHeight()-400);
	        	
			mapZone = new Pane();
			 
			map.getChildren().add(mapZone);
			map.getChildren().add(zoom);
			map.getChildren().add(deplacement);
			map.getChildren().add(transport);
			mapZone.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
			//mapPane.setLayoutX(-3000);
			//mapPane.setLayoutY(-3000);
			new Thread() {

	            // runnable for that thread
	            public void run() {
	            	addMap(null);
	            	Platform.runLater(new Runnable() {

                        public void run() {
                        	
                        	mapPane.getChildren().add(arriveePoint);
                			mapPane.getChildren().add(departPoint);
                        }
                    });
	            }
	        }.start();
			
			departPoint = new Circle(5);
			departPoint.setFill(Color.RED);
			arriveePoint = new Circle(5);
			arriveePoint.setFill(Color.LAWNGREEN);
			arriveePoint.setDisable(true);
			departPoint.setDisable(true);
			
			
			
			VBox resultat = new VBox();
			resultat.setPadding(new Insets(10));
			resultat.setSpacing(10);
			TextField resultatsInfo = new TextField("");
			resultatsInfo.setMinWidth(textAreaIti.getWidth());
			resultat.getChildren().add(resultatsInfo);
			vBox1.getChildren().add(resultat);
			modeTransport.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
					if(modeTransport.getSelectionModel().getSelectedItem()!=null && modeTransport.getSelectionModel().getSelectedItem().equals("Bus et Tram")) {
						selectedRelation = champTexteVille.getSelectionModel().getSelectedItem();
						modeCalcul.setDisable(true);
						sens.setDisable(true);
					}
					else {
						modeTransport.setDisable(false);
						sens.setDisable(false);
					}
					
				}
			});
			champTexteVille.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
					if(champTexteVille.getSelectionModel().getSelectedItem()!=null) {
						selectedRelation = champTexteVille.getSelectionModel().getSelectedItem();
					}
					else
						recherche.setDisable(true);
				}
			});
			nomVoieA.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
					if(nomVoieA.getSelectionModel().getSelectedItem()!=null) {
						arriveeWay = nomVoieA.getSelectionModel().getSelectedItem();
						if(nomVoieD.getSelectionModel().getSelectedItem()!=null)
							recherche.setDisable(false);
					}
					else
						recherche.setDisable(true);
				}
			});
			nomVoieD.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
					if(nomVoieD.getSelectionModel().getSelectedItem()!=null) {
						departWay = nomVoieD.getSelectionModel().getSelectedItem();
						if(nomVoieA.getSelectionModel().getSelectedItem()!=null)
							recherche.setDisable(false);
					}
					else
						recherche.setDisable(true);
				}
			});
			importer.setOnAction(event -> {
				importer.setDisable(true);
				arret.setDisable(false);
		        recherche.setDisable(true);
		        nomVoieA.setDisable(true);
		        nomVoieD.setDisable(true);
		        numVoieAText.setDisable(true);
		        numVoieDText.setDisable(true);
		        modeCalcul.setDisable(true);
		        modeTransport.setDisable(true);
		        sens.setDisable(true);
		        corresp.setDisable(true);
	            FileChooser fileChooser = new FileChooser();
	 
	            //Set extension filter for text files
	            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml");
	            fileChooser.getExtensionFilters().add(extFilter);
	 
	            //Show save file dialog
	            File file = fileChooser.showOpenDialog(primaryStage);
	 
	            if (file != null) {
	                try {
						Document xml = readXmlFile(file);
						Map.mapInstance.loadItineraireFromXml(xml);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (ParserConfigurationException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (SAXException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
	            }
	        });
			exporter.setOnAction(event -> {
	            FileChooser fileChooser = new FileChooser();
	 
	            //Set extension filter for text files
	            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml");
	            fileChooser.getExtensionFilters().add(extFilter);
	 
	            //Show save file dialog
	            File file = fileChooser.showSaveDialog(primaryStage);
	 
	            if (file != null) {
	                try {
						writeFile(file, Launcher.xmlData);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
	            }
	        });
	 
			choixTransport.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
			    
			    	String value = choixTransport.getSelectionModel().getSelectedItem();
			    	if(value.equals("Velo")||value.equals("Pietons")) {
			    		Map.getMapInstance().addFocus(MoyenDeTransport.PIED);
			    	}
			    	else if(value.equals("Aucun")) {
			    		Map.getMapInstance().removeFocus();
			    	}
			    	else if(value.equals("Voiture")) {
			    		Map.getMapInstance().addFocus(MoyenDeTransport.VOITURE);
			    	}
				}
			}
			);

			
			//Recherche Click
			recherche.setOnAction((ActionEvent e) -> {
				importer.setDisable(true);
				arret.setDisable(false);
		        recherche.setDisable(true);
		        nomVoieA.setDisable(true);
		        nomVoieD.setDisable(true);
		        numVoieAText.setDisable(true);
		        numVoieDText.setDisable(true);
		        modeCalcul.setDisable(true);
		        modeTransport.setDisable(true);
		        sens.setDisable(true);
		        corresp.setDisable(true);
				String searchMode = modeCalcul.getSelectionModel().getSelectedItem();
				String transportMoyen = modeTransport .getSelectionModel().getSelectedItem();
				boolean sensVoie = sens.isSelected();
				MoyenDeTransport moyenDeTransport=null;
				Graph.RechercheType rechercheType=null;
				if(transportMoyen.equals("A pied")) moyenDeTransport = MoyenDeTransport.PIED;
				if(transportMoyen.equals("Vélo")) moyenDeTransport = MoyenDeTransport.VELO;
				if(transportMoyen.equals("Voiture")) moyenDeTransport = MoyenDeTransport.VOITURE;
				if(transportMoyen.equals("Bus et Tram")) moyenDeTransport = MoyenDeTransport.BUS;
				if(searchMode.equals("Temps")) rechercheType = Graph.RechercheType.TEMP;
				if(searchMode.equals("Distance")) rechercheType = Graph.RechercheType.DISTANCE;
				
				isSearching = true;
				if(moyenDeTransport== MoyenDeTransport.BUS) {
					boolean correspondance = corresp.isSelected();
					Map.getMapInstance().busPath(departWay, arriveeWay, departNode, arriveeNode, correspondance);
				}
				else {
					Map.getMapInstance().path(departWay, arriveeWay,departNode, arriveeNode, moyenDeTransport, sensVoie,rechercheType);
				}
				
			    
			       
			}
			);
			//Annuler Click
			arret.setOnAction((ActionEvent e) -> {
					if(searchThread != null && searchThread.isAlive()) searchThread.stop();
					searchThread = null;
			        arret.setDisable(true);
			        isSearching = false;
			        exporter.setDisable(true);
			        importer.setDisable(false);
			        recherche.setDisable(false);
			        nomVoieA.setDisable(false);
			        nomVoieD.setDisable(false);
			        numVoieAText.setDisable(false);
			        numVoieDText.setDisable(false);
			        modeCalcul.setDisable(false);
			        importer.setDisable(false);
			        modeTransport.setDisable(false);
			        sens.setDisable(false);
			        corresp.setDisable(false);
			        rechercheActive = false;
			        textAreaIti.setText("");
			        for(Polyline pol:busLines) {
			        	mapPane.getChildren().remove(pol);
			        }
			        busLines = new ArrayList<Polyline>();
			        for(BusStop stop: arrets.keySet()) {
			        	if(stop.focus) {
			        		stop.removeFocus();
			        	}
			        }
			        Map.getMapInstance().stopRecherche();
			    }
			);
			rechercher.setOnAction((ActionEvent e) -> {    
				importer.setDisable(true);
		        if (selectedRelation == null) {	
		        	textAreaEtat.setText("Vous n'avez insérer aucune ville.");
		        }
		        else {
		        	new Thread() {

			            // runnable for that thread
			            public void run() {
			            	addMap(selectedRelation);
			            }
			        }.start();
		        	
		        }
			});
			
			
			root.setBottom(barre_etat);
	        root.setTop(split);
	       
	        primaryStage.widthProperty().addListener(e -> {
	        	
	            if(primaryStage.getWidth() > 1200) {
	            	vBox1.getChildren().clear();
	            	vBox1.setPadding(new Insets(50, 50, 20, 50));
	                vBox1.getChildren().addAll(ville, labelD, depart, labelA, arrivee, mode, lancerR, bus, labelI, itineraire, boutonI);
	                
	                barre_etat.getChildren().clear();
	                textAreaEtat.setPrefWidth(primaryStage.getWidth());
	                textAreaEtat.setPrefHeight(primaryStage.getHeight()-vBox1.getHeight()+100);	  
	                barre_etat.getChildren().add(textAreaEtat);
	            }
	            else {	        
	            	vBox1.getChildren().clear();	
	            	vBox1.setPadding(new Insets(20));
	            	vBox1.getChildren().addAll(ville, labelD, depart, labelA, arrivee, mode, lancerR, bus, labelI, itineraire, boutonI);          	            	
	            	
	            	barre_etat.getChildren().clear();
	            	textAreaEtat.setMinWidth(primaryStage.getWidth());
	                textAreaEtat.setMinHeight(primaryStage.getHeight()-vBox1.getHeight());	
	                barre_etat.getChildren().add(textAreaEtat);
	                
	            	resultatsInfo.setMaxWidth(textAreaIti.getWidth());
	            }
	        });

			depBas.addEventHandler(MouseEvent.MOUSE_CLICKED, this);
			depHaut.addEventHandler(MouseEvent.MOUSE_CLICKED, this);
			depGauche.addEventHandler(MouseEvent.MOUSE_CLICKED, this);
			depDroite.addEventHandler(MouseEvent.MOUSE_CLICKED, this);
			zoomIn.addEventHandler(MouseEvent.MOUSE_CLICKED, this);
			zoomOut.addEventHandler(MouseEvent.MOUSE_CLICKED, this);
	        primaryStage.sizeToScene();
			primaryStage.show();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	public static Thread searchThread = null;
	public static boolean isSearching = false;
	private static TextArea textAreaIti;
	private static Button recherche;
	private static Button arret;
	private static Button exporter;
	public static void stopSearch(String data, String xml) {
		searchThread = null;
		isSearching = false;
		if(xml==null) exporter.setDisable(true);
		else exporter.setDisable(false);
		xmlData = xml;
        textAreaIti.setText(data);
        rechercheActive = true;
	}
	public static void deplacer(Deplacement type, double mult) {
		double layoutX = mapPane.getLayoutX();
		double layoutY = mapPane.getLayoutY();
		if(type== Deplacement.BAS) {
			mapPane.setLayoutY(layoutY - mult);
			OverPassWay.pixelCenterY += mult;
		}
		if(type== Deplacement.HAUT) {
			mapPane.setLayoutY(layoutY + mult);
			OverPassWay.pixelCenterY -= mult;
		}
		if(type== Deplacement.GAUCHE) {
			OverPassWay.pixelCenterX -= mult;
			mapPane.setLayoutX(layoutX + mult); 
		}
		if(type== Deplacement.DROIT) {
			OverPassWay.pixelCenterX += mult;
			mapPane.setLayoutX(layoutX - mult); 
		}
		if(type== Deplacement.ZOOM_OUT) {
			mapPane.setScaleX(mapPane.getScaleX()/mult);
			mapPane.setScaleY(mapPane.getScaleY()/mult);
		}
		if(type== Deplacement.ZOOM_IN) {

			mapPane.setScaleX(mapPane.getScaleX()*mult);
			mapPane.setScaleY(mapPane.getScaleY()*mult);
		}
		//drawMap();
		
	}
	public int moveStep = 100;
	@Override
	public void handle(MouseEvent arg0) {
		Button button = (Button)arg0.getSource();
		if(button == this.depBas) {
			deplacer(Deplacement.BAS,this.moveStep);
		}
		if(button == this.depHaut) {
			//System.out.println("Haut");
			deplacer(Deplacement.HAUT,this.moveStep);
		}
		if(button == this.depDroite) {
			//System.out.println("Droite");
			deplacer(Deplacement.DROIT,this.moveStep);
		}
		if(button == this.depGauche) {
			//System.out.println("Gauche");
			deplacer(Deplacement.GAUCHE,this.moveStep);
		}
		if(button == this.zoomIn) {
			deplacer(Deplacement.ZOOM_IN,2);
		}
		if(button == this.zoomOut) {
			deplacer(Deplacement.ZOOM_OUT,2);
		}
		//drawMap();
	}
	public boolean writeFile(File file, String data) throws IOException {
		if(!file.exists()) file.createNewFile();
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		//System.out.println("writing "+data);
	    writer.write(data);
	    writer.close();
		return true;
	}
	public Document readXmlFile(File file) throws IOException, ParserConfigurationException, SAXException {
	
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(file);
		return doc;
	}
}