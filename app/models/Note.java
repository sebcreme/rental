package models;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;
import java.util.*;

@Entity
public class Note extends Model {
	public String note;
	public Date addedAt = new Date();
	public Note(String note){
		this.note = note;
	}
	public String toString(){
		return this.note;
	}
}

