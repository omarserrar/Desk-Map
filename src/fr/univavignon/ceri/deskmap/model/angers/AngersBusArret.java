package fr.univavignon.ceri.deskmap.model.angers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Element;
import fr.univavignon.ceri.deskmap.model.*;
import fr.univavignon.ceri.deskmap.model.angers.*;
import fr.univavignon.ceri.deskmap.model.itineraire.*;
import fr.univavignon.ceri.deskmap.model.map.*;
import fr.univavignon.ceri.deskmap.model.overpass.*;


public class AngersBusArret extends BusStop {
	private int numArret;
	private HashMap<AngersBusLigne, ArrayList<AngersBusDesserte>> dessertes;
	private String uniqueName;
	public AngersBusArret(String nomArret, String mNemoArret, int numArret, double lon, double lat) {
		super(lon, lat);
		this.name = nomArret;
		dessertes = new HashMap<AngersBusLigne, ArrayList<AngersBusDesserte>>();
		this.uniqueName = mNemoArret;
	}
	public HashMap<AngersBusLigne, ArrayList<AngersBusDesserte>> getDessertes() {
		return dessertes;
	}
	public void addDesserte(AngersBusLigne ligne, AngersBusDesserte desserte) {
		if(!dessertes.containsKey(ligne)) {
			dessertes.put(ligne, new ArrayList<AngersBusDesserte>());	
		}
		boolean found = false;
		for(AngersBusDesserte dessert: dessertes.get(ligne)) {
			if(dessert.getDest().equals(desserte.getDest())) {
				found = true;
				break;
			}
		}
		if(!found)
			dessertes.get(ligne).add(desserte);
	}
	public void downloadHorraires() {
		JSONObject horraireJSON = AngersData.getHorraireArretJSON(this);
		JSONArray records = horraireJSON.getJSONArray("records");
		for(int i=0;i<records.length();i++) {
			JSONObject fields = records.getJSONObject(i).getJSONObject("fields");
			String fiable = fields.getString("fiable");
			String codeParcour = fields.getString("codeparcours");
			String mNemoLigne = fields.getString("mnemoligne");
			String arrivee = fields.getString("arrivee");
			String dest = fields.getString("dest");
			for(AngersBusDesserte desserte: dessertes.get(AngersData.lignes.get(mNemoLigne))) {
				if(desserte.getDest().equals(dest)) {
					boolean theorique = fiable.equals("T");
					desserte.getHoraires().add(new Horaire(arrivee, theorique));
				}
			}
		}
	}
	public boolean contientLigneDest(AngersBusLigne ligne, String dest) {
		if(!dessertes.containsKey(ligne)) return false;
		for(AngersBusDesserte dessert: dessertes.get(ligne)) {
			if(dessert.getDest().equals(dest)) return true;
		}
		return false;
	}
	public String getUniqueName() {
		return uniqueName;
	}
	private OverPassNode closestNode = null;
	public OverPassNode getClosestNode() {
		if(closestNode==null) return findClosestNode();
		return closestNode;
	}
	public OverPassNode findClosestNode() {
		double minDist = Double.POSITIVE_INFINITY;
		OverPassNode nodeProche = null;
		int pos = 0,i=0;
		for(OverPassNode node :Map.mapInstance.roadNetword.values()) {
			i++;
			double distance = distance(node);
			if(distance<minDist) {
				minDist = distance;
				nodeProche = node;
				pos = i;
			}
		}
		closestNode = nodeProche;
		minDist = Double.POSITIVE_INFINITY;
		nodeProche = null;
		for(VertexNode nd: closestNode.connectedNode) {
			double distance = distance(nd.getNode());
			if(distance<minDist) {
				minDist = distance;
				nodeProche = nd.getNode();
				pos = i;
			}
		}
		
		return nodeProche;
	}
}