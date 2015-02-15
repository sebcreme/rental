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
 

@Every("cron.pap")
public class PAPRentalSearcher extends Job{
	
	public void doJob() throws Exception {
		Logger.info("Searching for new rentals on PAP");
		List<Rental> found = suggestPAPRentals();
		Logger.info("Have found %d return ntal(s) on PAP", found.size());
		if (!found.isEmpty()) Mails.newRentals(found);
	}
	
	private static Pattern rentalId = Pattern.compile("id=\"annonce_resume_(\\w*).*?class=\"date-publication\">(.*?)</p>",Pattern.MULTILINE+Pattern.DOTALL);
	private static Pattern annonceDetail = Pattern.compile("<span class=\"prix\">(.*?)</span>.*?<span class=\"surface\">(.*?)</span>.*?<p class=\"annonce-detail-texte\">(.*?)</p>",Pattern.MULTILINE+Pattern.DOTALL);
	private static Pattern annonceText = Pattern.compile("<p.*?>(.*?)</p>", Pattern.MULTILINE+Pattern.DOTALL);
	
	public static List<Rental> suggestPAPRentals(){
	    
	    List<Rental> found = new ArrayList<Rental>();
	    for (int i =0; i<4; i++){
			//String pap = WS.url("http://www.pap.fr/annonce/location-appartement-divers-location-accession-loft-atelier-peniche-vide-paris-01er-g37768g37769g37770g37771g37776g37777g37778g37779g37785g37786g37787-a-partir-du-3-pieces-jusqu-a-2000-euros-a-partir-de-50-m2"+ (i!=0 ? "-"+i : "")).timeout(10000).get().getString();
	    	String pap = WS.url(Play.configuration.get("pap.url")+ (i!=0 ? "-"+i : "")).timeout("30s").get().getString();
			
	    	Document rentalDoc = Jsoup.parse(pap);
	    	Elements rentalElts = rentalDoc.select("li.annonce");

			for (Element rentalElt : rentalElts) {
				


				Rental rental = new Rental();
				rental.type = "PAP";
				rental.suggested = true;
				rental.externalId = rentalElt.select("a[name]").attr("name");
				rental.href = "http://www.pap.fr/annonces/r" + rental.externalId;
				rental.text = rentalElt.select("div.description > p").text();
				rental.name = "PAP -- "+rental.externalId;
				rental.imgHref = rentalElt.select(".vignette-annonce img").attr("src").replace("thumb.", "");
				rental.text +="<a href=\""+rental.href+"\" target=\"_blank\">"+
				"<img src=\""+rental.imgHref+"\"></a>\n";
				String price = rentalElt.select("span.prix").text().replace("\u00A0", "").replace("€", "").replaceAll(" ","").replaceAll("\\.","");
				if (price!= null && !price.isEmpty()) rental.price = Integer.parseInt(price);
				rental.text+= "</br>" + rentalElt.select("div.description  li").text();


				if (!Rental.isExist(rental)){
					Logger.info("Rental {%s} seems not existing; retrieve and saving it...", rental.externalId);
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
