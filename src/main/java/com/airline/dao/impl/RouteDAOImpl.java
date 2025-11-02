package com.airline.dao.impl;

import com.airline.dao.RouteDAO;
import com.airline.entity.Route;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class RouteDAOImpl implements RouteDAO {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Route save(Route route) {
        if (route.getId() == null) {
            em.persist(route);
            return route;
        } else {
            return em.merge(route);
        }
    }

    @Override
    public Optional<Route> findById(Long id) {
        return Optional.ofNullable(em.find(Route.class, id));
    }

    @Override
    public Optional<Route> findByOriginAndDestination(String origin, String destination) {
        try {
            Route route = em.createQuery(
                    "SELECT r FROM Route r WHERE r.origin = :origin AND r.destination = :destination",
                    Route.class)
                    .setParameter("origin", origin)
                    .setParameter("destination", destination)
                    .getSingleResult();
            return Optional.of(route);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Route> findAll() {
        return em.createQuery("SELECT r FROM Route r ORDER BY r.origin", Route.class)
                .getResultList();
    }

    @Override
    public void delete(Route route) {
        if (em.contains(route)) {
            em.remove(route);
        } else {
            em.remove(em.merge(route));
        }
    }
}
