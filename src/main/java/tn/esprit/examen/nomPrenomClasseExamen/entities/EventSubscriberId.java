package tn.esprit.examen.nomPrenomClasseExamen.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class EventSubscriberId implements Serializable {

    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "subscriber_id")
    private Long subscriberId;

    // Constructeurs par défaut et paramétré
    public EventSubscriberId() {}

    public EventSubscriberId(Long eventId, Long subscriberId) {
        this.eventId = eventId;
        this.subscriberId = subscriberId;
    }

    // Getters & Setters
    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public Long getSubscriberId() { return subscriberId; }
    public void setSubscriberId(Long subscriberId) { this.subscriberId = subscriberId; }

    // Méthodes equals() et hashCode() nécessaires pour @Embeddable
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventSubscriberId that = (EventSubscriberId) o;
        return Objects.equals(eventId, that.eventId) && Objects.equals(subscriberId, that.subscriberId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, subscriberId);
    }
}

















