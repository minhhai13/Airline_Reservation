// ========================================
// AircraftDAO
// ========================================
package com.airline.dao;

import com.airline.entity.Aircraft;
import java.util.List;
import java.util.Optional;

public interface AircraftDAO {

    Aircraft save(Aircraft aircraft);

    Optional<Aircraft> findById(Long id);

    List<Aircraft> findAll();

    void delete(Aircraft aircraft);
}
