package fr.univavignon.ceri.deskmap.controllers;

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
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;


public class DesserteInfoController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private HBox horaires;

    @FXML
    private Label ligneTitle;

    @FXML
    private Label heureTemplate;
    
    @FXML
    private Label parcour;
    private AngersBusDesserte desserte;

    @FXML
    void initialize() {
        assert horaires != null : "fx:id=\"horaires\" was not injected: check your FXML file 'DesserteInfo.fxml'.";
        assert ligneTitle != null : "fx:id=\"ligneTitle\" was not injected: check your FXML file 'DesserteInfo.fxml'.";
        assert parcour != null : "fx:id=\"parcour\" was not injected: check your FXML file 'DesserteInfo.fxml'.";


    }
    public void init(AngersBusDesserte desserte) {
    	ligneTitle.setText(desserte.getLigne().getNumLigne()+" "+desserte.getLigne().getNomLigne());
    	parcour.setText(desserte.getDest());
    	this.desserte = desserte;
    }
    public int updateHorraire() {
    	ArrayList<Horaire> horaires = desserte.getNextNHoraire(0);
    	int c = 0;
    	for(Horaire horaire : horaires) {
    		System.out.println(horaire);
    		addHorraire(horaire);
    		c++;
    	}
    	return c;
    }
    private void addHorraire(Horaire horaire) {
    	Label text = new Label(horaire.toString());
    	text.setFont(heureTemplate.getFont());
    	text.setPrefHeight(heureTemplate.getPrefHeight());
    	text.setPrefWidth(heureTemplate.getPrefWidth());
    	if(!horaire.isTheorique()) {
    		text.setTextFill(Color.web("#00ff29"));
    	}
    	horaires.getChildren().add(text);
    }
}

