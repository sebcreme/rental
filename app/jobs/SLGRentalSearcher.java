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
 

@Every("5mn")
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
	    for (int i =1; i<=nbPages; i++){

            if(i>1){
                slgUrl = Play.configuration.getProperty("slg.url")+i+"";
                slgPage = WS.url(slgUrl).timeout("30s").get().getString();
                doc = Jsoup.parse(slgPage);
            }
            Elements rentalArticles = doc.select("article.annonce");
            for (Element rentalArticle : rentalArticles) {
                Rental rental = new Rental();
                rental.externalId = rentalArticle.id();
                if(null !=rental.externalId){
                rental.type="SLG";
                rental.suggested = true;
                rental.name = "SLG -- "+rental.externalId;
                if (!Rental.isExist(rental)){
                        Logger.info("Rental {%s} seems not existing; retrieve and saving it...", rental.externalId);
                        Element linkElement = rentalArticle.select("a.annonce__link").first();
                        String rentalHref = linkElement.attr("href");
                        rental.href = rentalHref;
                        String rentalTextPage = WS.url(rentalHref).timeout("10s").get().getString();
                        Document rentalText = Jsoup.parse(rentalTextPage);
                        Element titleElement = rentalText.select("h1.detail-title").first();
                        Element priceElement = rentalText.select(".resume__prix").first();
                        Element descElement = rentalText.select("p.description").first();
                        StringBuffer sb = new StringBuffer();
                        if(null!=titleElement)sb.append(titleElement.text());
                        sb.append(" ");
                        if(null!=priceElement)sb.append(priceElement.text());
                        sb.append(" ");
                        sb.append("</br>");
                        if(null!=descElement)sb.append(descElement.text());
                        Element picElement = rentalText.select("img.carrousel_image_small").first();
                        if(null!=picElement){
                            sb.append("</br>");
                            sb.append(picElement.outerHtml());
                        }
                        rental.text = sb.toString();
                        rental.save();
                        found.add(rental);
                } else {
                    Logger.info("Rental {%s} already exists!", rental.externalId);
                }   
            }
            }
    		i++;
		}
		return found;
	}
}
