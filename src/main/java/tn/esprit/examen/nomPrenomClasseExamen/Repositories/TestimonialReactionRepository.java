package tn.esprit.examen.nomPrenomClasseExamen.Repositories;



import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Testimonial;
import tn.esprit.examen.nomPrenomClasseExamen.entities.TestimonialReaction;
import tn.esprit.examen.nomPrenomClasseExamen.entities.User;

import java.util.List;
import java.util.Optional;

public interface TestimonialReactionRepository extends JpaRepository<TestimonialReaction, Long> {

    Optional<TestimonialReaction> findByUserAndTestimonial(User user, Testimonial testimonial);

    boolean existsByUserAndTestimonial(User user, Testimonial testimonial); // ✅ pour savoir si l'utilisateur a liké

    Long countByTestimonial(Testimonial testimonial); // ✅ pour compter les likes

    void deleteAllByTestimonialId(Long testimonialId); // ✅ pour pouvoir supprimer les réactions en cascade
}
