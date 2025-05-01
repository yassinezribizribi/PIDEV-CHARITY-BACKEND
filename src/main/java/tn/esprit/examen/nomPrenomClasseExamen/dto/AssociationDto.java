package tn.esprit.examen.nomPrenomClasseExamen.dto;

import com.fasterxml.jackson.annotation.*;
import lombok.*;
import tn.esprit.examen.nomPrenomClasseExamen.entities.*;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)  // Only include non-null fields
public class AssociationDto {
    private Long idAssociation;
    private String associationAddress;
    private Double latitude;
    private Double longitude;
    private String associationName;
    private String description;
    private Association.AssociationStatus status;
    private String associationLogoPath;
    private String registrationDocumentPath;
    private String legalDocumentPath;
    private Integer partnershipScore;

    // Partnership tier information (will be set conditionally)
    private Association.PartnershipTier partnershipTier = Association.PartnershipTier.BRONZE; // Default
    private Integer progressToNextTier;
    private Double similarityScore;  // For recommendations only

    @JsonIgnore
    private Subscriber subscriber;

    @JsonIgnore
    private Set<Subscription> subscriptions;

    @JsonIgnore
    private Set<Mission> missions;

    @JsonIgnore
    private Set<Event> events;

    @JsonIgnore
    private Set<Notification> notifications;

    @JsonIdentityInfo(
            generator = ObjectIdGenerators.PropertyGenerator.class,
            property = "idAssociation"
    )
    @JsonIgnoreProperties({"partners", "similarityScore"})
    @Builder.Default
    private Set<AssociationDto> partners = new HashSet<>();


}

