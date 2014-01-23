package controllers;

import play.Logger;
import play.mvc.*;
import models.*;
import jobs.*;
import notifiers.Mails;

import java.util.*;
import play.libs.*;
import org.w3c.dom.*;
import java.util.regex.*;

public class Application extends Controller {
	@Before
	private static void setStatistics(){
		Long bannedCount = Rental.count("byBanned", true);
		Long followedCount = Rental.count("banned is false and suggested is false");
		Long suggestedCount = Rental.count("bySuggested", true);
		renderArgs.put("suggestedCount", suggestedCount);
		renderArgs.put("followedCount", followedCount);
		renderArgs.put("bannedCount", bannedCount);
	}

    public static void index() {
    	List<Rental> rentals  = Rental.find("banned is false and suggested is false order by whenAdded desc").fetch();
    	Collections.sort(rentals);
		render(rentals);
    }
	public static void rental(Long id) {
		Rental rental = Rental.findById(id);
		render("Application/rental.html",rental);
	}
	public static void addNote(Long rentalId, String note) {
		Rental rental = Rental.findById(rentalId);
		Note noteO = new Note(note);
		rental.notes.add(noteO);
		rental.save();
	}
	public static void banned() {
		List<Rental> rentals  = Rental.find("banned is true order by whenAdded desc").fetch();
		Collections.sort(rentals);
		render("Application/index.html",rentals);
	}
	public static void suggestion() {
		List<Rental> rentals  = Rental.find("suggested is true order by whenAdded desc").fetch();
		render("Application/index.html",rentals);
	}
	public static void deleteAllSuggestions() {
		Rental.delete("suggested is true");
		index();
		
	}
	public static void ban(Long id) {
		Rental rental = Rental.findById(id);
		rental.banned = true;
		Boolean wasSuggested = rental.suggested;
		rental.suggested = false;
		rental.save();
		if (wasSuggested) suggestion();
		index();
	}
	public static void unban(Long id) {
		Rental rental = Rental.findById(id);
		rental.banned = false;
		Boolean wasSuggested = rental.suggested;
		
		rental.suggested = false;
		rental.save();
		if (wasSuggested) suggestion();
		index();
	}
	public static void test() throws Exception{
		new SLGRentalSearcher().doJob();
	}
	
}