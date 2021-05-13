package fr.univavignon.ceri.deskmap.model;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Horaire{
	private Date dateArrivee;
	private boolean theorique;
	private static final SimpleDateFormat dateFormat  = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
	
	public Horaire(Date dateArrivee, boolean theorique) {
		super();
		this.dateArrivee = dateArrivee;
		this.theorique = theorique;
	}
	public Horaire(String dateArrivee, boolean theorique) {
		super();
		try {
			this.dateArrivee = dateFormat.parse(dateArrivee);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.theorique = theorique;
	}
	public Date getDateArrivee() {
		return dateArrivee;
	}
	public void setDateArrivee(Date dateArrivee) {
		this.dateArrivee = dateArrivee;
	}
	public boolean isTheorique() {
		return theorique;
	}
	public void setTheorique(boolean theorique) {
		this.theorique = theorique;
	}
	public String toString() {
		if(dateArrivee.getMinutes()>9)
			return dateArrivee.getHours()+":"+dateArrivee.getMinutes()+""+((theorique)?" *":"");
		return dateArrivee.getHours()+":0"+dateArrivee.getMinutes()+""+((theorique)?" *":"");
	}
	
}