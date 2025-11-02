package com.airline.dao.impl;

import com.airline.dao.AircraftDAO;
import com.airline.entity.Aircraft;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class AircraftDAOImpl implements AircraftDAO {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Aircraft save(Aircraft aircraft) {
        if (aircraft.getId() == null) {
            em.persist(aircraft);
            return aircraft;
        } else {
            return em.merge(aircraft);
        }
    }

    @Override
    public Optional<Aircraft> findById(Long id) {
        return Optional.ofNullable(em.find(Aircraft.class, id));
    }

    @Override
    public List<Aircraft> findAll() {
        return em.createQuery("SELECT a FROM Aircraft a ORDER BY a.modelName", Aircraft.class)
                .getResultList();
    }

    @Override
    public void delete(Aircraft aircraft) {
        if (em.contains(aircraft)) {
            em.remove(aircraft);
        } else {
            em.remove(em.merge(aircraft));
        }
    }
}
