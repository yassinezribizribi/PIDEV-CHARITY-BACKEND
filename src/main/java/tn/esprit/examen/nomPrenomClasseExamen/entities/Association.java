package tn.esprit.examen.nomPrenomClasseExamen.entities;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Association implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAssociation;

    private String associationAddress;
    private String associationName;
    private String description;

    @Enumerated(EnumType.STRING) // Storing as string in the database
    private AssociationStatus status = AssociationStatus.PENDING; // Default status is PENDING

    // Association.java
    public enum AssociationStatus {
        PENDING,
        APPROVED,
        REJECTED;

        @JsonCreator
        public static AssociationStatus fromValue(String value) {
            return Arrays.stream(values())
                    .filter(status -> status.name().equalsIgnoreCase(value))
                    .findFirst()
                    .orElse(PENDING); // Default to PENDING if the value is invalid
        }
    }


    // Instead of storing large binary data, store file paths (more efficient)
    private String associationLogoPath;
    private String registrationDocumentPath;
    private String legalDocumentPath;

    // ✅ Relationship with Subscriber (1-1)
    @OneToOne
    private Subscriber subscriber;

    // ✅ Other relationships
    @ManyToMany(cascade = CascadeType.ALL)
    @JsonManagedReference
    private Set<Subscription> subscriptions;
    @OneToMany(cascade = CascadeType.ALL, mappedBy="associationMission")
    @JsonIgnore // Prevent recursion here

    private Set<Mission> missions;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "association")
    @JsonManagedReference
    private Set<Event> events;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "association")
    @JsonManagedReference
    private Set<Notification> notifications;
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "association_partnerships",
            joinColumns = @JoinColumn(name = "association_id"),
            inverseJoinColumns = @JoinColumn(name = "partner_id")
    )
    private Set<Association> partners = new HashSet<>();

    @Column(name = "partnership_score")
    private Integer partnershipScore = 0;

    public void addPartner(Association partner) {
        this.partners.add(partner);
        partner.getPartners().add(this);
        updatePartnershipScore();
        partner.updatePartnershipScore();
    }

    public void removePartner(Association partner) {
        this.partners.remove(partner);
        partner.getPartners().remove(this);
        updatePartnershipScore();
        partner.updatePartnershipScore();
    }

    private void updatePartnershipScore() {
        this.partnershipScore = calculatePartnershipScore();
    }

    private int calculatePartnershipScore() {
        int baseScore = this.partners.size() * 5;
        int jointProjectsScore = 0; // No joint project logic since a mission belongs to one association
        return Math.min(baseScore + jointProjectsScore, 100);
    }


    // Add this method to get the partnership tier
    public PartnershipTier getPartnershipTier() {
        return PartnershipTier.determineTier(this.partnershipScore);
    }

    // Add this method to get progress to next tier
    public int getProgressToNextTier() {
        PartnershipTier currentTier = getPartnershipTier();
        if (currentTier == PartnershipTier.GOLD) {
            return 100; // Already at highest tier
        }
        int range = currentTier.maxScore - currentTier.minScore;
        int progress = partnershipScore - currentTier.minScore;
        return (int) ((progress / (double) range) * 100);
    }

    // Enhanced partnership tier enum
    public enum PartnershipTier {
        BRONZE("Bronze", 0, 30, 3),
        SILVER("Silver", 31, 60, 8),
        GOLD("Gold", 61, 100, 15);

        private final String displayName;
        private final int minScore;
        private final int maxScore;
        private final int maxPartners;

        PartnershipTier(String displayName, int minScore, int maxScore, int maxPartners) {
            this.displayName = displayName;
            this.minScore = minScore;
            this.maxScore = maxScore;
            this.maxPartners = maxPartners;
        }

        // Add getter methods for all fields
        public String getDisplayName() {
            return displayName;
        }

        public int getMinScore() {
            return minScore;
        }

        public int getMaxScore() {
            return maxScore;
        }

        public int getMaxPartners() {
            return maxPartners;
        }

        public int getNextTierThreshold() {
            return this == GOLD ? Integer.MAX_VALUE : values()[ordinal() + 1].minScore;
        }

        public static PartnershipTier determineTier(int score) {
            for (PartnershipTier tier : values()) {
                if (score >= tier.minScore && score <= tier.maxScore) {
                    return tier;
                }
            }
            return BRONZE;
        }

        public boolean canAddMorePartners(int currentPartners) {
            return currentPartners < maxPartners;
        }
    }



}
