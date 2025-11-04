// ========================================
// IMPLEMENTATION
// ========================================
package com.airline.dao.impl;

import com.airline.dao.BookingDAO;
import com.airline.entity.Booking;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * BookingDAOImpl
 */
@Repository
public class BookingDAOImpl implements BookingDAO {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Booking save(Booking booking) {
        if (booking.getId() == null) {
            em.persist(booking);
            return booking;
        } else {
            return em.merge(booking);
        }
    }

    @Override
    public Optional<Booking> findById(Long id) {
        // Eager fetch passengers and flight details
        try {
            Booking booking = em.createQuery(
                    "SELECT b FROM Booking b "
                    + "LEFT JOIN FETCH b.passengers "
                    + "LEFT JOIN FETCH b.flight f "
                    + "LEFT JOIN FETCH f.route "
                    + "WHERE b.id = :id", Booking.class)
                    .setParameter("id", id)
                    .getSingleResult();
            return Optional.of(booking);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Booking> findAll() {
        return em.createQuery(
                "SELECT DISTINCT b FROM Booking b "
                + "LEFT JOIN FETCH b.passengers "
                + "JOIN FETCH b.user " // <-- Thêm dòng này
                + "JOIN FETCH b.flight f " // <-- Thêm dòng này
                + "JOIN FETCH f.route " // <-- Thêm dòng này (vì trang booking cũng hiển thị route)
                + "ORDER BY b.bookingDate DESC", Booking.class)
                .getResultList();
    }

    @Override
    public List<Booking> findByUserId(Long userId) {
        return em.createQuery(
                "SELECT DISTINCT b FROM Booking b "
                + "LEFT JOIN FETCH b.passengers "
                + "LEFT JOIN FETCH b.flight f "
                + "LEFT JOIN FETCH f.route "
                + "WHERE b.user.id = :userId "
                + "ORDER BY b.bookingDate DESC", Booking.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    @Override
    public List<Booking> findByFlightId(Long flightId) {
        return em.createQuery(
                "SELECT DISTINCT b FROM Booking b "
                + "LEFT JOIN FETCH b.passengers "
                + "WHERE b.flight.id = :flightId "
                + "ORDER BY b.bookingDate DESC", Booking.class)
                .setParameter("flightId", flightId)
                .getResultList();
    }

    @Override
    public List<Booking> findByStatus(Booking.BookingStatus status) {
        return em.createQuery(
                "SELECT DISTINCT b FROM Booking b "
                + "LEFT JOIN FETCH b.passengers "
                + "WHERE b.status = :status "
                + "ORDER BY b.bookingDate DESC", Booking.class)
                .setParameter("status", status)
                .getResultList();
    }

    @Override
    public void delete(Booking booking) {
        if (em.contains(booking)) {
            em.remove(booking);
        } else {
            em.remove(em.merge(booking));
        }
    }
}
