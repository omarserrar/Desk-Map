package fr.univavignon.ceri.deskmap.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import fr.univavignon.ceri.deskmap.*;
import fr.univavignon.ceri.deskmap.controllers.*;
import fr.univavignon.ceri.deskmap.view.*;
import fr.univavignon.ceri.deskmap.model.*;
import fr.univavignon.ceri.deskmap.model.angers.*;
import fr.univavignon.ceri.deskmap.model.itineraire.*;
import fr.univavignon.ceri.deskmap.model.overpass.*;
import fr.univavignon.ceri.deskmap.model.map.*;
import fr.univavignon.ceri.deskmap.Launcher;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;


public class LigneInfoController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Label afficherDansLaCarte;

    @FXML
    private VBox destList;

    @FXML
    private Label ligneNom;
    private AngersBusLigne ligne;
    private boolean affichee = false;
    @FXML
    private Label numLigne;


    @FXML
    void initialize() {
        assert afficherDansLaCarte != null : "fx:id=\"afficherDansLaCarte\" was not injected: check your FXML file 'LigneInfo.fxml'.";
        assert destList != null : "fx:id=\"destList\" was not injected: check your FXML file 'LigneInfo.fxml'.";
        assert ligneNom != null : "fx:id=\"ligneNom\" was not injected: check your FXML file 'LigneInfo.fxml'.";
        assert numLigne != null : "fx:id=\"numLigne\" was not injected: check your FXML file 'LigneInfo.fxml'.";


    }
    void initLigne(AngersBusLigne ligne, ArrayList<AngersBusDesserte> destinations) {
    	this.numLigne.setText(ligne.getNumLigne());
    	this.ligneNom.setText(ligne.getNomLigne());
    	this.ligne = ligne;
    	for(AngersBusDesserte destination: destinations) {
    		addDest(destination, true);
    	}
    	if(ligne.getWays().size()==0) {
    		afficherDansLaCarte.setVisible(false);
    	}
    	else if(Launcher.listeDesBus.containsKey(ligne) && Launcher.listeDesBus.get(ligne).getItem() !=null) {
			affichee = Launcher.listeDesBus.get(ligne).getItem().getCheck();
			if(affichee) {
		    	afficherDansLaCarte.setText("Cacher la ligne");
		    }
	    	else {
	    		afficherDansLaCarte.setText("Afficher la ligne dans la carte");
	    	}
		}
    }
    void addDest(AngersBusDesserte desserte, boolean aucunBus) {
    	FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/LigneDestination.fxml"));
        AnchorPane cellLayout;
        try {
			cellLayout = (AnchorPane) loader.load();
			DestinationInfoController destinationController = loader.getController();
			destinationController.init(desserte.getDest(), !aucunBus);
            	Platform.runLater(()->{
            		destList.getChildren().add(cellLayout);
            	});
	            
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    @FXML
    void afficherCacherLigne(MouseEvent event) {
    	if(!Launcher.listeDesBus.containsKey(ligne)) return;
    	affichee = Launcher.listeDesBus.get(ligne).getItem().getCheck();
    	if(!affichee&&ligne.getWays().size()>0) {
    	//	Launcher.ligneDeBus.getSelectionModel().select(ligne);
    		AngersData.afficherCacherLigne(Launcher.listeDesBus.get(ligne));
    		affichee = true;
    		afficherDansLaCarte.setText("Cacher la ligne");
    		
    	}
    	else if(affichee) {
    		affichee = false;
    		AngersData.afficherCacherLigne(Launcher.listeDesBus.get(ligne));
    		afficherDansLaCarte.setText("Afficher la ligne dans la carte");
    	}
    }
}
