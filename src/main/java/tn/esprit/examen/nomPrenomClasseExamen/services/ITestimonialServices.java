package tn.esprit.examen.nomPrenomClasseExamen.services;

import tn.esprit.examen.nomPrenomClasseExamen.dto.TestimonialDTO;

import java.util.List;
import java.util.Optional;

public interface ITestimonialServices {

    void likeTestimonial(Long testimonialId, String token);
    List<TestimonialDTO> getAllTestimonials(String token);
    Optional<TestimonialDTO> getTestimonialById(Long id);
    List<TestimonialDTO> getTestimonialsByUserId(String token);
    TestimonialDTO addTestimonial(TestimonialDTO dto, String token);
    TestimonialDTO updateTestimonial(Long id, TestimonialDTO dto, String token);
    void deleteTestimonial(Long id, String token);
    List<TestimonialDTO> searchTestimonials(String keyword);
}
