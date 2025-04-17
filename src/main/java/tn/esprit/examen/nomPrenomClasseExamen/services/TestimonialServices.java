package tn.esprit.examen.nomPrenomClasseExamen.services;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.TestimonialReactionRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.TestimonialRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.UserRepository;
import tn.esprit.examen.nomPrenomClasseExamen.dto.TestimonialDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Testimonial;
import tn.esprit.examen.nomPrenomClasseExamen.entities.TestimonialReaction;
import tn.esprit.examen.nomPrenomClasseExamen.entities.User;
import tn.esprit.examen.nomPrenomClasseExamen.jwt.JwtUtils;

import java.util.*;
import java.util.stream.Collectors;

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
        return java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                .replaceAll("[^a-zA-Z0-9\\s]", "")
                .toLowerCase()
                .trim();
    }

    private boolean containsApproximate(String text, String keyword) {
        if (text.contains(keyword)) return true;
        String[] words = text.split("\\s+");
        for (String word : words) {
            if (levenshtein(word, keyword) <= 2) return true;
        }
        return false;
    }

    @Override
    public void likeTestimonial(Long testimonialId, String token) {
        Long userId = jwtUtils.getUserIdFromJwtToken(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        Testimonial testimonial = testimonialRepository.findById(testimonialId)
                .orElseThrow(() -> new RuntimeException("Témoignage introuvable"));

        Optional<TestimonialReaction> existing = reactionRepo.findByUserAndTestimonial(user, testimonial);

        if (existing.isPresent()) {
            reactionRepo.delete(existing.get());
        } else {
            TestimonialReaction reaction = new TestimonialReaction();
            reaction.setLiked(true);
            reaction.setUser(user);
            reaction.setTestimonial(testimonial);
            reactionRepo.save(reaction);
        }
    }

    private int levenshtein(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) {
            for (int j = 0; j <= b.length(); j++) {
                if (i == 0) dp[i][j] = j;
                else if (j == 0) dp[i][j] = i;
                else {
                    dp[i][j] = Math.min(dp[i - 1][j - 1] + costOfSubstitution(a.charAt(i - 1), b.charAt(j - 1)),
                            Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1));
                }
            }
        }
        return dp[a.length()][b.length()];
    }

    private int costOfSubstitution(char a, char b) {
        return a == b ? 0 : 1;
    }

    private String generateSummary(String content) {
        if (content == null || content.length() < 100) return content;

        String[] sentences = content.split("(?<=[.!?])\\s+");
        Map<String, Integer> wordFreq = new HashMap<>();

        for (String s : sentences) {
            for (String word : normalize(s).split("\\s+")) {
                if (word.length() > 3) {
                    wordFreq.put(word, wordFreq.getOrDefault(word, 0) + 1);
                }
            }
        }

        return Arrays.stream(sentences)
                .sorted((s1, s2) -> scoreSentence(s2, wordFreq) - scoreSentence(s1, wordFreq))
                .limit(2)
                .collect(Collectors.joining(" "));
    }

    private int scoreSentence(String sentence, Map<String, Integer> freq) {
        return Arrays.stream(normalize(sentence).split("\\s+"))
                .mapToInt(w -> freq.getOrDefault(w, 0))
                .sum();
    }

    @Override
    public List<TestimonialDTO> getAllTestimonials(String token) {
        Long currentUserId = null;
        try {
            currentUserId = jwtUtils.getCurrentUserId();
        } catch (Exception e) {
            logger.warn("\u26a0\ufe0f Impossible d'extraire l'utilisateur courant : {}", e.getMessage());
        }

        Long finalCurrentUserId = currentUserId;
        return testimonialRepository.findAll()
                .stream()
                .map(t -> convertToDto(t, finalCurrentUserId))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<TestimonialDTO> getTestimonialById(Long id) {
        return testimonialRepository.findById(id).map(t -> convertToDto(t, null));
    }

    @Override
    public List<TestimonialDTO> getTestimonialsByUserId(String token) {
        Long userId = jwtUtils.getUserIdFromJwtToken(token);
        return testimonialRepository.findByUser_IdUser(userId)
                .stream()
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
        Testimonial existingTestimonial = testimonialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Témoignage non trouvé : " + id));

        existingTestimonial.setContent(dto.getContent());
        existingTestimonial.setDescription(dto.getDescription());
        existingTestimonial.setBeforePhotoPath(dto.getBeforePhotoPath());
        existingTestimonial.setAfterPhotoPath(dto.getAfterPhotoPath());
        existingTestimonial.setMediaPath(dto.getMediaPath());
        existingTestimonial.setMediaType(dto.getMediaType());
        existingTestimonial.setTranscriptionText(dto.getTranscriptionText());
        existingTestimonial.setDominantEmotion(dto.getDominantEmotion());

        return convertToDto(testimonialRepository.save(existingTestimonial), null);
    }

    @Transactional
    @Override
    public void deleteTestimonial(Long id, String token) {
        reactionRepo.deleteAllByTestimonialId(id);
        testimonialRepository.deleteById(id);
    }

    @Override
    public List<TestimonialDTO> searchTestimonials(String keyword) {
        String normalizedKeyword = normalize(keyword);
        return testimonialRepository.findAll().stream()
                .filter(t -> containsApproximate(normalize(t.getContent()), normalizedKeyword)
                        || containsApproximate(normalize(t.getDescription()), normalizedKeyword))
                .map(t -> convertToDto(t, null))
                .collect(Collectors.toList());
    }

    private TestimonialDTO convertToDto(Testimonial t, Long currentUserId) {
        String baseUrl = "http://localhost:8089/api/testimonials/images/";

        TestimonialDTO dto = new TestimonialDTO();
        dto.setId(t.getId());
        dto.setContent(t.getContent());
        dto.setDescription(t.getDescription());
        dto.setUserId(t.getUser() != null ? t.getUser().getIdUser() : null);
        dto.setBeforePhotoPath(t.getBeforePhotoPath() != null ? baseUrl + t.getBeforePhotoPath() : null);
        dto.setAfterPhotoPath(t.getAfterPhotoPath() != null ? baseUrl + t.getAfterPhotoPath() : null);
        dto.setMediaPath(t.getMediaPath());
        dto.setMediaType(t.getMediaType());
        dto.setTranscriptionText(t.getTranscriptionText());
        dto.setLikeCount(reactionRepo.countByTestimonial(t));
        dto.setSummary(generateSummary(t.getContent()));
        dto.setDominantEmotion(t.getDominantEmotion());

        try {
            if (currentUserId != null) {
                User currentUser = userRepository.findById(currentUserId).orElse(null);
                if (currentUser != null) {
                    dto.setLikedByCurrentUser(reactionRepo.existsByUserAndTestimonial(currentUser, t));
                }
            } else {
                dto.setLikedByCurrentUser(false);
            }
        } catch (Exception e) {
            dto.setLikedByCurrentUser(false);
        }

        return dto;
    }

    private Testimonial convertToEntity(TestimonialDTO dto) {
        Testimonial testimonial = new Testimonial();
        testimonial.setContent(dto.getContent());
        testimonial.setDescription(dto.getDescription());
        testimonial.setBeforePhotoPath(dto.getBeforePhotoPath());
        testimonial.setAfterPhotoPath(dto.getAfterPhotoPath());
        testimonial.setMediaPath(dto.getMediaPath());
        testimonial.setMediaType(dto.getMediaType());
        testimonial.setTranscriptionText(dto.getTranscriptionText());
        testimonial.setDominantEmotion(dto.getDominantEmotion());
        return testimonial;
    }
}
