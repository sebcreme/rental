package notifiers;

import java.util.List;

import models.Rental;

import play.mvc.Mailer;

public class Mails extends Mailer {
	 public static void newRentals(List<Rental> rentals) {
	      setSubject("De nouvelles annonces ont été récupérées");
	      setFrom("locations@sebcreme.fr");
	      addRecipient("julien.tricoire@gmail.com");
          addRecipient("sebastien.creme@gmail.com");
	      send(rentals);
	   }
}
