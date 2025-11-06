package com.airline.dao.impl;

import com.airline.dao.BookingPassengerDAO;
import com.airline.entity.BookingPassenger;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import java.util.List;

import java.util.Optional;

@Repository
public class BookingPassengerDAOImpl implements BookingPassengerDAO {

    @PersistenceContext
    private EntityManager em;

    @Override
    public BookingPassenger save(BookingPassenger passenger) {
        if (passenger.getId() == null) {
            em.persist(passenger);
            return passenger;
        } else {
            return em.merge(passenger);
        }
    }

    @Override
    public Optional<BookingPassenger> findById(Long id) {
        return Optional.ofNullable(em.find(BookingPassenger.class, id));
    }

    @Override
    public List<BookingPassenger> findByBookingId(Long bookingId) {
        return em.createQuery(
                "SELECT bp FROM BookingPassenger bp WHERE bp.booking.id = :bookingId",
                BookingPassenger.class)
                .setParameter("bookingId", bookingId)
                .getResultList();
    }

    @Override
    public void delete(BookingPassenger passenger) {
        if (em.contains(passenger)) {
            em.remove(passenger);
        } else {
            em.remove(em.merge(passenger));
        }
    }
}
