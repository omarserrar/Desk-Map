package fr.univavignon.ceri.deskmap.controllers;

import fr.univavignon.ceri.deskmap.*;
import fr.univavignon.ceri.deskmap.controllers.*;
import fr.univavignon.ceri.deskmap.view.*;
import fr.univavignon.ceri.deskmap.model.*;
import fr.univavignon.ceri.deskmap.model.angers.*;
import fr.univavignon.ceri.deskmap.model.itineraire.*;
import fr.univavignon.ceri.deskmap.model.overpass.*;
import fr.univavignon.ceri.deskmap.model.map.*;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.control.*;
import javafx.stage.*;

public class ElementPopUp extends Stage {
	private Clickable c=null;
	public ElementPopUp(Clickable c){
		this.c = c;
		String title = "";
	    if(c.getClass() == OverPassWay.class) title = "Chemin";
	    else title = "Relation";
		      
		      
		Text text1= new Text("Nom: "+c.getNom());
		Text text2= new Text("Type: "+c.getType());    
		
		VBox layout= new VBox(10);
		     
		      
		layout.getChildren().addAll(text2, text1);
		for(OverPassNode n: ((OverPassWay)c).inter) {
			Text t = new Text("NODE "+n.ref2+"--------------------");
			layout.getChildren().add(t);
			for(VertexNode vnd : n.connectedNode) {
				Text text = new Text("Distance "+vnd.getDistance()+" Inter "+vnd.getNode().ref2 + " Chemin "+vnd.getWay().getNom());
				layout.getChildren().add(text);
			}
		}
		layout.setAlignment(Pos.CENTER);
		
		this.initModality(Modality.APPLICATION_MODAL);
		this.setTitle(title);
		Scene scene1= new Scene(layout, 800, 800);
		      
		this.setScene(scene1);
	}

}
