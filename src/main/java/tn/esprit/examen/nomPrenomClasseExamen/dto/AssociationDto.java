package tn.esprit.examen.nomPrenomClasseExamen.dto;

import com.fasterxml.jackson.annotation.*;
import lombok.*;
import tn.esprit.examen.nomPrenomClasseExamen.entities.*;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)  // Only include non-null fields
public class AssociationDto {
    private Long idAssociation;
    private String associationAddress;
    private String associationName;
    private String description;
    private Association.AssociationStatus status;
    private String associationLogoPath;
    private String registrationDocumentPath;
    private String legalDocumentPath;
    private Integer partnershipScore;

    // Partnership tier information (will be set conditionally)
    private Association.PartnershipTier partnershipTier;
    private Integer progressToNextTier;

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
    @JsonIgnoreProperties({"partners"})
    private Set<Association> partners;
}