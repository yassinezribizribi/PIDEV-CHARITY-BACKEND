package tn.esprit.examen.nomPrenomClasseExamen.services;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.AssociationRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.SubscriberRepository;
import tn.esprit.examen.nomPrenomClasseExamen.dto.*;
import tn.esprit.examen.nomPrenomClasseExamen.entities.*;
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

    public AssociationDto createAssociation(AssociationDto associationDto, String token) {
        Long userId = jwtUtils.getUserIdFromJwtToken(token);

        if (associationRepository.findBySubscriberIdUser(userId).isPresent()) {
            throw new BusinessException("Subscriber already has an association");
        }

        Subscriber subscriber = subscriberRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscriber not found"));

        Association association = convertToEntity(associationDto);
        association.setSubscriber(subscriber);

        return convertToDto(associationRepository.save(association));
    }

    public Optional<AssociationDto> getAssociationByUserId(String token) {
        Long userId = jwtUtils.getUserIdFromJwtToken(token);
        return associationRepository.findBySubscriberIdUser(userId)
                .map(this::convertToDto);
    }

    public AssociationDto updateAssociation(Long id, AssociationDto associationDto, String token) {
        Long userId = jwtUtils.getUserIdFromJwtToken(token);
        getAuthenticatedAssociation(id, userId); // Verify ownership

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

            return convertToDto(associationRepository.save(existingAssociation));
        }).orElseThrow(() -> new ResourceNotFoundException("Association not found"));
    }

    public AssociationDto verifyAssociation(Long id, String token) {
        Association association = associationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Association not found"));

        association.setStatus(Association.AssociationStatus.APPROVED);
        associationRepository.save(association);

        Subscriber subscriber = association.getSubscriber();
        sendVerificationEmail(subscriber);

        return convertToDto(association);
    }

    public void deleteAssociation(Long id, String token) {
        Long userId = jwtUtils.getUserIdFromJwtToken(token);
        getAuthenticatedAssociation(id, userId); // Verify ownership
        associationRepository.deleteById(id);
    }

    // ============== PARTNERSHIP SYSTEM ==============
    @Transactional
    public AssociationDto createPartnership(Long associationId, Long partnerId, String token) {
        Long userId = jwtUtils.getUserIdFromJwtToken(token);
        Association association = getAuthenticatedAssociation(associationId, userId);
        Association partner = getAssociation(partnerId);

        if (association.getPartners().contains(partner)) {
            throw new BusinessException("Partnership already exists");
        }

        association.getPartners().add(partner);
        partner.getPartners().add(association);

        association.setPartnershipScore(calculatePartnershipScore(association));
        partner.setPartnershipScore(calculatePartnershipScore(partner));

        associationRepository.save(association);
        associationRepository.save(partner);

        return convertToDto(association);
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

        association.setPartnershipScore(calculatePartnershipScore(association));
        partner.setPartnershipScore(calculatePartnershipScore(partner));

        associationRepository.save(association);
        associationRepository.save(partner);
    }

    public Set<AssociationDto> getPartners(Long associationId) {
        return associationRepository.findById(associationId)
                .map(Association::getPartners)
                .orElse(Set.of())
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toSet());
    }

    public List<AssociationDto> getPotentialPartners(Long associationId) {
        Association current = getAssociation(associationId);
        List<Association> potentials = associationRepository.findPotentialPartners(associationId);

        return potentials.stream()
                .filter(a -> !current.getPartners().contains(a))
                .map(this::convertToDtoWithoutPartners)
                .collect(Collectors.toList());
    }

    public PartnershipTierDto getPartnershipTier(Long associationId) {
        Association association = associationRepository.findById(associationId)
                .orElseThrow(() -> new ResourceNotFoundException("Association not found"));

        Association.PartnershipTier tier = association.getPartnershipTier();

        return PartnershipTierDto.builder()
                .tier(tier)
                .score(association.getPartnershipScore())
                .nextTierThreshold(tier.getNextTierThreshold())
                .progress(association.getProgressToNextTier())
                .maxPartners(tier.getMaxPartners()) // Using the getter method
                .currentPartners(association.getPartners().size())
                .build();
    }

    public PartnershipImpactReportDto generateImpactReport(Long associationId, String token) {
        Long userId = jwtUtils.getUserIdFromJwtToken(token);
        Association association = getAuthenticatedAssociation(associationId, userId);

        // Since each mission belongs to only one association, joint missions are not possible in current model
        int jointMissions = 0;

        // Likewise, no shared volunteers in current model logic
        int sharedVolunteers = 0;

        double efficiencyGain = calculateEfficiencyGain(association);

        return PartnershipImpactReportDto.builder()
                .associationId(associationId)
                .jointMissionsCompleted(jointMissions)
                .volunteersShared(sharedVolunteers)
                .efficiencyImprovement(efficiencyGain)
                .partnershipScore(association.getPartnershipScore())
                .tier(association.getPartnershipTier())
                .recommendations(generateRecommendations(association))
                .build();
    }

    // ============== PRIVATE HELPER METHODS ==============
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

    private int calculatePartnershipScore(Association association) {
        int baseScore = association.getPartners().size() * 5;

        // No joint missions supported in current model
        int jointProjectsScore = 0;

        return Math.min(baseScore + jointProjectsScore, 100);
    }

    private int countJointMissions(Association association) {
        // No joint missions are possible in the current model
        return 0;
    }

    private int calculateSharedVolunteers(Association association) {
        // No shared volunteers since each mission belongs to one association
        return 0;
    }

    private double calculateEfficiencyGain(Association association) {
        // Only solo missions exist
        long soloMissions = association.getMissions().size();

        // No joint missions
        long jointMissions = 0;

        return 0.0; // No joint missions, so efficiency gain is not applicable
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

    private void sendVerificationEmail(Subscriber subscriber) {
        String subject = "Association Verified";
        String body = "Dear " + subscriber.getFirstName() + " " + subscriber.getLastName() +
                ",\n\nYour association has been successfully verified.\n\nBest regards,\nThe Team";
        emailService.sendEmail(subscriber.getEmail(), subject, body);
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
                .subscriptions(association.getSubscriptions())
                .missions(association.getMissions())
                .events(association.getEvents())
                .notifications(association.getNotifications())
                .partners(association.getPartners())
                .partnershipScore(association.getPartnershipScore())
                .build();
    }

    private AssociationDto convertToDtoWithoutPartners(Association association) {
        AssociationDto dto = convertToDto(association);
        dto.setPartners(null);
        return dto;
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
        return association;
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