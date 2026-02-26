package az.edu.ada.wm2.lab5.service;

import az.edu.ada.wm2.lab5.model.Event;
import az.edu.ada.wm2.lab5.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    @Autowired
    public EventServiceImpl(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public Event createEvent(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Event must not be null");
        }
        if (event.getId() == null) {
            event.setId(UUID.randomUUID());
        }
        return eventRepository.save(event);
    }

    @Override
    public Event getEventById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Id must not be null");
        }
        return eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));
    }

    @Override
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    @Override
    public Event updateEvent(UUID id, Event event) {
        if (id == null || event == null) {
            throw new IllegalArgumentException("Id and event must not be null");
        }
        if (!eventRepository.existsById(id)) {
            throw new RuntimeException("Event not found with id: " + id);
        }
        event.setId(id);
        return eventRepository.save(event);
    }

    @Override
    public void deleteEvent(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Id must not be null");
        }
        if (!eventRepository.existsById(id)) {
            throw new RuntimeException("Event not found with id: " + id);
        }
        eventRepository.deleteById(id);
    }

    @Override
    public Event partialUpdateEvent(UUID id, Event partialEvent) {
        if (id == null || partialEvent == null) {
            throw new IllegalArgumentException("Id and partialEvent must not be null");
        }

        Event existingEvent = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));

        if (partialEvent.getEventName() != null) {
            existingEvent.setEventName(partialEvent.getEventName());
        }
        if (partialEvent.getTags() != null && !partialEvent.getTags().isEmpty()) {
            existingEvent.setTags(partialEvent.getTags());
        }
        if (partialEvent.getTicketPrice() != null) {
            existingEvent.setTicketPrice(partialEvent.getTicketPrice());
        }
        if (partialEvent.getEventDateTime() != null) {
            existingEvent.setEventDateTime(partialEvent.getEventDateTime());
        }
        if (partialEvent.getDurationMinutes() > 0) {
            existingEvent.setDurationMinutes(partialEvent.getDurationMinutes());
        }

        return eventRepository.save(existingEvent);
    }

    // ===================== Q1 CUSTOM METHODS =====================

    @Override
    public List<Event> getEventsByTag(String tag) {
        if (tag == null || tag.isBlank()) {
            throw new IllegalArgumentException("Tag must not be null or blank");
        }

        String normalized = tag.trim().toLowerCase();

        return eventRepository.findAll().stream()
                .filter(e -> e.getTags() != null)
                .filter(e -> e.getTags().stream()
                        .filter(t -> t != null && !t.isBlank())
                        .map(t -> t.trim().toLowerCase())
                        .anyMatch(t -> t.equals(normalized)))
                .collect(Collectors.toList());
    }

    @Override
    public List<Event> getUpcomingEvents() {
        LocalDateTime now = LocalDateTime.now();

        return eventRepository.findAll().stream()
                .filter(e -> e.getEventDateTime() != null)
                .filter(e -> e.getEventDateTime().isAfter(now))
                .collect(Collectors.toList());
    }

    @Override
    public List<Event> getEventsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice == null || maxPrice == null) {
            throw new IllegalArgumentException("minPrice and maxPrice must not be null");
        }
        if (minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("minPrice must be <= maxPrice");
        }

        return eventRepository.findAll().stream()
                .filter(e -> e.getTicketPrice() != null)
                .filter(e -> e.getTicketPrice().compareTo(minPrice) >= 0
                        && e.getTicketPrice().compareTo(maxPrice) <= 0)
                .collect(Collectors.toList());
    }

    @Override
    public List<Event> getEventsByDateRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("start and end must not be null");
        }
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("start must be <= end");
        }

        return eventRepository.findAll().stream()
                .filter(e -> e.getEventDateTime() != null)
                .filter(e -> !e.getEventDateTime().isBefore(start)
                        && !e.getEventDateTime().isAfter(end))
                .collect(Collectors.toList());
    }

    @Override
    public Event updateEventPrice(UUID id, BigDecimal newPrice) {
        if (id == null) {
            throw new IllegalArgumentException("Id must not be null");
        }
        if (newPrice == null) {
            throw new IllegalArgumentException("New price must not be null");
        }
        if (newPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("New price must be >= 0");
        }

        Event existingEvent = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));

        existingEvent.setTicketPrice(newPrice);
        return eventRepository.save(existingEvent);
    }
}