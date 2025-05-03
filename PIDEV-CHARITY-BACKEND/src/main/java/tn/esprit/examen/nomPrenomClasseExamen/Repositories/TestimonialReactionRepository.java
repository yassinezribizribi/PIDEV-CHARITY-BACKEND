package tn.esprit.examen.nomPrenomClasseExamen.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Testimonial;
import tn.esprit.examen.nomPrenomClasseExamen.entities.TestimonialReaction;
import tn.esprit.examen.nomPrenomClasseExamen.entities.User;
import tn.esprit.examen.nomPrenomClasseExamen.entities.ReactionType;

import java.util.Optional;

public interface TestimonialReactionRepository extends JpaRepository<TestimonialReaction, Long> {
    Optional<TestimonialReaction> findByUserAndTestimonial(User user, Testimonial testimonial);
    long countByTestimonial(Testimonial testimonial);
    long countByTestimonialAndType(Testimonial testimonial, ReactionType type);

    boolean existsByUserAndTestimonial(User user, Testimonial testimonial);
    void deleteAllByTestimonialId(Long testimonialId);
}
