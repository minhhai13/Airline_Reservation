// ========================================
// RouteDAO
// ========================================
package com.airline.dao;

import com.airline.entity.Route;
import java.util.List;
import java.util.Optional;

public interface RouteDAO {

    Route save(Route route);

    Optional<Route> findById(Long id);

    Optional<Route> findByOriginAndDestination(String origin, String destination);

    List<Route> findAll();

    void delete(Route route);
}
