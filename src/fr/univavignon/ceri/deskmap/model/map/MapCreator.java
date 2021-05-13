package fr.univavignon.ceri.deskmap.model.map;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

import fr.univavignon.ceri.deskmap.model.overpass.*;
public class MapCreator {
	OverPassRelation ville;
	public MapCreator(OverPassRelation relation) {
		this.ville = relation;
	}
	public static Map createMap(OverPassRelation relation) {
		MapCreator m = new MapCreator(relation);
		Map map = m.deSerialize();
		if(map!=null) {
			System.out.println("Map serialized "+map.ways.size());

			Map.mapInstance = map;
			return map;
		}
		else return new Map(relation);
	}
	public Map deSerialize() {
		File f = new File("cache/"+ville.id+"/data.ser");
		Map map = null;
		FileInputStream fis;
		try {
			fis = new FileInputStream(f);
			 ObjectInputStream ois = new ObjectInputStream(fis);
			 map = (Map) ois.readObject();
			 
			 return map;
				
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
       
	}
}
