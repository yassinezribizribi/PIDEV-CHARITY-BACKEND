package tn.esprit.examen.nomPrenomClasseExamen.services;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.AssociationRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.SubscriberRepository;
import tn.esprit.examen.nomPrenomClasseExamen.dto.*;
import tn.esprit.examen.nomPrenomClasseExamen.entities.*;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Notification.NotificationType;
import tn.esprit.examen.nomPrenomClasseExamen.jwt.JwtUtils;

import java.util.*;
import java.util.stream.Collectors;


@Service
@AllArgsConstructor
public class AssociationServices {
    private final AssociationRepository associationRepository;
    private final SubscriberRepository subscriberRepository;
    private final EmailService emailService;
    private final JwtUtils jwtUtils;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;
    private static final Logger logger = LoggerFactory.getLogger(AssociationServices.class);

    // ============== CORE ASSOCIATION METHODS ==============
    public List<AssociationDto> getAllAssociations() {
        return associationRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Optional<AssociationDto> getAssociationById(Long id) {
        return associationRepository.findById(id)
                .map(this::convertToDto);
    }

    @Transactional
    public AssociationDto createAssociation(AssociationDto associationDto, String token) {
        Long userId = jwtUtils.getUserIdFromJwtToken(token);

        if (associationRepository.findBySubscriberIdUser(userId).isPresent()) {
            throw new BusinessException("Subscriber already has an association");
        }

        Subscriber subscriber = subscriberRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscriber not found"));

        Association association = convertToEntity(associationDto);
        association.setSubscriber(subscriber);

        Association savedAssociation = associationRepository.save(association);

        sendAssociationCreatedNotification(savedAssociation);
        return convertToDto(savedAssociation);
    }

    private void sendAssociationCreatedNotification(Association association) {
        notificationService.createAndSendNotification(
                "🎉 Your association " + association.getAssociationName() + " has been created successfully",
                NotificationType.SYSTEM_ALERT,
                "/associations/" + association.getIdAssociation(),
                association.getSubscriber(),
                null,
                null,
                association
        );
    }

    public Optional<AssociationDto> getAssociationByUserId(String token) {
        Long userId = jwtUtils.getUserIdFromJwtToken(token);
        return associationRepository.findBySubscriberIdUser(userId)
                .map(this::convertToDto);
    }

    public AssociationDto updateAssociation(Long id, AssociationDto associationDto, String token) {
        Long userId = jwtUtils.getUserIdFromJwtToken(token);
        getAuthenticatedAssociation(id, userId);

        return associationRepository.findById(id).map(existingAssociation -> {
            existingAssociation.setAssociationAddress(associationDto.getAssociationAddress());
            existingAssociation.setAssociationName(associationDto.getAssociationName());
            existingAssociation.setDescription(associationDto.getDescription());
            existingAssociation.setStatus(associationDto.getStatus());

            if (associationDto.getAssociationLogoPath() != null) {
                existingAssociation.setAssociationLogoPath(associationDto.getAssociationLogoPath());
            }
            if (associationDto.getRegistrationDocumentPath() != null) {
                existingAssociation.setRegistrationDocumentPath(associationDto.getRegistrationDocumentPath());
            }
            if (associationDto.getLegalDocumentPath() != null) {
                existingAssociation.setLegalDocumentPath(associationDto.getLegalDocumentPath());
            }

            Association updated = associationRepository.save(existingAssociation);
            sendAssociationUpdatedNotification(updated);
            return convertToDto(updated);
        }).orElseThrow(() -> new ResourceNotFoundException("Association not found"));
    }

    private void sendAssociationUpdatedNotification(Association association) {
        notificationService.createAndSendNotification(
                "🔧 Your association details have been updated",
                NotificationType.SYSTEM_ALERT,
                "/associations/" + association.getIdAssociation(),
                association.getSubscriber(),
                null,
                null,
                association
        );
    }
    public Association.PartnershipTier upgradeAssociationTier(
            Long associationId,
            Association.PartnershipTier currentTier,
            String reason) {

        // 1. Fetch the association
        Association association = associationRepository.findByIdAssociation(associationId);
        if (association == null) {
            throw new ResourceNotFoundException("Association not found");
        }

        // 2. Validate upgrade eligibility
        if (currentTier == Association.PartnershipTier.GOLD) {
            throw new IllegalStateException("Cannot upgrade GOLD tier further");
        }

        // 3. Determine new tier
        Association.PartnershipTier newTier;
        switch (currentTier) {
            case BRONZE:
                newTier = Association.PartnershipTier.SILVER;
                break;
            case SILVER:
                newTier = Association.PartnershipTier.GOLD;
                break;
            default:
                throw new IllegalStateException("Invalid current tier");
        }

        // 4. Update association's tier
        association.setPartnershipTier(newTier);
        // Save the updated association
        associationRepository.save(association);  // Fixed line

        return newTier;
    }
    public String getUserEmailByAssociationId(Long associationId) {
        Association association = associationRepository.findById(associationId)
                .orElseThrow(() -> new ResourceNotFoundException("Association not found with id: " + associationId));

        // Get the subscriber (user) associated with the association
        Subscriber subscriber = association.getSubscriber();
        if (subscriber == null) {
            throw new ResourceNotFoundException("No user found for association with id: " + associationId);
        }

        return subscriber.getEmail();
    }
    public Page<Association> findPaginatedAssociations(String search, String location, String tier, Pageable pageable) {
        Specification<Association> spec = Specification.where(null);

        // Add search filter
        if (!search.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("associationName")), "%" + search.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("description")), "%" + search.toLowerCase() + "%")
                    )
            );
        }

        if (!location.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(root.get("associationAddress"), "%" + location + "%")
            );
        }

        if (!tier.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("partnershipTier"), tier)
            );
        }

        // Only return approved associations
        spec = spec.and((root, query, cb) ->
                cb.equal(root.get("status"), Association.AssociationStatus.APPROVED)
        );

        return associationRepository.findAll(spec, pageable);
    }
    public AssociationDto verifyAssociation(Long id, String token) {
        Association association = associationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Association not found"));

        association.setStatus(Association.AssociationStatus.APPROVED);
        Association verified = associationRepository.save(association);

        Subscriber subscriber = association.getSubscriber();
        sendVerificationEmail(subscriber);
        sendVerificationNotification(verified);

        return convertToDto(verified);
    }

    private void sendVerificationNotification(Association association) {
        notificationService.createAndSendNotification(
                "✅ Your association has been verified",
                NotificationType.SYSTEM_ALERT,
                "/associations/" + association.getIdAssociation(),
                association.getSubscriber(),
                null,
                null,
                association
        );
    }

    // ... existing code ...

    @Transactional
    public void deleteAssociation(Long id, String token) {
        Long userId = jwtUtils.getUserIdFromJwtToken(token);
        List<String> roles = jwtUtils.getRolesFromJwtToken(token);

        Association association = associationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Association not found"));

        // Authorization check
        if (!roles.contains("ROLE_ADMIN") &&
                !isAssociationOwner(association, userId)) {
            throw new UnauthorizedException("Not authorized to manage this association");
        }

        // Clear notifications first
        association.getNotifications().clear();

        // Clear partnerships to avoid circular references
        new ArrayList<>(association.getPartners()).forEach(partner ->
                partner.getPartners().remove(association)
        );
        association.getPartners().clear();

        // Clear other relationships
        association.getMissions().clear();
        association.getEvents().clear();
        association.getSubscriptions().clear();

        // Save the association with cleared relationships
        associationRepository.save(association);

        // Now delete the association
        associationRepository.delete(association);
        associationRepository.flush();
    }


    private boolean isAssociationOwner(Association association, Long userId) {
        return association.getSubscriber() != null
                && association.getSubscriber().getIdUser().equals(userId);
    }



    // ============== ENHANCED PARTNERSHIP SYSTEM ==============

    public List<AssociationDto> getRecommendedPartners(Long associationId) {
        Association current = getAssociation(associationId);

        return associationRepository.findNonPartnerAssociations(current.getIdAssociation()).stream()
                .filter(a -> !a.equals(current))
                .sorted(Comparator.comparingDouble((Association a) ->
                                calculateMissionSimilarity(current, a))
                        .reversed()
                        .thenComparing(Association::getAssociationName))
                .limit(5)
                .map(a -> convertToDtoWithSimilarity(a, current))
                .collect(Collectors.toList());
    }

    private AssociationDto convertToDtoWithSimilarity(Association association, Association current) {
        AssociationDto dto = convertToDto(association);
        double similarity = calculateMissionSimilarity(current, association);
        dto.setSimilarityScore((double) Math.round(similarity * 100)); // Cast to Double
        return dto;
    }

    private double calculateMissionSimilarity(Association a1, Association a2) {
        Set<String> tags1 = extractTags(a1.getMissions());
        Set<String> tags2 = extractTags(a2.getMissions());

        if(tags1.isEmpty() && tags2.isEmpty()) return 0.0;
        if(tags1.isEmpty() || tags2.isEmpty()) return 0.0;

        long intersection = tags1.stream()
                .filter(tags2::contains)
                .count();

        long union = tags1.size() + tags2.size() - intersection;

        return (double) intersection / union;
    }

    private Set<String> extractTags(Set<Mission> missions) {
        return missions.stream()
                .flatMap(m -> m.getTags().stream())
                .map(String::toLowerCase)
                .filter(tag -> !tag.isBlank())
                .collect(Collectors.toSet());
    }
    @Transactional
    public AssociationDto createPartnership(Long associationId, Long partnerId, String token) {
        Long userId = jwtUtils.getUserIdFromJwtToken(token);
        Association association = getAuthenticatedAssociation(associationId, userId);
        Association partner = getAssociation(partnerId);

        // Initialize partnership tiers if null
        if (association.getPartnershipTier() == null) {
            association.setPartnershipTier(Association.PartnershipTier.BRONZE);
            associationRepository.save(association);
        }
        if (partner.getPartnershipTier() == null) {
            partner.setPartnershipTier(Association.PartnershipTier.BRONZE);
            associationRepository.save(partner);
        }

        // Perform validations after ensuring tiers are set
        validatePartnership(association, partner);
        validatePartnershipCapacity(association);
        validatePartnershipCapacity(partner);

        // Add partners and update scores
        association.getPartners().add(partner);
        partner.getPartners().add(association);
        updatePartnershipScoresAndTiers(association, partner);
        sendPartnershipNotifications(association, partner);

        return convertToDto(association);
    }
    private void validatePartnershipCapacity(Association association) {
        int currentPartners = association.getPartners().size();
        int maxAllowed = getMaxPartnersForTier(association.getPartnershipTier());

        if (currentPartners >= maxAllowed) {
            // Check if they would qualify for next tier if they add this partner
            int potentialPartnerCount = currentPartners + 1;
            Association.PartnershipTier potentialTier = calculateTier(association.getPartnershipScore(), potentialPartnerCount);

            if (potentialTier == association.getPartnershipTier()) {
                throw new BusinessException("Cannot add more partners. Max allowed for "
                        + association.getPartnershipTier() + " tier: " + maxAllowed);
            }
        }
    }
    private int getMaxPartnersForTier(Association.PartnershipTier tier) {
        return switch (tier) {
            case BRONZE -> 3;
            case SILVER -> 5;
            case GOLD -> Integer.MAX_VALUE;
        };
    }
    private void validatePartnership(Association association, Association partner) {
        if (association.equals(partner)) {
            throw new BusinessException("Cannot partner with yourself");
        }
        if (association.getPartners().contains(partner)) {
            throw new BusinessException("Partnership already exists");
        }
    }
    public Set<AssociationDto> getPartners(Long associationId) {
        Association association = getAssociation(associationId);
        return association.getPartners().stream()
                .map(this::convertToDtoWithoutPartners)  // Convert each Association to AssociationDto
                .collect(Collectors.toSet());
    }

    public PartnershipTierDto getPartnershipTierInfo(Long associationId) {
        Association association = getAssociation(associationId);
        return PartnershipTierDto.builder()
                .tier(association.getPartnershipTier())
                .score(association.getPartnershipScore())
                .build();
    }
    private void updatePartnershipScoresAndTiers(Association... associations) {
        for (Association assoc : associations) {
            int newScore = calculateEnhancedPartnershipScore(assoc);
            int partnerCount = assoc.getPartners().size();
            Association.PartnershipTier newTier = calculateTier(newScore, partnerCount);

            if (assoc.getPartnershipTier() != newTier) {
                Association.PartnershipTier oldTier = assoc.getPartnershipTier();
                assoc.setPartnershipTier(newTier);
                sendTierChangeNotification(assoc, oldTier, newTier);
            }

            assoc.setPartnershipScore(newScore);
            associationRepository.save(assoc);
        }
    }

    private int calculateEnhancedPartnershipScore(Association association) {
        int baseScore = Math.min(association.getPartners().size() * 5, 50);
        int jointProjectsScore = (int) association.getMissions().stream()
                .filter(m -> m.getAssociations().size() > 1)
                .count() * 10;
        int volunteerScore = calculateSharedVolunteers(association) * 2;

        return Math.min(baseScore + jointProjectsScore + volunteerScore, 100);
    }


    private void sendPartnershipNotifications(Association association, Association partner) {
        // Notification to initiating association
        notificationService.createAndSendNotification(
                "🤝 Partnership request sent to " + partner.getAssociationName(),
                NotificationType.PARTNERSHIP_REQUEST,
                "/associations/" + association.getIdAssociation() + "/partners",
                association.getSubscriber(),
                null,
                null,
                association
        );

        // Notification to partner association
        notificationService.createAndSendNotification(
                "🤝 New partnership request from " + association.getAssociationName(),
                NotificationType.PARTNERSHIP_REQUEST,
                "/associations/" + partner.getIdAssociation() + "/partners",
                partner.getSubscriber(),
                association.getSubscriber(),
                null,
                partner
        );
    }

    @Transactional
    public void removePartnership(Long associationId, Long partnerId, String token) {
        Long userId = jwtUtils.getUserIdFromJwtToken(token);
        Association association = getAuthenticatedAssociation(associationId, userId);
        Association partner = getAssociation(partnerId);

        if (!association.getPartners().contains(partner)) {
            throw new BusinessException("Partnership doesn't exist");
        }

        association.getPartners().remove(partner);
        partner.getPartners().remove(association);
        updatePartnershipScoresAndTiers(association, partner);
        sendPartnershipRemovedNotifications(association, partner);
    }

    private void sendPartnershipRemovedNotifications(Association association, Association partner) {
        // Notification to initiating association
        notificationService.createAndSendNotification(
                "🚫 Partnership with " + partner.getAssociationName() + " has been removed",
                NotificationType.PARTNERSHIP_REMOVED,
                "/associations/" + association.getIdAssociation() + "/partners",
                association.getSubscriber(),
                null,
                null,
                association
        );

        // Notification to partner association
        notificationService.createAndSendNotification(
                "🚫 Partnership with " + association.getAssociationName() + " has been removed",
                NotificationType.PARTNERSHIP_REMOVED,
                "/associations/" + partner.getIdAssociation() + "/partners",
                partner.getSubscriber(),
                association.getSubscriber(),
                null,
                partner
        );
    }

    // ============== TIER MANAGEMENT ==============
    @Scheduled(cron = "0 0 0 * * MON")
    @Transactional
    public void updateAllAssociationTiers() {
        associationRepository.findAll().forEach(association -> {
            int newScore = calculateEnhancedPartnershipScore(association);
            int partnerCount = association.getPartners().size();
            Association.PartnershipTier newTier = calculateTier(newScore, partnerCount);

            if (!association.getPartnershipTier().equals(newTier)) {
                Association.PartnershipTier oldTier = association.getPartnershipTier();
                association.setPartnershipScore(newScore);
                association.setPartnershipTier(newTier);
                associationRepository.save(association);
                sendTierChangeNotification(association, oldTier, newTier);
            }
        });
    }

    private void sendTierChangeNotification(Association association,
                                            Association.PartnershipTier oldTier,
                                            Association.PartnershipTier newTier) {
        notificationService.createAndSendNotification(
                String.format("🎉 Your partnership tier changed from %s to %s", oldTier, newTier),
                NotificationType.TIER_CHANGE,
                "/associations/" + association.getIdAssociation() + "/partnership",
                association.getSubscriber(),
                null,
                null,
                association
        );
    }

    // ============== IMPACT REPORT ENHANCEMENTS ==============
    public PartnershipImpactReportDto generateImpactReport(Long associationId, String email) {
        Subscriber subscriber = subscriberRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Subscriber not found"));

        Association association = getAuthenticatedAssociation(associationId, subscriber.getIdUser());
        sendImpactReportNotification(association);

        return buildImpactReport(association);
    }

    private void sendImpactReportNotification(Association association) {
        notificationService.createAndSendNotification(
                "📊 Your partnership impact report is ready",
                NotificationType.IMPACT_REPORT,
                "/associations/" + association.getIdAssociation() + "/reports",
                association.getSubscriber(),
                null,
                null,
                association
        );
    }

    // ============== HELPER METHODS ==============
    private Association.PartnershipTier calculateTier(int score, int partnerCount) {
        // Check if partner count qualifies for higher tier regardless of score
        if (partnerCount >= 10) return Association.PartnershipTier.GOLD;
        if (partnerCount >= 5) return Association.PartnershipTier.SILVER;

        // Fall back to score-based tier if partner count doesn't qualify
        if (score >= 61) return Association.PartnershipTier.GOLD;
        if (score >= 31) return Association.PartnershipTier.SILVER;
        return Association.PartnershipTier.BRONZE;
    }

    private Association getAuthenticatedAssociation(Long associationId, Long userId) {
        Association association = getAssociation(associationId);
        if (!association.getSubscriber().getIdUser().equals(userId)) {
            throw new UnauthorizedException("Not authorized to manage this association");
        }
        return association;
    }

    private Association getAssociation(Long id) {
        return associationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Association not found"));
    }

    private int calculateSharedVolunteers(Association association) {
        return association.getMissions().stream()
                .filter(m -> m.getAssociations().size() > 1)
                .mapToInt(Mission::getVolunteerCount)
                .sum();
    }

    private double calculateEfficiencyGain(Association association) {
        int totalMissions = association.getMissions().size();
        long jointMissions = association.getMissions().stream()
                .filter(m -> m.getAssociations().size() > 1)
                .count();

        return totalMissions == 0 ? 0 : (double) jointMissions / totalMissions * 100;
    }

    private List<String> generateRecommendations(Association association) {
        List<String> recommendations = new ArrayList<>();
        if (association.getPartnershipScore() < 30) {
            recommendations.add("Consider partnering with associations in your area");
        }
        if (countJointMissions(association) < 3) {
            recommendations.add("Try creating joint missions with your partners");
        }
        return recommendations;
    }

    private int countJointMissions(Association association) {
        return (int) association.getMissions().stream()
                .filter(m -> m.getAssociations().size() > 1)
                .count();
    }

    private void sendVerificationEmail(Subscriber subscriber) {
        try {
            String imagePath = "src/main/resources/static/assets/images/logo-dark.png";
            String contentId = "logoImage";
            String emailBody = buildVerificationEmailBody(subscriber);

            emailService.sendEmailWithInlineImage(
                    subscriber.getEmail(),
                    "Association Verified",
                    emailBody,
                    imagePath,
                    contentId
            );
        } catch (Exception e) {
            logger.error("Failed to send verification email to subscriber ID: {}", subscriber.getIdUser(), e);
        }
    }

    private String buildVerificationEmailBody(Subscriber subscriber) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "  <style>" +
                "    body {" +
                "      font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;" +
                "      line-height: 1.6;" +
                "      color: #333333;" +
                "      background-color: #f5f7fa;" +
                "      margin: 0;" +
                "      padding: 0;" +
                "    }" +
                "    .container {" +
                "      max-width: 600px;" +
                "      margin: 0 auto;" +
                "      background: white;" +
                "      border-radius: 8px;" +
                "      overflow: hidden;" +
                "      box-shadow: 0 4px 12px rgba(0,0,0,0.1);" +
                "    }" +
                "    .header {" +
                "      text-align: center;" +
                "      padding: 30px 20px 20px;" +
                "      background: linear-gradient(135deg, #2f55d4 0%, #3b82f6 100%);" +
                "      color: white;" +
                "    }" +
                "    .logo-container {" +
                "      margin-bottom: 15px;" +
                "    }" +
                "    .logo {" +
                "      max-width: 180px;" +
                "      height: auto;" +
                "    }" +
                "    .platform-name {" +
                "      font-size: 24px;" +
                "      font-weight: 700;" +
                "      margin: 10px 0 0;" +
                "      color: white;" +
                "      letter-spacing: 0.5px;" +
                "    }" +
                "    .content {" +
                "      padding: 30px;" +
                "    }" +
                "    h1 {" +
                "      color: #2f55d4;" +
                "      margin-bottom: 25px;" +
                "      font-size: 28px;" +
                "    }" +
                "    .highlight-box {" +
                "      background-color: #f0f5ff;" +
                "      border-left: 4px solid #2f55d4;" +
                "      padding: 15px;" +
                "      margin: 20px 0;" +
                "      border-radius: 0 4px 4px 0;" +
                "    }" +
                "    .footer {" +
                "      text-align: center;" +
                "      margin-top: 40px;" +
                "      font-size: 12px;" +
                "      color: #9ca3af;" +
                "      padding: 20px;" +
                "      background-color: #f9fafb;" +
                "      border-top: 1px solid #e5e7eb;" +
                "    }" +
                "  </style>" +
                "</head>" +
                "<body>" +
                "  <div class=\"container\">" +
                "    <div class=\"header\">" +
                "      <div class=\"logo-container\">" +
                "        <img src=\"cid:logoImage\" alt=\"Platform Logo\" class=\"logo\">" +
                "      </div>" +
                "      <h1 class=\"platform-name\">Solidarity & Refugee Platform</h1>" +
                "    </div>" +
                "    <div class=\"content\">" +
                "      <h1>Association Verified</h1>" +
                "      <p>Dear " + subscriber.getFirstName() + " " + subscriber.getLastName() + ",</p>" +
                "      <div class=\"highlight-box\">" +
                "        <p>Your association has been successfully verified.</p>" +
                "      </div>" +
                "      <p>You can now enjoy all the features of our platform.</p>" +
                "      <p>Best regards,<br>" +
                "      <strong>The Team</strong></p>" +
                "    </div>" +
                "    <div class=\"footer\">" +
                "      <p>© 2023 Solidarity & Refugee Platform. All rights reserved.</p>" +
                "    </div>" +
                "  </div>" +
                "</body>" +
                "</html>";
    }
    private AssociationDto convertToDtoWithoutPartners(Association association) {
        return AssociationDto.builder()
                .idAssociation(association.getIdAssociation())
                .associationAddress(association.getAssociationAddress())
                .associationName(association.getAssociationName())
                .description(association.getDescription())
                .status(association.getStatus())
                .associationLogoPath(association.getAssociationLogoPath())
                .registrationDocumentPath(association.getRegistrationDocumentPath())
                .legalDocumentPath(association.getLegalDocumentPath())
                .partnershipScore(association.getPartnershipScore())
                .partnershipTier(association.getPartnershipTier())

                .build();
    }
    private AssociationDto convertToDto(Association association) {
        return AssociationDto.builder()
                .idAssociation(association.getIdAssociation())
                .associationAddress(association.getAssociationAddress())
                .associationName(association.getAssociationName())
                .description(association.getDescription())
                .status(association.getStatus())
                .associationLogoPath(association.getAssociationLogoPath())
                .registrationDocumentPath(association.getRegistrationDocumentPath())
                .legalDocumentPath(association.getLegalDocumentPath())
                .subscriber(association.getSubscriber())
                .partnershipScore(association.getPartnershipScore())
                // Add partnership tier
                .partnershipTier(association.getPartnershipTier())
                .partners(association.getPartners().stream()
                        .map(this::convertToDtoWithoutPartners)
                        .collect(Collectors.toSet()))
                .build();
    }
    private Association convertToEntity(AssociationDto associationDto) {
        Association association = new Association();
        association.setIdAssociation(associationDto.getIdAssociation());
        association.setAssociationAddress(associationDto.getAssociationAddress());
        association.setAssociationName(associationDto.getAssociationName());
        association.setDescription(associationDto.getDescription());
        association.setStatus(associationDto.getStatus());
        association.setAssociationLogoPath(associationDto.getAssociationLogoPath());
        association.setRegistrationDocumentPath(associationDto.getRegistrationDocumentPath());
        association.setLegalDocumentPath(associationDto.getLegalDocumentPath());
        association.setSubscriptions(associationDto.getSubscriptions());
        association.setMissions(associationDto.getMissions());
        association.setEvents(associationDto.getEvents());
        association.setNotifications(associationDto.getNotifications());
        association.setPartnershipTier(associationDto.getPartnershipTier());
        association.setPartnershipScore(associationDto.getPartnershipScore());
        return association;
    }

    private PartnershipImpactReportDto buildImpactReport(Association association) {
        return PartnershipImpactReportDto.builder()
                .associationId(association.getIdAssociation())
                .jointMissionsCompleted(countJointMissions(association))
                .volunteersShared(calculateSharedVolunteers(association))
                .efficiencyImprovement(calculateEfficiencyGain(association))
                .partnershipScore(association.getPartnershipScore())
                .tier(association.getPartnershipTier())
                .recommendations(generateRecommendations(association))
                .build();
    }

    // ============== EXCEPTION CLASSES ==============
    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) { super(message); }
    }

    public static class BusinessException extends RuntimeException {
        public BusinessException(String message) { super(message); }
    }

    public static class UnauthorizedException extends RuntimeException {
        public UnauthorizedException(String message) { super(message); }
    }
}