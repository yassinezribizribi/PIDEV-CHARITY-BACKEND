package tn.esprit.examen.nomPrenomClasseExamen.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.examen.nomPrenomClasseExamen.dto.TestimonialDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Testimonial;
import tn.esprit.examen.nomPrenomClasseExamen.entities.User;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.TestimonialRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class TestimonialServices implements ITestimonialServices {

    @Autowired
    private TestimonialRepository testimonialRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Testimonial addTestimonial(TestimonialDTO testimonialDTO) {
        User user = userRepository.findById(testimonialDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Testimonial testimonial = new Testimonial();
        testimonial.setContent(testimonialDTO.getContent());
        testimonial.setBeforePhoto(testimonialDTO.getBeforePhoto());
        testimonial.setAfterPhoto(testimonialDTO.getAfterPhoto());
        testimonial.setDescription(testimonialDTO.getDescription());
        testimonial.setUser(user);

        return testimonialRepository.save(testimonial);
    }

    @Override
    public Testimonial updateTestimonial(Long id, TestimonialDTO testimonialDTO) {
        Testimonial testimonial = testimonialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Témoignage non trouvé"));

        testimonial.setContent(testimonialDTO.getContent());
        testimonial.setBeforePhoto(testimonialDTO.getBeforePhoto());
        testimonial.setAfterPhoto(testimonialDTO.getAfterPhoto());
        testimonial.setDescription(testimonialDTO.getDescription());

        return testimonialRepository.save(testimonial);
    }

    @Override
    public void deleteTestimonial(Long id) {
        if (!testimonialRepository.existsById(id)) {
            throw new RuntimeException("Témoignage non trouvé");
        }
        testimonialRepository.deleteById(id);
    }

    @Override
    public Optional<Testimonial> getTestimonialById(Long id) {
        return testimonialRepository.findById(id);
    }

    @Override
    public List<Testimonial> getAllTestimonials() {
        return testimonialRepository.findAll();
    }
}
