package tn.esprit.examen.nomPrenomClasseExamen.services;

import tn.esprit.examen.nomPrenomClasseExamen.dto.TestimonialDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Testimonial;

import java.util.List;
import java.util.Optional;

public interface ITestimonialServices {
    Testimonial addTestimonial(TestimonialDTO testimonialDTO);
    Testimonial updateTestimonial(Long id, TestimonialDTO testimonialDTO);
    void deleteTestimonial(Long id);
    Optional<Testimonial> getTestimonialById(Long id);
    List<Testimonial> getAllTestimonials();
}
