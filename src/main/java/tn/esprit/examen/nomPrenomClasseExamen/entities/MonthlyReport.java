package tn.esprit.examen.nomPrenomClasseExamen.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class MonthlyReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Association association;

    private int year;
    private int month;

    // Metrics
    private int newPartners;
    private int completedMissions;
    private int jointMissions;
    private int volunteerHours;
    private double fundsRaised;
    private int eventCount;

    @Enumerated(EnumType.STRING)
    private Association.PartnershipTier tierAtMonthEnd;

    private String highlights;
    private String recommendations;

    @Lob
    private byte[] pdfReport;
}
