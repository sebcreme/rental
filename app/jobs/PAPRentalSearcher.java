package jobs;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import notifiers.Mails;

import models.Rental;
import play.Invoker;
import play.Logger;
import play.Play;
import play.Invoker.Invocation;
import play.jobs.Every;
import play.jobs.*;
import play.libs.WS;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
 

@Every("5mn")
public class PAPRentalSearcher extends Job{
	
	public void doJob() throws Exception {
		Logger.info("Searching for new rentals on PAP");
		List<Rental> found = suggestPAPRentals();
		Logger.info("Have found %d rental(s) on PAP", found.size());
		if (!found.isEmpty()) Mails.newRentals(found);
	}
	
	private static Pattern rentalId = Pattern.compile("id=\"annonce_resume_(\\w*).*?class=\"date-publication\">(.*?)</p>",Pattern.MULTILINE+Pattern.DOTALL);
	private static Pattern annonceDetail = Pattern.compile("<span class=\"prix\">(.*?)</span>.*?<span class=\"surface\">(.*?)</span>.*?<p class=\"annonce-detail-texte\">(.*?)</p>",Pattern.MULTILINE+Pattern.DOTALL);
	private static Pattern annonceText = Pattern.compile("<p.*?>(.*?)</p>", Pattern.MULTILINE+Pattern.DOTALL);
	
	public static List<Rental> suggestPAPRentals(){
	    int page = 0;
	    List<Rental> found = new ArrayList<Rental>();
	    for (int i =0; i<12; i++){
		//String pap = WS.url("http://www.pap.fr/annonce/location-appartement-divers-location-accession-loft-atelier-peniche-vide-paris-01er-g37768g37769g37770g37771g37776g37777g37778g37779g37785g37786g37787-a-partir-du-3-pieces-jusqu-a-2000-euros-a-partir-de-50-m2"+ (i!=0 ? "-"+i : "")).timeout(10000).get().getString();
    	String pap = WS.url(Play.configuration.get("pap.url")+ (i!=0 ? "-"+i : "")).timeout("30s").get().getString();
		Matcher matcher = rentalId.matcher(pap);
	        
    		while(matcher.find()){
    			Rental rental = new Rental();
    			rental.type="PAP";
    			rental.suggested = true;
    			rental.externalId = matcher.group(1);
    			rental.name = "PAP -- "+rental.externalId;
    		
    			
    			if (!Rental.isExist(rental)){
    				Logger.info("Rental {%s} seems not existing; retrieve and saving it...", rental.externalId);
    				String rentalText = WS.url("http://www.pap.fr/recherche/fonctionnalites/print?id="+rental.externalId).timeout("10s").get().getString();
    				Matcher textmatcher = annonceText.matcher(rentalText);
    				textmatcher.find();
    				rental.text = textmatcher.group(1);
    				rental.save();
    				found.add(rental);
    			} else {
    				Logger.info("Rental {%s} already exists!", rental.externalId);
    			}
    		}
		}
		return found;
	}
}
