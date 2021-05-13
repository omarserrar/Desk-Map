package fr.univavignon.ceri.deskmap.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import org.json.*;

import fr.univavignon.ceri.deskmap.Launcher;
import fr.univavignon.ceri.deskmap.model.angers.AngersBusLigne;
import fr.univavignon.ceri.deskmap.model.angers.AngersData;
import fr.univavignon.ceri.deskmap.model.overpass.OverPassNode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Bus extends OverPassNode {
	private static Image icon;
	private static Image focusIcon;
	private AngersBusLigne ligne;
	private boolean focus = false;
	static{
		try {
			icon = new Image(new FileInputStream(new File("assets/img/bus.png")),900,900,true, true);
			focusIcon = new Image(new FileInputStream(new File("assets/img/bus-focus.png")),900,900,true, true);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	public Bus(double lon, double lat, String ligne) {
		super(lon, lat);
		this.ligne = AngersData.lignes.get(ligne);
		if(this.ligne!= null && Launcher.listeDesBus.get(this.ligne)!=null && Launcher.listeDesBus.get(this.ligne).getItem() != null &&  Launcher.listeDesBus.get(this.ligne).getItem().getCheck()) {
			focus = true;
		}
	}
	public ImageView getIcon() {
		ImageView iv1 = new ImageView();
		double[] pos = this.getScreenPos();
        iv1.setImage(Bus.icon);
        iv1.setFitWidth(10);
        iv1.setFitHeight(10);
        iv1.setLayoutX(pos[0]);
        iv1.setLayoutY(pos[1]);
        if(focus) {
        	iv1.setImage(focusIcon);
        }
		return iv1;
	}
	public static Image getFocusIcon() {
		return focusIcon;
	}
	public AngersBusLigne getLigne() {
		return ligne;
	}
	public boolean isFocus() {
		return focus;
	}
	public void addFocus() {
		if(Launcher.bus.containsKey(this)) {
			Launcher.bus.get(this).setImage(focusIcon);
		}
	}
	public void removeFocus() {
		if(Launcher.bus.containsKey(this)) {
			Launcher.bus.get(this).setImage(icon);
		}
	}

}
