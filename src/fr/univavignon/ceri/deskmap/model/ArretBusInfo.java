package fr.univavignon.ceri.deskmap.model;

import java.io.IOException;
import fr.univavignon.ceri.deskmap.*;
import fr.univavignon.ceri.deskmap.controllers.*;
import fr.univavignon.ceri.deskmap.view.*;
import fr.univavignon.ceri.deskmap.model.*;
import fr.univavignon.ceri.deskmap.model.angers.*;
import fr.univavignon.ceri.deskmap.model.itineraire.*;
import fr.univavignon.ceri.deskmap.model.overpass.*;
import fr.univavignon.ceri.deskmap.model.map.*;
import fr.univavignon.ceri.deskmap.controllers.ArretInfoController;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ArretBusInfo extends Stage {
	private Clickable c=null;
	public ArretBusInfo(AngersBusArret arret){
		AnchorPane root = null;
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("./view/ArretInfo.fxml"));
			root = (AnchorPane)loader.load();
			ArretInfoController controller = (ArretInfoController)loader.getController();
			controller.init(arret);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Scene scene = new Scene(root);
		this.setTitle(arret.name);

		this.setScene(scene);
        //primaryStage.setResizable(false);
		this.sizeToScene();
	}

}