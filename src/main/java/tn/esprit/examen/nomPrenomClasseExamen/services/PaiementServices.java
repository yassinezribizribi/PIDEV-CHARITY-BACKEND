package tn.esprit.examen.nomPrenomClasseExamen.services;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.CagnotteEnligneRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.DonationRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.PaiementRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.UserRepository;
import tn.esprit.examen.nomPrenomClasseExamen.dto.DonationSuggestionDTO;
import tn.esprit.examen.nomPrenomClasseExamen.dto.PaiementDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.CagnotteEnligne;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Donation;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Paiement;
import tn.esprit.examen.nomPrenomClasseExamen.jwt.JwtUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaiementServices {

    private final PaiementRepository paiementRepository;
    private final StripeServices stripeServices;
    private final CagnotteEnligneRepository cagnotteEnligneRepository;
    private final JwtUtils jwtUtils;
    private final UserRepository utilisateurRepository;
    @Autowired
    private DonationRepository donationRepository;
    private static final Logger logger = LoggerFactory.getLogger(PaiementServices.class);

    // Create the payment, update the cagnotte, and save both to the database
    public Paiement createPaiement(PaiementDTO paiementDTO, Long amount, Long cagnotteId, String jwtToken) throws StripeException {
        try {
            // 1. Create PaymentIntent with Stripe
            PaymentIntent paymentIntent = stripeServices.createPaymentIntent(amount, "eur");

            // 2. Convert DTO to Entity
            Paiement paiement = new Paiement();
            paiement.setDatePaiement(new Date());
            paiement.setMontant(amount.intValue());
            paiement.setStripePaymentId(paymentIntent.getId());
            paiement.setStatus(paymentIntent.getStatus());
            paiement.setCurrency(paymentIntent.getCurrency());

            // 3. Set Cagnotte
            CagnotteEnligne cagnotte = cagnotteEnligneRepository.findById(cagnotteId)
                    .orElseThrow(() -> new RuntimeException("Cagnotte not found"));
            paiement.setCagnotte(cagnotte);

            // 4. Extract userId from JWT token and set utilisateur
            if (jwtToken != null && !jwtToken.trim().isEmpty()) {
                try {
                    Long userId = jwtUtils.getUserIdFromJwtToken(jwtToken);
                    logger.info("🔍 User ID extracted from JWT: {}", userId);
                    utilisateurRepository.findById(userId).ifPresentOrElse(
                            paiement::setUtilisateur,
                            () -> paiement.setUtilisateur(null)
                    );
                } catch (Exception ex) {
                    logger.warn("⚠️ Could not extract user from token: {}", ex.getMessage());
                    paiement.setUtilisateur(null);
                }
            } else {
                paiement.setUtilisateur(null);
            }

            // 5. Save paiement
            Paiement savedPaiement = paiementRepository.save(paiement);

            // 6. Update cagnotte amount
            cagnotte.setCurrentAmount(cagnotte.getCurrentAmount() + amount);
            cagnotteEnligneRepository.save(cagnotte);

            logger.info("✅ Paiement successfully created with ID: {}", savedPaiement.getIdPaiement());
            return savedPaiement;

        } catch (Exception e) {
            logger.error("❌ Error while creating paiement: {}", e.getMessage(), e);
            throw new RuntimeException("Error while creating paiement: " + e.getMessage());
        }
    }


    // Process Stripe Payment without creating Paiement, if needed
    public PaymentIntent processStripePayment(Long amount) throws StripeException {
        return stripeServices.createPaymentIntent(amount, "usd"); // or "eur" / "tnd"
    }

    //    public DonationSuggestionDTO getSuggestions(Long cagnotteId, Integer userInput) {
//        Double avg = paiementRepository.findAverageByCagnotteId(cagnotteId);
//        Double todayAvg = paiementRepository.findTodayAverageByCagnotteId(cagnotteId);
//
//        // Round to nearest 5
//        int roundedSuggestion = (int) (Math.ceil((double) userInput / 5) * 5);
//
//        // Convert averages to rounded values if they exist
//        Long roundedAvg = avg != null ? Math.round(avg) : null;
//        Long roundedTodayAvg = todayAvg != null ? Math.round(todayAvg) : null;
//
//        // Check if userInput is greater than or equal to all values
//        boolean isGenerous =
//                (roundedAvg == null || userInput >= roundedAvg) &&
//                        (roundedTodayAvg == null || userInput >= roundedTodayAvg) &&
//                        userInput >= roundedSuggestion;
//
//        if (isGenerous) {
//            return null; // User is already donating enough, no suggestions needed
//        }
//
//        return new DonationSuggestionDTO(
//                roundedAvg,
//                roundedTodayAvg,
//                roundedSuggestion
//        );
//    }
//    public List<Integer> getWheelSuggestedAmounts(Long cagnotteId, Integer userInput) {
//        CagnotteEnligne cagnotte = cagnotteEnligneRepository.findById(cagnotteId)
//                .orElseThrow(() -> new RuntimeException("Cagnotte not found"));
//
//        double goal = cagnotte.getGoalAmount();
//        double collected = cagnotte.getCurrentAmount();
//        double remaining = goal - collected;
//
//        if (remaining <= 0) {
//            return Collections.emptyList();
//        }
//
//        double progress = collected / goal;
//
//        System.out.println("Progress: " + progress);
//        System.out.println("Remaining: " + remaining);
//
//        List<Integer> symbolic = Arrays.asList(7, 11, 25, 33, 50, 66, 77, 88, 100, 111, 150, 222, 250, 333, 444, 500, 777);
//        Set<Integer> suggestionSet = new TreeSet<>();
//
//        int min = 5;
//        int max;
//
//        if (userInput != null && userInput > 0) {
//            // CASE 1: With user input (modified logic)
//            int input = userInput;
//            max = (int) Math.min(remaining, input + input / 2.0);
//            min = Math.max(5, input - input / 2); // Lower bound for below-input values
//
//            Set<Integer> lessThanInput = new TreeSet<>();
//            Set<Integer> moreThanInput = new TreeSet<>();
//
//            // Add symbolic values in each category
//            for (Integer val : symbolic) {
//                if (val < input && val >= min) {
//                    lessThanInput.add(val);
//                } else if (val >= input && val <= max) {
//                    moreThanInput.add(val);
//                }
//            }
//
//            // Fill both sets with randoms if not enough symbolic
//            while (lessThanInput.size() < 2) {
//                int rand = ThreadLocalRandom.current().nextInt(min, input);
//                rand = (int) (Math.round(rand / 5.0) * 5);
//                lessThanInput.add(rand);
//            }
//
//            while (moreThanInput.size() < 4) {
//                int rand = ThreadLocalRandom.current().nextInt(input, max + 1);
//                rand = (int) (Math.round(rand / 5.0) * 5);
//                moreThanInput.add(rand);
//            }
//
//            suggestionSet.addAll(lessThanInput);
//            suggestionSet.addAll(moreThanInput);
//
//        } else {
//            // CASE 2: Based on progress (unchanged)
//            if (progress < 0.1) {
//                max = (int) Math.min(remaining, 75);
//                suggestionSet.addAll(Arrays.asList(5, 10, 15, 20, 25, 33, 50));
//            } else if (progress < 0.5) {
//                max = (int) Math.min(remaining, 150);
//                List<Integer> filteredSymbolic = Arrays.asList(50, 66, 77, 88, 100, 111, 150);
//                suggestionSet.addAll(filteredSymbolic);
//            } else {
//                max = (int) Math.min(remaining, 333);
//                List<Integer> filteredSymbolic = Arrays.asList(50, 100, 150, 222, 250, 333);
//                suggestionSet.addAll(filteredSymbolic);
//            }
//
//            while (suggestionSet.size() < 7) {
//                int rand = ThreadLocalRandom.current().nextInt(min, max + 1);
//                rand = (int) (Math.round(rand / 5.0) * 5);
//                suggestionSet.add(rand);
//            }
//        }
//
//        // Shuffle for variety, then limit and sort
//        List<Integer> finalList = new ArrayList<>(suggestionSet);
//        Collections.shuffle(finalList);
//
//        return finalList.stream()
//                .limit(userInput != null && userInput > 0 ? 6 : 7)
//                .sorted()
//                .collect(Collectors.toList());
//    }



//    // --- Helper 1: Check if donation is urgent or close to deadline
//    private boolean isUrgentOrCloseDeadline(Donation donation) {
//        if (donation.getPriority() == 4) return true; // urgent priority
//        if (donation.getEndDate() == null) return false;
//
//// --- Helper 2: Suggest generous amount based on urgency
//        private Integer suggestUrgentAmount(CagnotteEnligne cagnotte, Donation donation) {
//            float remaining = cagnotte.getGoalAmount() - cagnotte.getCurrentAmount();
//            if (remaining <= 0) return 0;
//
//            long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), donation.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
//            if (daysLeft <= 0) daysLeft = 1;
//
//            float base = remaining / daysLeft;
//            int rounded = (int) (Math.ceil(base / 10) * 10); // Round to nearest 10
//            return rounded;
//
//    // --- Helper 3: Suggest based on last donation to this cagnotte
//    private Integer getLastDonationAmountFromUserToThisCagnotte(Long userId, Long cagnotteId) {
//        Paiement lastPaiement = paiementRepository.findTopByUserIdAndCagnotteIdOrderByDateDesc(userId, cagnotteId);
//        if (lastPaiement != null) {
//            return lastPaiement.getMontant();
//// --- Helper 5: Handle when user starts typing
//            private Integer suggestBasedOnInput(Long cagnotteId, Integer userInput) {
//                Double avg = paiementRepository.findAverageByCagnotteId(cagnotteId);
//                Double todayAvg = paiementRepository.findTodayAverageByCagnotteId(cagnotteId);
//
//                int roundedSuggestion = (int) (Math.ceil((double) userInput / 5) * 5);
//                Long roundedAvg = avg != null ? Math.round(avg) : null;
//                Long roundedTodayAvg = todayAvg != null ? Math.round(todayAvg) : null;
//
//                boolean isGenerous = (roundedAvg == null || userInput >= roundedAvg)
//                        && (roundedTodayAvg == null || userInput >= roundedTodayAvg)
//                        && userInput >= roundedSuggestion;
//
//                if (isGenerous) return null;
//                return roundedSuggestion;
//            }
//


//
//    public Map<String, Object> getSuggestionsForRegisteredUser(String jwtToken, Long cagnotteId, Integer userInput) {
//        Long userId = null;
//
//        // Step 1: Extract user ID from JWT Token
//        if (jwtToken != null && !jwtToken.trim().isEmpty()) {
//            try {
//                userId = jwtUtils.getUserIdFromJwtToken(jwtToken);
//                logger.info("🔐 User ID extracted from JWT: {}", userId);
//            } catch (Exception ex) {
//                logger.warn("⚠️ Could not extract user from token: {}", ex.getMessage());
//                return null; // Or handle anonymously if needed
//            }
//        }
//
//        // Step 2: Get Cagnotte
//        CagnotteEnligne cagnotte = cagnotteEnligneRepository.findById(cagnotteId).orElse(null);
//        if (cagnotte == null || cagnotte.getDonation() == null) return null;
//
//        Donation donation = cagnotte.getDonation();
//        Map<String, Object> response = new HashMap<>();
//
//        // Case 1: No user input yet
//        if (userInput == null) {
//            if (isUrgentOrCloseDeadline(donation)) {
//                Integer urgentSuggestion = suggestUrgentAmount(cagnotte, donation, userId);
//                response.put("amount", urgentSuggestion);
//                response.put("message", "Only a few days left! Your generous donation can make a big difference now.");
//            } else {
//                Integer lastDonation = getLastDonationAmountFromUserToThisCagnotte(userId, cagnotteId);
//                if (lastDonation != null) {
//                    response.put("amount", lastDonation);
//                    response.put("message", "You gave " + lastDonation + " TND last time — want to do it again?");
//                } else {
//                    Integer globalAvg = getUserGlobalAverageDonation(userId);
//                    response.put("amount", globalAvg);
//                    response.put("message", "Your average donation is " + globalAvg + " TND — that would go a long way here too.");
//                }
//            }
//        }
//        // Case 2: User is typing
//        else {
//            Integer suggestion = suggestBasedOnInput(cagnotteId, userInput);
//            if (suggestion != null) {
//                response.put("amount", suggestion);
//                response.put("message", "Want to round up your gift to " + suggestion + "? You're almost there!");
//            }
//        }
//
//        return response;
//    }
//
//    // --- Helper 1: Check if donation is urgent or close to deadline
//        private boolean isUrgentOrCloseDeadline(Donation donation) {
//            if (donation.getPriority() == 4) return true;
//            if (donation.getEndDate() == null) return false;
//            long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(),
//                    donation.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
//            return daysLeft <= 5;
//        }
//
//        // --- Helper 2: Suggest generous amount based on urgency
//        private Integer suggestUrgentAmount(CagnotteEnligne cagnotte, Donation donation, Long userId) {
//            float remaining = cagnotte.getGoalAmount() - cagnotte.getCurrentAmount();
//            if (remaining <= 0) return 0;
//
//            long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(),
//                    donation.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
//            if (daysLeft <= 0) daysLeft = 1;
//
//            float baseUrgent = remaining / daysLeft;
//
//            // 📊 Get user's max donation ever made (optional: or recent average)
//            Integer userMax = paiementRepository.findMaxDonationByUserId(userId);
//            if (userMax == null) userMax = 0;
//
//            // 🧠 Logic: if user is known to be generous and remaining is big, suggest close to their max
//            float suggestion = Math.min(baseUrgent * 1.5f, userMax); // don’t go above their historical max
//            return (int) (Math.ceil(suggestion / 10) * 10); // Round up
//        }
//
//
//    // --- Helper 3: Last donation from this user to this cagnotte
//        private Integer getLastDonationAmountFromUserToThisCagnotte(Long userId, Long cagnotteId) {
//            Paiement last = paiementRepository.findTopByUtilisateur_IdUserAndCagnotte_IdCagnotteOrderByDatePaiementDesc(userId, cagnotteId);
//            return last != null ? last.getMontant() : null;
//        }
//
//        // --- Helper 4: User's average donation across all cagnottes
//        private Integer getUserGlobalAverageDonation(Long userId) {
//            Double avg = paiementRepository.findAverageByUserId(userId);
//            if (avg == null) return 0;
//            return (int) (Math.ceil(avg / 5) * 5);
//        }
//
//        // --- Helper 5: Suggestion while user is typing
//        private Integer suggestBasedOnInput(Long cagnotteId, Integer userInput) {
//            Double avg = paiementRepository.findAverageByCagnotteId(cagnotteId);
//            Double todayAvg = paiementRepository.findTodayAverageByCagnotteId(cagnotteId);
//
//            int roundedInput = (int) (Math.ceil((double) userInput / 5) * 5);
//            Long roundedAvg = avg != null ? Math.round(avg) : null;
//            Long roundedTodayAvg = todayAvg != null ? Math.round(todayAvg) : null;
//
//            boolean isGenerous = (roundedAvg == null || userInput >= roundedAvg)
//                    && (roundedTodayAvg == null || userInput >= roundedTodayAvg)
//                    && userInput >= roundedInput;
//
//            if (isGenerous) return null;
//            return roundedInput;
//        }
//    }

    public DonationSuggestionDTO getSuggestions(String jwtToken, Long cagnotteId, Integer userInput) {
        if (jwtToken == null || jwtToken.trim().isEmpty()) {
            return suggestBasedOnInput(cagnotteId, userInput);
        } else {
            return getSuggestionsForRegisteredUser(jwtToken, cagnotteId, userInput);
        }
    }

    public DonationSuggestionDTO getSuggestionsForRegisteredUser(String jwtToken, Long cagnotteId, Integer userInput) {
        Long userId = null;

        // 1) Extract user ID
        if (jwtToken != null && !jwtToken.trim().isEmpty()) {
            try {
                userId = jwtUtils.getUserIdFromJwtToken(jwtToken);
                logger.info("🔐 User ID extracted from JWT: {}", userId);
            } catch (Exception ex) {
                logger.warn("⚠️ Could not extract user from token: {}", ex.getMessage());
                return null;
            }
        }

        // 2) Load cagnotte + donation
        CagnotteEnligne cagnotte = cagnotteEnligneRepository.findById(cagnotteId).orElse(null);
        if (cagnotte == null || cagnotte.getDonation() == null) return null;
        Donation donation = cagnotte.getDonation();

        // 3) No user input yet → urgency or history/average
        if (userInput == null) {
            if (isUrgentOrCloseDeadline(donation)) {
                return suggestUrgentAmount(cagnotte, donation, userId);
            } else {
                return suggestFromHistoryOrAverage(userId, cagnotteId);
            }
        }

        // 4) User *is* typing → use “round‑up” logic
        return suggestBasedOnInput(cagnotteId, userInput);
    }

    // --- Urgency check
    private boolean isUrgentOrCloseDeadline(Donation donation) {
        if (donation.getPriority() == 4) return true;
        if (donation.getEndDate() == null) return false;
        long daysLeft = ChronoUnit.DAYS.between(
                LocalDate.now(),
                donation.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        );
        return daysLeft <= 5;
    }

    // --- Case 1A: Urgent / near deadline
    private DonationSuggestionDTO suggestUrgentAmount(CagnotteEnligne cagnotte, Donation donation, Long userId) {
        float remaining = cagnotte.getGoalAmount() - cagnotte.getCurrentAmount();
        if (remaining <= 0) return null;

        long daysLeft = ChronoUnit.DAYS.between(
                LocalDate.now(),
                donation.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        );
        if (daysLeft <= 0) daysLeft = 1;

        float baseUrgent = remaining / daysLeft;
        Integer userMax = paiementRepository.findMaxDonationByUserId(userId);
        if (userMax == null) userMax = 0;

        float raw = Math.min(baseUrgent * 1.5f, userMax);
        int rounded = (int) (Math.ceil(raw / 10) * 10);

        return DonationSuggestionDTO.builder()
                .average(null)
                .todayAverage(null)
                .suggestion(rounded)
                .message("Only a few days left! Your generous donation can make a big difference now.")
                .build();
    }

    // --- Case 1B: No urgency → use last donation or global average
    private DonationSuggestionDTO suggestFromHistoryOrAverage(Long userId, Long cagnotteId) {
        Integer last = getLastDonationAmountFromUserToThisCagnotte(userId, cagnotteId);
        if (last != null) {
            return DonationSuggestionDTO.builder()
                    .average(null)
                    .todayAverage(null)
                    .suggestion(last)
                    .message("You gave " + last + " TND last time — want to do it again?")
                    .build();
        }
        Integer avg = getUserGlobalAverageDonation(userId);
        return DonationSuggestionDTO.builder()
                .average((long) avg)
                .todayAverage(null)
                .suggestion(avg)
                .message("Your average donation is " + avg + " TND — that would go a long way here too.")
                .build();
    }

    private Integer getLastDonationAmountFromUserToThisCagnotte(Long userId, Long cagnotteId) {
        Paiement last = paiementRepository
                .findTopByUtilisateur_IdUserAndCagnotte_IdCagnotteOrderByDatePaiementDesc(userId, cagnotteId);
        return last != null ? last.getMontant() : null;
    }

    private Integer getUserGlobalAverageDonation(Long userId) {
        Double a = paiementRepository.findAverageByUserId(userId);
        if (a == null) return 0;
        return (int) (Math.ceil(a / 5) * 5);
    }

    // --- Case 2: User typing → old `getSuggestions` logic, now DTO
    public DonationSuggestionDTO suggestBasedOnInput(Long cagnotteId, Integer userInput) {
        Double avg = paiementRepository.findAverageByCagnotteId(cagnotteId);
        Double todayAvg = paiementRepository.findTodayAverageByCagnotteId(cagnotteId);

        int rounded = (int) (Math.ceil((double) userInput / 5) * 5);
        Long rAvg = avg != null ? Math.round(avg) : null;
        Long rToday = todayAvg != null ? Math.round(todayAvg) : null;

        boolean isGenerous =
                (rAvg == null || userInput >= rAvg) &&
                        (rToday == null || userInput >= rToday) &&
                        userInput >= rounded;
        if (isGenerous) return null;

        return DonationSuggestionDTO.builder()
                .average(rAvg)
                .todayAverage(rToday)
                .suggestion(rounded)
                .message(null)  // front-end can decide how to word this
                .build();
    }

    public List<Integer> getWheelSuggestedAmounts(String jwtToken, Long cagnotteId, Integer userInput) {
        Long userId = extractUserIdFromToken(jwtToken);
        if (userId == null) return Collections.emptyList();

        CagnotteEnligne cagnotte = cagnotteEnligneRepository.findById(cagnotteId)
                .orElseThrow(() -> new RuntimeException("Cagnotte not found"));

        if (userInput != null && userInput > 0) {
            return generateSuggestionsWithUserInput(cagnotte, userInput);
        } else {
            return generateSuggestionsWithoutInput(cagnotte, userId);
        }
    }
    private Long extractUserIdFromToken(String jwtToken) {
        if (jwtToken == null || jwtToken.trim().isEmpty()) return null;
        try {
            return jwtUtils.getUserIdFromJwtToken(jwtToken);
        } catch (Exception ex) {
            logger.warn("⚠️ Could not extract user from token: {}", ex.getMessage());
            return null;
        }
    }
    private List<Integer> generateSuggestionsWithUserInput(CagnotteEnligne cagnotte, Integer input) {
        double remaining = cagnotte.getGoalAmount() - cagnotte.getCurrentAmount();
        int min = Math.max(5, input - input / 2);
        int max = (int) Math.min(remaining, input + input / 2.0);

        Set<Integer> symbolic = getSymbolicAmounts();
        Set<Integer> lessThan = symbolic.stream().filter(val -> val < input && val >= min).collect(Collectors.toSet());
        Set<Integer> moreThan = symbolic.stream().filter(val -> val >= input && val <= max).collect(Collectors.toSet());

        fillWithRandoms(lessThan, min, input, 2);
        fillWithRandoms(moreThan, input, max + 1, 4);

        Set<Integer> suggestions = new TreeSet<>();
        suggestions.addAll(lessThan);
        suggestions.addAll(moreThan);

        return finalizeSuggestions(suggestions);
    }
    private List<Integer> generateSuggestionsWithoutInput(CagnotteEnligne cagnotte, Long userId) {
        double goal = cagnotte.getGoalAmount();
        double collected = cagnotte.getCurrentAmount();
        double remaining = goal - collected;
        double progress = collected / goal;

        Set<Integer> suggestions = new TreeSet<>();
        Set<Integer> symbolic = getSymbolicAmounts();

        Double avgDonation = paiementRepository.findAverageByUserId(userId);

        if (avgDonation == null) {
            if (progress < 0.1) {
                suggestions.addAll(Arrays.asList(5, 10, 15, 20, 25, 33, 50));
            } else if (progress < 0.5) {
                suggestions.addAll(Arrays.asList(50, 66, 77, 88, 100, 111, 150));
            } else {
                suggestions.addAll(Arrays.asList(50, 100, 150, 222, 250, 333));
            }
        } else {
            double factor = (progress < 0.1) ? 0.75 : (progress < 0.5) ? 1.25 : 1.5;
            double base = avgDonation * factor;

            int min = (int) Math.max(5, base * 0.75);
            int max = (int) Math.min(remaining, base * 1.5);

            for (Integer val : symbolic) {
                if (val >= min && val <= max) suggestions.add(val);
            }

            suggestions.add((int) Math.round(avgDonation));
            suggestions.add((int) Math.round(avgDonation * factor));
            suggestions.add((int) Math.round(avgDonation * 1.1));
        }

        fillWithRandoms(suggestions, 5, (int) remaining, 6 - suggestions.size());

        return finalizeSuggestions(suggestions);
    }
    private Set<Integer> getSymbolicAmounts() {
        return new HashSet<>(Arrays.asList(7, 11, 25, 33, 50, 66, 77, 88, 100, 111, 150, 222, 250, 333, 444, 500, 777));
    }

    private void fillWithRandoms(Set<Integer> set, int min, int max, int targetCount) {
        int attempts = 0;
        int maxAttempts = 50; // Arbitrary safety limit
        while (set.size() < targetCount && max > min && attempts < maxAttempts) {
            int rand = ThreadLocalRandom.current().nextInt(min, max);
            rand = (int) (Math.round(rand / 5.0) * 5);
            set.add(rand);
            attempts++;
        }
    }


    private List<Integer> finalizeSuggestions(Set<Integer> suggestions) {
        List<Integer> list = new ArrayList<>(suggestions);
        Collections.shuffle(list);
        return list.stream().limit(6).sorted().collect(Collectors.toList());
    }

}





