// ========================================
// IMPLEMENTATION
// ========================================
package com.airline.dao.impl;

import com.airline.dao.FlightDAO;
import com.airline.entity.Flight;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * FlightDAOImpl
 */
@Repository
public class FlightDAOImpl implements FlightDAO {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Flight save(Flight flight) {
        if (flight.getId() == null) {
            em.persist(flight);
            return flight;
        } else {
            return em.merge(flight);
        }
    }

    @Override
    public Optional<Flight> findById(Long id) {
        return Optional.ofNullable(em.find(Flight.class, id));
    }

    @Override
    public Optional<Flight> findByFlightNumber(String flightNumber) {
        try {
            Flight flight = em.createQuery(
                    "SELECT f FROM Flight f WHERE f.flightNumber = :flightNumber", Flight.class)
                    .setParameter("flightNumber", flightNumber)
                    .getSingleResult();
            return Optional.of(flight);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Flight> findAll() {
        return em.createQuery(
                "SELECT f FROM Flight f ORDER BY f.departureTime", Flight.class)
                .getResultList();
    }

    @Override
    public List<Flight> findByRouteId(Long routeId) {
        return em.createQuery(
                "SELECT f FROM Flight f WHERE f.route.id = :routeId "
                + "ORDER BY f.departureTime", Flight.class)
                .setParameter("routeId", routeId)
                .getResultList();
    }

    @Override
    public List<Flight> findByRouteAndDate(Long routeId, LocalDateTime date) {
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        return em.createQuery(
                "SELECT f FROM Flight f WHERE f.route.id = :routeId "
                + "AND f.departureTime >= :start AND f.departureTime < :end "
                + "ORDER BY f.departureTime", Flight.class)
                .setParameter("routeId", routeId)
                .setParameter("start", startOfDay)
                .setParameter("end", endOfDay)
                .getResultList();
    }

    @Override
    public List<Flight> findAvailableFlights() {
        return em.createQuery(
                "SELECT f FROM Flight f WHERE f.availableSeats > 0 "
                + "AND f.departureTime > :now ORDER BY f.departureTime", Flight.class)
                .setParameter("now", LocalDateTime.now())
                .getResultList();
    }

    @Override
    public void delete(Flight flight) {
        if (em.contains(flight)) {
            em.remove(flight);
        } else {
            em.remove(em.merge(flight));
        }
    }
}
