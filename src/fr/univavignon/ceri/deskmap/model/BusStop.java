package fr.univavignon.ceri.deskmap.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import fr.univavignon.ceri.deskmap.Launcher;
import fr.univavignon.ceri.deskmap.model.*;
import fr.univavignon.ceri.deskmap.model.angers.*;
import fr.univavignon.ceri.deskmap.model.itineraire.*;
import fr.univavignon.ceri.deskmap.model.map.*;
import fr.univavignon.ceri.deskmap.model.overpass.*;
public class BusStop extends OverPassNode implements Serializable {
	public String name ="";
	public boolean focus=false;
	public static Image icon;
	public static Image iconFocus;
	static {
		try {
			icon = new Image(new FileInputStream(new File("assets/img/bus-arret.png")),900,900,true, true);
			iconFocus = new Image(new FileInputStream(new File("assets/img/bus-arret-focus.png")),900,900,true, true);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public BusStop(double lon, double lat) {
		super(lon, lat);
	}
	public BusStop(Element nodeXml) {
		super(nodeXml);
		NodeList tags = nodeXml.getElementsByTagName("tag");
		for(int j =0; j<tags.getLength();j++ ) {
			Element membre =(Element) tags.item(j);
			String k = membre.getAttribute("k");
			if(k.equals("name")) {
				name = membre.getAttribute("v");
			}
		}
	}
	public ImageView getIcon() {
		ImageView iv1 = new ImageView();
		double[] pos = this.getScreenPos();
        iv1.setImage(icon);
        iv1.setLayoutX(pos[0]);
        iv1.setLayoutY(pos[1]);
        iv1.resize(10, 10);
		return iv1;
	}
	public void addFocus() {
		if(Launcher.arrets.containsKey(this)) {
			focus = true;
			Launcher.arrets.get(this).setImage(iconFocus);
		}
	}
	public void removeFocus() {
		if(Launcher.arrets.containsKey(this)) {
			focus = false;
			Launcher.arrets.get(this).setImage(icon);
		}
	}
}
