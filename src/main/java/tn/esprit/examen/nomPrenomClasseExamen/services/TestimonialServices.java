package tn.esprit.examen.nomPrenomClasseExamen.services;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.TestimonialRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.UserRepository;
import tn.esprit.examen.nomPrenomClasseExamen.dto.TestimonialDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Testimonial;
import tn.esprit.examen.nomPrenomClasseExamen.entities.User;
import tn.esprit.examen.nomPrenomClasseExamen.jwt.JwtUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TestimonialServices implements ITestimonialServices {

    private static final Logger logger = LoggerFactory.getLogger(TestimonialServices.class);
    private final TestimonialRepository testimonialRepository;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;

    @Override
    public List<TestimonialDTO> getAllTestimonials() {
        return testimonialRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    @Override
    public List<TestimonialDTO> getTestimonialsByUserId(String token) {
        Long userId = jwtUtils.getUserIdFromJwtToken(token);
        logger.info("🔍 Recherche des témoignages pour l'utilisateur ID: {}", userId);

        return testimonialRepository.findByUser_IdUser(userId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<TestimonialDTO> getTestimonialById(Long id) {
        return testimonialRepository.findById(id)
                .map(this::convertToDto);
    }

    @Override
    public TestimonialDTO addTestimonial(TestimonialDTO testimonialDTO, String token) {
        Long userId = jwtUtils.getUserIdFromJwtToken(token);
        logger.info("🔍 Vérification de l'utilisateur ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID : " + userId));

        Testimonial testimonial = convertToEntity(testimonialDTO);
        testimonial.setUser(user);

        Testimonial savedTestimonial = testimonialRepository.save(testimonial);
        logger.info("✅ Témoignage ajouté avec succès (ID: {})", savedTestimonial.getId());

        return convertToDto(savedTestimonial);
    }

    @Override
    public TestimonialDTO updateTestimonial(Long id, TestimonialDTO testimonialDTO, String token) {
        Long userId = jwtUtils.getUserIdFromJwtToken(token);
        logger.info("🛠️ Mise à jour du témoignage ID: {} par l'utilisateur ID: {}", id, userId);

        return testimonialRepository.findById(id).map(existingTestimonial -> {
            existingTestimonial.setContent(testimonialDTO.getContent());
            existingTestimonial.setDescription(testimonialDTO.getDescription());

            Testimonial updatedTestimonial = testimonialRepository.save(existingTestimonial);
            logger.info("✅ Témoignage ID {} mis à jour avec succès", id);
            return convertToDto(updatedTestimonial);
        }).orElseThrow(() -> new RuntimeException("Témoignage non trouvé avec ID " + id));
    }

    @Override
    public void deleteTestimonial(Long id, String token) {
        logger.info("🗑️ Suppression du témoignage ID: {}", id);
        testimonialRepository.deleteById(id);
        logger.info("✅ Témoignage ID {} supprimé avec succès", id);
    }

    @Override
    public List<TestimonialDTO> searchTestimonials(String keyword) {
        return testimonialRepository.findByContentContainingIgnoreCase(keyword)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private TestimonialDTO convertToDto(Testimonial testimonial) {
        TestimonialDTO dto = new TestimonialDTO();
        dto.setContent(testimonial.getContent());
        dto.setDescription(testimonial.getDescription());
        dto.setBeforePhotoPath(testimonial.getBeforePhotoPath());
        dto.setAfterPhotoPath(testimonial.getAfterPhotoPath());

        return dto;
    }

    private Testimonial convertToEntity(TestimonialDTO testimonialDTO) {
        Testimonial testimonial = new Testimonial();
        testimonial.setContent(testimonialDTO.getContent());
        testimonial.setDescription(testimonialDTO.getDescription());
        return testimonial;
    }
}
