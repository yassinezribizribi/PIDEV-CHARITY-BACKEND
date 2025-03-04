package tn.esprit.examen.nomPrenomClasseExamen.services;

import tn.esprit.examen.nomPrenomClasseExamen.dto.SubscriptionDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Subscription;

import java.util.List;
import java.util.Optional;

public interface ISubscriptionServices {

    Subscription addSubscriptionAndAssociateToAssociation(SubscriptionDTO subscriptionDTO);

    Subscription updateSubscription(Long id, Subscription updatedSubscription);

    void deleteSubscription(Long id);

    Optional<Subscription> getSubscriptionById(Long id);

    List<Subscription> getAllSubscriptions();

}
