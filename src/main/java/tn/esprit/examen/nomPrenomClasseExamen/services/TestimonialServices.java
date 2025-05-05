// src/main/java/tn/esprit/examen/nomPrenomClasseExamen/services/TestimonialServices.java
package tn.esprit.examen.nomPrenomClasseExamen.services;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.TestimonialReactionRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.TestimonialRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.UserRepository;
import tn.esprit.examen.nomPrenomClasseExamen.dto.TestimonialDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.ReactionType;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Testimonial;
import tn.esprit.examen.nomPrenomClasseExamen.entities.TestimonialReaction;
import tn.esprit.examen.nomPrenomClasseExamen.entities.User;
import tn.esprit.examen.nomPrenomClasseExamen.jwt.JwtUtils;

import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;
import java.nio.file.Paths;

@Service
@AllArgsConstructor
public class TestimonialServices implements ITestimonialServices {

    private final TestimonialRepository testimonialRepository;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final TestimonialReactionRepository reactionRepo;
    private static final Logger logger = LoggerFactory.getLogger(TestimonialServices.class);

    private String normalize(String text) {
        if (text == null) return "";
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                .replaceAll("[^a-zA-Z0-9\\s]", "")
                .toLowerCase()
                .trim();
    }

    private boolean containsApproximate(String text, String keyword) {
        if (text.contains(keyword)) return true;
        for (String word : text.split("\\s+")) {
            if (levenshtein(word, keyword) <= 2) return true;
        }
        return false;
    }


    private int levenshtein(String a, String b) {
        int[][] dp = new int[a.length()+1][b.length()+1];
        for (int i = 0; i <= a.length(); i++) {
            for (int j = 0; j <= b.length(); j++) {
                if (i == 0) dp[i][j] = j;
                else if (j == 0) dp[i][j] = i;
                else {
                    dp[i][j] = Math.min(
                            dp[i-1][j-1] + (a.charAt(i-1) == b.charAt(j-1) ? 0 : 1),
                            Math.min(dp[i-1][j] + 1, dp[i][j-1] + 1)
                    );
                }
            }
        }
        return dp[a.length()][b.length()];
    }

    private String generateSummary(String content) {
        if (content == null || content.length() < 100) return content;
        String[] sentences = content.split("(?<=[.!?])\\s+");
        Map<String,Integer> freq = new HashMap<>();
        for (String s : sentences) {
            for (String word : normalize(s).split("\\s+")) {
                if (word.length() > 3) freq.put(word, freq.getOrDefault(word, 0) + 1);
            }
        }
        return Arrays.stream(sentences)
                .sorted((s1, s2) -> scoreSentence(s2, freq) - scoreSentence(s1, freq))
                .limit(2)
                .collect(Collectors.joining(" "));
    }

    private int scoreSentence(String s, Map<String,Integer> freq) {
        return Arrays.stream(normalize(s).split("\\s+"))
                .mapToInt(w -> freq.getOrDefault(w, 0))
                .sum();
    }

    @Override
    public void likeTestimonial(Long testimonialId, String token) {
        saveReaction(testimonialId, token, ReactionType.LIKE);
    }

    @Override
    public void unlikeTestimonial(Long testimonialId, String token) {
        saveReaction(testimonialId, token, ReactionType.UNLIKE);
    }

    private void saveReaction(Long testimonialId, String token, ReactionType type) {
        Long userId = jwtUtils.getUserIdFromJwtToken(token);
        User user = userRepository.findById(userId).orElseThrow();
        Testimonial testimonial = testimonialRepository.findById(testimonialId).orElseThrow();

        Optional<TestimonialReaction> existing = reactionRepo.findByUserAndTestimonial(user, testimonial);
        existing.ifPresent(reactionRepo::delete); // Supprimer l'ancienne réaction

        TestimonialReaction reaction = new TestimonialReaction();
        reaction.setUser(user);
        reaction.setTestimonial(testimonial);
        reaction.setType(type);
        reactionRepo.save(reaction);
    }

