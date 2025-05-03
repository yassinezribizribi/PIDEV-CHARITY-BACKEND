package tn.esprit.examen.nomPrenomClasseExamen.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "event_subscriber")
public class EventSubscriber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-incrémenté
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne
    @JoinColumn(name = "subscriber_id", nullable = false)
    private Subscriber subscriber;

    @Column(nullable = false)
    private Boolean isAffected = false;

    // Constructeurs
    public EventSubscriber() {}

    public EventSubscriber(Event event, Subscriber subscriber, Boolean isAffected) {
        this.event = event;
        this.subscriber = subscriber;
        this.isAffected = isAffected;
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Subscriber getSubscriber() {
        return subscriber;
    }

    public void setSubscriber(Subscriber subscriber) {
        this.subscriber = subscriber;
    }

    public Boolean getIsAffected() {
        return isAffected;
    }

    public void setIsAffected(Boolean isAffected) {
        this.isAffected = isAffected;
    }
}
