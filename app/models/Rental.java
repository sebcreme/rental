package models;

import play.*;
import play.db.jpa.*;
import play.data.validation.*;

import javax.persistence.*;

import org.hibernate.annotations.Type;

import controllers.Rentals;

import java.util.*;
import java.text.*;

@Entity

public class Rental extends Model implements Comparable<Rental>{
	
	@Lob @Type(type="org.hibernate.type.StringClobType")  public String text;
	public String externalId;
	@Column(name="whenAdded")
	public Date when = new Date();
	public String address;
	public String name;
	public Boolean banned = false;
	public Boolean suggested = false;
	public String type ="PAP";
	@OneToMany(cascade=CascadeType.ALL)
	public List<Note> notes = new ArrayList<Note>();
	@Lob @Type(type="org.hibernate.type.StringClobType") 
	public String href;
	public Integer price;
	public String imgHref;
	private static DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.FRANCE);

    
	public String toString(){
		return (address != null && !address.equals("")) ? address : name;
	}
	public String price(){
		if (this.price == null) return "Prix Non Renseigné";
		else {
			return formatter.format(this.price).trim() + "€";
		}
	}
	public static boolean isExist(Rental rental) {
		return !Rental.find("byExternalId", rental.externalId).fetch().isEmpty();
	}
	public boolean isFollowed() {
		return !this.banned && !this.suggested;
	}
	public Rental prev() {
		List<Rental> rentals = Rental.find("banned is "+banned+" and suggested is "+suggested+" and whenAdded < '"+this.when+"' order by whenAdded desc").fetch();
		if (rentals.size() > 0)
			return rentals.get(0);
		return null;
	}
	public Rental next() {
		List<Rental> rentals = Rental.find("banned is "+banned+" and suggested is "+suggested+" and whenAdded > '"+this.when+"'").fetch();
		if (rentals.size() > 0)
			return rentals.get(0);
		return null;
	}
	public String getHref() {
		if (this.type.equals("PAP")) return "http://www.pap.fr/annonce/r"+ this.externalId;
		else if (this.type.equals("LBC")) return "http://www.leboncoin.fr/locations/" +this.externalId+".htm";
		return href;
	}
	@Override
	public int compareTo(Rental o) {
		return o.notes.size() - this.notes.size();
	}
}




