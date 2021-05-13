package fr.univavignon.ceri.deskmap.model.overpass;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import fr.univavignon.ceri.deskmap.model.*;
import fr.univavignon.ceri.deskmap.model.map.*;
import org.w3c.dom.*;
import org.json.*;
public class OverPassQuery {
	public static String apiURL = "https://lz4.overpass-api.de/api/interpreter";
	private static String USER_AGENT = "Mozilla/5.0";
	
	public static Document query(String data) {	
		try {
			
			URL obj = new URL(apiURL);
			HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

			//add reuqest header
			con.setRequestMethod("POST");
			con.setRequestProperty("User-Agent", USER_AGENT);
			con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

			String urlParameters = "data="+data;
			
			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();

			int responseCode = con.getResponseCode();

			BufferedReader in = new BufferedReader(
			        new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
			//	System.out.println(inputLine);
				response.append(inputLine);
			}
			in.close();
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			ByteArrayInputStream input = new ByteArrayInputStream(response.toString().getBytes("UTF-8"));
			Document doc = builder.parse(input);
			return doc;
		}
		catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	public static void main(String[] args) throws IOException {
		searchCity("Par");
	}
	public static ArrayList<OverPassRelation> searchCity(String city) {
		if(city.length()<3) return null;
		JSONObject obj = queryJSON(city);
		ArrayList<OverPassRelation> villes = new ArrayList<OverPassRelation>();
	    JSONArray features = obj.getJSONArray("features");
	    for(int i=0; i<features.length();i++) { // TYPE CITY SUBCITY COUNTY
		   JSONObject ville = features.getJSONObject(i);
		   String name = ville.getString("place_name");
		   String typeID = ville.getString("id");
		   String type = typeID.substring(0, typeID.indexOf("."));
		   if(!type.equalsIgnoreCase("city")&&!type.equalsIgnoreCase("county")&&!type.equalsIgnoreCase("subcity")) continue;
		   String ID = typeID.substring(typeID.indexOf(".")+1, typeID.length());
//		   double centerLon = ville.getJSONArray("center").getDouble(0);
//		   double centerLat = ville.getJSONArray("center").getDouble(1);
//		   double bboxSWlon = ville.getJSONArray("bbox").getDouble(0);
//		   double bboxSWlat = ville.getJSONArray("bbox").getDouble(1);
//		   double bboxNElon = ville.getJSONArray("bbox").getDouble(2);
//		   double bboxNElat = ville.getJSONArray("bbox").getDouble(3);
		   OverPassNode[] bbox = new OverPassNode[2];
//		   bbox[0] = new OverPassNode(bboxNElon, bboxNElat);
//		   bbox[1] = new OverPassNode(bboxSWlon, bboxSWlat);
		   villes.add(new OverPassRelation(name, null, null, Long.parseLong(ID)));
//		   System.out.println(name+" "+type+"- "+centerLon+", "+centerLat+"  "+bboxNElon+", "+bboxNElat+" "+bboxSWlat+" "+bboxSWlon);
	    }
	    return villes;
	}
	
	public static JSONObject queryJSON(String data) {	
		// build a URL
		try {
		    String s = "https://api.maptiler.com/geocoding/"+data+".json?key=28GWnqnwOre1B7TniPPw";
		    System.out.println(s);
		    //s += URLEncoder.encode(addr, "UTF-8");
		    URL url;
			
				url = new URL(s);
	
		 
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
	public static OverPassNode[] getBBOX(long id) {
		String query = "[timeout:25];( relation("+id+"););out bb;";
		Document resultat = query(query);
		if(resultat != null) {
			if(resultat.getElementsByTagName("bounds") != null) {
				Element bounds = (Element)resultat.getElementsByTagName("bounds").item(0);
				double maxLat = Double.parseDouble(bounds.getAttribute("maxlat"));
				double minlat = Double.parseDouble(bounds.getAttribute("minlat"));
				double maxlon = Double.parseDouble(bounds.getAttribute("maxlon"));
				double minlon = Double.parseDouble(bounds.getAttribute("minlon"));
				OverPassNode NE = new OverPassNode(maxlon, maxLat);
				OverPassNode SW = new OverPassNode(minlon, minlat);
				OverPassNode[] bbox = {NE,SW};
				return bbox;
			}
		}
		return null;
	}
	public static Element relation(long id) {
		return (Element)query("relation("+id+");\r\nout;").getElementsByTagName("relation").item(0);
	}
	public static Element way(long id) {
		return (Element)query("way("+id+");\r\nout;").getElementsByTagName("way").item(0);
	}
	public static Element node(long id) {
		return (Element)query("node("+id+");\r\nout;").getElementsByTagName("node").item(0);
	}
	public static NodeList nodeOfWay(long id) {
		return query("way("+id+");\r\nnode(w);\r\nout;").getElementsByTagName("node");	
	}
	public static String findBusStop() {
		String query;
		Map map = Map.getMapInstance();
		query = "node[\"highway\"=\"bus_stop\"]("+map.limitBottom.lat+","+map.limitBottom.lon+","+map.limitTop.lat+","+map.limitTop.lon+");";
		return query;
	}
	public static String createQuery(String key, String val){
		String query;
		Map map = Map.getMapInstance();
		if(key.equals("*")) {
			query = 
					 "relation[\""+key+"\"]("+map.limitBottom.lat+","+map.limitBottom.lon+","+map.limitTop.lat+","+map.limitTop.lon+");"+
					 "way[\""+key+"\"]("+map.limitBottom.lat+","+map.limitBottom.lon+","+map.limitTop.lat+","+map.limitTop.lon+");";
		}
		else {
		query = 				
				 "relation[\""+key+"\"=\""+val+"\"]("+map.limitBottom.lat+","+map.limitBottom.lon+","+map.limitTop.lat+","+map.limitTop.lon+");"+
				 "way[\""+key+"\"=\""+val+"\"]("+map.limitBottom.lat+","+map.limitBottom.lon+","+map.limitTop.lat+","+map.limitTop.lon+");";				
		}
		return query;
	}
//	public static OverPassRelation searchCity(String query) {
//		if(query.length()>2) {
//			
//		}
//	}
	public static void execQuery(String query, int timeout, String out) {
		String q = "[timeout:"+timeout+"];(";
		q+= query;
		q+= ");out "+out+";";
		System.out.println(q);
		Document data =  query(q);
		if(data==null) return;
		Map map = Map.getMapInstance();
		NodeList relation = data.getElementsByTagName("relation");
		for(int i=0; i<relation.getLength(); i++) {
			Element relationXml = (Element) relation.item(i);
			long id = Long.parseLong(relationXml.getAttribute("id"));
			if(!map.relations.containsKey(id)) {
				map.relations.put(id, new OverPassRelation(relationXml, map.ways, map.nodes));
			}
		}
		NodeList wayList = data.getElementsByTagName("way");
		for(int i=0; i<wayList.getLength(); i++) {
			Element wayXml = (Element) wayList.item(i);
			long id = Long.parseLong(wayXml.getAttribute("id"));
			if(!map.ways.containsKey(id)) {
				OverPassWay way = new OverPassWay(wayXml, map.nodes);
				if(!way.highWay.equals(""))
					map.highWays.add(way);
				map.ways.put(id,way );
				
			}
		}
		NodeList nodeList = data.getElementsByTagName("node");
		for(int i=0; i<nodeList.getLength(); i++) {
				Element nodeXml = (Element) nodeList.item(i);
				NodeList tags = nodeXml.getElementsByTagName("tag");
				for(int j =0; j<tags.getLength();j++ ) {
					Element membre =(Element) tags.item(j);
					String k = membre.getAttribute("k");
					if(k.equals("highway")&&membre.getAttribute("v").equals("bus_stop")) {
						map.busStops.add(new BusStop(nodeXml));
					}
				}
		}
	}
	public static void getNatural(String type,OverPassNode limitTop, OverPassNode limitBottom, HashMap<String,OverPassNode> nodes, HashMap<Long,OverPassWay> ways, HashMap<Long,OverPassRelation> relations) {
		String query;
		if(type.equals("*")) {
			query = 
					"[timeout:25];("+
					 "relation[\"natural\"]("+limitBottom.lat+","+limitBottom.lon+","+limitTop.lat+","+limitTop.lon+");"+
					 "way[\"natural\"]("+limitBottom.lat+","+limitBottom.lon+","+limitTop.lat+","+limitTop.lon+");"+
					");out geom;";
		}
		else {
		query = 
				"[timeout:25];("+
				 "relation[\"natural\"=\""+type+"\"]("+limitBottom.lat+","+limitBottom.lon+","+limitTop.lat+","+limitTop.lon+");"+
				 "way[\"natural\"=\""+type+"\"]("+limitBottom.lat+","+limitBottom.lon+","+limitTop.lat+","+limitTop.lon+");"+
				");out geom;";
		}
		/*String query = 
				"[timeout:25];("+
				 "relation(6412662);"+
				");out geom;";*/
		System.out.println(query);
		Document data =  query(query);
		if(data==null)return;
		NodeList relation = data.getElementsByTagName("relation");
		for(int i=0; i<relation.getLength(); i++) {
			Element relationXml = (Element) relation.item(i);
			long id = Long.parseLong(relationXml.getAttribute("id"));
			if(!relations.containsKey(id)) {
				relations.put(id, new OverPassRelation(relationXml, ways, nodes));
			}
		}
		NodeList wayList = data.getElementsByTagName("way");
		for(int i=0; i<wayList.getLength(); i++) {
			Element wayXml = (Element) wayList.item(i);
			long id = Long.parseLong(wayXml.getAttribute("id"));
			if(!ways.containsKey(id)) {
				ways.put(id, new OverPassWay(wayXml, nodes));
			}
		}
	}
	public static void getPlace(String type,OverPassNode limitTop, OverPassNode limitBottom, HashMap<String, OverPassNode> nodes, HashMap<Long,OverPassWay> ways, HashMap<Long,OverPassRelation> relations) {
		String query = 
				"[timeout:25];("+
				 "relation[\"place\"=\""+type+"\"]("+limitBottom.lat+","+limitBottom.lon+","+limitTop.lat+","+limitTop.lon+");"+
				 "way[\"place\"=\""+type+"\"]("+limitBottom.lat+","+limitBottom.lon+","+limitTop.lat+","+limitTop.lon+");"+
				");out geom;";
		/*String query = 
				"[timeout:25];("+
				 "relation(6412662);"+
				");out geom;";*/
		System.out.println(query);
		Document data =  query(query);
		
		NodeList relation = data.getElementsByTagName("relation");
		for(int i=0; i<relation.getLength(); i++) {
			Element relationXml = (Element) relation.item(i);
			long id = Long.parseLong(relationXml.getAttribute("id"));
			if(!relations.containsKey(id)) {
				relations.put(id, new OverPassRelation(relationXml, ways, nodes));
			}
		}
	}
	public static void getWay(String type, OverPassNode limitTop, OverPassNode limitBottom, HashMap<String,OverPassNode> nodes, HashMap<Long,OverPassWay> ways) {
		String query = "[timeout:25];\r\n" + 
				"// gather results\r\n" + 
				"(\r\n" +  
				"  way[\"highway\"=\""+type+"\"]("+limitBottom.lat+","+limitBottom.lon+","+limitTop.lat+","+limitTop.lon+");\r\n" + 
				"  relation[\"highway\"=\""+type+"\"]("+limitBottom.lat+","+limitBottom.lon+","+limitTop.lat+","+limitTop.lon+");\r\n" + 
				");\r\n" + 
				"// print results\r\n" + 
				"out body;\r\n" + 
				">;\r\n" + 
				"out skel qt;";
	//	System.out.println(query);
		Document data =  query(query);	
		NodeList nodesData = data.getElementsByTagName("node");
		int c = 0;
		for(int i=0; i<nodesData.getLength(); i++) {
			Element nodeXml = (Element) nodesData.item(i);
			double lat = Double.parseDouble(nodeXml.getAttribute("lat"));
			double lon = Double.parseDouble(nodeXml.getAttribute("lon"));
		//	if(lon > limitBottom.lon || lon < limitTop.lon || lat < limitTop.lat || lat > limitBottom.lat) continue;
			String ref = OverPassNode.generateRef(lat, lon);
			long id = Long.parseLong(nodeXml.getAttribute("id"));
			if(!nodes.containsKey(ref)) {
				nodes.put(ref, new OverPassNode(nodeXml));
				c++;
			}
		}
		System.out.println("C "+c);
		c = 0;
		NodeList waysData = data.getElementsByTagName("way");
		for(int i=0; i<waysData.getLength(); i++) {
			Element waysXml = (Element) waysData.item(i);
			long id = Long.parseLong(waysXml.getAttribute("id"));
			if(!ways.containsKey(id)) {
				ways.put(id, new OverPassWay(waysXml, nodes));
			}
			c=i;
		}
		System.out.println("C "+c);
	}
	public static void getWater(OverPassNode limitTop, OverPassNode limitBottom, HashMap<String,OverPassNode> nodes, HashMap<Long,OverPassWay> ways, HashMap<Long,OverPassRelation> relations) {
		String query = "[timeout:25];\r\n" + 
				"// gather results\r\n" + 
				"(\r\n" +  
				"  relation[\"natural\"=\"water\"]("+limitBottom.lat+","+limitBottom.lon+","+limitTop.lat+","+limitTop.lon+");\r\n" + 
				"way(r)("+limitBottom.lat+","+limitBottom.lon+","+limitTop.lat+","+limitTop.lon+");\r\n"+
				">;"+
				");\r\n" + 
				"// print results\r\n" + 
				"out body;\r\n"	;
		//System.out.println(query);
		Document data =  query(query);	
		NodeList nodesData = data.getElementsByTagName("node");
		int c = 0;
		for(int i=0; i<nodesData.getLength(); i++) {
			Element nodeXml = (Element) nodesData.item(i);
			long id = Long.parseLong(nodeXml.getAttribute("id"));
			double lat = Double.parseDouble(nodeXml.getAttribute("lat"));
			double lon = Double.parseDouble(nodeXml.getAttribute("lon"));
			String ref = OverPassNode.generateRef(lat, lon);
			if(lon > limitBottom.lon || lon < limitTop.lon || lat < limitTop.lat || lat > limitBottom.lat) continue;
			if(!nodes.containsKey(ref)) {
				nodes.put(ref, new OverPassNode(nodeXml));
				c++;
			}
		}
		System.out.println("C "+c);
		c = 0;
		NodeList waysData = data.getElementsByTagName("way");
		for(int i=0; i<waysData.getLength(); i++) {
			Element waysXml = (Element) waysData.item(i);
			long id = Long.parseLong(waysXml.getAttribute("id"));
			if(!ways.containsKey(id)) {
				ways.put(id, new OverPassWay(waysXml, nodes));
			}
			c=i;
		}
		NodeList relation = data.getElementsByTagName("relation");
		for(int i=0; i<relation.getLength(); i++) {
			Element relationXml = (Element) relation.item(i);
			long id = Long.parseLong(relationXml.getAttribute("id"));
			if(!relations.containsKey(id)) {
				relations.put(id, new OverPassRelation(relationXml, ways, nodes));
			}
			c=i;
		}
//		System.out.println("C "+c);
	}
	public static void getGreenSpace(String type, OverPassNode limitTop, OverPassNode limitBottom, HashMap<String,OverPassNode> nodes, HashMap<Long,OverPassWay> ways) {
		String query = "[timeout:25];\r\n" + 
				"// gather results\r\n" + 
				"(\r\n" +  
				"  node[\"landuse\"=\""+type+"\"]("+limitBottom.lat+","+limitBottom.lon+","+limitTop.lat+","+limitTop.lon+");\r\n" + 
				"  way[\"landuse\"=\""+type+"\"]("+limitBottom.lat+","+limitBottom.lon+","+limitTop.lat+","+limitTop.lon+");\r\n" + 
				"  relation[\"landuse\"=\""+type+"\"]("+limitBottom.lat+","+limitBottom.lon+","+limitTop.lat+","+limitTop.lon+");\r\n" + 
				");\r\n" + 
				"// print results\r\n" + 
				"out body;\r\n" + 
				">;\r\n" + 
				"out skel qt;";
		//System.out.println(query);
		Document data =  query(query);	
		NodeList nodesData = data.getElementsByTagName("node");
		int c = 0;
		for(int i=0; i<nodesData.getLength(); i++) {
			Element nodeXml = (Element) nodesData.item(i);
			double lat = Double.parseDouble(nodeXml.getAttribute("lat"));
			double lon = Double.parseDouble(nodeXml.getAttribute("lon"));
			String ref = OverPassNode.generateRef(lat, lon);
			if(lon > limitBottom.lon || lon < limitTop.lon || lat < limitTop.lat || lat > limitBottom.lat) continue;
			long id = Long.parseLong(nodeXml.getAttribute("id"));
			if(!nodes.containsKey(ref)) {
				nodes.put(ref, new OverPassNode(nodeXml));
				c++;
			}
		}
		System.out.println("C "+c);
		c = 0;
		NodeList waysData = data.getElementsByTagName("way");
		for(int i=0; i<waysData.getLength(); i++) {
			Element waysXml = (Element) waysData.item(i);
			long id = Long.parseLong(waysXml.getAttribute("id"));
			if(!ways.containsKey(id)) {
				ways.put(id, new OverPassWay(waysXml, nodes));
			}
			c=i;
		}
	//	System.out.println("C "+c);
	}
	
}
