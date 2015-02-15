package jobs;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.lang.StringBuffer;

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
 

@Every("cron.slg")
public class SLGRentalSearcher extends Job{
	
	public void doJob() throws Exception {
		Logger.info("Searching for new rentals on SELOGER");
		List<Rental> found = suggestSELOGERRentals();
		Logger.info("Have found %d rental(s) on SELOGER", found.size());
		if (!found.isEmpty()) Mails.newRentals(found);
	}
	
	public static List<Rental> suggestSELOGERRentals(){
	    List<Rental> found = new ArrayList<Rental>();
        String slgUrl = Play.configuration.getProperty("slg.url");
        String slgPage = WS.url(slgUrl).timeout("30s").get().getString();
        Document doc = Jsoup.parse(slgPage);
        Element nbPagesElement = doc.select("span.title_nbresult").first();
        int nbPages = (int)Math.ceil((Integer.parseInt(nbPagesElement.text())/10)+0.5);
        Logger.debug("Found %d pages", nbPages);
	    for (int i =1; i<=nbPages; i++){
            Logger.debug("Get page %d", i);
            slgUrl = Play.configuration.getProperty("slg.url")+i+"";
            Logger.debug("Retrieve URL : %s", slgUrl);
            slgPage = WS.url(slgUrl).timeout("30s").get().getString();
            doc = Jsoup.parse(slgPage);
            Elements rentalArticles = doc.select("article.listing");
            for (Element rentalArticle : rentalArticles) {
                Rental rental = new Rental();
                rental.externalId = rentalArticle.attr("data-listing-id");
                if(null != rental.externalId){
                    rental.type="SLG";
                    rental.suggested = true;
                    rental.name = "SLG -- "+rental.externalId;
                    if (!Rental.isExist(rental)){
                            Logger.info("Rental {%s} seems not existing; retrieve and saving it...", rental.externalId);
                            Element linkElement = rentalArticle.select("div.listing_infos h2 a").first();
                            String rentalHref = linkElement.attr("href");
                            rental.href = rentalHref;
                            //String rentalTextPage = WS.url(rentalHref).timeout("10s").get().getString();

                            rental.text = rentalArticle.select("div.listing_infos p.description").text();
                            rental.name = linkElement.text();
                            rental.price = Integer.parseInt(rentalArticle.select("div.listing_infos a.amount").text().replace("\u00A0", "").replace("€", "").replaceAll(" ","").replaceAll("\\.","").replaceAll("FAI", ""));
                            Element r = rentalArticle.select("div.listing_photo_container img").first();
                            if (r != null ) rental.imgHref = r.attr("src").replaceAll("c[0-9]{3}", "bigs");
                            
                            rental.save();
                            found.add(rental);
                    } else {
                        Logger.info("Rental {%s} already exists!", rental.externalId);
                    }   
            }
            }
		}
		return found;
	}
}
