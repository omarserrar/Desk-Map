package fr.univavignon.ceri.deskmap.model;

import fr.univavignon.ceri.deskmap.model.angers.AngersBusLigne;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

public class BusLigneCheck {
	 private BooleanProperty check = new SimpleBooleanProperty(false);
	    private ObjectProperty<AngersBusLigne> item = new SimpleObjectProperty<AngersBusLigne>();

	    BusLigneCheck() {
	    }

	    public BusLigneCheck(AngersBusLigne ligne) {
	        this.item.set(ligne);
	    }

	    BusLigneCheck(AngersBusLigne ligne, Boolean check) {
	        this.item.set(ligne);
	        this.check.set(check);
	    }

	    public BooleanProperty checkProperty() {
	        return check;
	    }

	    public Boolean getCheck() {
	        return check.getValue();
	    }

	    public void setCheck(Boolean value) {
	        check.set(value);
	    }

	    public ObjectProperty<AngersBusLigne> itemProperty() {
	        return item;
	    }

	    public AngersBusLigne getLigne() {
	        return item.getValue();
	    }

	    public void setLigne(AngersBusLigne ligne) {
	        item.setValue(ligne);
	    }

	    @Override
	    public String toString() {
	        return item.getValue().toString();
	    }
}
