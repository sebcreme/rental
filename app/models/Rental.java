package models;

import play.*;
import play.db.jpa.*;
import play.data.validation.*;

import javax.persistence.*;

import org.hibernate.annotations.Type;

import controllers.Rentals;

import java.util.*;

@Entity

public class Rental extends Model implements Comparable<Rental>{
	
	@Lob @Type(type="org.hibernate.type.StringClobType")  public String text;
	public String externalId;
	@Column(name="whenAdded")
	public Date when = new Date();
	public String address;
	public String name;
	public Boolean banned;
	public Boolean suggested = false;
	public String type ="PAP";
	@OneToMany(cascade=CascadeType.ALL)
	public List<Note> notes = new ArrayList<Note>();
    
	public String toString(){
		return (address != null && !address.equals("")) ? address : name;
	}
	
	public static boolean isExist(Rental rental) {
		return !Rental.find("byExternalId", rental.externalId).fetch().isEmpty();
	}

	@Override
	public int compareTo(Rental o) {
		return o.notes.size() - this.notes.size();
	}
}




