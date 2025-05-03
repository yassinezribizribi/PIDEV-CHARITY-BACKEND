package tn.esprit.examen.nomPrenomClasseExamen.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Event;
import tn.esprit.examen.nomPrenomClasseExamen.entities.EventSubscriber;
import tn.esprit.examen.nomPrenomClasseExamen.entities.User;

import java.util.List;

public interface EventSubscriberRepository extends JpaRepository<EventSubscriber,Long> {
    List<EventSubscriber> findByEvent(Event event);

    EventSubscriber findEventSubscriberBySubscriber(User user);

    @Query("SELECT es.event FROM EventSubscriber es WHERE es.subscriber = :user")
    List<Event> findEventsBySubscriber(@Param("user") User user);

}
