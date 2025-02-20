package tn.esprit.examen.nomPrenomClasseExamen.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.SubscriberRepository;
import tn.esprit.examen.nomPrenomClasseExamen.dto.SignupRequestDto;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Role;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Subscriber;
import tn.esprit.examen.nomPrenomClasseExamen.jwt.JwtUtils;

@Service
@RequiredArgsConstructor
public class SubscriberServices implements ISubscriberServices {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final SubscriberRepository subscriberRepository;

    public Subscriber registerSubscriber(SignupRequestDto request) {
        validateRoleFields(request);

        Subscriber subscriber = new Subscriber();
        subscriber.setRole(request.getRole());
        subscriber.setFirstName(request.getFirstName());
        subscriber.setLastName(request.getLastName()); // Correction ici
        subscriber.setEmail(request.getEmail());
        subscriber.setPassword(passwordEncoder.encode(request.getPassword()));
        subscriber.setJob(request.getJob()); // Correction ici
        subscriber.setTelephone(request.getTelephone()); // Correction ici
        subscriber.setIsBanned(false); // Pour éviter les valeurs nulles

        // Set role-specific fields
        switch (request.getRole()) {
            case VOLUNTEER -> subscriber.setSkills(request.getSkills());
            case REFUGEE -> subscriber.setNationality(request.getNationality());
            case MENTOR -> subscriber.setExpertiseArea(request.getExpertiseArea());
            case ASSOCIATION_MEMBER -> subscriber.setAssociationRole(request.getAssociationRole());
        }

        return subscriberRepository.save(subscriber);
    }

    private void validateRoleFields(SignupRequestDto request) {
        switch (request.getRole()) {
            case VOLUNTEER -> {
                if (request.getSkills() == null)
                    throw new IllegalArgumentException("Skills required for volunteers");
            }
            case REFUGEE -> {
                if (request.getNationality() == null)
                    throw new IllegalArgumentException("Nationality required for refugees");
            }
            case MENTOR -> {
                if (request.getExpertiseArea() == null)
                    throw new IllegalArgumentException("Expertise is required for mentors");
            }
            case ASSOCIATION_MEMBER -> {
                if (request.getAssociationRole() == null)
                    throw new IllegalArgumentException("Role of the association is required ");
            }
        }
    }
}