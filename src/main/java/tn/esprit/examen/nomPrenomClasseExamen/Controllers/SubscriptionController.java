package tn.esprit.examen.nomPrenomClasseExamen.Controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.nomPrenomClasseExamen.dto.SubscriptionDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Subscription;
import tn.esprit.examen.nomPrenomClasseExamen.services.ISubscriptionServices;

import java.util.List;
import java.util.Optional;

@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    @Autowired
    private ISubscriptionServices subscriptionServices;

    // Ajouter une Subscription et l'associer à une Association
    @PostMapping("/add")
    public ResponseEntity<?> addSubscriptionAndAssociateToAssociation(@RequestBody SubscriptionDTO subscriptionDTO) {
        try {
            Subscription subscription = subscriptionServices.addSubscriptionAndAssociateToAssociation(subscriptionDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(subscription);
        } catch (IllegalArgumentException e) {
            log.error("Erreur: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Erreur inattendue: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Une erreur s'est produite.");
        }
    }

    // Mettre à jour un abonnement existant
    @PutMapping("/updateSubscription/{id}")
    public ResponseEntity<Subscription> updateSubscription(@PathVariable Long id, @RequestBody Subscription updatedSubscription) {
        try {
            Subscription updated = subscriptionServices.updateSubscription(id, updatedSubscription);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    // Supprimer un abonnement par ID
    @DeleteMapping("/deleteSubscription/{id}")
    public ResponseEntity<Void> deleteSubscription(@PathVariable Long id) {
        try {
            subscriptionServices.deleteSubscription(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Récupérer un abonnement par son ID
    @GetMapping("/getSubscriptionById/{id}")
    public ResponseEntity<Optional<Subscription>> getSubscriptionById(@PathVariable Long id) {
        Optional<Subscription> subscription = subscriptionServices.getSubscriptionById(id);
        return subscription.isPresent() ?
                new ResponseEntity<>(subscription, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Récupérer tous les abonnements
    @GetMapping("/getAllSubscriptions")
    public ResponseEntity<List<Subscription>> getAllSubscriptions() {
        List<Subscription> subscriptions = subscriptionServices.getAllSubscriptions();
        return new ResponseEntity<>(subscriptions, HttpStatus.OK);
    }


}
