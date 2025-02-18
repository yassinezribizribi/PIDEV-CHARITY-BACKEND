package tn.esprit.examen.nomPrenomClasseExamen.dto;

import tn.esprit.examen.nomPrenomClasseExamen.entities.Categorie;

import java.util.Date;

public class CrisisDTO {
    private Long idCrisis;
    private Categorie categorie;
    private String location;
    private String updates;
    private String description;
    private Date crisisDate;
    private Long subscriberId;  // ID de l'utilisateur qui a signalé la crise

    // Getters et Setters
    public Long getIdCrisis() {
        return idCrisis;
    }

    public void setIdCrisis(Long idCrisis) {
        this.idCrisis = idCrisis;
    }

    public Categorie getCategorie() {
        return categorie;
    }

    public void setCategorie(Categorie categorie) {
        this.categorie = categorie;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getUpdates() {
        return updates;
    }

    public void setUpdates(String updates) {
        this.updates = updates;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCrisisDate() {
        return crisisDate;
    }

    public void setCrisisDate(Date crisisDate) {
        this.crisisDate = crisisDate;
    }

    public Long getSubscriberId() {
        return subscriberId;
    }

    public void setSubscriberId(Long subscriberId) {
        this.subscriberId = subscriberId;
    }
}