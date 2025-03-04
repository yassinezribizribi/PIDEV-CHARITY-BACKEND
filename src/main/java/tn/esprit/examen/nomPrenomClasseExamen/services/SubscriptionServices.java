package tn.esprit.examen.nomPrenomClasseExamen.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.examen.nomPrenomClasseExamen.dto.SubscriptionDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Association;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Subscription;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.AssociationRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.SubscriptionRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class SubscriptionServices implements ISubscriptionServices {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private AssociationRepository associationRepository;

    @Override
    public Subscription addSubscriptionAndAssociateToAssociation(SubscriptionDTO subscriptionDTO) {
        Subscription subscription = new Subscription();

        // Vérification que l'ID n'est pas forcé si @GeneratedValue est utilisé
        if (subscriptionDTO.getIdSubscription() != null) {
            throw new IllegalArgumentException("L'ID de la Subscription ne doit pas être fourni.");
        }

        subscription.setSubscriptionDate(subscriptionDTO.getSubscriptionDate());

        Set<Association> associations = new HashSet<>();
        if (subscriptionDTO.getAssociations() != null) {
            for (Association association : subscriptionDTO.getAssociations()) {
                Optional<Association> existingAssociation = associationRepository.findById(association.getIdAssociation());
                if (existingAssociation.isPresent()) {
                    associations.add(existingAssociation.get());
                } else {
                    throw new IllegalArgumentException("Association ID " + association.getIdAssociation() + " introuvable.");
                }
            }
        }

        subscription.setAssociations(associations);
        return subscriptionRepository.save(subscription);
    }


    @Override
    public Subscription updateSubscription(Long id, Subscription updatedSubscription) {
        return subscriptionRepository.findById(id)
                .map(existingSubscription -> {
                    existingSubscription.setSubscriptionDate(updatedSubscription.getSubscriptionDate());
                    existingSubscription.setPosts(updatedSubscription.getPosts());
                    existingSubscription.setAssociations(updatedSubscription.getAssociations());
                    return subscriptionRepository.save(existingSubscription);
                }).orElseThrow(() -> new RuntimeException("Abonnement non trouvé avec l'ID : " + id));
    }

    @Override
    public void deleteSubscription(Long id) {
        if (!subscriptionRepository.existsById(id)) {
            throw new RuntimeException("Abonnement non trouvé avec l'ID : " + id);
        }
        subscriptionRepository.deleteById(id);
    }

    @Override
    public Optional<Subscription> getSubscriptionById(Long id) {
        return subscriptionRepository.findById(id);
    }

    @Override
    public List<Subscription> getAllSubscriptions() {
        return subscriptionRepository.findAll();
    }




}
