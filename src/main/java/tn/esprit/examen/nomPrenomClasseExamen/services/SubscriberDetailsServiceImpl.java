package tn.esprit.examen.nomPrenomClasseExamen.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.SubscriberRepository;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Subscriber;

@Service
@RequiredArgsConstructor
public class SubscriberDetailsServiceImpl implements UserDetailsService {

    private final SubscriberRepository subscriberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("🔍 Vérification de l'email en base : " + email);

        if (email == null || email.isEmpty()) {
            throw new UsernameNotFoundException("❌ Email est null ou vide !");
        }

        Subscriber subscriber = subscriberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("❌ Subscriber non trouvé avec cet email : " + email));

        System.out.println("✅ Subscriber trouvé : " + subscriber.getEmail());
        return SubDetailsImpl.build(subscriber);
    }
}