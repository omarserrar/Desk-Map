package fr.univavignon.ceri.deskmap.controllers;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;


import fr.univavignon.ceri.deskmap.*;
import fr.univavignon.ceri.deskmap.controllers.*;
import fr.univavignon.ceri.deskmap.view.*;
import fr.univavignon.ceri.deskmap.model.*;
import fr.univavignon.ceri.deskmap.model.angers.*;
import fr.univavignon.ceri.deskmap.model.itineraire.*;
import fr.univavignon.ceri.deskmap.model.overpass.*;
import fr.univavignon.ceri.deskmap.model.map.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;


public class ArretInfoController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Label titre;
    @FXML
    private VBox list;
    @FXML
    private VBox listLignes;


    private HashMap<AngersBusDesserte, DesserteInfoController> controllers;
    @FXML
    void initialize() {
        assert titre != null : "fx:id=\"titre\" was not injected: check your FXML file 'ArretInfo.fxml'.";

    }
   public void init(AngersBusArret arret) {
	   controllers = new HashMap<AngersBusDesserte, DesserteInfoController>();
	   
    	titre.setText(arret.name);
    	new Thread(()->{
    		addLignes(arret);
    		arret.downloadHorraires();
	    	for(AngersBusLigne ligne: arret.getDessertes().keySet()) {
	    		for(AngersBusDesserte desserte: arret.getDessertes().get(ligne)) {
		    		FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/DesserteInfo.fxml"));
		            AnchorPane cellLayout;
					try {
						cellLayout = (AnchorPane) loader.load();
						 DesserteInfoController cellLayoutController = loader.getController();
				            cellLayoutController.init(desserte);
				            if(cellLayoutController.updateHorraire()>0) {
				            	Platform.runLater(()->{
				            		list.getChildren().add(cellLayout);
				            	});
				            }
				            controllers.put(desserte, cellLayoutController);
				            
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	    		}
	    	}
    	}).start();
    }
   void addLignes(AngersBusArret arret) {
	   for(AngersBusLigne ligne: arret.getDessertes().keySet()) {
		   FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/LigneInfo.fxml"));
	       AnchorPane layout;
	       try {
				layout = (AnchorPane) loader.load();
				LigneInfoController ligneInfoController = loader.getController();
				ligneInfoController.initLigne(ligne, arret.getDessertes().get(ligne));
	           	Platform.runLater(()->{
	           		listLignes.getChildren().add(layout);
	           	});
		            
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		   }
   }

}