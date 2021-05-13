package fr.univavignon.ceri.deskmap.controllers;

import fr.univavignon.ceri.deskmap.*;
import fr.univavignon.ceri.deskmap.controllers.*;
import fr.univavignon.ceri.deskmap.view.*;
import fr.univavignon.ceri.deskmap.model.*;
import fr.univavignon.ceri.deskmap.model.angers.*;
import fr.univavignon.ceri.deskmap.model.itineraire.*;
import fr.univavignon.ceri.deskmap.model.overpass.*;
import fr.univavignon.ceri.deskmap.model.map.*;

import java.util.ArrayList;
import java.util.Collection;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class AutoComplete<T> implements EventHandler<KeyEvent> {
	 
    private ComboBox<T> box;
    private ObservableList<T> donnees;
    private boolean movePos = false;
    private int pos;
    private boolean ville = false;
    public AutoComplete(ComboBox<T> box1, boolean ville) {
        this.ville = ville;
    	box = box1;
        donnees = box.getItems();
        box.setOnKeyReleased(this);
    }
 
    @Override
    public void handle(KeyEvent event) {
 
        if(event.getCode() == KeyCode.UP) {
        	
            pos = -1;
            move(box.getEditor().getText().length());
            return;
        } 
        else if(event.getCode() == KeyCode.DOWN) {
        	
            if(!box.isShowing()) {
                box.show();
            }
            pos = -1;
            move(box.getEditor().getText().length());
            return;
        } 
        else if(event.getCode() == KeyCode.BACK_SPACE) {
        	
            movePos = true;
            pos = box.getEditor().getCaretPosition();
        }
        if(ville) {
        	ArrayList<OverPassRelation> villes = OverPassQuery.searchCity(this.box.getEditor().getText().toLowerCase());
        	if(villes!=null)
        		donnees.setAll((Collection<? extends T>)villes);
        }
        ObservableList<T> liste = FXCollections.observableArrayList();
        for (int i=0; i<donnees.size(); i++) {
            if(donnees.get(i).toString().toLowerCase().startsWith(this.box.getEditor().getText().toLowerCase())){
            	liste.add(donnees.get(i));
            }
        }
        String boxTexte = box.getEditor().getText();
        box.setItems(liste);
        box.getEditor().setText(boxTexte);
        
        if(!movePos) {
            pos = -1;
        }
        
        move(boxTexte.length());
        if(!liste.isEmpty()) {
            box.show();
        }
    }
 
    private void move(int len) {
        if(pos == -1) {
            box.getEditor().positionCaret(len);
        } else {
            box.getEditor().positionCaret(pos);
        }
        movePos = false;
    }
 
}
