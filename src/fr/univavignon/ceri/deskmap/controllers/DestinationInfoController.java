package fr.univavignon.ceri.deskmap.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Label;


public class DestinationInfoController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;


    @FXML
    private Label aucunBus;

    @FXML
    private Label destination;
    
    @FXML
    void initialize() {
    	

    }
    void init(String destination, boolean aucunBus) {
    	this.aucunBus.setVisible(aucunBus);
    	this.destination.setText(destination);
    }
}
