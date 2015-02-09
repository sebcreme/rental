package notifiers;

import java.util.List;

import models.Rental;

import play.mvc.Mailer;

public class Mails extends Mailer {
	 public static void newRentals(List<Rental> rentals) {
	      setSubject("De nouvelles annonces ont été récupérées");
	      setFrom("locations@sebcreme.fr");
	      addRecipient("sebastien.creme@gmail.com");
          addRecipient("mlbayle@gmail.com");
	      send(rentals);
	   }
}
