package fr.univavignon.ceri.deskmap.controllers;

import fr.univavignon.ceri.deskmap.*;
import fr.univavignon.ceri.deskmap.controllers.*;
import fr.univavignon.ceri.deskmap.view.*;
import fr.univavignon.ceri.deskmap.model.*;
import fr.univavignon.ceri.deskmap.model.angers.*;
import fr.univavignon.ceri.deskmap.model.itineraire.*;
import fr.univavignon.ceri.deskmap.model.overpass.*;
import fr.univavignon.ceri.deskmap.model.map.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

public class ElementMenu extends ContextMenu {
	private Clickable element;
	public ElementMenu(Clickable element, double d, double e) {
		super();
		
		this.element = element;
		OverPassNode nd = ((OverPassWay)element).getClosestNode(d, e);
		 MenuItem item1 = new MenuItem("Nouveau point de départ");
	        item1.setOnAction(new EventHandler<ActionEvent>() {
	 
	            @Override
	            public void handle(ActionEvent event) {
	            	System.out.println(((OverPassWay)element).toString());
	            	Launcher.numVoieDText.setText(((OverPassWay)element).toString());
	            	Launcher.departWay = ((OverPassWay)element);
	            	Launcher.nomVoieD.getSelectionModel().select(((OverPassWay)element));
	            	Launcher.departNode = nd;
	            }
	        });
	        MenuItem item2 = new MenuItem("Nouveau point d'arrivée");
	        item2.setOnAction(new EventHandler<ActionEvent>() {
	 
	            @Override
	            public void handle(ActionEvent event) {
	            	System.out.println(((OverPassWay)element).toString());
	            	Launcher.numVoieAText.setText(((OverPassWay)element).toString());
	            	Launcher.arriveeWay = ((OverPassWay)element);
	            	Launcher.nomVoieA.getSelectionModel().select(((OverPassWay)element));
	            	Launcher.arriveeNode = nd;
	            }
	        });
	        
	        item1.setDisable(Launcher.rechercheActive);
	        item2.setDisable(Launcher.rechercheActive);
	        MenuItem item3 = new MenuItem("Proprieté");
	        item3.setOnAction(new EventHandler<ActionEvent>() {
	 
	            @Override
	            public void handle(ActionEvent event) {
	            	System.out.println(element.getNom());
	            	 new ElementPopUp(element).showAndWait();
	            }
	        });
	        this.getItems().addAll(item1, item2, item3);
	}
	

}
