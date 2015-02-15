package jobs;

import java.util.*;
import java.collections.*;
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
import play.libs.URLs;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;


@Every("cron.lbc")
public class LBCRentalSearcher extends Job{
private static Pattern lbcRentalId = Pattern.compile("ventes_immobilieres/(\\d*)");

public void doJob() throws Exception {
		Logger.info("Searching for new rentals on LeBonCoin...");
		List<Rental> found = suggestLeBonCoinRentals();
		Logger.info("Have found %d rental(s) on LEBONCOIN", found.size());
		if (!found.isEmpty()) Mails.newRentals(found);
}

public static List<Rental> suggestLeBonCoinRentals() throws Exception{
	String[] locationsArray = Play.configuration.getProperty("lbc.locations").split(",");
	List<String> locations = Arrays.asList(locationsArray);
	List<Rental> found = new ArrayList<Rental>();
	for (String location : locations){
		String lbcUrl = Play.configuration.get("lbc.url")+location;
		Logger.info(lbcUrl);
		String lbcPage = WS.url(lbcUrl).get().getString();
		Document doc = Jsoup.parse(lbcPage);
		Elements rentalLinks = doc.select("div.list-lbc > a");

		for (Element link : rentalLinks) {
		  String linkHref = link.attr("href");
		  String linkdescription = link.attr("title");
		  String lbcRental = WS.url(linkHref).get().getString();
		  
		  Document rentalDoc = Jsoup.parse(lbcRental);
		  
		  Integer price = Integer.parseInt(rentalDoc.select("span.price").text().replace("\u00A0", "").replace("€", "").replaceAll(" ",""));

		  String description = rentalDoc.select("div .AdviewContent .content").text();
		  String image = rentalDoc.select("div .images_cadre a").attr("style");
		  String imgHref = image.split("'")[1];
		  if (image.indexOf("'") > 0) {
		  	description +="<a href=\""+linkHref+"\" target=\"_blank\"><img src=\""+imgHref+"\"></a>\n";
		  }


		  Rental rental = new Rental();
		  rental.type="LBC";
		  rental.suggested = true;
		  rental.price = price; 
		  Matcher matcher = lbcRentalId.matcher(linkHref);
		  if (matcher.find()) rental.externalId = matcher.group(1);
		  rental.name = "LBC -- "+rental.externalId;
		  if (!Rental.isExist(rental)){
		  	rental.text = linkdescription + "</br>"+ description;
		  	rental.imgHref = imgHref;
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