    @Override
    public List<TestimonialDTO> getAllTestimonials(String token) {
        Long currentUserId = null;
        try {
            currentUserId = jwtUtils.getCurrentUserId();
        } catch (Exception e) {
            logger.warn("⚠️ Impossible d'extraire l'utilisateur courant : {}", e.getMessage());
        }
        Long uid = currentUserId;
        return testimonialRepository.findAll().stream()
                .map(t -> convertToDto(t, uid))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<TestimonialDTO> getTestimonialById(Long id) {
        return testimonialRepository.findById(id)
                .map(t -> convertToDto(t, null));
    }

    @Override
    public List<TestimonialDTO> getTestimonialsByUserId(String token) {
        Long userId = jwtUtils.getUserIdFromJwtToken(token);
        return testimonialRepository.findByUser_IdUser(userId).stream()
                .map(t -> convertToDto(t, userId))
                .collect(Collectors.toList());
    }

    @Override
    public TestimonialDTO addTestimonial(TestimonialDTO dto, String token) {
        Long userId = jwtUtils.getUserIdFromJwtToken(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé : " + userId));
        Testimonial entity = convertToEntity(dto);
        entity.setUser(user);
        Testimonial saved = testimonialRepository.save(entity);
        return convertToDto(saved, userId);
    }

    @Override
    public TestimonialDTO updateTestimonial(Long id, TestimonialDTO dto, String token) {
        Long currentUserId = jwtUtils.getUserIdFromJwtToken(token);
        Testimonial existing = testimonialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Témoignage non trouvé : " + id));

        if (existing.getUser() == null || !existing.getUser().getIdUser().equals(currentUserId)) {
            throw new SecurityException("⛔ Vous n'êtes pas autorisé à modifier ce témoignage.");
        }

        existing.setContent(dto.getContent());
        existing.setDescription(dto.getDescription());
        existing.setBeforePhotoPath(dto.getBeforePhotoPath());
        existing.setAfterPhotoPath(dto.getAfterPhotoPath());
        existing.setMediaPath(dto.getMediaPath());
        existing.setMediaType(dto.getMediaType());

        Testimonial saved = testimonialRepository.save(existing);
        return convertToDto(saved, currentUserId);
    }
    @Transactional
    @Override
    public void deleteTestimonial(Long id, String token) {
        Long currentUserId = jwtUtils.getUserIdFromJwtToken(token);
        Testimonial testimonial = testimonialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Témoignage non trouvé : " + id));

        if (testimonial.getUser() == null || !testimonial.getUser().getIdUser().equals(currentUserId)) {
            throw new SecurityException("⛔ Vous ne pouvez supprimer que vos propres témoignages.");
        }

        reactionRepo.deleteAllByTestimonialId(id);
        testimonialRepository.deleteById(id);
    }


    @Override
    public List<TestimonialDTO> searchTestimonials(String keyword) {
        String norm = normalize(keyword);
        return testimonialRepository.findAll().stream()
                .filter(t ->
                        containsApproximate(normalize(t.getContent()), norm) ||
                                containsApproximate(normalize(t.getDescription()), norm)
                )
                .map(t -> convertToDto(t, null))
                .collect(Collectors.toList());
    }

    private TestimonialDTO convertToDto(Testimonial t, Long currentUserId) {
        String imageBaseUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/testimonials/images/")
                .toUriString();
        String mediaBaseUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/testimonials/media/")
                .toUriString();

        TestimonialDTO dto = new TestimonialDTO();
        dto.setId(t.getId());
        dto.setContent(t.getContent());
        dto.setDescription(t.getDescription());
        dto.setUserId(Optional.ofNullable(t.getUser()).map(User::getIdUser).orElse(null));

        if (t.getBeforePhotoPath() != null) {
            dto.setBeforePhotoPath(imageBaseUrl + Paths.get(t.getBeforePhotoPath()).getFileName());
        }
        if (t.getAfterPhotoPath() != null) {
            dto.setAfterPhotoPath(imageBaseUrl + Paths.get(t.getAfterPhotoPath()).getFileName());
        }

        if (t.getMediaPath() != null) {
            String raw = t.getMediaPath();
            if (raw.startsWith("http")) {
                dto.setMediaPath(raw);
            } else {
                dto.setMediaPath(mediaBaseUrl + Paths.get(raw).getFileName());
            }
        }
        dto.setMediaType(t.getMediaType());

        dto.setLikeCount((int) reactionRepo.countByTestimonialAndType(t, ReactionType.LIKE));
        dto.setUnlikeCount((int) reactionRepo.countByTestimonialAndType(t, ReactionType.UNLIKE));


        // likedByCurrentUser
        boolean likedByCurrentUser = false;
        if (currentUserId != null) {
            Optional<User> uOpt = userRepository.findById(currentUserId);
            if (uOpt.isPresent()) {
                likedByCurrentUser = reactionRepo.existsByUserAndTestimonial(uOpt.get(), t);
            }
        }
        dto.setLikedByCurrentUser(likedByCurrentUser);

        return dto;
    }
    @Override
    public long countByTestimonialAndType(Testimonial t, ReactionType type) {
        return reactionRepo.countByTestimonialAndType(t, type);
    }

    private Testimonial convertToEntity(TestimonialDTO dto) {
        Testimonial t = new Testimonial();
        t.setContent(dto.getContent());
        t.setDescription(dto.getDescription());
        t.setBeforePhotoPath(dto.getBeforePhotoPath());
        t.setAfterPhotoPath(dto.getAfterPhotoPath());
        t.setMediaPath(dto.getMediaPath());
        t.setMediaType(dto.getMediaType());
        return t;
    }
}
