package com.airline.dao.impl;

import com.airline.dao.PaymentDAO;
import com.airline.entity.Payment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class PaymentDAOImpl implements PaymentDAO {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Payment save(Payment payment) {
        if (payment.getId() == null) {
            em.persist(payment);
            return payment;
        } else {
            return em.merge(payment);
        }
    }

    @Override
    public Optional<Payment> findById(Long id) {
        return Optional.ofNullable(em.find(Payment.class, id));
    }

    @Override
    public Optional<Payment> findByTransactionId(String transactionId) {
        try {
            Payment payment = em.createQuery(
                    "SELECT p FROM Payment p WHERE p.transactionId = :transactionId", Payment.class)
                    .setParameter("transactionId", transactionId)
                    .getSingleResult();
            return Optional.of(payment);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Payment> findByBookingId(Long bookingId) {
        return em.createQuery(
                "SELECT p FROM Payment p WHERE p.booking.id = :bookingId ORDER BY p.paymentDate DESC",
                Payment.class)
                .setParameter("bookingId", bookingId)
                .getResultList();
    }

    @Override
    public List<Payment> findAll() {
        return em.createQuery("SELECT p FROM Payment p ORDER BY p.paymentDate DESC", Payment.class)
                .getResultList();
    }

    @Override
    public void delete(Payment payment) {
        if (em.contains(payment)) {
            em.remove(payment);
        } else {
            em.remove(em.merge(payment));
        }
    }
}
