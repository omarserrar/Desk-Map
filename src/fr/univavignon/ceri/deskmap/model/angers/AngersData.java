package fr.univavignon.ceri.deskmap.model.angers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polyline;
import fr.univavignon.ceri.deskmap.Launcher;
import fr.univavignon.ceri.deskmap.model.*;
import fr.univavignon.ceri.deskmap.model.angers.*;
import fr.univavignon.ceri.deskmap.model.itineraire.*;
import fr.univavignon.ceri.deskmap.model.map.*;
import fr.univavignon.ceri.deskmap.model.overpass.*;


public class AngersData {
	public static HashMap<Integer, AngersBusArret> arrets = new HashMap<Integer, AngersBusArret>();
	public static HashMap<String, AngersBusLigne> lignes = new HashMap<String, AngersBusLigne>();
	public static HashMap<Integer, AngersBusParcour> parcours = new HashMap<Integer, AngersBusParcour>();
	public static HashMap<Integer, AngersBusDesserte> dessertes = new HashMap<Integer, AngersBusDesserte>();
	public static ArrayList<Bus> getAllBus(){
		ArrayList<Bus> bus = new ArrayList<Bus>();
		JSONObject obj = AngersData.searchBus();
		JSONArray records = obj.getJSONArray("records");
		
		for(int i=0;i<records.length();i++) {
			JSONObject record = records.getJSONObject(i);
			JSONObject fields = record.getJSONObject("fields");
			JSONArray coordonnees = fields.getJSONArray("coordonnees");
			String nomLigne = fields.getString("nomligne");
			String mNemoLigne = fields.getString("mnemoligne");
			String mNemoArret = fields.getString("mnemoarret");
			String arret = fields.getString("nomarret");
			String dest = fields.getString("dest");
			int idligne = fields.getInt("idligne");
			int idParcours = fields.getInt("idparcours");
			double lon = coordonnees.getDouble(1);
			double lat = coordonnees.getDouble(0);
			bus.add(new Bus(lon,lat,mNemoLigne));
		}
		return bus;
		
	}
	public static void getAllBusInfo() {
		File arretData = new File("data/bus-arret.json");
		JSONArray data = getJsonFromFile(arretData);
		for(int i=0;i<data.length();i++) {
			JSONObject dessertJSON = data.getJSONObject(i);
			JSONObject fields = dessertJSON.getJSONObject("fields");
			String nomLigne = fields.getString("nomligne");
			String nomArret = fields.getString("nomarret");
			String numLigne = fields.getString("mnemoligne");
			String codeParcour = fields.getString("codeparcours");
			String mNemoArret = fields.getString("mnemoarret");
			int numArret = fields.getInt("numarret");
			int numDesserte = fields.getInt("iddesserte");
			String dest = fields.getString("dest");
			JSONArray coord = fields.getJSONArray("coordonnees");
			double lon = coord.getDouble(1);
			double lat = coord.getDouble(0);
			int idDesserte = fields.getInt("iddesserte");
			AngersBusLigne ligne = addLigneIfNotExist(numLigne, nomLigne);
			AngersBusArret arret = addArretIfNotExist(numArret, nomArret,mNemoArret, lon, lat);
			AngersBusDesserte desserte = new AngersBusDesserte(arret, numDesserte,ligne, dest, codeParcour);
			arret.addDesserte(ligne,desserte);
			ligne.getDessertes().add(desserte);
		}
		long time = System.currentTimeMillis();
		for(AngersBusArret arret: arrets.values()) {
			arret.findClosestNode();
		}
		System.out.println("Plus Proche Trouve "+(System.currentTimeMillis()-time)/1000);
		getBusLigneTrajet();
		updateListeLigne();
	//	System.out.println(arrets.size());
	}
	public static void getBusLigneTrajet() {
		String url = "https://data.angers.fr/api/records/1.0/search/?dataset=lignes-irigo&rows=10000&facet=ligne";
		JSONObject obj = getJsonFromURL(url);
		JSONArray records = obj.getJSONArray("records");
		for(int i=0;i<records.length();i++) {
			JSONObject record = records.getJSONObject(i);
			JSONObject fields = record.getJSONObject("fields");
			String numligne = fields.getString("ligne");
			if(numligne.charAt(0)!='L') continue;
			AngersBusLigne ligne = numToLigne(numligne);
			if(ligne == null)
				continue;
			int wayCount = 0;
			JSONArray coordinates = fields.getJSONObject("geo_shape").getJSONArray("coordinates");
			for(int j=0;j<coordinates.length();j++) {
				JSONArray waysJSON = coordinates.getJSONArray(j);
				OverPassWay way = new OverPassWay(j+"", true) ;
				wayCount++;
				for(int z=0;z<waysJSON.length();z++) {
					double lat = waysJSON.getJSONArray(z).getDouble(1);
					double lon = waysJSON.getJSONArray(z).getDouble(0);
					way.addNode(-1, lon, lat, ligne);
				}
				
				Polyline pol = (Polyline) way.draw();
				pol.setVisible(false);
				Platform.runLater(new Runnable() {

    	            public void run() {
    	            	Launcher.mapPane.getChildren().add(pol);
    	        
    	                }
    	            });
				
				Launcher.ways.put(way, pol);
				ligne.getWays().add(way);
			}
		}
		
	}
	public static JSONObject getHorraireArretJSON(AngersBusArret arret) {
		String url = "https://data.angers.fr/api/records/1.0/search/?dataset=bus-tram-circulation-passages&rows=1000&sort=-arriveetheorique&facet=mnemoligne&facet=nomligne&facet=dest&facet=mnemoarret&facet=nomarret&facet=numarret&refine.mnemoarret="+arret.getUniqueName();
		return getJsonFromURL(url);
	}
	public static AngersBusLigne numToLigne(String num) {
		if(num.charAt(0)=='L') {
			num = num.replace("L_", "");
			for(AngersBusLigne ligne: lignes.values()) {
				String numLigne = ligne.getNumLigne();
				if(numLigne.charAt(0)=='0') {
					numLigne = numLigne.replace("0", "");
				}
			//	System.out.println(num+" vs "+numLigne+" "+(num.equals(numLigne)));
				if(num.equals(numLigne))
					return ligne;
			}
		}
		return null;
	}
	public static void updateListeLigne() {
		if(Launcher.cb == null) return;
		ArrayList<BusLigneCheck> lignesAffichable = new ArrayList<BusLigneCheck>();
		for(AngersBusLigne ligne: lignes.values()) {
			if(ligne.getWays().size()!=0) {
				lignesAffichable.add(new BusLigneCheck(ligne));
			}
		}
		Launcher.cb.setItems(FXCollections.observableArrayList(lignesAffichable));
	/*	Launcher.afficherLigneBus.setOnAction(event -> {
			AngersBusLigne selectedLigne = Launcher.ligneDeBus.getSelectionModel().getSelectedItem();
			showLigne(selectedLigne);
		});
		Launcher.cacherLigneBus.setOnAction(event -> {
			hideLigne();
		});*/
	
	}
	public static AngersBusArret addArretIfNotExist(int idArret, String nomArret,String uniqueName, double lon, double lat) {
		AngersBusArret arret = null;
		if(!arrets.containsKey(idArret)) {
			arret = new AngersBusArret(nomArret,uniqueName, idArret, lon,lat);
			arrets.put(idArret,arret);
			if(Map.mapInstance!=null)
				Map.mapInstance.busStops.add(arret);
		}
		else arret = arrets.get(idArret);
		return arret;
	}
	public static AngersBusLigne addLigneIfNotExist(String numLigne, String nomLigne) {
		AngersBusLigne ligne = null;
		if(!lignes.containsKey(numLigne)) {
			ligne = new AngersBusLigne(nomLigne, numLigne);
			lignes.put(numLigne, ligne);
		}
		else ligne = lignes.get(numLigne);
		return ligne;
	}
	public static void main(String[] args) {
		getAllBusInfo();
		
	}
	public static JSONArray getJsonFromFile(File file) {
		try {
		    Scanner scan;
			scan = new Scanner(new FileInputStream(file));
		    String str = new String();
		    while (scan.hasNext())
		        str += scan.nextLine();
		    scan.close();
		    //System.out.println(str);
		    // build a JSON object
		   
		    JSONArray obj = new JSONArray(str);
		    return obj;
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return null;
	}
	public static JSONObject getJsonFromURL(String s) {
		try {
			URL url= null;
			try {
				url = new URL(s);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    // read from the URL
		    Scanner scan;
			scan = new Scanner(url.openStream());
		    String str = new String();
		    while (scan.hasNext())
		        str += scan.nextLine();
		    scan.close();
		 
		    // build a JSON object
		    JSONObject obj = new JSONObject(str);
		    return obj;
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return null;
	}
	public static JSONObject searchBus() {
		String s = "https://data.angers.fr/api/records/1.0/search/?dataset=bus-tram-position-tr&rows=10000&facet=novh&facet=mnemoligne&facet=nomligne&facet=dest";
		return getJsonFromURL(s);
	}
	
	public static void showLigne(AngersBusLigne selectedLigne) {
		Launcher.busLigneWays = new ArrayList<OverPassWay>();
		
		for(OverPassWay way: selectedLigne.getWays()) {
			Launcher.ways.get(way).setVisible(true);
			Launcher.busLigneWays.add(way);
		}
		for(Bus bus: Launcher.bus.keySet()) {
			if(bus.getLigne()==selectedLigne) {
				bus.addFocus();
			}
		}
	}
	public static void hideLigne(AngersBusLigne selectedLigne) {
		Launcher.busLigneWays = new ArrayList<OverPassWay>();
		
		for(OverPassWay way: selectedLigne.getWays()) {
			Launcher.ways.get(way).setVisible(false);
			Launcher.busLigneWays.remove(way);
		}
		for(Bus bus: Launcher.bus.keySet()) {
			if(bus.getLigne()==selectedLigne) {
				bus.removeFocus();
			}
		}
	}
	public static void afficherCacherLigne(ListCell<BusLigneCheck> ligneDeBusElement) {
		ligneDeBusElement.getItem().checkProperty().set(!ligneDeBusElement.getItem().checkProperty().get());
    	
    	if(ligneDeBusElement.getItem().getCheck()) {
			AngersData.showLigne(ligneDeBusElement.getItem().getLigne());
    	}
    	else {
    		AngersData.hideLigne(ligneDeBusElement.getItem().getLigne());
    	}
    	
        StringBuilder sb = new StringBuilder();
        Launcher.cb.getItems().filtered( f-> f!=null).filtered( f-> f.getCheck()).forEach( p -> {
            sb.append("; "+p.getLigne().getNumLigne());
        });
        final String string = sb.toString();
        Launcher.cb.setPromptText(string.substring(Integer.min(2, string.length())));
	}
}
