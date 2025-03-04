package tn.esprit.examen.nomPrenomClasseExamen.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "event_subscriber")
public class EventSubscriber {

    @EmbeddedId
    private EventSubscriberId id;

    @ManyToOne
    @MapsId("eventId")
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne
    @MapsId("subscriberId")
    @JoinColumn(name = "subscriber_id")
    private Subscriber subscriber;

    @Column(nullable = false)
    private Boolean isAffected = false;

    // Constructeurs
    public EventSubscriber() {}

    public EventSubscriber(Event event, Subscriber subscriber, Boolean isAffected) {
        this.id = new EventSubscriberId(event.getIdEvent(), subscriber.getIdUser());
        this.event = event;
        this.subscriber = subscriber;
        this.isAffected = isAffected;
    }

    // Getters & Setters
    public EventSubscriberId getId() { return id; }
    public void setId(EventSubscriberId id) { this.id = id; }

    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }

    public Subscriber getSubscriber() { return subscriber; }
    public void setSubscriber(Subscriber subscriber) { this.subscriber = subscriber; }

    public Boolean getIsAffected() { return isAffected; }
    public void setIsAffected(Boolean isAffected) { this.isAffected = isAffected; }
}