package tn.esprit.examen.nomPrenomClasseExamen.Controllers;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.nomPrenomClasseExamen.dto.TestimonialDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Testimonial;
import tn.esprit.examen.nomPrenomClasseExamen.services.ITestimonialServices;

import java.util.List;
import java.util.Optional;

@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/testimonials")
public class TestimonialController {

    @Autowired
    private ITestimonialServices testimonialService;

    @PostMapping("/add")
    public ResponseEntity<Testimonial> createTestimonial(@Valid @RequestBody TestimonialDTO testimonialDTO) {
        Testimonial testimonial = testimonialService.addTestimonial(testimonialDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(testimonial);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Testimonial> updateTestimonial(@PathVariable Long id, @Valid @RequestBody TestimonialDTO testimonialDTO) {
        Testimonial updatedTestimonial = testimonialService.updateTestimonial(id, testimonialDTO);
        return ResponseEntity.ok(updatedTestimonial);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTestimonial(@PathVariable Long id) {
        testimonialService.deleteTestimonial(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Testimonial> getTestimonialById(@PathVariable Long id) {
        Optional<Testimonial> testimonial = testimonialService.getTestimonialById(id);
        return testimonial.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Testimonial>> getAllTestimonials() {
        return ResponseEntity.ok(testimonialService.getAllTestimonials());
    }
}
