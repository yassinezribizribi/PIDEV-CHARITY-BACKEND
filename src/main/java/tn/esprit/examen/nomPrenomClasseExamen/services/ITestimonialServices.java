package tn.esprit.examen.nomPrenomClasseExamen.services;

import tn.esprit.examen.nomPrenomClasseExamen.dto.TestimonialDTO;
import java.util.List;
import java.util.Optional;

public interface ITestimonialServices {

    List<TestimonialDTO> getAllTestimonials();
    Optional<TestimonialDTO> getTestimonialById(Long id);
    TestimonialDTO addTestimonial(TestimonialDTO testimonialDTO, String token);
    TestimonialDTO updateTestimonial(Long id, TestimonialDTO testimonialDTO, String token);
    void deleteTestimonial(Long id, String token);
    List<TestimonialDTO> getTestimonialsByUserId(String token);

    // ✅ Ajout de la méthode de recherche
    List<TestimonialDTO> searchTestimonials(String keyword);
}
